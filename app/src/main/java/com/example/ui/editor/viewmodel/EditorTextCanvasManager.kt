package com.example.ui.editor.viewmodel

import androidx.compose.ui.text.TextRange
import com.example.core.typography.BidiUtils
import com.example.data.model.HeadingStyle
import com.example.data.model.ListType
import com.example.data.model.ParagraphBlock
import com.example.data.model.RichDocument
import com.example.data.model.TextAlignment
import com.example.editor.engine.EditorCommand
import com.example.editor.engine.SpanOperations
import com.example.editor.engine.UpdateParagraphTextAndSpansCommand
import com.example.ui.editor.EditorViewModel
import java.util.UUID

/**
 * Manages text typing, continuous canvas updates, and paragraph splitting.
 */
class EditorTextCanvasManager(
    private val vm: EditorViewModel
) {
    internal val reconciler = EditorTextCanvasReconciler(vm)

    fun calculateEditDelta(oldText: String, newText: String) = reconciler.calculateEditDelta(oldText, newText)
    fun deleteOrMergeWithPrevious(blockIndex: Int) = reconciler.deleteOrMergeWithPrevious(blockIndex)

    fun splitParagraph(blockIndex: Int, cursorOffset: Int) {
        val currentDoc = vm.currentDocument ?: return
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

        val updatedCurrent = block.copy(text = text1, spans = spans1)
        val newBlock = ParagraphBlock(
            id = UUID.randomUUID().toString(),
            text = text2,
            spans = spans2,
            alignment = block.alignment,
            isRtl = block.isRtl || BidiUtils.detectParagraphIsRtl(text2),
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
        vm.historyManager.undoRedoManager.execute(
            object : EditorCommand {
                override val description: String = "Split Paragraph"
                override fun apply(document: RichDocument): RichDocument = updatedDoc
                override fun revert(document: RichDocument): RichDocument = currentDoc
            },
            currentDoc
        )
        vm.updateDocumentInternal(updatedDoc)
        vm.setActiveBlock(blockIndex + 1)
        vm.updateSelection(TextRange.Zero)
        vm.historyManager.scheduleDocumentAutosave()
    }

    fun updateParagraphText(
        blockIndex: Int,
        newText: String,
        newSelection: TextRange = TextRange(newText.length)
    ) {
        val currentDoc = vm.currentDocument ?: return
        val block = currentDoc.blocks.getOrNull(blockIndex) as? ParagraphBlock ?: return
        val oldText = block.text
        if (oldText == newText) {
            vm.updateSelection(newSelection)
            return
        }

        if (newText.contains("\n")) {
            val lines = newText.split("\n")
            if (lines.size == 2) {
                splitParagraph(blockIndex, lines[0].length)
                return
            }

            val firstLine = lines.first()
            val (editOffset, deletedLen, insertedLen) = reconciler.calculateEditDelta(oldText, firstLine)
            val shiftedSpans = SpanOperations.shiftSpansOnTextChange(
                spans = block.spans,
                editOffset = editOffset,
                deletedLength = deletedLen,
                insertedLength = insertedLen,
                pendingFormatting = if (insertedLen > 0) vm.pendingFormatting.value else null
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
                    id = UUID.randomUUID().toString(),
                    text = lineText,
                    alignment = block.alignment,
                    isRtl = block.isRtl || BidiUtils.detectParagraphIsRtl(lineText),
                    headingStyle = if (block.headingStyle == HeadingStyle.TITLE || block.headingStyle == HeadingStyle.SUBTITLE) HeadingStyle.NORMAL else block.headingStyle,
                    listType = if (block.listType == ListType.NUMBERED) ListType.NUMBERED else ListType.NONE,
                    lineSpacingMultiplier = block.lineSpacingMultiplier
                )
                newBlocks.add(blockIndex + i, newBlock)
            }

            val updatedDoc = currentDoc.copy(blocks = newBlocks, updatedAt = System.currentTimeMillis())
            vm.historyManager.undoRedoManager.execute(
                object : EditorCommand {
                    override val description: String = "Paste / Newlines"
                    override fun apply(document: RichDocument): RichDocument = updatedDoc
                    override fun revert(document: RichDocument): RichDocument = currentDoc
                },
                currentDoc
            )
            vm.updateDocumentInternal(updatedDoc)
            vm.setActiveBlock(blockIndex + lines.size - 1)
            vm.updateSelection(TextRange(lines.last().length))
            vm.historyManager.scheduleDocumentAutosave()
            return
        }

        val (editOffset, deletedLen, insertedLen) = reconciler.calculateEditDelta(oldText, newText)
        val newSpans = SpanOperations.shiftSpansOnTextChange(
            spans = block.spans,
            editOffset = editOffset,
            deletedLength = deletedLen,
            insertedLength = insertedLen,
            pendingFormatting = if (insertedLen > 0) vm.pendingFormatting.value else null
        )
        val autoRtl = if (oldText.isBlank() && !block.isRtl && BidiUtils.detectParagraphIsRtl(newText)) true else null

        vm.executeCommand(
            UpdateParagraphTextAndSpansCommand(
                blockId = block.id,
                oldText = oldText,
                newText = newText,
                oldSpans = block.spans,
                newSpans = newSpans,
                wasRtl = block.isRtl,
                isRtl = autoRtl
            )
        )
        vm.updateSelection(newSelection)
    }

    fun updateParagraphSubSliceText(
        blockIndex: Int,
        startChar: Int,
        endChar: Int,
        newSliceText: String,
        sliceSelection: TextRange
    ) {
        val currentDoc = vm.currentDocument ?: return
        val block = currentDoc.blocks.getOrNull(blockIndex) as? ParagraphBlock ?: return
        val safeStart = startChar.coerceIn(0, block.text.length)
        val safeEnd = endChar.coerceIn(safeStart, block.text.length)
        if (safeStart == 0 && safeEnd == block.text.length) {
            updateParagraphText(blockIndex, newSliceText, sliceSelection)
            return
        }
        val fullNewText = block.text.substring(0, safeStart) + newSliceText + block.text.substring(safeEnd)
        val fullSelection = TextRange(
            (safeStart + sliceSelection.start).coerceIn(0, fullNewText.length),
            (safeStart + sliceSelection.end).coerceIn(0, fullNewText.length)
        )
        updateParagraphText(blockIndex, fullNewText, fullSelection)
    }

    fun updateUnifiedDocumentText(
        newFullText: String,
        newSelection: TextRange = TextRange(newFullText.length)
    ) {
        val currentDoc = vm.currentDocument ?: return

        if (newFullText.isEmpty()) {
            val emptyBlock = ParagraphBlock(
                text = "",
                spans = emptyList(),
                alignment = if (vm.defaultRtl.value) TextAlignment.END else TextAlignment.START,
                isRtl = vm.defaultRtl.value
            )
            val updatedDoc = currentDoc.copy(blocks = listOf(emptyBlock), updatedAt = System.currentTimeMillis())
            vm.historyManager.undoRedoManager.execute(
                object : EditorCommand {
                    override val description: String = "Clear All Text"
                    override fun apply(document: RichDocument): RichDocument = updatedDoc
                    override fun revert(document: RichDocument): RichDocument = currentDoc
                },
                currentDoc
            )
            reconciler.clearTypingBaseDoc()
            vm.updateDocumentInternal(updatedDoc)
            vm.setActiveBlock(0)
            vm.updateSelection(TextRange.Zero)
            vm.historyManager.updateUndoRedoStates()
            vm.historyManager.scheduleDocumentAutosave()
            return
        }

        val oldParagraphs = currentDoc.blocks.filterIsInstance<ParagraphBlock>()
        val nonParagraphBlocks = currentDoc.blocks.filter { it !is ParagraphBlock }
        val oldFullText = oldParagraphs.joinToString("\n") { it.text }
        if (oldFullText == newFullText && oldParagraphs.isNotEmpty()) {
            vm.updateSelection(newSelection)
            return
        }

        val (editOffset, deletedLen, insertedLen) = reconciler.calculateEditDelta(oldFullText, newFullText)
        val newLines = newFullText.split("\n")

        val newParagraphs = reconciler.reconcileParagraphBlocks(
            oldParagraphs = oldParagraphs,
            newLines = newLines,
            editOffset = editOffset,
            deletedLen = deletedLen,
            insertedLen = insertedLen,
            pendingFormatting = vm.pendingFormatting.value
        )

        val newBlocks = newParagraphs + nonParagraphBlocks
        val updatedDoc = currentDoc.copy(blocks = newBlocks, updatedAt = System.currentTimeMillis())
        reconciler.registerTypingUndoCheckpoint(currentDoc, updatedDoc, newFullText)
        vm.updateDocumentInternal(updatedDoc)
        vm.updateSelection(newSelection)
        vm.historyManager.scheduleDocumentAutosave()
    }

    fun appendVoiceTypingText(spokenText: String) {
        if (spokenText.isBlank()) return
        val doc = vm.currentDocument ?: return
        val activeIndex = vm.activeBlockIndex.value.coerceIn(0, (doc.blocks.size - 1).coerceAtLeast(0))
        val currentBlock = doc.blocks.getOrNull(activeIndex) as? ParagraphBlock
        if (currentBlock != null) {
            val space = if (currentBlock.text.isNotBlank() && !currentBlock.text.endsWith(" ")) " " else ""
            val newText = currentBlock.text + space + spokenText
            updateParagraphText(activeIndex, newText, TextRange(newText.length))
            vm.showStatusMessage("Dictation added: $spokenText")
        }
    }
}
