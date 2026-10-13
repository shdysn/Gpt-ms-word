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

import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import com.example.ui.editor.components.DocumentRevisionSnapshot
import java.security.MessageDigest
import java.util.Locale
import java.util.Date
import java.text.SimpleDateFormat

/**
 * Microsoft Word Ribbon tab categories.
 */
enum class RibbonTab {
    HOME,
    INSERT,
    LAYOUT,
    REVIEW,
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

    data class CopiedFormatting(
        val isBold: Boolean = false,
        val isItalic: Boolean = false,
        val isUnderline: Boolean = false,
        val isStrikethrough: Boolean = false,
        val textColorHex: String? = null,
        val highlightColorHex: String? = null,
        val fontFamilyName: String? = null,
        val fontSizeSp: Float? = null,
        val alignment: TextAlignment = TextAlignment.START,
        val headingStyle: HeadingStyle = HeadingStyle.NORMAL,
        val lineSpacingMultiplier: Float = 1.25f
    )

    private val _isFormatPainterActive = MutableStateFlow(false)
    val isFormatPainterActive: StateFlow<Boolean> = _isFormatPainterActive.asStateFlow()

    private var _copiedFormatting: CopiedFormatting? = null

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

    // Text-To-Speech (TTS) Read Aloud Engine
    private var tts: TextToSpeech? = null
    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking.asStateFlow()

    private val _speakingParagraphIndex = MutableStateFlow<Int?>(null)
    val speakingParagraphIndex: StateFlow<Int?> = _speakingParagraphIndex.asStateFlow()

    private val _speechRate = MutableStateFlow(1.0f)
    val speechRate: StateFlow<Float> = _speechRate.asStateFlow()

    // Canvas Comfort Paper Mode ("WHITE", "SEPIA", "DARK")
    private val _canvasPaperMode = MutableStateFlow("WHITE")
    val canvasPaperMode: StateFlow<String> = _canvasPaperMode.asStateFlow()

    // Document Revision Snapshots History
    private val _revisionSnapshots = MutableStateFlow<List<DocumentRevisionSnapshot>>(emptyList())
    val revisionSnapshots: StateFlow<List<DocumentRevisionSnapshot>> = _revisionSnapshots.asStateFlow()

    // Document Security Lock state
    private val _isDocumentLocked = MutableStateFlow(false)
    val isDocumentLocked: StateFlow<Boolean> = _isDocumentLocked.asStateFlow()

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
                    if (loadedDoc.isPasswordProtected) {
                        _isDocumentLocked.value = true
                    }
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
        if (_isFormatPainterActive.value && index != _activeBlockIndex.value) {
            applyFormatPainterIfActive(index)
        }
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
            if (lines.size == 2) {
                val splitOffset = lines[0].length
                splitParagraph(blockIndex, splitOffset)
                return
            }

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
            undoRedoManager.execute(
                object : com.example.editor.engine.EditorCommand {
                    override val description: String = "Paste / Newlines"
                    override fun apply(document: RichDocument): RichDocument = updatedDoc
                    override fun revert(document: RichDocument): RichDocument = currentDoc
                },
                currentDoc
            )
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
            headingStyle = if (cursor >= block.text.length && block.headingStyle != HeadingStyle.NORMAL) {
                HeadingStyle.NORMAL
            } else if (block.headingStyle == HeadingStyle.TITLE || block.headingStyle == HeadingStyle.SUBTITLE) {
                HeadingStyle.NORMAL
            } else {
                block.headingStyle
            },
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

    fun setLineSpacing(multiplier: Float) {
        val currentDoc = _document.value ?: return
        val targetIdx = _activeBlockIndex.value.coerceIn(0, (currentDoc.blocks.size - 1).coerceAtLeast(0))
        val block = currentDoc.blocks.getOrNull(targetIdx) as? ParagraphBlock ?: return
        val updated = block.copy(lineSpacingMultiplier = multiplier.coerceIn(0.8f, 3.5f))
        val newBlocks = currentDoc.blocks.toMutableList()
        newBlocks[targetIdx] = updated
        val updatedDoc = currentDoc.copy(blocks = newBlocks, updatedAt = System.currentTimeMillis())
        executeCommand(
            object : EditorCommand {
                override val description: String = "Set Line Spacing"
                override fun apply(document: RichDocument): RichDocument = updatedDoc
                override fun revert(document: RichDocument): RichDocument = currentDoc
            }
        )
    }

