package com.example.ui.editor.components.bottomsheet

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class WordShapeItem(
    val type: String,
    val label: String,
    val category: String
)

val WordBlueFill = Color(0xFF41719C)
val WordDarkBorder = Color(0xFF1B365D)

@Composable
fun WordShapeThumbnail(
    shape: WordShapeItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .size(width = 46.dp, height = 40.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(4.dp),
        color = Color.Transparent
    ) {
        Box(contentAlignment = Alignment.Center) {
            Canvas(modifier = Modifier.size(34.dp, 28.dp)) {
                val sw = 1.4.dp.toPx()
                val w = size.width
                val h = size.height

                when (shape.type) {
                    "LINE" -> {
                        drawLine(WordBlueFill, Offset(2.dp.toPx(), h - 2.dp.toPx()), Offset(w - 2.dp.toPx(), 2.dp.toPx()), strokeWidth = 2.dp.toPx(), cap = StrokeCap.Round)
                    }
                    "ARROW_LINE" -> {
                        drawLine(WordBlueFill, Offset(2.dp.toPx(), h - 2.dp.toPx()), Offset(w - 4.dp.toPx(), 4.dp.toPx()), strokeWidth = 2.dp.toPx(), cap = StrokeCap.Round)
                        drawArrowHead(WordBlueFill, Offset(w - 4.dp.toPx(), 4.dp.toPx()), angleDeg = -45f)
                    }
                    "DOUBLE_ARROW" -> {
                        drawLine(WordBlueFill, Offset(4.dp.toPx(), h - 4.dp.toPx()), Offset(w - 4.dp.toPx(), 4.dp.toPx()), strokeWidth = 2.dp.toPx(), cap = StrokeCap.Round)
                    }
                    "ELBOW", "CURVE" -> {
                        val p = Path().apply {
                            moveTo(2.dp.toPx(), 2.dp.toPx())
                            lineTo(w / 2f, 2.dp.toPx())
                            lineTo(w / 2f, h - 2.dp.toPx())
                            lineTo(w - 2.dp.toPx(), h - 2.dp.toPx())
                        }
                        drawPath(p, color = WordBlueFill, style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round))
                    }
                    "RECTANGLE", "FLOW_PROCESS" -> {
                        drawRect(color = WordBlueFill, topLeft = Offset(1.dp.toPx(), 1.dp.toPx()), size = Size(w - 2.dp.toPx(), h - 2.dp.toPx()), style = Fill)
                        drawRect(color = WordDarkBorder, topLeft = Offset(1.dp.toPx(), 1.dp.toPx()), size = Size(w - 2.dp.toPx(), h - 2.dp.toPx()), style = Stroke(width = sw))
                    }
                    "ROUNDED_RECT", "FLOW_TERM" -> {
                        drawRoundRect(color = WordBlueFill, topLeft = Offset(1.dp.toPx(), 1.dp.toPx()), size = Size(w - 2.dp.toPx(), h - 2.dp.toPx()), cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx()), style = Fill)
                        drawRoundRect(color = WordDarkBorder, topLeft = Offset(1.dp.toPx(), 1.dp.toPx()), size = Size(w - 2.dp.toPx(), h - 2.dp.toPx()), cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx()), style = Stroke(width = sw))
                    }
                    "OVAL", "FLOW_CONN" -> {
                        drawOval(color = WordBlueFill, topLeft = Offset(1.dp.toPx(), 1.dp.toPx()), size = Size(w - 2.dp.toPx(), h - 2.dp.toPx()), style = Fill)
                        drawOval(color = WordDarkBorder, topLeft = Offset(1.dp.toPx(), 1.dp.toPx()), size = Size(w - 2.dp.toPx(), h - 2.dp.toPx()), style = Stroke(width = sw))
                    }
                    "TRIANGLE" -> {
                        val p = Path().apply {
                            moveTo(w / 2f, 1.dp.toPx())
                            lineTo(w - 1.dp.toPx(), h - 1.dp.toPx())
                            lineTo(1.dp.toPx(), h - 1.dp.toPx())
                            close()
                        }
                        drawPath(p, color = WordBlueFill, style = Fill)
                        drawPath(p, color = WordDarkBorder, style = Stroke(width = sw))
                    }
                    "RIGHT_TRIANGLE" -> {
                        val p = Path().apply {
                            moveTo(1.dp.toPx(), 1.dp.toPx())
                            lineTo(w - 1.dp.toPx(), h - 1.dp.toPx())
                            lineTo(1.dp.toPx(), h - 1.dp.toPx())
                            close()
                        }
                        drawPath(p, color = WordBlueFill, style = Fill)
                        drawPath(p, color = WordDarkBorder, style = Stroke(width = sw))
                    }
                    "DIAMOND", "FLOW_DECISION" -> {
                        val p = Path().apply {
                            moveTo(w / 2f, 1.dp.toPx())
                            lineTo(w - 1.dp.toPx(), h / 2f)
                            lineTo(w / 2f, h - 1.dp.toPx())
                            lineTo(1.dp.toPx(), h / 2f)
                            close()
                        }
                        drawPath(p, color = WordBlueFill, style = Fill)
                        drawPath(p, color = WordDarkBorder, style = Stroke(width = sw))
                    }
                    "PARALLELOGRAM", "FLOW_DATA" -> {
                        val p = Path().apply {
                            moveTo(w * 0.25f, 1.dp.toPx())
                            lineTo(w - 1.dp.toPx(), 1.dp.toPx())
                            lineTo(w * 0.75f, h - 1.dp.toPx())
                            lineTo(1.dp.toPx(), h - 1.dp.toPx())
                            close()
                        }
                        drawPath(p, color = WordBlueFill, style = Fill)
                        drawPath(p, color = WordDarkBorder, style = Stroke(width = sw))
                    }
                    "TRAPEZOID" -> {
                        val p = Path().apply {
                            moveTo(w * 0.2f, 1.dp.toPx())
                            lineTo(w * 0.8f, 1.dp.toPx())
                            lineTo(w - 1.dp.toPx(), h - 1.dp.toPx())
                            lineTo(1.dp.toPx(), h - 1.dp.toPx())
                            close()
                        }
                        drawPath(p, color = WordBlueFill, style = Fill)
                        drawPath(p, color = WordDarkBorder, style = Stroke(width = sw))
                    }
                    "ARROW_RIGHT" -> {
                        val p = Path().apply {
                            val my = h / 2f
                            val hh = h * 0.24f
                            moveTo(1.dp.toPx(), my - hh)
                            lineTo(w * 0.6f, my - hh)
                            lineTo(w * 0.6f, 1.dp.toPx())
                            lineTo(w - 1.dp.toPx(), my)
                            lineTo(w * 0.6f, h - 1.dp.toPx())
                            lineTo(w * 0.6f, my + hh)
                            lineTo(1.dp.toPx(), my + hh)
                            close()
                        }
                        drawPath(p, color = WordBlueFill, style = Fill)
                        drawPath(p, color = WordDarkBorder, style = Stroke(width = sw))
                    }
                    "ARROW_LEFT" -> {
                        val p = Path().apply {
                            val my = h / 2f
                            val hh = h * 0.24f
                            moveTo(w - 1.dp.toPx(), my - hh)
                            lineTo(w * 0.4f, my - hh)
                            lineTo(w * 0.4f, 1.dp.toPx())
                            lineTo(1.dp.toPx(), my)
                            lineTo(w * 0.4f, h - 1.dp.toPx())
                            lineTo(w * 0.4f, my + hh)
                            lineTo(w - 1.dp.toPx(), my + hh)
                            close()
                        }
                        drawPath(p, color = WordBlueFill, style = Fill)
                        drawPath(p, color = WordDarkBorder, style = Stroke(width = sw))
                    }
                    "ARROW_UP", "ARROW_DOWN", "ARROW_LEFTRIGHT", "ARROW_UPDOWN", "ARROW_QUAD", "ARROW_UTURN" -> {
                        drawRect(color = WordBlueFill, topLeft = Offset(4.dp.toPx(), 4.dp.toPx()), size = Size(w - 8.dp.toPx(), h - 8.dp.toPx()), style = Fill)
                        drawRect(color = WordDarkBorder, topLeft = Offset(4.dp.toPx(), 4.dp.toPx()), size = Size(w - 8.dp.toPx(), h - 8.dp.toPx()), style = Stroke(width = sw))
                    }
                    "STAR", "STAR_4", "STAR_6", "STAR_8", "STAR_12", "EXPLOSION" -> {
                        val p = createStarPath(w, h, points = if (shape.type == "STAR_4") 4 else if (shape.type == "STAR_6") 6 else if (shape.type == "STAR_8") 8 else 5)
                        drawPath(p, color = WordBlueFill, style = Fill)
                        drawPath(p, color = WordDarkBorder, style = Stroke(width = sw))
                    }
                    "PLUS" -> drawMathSymbol("+", w, h)
                    "MINUS" -> drawMathSymbol("—", w, h)
                    "MULTIPLY" -> drawMathSymbol("×", w, h)
                    "DIVIDE" -> drawMathSymbol("÷", w, h)
                    "EQUAL" -> drawMathSymbol("=", w, h)
                    "NOT_EQUAL" -> drawMathSymbol("≠", w, h)
                    "CALLOUT_RECT", "CALLOUT_ROUND", "CALLOUT_OVAL", "CALLOUT_CLOUD" -> {
                        val p = Path().apply {
                            moveTo(1.dp.toPx(), 1.dp.toPx())
                            lineTo(w - 1.dp.toPx(), 1.dp.toPx())
                            lineTo(w - 1.dp.toPx(), h - 7.dp.toPx())
                            lineTo(12.dp.toPx(), h - 7.dp.toPx())
                            lineTo(4.dp.toPx(), h - 1.dp.toPx())
                            lineTo(6.dp.toPx(), h - 7.dp.toPx())
                            lineTo(1.dp.toPx(), h - 7.dp.toPx())
                            close()
                        }
                        drawPath(p, color = WordBlueFill, style = Fill)
                        drawPath(p, color = WordDarkBorder, style = Stroke(width = sw))
                    }
                    else -> {
                        // Default geometric polygon
                        drawRoundRect(color = WordBlueFill, topLeft = Offset(2.dp.toPx(), 2.dp.toPx()), size = Size(w - 4.dp.toPx(), h - 4.dp.toPx()), cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx()), style = Fill)
                        drawRoundRect(color = WordDarkBorder, topLeft = Offset(2.dp.toPx(), 2.dp.toPx()), size = Size(w - 4.dp.toPx(), h - 4.dp.toPx()), cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx()), style = Stroke(width = sw))
                    }
                }
            }
        }
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawArrowHead(color: Color, tip: Offset, angleDeg: Float) {
    val r = 5.dp.toPx()
    val rad = Math.toRadians(angleDeg.toDouble())
    val p1 = Offset((tip.x - r * Math.cos(rad - 0.5)).toFloat(), (tip.y - r * Math.sin(rad - 0.5)).toFloat())
    val p2 = Offset((tip.x - r * Math.cos(rad + 0.5)).toFloat(), (tip.y - r * Math.sin(rad + 0.5)).toFloat())
    drawLine(color, tip, p1, strokeWidth = 2.dp.toPx(), cap = StrokeCap.Round)
    drawLine(color, tip, p2, strokeWidth = 2.dp.toPx(), cap = StrokeCap.Round)
}

