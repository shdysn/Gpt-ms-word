package com.example.ui.editor.components

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
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
import com.example.ui.editor.components.ribbon.DrawRibbonControls
import com.example.ui.editor.components.ribbon.InsertRibbonControls
import com.example.ui.editor.components.ribbon.LayoutRibbonControls
import com.example.ui.editor.components.ribbon.ReviewRibbonControls
import com.example.ui.editor.components.ribbon.ViewRibbonControls
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
                when (activeTab) {
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
                            customStyles = currentDocument?.customStyles ?: emptyList(),
                            onApplyCustomStyle = onApplyCustomStyle,
                            onCreateCustomStyle = onCreateCustomStyle
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
        }
    }
}
