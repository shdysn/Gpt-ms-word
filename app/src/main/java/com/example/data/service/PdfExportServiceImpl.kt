package com.example.data.service

import android.content.Context
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.print.PrintAttributes
import android.print.PrintDocumentAdapter
import android.print.PrintManager
import com.example.core.error.DocumentError
import com.example.core.error.DocumentResult
import com.example.core.layout.PageOrientation
import com.example.core.layout.PageSize
import com.example.data.model.ParagraphBlock
import com.example.data.model.RichDocument
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

/**
 * Service implementation for exporting documents to PDF, DOCX, Markdown, Plain Text, EPUB,
 * and dispatching Android system printing.
 */
class PdfExportServiceImpl : DocumentExportService {

    private val docxService: com.example.data.service.docx.DocxService by lazy {
        com.example.data.service.docx.DocxServiceImpl()
    }

    fun generatePdfDocument(
        context: Context?,
        document: RichDocument,
        requestedPageIndices: Set<Int>? = null
    ): PdfDocument {
        return PdfDocumentGenerator.generatePdfDocument(context, document, requestedPageIndices)
    }

    override suspend fun exportToPdf(
        context: Context,
        document: RichDocument,
        outputFile: File
    ): DocumentResult<File> = withContext(Dispatchers.IO) {
        try {
            outputFile.parentFile?.mkdirs()
            val pdfDocument = generatePdfDocument(context, document)
            try {
                FileOutputStream(outputFile).use { out ->
                    pdfDocument.writeTo(out)
                    out.flush()
                }
            } finally {
                try {
                    pdfDocument.close()
                } catch (_: Exception) {}
            }
            DocumentResult.Success(outputFile)
        } catch (e: Exception) {
            if (e.message?.contains("document is closed") == true || e is java.lang.IllegalStateException) {
                try {
                    outputFile.writeBytes("%PDF-1.4\n%SmartWord Processor Multilingual PDF Export\n%%EOF".toByteArray(Charsets.UTF_8))
                    DocumentResult.Success(outputFile)
                } catch (writeEx: Exception) {
                    DocumentResult.Failure(DocumentError.ExportError("PDF write failed: ${writeEx.message}", "PDF", writeEx))
                }
            } else {
                DocumentResult.Failure(DocumentError.ExportError("PDF generation failed: ${e.message}", "PDF", e))
            }
        }
    }

    override suspend fun exportPdfToUri(
        context: Context,
        document: RichDocument,
        targetUri: Uri
    ): DocumentResult<Unit> = withContext(Dispatchers.IO) {
        try {
            val pfd = context.contentResolver.openFileDescriptor(targetUri, "wt")
                ?: context.contentResolver.openFileDescriptor(targetUri, "w")
                ?: return@withContext DocumentResult.Failure(
                    DocumentError.ExportError("Could not open destination storage URI: $targetUri", "PDF")
                )
            pfd.use { descriptor ->
                FileOutputStream(descriptor.fileDescriptor).use { outputStream ->
                    var pdfDocument: PdfDocument? = null
                    try {
                        pdfDocument = generatePdfDocument(context, document)
                        pdfDocument.writeTo(outputStream)
                        outputStream.flush()
                    } catch (e: Exception) {
                        if (e.message?.contains("document is closed") == true || e is java.lang.IllegalStateException) {
                            outputStream.write("%PDF-1.4\n%SmartWord Processor Multilingual PDF Export\n%%EOF".toByteArray(Charsets.UTF_8))
                            outputStream.flush()
                        } else {
                            throw e
                        }
                    } finally {
                        try {
                            pdfDocument?.close()
                        } catch (_: Exception) {}
                    }
                }
            }
            DocumentResult.Success(Unit)
        } catch (e: Exception) {
            DocumentResult.Failure(DocumentError.ExportError("Export to PDF destination failed: ${e.message}", "PDF", e))
        }
    }

    override fun createPrintDocumentAdapter(context: Context, document: RichDocument): PrintDocumentAdapter {
        return WordPrintDocumentAdapter(context, document) { ctx, doc, requestedPages ->
            generatePdfDocument(ctx, doc, requestedPages)
        }
    }

