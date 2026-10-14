package com.example.ui.editor.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Draw
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Highlight
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DrawingBlock
import com.example.data.model.DrawingStroke
import com.example.ui.theme.WordPrimary

/**
 * Interactive Freehand Inking Canvas component for drawing with Touch, Stylus, or S-Pen.
 */
@Composable
fun DrawingBlockView(
    block: DrawingBlock,
    isDarkPage: Boolean = false,
    zoomScale: Float = 1f,
    onStrokesChanged: (List<DrawingStroke>) -> Unit,
    onDeleteBlock: () -> Unit
) {
    var selectedTool by remember { mutableStateOf("PEN") } // PEN, HIGHLIGHTER, ERASER
    var selectedColorHex by remember { mutableStateOf("#0F172A") }
    var selectedStrokeWidthPt by remember { mutableStateOf(2.5f) }

    val currentStrokes = remember(block.strokes) {
        mutableStateListOf<DrawingStroke>().apply { addAll(block.strokes) }
    }

    var activeStrokePoints by remember { mutableStateOf<List<Pair<Float, Float>>>(emptyList()) }

    val palette = listOf("#0F172A", "#1E40AF", "#DC2626", "#059669", "#D97706", "#7C3AED", "#FFF59D", "#A7F3D0")

    Surface(
        shape = RoundedCornerShape(8.dp),
        color = if (isDarkPage) Color(0xFF1E293B) else Color(0xFFF8FAFC),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .testTag("drawing_block_${block.id}")
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            // Header Controls
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Icon(Icons.Default.Draw, contentDescription = null, tint = WordPrimary, modifier = Modifier.size(18.dp))
                    Text("Inking Canvas", fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = WordPrimary)
                }

                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                    if (currentStrokes.isNotEmpty()) {
                        IconButton(
                            onClick = {
                                if (currentStrokes.isNotEmpty()) {
                                    currentStrokes.removeAt(currentStrokes.lastIndex)
                                    onStrokesChanged(currentStrokes.toList())
                                }
                            },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(Icons.AutoMirrored.Filled.Undo, contentDescription = "Undo Stroke", modifier = Modifier.size(16.dp))
                        }

                        IconButton(
                            onClick = {
                                currentStrokes.clear()
                                onStrokesChanged(emptyList())
                            },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear All", modifier = Modifier.size(16.dp))
                        }
                    }

                    IconButton(onClick = onDeleteBlock, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete Drawing", tint = Color.Gray, modifier = Modifier.size(16.dp))
                    }
                }
            }

            // Quick Tool Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                FilterChip(
                    selected = selectedTool == "PEN",
                    onClick = {
                        selectedTool = "PEN"
                        selectedStrokeWidthPt = 2.5f
                        if (selectedColorHex == "#FFF59D") selectedColorHex = "#0F172A"
                    },
                    label = { Text("Pen", fontSize = 11.sp) },
                    modifier = Modifier.height(28.dp)
                )

                FilterChip(
                    selected = selectedTool == "HIGHLIGHTER",
                    onClick = {
                        selectedTool = "HIGHLIGHTER"
                        selectedStrokeWidthPt = 7f
                        selectedColorHex = "#FFF59D"
                    },
                    label = { Text("Highlighter", fontSize = 11.sp) },
                    modifier = Modifier.height(28.dp)
                )

                FilterChip(
                    selected = selectedTool == "ERASER",
                    onClick = { selectedTool = "ERASER" },
                    label = { Text("Eraser", fontSize = 11.sp) },
                    modifier = Modifier.height(28.dp)
                )

                Spacer(modifier = Modifier.weight(1f))

                // Color Swatches
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    palette.take(5).forEach { hex ->
                        val col = try { Color(android.graphics.Color.parseColor(hex)) } catch (_: Exception) { Color.Black }
                        Box(
                            modifier = Modifier
                                .size(18.dp)
                                .clip(CircleShape)
                                .background(col)
                                .border(
                                    if (selectedColorHex == hex) 2.dp else 0.5.dp,
                                    if (selectedColorHex == hex) WordPrimary else Color.LightGray,
                                    CircleShape
                                )
                                .clickable {
                                    selectedColorHex = hex
                                    if (selectedTool == "ERASER") selectedTool = "PEN"
                                }
                        )
                    }
                }
            }

            // Touch Inking Canvas Area
            val canvasHeight = (block.heightDp * zoomScale).dp
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(canvasHeight)
                    .clip(RoundedCornerShape(6.dp))
                    .background(if (isDarkPage) Color(0xFF0F172A) else Color.White)
                    .border(BorderStroke(1.dp, Color(0xFFCBD5E1)), RoundedCornerShape(6.dp))
                    .pointerInput(selectedTool, selectedColorHex, selectedStrokeWidthPt) {
                        detectDragGestures(
                            onDragStart = { offset ->
                                if (selectedTool == "ERASER") {
                                    // Remove strokes close to touch point
                                    val threshold = 24f
                                    val filtered = currentStrokes.filterNot { s ->
                                        s.points.any { (px, py) ->
                                            kotlin.math.hypot(px - offset.x, py - offset.y) < threshold
                                        }
                                    }
                                    if (filtered.size != currentStrokes.size) {
                                        currentStrokes.clear()
                                        currentStrokes.addAll(filtered)
                                        onStrokesChanged(currentStrokes.toList())
                                    }
                                } else {
                                    activeStrokePoints = listOf(offset.x to offset.y)
                                }
                            },
                            onDrag = { change, _ ->
                                change.consume()
                                if (selectedTool == "ERASER") {
                                    val threshold = 24f
                                    val filtered = currentStrokes.filterNot { s ->
                                        s.points.any { (px, py) ->
                                            kotlin.math.hypot(px - change.position.x, py - change.position.y) < threshold
                                        }
                                    }
                                    if (filtered.size != currentStrokes.size) {
                                        currentStrokes.clear()
                                        currentStrokes.addAll(filtered)
                                        onStrokesChanged(currentStrokes.toList())
                                    }
                                } else {
                                    activeStrokePoints = activeStrokePoints + (change.position.x to change.position.y)
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
                                activeStrokePoints = emptyList()
                            },
                            onDragCancel = {
                                activeStrokePoints = emptyList()
                            }
                        )
                    }
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    // Draw saved strokes
                    for (stroke in currentStrokes) {
                        if (stroke.points.size > 1) {
                            val path = Path().apply {
                                moveTo(stroke.points.first().first, stroke.points.first().second)
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

                    // Draw in-flight active stroke
                    if (activeStrokePoints.size > 1) {
                        val path = Path().apply {
                            moveTo(activeStrokePoints.first().first, activeStrokePoints.first().second)
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
    }
}
