package com.example.ui.editor

import android.app.Activity
import android.speech.RecognizerIntent
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.ime
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.ParagraphBlock
import com.example.ui.editor.components.DocumentOutlineSheet
import com.example.ui.editor.components.EditorCanvasContent
import com.example.ui.editor.components.EditorQuickActionsArea
import com.example.ui.editor.components.EditorRecoveryBanner
import com.example.ui.editor.components.EditorStatusBar
import com.example.ui.editor.components.EditorTopAppBar
import com.example.ui.editor.components.FindReplaceBar
import com.example.ui.theme.WordDeskCanvasDark
import com.example.ui.theme.WordDeskCanvasLight

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
    val dState = rememberEditorDialogsState()

    val document by viewModel.document.collectAsStateWithLifecycle()
    val activeRibbonTab by viewModel.activeRibbonTab.collectAsStateWithLifecycle()
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
    val isDocumentAllSelected by viewModel.isDocumentAllSelected.collectAsStateWithLifecycle()
    val selection by viewModel.selection.collectAsStateWithLifecycle()
    val activeFormatting by viewModel.activeFormatting.collectAsStateWithLifecycle()
    val density = LocalDensity.current
    val isImeVisible = WindowInsets.ime.getBottom(density) > 0
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    var isBottomRibbonExpanded by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(isImeVisible) {
        if (isImeVisible && isBottomRibbonExpanded) {
            isBottomRibbonExpanded = false
        }
    }

    val actualPageCount = remember(paginatedPages, document) {
        if (paginatedPages.isNotEmpty()) paginatedPages.size else (document?.estimatedPageCount ?: 1)
    }
    val currentPageNumber = remember(paginatedPages, activeBlockIndex) {
        paginatedPages.firstOrNull { page ->
            page.items.any { it.originalBlockIndex == activeBlockIndex }
        }?.pageNumber ?: 1
    }

    val coroutineScope = rememberCoroutineScope()

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

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_PAUSE || event == Lifecycle.Event.ON_STOP) {
                viewModel.onAppBackgrounded()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        uri?.let { viewModel.insertImageFromUri(context, it, "Inserted Picture") }
    }

    val safPdfSaveLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/pdf")
    ) { uri ->
        uri?.let { viewModel.exportPdfToSafUri(context, it) }
    }

    val safDocxSaveLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/vnd.openxmlformats-officedocument.wordprocessingml.document")
    ) { uri ->
        uri?.let { viewModel.exportDocxToSafUri(context, it) }
    }

    val handleBackNavigation = {
        if (isBottomRibbonExpanded) {
            isBottomRibbonExpanded = false
        } else if (dState.showOutlineSheet) {
            dState.showOutlineSheet = false
        } else if (isDirty) {
            dState.showDiscardDialog = true
        } else {
            onNavigateBack()
        }
    }

    BackHandler { handleBackNavigation() }

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
                    title = document?.title ?: "Document", autosaveStatus = autosaveStatus,
                    canUndo = canUndo, canRedo = canRedo, viewMode = viewMode,
                    isFindReplaceVisible = isFindReplaceVisible, isOutlineVisible = dState.showOutlineSheet,
                    onBackClick = handleBackNavigation, onUndoClick = viewModel::undo, onRedoClick = viewModel::redo,
                    onToggleViewMode = viewModel::toggleViewMode, onToggleFindReplace = viewModel::toggleFindReplace,
                    onToggleOutline = { dState.showOutlineSheet = !dState.showOutlineSheet },
                    isRibbonVisible = isBottomRibbonExpanded, activeRibbonTab = activeRibbonTab,
                    onTabSelected = viewModel::selectRibbonTab,
                    onToggleRibbon = {
                        isBottomRibbonExpanded = !isBottomRibbonExpanded
                        if (isBottomRibbonExpanded) {
                            keyboardController?.hide()
                            focusManager.clearFocus()
                        }
                    },
                    onSaveClick = { dState.showSaveOptionsDialog = true }, onExportClick = { dState.showExportOptionsDialog = true },
                    onPageSetupClick = { dState.showLayoutDialog = true }, onPrintClick = { viewModel.printDocument(context) },
                    onStatsClick = { dState.showStatsDialog = true }, isDocumentAllSelected = isDocumentAllSelected,
                    onSelectAllClick = viewModel::selectAllDocument
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
            if (!isImeVisible && !isBottomRibbonExpanded) {
                EditorStatusBar(
                    document = document, currentPageNumber = currentPageNumber, totalPages = actualPageCount,
                    showWordCount = showWordCount, autosaveStatus = autosaveStatus, zoomPercent = zoomPercent,
                    activeBlock = activeParagraph, onOpenLayoutDialog = { dState.showLayoutDialog = true }
                )
            }
        }
    ) { innerPadding ->
        val deskCanvasColor = if (isSystemInDarkTheme()) WordDeskCanvasDark else WordDeskCanvasLight
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = innerPadding.calculateTopPadding(), bottom = if (isImeVisible) 0.dp else innerPadding.calculateBottomPadding())
                .background(deskCanvasColor)
                .imePadding()
        ) {
            if (recoveryAvailable) {
                EditorRecoveryBanner(
                    onRestore = { viewModel.restoreRecoveryDraft() },
                    onDismiss = { viewModel.dismissRecoveryDraft() }
                )
            }

            EditorCanvasContent(
                document = document, viewModel = viewModel, paginatedPages = paginatedPages,
                viewMode = viewMode, zoomPercent = zoomPercent, showRuler = showRuler,
                activeBlockIndex = activeBlockIndex, dState = dState, modifier = Modifier.weight(1f)
            )

            EditorQuickActionsArea(
                viewModel = viewModel,
                dState = dState,
                context = context,
                selection = selection,
                isDocumentAllSelected = isDocumentAllSelected,
                isImeVisible = isImeVisible,
                isBottomRibbonExpanded = isBottomRibbonExpanded,
                activeRibbonTab = activeRibbonTab,
                activeFormatting = activeFormatting,
                activeParagraph = activeParagraph,
                onToggleExpandRibbon = {
                    if (!isBottomRibbonExpanded) {
                        keyboardController?.hide()
                        focusManager.clearFocus()
                        isBottomRibbonExpanded = true
                    } else {
                        isBottomRibbonExpanded = false
                    }
                },
                onDismissRibbon = { isBottomRibbonExpanded = false },
                photoPickerLauncher = photoPickerLauncher,
                safDocxSaveLauncher = safDocxSaveLauncher
            )
        }
    }

    EditorDialogsWiring(
        viewModel = viewModel, document = document, context = context,
        actualPageCount = actualPageCount, activeParagraph = activeParagraph, dState = dState,
        onNavigateBack = onNavigateBack, safDocxSaveLauncher = safDocxSaveLauncher, safPdfSaveLauncher = safPdfSaveLauncher
    )

    if (dState.showOutlineSheet && document != null) {
        DocumentOutlineSheet(
            document = document!!,
            paginatedPages = paginatedPages,
            activeBlockIndex = activeBlockIndex,
            onDismiss = { dState.showOutlineSheet = false },
            onNavigateToHeading = { blockIdx, _ ->
                viewModel.setActiveBlock(blockIdx)
                dState.showOutlineSheet = false
            }
        )
    }
}
