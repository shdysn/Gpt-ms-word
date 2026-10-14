package com.example.ui.editor.components.dialogs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CellVerticalAlignment
import com.example.data.model.TableCell
import com.example.ui.theme.WordPrimary

/**
 * Dialog for Table Cell Properties (Vertical Alignment, Shading, Merge/Split).
 */
@Composable
fun TableCellPropertiesDialog(
    cell: TableCell,
    onDismiss: () -> Unit,
    onApply: (vAlign: CellVerticalAlignment, bgColorHex: String?) -> Unit,
    onMergeCells: () -> Unit,
    onSplitCell: () -> Unit
) {
    var vAlign by remember { mutableStateOf(cell.verticalAlignment) }
    var bgColorHex by remember { mutableStateOf(cell.backgroundColorHex) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Cell Formatting & Alignment", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Vertical Alignment in Cell:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf(
                        CellVerticalAlignment.TOP to "Top",
                        CellVerticalAlignment.CENTER to "Center",
                        CellVerticalAlignment.BOTTOM to "Bottom"
                    ).forEach { (align, label) ->
                        FilterChip(
                            selected = vAlign == align,
                            onClick = { vAlign = align },
                            label = { Text(label, fontSize = 11.sp) }
                        )
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                Text("Cell Shading (Background):", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf(
                        null to "None",
                        "#F1F5F9" to "Light Gray",
                        "#EFF6FF" to "Blue Tint",
                        "#FEF3C7" to "Warm Yellow"
                    ).forEach { (hex, name) ->
                        FilterChip(
                            selected = bgColorHex == hex,
                            onClick = { bgColorHex = hex },
                            label = { Text(name, fontSize = 11.sp) }
                        )
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                Text("Cell Merge & Split:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = { onMergeCells(); onDismiss() }) {
                        Text("Merge with Next Col", fontSize = 11.sp)
                    }
                    if (cell.colSpan > 1) {
                        OutlinedButton(onClick = { onSplitCell(); onDismiss() }) {
                            Text("Split Cell", fontSize = 11.sp)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onApply(vAlign, bgColorHex) },
                colors = ButtonDefaults.buttonColors(containerColor = WordPrimary)
            ) {
                Text("Apply")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
