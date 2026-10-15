package com.example.data.service

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.net.Uri
import android.text.TextPaint
import com.example.core.typography.BidiUtils
import com.example.core.typography.DocumentFontManager
import com.example.data.model.TextAlignment

/**
 * Renders Table and Image blocks onto PDF Canvas pages with high-fidelity formatting.
 */
object PdfTableAndImageRenderer {

    fun renderTableItem(
        context: Context?,
        canvas: Canvas,
        item: com.example.core.layout.TablePageItem,
        cursorY: Float,
        leftMargin: Float,
        rightMargin: Float,
        pageWidth: Float,
        contentWidth: Float,
        textPaint: TextPaint
    ): Float {
        var curY = cursorY
        val block = item.originalBlock
        val rowHeight = 22f
        val effectiveTableWidth = contentWidth * (block.tableWidthPercent.coerceIn(20, 100) / 100f)
        val tableStartX = when (block.alignment) {
            TextAlignment.START -> leftMargin
            TextAlignment.END -> pageWidth - rightMargin - effectiveTableWidth
            else -> leftMargin + (contentWidth - effectiveTableWidth) / 2f
        }
        val colWeights = if (block.columnWidthWeights.size == block.columnCount && block.columnWidthWeights.sum() > 0f) {
            val sum = block.columnWidthWeights.sum()
            block.columnWidthWeights.map { it / sum }
        } else {
            List(block.columnCount) { 1f / block.columnCount.toFloat() }
        }
        val tableBorderColor = try { Color.parseColor(block.borderColorHex) } catch (_: Exception) { Color.rgb(203, 213, 225) }
        val tablePaint = Paint().apply { color = tableBorderColor; style = Paint.Style.STROKE; strokeWidth = 1f }
        val cellFillPaint = Paint().apply { style = Paint.Style.FILL }

        for (r in item.startRow until item.endRow) {
            var currentCellX = tableStartX
            for (c in 0 until block.columnCount) {
                val cellColWidth = effectiveTableWidth * colWeights.getOrElse(c) { 1f / block.columnCount }
                val cellX = currentCellX
                currentCellX += cellColWidth
                val cell = block.cells.getOrNull(r)?.getOrNull(c)

                if (cell?.backgroundColorHex != null) {
                    try {
                        cellFillPaint.color = Color.parseColor(cell.backgroundColorHex)
                        canvas.drawRect(cellX, curY, cellX + cellColWidth, curY + rowHeight, cellFillPaint)
                    } catch (_: Exception) {}
                } else if (r == 0) {
                    cellFillPaint.color = Color.rgb(241, 245, 249)
                    canvas.drawRect(cellX, curY, cellX + cellColWidth, curY + rowHeight, cellFillPaint)
                }

                if (block.hasBorders) {
                    canvas.drawRect(cellX, curY, cellX + cellColWidth, curY + rowHeight, tablePaint)
                }

                val cellText = cell?.text ?: ""
                if (cellText.isNotBlank()) {
                    val isCellRtl = BidiUtils.detectParagraphIsRtl(cellText)
                    textPaint.textSize = 9.5f
                    val isBold = cell?.isBold ?: (r == 0)
                    val isItalic = cell?.isItalic ?: false
                    textPaint.typeface = DocumentFontManager.getTypeface(context, null, isCellRtl, isBold, isItalic)
                    textPaint.color = Color.rgb(30, 30, 30)

                    val cellMeasure = textPaint.measureText(cellText)
                    val cellAlign = cell?.alignment ?: TextAlignment.START
                    val textX = when (cellAlign) {
                        TextAlignment.CENTER -> cellX + (cellColWidth - cellMeasure) / 2f
                        TextAlignment.END -> cellX + cellColWidth - cellMeasure - 4f
                        TextAlignment.START -> if (isCellRtl) cellX + cellColWidth - cellMeasure - 4f else cellX + 4f
                        TextAlignment.JUSTIFY -> cellX + 4f
                    }
                    canvas.drawText(cellText, textX.coerceIn(cellX + 2f, (cellX + cellColWidth - cellMeasure).coerceAtLeast(cellX + 2f)), curY + 15f, textPaint)
                }
            }
            curY += rowHeight
        }
        return curY + 8f
    }

    fun renderImageItem(
        context: Context?,
        canvas: Canvas,
        item: com.example.core.layout.ImagePageItem,
        cursorY: Float,
        leftMargin: Float,
        rightMargin: Float,
        pageWidth: Float,
        contentWidth: Float,
        dividerPaint: Paint,
        headerFooterPaint: Paint
    ): Float {
        val block = item.originalBlock
        val imgWidth = (block.widthDp * 72f / 160f).coerceIn(40f, contentWidth)
        val imgHeight = (block.heightDp * 72f / 160f).coerceIn(40f, contentWidth)
        val imgX = when (block.alignment) {
            TextAlignment.START -> leftMargin
            TextAlignment.END -> pageWidth - rightMargin - imgWidth
            else -> leftMargin + (contentWidth - imgWidth) / 2f
        }

        val bitmap = tryLoadBitmapForPdf(context, block.imageUri)
        if (bitmap != null) {
            val srcRect = android.graphics.Rect(0, 0, bitmap.width, bitmap.height)
            val dstRect = android.graphics.RectF(imgX, cursorY, imgX + imgWidth, cursorY + imgHeight)
            canvas.drawBitmap(bitmap, srcRect, dstRect, null)
        } else {
            val imgBoxPaint = Paint().apply { color = Color.rgb(240, 244, 248); style = Paint.Style.FILL }
            canvas.drawRect(imgX, cursorY, imgX + imgWidth, cursorY + imgHeight, imgBoxPaint)
            canvas.drawRect(imgX, cursorY, imgX + imgWidth, cursorY + imgHeight, dividerPaint)
            headerFooterPaint.textSize = 8.5f
            canvas.drawText(block.caption.ifBlank { "Image Attachment" }, imgX + 8f, cursorY + imgHeight / 2f, headerFooterPaint)
        }

        if (block.caption.isNotBlank()) {
            headerFooterPaint.textSize = 8.5f
            canvas.drawText(block.caption, imgX + 4f, cursorY + imgHeight + 11f, headerFooterPaint)
        }
        return cursorY + imgHeight + 16f
    }

    fun tryLoadBitmapForPdf(context: Context?, uriStr: String): android.graphics.Bitmap? {
        if (uriStr.isBlank()) return null
        return try {
            if (context != null && uriStr.startsWith("content://")) {
                context.contentResolver.openInputStream(Uri.parse(uriStr))?.use { android.graphics.BitmapFactory.decodeStream(it) }
            } else if (uriStr.startsWith("file://")) {
                android.graphics.BitmapFactory.decodeFile(Uri.parse(uriStr).path ?: "")
            } else if (context != null && (uriStr.startsWith("android.resource://") || uriStr.startsWith("res://"))) {
                context.contentResolver.openInputStream(Uri.parse(uriStr))?.use { android.graphics.BitmapFactory.decodeStream(it) }
            } else {
                android.graphics.BitmapFactory.decodeFile(uriStr)
            }
        } catch (_: Throwable) {
            null
        }
    }
}
