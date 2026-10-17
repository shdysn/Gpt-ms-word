package com.example.ui.editor.components.shapes

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt

/**
 * Microsoft Word Mobile interactive shape selection handles overlay.
 * Renders 8 resize handles (4 corners + 4 edge midpoints), a top rotation stem with
 * rotate handle (⟳), a yellow adjustment handle (●), and the blue anchor (⚓).
 */
@Composable
fun ShapeHandlesOverlay(
    widthDp: Dp,
    heightDp: Dp,
    shapeType: String,
    rotationDeg: Float,
    zoomScale: Float,
    onResize: (newWidth: Int, newHeight: Int) -> Unit,
    onRotate: (newRotation: Float) -> Unit,
    onAdjustParam: (delta: Float) -> Unit = {}
) {
    val handleSize = (14 * zoomScale).coerceIn(12f, 18f).dp
    val yellowSize = (13 * zoomScale).coerceIn(11f, 17f).dp

    var currentW by remember(widthDp) { mutableFloatStateOf(widthDp.value) }
    var currentH by remember(heightDp) { mutableFloatStateOf(heightDp.value) }
    var currentRot by remember(rotationDeg) { mutableFloatStateOf(rotationDeg) }

    Box(
        modifier = Modifier
            .size(widthDp, heightDp)
            .border(1.dp, Color(0xFF2563EB).copy(alpha = 0.85f))
    ) {
        // --- 1. Anchor Icon (Blue Anchor at top-left) ---
        Box(
            modifier = Modifier
                .offset(x = (-18).dp, y = (-20).dp)
                .size(18.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(text = "⚓", fontSize = 14.sp, color = Color(0xFF1E40AF))
        }

        // --- 2. Rotation Stem & Handle (MS Word Style) ---
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = (-32).dp)
                .size(24.dp)
                .clip(CircleShape)
                .background(Color.White)
                .border(1.5.dp, Color(0xFF475569), CircleShape)
                .pointerInput(Unit) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        currentRot = (currentRot + dragAmount.x * 0.8f) % 360f
                        if (currentRot < 0) currentRot += 360f
                        onRotate(currentRot)
                    }
                }
                .testTag("shape_rotation_handle"),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = "Rotate Shape",
                tint = Color(0xFF1E293B),
                modifier = Modifier.size(15.dp)
            )
        }

        // Stem line connecting rotation handle to top-center
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = (-16).dp)
                .size(width = 1.5.dp, height = 16.dp)
                .background(Color(0xFF64748B))
        )

        // --- 3. Yellow Adjustment Handle (● for Snip / Rounded corners) ---
        val showYellowHandle = shapeType.uppercase() in listOf(
            "SNIP_CORNER", "SNIP_TOP", "SNIP_DIAG", "ROUNDED_RECT", "ROUND_CORNER",
            "ARROW_RIGHT", "ARROW_LEFT", "CALLOUT_RECT", "CALLOUT_ROUND"
        )
        if (showYellowHandle) {
            val yellowOffsetX = if (shapeType.uppercase().startsWith("SNIP")) (-24).dp else (-16).dp
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = yellowOffsetX, y = (4).dp)
                    .size(yellowSize)
                    .clip(CircleShape)
                    .background(Color(0xFFEAB308))
                    .border(1.2.dp, Color(0xFF713F12), CircleShape)
                    .pointerInput(Unit) {
                        detectDragGestures { change, dragAmount ->
                            change.consume()
                            onAdjustParam(dragAmount.x)
                        }
                    }
                    .testTag("shape_adjustment_handle")
            )
        }

        // --- 4. Eight White Resize Handles ---
        // Corner handles
        ResizeHandle(Alignment.TopStart, handleSize) { dx, dy ->
            currentW = (currentW - dx).coerceIn(50f, 400f)
            currentH = (currentH - dy).coerceIn(30f, 350f)
            onResize(currentW.roundToInt(), currentH.roundToInt())
        }
        ResizeHandle(Alignment.TopEnd, handleSize) { dx, dy ->
            currentW = (currentW + dx).coerceIn(50f, 400f)
            currentH = (currentH - dy).coerceIn(30f, 350f)
            onResize(currentW.roundToInt(), currentH.roundToInt())
        }
        ResizeHandle(Alignment.BottomStart, handleSize) { dx, dy ->
            currentW = (currentW - dx).coerceIn(50f, 400f)
            currentH = (currentH + dy).coerceIn(30f, 350f)
            onResize(currentW.roundToInt(), currentH.roundToInt())
        }
        ResizeHandle(Alignment.BottomEnd, handleSize) { dx, dy ->
            currentW = (currentW + dx).coerceIn(50f, 400f)
            currentH = (currentH + dy).coerceIn(30f, 350f)
            onResize(currentW.roundToInt(), currentH.roundToInt())
        }

        // Edge midpoint handles
        ResizeHandle(Alignment.TopCenter, handleSize) { _, dy ->
            currentH = (currentH - dy).coerceIn(30f, 350f)
            onResize(currentW.roundToInt(), currentH.roundToInt())
        }
        ResizeHandle(Alignment.BottomCenter, handleSize) { _, dy ->
            currentH = (currentH + dy).coerceIn(30f, 350f)
            onResize(currentW.roundToInt(), currentH.roundToInt())
        }
        ResizeHandle(Alignment.CenterStart, handleSize) { dx, _ ->
            currentW = (currentW - dx).coerceIn(50f, 400f)
            onResize(currentW.roundToInt(), currentH.roundToInt())
        }
        ResizeHandle(Alignment.CenterEnd, handleSize) { dx, _ ->
            currentW = (currentW + dx).coerceIn(50f, 400f)
            onResize(currentW.roundToInt(), currentH.roundToInt())
        }
    }
}

@Composable
private fun ResizeHandle(
    alignment: Alignment,
    size: Dp,
    onDrag: (dx: Float, dy: Float) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize(),
        contentAlignment = alignment
    ) {
        val half = size / 2
        val offsetX = when (alignment) {
            Alignment.TopStart, Alignment.CenterStart, Alignment.BottomStart -> -half
            Alignment.TopEnd, Alignment.CenterEnd, Alignment.BottomEnd -> half
            else -> 0.dp
        }
        val offsetY = when (alignment) {
            Alignment.TopStart, Alignment.TopCenter, Alignment.TopEnd -> -half
            Alignment.BottomStart, Alignment.BottomCenter, Alignment.BottomEnd -> half
            else -> 0.dp
        }

        Box(
            modifier = Modifier
                .offset(x = offsetX, y = offsetY)
                .size(size)
                .clip(CircleShape)
                .background(Color.White)
                .border(1.2.dp, Color(0xFF334155), CircleShape)
                .pointerInput(Unit) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        onDrag(dragAmount.x, dragAmount.y)
                    }
                }
        )
    }
}
