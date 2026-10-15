package com.example.ui.editor.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ListAlt
import androidx.compose.material.icons.automirrored.filled.Redo
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FormatListNumbered
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.editor.engine.AutosaveStatus
import com.example.ui.editor.EditorViewMode
import com.example.ui.theme.WordPrimary

/**
 * Ultra-sleek, modern Top App Bar for the document editor.
 * Reclaims screen space by prioritizing the writing canvas, providing clean title display,
 * integrated autosave indicator, Undo/Redo, View Mode switch, and an organized overflow menu.
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
    var showOptionsMenu by remember { mutableStateOf(false) }

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
            Column(
                modifier = Modifier
                    .clickable { onTitleClick() }
                    .padding(vertical = 2.dp)
                    .testTag("title_edit_container")
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit Title",
                        tint = MaterialTheme.colorScheme.outline,
                        modifier = Modifier.size(13.dp)
                    )
                }
                val statusText = when (autosaveStatus) {
                    AutosaveStatus.SAVING -> "Saving..."
                    AutosaveStatus.SAVED -> "Saved"
                    AutosaveStatus.DIRTY -> "Editing..."
                    AutosaveStatus.ERROR -> "Unsaved changes"
                    AutosaveStatus.IDLE -> ""
                }
                if (statusText.isNotEmpty()) {
                    Text(
                        text = statusText,
                        fontSize = 11.sp,
                        color = when (autosaveStatus) {
                            AutosaveStatus.ERROR -> MaterialTheme.colorScheme.error
                            AutosaveStatus.SAVED -> WordPrimary
                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                        },
                        maxLines = 1
                    )
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

            Box {
                IconButton(onClick = { showOptionsMenu = true }) {
                    Icon(Icons.Default.MoreVert, contentDescription = "More Actions")
                }

                DropdownMenu(
                    expanded = showOptionsMenu,
                    onDismissRequest = { showOptionsMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Find & Replace") },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Search,
                                contentDescription = null,
                                tint = if (isFindReplaceVisible) WordPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        onClick = {
                            showOptionsMenu = false
                            onToggleFindReplace()
                        },
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
                        onClick = {
                            showOptionsMenu = false
                            onToggleOutline()
                        },
                        modifier = Modifier.testTag("action_document_outline")
                    )
                    DropdownMenuItem(
                        text = { Text(if (isRibbonVisible) "Hide Ribbon Toolbar" else "Show Ribbon Toolbar") },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Tune,
                                contentDescription = null,
                                tint = if (isRibbonVisible) WordPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        onClick = {
                            showOptionsMenu = false
                            onToggleRibbon()
                        },
                        modifier = Modifier.testTag("action_toggle_ribbon")
                    )
                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                    DropdownMenuItem(
                        text = { Text("Save Document...") },
                        leadingIcon = { Icon(Icons.Default.Save, contentDescription = null, tint = WordPrimary) },
                        onClick = {
                            showOptionsMenu = false
                            onSaveClick()
                        },
                        modifier = Modifier.testTag("menu_save_options")
                    )
                    DropdownMenuItem(
                        text = { Text("Export Document...") },
                        leadingIcon = { Icon(Icons.Default.Share, contentDescription = null, tint = WordPrimary) },
                        onClick = {
                            showOptionsMenu = false
                            onExportClick()
                        },
                        modifier = Modifier.testTag("menu_export_options")
                    )
                    DropdownMenuItem(
                        text = { Text("Page Setup...") },
                        leadingIcon = { Icon(Icons.Default.Tune, contentDescription = null, tint = WordPrimary) },
                        onClick = {
                            showOptionsMenu = false
                            onPageSetupClick()
                        },
                        modifier = Modifier.testTag("menu_page_setup")
                    )
                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                    DropdownMenuItem(
                        text = { Text("Print") },
                        leadingIcon = { Icon(Icons.Default.Print, contentDescription = null) },
                        onClick = {
                            showOptionsMenu = false
                            onPrintClick()
                        },
                        modifier = Modifier.testTag("menu_print")
                    )
                    DropdownMenuItem(
                        text = { Text("Document Statistics") },
                        leadingIcon = { Icon(Icons.Default.FormatListNumbered, contentDescription = null) },
                        onClick = {
                            showOptionsMenu = false
                            onStatsClick()
                        },
                        modifier = Modifier.testTag("menu_stats")
                    )
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    )
}
