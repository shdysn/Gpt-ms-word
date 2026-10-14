package com.example.ui.home.components

import androidx.compose.runtime.Composable
import com.example.data.model.DocumentEntity
import com.example.data.repository.TemplateType

@Composable
fun HomeDialogsHost(
    showSettingsDialog: Boolean,
    isAutosaveEnabled: Boolean,
    onAutosaveChanged: (Boolean) -> Unit,
    defaultRtl: Boolean,
    onDefaultRtlChanged: (Boolean) -> Unit,
    showWordCount: Boolean,
    onShowWordCountChanged: (Boolean) -> Unit,
    showRuler: Boolean,
    onShowRulerChanged: (Boolean) -> Unit,
    onDismissSettings: () -> Unit,
    showCreateDialog: Boolean,
    onDismissCreate: () -> Unit,
    onCreateDocument: (String, TemplateType) -> Unit,
    renameTargetDoc: DocumentEntity?,
    onDismissRename: () -> Unit,
    onRenameDocument: (String, String) -> Unit,
    deleteTargetDoc: DocumentEntity?,
    onDismissDelete: () -> Unit,
    onDeleteDocument: (String) -> Unit
) {
    if (showSettingsDialog) {
        HomeSettingsDialog(
            isAutosaveEnabled = isAutosaveEnabled,
            onAutosaveChanged = onAutosaveChanged,
            defaultRtl = defaultRtl,
            onDefaultRtlChanged = onDefaultRtlChanged,
            showWordCount = showWordCount,
            onShowWordCountChanged = onShowWordCountChanged,
            showRuler = showRuler,
            onShowRulerChanged = onShowRulerChanged,
            onDismiss = onDismissSettings
        )
    }

    if (showCreateDialog) {
        CreateDocumentDialog(
            onDismiss = onDismissCreate,
            onConfirm = { title, template ->
                onDismissCreate()
                onCreateDocument(title, template)
            }
        )
    }

    renameTargetDoc?.let { target ->
        RenameDocumentDialog(
            doc = target,
            onDismiss = onDismissRename,
            onConfirm = { newTitle ->
                onRenameDocument(target.id, newTitle)
                onDismissRename()
            }
        )
    }

    deleteTargetDoc?.let { target ->
        DeleteDocumentDialog(
            doc = target,
            onDismiss = onDismissDelete,
            onConfirm = {
                onDeleteDocument(target.id)
                onDismissDelete()
            }
        )
    }
}
