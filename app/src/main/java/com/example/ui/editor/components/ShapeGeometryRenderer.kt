package com.example.ui.editor.components

import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke

/**
 * Complete MS Word Geometry Renderer for all Vector Shapes.
 * Accurately draws polygons, arrows, math symbols, stars, banners, and callouts.
 */
fun DrawScope.drawShapeGeometry(
    shapeType: String,
    bgCol: Color,
    borderCol: Color,
    strokeW: Float
) {
    val w = size.width
    val h = size.height

    when (shapeType.uppercase()) {
        // --- LINES ---
        "LINE" -> {
            drawLine(borderCol, Offset(strokeW, h - strokeW), Offset(w - strokeW, strokeW), strokeWidth = strokeW.coerceAtLeast(3f), cap = StrokeCap.Round)
        }
        "ARROW_LINE" -> {
            drawLine(borderCol, Offset(strokeW, h - strokeW), Offset(w - 12f, 12f), strokeWidth = strokeW.coerceAtLeast(3f), cap = StrokeCap.Round)
            drawArrowTip(borderCol, tip = Offset(w - strokeW, strokeW), angleDeg = -45f, strokeW = strokeW)
        }
        "DOUBLE_ARROW" -> {
            drawLine(borderCol, Offset(12f, h - 12f), Offset(w - 12f, 12f), strokeWidth = strokeW.coerceAtLeast(3f), cap = StrokeCap.Round)
            drawArrowTip(borderCol, tip = Offset(w - strokeW, strokeW), angleDeg = -45f, strokeW = strokeW)
            drawArrowTip(borderCol, tip = Offset(strokeW, h - strokeW), angleDeg = 135f, strokeW = strokeW)
        }
        "ELBOW", "CURVE" -> {
            val path = Path().apply {
                moveTo(strokeW, strokeW)
                lineTo(w / 2f, strokeW)
                lineTo(w / 2f, h - strokeW)
                lineTo(w - strokeW, h - strokeW)
            }
            drawPath(path, color = borderCol, style = Stroke(width = strokeW.coerceAtLeast(3f), cap = StrokeCap.Round))
        }

        // --- RECTANGLES ---
        "RECTANGLE", "FLOW_PROCESS" -> {
            drawRect(color = bgCol, style = Fill)
            drawRect(color = borderCol, style = Stroke(width = strokeW))
        }
        "ROUNDED_RECT", "FLOW_TERM" -> {
            drawRoundRect(color = bgCol, cornerRadius = CornerRadius(20f, 20f), style = Fill)
            drawRoundRect(color = borderCol, cornerRadius = CornerRadius(20f, 20f), style = Stroke(width = strokeW))
        }
        "SNIP_CORNER" -> {
            val cut = 24f
            val p = Path().apply {
                moveTo(strokeW, strokeW)
                lineTo(w - cut, strokeW)
                lineTo(w - strokeW, cut)
                lineTo(w - strokeW, h - strokeW)
                lineTo(strokeW, h - strokeW)
                close()
            }
            drawPath(p, color = bgCol, style = Fill)
            drawPath(p, color = borderCol, style = Stroke(width = strokeW))
        }
        "SNIP_TOP" -> {
            val cut = 22f
            val p = Path().apply {
                moveTo(strokeW + cut, strokeW)
                lineTo(w - cut, strokeW)
                lineTo(w - strokeW, cut)
                lineTo(w - strokeW, h - strokeW)
                lineTo(strokeW, h - strokeW)
                lineTo(strokeW, cut)
                close()
            }
            drawPath(p, color = bgCol, style = Fill)
            drawPath(p, color = borderCol, style = Stroke(width = strokeW))
        }
        "SNIP_DIAG" -> {
            val cut = 22f
            val p = Path().apply {
                moveTo(strokeW + cut, strokeW)
                lineTo(w - strokeW, strokeW)
                lineTo(w - strokeW, h - cut)
                lineTo(w - cut, h - strokeW)
                lineTo(strokeW, h - strokeW)
                lineTo(strokeW, cut)
                close()
            }
            drawPath(p, color = bgCol, style = Fill)
            drawPath(p, color = borderCol, style = Stroke(width = strokeW))
        }
        "ROUND_CORNER" -> {
            val p = Path().apply {
                addRoundRect(RoundRect(Rect(0f, 0f, w, h), topLeft = CornerRadius(30f, 30f)))
            }
            drawPath(p, color = bgCol, style = Fill)
            drawPath(p, color = borderCol, style = Stroke(width = strokeW))
        }

        // --- BASIC SHAPES ---
        "OVAL", "FLOW_CONN" -> {
            drawOval(color = bgCol, style = Fill)
            drawOval(color = borderCol, style = Stroke(width = strokeW))
        }
        "TRIANGLE" -> {
            val p = Path().apply {
                moveTo(w / 2f, strokeW)
                lineTo(w - strokeW, h - strokeW)
                lineTo(strokeW, h - strokeW)
                close()
            }
            drawPath(p, color = bgCol, style = Fill)
            drawPath(p, color = borderCol, style = Stroke(width = strokeW))
        }
        "RIGHT_TRIANGLE" -> {
            val p = Path().apply {
                moveTo(strokeW, strokeW)
                lineTo(w - strokeW, h - strokeW)
                lineTo(strokeW, h - strokeW)
                close()
            }
            drawPath(p, color = bgCol, style = Fill)
            drawPath(p, color = borderCol, style = Stroke(width = strokeW))
        }
        "DIAMOND", "FLOW_DECISION" -> {
            val p = Path().apply {
                moveTo(w / 2f, strokeW)
                lineTo(w - strokeW, h / 2f)
                lineTo(w / 2f, h - strokeW)
                lineTo(strokeW, h / 2f)
                close()
            }
            drawPath(p, color = bgCol, style = Fill)
            drawPath(p, color = borderCol, style = Stroke(width = strokeW))
        }
        "PARALLELOGRAM", "FLOW_DATA" -> {
            val slant = w * 0.2f
            val p = Path().apply {
                moveTo(slant, strokeW)
                lineTo(w - strokeW, strokeW)
                lineTo(w - slant, h - strokeW)
                lineTo(strokeW, h - strokeW)
                close()
            }
            drawPath(p, color = bgCol, style = Fill)
            drawPath(p, color = borderCol, style = Stroke(width = strokeW))
        }
        "TRAPEZOID" -> {
            val cut = w * 0.18f
            val p = Path().apply {
                moveTo(cut, strokeW)
                lineTo(w - cut, strokeW)
                lineTo(w - strokeW, h - strokeW)
                lineTo(strokeW, h - strokeW)
                close()
            }
            drawPath(p, color = bgCol, style = Fill)
            drawPath(p, color = borderCol, style = Stroke(width = strokeW))
        }
        "PENTAGON" -> drawPolygonPath(bgCol, borderCol, strokeW, sides = 5)
        "HEXAGON" -> drawPolygonPath(bgCol, borderCol, strokeW, sides = 6)
        "OCTAGON" -> drawPolygonPath(bgCol, borderCol, strokeW, sides = 8)
        "CROSS" -> {
            val cw = w * 0.33f
            val ch = h * 0.33f
            val p = Path().apply {
                moveTo(cw, strokeW)
                lineTo(w - cw, strokeW)
                lineTo(w - cw, ch)
                lineTo(w - strokeW, ch)
                lineTo(w - strokeW, h - ch)
                lineTo(w - cw, h - ch)
                lineTo(w - cw, h - strokeW)
                lineTo(cw, h - strokeW)
                lineTo(cw, h - ch)
                lineTo(strokeW, h - ch)
                lineTo(strokeW, ch)
                lineTo(cw, ch)
                close()
            }
            drawPath(p, color = bgCol, style = Fill)
            drawPath(p, color = borderCol, style = Stroke(width = strokeW))
        }
        "CYLINDER" -> {
            val eh = h * 0.22f
            drawRoundRect(color = bgCol, topLeft = Offset(strokeW, eh / 2f), size = Size(w - strokeW * 2, h - eh), cornerRadius = CornerRadius(0f, 0f), style = Fill)
            drawOval(color = bgCol, topLeft = Offset(strokeW, h - eh), size = Size(w - strokeW * 2, eh), style = Fill)
            drawOval(color = bgCol, topLeft = Offset(strokeW, strokeW), size = Size(w - strokeW * 2, eh), style = Fill)
            drawLine(borderCol, Offset(strokeW, eh / 2f), Offset(strokeW, h - eh / 2f), strokeWidth = strokeW)
            drawLine(borderCol, Offset(w - strokeW, eh / 2f), Offset(w - strokeW, h - eh / 2f), strokeWidth = strokeW)
            drawOval(color = borderCol, topLeft = Offset(strokeW, h - eh), size = Size(w - strokeW * 2, eh), style = Stroke(width = strokeW))
            drawOval(color = borderCol, topLeft = Offset(strokeW, strokeW), size = Size(w - strokeW * 2, eh), style = Stroke(width = strokeW))
        }
        "HEART" -> {
            val p = Path().apply {
                moveTo(w / 2f, h * 0.85f)
                cubicTo(w * 0.1f, h * 0.55f, strokeW, h * 0.25f, w * 0.25f, strokeW + 4f)
                cubicTo(w * 0.42f, strokeW, w / 2f, h * 0.2f, w / 2f, h * 0.32f)
                cubicTo(w / 2f, h * 0.2f, w * 0.58f, strokeW, w * 0.75f, strokeW + 4f)
                cubicTo(w - strokeW, h * 0.25f, w * 0.9f, h * 0.55f, w / 2f, h * 0.85f)
                close()
            }
            drawPath(p, color = bgCol, style = Fill)
            drawPath(p, color = borderCol, style = Stroke(width = strokeW))
        }
        "LIGHTNING" -> {
            val p = Path().apply {
                moveTo(w * 0.55f, strokeW)
                lineTo(w * 0.2f, h * 0.52f)
                lineTo(w * 0.48f, h * 0.52f)
                lineTo(w * 0.35f, h - strokeW)
                lineTo(w * 0.8f, h * 0.42f)
                lineTo(w * 0.52f, h * 0.42f)
                close()
            }
            drawPath(p, color = bgCol, style = Fill)
            drawPath(p, color = borderCol, style = Stroke(width = strokeW))
        }
        "SUN" -> {
            drawCircle(color = bgCol, radius = minOf(w, h) * 0.28f, center = Offset(w / 2f, h / 2f), style = Fill)
            drawCircle(color = borderCol, radius = minOf(w, h) * 0.28f, center = Offset(w / 2f, h / 2f), style = Stroke(width = strokeW))
            val p = createStarPath(w, h, points = 8, innerRatio = 0.65f)
            drawPath(p, color = borderCol, style = Stroke(width = strokeW))
        }
        "MOON" -> {
            val p = Path().apply {
                moveTo(w * 0.6f, strokeW)
                cubicTo(w * 0.15f, h * 0.2f, w * 0.15f, h * 0.8f, w * 0.6f, h - strokeW)
                cubicTo(w * 0.35f, h * 0.7f, w * 0.35f, h * 0.3f, w * 0.6f, strokeW)
                close()
            }
            drawPath(p, color = bgCol, style = Fill)
            drawPath(p, color = borderCol, style = Stroke(width = strokeW))
        }
        "CLOUD", "CALLOUT_CLOUD" -> {
            val p = Path().apply {
                addRoundRect(RoundRect(Rect(w * 0.12f, h * 0.35f, w * 0.88f, h * 0.82f), CornerRadius(20f, 20f)))
            }
            drawPath(p, color = bgCol, style = Fill)
            drawCircle(bgCol, radius = h * 0.28f, center = Offset(w * 0.38f, h * 0.42f))
            drawCircle(bgCol, radius = h * 0.32f, center = Offset(w * 0.6f, h * 0.38f))
            drawCircle(borderCol, radius = h * 0.28f, center = Offset(w * 0.38f, h * 0.42f), style = Stroke(width = strokeW))
            drawCircle(borderCol, radius = h * 0.32f, center = Offset(w * 0.6f, h * 0.38f), style = Stroke(width = strokeW))
            drawPath(p, color = borderCol, style = Stroke(width = strokeW))
        }

        // --- BLOCK ARROWS ---
        "ARROW_RIGHT" -> {
            val my = h / 2f
            val hw = w * 0.38f
            val hh = h * 0.25f
            val p = Path().apply {
                moveTo(strokeW, my - hh)
                lineTo(w - hw, my - hh)
                lineTo(w - hw, strokeW)
                lineTo(w - strokeW, my)
                lineTo(w - hw, h - strokeW)
                lineTo(w - hw, my + hh)
                lineTo(strokeW, my + hh)
                close()
            }
            drawPath(p, color = bgCol, style = Fill)
            drawPath(p, color = borderCol, style = Stroke(width = strokeW))
        }
        "ARROW_LEFT" -> {
            val my = h / 2f
            val hw = w * 0.38f
            val hh = h * 0.25f
            val p = Path().apply {
                moveTo(w - strokeW, my - hh)
                lineTo(hw, my - hh)
                lineTo(hw, strokeW)
                lineTo(strokeW, my)
                lineTo(hw, h - strokeW)
                lineTo(hw, my + hh)
                lineTo(w - strokeW, my + hh)
                close()
            }
            drawPath(p, color = bgCol, style = Fill)
            drawPath(p, color = borderCol, style = Stroke(width = strokeW))
        }
        "ARROW_UP" -> {
            val mx = w / 2f
            val hh = h * 0.38f
            val hw = w * 0.25f
            val p = Path().apply {
                moveTo(mx, strokeW)
                lineTo(w - strokeW, hh)
                lineTo(mx + hw, hh)
                lineTo(mx + hw, h - strokeW)
                lineTo(mx - hw, h - strokeW)
                lineTo(mx - hw, hh)
                lineTo(strokeW, hh)
                close()
            }
            drawPath(p, color = bgCol, style = Fill)
            drawPath(p, color = borderCol, style = Stroke(width = strokeW))
        }
        "ARROW_DOWN" -> {
            val mx = w / 2f
            val hh = h * 0.38f
            val hw = w * 0.25f
            val p = Path().apply {
                moveTo(mx - hw, strokeW)
                lineTo(mx + hw, strokeW)
                lineTo(mx + hw, h - hh)
                lineTo(w - strokeW, h - hh)
                lineTo(mx, h - strokeW)
                lineTo(strokeW, h - hh)
                lineTo(mx - hw, h - hh)
                close()
            }
            drawPath(p, color = bgCol, style = Fill)
            drawPath(p, color = borderCol, style = Stroke(width = strokeW))
        }
        "ARROW_LEFTRIGHT" -> {
            drawArrowRightLeft(bgCol, borderCol, strokeW, w, h)
        }
        "ARROW_UPDOWN" -> {
            val mx = w / 2f
            val hh = h * 0.3f
            val hw = w * 0.25f
            val p = Path().apply {
                moveTo(mx, strokeW)
                lineTo(w - strokeW, hh); lineTo(mx + hw, hh); lineTo(mx + hw, h - hh); lineTo(w - strokeW, h - hh)
                lineTo(mx, h - strokeW); lineTo(strokeW, h - hh); lineTo(mx - hw, h - hh); lineTo(mx - hw, hh)
                lineTo(strokeW, hh); close()
            }
            drawPath(p, color = bgCol, style = Fill)
            drawPath(p, color = borderCol, style = Stroke(width = strokeW))
        }
        "ARROW_QUAD" -> {
            val cx = w / 2f
            val cy = h / 2f
            val arm = minOf(w, h) * 0.18f
            val tip = minOf(w, h) * 0.35f
            val p = Path().apply {
                moveTo(cx, strokeW); lineTo(cx + tip, tip); lineTo(cx + arm, tip); lineTo(cx + arm, cy - arm)
                lineTo(w - tip, cy - arm); lineTo(w - tip, cy - tip); lineTo(w - strokeW, cy); lineTo(w - tip, cy + tip)
                lineTo(w - tip, cy + arm); lineTo(cx + arm, cy + arm); lineTo(cx + arm, h - tip); lineTo(cx + tip, h - tip)
                lineTo(cx, h - strokeW); lineTo(cx - tip, h - tip); lineTo(cx - arm, h - tip); lineTo(cx - arm, cy + arm)
                lineTo(tip, cy + arm); lineTo(tip, cy + tip); lineTo(strokeW, cy); lineTo(tip, cy - tip)
                lineTo(tip, cy - arm); lineTo(cx - arm, cy - arm); lineTo(cx - arm, tip); lineTo(cx - tip, tip)
                close()
            }
            drawPath(p, color = bgCol, style = Fill)
            drawPath(p, color = borderCol, style = Stroke(width = strokeW))
        }
        "ARROW_UTURN" -> {
            val p = Path().apply {
                moveTo(strokeW, h * 0.4f)
                cubicTo(strokeW, strokeW, w * 0.7f, strokeW, w * 0.7f, h * 0.5f)
                lineTo(w * 0.85f, h * 0.5f)
                lineTo(w * 0.6f, h - strokeW)
                lineTo(w * 0.35f, h * 0.5f)
                lineTo(w * 0.5f, h * 0.5f)
                cubicTo(w * 0.5f, h * 0.25f, w * 0.2f, h * 0.25f, w * 0.2f, h * 0.4f)
                close()
            }
            drawPath(p, color = bgCol, style = Fill)
            drawPath(p, color = borderCol, style = Stroke(width = strokeW))
        }

        // --- EQUATION SHAPES ---
        "PLUS" -> {
            val cw = w * 0.32f
            val ch = h * 0.32f
            val p = Path().apply {
                moveTo(cw, strokeW); lineTo(w - cw, strokeW); lineTo(w - cw, ch); lineTo(w - strokeW, ch)
                lineTo(w - strokeW, h - ch); lineTo(w - cw, h - ch); lineTo(w - cw, h - strokeW)
                lineTo(cw, h - strokeW); lineTo(cw, h - ch); lineTo(strokeW, h - ch); lineTo(strokeW, ch)
                lineTo(cw, ch); close()
            }
            drawPath(p, color = bgCol, style = Fill)
            drawPath(p, color = borderCol, style = Stroke(width = strokeW))
        }
        "MINUS" -> {
            val barH = h * 0.3f
            drawRect(bgCol, topLeft = Offset(strokeW, h / 2f - barH / 2f), size = Size(w - strokeW * 2, barH), style = Fill)
            drawRect(borderCol, topLeft = Offset(strokeW, h / 2f - barH / 2f), size = Size(w - strokeW * 2, barH), style = Stroke(width = strokeW))
        }
        "MULTIPLY" -> {
            val p = createStarPath(w, h, points = 4, innerRatio = 0.35f)
            drawPath(p, color = bgCol, style = Fill)
            drawPath(p, color = borderCol, style = Stroke(width = strokeW))
        }
        "DIVIDE" -> {
            val barH = h * 0.16f
            val dotR = minOf(w, h) * 0.12f
            drawRect(bgCol, topLeft = Offset(strokeW, h / 2f - barH / 2f), size = Size(w - strokeW * 2, barH), style = Fill)
            drawRect(borderCol, topLeft = Offset(strokeW, h / 2f - barH / 2f), size = Size(w - strokeW * 2, barH), style = Stroke(width = strokeW))
            drawCircle(bgCol, radius = dotR, center = Offset(w / 2f, h * 0.22f), style = Fill)
            drawCircle(borderCol, radius = dotR, center = Offset(w / 2f, h * 0.22f), style = Stroke(width = strokeW))
            drawCircle(bgCol, radius = dotR, center = Offset(w / 2f, h * 0.78f), style = Fill)
            drawCircle(borderCol, radius = dotR, center = Offset(w / 2f, h * 0.78f), style = Stroke(width = strokeW))
        }
        "EQUAL" -> {
            val barH = h * 0.18f
            drawRect(bgCol, topLeft = Offset(strokeW, h * 0.25f), size = Size(w - strokeW * 2, barH), style = Fill)
            drawRect(borderCol, topLeft = Offset(strokeW, h * 0.25f), size = Size(w - strokeW * 2, barH), style = Stroke(width = strokeW))
            drawRect(bgCol, topLeft = Offset(strokeW, h * 0.58f), size = Size(w - strokeW * 2, barH), style = Fill)
            drawRect(borderCol, topLeft = Offset(strokeW, h * 0.58f), size = Size(w - strokeW * 2, barH), style = Stroke(width = strokeW))
        }
        "NOT_EQUAL" -> {
            val barH = h * 0.18f
            drawRect(bgCol, topLeft = Offset(strokeW, h * 0.25f), size = Size(w - strokeW * 2, barH), style = Fill)
            drawRect(borderCol, topLeft = Offset(strokeW, h * 0.25f), size = Size(w - strokeW * 2, barH), style = Stroke(width = strokeW))
            drawRect(bgCol, topLeft = Offset(strokeW, h * 0.58f), size = Size(w - strokeW * 2, barH), style = Fill)
            drawRect(borderCol, topLeft = Offset(strokeW, h * 0.58f), size = Size(w - strokeW * 2, barH), style = Stroke(width = strokeW))
            drawLine(borderCol, Offset(w * 0.7f, strokeW), Offset(w * 0.3f, h - strokeW), strokeWidth = strokeW.coerceAtLeast(3f), cap = StrokeCap.Round)
        }
        "CUBE" -> {
            val dx = w * 0.25f
            val dy = h * 0.25f
            val pFront = Path().apply {
                moveTo(strokeW, dy); lineTo(w - dx, dy); lineTo(w - dx, h - strokeW); lineTo(strokeW, h - strokeW); close()
            }
            val pTop = Path().apply {
                moveTo(strokeW, dy); lineTo(dx, strokeW); lineTo(w - strokeW, strokeW); lineTo(w - dx, dy); close()
            }
            val pRight = Path().apply {
                moveTo(w - dx, dy); lineTo(w - strokeW, strokeW); lineTo(w - strokeW, h - dy); lineTo(w - dx, h - strokeW); close()
            }
            drawPath(pFront, color = bgCol, style = Fill)
            drawPath(pTop, color = bgCol.copy(alpha = 0.85f), style = Fill)
            drawPath(pRight, color = bgCol.copy(alpha = 0.7f), style = Fill)
            drawPath(pFront, color = borderCol, style = Stroke(width = strokeW))
            drawPath(pTop, color = borderCol, style = Stroke(width = strokeW))
            drawPath(pRight, color = borderCol, style = Stroke(width = strokeW))
        }

        // --- STARS AND BANNERS ---
        "STAR", "STAR_5" -> drawStarDirect(bgCol, borderCol, strokeW, w, h, points = 5)
        "STAR_4" -> drawStarDirect(bgCol, borderCol, strokeW, w, h, points = 4)
        "STAR_6" -> drawStarDirect(bgCol, borderCol, strokeW, w, h, points = 6)
        "STAR_8" -> drawStarDirect(bgCol, borderCol, strokeW, w, h, points = 8)
        "STAR_12", "EXPLOSION" -> drawStarDirect(bgCol, borderCol, strokeW, w, h, points = 12, innerRatio = 0.6f)
        "RIBBON_UP", "RIBBON_DOWN", "SCROLL", "BANNER_WAVE" -> {
            val p = Path().apply {
                moveTo(strokeW, h * 0.2f)
                cubicTo(w * 0.35f, strokeW, w * 0.65f, h * 0.4f, w - strokeW, h * 0.2f)
                lineTo(w - strokeW, h * 0.8f)
                cubicTo(w * 0.65f, h, w * 0.35f, h * 0.6f, strokeW, h * 0.8f)
                close()
            }
            drawPath(p, color = bgCol, style = Fill)
            drawPath(p, color = borderCol, style = Stroke(width = strokeW))
        }

        // --- CALLOUTS ---
        "CALLOUT", "CALLOUT_RECT", "CALLOUT_ROUND", "CALLOUT_OVAL", "NOTE", "WARNING", "SUCCESS", "QUOTE" -> {
            val bh = h * 0.78f
            val p = Path().apply {
                addRoundRect(RoundRect(Rect(strokeW, strokeW, w - strokeW, bh), CornerRadius(16f, 16f)))
                moveTo(w * 0.22f, bh)
                lineTo(w * 0.32f, h - strokeW)
                lineTo(w * 0.42f, bh)
            }
            drawPath(p, color = bgCol, style = Fill)
            drawPath(p, color = borderCol, style = Stroke(width = strokeW))
        }

        // Fallback default
        else -> {
            drawRoundRect(color = bgCol, cornerRadius = CornerRadius(16f, 16f), style = Fill)
            drawRoundRect(color = borderCol, cornerRadius = CornerRadius(16f, 16f), style = Stroke(width = strokeW))
        }
    }
}

