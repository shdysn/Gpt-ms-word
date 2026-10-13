package com.example.ui.home.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.DocumentEntity
import com.example.data.repository.TemplateType
import com.example.ui.theme.WordPrimary

@Composable
fun HomeSettingsDialog(
    isAutosaveEnabled: Boolean,
    onAutosaveChanged: (Boolean) -> Unit,
    defaultRtl: Boolean,
    onDefaultRtlChanged: (Boolean) -> Unit,
    showWordCount: Boolean,
    onShowWordCountChanged: (Boolean) -> Unit,
    showRuler: Boolean,
    onShowRulerChanged: (Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Settings, contentDescription = null, tint = WordPrimary)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Editor Settings")
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Automatic Saving", fontWeight = FontWeight.SemiBold)
                        Text("Save edits automatically to device storage", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Switch(
                        checked = isAutosaveEnabled,
                        onCheckedChange = onAutosaveChanged
                    )
                }

                HorizontalDivider()

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Urdu / RTL Support", fontWeight = FontWeight.SemiBold)
                        Text("Default to Right-to-Left bidirectional typing", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Switch(
                        checked = defaultRtl,
                        onCheckedChange = onDefaultRtlChanged
                    )
                }

                HorizontalDivider()

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Word Count Status Bar", fontWeight = FontWeight.SemiBold)
                        Text("Display live word and character counters", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Switch(
                        checked = showWordCount,
                        onCheckedChange = onShowWordCountChanged
                    )
                }

                HorizontalDivider()

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Document Ruler", fontWeight = FontWeight.SemiBold)
                        Text("Display page dimensions and margins guide", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Switch(
                        checked = showRuler,
                        onCheckedChange = onShowRulerChanged
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = WordPrimary)
            ) {
                Text("Done")
            }
        }
    )
}

@Composable
fun CreateDocumentDialog(
    initialTemplate: TemplateType = TemplateType.BLANK,
    onDismiss: () -> Unit,
    onConfirm: (title: String, template: TemplateType) -> Unit
) {
    var newTitle by remember { mutableStateOf("") }
    var selectedTemplate by remember { mutableStateOf(initialTemplate) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create New Document") },
        text = {
            Column {
                OutlinedTextField(
                    value = newTitle,
                    onValueChange = { newTitle = it },
                    label = { Text("Document Title") },
                    placeholder = { Text(selectedTemplate.title) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text("Select Template:", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(8.dp))
                TemplateType.entries.forEach { t ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedTemplate = t }
                            .padding(vertical = 2.dp)
                    ) {
                        RadioButton(
                            selected = selectedTemplate == t,
                            onClick = { selectedTemplate = t }
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = t.title,
                            color = if (selectedTemplate == t) WordPrimary else MaterialTheme.colorScheme.onSurface,
                            fontWeight = if (selectedTemplate == t) FontWeight.Bold else FontWeight.Normal,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val finalTitle = newTitle.ifBlank { selectedTemplate.title }
                    onConfirm(finalTitle, selectedTemplate)
                },
                colors = ButtonDefaults.buttonColors(containerColor = WordPrimary)
            ) {
                Text("Create")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun RenameDocumentDialog(
    doc: DocumentEntity,
    onDismiss: () -> Unit,
    onConfirm: (newTitle: String) -> Unit
) {
    var renameText by remember { mutableStateOf(doc.title) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Rename Document") },
        text = {
            OutlinedTextField(
                value = renameText,
                onValueChange = { renameText = it },
                label = { Text("New Title") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            Button(
                onClick = {
                    if (renameText.isNotBlank()) {
                        onConfirm(renameText.trim())
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = WordPrimary)
            ) {
                Text("Rename")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun DeleteDocumentDialog(
    doc: DocumentEntity,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Delete Document") },
        text = { Text("Are you sure you want to delete '${doc.title}'? This action cannot be undone.") },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Text("Delete")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
