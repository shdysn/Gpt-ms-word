package com.example.data.service

import android.content.Context
import android.graphics.Color
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.text.TextPaint
import com.example.core.layout.PageBorderStyle
import com.example.core.layout.PaginationEngine
import com.example.core.typography.BidiUtils
import com.example.core.typography.DocumentFontManager
import com.example.data.model.RichDocument
import com.example.data.model.TextAlignment

/**
 * Handles Canvas rendering of PDF pages, margins, borders, watermarks, headers, and footers.
 */
object PdfDocumentGenerator {

    fun generatePdfDocument(
        context: Context?,
        document: RichDocument,
        requestedPageIndices: Set<Int>? = null
    ): PdfDocument {
        val pdfDocument = PdfDocument()
        val layout = document.layoutConfig
        val pageWidth = layout.pageWidthPt.toFloat()
        val pageHeight = layout.pageHeightPt.toFloat()

        val leftMargin = layout.leftMarginPt
        val rightMargin = layout.rightMarginPt
        val topMargin = layout.topMarginPt
        val bottomMargin = layout.bottomMarginPt
        val contentWidth = layout.printableWidthPt

        val allPages = PaginationEngine.paginate(document)
        val pagesToRender = if (requestedPageIndices != null && requestedPageIndices.isNotEmpty()) {
            allPages.filter { it.pageIndex in requestedPageIndices }
        } else {
            allPages
        }

        val textPaint = TextPaint().apply {
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
            } catch (_: Exception) { Color.LTGRAY }
            strokeWidth = layout.borderStyle.strokeWidthPt
        }

        val parsedBgColor = try {
            Color.parseColor(layout.pageBackgroundHex)
        } catch (_: Exception) { Color.WHITE }

        for (docPage in pagesToRender) {
            val pageInfo = PdfDocument.PageInfo.Builder(pageWidth.toInt().coerceAtLeast(100), pageHeight.toInt().coerceAtLeast(100), docPage.pageNumber).create()
            val page = pdfDocument.startPage(pageInfo)
            val canvas = page.canvas

            // 1. Draw page background
            if (parsedBgColor != Color.WHITE) {
                canvas.drawColor(parsedBgColor)
            }

            // 2. Draw page border if configured
            if (layout.borderStyle != PageBorderStyle.NONE) {
                val borderInset = 16f
                when (layout.borderStyle) {
                    PageBorderStyle.DASHED -> {
                        borderPaint.pathEffect = DashPathEffect(floatArrayOf(6f, 4f), 0f)
                        canvas.drawRect(borderInset, borderInset, pageWidth - borderInset, pageHeight - borderInset, borderPaint)
                    }
                    PageBorderStyle.DOUBLE -> {
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

            // 2b. Draw watermark if configured
            if (layout.watermarkText.isNotBlank()) {
                val watermarkPaint = Paint().apply {
                    isAntiAlias = true
                    color = try { Color.parseColor(layout.watermarkColorHex) } catch (_: Exception) { Color.argb(40, 148, 163, 184) }
                    textSize = 52f
                    textAlign = Paint.Align.CENTER
                    typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                }
                canvas.save()
                canvas.translate(pageWidth / 2f, pageHeight / 2f)
                canvas.rotate(layout.watermarkAngleDeg)
                canvas.drawText(layout.watermarkText, 0f, 18f, watermarkPaint)
                canvas.restore()
            }

            // 3. Draw header (with safe vertical bounds)
            if (docPage.hasHeader && docPage.formattedHeader.isNotBlank()) {
                val headerRtl = BidiUtils.detectParagraphIsRtl(docPage.formattedHeader)
                headerFooterPaint.typeface = DocumentFontManager.getTypeface(context, null, headerRtl)
                val headerMeasure = headerFooterPaint.measureText(docPage.formattedHeader)
                val headerX = when (layout.headerAlignment) {
                    TextAlignment.CENTER -> leftMargin + (contentWidth - headerMeasure) / 2f
                    TextAlignment.END -> pageWidth - rightMargin - headerMeasure
                    else -> if (headerRtl) pageWidth - rightMargin - headerMeasure else leftMargin
                }
                val headerTextY = (topMargin - 10f).coerceIn(16f, topMargin - 4f)
                val headerLineY = (topMargin - 4f).coerceIn(18f, topMargin - 2f)
                canvas.drawText(docPage.formattedHeader, headerX, headerTextY, headerFooterPaint)
                canvas.drawLine(leftMargin, headerLineY, pageWidth - rightMargin, headerLineY, dividerPaint)
            }

            // 4. Draw footer with automatic page numbering
            if (docPage.hasFooter && docPage.formattedFooter.isNotBlank()) {
                val footerRtl = BidiUtils.detectParagraphIsRtl(docPage.formattedFooter)
                headerFooterPaint.typeface = DocumentFontManager.getTypeface(context, null, footerRtl)
                val footerLineY = (pageHeight - bottomMargin + 6f).coerceIn(pageHeight - bottomMargin + 2f, pageHeight - 20f)
                val footerTextY = (pageHeight - bottomMargin + 18f).coerceIn(footerLineY + 10f, pageHeight - 6f)
                canvas.drawLine(leftMargin, footerLineY, pageWidth - rightMargin, footerLineY, dividerPaint)
                val footerMeasure = headerFooterPaint.measureText(docPage.formattedFooter)
                val footerX = when (layout.footerAlignment) {
                    TextAlignment.START -> if (footerRtl) pageWidth - rightMargin - footerMeasure else leftMargin
                    TextAlignment.END -> if (footerRtl) leftMargin else pageWidth - rightMargin - footerMeasure
                    else -> leftMargin + (contentWidth - footerMeasure) / 2f
                }
                canvas.drawText(docPage.formattedFooter, footerX, footerTextY, headerFooterPaint)
            }

            // 5. Column divider
            if (layout.columnCount == 2) {
                val colDividerX = leftMargin + (contentWidth - 18f) / 2f + 9f
                canvas.drawLine(colDividerX, topMargin + 8f, colDividerX, pageHeight - bottomMargin - 8f, dividerPaint)
            }

            // 6. Draw page items using PdfPageItemRenderer
            var cursorY = topMargin + 14f
            for (item in docPage.items) {
                cursorY = PdfPageItemRenderer.renderItem(
                    context = context,
                    canvas = canvas,
                    item = item,
                    document = document,
                    cursorY = cursorY,
                    leftMargin = leftMargin,
                    rightMargin = rightMargin,
                    pageWidth = pageWidth,
                    contentWidth = contentWidth,
                    textPaint = textPaint,
                    dividerPaint = dividerPaint,
                    headerFooterPaint = headerFooterPaint
                )
            }

            pdfDocument.finishPage(page)
        }

        return pdfDocument
    }
}