private fun createStarPath(w: Float, h: Float, points: Int): Path {
    val path = Path()
    val cx = w / 2f
    val cy = h / 2f
    val rOuter = minOf(w, h) / 2f - 2f
    val rInner = rOuter * 0.45f
    val totalPts = points * 2
    for (i in 0 until totalPts) {
        val r = if (i % 2 == 0) rOuter else rInner
        val angle = (i * (360f / totalPts) - 90) * Math.PI / 180.0
        val px = (cx + r * Math.cos(angle)).toFloat()
        val py = (cy + r * Math.sin(angle)).toFloat()
        if (i == 0) path.moveTo(px, py) else path.lineTo(px, py)
    }
    path.close()
    return path
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawMathSymbol(symbol: String, w: Float, h: Float) {
    drawRoundRect(color = WordBlueFill, topLeft = Offset(2.dp.toPx(), 2.dp.toPx()), size = Size(w - 4.dp.toPx(), h - 4.dp.toPx()), cornerRadius = CornerRadius(3.dp.toPx(), 3.dp.toPx()), style = Fill)
    drawRoundRect(color = WordDarkBorder, topLeft = Offset(2.dp.toPx(), 2.dp.toPx()), size = Size(w - 4.dp.toPx(), h - 4.dp.toPx()), cornerRadius = CornerRadius(3.dp.toPx(), 3.dp.toPx()), style = Stroke(width = 1.4.dp.toPx()))
}
