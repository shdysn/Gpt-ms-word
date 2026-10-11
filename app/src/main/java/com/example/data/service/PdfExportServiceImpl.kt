package com.example.data.service

import android.content.Context
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Bundle
import android.os.CancellationSignal
import android.os.ParcelFileDescriptor
import android.print.PageRange
import android.print.PrintAttributes
import android.print.PrintDocumentAdapter
import android.print.PrintDocumentInfo
import android.print.PrintManager
import com.example.core.error.DocumentError
import com.example.core.error.DocumentResult
import com.example.data.model.DividerBlock
import com.example.data.model.HeadingStyle
import com.example.data.model.PageBreakBlock
import com.example.data.model.ParagraphBlock
import com.example.data.model.RichDocument
import com.example.data.model.TableBlock
import com.example.data.model.TextAlignment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

class PdfExportServiceImpl : DocumentExportService {

    override suspend fun exportToPdf(
        context: Context,
        document: RichDocument,
        outputFile: File
    ): DocumentResult<File> = withContext(Dispatchers.IO) {
        try {
            outputFile.parentFile?.mkdirs()
            val pdfDocument = generatePdfDocument(document)
            FileOutputStream(outputFile).use { out ->
                pdfDocument.writeTo(out)
            }
            pdfDocument.close()
            DocumentResult.Success(outputFile)
        } catch (e: Exception) {
            DocumentResult.Failure(
                DocumentError.ExportError("PDF generation failed: ${e.message}", "PDF", e)
            )
        }
    }

    override suspend fun exportPdfToUri(
        context: Context,
        document: RichDocument,
        targetUri: Uri
    ): DocumentResult<Unit> = withContext(Dispatchers.IO) {
        try {
            val pfd = context.contentResolver.openFileDescriptor(targetUri, "w")
                ?: return@withContext DocumentResult.Failure(
                    DocumentError.ExportError("Could not open output stream for URI: $targetUri", "PDF")
                )
            pfd.use { descriptor ->
                FileOutputStream(descriptor.fileDescriptor).use { outputStream ->
                    val pdfDocument = generatePdfDocument(document)
                    pdfDocument.writeTo(outputStream)
                    pdfDocument.close()
                }
            }
            DocumentResult.Success(Unit)
        } catch (e: Exception) {
            DocumentResult.Failure(
                DocumentError.ExportError("Export to PDF URI failed: ${e.message}", "PDF", e)
            )
        }
    }

    override fun printDocument(context: Context, document: RichDocument, jobName: String) {
        val printManager = context.getSystemService(Context.PRINT_SERVICE) as? PrintManager ?: return
        val printAdapter = object : PrintDocumentAdapter() {
            private var tempPdfFile: File? = null

            override fun onLayout(
                oldAttributes: PrintAttributes?,
                newAttributes: PrintAttributes?,
                cancellationSignal: CancellationSignal?,
                callback: LayoutResultCallback?,
                extras: Bundle?
            ) {
                if (cancellationSignal?.isCanceled == true) {
                    callback?.onLayoutCancelled()
                    return
                }

                val info = PrintDocumentInfo.Builder("${document.title}.pdf")
                    .setContentType(PrintDocumentInfo.CONTENT_TYPE_DOCUMENT)
                    .setPageCount(document.estimatedPageCount)
                    .build()

                callback?.onLayoutFinished(info, true)
            }

            override fun onWrite(
                pages: Array<out PageRange>?,
                destination: ParcelFileDescriptor?,
                cancellationSignal: CancellationSignal?,
                callback: WriteResultCallback?
            ) {
                try {
                    val pdfDocument = generatePdfDocument(document)
                    destination?.fileDescriptor?.let { fd ->
                        FileOutputStream(fd).use { out ->
                            pdfDocument.writeTo(out)
                        }
                    }
                    pdfDocument.close()
                    callback?.onWriteFinished(arrayOf(PageRange.ALL_PAGES))
                } catch (e: Exception) {
                    callback?.onWriteFailed(e.message)
                }
            }
        }

        val attributes = PrintAttributes.Builder()
            .setMediaSize(PrintAttributes.MediaSize.ISO_A4)
            .setResolution(PrintAttributes.Resolution("smart_word_res", "High", 300, 300))
            .setMinMargins(PrintAttributes.Margins.NO_MARGINS)
            .build()

        printManager.print(jobName.ifBlank { "Smart Word Document" }, printAdapter, attributes)
    }