private fun DrawScope.drawArrowTip(color: Color, tip: Offset, angleDeg: Float, strokeW: Float) {
    val r = 10f
    val rad = Math.toRadians(angleDeg.toDouble())
    val p1 = Offset((tip.x - r * Math.cos(rad - 0.5)).toFloat(), (tip.y - r * Math.sin(rad - 0.5)).toFloat())
    val p2 = Offset((tip.x - r * Math.cos(rad + 0.5)).toFloat(), (tip.y - r * Math.sin(rad + 0.5)).toFloat())
    drawLine(color, tip, p1, strokeWidth = strokeW.coerceAtLeast(3f), cap = StrokeCap.Round)
    drawLine(color, tip, p2, strokeWidth = strokeW.coerceAtLeast(3f), cap = StrokeCap.Round)
}

private fun DrawScope.drawArrowRightLeft(bgCol: Color, borderCol: Color, strokeW: Float, w: Float, h: Float) {
    val my = h / 2f
    val hw = w * 0.3f
    val hh = h * 0.25f
    val p = Path().apply {
        moveTo(strokeW, my)
        lineTo(hw, strokeW); lineTo(hw, my - hh); lineTo(w - hw, my - hh); lineTo(w - hw, strokeW)
        lineTo(w - strokeW, my); lineTo(w - hw, h - strokeW); lineTo(w - hw, my + hh)
        lineTo(hw, my + hh); lineTo(hw, h - strokeW); close()
    }
    drawPath(p, color = bgCol, style = Fill)
    drawPath(p, color = borderCol, style = Stroke(width = strokeW))
}

