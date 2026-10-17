package com.example.ui.editor.components.bottomsheet

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.ui.editor.components.drawShapeGeometry

data class WordShapeItem(
    val type: String,
    val label: String,
    val category: String
)

val WordBlueFill = Color(0xFF41719C)
val WordDarkBorder = Color(0xFF1B365D)

/**
 * Renders MS Word Mobile vector shape thumbnail in the Shapes gallery.
 * Directly leverages drawShapeGeometry to guarantee 100% fidelity between preview and canvas.
 */
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
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.padding(3.dp)
        ) {
            Canvas(modifier = Modifier.size(36.dp, 28.dp)) {
                val sw = 1.3.dp.toPx()
                drawShapeGeometry(
                    shapeType = shape.type,
                    bgCol = WordBlueFill,
                    borderCol = WordDarkBorder,
                    strokeW = sw
                )
            }
        }
    }
}
