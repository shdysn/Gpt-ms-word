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
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.FormatListNumbered
import androidx.compose.material.icons.filled.HorizontalRule
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material.icons.filled.Title
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.VerticalSplit
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
    onOpenLayoutDialog: () -> Unit = {}
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
                                onOpenPageNumberDialog = onOpenPageNumberDialog
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
    onOpenPageNumberDialog: () -> Unit
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
fun ViewRibbonControls(
    viewModel: EditorViewModel,
    onOpenLayoutDialog: () -> Unit = {}
) {
    val currentViewMode by viewModel.viewMode.collectAsStateWithLifecycle()
    val zoomPercent by viewModel.zoomPercent.collectAsStateWithLifecycle()

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
