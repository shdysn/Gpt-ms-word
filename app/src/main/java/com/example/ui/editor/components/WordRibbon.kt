package com.example.ui.editor.components

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TabRow(
                    selectedTabIndex = activeTab.ordinal,
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = WordPrimary,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            modifier = Modifier.tabIndicatorOffset(tabPositions[activeTab.ordinal]),
                            color = WordPrimary,
                            height = 2.5.dp
                        )
                    },
                    divider = {},
                    modifier = Modifier.weight(1f)
                ) {
                    RibbonTab.entries.forEach { tab ->
                        val isSelected = activeTab == tab
                        Tab(
                            selected = isSelected,
                            onClick = {
                                if (isSelected && isRibbonVisible) {
                                    onToggleRibbon()
                                } else {
                                    if (!isRibbonVisible) onToggleRibbon()
                                    onTabSelected(tab)
                                }
                            },
                            text = {
                                val tabTitle = when (tab) {
                                    RibbonTab.HOME -> "Home"
                                    RibbonTab.INSERT -> "Insert"
                                    RibbonTab.DRAW -> "Draw"
                                    RibbonTab.LAYOUT -> "Layout"
                                    RibbonTab.REVIEW -> "Review"
                                    RibbonTab.VIEW -> "View"
                                }
                                Text(
                                    text = tabTitle,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) WordPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            },
                            modifier = Modifier
                                .height(40.dp)
                                .testTag("ribbon_tab_${tab.name.lowercase()}")
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clickable(onClick = onToggleRibbon)
                        .testTag("toggle_ribbon_visibility"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isRibbonVisible) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = if (isRibbonVisible) "Collapse Ribbon" else "Expand Ribbon",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 0.5.dp)

            AnimatedVisibility(visible = isRibbonVisible) {
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
                    onCreateCustomStyle = onCreateCustomStyle
                )
            }
        }
    }
}
}