private fun DrawScope.drawPolygonPath(bgCol: Color, borderCol: Color, strokeW: Float, sides: Int) {
    val cx = size.width / 2f
    val cy = size.height / 2f
    val r = minOf(size.width, size.height) / 2f - strokeW
    val p = Path()
    for (i in 0 until sides) {
        val angle = (i * (360.0 / sides) - 90) * Math.PI / 180.0
        val px = (cx + r * Math.cos(angle)).toFloat()
        val py = (cy + r * Math.sin(angle)).toFloat()
        if (i == 0) p.moveTo(px, py) else p.lineTo(px, py)
    }
    p.close()
    drawPath(p, color = bgCol, style = Fill)
    drawPath(p, color = borderCol, style = Stroke(width = strokeW))
}

private fun DrawScope.drawStarDirect(bgCol: Color, borderCol: Color, strokeW: Float, w: Float, h: Float, points: Int, innerRatio: Float = 0.45f) {
    val p = createStarPath(w, h, points, innerRatio)
    drawPath(p, color = bgCol, style = Fill)
    drawPath(p, color = borderCol, style = Stroke(width = strokeW))
}

private fun createStarPath(w: Float, h: Float, points: Int, innerRatio: Float = 0.45f): Path {
    val path = Path()
    val cx = w / 2f
    val cy = h / 2f
    val rOuter = minOf(w, h) / 2f - 4f
    val rInner = rOuter * innerRatio
    val total = points * 2
    for (i in 0 until total) {
        val r = if (i % 2 == 0) rOuter else rInner
        val angle = (i * (360.0 / total) - 90) * Math.PI / 180.0
        val px = (cx + r * Math.cos(angle)).toFloat()
        val py = (cy + r * Math.sin(angle)).toFloat()
        if (i == 0) path.moveTo(px, py) else path.lineTo(px, py)
    }
    path.close()
    return path
}