    fun toggleFormatPainter() {
        if (_isFormatPainterActive.value) {
            _isFormatPainterActive.value = false
            _copiedFormatting = null
            _statusMessage.value = "Format Painter deactivated"
        } else {
            val currentDoc = _document.value ?: return
            val block = currentDoc.blocks.getOrNull(_activeBlockIndex.value) as? ParagraphBlock ?: return
            val currentFormat = activeFormatting.value
            _copiedFormatting = CopiedFormatting(
                isBold = currentFormat.isBold,
                isItalic = currentFormat.isItalic,
                isUnderline = currentFormat.isUnderline,
                isStrikethrough = currentFormat.isStrikethrough,
                textColorHex = currentFormat.textColorHex,
                highlightColorHex = currentFormat.highlightColorHex,
                fontFamilyName = currentFormat.fontFamilyName,
                fontSizeSp = currentFormat.fontSizeSp,
                alignment = block.alignment,
                headingStyle = block.headingStyle,
                lineSpacingMultiplier = block.lineSpacingMultiplier
            )
            _isFormatPainterActive.value = true
            _statusMessage.value = "Format Painter active: Tap any paragraph to apply style"
        }
    }

    fun applyFormatPainterIfActive(targetBlockIndex: Int) {
        if (!_isFormatPainterActive.value) return
        val copied = _copiedFormatting ?: return
        val currentDoc = _document.value ?: return
        val block = currentDoc.blocks.getOrNull(targetBlockIndex) as? ParagraphBlock ?: return

        val newSpans = if (block.text.isNotEmpty()) {
            listOf(
                InlineSpan(
                    start = 0,
                    end = block.text.length,
                    isBold = copied.isBold,
                    isItalic = copied.isItalic,
                    isUnderline = copied.isUnderline,
                    isStrikethrough = copied.isStrikethrough,
                    textColorHex = copied.textColorHex,
                    highlightColorHex = copied.highlightColorHex,
                    fontFamilyName = copied.fontFamilyName,
                    fontSizeSp = copied.fontSizeSp
                )
            )
        } else emptyList()

        val updated = block.copy(
            spans = newSpans,
            alignment = copied.alignment,
            headingStyle = copied.headingStyle,
            lineSpacingMultiplier = copied.lineSpacingMultiplier
        )

        val newBlocks = currentDoc.blocks.toMutableList()
        newBlocks[targetBlockIndex] = updated
        val updatedDoc = currentDoc.copy(blocks = newBlocks, updatedAt = System.currentTimeMillis())

        executeCommand(
            object : EditorCommand {
                override val description: String = "Apply Format Painter"
                override fun apply(document: RichDocument): RichDocument = updatedDoc
                override fun revert(document: RichDocument): RichDocument = currentDoc
            }
        )

        _isFormatPainterActive.value = false
        _copiedFormatting = null
        _statusMessage.value = "Style applied"
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
        aspectRatio: Float? = null,
        wrapMode: String? = null,
        filterMode: String? = null,
        cornerRadiusDp: Int? = null
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
            aspectRatio = newRatio,
            wrapMode = wrapMode ?: oldBlock.wrapMode,
            filterMode = filterMode ?: oldBlock.filterMode,
            cornerRadiusDp = cornerRadiusDp ?: oldBlock.cornerRadiusDp
        )

