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
import com.example.ui.editor.components.drawShapeGeometry

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
            drawShapeGeometry(
                shapeType = shapeType,
                bgCol = bg,
                borderCol = bdr,
                strokeW = sw
            )
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
