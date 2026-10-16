package com.example.ui.editor.components

import android.content.Context
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.data.model.CustomStyle
import com.example.data.model.ParagraphBlock
import com.example.editor.engine.ActiveFormatting
import com.example.editor.engine.CaseType
import com.example.ui.editor.EditorViewModel
import com.example.ui.editor.RibbonTab
import com.example.ui.editor.components.ribbon.DrawRibbonControls
import com.example.ui.editor.components.ribbon.FileRibbonControls
import com.example.ui.editor.components.ribbon.InsertRibbonControls
import com.example.ui.editor.components.ribbon.LayoutRibbonControls
import com.example.ui.editor.components.ribbon.ReviewRibbonControls
import com.example.ui.editor.components.ribbon.ViewRibbonControls

@Composable
fun WordRibbonContent(
    activeTab: RibbonTab,
    activeParagraph: ParagraphBlock?,
    activeFormatting: ActiveFormatting,
    isFormatPainterActive: Boolean,
    viewModel: EditorViewModel,
    context: Context,
    customStyles: List<CustomStyle>,
    onOpenTableDialog: () -> Unit,
    onOpenPhotoPicker: () -> Unit,
    onOpenLinkDialog: () -> Unit,
    onOpenWatermarkDialog: () -> Unit,
    onOpenLayoutDialog: () -> Unit,
    onOpenPasswordDialog: () -> Unit,
    onOpenVersionHistoryDialog: () -> Unit,
    onOpenStatsDialog: () -> Unit,
    onOpenHeaderFooterDialog: () -> Unit,
    onOpenPageNumberDialog: () -> Unit,
    onStartVoiceTyping: () -> Unit,
    onOpenSignatureDialog: () -> Unit,
    onOpenShapeDialog: () -> Unit,
    onOpenCommentsDialog: () -> Unit = {},
    onOpenTrackChangesDialog: () -> Unit = {},
    onOpenPageBordersDialog: () -> Unit = {},
    onOpenPageColorDialog: () -> Unit = {},
    onOpenFootnoteDialog: () -> Unit = {},
    onOpenSmartWritingDialog: () -> Unit = {},
    onOpenOcrScanDialog: () -> Unit = {},
    onOpenOutline: () -> Unit = {},
    onClearFormatting: () -> Unit = {},
    onChangeCase: (CaseType) -> Unit = {},
    onOpenTypographyEffectsDialog: () -> Unit = {},
    onOpenParagraphSpacingDialog: () -> Unit = {},
    onToggleChecklist: () -> Unit = {},
    onApplyCustomStyle: (CustomStyle) -> Unit = {},
    onCreateCustomStyle: () -> Unit = {},
    onTabSelected: (RibbonTab) -> Unit = {},
    onQuickSave: () -> Unit = {},
    onSaveAs: () -> Unit = {},
    onSaveSafDocx: () -> Unit = {},
    onOpenExportOptions: () -> Unit = {},
    onPrint: () -> Unit = {},
    onToggleFindReplace: () -> Unit = {}
) {
    when (activeTab) {
        RibbonTab.FILE -> {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FileRibbonControls(
                    viewModel = viewModel,
                    onTabSelected = onTabSelected,
                    onSaveNow = onQuickSave,
                    onOpenSaveAs = onSaveAs,
                    onSaveSafDocx = onSaveSafDocx,
                    onOpenExportOptions = onOpenExportOptions,
                    onOpenLayoutDialog = onOpenLayoutDialog,
                    onPrint = onPrint,
                    onToggleFindReplace = onToggleFindReplace,
                    onOpenOutline = onOpenOutline,
                    onOpenStatsDialog = onOpenStatsDialog
                )
            }
        }

        RibbonTab.HOME -> {
            FormattingToolbar(
                activeParagraph = activeParagraph,
                activeFormatting = activeFormatting,
                onSetHeadingStyle = viewModel::setHeadingStyle,
                onToggleBold = viewModel::toggleBold,
                onToggleItalic = viewModel::toggleItalic,
                onToggleUnderline = viewModel::toggleUnderline,
                onToggleStrikethrough = viewModel::toggleStrikethrough,
                onToggleSuperscript = viewModel::toggleSuperscript,
                onToggleSubscript = viewModel::toggleSubscript,
                onSetFontFamily = viewModel::setFontFamily,
                onSetFontSize = viewModel::setFontSize,
                onSetTextColor = viewModel::setTextColor,
                onSetHighlightColor = viewModel::setHighlightColor,
                onSetAlignment = viewModel::setAlignment,
                onToggleListType = viewModel::toggleListType,
                onIncreaseIndent = viewModel::increaseIndent,
                onDecreaseIndent = viewModel::decreaseIndent,
                onOpenLinkDialog = onOpenLinkDialog,
                onToggleRtl = viewModel::toggleRtl,
                onSetLineSpacing = viewModel::setLineSpacing,
                isFormatPainterActive = isFormatPainterActive,
                onToggleFormatPainter = viewModel::toggleFormatPainter,
                onClearFormatting = onClearFormatting,
                onChangeCase = onChangeCase,
                onOpenTypographyEffectsDialog = onOpenTypographyEffectsDialog,
                onOpenParagraphSpacingDialog = onOpenParagraphSpacingDialog,
                onToggleChecklist = onToggleChecklist,
                customStyles = customStyles,
                onApplyCustomStyle = onApplyCustomStyle,
                onCreateCustomStyle = onCreateCustomStyle,
                onOpenFileTab = { onTabSelected(RibbonTab.FILE) }
            )
        }

        RibbonTab.INSERT -> {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                InsertRibbonControls(
                    viewModel = viewModel,
                    onOpenTableDialog = onOpenTableDialog,
                    onOpenPhotoPicker = onOpenPhotoPicker,
                    onOpenLinkDialog = onOpenLinkDialog,
                    onOpenHeaderFooterDialog = onOpenHeaderFooterDialog,
                    onOpenPageNumberDialog = onOpenPageNumberDialog,
                    onOpenSignatureDialog = onOpenSignatureDialog,
                    onOpenShapeDialog = onOpenShapeDialog,
                    onOpenFootnoteDialog = onOpenFootnoteDialog,
                    onOpenOcrScanDialog = onOpenOcrScanDialog
                )
            }
        }

        RibbonTab.DRAW -> {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                DrawRibbonControls(viewModel = viewModel)
            }
        }

        RibbonTab.LAYOUT -> {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                LayoutRibbonControls(
                    viewModel = viewModel,
                    onOpenWatermarkDialog = onOpenWatermarkDialog,
                    onOpenLayoutDialog = onOpenLayoutDialog,
                    onOpenPageBordersDialog = onOpenPageBordersDialog,
                    onOpenPageColorDialog = onOpenPageColorDialog
                )
            }
        }

        RibbonTab.REVIEW -> {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ReviewRibbonControls(
                    viewModel = viewModel,
                    context = context,
                    onOpenPasswordDialog = onOpenPasswordDialog,
                    onOpenVersionHistoryDialog = onOpenVersionHistoryDialog,
                    onOpenStatsDialog = onOpenStatsDialog,
                    onStartVoiceTyping = onStartVoiceTyping,
                    onOpenCommentsDialog = onOpenCommentsDialog,
                    onOpenTrackChangesDialog = onOpenTrackChangesDialog,
                    onOpenSmartWritingDialog = onOpenSmartWritingDialog
                )
            }
        }

        RibbonTab.VIEW -> {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ViewRibbonControls(
                    viewModel = viewModel,
                    onOpenLayoutDialog = onOpenLayoutDialog,
                    onOpenOutline = onOpenOutline
                )
            }
        }
    }
}
