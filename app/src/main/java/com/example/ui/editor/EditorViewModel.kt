package com.example.ui.editor

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.core.error.DocumentResult
import com.example.core.layout.MarginPreset
import com.example.core.layout.PageOrientation
import com.example.core.layout.PageSize
import com.example.data.local.PreferencesManager
import com.example.data.model.BlockElement
import com.example.data.model.DividerBlock
import com.example.data.model.HeadingStyle
import com.example.data.model.InlineSpan
import com.example.data.model.PageBreakBlock
import com.example.data.model.ParagraphBlock
import com.example.data.model.RichDocument
import com.example.data.model.TableBlock
import com.example.data.model.TextAlignment
import com.example.data.repository.DocumentRepository
import com.example.editor.engine.ApplyInlineSpanCommand
import com.example.editor.engine.AutosaveController
import com.example.editor.engine.AutosaveStatus
import com.example.editor.engine.ChangeAlignmentCommand
import com.example.editor.engine.ChangeHeadingStyleCommand
import com.example.editor.engine.DeleteBlockCommand
import com.example.editor.engine.EditorCommand
import com.example.editor.engine.InsertBlockCommand
import com.example.editor.engine.ToggleRtlCommand
import com.example.editor.engine.UndoRedoManager
import com.example.editor.engine.UpdateLayoutCommand
import com.example.editor.engine.UpdateParagraphTextCommand
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File

/**
 * Microsoft Word Ribbon tab categories.
 */
enum class RibbonTab {
    HOME,
    INSERT,
    LAYOUT,
    VIEW,
    FILE
}

/**
 * Visual canvas presentation mode.
 */
enum class EditorViewMode {
    PAGINATED_A4,     // Realistic A4 paper sheet on desk surface
    CONTINUOUS_MOBILE // Flowing full-bleed mobile document canvas
}

