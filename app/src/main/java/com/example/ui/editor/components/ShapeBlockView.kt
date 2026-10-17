package com.example.ui.editor.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ShapeBlock
import com.example.ui.editor.components.shapes.ShapeContextMenu
import com.example.ui.editor.components.shapes.ShapeHandlesOverlay

/**
 * Microsoft Word Mobile Vector Shape Renderer & Interactive Editor.
 * Features 8 transform handles, top rotation stem, yellow adjustment handle,
 * anchor indicator, floating context bar (Cut/Copy/Delete/Add Text), and live geometry updates.
 */
@Composable
fun ShapeBlockView(
    block: ShapeBlock,
    isActive: Boolean = false,
    zoomScale: Float = 1f,
    onFocus: () -> Unit = {},
    onEditShape: () -> Unit = {},
    onDeleteShape: () -> Unit = {},
    onDuplicateShape: () -> Unit = {},
    onCutShape: () -> Unit = {},
    onCopyShape: () -> Unit = {},
    onAddComment: () -> Unit = {},
    onUpdateGeometry: (widthDp: Int, heightDp: Int, rotationDeg: Float) -> Unit = { _, _, _ -> },
    onUpdateText: (title: String, content: String) -> Unit = { _, _ -> }
) {
    val bgCol = try { Color(android.graphics.Color.parseColor(block.backgroundColorHex)) } catch (_: Exception) { Color(0xFF41719C) }
    val borderCol = try { Color(android.graphics.Color.parseColor(block.borderColorHex)) } catch (_: Exception) { Color(0xFF1B365D) }
    val textCol = try { Color(android.graphics.Color.parseColor(block.textColorHex)) } catch (_: Exception) { Color(0xFFFFFFFF) }

    var widthDp by remember(block.widthDp) { mutableIntStateOf(block.widthDp) }
    var heightDp by remember(block.heightDp) { mutableIntStateOf(block.heightDp) }
    var rotationDeg by remember(block.rotationDeg) { mutableFloatStateOf(block.rotationDeg) }

    val scaledWidth = (widthDp * zoomScale).dp
    val scaledHeight = (heightDp * zoomScale).dp

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = if (isActive) 18.dp else 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // --- 1. MS Word Floating Context Menu (Above the Shape) ---
        if (isActive) {
            ShapeContextMenu(
                shape = block,
                onCut = onCutShape,
                onCopy = onCopyShape,
                onDuplicate = onDuplicateShape,
                onDelete = onDeleteShape,
                onUpdateText = onUpdateText,
                onAddComment = onAddComment,
                modifier = Modifier.padding(bottom = 12.dp)
            )
        }

        // --- 2. Interactive Shape Container ---
        Box(
            modifier = Modifier
                .width(scaledWidth)
                .height(scaledHeight)
                .rotate(rotationDeg)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onFocus
                )
                .testTag("vector_shape_${block.id}"),
            contentAlignment = Alignment.Center
        ) {
            // Render Shape Geometry
            Canvas(modifier = Modifier.fillMaxSize()) {
                val strokeW = block.strokeWidthPt * density * zoomScale
                drawShapeGeometry(
                    shapeType = block.shapeType,
                    bgCol = bgCol,
                    borderCol = borderCol,
                    strokeW = strokeW
                )
            }

            // Text Content Overlay (Centered within shape)
            if (block.title.isNotBlank() || block.content.isNotBlank()) {
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
                            color = textCol.copy(alpha = 0.95f),
                            textAlign = TextAlign.Center,
                            maxLines = 4
                        )
                    }
                }
            }

            // --- 3. Selection Handles, Rotation Stem & Anchor (When Active) ---
            if (isActive) {
                ShapeHandlesOverlay(
                    widthDp = scaledWidth,
                    heightDp = scaledHeight,
                    shapeType = block.shapeType,
                    rotationDeg = rotationDeg,
                    zoomScale = zoomScale,
                    onResize = { newW, newH ->
                        widthDp = newW
                        heightDp = newH
                        onUpdateGeometry(newW, newH, rotationDeg)
                    },
                    onRotate = { newRot ->
                        rotationDeg = newRot
                        onUpdateGeometry(widthDp, heightDp, newRot)
                    },
                    onAdjustParam = { _ ->
                        // Fine-tune shape parameter
                    }
                )
            }
        }
    }
}
