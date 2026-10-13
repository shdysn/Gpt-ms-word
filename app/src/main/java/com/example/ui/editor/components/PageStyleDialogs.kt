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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BorderAll
import androidx.compose.material.icons.filled.FormatListNumbered
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
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
import com.example.core.layout.PageBackgroundPreset
import com.example.core.layout.PageBorderStyle
import com.example.ui.theme.WordPrimary

/**
 * Dialog for selecting and customizing page border frames.
 */
@Composable
fun PageBordersDialog(
    initialStyle: PageBorderStyle,
    initialColorHex: String,
    onDismiss: () -> Unit,
    onApply: (style: PageBorderStyle, colorHex: String) -> Unit
) {
    var selectedStyle by remember { mutableStateOf(initialStyle) }
    var selectedColorHex by remember { mutableStateOf(initialColorHex) }

    val colors = listOf(
        "#CBD5E1" to "Slate",
        "#1E3A8A" to "Navy",
        "#991B1B" to "Burgundy",
        "#047857" to "Emerald",
        "#B45309" to "Gold",
        "#0F172A" to "Black"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.BorderAll, contentDescription = null, tint = WordPrimary, modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Page Borders & Framing", style = MaterialTheme.typography.titleMedium)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Border Style:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                PageBorderStyle.entries.forEach { style ->
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (selectedStyle == style) Color(0xFFEFF6FF) else Color(0xFFF8FAFC),
                        border = BorderStroke(1.dp, if (selectedStyle == style) WordPrimary else Color(0xFFCBD5E1)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedStyle = style }
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(style.displayName, fontSize = 12.sp, fontWeight = if (selectedStyle == style) FontWeight.Bold else FontWeight.Normal)
                            if (selectedStyle == style) {
                                Text("✓ Selected", color = WordPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                HorizontalDivider()

                Text("Border Color:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    colors.forEach { (hex, name) ->
                        val col = try { Color(android.graphics.Color.parseColor(hex)) } catch (_: Exception) { Color.Black }
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(col)
                                .border(
                                    width = if (selectedColorHex == hex) 2.5.dp else 1.dp,
                                    color = if (selectedColorHex == hex) WordPrimary else Color.LightGray,
                                    shape = CircleShape
                                )
                                .clickable { selectedColorHex = hex }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onApply(selectedStyle, selectedColorHex)
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = WordPrimary)
            ) {
                Text("Apply Border")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

/**
 * Dialog for selecting page background tint color.
 */
@Composable
fun PageColorDialog(
    initialColorHex: String,
    onDismiss: () -> Unit,
    onApply: (colorHex: String) -> Unit
) {
    var selectedHex by remember { mutableStateOf(initialColorHex) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Palette, contentDescription = null, tint = WordPrimary, modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Page Background Color", style = MaterialTheme.typography.titleMedium)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Select Paper Tint Preset:", fontSize = 12.sp, color = Color.Gray)
                PageBackgroundPreset.entries.forEach { preset ->
                    val presetColor = try { Color(android.graphics.Color.parseColor(preset.colorHex)) } catch (_: Exception) { Color.White }
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (selectedHex.equals(preset.colorHex, ignoreCase = true)) Color(0xFFEFF6FF) else Color(0xFFF8FAFC),
                        border = BorderStroke(1.dp, if (selectedHex.equals(preset.colorHex, ignoreCase = true)) WordPrimary else Color(0xFFCBD5E1)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedHex = preset.colorHex }
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(presetColor)
                                    .border(1.dp, Color.Gray, RoundedCornerShape(4.dp))
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(preset.displayName, fontSize = 13.sp, modifier = Modifier.weight(1f))
                            if (selectedHex.equals(preset.colorHex, ignoreCase = true)) {
                                Text("✓ Active", color = WordPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onApply(selectedHex)
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = WordPrimary)
            ) {
                Text("Apply Color")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

/**
 * Dialog for inserting a footnote into the active document location.
 */
@Composable
fun InsertFootnoteDialog(
    defaultReferenceText: String = "",
    onDismiss: () -> Unit,
    onConfirm: (referenceText: String, noteContent: String) -> Unit
) {
    var refText by remember { mutableStateOf(defaultReferenceText) }
    var noteContent by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.FormatListNumbered, contentDescription = null, tint = WordPrimary, modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Insert Footnote / Citation", style = MaterialTheme.typography.titleMedium)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Attach citation or explanatory note at the bottom of the page:", fontSize = 12.sp, color = Color.Gray)

                OutlinedTextField(
                    value = refText,
                    onValueChange = { refText = it },
                    label = { Text("Referenced Term / Text") },
                    placeholder = { Text("e.g. Oxford Reference / قانون شہادت") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("footnote_ref_input")
                )

                OutlinedTextField(
                    value = noteContent,
                    onValueChange = { noteContent = it },
                    label = { Text("Footnote Explanation / Source") },
                    placeholder = { Text("Enter detailed bibliographic citation, note, or reference...") },
                    minLines = 3,
                    modifier = Modifier.fillMaxWidth().testTag("footnote_content_input")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (noteContent.isNotBlank()) {
                        onConfirm(refText.trim(), noteContent.trim())
                    }
                },
                enabled = noteContent.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = WordPrimary),
                modifier = Modifier.testTag("confirm_footnote_btn")
            ) {
                Text("Insert Footnote")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