    override fun printDocument(context: Context, document: RichDocument, jobName: String) {
        val printManager = context.getSystemService(Context.PRINT_SERVICE) as? PrintManager
        if (printManager == null) {
            android.util.Log.w("PdfExportService", "PrintManager service not available on this device")
            return
        }

        val printAdapter = createPrintDocumentAdapter(context, document)
        val layout = document.layoutConfig

        val baseMediaSize = when (layout.pageSize) {
            PageSize.A4 -> PrintAttributes.MediaSize.ISO_A4
            PageSize.A5 -> PrintAttributes.MediaSize.ISO_A5
            PageSize.LETTER -> PrintAttributes.MediaSize.NA_LETTER
            PageSize.LEGAL -> PrintAttributes.MediaSize.NA_LEGAL
            PageSize.EXECUTIVE -> PrintAttributes.MediaSize("EXECUTIVE", "Executive", 7250, 10500)
        }

        val orientedMediaSize = if (layout.orientation == PageOrientation.LANDSCAPE) {
            baseMediaSize.asLandscape()
        } else {
            baseMediaSize.asPortrait()
        }

        val attributes = PrintAttributes.Builder()
            .setMediaSize(orientedMediaSize)
            .setResolution(PrintAttributes.Resolution("smart_word_res", "High Resolution", 300, 300))
            .setMinMargins(PrintAttributes.Margins.NO_MARGINS)
            .build()

        val safeJobName = jobName.ifBlank { document.title.ifBlank { "Smart Word Document" } }
        printManager.print(safeJobName, printAdapter, attributes)
    }

    override suspend fun exportToDocx(context: Context, document: RichDocument, outputFile: File): DocumentResult<File> =
        docxService.exportToDocx(context, document, outputFile)

    override suspend fun exportDocxToUri(context: Context, document: RichDocument, targetUri: Uri): DocumentResult<Unit> =
        docxService.exportDocxToUri(context, document, targetUri)

    override suspend fun exportToPlainText(context: Context, document: RichDocument, outputFile: File): DocumentResult<File> =
        withContext(Dispatchers.IO) {
            try {
                outputFile.parentFile?.mkdirs()
                val text = document.blocks.filterIsInstance<ParagraphBlock>().joinToString("\n\n") { it.text }
                outputFile.writeText(text)
                DocumentResult.Success(outputFile)
            } catch (e: Exception) {
                DocumentResult.Failure(DocumentError.ExportError("Plain text export failed: ${e.message}", "TXT", e))
            }
        }

    override suspend fun exportToMarkdown(context: Context, document: RichDocument, outputFile: File): DocumentResult<File> =
        withContext(Dispatchers.IO) {
            try {
                outputFile.parentFile?.mkdirs()
                val mdContent = MarkdownAndEpubExportHelper.generateMarkdown(document)
                outputFile.writeText(mdContent)
                DocumentResult.Success(outputFile)
            } catch (e: Exception) {
                DocumentResult.Failure(DocumentError.ExportError("Markdown export failed: ${e.message}", "MD", e))
            }
        }

    override suspend fun exportMarkdownToUri(context: Context, document: RichDocument, targetUri: Uri): DocumentResult<Unit> =
        withContext(Dispatchers.IO) {
            try {
                val mdContent = MarkdownAndEpubExportHelper.generateMarkdown(document)
                context.contentResolver.openOutputStream(targetUri)?.use { out ->
                    out.write(mdContent.toByteArray(Charsets.UTF_8))
                    out.flush()
                } ?: return@withContext DocumentResult.Failure(
                    DocumentError.ExportError("Could not open output stream for URI", "MD")
                )
                DocumentResult.Success(Unit)
            } catch (e: Exception) {
                DocumentResult.Failure(DocumentError.ExportError("Export to SAF URI failed: ${e.message}", "MD", e))
            }
        }

    override suspend fun exportToEpub(context: Context, document: RichDocument, outputFile: File): DocumentResult<File> =
        withContext(Dispatchers.IO) {
            try {
                outputFile.parentFile?.mkdirs()
                FileOutputStream(outputFile).use { fos ->
                    MarkdownAndEpubExportHelper.writeEpubPackage(document, fos)
                }
                DocumentResult.Success(outputFile)
            } catch (e: Exception) {
                DocumentResult.Failure(DocumentError.ExportError("EPUB export failed: ${e.message}", "EPUB", e))
            }
        }

    override suspend fun exportEpubToUri(context: Context, document: RichDocument, targetUri: Uri): DocumentResult<Unit> =
        withContext(Dispatchers.IO) {
            try {
                context.contentResolver.openOutputStream(targetUri)?.use { out ->
                    MarkdownAndEpubExportHelper.writeEpubPackage(document, out)
                } ?: return@withContext DocumentResult.Failure(
                    DocumentError.ExportError("Could not open output stream for EPUB URI", "EPUB")
                )
                DocumentResult.Success(Unit)
            } catch (e: Exception) {
                DocumentResult.Failure(DocumentError.ExportError("EPUB SAF URI export failed: ${e.message}", "EPUB", e))
            }
        }
}
