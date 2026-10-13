package com.example.ui.editor

import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Redo
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FormatBold
import androidx.compose.material.icons.filled.FormatColorText
import androidx.compose.material.icons.filled.FormatItalic
import androidx.compose.material.icons.filled.FormatListNumbered
import androidx.compose.material.icons.filled.FormatStrikethrough
import androidx.compose.material.icons.filled.FormatUnderlined
import androidx.compose.material.icons.filled.Highlight
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.ImageBlock
import com.example.data.model.ParagraphBlock
import com.example.data.model.TableBlock
import com.example.ui.editor.components.AutosaveChip
import com.example.ui.editor.components.DiscardChangesDialog
import com.example.ui.editor.components.DocumentCanvas
import com.example.ui.editor.components.DocumentStatisticsDialog
import com.example.ui.editor.components.EditTitleDialog
import com.example.ui.editor.components.EditorStatusBar
import com.example.ui.editor.components.ExportOptionsDialog
import com.example.ui.editor.components.ExportPdfDialog
import com.example.ui.editor.components.FindReplaceBar
import com.example.ui.editor.components.HeaderFooterEditDialog
import com.example.ui.editor.components.ImagePropertiesDialog
import com.example.ui.editor.components.InsertHyperlinkDialog
import com.example.ui.editor.components.InsertTableDialog
import com.example.ui.editor.components.PageLayoutDialog
import com.example.ui.editor.components.PageNumberInsertDialog
import com.example.ui.editor.components.SaveAsDialog
import com.example.ui.editor.components.SaveOptionsDialog
import com.example.ui.editor.components.TableCellEditDialog
import com.example.ui.editor.components.TablePropertiesDialog
import com.example.ui.editor.components.WordRibbon
import com.example.ui.editor.components.shareDocxFile
import com.example.ui.editor.components.sharePdfFile
import com.example.ui.editor.components.shareTextFile
import com.example.ui.theme.WordDeskCanvasDark
import com.example.ui.theme.WordDeskCanvasLight
import com.example.ui.theme.WordPrimary
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditorScreen(
    viewModel: EditorViewModel,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    val document by viewModel.document.collectAsStateWithLifecycle()
    val activeRibbonTab by viewModel.activeRibbonTab.collectAsStateWithLifecycle()
    val isRibbonVisible by viewModel.isRibbonVisible.collectAsStateWithLifecycle()
    val viewMode by viewModel.viewMode.collectAsStateWithLifecycle()
    val zoomPercent by viewModel.zoomPercent.collectAsStateWithLifecycle()
    val canUndo by viewModel.canUndo.collectAsStateWithLifecycle()
    val canRedo by viewModel.canRedo.collectAsStateWithLifecycle()
    val paginatedPages by viewModel.paginatedPages.collectAsStateWithLifecycle()
    val activeBlockIndex by viewModel.activeBlockIndex.collectAsStateWithLifecycle()
    val autosaveStatus by viewModel.autosaveStatus.collectAsStateWithLifecycle()
    val isDirty by viewModel.isDirty.collectAsStateWithLifecycle()
    val recoveryAvailable by viewModel.recoveryAvailable.collectAsStateWithLifecycle()
    val statusMessage by viewModel.statusMessage.collectAsStateWithLifecycle()

    val isFindReplaceVisible by viewModel.isFindReplaceVisible.collectAsStateWithLifecycle()
    val findQuery by viewModel.findQuery.collectAsStateWithLifecycle()
    val replaceQuery by viewModel.replaceQuery.collectAsStateWithLifecycle()
    val matchCount by viewModel.matchCount.collectAsStateWithLifecycle()
    val showWordCount by viewModel.showWordCount.collectAsStateWithLifecycle()
    val showRuler by viewModel.showRuler.collectAsStateWithLifecycle()

    val actualPageCount = remember(paginatedPages, document) {
        if (paginatedPages.isNotEmpty()) paginatedPages.size else (document?.estimatedPageCount ?: 1)
    }
    val currentPageNumber = remember(paginatedPages, activeBlockIndex) {
        val foundPage = paginatedPages.firstOrNull { page ->
            page.items.any { it.originalBlockIndex == activeBlockIndex }
        }
        foundPage?.pageNumber ?: 1
    }

    val coroutineScope = rememberCoroutineScope()
    var showTitleEditDialog by remember { mutableStateOf(false) }
    var showSaveOptionsDialog by remember { mutableStateOf(false) }
    var showExportOptionsDialog by remember { mutableStateOf(false) }
    var showSaveAsDialog by remember { mutableStateOf(false) }
    var showDiscardDialog by remember { mutableStateOf(false) }
    var showTableDialog by remember { mutableStateOf(false) }
    var showLinkDialog by remember { mutableStateOf(false) }
    var showLayoutDialog by remember { mutableStateOf(false) }
    var showExportPdfDialog by remember { mutableStateOf(false) }
    var showStatsDialog by remember { mutableStateOf(false) }
    var showHeaderFooterDialog by remember { mutableStateOf(false) }
    var showPageNumberDialog by remember { mutableStateOf(false) }
    var selectedTableForProperties by remember { mutableStateOf<TableBlock?>(null) }
    var selectedCellForEdit by remember { mutableStateOf<Triple<TableBlock, Int, Int>?>(null) }
    var selectedImageForProperties by remember { mutableStateOf<ImageBlock?>(null) }
    var showOptionsMenu by remember { mutableStateOf(false) }

    // Flush edits to storage when application is backgrounded or minimized
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_PAUSE || event == Lifecycle.Event.ON_STOP) {
                viewModel.onAppBackgrounded()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // Android Zero-Permission Photo Picker for Image Insertion
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        uri?.let { viewModel.insertImageFromUri(context, it, "Inserted Picture") }
    }

    // SAF PDF Save Launcher
    val safPdfSaveLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/pdf")
    ) { uri ->
        uri?.let { viewModel.exportPdfToSafUri(context, it) }
    }

    // SAF DOCX Save Launcher
    val safDocxSaveLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/vnd.openxmlformats-officedocument.wordprocessingml.document")
    ) { uri ->
        uri?.let { viewModel.exportDocxToSafUri(context, it) }
    }

    // Safe navigation back handling: prompt user if unsaved changes exist
    val handleBackNavigation = {
        if (isDirty) {
            showDiscardDialog = true
        } else {
            onNavigateBack()
        }
    }

    // Intercept hardware/system back button safely
    BackHandler {
        handleBackNavigation()
    }

    LaunchedEffect(statusMessage) {
        statusMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearStatusMessage()
        }
    }

    val activeParagraph = document?.blocks?.getOrNull(activeBlockIndex) as? ParagraphBlock

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            Column {
                TopAppBar(
                    navigationIcon = {
                        IconButton(
                            onClick = { handleBackNavigation() },
                            modifier = Modifier.testTag("editor_back_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back to Home"
                            )
                        }
                    },
                    title = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clickable { showTitleEditDialog = true }
                                .padding(vertical = 4.dp)
                                .testTag("title_edit_container")
                        ) {
                            Text(
                                text = document?.title ?: "Document",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f, fill = false)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit Title",
                                tint = MaterialTheme.colorScheme.outline,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    },
                    actions = {
                        AutosaveChip(status = autosaveStatus)

                        IconButton(
                            onClick = { viewModel.undo() },
                            enabled = canUndo,
                            modifier = Modifier.testTag("action_undo")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Undo,
                                contentDescription = "Undo",
                                tint = if (canUndo) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                            )
                        }

                        IconButton(
                            onClick = { viewModel.redo() },
                            enabled = canRedo,
                            modifier = Modifier.testTag("action_redo")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Redo,
                                contentDescription = "Redo",
                                tint = if (canRedo) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                            )
                        }

                        IconButton(
                            onClick = { viewModel.toggleViewMode() },
                            modifier = Modifier.testTag("action_toggle_view_mode")
                        ) {
                            Icon(
                                imageVector = if (viewMode == EditorViewMode.PAGINATED_A4) Icons.Default.PhoneAndroid else Icons.Default.Description,
                                contentDescription = if (viewMode == EditorViewMode.PAGINATED_A4) "Switch to Mobile View" else "Switch to Full Page A4 View",
                                tint = if (viewMode == EditorViewMode.PAGINATED_A4) WordPrimary else MaterialTheme.colorScheme.onSurface
                            )
                        }

                        IconButton(
                            onClick = { viewModel.toggleFindReplace() },
                            modifier = Modifier.testTag("action_find_replace")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search and Replace",
                                tint = if (isFindReplaceVisible) WordPrimary else MaterialTheme.colorScheme.onSurface
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
                                    text = { Text("Save...") },
                                    leadingIcon = { Icon(Icons.Default.Save, contentDescription = null, tint = WordPrimary) },
                                    onClick = {
                                        showOptionsMenu = false
                                        showSaveOptionsDialog = true
                                    },
                                    modifier = Modifier.testTag("menu_save_options")
                                )
                                DropdownMenuItem(
                                    text = { Text("Export...") },
                                    leadingIcon = { Icon(Icons.Default.Share, contentDescription = null, tint = WordPrimary) },
                                    onClick = {
                                        showOptionsMenu = false
                                        showExportOptionsDialog = true
                                    },
                                    modifier = Modifier.testTag("menu_export_options")
                                )
                                DropdownMenuItem(
                                    text = { Text("Page Setup...") },
                                    leadingIcon = { Icon(Icons.Default.Tune, contentDescription = null, tint = WordPrimary) },
                                    onClick = {
                                        showOptionsMenu = false
                                        showLayoutDialog = true
                                    },
                                    modifier = Modifier.testTag("menu_page_setup")
                                )
                                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                                DropdownMenuItem(
                                    text = { Text("Print") },
                                    leadingIcon = { Icon(Icons.Default.Print, contentDescription = null) },
                                    onClick = {
                                        showOptionsMenu = false
                                        viewModel.printDocument(context)
                                    },
                                    modifier = Modifier.testTag("menu_print")
                                )
                                DropdownMenuItem(
                                    text = { Text("Document Statistics") },
                                    leadingIcon = { Icon(Icons.Default.FormatListNumbered, contentDescription = null) },
                                    onClick = {
                                        showOptionsMenu = false
                                        showStatsDialog = true
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

                FindReplaceBar(
                    isVisible = isFindReplaceVisible,
                    findQuery = findQuery,
                    replaceQuery = replaceQuery,
                    matchCount = matchCount,
                    onFindQueryChanged = viewModel::updateFindQuery,
                    onReplaceQueryChanged = viewModel::updateReplaceQuery,
                    onReplaceAll = viewModel::replaceAll,
                    onClose = { viewModel.toggleFindReplace() }
                )
            }
        },
        bottomBar = {
            EditorStatusBar(
                document = document,
                currentPageNumber = currentPageNumber,
                totalPages = actualPageCount,
                showWordCount = showWordCount,
                autosaveStatus = autosaveStatus,
                zoomPercent = zoomPercent,
                activeBlock = activeParagraph,
                onOpenLayoutDialog = { showLayoutDialog = true }
            )
        }
    ) { innerPadding ->
        val deskCanvasColor = if (isSystemInDarkTheme()) WordDeskCanvasDark else WordDeskCanvasLight
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(deskCanvasColor)
                .imePadding()
        ) {
            WordRibbon(
                activeTab = activeRibbonTab,
                onTabSelected = viewModel::selectRibbonTab,
                isRibbonVisible = isRibbonVisible,
                onToggleRibbon = viewModel::toggleRibbonVisibility,
                activeParagraph = activeParagraph,
                viewModel = viewModel,
                context = context,
                onOpenTableDialog = { showTableDialog = true },
                onOpenPhotoPicker = {
                    photoPickerLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                },
                onOpenLinkDialog = { showLinkDialog = true },
                onOpenHeaderFooterDialog = { showHeaderFooterDialog = true },
                onOpenPageNumberDialog = { showPageNumberDialog = true },
                onOpenLayoutDialog = { showLayoutDialog = true }
            )

            // Crash/Restart Recovery Notification Banner
            if (recoveryAvailable) {
                Surface(
                    color = Color(0xFFFFF3CD),
                    border = BorderStroke(1.dp, Color(0xFFFFEEBA)),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                        .testTag("recovery_banner")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = Color(0xFF856404),
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Unsaved Recovery Draft Found",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = Color(0xFF856404)
                            )
                            Text(
                                text = "A draft from an earlier session is available for restoration.",
                                fontSize = 11.sp,
                                color = Color(0xFF856404)
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Button(
                            onClick = { viewModel.restoreRecoveryDraft() },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF856404)),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            modifier = Modifier.testTag("restore_recovery_draft_button")
                        ) {
                            Text("Restore", fontSize = 11.sp, color = Color.White)
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                        TextButton(
                            onClick = { viewModel.dismissRecoveryDraft() },
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                            modifier = Modifier.testTag("dismiss_recovery_draft_button")
                        ) {
                            Text("Dismiss", fontSize = 11.sp, color = Color(0xFF856404))
                        }
                    }
                }
            }

            BoxWithConstraints(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.TopCenter
            ) {
                val isTabletOrExpanded = maxWidth > 600.dp

                if (document != null) {
                    DocumentCanvas(
                        document = document!!,
                        paginatedPages = paginatedPages,
                        viewMode = viewMode,
                        zoomPercent = zoomPercent,
                        showRuler = showRuler,
                        isTabletOrExpanded = isTabletOrExpanded,
                        activeBlockIndex = activeBlockIndex,
                        onBlockSelected = viewModel::setActiveBlock,
                        onUpdateBlockText = { index, text, sel -> viewModel.updateParagraphText(index, text, sel) },
                        onUpdateSubSliceText = { index, startChar, endChar, text, sel ->
                            viewModel.updateParagraphSubSliceText(index, startChar, endChar, text, sel)
                        },
                        onSelectionChanged = viewModel::updateSelection,
                        onSelectionChangedForBlock = viewModel::updateSelectionForBlock,
                        onSplitParagraph = viewModel::splitParagraph,
                        onInsertParagraph = viewModel::insertParagraph,
                        onDeleteCurrentBlock = viewModel::deleteCurrentBlock,
                        onDeleteBlockById = viewModel::deleteBlockById,
                        onUpdateUnifiedText = { text, sel -> viewModel.updateUnifiedDocumentText(text, sel) },
                        onDeleteOrMergeWithPrevious = { index -> viewModel.deleteOrMergeWithPrevious(index) },
                        onEditTableProperties = { table -> selectedTableForProperties = table },
                        onEditTableCell = { table, r, c -> selectedCellForEdit = Triple(table, r, c) },
                        onQuickAddTableRow = { id -> viewModel.addTableRow(id) },
                        onQuickDeleteTableRow = { id -> viewModel.deleteTableRow(id, -1) },
                        onQuickAddTableCol = { id -> viewModel.addTableColumn(id) },
                        onQuickDeleteTableCol = { id -> viewModel.deleteTableColumn(id, -1) },
                        onEditImageProperties = { img -> selectedImageForProperties = img },
                        onOpenPageLayoutDialog = { showLayoutDialog = true }
                    )
                }

                // Floating Contextual Quick-Formatting Bar
                val selection by viewModel.selection.collectAsStateWithLifecycle()
                val activeFormatting by viewModel.activeFormatting.collectAsStateWithLifecycle()

                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 16.dp)
                ) {
                    androidx.compose.animation.AnimatedVisibility(
                        visible = !selection.collapsed && selection.min < selection.max,
                        enter = fadeIn() + slideInVertically(initialOffsetY = { it }),
                        exit = fadeOut() + slideOutVertically(targetOffsetY = { it })
                    ) {
                    Surface(
                        shape = RoundedCornerShape(24.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shadowElevation = 6.dp,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            IconButton(
                                onClick = { viewModel.toggleBold() },
                                modifier = Modifier.size(36.dp).testTag("quick_format_bold")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.FormatBold,
                                    contentDescription = "Bold",
                                    tint = if (activeFormatting.isBold) WordPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            IconButton(
                                onClick = { viewModel.toggleItalic() },
                                modifier = Modifier.size(36.dp).testTag("quick_format_italic")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.FormatItalic,
                                    contentDescription = "Italic",
                                    tint = if (activeFormatting.isItalic) WordPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            IconButton(
                                onClick = { viewModel.toggleUnderline() },
                                modifier = Modifier.size(36.dp).testTag("quick_format_underline")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.FormatUnderlined,
                                    contentDescription = "Underline",
                                    tint = if (activeFormatting.isUnderline) WordPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            IconButton(
                                onClick = { viewModel.toggleStrikethrough() },
                                modifier = Modifier.size(36.dp).testTag("quick_format_strikethrough")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.FormatStrikethrough,
                                    contentDescription = "Strikethrough",
                                    tint = if (activeFormatting.isStrikethrough) WordPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            IconButton(
                                onClick = {
                                    if (activeFormatting.highlightColorHex != null) viewModel.setHighlightColor(null)
                                    else viewModel.setHighlightColor("#FEF08A")
                                },
                                modifier = Modifier.size(36.dp).testTag("quick_format_highlight")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Highlight,
                                    contentDescription = "Highlight",
                                    tint = if (activeFormatting.highlightColorHex != null) Color(0xFFEAB308) else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            IconButton(
                                onClick = {
                                    if (activeFormatting.textColorHex != null) viewModel.setTextColor(null)
                                    else viewModel.setTextColor("#185ABD")
                                },
                                modifier = Modifier.size(36.dp).testTag("quick_format_color")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.FormatColorText,
                                    contentDescription = "Color",
                                    tint = if (activeFormatting.textColorHex != null) Color(0xFF185ABD) else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
                }
            }
        }
    }

    // Title Edit Dialog
    if (showTitleEditDialog) {
        EditTitleDialog(
            initialTitle = document?.title ?: "",
            onDismiss = { showTitleEditDialog = false },
            onConfirm = { newTitle ->
                viewModel.updateTitle(newTitle)
                showTitleEditDialog = false
            }
        )
    }

    // Save Options Dialog
    if (showSaveOptionsDialog) {
        SaveOptionsDialog(
            onDismiss = { showSaveOptionsDialog = false },
            onQuickSave = {
                showSaveOptionsDialog = false
                viewModel.saveNow()
            },
            onSaveAs = {
                showSaveOptionsDialog = false
                showSaveAsDialog = true
            },
            onSaveSafDocx = {
                showSaveOptionsDialog = false
                val safeTitle = (document?.title ?: "document").replace("[^a-zA-Z0-9.-]".toRegex(), "_")
                safDocxSaveLauncher.launch("$safeTitle.docx")
            }
        )
    }

    // Export Options Dialog
    if (showExportOptionsDialog) {
        ExportOptionsDialog(
            onDismiss = { showExportOptionsDialog = false },
            onExportPdf = {
                showExportOptionsDialog = false
                showExportPdfDialog = true
            },
            onExportDocx = {
                showExportOptionsDialog = false
                viewModel.exportToDocx(context) { file ->
                    file?.let { shareDocxFile(context, it) }
                }
            },
            onExportPlainText = {
                showExportOptionsDialog = false
                viewModel.exportToPlainText(context) { file ->
                    file?.let { shareTextFile(context, it) }
                }
            }
        )
    }

    // Save As Dialog
    if (showSaveAsDialog) {
        SaveAsDialog(
            initialTitle = "${document?.title ?: "Document"} (Copy)",
            onDismiss = { showSaveAsDialog = false },
            onConfirm = { copyTitle ->
                viewModel.saveAs(copyTitle) {
                    showSaveAsDialog = false
                }
            }
        )
    }

    // Unsaved Changes Discard Warning Dialog
    if (showDiscardDialog) {
        DiscardChangesDialog(
            docTitle = document?.title ?: "this document",
            onDismiss = { showDiscardDialog = false },
            onSaveAndExit = {
                coroutineScope.launch {
                    viewModel.flushSave()
                    showDiscardDialog = false
                    onNavigateBack()
                }
            },
            onDiscard = {
                viewModel.dismissRecoveryDraft()
                showDiscardDialog = false
                onNavigateBack()
            }
        )
    }

    // Insert Table Dialog
    if (showTableDialog) {
        InsertTableDialog(
            onDismiss = { showTableDialog = false },
            onConfirm = { r, c, hasBorders, widthPercent ->
                viewModel.insertTable(
                    rows = r,
                    cols = c,
                    hasBorders = hasBorders,
                    tableWidthPercent = widthPercent
                )
                showTableDialog = false
            }
        )
    }

    // Table Properties Dialog
    selectedTableForProperties?.let { table ->
        TablePropertiesDialog(
            table = table,
            onDismiss = { selectedTableForProperties = null },
            onAddRow = { viewModel.addTableRow(table.id) },
            onDeleteRow = { viewModel.deleteTableRow(table.id, table.rowCount - 1) },
            onAddCol = { viewModel.addTableColumn(table.id) },
            onDeleteCol = { viewModel.deleteTableColumn(table.id, table.columnCount - 1) },
            onUpdateProperties = { hasBorders, borderColorHex, widthPercent, alignment ->
                viewModel.updateTableProperties(
                    blockId = table.id,
                    hasBorders = hasBorders,
                    borderColorHex = borderColorHex,
                    tableWidthPercent = widthPercent,
                    alignment = alignment
                )
            },
            onDeleteTable = {
                viewModel.deleteBlockById(table.id)
            }
        )
    }

    // Table Cell Edit Dialog
    selectedCellForEdit?.let { (table, row, col) ->
        val cell = table.cells.getOrNull(row)?.getOrNull(col) ?: com.example.data.model.TableCell()
        TableCellEditDialog(
            cell = cell,
            row = row,
            col = col,
            onDismiss = { selectedCellForEdit = null },
            onConfirm = { text, isBold, isItalic, bgHex, alignment ->
                viewModel.updateTableCell(table.id, row, col, text)
                viewModel.updateTableCellFormat(
                    blockId = table.id,
                    row = row,
                    col = col,
                    isBold = isBold,
                    isItalic = isItalic,
                    backgroundColorHex = bgHex,
                    alignment = alignment
                )
                selectedCellForEdit = null
            }
        )
    }

    // Image Properties Dialog
    selectedImageForProperties?.let { img ->
        ImagePropertiesDialog(
            imageBlock = img,
            onDismiss = { selectedImageForProperties = null },
            onConfirm = { widthDp, heightDp, alignment, caption, ratio ->
                viewModel.updateImageProperties(
                    blockId = img.id,
                    widthDp = widthDp,
                    heightDp = heightDp,
                    alignment = alignment,
                    caption = caption,
                    aspectRatio = ratio
                )
            },
            onDeleteImage = {
                viewModel.deleteBlockById(img.id)
            }
        )
    }

    // Header & Footer Dialog
    if (showHeaderFooterDialog && document != null) {
        val layout = document!!.layoutConfig
        HeaderFooterEditDialog(
            currentHeader = layout.headerText,
            currentFooter = layout.footerText,
            showHeader = layout.showHeader,
            showFooter = layout.showFooter,
            onDismiss = { showHeaderFooterDialog = false },
            onConfirm = { headerText, footerText, showHeader, showFooter ->
                viewModel.setHeaderFooter(headerText, footerText, showHeader, showFooter)
                showHeaderFooterDialog = false
            }
        )
    }

    // Page Number Insert Dialog
    if (showPageNumberDialog) {
        PageNumberInsertDialog(
            onDismiss = { showPageNumberDialog = false },
            onSelectPreset = { pattern, alignment ->
                viewModel.setPageNumberPreset(pattern, alignment)
                showPageNumberDialog = false
            }
        )
    }

    // Insert Hyperlink Dialog
    if (showLinkDialog) {
        val activeFormatting by viewModel.activeFormatting.collectAsStateWithLifecycle()
        InsertHyperlinkDialog(
            initialUrl = activeFormatting.linkUrl ?: "https://",
            onDismiss = { showLinkDialog = false },
            onConfirm = { url ->
                viewModel.setHyperlink(url)
                showLinkDialog = false
            },
            onRemoveLink = {
                viewModel.setHyperlink(null)
                showLinkDialog = false
            }
        )
    }

    // Page Layout Dialog
    if (showLayoutDialog && document != null) {
        PageLayoutDialog(
            currentLayout = document!!.layoutConfig,
            onDismiss = { showLayoutDialog = false },
            onSaveConfig = { newConfig ->
                viewModel.updatePageLayoutConfig(newConfig)
                showLayoutDialog = false
            }
        )
    }

    // Export PDF Dialog
    if (showExportPdfDialog && document != null) {
        ExportPdfDialog(
            document = document!!,
            actualPageCount = actualPageCount,
            onDismiss = { showExportPdfDialog = false },
            onSaveToStorage = {
                val safeTitle = (document?.title ?: "document").replace("[^a-zA-Z0-9.-]".toRegex(), "_")
                safPdfSaveLauncher.launch("$safeTitle.pdf")
            },
            onSharePdf = {
                viewModel.exportToPdf(context) { file ->
                    file?.let { sharePdfFile(context, it) }
                }
            },
            onPrint = {
                viewModel.printDocument(context)
            },
            onOpenPageSetup = {
                showLayoutDialog = true
            }
        )
    }

    // Document Statistics Dialog
    if (showStatsDialog) {
        DocumentStatisticsDialog(
            document = document,
            actualPageCount = actualPageCount,
            activeParagraph = activeParagraph,
            onDismiss = { showStatsDialog = false }
        )
    }
}
