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
     * Accurately lays out pages based on PageLayoutConfig (A4/Letter), margins,
     * headers, footers, alignments, and fonts.
     */
    private fun generatePdfDocument(document: RichDocument): PdfDocument {
        val pdfDocument = PdfDocument()
        val layout = document.layoutConfig
        val pageWidth = layout.pageWidthPt
        val pageHeight = layout.pageHeightPt

        val leftMargin = (layout.leftMarginMm / 25.4f * 72f)
        val rightMargin = (layout.rightMarginMm / 25.4f * 72f)
        val topMargin = (layout.topMarginMm / 25.4f * 72f)
        val bottomMargin = (layout.bottomMarginMm / 25.4f * 72f)
        val contentWidth = pageWidth - leftMargin - rightMargin

        // Setup paints
        val textPaint = Paint().apply {
            isAntiAlias = true
            color = Color.BLACK
            textSize = 12f
        }

        val headerFooterPaint = Paint().apply {
            isAntiAlias = true
            color = Color.DKGRAY
            textSize = 9f
        }

        val dividerPaint = Paint().apply {
            color = Color.LTGRAY
            strokeWidth = 1f
        }

        var currentPageNumber = 1
        var pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, currentPageNumber).create()
        var page = pdfDocument.startPage(pageInfo)
        var canvas = page.canvas

        var cursorY = topMargin + 20f

        // Draw header
        if (layout.headerText.isNotBlank()) {
            canvas.drawText(layout.headerText, leftMargin, topMargin - 10f, headerFooterPaint)
            canvas.drawLine(leftMargin, topMargin - 5f, pageWidth - rightMargin, topMargin - 5f, dividerPaint)
        }

        fun drawFooter(pageNum: Int) {
            val footerStr = layout.footerText
                .replace("{page}", pageNum.toString())
                .replace("{pages}", document.estimatedPageCount.toString())
            canvas.drawLine(leftMargin, pageHeight - bottomMargin + 10f, pageWidth - rightMargin, pageHeight - bottomMargin + 10f, dividerPaint)
            val measure = headerFooterPaint.measureText(footerStr)
            canvas.drawText(footerStr, (pageWidth - measure) / 2f, pageHeight - bottomMargin + 24f, headerFooterPaint)
        }

        // Draw document title if first page
        val titlePaint = Paint().apply {
            isAntiAlias = true
            color = Color.rgb(24, 90, 189) // Word Blue
            textSize = 22f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        canvas.drawText(document.title, leftMargin, cursorY, titlePaint)
        cursorY += 34f

        for (block in document.blocks) {
            when (block) {
                is ParagraphBlock -> {
                    // Check style
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

                    // Line wrap
                    val words = block.text.split(" ")
                    var line = ""
                    val lineHeight = pSize * block.lineSpacingMultiplier + 3f

                    for (word in words) {
                        val testLine = if (line.isEmpty()) word else "$line $word"
                        val testWidth = textPaint.measureText(testLine)

                        if (testWidth > contentWidth && line.isNotEmpty()) {
                            // Check page overflow
                            if (cursorY + lineHeight > pageHeight - bottomMargin - 30f) {
                                drawFooter(currentPageNumber)
                                pdfDocument.finishPage(page)
                                currentPageNumber++
                                pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, currentPageNumber).create()
                                page = pdfDocument.startPage(pageInfo)
                                canvas = page.canvas
                                cursorY = topMargin + 20f
                            }

                            // Align text
                            val startX = when (block.alignment) {
                                TextAlignment.CENTER -> leftMargin + (contentWidth - textPaint.measureText(line)) / 2f
                                TextAlignment.END -> pageWidth - rightMargin - textPaint.measureText(line)
                                else -> if (block.isRtl) pageWidth - rightMargin - textPaint.measureText(line) else leftMargin
                            }
                            canvas.drawText(line, startX, cursorY, textPaint)
                            cursorY += lineHeight
                            line = word
                        } else {
                            line = testLine
                        }
                    }

                    if (line.isNotEmpty()) {
                        if (cursorY + lineHeight > pageHeight - bottomMargin - 30f) {
                            drawFooter(currentPageNumber)
                            pdfDocument.finishPage(page)
                            currentPageNumber++
                            pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, currentPageNumber).create()
                            page = pdfDocument.startPage(pageInfo)
                            canvas = page.canvas
                            cursorY = topMargin + 20f
                        }
                        val startX = when (block.alignment) {
                            TextAlignment.CENTER -> leftMargin + (contentWidth - textPaint.measureText(line)) / 2f
                            TextAlignment.END -> pageWidth - rightMargin - textPaint.measureText(line)
                            else -> if (block.isRtl) pageWidth - rightMargin - textPaint.measureText(line) else leftMargin
                        }
                        canvas.drawText(line, startX, cursorY, textPaint)
                        cursorY += lineHeight + 4f
                    }
                }
                is DividerBlock -> {
                    if (cursorY + 15f > pageHeight - bottomMargin - 30f) {
                        drawFooter(currentPageNumber)
                        pdfDocument.finishPage(page)
                        currentPageNumber++
                        pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, currentPageNumber).create()
                        page = pdfDocument.startPage(pageInfo)
                        canvas = page.canvas
                        cursorY = topMargin + 20f
                    }
                    cursorY += 8f
                    canvas.drawLine(leftMargin, cursorY, pageWidth - rightMargin, cursorY, dividerPaint)
                    cursorY += 12f
                }
                is PageBreakBlock -> {
                    drawFooter(currentPageNumber)
                    pdfDocument.finishPage(page)
                    currentPageNumber++
                    pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, currentPageNumber).create()
                    page = pdfDocument.startPage(pageInfo)
                    canvas = page.canvas
                    cursorY = topMargin + 20f
                }
                is TableBlock -> {
                    // Render simple table outline
                    val rowHeight = 22f
                    val colWidth = contentWidth / block.columnCount.coerceAtLeast(1)
                    for (r in 0 until block.rowCount) {
                        if (cursorY + rowHeight > pageHeight - bottomMargin - 30f) {
                            drawFooter(currentPageNumber)
                            pdfDocument.finishPage(page)
                            currentPageNumber++
                            pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, currentPageNumber).create()
                            page = pdfDocument.startPage(pageInfo)
                            canvas = page.canvas
                            cursorY = topMargin + 20f
                        }
                        for (c in 0 until block.columnCount) {
                            val cellX = leftMargin + c * colWidth
                            canvas.drawRect(cellX, cursorY, cellX + colWidth, cursorY + rowHeight, dividerPaint)
                            val cellText = block.cells.getOrNull(r)?.getOrNull(c)?.text ?: ""
                            if (cellText.isNotBlank()) {
                                canvas.drawText(cellText, cellX + 4f, cursorY + 15f, textPaint)
                            }
                        }
                        cursorY += rowHeight
                    }
                    cursorY += 10f
                }
                else -> Unit
            }
        }

        drawFooter(currentPageNumber)
        pdfDocument.finishPage(page)
        return pdfDocument
    }
}
