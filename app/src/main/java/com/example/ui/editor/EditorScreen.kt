package com.example.ui.editor

import android.app.Activity
import android.content.Intent
import android.speech.RecognizerIntent
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
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FormatBold
import androidx.compose.material.icons.filled.FormatColorText
import androidx.compose.material.icons.filled.FormatItalic
import androidx.compose.material.icons.filled.FormatStrikethrough
import androidx.compose.material.icons.filled.FormatUnderlined
import androidx.compose.material.icons.filled.Highlight
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.ImageBlock
import com.example.data.model.ParagraphBlock
import com.example.data.model.TableBlock
import com.example.ui.editor.components.DocumentCanvas
import com.example.ui.editor.components.EditorDialogsHost
import com.example.ui.editor.components.EditorRecoveryBanner
import com.example.ui.editor.components.EditorStatusBar
import com.example.ui.editor.components.EditorTopAppBar
import com.example.ui.editor.components.FindReplaceBar
import com.example.ui.editor.components.WordRibbon
import com.example.ui.theme.WordDeskCanvasDark
import com.example.ui.theme.WordDeskCanvasLight
import com.example.ui.theme.WordPrimary
import kotlinx.coroutines.launch

/**
 * Main coordinator screen for document editing.
 * Composes modular editor blocks: Top App Bar, Ribbon, Canvas, Status Bar,
 * Floating Quick-Format Bar, and Dialog Host.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditorScreen(
    viewModel: EditorViewModel,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    val document by viewModel.document.collectAsStateWithLifecycle()
    val isDocumentLocked by viewModel.isDocumentLocked.collectAsStateWithLifecycle()
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
    var showSignatureDialog by remember { mutableStateOf(false) }
    var showShapeDialog by remember { mutableStateOf(false) }
    var showWatermarkDialog by remember { mutableStateOf(false) }
    var showPasswordDialog by remember { mutableStateOf(false) }
    var showVersionHistoryDialog by remember { mutableStateOf(false) }
    var showCommentsDialog by remember { mutableStateOf(false) }
    var showTrackChangesDialog by remember { mutableStateOf(false) }
    var showSmartWritingDialog by remember { mutableStateOf(false) }
    var showFootnoteDialog by remember { mutableStateOf(false) }
    var showPageBordersDialog by remember { mutableStateOf(false) }
    var showPageColorDialog by remember { mutableStateOf(false) }
    var selectedTableForProperties by remember { mutableStateOf<TableBlock?>(null) }
    var selectedCellForEdit by remember { mutableStateOf<Triple<TableBlock, Int, Int>?>(null) }
    var selectedImageForProperties by remember { mutableStateOf<ImageBlock?>(null) }

    // Voice Typing Dictation Launcher
    val voiceTypingLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val spokenMatches = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            val spokenText = spokenMatches?.firstOrNull()
            if (!spokenText.isNullOrBlank()) {
                viewModel.appendVoiceTypingText(spokenText)
            }
        }
    }

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
                EditorTopAppBar(
                    title = document?.title ?: "Document",
                    autosaveStatus = autosaveStatus,
                    canUndo = canUndo,
                    canRedo = canRedo,
                    viewMode = viewMode,
                    isFindReplaceVisible = isFindReplaceVisible,
                    onBackClick = handleBackNavigation,
                    onTitleClick = { showTitleEditDialog = true },
                    onUndoClick = { viewModel.undo() },
                    onRedoClick = { viewModel.redo() },
                    onToggleViewMode = { viewModel.toggleViewMode() },
                    onToggleFindReplace = { viewModel.toggleFindReplace() },
                    onSaveClick = { showSaveOptionsDialog = true },
                    onExportClick = { showExportOptionsDialog = true },
                    onPageSetupClick = { showLayoutDialog = true },
                    onPrintClick = { viewModel.printDocument(context) },
                    onStatsClick = { showStatsDialog = true }
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
                onOpenLayoutDialog = { showLayoutDialog = true },
                onOpenSignatureDialog = { showSignatureDialog = true },
                onOpenShapeDialog = { showShapeDialog = true },
                onOpenWatermarkDialog = { showWatermarkDialog = true },
                onOpenPasswordDialog = { showPasswordDialog = true },
                onOpenVersionHistoryDialog = { showVersionHistoryDialog = true },
                onOpenStatsDialog = { showStatsDialog = true },
                onOpenFootnoteDialog = { showFootnoteDialog = true },
                onOpenPageBordersDialog = { showPageBordersDialog = true },
                onOpenPageColorDialog = { showPageColorDialog = true },
                onOpenCommentsDialog = { showCommentsDialog = true },
                onOpenTrackChangesDialog = { showTrackChangesDialog = true },
                onOpenSmartWritingDialog = { showSmartWritingDialog = true },
                onStartVoiceTyping = {
                    try {
                        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                            putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak to dictate text into document...")
                        }
                        val activities = context.packageManager.queryIntentActivities(intent, 0)
                        if (activities.isNotEmpty()) {
                            voiceTypingLauncher.launch(intent)
                        } else {
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar("Voice typing requires Google Speech Services on your device")
                            }
                        }
                    } catch (_: Exception) {
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar("Speech recognition service not available on this device")
                        }
                    }
                }
            )

            // Crash/Restart Recovery Notification Banner
            if (recoveryAvailable) {
                EditorRecoveryBanner(
                    onRestore = { viewModel.restoreRecoveryDraft() },
                    onDismiss = { viewModel.dismissRecoveryDraft() }
                )
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
                        onOpenPageLayoutDialog = { showLayoutDialog = true },
                        onRefreshTOC = viewModel::refreshTableOfContents
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

    // Decoupled Editor Dialogs and Modals Host
    EditorDialogsHost(
        viewModel = viewModel,
        document = document,
        context = context,
        actualPageCount = actualPageCount,
        activeParagraph = activeParagraph,
        showTitleEditDialog = showTitleEditDialog,
        onDismissTitleEdit = { showTitleEditDialog = false },
        showSaveOptionsDialog = showSaveOptionsDialog,
        onDismissSaveOptions = { showSaveOptionsDialog = false },
        onOpenSaveAs = { showSaveAsDialog = true },
        showExportOptionsDialog = showExportOptionsDialog,
        onDismissExportOptions = { showExportOptionsDialog = false },
        onOpenExportPdf = { showExportPdfDialog = true },
        showSaveAsDialog = showSaveAsDialog,
        onDismissSaveAs = { showSaveAsDialog = false },
        showDiscardDialog = showDiscardDialog,
        onDismissDiscard = { showDiscardDialog = false },
        onNavigateBack = onNavigateBack,
        showTableDialog = showTableDialog,
        onDismissTableDialog = { showTableDialog = false },
        selectedTableForProperties = selectedTableForProperties,
        onDismissTableProperties = { selectedTableForProperties = null },
        selectedCellForEdit = selectedCellForEdit,
        onDismissCellEdit = { selectedCellForEdit = null },
        selectedImageForProperties = selectedImageForProperties,
        onDismissImageProperties = { selectedImageForProperties = null },
        showHeaderFooterDialog = showHeaderFooterDialog,
        onDismissHeaderFooter = { showHeaderFooterDialog = false },
        showPageNumberDialog = showPageNumberDialog,
        onDismissPageNumber = { showPageNumberDialog = false },
        showLinkDialog = showLinkDialog,
        onDismissLinkDialog = { showLinkDialog = false },
        showLayoutDialog = showLayoutDialog,
        onDismissLayoutDialog = { showLayoutDialog = false },
        showExportPdfDialog = showExportPdfDialog,
        onDismissExportPdf = { showExportPdfDialog = false },
        onOpenPageSetupFromPdf = { showLayoutDialog = true },
        showStatsDialog = showStatsDialog,
        onDismissStatsDialog = { showStatsDialog = false },
        isDocumentLocked = isDocumentLocked,
        showSignatureDialog = showSignatureDialog,
        onDismissSignatureDialog = { showSignatureDialog = false },
        showShapeDialog = showShapeDialog,
        onDismissShapeDialog = { showShapeDialog = false },
        showWatermarkDialog = showWatermarkDialog,
        onDismissWatermarkDialog = { showWatermarkDialog = false },
        showPasswordDialog = showPasswordDialog,
        onDismissPasswordDialog = { showPasswordDialog = false },
        showVersionHistoryDialog = showVersionHistoryDialog,
        onDismissVersionHistoryDialog = { showVersionHistoryDialog = false },
        showCommentsDialog = showCommentsDialog,
        onDismissCommentsDialog = { showCommentsDialog = false },
        showTrackChangesDialog = showTrackChangesDialog,
        onDismissTrackChangesDialog = { showTrackChangesDialog = false },
        showSmartWritingDialog = showSmartWritingDialog,
        onDismissSmartWritingDialog = { showSmartWritingDialog = false },
        showFootnoteDialog = showFootnoteDialog,
        onDismissFootnoteDialog = { showFootnoteDialog = false },
        showPageBordersDialog = showPageBordersDialog,
        onDismissPageBordersDialog = { showPageBordersDialog = false },
        showPageColorDialog = showPageColorDialog,
        onDismissPageColorDialog = { showPageColorDialog = false },
        safDocxSaveLauncher = safDocxSaveLauncher,
        safPdfSaveLauncher = safPdfSaveLauncher
    )
}
