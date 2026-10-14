package com.example.ui.editor.components.dialogs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.WordPrimary

/**
 * Dialog for inserting Callouts / Shape Note blocks into the document.
 */
@Composable
fun ShapeInsertDialog(
    onDismiss: () -> Unit,
    onConfirm: (shapeType: String, title: String, content: String, bgHex: String, borderHex: String) -> Unit
) {
    var selectedType by remember { mutableStateOf("NOTE") }
    var shapeTitle by remember { mutableStateOf("Important Note") }
    var shapeContent by remember { mutableStateOf("") }

    val presetColors = when (selectedType) {
        "WARNING" -> "#FEF3C7" to "#D97706"
        "SUCCESS" -> "#ECFDF5" to "#059669"
        "QUOTE" -> "#F3E8FF" to "#7C3AED"
        else -> "#EFF6FF" to "#3B82F6"
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Insert Callout / Shape Box") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Select Shape Style:", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    FilterChip(
                        selected = selectedType == "NOTE",
                        onClick = {
                            selectedType = "NOTE"
                            if (shapeTitle in listOf("Warning", "Tip", "Quote")) shapeTitle = "Important Note"
                        },
                        label = { Text("Note") }
                    )
                    FilterChip(
                        selected = selectedType == "WARNING",
                        onClick = {
                            selectedType = "WARNING"
                            shapeTitle = "Warning"
                        },
                        label = { Text("Warning") }
                    )
                    FilterChip(
                        selected = selectedType == "SUCCESS",
                        onClick = {
                            selectedType = "SUCCESS"
                            shapeTitle = "Tip"
                        },
                        label = { Text("Tip / Key") }
                    )
                    FilterChip(
                        selected = selectedType == "QUOTE",
                        onClick = {
                            selectedType = "QUOTE"
                            shapeTitle = "Quote"
                        },
                        label = { Text("Quote") }
                    )
                }

                OutlinedTextField(
                    value = shapeTitle,
                    onValueChange = { shapeTitle = it },
                    label = { Text("Title / Header") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = shapeContent,
                    onValueChange = { shapeContent = it },
                    label = { Text("Content Text") },
                    placeholder = { Text("Enter note, citation, or announcement details...") },
                    minLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm(
                        selectedType,
                        shapeTitle.trim().ifBlank { "Note" },
                        shapeContent.trim(),
                        presetColors.first,
                        presetColors.second
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = WordPrimary)
            ) {
                Text("Insert Shape")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
