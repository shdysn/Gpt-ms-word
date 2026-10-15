package com.example.ui.editor.components.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import com.example.data.model.ShapeBlock
import com.example.ui.theme.WordPrimary

/**
 * Phase 2 Vector Shapes Gallery Dialog.
 * Supports geometric shapes, callouts, live shape previews, colors, strokes, and rotation.
 */
@Composable
fun VectorShapeGalleryDialog(
    shapeToEdit: ShapeBlock? = null,
    onDismiss: () -> Unit,
    onConfirm: (
        shapeType: String,
        title: String,
        content: String,
        bgHex: String,
        borderHex: String,
        strokeWidthPt: Float,
        rotationDeg: Float,
        widthDp: Int,
        heightDp: Int
    ) -> Unit
) {
    val shapes = VectorShapePresets.shapes
    val colorThemes = VectorShapePresets.colorThemes

    var selectedType by remember { mutableStateOf(shapeToEdit?.shapeType ?: "ROUNDED_RECT") }
    var selectedCategory by remember { mutableStateOf(if (shapeToEdit?.shapeType in listOf("NOTE", "WARNING", "SUCCESS", "QUOTE")) "Callout" else "Geometric") }
    var shapeTitle by remember { mutableStateOf(shapeToEdit?.title ?: "Vector Shape") }
    var shapeContent by remember { mutableStateOf(shapeToEdit?.content ?: "") }
    var selectedBgHex by remember { mutableStateOf(shapeToEdit?.backgroundColorHex ?: "#EFF6FF") }
    var selectedBorderHex by remember { mutableStateOf(shapeToEdit?.borderColorHex ?: "#3B82F6") }
    var strokeWidthPt by remember { mutableStateOf(shapeToEdit?.strokeWidthPt ?: 1.5f) }
    var rotationDeg by remember { mutableStateOf(shapeToEdit?.rotationDeg ?: 0f) }
    var selectedSizeOption by remember { mutableStateOf("Standard") }

    val (wDp, hDp) = when (selectedSizeOption) {
        "Compact" -> 180 to 90
        "Wide" -> 280 to 120
        else -> 220 to 110
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Category, contentDescription = null, tint = WordPrimary, modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (shapeToEdit != null) "Edit Vector Shape" else "Vector Shapes Gallery",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Interactive Shape Preview
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(110.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFF1F5F9))
                        .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    MiniShapePreview(
                        shapeType = selectedType,
                        bgHex = selectedBgHex,
                        borderHex = selectedBorderHex,
                        strokeWidthPt = strokeWidthPt,
                        rotationDeg = rotationDeg,
                        title = shapeTitle
                    )
                }

                // Category Tabs
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf("Geometric", "Callout").forEach { cat ->
                        FilterChip(
                            selected = selectedCategory == cat,
                            onClick = { selectedCategory = cat },
                            label = { Text(cat, fontSize = 12.sp) }
                        )
                    }
                }

                // Shapes Grid / Carousel
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    shapes.filter { it.category == selectedCategory }.forEach { sh ->
                        FilterChip(
                            selected = selectedType == sh.type,
                            onClick = {
                                selectedType = sh.type
                                if (shapeToEdit == null && shapeTitle in listOf("Vector Shape", "Important Note", "Warning", "Quote")) {
                                    shapeTitle = sh.label
                                }
                            },
                            label = { Text(sh.label, fontSize = 11.sp) }
                        )
                    }
                }

                // Title & Content Inputs
                OutlinedTextField(
                    value = shapeTitle,
                    onValueChange = { shapeTitle = it },
                    label = { Text("Shape Label / Title") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("shape_title_input")
                )

                OutlinedTextField(
                    value = shapeContent,
                    onValueChange = { shapeContent = it },
                    label = { Text("Interior Text (Optional)") },
                    placeholder = { Text("Details or announcement...") },
                    maxLines = 2,
                    modifier = Modifier.fillMaxWidth().testTag("shape_content_input")
                )

                // Color Palette Swatches
                Text("Color Theme:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    colorThemes.forEach { (bg, bdr, name) ->
                        val isSel = selectedBgHex == bg && selectedBorderHex == bdr
                        val bgC = try { Color(android.graphics.Color.parseColor(bg)) } catch (_: Exception) { Color.White }
                        val bdrC = try { Color(android.graphics.Color.parseColor(bdr)) } catch (_: Exception) { Color.Blue }
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(bgC)
                                .border(if (isSel) 2.5.dp else 1.dp, if (isSel) WordPrimary else bdrC, CircleShape)
                                .clickable {
                                    selectedBgHex = bg
                                    selectedBorderHex = bdr
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            if (isSel) {
                                Icon(Icons.Default.Check, contentDescription = name, tint = bdrC, modifier = Modifier.size(14.dp))
                            }
                        }
                    }
                }

                // Stroke Width Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Stroke Width:", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        listOf(1f to "1pt", 2f to "2pt", 3.5f to "3.5pt").forEach { (pt, lbl) ->
                            FilterChip(
                                selected = strokeWidthPt == pt,
                                onClick = { strokeWidthPt = pt },
                                label = { Text(lbl, fontSize = 10.sp) },
                                modifier = Modifier.height(28.dp)
                            )
                        }
                    }
                }

                // Rotation Controls
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Rotation:", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        listOf(0f to "0°", 15f to "15°", 45f to "45°", 90f to "90°").forEach { (deg, lbl) ->
                            FilterChip(
                                selected = rotationDeg == deg,
                                onClick = { rotationDeg = deg },
                                label = { Text(lbl, fontSize = 10.sp) },
                                modifier = Modifier.height(28.dp)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm(
                        selectedType,
                        shapeTitle.trim().ifBlank { "Shape" },
                        shapeContent.trim(),
                        selectedBgHex,
                        selectedBorderHex,
                        strokeWidthPt,
                        rotationDeg,
                        wDp,
                        hDp
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = WordPrimary),
                modifier = Modifier.testTag("confirm_insert_shape_btn")
            ) {
                Text(if (shapeToEdit != null) "Update Shape" else "Insert Shape")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
