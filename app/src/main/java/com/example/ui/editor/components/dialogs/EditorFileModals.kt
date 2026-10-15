package com.example.ui.editor.components.dialogs

import android.content.Context
import androidx.activity.result.ActivityResultLauncher
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.RichDocument
import com.example.ui.editor.EditorViewModel
import com.example.ui.editor.components.DiscardChangesDialog
import com.example.ui.editor.components.EditTitleDialog
import com.example.ui.editor.components.ExportOptionsDialog
import com.example.ui.editor.components.SaveAsDialog
import com.example.ui.editor.components.SaveOptionsDialog
import com.example.ui.editor.components.shareDocxFile
import com.example.ui.editor.components.shareEpubFile
import com.example.ui.editor.components.shareMarkdownFile
import com.example.ui.editor.components.shareTextFile
import com.example.ui.theme.WordPrimary
import kotlinx.coroutines.launch

@Composable
fun EditorFileModals(
    viewModel: EditorViewModel,
    document: RichDocument?,
    context: Context,
    showTitleEditDialog: Boolean,
    onDismissTitleEdit: () -> Unit,
    showSaveOptionsDialog: Boolean,
    onDismissSaveOptions: () -> Unit,
    onOpenSaveAs: () -> Unit,
    showExportOptionsDialog: Boolean,
    onDismissExportOptions: () -> Unit,
    onOpenExportPdf: () -> Unit,
    showSaveAsDialog: Boolean,
    onDismissSaveAs: () -> Unit,
    showDiscardDialog: Boolean,
    onDismissDiscard: () -> Unit,
    onNavigateBack: () -> Unit,
    isDocumentLocked: Boolean,
    safDocxSaveLauncher: ActivityResultLauncher<String>
) {
    val coroutineScope = rememberCoroutineScope()

    if (showTitleEditDialog && document != null) {
        EditTitleDialog(
            initialTitle = document.title,
            onDismiss = onDismissTitleEdit,
            onConfirm = { newTitle ->
                viewModel.updateTitle(newTitle)
                onDismissTitleEdit()
            }
        )
    }

    if (showSaveOptionsDialog) {
        SaveOptionsDialog(
            onDismiss = onDismissSaveOptions,
            onQuickSave = {
                onDismissSaveOptions()
                viewModel.saveNow()
            },
            onSaveAs = {
                onDismissSaveOptions()
                onOpenSaveAs()
            },
            onSaveSafDocx = {
                onDismissSaveOptions()
                val safeTitle = (document?.title ?: "document").replace("[^a-zA-Z0-9.-]".toRegex(), "_")
                safDocxSaveLauncher.launch("$safeTitle.docx")
            }
        )
    }

    if (showExportOptionsDialog) {
        ExportOptionsDialog(
            onDismiss = onDismissExportOptions,
            onExportPdf = {
                onDismissExportOptions()
                onOpenExportPdf()
            },
            onExportDocx = {
                onDismissExportOptions()
                viewModel.exportToDocx(context) { file ->
                    file?.let { shareDocxFile(context, it) }
                }
            },
            onExportPlainText = {
                onDismissExportOptions()
                viewModel.exportToPlainText(context) { file ->
                    file?.let { shareTextFile(context, it) }
                }
            },
            onExportMarkdown = {
                onDismissExportOptions()
                viewModel.exportToMarkdown(context) { file ->
                    file?.let { shareMarkdownFile(context, it) }
                }
            },
            onExportEpub = {
                onDismissExportOptions()
                viewModel.exportToEpub(context) { file ->
                    file?.let { shareEpubFile(context, it) }
                }
            }
        )
    }

    if (showSaveAsDialog) {
        SaveAsDialog(
            initialTitle = "${document?.title ?: "Document"} (Copy)",
            onDismiss = onDismissSaveAs,
            onConfirm = { copyTitle ->
                viewModel.saveAs(copyTitle) {
                    onDismissSaveAs()
                }
            }
        )
    }

    if (showDiscardDialog) {
        DiscardChangesDialog(
            docTitle = document?.title ?: "this document",
            onDismiss = onDismissDiscard,
            onSaveAndExit = {
                coroutineScope.launch {
                    viewModel.flushSave()
                    onDismissDiscard()
                    onNavigateBack()
                }
            },
            onDiscard = {
                viewModel.dismissRecoveryDraft()
                onDismissDiscard()
                onNavigateBack()
            }
        )
    }

    if (isDocumentLocked) {
        var unlockPasswordInput by remember { mutableStateOf("") }
        var unlockError by remember { mutableStateOf<String?>(null) }

        AlertDialog(
            onDismissRequest = { /* Require valid PIN or back navigation */ },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        tint = WordPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("Document Locked", fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        "This document is protected with a password PIN. Please enter the PIN to view and edit:",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    OutlinedTextField(
                        value = unlockPasswordInput,
                        onValueChange = {
                            unlockPasswordInput = it
                            unlockError = null
                        },
                        label = { Text("Enter PIN / Password") },
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true,
                        isError = unlockError != null,
                        modifier = Modifier.fillMaxWidth().testTag("unlock_pin_input")
                    )
                    unlockError?.let {
                        Text(it, color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val ok = viewModel.unlockDocument(unlockPasswordInput)
                        if (!ok) {
                            unlockError = "Incorrect PIN or Password"
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = WordPrimary),
                    modifier = Modifier.testTag("unlock_confirm_btn")
                ) {
                    Text("Unlock")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = onNavigateBack,
                    modifier = Modifier.testTag("unlock_cancel_btn")
                ) {
                    Text("Back to Home")
                }
            }
        )
    }
}