class EditorViewModel(
    private val documentId: String,
    private val repository: DocumentRepository,
    private val preferencesManager: PreferencesManager
) : ViewModel() {

    private val undoRedoManager = UndoRedoManager()

    private val _document = MutableStateFlow<RichDocument?>(null)
    val document: StateFlow<RichDocument?> = _document.asStateFlow()

    private val _activeRibbonTab = MutableStateFlow(RibbonTab.HOME)
    val activeRibbonTab: StateFlow<RibbonTab> = _activeRibbonTab.asStateFlow()

    private val _isRibbonVisible = MutableStateFlow(true)
    val isRibbonVisible: StateFlow<Boolean> = _isRibbonVisible.asStateFlow()

    private val _viewMode = MutableStateFlow(EditorViewMode.PAGINATED_A4)
    val viewMode: StateFlow<EditorViewMode> = _viewMode.asStateFlow()

    private val _zoomPercent = MutableStateFlow(100)
    val zoomPercent: StateFlow<Int> = _zoomPercent.asStateFlow()

    private val _canUndo = MutableStateFlow(false)
    val canUndo: StateFlow<Boolean> = _canUndo.asStateFlow()

    private val _canRedo = MutableStateFlow(false)
    val canRedo: StateFlow<Boolean> = _canRedo.asStateFlow()

    private val _activeBlockIndex = MutableStateFlow(0)
    val activeBlockIndex: StateFlow<Int> = _activeBlockIndex.asStateFlow()

    private val _statusMessage = MutableStateFlow<String?>(null)
    val statusMessage: StateFlow<String?> = _statusMessage.asStateFlow()

    // Autosave Controller
    private val autosaveController = AutosaveController(
        scope = viewModelScope,
        debounceMillis = 2000L,
        onPerformSave = {
            val doc = _document.value ?: return@AutosaveController false
            val res = repository.saveDocument(doc)
            res.isSuccess
        }
    )
    val autosaveStatus: StateFlow<AutosaveStatus> = autosaveController.status

    init {
        loadDocument()
    }

    private fun loadDocument() {
        viewModelScope.launch {
            when (val res = repository.getDocument(documentId)) {
                is DocumentResult.Success -> {
                    _document.value = res.data
                    undoRedoManager.clear()
                    updateUndoRedoStates()
                    autosaveController.markSaved()
                }
                is DocumentResult.Failure -> {
                    _statusMessage.value = "Failed to load document: ${res.error.message}"
                }
            }
        }
    }

    fun selectRibbonTab(tab: RibbonTab) {
        _activeRibbonTab.value = tab
    }

    fun toggleRibbonVisibility() {
        _isRibbonVisible.value = !_isRibbonVisible.value
    }

    fun setViewMode(mode: EditorViewMode) {
        _viewMode.value = mode
    }

    fun setZoomPercent(percent: Int) {
        _zoomPercent.value = percent.coerceIn(50, 200)
    }

    fun setActiveBlock(index: Int) {
        _activeBlockIndex.value = index
    }

    fun clearStatusMessage() {
        _statusMessage.value = null
    }

    private fun updateUndoRedoStates() {
        _canUndo.value = undoRedoManager.canUndo
        _canRedo.value = undoRedoManager.canRedo
    }

    private fun executeCommand(command: EditorCommand) {
        val currentDoc = _document.value ?: return
        val newDoc = undoRedoManager.execute(command, currentDoc)
        _document.value = newDoc
        updateUndoRedoStates()
        autosaveController.scheduleAutosave()
    }

    fun undo() {
        val currentDoc = _document.value ?: return
        val (revertedDoc, _) = undoRedoManager.undo(currentDoc)
        _document.value = revertedDoc
        updateUndoRedoStates()
        autosaveController.scheduleAutosave()
    }

    fun redo() {
        val currentDoc = _document.value ?: return
        val (reappliedDoc, _) = undoRedoManager.redo(currentDoc)
        _document.value = reappliedDoc
        updateUndoRedoStates()
        autosaveController.scheduleAutosave()
    }

    // ----------------------------------------------------
    // Document Editing Actions
    // ----------------------------------------------------

    fun updateTitle(newTitle: String) {
        val currentDoc = _document.value ?: return
        _document.value = currentDoc.copy(title = newTitle)
        autosaveController.scheduleAutosave()
    }

    fun updateParagraphText(blockIndex: Int, newText: String) {
        val currentDoc = _document.value ?: return
        val block = currentDoc.blocks.getOrNull(blockIndex) as? ParagraphBlock ?: return
        if (block.text == newText) return

        executeCommand(
            UpdateParagraphTextCommand(
                blockId = block.id,
                oldText = block.text,
                newText = newText
            )
        )
    }

    fun setAlignment(alignment: TextAlignment) {
        val currentDoc = _document.value ?: return
        val block = currentDoc.blocks.getOrNull(_activeBlockIndex.value) as? ParagraphBlock ?: return

        executeCommand(
            ChangeAlignmentCommand(
                blockId = block.id,
                oldAlignment = block.alignment,
                newAlignment = alignment
            )
        )
    }

    fun setHeadingStyle(heading: HeadingStyle) {
        val currentDoc = _document.value ?: return
        val block = currentDoc.blocks.getOrNull(_activeBlockIndex.value) as? ParagraphBlock ?: return

        executeCommand(
            ChangeHeadingStyleCommand(
                blockId = block.id,
                oldHeading = block.headingStyle,
                newHeading = heading
            )
        )
    }

    fun toggleRtl() {
        val currentDoc = _document.value ?: return
        val block = currentDoc.blocks.getOrNull(_activeBlockIndex.value) as? ParagraphBlock ?: return

        executeCommand(
            ToggleRtlCommand(
                blockId = block.id,
                wasRtl = block.isRtl,
                isRtl = !block.isRtl
            )
        )
    }

    fun toggleBold() {
        val currentDoc = _document.value ?: return
        val block = currentDoc.blocks.getOrNull(_activeBlockIndex.value) as? ParagraphBlock ?: return
        val currentlyBold = block.spans.any { it.isBold }
        val newSpans = if (currentlyBold) {
            block.spans.map { it.copy(isBold = false) }
        } else {
            block.spans + InlineSpan(start = 0, end = block.text.length, isBold = true)
        }
        executeCommand(ApplyInlineSpanCommand(block.id, block.spans, newSpans))
    }

    fun toggleItalic() {
        val currentDoc = _document.value ?: return
        val block = currentDoc.blocks.getOrNull(_activeBlockIndex.value) as? ParagraphBlock ?: return
        val currentlyItalic = block.spans.any { it.isItalic }
        val newSpans = if (currentlyItalic) {
            block.spans.map { it.copy(isItalic = false) }
        } else {
            block.spans + InlineSpan(start = 0, end = block.text.length, isItalic = true)
        }
        executeCommand(ApplyInlineSpanCommand(block.id, block.spans, newSpans))
    }

    fun toggleUnderline() {
        val currentDoc = _document.value ?: return
        val block = currentDoc.blocks.getOrNull(_activeBlockIndex.value) as? ParagraphBlock ?: return
        val currentlyUnderline = block.spans.any { it.isUnderline }
        val newSpans = if (currentlyUnderline) {
            block.spans.map { it.copy(isUnderline = false) }
        } else {
            block.spans + InlineSpan(start = 0, end = block.text.length, isUnderline = true)
        }
        executeCommand(ApplyInlineSpanCommand(block.id, block.spans, newSpans))
    }

    fun toggleStrikethrough() {
        val currentDoc = _document.value ?: return
        val block = currentDoc.blocks.getOrNull(_activeBlockIndex.value) as? ParagraphBlock ?: return
        val currentlyStrike = block.spans.any { it.isStrikethrough }
        val newSpans = if (currentlyStrike) {
            block.spans.map { it.copy(isStrikethrough = false) }
        } else {
            block.spans + InlineSpan(start = 0, end = block.text.length, isStrikethrough = true)
        }
        executeCommand(ApplyInlineSpanCommand(block.id, block.spans, newSpans))
    }

    fun setTextColor(colorHex: String) {
        val currentDoc = _document.value ?: return
        val block = currentDoc.blocks.getOrNull(_activeBlockIndex.value) as? ParagraphBlock ?: return
        val span = InlineSpan(start = 0, end = block.text.length, textColorHex = colorHex)
        val newSpans = block.spans + span

        executeCommand(
            ApplyInlineSpanCommand(
                blockId = block.id,
                oldSpans = block.spans,
                newSpans = newSpans
            )
        )
    }

    fun setHighlightColor(colorHex: String) {
        val currentDoc = _document.value ?: return
        val block = currentDoc.blocks.getOrNull(_activeBlockIndex.value) as? ParagraphBlock ?: return
        val span = InlineSpan(start = 0, end = block.text.length, highlightColorHex = colorHex)
        val newSpans = block.spans + span

        executeCommand(
            ApplyInlineSpanCommand(
                blockId = block.id,
                oldSpans = block.spans,
                newSpans = newSpans
            )
        )
    }

    // ----------------------------------------------------
    // Insert Operations
    // ----------------------------------------------------

    fun insertParagraph() {
        val currentDoc = _document.value ?: return
        val insertIndex = _activeBlockIndex.value + 1
        val newBlock = ParagraphBlock()

        executeCommand(
            InsertBlockCommand(index = insertIndex, block = newBlock)
        )
        _activeBlockIndex.value = insertIndex
    }

    fun insertDivider() {
        val currentDoc = _document.value ?: return
        val insertIndex = _activeBlockIndex.value + 1
        val newBlock = DividerBlock()

        executeCommand(
            InsertBlockCommand(index = insertIndex, block = newBlock)
        )
        _activeBlockIndex.value = insertIndex
    }

    fun insertPageBreak() {
        val currentDoc = _document.value ?: return
        val insertIndex = _activeBlockIndex.value + 1
        val newBlock = PageBreakBlock()

        executeCommand(
            InsertBlockCommand(index = insertIndex, block = newBlock)
        )
        _activeBlockIndex.value = insertIndex
    }

    fun insertTable(rows: Int, cols: Int) {
        val currentDoc = _document.value ?: return
        val insertIndex = _activeBlockIndex.value + 1
        val tableBlock = TableBlock(rowCount = rows, columnCount = cols)

        executeCommand(
            InsertBlockCommand(index = insertIndex, block = tableBlock)
        )
        _activeBlockIndex.value = insertIndex
    }

    fun deleteCurrentBlock() {
        val currentDoc = _document.value ?: return
        if (currentDoc.blocks.size <= 1) return
        val block = currentDoc.blocks.getOrNull(_activeBlockIndex.value) ?: return

        executeCommand(
            DeleteBlockCommand(index = _activeBlockIndex.value, deletedBlock = block)
        )
        _activeBlockIndex.value = (_activeBlockIndex.value - 1).coerceAtLeast(0)
    }

    // ----------------------------------------------------
    // Layout Operations
    // ----------------------------------------------------

    fun updatePageLayout(
        pageSize: PageSize? = null,
        orientation: PageOrientation? = null,
        marginPreset: MarginPreset? = null
    ) {
        val currentDoc = _document.value ?: return
        val oldLayout = currentDoc.layoutConfig
        val newLayout = oldLayout.copy(
            pageSize = pageSize ?: oldLayout.pageSize,
            orientation = orientation ?: oldLayout.orientation,
            marginPreset = marginPreset ?: oldLayout.marginPreset
        )

        executeCommand(
            UpdateLayoutCommand(oldLayout = oldLayout, newLayout = newLayout)
        )
    }

    // ----------------------------------------------------
    // Save & Export Operations
    // ----------------------------------------------------

    fun saveNow() {
        autosaveController.triggerImmediateSave()
    }

    fun exportToPdf(context: Context, onComplete: (File?) -> Unit) {
        val doc = _document.value ?: return
        viewModelScope.launch {
            val exportDir = File(context.cacheDir, "exports").apply { mkdirs() }
            val sanitized = doc.title.replace("[^a-zA-Z0-9.-]".toRegex(), "_")
            val outFile = File(exportDir, "$sanitized.pdf")
            when (val res = repository.exportToPdf(context, doc, outFile)) {
                is DocumentResult.Success -> {
                    _statusMessage.value = "PDF Exported: ${outFile.name}"
                    onComplete(outFile)
                }
                is DocumentResult.Failure -> {
                    _statusMessage.value = "PDF Error: ${res.error.message}"
                    onComplete(null)
                }
            }
        }
    }

    fun exportPdfToSafUri(context: Context, uri: Uri) {
        val doc = _document.value ?: return
        viewModelScope.launch {
            when (val res = repository.exportPdfToUri(context, doc, uri)) {
                is DocumentResult.Success -> {
                    _statusMessage.value = "PDF saved to external storage"
                }
                is DocumentResult.Failure -> {
                    _statusMessage.value = "Save to SAF failed: ${res.error.message}"
                }
            }
        }
    }

    fun printDocument(context: Context) {
        val doc = _document.value ?: return
        repository.printDocument(context, doc)
    }
}

class EditorViewModelFactory(
    private val documentId: String,
    private val repository: DocumentRepository,
    private val preferencesManager: PreferencesManager
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return EditorViewModel(documentId, repository, preferencesManager) as T
    }
}
