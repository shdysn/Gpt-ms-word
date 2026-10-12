package com.example.ui.editor

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.core.error.DocumentResult
import com.example.core.layout.MarginPreset
import com.example.core.layout.PageLayoutConfig
import com.example.core.layout.PageOrientation
import com.example.core.layout.PageSize
import com.example.data.local.PreferencesManager
import com.example.data.model.BlockElement
import com.example.data.model.DividerBlock
import com.example.data.model.HeadingStyle
import com.example.data.model.ImageBlock
import com.example.data.model.InlineSpan
import com.example.data.model.ListType
import com.example.data.model.PageBreakBlock
import com.example.data.model.ParagraphBlock
import com.example.data.model.RichDocument
import com.example.data.model.TableCell
import com.example.data.model.TableBlock
import com.example.data.model.TextAlignment
import com.example.data.repository.DocumentRepository
import com.example.data.storage.DocumentStorageManager
import com.example.editor.engine.ApplyInlineSpanCommand
import com.example.editor.engine.AutosaveController
import com.example.editor.engine.AutosaveStatus
import com.example.editor.engine.ChangeAlignmentCommand
import com.example.editor.engine.ChangeHeadingStyleCommand
import com.example.editor.engine.ChangeMultipleParagraphsAlignmentCommand
import com.example.editor.engine.DeleteBlockCommand
import com.example.editor.engine.EditorCommand
import com.example.editor.engine.InsertBlockCommand
import com.example.editor.engine.ToggleRtlCommand
import com.example.editor.engine.UndoRedoManager
import com.example.editor.engine.UpdateImageBlockCommand
import com.example.editor.engine.UpdateLayoutCommand
import com.example.editor.engine.UpdateParagraphTextCommand
import com.example.editor.engine.UpdateTableBlockCommand
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File

/**
 * Microsoft Word Ribbon tab categories.
 */
enum class RibbonTab {
    HOME,
    INSERT,
    VIEW
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

