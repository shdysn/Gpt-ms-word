package com.example.ui.editor.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import com.example.core.layout.MarginPreset
import com.example.core.layout.PageLayoutConfig
import com.example.core.layout.PageOrientation
import com.example.core.layout.PageSize
import com.example.ui.theme.WordPrimary

/**
 * Dialog for inserting structured tables into the document.
 */
@Composable
fun InsertTableDialog(
    onDismiss: () -> Unit,
    onConfirm: (rows: Int, cols: Int) -> Unit
) {
    var rowsText by remember { mutableStateOf("3") }
    var colsText by remember { mutableStateOf("3") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Insert Table") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Specify table dimensions:",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                OutlinedTextField(
                    value = rowsText,
                    onValueChange = { rowsText = it },
                    label = { Text("Number of Rows (1-10)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("table_rows_input")
                )
                OutlinedTextField(
                    value = colsText,
                    onValueChange = { colsText = it },
                    label = { Text("Number of Columns (1-6)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("table_cols_input")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val r = rowsText.toIntOrNull()?.coerceIn(1, 10) ?: 3
                    val c = colsText.toIntOrNull()?.coerceIn(1, 6) ?: 3
                    onConfirm(r, c)
                },
                colors = ButtonDefaults.buttonColors(containerColor = WordPrimary),
                modifier = Modifier.testTag("confirm_insert_table")
            ) {
                Text("Insert")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

/**
 * Dialog for configuring page layout, paper size, margins, and headers/footers.
 */
@Composable
fun PageLayoutDialog(
    currentLayout: PageLayoutConfig,
    onDismiss: () -> Unit,
    onSave: (PageSize, PageOrientation, MarginPreset, String, String) -> Unit
) {
    var selectedPageSize by remember { mutableStateOf(currentLayout.pageSize) }
    var selectedOrientation by remember { mutableStateOf(currentLayout.orientation) }
    var selectedMargins by remember { mutableStateOf(currentLayout.marginPreset) }
    var headerText by remember { mutableStateOf(currentLayout.headerText) }
    var footerText by remember { mutableStateOf(currentLayout.footerText) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Page Setup") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // Paper Size
                Text("Paper Size", fontWeight = FontWeight.SemiBold)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf(PageSize.A4, PageSize.LETTER, PageSize.LEGAL).forEach { ps ->
                        FilterChip(
                            selected = selectedPageSize == ps,
                            onClick = { selectedPageSize = ps },
                            label = { Text(ps.name, fontSize = 11.sp) }
                        )
                    }
                }

                HorizontalDivider()

                // Orientation
                Text("Orientation", fontWeight = FontWeight.SemiBold)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf(PageOrientation.PORTRAIT, PageOrientation.LANDSCAPE).forEach { po ->
                        FilterChip(
                            selected = selectedOrientation == po,
                            onClick = { selectedOrientation = po },
                            label = { Text(po.name, fontSize = 11.sp) }
                        )
                    }
                }

                HorizontalDivider()

                // Margins
                Text("Margins", fontWeight = FontWeight.SemiBold)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf(MarginPreset.NORMAL, MarginPreset.NARROW, MarginPreset.WIDE).forEach { mp ->
                        FilterChip(
                            selected = selectedMargins == mp,
                            onClick = { selectedMargins = mp },
                            label = { Text(mp.displayName.split(" ")[0], fontSize = 11.sp) }
                        )
                    }
                }

                HorizontalDivider()

                // Header & Footer
                OutlinedTextField(
                    value = headerText,
                    onValueChange = { headerText = it },
                    label = { Text("Header Text") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = footerText,
                    onValueChange = { footerText = it },
                    label = { Text("Footer Text (use {page} and {pages})") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(selectedPageSize, selectedOrientation, selectedMargins, headerText, footerText)
                },
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
