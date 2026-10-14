package com.example.ui.editor.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ShapeBlock

/**
 * Geometric Vector Shape Renderer supporting Rectangle, Rounded Rect, Oval,
 * Arrows, Star, Diamond, Callout Speech Bubble, and Callout Notes.
 */
@Composable
fun ShapeBlockView(
    block: ShapeBlock,
    zoomScale: Float = 1f,
    onEditShape: () -> Unit = {},
    onDeleteShape: () -> Unit = {}
) {
    val bgCol = try { Color(android.graphics.Color.parseColor(block.backgroundColorHex)) } catch (_: Exception) { Color(0xFFEFF6FF) }
    val borderCol = try { Color(android.graphics.Color.parseColor(block.borderColorHex)) } catch (_: Exception) { Color(0xFF3B82F6) }
    val textCol = try { Color(android.graphics.Color.parseColor(block.textColorHex)) } catch (_: Exception) { Color(0xFF1E293B) }

    val widthDp = (block.widthDp * zoomScale).dp
    val heightDp = (block.heightDp * zoomScale).dp

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .width(widthDp)
                .height(heightDp)
                .rotate(block.rotationDeg)
                .clickable(onClick = onEditShape)
                .testTag("vector_shape_${block.id}"),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val strokeW = block.strokeWidthPt * density * zoomScale

                when (block.shapeType.uppercase()) {
                    "OVAL" -> {
                        drawOval(color = bgCol, style = Fill)
                        drawOval(color = borderCol, style = Stroke(width = strokeW))
                    }
                    "STAR" -> {
                        val path = Path()
                        val cx = size.width / 2f
                        val cy = size.height / 2f
                        val rOuter = minOf(size.width, size.height) / 2f - strokeW
                        val rInner = rOuter * 0.42f
                        for (i in 0 until 10) {
                            val r = if (i % 2 == 0) rOuter else rInner
                            val angle = (i * 36 - 90) * Math.PI / 180.0
                            val px = (cx + r * Math.cos(angle)).toFloat()
                            val py = (cy + r * Math.sin(angle)).toFloat()
                            if (i == 0) path.moveTo(px, py) else path.lineTo(px, py)
                        }
                        path.close()
                        drawPath(path, color = bgCol, style = Fill)
                        drawPath(path, color = borderCol, style = Stroke(width = strokeW))
                    }
                    "DIAMOND" -> {
                        val path = Path().apply {
                            moveTo(size.width / 2f, strokeW / 2)
                            lineTo(size.width - strokeW / 2, size.height / 2f)
                            lineTo(size.width / 2f, size.height - strokeW / 2)
                            lineTo(strokeW / 2, size.height / 2f)
                            close()
                        }
                        drawPath(path, color = bgCol, style = Fill)
                        drawPath(path, color = borderCol, style = Stroke(width = strokeW))
                    }
                    "ARROW_RIGHT" -> {
                        val path = Path().apply {
                            val my = size.height / 2f
                            val headW = size.width * 0.35f
                            val hH = size.height * 0.25f
                            moveTo(strokeW, my - hH)
                            lineTo(size.width - headW, my - hH)
                            lineTo(size.width - headW, strokeW)
                            lineTo(size.width - strokeW, my)
                            lineTo(size.width - headW, size.height - strokeW)
                            lineTo(size.width - headW, my + hH)
                            lineTo(strokeW, my + hH)
                            close()
                        }
                        drawPath(path, color = bgCol, style = Fill)
                        drawPath(path, color = borderCol, style = Stroke(width = strokeW))
                    }
                    "ARROW_LEFT" -> {
                        val path = Path().apply {
                            val my = size.height / 2f
                            val headW = size.width * 0.35f
                            val hH = size.height * 0.25f
                            moveTo(size.width - strokeW, my - hH)
                            lineTo(headW, my - hH)
                            lineTo(headW, strokeW)
                            lineTo(strokeW, my)
                            lineTo(headW, size.height - strokeW)
                            lineTo(headW, my + hH)
                            lineTo(size.width - strokeW, my + hH)
                            close()
                        }
                        drawPath(path, color = bgCol, style = Fill)
                        drawPath(path, color = borderCol, style = Stroke(width = strokeW))
                    }
                    "CALLOUT" -> {
                        val bubbleH = size.height * 0.78f
                        val path = Path().apply {
                            addRoundRect(RoundRect(Rect(Offset(0f, 0f), Size(size.width, bubbleH)), CornerRadius(16f, 16f)))
                            moveTo(size.width * 0.25f, bubbleH)
                            lineTo(size.width * 0.35f, size.height - 4f)
                            lineTo(size.width * 0.45f, bubbleH)
                        }
                        drawPath(path, color = bgCol, style = Fill)
                        drawPath(path, color = borderCol, style = Stroke(width = strokeW))
                    }
                    "RECTANGLE" -> {
                        drawRect(color = bgCol, style = Fill)
                        drawRect(color = borderCol, style = Stroke(width = strokeW))
                    }
                    else -> {
                        // ROUNDED_RECT, NOTE, WARNING, QUOTE, etc.
                        drawRoundRect(color = bgCol, cornerRadius = CornerRadius(16f, 16f), style = Fill)
                        drawRoundRect(color = borderCol, cornerRadius = CornerRadius(16f, 16f), style = Stroke(width = strokeW))
                    }
                }
            }

            // Content Overlay
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center
            ) {
                if (block.title.isNotBlank()) {
                    Text(
                        text = block.title,
                        fontWeight = FontWeight.Bold,
                        fontSize = (12f * zoomScale).sp,
                        color = textCol,
                        textAlign = TextAlign.Center
                    )
                }
                if (block.content.isNotBlank()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = block.content,
                        fontSize = (11f * zoomScale).sp,
                        color = textCol.copy(alpha = 0.9f),
                        textAlign = TextAlign.Center,
                        maxLines = 4
                    )
                }
            }

            // Action overlay on top-right
            Row(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(2.dp)
            ) {
                IconButton(onClick = onEditShape, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit Shape", tint = borderCol, modifier = Modifier.size(13.dp))
                }
                IconButton(onClick = onDeleteShape, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete Shape", tint = Color.Gray, modifier = Modifier.size(13.dp))
                }
            }
        }
    }
}
