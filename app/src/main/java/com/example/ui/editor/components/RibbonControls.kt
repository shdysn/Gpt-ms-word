package com.example.ui.editor.components

import android.content.Context
import androidx.compose.runtime.Composable
import com.example.ui.editor.EditorViewModel

// Re-export ribbon controls for backward compatibility
@Composable
fun InsertRibbonControls(
    viewModel: EditorViewModel,
    onOpenTableDialog: () -> Unit,
    onOpenPhotoPicker: () -> Unit,
    onOpenLinkDialog: () -> Unit,
    onOpenHeaderFooterDialog: () -> Unit,
    onOpenPageNumberDialog: () -> Unit,
    onOpenSignatureDialog: () -> Unit,
    onOpenShapeDialog: () -> Unit,
    onOpenFootnoteDialog: () -> Unit = {},
    onOpenOcrScanDialog: () -> Unit = {}
) = com.example.ui.editor.components.ribbon.InsertRibbonControls(
    viewModel, onOpenTableDialog, onOpenPhotoPicker, onOpenLinkDialog,
    onOpenHeaderFooterDialog, onOpenPageNumberDialog, onOpenSignatureDialog,
    onOpenShapeDialog, onOpenFootnoteDialog, onOpenOcrScanDialog
)

@Composable
fun LayoutRibbonControls(
    viewModel: EditorViewModel,
    onOpenWatermarkDialog: () -> Unit,
    onOpenLayoutDialog: () -> Unit,
    onOpenPageBordersDialog: () -> Unit = {},
    onOpenPageColorDialog: () -> Unit = {}
) = com.example.ui.editor.components.ribbon.LayoutRibbonControls(
    viewModel, onOpenWatermarkDialog, onOpenLayoutDialog, onOpenPageBordersDialog, onOpenPageColorDialog
)

@Composable
fun ReviewRibbonControls(
    viewModel: EditorViewModel,
    context: Context,
    onOpenPasswordDialog: () -> Unit,
    onOpenVersionHistoryDialog: () -> Unit,
    onOpenStatsDialog: () -> Unit,
    onStartVoiceTyping: () -> Unit,
    onOpenCommentsDialog: () -> Unit = {},
    onOpenTrackChangesDialog: () -> Unit = {},
    onOpenSmartWritingDialog: () -> Unit = {}
) = com.example.ui.editor.components.ribbon.ReviewRibbonControls(
    viewModel, context, onOpenPasswordDialog, onOpenVersionHistoryDialog, onOpenStatsDialog,
    onStartVoiceTyping, onOpenCommentsDialog, onOpenTrackChangesDialog, onOpenSmartWritingDialog
)

@Composable
fun ViewRibbonControls(
    viewModel: EditorViewModel,
    onOpenLayoutDialog: () -> Unit = {},
    onOpenOutline: () -> Unit = {}
) = com.example.ui.editor.components.ribbon.ViewRibbonControls(
    viewModel, onOpenLayoutDialog, onOpenOutline
)

@Composable
fun DrawRibbonControls(
    viewModel: EditorViewModel,
    onInsertDrawing: () -> Unit = { viewModel.insertDrawing() }
) = com.example.ui.editor.components.ribbon.DrawRibbonControls(
    viewModel, onInsertDrawing
)
