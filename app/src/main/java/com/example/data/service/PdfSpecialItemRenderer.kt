package com.example.data.service

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import com.example.core.layout.PaginationEngine
import com.example.data.model.HeadingStyle
import com.example.data.model.ParagraphBlock
import com.example.data.model.RichDocument
import com.example.data.model.TextAlignment

/**
 * Renders special document elements for PDF pages: Table of Contents, Digital Signatures,
 * Hand drawings, Text boxes, and Shapes.
 */
object PdfSpecialItemRenderer {

    fun renderTocItem(
        canvas: Canvas,
        item: com.example.core.layout.TableOfContentsPageItem,
        document: RichDocument,
        cursorY: Float,
        leftMargin: Float,
        rightMargin: Float,
        pageWidth: Float
    ): Float {
        var curY = cursorY
        val tocPaint = Paint().apply { isAntiAlias = true; typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD); textSize = 14f; color = Color.rgb(30, 41, 59) }
        canvas.drawText(item.originalBlock.title, leftMargin, curY + 12f, tocPaint)
        curY += 22f

        val entryPaint = Paint().apply { isAntiAlias = true; textSize = 10.5f; color = Color.rgb(51, 65, 85) }
        val leaderPaint = Paint().apply { isAntiAlias = true; textSize = 10f; color = Color.rgb(148, 163, 184) }

        val entries = if (item.resolvedEntries.isNotEmpty()) item.resolvedEntries else {
            val headingPageMap = try { PaginationEngine.computeHeadingPageMap(document) } catch (_: Exception) { emptyMap<String, Int>() }
            document.blocks.filterIsInstance<ParagraphBlock>().filter { it.headingStyle != HeadingStyle.NORMAL && it.text.isNotBlank() }
                .map { com.example.core.layout.TocHeadingEntry(it.id, it.text.trim(), it.headingStyle, headingPageMap[it.id] ?: 1) }
        }