    override suspend fun exportToDocx(
        context: Context,
        document: RichDocument,
        outputFile: File
    ): DocumentResult<File> = withContext(Dispatchers.IO) {
        // Architecture Hook for Phase 2: OpenXML / Apache POI packaging
        // For Phase 1, generate a structured interoperable document package
        try {
            outputFile.parentFile?.mkdirs()
            // In Phase 1 foundation: write plain text representation or draft XML structure
            val textBuilder = StringBuilder()
            textBuilder.append("Smart Word Processor - Document Export\n")
            textBuilder.append("Title: ${document.title}\n")
            textBuilder.append("Author: ${document.author}\n")
            textBuilder.append("----------------------------------------\n\n")
            for (block in document.blocks) {
                when (block) {
                    is ParagraphBlock -> textBuilder.append(block.text).append("\n\n")
                    is TableBlock -> textBuilder.append("[Table ${block.rowCount}x${block.columnCount}]\n\n")
                    is DividerBlock -> textBuilder.append("----------------------------------------\n\n")
                    is PageBreakBlock -> textBuilder.append("=== PAGE BREAK ===\n\n")
                    else -> Unit
                }
            }
            outputFile.writeText(textBuilder.toString())
            DocumentResult.Success(outputFile)
        } catch (e: Exception) {
            DocumentResult.Failure(
                DocumentError.ExportError("DOCX foundation export failed: ${e.message}", "DOCX", e)
            )
        }
    }

    override suspend fun exportToPlainText(
        context: Context,
        document: RichDocument,
        outputFile: File
    ): DocumentResult<File> = withContext(Dispatchers.IO) {
        try {
            outputFile.parentFile?.mkdirs()
            val text = document.blocks.filterIsInstance<ParagraphBlock>()
                .joinToString("\n\n") { it.text }
            outputFile.writeText(text)
            DocumentResult.Success(outputFile)
        } catch (e: Exception) {
            DocumentResult.Failure(
                DocumentError.ExportError("Plain text export failed: ${e.message}", "TXT", e)
            )
        }
    }

