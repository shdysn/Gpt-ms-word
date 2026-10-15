package com.example.ui.editor

import androidx.compose.ui.text.TextRange
import androidx.lifecycle.viewModelScope
import com.example.core.error.DocumentResult
import com.example.core.layout.DocumentPage
import com.example.core.layout.PageLayoutConfig
import com.example.core.layout.PaginationEngine
import com.example.data.local.PreferencesManager
import com.example.data.model.DocumentRevisionSnapshot
import com.example.data.model.DrawingBlock
import com.example.data.model.ParagraphBlock
import com.example.data.model.RichDocument
import com.example.data.model.TextAlignment
import com.example.data.repository.DocumentRepository
import com.example.editor.engine.ActiveFormatting
import com.example.editor.engine.AutosaveStatus
import com.example.editor.engine.SpanOperations
import com.example.editor.engine.UpdateLayoutCommand
import com.example.ui.editor.viewmodel.EditorBlockUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * Clean coordinator ViewModel for Document Editing (< 220 lines).
 * Subclasses [EditorCoordinator] to delegate domain operations to specialized managers.
 */
class EditorViewModel(
    docId: String,
    repository: DocumentRepository,
    preferencesManager: PreferencesManager
) : EditorCoordinator(docId, repository, preferencesManager) {

    override val selfViewModel: EditorViewModel get() = this

    private val _document = MutableStateFlow<RichDocument?>(null)
    val document: StateFlow<RichDocument?> = _document.asStateFlow()
    val currentDocument: RichDocument? get() = _document.value

    private val _activeRibbonTab = MutableStateFlow(RibbonTab.HOME)
    val activeRibbonTab: StateFlow<RibbonTab> = _activeRibbonTab.asStateFlow()

    private val _isRibbonVisible = MutableStateFlow(false)
    val isRibbonVisible: StateFlow<Boolean> = _isRibbonVisible.asStateFlow()

    private val _viewMode = MutableStateFlow(EditorViewMode.PAGINATED_A4)
    val viewMode: StateFlow<EditorViewMode> = _viewMode.asStateFlow()

    val paginatedPages: StateFlow<List<DocumentPage>> = _document.map { doc ->
        if (doc == null) emptyList() else PaginationEngine.paginate(doc)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _zoomPercent = MutableStateFlow(100)
    val zoomPercent: StateFlow<Int> = _zoomPercent.asStateFlow()

    val canUndo: StateFlow<Boolean> get() = historyManager.canUndo
    val canRedo: StateFlow<Boolean> get() = historyManager.canRedo

    private val _activeBlockIndex = MutableStateFlow(0)
    val activeBlockIndex: StateFlow<Int> = _activeBlockIndex.asStateFlow()

    private val _statusMessage = MutableStateFlow<String?>(null)
    val statusMessage: StateFlow<String?> = _statusMessage.asStateFlow()

    private val _drawTool = MutableStateFlow("PEN")
    val drawTool: StateFlow<String> = _drawTool.asStateFlow()

    private val _drawColorHex = MutableStateFlow("#0F172A")
    val drawColorHex: StateFlow<String> = _drawColorHex.asStateFlow()

    private val _drawStrokeWidthPt = MutableStateFlow(2.5f)
    val drawStrokeWidthPt: StateFlow<Float> = _drawStrokeWidthPt.asStateFlow()

    val isFindReplaceVisible: StateFlow<Boolean> get() = reviewManager.isFindReplaceVisible
    val findQuery: StateFlow<String> get() = reviewManager.findQuery
    val replaceQuery: StateFlow<String> get() = reviewManager.replaceQuery
    val matchCount: StateFlow<Int> get() = reviewManager.matchCount

    private val _selection = MutableStateFlow(TextRange.Zero)
    val selection: StateFlow<TextRange> = _selection.asStateFlow()

    private val _pendingFormatting = MutableStateFlow(ActiveFormatting())
    val pendingFormatting: StateFlow<ActiveFormatting> = _pendingFormatting.asStateFlow()

    val activeFormatting: StateFlow<ActiveFormatting> = combine(
        _document, _activeBlockIndex, _selection, _pendingFormatting
    ) { doc, index, sel, pending ->
        val block = doc?.blocks?.getOrNull(index) as? ParagraphBlock
        if (block == null) pending
        else if (!sel.collapsed && sel.min < sel.max) SpanOperations.queryFormatting(block.spans, sel.min, sel.max)
        else {
            val cursor = SpanOperations.queryFormatting(block.spans, sel.start, sel.end)
            if (pending != ActiveFormatting()) pending else cursor
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ActiveFormatting())

    val isFormatPainterActive: StateFlow<Boolean> get() = formattingManager.isFormatPainterActive
    val autosaveStatus: StateFlow<AutosaveStatus> get() = historyManager.autosaveController.status
    val recoveryAvailable: StateFlow<Boolean> get() = historyManager.recoveryAvailable

    val isAutosaveEnabled: StateFlow<Boolean> = preferencesManager.isAutosaveEnabled
        .stateIn(viewModelScope, SharingStarted.Eagerly, true)
    val defaultRtl: StateFlow<Boolean> = preferencesManager.defaultRtl
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)
    val showWordCount: StateFlow<Boolean> = preferencesManager.showWordCount
        .stateIn(viewModelScope, SharingStarted.Eagerly, true)
    val showRuler: StateFlow<Boolean> = preferencesManager.showRuler
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    val isDirty: StateFlow<Boolean> get() = historyManager.autosaveController.status.map {
        it == AutosaveStatus.DIRTY || it == AutosaveStatus.ERROR
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val isSpeaking: StateFlow<Boolean> get() = reviewManager.isSpeaking
    val speakingParagraphIndex: StateFlow<Int?> get() = reviewManager.speakingParagraphIndex
    val speechRate: StateFlow<Float> get() = reviewManager.speechRate

    private val _canvasPaperMode = MutableStateFlow("WHITE")
    val canvasPaperMode: StateFlow<String> = _canvasPaperMode.asStateFlow()

    val revisionSnapshots: StateFlow<List<DocumentRevisionSnapshot>> get() = historyManager.revisionSnapshots
    val isDocumentLocked: StateFlow<Boolean> get() = reviewManager.isDocumentLocked

    init {
        loadDocument()
        historyManager.observeSnapshots()
    }

    private fun loadDocument() {
        viewModelScope.launch {
            if (repository.hasRecoveryDraft(docId)) historyManager.setRecoveryAvailable(true)
            when (val res = repository.getDocument(docId)) {
                is DocumentResult.Success -> {
                    val loaded = res.data
                    if (loaded.isPasswordProtected) reviewManager.setDocumentLocked(true)
                    val normalized = EditorBlockUtils.normalizeParagraphBlocks(loaded.blocks)
                    val first = normalized.firstOrNull() as? ParagraphBlock
                    val adjusted = if (defaultRtl.value && first != null && first.text.isEmpty() && !first.isRtl) {
                        loaded.copy(blocks = listOf(first.copy(isRtl = true, alignment = TextAlignment.END)))
                    } else loaded.copy(blocks = normalized)
                    _document.value = adjusted
                    historyManager.undoRedoManager.clear()
                    historyManager.updateUndoRedoStates()
                    historyManager.autosaveController.markSaved()
                }
                is DocumentResult.Failure -> { _statusMessage.value = "Failed to load document: ${res.error.message}" }
            }
        }
    }

    fun updateDocumentInternal(newDoc: RichDocument) { _document.value = newDoc }
    fun showStatusMessage(msg: String?) { _statusMessage.value = msg }
    fun updatePendingFormatting(mutator: (ActiveFormatting) -> ActiveFormatting) { _pendingFormatting.value = mutator(_pendingFormatting.value) }

    fun selectRibbonTab(tab: RibbonTab) { _activeRibbonTab.value = tab }
    fun toggleRibbonVisibility() { _isRibbonVisible.value = !_isRibbonVisible.value }
    fun setViewMode(mode: EditorViewMode) { _viewMode.value = mode }
    fun toggleViewMode() { _viewMode.value = if (_viewMode.value == EditorViewMode.PAGINATED_A4) EditorViewMode.CONTINUOUS_MOBILE else EditorViewMode.PAGINATED_A4 }
    fun setZoomPercent(percent: Int) { _zoomPercent.value = percent.coerceIn(25, 250) }
    fun zoomIn() = setZoomPercent(_zoomPercent.value + 10)
    fun zoomOut() = setZoomPercent(_zoomPercent.value - 10)
    fun zoom100() = setZoomPercent(100)
    fun setActiveBlock(index: Int) {
        if (formattingManager.isFormatPainterActive.value && index != _activeBlockIndex.value) formattingManager.applyFormatPainterIfActive(index)
        _activeBlockIndex.value = index
    }
    fun clearStatusMessage() { _statusMessage.value = null }
    fun setDrawTool(tool: String) {
        _drawTool.value = tool
        val hasDrawing = currentDocument?.blocks?.any { it is DrawingBlock } == true
        if (!hasDrawing && (tool == "PEN" || tool == "HIGHLIGHTER")) {
            insertDrawing()
        }
    }
    fun setDrawColorHex(hex: String) { _drawColorHex.value = hex }
    fun setDrawStrokeWidthPt(width: Float) { _drawStrokeWidthPt.value = width }
    fun setCanvasPaperMode(mode: String) {
        _canvasPaperMode.value = mode
        val hex = when (mode) { "SEPIA" -> "#FBF0D9"; "DARK" -> "#18181B"; else -> "#FFFFFF" }
        val cur = _document.value?.layoutConfig ?: PageLayoutConfig()
        executeCommand(UpdateLayoutCommand(cur, cur.copy(pageBackgroundHex = hex)))
    }

    fun updateTitle(newTitle: String) {
        val cur = _document.value ?: return
        _document.value = cur.copy(title = newTitle)
        historyManager.scheduleDocumentAutosave()
    }
    fun updateSelection(newSelection: TextRange) { _selection.value = newSelection }
    fun updateSelectionForBlock(blockIndex: Int, newSelection: TextRange) {
        _activeBlockIndex.value = blockIndex
        _selection.value = newSelection
    }

    fun toggleRuler() { viewModelScope.launch { preferencesManager.setShowRuler(!showRuler.value) } }
    fun toggleWordCount() { viewModelScope.launch { preferencesManager.setShowWordCount(!showWordCount.value) } }

    override fun onCleared() {
        super.onCleared()
        reviewManager.cleanupTts()
    }

    class Factory(
        private val documentId: String,
        private val repository: DocumentRepository,
        private val preferencesManager: PreferencesManager
    ) : androidx.lifecycle.ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
            return EditorViewModel(documentId, repository, preferencesManager) as T
        }
    }
}
