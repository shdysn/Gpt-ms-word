package com.example.ui.editor

import android.content.Context
import android.content.Intent
import androidx.activity.compose.BackHandler
import kotlinx.coroutines.launch
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.foundation.Canvas
import androidx.compose.material.icons.filled.FitScreen
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Redo
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.BrokenImage
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FindReplace
import androidx.compose.material.icons.filled.FormatBold
import androidx.compose.material.icons.filled.FormatColorText
import androidx.compose.material.icons.filled.FormatItalic
import androidx.compose.material.icons.filled.FormatListNumbered
import androidx.compose.material.icons.filled.FormatStrikethrough
import androidx.compose.material.icons.filled.FormatUnderlined
import androidx.compose.material.icons.filled.Highlight
import androidx.compose.material.icons.filled.HorizontalRule
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.filled.Title
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.VerticalSplit
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material.icons.filled.ZoomOut
import androidx.compose.material.icons.outlined.Sync
import androidx.compose.material.icons.outlined.SyncProblem
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.type
import androidx.compose.ui.input.key.isShiftPressed
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.example.core.layout.MarginPreset
import com.example.core.layout.PageLayoutConfig
import com.example.core.layout.PageOrientation
import com.example.core.layout.PageSize
import com.example.data.model.BlockElement
import com.example.data.model.DividerBlock
import com.example.data.model.HeadingStyle
import com.example.data.model.ImageBlock
import com.example.data.model.ListType
import com.example.data.model.PageBreakBlock
import com.example.data.model.ParagraphBlock
import com.example.data.model.RichDocument
import com.example.data.model.TableBlock
import com.example.data.model.TableCell
import com.example.data.model.TextAlignment
import com.example.editor.engine.AutosaveStatus
import com.example.ui.editor.components.ExportPdfDialog
import com.example.ui.editor.components.FindReplaceBar
import com.example.ui.editor.components.FormattingToolbar
import com.example.ui.editor.components.HeaderFooterEditDialog
import com.example.ui.editor.components.ImagePropertiesDialog
import com.example.ui.editor.components.InsertHyperlinkDialog
import com.example.ui.editor.components.InsertTableDialog
import com.example.ui.editor.components.PageLayoutDialog
import com.example.ui.editor.components.PageNumberInsertDialog
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.minimumInteractiveComponentSize
import com.example.ui.editor.components.TableCellEditDialog
import com.example.ui.editor.components.TablePropertiesDialog
import com.example.ui.theme.WordDeskCanvasDark
import com.example.ui.theme.WordDeskCanvasLight
import com.example.ui.theme.WordPrimary
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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

    val coroutineScope = androidx.compose.runtime.rememberCoroutineScope()
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
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    androidx.compose.runtime.DisposableEffect(lifecycleOwner) {
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            if (event == androidx.lifecycle.Lifecycle.Event.ON_PAUSE ||
                event == androidx.lifecycle.Lifecycle.Event.ON_STOP
            ) {
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
                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
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
                        // Autosave Status Chip
                        AutosaveChip(status = autosaveStatus)

                        // Undo Button
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

                        // Redo Button
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

                        // Switch between MS Word Full Page Print Layout and Mobile View
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

                        // Search & Replace Toggle
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

                        // Overflow Options Menu
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

                // Docked In-Document Find and Replace Bar
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
            // Word Status Bar
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
                .imePadding() // Keyboard-aware layout: ensures soft keyboard does not obscure active content
        ) {
            // Microsoft Word Ribbon Component
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

            // Document Crash/Restart Recovery Notification Banner
            if (recoveryAvailable) {
                Surface(
                    color = Color(0xFFFFF3CD),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFFEEBA)),
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



            // Document Canvas Surface with Responsive Phone / Tablet Layout
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

                // Floating Contextual Quick-Formatting Bar for Selection
                val selection by viewModel.selection.collectAsStateWithLifecycle()
                val activeFormatting by viewModel.activeFormatting.collectAsStateWithLifecycle()

                androidx.compose.animation.AnimatedVisibility(
                    visible = !selection.collapsed && selection.min < selection.max,
                    enter = androidx.compose.animation.fadeIn() + androidx.compose.animation.slideInVertically(initialOffsetY = { it }),
                    exit = androidx.compose.animation.fadeOut() + androidx.compose.animation.slideOutVertically(targetOffsetY = { it }),
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 16.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(24.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shadowElevation = 6.dp,
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
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

    // Title Edit Dialog
    if (showTitleEditDialog) {
        var editedTitle by remember { mutableStateOf(document?.title ?: "") }
        AlertDialog(
            onDismissRequest = { showTitleEditDialog = false },
            title = { Text("Edit Document Title") },
            text = {
                OutlinedTextField(
                    value = editedTitle,
                    onValueChange = { editedTitle = it },
                    label = { Text("Title") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("edit_title_input")
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (editedTitle.isNotBlank()) {
                            viewModel.updateTitle(editedTitle.trim())
                        }
                        showTitleEditDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = WordPrimary)
                ) {
                    Text("Done")
                }
            },
            dismissButton = {
                TextButton(onClick = { showTitleEditDialog = false }) { Text("Cancel") }
            }
        )
    }

    // Save Options Popup Dialog
    if (showSaveOptionsDialog) {
        AlertDialog(
            onDismissRequest = { showSaveOptionsDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Save,
                        contentDescription = null,
                        tint = WordPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("Save Document", style = MaterialTheme.typography.titleLarge)
                }
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    // Option 1: Quick Save Current Edits
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                showSaveOptionsDialog = false
                                viewModel.saveNow()
                            }
                            .testTag("save_dialog_quick_save")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(14.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(WordPrimary.copy(alpha = 0.12f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Save, contentDescription = null, tint = WordPrimary, modifier = Modifier.size(22.dp))
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Save Document", fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                                Text("Save all current edits instantly", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }

                    // Option 2: Save As (Duplicate)
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                showSaveOptionsDialog = false
                                showSaveAsDialog = true
                            }
                            .testTag("save_dialog_save_as")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(14.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(WordPrimary.copy(alpha = 0.12f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Edit, contentDescription = null, tint = WordPrimary, modifier = Modifier.size(22.dp))
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Save As (Duplicate)...", fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                                Text("Create an independent copy with a new title", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }

                    // Option 3: Save as DOCX (SAF)
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                showSaveOptionsDialog = false
                                val safeTitle = (document?.title ?: "document").replace("[^a-zA-Z0-9.-]".toRegex(), "_")
                                safDocxSaveLauncher.launch("$safeTitle.docx")
                            }
                            .testTag("save_dialog_save_saf")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(14.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(WordPrimary.copy(alpha = 0.12f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Description, contentDescription = null, tint = WordPrimary, modifier = Modifier.size(22.dp))
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Save as DOCX (SAF)...", fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                                Text("Save .docx file directly to device storage", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(
                    onClick = { showSaveOptionsDialog = false },
                    modifier = Modifier.testTag("dismiss_save_dialog")
                ) {
                    Text("Close")
                }
            }
        )
    }

    // Export Options Popup Dialog
    if (showExportOptionsDialog) {
        AlertDialog(
            onDismissRequest = { showExportOptionsDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = null,
                        tint = WordPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("Export Document", style = MaterialTheme.typography.titleLarge)
                }
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    // Option 1: Export as PDF
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                showExportOptionsDialog = false
                                showExportPdfDialog = true
                            }
                            .testTag("export_dialog_pdf")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(14.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFE53935).copy(alpha = 0.12f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.PictureAsPdf, contentDescription = null, tint = Color(0xFFE53935), modifier = Modifier.size(22.dp))
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Export / Save PDF...", fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                                Text("Generate standard high-resolution PDF document", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }

                    // Option 2: Export as Word DOCX
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                showExportOptionsDialog = false
                                viewModel.exportToDocx(context) { file ->
                                    file?.let { shareDocxFile(context, it) }
                                }
                            }
                            .testTag("export_dialog_docx")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(14.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(WordPrimary.copy(alpha = 0.12f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Share, contentDescription = null, tint = WordPrimary, modifier = Modifier.size(22.dp))
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Export as Word DOCX (.docx)", fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                                Text("Share Microsoft Word compatible format to apps", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }

                    // Option 3: Export as Plain Text
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                showExportOptionsDialog = false
                                viewModel.exportToPlainText(context) { file ->
                                    file?.let { shareTextFile(context, it) }
                                }
                            }
                            .testTag("export_dialog_txt")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(14.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.12f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.TextFields, contentDescription = null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(22.dp))
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Export as Plain Text (.txt)", fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                                Text("Share raw text content without formatting", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(
                    onClick = { showExportOptionsDialog = false },
                    modifier = Modifier.testTag("dismiss_export_dialog")
                ) {
                    Text("Close")
                }
            }
        )
    }

    // Save As Dialog
    if (showSaveAsDialog) {
        var saveAsTitle by remember { mutableStateOf("${document?.title ?: "Document"} (Copy)") }
        AlertDialog(
            onDismissRequest = { showSaveAsDialog = false },
            title = { Text("Save As") },
            text = {
                Column {
                    Text("Enter a new title to save an independent copy of this document:")
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = saveAsTitle,
                        onValueChange = { saveAsTitle = it },
                        label = { Text("New Title") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("save_as_title_input")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val title = saveAsTitle.trim().ifBlank { "${document?.title} (Copy)" }
                        viewModel.saveAs(title) {
                            showSaveAsDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = WordPrimary),
                    modifier = Modifier.testTag("confirm_save_as_button")
                ) {
                    Text("Save Copy")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSaveAsDialog = false }) { Text("Cancel") }
            }
        )
    }

    // Unsaved Changes Discard Warning Dialog
    if (showDiscardDialog) {
        AlertDialog(
            onDismissRequest = { showDiscardDialog = false },
            title = { Text("Unsaved Changes") },
            text = {
                Text("You have unsaved changes in '${document?.title ?: "this document"}'. Would you like to save before leaving?")
            },
            confirmButton = {
                Button(
                    onClick = {
                        coroutineScope.launch {
                            viewModel.flushSave()
                            showDiscardDialog = false
                            onNavigateBack()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = WordPrimary),
                    modifier = Modifier.testTag("save_and_exit_button")
                ) {
                    Text("Save & Exit")
                }
            },
            dismissButton = {
                Row {
                    TextButton(
                        onClick = {
                            viewModel.dismissRecoveryDraft()
                            showDiscardDialog = false
                            onNavigateBack()
                        },
                        modifier = Modifier.testTag("discard_changes_button")
                    ) {
                        Text("Discard", color = MaterialTheme.colorScheme.error)
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    TextButton(onClick = { showDiscardDialog = false }) {
                        Text("Keep Editing")
                    }
                }
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
        val cell = table.cells.getOrNull(row)?.getOrNull(col) ?: TableCell()
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

    // Export PDF Dialog with Destination Selection
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
        val doc = document
        AlertDialog(
            onDismissRequest = { showStatsDialog = false },
            title = { Text("Document Statistics") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    StatRow("Words", "${doc?.wordCount ?: 0}")
                    StatRow("Characters", "${doc?.characterCount ?: 0}")
                    StatRow("Paragraphs", "${doc?.blocks?.count { it is ParagraphBlock } ?: 0}")
                    StatRow("Pages", "$actualPageCount")
                    StatRow("Paper Size", doc?.layoutConfig?.pageSize?.displayName ?: "A4")
                    StatRow("Orientation", doc?.layoutConfig?.orientation?.name ?: "PORTRAIT")
                    StatRow("Active Language", if (activeParagraph?.isRtl == true) "Urdu (RTL Nastaliq)" else "English (LTR)")
                }
            },
            confirmButton = {
                Button(
                    onClick = { showStatsDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = WordPrimary)
                ) {
                    Text("Close")
                }
            }
        )
    }
}

@Composable
fun StatRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun AutosaveChip(status: AutosaveStatus) {
    val (text, color, icon) = when (status) {
        AutosaveStatus.IDLE -> Triple("Saved", Color(0xFF107C41), Icons.Default.Check)
        AutosaveStatus.DIRTY -> Triple("Editing", Color(0xFFD83B01), Icons.Outlined.Sync)
        AutosaveStatus.SAVING -> Triple("Saving...", Color(0xFF185ABD), Icons.Outlined.Sync)
        AutosaveStatus.SAVED -> Triple("Saved", Color(0xFF107C41), Icons.Default.Check)
        AutosaveStatus.ERROR -> Triple("Sync Error", Color.Red, Icons.Outlined.SyncProblem)
    }

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = color.copy(alpha = 0.12f),
        modifier = Modifier.padding(end = 4.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(13.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = text,
                color = color,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

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
            // Tab Header Strip with Single-Line Tabs & Compact Collapse Toggle
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

            // Toolbar Contents based on Selected Tab
            AnimatedVisibility(visible = isRibbonVisible) {
                when (activeTab) {
                    RibbonTab.HOME -> {
                        // Reusable full-featured Formatting Toolbar
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
    icon: androidx.compose.ui.graphics.vector.ImageVector,
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
    icon: androidx.compose.ui.graphics.vector.ImageVector,
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

private fun isColorDarkHex(hex: String): Boolean {
    return try {
        val colorInt = android.graphics.Color.parseColor(hex)
        val r = android.graphics.Color.red(colorInt) / 255.0
        val g = android.graphics.Color.green(colorInt) / 255.0
        val b = android.graphics.Color.blue(colorInt) / 255.0
        val luminance = 0.2126 * r + 0.7152 * g + 0.0722 * b
        luminance < 0.45
    } catch (e: Exception) {
        false
    }
}

@Composable
fun DocumentRulerBar(
    layout: PageLayoutConfig,
    maxSheetWidth: androidx.compose.ui.unit.Dp
) {
    val leftMarginDp = (layout.leftMarginMm * 1.25f).dp.coerceIn(16.dp, 48.dp)
    val rightMarginDp = (layout.rightMarginMm * 1.25f).dp.coerceIn(16.dp, 48.dp)
    val totalWidthMm = if (layout.orientation == PageOrientation.LANDSCAPE) {
        layout.pageSize.heightMm.toInt()
    } else {
        layout.pageSize.widthMm.toInt()
    }

    Surface(
        shape = RoundedCornerShape(4.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        border = androidx.compose.foundation.BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier
            .width(maxSheetWidth)
            .padding(bottom = 8.dp)
            .height(22.dp)
            .testTag("document_ruler_bar")
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left Margin Shaded Zone
            Box(
                modifier = Modifier
                    .width(leftMarginDp)
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "${layout.leftMarginMm.toInt()}",
                    fontSize = 8.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Printable Content Tick Marks (in cm)
            Row(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                val cmSteps = (totalWidthMm / 20).coerceIn(4, 12)
                for (i in 1..cmSteps) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "${i * 2}",
                            fontSize = 8.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Box(
                            modifier = Modifier
                                .width(1.dp)
                                .height(4.dp)
                                .background(MaterialTheme.colorScheme.onSurfaceVariant)
                        )
                    }
                }
            }

            // Right Margin Shaded Zone
            Box(
                modifier = Modifier
                    .width(rightMarginDp)
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "${layout.rightMarginMm.toInt()}",
                    fontSize = 8.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun DocumentCanvas(
    document: RichDocument,
    paginatedPages: List<com.example.core.layout.DocumentPage>,
    viewMode: EditorViewMode,
    zoomPercent: Int,
    showRuler: Boolean = true,
    isTabletOrExpanded: Boolean,
    activeBlockIndex: Int,
    onBlockSelected: (Int) -> Unit,
    onUpdateBlockText: (Int, String, TextRange) -> Unit,
    onUpdateSubSliceText: (Int, Int, Int, String, TextRange) -> Unit = { idx, _, _, text, sel ->
        onUpdateBlockText(idx, text, sel)
    },
    onSelectionChanged: (TextRange) -> Unit,
    onInsertParagraph: () -> Unit,
    onDeleteCurrentBlock: () -> Unit,
    onDeleteBlockById: (String) -> Unit = {},
    onUpdateUnifiedText: (String, TextRange) -> Unit = { _, _ -> },
    onDeleteOrMergeWithPrevious: (Int) -> Unit = {},
    onEditTableProperties: (TableBlock) -> Unit = {},
    onEditTableCell: (TableBlock, Int, Int) -> Unit = { _, _, _ -> },
    onQuickAddTableRow: (String) -> Unit = {},
    onQuickDeleteTableRow: (String) -> Unit = {},
    onQuickAddTableCol: (String) -> Unit = {},
    onQuickDeleteTableCol: (String) -> Unit = {},
    onEditImageProperties: (ImageBlock) -> Unit = {},
    onOpenPageLayoutDialog: () -> Unit = {},
    onSelectionChangedForBlock: (Int, TextRange) -> Unit = { _, _ -> },
    onSplitParagraph: (Int, Int) -> Unit = { _, _ -> }
) {
    val scrollState = rememberScrollState()
    val zoomScale = (zoomPercent / 100f).coerceIn(0.5f, 2.0f)

    // Pre-compute global sequential list numbers across the entire document so numbered lists
    // never reset to 1 at page boundaries
    val globalListNumbers = remember(document.blocks) {
        val map = mutableMapOf<Int, Int>()
        var counter = 1
        document.blocks.forEachIndexed { idx, block ->
            if (block is ParagraphBlock && block.listType == ListType.NUMBERED) {
                map[idx] = counter++
            } else {
                counter = 1
            }
        }
        map
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(if (isTabletOrExpanded) 28.dp else 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (viewMode == EditorViewMode.PAGINATED_A4) {
            val hasOnlyParagraphs = document.blocks.all { it is ParagraphBlock }
            val pagesToDisplay = if (hasOnlyParagraphs) {
                listOf(com.example.core.layout.DocumentPage(
                    pageIndex = 0,
                    pageNumber = 1,
                    totalPages = 1,
                    layout = document.layoutConfig,
                    items = emptyList()
                ))
            } else if (paginatedPages.isEmpty()) {
                com.example.core.layout.PaginationEngine.paginate(document)
            } else {
                paginatedPages
            }

            // Proportional physical sheet width based on paper size points (anchored to A4 portrait baseline 595 pt)
            val a4PortraitReferenceWidth = if (isTabletOrExpanded) 720f else 580f
            val baseSheetWidth = (document.layoutConfig.pageWidthPt.toFloat() / 595f) * a4PortraitReferenceWidth
            val scaledMaxSheetWidth = (baseSheetWidth * zoomScale).dp

            pagesToDisplay.forEachIndexed { pageIdx, page ->


                if (showRuler && pageIdx == 0) {
                    DocumentRulerBar(
                        layout = document.layoutConfig,
                        maxSheetWidth = scaledMaxSheetWidth
                    )
                }

                val pageBgColor = try {
                    Color(android.graphics.Color.parseColor(page.layout.pageBackgroundHex))
                } catch (e: Exception) { Color.White }
                val isDarkPage = isColorDarkHex(page.layout.pageBackgroundHex)

                val borderStroke = when (page.layout.borderStyle) {
                    com.example.core.layout.PageBorderStyle.SINGLE_THIN -> androidx.compose.foundation.BorderStroke(1.dp, Color(try { android.graphics.Color.parseColor(page.layout.borderColorHex) } catch (e: Exception) { android.graphics.Color.LTGRAY }))
                    com.example.core.layout.PageBorderStyle.SINGLE_THICK -> androidx.compose.foundation.BorderStroke(2.dp, Color(try { android.graphics.Color.parseColor(page.layout.borderColorHex) } catch (e: Exception) { android.graphics.Color.LTGRAY }))
                    com.example.core.layout.PageBorderStyle.DOUBLE -> androidx.compose.foundation.BorderStroke(3.dp, Color(try { android.graphics.Color.parseColor(page.layout.borderColorHex) } catch (e: Exception) { android.graphics.Color.LTGRAY }))
                    com.example.core.layout.PageBorderStyle.DASHED -> androidx.compose.foundation.BorderStroke(1.5.dp, Color(try { android.graphics.Color.parseColor(page.layout.borderColorHex) } catch (e: Exception) { android.graphics.Color.LTGRAY }))
                    com.example.core.layout.PageBorderStyle.NONE -> null
                }

                // Physical Paper Sheet Dimensions and Aspect Ratio
                val sheetHeightDp = (scaledMaxSheetWidth.value / page.layout.aspectRatio).dp

                // Margins accurately mapped to physical sheet proportions
                val leftMarginDp = ((page.layout.leftMarginPt / page.layout.pageWidthPt) * scaledMaxSheetWidth.value).dp
                val rightMarginDp = ((page.layout.rightMarginPt / page.layout.pageWidthPt) * scaledMaxSheetWidth.value).dp
                val topMarginDp = ((page.layout.topMarginPt / page.layout.pageHeightPt) * sheetHeightDp.value).dp
                val bottomMarginDp = ((page.layout.bottomMarginPt / page.layout.pageHeightPt) * sheetHeightDp.value).dp

                // Physical Paper Sheet Surface (clean sheet, no rounded card box)
                Surface(
                    modifier = Modifier
                        .width(scaledMaxSheetWidth)
                        .heightIn(min = sheetHeightDp)
                        .shadow(2.dp, RoundedCornerShape(0.dp)),
                    shape = RoundedCornerShape(0.dp),
                    color = pageBgColor,
                    border = borderStroke
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(
                                start = leftMarginDp,
                                end = rightMarginDp,
                                top = topMarginDp,
                                bottom = bottomMarginDp
                            )
                    ) {
                        // Header Area
                        if (page.hasHeader && page.formattedHeader.isNotBlank()) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = when (page.layout.headerAlignment) {
                                    TextAlignment.CENTER -> Arrangement.Center
                                    TextAlignment.END -> Arrangement.End
                                    else -> Arrangement.Start
                                }
                            ) {
                                Text(
                                    text = page.formattedHeader,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (isDarkPage) Color(0xFFCBD5E1) else Color.Gray
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            HorizontalDivider(
                                color = if (isDarkPage) Color(0xFF334155) else Color(0xFFE5E7EB),
                                thickness = 0.5.dp
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                        }

                        // Content items: Each paragraph and block rendered with its own formatting and alignment
                        if (page.items.isEmpty()) {
                            val dummyBlock = document.blocks.firstOrNull() as? ParagraphBlock ?: ParagraphBlock()
                            BlockItemView(
                                block = dummyBlock,
                                itemNumber = null,
                                isActive = true,
                                isDarkPage = isDarkPage,
                                zoomScale = zoomScale,
                                canDeleteBlock = false,
                                onFocus = { onBlockSelected(0) },
                                onSelectionChanged = { sel -> onSelectionChangedForBlock(0, sel) },
                                onTextChanged = { newText, sel -> onUpdateBlockText(0, newText, sel) },
                                onEnterPressed = { cursorOffset -> onSplitParagraph(0, cursorOffset) },
                                onDeleteBlock = {},
                                onDeleteOrMergeWithPrevious = {}
                            )
                        } else {
                            page.items.forEach { pageItem ->
                                when (pageItem) {
                                    is com.example.core.layout.ParagraphPageItem -> {
                                        val block = pageItem.originalBlock
                                        val isSplitSlice = pageItem.startChar > 0 || (pageItem.endChar > 0 && pageItem.endChar < block.text.length)
                                        val sliceStart = if (isSplitSlice) pageItem.startChar.coerceIn(0, block.text.length) else 0
                                        val sliceEnd = if (isSplitSlice) pageItem.endChar.coerceIn(sliceStart, block.text.length) else block.text.length

                                        val displayBlock = remember(block, isSplitSlice, sliceStart, sliceEnd) {
                                            if (isSplitSlice) {
                                                val subSpans = block.spans.mapNotNull { span ->
                                                    val spStart = (span.start - sliceStart).coerceAtLeast(0)
                                                    val spEnd = (span.end - sliceStart).coerceAtMost(sliceEnd - sliceStart)
                                                    if (spStart < spEnd) span.copy(start = spStart, end = spEnd) else null
                                                }
                                                block.copy(
                                                    text = block.text.substring(sliceStart, sliceEnd),
                                                    spans = subSpans,
                                                    listType = if (pageItem.isContinuation) ListType.NONE else block.listType
                                                )
                                            } else {
                                                block
                                            }
                                        }

                                        val itemNumber = if (!pageItem.isContinuation && block.listType == ListType.NUMBERED) {
                                            globalListNumbers[pageItem.originalBlockIndex] ?: 1
                                        } else {
                                            null
                                        }

                                        BlockItemView(
                                            block = displayBlock,
                                            itemNumber = itemNumber,
                                            isActive = pageItem.originalBlockIndex == activeBlockIndex,
                                            isDarkPage = isDarkPage,
                                            zoomScale = zoomScale,
                                            canDeleteBlock = document.blocks.size > 1,
                                            onFocus = { onBlockSelected(pageItem.originalBlockIndex) },
                                            onSelectionChanged = { sel ->
                                                onSelectionChangedForBlock(
                                                    pageItem.originalBlockIndex,
                                                    TextRange(
                                                        (sliceStart + sel.start).coerceIn(0, block.text.length),
                                                        (sliceStart + sel.end).coerceIn(0, block.text.length)
                                                    )
                                                )
                                            },
                                            onTextChanged = { newText, sel ->
                                                if (isSplitSlice) {
                                                    onUpdateSubSliceText(pageItem.originalBlockIndex, sliceStart, sliceEnd, newText, sel)
                                                } else {
                                                    onUpdateBlockText(pageItem.originalBlockIndex, newText, sel)
                                                }
                                            },
                                            onEnterPressed = { cursorOffset ->
                                                onSplitParagraph(pageItem.originalBlockIndex, sliceStart + cursorOffset)
                                            },
                                            onDeleteBlock = { onDeleteBlockById(block.id) },
                                            onDeleteOrMergeWithPrevious = { onDeleteOrMergeWithPrevious(pageItem.originalBlockIndex) }
                                        )
                                        Spacer(modifier = Modifier.height(6.dp))
                                    }

                                is com.example.core.layout.TablePageItem -> {
                                    TableBlockView(
                                        table = pageItem.originalBlock,
                                        startRow = pageItem.startRow,
                                        endRow = pageItem.endRow,
                                        isDarkPage = isDarkPage,
                                        zoomScale = zoomScale,
                                        onEditProperties = { onEditTableProperties(pageItem.originalBlock) },
                                        onEditCell = { r, c -> onEditTableCell(pageItem.originalBlock, r, c) },
                                        onAddRow = { onQuickAddTableRow(pageItem.originalBlock.id) },
                                        onDeleteRow = { onQuickDeleteTableRow(pageItem.originalBlock.id) },
                                        onAddCol = { onQuickAddTableCol(pageItem.originalBlock.id) },
                                        onDeleteCol = { onQuickDeleteTableCol(pageItem.originalBlock.id) }
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                }

                                is com.example.core.layout.ImagePageItem -> {
                                    ImageBlockView(
                                        imageBlock = pageItem.originalBlock,
                                        zoomScale = zoomScale,
                                        onFocus = { onBlockSelected(pageItem.originalBlockIndex) },
                                        onEditProperties = { onEditImageProperties(pageItem.originalBlock) }
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                }

                                is com.example.core.layout.DividerPageItem -> {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        HorizontalDivider(
                                            color = if (isDarkPage) Color(0xFF475569) else Color(0xFFCBD5E1),
                                            thickness = 1.dp,
                                            modifier = Modifier.weight(1f)
                                        )
                                        if (document.blocks.size > 1) {
                                            IconButton(
                                                onClick = { onDeleteBlockById(pageItem.originalBlock.id) },
                                                modifier = Modifier.size(24.dp).testTag("delete_divider_${pageItem.originalBlock.id}")
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Delete,
                                                    contentDescription = "Delete Horizontal Line",
                                                    tint = if (isDarkPage) Color(0xFF94A3B8) else Color.Gray,
                                                    modifier = Modifier.size(14.dp)
                                                )
                                            }
                                        }
                                    }
                                }

                                is com.example.core.layout.PageBreakPageItem -> {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        HorizontalDivider(modifier = Modifier.weight(1f), color = WordPrimary.copy(alpha = 0.35f))
                                        Text(
                                            text = " PAGE BREAK ",
                                            fontSize = 9.sp,
                                            color = WordPrimary,
                                            fontWeight = FontWeight.Bold
                                        )
                                        HorizontalDivider(modifier = Modifier.weight(1f), color = WordPrimary.copy(alpha = 0.35f))
                                        if (document.blocks.size > 1) {
                                            IconButton(
                                                onClick = { onDeleteBlockById(pageItem.originalBlock.id) },
                                                modifier = Modifier.size(24.dp).testTag("delete_page_break_${pageItem.originalBlock.id}")
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Delete,
                                                    contentDescription = "Remove Page Break",
                                                    tint = WordPrimary,
                                                    modifier = Modifier.size(14.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                        // Footer Area (only rendered if footer exists and is non-blank)
                        if (page.hasFooter && page.formattedFooter.isNotBlank()) {
                            Spacer(modifier = Modifier.height(20.dp))
                            HorizontalDivider(
                                color = if (isDarkPage) Color(0xFF334155) else Color(0xFFE5E7EB),
                                thickness = 0.5.dp
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = when (page.layout.footerAlignment) {
                                    TextAlignment.START -> Arrangement.Start
                                    TextAlignment.END -> Arrangement.End
                                    else -> Arrangement.Center
                                }
                            ) {
                                Text(
                                    text = page.formattedFooter,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (isDarkPage) Color(0xFFCBD5E1) else Color.Gray
                                )
                            }
                        }
                    }
                }

                // Desk gap between physical paper sheets
                Spacer(modifier = Modifier.height(24.dp))
            }
        } else {
            // Continuous Mobile View (clean seamless sheet, no card box)
            val pageBgColor = try {
                Color(android.graphics.Color.parseColor(document.layoutConfig.pageBackgroundHex))
            } catch (e: Exception) { Color.White }
            val isDarkPage = isColorDarkHex(document.layoutConfig.pageBackgroundHex)

            if (showRuler) {
                DocumentRulerBar(
                    layout = document.layoutConfig,
                    maxSheetWidth = 920.dp
                )
            }

            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = pageBgColor
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    document.blocks.forEachIndexed { index, block ->
                        val itemNumber = if (block is ParagraphBlock && block.listType == ListType.NUMBERED) {
                            globalListNumbers[index] ?: 1
                        } else {
                            null
                        }

                        BlockItemView(
                            block = block,
                            itemNumber = itemNumber,
                            isActive = index == activeBlockIndex,
                            isDarkPage = isDarkPage,
                            zoomScale = zoomScale,
                            canDeleteBlock = document.blocks.size > 1,
                            onFocus = { onBlockSelected(index) },
                            onSelectionChanged = { sel -> onSelectionChangedForBlock(index, sel) },
                            onTextChanged = { newText, sel -> onUpdateBlockText(index, newText, sel) },
                            onEnterPressed = { cursorOffset -> onSplitParagraph(index, cursorOffset) },
                            onDeleteBlock = { onDeleteBlockById(block.id) },
                            onDeleteOrMergeWithPrevious = { onDeleteOrMergeWithPrevious(index) },
                            onEditTableProperties = { if (block is TableBlock) onEditTableProperties(block) },
                            onEditTableCell = { r, c -> if (block is TableBlock) onEditTableCell(block, r, c) },
                            onAddTableRow = { if (block is TableBlock) onQuickAddTableRow(block.id) },
                            onDeleteTableRow = { if (block is TableBlock) onQuickDeleteTableRow(block.id) },
                            onAddTableCol = { if (block is TableBlock) onQuickAddTableCol(block.id) },
                            onDeleteTableCol = { if (block is TableBlock) onQuickDeleteTableCol(block.id) },
                            onEditImageProperties = { if (block is ImageBlock) onEditImageProperties(block) }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun TableBlockView(
    table: TableBlock,
    startRow: Int = 0,
    endRow: Int = table.rowCount,
    isDarkPage: Boolean = false,
    zoomScale: Float = 1f,
    onEditProperties: () -> Unit = {},
    onEditCell: (row: Int, col: Int) -> Unit = { _, _ -> },
    onAddRow: () -> Unit = {},
    onDeleteRow: () -> Unit = {},
    onAddCol: () -> Unit = {},
    onDeleteCol: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val tableBorderColor = try {
        Color(android.graphics.Color.parseColor(table.borderColorHex))
    } catch (e: Exception) {
        Color(0xFFCBD5E1)
    }

    val tableAlignment = when (table.alignment) {
        TextAlignment.START -> Alignment.Start
        TextAlignment.END -> Alignment.End
        else -> Alignment.CenterHorizontally
    }

    val widthFraction = (table.tableWidthPercent.coerceIn(20, 100) / 100f)
    val safeStartRow = startRow.coerceIn(0, table.rowCount)
    val safeEndRow = endRow.coerceIn(safeStartRow, table.rowCount)

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = tableAlignment
    ) {
        // Table Header Quick Action Toolbar (shown on first slice of table)
        if (safeStartRow == 0) {
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                shape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp),
                modifier = Modifier.fillMaxWidth(widthFraction)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.TableChart,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = WordPrimary
                        )
                        Text(
                            text = "Table (${table.rowCount}×${table.columnCount})",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = WordPrimary.copy(alpha = 0.12f),
                            modifier = Modifier
                                .clickable(onClick = onAddRow)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                .testTag("quick_add_row_${table.id}")
                        ) {
                            Text("+ Row", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = WordPrimary)
                        }

                        if (table.rowCount > 1) {
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.7f),
                                modifier = Modifier
                                    .clickable(onClick = onDeleteRow)
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                    .testTag("quick_del_row_${table.id}")
                            ) {
                                Text("- Row", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onErrorContainer)
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = WordPrimary.copy(alpha = 0.12f),
                            modifier = Modifier
                                .clickable(onClick = onAddCol)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                .testTag("quick_add_col_${table.id}")
                        ) {
                            Text("+ Col", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = WordPrimary)
                        }

                        if (table.columnCount > 1) {
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.7f),
                                modifier = Modifier
                                    .clickable(onClick = onDeleteCol)
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                    .testTag("quick_del_col_${table.id}")
                            ) {
                                Text("- Col", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onErrorContainer)
                            }
                        }

                        IconButton(
                            onClick = onEditProperties,
                            modifier = Modifier.size(24.dp).testTag("table_properties_btn_${table.id}")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Tune,
                                contentDescription = "Table Properties",
                                modifier = Modifier.size(14.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        // Table Body Grid (renders only rows in [safeStartRow, safeEndRow))
        Column(
            modifier = Modifier
                .fillMaxWidth(widthFraction)
                .border(
                    width = if (table.hasBorders) 1.dp else 0.5.dp,
                    color = if (table.hasBorders) tableBorderColor else Color(0xFFE2E8F0),
                    shape = RoundedCornerShape(
                        topStart = if (safeStartRow > 0) 4.dp else 0.dp,
                        topEnd = if (safeStartRow > 0) 4.dp else 0.dp,
                        bottomStart = 8.dp,
                        bottomEnd = 8.dp
                    )
                )
        ) {
            for (r in safeStartRow until safeEndRow) {
                Row(modifier = Modifier.fillMaxWidth()) {
                    for (c in 0 until table.columnCount) {
                        val cell = table.cells.getOrNull(r)?.getOrNull(c) ?: TableCell()

                        val cellBg = when {
                            cell.backgroundColorHex != null -> try {
                                Color(android.graphics.Color.parseColor(cell.backgroundColorHex))
                            } catch (e: Exception) { if (isDarkPage) Color(0xFF1E293B) else Color.White }
                            r == 0 -> if (isDarkPage) Color(0xFF334155) else Color(0xFFF1F5F9)
                            else -> if (isDarkPage) Color(0xFF1E293B) else Color.White
                        }

                        val cellTextAlign = when (cell.alignment) {
                            TextAlignment.CENTER -> TextAlign.Center
                            TextAlignment.END -> TextAlign.End
                            TextAlignment.JUSTIFY -> TextAlign.Justify
                            TextAlignment.START -> TextAlign.Start
                        }

                        val cellIsDark = if (cell.backgroundColorHex != null) {
                            isColorDarkHex(cell.backgroundColorHex)
                        } else {
                            isDarkPage
                        }

                        val colWeight = table.columnWidthWeights.getOrNull(c) ?: 1f
                        Box(
                            modifier = Modifier
                                .weight(colWeight.coerceAtLeast(0.05f))
                                .border(
                                    width = 0.5.dp,
                                    color = if (table.hasBorders) tableBorderColor else Color(0xFFE2E8F0)
                                )
                                .background(cellBg)
                                .clickable { onEditCell(r, c) }
                                .padding(8.dp)
                                .testTag("table_cell_${table.id}_${r}_$c"),
                            contentAlignment = when (cell.alignment) {
                                TextAlignment.CENTER -> Alignment.Center
                                TextAlignment.END -> Alignment.CenterEnd
                                else -> Alignment.CenterStart
                            }
                        ) {
                            Text(
                                text = cell.text.ifBlank { "—" },
                                fontWeight = if (cell.isBold || r == 0) FontWeight.Bold else FontWeight.Normal,
                                fontStyle = if (cell.isItalic) FontStyle.Italic else FontStyle.Normal,
                                fontSize = (12f * zoomScale).sp,
                                textAlign = cellTextAlign,
                                color = when {
                                    cell.text.isBlank() -> if (cellIsDark) Color(0xFF64748B) else Color.LightGray
                                    cellIsDark -> Color(0xFFF8FAFC)
                                    else -> Color(0xFF1E293B)
                                },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ImageBlockView(
    imageBlock: ImageBlock,
    zoomScale: Float = 1f,
    onFocus: () -> Unit = {},
    onEditProperties: () -> Unit = {}
) {
    val context = LocalContext.current
    val imageAlignment = when (imageBlock.alignment) {
        TextAlignment.START -> Alignment.Start
        TextAlignment.END -> Alignment.End
        else -> Alignment.CenterHorizontally
    }
    val scaledWidthDp = (imageBlock.widthDp * zoomScale).dp
    val scaledHeightDp = (imageBlock.heightDp * zoomScale).dp

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onFocus)
            .padding(vertical = 4.dp),
        horizontalAlignment = imageAlignment
    ) {
        Box(
            modifier = Modifier
                .width(scaledWidthDp)
                .height(scaledHeightDp)
                .clip(RoundedCornerShape(8.dp))
                .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(8.dp))
                .clickable { onEditProperties() }
                .testTag("image_block_${imageBlock.id}")
        ) {
            SubcomposeAsyncImage(
                model = ImageRequest.Builder(context)
                    .data(imageBlock.imageUri)
                    .crossfade(true)
                    .build(),
                contentDescription = imageBlock.caption.ifBlank { "Document image" },
                contentScale = ContentScale.Fit,
                modifier = Modifier.fillMaxSize(),
                loading = {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(
                            color = WordPrimary,
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                },
                error = {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color(0xFFF8FAFC)),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.BrokenImage,
                            contentDescription = null,
                            tint = Color.Gray,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = imageBlock.caption.ifBlank { "Picture Asset" },
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                    }
                }
            )

            // Format Overlay Chip at bottom corner
            Surface(
                shape = CircleShape,
                color = WordPrimary.copy(alpha = 0.85f),
                modifier = Modifier
                    .padding(6.dp)
                    .align(Alignment.BottomEnd)
                    .clickable { onEditProperties() }
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Tune,
                        contentDescription = "Format Image",
                        tint = Color.White,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = "${imageBlock.widthDp}dp",
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        if (imageBlock.caption.isNotBlank()) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = imageBlock.caption,
                style = MaterialTheme.typography.bodySmall,
                fontStyle = FontStyle.Italic,
                color = Color.Gray,
                textAlign = when (imageBlock.alignment) {
                    TextAlignment.START -> TextAlign.Start
                    TextAlignment.END -> TextAlign.End
                    else -> TextAlign.Center
                },
                modifier = Modifier.width(scaledWidthDp)
            )
        }
    }
}

/**
 * Unified Text Canvas:
 * Provides a clean, distraction-free, paper-native continuous writing canvas without
 * separated block boundaries. Users can type fluidly with Enter creating new lines,
 * and can delete all text down to an empty clean canvas using Backspace.
 */
@Composable
fun UnifiedTextCanvas(
    document: RichDocument,
    zoomScale: Float = 1f,
    isDarkPage: Boolean = false,
    onSelectionChanged: (TextRange) -> Unit = {},
    onUpdateUnifiedText: (String, TextRange) -> Unit = { _, _ -> },
    modifier: Modifier = Modifier
) {
    val paragraphs = remember(document.blocks) {
        document.blocks.filterIsInstance<ParagraphBlock>()
    }
    val firstParagraph = paragraphs.firstOrNull() ?: ParagraphBlock()
    val fullText = remember(paragraphs) {
        paragraphs.joinToString("\n") { it.text }
    }

    var textFieldValue by remember(document.id) {
        mutableStateOf(TextFieldValue(text = fullText, selection = TextRange(fullText.length)))
    }

    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(document.id) {
        try {
            focusRequester.requestFocus()
        } catch (_: Exception) {}
    }

    LaunchedEffect(fullText) {
        if (textFieldValue.text != fullText) {
            val safeStart = textFieldValue.selection.start.coerceIn(0, fullText.length)
            val safeEnd = textFieldValue.selection.end.coerceIn(0, fullText.length)
            textFieldValue = textFieldValue.copy(
                text = fullText,
                selection = TextRange(safeStart, safeEnd)
            )
        }
    }

    val baseFontSize = (15f * zoomScale).sp
    val defaultTextColor = if (isDarkPage) Color(0xFFF8FAFC) else Color(0xFF1E293B)

    val isRtl = remember(paragraphs, fullText) {
        paragraphs.any { it.isRtl } || com.example.core.typography.BidiUtils.detectParagraphIsRtl(fullText)
    }
    val textDirection = if (isRtl) TextDirection.Rtl else TextDirection.Ltr

    val effectiveFontFamily = com.example.core.typography.DocumentFontManager.getFontFamily(
        fontName = paragraphs.firstOrNull { it.spans.any { sp -> sp.fontFamilyName != null } }
            ?.spans?.firstOrNull { it.fontFamilyName != null }?.fontFamilyName,
        isRtl = isRtl
    )

    val richTransformation = remember(paragraphs, isDarkPage, zoomScale) {
        VisualTransformation { inputText ->
            val annotated = com.example.editor.engine.EditorEngineIntegration.buildAnnotatedTextWithParagraphBlocks(
                text = inputText.text,
                paragraphs = paragraphs,
                zoomScale = zoomScale
            )
            TransformedText(annotated, OffsetMapping.Identity)
        }
    }

    androidx.compose.runtime.CompositionLocalProvider(
        androidx.compose.ui.platform.LocalLayoutDirection provides (if (isRtl) androidx.compose.ui.unit.LayoutDirection.Rtl else androidx.compose.ui.unit.LayoutDirection.Ltr)
    ) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
        ) {
            BasicTextField(
                value = textFieldValue,
                onValueChange = { newTfv ->
                    textFieldValue = newTfv
                    onSelectionChanged(newTfv.selection)
                    if (newTfv.text != fullText) {
                        onUpdateUnifiedText(newTfv.text, newTfv.selection)
                    }
                },
                visualTransformation = richTransformation,
                textStyle = TextStyle(
                    fontSize = baseFontSize,
                    fontFamily = effectiveFontFamily,
                    color = defaultTextColor,
                    textAlign = TextAlign.Unspecified,
                    textDirection = textDirection,
                    lineHeight = baseFontSize * (if (isRtl) 1.4f else 1.25f)
                ),
                cursorBrush = SolidColor(if (isDarkPage) Color(0xFF60A5FA) else WordPrimary),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 350.dp)
                    .focusRequester(focusRequester)
                    .testTag("unified_text_canvas")
                    .testTag("paragraph_input_${firstParagraph.id}"),
                decorationBox = { innerTextField ->
                    Box(modifier = Modifier.fillMaxWidth()) {
                        if (textFieldValue.text.isEmpty()) {
                            Text(
                                text = "Start typing...",
                                style = TextStyle(
                                    fontSize = baseFontSize,
                                    fontFamily = effectiveFontFamily,
                                    color = if (isDarkPage) Color(0xFF64748B) else Color(0xFF94A3B8),
                                    textAlign = TextAlign.Start,
                                    textDirection = textDirection
                                )
                            )
                        }
                        innerTextField()
                    }
                }
            )
        }
    }

}

@Composable
fun BlockItemView(
    block: BlockElement,
    itemNumber: Int?,
    isActive: Boolean,
    isDarkPage: Boolean = false,
    zoomScale: Float = 1f,
    canDeleteBlock: Boolean = false,
    onFocus: () -> Unit,
    onSelectionChanged: (TextRange) -> Unit,
    onTextChanged: (String, TextRange) -> Unit,
    onEnterPressed: (Int) -> Unit = {},
    onDeleteBlock: () -> Unit = {},
    onDeleteOrMergeWithPrevious: () -> Unit = {},
    onEditTableProperties: () -> Unit = {},
    onEditTableCell: (Int, Int) -> Unit = { _, _ -> },
    onAddTableRow: () -> Unit = {},
    onDeleteTableRow: () -> Unit = {},
    onAddTableCol: () -> Unit = {},
    onDeleteTableCol: () -> Unit = {},
    onEditImageProperties: () -> Unit = {}
) {
    when (block) {
        is ParagraphBlock -> {
            val align = when (block.alignment) {
                TextAlignment.CENTER -> TextAlign.Center
                TextAlignment.END -> TextAlign.End
                TextAlignment.JUSTIFY -> TextAlign.Justify
                TextAlignment.START -> TextAlign.Start
            }

            val textDirection = if (block.isRtl) TextDirection.Rtl else TextDirection.Ltr

            val effectiveFontFamily = com.example.core.typography.DocumentFontManager.getFontFamily(
                fontName = block.spans.firstOrNull { it.fontFamilyName != null }?.fontFamilyName,
                isRtl = block.isRtl
            )

            val unscaledFontSize = when (block.headingStyle) {
                HeadingStyle.TITLE -> 26f
                HeadingStyle.SUBTITLE -> 18f
                HeadingStyle.HEADING_1 -> 22f
                HeadingStyle.HEADING_2 -> 18f
                HeadingStyle.HEADING_3 -> 16f
                HeadingStyle.QUOTE -> 15f
                HeadingStyle.NORMAL -> 15f
            }
            val baseFontSize = (unscaledFontSize * zoomScale).sp

            val defaultTextColor = when {
                block.headingStyle == HeadingStyle.TITLE || block.headingStyle == HeadingStyle.HEADING_1 ->
                    if (isDarkPage) Color(0xFF60A5FA) else WordPrimary
                isDarkPage -> Color(0xFFF8FAFC)
                else -> Color(0xFF1E293B)
            }

            // Stateful TextFieldValue to accurately capture selection, cursor, and text deltas
            var textFieldValue by remember(block.id) {
                mutableStateOf(TextFieldValue(text = block.text, selection = TextRange(block.text.length)))
            }

            val focusRequester = remember { FocusRequester() }

            LaunchedEffect(isActive) {
                if (isActive) {
                    try {
                        focusRequester.requestFocus()
                    } catch (_: Exception) {}
                }
            }

            // Sync with external updates (such as undo, redo, find & replace, or slice updates)
            LaunchedEffect(block.text) {
                if (textFieldValue.text != block.text) {
                    val safeStart = textFieldValue.selection.start.coerceIn(0, block.text.length)
                    val safeEnd = textFieldValue.selection.end.coerceIn(0, block.text.length)
                    textFieldValue = textFieldValue.copy(
                        text = block.text,
                        selection = TextRange(safeStart, safeEnd)
                    )
                }
            }

            // VisualTransformation applying rich text styles from block.spans
            val richTransformation = remember(block.text, block.spans, block.headingStyle) {
                VisualTransformation { _ ->
                    val annotated = com.example.editor.engine.EditorEngineIntegration.buildAnnotatedParagraph(block)
                    TransformedText(annotated, OffsetMapping.Identity)
                }
            }

            val indentPadding = if (block.leftIndentPt > 0f) (block.leftIndentPt * zoomScale).dp else (block.indentationLevel * 20 * zoomScale).dp
            val spaceBeforeDp = (block.spaceBeforePt * zoomScale).dp
            val spaceAfterDp = (block.spaceAfterPt * zoomScale).dp

            androidx.compose.runtime.CompositionLocalProvider(
                androidx.compose.ui.platform.LocalLayoutDirection provides (if (block.isRtl) androidx.compose.ui.unit.LayoutDirection.Rtl else androidx.compose.ui.unit.LayoutDirection.Ltr)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = indentPadding, top = spaceBeforeDp, bottom = spaceAfterDp)
                        .clickable(onClick = onFocus),
                    verticalAlignment = Alignment.Top
                ) {
                    // List prefix
                    when (block.listType) {
                        ListType.BULLET -> {
                            Text(
                                text = "• ",
                                fontWeight = FontWeight.Bold,
                                fontSize = baseFontSize,
                                color = if (isDarkPage) Color(0xFF60A5FA) else WordPrimary,
                                modifier = Modifier.padding(end = 6.dp)
                            )
                        }
                        ListType.NUMBERED -> {
                            val formattedNumber = com.example.core.typography.BidiUtils.formatListNumber(
                                number = itemNumber ?: 1,
                                isRtl = block.isRtl,
                                useUrduNumerals = false
                            )
                            Text(
                                text = formattedNumber,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = baseFontSize,
                                color = if (isDarkPage) Color(0xFF60A5FA) else WordPrimary,
                                modifier = Modifier.padding(end = 6.dp)
                            )
                        }
                        ListType.NONE -> Unit
                    }

                    Box(
                        modifier = Modifier.weight(1f)
                    ) {
                        BasicTextField(
                            value = textFieldValue,
                            onValueChange = { newTfv ->
                                textFieldValue = newTfv
                                onSelectionChanged(newTfv.selection)
                                if (newTfv.text != block.text) {
                                    onTextChanged(newTfv.text, newTfv.selection)
                                }
                            },
                            visualTransformation = richTransformation,
                            textStyle = TextStyle(
                                fontSize = baseFontSize,
                                fontFamily = effectiveFontFamily,
                                fontWeight = if (block.headingStyle.isBold) FontWeight.Bold else FontWeight.Normal,
                                fontStyle = if (block.headingStyle == HeadingStyle.QUOTE) FontStyle.Italic else FontStyle.Normal,
                                color = defaultTextColor,
                                textAlign = align,
                                textDirection = textDirection,
                                lineHeight = baseFontSize * (if (block.isRtl) block.lineSpacingMultiplier.coerceAtLeast(1.4f) else block.lineSpacingMultiplier)
                            ),
                            cursorBrush = SolidColor(if (isDarkPage) Color(0xFF60A5FA) else WordPrimary),
                            modifier = Modifier
                                .fillMaxWidth()
                                .focusRequester(focusRequester)
                                .onPreviewKeyEvent { event ->
                                    if (event.type == KeyEventType.KeyDown && event.key == Key.Backspace) {
                                        if (textFieldValue.selection.collapsed && textFieldValue.selection.start == 0) {
                                            onDeleteOrMergeWithPrevious()
                                            true
                                        } else {
                                            false
                                        }
                                    } else if (event.type == KeyEventType.KeyDown && (event.key == Key.Enter || event.key == Key.NumPadEnter)) {
                                        if (!event.isShiftPressed) {
                                            onEnterPressed(textFieldValue.selection.start)
                                            true
                                        } else {
                                            false
                                        }
                                    } else {
                                        false
                                    }
                                }
                                .onFocusChanged { focusState ->
                                    if (focusState.isFocused) {
                                        onFocus()
                                    }
                                }
                                .testTag("paragraph_input_${block.id}"),
                            decorationBox = { innerTextField ->
                                Box(modifier = Modifier.fillMaxWidth()) {
                                    if (textFieldValue.text.isEmpty() && !canDeleteBlock) {
                                        Text(
                                            text = "Start typing...",
                                            style = TextStyle(
                                                fontSize = baseFontSize,
                                                fontFamily = effectiveFontFamily,
                                                color = if (isDarkPage) Color(0xFF64748B) else Color(0xFF94A3B8),
                                                textAlign = align,
                                                textDirection = textDirection
                                            )
                                        )
                                    }
                                    innerTextField()
                                }
                            }
                        )
                    }
                }
            }
        }

        is TableBlock -> {
            TableBlockView(
                table = block,
                isDarkPage = isDarkPage,
                zoomScale = zoomScale,
                onEditProperties = onEditTableProperties,
                onEditCell = onEditTableCell,
                onAddRow = onAddTableRow,
                onDeleteRow = onDeleteTableRow,
                onAddCol = onAddTableCol,
                onDeleteCol = onDeleteTableCol
            )
        }

        is ImageBlock -> {
            ImageBlockView(
                imageBlock = block,
                zoomScale = zoomScale,
                onFocus = onFocus,
                onEditProperties = onEditImageProperties
            )
        }

        is DividerBlock -> {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                HorizontalDivider(
                    color = if (isDarkPage) Color(0xFF475569) else Color(0xFFCBD5E1),
                    thickness = 1.dp,
                    modifier = Modifier.weight(1f)
                )
                if (canDeleteBlock) {
                    IconButton(
                        onClick = onDeleteBlock,
                        modifier = Modifier.size(24.dp).testTag("delete_divider_${block.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete Horizontal Line",
                            tint = if (isDarkPage) Color(0xFF94A3B8) else Color.Gray,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
        }

        is PageBreakBlock -> {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                HorizontalDivider(modifier = Modifier.weight(1f), color = WordPrimary.copy(alpha = 0.4f))
                Text(
                    text = " PAGE BREAK ",
                    fontSize = 10.sp,
                    color = WordPrimary,
                    fontWeight = FontWeight.Bold
                )
                HorizontalDivider(modifier = Modifier.weight(1f), color = WordPrimary.copy(alpha = 0.4f))
                if (canDeleteBlock) {
                    IconButton(
                        onClick = onDeleteBlock,
                        modifier = Modifier.size(24.dp).testTag("delete_page_break_${block.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Remove Page Break",
                            tint = WordPrimary,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun EditorStatusBar(
    document: RichDocument?,
    currentPageNumber: Int = 1,
    totalPages: Int = document?.estimatedPageCount ?: 1,
    showWordCount: Boolean = true,
    autosaveStatus: AutosaveStatus,
    zoomPercent: Int,
    activeBlock: ParagraphBlock?,
    onOpenLayoutDialog: () -> Unit = {}
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 4.dp,
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (showWordCount) {
                    Text(
                        text = "${document?.wordCount ?: 0} words",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text("•", fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
                    Text(
                        text = "${document?.characterCount ?: 0} chars",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text("•", fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
                }
                Text(
                    text = if (activeBlock?.isRtl == true) "Urdu (RTL)" else "English (LTR)",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = WordPrimary
                )
            }

            Text(
                text = "$zoomPercent%",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

fun sharePdfFile(context: Context, file: File) {
    try {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        val chooser = Intent.createChooser(sendIntent, "Share Document PDF")
        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(chooser)
    } catch (e: Exception) {
        // Fallback
    }
}

fun shareTextFile(context: Context, file: File) {
    try {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        val chooser = Intent.createChooser(sendIntent, "Share Plain Text Document")
        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(chooser)
    } catch (e: Exception) {
        // Fallback
    }
}

fun shareDocxFile(context: Context, file: File) {
    try {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            type = "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        val chooser = Intent.createChooser(sendIntent, "Share Word Document (.docx)")
        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(chooser)
    } catch (e: Exception) {
        // Fallback
    }
}
