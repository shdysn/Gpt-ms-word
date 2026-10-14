package com.example.ui.editor.components

import androidx.compose.runtime.Composable
import com.example.data.model.ParagraphBlock
import com.example.data.model.RichDocument

// Re-exports for backward compatibility
@Composable
fun EditTitleDialog(
    initialTitle: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) = com.example.ui.editor.components.dialogs.EditTitleDialog(initialTitle, onDismiss, onConfirm)

@Composable
fun SaveOptionsDialog(
    onDismiss: () -> Unit,
    onQuickSave: () -> Unit,
    onSaveAs: () -> Unit,
    onSaveSafDocx: () -> Unit
) = com.example.ui.editor.components.dialogs.SaveOptionsDialog(onDismiss, onQuickSave, onSaveAs, onSaveSafDocx)

@Composable
fun ExportOptionsDialog(
    onDismiss: () -> Unit,
    onExportPdf: () -> Unit,
    onExportDocx: () -> Unit,
    onExportPlainText: () -> Unit
) = com.example.ui.editor.components.dialogs.ExportOptionsDialog(onDismiss, onExportPdf, onExportDocx, onExportPlainText)

@Composable
fun SaveAsDialog(
    initialTitle: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) = com.example.ui.editor.components.dialogs.SaveAsDialog(initialTitle, onDismiss, onConfirm)

@Composable
fun DiscardChangesDialog(
    docTitle: String,
    onDismiss: () -> Unit,
    onSaveAndExit: () -> Unit,
    onDiscard: () -> Unit
) = com.example.ui.editor.components.dialogs.DiscardChangesDialog(docTitle, onDismiss, onSaveAndExit, onDiscard)

@Composable
fun DocumentStatisticsDialog(
    document: RichDocument?,
    actualPageCount: Int,
    activeParagraph: ParagraphBlock?,
    onDismiss: () -> Unit
) = com.example.ui.editor.components.dialogs.DocumentStatisticsDialog(document, actualPageCount, activeParagraph, onDismiss)
