package com.example.ui.editor.components

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.CustomStyle
import com.example.data.model.ParagraphBlock
import com.example.editor.engine.CaseType
import com.example.ui.editor.EditorViewModel
import com.example.ui.editor.RibbonTab
import com.example.ui.theme.WordPrimary

// Re-export ribbon buttons for backward compatibility
@Composable
fun RibbonActionButton(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit
) = com.example.ui.editor.components.ribbon.RibbonActionButton(icon, label, onClick)

@Composable
fun RibbonToggleButton(
    icon: ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    tag: String = ""
) = com.example.ui.editor.components.ribbon.RibbonToggleButton(icon, label, isSelected, onClick, tag)

/**
 * WordRibbon container component.
 * Displays the active tab's tool buttons cleanly below the Top Bar when expanded.
 * Tab switching is controlled via the MS Word style dropdown situated in the top bar.
 */
@Composable
fun WordRibbon(
    activeTab: RibbonTab,
    onTabSelected: (RibbonTab) -> Unit,
    isRibbonVisible: Boolean,
    onToggleRibbon: () -> Unit,
    activeParagraph: ParagraphBlock?,
    viewModel: EditorViewModel,
    context: Context,
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
    onQuickSave: () -> Unit = {},
    onSaveAs: () -> Unit = {},
    onSaveSafDocx: () -> Unit = {},
    onOpenExportOptions: () -> Unit = {},
    onPrint: () -> Unit = {},
    onToggleFindReplace: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val activeFormatting by viewModel.activeFormatting.collectAsStateWithLifecycle()
    val isFormatPainterActive by viewModel.isFormatPainterActive.collectAsStateWithLifecycle()
    val currentDocument by viewModel.document.collectAsStateWithLifecycle()

    AnimatedVisibility(
        visible = isRibbonVisible,
        enter = expandVertically() + fadeIn(),
        exit = shrinkVertically() + fadeOut()
    ) {
        Surface(
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 2.dp,
            modifier = modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Top Ribbon Tabs selector bar: [File ▸] [Home] [Insert] [Draw] [Layout] [Review] [View]
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    val visibleTabs = if (activeTab == RibbonTab.FILE) {
                        RibbonTab.entries
                    } else {
                        RibbonTab.entries.filter { it != RibbonTab.FILE }
                    }
                    visibleTabs.forEach { tab ->
                        val isSelected = activeTab == tab
                        val isFile = tab == RibbonTab.FILE
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (isSelected) WordPrimary.copy(alpha = 0.15f) else Color.Transparent,
                            border = if (isSelected) BorderStroke(1.dp, WordPrimary) else null,
                            modifier = Modifier
                                .clickable { onTabSelected(tab) }
                                .testTag("ribbon_tab_item_${tab.name.lowercase()}")
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = when (tab) {
                                        RibbonTab.FILE -> "File"
                                        RibbonTab.HOME -> "Home"
                                        RibbonTab.INSERT -> "Insert"
                                        RibbonTab.DRAW -> "Draw"
                                        RibbonTab.LAYOUT -> "Layout"
                                        RibbonTab.REVIEW -> "Review"
                                        RibbonTab.VIEW -> "View"
                                    },
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) WordPrimary else MaterialTheme.colorScheme.onSurface
                                )
                                if (isFile) {
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                        contentDescription = null,
                                        tint = if (isSelected) WordPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(13.dp)
                                    )
                                }
                            }
                        }
                    }
                }
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f), thickness = 0.5.dp)

                WordRibbonContent(
                    activeTab = activeTab,
                    activeParagraph = activeParagraph,
                    activeFormatting = activeFormatting,
                    isFormatPainterActive = isFormatPainterActive,
                    viewModel = viewModel,
                    context = context,
                    customStyles = currentDocument?.customStyles ?: emptyList(),
                    onOpenTableDialog = onOpenTableDialog,
                    onOpenPhotoPicker = onOpenPhotoPicker,
                    onOpenLinkDialog = onOpenLinkDialog,
                    onOpenWatermarkDialog = onOpenWatermarkDialog,
                    onOpenLayoutDialog = onOpenLayoutDialog,
                    onOpenPasswordDialog = onOpenPasswordDialog,
                    onOpenVersionHistoryDialog = onOpenVersionHistoryDialog,
                    onOpenStatsDialog = onOpenStatsDialog,
                    onOpenHeaderFooterDialog = onOpenHeaderFooterDialog,
                    onOpenPageNumberDialog = onOpenPageNumberDialog,
                    onStartVoiceTyping = onStartVoiceTyping,
                    onOpenSignatureDialog = onOpenSignatureDialog,
                    onOpenShapeDialog = onOpenShapeDialog,
                    onOpenCommentsDialog = onOpenCommentsDialog,
                    onOpenTrackChangesDialog = onOpenTrackChangesDialog,
                    onOpenPageBordersDialog = onOpenPageBordersDialog,
                    onOpenPageColorDialog = onOpenPageColorDialog,
                    onOpenFootnoteDialog = onOpenFootnoteDialog,
                    onOpenSmartWritingDialog = onOpenSmartWritingDialog,
                    onOpenOcrScanDialog = onOpenOcrScanDialog,
                    onOpenOutline = onOpenOutline,
                    onClearFormatting = onClearFormatting,
                    onChangeCase = onChangeCase,
                    onOpenTypographyEffectsDialog = onOpenTypographyEffectsDialog,
                    onOpenParagraphSpacingDialog = onOpenParagraphSpacingDialog,
                    onToggleChecklist = onToggleChecklist,
                    onApplyCustomStyle = onApplyCustomStyle,
                    onCreateCustomStyle = onCreateCustomStyle,
                    onTabSelected = onTabSelected,
                    onQuickSave = onQuickSave,
                    onSaveAs = onSaveAs,
                    onSaveSafDocx = onSaveSafDocx,
                    onOpenExportOptions = onOpenExportOptions,
                    onPrint = onPrint,
                    onToggleFindReplace = onToggleFindReplace
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 0.5.dp)
            }
        }
    }
}
