package com.example.ui.editor.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ListAlt
import androidx.compose.material.icons.filled.AddCircleOutline
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FormatListNumbered
import androidx.compose.material.icons.filled.Gesture
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.RateReview
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.editor.RibbonTab
import com.example.ui.theme.WordPrimary

/**
 * Dropdown selector for the active ribbon tab in MS Word style situated in TopAppBar.
 */
@Composable
fun EditorRibbonTabDropdown(
    activeRibbonTab: RibbonTab,
    isRibbonVisible: Boolean,
    onTabSelected: (RibbonTab) -> Unit,
    onToggleRibbon: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isTabDropdownOpen by remember { mutableStateOf(false) }
    val arrowRotation by animateFloatAsState(
        targetValue = if (isTabDropdownOpen) 180f else 0f,
        label = "ribbonTabArrowRotation"
    )

    Box(modifier = modifier) {
        Surface(
            color = WordPrimary.copy(alpha = 0.12f),
            shape = RoundedCornerShape(20.dp),
            border = BorderStroke(1.dp, WordPrimary.copy(alpha = 0.4f)),
            modifier = Modifier
                .clickable { isTabDropdownOpen = true }
                .testTag("ribbon_tab_dropdown_button")
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
            ) {
                val (tabIcon, tabTitle) = getRibbonTabMetadata(activeRibbonTab)
                Icon(tabIcon, contentDescription = null, tint = WordPrimary, modifier = Modifier.size(15.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(tabTitle, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = WordPrimary)
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = "Switch Tab",
                    tint = WordPrimary,
                    modifier = Modifier
                        .size(18.dp)
                        .rotate(arrowRotation)
                )
            }
        }

        DropdownMenu(
            expanded = isTabDropdownOpen,
            onDismissRequest = { isTabDropdownOpen = false },
            shape = RoundedCornerShape(12.dp)
        ) {
            RibbonTab.entries.forEach { tab ->
                val isSelected = activeRibbonTab == tab
                val (tabIcon, tabTitle) = getRibbonTabMetadata(tab)
                DropdownMenuItem(
                    text = {
                        Text(
                            text = tabTitle,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) WordPrimary else MaterialTheme.colorScheme.onSurface
                        )
                    },
                    leadingIcon = {
                        Icon(tabIcon, contentDescription = null, tint = if (isSelected) WordPrimary else MaterialTheme.colorScheme.onSurfaceVariant)
                    },
                    trailingIcon = if (isSelected) {
                        {
                            Icon(Icons.Default.Check, contentDescription = "Selected", tint = WordPrimary, modifier = Modifier.size(16.dp))
                        }
                    } else null,
                    onClick = {
                        isTabDropdownOpen = false
                        onTabSelected(tab)
                        if (!isRibbonVisible) onToggleRibbon()
                    },
                    modifier = Modifier.testTag("ribbon_tab_${tab.name.lowercase()}")
                )
            }
        }
    }
}

internal fun getRibbonTabMetadata(tab: RibbonTab): Pair<ImageVector, String> {
    return when (tab) {
        RibbonTab.FILE -> Icons.Default.Description to "File"
        RibbonTab.HOME -> Icons.Default.Home to "Home"
        RibbonTab.INSERT -> Icons.Default.AddCircleOutline to "Insert"
        RibbonTab.DRAW -> Icons.Default.Gesture to "Draw"
        RibbonTab.LAYOUT -> Icons.Default.Layers to "Layout"
        RibbonTab.REVIEW -> Icons.Default.RateReview to "Review"
        RibbonTab.VIEW -> Icons.Default.Visibility to "View"
    }
}

/**
 * Overflow action menu for EditorTopAppBar.
 */
@Composable
fun EditorTopAppBarOverflowMenu(
    isFindReplaceVisible: Boolean,
    isOutlineVisible: Boolean,
    onTitleClick: () -> Unit = {},
    onToggleFindReplace: () -> Unit,
    onToggleOutline: () -> Unit,
    onSaveClick: () -> Unit,
    onExportClick: () -> Unit,
    onPageSetupClick: () -> Unit,
    onPrintClick: () -> Unit,
    onStatsClick: () -> Unit,
    onSelectAllClick: () -> Unit = {}
) {
    var showOptionsMenu by remember { mutableStateOf(false) }

    Box {
        IconButton(onClick = { showOptionsMenu = true }) {
            Icon(Icons.Default.MoreVert, contentDescription = "More Actions")
        }

        DropdownMenu(
            expanded = showOptionsMenu,
            onDismissRequest = { showOptionsMenu = false }
        ) {
            DropdownMenuItem(
                text = { Text("Select All Document") },
                leadingIcon = {
                    Icon(
                        Icons.Default.SelectAll,
                        contentDescription = null,
                        tint = WordPrimary
                    )
                },
                onClick = { showOptionsMenu = false; onSelectAllClick() },
                modifier = Modifier.testTag("action_menu_select_all")
            )
            DropdownMenuItem(
                text = { Text("Find & Replace") },
                leadingIcon = {
                    Icon(
                        Icons.Default.Search,
                        contentDescription = null,
                        tint = if (isFindReplaceVisible) WordPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                onClick = { showOptionsMenu = false; onToggleFindReplace() },
                modifier = Modifier.testTag("action_find_replace")
            )
            DropdownMenuItem(
                text = { Text("Document Outline") },
                leadingIcon = {
                    Icon(
                        Icons.AutoMirrored.Filled.ListAlt,
                        contentDescription = null,
                        tint = if (isOutlineVisible) WordPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                onClick = { showOptionsMenu = false; onToggleOutline() },
                modifier = Modifier.testTag("action_document_outline")
            )
            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
            DropdownMenuItem(
                text = { Text("Save Document...") },
                leadingIcon = { Icon(Icons.Default.Save, contentDescription = null, tint = WordPrimary) },
                onClick = { showOptionsMenu = false; onSaveClick() },
                modifier = Modifier.testTag("menu_save_options")
            )
            DropdownMenuItem(
                text = { Text("Export Document...") },
                leadingIcon = { Icon(Icons.Default.Share, contentDescription = null, tint = WordPrimary) },
                onClick = { showOptionsMenu = false; onExportClick() },
                modifier = Modifier.testTag("menu_export_options")
            )
            DropdownMenuItem(
                text = { Text("Page Setup...") },
                leadingIcon = { Icon(Icons.Default.Tune, contentDescription = null, tint = WordPrimary) },
                onClick = { showOptionsMenu = false; onPageSetupClick() },
                modifier = Modifier.testTag("menu_page_setup")
            )
            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
            DropdownMenuItem(
                text = { Text("Print") },
                leadingIcon = { Icon(Icons.Default.Print, contentDescription = null) },
                onClick = { showOptionsMenu = false; onPrintClick() },
                modifier = Modifier.testTag("menu_print")
            )
            DropdownMenuItem(
                text = { Text("Document Statistics") },
                leadingIcon = { Icon(Icons.Default.FormatListNumbered, contentDescription = null) },
                onClick = { showOptionsMenu = false; onStatsClick() },
                modifier = Modifier.testTag("menu_stats")
            )
        }
    }
}
