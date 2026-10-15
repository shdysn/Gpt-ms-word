package com.example.ui.editor.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AspectRatio
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.OpenWith
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.TextBoxBlock
import kotlin.math.roundToInt

/**
 * Floating / Draggable and Resizable Text Box component (Phase 3).
 */
@Composable
fun TextBoxBlockView(
    block: TextBoxBlock,
    zoomScale: Float = 1f,
    onTextChanged: (String) -> Unit,
    onDeleteBlock: () -> Unit,
    onGeometryChanged: (offsetX: Float, offsetY: Float, widthDp: Int, heightDp: Int) -> Unit = { _, _, _, _ -> },
    onColorChanged: (bgHex: String, borderHex: String) -> Unit = { _, _ -> }
) {
    var textValue by remember(block.text) { mutableStateOf(block.text) }
    var currentOffsetX by remember(block.offsetXPt) { mutableFloatStateOf(block.offsetXPt) }
    var currentOffsetY by remember(block.offsetYPt) { mutableFloatStateOf(block.offsetYPt) }
    var currentWidthDp by remember(block.widthDp) { mutableIntStateOf(block.widthDp) }
    var currentHeightDp by remember(block.heightDp) { mutableIntStateOf(block.heightDp) }
    var showColorPalette by remember { mutableStateOf(false) }

    val bgCol = try { Color(android.graphics.Color.parseColor(block.backgroundColorHex)) } catch (_: Exception) { Color.White }
    val borderCol = try { Color(android.graphics.Color.parseColor(block.borderColorHex)) } catch (_: Exception) { Color(0xFF2563EB) }
    val textCol = try { Color(android.graphics.Color.parseColor(block.textColorHex)) } catch (_: Exception) { Color(0xFF1E293B) }

    val scaledWidthDp = (currentWidthDp * zoomScale).dp
    val scaledHeightDp = (currentHeightDp * zoomScale).dp

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = bgCol,
            border = BorderStroke((block.borderWidthPt * zoomScale).dp, borderCol),
            shadowElevation = 3.dp,
            modifier = Modifier
                .offset { IntOffset((currentOffsetX * zoomScale).roundToInt(), (currentOffsetY * zoomScale).roundToInt()) }
                .width(scaledWidthDp)
                .height(scaledHeightDp)
                .testTag("text_box_block_${block.id}")
        ) {
            Box {
                Column(modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)) {
                    // Draggable Header Bar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .pointerInput(block.id) {
                                detectDragGestures(
                                    onDragEnd = {
                                        onGeometryChanged(currentOffsetX, currentOffsetY, currentWidthDp, currentHeightDp)
                                    }
                                ) { change, dragAmount ->
                                    change.consume()
                                    currentOffsetX = (currentOffsetX + dragAmount.x / zoomScale).coerceIn(-200f, 200f)
                                    currentOffsetY = (currentOffsetY + dragAmount.y / zoomScale).coerceIn(-300f, 300f)
                                }
                            },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.OpenWith,
                            contentDescription = "Drag Text Box",
                            tint = borderCol,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Text Box",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = borderCol,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(
                            onClick = { showColorPalette = !showColorPalette },
                            modifier = Modifier.size(22.dp)
                        ) {
                            Icon(
                                Icons.Default.Palette,
                                contentDescription = "Change Colors",
                                tint = borderCol,
                                modifier = Modifier.size(13.dp)
                            )
                        }
                        IconButton(onClick = onDeleteBlock, modifier = Modifier.size(22.dp)) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "Delete Text Box",
                                tint = Color.Gray,
                                modifier = Modifier.size(13.dp)
                            )
                        }
                    }

                    if (showColorPalette) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 3.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            listOf(
                                Pair("#FFFFFF", "#2563EB"),
                                Pair("#EFF6FF", "#1D4ED8"),
                                Pair("#ECFDF5", "#059669"),
                                Pair("#FEF3C7", "#D97706"),
                                Pair("#FAF5FF", "#7C3AED")
                            ).forEach { (bg, bdr) ->
                                Box(
                                    modifier = Modifier
                                        .size(16.dp)
                                        .background(Color(android.graphics.Color.parseColor(bg)), CircleShape)
                                        .border(BorderStroke(1.5.dp, Color(android.graphics.Color.parseColor(bdr))), CircleShape)
                                        .clickable {
                                            onColorChanged(bg, bdr)
                                            showColorPalette = false
                                        }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    BasicTextField(
                        value = textValue,
                        onValueChange = {
                            textValue = it
                            onTextChanged(it)
                        },
                        textStyle = TextStyle(
                            fontSize = (12f * zoomScale).sp,
                            color = textCol,
                            lineHeight = (16f * zoomScale).sp
                        ),
                        cursorBrush = SolidColor(borderCol),
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .testTag("text_box_input_${block.id}")
                    )
                }

                // Interactive Corner Resize Handle
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(2.dp)
                        .size(18.dp)
                        .pointerInput(block.id) {
                            detectDragGestures(
                                onDragEnd = {
                                    onGeometryChanged(currentOffsetX, currentOffsetY, currentWidthDp, currentHeightDp)
                                }
                            ) { change, dragAmount ->
                                change.consume()
                                val newW = (currentWidthDp + (dragAmount.x / zoomScale)).roundToInt().coerceIn(100, 480)
                                val newH = (currentHeightDp + (dragAmount.y / zoomScale)).roundToInt().coerceIn(60, 360)
                                currentWidthDp = newW
                                currentHeightDp = newH
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AspectRatio,
                        contentDescription = "Resize Text Box",
                        tint = borderCol.copy(alpha = 0.7f),
                        modifier = Modifier.size(12.dp)
                    )
                }
            }
        }
    }
}
