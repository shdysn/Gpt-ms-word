package com.example.ui.editor.components.dialogs

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.TableCell
import com.example.data.model.TableBlock
import com.example.data.model.TextAlignment
import com.example.ui.theme.WordPrimary

/**
 * Dialog for editing cell contents and cell-level formatting (bold, italic, alignment, background).
 */
@Composable
fun TableCellEditDialog(
    cell: TableCell,
    row: Int,
    col: Int,
    onDismiss: () -> Unit,
    onConfirm: (text: String, isBold: Boolean, isItalic: Boolean, bgHex: String?, alignment: TextAlignment) -> Unit
) {
    var text by remember { mutableStateOf(cell.text) }
    var isBold by remember { mutableStateOf(cell.isBold) }
    var isItalic by remember { mutableStateOf(cell.isItalic) }
    var alignment by remember { mutableStateOf(cell.alignment) }
    var bgHex by remember { mutableStateOf(cell.backgroundColorHex) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Cell (${row + 1}, ${col + 1})") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    label = { Text("Cell Text") },
                    modifier = Modifier.fillMaxWidth().testTag("cell_text_input")
                )
                Text("Formatting & Style:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    FilterChip(
                        selected = isBold,
                        onClick = { isBold = !isBold },
                        label = { Text("Bold", fontWeight = FontWeight.Bold) }
                    )
                    FilterChip(
                        selected = isItalic,
                        onClick = { isItalic = !isItalic },
                        label = { Text("Italic", fontStyle = androidx.compose.ui.text.font.FontStyle.Italic) }
                    )
                }
                Text("Cell Alignment:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf(
                        TextAlignment.START to "Left",
                        TextAlignment.CENTER to "Center",
                        TextAlignment.END to "Right"
                    ).forEach { (align, name) ->
                        FilterChip(
                            selected = alignment == align,
                            onClick = { alignment = align },
                            label = { Text(name, fontSize = 11.sp) }
                        )
                    }
                }
                Text("Cell Shading:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(
                        null to "None",
                        "#F1F5F9" to "Light Gray",
                        "#E0F2FE" to "Soft Blue",
                        "#FEF9C3" to "Soft Yellow",
                        "#DCFCE7" to "Soft Green"
                    ).forEach { (hex, name) ->
                        FilterChip(
                            selected = bgHex == hex,
                            onClick = { bgHex = hex },
                            label = { Text(name, fontSize = 10.sp) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm(text, isBold, isItalic, bgHex, alignment)
                },
                colors = ButtonDefaults.buttonColors(containerColor = WordPrimary),
                modifier = Modifier.testTag("confirm_cell_edit")
            ) {
                Text("Done")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

/**
 * Dialog for configuring table properties: row/col addition and deletion, borders, and width.
 */
@Composable
fun TablePropertiesDialog(
    table: TableBlock,
    onDismiss: () -> Unit,
    onAddRow: () -> Unit,
    onDeleteRow: () -> Unit,
    onAddCol: () -> Unit,
    onDeleteCol: () -> Unit,
    onUpdateProperties: (hasBorders: Boolean, borderColorHex: String, widthPercent: Int, alignment: TextAlignment) -> Unit,
    onDeleteTable: () -> Unit
) {
    var hasBorders by remember { mutableStateOf(table.hasBorders) }
    var borderColorHex by remember { mutableStateOf(table.borderColorHex) }
    var widthPercent by remember { mutableStateOf(table.tableWidthPercent) }
    var alignment by remember { mutableStateOf(table.alignment) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Table Options (${table.rowCount} × ${table.columnCount})") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("Rows & Columns:", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onAddRow,
                        modifier = Modifier.weight(1f).testTag("add_table_row_btn"),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer, contentColor = MaterialTheme.colorScheme.onSecondaryContainer)
                    ) { Text("+ Row", fontSize = 12.sp) }

                    Button(
                        onClick = onDeleteRow,
                        enabled = table.rowCount > 1,
                        modifier = Modifier.weight(1f).testTag("delete_table_row_btn"),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer, contentColor = MaterialTheme.colorScheme.onSecondaryContainer)
                    ) { Text("- Row", fontSize = 12.sp) }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onAddCol,
                        modifier = Modifier.weight(1f).testTag("add_table_col_btn"),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer, contentColor = MaterialTheme.colorScheme.onSecondaryContainer)
                    ) { Text("+ Column", fontSize = 12.sp) }

                    Button(
                        onClick = onDeleteCol,
                        enabled = table.columnCount > 1,
                        modifier = Modifier.weight(1f).testTag("delete_table_col_btn"),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer, contentColor = MaterialTheme.colorScheme.onSecondaryContainer)
                    ) { Text("- Column", fontSize = 12.sp) }
                }

                HorizontalDivider()

                Text("Table Borders:", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { hasBorders = !hasBorders }
                ) {
                    Checkbox(checked = hasBorders, onCheckedChange = { hasBorders = it })
                    Text("Display borders", fontSize = 13.sp)
                }

                if (hasBorders) {
                    Text("Border Color:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(
                            "#CBD5E1" to "Classic Gray",
                            "#185ABD" to "Word Blue",
                            "#000000" to "Black",
                            "#EF4444" to "Red"
                        ).forEach { (hex, name) ->
                            FilterChip(
                                selected = borderColorHex.equals(hex, ignoreCase = true),
                                onClick = { borderColorHex = hex },
                                label = { Text(name, fontSize = 10.sp) }
                            )
                        }
                    }
                }

                HorizontalDivider()

                Text("Table Width & Sizing:", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(50, 75, 100).forEach { w ->
                        FilterChip(
                            selected = widthPercent == w,
                            onClick = { widthPercent = w },
                            label = { Text("$w% Width", fontSize = 11.sp) }
                        )
                    }
                }

                Text("Table Alignment:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(
                        TextAlignment.START to "Left",
                        TextAlignment.CENTER to "Center",
                        TextAlignment.END to "Right"
                    ).forEach { (align, name) ->
                        FilterChip(
                            selected = alignment == align,
                            onClick = { alignment = align },
                            label = { Text(name, fontSize = 11.sp) }
                        )
                    }
                }

                HorizontalDivider()

                Button(
                    onClick = {
                        onDeleteTable()
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.errorContainer, contentColor = MaterialTheme.colorScheme.onErrorContainer),
                    modifier = Modifier.fillMaxWidth().testTag("delete_table_btn")
                ) {
                    Text("Delete Entire Table")
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onUpdateProperties(hasBorders, borderColorHex, widthPercent, alignment)
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = WordPrimary),
                modifier = Modifier.testTag("apply_table_properties")
            ) {
                Text("Apply")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Close") }
        }
    )
}
