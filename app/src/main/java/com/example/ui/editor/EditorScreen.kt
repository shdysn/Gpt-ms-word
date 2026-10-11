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
import androidx.compose.material.icons.filled.FormatListNumbered
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
import com.example.ui.editor.components.FindReplaceBar
import com.example.ui.editor.components.FormattingToolbar
import com.example.ui.editor.components.HeaderFooterEditDialog
import com.example.ui.editor.components.ImagePropertiesDialog
import com.example.ui.editor.components.InsertHyperlinkDialog
import com.example.ui.editor.components.InsertTableDialog
import com.example.ui.editor.components.PageLayoutDialog
import com.example.ui.editor.components.PageNumberInsertDialog
import com.example.ui.editor.components.TableCellEditDialog
import com.example.ui.editor.components.TablePropertiesDialog
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

    val coroutineScope = androidx.compose.runtime.rememberCoroutineScope()
    var showTitleEditDialog by remember { mutableStateOf(false) }
    var showSaveAsDialog by remember { mutableStateOf(false) }
    var showDiscardDialog by remember { mutableStateOf(false) }
    var showTableDialog by remember { mutableStateOf(false) }
    var showLinkDialog by remember { mutableStateOf(false) }
    var showLayoutDialog by remember { mutableStateOf(false) }
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

                        // Share / Export PDF Quick Action
                        IconButton(
                            onClick = {
                                viewModel.exportToPdf(context) { file ->
                                    file?.let { sharePdfFile(context, it) }
                                }
                            },
                            modifier = Modifier.testTag("action_share_pdf")
                        ) {
                            Icon(Icons.Default.Share, contentDescription = "Share PDF")
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
                                    text = { Text("Save Document") },
                                    leadingIcon = { Icon(Icons.Default.Save, contentDescription = null) },
                                    onClick = {
                                        showOptionsMenu = false
                                        viewModel.saveNow()
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Save As (Duplicate)...") },
                                    leadingIcon = { Icon(Icons.Default.Save, contentDescription = null) },
                                    onClick = {
                                        showOptionsMenu = false
                                        showSaveAsDialog = true
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Rename Document...") },
                                    leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) },
                                    onClick = {
                                        showOptionsMenu = false
                                        showTitleEditDialog = true
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Save as DOCX (SAF)...") },
                                    leadingIcon = { Icon(Icons.Default.Description, contentDescription = null, tint = WordPrimary) },
                                    onClick = {
                                        showOptionsMenu = false
                                        val safeTitle = (document?.title ?: "document").replace("[^a-zA-Z0-9.-]".toRegex(), "_")
                                        safDocxSaveLauncher.launch("$safeTitle.docx")
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Export as Word DOCX (.docx)") },
                                    leadingIcon = { Icon(Icons.Default.Share, contentDescription = null, tint = WordPrimary) },
                                    onClick = {
                                        showOptionsMenu = false
                                        viewModel.exportToDocx(context) { file ->
                                            file?.let { shareDocxFile(context, it) }
                                        }
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Save as PDF (SAF)...") },
                                    leadingIcon = { Icon(Icons.Default.PictureAsPdf, contentDescription = null) },
                                    onClick = {
                                        showOptionsMenu = false
                                        val safeTitle = (document?.title ?: "document").replace("[^a-zA-Z0-9.-]".toRegex(), "_")
                                        safPdfSaveLauncher.launch("$safeTitle.pdf")
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Export as Plain Text (.txt)") },
                                    leadingIcon = { Icon(Icons.Default.TextFields, contentDescription = null) },
                                    onClick = {
                                        showOptionsMenu = false
                                        viewModel.exportToPlainText(context) { file ->
                                            file?.let { shareTextFile(context, it) }
                                        }
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Print") },
                                    leadingIcon = { Icon(Icons.Default.Print, contentDescription = null) },
                                    onClick = {
                                        showOptionsMenu = false
                                        viewModel.printDocument(context)
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Page Setup...") },
                                    leadingIcon = { Icon(Icons.Default.Title, contentDescription = null) },
                                    onClick = {
                                        showOptionsMenu = false
                                        showLayoutDialog = true
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Document Statistics") },
                                    leadingIcon = { Icon(Icons.Default.Title, contentDescription = null) },
                                    onClick = {
                                        showOptionsMenu = false
                                        showStatsDialog = true
                                    }
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
                autosaveStatus = autosaveStatus,
                zoomPercent = zoomPercent,
                activeBlock = activeParagraph
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(WordDeskCanvasLight)
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
                onOpenLayoutDialog = { showLayoutDialog = true },
                onOpenStatsDialog = { showStatsDialog = true },
                onOpenLinkDialog = { showLinkDialog = true },
                onOpenHeaderFooterDialog = { showHeaderFooterDialog = true },
                onOpenPageNumberDialog = { showPageNumberDialog = true },
                onSaveAsDocx = {
                    val safeTitle = (document?.title ?: "document").replace("[^a-zA-Z0-9.-]".toRegex(), "_")
                    safDocxSaveLauncher.launch("$safeTitle.docx")
                }
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
                        isTabletOrExpanded = isTabletOrExpanded,
                        activeBlockIndex = activeBlockIndex,
                        onBlockSelected = viewModel::setActiveBlock,
                        onUpdateBlockText = { index, text, sel -> viewModel.updateParagraphText(index, text, sel) },
                        onSelectionChanged = viewModel::updateSelection,
                        onInsertParagraph = viewModel::insertParagraph,
                        onDeleteCurrentBlock = viewModel::deleteCurrentBlock,
                        onEditTableProperties = { table -> selectedTableForProperties = table },
                        onEditTableCell = { table, r, c -> selectedCellForEdit = Triple(table, r, c) },
                        onQuickAddTableRow = { id -> viewModel.addTableRow(id) },
                        onQuickDeleteTableRow = { id -> viewModel.deleteTableRow(id, -1) },
                        onQuickAddTableCol = { id -> viewModel.addTableColumn(id) },
                        onQuickDeleteTableCol = { id -> viewModel.deleteTableColumn(id, -1) },
                        onEditImageProperties = { img -> selectedImageForProperties = img }
                    )
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
                    StatRow("Estimated Pages", "${doc?.estimatedPageCount ?: 1}")
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
    onOpenLayoutDialog: () -> Unit,
    onOpenStatsDialog: () -> Unit,
    onOpenLinkDialog: () -> Unit,
    onOpenHeaderFooterDialog: () -> Unit = {},
    onOpenPageNumberDialog: () -> Unit = {},
    onSaveAsDocx: () -> Unit = {}
) {
    val activeFormatting by viewModel.activeFormatting.collectAsStateWithLifecycle()

    Surface(
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 2.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {
            // Tab Header Strip
            val tabs = RibbonTab.entries
            val selectedIndex = tabs.indexOf(activeTab)

            TabRow(
                selectedTabIndex = selectedIndex,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = WordPrimary,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedIndex]),
                        color = WordPrimary,
                        height = 3.dp
                    )
                }
            ) {
                tabs.forEach { tab ->
                    Tab(
                        selected = activeTab == tab,
                        onClick = { onTabSelected(tab) },
                        text = {
                            Text(
                                text = tab.name.lowercase().replaceFirstChar { it.uppercase() },
                                fontWeight = if (activeTab == tab) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 13.sp
                            )
                        }
                    )
                }
            }

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
                                onOpenLayoutDialog = onOpenLayoutDialog
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
                            ViewRibbonControls(viewModel)
                        }
                    }

                    RibbonTab.FILE -> {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState())
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FileRibbonControls(
                                viewModel = viewModel,
                                context = context,
                                onOpenStatsDialog = onOpenStatsDialog,
                                onSaveAsDocx = onSaveAsDocx
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
        icon = Icons.Default.Link,
        label = "Hyperlink",
        onClick = onOpenLinkDialog
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
    onOpenLayoutDialog: () -> Unit
) {
    val document by viewModel.document.collectAsStateWithLifecycle()
    val layout = document?.layoutConfig ?: PageLayoutConfig()

    Text("Paper:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    listOf(PageSize.A4, PageSize.LETTER, PageSize.LEGAL, PageSize.A5).forEach { ps ->
        FilterChip(
            selected = layout.pageSize == ps,
            onClick = { viewModel.updatePageLayout(pageSize = ps) },
            label = { Text(ps.name, fontSize = 11.sp) }
        )
    }

    Box(modifier = Modifier.height(20.dp).width(1.dp).background(Color(0xFFE2E8F0)))

    Text("Orientation:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    FilterChip(
        selected = layout.orientation == PageOrientation.PORTRAIT,
        onClick = { viewModel.updatePageLayout(orientation = PageOrientation.PORTRAIT) },
        label = { Text("Portrait", fontSize = 11.sp) }
    )
    FilterChip(
        selected = layout.orientation == PageOrientation.LANDSCAPE,
        onClick = { viewModel.updatePageLayout(orientation = PageOrientation.LANDSCAPE) },
        label = { Text("Landscape", fontSize = 11.sp) }
    )

    Box(modifier = Modifier.height(20.dp).width(1.dp).background(Color(0xFFE2E8F0)))

    Text("Margins:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    listOf(MarginPreset.NORMAL, MarginPreset.NARROW, MarginPreset.WIDE).forEach { mp ->
        FilterChip(
            selected = layout.marginPreset == mp,
            onClick = { viewModel.updatePageLayout(marginPreset = mp) },
            label = { Text(mp.displayName.split(" ")[0], fontSize = 11.sp) }
        )
    }

    Box(modifier = Modifier.height(20.dp).width(1.dp).background(Color(0xFFE2E8F0)))

    RibbonActionButton(
        icon = Icons.Default.Edit,
        label = "Page Setup...",
        onClick = onOpenLayoutDialog
    )
}

@Composable
fun ViewRibbonControls(viewModel: EditorViewModel) {
    FilterChip(
        selected = true,
        onClick = { viewModel.setViewMode(EditorViewMode.PAGINATED_A4) },
        label = { Text("A4 Page View", fontSize = 11.sp) }
    )
    FilterChip(
        selected = false,
        onClick = { viewModel.setViewMode(EditorViewMode.CONTINUOUS_MOBILE) },
        label = { Text("Mobile Continuous", fontSize = 11.sp) }
    )

    Box(modifier = Modifier.height(20.dp).width(1.dp).background(Color(0xFFE2E8F0)))

    IconButton(onClick = { viewModel.setZoomPercent(75) }, modifier = Modifier.size(32.dp)) {
        Icon(Icons.Default.ZoomOut, contentDescription = "Zoom 75%")
    }
    Text("100%", fontSize = 12.sp, fontWeight = FontWeight.Bold)
    IconButton(onClick = { viewModel.setZoomPercent(125) }, modifier = Modifier.size(32.dp)) {
        Icon(Icons.Default.ZoomIn, contentDescription = "Zoom 125%")
    }
}

@Composable
fun FileRibbonControls(
    viewModel: EditorViewModel,
    context: Context,
    onOpenStatsDialog: () -> Unit,
    onSaveAsDocx: () -> Unit = {}
) {
    RibbonActionButton(
        icon = Icons.Default.Save,
        label = "Save Now",
        onClick = { viewModel.saveNow() }
    )

    RibbonActionButton(
        icon = Icons.Default.Description,
        label = "Export DOCX",
        onClick = {
            viewModel.exportToDocx(context) { file ->
                file?.let { shareDocxFile(context, it) }
            }
        }
    )

    RibbonActionButton(
        icon = Icons.Default.Save,
        label = "Save As DOCX",
        onClick = onSaveAsDocx
    )

    RibbonActionButton(
        icon = Icons.Default.PictureAsPdf,
        label = "Export PDF",
        onClick = {
            viewModel.exportToPdf(context) { file ->
                file?.let { sharePdfFile(context, it) }
            }
        }
    )

    RibbonActionButton(
        icon = Icons.Default.TextFields,
        label = "Plain Text",
        onClick = {
            viewModel.exportToPlainText(context) { file ->
                file?.let { shareTextFile(context, it) }
            }
        }
    )

    RibbonActionButton(
        icon = Icons.Default.Print,
        label = "Print",
        onClick = { viewModel.printDocument(context) }
    )

    RibbonActionButton(
        icon = Icons.Default.Title,
        label = "Statistics",
        onClick = onOpenStatsDialog
    )
}

@Composable
fun RibbonActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp)
        ) {
            Icon(imageVector = icon, contentDescription = label, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(label, fontSize = 11.sp, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
fun DocumentCanvas(
    document: RichDocument,
    paginatedPages: List<com.example.core.layout.DocumentPage>,
    viewMode: EditorViewMode,
    zoomPercent: Int,
    isTabletOrExpanded: Boolean,
    activeBlockIndex: Int,
    onBlockSelected: (Int) -> Unit,
    onUpdateBlockText: (Int, String, TextRange) -> Unit,
    onSelectionChanged: (TextRange) -> Unit,
    onInsertParagraph: () -> Unit,
    onDeleteCurrentBlock: () -> Unit,
    onEditTableProperties: (TableBlock) -> Unit = {},
    onEditTableCell: (TableBlock, Int, Int) -> Unit = { _, _, _ -> },
    onQuickAddTableRow: (String) -> Unit = {},
    onQuickDeleteTableRow: (String) -> Unit = {},
    onQuickAddTableCol: (String) -> Unit = {},
    onQuickDeleteTableCol: (String) -> Unit = {},
    onEditImageProperties: (ImageBlock) -> Unit = {}
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(if (isTabletOrExpanded) 28.dp else 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (viewMode == EditorViewMode.PAGINATED_A4) {
            val pagesToDisplay = if (paginatedPages.isEmpty()) {
                com.example.core.layout.PaginationEngine.paginate(document)
            } else {
                paginatedPages
            }

            pagesToDisplay.forEach { page ->
                // Page indicator badge above physical sheet
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color.White.copy(alpha = 0.9f),
                    shadowElevation = 1.dp,
                    modifier = Modifier.padding(bottom = 8.dp)
                ) {
                    Text(
                        text = "Page ${page.pageNumber} of ${page.totalPages} • ${page.layout.pageSize.name} (${page.layout.orientation.name.lowercase().replaceFirstChar { it.uppercase() }})",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF64748B),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }

                val pageBgColor = try {
                    Color(android.graphics.Color.parseColor(page.layout.pageBackgroundHex))
                } catch (e: Exception) { Color.White }

                val borderStroke = when (page.layout.borderStyle) {
                    com.example.core.layout.PageBorderStyle.SINGLE_THIN -> androidx.compose.foundation.BorderStroke(1.dp, Color(try { android.graphics.Color.parseColor(page.layout.borderColorHex) } catch (e: Exception) { android.graphics.Color.LTGRAY }))
                    com.example.core.layout.PageBorderStyle.SINGLE_THICK -> androidx.compose.foundation.BorderStroke(2.dp, Color(try { android.graphics.Color.parseColor(page.layout.borderColorHex) } catch (e: Exception) { android.graphics.Color.LTGRAY }))
                    com.example.core.layout.PageBorderStyle.DOUBLE -> androidx.compose.foundation.BorderStroke(3.dp, Color(try { android.graphics.Color.parseColor(page.layout.borderColorHex) } catch (e: Exception) { android.graphics.Color.LTGRAY }))
                    com.example.core.layout.PageBorderStyle.DASHED -> androidx.compose.foundation.BorderStroke(1.5.dp, Color(try { android.graphics.Color.parseColor(page.layout.borderColorHex) } catch (e: Exception) { android.graphics.Color.LTGRAY }))
                    com.example.core.layout.PageBorderStyle.NONE -> null
                }

                val maxSheetWidth = if (page.layout.orientation == PageOrientation.LANDSCAPE) {
                    if (isTabletOrExpanded) 920.dp else 720.dp
                } else {
                    if (isTabletOrExpanded) 760.dp else 640.dp
                }

                // Margins mapped to responsive dp
                val leftMarginDp = (page.layout.leftMarginMm * 1.25f).dp.coerceIn(16.dp, 48.dp)
                val rightMarginDp = (page.layout.rightMarginMm * 1.25f).dp.coerceIn(16.dp, 48.dp)
                val topMarginDp = (page.layout.topMarginMm * 1.2f).dp.coerceIn(20.dp, 44.dp)
                val bottomMarginDp = (page.layout.bottomMarginMm * 1.2f).dp.coerceIn(20.dp, 44.dp)

                // Physical Paper Sheet Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = maxSheetWidth)
                        .shadow(6.dp, RoundedCornerShape(2.dp)),
                    shape = RoundedCornerShape(2.dp),
                    colors = CardDefaults.cardColors(containerColor = pageBgColor),
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
                        if (page.hasHeader) {
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
                                    color = Color.Gray
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            HorizontalDivider(color = Color(0xFFE5E7EB), thickness = 0.5.dp)
                            Spacer(modifier = Modifier.height(16.dp))
                        }

                        // Content items on this page
                        var listNumberCounter = 1
                        page.items.forEach { pageItem ->
                            when (pageItem) {
                                is com.example.core.layout.ParagraphPageItem -> {
                                    val block = pageItem.originalBlock
                                    val itemNumber = if (block.listType == ListType.NUMBERED) {
                                        listNumberCounter++
                                    } else {
                                        listNumberCounter = 1
                                        null
                                    }

                                    BlockItemView(
                                        block = block,
                                        itemNumber = itemNumber,
                                        isActive = pageItem.originalBlockIndex == activeBlockIndex,
                                        onFocus = { onBlockSelected(pageItem.originalBlockIndex) },
                                        onSelectionChanged = onSelectionChanged,
                                        onTextChanged = { newText, sel -> onUpdateBlockText(pageItem.originalBlockIndex, newText, sel) },
                                        onEnterPressed = onInsertParagraph
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                }

                                is com.example.core.layout.TablePageItem -> {
                                    TableBlockView(
                                        table = pageItem.originalBlock,
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
                                        onFocus = { onBlockSelected(pageItem.originalBlockIndex) },
                                        onEditProperties = { onEditImageProperties(pageItem.originalBlock) }
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                }

                                is com.example.core.layout.DividerPageItem -> {
                                    HorizontalDivider(
                                        color = Color(0xFFCBD5E1),
                                        thickness = 1.dp,
                                        modifier = Modifier.padding(vertical = 8.dp)
                                    )
                                }

                                is com.example.core.layout.PageBreakPageItem -> {
                                    // Page break boundary is naturally reflected by the next paper sheet
                                }
                            }
                        }

                        // Footer Area with Automatic Page Numbering
                        if (page.hasFooter) {
                            Spacer(modifier = Modifier.height(20.dp))
                            HorizontalDivider(color = Color(0xFFE5E7EB), thickness = 0.5.dp)
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
                                    color = Color.Gray
                                )
                            }
                        }
                    }
                }

                // Desk gap between physical paper sheets
                Spacer(modifier = Modifier.height(24.dp))
            }
        } else {
            // Continuous Mobile View
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(1.dp, RoundedCornerShape(12.dp)),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    var listNumberCounter = 1
                    document.blocks.forEachIndexed { index, block ->
                        val itemNumber = if (block is ParagraphBlock && block.listType == ListType.NUMBERED) {
                            listNumberCounter++
                        } else {
                            listNumberCounter = 1
                            null
                        }

                        BlockItemView(
                            block = block,
                            itemNumber = itemNumber,
                            isActive = index == activeBlockIndex,
                            onFocus = { onBlockSelected(index) },
                            onSelectionChanged = onSelectionChanged,
                            onTextChanged = { newText, sel -> onUpdateBlockText(index, newText, sel) },
                            onEnterPressed = onInsertParagraph,
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

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = tableAlignment
    ) {
        // Table Header Quick Action Toolbar
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

        // Table Body Grid
        Column(
            modifier = Modifier
                .fillMaxWidth(widthFraction)
                .border(
                    width = if (table.hasBorders) 1.dp else 0.5.dp,
                    color = if (table.hasBorders) tableBorderColor else Color(0xFFE2E8F0),
                    shape = RoundedCornerShape(bottomStart = 8.dp, bottomEnd = 8.dp)
                )
        ) {
            for (r in 0 until table.rowCount) {
                Row(modifier = Modifier.fillMaxWidth()) {
                    for (c in 0 until table.columnCount) {
                        val cell = table.cells.getOrNull(r)?.getOrNull(c) ?: TableCell()

                        val cellBg = when {
                            cell.backgroundColorHex != null -> try {
                                Color(android.graphics.Color.parseColor(cell.backgroundColorHex))
                            } catch (e: Exception) { Color.White }
                            r == 0 -> Color(0xFFF1F5F9)
                            else -> Color.White
                        }

                        val cellTextAlign = when (cell.alignment) {
                            TextAlignment.CENTER -> TextAlign.Center
                            TextAlignment.END -> TextAlign.End
                            TextAlignment.JUSTIFY -> TextAlign.Justify
                            TextAlignment.START -> TextAlign.Start
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
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
                                fontSize = 12.sp,
                                textAlign = cellTextAlign,
                                color = if (cell.text.isBlank()) Color.LightGray else Color(0xFF1E293B),
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
    onFocus: () -> Unit = {},
    onEditProperties: () -> Unit = {}
) {
    val context = LocalContext.current
    val imageAlignment = when (imageBlock.alignment) {
        TextAlignment.START -> Alignment.Start
        TextAlignment.END -> Alignment.End
        else -> Alignment.CenterHorizontally
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onFocus)
            .padding(vertical = 4.dp),
        horizontalAlignment = imageAlignment
    ) {
        Box(
            modifier = Modifier
                .width(imageBlock.widthDp.dp)
                .height(imageBlock.heightDp.dp)
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
                modifier = Modifier.width(imageBlock.widthDp.dp)
            )
        }
    }
}

@Composable
fun BlockItemView(
    block: BlockElement,
    itemNumber: Int?,
    isActive: Boolean,
    onFocus: () -> Unit,
    onSelectionChanged: (TextRange) -> Unit,
    onTextChanged: (String, TextRange) -> Unit,
    onEnterPressed: () -> Unit,
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

            val baseFontSize = when (block.headingStyle) {
                HeadingStyle.TITLE -> 26.sp
                HeadingStyle.SUBTITLE -> 18.sp
                HeadingStyle.HEADING_1 -> 22.sp
                HeadingStyle.HEADING_2 -> 18.sp
                HeadingStyle.HEADING_3 -> 16.sp
                HeadingStyle.QUOTE -> 15.sp
                HeadingStyle.NORMAL -> 15.sp
            }

            val defaultTextColor = when {
                block.headingStyle == HeadingStyle.TITLE || block.headingStyle == HeadingStyle.HEADING_1 -> WordPrimary
                else -> Color(0xFF1E293B)
            }

            // Stateful TextFieldValue to accurately capture selection, cursor, and text deltas
            var textFieldValue by remember(block.id) {
                mutableStateOf(TextFieldValue(text = block.text, selection = TextRange(block.text.length)))
            }

            // Sync with external updates (such as undo, redo, find & replace) without resetting cursor on span edits
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

            val indentPadding = (block.indentationLevel * 20).dp

            androidx.compose.runtime.CompositionLocalProvider(
                androidx.compose.ui.platform.LocalLayoutDirection provides (if (block.isRtl) androidx.compose.ui.unit.LayoutDirection.Rtl else androidx.compose.ui.unit.LayoutDirection.Ltr)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = indentPadding)
                        .clickable(onClick = onFocus)
                        .background(
                            if (isActive) WordPrimary.copy(alpha = 0.04f) else Color.Transparent,
                            RoundedCornerShape(4.dp)
                        )
                        .padding(horizontal = 4.dp, vertical = 2.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    // List prefix
                    when (block.listType) {
                        ListType.BULLET -> {
                            Text(
                                text = "• ",
                                fontWeight = FontWeight.Bold,
                                fontSize = baseFontSize,
                                color = WordPrimary,
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
                                color = WordPrimary,
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
                            cursorBrush = SolidColor(WordPrimary),
                            modifier = Modifier
                                .fillMaxWidth()
                                .onFocusChanged { focusState ->
                                    if (focusState.isFocused) {
                                        onFocus()
                                    }
                                }
                                .testTag("paragraph_input_${block.id}")
                        )
                    }
                }
            }
        }

        is TableBlock -> {
            TableBlockView(
                table = block,
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
                onFocus = onFocus,
                onEditProperties = onEditImageProperties
            )
        }

        is DividerBlock -> {
            HorizontalDivider(
                color = Color(0xFFCBD5E1),
                thickness = 1.dp,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }

        is PageBreakBlock -> {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
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
            }
        }
    }
}

@Composable
fun EditorStatusBar(
    document: RichDocument?,
    autosaveStatus: AutosaveStatus,
    zoomPercent: Int,
    activeBlock: ParagraphBlock?
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 4.dp,
        modifier = Modifier.fillMaxWidth()
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
                Text(
                    text = "Page 1 of ${document?.estimatedPageCount ?: 1}",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text("•", fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
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
