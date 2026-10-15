package com.example.ui.editor.components.dialogs

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun MiniShapePreview(
    shapeType: String,
    bgHex: String,
    borderHex: String,
    strokeWidthPt: Float,
    rotationDeg: Float,
    title: String
) {
    val bg = try { Color(android.graphics.Color.parseColor(bgHex)) } catch (_: Exception) { Color(0xFFEFF6FF) }
    val bdr = try { Color(android.graphics.Color.parseColor(borderHex)) } catch (_: Exception) { Color(0xFF3B82F6) }

    Box(
        modifier = Modifier
            .size(width = 160.dp, height = 80.dp)
            .rotate(rotationDeg),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val sw = strokeWidthPt * density
            when (shapeType.uppercase()) {
                "OVAL" -> {
                    drawOval(color = bg, style = Fill)
                    drawOval(color = bdr, style = Stroke(width = sw))
                }
                "STAR" -> {
                    val p = Path()
                    val cx = size.width / 2f
                    val cy = size.height / 2f
                    val rOuter = minOf(size.width, size.height) / 2f - sw
                    val rInner = rOuter * 0.42f
                    for (i in 0 until 10) {
                        val r = if (i % 2 == 0) rOuter else rInner
                        val angle = (i * 36 - 90) * Math.PI / 180.0
                        val px = (cx + r * Math.cos(angle)).toFloat()
                        val py = (cy + r * Math.sin(angle)).toFloat()
                        if (i == 0) p.moveTo(px, py) else p.lineTo(px, py)
                    }
                    p.close()
                    drawPath(p, color = bg, style = Fill)
                    drawPath(p, color = bdr, style = Stroke(width = sw))
                }
                "DIAMOND" -> {
                    val p = Path().apply {
                        moveTo(size.width / 2f, sw)
                        lineTo(size.width - sw, size.height / 2f)
                        lineTo(size.width / 2f, size.height - sw)
                        lineTo(sw, size.height / 2f)
                        close()
                    }
                    drawPath(p, color = bg, style = Fill)
                    drawPath(p, color = bdr, style = Stroke(width = sw))
                }
                "ARROW_RIGHT" -> {
                    val p = Path().apply {
                        val my = size.height / 2f
                        val hw = size.width * 0.35f
                        val hh = size.height * 0.25f
                        moveTo(sw, my - hh)
                        lineTo(size.width - hw, my - hh)
                        lineTo(size.width - hw, sw)
                        lineTo(size.width - sw, my)
                        lineTo(size.width - hw, size.height - sw)
                        lineTo(size.width - hw, my + hh)
                        lineTo(sw, my + hh)
                        close()
                    }
                    drawPath(p, color = bg, style = Fill)
                    drawPath(p, color = bdr, style = Stroke(width = sw))
                }
                "ARROW_LEFT" -> {
                    val p = Path().apply {
                        val my = size.height / 2f
                        val hw = size.width * 0.35f
                        val hh = size.height * 0.25f
                        moveTo(size.width - sw, my - hh)
                        lineTo(hw, my - hh)
                        lineTo(hw, sw)
                        lineTo(sw, my)
                        lineTo(hw, size.height - sw)
                        lineTo(hw, my + hh)
                        lineTo(size.width - sw, my + hh)
                        close()
                    }
                    drawPath(p, color = bg, style = Fill)
                    drawPath(p, color = bdr, style = Stroke(width = sw))
                }
                "CALLOUT" -> {
                    val bh = size.height * 0.78f
                    val p = Path().apply {
                        addRoundRect(RoundRect(Rect(Offset(0f, 0f), Size(size.width, bh)), CornerRadius(12f, 12f)))
                        moveTo(size.width * 0.25f, bh)
                        lineTo(size.width * 0.35f, size.height - 2f)
                        lineTo(size.width * 0.45f, bh)
                    }
                    drawPath(p, color = bg, style = Fill)
                    drawPath(p, color = bdr, style = Stroke(width = sw))
                }
                "RECTANGLE" -> {
                    drawRect(color = bg, style = Fill)
                    drawRect(color = bdr, style = Stroke(width = sw))
                }
                else -> {
                    drawRoundRect(color = bg, cornerRadius = CornerRadius(12f, 12f), style = Fill)
                    drawRoundRect(color = bdr, cornerRadius = CornerRadius(12f, 12f), style = Stroke(width = sw))
                }
            }
        }
        Text(
            text = title.ifBlank { "Shape" },
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp,
            color = bdr,
            textAlign = TextAlign.Center,
            maxLines = 1,
            modifier = Modifier.padding(horizontal = 8.dp)
        )
    }
}
