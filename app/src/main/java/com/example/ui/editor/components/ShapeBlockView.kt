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
                drawShapeGeometry(
                    shapeType = block.shapeType,
                    bgCol = bgCol,
                    borderCol = borderCol,
                    strokeW = strokeW
                )
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
