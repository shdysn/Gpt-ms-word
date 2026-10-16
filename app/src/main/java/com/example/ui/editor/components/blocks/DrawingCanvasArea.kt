package com.example.ui.editor.components.blocks

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.drag
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DrawingStroke

@Composable
fun DrawingCanvasArea(
    canvasHeight: Dp,
    isDarkPage: Boolean,
    zoomScale: Float,
    selectedTool: String,
    selectedColorHex: String,
    selectedStrokeWidthPt: Float,
    currentStrokes: SnapshotStateList<DrawingStroke>,
    onStrokesChanged: (List<DrawingStroke>) -> Unit,
    modifier: Modifier = Modifier
) {
    val currentToolState = rememberUpdatedState(selectedTool)
    val currentColorState = rememberUpdatedState(selectedColorHex)
    val currentStrokeWidthState = rememberUpdatedState(selectedStrokeWidthPt)
    val onStrokesChangedState = rememberUpdatedState(onStrokesChanged)

    val activePoints = remember { mutableStateListOf<Pair<Float, Float>>() }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(canvasHeight)
            .clip(RoundedCornerShape(6.dp))
            .background(if (isDarkPage) Color(0xFF0F172A) else Color.White)
            .border(BorderStroke(1.dp, Color(0xFFCBD5E1)), RoundedCornerShape(6.dp))
            .pointerInput(Unit) {
                awaitEachGesture {
                    val down = awaitFirstDown(requireUnconsumed = false)
                    down.consume()
                    val tool = currentToolState.value
                    val colorHex = currentColorState.value
                    val strokeWidth = currentStrokeWidthState.value

                    if (tool == "ERASER") {
                        val threshold = 36f
                        fun eraseAt(x: Float, y: Float) {
                            val filtered = currentStrokes.filterNot { s: DrawingStroke ->
                                s.points.any { pt: Pair<Float, Float> ->
                                    kotlin.math.hypot(pt.first - x, pt.second - y) < threshold
                                }
                            }
                            if (filtered.size != currentStrokes.size) {
                                currentStrokes.clear()
                                currentStrokes.addAll(filtered)
                                onStrokesChangedState.value(currentStrokes.toList())
                            }
                        }

                        eraseAt(down.position.x, down.position.y)

                        drag(down.id) { change ->
                            change.consume()
                            eraseAt(change.position.x, change.position.y)
                        }
                    } else {
                        activePoints.clear()
                        activePoints.add(down.position.x to down.position.y)

                        drag(down.id) { change ->
                            change.consume()
                            activePoints.add(change.position.x to change.position.y)
                        }

                        if (activePoints.isNotEmpty()) {
                            val pts = activePoints.toList()
                            val newStroke = DrawingStroke(
                                points = pts,
                                colorHex = colorHex,
                                strokeWidthPt = strokeWidth,
                                isHighlighter = tool == "HIGHLIGHTER"
                            )
                            currentStrokes.add(newStroke)
                            onStrokesChangedState.value(currentStrokes.toList())
                            activePoints.clear()
                        }
                    }
                }
            }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            for (stroke in currentStrokes) {
                val baseCol = try {
                    Color(android.graphics.Color.parseColor(stroke.colorHex))
                } catch (_: Exception) {
                    Color.Black
                }
                val effectiveCol = if (stroke.isHighlighter) baseCol.copy(alpha = 0.45f) else baseCol
                val strokeWidthPx = (stroke.strokeWidthPt * zoomScale * density).coerceAtLeast(1f)

                if (stroke.points.size > 1) {
                    val path = Path().apply {
                        val firstPt = stroke.points.first()
                        moveTo(firstPt.first, firstPt.second)
                        for (i in 1 until stroke.points.size) {
                            val pt = stroke.points[i]
                            lineTo(pt.first, pt.second)
                        }
                    }
                    drawPath(
                        path = path,
                        color = effectiveCol,
                        style = Stroke(
                            width = strokeWidthPx,
                            cap = StrokeCap.Round,
                            join = StrokeJoin.Round
                        )
                    )
                } else if (stroke.points.size == 1) {
                    val pt = stroke.points.first()
                    drawCircle(
                        color = effectiveCol,
                        radius = (strokeWidthPx / 2f).coerceAtLeast(1.5f),
                        center = Offset(pt.first, pt.second)
                    )
                }
            }

            if (activePoints.isNotEmpty()) {
                val activeTool = currentToolState.value
                val baseCol = try {
                    Color(android.graphics.Color.parseColor(currentColorState.value))
                } catch (_: Exception) {
                    Color.Black
                }
                val effectiveCol = if (activeTool == "HIGHLIGHTER") baseCol.copy(alpha = 0.45f) else baseCol
                val strokeWidthPx = (currentStrokeWidthState.value * zoomScale * density).coerceAtLeast(1f)

                if (activePoints.size > 1) {
                    val path = Path().apply {
                        val firstPt = activePoints.first()
                        moveTo(firstPt.first, firstPt.second)
                        for (i in 1 until activePoints.size) {
                            val pt = activePoints[i]
                            lineTo(pt.first, pt.second)
                        }
                    }
                    drawPath(
                        path = path,
                        color = effectiveCol,
                        style = Stroke(
                            width = strokeWidthPx,
                            cap = StrokeCap.Round,
                            join = StrokeJoin.Round
                        )
                    )
                } else if (activePoints.size == 1) {
                    val pt = activePoints.first()
                    drawCircle(
                        color = effectiveCol,
                        radius = (strokeWidthPx / 2f).coerceAtLeast(1.5f),
                        center = Offset(pt.first, pt.second)
                    )
                }
            }
        }

        if (currentStrokes.isEmpty() && activePoints.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = "Touch or use Stylus / Pen to draw freehand notes",
                    color = Color.Gray,
                    fontSize = 12.sp
                )
            }
        }
    }
}