        executeCommand(
            UpdateImageBlockCommand(
                blockId = blockId,
                oldImage = oldBlock,
                newImage = newBlock,
                actionDescription = "Format Picture Properties"
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
        if (block is com.example.data.model.FootnoteBlock) {
            renumberFootnotes()
        }
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

    // ----------------------------------------------------
    // TTS Read Aloud Controller
    // ----------------------------------------------------

    fun startReadAloud(context: Context) {
        val doc = _document.value ?: return
        val utterances = mutableListOf<String>()
        doc.blocks.forEach { block ->
            when (block) {
                is ParagraphBlock -> {
                    if (block.text.isNotBlank()) utterances.add(block.text)
                }
                is TableBlock -> {
                    val tableText = block.cells.flatten()
                        .map { it.text.trim() }
                        .filter { it.isNotBlank() }
                        .joinToString(", ")
                    if (tableText.isNotBlank()) {
                        utterances.add("Table with ${block.rowCount} rows: $tableText")
                    }
                }
                is com.example.data.model.ShapeBlock -> {
                    val shapeText = buildString {
                        if (block.title.isNotBlank()) append("${block.title}. ")
                        if (block.content.isNotBlank()) append(block.content)
                    }.trim()
                    if (shapeText.isNotBlank()) utterances.add("Note: $shapeText")
                }
                is ImageBlock -> {
                    if (block.caption.isNotBlank()) {
                        utterances.add("Image caption: ${block.caption}")
                    }
                }
                is com.example.data.model.SignatureBlock -> {
                    if (block.signerName.isNotBlank()) {
                        utterances.add("Digitally signed by ${block.signerName}, ${block.signerTitle}")
                    }
                }
                else -> Unit
            }
        }

        if (utterances.isEmpty()) {
            _statusMessage.value = "Document contains no text to read aloud"
            return
        }

        if (tts == null) {
            tts = TextToSpeech(context.applicationContext) { status ->
                if (status == TextToSpeech.SUCCESS) {
                    tts?.language = Locale.getDefault()
                    tts?.setSpeechRate(_speechRate.value)
                    speakUtteranceSequence(utterances)
                } else {
                    _statusMessage.value = "Failed to initialize Text-To-Speech engine"
                }
            }
        } else {
            tts?.setSpeechRate(_speechRate.value)
            speakUtteranceSequence(utterances)
        }
    }

    private fun speakUtteranceSequence(utterances: List<String>) {
        _isSpeaking.value = true
        tts?.stop()

        var uIndex = 0
        val total = utterances.size

        tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {
                _speakingParagraphIndex.value = uIndex
            }

            override fun onDone(utteranceId: String?) {
                uIndex++
                if (uIndex < total) {
                    speakNextUtterance(utterances[uIndex], uIndex)
                } else {
                    _isSpeaking.value = false
                    _speakingParagraphIndex.value = null
                }
            }

            override fun onError(utteranceId: String?) {
                _isSpeaking.value = false
                _speakingParagraphIndex.value = null
            }
        })

        speakNextUtterance(utterances[0], 0)
    }

