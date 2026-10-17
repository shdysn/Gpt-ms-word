package com.example.data.service

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Typeface
import com.example.core.layout.ShapePageItem

/**
 * Dedicated PDF renderer for vector and flowchart shapes, ensuring modularity
 * and adherence to the codebase line limit.
 */
object PdfShapeItemRenderer {

    fun renderShapeItem(
        canvas: Canvas,
        item: ShapePageItem,
        cursorY: Float,
        leftMargin: Float,
        contentWidth: Float
    ): Float {
        val shape = item.originalBlock
        val shapeW = (shape.widthDp * 72f / 160f).coerceIn(100f, contentWidth)
        val shapeH = (shape.heightDp * 72f / 160f).coerceIn(40f, 180f)
        val boxRect = RectF(leftMargin, cursorY, leftMargin + shapeW, cursorY + shapeH)
        val bgPaint = Paint().apply {
            color = try { Color.parseColor(shape.backgroundColorHex) } catch (_: Exception) { Color.rgb(239, 246, 255) }
            style = Paint.Style.FILL
        }
        val bdrPaint = Paint().apply {
            color = try { Color.parseColor(shape.borderColorHex) } catch (_: Exception) { Color.rgb(59, 130, 246) }
            style = Paint.Style.STROKE
            strokeWidth = shape.strokeWidthPt.coerceAtLeast(1f)
        }

        if (shape.rotationDeg != 0f) {
            canvas.save()
            canvas.rotate(shape.rotationDeg, boxRect.centerX(), boxRect.centerY())
        }

        when (shape.shapeType.uppercase()) {
            "OVAL" -> {
                canvas.drawOval(boxRect, bgPaint)
                canvas.drawOval(boxRect, bdrPaint)
            }
            "RECTANGLE" -> {
                canvas.drawRect(boxRect, bgPaint)
                canvas.drawRect(boxRect, bdrPaint)
            }
            "STAR" -> {
                val path = Path()
                val cx = boxRect.centerX()
                val cy = boxRect.centerY()
                val rOuter = minOf(shapeW, shapeH) / 2f
                val rInner = rOuter * 0.45f
                for (i in 0 until 10) {
                    val r = if (i % 2 == 0) rOuter else rInner
                    val angle = (i * 36 - 90) * Math.PI / 180.0
                    val px = (cx + r * Math.cos(angle)).toFloat()
                    val py = (cy + r * Math.sin(angle)).toFloat()
                    if (i == 0) path.moveTo(px, py) else path.lineTo(px, py)
                }
                path.close()
                canvas.drawPath(path, bgPaint)
                canvas.drawPath(path, bdrPaint)
            }
            "DIAMOND" -> {
                val path = Path()
                path.moveTo(boxRect.centerX(), boxRect.top)
                path.lineTo(boxRect.right, boxRect.centerY())
                path.lineTo(boxRect.centerX(), boxRect.bottom)
                path.lineTo(boxRect.left, boxRect.centerY())
                path.close()
                canvas.drawPath(path, bgPaint)
                canvas.drawPath(path, bdrPaint)
            }
            "ARROW_RIGHT" -> {
                val path = Path()
                val my = boxRect.centerY()
                val headW = shapeW * 0.35f
                val hH = shapeH * 0.25f
                path.moveTo(boxRect.left, my - hH)
                path.lineTo(boxRect.right - headW, my - hH)
                path.lineTo(boxRect.right - headW, boxRect.top)
                path.lineTo(boxRect.right, my)
                path.lineTo(boxRect.right - headW, boxRect.bottom)
                path.lineTo(boxRect.right - headW, my + hH)
                path.lineTo(boxRect.left, my + hH)
                path.close()
                canvas.drawPath(path, bgPaint)
                canvas.drawPath(path, bdrPaint)
            }
            "ARROW_LEFT" -> {
                val path = Path()
                val my = boxRect.centerY()
                val headW = shapeW * 0.35f
                val hH = shapeH * 0.25f
                path.moveTo(boxRect.right, my - hH)
                path.lineTo(boxRect.left + headW, my - hH)
                path.lineTo(boxRect.left + headW, boxRect.top)
                path.lineTo(boxRect.left, my)
                path.lineTo(boxRect.left + headW, boxRect.bottom)
                path.lineTo(boxRect.left + headW, my + hH)
                path.lineTo(boxRect.right, my + hH)
                path.close()
                canvas.drawPath(path, bgPaint)
                canvas.drawPath(path, bdrPaint)
            }
            "LINE" -> {
                canvas.drawLine(boxRect.left, boxRect.centerY(), boxRect.right, boxRect.centerY(), bdrPaint)
            }
            "ARROW_LINE" -> {
                canvas.drawLine(boxRect.left, boxRect.centerY(), boxRect.right, boxRect.centerY(), bdrPaint)
                val path = Path()
                path.moveTo(boxRect.right - 10f, boxRect.centerY() - 6f)
                path.lineTo(boxRect.right, boxRect.centerY())
                path.lineTo(boxRect.right - 10f, boxRect.centerY() + 6f)
                canvas.drawPath(path, bdrPaint)
            }
            "DOUBLE_ARROW" -> {
                canvas.drawLine(boxRect.left, boxRect.centerY(), boxRect.right, boxRect.centerY(), bdrPaint)
                val path = Path()
                path.moveTo(boxRect.right - 10f, boxRect.centerY() - 6f)
                path.lineTo(boxRect.right, boxRect.centerY())
                path.lineTo(boxRect.right - 10f, boxRect.centerY() + 6f)
                path.moveTo(boxRect.left + 10f, boxRect.centerY() - 6f)
                path.lineTo(boxRect.left, boxRect.centerY())
                path.lineTo(boxRect.left + 10f, boxRect.centerY() + 6f)
                canvas.drawPath(path, bdrPaint)
            }
            "TRIANGLE" -> {
                val path = Path()
                path.moveTo(boxRect.centerX(), boxRect.top)
                path.lineTo(boxRect.right, boxRect.bottom)
                path.lineTo(boxRect.left, boxRect.bottom)
                path.close()
                canvas.drawPath(path, bgPaint)
                canvas.drawPath(path, bdrPaint)
            }
            "RIGHT_TRIANGLE" -> {
                val path = Path()
                path.moveTo(boxRect.left, boxRect.top)
                path.lineTo(boxRect.right, boxRect.bottom)
                path.lineTo(boxRect.left, boxRect.bottom)
                path.close()
                canvas.drawPath(path, bgPaint)
                canvas.drawPath(path, bdrPaint)
            }
            "HEXAGON", "OCTAGON", "PENTAGON" -> {
                val sides = if (shape.shapeType.uppercase() == "HEXAGON") 6 else if (shape.shapeType.uppercase() == "OCTAGON") 8 else 5
                val path = Path()
                val cx = boxRect.centerX()
                val cy = boxRect.centerY()
                val r = minOf(shapeW, shapeH) / 2f
                for (i in 0 until sides) {
                    val angle = (i * (360.0 / sides) - 90) * Math.PI / 180.0
                    val px = (cx + r * Math.cos(angle)).toFloat()
                    val py = (cy + r * Math.sin(angle)).toFloat()
                    if (i == 0) path.moveTo(px, py) else path.lineTo(px, py)
                }
                path.close()
                canvas.drawPath(path, bgPaint)
                canvas.drawPath(path, bdrPaint)
            }
            "CROSS", "PLUS" -> {
                val cw = shapeW * 0.3f
                val ch = shapeH * 0.3f
                val path = Path()
                path.moveTo(boxRect.left + cw, boxRect.top)
                path.lineTo(boxRect.right - cw, boxRect.top)
                path.lineTo(boxRect.right - cw, boxRect.top + ch)
                path.lineTo(boxRect.right, boxRect.top + ch)
                path.lineTo(boxRect.right, boxRect.bottom - ch)
                path.lineTo(boxRect.right - cw, boxRect.bottom - ch)
                path.lineTo(boxRect.right - cw, boxRect.bottom)
                path.lineTo(boxRect.left + cw, boxRect.bottom)
                path.lineTo(boxRect.left + cw, boxRect.bottom - ch)
                path.lineTo(boxRect.left, boxRect.bottom - ch)
                path.lineTo(boxRect.left, boxRect.top + ch)
                path.lineTo(boxRect.left + cw, boxRect.top + ch)
                path.close()
                canvas.drawPath(path, bgPaint)
                canvas.drawPath(path, bdrPaint)
            }
            "HEART" -> {
                val path = Path()
                val cx = boxRect.centerX()
                path.moveTo(cx, boxRect.top + shapeH * 0.8f)
                path.cubicTo(boxRect.left, boxRect.top + shapeH * 0.5f, boxRect.left, boxRect.top + shapeH * 0.1f, boxRect.left + shapeW * 0.25f, boxRect.top)
                path.cubicTo(boxRect.left + shapeW * 0.4f, boxRect.top, cx, boxRect.top + shapeH * 0.15f, cx, boxRect.top + shapeH * 0.3f)
                path.cubicTo(cx, boxRect.top + shapeH * 0.15f, boxRect.right - shapeW * 0.4f, boxRect.top, boxRect.right - shapeW * 0.25f, boxRect.top)
                path.cubicTo(boxRect.right, boxRect.top + shapeH * 0.1f, boxRect.right, boxRect.top + shapeH * 0.5f, cx, boxRect.top + shapeH * 0.8f)
                path.close()
                canvas.drawPath(path, bgPaint)
                canvas.drawPath(path, bdrPaint)
            }
            "ROUNDED_RECT", "FLOW_TERM" -> {
                canvas.drawRoundRect(boxRect, 14f, 14f, bgPaint)
                canvas.drawRoundRect(boxRect, 14f, 14f, bdrPaint)
            }
            else -> {
                canvas.drawRoundRect(boxRect, 8f, 8f, bgPaint)
                canvas.drawRoundRect(boxRect, 8f, 8f, bdrPaint)
            }
        }

        val titlePaint = Paint().apply {
            isAntiAlias = true
            color = try { Color.parseColor(shape.textColorHex) } catch (_: Exception) { bdrPaint.color }
            textSize = 10.5f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        if (shape.title.isNotBlank()) canvas.drawText(shape.title, leftMargin + 12f, cursorY + 20f, titlePaint)
        val bodyPaint = Paint().apply {
            isAntiAlias = true
            color = try { Color.parseColor(shape.textColorHex) } catch (_: Exception) { Color.rgb(30, 41, 59) }
            textSize = 9.5f
        }
        if (shape.content.isNotBlank()) canvas.drawText(shape.content, leftMargin + 12f, cursorY + 38f, bodyPaint)

        if (shape.rotationDeg != 0f) {
            canvas.restore()
        }

        return cursorY + shapeH + 12f
    }
}
