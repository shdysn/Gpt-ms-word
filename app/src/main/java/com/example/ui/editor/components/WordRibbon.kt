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
    onStartVoiceTyping: () -> Unit = {}
) {
    val activeFormatting by viewModel.activeFormatting.collectAsStateWithLifecycle()

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
                            onToggleRtl = viewModel::toggleRtl
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
                                onOpenShapeDialog = onOpenShapeDialog
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
                                onOpenLayoutDialog = onOpenLayoutDialog
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
                                onStartVoiceTyping = onStartVoiceTyping
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
fun InsertRibbonControls(
    viewModel: EditorViewModel,
    onOpenTableDialog: () -> Unit,
    onOpenPhotoPicker: () -> Unit,
    onOpenLinkDialog: () -> Unit,
    onOpenHeaderFooterDialog: () -> Unit,
    onOpenPageNumberDialog: () -> Unit,
    onOpenSignatureDialog: () -> Unit,
    onOpenShapeDialog: () -> Unit
) {
    RibbonActionButton(
        icon = Icons.Default.Add,
        label = "Paragraph",
        onClick = { viewModel.insertParagraph() }
    )

    RibbonActionButton(
        icon = Icons.Default.TableChart,
        label = "Table",
        onClick = onOpenTableDialog
    )

    RibbonActionButton(
        icon = Icons.Default.Image,
        label = "Picture",
        onClick = onOpenPhotoPicker
    )

    // Table of Contents (Point 3)
    RibbonActionButton(
        icon = Icons.Default.ListAlt,
        label = "Table of Contents",
        onClick = { viewModel.insertTableOfContents() }
    )

    // Digital Signature (Point 4)
    RibbonActionButton(
        icon = Icons.Default.Draw,
        label = "Signature",
        onClick = onOpenSignatureDialog
    )

    // Shapes & Callout Box (Point 4)
    RibbonActionButton(
        icon = Icons.Default.Category,
        label = "Shape / Note",
        onClick = onOpenShapeDialog
    )

    RibbonActionButton(
        icon = Icons.Default.HorizontalRule,
        label = "Horizontal Line",
        onClick = { viewModel.insertDivider() }
    )

    RibbonActionButton(
        icon = Icons.Default.Title,
        label = "Page Break",
        onClick = { viewModel.insertPageBreak() }
    )

    Box(
        modifier = Modifier
            .height(20.dp)
            .width(1.dp)
            .background(MaterialTheme.colorScheme.outlineVariant)
    )

    RibbonActionButton(
        icon = Icons.Default.Link,
        label = "Hyperlink",
        onClick = onOpenLinkDialog
    )

    RibbonActionButton(
        icon = Icons.Default.VerticalSplit,
        label = "Header & Footer",
        onClick = onOpenHeaderFooterDialog
    )

    RibbonActionButton(
        icon = Icons.Default.FormatListNumbered,
        label = "Page Numbers",
        onClick = onOpenPageNumberDialog
    )

    Box(
        modifier = Modifier
            .height(20.dp)
            .width(1.dp)
            .background(MaterialTheme.colorScheme.outlineVariant)
    )

    val dateFormat = remember { SimpleDateFormat("MMMM d, yyyy", Locale.getDefault()) }
    RibbonActionButton(
        icon = Icons.Default.CalendarMonth,
        label = "Date & Time",
        onClick = {
            val dateStr = dateFormat.format(Date())
            viewModel.insertDateTimeStamp(dateStr)
        }
    )
}

@Composable
fun LayoutRibbonControls(
    viewModel: EditorViewModel,
    onOpenWatermarkDialog: () -> Unit,
    onOpenLayoutDialog: () -> Unit
) {
    val document by viewModel.document.collectAsStateWithLifecycle()
    val layout = document?.layoutConfig ?: com.example.core.layout.PageLayoutConfig()

    // Orientation Toggles
    RibbonToggleButton(
        icon = Icons.Default.Description,
        label = "Portrait",
        isSelected = layout.orientation == PageOrientation.PORTRAIT,
        onClick = { viewModel.updatePageLayout(orientation = PageOrientation.PORTRAIT) }
    )

    RibbonToggleButton(
        icon = Icons.Default.PhoneAndroid,
        label = "Landscape",
        isSelected = layout.orientation == PageOrientation.LANDSCAPE,
        onClick = { viewModel.updatePageLayout(orientation = PageOrientation.LANDSCAPE) }
    )

    Box(modifier = Modifier.height(20.dp).width(1.dp).background(MaterialTheme.colorScheme.outlineVariant))

    // Columns: 1 or 2 Columns
    RibbonToggleButton(
        icon = Icons.Default.Description,
        label = "1 Column",
        isSelected = layout.columnCount == 1,
        onClick = { viewModel.setColumnCount(1) }
    )

    RibbonToggleButton(
        icon = Icons.Default.ViewColumn,
        label = "2 Columns",
        isSelected = layout.columnCount == 2,
        onClick = { viewModel.setColumnCount(2) }
    )

    Box(modifier = Modifier.height(20.dp).width(1.dp).background(MaterialTheme.colorScheme.outlineVariant))

    // Watermark (Point 3)
    val hasWatermark = layout.watermarkText.isNotBlank()
    RibbonToggleButton(
        icon = Icons.Default.WaterDrop,
        label = if (hasWatermark) "Watermark (${layout.watermarkText.take(8)})" else "Watermark",
        isSelected = hasWatermark,
        onClick = onOpenWatermarkDialog
    )

    Box(modifier = Modifier.height(20.dp).width(1.dp).background(MaterialTheme.colorScheme.outlineVariant))

    // Quick Margins
    RibbonToggleButton(
        icon = Icons.Default.BorderAll,
        label = "Normal Margins",
        isSelected = layout.marginPreset == MarginPreset.NORMAL,
        onClick = { viewModel.updatePageLayout(marginPreset = MarginPreset.NORMAL) }
    )

    RibbonToggleButton(
        icon = Icons.Default.BorderAll,
        label = "Narrow Margins",
        isSelected = layout.marginPreset == MarginPreset.NARROW,
        onClick = { viewModel.updatePageLayout(marginPreset = MarginPreset.NARROW) }
    )

    Box(modifier = Modifier.height(20.dp).width(1.dp).background(MaterialTheme.colorScheme.outlineVariant))

    // Full Page Setup Dialog
    RibbonActionButton(
        icon = Icons.Default.Tune,
        label = "Page Setup",
        onClick = onOpenLayoutDialog
    )
}

@Composable
fun ReviewRibbonControls(
    viewModel: EditorViewModel,
    context: Context,
    onOpenPasswordDialog: () -> Unit,
    onOpenVersionHistoryDialog: () -> Unit,
    onOpenStatsDialog: () -> Unit,
    onStartVoiceTyping: () -> Unit
) {
    val document by viewModel.document.collectAsStateWithLifecycle()
    val isSpeaking by viewModel.isSpeaking.collectAsStateWithLifecycle()
    val speechRate by viewModel.speechRate.collectAsStateWithLifecycle()
    val snapshots by viewModel.revisionSnapshots.collectAsStateWithLifecycle()
    val isProtected = document?.isPasswordProtected == true

    // Text-to-Speech (Read Aloud) - Point 5
    if (isSpeaking) {
        RibbonActionButton(
            icon = Icons.Default.Stop,
            label = "Stop Reading",
            onClick = { viewModel.stopReadAloud() }
        )
    } else {
        RibbonActionButton(
            icon = Icons.Default.VolumeUp,
            label = "Read Aloud",
            onClick = { viewModel.startReadAloud(context) }
        )
    }

    // TTS Speed Rate toggle
    RibbonActionButton(
        icon = Icons.Default.Speed,
        label = "${speechRate}x Speed",
        onClick = { viewModel.cycleSpeechRate() }
    )

    Box(modifier = Modifier.height(20.dp).width(1.dp).background(MaterialTheme.colorScheme.outlineVariant))

    // Voice Typing (Dictation / Speech-to-Text) - Point 5
    RibbonActionButton(
        icon = Icons.Default.Mic,
        label = "Voice Typing",
        onClick = onStartVoiceTyping
    )

    Box(modifier = Modifier.height(20.dp).width(1.dp).background(MaterialTheme.colorScheme.outlineVariant))

    // Document Security Lock (Point 5)
    RibbonToggleButton(
        icon = if (isProtected) Icons.Default.Lock else Icons.Default.LockOpen,
        label = if (isProtected) "Protected PIN" else "Password Lock",
        isSelected = isProtected,
        onClick = onOpenPasswordDialog
    )

    // Version History Snapshots (Point 5)
    RibbonActionButton(
        icon = Icons.Default.History,
        label = if (snapshots.isNotEmpty()) "History (${snapshots.size})" else "Version History",
        onClick = onOpenVersionHistoryDialog
    )

    Box(modifier = Modifier.height(20.dp).width(1.dp).background(MaterialTheme.colorScheme.outlineVariant))

    // Word Count & Statistics
    RibbonActionButton(
        icon = Icons.Default.BarChart,
        label = "Document Stats",
        onClick = onOpenStatsDialog
    )
}

@Composable
fun ViewRibbonControls(
    viewModel: EditorViewModel,
    onOpenLayoutDialog: () -> Unit = {}
) {
    val currentViewMode by viewModel.viewMode.collectAsStateWithLifecycle()
    val zoomPercent by viewModel.zoomPercent.collectAsStateWithLifecycle()
    val canvasPaperMode by viewModel.canvasPaperMode.collectAsStateWithLifecycle()
    val showRuler by viewModel.showRuler.collectAsStateWithLifecycle()
    val showWordCount by viewModel.showWordCount.collectAsStateWithLifecycle()

    // Canvas Paper Theme (Point 5: Canvas Dark Mode & Eye Care)
    RibbonToggleButton(
        icon = Icons.Default.LightMode,
        label = "White Sheet",
        isSelected = canvasPaperMode == "WHITE",
        onClick = { viewModel.setCanvasPaperMode("WHITE") }
    )

    RibbonToggleButton(
        icon = Icons.Default.Palette,
        label = "Eye-Care Sepia",
        isSelected = canvasPaperMode == "SEPIA",
        onClick = { viewModel.setCanvasPaperMode("SEPIA") }
    )

    RibbonToggleButton(
        icon = Icons.Default.DarkMode,
        label = "Night / Dark",
        isSelected = canvasPaperMode == "DARK",
        onClick = { viewModel.setCanvasPaperMode("DARK") }
    )

    Box(modifier = Modifier.height(20.dp).width(1.dp).background(MaterialTheme.colorScheme.outlineVariant))

    RibbonToggleButton(
        icon = Icons.Default.Description,
        label = "Page View",
        isSelected = currentViewMode == EditorViewMode.PAGINATED_A4,
        onClick = { viewModel.setViewMode(EditorViewMode.PAGINATED_A4) },
        tag = "view_mode_a4"
    )

    RibbonToggleButton(
        icon = Icons.Default.PhoneAndroid,
        label = "Mobile Continuous",
        isSelected = currentViewMode == EditorViewMode.CONTINUOUS_MOBILE,
        onClick = { viewModel.setViewMode(EditorViewMode.CONTINUOUS_MOBILE) },
        tag = "view_mode_mobile"
    )

    Box(modifier = Modifier.height(20.dp).width(1.dp).background(MaterialTheme.colorScheme.outlineVariant))

    // Ruler toggle
    RibbonToggleButton(
        icon = Icons.Default.SquareFoot,
        label = "Ruler",
        isSelected = showRuler,
        onClick = { viewModel.toggleRuler() }
    )

    // Word count status toggle
    RibbonToggleButton(
        icon = Icons.Default.BarChart,
        label = "Word Count",
        isSelected = showWordCount,
        onClick = { viewModel.toggleWordCount() }
    )

    Box(modifier = Modifier.height(20.dp).width(1.dp).background(MaterialTheme.colorScheme.outlineVariant))

    RibbonActionButton(
        icon = Icons.Default.Tune,
        label = "Page Setup",
        onClick = onOpenLayoutDialog
    )

    Box(modifier = Modifier.height(20.dp).width(1.dp).background(MaterialTheme.colorScheme.outlineVariant))

    Surface(
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        border = androidx.compose.foundation.BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier.height(34.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 4.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clickable { viewModel.setZoomPercent(zoomPercent - 10) }
                    .testTag("zoom_out_button"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.ZoomOut,
                    contentDescription = "Zoom Out",
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
            Text(
                text = "$zoomPercent%",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier
                    .padding(horizontal = 6.dp)
                    .testTag("zoom_percent_text")
            )
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clickable { viewModel.setZoomPercent(zoomPercent + 10) }
                    .testTag("zoom_in_button"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.ZoomIn,
                    contentDescription = "Zoom In",
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onSurface
                )
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
