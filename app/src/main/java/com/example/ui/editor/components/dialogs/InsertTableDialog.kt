package com.example.ui.editor.components.dialogs

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilterChip
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.WordPrimary

/**
 * Dialog for inserting structured tables into the document.
 */
@Composable
fun InsertTableDialog(
    onDismiss: () -> Unit,
    onConfirm: (rows: Int, cols: Int, hasBorders: Boolean, widthPercent: Int) -> Unit
) {
    var rowsText by remember { mutableStateOf("3") }
    var colsText by remember { mutableStateOf("3") }
    var hasBorders by remember { mutableStateOf(true) }
    var selectedWidthPercent by remember { mutableStateOf(100) }

    val rowsInt = rowsText.toIntOrNull() ?: 3
    val colsInt = colsText.toIntOrNull() ?: 3

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = WordPrimary.copy(alpha = 0.12f),
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.TableChart,
                            contentDescription = null,
                            tint = WordPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text("Insert Table", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text("Customize grid dimensions & width", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Dimensions section with stepper buttons
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                    border = androidx.compose.foundation.BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("Dimensions", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)

                        // Rows Stepper & Input
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("Rows (1–10)", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                                Text("Current: $rowsInt", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    shape = CircleShape,
                                    color = MaterialTheme.colorScheme.surface,
                                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clickable {
                                            val newR = (rowsInt - 1).coerceAtLeast(1)
                                            rowsText = newR.toString()
                                        }
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(Icons.Default.Remove, contentDescription = "Decrease rows", modifier = Modifier.size(16.dp))
                                    }
                                }
                                OutlinedTextField(
                                    value = rowsText,
                                    onValueChange = { rowsText = it.filter { ch -> ch.isDigit() }.take(2) },
                                    singleLine = true,
                                    textStyle = androidx.compose.ui.text.TextStyle(
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    modifier = Modifier
                                        .width(64.dp)
                                        .padding(horizontal = 6.dp)
                                        .testTag("table_rows_input")
                                )
                                Surface(
                                    shape = CircleShape,
                                    color = MaterialTheme.colorScheme.surface,
                                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clickable {
                                            val newR = (rowsInt + 1).coerceAtMost(10)
                                            rowsText = newR.toString()
                                        }
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(Icons.Default.Add, contentDescription = "Increase rows", modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                        }

                        // Columns Stepper & Input
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("Columns (1–6)", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                                Text("Current: $colsInt", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    shape = CircleShape,
                                    color = MaterialTheme.colorScheme.surface,
                                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clickable {
                                            val newC = (colsInt - 1).coerceAtLeast(1)
                                            colsText = newC.toString()
                                        }
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(Icons.Default.Remove, contentDescription = "Decrease columns", modifier = Modifier.size(16.dp))
                                    }
                                }
                                OutlinedTextField(
                                    value = colsText,
                                    onValueChange = { colsText = it.filter { ch -> ch.isDigit() }.take(1) },
                                    singleLine = true,
                                    textStyle = androidx.compose.ui.text.TextStyle(
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    modifier = Modifier
                                        .width(64.dp)
                                        .padding(horizontal = 6.dp)
                                        .testTag("table_cols_input")
                                )
                                Surface(
                                    shape = CircleShape,
                                    color = MaterialTheme.colorScheme.surface,
                                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clickable {
                                            val newC = (colsInt + 1).coerceAtMost(6)
                                            colsText = newC.toString()
                                        }
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(Icons.Default.Add, contentDescription = "Increase columns", modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                        }
                    }
                }

                // Table Width Options
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Table Width:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(50, 75, 100).forEach { w ->
                            FilterChip(
                                selected = selectedWidthPercent == w,
                                onClick = { selectedWidthPercent = w },
                                label = { Text("$w% Width", fontSize = 11.sp) }
                            )
                        }
                    }
                }

                // Border Toggle Card
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { hasBorders = !hasBorders }
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Checkbox(
                            checked = hasBorders,
                            onCheckedChange = { hasBorders = it },
                            modifier = Modifier.testTag("table_borders_checkbox")
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Column {
                            Text("Include Table Grid Borders", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                            Text("Draw subtle border grid lines between cells", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val r = rowsText.toIntOrNull()?.coerceIn(1, 10) ?: 3
                    val c = colsText.toIntOrNull()?.coerceIn(1, 6) ?: 3
                    onConfirm(r, c, hasBorders, selectedWidthPercent)
                },
                colors = ButtonDefaults.buttonColors(containerColor = WordPrimary),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.testTag("confirm_insert_table")
            ) {
                Text("Insert Table")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
