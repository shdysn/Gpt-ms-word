package com.example.ui.editor.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Redo
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.editor.engine.AutosaveStatus
import com.example.ui.editor.EditorViewMode
import com.example.ui.editor.RibbonTab
import com.example.ui.theme.WordPrimary

/**
 * Ultra-sleek, modern Top App Bar for the document editor with MS Word Mobile Ribbon Dropdown.
 * Situates the Ribbon Tab selector ([ Home ▾ ]) prominently in the title area, with the
 * document name neatly fitted as a clickable subtitle and in the overflow menu.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditorTopAppBar(
    title: String,
    autosaveStatus: AutosaveStatus,
    canUndo: Boolean,
    canRedo: Boolean,
    viewMode: EditorViewMode,
    isFindReplaceVisible: Boolean,
    isOutlineVisible: Boolean = false,
    isRibbonVisible: Boolean = false,
    activeRibbonTab: RibbonTab = RibbonTab.HOME,
    onTabSelected: (RibbonTab) -> Unit = {},
    onBackClick: () -> Unit,
    onTitleClick: () -> Unit,
    onUndoClick: () -> Unit,
    onRedoClick: () -> Unit,
    onToggleViewMode: () -> Unit,
    onToggleFindReplace: () -> Unit,
    onToggleOutline: () -> Unit = {},
    onToggleRibbon: () -> Unit = {},
    onSaveClick: () -> Unit,
    onExportClick: () -> Unit,
    onPageSetupClick: () -> Unit,
    onPrintClick: () -> Unit,
    onStatsClick: () -> Unit
) {
    TopAppBar(
        navigationIcon = {
            IconButton(
                onClick = onBackClick,
                modifier = Modifier.testTag("editor_back_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back to Home"
                )
            }
        },
        title = {
            Column(modifier = Modifier.padding(vertical = 2.dp)) {
                // MS Word style Ribbon Tab Dropdown in the title position
                EditorRibbonTabDropdown(
                    activeRibbonTab = activeRibbonTab,
                    isRibbonVisible = isRibbonVisible,
                    onTabSelected = onTabSelected,
                    onToggleRibbon = onToggleRibbon
                )

                // Fitted Document Name subtitle (clickable to rename)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clickable { onTitleClick() }
                        .padding(top = 1.dp)
                        .testTag("title_edit_container")
                ) {
                    Text(
                        text = title,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit Title",
                        tint = MaterialTheme.colorScheme.outline,
                        modifier = Modifier.size(10.dp)
                    )
                    val statusText = when (autosaveStatus) {
                        AutosaveStatus.SAVING -> " • Saving..."
                        AutosaveStatus.SAVED -> " • Saved"
                        AutosaveStatus.DIRTY -> " • Editing"
                        AutosaveStatus.ERROR -> " • Unsaved"
                        AutosaveStatus.IDLE -> ""
                    }
                    if (statusText.isNotEmpty()) {
                        Text(
                            text = statusText,
                            fontSize = 10.sp,
                            color = if (autosaveStatus == AutosaveStatus.ERROR) MaterialTheme.colorScheme.error else WordPrimary,
                            maxLines = 1
                        )
                    }
                }
            }
        },
        actions = {
            IconButton(
                onClick = onUndoClick,
                enabled = canUndo,
                modifier = Modifier.testTag("action_undo")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Undo,
                    contentDescription = "Undo",
                    tint = if (canUndo) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)
                )
            }

            IconButton(
                onClick = onRedoClick,
                enabled = canRedo,
                modifier = Modifier.testTag("action_redo")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Redo,
                    contentDescription = "Redo",
                    tint = if (canRedo) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)
                )
            }

            IconButton(
                onClick = onToggleViewMode,
                modifier = Modifier.testTag("action_toggle_view_mode")
            ) {
                Icon(
                    imageVector = if (viewMode == EditorViewMode.PAGINATED_A4) Icons.Default.PhoneAndroid else Icons.Default.Description,
                    contentDescription = if (viewMode == EditorViewMode.PAGINATED_A4) "Switch to Mobile View" else "Switch to Full Page A4 View",
                    tint = if (viewMode == EditorViewMode.PAGINATED_A4) WordPrimary else MaterialTheme.colorScheme.onSurface
                )
            }

            IconButton(
                onClick = onToggleRibbon,
                modifier = Modifier.testTag("toggle_ribbon_visibility")
            ) {
                Icon(
                    imageVector = if (isRibbonVisible) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = if (isRibbonVisible) "Collapse Tools" else "Expand Tools",
                    tint = if (isRibbonVisible) WordPrimary else MaterialTheme.colorScheme.onSurface
                )
            }

            EditorTopAppBarOverflowMenu(
                isFindReplaceVisible = isFindReplaceVisible,
                isOutlineVisible = isOutlineVisible,
                onTitleClick = onTitleClick,
                onToggleFindReplace = onToggleFindReplace,
                onToggleOutline = onToggleOutline,
                onSaveClick = onSaveClick,
                onExportClick = onExportClick,
                onPageSetupClick = onPageSetupClick,
                onPrintClick = onPrintClick,
                onStatsClick = onStatsClick
            )
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onSurface,
            navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
            actionIconContentColor = MaterialTheme.colorScheme.onSurface
        )
    )
}
