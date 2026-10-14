package com.example.ui.editor.components.blocks

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
    activeStrokePoints: List<Pair<Float, Float>>,
    onActiveStrokePointsChanged: (List<Pair<Float, Float>>) -> Unit,
    onStrokesChanged: (List<DrawingStroke>) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(canvasHeight)
            .clip(RoundedCornerShape(6.dp))
            .background(if (isDarkPage) Color(0xFF0F172A) else Color.White)
            .border(BorderStroke(1.dp, Color(0xFFCBD5E1)), RoundedCornerShape(6.dp))
            .pointerInput(selectedTool, selectedColorHex, selectedStrokeWidthPt) {
                detectDragGestures(
                    onDragStart = { offset ->
                        if (selectedTool == "ERASER") {
                            val threshold = 24f
                            val filtered = currentStrokes.filterNot { s: DrawingStroke ->
                                s.points.any { pt: Pair<Float, Float> ->
                                    val (px, py) = pt
                                    kotlin.math.hypot(px - offset.x, py - offset.y) < threshold
                                }
                            }
                            if (filtered.size != currentStrokes.size) {
                                currentStrokes.clear()
                                currentStrokes.addAll(filtered)
                                onStrokesChanged(currentStrokes.toList())
                            }
                        } else {
                            onActiveStrokePointsChanged(listOf(offset.x to offset.y))
                        }
                    },
                    onDrag = { change, _ ->
                        change.consume()
                        if (selectedTool == "ERASER") {
                            val threshold = 24f
                            val filtered = currentStrokes.filterNot { s: DrawingStroke ->
                                s.points.any { pt: Pair<Float, Float> ->
                                    val (px, py) = pt
                                    kotlin.math.hypot(px - change.position.x, py - change.position.y) < threshold
                                }
                            }
                            if (filtered.size != currentStrokes.size) {
                                currentStrokes.clear()
                                currentStrokes.addAll(filtered)
                                onStrokesChanged(currentStrokes.toList())
                            }
                        } else {
                            onActiveStrokePointsChanged(activeStrokePoints + (change.position.x to change.position.y))
                        }
                    },
                    onDragEnd = {
                        if (selectedTool != "ERASER" && activeStrokePoints.size > 1) {
                            val newStroke = DrawingStroke(
                                points = activeStrokePoints,
                                colorHex = selectedColorHex,
                                strokeWidthPt = selectedStrokeWidthPt,
                                isHighlighter = selectedTool == "HIGHLIGHTER"
                            )
                            currentStrokes.add(newStroke)
                            onStrokesChanged(currentStrokes.toList())
                        }
                        onActiveStrokePointsChanged(emptyList())
                    },
                    onDragCancel = {
                        onActiveStrokePointsChanged(emptyList())
                    }
                )
            }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            for (stroke in currentStrokes) {
                if (stroke.points.size > 1) {
                    val path = Path().apply {
                        val firstPt = stroke.points.first()
                        moveTo(firstPt.first, firstPt.second)
                        for (i in 1 until stroke.points.size) {
                            val pt = stroke.points[i]
                            lineTo(pt.first, pt.second)
                        }
                    }
                    val baseCol = try { Color(android.graphics.Color.parseColor(stroke.colorHex)) } catch (_: Exception) { Color.Black }
                    val effectiveCol = if (stroke.isHighlighter) baseCol.copy(alpha = 0.45f) else baseCol
                    drawPath(
                        path = path,
                        color = effectiveCol,
                        style = Stroke(
                            width = stroke.strokeWidthPt * zoomScale * density,
                            cap = StrokeCap.Round,
                            join = StrokeJoin.Round
                        )
                    )
                }
            }

            if (activeStrokePoints.size > 1) {
                val path = Path().apply {
                    val firstPt = activeStrokePoints.first()
                    moveTo(firstPt.first, firstPt.second)
                    for (i in 1 until activeStrokePoints.size) {
                        val pt = activeStrokePoints[i]
                        lineTo(pt.first, pt.second)
                    }
                }
                val baseCol = try { Color(android.graphics.Color.parseColor(selectedColorHex)) } catch (_: Exception) { Color.Black }
                val effectiveCol = if (selectedTool == "HIGHLIGHTER") baseCol.copy(alpha = 0.45f) else baseCol
                drawPath(
                    path = path,
                    color = effectiveCol,
                    style = Stroke(
                        width = selectedStrokeWidthPt * zoomScale * density,
                        cap = StrokeCap.Round,
                        join = StrokeJoin.Round
                    )
                )
            }
        }

        if (currentStrokes.isEmpty() && activeStrokePoints.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = "Touch or use Stylus / S-Pen to draw freehand notes",
                    color = Color.Gray,
                    fontSize = 11.sp
                )
            }
        }
    }
}