    val paginatedPages: StateFlow<List<com.example.core.layout.DocumentPage>> = _document.map { doc ->
        if (doc == null) emptyList() else com.example.core.layout.PaginationEngine.paginate(doc)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

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

    // Find and Replace in Document
    private val _isFindReplaceVisible = MutableStateFlow(false)
    val isFindReplaceVisible: StateFlow<Boolean> = _isFindReplaceVisible.asStateFlow()

    private val _findQuery = MutableStateFlow("")
    val findQuery: StateFlow<String> = _findQuery.asStateFlow()

    private val _replaceQuery = MutableStateFlow("")
    val replaceQuery: StateFlow<String> = _replaceQuery.asStateFlow()

    private val _matchCount = MutableStateFlow(0)
    val matchCount: StateFlow<Int> = _matchCount.asStateFlow()

    // Selection & Pending Typing Attributes
    private val _selection = MutableStateFlow(androidx.compose.ui.text.TextRange.Zero)
    val selection: StateFlow<androidx.compose.ui.text.TextRange> = _selection.asStateFlow()

    private val _pendingFormatting = MutableStateFlow(com.example.editor.engine.ActiveFormatting())
    val pendingFormatting: StateFlow<com.example.editor.engine.ActiveFormatting> = _pendingFormatting.asStateFlow()

    val activeFormatting: StateFlow<com.example.editor.engine.ActiveFormatting> = combine(
        _document,
        _activeBlockIndex,
        _selection,
        _pendingFormatting
    ) { doc, index, sel, pending ->
        val block = doc?.blocks?.getOrNull(index) as? ParagraphBlock
        if (block == null) {
            pending
        } else if (!sel.collapsed && sel.min < sel.max) {
            com.example.editor.engine.SpanOperations.queryFormatting(block.spans, sel.min, sel.max)
        } else {
            val cursorFormat = com.example.editor.engine.SpanOperations.queryFormatting(block.spans, sel.start, sel.end)
            if (pending != com.example.editor.engine.ActiveFormatting()) pending else cursorFormat
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = com.example.editor.engine.ActiveFormatting()
    )

    // Autosave Controller with Crash-Recovery Draft Snapshotting
    private val autosaveController = AutosaveController(
        scope = viewModelScope,
        debounceMillis = 1500L,
        onPerformSave = {
            val doc = _document.value ?: return@AutosaveController false
            val res = repository.saveDocument(doc)
            res.isSuccess
        },
        onPerformRecoverySnapshot = {
            val doc = _document.value ?: return@AutosaveController
            repository.saveRecoveryDraft(doc)
        }
    )
    val autosaveStatus: StateFlow<AutosaveStatus> = autosaveController.status

    private val _recoveryAvailable = MutableStateFlow(false)
    val recoveryAvailable: StateFlow<Boolean> = _recoveryAvailable.asStateFlow()

    val isAutosaveEnabled: StateFlow<Boolean> = preferencesManager.isAutosaveEnabled
        .stateIn(viewModelScope, SharingStarted.Eagerly, true)

    val defaultRtl: StateFlow<Boolean> = preferencesManager.defaultRtl
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    val showWordCount: StateFlow<Boolean> = preferencesManager.showWordCount
        .stateIn(viewModelScope, SharingStarted.Eagerly, true)

    val showRuler: StateFlow<Boolean> = preferencesManager.showRuler
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    val isDirty: StateFlow<Boolean> = autosaveController.status.map {
        it == AutosaveStatus.DIRTY || it == AutosaveStatus.ERROR
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = false
    )

    init {
        loadDocument()
    }

    private fun scheduleDocumentAutosave() {
        if (isAutosaveEnabled.value) {
            autosaveController.scheduleAutosave()
        } else {
            autosaveController.markDirty()
        }
    }

    private fun loadDocument() {
        viewModelScope.launch {
            if (repository.hasRecoveryDraft(documentId)) {
                _recoveryAvailable.value = true
            }
            when (val res = repository.getDocument(documentId)) {
                is DocumentResult.Success -> {
                    val loadedDoc = res.data
                    val normalizedBlocks = normalizeParagraphBlocks(loadedDoc.blocks)
                    val firstBlock = normalizedBlocks.firstOrNull() as? ParagraphBlock
                    val adjustedDoc = if (defaultRtl.value && firstBlock != null && firstBlock.text.isEmpty() && !firstBlock.isRtl) {
                        loadedDoc.copy(
                            blocks = listOf(
                                firstBlock.copy(
                                    isRtl = true,
                                    alignment = TextAlignment.END
                                )
                            )
                        )
                    } else {
                        loadedDoc.copy(blocks = normalizedBlocks)
                    }
                    _document.value = adjustedDoc
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

    private fun normalizeParagraphBlocks(blocks: List<BlockElement>): List<BlockElement> {
        val result = mutableListOf<BlockElement>()
        for (block in blocks) {
            if (block is ParagraphBlock && block.text.contains("\n")) {
                val lines = block.text.split("\n")
                var offset = 0
                lines.forEachIndexed { idx, line ->
                    val lineStart = offset
                    val lineEnd = offset + line.length
                    val lineSpans = block.spans.mapNotNull { span ->
                        val s = (span.start - lineStart).coerceIn(0, line.length)
                        val e = (span.end - lineStart).coerceIn(0, line.length)
                        if (s < e && span.start < lineEnd && span.end > lineStart) {
                            span.copy(start = s, end = e)
                        } else null
                    }
                    result.add(
                        block.copy(
                            id = if (idx == 0) block.id else java.util.UUID.randomUUID().toString(),
                            text = line,
                            spans = lineSpans
                        )
                    )
                    offset = lineEnd + 1
                }
            } else {
                result.add(block)
            }
        }
        return if (result.isEmpty()) listOf(ParagraphBlock()) else result
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

    fun toggleViewMode() {
        _viewMode.value = if (_viewMode.value == EditorViewMode.PAGINATED_A4) {
            EditorViewMode.CONTINUOUS_MOBILE
        } else {
            EditorViewMode.PAGINATED_A4
        }
    }

    fun setZoomPercent(percent: Int) {
        _zoomPercent.value = percent.coerceIn(25, 250)
    }

    fun zoomIn() {
        setZoomPercent(_zoomPercent.value + 10)
    }

    fun zoomOut() {
        setZoomPercent(_zoomPercent.value - 10)
    }

    fun zoom100() {
        setZoomPercent(100)
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
        scheduleDocumentAutosave()
    }

    fun undo() {
        val currentDoc = _document.value ?: return
        val (revertedDoc, _) = undoRedoManager.undo(currentDoc)
        _document.value = revertedDoc
        updateUndoRedoStates()
        scheduleDocumentAutosave()
    }

    fun redo() {
        val currentDoc = _document.value ?: return
        val (reappliedDoc, _) = undoRedoManager.redo(currentDoc)
        _document.value = reappliedDoc
        updateUndoRedoStates()
        scheduleDocumentAutosave()
    }

    // ----------------------------------------------------
    // Document Editing Actions
    // ----------------------------------------------------

    fun updateTitle(newTitle: String) {
        val currentDoc = _document.value ?: return
        _document.value = currentDoc.copy(title = newTitle)
        scheduleDocumentAutosave()
    }

    fun updateSelection(newSelection: androidx.compose.ui.text.TextRange) {
        _selection.value = newSelection
    }

    fun updateSelectionForBlock(blockIndex: Int, newSelection: androidx.compose.ui.text.TextRange) {
        _activeBlockIndex.value = blockIndex
        _selection.value = newSelection
    }

    fun findActiveParagraphBlockIndex(blocks: List<BlockElement>, offset: Int): Int {
        var accumulated = 0
        var lastParagraphIndex = 0
        blocks.forEachIndexed { index, block ->
            if (block is ParagraphBlock) {
                lastParagraphIndex = index
                val len = block.text.length
                if (offset <= accumulated + len) {
                    return index
                }
                accumulated += len + 1
            }
        }
        return lastParagraphIndex
    }

    fun findParagraphBlockIndicesForRange(blocks: List<BlockElement>, startOffset: Int, endOffset: Int): List<Int> {
        val min = minOf(startOffset, endOffset)
        val max = maxOf(startOffset, endOffset)
        if (min == max) return listOf(findActiveParagraphBlockIndex(blocks, min))

        val result = mutableListOf<Int>()
        var accumulated = 0
        blocks.forEachIndexed { index, block ->
            if (block is ParagraphBlock) {
                val blockStart = accumulated
                val blockEnd = accumulated + block.text.length
                if (blockStart <= max && (blockEnd >= min || (blockStart == blockEnd && blockStart == min))) {
                    result.add(index)
                }
                accumulated = blockEnd + 1
            }
        }
        return if (result.isEmpty()) listOf(findActiveParagraphBlockIndex(blocks, min)) else result
    }

    fun updateParagraphSubSliceText(
        blockIndex: Int,
        startChar: Int,
        endChar: Int,
        newSliceText: String,
        sliceSelection: androidx.compose.ui.text.TextRange
    ) {
        val currentDoc = _document.value ?: return
        val block = currentDoc.blocks.getOrNull(blockIndex) as? ParagraphBlock ?: return
        val safeStart = startChar.coerceIn(0, block.text.length)
        val safeEnd = endChar.coerceIn(safeStart, block.text.length)
        if (safeStart == 0 && safeEnd == block.text.length) {
            updateParagraphText(blockIndex, newSliceText, sliceSelection)
            return
        }
        val fullNewText = block.text.substring(0, safeStart) + newSliceText + block.text.substring(safeEnd)
        val fullSelection = androidx.compose.ui.text.TextRange(
            (safeStart + sliceSelection.start).coerceIn(0, fullNewText.length),
            (safeStart + sliceSelection.end).coerceIn(0, fullNewText.length)
        )
        updateParagraphText(blockIndex, fullNewText, fullSelection)
    }

    fun updateParagraphText(
        blockIndex: Int,
        newText: String,
        newSelection: androidx.compose.ui.text.TextRange = androidx.compose.ui.text.TextRange(newText.length)
    ) {
        val currentDoc = _document.value ?: return
        val block = currentDoc.blocks.getOrNull(blockIndex) as? ParagraphBlock ?: return
        val oldText = block.text
        if (oldText == newText) {
            _selection.value = newSelection
            return
        }

        // If user pressed Enter on soft keyboard or pasted text containing newlines:
        if (newText.contains("\n")) {
            val lines = newText.split("\n")
            val firstLine = lines.first()
            val (editOffset, deletedLen, insertedLen) = calculateEditDelta(oldText, firstLine)
            val shiftedSpans = com.example.editor.engine.SpanOperations.shiftSpansOnTextChange(
                spans = block.spans,
                editOffset = editOffset,
                deletedLength = deletedLen,
                insertedLength = insertedLen,
                pendingFormatting = if (insertedLen > 0) _pendingFormatting.value else null
            )
            val updatedFirst = block.copy(
                text = firstLine,
                spans = shiftedSpans.mapNotNull { if (it.start < firstLine.length) it.copy(end = it.end.coerceAtMost(firstLine.length)) else null }
            )

            val newBlocks = currentDoc.blocks.toMutableList()
            newBlocks[blockIndex] = updatedFirst

            for (i in 1 until lines.size) {
                val lineText = lines[i]
                val newBlock = ParagraphBlock(
                    id = java.util.UUID.randomUUID().toString(),
                    text = lineText,
                    alignment = block.alignment,
                    isRtl = block.isRtl || com.example.core.typography.BidiUtils.detectParagraphIsRtl(lineText),
                    headingStyle = if (block.headingStyle == HeadingStyle.TITLE || block.headingStyle == HeadingStyle.SUBTITLE) HeadingStyle.NORMAL else block.headingStyle,
                    listType = if (block.listType == ListType.NUMBERED) ListType.NUMBERED else ListType.NONE,
                    lineSpacingMultiplier = block.lineSpacingMultiplier
                )
                newBlocks.add(blockIndex + i, newBlock)
            }

            val updatedDoc = currentDoc.copy(blocks = newBlocks, updatedAt = System.currentTimeMillis())
            _document.value = updatedDoc
            val finalActiveIndex = blockIndex + lines.size - 1
            _activeBlockIndex.value = finalActiveIndex
            _selection.value = androidx.compose.ui.text.TextRange(lines.last().length)
            scheduleDocumentAutosave()
            return
        }

        // Calculate edit offset and delta
        val (editOffset, deletedLen, insertedLen) = calculateEditDelta(oldText, newText)

        val newSpans = com.example.editor.engine.SpanOperations.shiftSpansOnTextChange(
            spans = block.spans,
            editOffset = editOffset,
            deletedLength = deletedLen,
            insertedLength = insertedLen,
            pendingFormatting = if (insertedLen > 0) _pendingFormatting.value else null
        )

        val autoRtl = if (oldText.isBlank() && !block.isRtl && com.example.core.typography.BidiUtils.detectParagraphIsRtl(newText)) true else null

        executeCommand(
            com.example.editor.engine.UpdateParagraphTextAndSpansCommand(
                blockId = block.id,
                oldText = oldText,
                newText = newText,
                oldSpans = block.spans,
                newSpans = newSpans,
                wasRtl = block.isRtl,
                isRtl = autoRtl
            )
        )
        _selection.value = newSelection
    }

    fun splitParagraph(blockIndex: Int, cursorOffset: Int) {
        val currentDoc = _document.value ?: return
        val block = currentDoc.blocks.getOrNull(blockIndex) as? ParagraphBlock ?: return
        val cursor = cursorOffset.coerceIn(0, block.text.length)

        val text1 = block.text.substring(0, cursor)
        val text2 = block.text.substring(cursor)

        val spans1 = block.spans.mapNotNull { span ->
            val s = span.start.coerceIn(0, cursor)
            val e = span.end.coerceIn(0, cursor)
            if (s < e) span.copy(start = s, end = e) else null
        }
        val spans2 = block.spans.mapNotNull { span ->
            if (span.end > cursor) {
                val s = (span.start - cursor).coerceAtLeast(0)
                val e = (span.end - cursor).coerceAtLeast(0)
                if (s < e) span.copy(start = s, end = e) else null
            } else null
        }

        val updatedCurrent = block.copy(
            text = text1,
            spans = spans1
        )
        val newBlock = ParagraphBlock(
            id = java.util.UUID.randomUUID().toString(),
            text = text2,
            spans = spans2,
            alignment = block.alignment,
            isRtl = block.isRtl || com.example.core.typography.BidiUtils.detectParagraphIsRtl(text2),
            headingStyle = if (block.headingStyle == HeadingStyle.TITLE || block.headingStyle == HeadingStyle.SUBTITLE) HeadingStyle.NORMAL else block.headingStyle,
            listType = if (block.listType == ListType.NUMBERED) ListType.NUMBERED else ListType.NONE,
            lineSpacingMultiplier = block.lineSpacingMultiplier
        )

        val newBlocks = currentDoc.blocks.toMutableList()
        newBlocks[blockIndex] = updatedCurrent
        newBlocks.add(blockIndex + 1, newBlock)

        val updatedDoc = currentDoc.copy(blocks = newBlocks, updatedAt = System.currentTimeMillis())
        undoRedoManager.execute(
            object : com.example.editor.engine.EditorCommand {
                override val description: String = "Split Paragraph"
                override fun apply(document: RichDocument): RichDocument = updatedDoc
                override fun revert(document: RichDocument): RichDocument = currentDoc
            },
            currentDoc
        )
        _document.value = updatedDoc
        _activeBlockIndex.value = blockIndex + 1
        _selection.value = androidx.compose.ui.text.TextRange.Zero
        scheduleDocumentAutosave()
    }

    private var typingBaseDoc: RichDocument? = null
    private var lastTypingTimestamp: Long = 0L

    private fun registerTypingUndoCheckpoint(currentDoc: RichDocument, newDoc: RichDocument, newText: String) {
        val now = System.currentTimeMillis()
        if (typingBaseDoc == null) {
            typingBaseDoc = currentDoc
        }
        val isWordBoundary = newText.endsWith(" ") || newText.endsWith("\n") || (now - lastTypingTimestamp > 700L)
        lastTypingTimestamp = now
        if (isWordBoundary && typingBaseDoc != null && typingBaseDoc != newDoc) {
            val base = typingBaseDoc!!
            undoRedoManager.execute(
                object : com.example.editor.engine.EditorCommand {
                    override val description: String = "Typing"
                    override fun apply(document: RichDocument): RichDocument = newDoc
                    override fun revert(document: RichDocument): RichDocument = base
                },
                base
            )
            typingBaseDoc = newDoc
            updateUndoRedoStates()
        }
    }

    /**
     * Unified Text Canvas editing handler: updates the full continuous document text,
     * allowing characters to be added or deleted smoothly across the entire page.
     * When newFullText is empty (e.g. user backspaces everything), all text is wiped out
     * leaving a clean single blank paragraph with blinking cursor.
     */
    fun updateUnifiedDocumentText(
        newFullText: String,
        newSelection: androidx.compose.ui.text.TextRange = androidx.compose.ui.text.TextRange(newFullText.length)
    ) {
        val currentDoc = _document.value ?: return

        // If the user deleted all text via backspace or select-all + backspace:
        if (newFullText.isEmpty()) {
            val emptyBlock = ParagraphBlock(
                text = "",
                spans = emptyList(),
                alignment = if (defaultRtl.value) TextAlignment.END else TextAlignment.START,
                isRtl = defaultRtl.value
            )
            val updatedDoc = currentDoc.copy(
                blocks = listOf(emptyBlock),
                updatedAt = System.currentTimeMillis()
            )
            undoRedoManager.execute(
                object : com.example.editor.engine.EditorCommand {
                    override val description: String = "Clear All Text"
                    override fun apply(document: RichDocument): RichDocument = updatedDoc
                    override fun revert(document: RichDocument): RichDocument = currentDoc
                },
                currentDoc
            )
            typingBaseDoc = null
            _document.value = updatedDoc
            _activeBlockIndex.value = 0
            _selection.value = androidx.compose.ui.text.TextRange.Zero
            updateUndoRedoStates()
            scheduleDocumentAutosave()
            return
        }

        val oldParagraphs = currentDoc.blocks.filterIsInstance<ParagraphBlock>()
        val nonParagraphBlocks = currentDoc.blocks.filter { it !is ParagraphBlock }
        val oldFullText = oldParagraphs.joinToString("\n") { it.text }
        if (oldFullText == newFullText && oldParagraphs.isNotEmpty()) {
            updateSelection(newSelection)
            return
        }

        val (editOffset, deletedLen, insertedLen) = calculateEditDelta(oldFullText, newFullText)
        val newLines = newFullText.split("\n")

        val newParagraphs = reconcileParagraphBlocks(
            oldParagraphs = oldParagraphs,
            newLines = newLines,
            editOffset = editOffset,
            deletedLen = deletedLen,
            insertedLen = insertedLen,
            pendingFormatting = _pendingFormatting.value
        )

        val newBlocks = newParagraphs + nonParagraphBlocks

        val updatedDoc = currentDoc.copy(
            blocks = newBlocks,
            updatedAt = System.currentTimeMillis()
        )
        registerTypingUndoCheckpoint(currentDoc, updatedDoc, newFullText)
        _document.value = updatedDoc
        updateSelection(newSelection)
        scheduleDocumentAutosave()
    }

    private fun reconcileParagraphBlocks(
        oldParagraphs: List<ParagraphBlock>,
        newLines: List<String>,
        editOffset: Int,
        deletedLen: Int,
        insertedLen: Int,
        pendingFormatting: com.example.editor.engine.ActiveFormatting?
    ): List<ParagraphBlock> {
        if (oldParagraphs.isEmpty()) {
            return newLines.map { line ->
                ParagraphBlock(text = line)
            }
        }

        // Case 1: Same number of lines (editing within lines, no newline added/removed)
        if (newLines.size == oldParagraphs.size) {
            return newLines.mapIndexed { idx, line ->
                val oldP = oldParagraphs[idx]
                if (oldP.text == line) {
                    oldP
                } else {
                    val (pOffset, pDeleted, pInserted) = calculateEditDelta(oldP.text, line)
                    val shiftedSpans = com.example.editor.engine.SpanOperations.shiftSpansOnTextChange(
                        spans = oldP.spans,
                        editOffset = pOffset,
                        deletedLength = pDeleted,
                        insertedLength = pInserted,
                        pendingFormatting = if (pInserted > 0) pendingFormatting else null
                    )
                    val autoRtl = if (oldP.text.isBlank() && !oldP.isRtl && com.example.core.typography.BidiUtils.detectParagraphIsRtl(line)) true else oldP.isRtl
                    oldP.copy(text = line, spans = shiftedSpans, isRtl = autoRtl)
                }
            }
        }

        // Case 2: Newlines were added or removed
        var accumulated = 0
        var editParaIndex = oldParagraphs.lastIndex
        for (i in oldParagraphs.indices) {
            val pLen = oldParagraphs[i].text.length
            if (editOffset <= accumulated + pLen) {
                editParaIndex = i
                break
            }
            accumulated += pLen + 1
        }

        val deltaLines = newLines.size - oldParagraphs.size
        val result = mutableListOf<ParagraphBlock>()

        if (deltaLines > 0) {
            // Newlines inserted (e.g. Enter pressed)
            for (i in 0 until editParaIndex) {
                val lineText = newLines.getOrElse(i) { oldParagraphs[i].text }
                result.add(oldParagraphs[i].copy(text = lineText))
            }

            val sourceP = oldParagraphs[editParaIndex]
            val firstLineText = newLines.getOrElse(editParaIndex) { sourceP.text }
            result.add(sourceP.copy(text = firstLineText))

            for (d in 1..deltaLines) {
                val newLineIndex = editParaIndex + d
                val newLineText = newLines.getOrElse(newLineIndex) { "" }
                result.add(
                    ParagraphBlock(
                        text = newLineText,
                        alignment = sourceP.alignment,
                        isRtl = sourceP.isRtl || com.example.core.typography.BidiUtils.detectParagraphIsRtl(newLineText),
                        lineSpacingMultiplier = sourceP.lineSpacingMultiplier
                    )
                )
            }

            for (i in (editParaIndex + 1) until oldParagraphs.size) {
                val targetLineIndex = i + deltaLines
                val lineText = newLines.getOrElse(targetLineIndex) { oldParagraphs[i].text }
                result.add(oldParagraphs[i].copy(text = lineText))
            }
        } else {
            // Newlines deleted (e.g. Backspace merged lines)
            for (i in 0 until editParaIndex) {
                val lineText = newLines.getOrElse(i) { oldParagraphs[i].text }
                result.add(oldParagraphs[i].copy(text = lineText))
            }

            val sourceP = oldParagraphs[editParaIndex]
            val mergedText = newLines.getOrElse(editParaIndex) { sourceP.text }
            result.add(sourceP.copy(text = mergedText))

            val deletedCount = -deltaLines
            for (i in (editParaIndex + 1 + deletedCount) until oldParagraphs.size) {
                val targetLineIndex = i - deletedCount
                if (targetLineIndex in newLines.indices) {
                    val lineText = newLines[targetLineIndex]
                    result.add(oldParagraphs[i].copy(text = lineText))
                }
            }

            while (result.size < newLines.size) {
                val idx = result.size
                result.add(ParagraphBlock(text = newLines[idx]))
            }
        }

        return if (result.isEmpty()) listOf(ParagraphBlock()) else result
    }


    /**
     * Seamless backspace handling: when cursor is at the beginning of a block
     * or on an empty block, merges it with the preceding block or deletes it,
     * ensuring backspace can traverse and delete the entire document without getting stuck.
     */
    fun deleteOrMergeWithPrevious(blockIndex: Int) {
        val currentDoc = _document.value ?: return
        if (blockIndex <= 0) {
            val firstBlock = currentDoc.blocks.firstOrNull() as? ParagraphBlock ?: return
            if (firstBlock.text.isNotEmpty()) {
                updateParagraphText(0, "", androidx.compose.ui.text.TextRange.Zero)
            }
            return
        }

        val currentBlock = currentDoc.blocks.getOrNull(blockIndex) as? ParagraphBlock ?: return
        val prevBlock = currentDoc.blocks.getOrNull(blockIndex - 1) as? ParagraphBlock

        if (prevBlock != null) {
            val prevText = prevBlock.text
            val mergedText = prevText + currentBlock.text
            val joinPosition = prevText.length

            val shiftedCurrentSpans = currentBlock.spans.map { span ->
                span.copy(start = span.start + joinPosition, end = span.end + joinPosition)
            }
            val mergedSpans = prevBlock.spans + shiftedCurrentSpans

            val updatedPrev = prevBlock.copy(text = mergedText, spans = mergedSpans)
            val newBlocks = currentDoc.blocks.toMutableList()
            newBlocks[blockIndex - 1] = updatedPrev
            newBlocks.removeAt(blockIndex)

            _document.value = currentDoc.copy(blocks = newBlocks, updatedAt = System.currentTimeMillis())
            _activeBlockIndex.value = blockIndex - 1
            _selection.value = androidx.compose.ui.text.TextRange(joinPosition)
            scheduleDocumentAutosave()
        } else {
            if (currentDoc.blocks.size > 1) {
                deleteCurrentBlock()
            }
        }
    }


    private fun calculateEditDelta(oldText: String, newText: String): Triple<Int, Int, Int> {
        var start = 0
        while (start < oldText.length && start < newText.length && oldText[start] == newText[start]) {
            start++
        }
        var oldEnd = oldText.length - 1
        var newEnd = newText.length - 1
        while (oldEnd >= start && newEnd >= start && oldText[oldEnd] == newText[newEnd]) {
            oldEnd--
            newEnd--
        }
        val deleted = (oldEnd - start + 1).coerceAtLeast(0)
        val inserted = (newEnd - start + 1).coerceAtLeast(0)
        return Triple(start, deleted, inserted)
    }

    private fun applyStyleToSelectionOrPending(
        onSelection: (List<InlineSpan>, Int, Int, Int) -> List<InlineSpan>,
        onPending: (com.example.editor.engine.ActiveFormatting) -> com.example.editor.engine.ActiveFormatting
    ) {
        val currentDoc = _document.value ?: return
        val block = currentDoc.blocks.getOrNull(_activeBlockIndex.value) as? ParagraphBlock ?: return
        val sel = _selection.value

        if (!sel.collapsed && sel.min < sel.max) {
            val newSpans = onSelection(block.spans, sel.min, sel.max, block.text.length)
            executeCommand(
                ApplyInlineSpanCommand(
                    blockId = block.id,
                    oldSpans = block.spans,
                    newSpans = newSpans
                )
            )
        } else {
            _pendingFormatting.value = onPending(_pendingFormatting.value)
        }
    }

    fun setAlignment(alignment: TextAlignment) {
        val currentDoc = _document.value ?: return
        val targetIdx = _activeBlockIndex.value.coerceIn(0, (currentDoc.blocks.size - 1).coerceAtLeast(0))
        val block = currentDoc.blocks.getOrNull(targetIdx) as? ParagraphBlock ?: return

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
        applyStyleToSelectionOrPending(
            onSelection = { spans, s, e, len ->
                com.example.editor.engine.SpanOperations.applyOrToggleStyle(spans, s, e, len) { span, enable ->
                    span.copy(isBold = enable)
                }
            },
            onPending = { it.copy(isBold = !it.isBold) }
        )
    }

    fun toggleItalic() {
        applyStyleToSelectionOrPending(
            onSelection = { spans, s, e, len ->
                com.example.editor.engine.SpanOperations.applyOrToggleStyle(spans, s, e, len) { span, enable ->
                    span.copy(isItalic = enable)
                }
            },
            onPending = { it.copy(isItalic = !it.isItalic) }
        )
    }

    fun toggleUnderline() {
        applyStyleToSelectionOrPending(
            onSelection = { spans, s, e, len ->
                com.example.editor.engine.SpanOperations.applyOrToggleStyle(spans, s, e, len) { span, enable ->
                    span.copy(isUnderline = enable)
                }
            },
            onPending = { it.copy(isUnderline = !it.isUnderline) }
        )
    }

    fun toggleStrikethrough() {
        applyStyleToSelectionOrPending(
            onSelection = { spans, s, e, len ->
                com.example.editor.engine.SpanOperations.applyOrToggleStyle(spans, s, e, len) { span, enable ->
                    span.copy(isStrikethrough = enable)
                }
            },
            onPending = { it.copy(isStrikethrough = !it.isStrikethrough) }
        )
    }

    fun toggleSuperscript() {
        applyStyleToSelectionOrPending(
            onSelection = { spans, s, e, len ->
                com.example.editor.engine.SpanOperations.applyOrToggleStyle(spans, s, e, len) { span, enable ->
                    span.copy(isSuperscript = enable, isSubscript = if (enable) false else span.isSubscript)
                }
            },
            onPending = { it.copy(isSuperscript = !it.isSuperscript, isSubscript = false) }
        )
    }

    fun toggleSubscript() {
        applyStyleToSelectionOrPending(
            onSelection = { spans, s, e, len ->
                com.example.editor.engine.SpanOperations.applyOrToggleStyle(spans, s, e, len) { span, enable ->
                    span.copy(isSubscript = enable, isSuperscript = if (enable) false else span.isSuperscript)
                }
            },
            onPending = { it.copy(isSubscript = !it.isSubscript, isSuperscript = false) }
        )
    }

    fun setTextColor(colorHex: String?) {
        applyStyleToSelectionOrPending(
            onSelection = { spans, s, e, len ->
                com.example.editor.engine.SpanOperations.applyExplicitStyle(spans, s, e, len) { span ->
                    span.copy(textColorHex = colorHex)
                }
            },
            onPending = { it.copy(textColorHex = colorHex) }
        )
    }

    fun setHighlightColor(colorHex: String?) {
        applyStyleToSelectionOrPending(
            onSelection = { spans, s, e, len ->
                com.example.editor.engine.SpanOperations.applyExplicitStyle(spans, s, e, len) { span ->
                    span.copy(highlightColorHex = colorHex)
                }
            },
            onPending = { it.copy(highlightColorHex = colorHex) }
        )
    }

    fun setFontFamily(fontName: String) {
        applyStyleToSelectionOrPending(
            onSelection = { spans, s, e, len ->
                com.example.editor.engine.SpanOperations.applyExplicitStyle(spans, s, e, len) { span ->
                    span.copy(fontFamilyName = fontName)
                }
            },
            onPending = { it.copy(fontFamilyName = fontName) }
        )
    }

    fun setFontSize(sizeSp: Float) {
        applyStyleToSelectionOrPending(
            onSelection = { spans, s, e, len ->
                com.example.editor.engine.SpanOperations.applyExplicitStyle(spans, s, e, len) { span ->
                    span.copy(fontSizeSp = sizeSp)
                }
            },
            onPending = { it.copy(fontSizeSp = sizeSp) }
        )
    }

    fun setHyperlink(url: String?) {
        applyStyleToSelectionOrPending(
            onSelection = { spans, s, e, len ->
                com.example.editor.engine.SpanOperations.applyExplicitStyle(spans, s, e, len) { span ->
                    span.copy(linkUrl = url, isUnderline = if (url != null) true else span.isUnderline)
                }
            },
            onPending = { it.copy(linkUrl = url) }
        )
    }

    fun toggleListType(targetType: com.example.data.model.ListType) {
        val currentDoc = _document.value ?: return
        val block = currentDoc.blocks.getOrNull(_activeBlockIndex.value) as? ParagraphBlock ?: return
        val newType = if (block.listType == targetType) com.example.data.model.ListType.NONE else targetType

        executeCommand(
            com.example.editor.engine.ChangeListTypeCommand(
                blockId = block.id,
                oldListType = block.listType,
                newListType = newType
            )
        )
    }

    fun increaseIndent() {
        val currentDoc = _document.value ?: return
        val block = currentDoc.blocks.getOrNull(_activeBlockIndex.value) as? ParagraphBlock ?: return
        val newLevel = (block.indentationLevel + 1).coerceAtMost(6)
        if (newLevel != block.indentationLevel) {
            executeCommand(
                com.example.editor.engine.SetIndentationCommand(
                    blockId = block.id,
                    oldLevel = block.indentationLevel,
                    newLevel = newLevel
                )
            )
        }
    }

    fun decreaseIndent() {
        val currentDoc = _document.value ?: return
        val block = currentDoc.blocks.getOrNull(_activeBlockIndex.value) as? ParagraphBlock ?: return
        val newLevel = (block.indentationLevel - 1).coerceAtLeast(0)
        if (newLevel != block.indentationLevel) {
            executeCommand(
                com.example.editor.engine.SetIndentationCommand(
                    blockId = block.id,
                    oldLevel = block.indentationLevel,
                    newLevel = newLevel
                )
            )
        }
    }

    // ----------------------------------------------------
    // Find & Replace Operations
    // ----------------------------------------------------

    fun toggleFindReplace() {
        _isFindReplaceVisible.value = !_isFindReplaceVisible.value
        if (!_isFindReplaceVisible.value) {
            _findQuery.value = ""
            _replaceQuery.value = ""
            _matchCount.value = 0
        }
    }

    fun updateFindQuery(query: String) {
        _findQuery.value = query
        recomputeMatches(query)
    }

    fun updateReplaceQuery(query: String) {
        _replaceQuery.value = query
    }

    private fun recomputeMatches(query: String) {
        val doc = _document.value ?: return
        if (query.isBlank()) {
            _matchCount.value = 0
            return
        }
        var total = 0
        for (block in doc.blocks) {
            if (block is ParagraphBlock) {
                var idx = 0
                while (true) {
                    val found = block.text.indexOf(query, idx, ignoreCase = true)
                    if (found == -1) break
                    total++
                    idx = found + query.length
                }
            }
        }
        _matchCount.value = total
    }

    fun replaceAll() {
        val doc = _document.value ?: return
        val find = _findQuery.value
        val replace = _replaceQuery.value
        if (find.isBlank()) return

        var count = 0
        val newBlocks = doc.blocks.map { block ->
            if (block is ParagraphBlock && block.text.contains(find, ignoreCase = true)) {
                val (replacedText, shiftedSpans) = com.example.editor.engine.SpanOperations.replaceTextAndShiftSpans(
                    text = block.text,
                    spans = block.spans,
                    find = find,
                    replace = replace,
                    ignoreCase = true
                )
                count += 1
                block.copy(text = replacedText, spans = shiftedSpans)
            } else block
        }

        if (count > 0 || newBlocks != doc.blocks) {
            val newDoc = doc.copy(blocks = newBlocks)
            executeCommand(
                com.example.editor.engine.BatchReplaceTextCommand(
                    oldDocument = doc,
                    newDocument = newDoc,
                    replacementsCount = _matchCount.value
                )
            )
            _statusMessage.value = "Replaced occurrences"
            recomputeMatches(find)
        }
    }


    // ----------------------------------------------------
    // Insert Operations
    // ----------------------------------------------------

    fun insertParagraph() {
        val currentDoc = _document.value ?: return
        val activeIdx = _activeBlockIndex.value.coerceIn(0, (currentDoc.blocks.size - 1).coerceAtLeast(0))
        val block = currentDoc.blocks.getOrNull(activeIdx) as? ParagraphBlock
        if (block != null) {
            val sel = _selection.value
            val cursor = sel.start.coerceIn(0, block.text.length)
            splitParagraph(activeIdx, cursor)
        } else {
            val newBlock = ParagraphBlock(
                alignment = if (defaultRtl.value) TextAlignment.END else TextAlignment.START,
                isRtl = defaultRtl.value
            )
            val newBlocks = currentDoc.blocks.toMutableList()
            newBlocks.add(activeIdx + 1, newBlock)
            _document.value = currentDoc.copy(blocks = newBlocks, updatedAt = System.currentTimeMillis())
            _activeBlockIndex.value = activeIdx + 1
            _selection.value = androidx.compose.ui.text.TextRange.Zero
            scheduleDocumentAutosave()
        }
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

    fun insertImageFromUri(context: Context, sourceUri: Uri, caption: String = "") {
        val storageManager = DocumentStorageManager(context)
        val persistentUri = storageManager.persistImageFromUri(sourceUri)
        insertImage(persistentUri, caption)
    }

    fun insertImage(uri: String, caption: String = "") {
        val currentDoc = _document.value ?: return
        val insertIndex = _activeBlockIndex.value + 1
        val imageBlock = ImageBlock(imageUri = uri, caption = caption)

        executeCommand(
            InsertBlockCommand(index = insertIndex, block = imageBlock)
        )
        _activeBlockIndex.value = insertIndex
    }

    fun updateImageProperties(
        blockId: String,
        widthDp: Int? = null,
        heightDp: Int? = null,
        alignment: TextAlignment? = null,
        caption: String? = null,
        aspectRatio: Float? = null
    ) {
        val currentDoc = _document.value ?: return
        val oldBlock = currentDoc.blocks.firstOrNull { it.id == blockId } as? ImageBlock ?: return
        val newWidth = widthDp ?: oldBlock.widthDp
        val newRatio = aspectRatio ?: oldBlock.aspectRatio
        val newHeight = heightDp ?: if (widthDp != null && newRatio > 0) (widthDp / newRatio).toInt() else oldBlock.heightDp

        val newBlock = oldBlock.copy(
            widthDp = newWidth.coerceIn(60, 600),
            heightDp = newHeight.coerceIn(60, 600),
            alignment = alignment ?: oldBlock.alignment,
            caption = caption ?: oldBlock.caption,
            aspectRatio = newRatio
        )

        executeCommand(
            UpdateImageBlockCommand(
                blockId = blockId,
                oldImage = oldBlock,
                newImage = newBlock,
                actionDescription = "Resize / Align Image"
            )
        )
    }

    fun insertDateTimeStamp(formattedDate: String) {
        val currentDoc = _document.value ?: return
        val insertIndex = _activeBlockIndex.value + 1
        val dateBlock = ParagraphBlock(text = formattedDate, headingStyle = HeadingStyle.SUBTITLE)

        executeCommand(
            InsertBlockCommand(index = insertIndex, block = dateBlock)
        )
        _activeBlockIndex.value = insertIndex
    }

    fun insertTable(
        rows: Int,
        cols: Int,
        hasBorders: Boolean = true,
        borderColorHex: String = "#CBD5E1",
        tableWidthPercent: Int = 100
    ) {
        val currentDoc = _document.value ?: return
        val insertIndex = _activeBlockIndex.value + 1
        val safeRows = rows.coerceIn(1, 15)
        val safeCols = cols.coerceIn(1, 10)
        val initialCells = List(safeRows) { r ->
            List(safeCols) { c ->
                TableCell(
                    text = "",
                    isBold = r == 0
                )
            }
        }
        val tableBlock = TableBlock(
            rowCount = safeRows,
            columnCount = safeCols,
            cells = initialCells,
            hasBorders = hasBorders,
            borderColorHex = borderColorHex,
            tableWidthPercent = tableWidthPercent
        )

        executeCommand(
            InsertBlockCommand(index = insertIndex, block = tableBlock)
        )
        _activeBlockIndex.value = insertIndex
    }

    fun updateTableCell(blockId: String, row: Int, col: Int, text: String) {
        val currentDoc = _document.value ?: return
        val oldTable = currentDoc.blocks.firstOrNull { it.id == blockId } as? TableBlock ?: return
        val newCells = oldTable.cells.mapIndexed { rIndex, rowList ->
            if (rIndex == row) {
                rowList.mapIndexed { cIndex, cell ->
                    if (cIndex == col) cell.copy(text = text) else cell
                }
            } else rowList
        }
        val newTable = oldTable.copy(cells = newCells)
        executeCommand(
            UpdateTableBlockCommand(
                blockId = blockId,
                oldTable = oldTable,
                newTable = newTable,
                actionDescription = "Edit Cell Text"
            )
        )
    }

    fun updateTableCellFormat(
        blockId: String,
        row: Int,
        col: Int,
        isBold: Boolean? = null,
        isItalic: Boolean? = null,
        backgroundColorHex: String? = null,
        alignment: TextAlignment? = null
    ) {
        val currentDoc = _document.value ?: return
        val oldTable = currentDoc.blocks.firstOrNull { it.id == blockId } as? TableBlock ?: return
        val newCells = oldTable.cells.mapIndexed { rIndex, rowList ->
            if (rIndex == row) {
                rowList.mapIndexed { cIndex, cell ->
                    if (cIndex == col) {
                        cell.copy(
                            isBold = isBold ?: cell.isBold,
                            isItalic = isItalic ?: cell.isItalic,
                            backgroundColorHex = backgroundColorHex ?: cell.backgroundColorHex,
                            alignment = alignment ?: cell.alignment
                        )
                    } else cell
                }
            } else rowList
        }
        val newTable = oldTable.copy(cells = newCells)
        executeCommand(
            UpdateTableBlockCommand(
                blockId = blockId,
                oldTable = oldTable,
                newTable = newTable,
                actionDescription = "Format Cell"
            )
        )
    }

    fun addTableRow(blockId: String, afterRow: Int = -1) {
        val currentDoc = _document.value ?: return
        val oldTable = currentDoc.blocks.firstOrNull { it.id == blockId } as? TableBlock ?: return
        val newRow = List(oldTable.columnCount) { TableCell() }
        val mutableRows = oldTable.cells.toMutableList()
        val insertIndex = if (afterRow in 0 until mutableRows.size) afterRow + 1 else mutableRows.size
        mutableRows.add(insertIndex, newRow)
        val newTable = oldTable.copy(
            rowCount = mutableRows.size,
            cells = mutableRows
        )
        executeCommand(
            UpdateTableBlockCommand(
                blockId = blockId,
                oldTable = oldTable,
                newTable = newTable,
                actionDescription = "Add Table Row"
            )
        )
    }

    fun deleteTableRow(blockId: String, rowIndex: Int) {
        val currentDoc = _document.value ?: return
        val oldTable = currentDoc.blocks.firstOrNull { it.id == blockId } as? TableBlock ?: return
        if (oldTable.rowCount <= 1) return // Keep at least 1 row
        val safeIndex = if (rowIndex in 0 until oldTable.rowCount) rowIndex else oldTable.rowCount - 1
        val mutableRows = oldTable.cells.toMutableList()
        mutableRows.removeAt(safeIndex)
        val newTable = oldTable.copy(
            rowCount = mutableRows.size,
            cells = mutableRows
        )
        executeCommand(
            UpdateTableBlockCommand(
                blockId = blockId,
                oldTable = oldTable,
                newTable = newTable,
                actionDescription = "Delete Table Row"
            )
        )
    }

    fun addTableColumn(blockId: String, afterCol: Int = -1) {
        val currentDoc = _document.value ?: return
        val oldTable = currentDoc.blocks.firstOrNull { it.id == blockId } as? TableBlock ?: return
        val newColumnCount = oldTable.columnCount + 1
        val newCells = oldTable.cells.map { row ->
            val mutableRow = row.toMutableList()
            val insertIndex = if (afterCol in 0 until mutableRow.size) afterCol + 1 else mutableRow.size
            mutableRow.add(insertIndex, TableCell())
            mutableRow.toList()
        }
        val newTable = oldTable.copy(
            columnCount = newColumnCount,
            cells = newCells
        )
        executeCommand(
            UpdateTableBlockCommand(
                blockId = blockId,
                oldTable = oldTable,
                newTable = newTable,
                actionDescription = "Add Table Column"
            )
        )
    }

    fun deleteTableColumn(blockId: String, colIndex: Int) {
        val currentDoc = _document.value ?: return
        val oldTable = currentDoc.blocks.firstOrNull { it.id == blockId } as? TableBlock ?: return
        if (oldTable.columnCount <= 1) return // Keep at least 1 column
        val safeCol = if (colIndex in 0 until oldTable.columnCount) colIndex else oldTable.columnCount - 1
        val newColumnCount = oldTable.columnCount - 1
        val newCells = oldTable.cells.map { row ->
            val mutableRow = row.toMutableList()
            mutableRow.removeAt(safeCol)
            mutableRow.toList()
        }
        val newTable = oldTable.copy(
            columnCount = newColumnCount,
            cells = newCells
        )
        executeCommand(
            UpdateTableBlockCommand(
                blockId = blockId,
                oldTable = oldTable,
                newTable = newTable,
                actionDescription = "Delete Table Column"
            )
        )
    }

    fun updateTableProperties(
        blockId: String,
        hasBorders: Boolean? = null,
        borderColorHex: String? = null,
        tableWidthPercent: Int? = null,
        alignment: TextAlignment? = null
    ) {
        val currentDoc = _document.value ?: return
        val oldTable = currentDoc.blocks.firstOrNull { it.id == blockId } as? TableBlock ?: return
        val newTable = oldTable.copy(
            hasBorders = hasBorders ?: oldTable.hasBorders,
            borderColorHex = borderColorHex ?: oldTable.borderColorHex,
            tableWidthPercent = tableWidthPercent ?: oldTable.tableWidthPercent,
            alignment = alignment ?: oldTable.alignment
        )
        executeCommand(
            UpdateTableBlockCommand(
                blockId = blockId,
                oldTable = oldTable,
                newTable = newTable,
                actionDescription = "Update Table Properties"
            )
        )
    }

    fun setHeaderFooter(
        headerText: String? = null,
        footerText: String? = null,
        showHeader: Boolean? = null,
        showFooter: Boolean? = null
    ) {
        val currentDoc = _document.value ?: return
        val oldLayout = currentDoc.layoutConfig
        val newLayout = oldLayout.copy(
            headerText = headerText ?: oldLayout.headerText,
            footerText = footerText ?: oldLayout.footerText,
            showHeader = showHeader ?: oldLayout.showHeader,
            showFooter = showFooter ?: oldLayout.showFooter
        )
        executeCommand(
            UpdateLayoutCommand(oldLayout = oldLayout, newLayout = newLayout)
        )
    }

    fun setPageNumberPreset(
        formatPattern: String,
        alignment: TextAlignment = TextAlignment.CENTER
    ) {
        val currentDoc = _document.value ?: return
        val oldLayout = currentDoc.layoutConfig
        val newLayout = oldLayout.copy(
            showFooter = true,
            footerText = formatPattern,
            footerAlignment = alignment
        )
        executeCommand(
            UpdateLayoutCommand(oldLayout = oldLayout, newLayout = newLayout)
        )
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

    fun deleteBlockById(blockId: String) {
        val currentDoc = _document.value ?: return
        if (currentDoc.blocks.size <= 1) return
        val index = currentDoc.blocks.indexOfFirst { it.id == blockId }
        if (index == -1) return
        val block = currentDoc.blocks[index]
        executeCommand(
            DeleteBlockCommand(index = index, deletedBlock = block)
        )
        _activeBlockIndex.value = (_activeBlockIndex.value - 1).coerceAtLeast(0)
    }

    // ----------------------------------------------------
    // Layout Operations
    // ----------------------------------------------------

    fun updatePageLayoutConfig(newLayout: PageLayoutConfig) {
        val currentDoc = _document.value ?: return
        val oldLayout = currentDoc.layoutConfig
        executeCommand(
            UpdateLayoutCommand(oldLayout = oldLayout, newLayout = newLayout)
        )
    }

    fun updatePageLayout(
        pageSize: PageSize? = null,
        orientation: PageOrientation? = null,
        marginPreset: MarginPreset? = null,
        headerText: String? = null,
        footerText: String? = null,
        pageBackgroundHex: String? = null,
        borderStyle: com.example.core.layout.PageBorderStyle? = null,
        borderColorHex: String? = null,
        pageNumberFormat: com.example.core.layout.PageNumberFormat? = null
    ) {
        val currentDoc = _document.value ?: return
        val oldLayout = currentDoc.layoutConfig
        val newLayout = oldLayout.copy(
            pageSize = pageSize ?: oldLayout.pageSize,
            orientation = orientation ?: oldLayout.orientation,
            marginPreset = marginPreset ?: oldLayout.marginPreset,
            headerText = headerText ?: oldLayout.headerText,
            footerText = footerText ?: oldLayout.footerText,
            pageBackgroundHex = pageBackgroundHex ?: oldLayout.pageBackgroundHex,
            borderStyle = borderStyle ?: oldLayout.borderStyle,
            borderColorHex = borderColorHex ?: oldLayout.borderColorHex,
            pageNumberFormat = pageNumberFormat ?: oldLayout.pageNumberFormat
        )

        executeCommand(
            UpdateLayoutCommand(oldLayout = oldLayout, newLayout = newLayout)
        )
    }

    // ----------------------------------------------------
    // Save & Recovery & Export Operations
    // ----------------------------------------------------

    fun saveNow() {
        autosaveController.triggerImmediateSave()
    }

    suspend fun flushSave(): Boolean {
        return autosaveController.flushImmediateSave()
    }

    fun onAppBackgrounded() {
        if (autosaveController.isDirty) {
            autosaveController.triggerImmediateSave()
        }
    }

    fun restoreRecoveryDraft() {
        viewModelScope.launch {
            when (val draftRes = repository.loadRecoveryDraft(documentId)) {
                is DocumentResult.Success -> {
                    _document.value = draftRes.data
                    _recoveryAvailable.value = false
                    _statusMessage.value = "Restored recovery draft from previous session"
                    autosaveController.triggerImmediateSave()
                }
                is DocumentResult.Failure -> {
                    _statusMessage.value = "Could not restore recovery draft: ${draftRes.error.message}"
                    _recoveryAvailable.value = false
                }
            }
        }
    }

    fun dismissRecoveryDraft() {
        viewModelScope.launch {
            repository.clearRecoveryDraft(documentId)
            _recoveryAvailable.value = false
            _statusMessage.value = "Dismissed recovery draft"
        }
    }

    fun saveAs(newTitle: String, onComplete: (RichDocument?) -> Unit) {
        val currentDoc = _document.value ?: return
        viewModelScope.launch {
            when (val res = repository.saveDocumentAs(currentDoc, newTitle)) {
                is DocumentResult.Success -> {
                    _statusMessage.value = "Saved copy as '${res.data.title}'"
                    onComplete(res.data)
                }
                is DocumentResult.Failure -> {
                    _statusMessage.value = "Save As failed: ${res.error.message}"
                    onComplete(null)
                }
            }
        }
    }

    fun renameDocument(newTitle: String) {
        val currentDoc = _document.value ?: return
        val trimmed = newTitle.trim().ifBlank { "Untitled Document" }
        val updated = currentDoc.copy(title = trimmed)
        _document.value = updated
        viewModelScope.launch {
            repository.renameDocument(currentDoc.id, trimmed)
            autosaveController.triggerImmediateSave()
        }
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

    fun exportToPlainText(context: Context, onComplete: (File?) -> Unit) {
        val doc = _document.value ?: return
        viewModelScope.launch {
            val exportDir = File(context.cacheDir, "exports").apply { mkdirs() }
            val sanitized = doc.title.replace("[^a-zA-Z0-9.-]".toRegex(), "_")
            val outFile = File(exportDir, "$sanitized.txt")
            when (val res = repository.exportToPlainText(context, doc, outFile)) {
                is DocumentResult.Success -> {
                    _statusMessage.value = "Text Exported: ${outFile.name}"
                    onComplete(outFile)
                }
                is DocumentResult.Failure -> {
                    _statusMessage.value = "Text Export Error: ${res.error.message}"
                    onComplete(null)
                }
            }
        }
    }

    fun exportToDocx(context: Context, onComplete: (File?) -> Unit) {
        val doc = _document.value ?: return
        viewModelScope.launch {
            val exportDir = File(context.cacheDir, "exports").apply { mkdirs() }
            val sanitized = doc.title.replace("[^a-zA-Z0-9.-]".toRegex(), "_")
            val outFile = File(exportDir, "$sanitized.docx")
            when (val res = repository.exportToDocx(context, doc, outFile)) {
                is DocumentResult.Success -> {
                    _statusMessage.value = "Word Document (.docx) Exported: ${outFile.name}"
                    onComplete(outFile)
                }
                is DocumentResult.Failure -> {
                    _statusMessage.value = "DOCX Error: ${res.error.message}"
                    onComplete(null)
                }
            }
        }
    }

    fun exportDocxToSafUri(context: Context, uri: Uri) {
        val doc = _document.value ?: return
        viewModelScope.launch {
            when (val res = repository.exportDocxToUri(context, doc, uri)) {
                is DocumentResult.Success -> {
                    _statusMessage.value = "Word Document (.docx) saved to external storage"
                }
                is DocumentResult.Failure -> {
                    _statusMessage.value = "Save DOCX failed: ${res.error.message}"
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

    fun createPrintDocumentAdapter(context: Context): android.print.PrintDocumentAdapter? {
        val doc = _document.value ?: return null
        return repository.createPrintDocumentAdapter(context, doc)
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