    /**
     * Core PDF generation engine using Android's native PdfDocument.
     * Uses PaginationEngine to render each discrete page with exact layout dimensions,
     * margins, background color, page borders, headers, footers, and page numbers.
     */
    private fun generatePdfDocument(document: RichDocument): PdfDocument {
        val pdfDocument = PdfDocument()
        val layout = document.layoutConfig
        val pageWidth = layout.pageWidthPt
        val pageHeight = layout.pageHeightPt

        val leftMargin = layout.leftMarginPt
        val rightMargin = layout.rightMarginPt
        val topMargin = layout.topMarginPt
        val bottomMargin = layout.bottomMarginPt
        val contentWidth = layout.printableWidthPt

        val pages = com.example.core.layout.PaginationEngine.paginate(document)

        val textPaint = Paint().apply {
            isAntiAlias = true
            color = Color.BLACK
            textSize = 11f
        }

        val headerFooterPaint = Paint().apply {
            isAntiAlias = true
            color = Color.DKGRAY
            textSize = 9f
        }

        val dividerPaint = Paint().apply {
            color = Color.LTGRAY
            strokeWidth = 0.75f
        }

        val borderPaint = Paint().apply {
            isAntiAlias = true
            style = Paint.Style.STROKE
            color = try {
                Color.parseColor(layout.borderColorHex)
            } catch (e: Exception) { Color.LTGRAY }
            strokeWidth = layout.borderStyle.strokeWidthPt
        }

        val parsedBgColor = try {
            Color.parseColor(layout.pageBackgroundHex)
        } catch (e: Exception) { Color.WHITE }

        for (docPage in pages) {
            val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, docPage.pageNumber).create()
            val page = pdfDocument.startPage(pageInfo)
            val canvas = page.canvas

            // 1. Draw page background
            if (parsedBgColor != Color.WHITE) {
                canvas.drawColor(parsedBgColor)
            }

            // 2. Draw page border if configured
            if (layout.borderStyle != com.example.core.layout.PageBorderStyle.NONE) {
                val borderInset = 16f
                when (layout.borderStyle) {
                    com.example.core.layout.PageBorderStyle.DASHED -> {
                        borderPaint.pathEffect = android.graphics.DashPathEffect(floatArrayOf(6f, 4f), 0f)
                        canvas.drawRect(borderInset, borderInset, pageWidth - borderInset, pageHeight - borderInset, borderPaint)
                    }
                    com.example.core.layout.PageBorderStyle.DOUBLE -> {
                        borderPaint.pathEffect = null
                        borderPaint.strokeWidth = 1f
                        canvas.drawRect(borderInset, borderInset, pageWidth - borderInset, pageHeight - borderInset, borderPaint)
                        canvas.drawRect(borderInset + 3f, borderInset + 3f, pageWidth - borderInset - 3f, pageHeight - borderInset - 3f, borderPaint)
                    }
                    else -> {
                        borderPaint.pathEffect = null
                        borderPaint.strokeWidth = layout.borderStyle.strokeWidthPt
                        canvas.drawRect(borderInset, borderInset, pageWidth - borderInset, pageHeight - borderInset, borderPaint)
                    }
                }
            }

            // 3. Draw header
            if (docPage.hasHeader) {
                val headerMeasure = headerFooterPaint.measureText(docPage.formattedHeader)
                val headerX = when (layout.headerAlignment) {
                    TextAlignment.CENTER -> leftMargin + (contentWidth - headerMeasure) / 2f
                    TextAlignment.END -> pageWidth - rightMargin - headerMeasure
                    else -> leftMargin
                }
                canvas.drawText(docPage.formattedHeader, headerX, topMargin - 10f, headerFooterPaint)
                canvas.drawLine(leftMargin, topMargin - 5f, pageWidth - rightMargin, topMargin - 5f, dividerPaint)
            }

            // 4. Draw footer with automatic page numbering
            if (docPage.hasFooter) {
                canvas.drawLine(leftMargin, pageHeight - bottomMargin + 8f, pageWidth - rightMargin, pageHeight - bottomMargin + 8f, dividerPaint)
                val footerMeasure = headerFooterPaint.measureText(docPage.formattedFooter)
                val footerX = when (layout.footerAlignment) {
                    TextAlignment.START -> leftMargin
                    TextAlignment.END -> pageWidth - rightMargin - footerMeasure
                    else -> leftMargin + (contentWidth - footerMeasure) / 2f
                }
                canvas.drawText(docPage.formattedFooter, footerX, pageHeight - bottomMargin + 20f, headerFooterPaint)
            }

            // 5. Render page items
            var cursorY = topMargin + 14f

            for (item in docPage.items) {
                when (item) {
                    is com.example.core.layout.ParagraphPageItem -> {
                        val block = item.originalBlock
                        val isBold = block.headingStyle.isBold || block.spans.any { it.isBold }
                        val isItalic = block.spans.any { it.isItalic }
                        val typefaceStyle = when {
                            isBold && isItalic -> Typeface.BOLD_ITALIC
                            isBold -> Typeface.BOLD
                            isItalic -> Typeface.ITALIC
                            else -> Typeface.NORMAL
                        }

                        val pSize = when (block.headingStyle) {
                            HeadingStyle.TITLE -> 20f
                            HeadingStyle.HEADING_1 -> 16f
                            HeadingStyle.HEADING_2 -> 14f
                            HeadingStyle.HEADING_3 -> 12f
                            HeadingStyle.SUBTITLE -> 13f
                            HeadingStyle.QUOTE -> 11f
                            HeadingStyle.NORMAL -> 11f
                        }

                        textPaint.textSize = pSize
                        textPaint.typeface = Typeface.create(Typeface.DEFAULT, typefaceStyle)
                        textPaint.color = if (block.headingStyle == HeadingStyle.HEADING_1 || block.headingStyle == HeadingStyle.TITLE) {
                            Color.rgb(24, 90, 189)
                        } else Color.rgb(30, 30, 30)

                        val lineHeight = pSize * block.lineSpacingMultiplier + 2f
                        val lines = item.lineSliceText.lines()

                        for (line in lines) {
                            if (line.isNotEmpty()) {
                                val lineMeasure = textPaint.measureText(line)
                                val startX = when (block.alignment) {
                                    TextAlignment.CENTER -> leftMargin + (contentWidth - lineMeasure) / 2f
                                    TextAlignment.END -> pageWidth - rightMargin - lineMeasure
                                    else -> if (block.isRtl) pageWidth - rightMargin - lineMeasure else leftMargin
                                }
                                canvas.drawText(line, startX, cursorY, textPaint)
                            }
                            cursorY += lineHeight
                        }
                        cursorY += 4f
                    }

                    is com.example.core.layout.DividerPageItem -> {
                        cursorY += 4f
                        canvas.drawLine(leftMargin, cursorY, pageWidth - rightMargin, cursorY, dividerPaint)
                        cursorY += 12f
                    }

                    is com.example.core.layout.TablePageItem -> {
                        val block = item.originalBlock
                        val rowHeight = 20f
                        val colWidth = contentWidth / block.columnCount.coerceAtLeast(1)
                        val tablePaint = Paint().apply {
                            color = Color.rgb(220, 225, 230)
                            style = Paint.Style.STROKE
                            strokeWidth = 1f
                        }

                        for (r in item.startRow until item.endRow) {
                            for (c in 0 until block.columnCount) {
                                val cellX = leftMargin + c * colWidth
                                canvas.drawRect(cellX, cursorY, cellX + colWidth, cursorY + rowHeight, tablePaint)
                                val cellText = block.cells.getOrNull(r)?.getOrNull(c)?.text ?: ""
                                if (cellText.isNotBlank()) {
                                    textPaint.textSize = 9.5f
                                    textPaint.typeface = if (r == 0) Typeface.DEFAULT_BOLD else Typeface.DEFAULT
                                    textPaint.color = Color.BLACK
                                    canvas.drawText(cellText, cellX + 4f, cursorY + 14f, textPaint)
                                }
                            }
                            cursorY += rowHeight
                        }
                        cursorY += 8f
                    }

                    is com.example.core.layout.ImagePageItem -> {
                        val imgWidth = contentWidth * 0.7f
                        val imgHeight = (item.heightPt - 16f).coerceAtLeast(40f)
                        val imgX = leftMargin + (contentWidth - imgWidth) / 2f

                        // Draw placeholder rect for image block
                        val imgBoxPaint = Paint().apply {
                            color = Color.rgb(240, 244, 248)
                            style = Paint.Style.FILL
                        }
                        canvas.drawRect(imgX, cursorY, imgX + imgWidth, cursorY + imgHeight, imgBoxPaint)
                        canvas.drawRect(imgX, cursorY, imgX + imgWidth, cursorY + imgHeight, dividerPaint)

                        if (item.originalBlock.caption.isNotBlank()) {
                            headerFooterPaint.textSize = 8.5f
                            canvas.drawText(item.originalBlock.caption, imgX + 4f, cursorY + imgHeight + 11f, headerFooterPaint)
                        }
                        cursorY += item.heightPt
                    }

                    is com.example.core.layout.PageBreakPageItem -> {
                        // Handled at page boundaries
                    }
                }
            }

            pdfDocument.finishPage(page)
        }

        return pdfDocument
    }
}
