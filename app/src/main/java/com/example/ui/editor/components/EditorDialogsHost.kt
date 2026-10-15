package com.example.ui.editor.components

import android.content.Context
import androidx.activity.result.ActivityResultLauncher
import androidx.compose.runtime.Composable
import com.example.data.model.ImageBlock
import com.example.data.model.ParagraphBlock
import com.example.data.model.RichDocument
import com.example.data.model.ShapeBlock
import com.example.data.model.TableBlock
import com.example.ui.editor.EditorViewModel
import com.example.ui.editor.components.dialogs.EditorAdvancedModals
import com.example.ui.editor.components.dialogs.EditorFileModals
import com.example.ui.editor.components.dialogs.EditorInsertModals

/**
 * Host component for all Editor Dialogs and Modals.
 * Decouples modal orchestration and lifecycle from the main canvas layout.
 * Delegates to focused modal managers (File, Insert, and Advanced).
 */
@Composable
fun EditorDialogsHost(
    viewModel: EditorViewModel,
    document: RichDocument?,
    context: Context,
    actualPageCount: Int,
    activeParagraph: ParagraphBlock?,
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
    showTableDialog: Boolean,
    onDismissTableDialog: () -> Unit,
    selectedTableForProperties: TableBlock?,
    onDismissTableProperties: () -> Unit,
    selectedCellForEdit: Triple<TableBlock, Int, Int>?,
    onDismissCellEdit: () -> Unit,
    selectedImageForProperties: ImageBlock?,
    onDismissImageProperties: () -> Unit,
    showHeaderFooterDialog: Boolean,
    onDismissHeaderFooter: () -> Unit,
    showPageNumberDialog: Boolean,
    onDismissPageNumber: () -> Unit,
    showLinkDialog: Boolean,
    onDismissLinkDialog: () -> Unit,
    showLayoutDialog: Boolean,
    onDismissLayoutDialog: () -> Unit,
    showExportPdfDialog: Boolean,
    onDismissExportPdf: () -> Unit,
    onOpenPageSetupFromPdf: () -> Unit,
    showStatsDialog: Boolean,
    onDismissStatsDialog: () -> Unit,
    isDocumentLocked: Boolean,
    showSignatureDialog: Boolean,
    onDismissSignatureDialog: () -> Unit,
    showShapeDialog: Boolean,
    selectedShapeForEdit: ShapeBlock? = null,
    onDismissShapeDialog: () -> Unit,
    showWatermarkDialog: Boolean,
    onDismissWatermarkDialog: () -> Unit,
    showPasswordDialog: Boolean,
    onDismissPasswordDialog: () -> Unit,
    showVersionHistoryDialog: Boolean,
    onDismissVersionHistoryDialog: () -> Unit,
    showCommentsDialog: Boolean = false,
    onDismissCommentsDialog: () -> Unit = {},
    showTrackChangesDialog: Boolean = false,
    onDismissTrackChangesDialog: () -> Unit = {},
    showSmartWritingDialog: Boolean = false,
    onDismissSmartWritingDialog: () -> Unit = {},
    showFootnoteDialog: Boolean = false,
    onDismissFootnoteDialog: () -> Unit = {},
    showPageBordersDialog: Boolean = false,
    onDismissPageBordersDialog: () -> Unit = {},
    showPageColorDialog: Boolean = false,
    onDismissPageColorDialog: () -> Unit = {},
    showTypographyEffectsDialog: Boolean = false,
    onDismissTypographyEffectsDialog: () -> Unit = {},
    showParagraphSpacingDialog: Boolean = false,
    onDismissParagraphSpacingDialog: () -> Unit = {},
    showCreateCustomStyleDialog: Boolean = false,
    onDismissCreateCustomStyleDialog: () -> Unit = {},
    selectedCellForProperties: Triple<TableBlock, Int, Int>? = null,
    onDismissCellProperties: () -> Unit = {},
    safDocxSaveLauncher: ActivityResultLauncher<String>,
    safPdfSaveLauncher: ActivityResultLauncher<String>
) {
    EditorFileModals(
        viewModel = viewModel,
        document = document,
        context = context,
        showTitleEditDialog = showTitleEditDialog,
        onDismissTitleEdit = onDismissTitleEdit,
        showSaveOptionsDialog = showSaveOptionsDialog,
        onDismissSaveOptions = onDismissSaveOptions,
        onOpenSaveAs = onOpenSaveAs,
        showExportOptionsDialog = showExportOptionsDialog,
        onDismissExportOptions = onDismissExportOptions,
        onOpenExportPdf = onOpenExportPdf,
        showSaveAsDialog = showSaveAsDialog,
        onDismissSaveAs = onDismissSaveAs,
        showDiscardDialog = showDiscardDialog,
        onDismissDiscard = onDismissDiscard,
        onNavigateBack = onNavigateBack,
        isDocumentLocked = isDocumentLocked,
        safDocxSaveLauncher = safDocxSaveLauncher
    )

    EditorInsertModals(
        viewModel = viewModel,
        document = document,
        context = context,
        actualPageCount = actualPageCount,
        showTableDialog = showTableDialog,
        onDismissTableDialog = onDismissTableDialog,
        selectedTableForProperties = selectedTableForProperties,
        onDismissTableProperties = onDismissTableProperties,
        selectedCellForEdit = selectedCellForEdit,
        onDismissCellEdit = onDismissCellEdit,
        selectedImageForProperties = selectedImageForProperties,
        onDismissImageProperties = onDismissImageProperties,
        showHeaderFooterDialog = showHeaderFooterDialog,
        onDismissHeaderFooter = onDismissHeaderFooter,
        showPageNumberDialog = showPageNumberDialog,
        onDismissPageNumber = onDismissPageNumber,
        showLinkDialog = showLinkDialog,
        onDismissLinkDialog = onDismissLinkDialog,
        showLayoutDialog = showLayoutDialog,
        onDismissLayoutDialog = onDismissLayoutDialog,
        showExportPdfDialog = showExportPdfDialog,
        onDismissExportPdf = onDismissExportPdf,
        onOpenPageSetupFromPdf = onOpenPageSetupFromPdf,
        safPdfSaveLauncher = safPdfSaveLauncher
    )

    EditorAdvancedModals(
        viewModel = viewModel,
        document = document,
        actualPageCount = actualPageCount,
        activeParagraph = activeParagraph,
        showStatsDialog = showStatsDialog,
        onDismissStatsDialog = onDismissStatsDialog,
        showSignatureDialog = showSignatureDialog,
        onDismissSignatureDialog = onDismissSignatureDialog,
        showShapeDialog = showShapeDialog,
        selectedShapeForEdit = selectedShapeForEdit,
        onDismissShapeDialog = onDismissShapeDialog,
        showWatermarkDialog = showWatermarkDialog,
        onDismissWatermarkDialog = onDismissWatermarkDialog,
        showPasswordDialog = showPasswordDialog,
        onDismissPasswordDialog = onDismissPasswordDialog,
        showVersionHistoryDialog = showVersionHistoryDialog,
        onDismissVersionHistoryDialog = onDismissVersionHistoryDialog,
        showCommentsDialog = showCommentsDialog,
        onDismissCommentsDialog = onDismissCommentsDialog,
        showTrackChangesDialog = showTrackChangesDialog,
        onDismissTrackChangesDialog = onDismissTrackChangesDialog,
        showSmartWritingDialog = showSmartWritingDialog,
        onDismissSmartWritingDialog = onDismissSmartWritingDialog,
        showFootnoteDialog = showFootnoteDialog,
        onDismissFootnoteDialog = onDismissFootnoteDialog,
        showPageBordersDialog = showPageBordersDialog,
        onDismissPageBordersDialog = onDismissPageBordersDialog,
        showPageColorDialog = showPageColorDialog,
        onDismissPageColorDialog = onDismissPageColorDialog,
        showTypographyEffectsDialog = showTypographyEffectsDialog,
        onDismissTypographyEffectsDialog = onDismissTypographyEffectsDialog,
        showParagraphSpacingDialog = showParagraphSpacingDialog,
        onDismissParagraphSpacingDialog = onDismissParagraphSpacingDialog,
        showCreateCustomStyleDialog = showCreateCustomStyleDialog,
        onDismissCreateCustomStyleDialog = onDismissCreateCustomStyleDialog,
        selectedCellForProperties = selectedCellForProperties,
        onDismissCellProperties = onDismissCellProperties
    )
}
