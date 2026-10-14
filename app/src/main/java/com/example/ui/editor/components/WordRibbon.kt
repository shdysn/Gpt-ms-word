package com.example.ui.editor.components

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.BorderAll
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Draw
import androidx.compose.material.icons.filled.FormatListNumbered
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.HorizontalRule
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.ListAlt
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.SquareFoot
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material.icons.filled.Title
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.VerticalSplit
import androidx.compose.material.icons.filled.ViewColumn
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material.icons.filled.ZoomOut
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.core.layout.MarginPreset
import com.example.core.layout.PageBorderStyle
import com.example.core.layout.PageOrientation
import com.example.core.layout.PageSize
import com.example.data.model.ParagraphBlock
import com.example.ui.editor.EditorViewModel
import com.example.ui.editor.EditorViewMode
import com.example.ui.editor.RibbonTab
import com.example.ui.theme.WordPrimary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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
    onOpenHeaderFooterDialog: () -> Unit = {},
    onOpenPageNumberDialog: () -> Unit = {},
    onOpenLayoutDialog: () -> Unit = {},
    onOpenSignatureDialog: () -> Unit = {},
    onOpenShapeDialog: () -> Unit = {},
    onOpenWatermarkDialog: () -> Unit = {},
    onOpenPasswordDialog: () -> Unit = {},
    onOpenVersionHistoryDialog: () -> Unit = {},
    onOpenStatsDialog: () -> Unit = {},
    onStartVoiceTyping: () -> Unit = {},
    onOpenFootnoteDialog: () -> Unit = {},
    onOpenPageBordersDialog: () -> Unit = {},
    onOpenPageColorDialog: () -> Unit = {},
    showCommentsDialog: Boolean = false,
    onOpenCommentsDialog: () -> Unit = {},
    showTrackChangesDialog: Boolean = false,
    onOpenTrackChangesDialog: () -> Unit = {},
    onOpenSmartWritingDialog: () -> Unit = {},
    onClearFormatting: () -> Unit = { viewModel.clearFormatting() },
    onChangeCase: (com.example.editor.engine.CaseType) -> Unit = { viewModel.changeCase(it) },
    onOpenTypographyEffectsDialog: () -> Unit = {},
    onOpenParagraphSpacingDialog: () -> Unit = {},
    onToggleChecklist: () -> Unit = { viewModel.toggleChecklist() },
    onCreateCustomStyle: () -> Unit = {},
    onApplyCustomStyle: (com.example.data.model.CustomStyle) -> Unit = { viewModel.applyCustomStyle(it) }
) {
    val activeFormatting by viewModel.activeFormatting.collectAsStateWithLifecycle()
    val isFormatPainterActive by viewModel.isFormatPainterActive.collectAsStateWithLifecycle()
    val currentDocument by viewModel.document.collectAsStateWithLifecycle()

    Surface(
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 2.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {
            val tabs = RibbonTab.entries
            val selectedIndex = tabs.indexOf(activeTab)

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(end = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TabRow(
                    selectedTabIndex = selectedIndex,
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = WordPrimary,
                    modifier = Modifier.weight(1f),
                    divider = {},
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[selectedIndex]),
                            color = WordPrimary,
                            height = 2.5.dp
                        )
                    }
                ) {
                    tabs.forEach { tab ->
                        val isSelected = activeTab == tab
                        Tab(
                            selected = isSelected,
                            onClick = {
                                if (isSelected) {
                                    onToggleRibbon()
                                } else {
                                    onTabSelected(tab)
                                    if (!isRibbonVisible) {
                                        onToggleRibbon()
                                    }
                                }
                            }
                        ) {
                            Box(
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = tab.name.lowercase().replaceFirstChar { it.uppercase() },
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    fontSize = 13.sp,
                                    maxLines = 1,
                                    softWrap = false,
                                    color = if (isSelected) WordPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(6.dp))
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
                                onOpenFootnoteDialog = onOpenFootnoteDialog
                            )
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
                                onOpenLayoutDialog = onOpenLayoutDialog
                            )
                        }
                    }
                }
            }
        }
    }
}



@Composable
fun RibbonActionButton(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        border = androidx.compose.foundation.BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier
            .height(34.dp)
            .clickable(onClick = onClick)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 10.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                modifier = Modifier.size(16.dp),
                tint = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun RibbonToggleButton(
    icon: ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    tag: String = ""
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = if (isSelected) WordPrimary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        border = androidx.compose.foundation.BorderStroke(
            if (isSelected) 1.dp else 0.5.dp,
            if (isSelected) WordPrimary else MaterialTheme.colorScheme.outlineVariant
        ),
        modifier = Modifier
            .height(34.dp)
            .clickable(onClick = onClick)
            .testTag(tag)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 10.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                modifier = Modifier.size(16.dp),
                tint = if (isSelected) WordPrimary else MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) WordPrimary else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
