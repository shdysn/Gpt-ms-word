package com.example.ui.editor.components.dialogs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.data.model.ParagraphBlock
import com.example.data.model.RichDocument
import com.example.ui.editor.components.StatRow
import com.example.ui.theme.WordPrimary

@Composable
fun EditTitleDialog(
    initialTitle: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var editedTitle by remember { mutableStateOf(initialTitle) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Document Title") },
        text = {
            OutlinedTextField(
                value = editedTitle,
                onValueChange = { editedTitle = it },
                label = { Text("Title") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("edit_title_input")
            )
        },
        confirmButton = {
            Button(
                onClick = {
                    if (editedTitle.isNotBlank()) {
                        onConfirm(editedTitle.trim())
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = WordPrimary)
            ) {
                Text("Done")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun DocumentStatisticsDialog(
    document: RichDocument?,
    actualPageCount: Int,
    activeParagraph: ParagraphBlock?,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Document Statistics") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                StatRow("Words", "${document?.wordCount ?: 0}")
                StatRow("Characters", "${document?.characterCount ?: 0}")
                StatRow("Paragraphs", "${document?.blocks?.count { it is ParagraphBlock } ?: 0}")
                StatRow("Pages", "$actualPageCount")
                StatRow("Paper Size", document?.layoutConfig?.pageSize?.displayName ?: "A4")
                StatRow("Orientation", document?.layoutConfig?.orientation?.name ?: "PORTRAIT")
                StatRow("Active Language", if (activeParagraph?.isRtl == true) "Urdu (RTL Nastaliq)" else "English (LTR)")
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = WordPrimary)
            ) {
                Text("Close")
            }
        }
    )
}