        var headingCount = 0
        for (entry in entries) {
            if (headingCount >= 10) break
            val indent = when (entry.headingStyle) {
                HeadingStyle.TITLE, HeadingStyle.HEADING_1 -> 0f
                HeadingStyle.HEADING_2 -> 14f
                else -> 24f
            }
            val titleText = entry.title.take(45)
            canvas.drawText(titleText, leftMargin + indent, curY + 10f, entryPaint)
            val textWidth = entryPaint.measureText(titleText)
            val pageNumStr = entry.pageNumber.toString()
            val pageNumWidth = entryPaint.measureText(pageNumStr)
            val leaderStartX = leftMargin + indent + textWidth + 8f
            val leaderEndX = pageWidth - rightMargin - pageNumWidth - 8f
            if (leaderEndX > leaderStartX) {
                canvas.drawText(". . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . .", leaderStartX, curY + 10f, leaderPaint)
            }
            canvas.drawText(pageNumStr, pageWidth - rightMargin - pageNumWidth, curY + 10f, entryPaint)
            curY += 18f
            headingCount++
        }
        if (headingCount == 0) {
            entryPaint.color = Color.GRAY
            canvas.drawText("No headings found (Add Heading 1, 2, or 3 to populate)", leftMargin, curY + 10f, entryPaint)
            curY += 20f
        }
        return curY + 10f
    }

    fun renderSignatureItem(
        canvas: Canvas,
        item: com.example.core.layout.SignaturePageItem,
        cursorY: Float,
        leftMargin: Float,
        rightMargin: Float,
        pageWidth: Float,
        contentWidth: Float
    ): Float {
        val sig = item.originalBlock
        val sigBoxWidth = 200f
        val sigBoxHeight = 45f
        val sigStartX = when (sig.alignment) {
            TextAlignment.END -> pageWidth - rightMargin - sigBoxWidth
            TextAlignment.CENTER -> leftMargin + (contentWidth - sigBoxWidth) / 2f
            else -> leftMargin
        }
        val inkColor = try { Color.parseColor(sig.strokeColorHex) } catch (_: Exception) { Color.rgb(15, 23, 42) }
        val badgePaint = Paint().apply { isAntiAlias = true; color = Color.rgb(30, 64, 175); textSize = 7.5f; typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD) }
        canvas.drawText("✓ VERIFIED DIGITAL SIGNATURE", sigStartX, cursorY + 10f, badgePaint)

        val decodedStrokes = com.example.core.typography.SignatureUtils.decodeNormalizedStrokes(sig.svgPoints, sigBoxWidth, sigBoxHeight)
        if (decodedStrokes.isNotEmpty()) {
            val strokePaint = Paint().apply { isAntiAlias = true; color = inkColor; style = Paint.Style.STROKE; strokeWidth = 2.0f; strokeCap = Paint.Cap.ROUND; strokeJoin = Paint.Join.ROUND }
            for (stroke in decodedStrokes) {
                if (stroke.size > 1) {
                    val path = android.graphics.Path()
                    path.moveTo(sigStartX + stroke.first().first, cursorY + 15f + stroke.first().second)
                    for (i in 1 until stroke.size) path.lineTo(sigStartX + stroke[i].first, cursorY + 15f + stroke[i].second)
                    canvas.drawPath(path, strokePaint)
                }
            }
        } else {
            val penPaint = Paint().apply { isAntiAlias = true; color = inkColor; textSize = 18f; typeface = Typeface.create(Typeface.SERIF, Typeface.ITALIC) }
            canvas.drawText(sig.signerName.ifBlank { "Authorized Signature" }, sigStartX + 8f, cursorY + 45f, penPaint)
        }

        val linePaint = Paint().apply { color = inkColor; strokeWidth = 1.2f }
        canvas.drawLine(sigStartX, cursorY + 65f, sigStartX + sigBoxWidth, cursorY + 65f, linePaint)
        val infoPaint = Paint().apply { isAntiAlias = true; color = Color.rgb(71, 85, 105); textSize = 8.5f }
        canvas.drawText(sig.signerTitle, sigStartX, cursorY + 77f, infoPaint)
        if (sig.signedDate.isNotBlank()) canvas.drawText("Date: ${sig.signedDate}", sigStartX, cursorY + 88f, infoPaint)
        return cursorY + 98f
    }

    fun renderDrawingItem(
        canvas: Canvas,
        item: com.example.core.layout.DrawingPageItem,
        cursorY: Float,
        leftMargin: Float,
        contentWidth: Float
    ): Float {
        val drawBlock = item.originalBlock
        val drawHeight = (drawBlock.heightDp * 72f / 160f).coerceIn(60f, 220f)
        if (drawBlock.backgroundColorHex != null) {
            val bgP = Paint().apply { color = try { Color.parseColor(drawBlock.backgroundColorHex) } catch (_: Exception) { Color.WHITE } }
            canvas.drawRect(leftMargin, cursorY, leftMargin + contentWidth, cursorY + drawHeight, bgP)
        }
        for (stroke in drawBlock.strokes) {
            if (stroke.points.size > 1) {
                val sPaint = Paint().apply {
                    isAntiAlias = true
                    color = try { Color.parseColor(stroke.colorHex) } catch (_: Exception) { Color.BLACK }
                    if (stroke.isHighlighter) alpha = 110
                    style = Paint.Style.STROKE; strokeWidth = stroke.strokeWidthPt; strokeCap = Paint.Cap.ROUND; strokeJoin = Paint.Join.ROUND
                }
                val path = android.graphics.Path()
                val p0 = stroke.points.first()
                path.moveTo(leftMargin + p0.first, cursorY + p0.second)
                for (i in 1 until stroke.points.size) path.lineTo(leftMargin + stroke.points[i].first, cursorY + stroke.points[i].second)
                canvas.drawPath(path, sPaint)
            }
        }
        return cursorY + drawHeight + 12f
    }

    fun renderTextBoxItem(
        canvas: Canvas,
        item: com.example.core.layout.TextBoxPageItem,
        cursorY: Float,
        leftMargin: Float,
        contentWidth: Float
    ): Float {
        val tb = item.originalBlock
        val tbW = (tb.widthDp * 72f / 160f).coerceIn(80f, contentWidth)
        val tbH = (tb.heightDp * 72f / 160f).coerceIn(40f, 160f)
        val tbRect = android.graphics.RectF(leftMargin, cursorY, leftMargin + tbW, cursorY + tbH)
        val bgP = Paint().apply { color = try { Color.parseColor(tb.backgroundColorHex) } catch (_: Exception) { Color.WHITE }; style = Paint.Style.FILL }
        val bdrP = Paint().apply { color = try { Color.parseColor(tb.borderColorHex) } catch (_: Exception) { Color.rgb(37, 99, 235) }; style = Paint.Style.STROKE; strokeWidth = tb.borderWidthPt }
        canvas.drawRoundRect(tbRect, 4f, 4f, bgP)
        canvas.drawRoundRect(tbRect, 4f, 4f, bdrP)
        val textP = Paint().apply { isAntiAlias = true; color = try { Color.parseColor(tb.textColorHex) } catch (_: Exception) { Color.BLACK }; textSize = 10f }
        canvas.drawText(tb.text.ifBlank { "..." }, leftMargin + 8f, cursorY + 22f, textP)
        return cursorY + tbH + 12f
    }

    fun renderShapeItem(
        canvas: Canvas,
        item: com.example.core.layout.ShapePageItem,
        cursorY: Float,
        leftMargin: Float,
        contentWidth: Float
    ): Float {
        return PdfShapeItemRenderer.renderShapeItem(canvas, item, cursorY, leftMargin, contentWidth)
    }
}