    private fun speakNextUtterance(text: String, index: Int) {
        val params = android.os.Bundle()
        params.putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "u_$index")
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, params, "u_$index")
    }

    fun stopReadAloud() {
        tts?.stop()
        _isSpeaking.value = false
        _speakingParagraphIndex.value = null
    }

    fun setTtsSpeechRate(rate: Float) {
        _speechRate.value = rate.coerceIn(0.5f, 2.5f)
        tts?.setSpeechRate(_speechRate.value)
    }

    // ----------------------------------------------------
    // Canvas Comfort Paper Mode ("WHITE", "SEPIA", "DARK")
    // ----------------------------------------------------

    fun setCanvasPaperMode(mode: String) {
        _canvasPaperMode.value = mode
        val colorHex = when (mode) {
            "SEPIA" -> "#FBF0D9"
            "DARK" -> "#18181B"
            else -> "#FFFFFF"
        }
        val currentLayout = _document.value?.layoutConfig ?: PageLayoutConfig()
        executeCommand(UpdateLayoutCommand(currentLayout, currentLayout.copy(pageBackgroundHex = colorHex)))
    }

    // ----------------------------------------------------
    // Version History & Snapshots
    // ----------------------------------------------------

    fun createNamedSnapshot(label: String = "Manual Snapshot") {
        val doc = _document.value ?: return
        val snapshot = DocumentRevisionSnapshot(
            id = java.util.UUID.randomUUID().toString(),
            timestamp = System.currentTimeMillis(),
            label = label.ifBlank { "Snapshot" },
            wordCount = doc.wordCount,
            characterCount = doc.characterCount,
            snippet = doc.plainTextSnippet,
            snapshotDoc = doc.copy()
        )
        _revisionSnapshots.value = listOf(snapshot) + _revisionSnapshots.value.take(9)
        _statusMessage.value = "Revision snapshot '$label' saved"
    }

    fun restoreRevisionSnapshot(snapshotId: String) {
        val snap = _revisionSnapshots.value.firstOrNull { it.id == snapshotId } ?: return
        snap.snapshotDoc?.let { restoredDoc ->
            _document.value = restoredDoc.copy(updatedAt = System.currentTimeMillis())
            scheduleDocumentAutosave()
        }
        _statusMessage.value = "Restored snapshot from ${java.text.SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(snap.timestamp))}"
    }

    fun cycleSpeechRate() {
        val nextRate = when (_speechRate.value) {
            0.75f -> 1.0f
            1.0f -> 1.25f
            1.25f -> 1.5f
            1.5f -> 2.0f
            else -> 0.75f
        }
        setTtsSpeechRate(nextRate)
    }

    fun toggleRuler() {
        viewModelScope.launch {
            preferencesManager.setShowRuler(!showRuler.value)
        }
    }

    fun toggleWordCount() {
        viewModelScope.launch {
            preferencesManager.setShowWordCount(!showWordCount.value)
        }
    }

    // ----------------------------------------------------
    // Watermark & Layout Columns
    // ----------------------------------------------------

    fun setWatermark(text: String, colorHex: String = "#2594A3B8", angleDeg: Float = -45f) {
        val currentLayout = _document.value?.layoutConfig ?: PageLayoutConfig()
        val newLayout = currentLayout.copy(
            watermarkText = text,
            watermarkColorHex = colorHex,
            watermarkAngleDeg = angleDeg
        )
        executeCommand(UpdateLayoutCommand(currentLayout, newLayout))
        _statusMessage.value = if (text.isNotBlank()) "Watermark set to '$text'" else "Watermark removed"
    }

    fun removeWatermark() {
        setWatermark("")
    }

    fun setColumnCount(columns: Int) {
        val currentLayout = _document.value?.layoutConfig ?: PageLayoutConfig()
        val cols = columns.coerceIn(1, 2)
        executeCommand(UpdateLayoutCommand(currentLayout, currentLayout.copy(columnCount = cols)))
        _statusMessage.value = if (cols == 2) "Two-Column layout applied" else "Single-Column layout applied"
    }

    // ----------------------------------------------------
    // Document Password Lock (Security)
    // ----------------------------------------------------

    private fun hashPassword(password: String): String {
        val md = MessageDigest.getInstance("SHA-256")
        val bytes = md.digest(password.toByteArray(Charsets.UTF_8))
        return bytes.joinToString("") { "%02x".format(it) }
    }

    fun setDocumentPassword(password: String) {
        val doc = _document.value ?: return
        val hashed = hashPassword(password)
        _document.value = doc.copy(passwordHash = hashed)
        _isDocumentLocked.value = false
        scheduleDocumentAutosave()
        _statusMessage.value = "Document locked with password PIN"
    }

    fun removeDocumentPassword() {
        val doc = _document.value ?: return
        _document.value = doc.copy(passwordHash = null)
        _isDocumentLocked.value = false
        scheduleDocumentAutosave()
        _statusMessage.value = "Password protection removed"
    }

    fun verifyPassword(input: String): Boolean {
        val doc = _document.value ?: return true
        val target = doc.passwordHash ?: return true
        return hashPassword(input) == target
    }

    fun unlockDocument(password: String): Boolean {
        val verified = verifyPassword(password)
        if (verified) {
            _isDocumentLocked.value = false
            _statusMessage.value = "Document unlocked"
        } else {
            _statusMessage.value = "Incorrect PIN or Password"
        }
        return verified
    }

    // ----------------------------------------------------
    // Voice Typing (Speech-to-Text) Append
    // ----------------------------------------------------

    fun appendVoiceTypingText(spokenText: String) {
        if (spokenText.isBlank()) return
        val doc = _document.value ?: return
        val activeIndex = _activeBlockIndex.value.coerceIn(0, (doc.blocks.size - 1).coerceAtLeast(0))
        val currentBlock = doc.blocks.getOrNull(activeIndex) as? ParagraphBlock
        if (currentBlock != null) {
            val space = if (currentBlock.text.isNotBlank() && !currentBlock.text.endsWith(" ")) " " else ""
            val newText = currentBlock.text + space + spokenText
            updateParagraphText(activeIndex, newText, androidx.compose.ui.text.TextRange(newText.length))
            _statusMessage.value = "Dictation added: $spokenText"
        }
    }

    // ----------------------------------------------------
    // Insert TOC, Signature, and Shape
    // ----------------------------------------------------

    fun insertTableOfContents() {
        val insertIndex = _activeBlockIndex.value.coerceIn(0, (_document.value?.blocks?.size ?: 0))
        executeCommand(InsertBlockCommand(insertIndex, com.example.data.model.TableOfContentsBlock()))
        _statusMessage.value = "Table of Contents inserted"
    }

    fun insertSignature(signerName: String, signerTitle: String, strokeHex: String, points: String) {
        val insertIndex = (_activeBlockIndex.value + 1).coerceIn(0, (_document.value?.blocks?.size ?: 0))
        val dateStr = SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(Date())
        val sigBlock = com.example.data.model.SignatureBlock(
            svgPoints = points,
            signerName = signerName,
            signerTitle = signerTitle,
            signedDate = dateStr,
            strokeColorHex = strokeHex
        )
        executeCommand(InsertBlockCommand(insertIndex, sigBlock))
        _statusMessage.value = "Digital Signature inserted"
    }

    fun insertShape(shapeType: String, title: String, content: String, bgHex: String, borderHex: String) {
        val insertIndex = (_activeBlockIndex.value + 1).coerceIn(0, (_document.value?.blocks?.size ?: 0))
        val shapeBlock = com.example.data.model.ShapeBlock(
            shapeType = shapeType,
            title = title,
            content = content,
            backgroundColorHex = bgHex,
            borderColorHex = borderHex
        )
        executeCommand(InsertBlockCommand(insertIndex, shapeBlock))
        _statusMessage.value = "Shape / Callout Box inserted"
    }

    // ----------------------------------------------------
    // Table and Image Advanced Formatting
    // ----------------------------------------------------

    fun updateImageFormatting(
        blockId: String,
        filterMode: String,
        cornerRadiusDp: Int,
        borderThicknessDp: Int,
        borderColorHex: String,
        rotationDegrees: Float
    ) {
        val doc = _document.value ?: return
        val currentImg = doc.blocks.filterIsInstance<ImageBlock>().firstOrNull { it.id == blockId } ?: return
        val updatedImg = currentImg.copy(
            filterMode = filterMode,
            cornerRadiusDp = cornerRadiusDp,
            borderThicknessDp = borderThicknessDp,
            borderColorHex = borderColorHex,
            rotationDegrees = rotationDegrees
        )
        executeCommand(UpdateImageBlockCommand(currentImg.id, currentImg, updatedImg))
        _statusMessage.value = "Image properties updated"
    }

    fun toggleTableHeaderRow(tableId: String) {
        val doc = _document.value ?: return
        val currentTable = doc.blocks.filterIsInstance<TableBlock>().firstOrNull { it.id == tableId } ?: return
        val updated = currentTable.copy(hasHeaderRow = !currentTable.hasHeaderRow)
        executeCommand(UpdateTableBlockCommand(currentTable.id, currentTable, updated))
    }

    fun toggleTableZebraStriping(tableId: String) {
        val doc = _document.value ?: return
        val currentTable = doc.blocks.filterIsInstance<TableBlock>().firstOrNull { it.id == tableId } ?: return
        val updated = currentTable.copy(isZebraStriped = !currentTable.isZebraStriped)
        executeCommand(UpdateTableBlockCommand(currentTable.id, currentTable, updated))
    }

    fun setTableCellBackground(tableId: String, row: Int, col: Int, bgHex: String?) {
        val doc = _document.value ?: return
        val currentTable = doc.blocks.filterIsInstance<TableBlock>().firstOrNull { it.id == tableId } ?: return
        val updatedCells = currentTable.cells.mapIndexed { r, rList ->
            rList.mapIndexed { c, cell ->
                if (r == row && c == col) cell.copy(backgroundColorHex = bgHex) else cell
            }
        }
        val updated = currentTable.copy(cells = updatedCells)
        executeCommand(UpdateTableBlockCommand(currentTable.id, currentTable, updated))
    }

    // ----------------------------------------------------
    // Review & Tracking Operations
    // ----------------------------------------------------

    fun addComment(blockId: String, author: String, text: String) {
        val doc = _document.value ?: return
        val newComment = com.example.data.model.DocumentComment(
            blockId = blockId,
            author = author.ifBlank { "Reviewer" },
            text = text
        )
        val updated = doc.copy(comments = doc.comments + newComment)
        _document.value = updated
        _statusMessage.value = "Comment added"
    }

    fun resolveComment(commentId: String) {
        val doc = _document.value ?: return
        val updatedComments = doc.comments.map {
            if (it.id == commentId) it.copy(isResolved = !it.isResolved) else it
        }
        _document.value = doc.copy(comments = updatedComments)
    }

    fun deleteComment(commentId: String) {
        val doc = _document.value ?: return
        _document.value = doc.copy(comments = doc.comments.filterNot { it.id == commentId })
        _statusMessage.value = "Comment removed"
    }

    fun toggleTrackChanges(enabled: Boolean? = null) {
        val doc = _document.value ?: return
        val newEnabled = enabled ?: !doc.isTrackChangesEnabled
        _document.value = doc.copy(isTrackChangesEnabled = newEnabled)
        _statusMessage.value = if (newEnabled) "Track Changes enabled" else "Track Changes disabled"
    }

    fun acceptChange(changeId: String) {
        val doc = _document.value ?: return
        _document.value = doc.copy(trackedChanges = doc.trackedChanges.filterNot { it.id == changeId })
        _statusMessage.value = "Change accepted"
    }

    fun rejectChange(changeId: String) {
        val doc = _document.value ?: return
        val change = doc.trackedChanges.firstOrNull { it.id == changeId } ?: return
        val updatedBlocks = doc.blocks.map { b ->
            if (b.id == change.blockId && b is ParagraphBlock) {
                b.copy(text = change.originalText)
            } else b
        }
        _document.value = doc.copy(
            blocks = updatedBlocks,
            trackedChanges = doc.trackedChanges.filterNot { it.id == changeId }
        )
        _statusMessage.value = "Change rejected"
    }

    fun acceptAllChanges() {
        val doc = _document.value ?: return
        _document.value = doc.copy(trackedChanges = emptyList())
        _statusMessage.value = "All changes accepted"
    }

    fun rejectAllChanges() {
        val doc = _document.value ?: return
        var currentBlocks = doc.blocks
        for (change in doc.trackedChanges) {
            currentBlocks = currentBlocks.map { b ->
                if (b.id == change.blockId && b is ParagraphBlock) {
                    b.copy(text = change.originalText)
                } else b
            }
        }
        _document.value = doc.copy(blocks = currentBlocks, trackedChanges = emptyList())
        _statusMessage.value = "All changes rejected"
    }

    // ----------------------------------------------------
    // Footnote, Page Border & Color Operations
    // ----------------------------------------------------

    fun insertFootnote(referenceText: String, noteContent: String) {
        val doc = _document.value ?: return
        val currentNotes = doc.blocks.filterIsInstance<com.example.data.model.FootnoteBlock>()
        val nextNoteNumber = currentNotes.size + 1
        val activeIndex = _activeBlockIndex.value
        val activeBlock = doc.blocks.getOrNull(activeIndex)

        // Automatically append or link superscript marker to active paragraph text
        if (activeBlock is ParagraphBlock) {
            val superscriptMark = when (nextNoteNumber) {
                1 -> "¹"
                2 -> "²"
                3 -> "³"
                4 -> "⁴"
                5 -> "⁵"
                else -> "[$nextNoteNumber]"
            }
            val updatedText = if (referenceText.isNotBlank() && activeBlock.text.contains(referenceText)) {
                activeBlock.text.replaceFirst(referenceText, "$referenceText$superscriptMark")
            } else {
                if (activeBlock.text.isBlank()) superscriptMark else "${activeBlock.text} $superscriptMark"
            }
            updateParagraphText(activeIndex, updatedText)
        }

        val insertIndex = (activeIndex + 1).coerceIn(0, doc.blocks.size)
        val footnote = com.example.data.model.FootnoteBlock(
            noteNumber = nextNoteNumber,
            referenceText = referenceText.ifBlank { "Citation $nextNoteNumber" },
            noteContent = noteContent
        )
        executeCommand(InsertBlockCommand(insertIndex, footnote))
        renumberFootnotes()
        _statusMessage.value = "Footnote [$nextNoteNumber] linked & inserted"
    }

    fun renumberFootnotes() {
        val doc = _document.value ?: return
        var counter = 1
        val updatedBlocks = doc.blocks.map { block ->
            if (block is com.example.data.model.FootnoteBlock) {
                block.copy(noteNumber = counter++)
            } else block
        }
        _document.value = doc.copy(blocks = updatedBlocks)
    }

    fun refreshTableOfContents() {
        val doc = _document.value ?: return
        val hasToc = doc.blocks.any { it is com.example.data.model.TableOfContentsBlock }
        if (hasToc) {
            // Trigger state change so TOC recombines live heading data
            _document.value = doc.copy(updatedAt = System.currentTimeMillis())
            _statusMessage.value = "Table of Contents synchronized"
        }
    }

    fun setPageBorder(style: com.example.core.layout.PageBorderStyle, colorHex: String) {
        val doc = _document.value ?: return
        val newLayout = doc.layoutConfig.copy(borderStyle = style, borderColorHex = colorHex)
        updatePageLayoutConfig(newLayout)
        _statusMessage.value = "Page border updated"
    }

    fun setPageBackgroundColor(bgHex: String) {
        val doc = _document.value ?: return
        val newLayout = doc.layoutConfig.copy(pageBackgroundHex = bgHex)
        updatePageLayoutConfig(newLayout)
        _statusMessage.value = "Page background color updated"
    }

    // ----------------------------------------------------
    // AI Smart Writing Assistant Operations
    // ----------------------------------------------------

    fun applyAiRewrite(paragraphId: String, newText: String) {
        val doc = _document.value ?: return
        val blockIndex = doc.blocks.indexOfFirst { it.id == paragraphId }
        if (blockIndex == -1) return
        val oldBlock = doc.blocks[blockIndex] as? ParagraphBlock ?: return
        if (doc.isTrackChangesEnabled) {
            val record = com.example.data.model.TrackedChange(
                blockId = paragraphId,
                originalText = oldBlock.text,
                updatedText = newText,
                author = "AI Assistant",
                changeType = "POLISH"
            )
            _document.value = doc.copy(trackedChanges = doc.trackedChanges + record)
        }
        updateParagraphText(blockIndex, newText)
        _statusMessage.value = "Paragraph polished"
    }

    fun insertAiSummary(summaryText: String) {
        val insertIndex = (_activeBlockIndex.value + 1).coerceIn(0, (_document.value?.blocks?.size ?: 0))
        val summaryBlock = com.example.data.model.ShapeBlock(
            shapeType = "NOTE",
            title = "Executive Summary",
            content = summaryText,
            backgroundColorHex = "#EFF6FF",
            borderColorHex = "#3B82F6"
        )
        executeCommand(InsertBlockCommand(insertIndex, summaryBlock))
        _statusMessage.value = "Summary box inserted"
    }

    override fun onCleared() {
        super.onCleared()
        tts?.stop()
        tts?.shutdown()
        tts = null
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
