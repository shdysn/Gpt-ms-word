package com.example.ui.editor.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Draw
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DrawingBlock
import com.example.data.model.DrawingStroke
import com.example.ui.editor.components.blocks.DrawingCanvasArea
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
            DrawingCanvasArea(
                canvasHeight = canvasHeight,
                isDarkPage = isDarkPage,
                zoomScale = zoomScale,
                selectedTool = selectedTool,
                selectedColorHex = selectedColorHex,
                selectedStrokeWidthPt = selectedStrokeWidthPt,
                currentStrokes = currentStrokes,
                activeStrokePoints = activeStrokePoints,
                onActiveStrokePointsChanged = { activeStrokePoints = it },
                onStrokesChanged = onStrokesChanged
            )
        }
    }
}
