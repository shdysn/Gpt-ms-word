package com.example.ui.editor.viewmodel

import androidx.compose.ui.text.TextRange
import com.example.data.model.HeadingStyle
import com.example.data.model.ParagraphBlock
import com.example.data.model.RichDocument
import com.example.data.model.TextAlignment
import com.example.editor.engine.ActiveFormatting
import com.example.editor.engine.EditorCommand
import com.example.editor.engine.SpanOperations
import com.example.ui.editor.EditorViewModel
import java.util.UUID

/**
 * Text canvas line reconciler, diff calculation, and typing checkpoint controller.
 */
class EditorTextCanvasReconciler(
    private val vm: EditorViewModel
) {
    private var typingBaseDoc: RichDocument? = null
    private var lastTypingTimestamp: Long = 0L

    fun calculateEditDelta(oldText: String, newText: String): Triple<Int, Int, Int> {
        var prefix = 0
        while (prefix < oldText.length && prefix < newText.length && oldText[prefix] == newText[prefix]) {
            prefix++
        }
        var suffixOld = oldText.length
        var suffixNew = newText.length
        while (suffixOld > prefix && suffixNew > prefix && oldText[suffixOld - 1] == newText[suffixNew - 1]) {
            suffixOld--
            suffixNew--
        }
        val deletedLength = suffixOld - prefix
        val insertedLength = suffixNew - prefix
        return Triple(prefix, deletedLength, insertedLength)
    }

    fun registerTypingUndoCheckpoint(currentDoc: RichDocument, newDoc: RichDocument, newText: String) {
        val now = System.currentTimeMillis()
        if (typingBaseDoc == null) {
            typingBaseDoc = currentDoc
        }
        val isWordBoundary = newText.endsWith(" ") || newText.endsWith("\n") || (now - lastTypingTimestamp > 700L)
        lastTypingTimestamp = now
        if (isWordBoundary && typingBaseDoc != null && typingBaseDoc != newDoc) {
            val base = typingBaseDoc!!
            vm.historyManager.undoRedoManager.execute(
                object : EditorCommand {
                    override val description: String = "Typing"
                    override fun apply(document: RichDocument): RichDocument = newDoc
                    override fun revert(document: RichDocument): RichDocument = base
                },
                base
            )
            typingBaseDoc = newDoc
            vm.historyManager.updateUndoRedoStates()
        }
    }

    fun clearTypingBaseDoc() {
        typingBaseDoc = null
    }

    fun reconcileParagraphBlocks(
        oldParagraphs: List<ParagraphBlock>,
        newLines: List<String>,
        editOffset: Int,
        deletedLen: Int,
        insertedLen: Int,
        pendingFormatting: ActiveFormatting
    ): List<ParagraphBlock> {
        return newLines.mapIndexed { idx, lineText ->
            val oldBlock = oldParagraphs.getOrNull(idx)
            if (oldBlock != null) {
                val shiftedSpans = SpanOperations.shiftSpansOnTextChange(
                    spans = oldBlock.spans,
                    editOffset = editOffset,
                    deletedLength = deletedLen,
                    insertedLength = insertedLen,
                    pendingFormatting = if (insertedLen > 0) pendingFormatting else null
                ).mapNotNull {
                    if (it.start < lineText.length) it.copy(end = it.end.coerceAtMost(lineText.length)) else null
                }
                oldBlock.copy(text = lineText, spans = shiftedSpans)
            } else {
                val template = oldParagraphs.lastOrNull()
                ParagraphBlock(
                    id = UUID.randomUUID().toString(),
                    text = lineText,
                    alignment = template?.alignment ?: TextAlignment.START,
                    isRtl = template?.isRtl ?: false,
                    headingStyle = HeadingStyle.NORMAL,
                    lineSpacingMultiplier = template?.lineSpacingMultiplier ?: 1.25f
                )
            }
        }
    }

    fun deleteOrMergeWithPrevious(blockIndex: Int) {
        val currentDoc = vm.currentDocument ?: return
        if (blockIndex <= 0) return
        val currentBlock = currentDoc.blocks.getOrNull(blockIndex) as? ParagraphBlock ?: return
        val prevBlock = currentDoc.blocks.getOrNull(blockIndex - 1) as? ParagraphBlock ?: return

        val prevLen = prevBlock.text.length
        val mergedText = prevBlock.text + currentBlock.text
        val shiftedCurrentSpans = currentBlock.spans.map { it.copy(start = it.start + prevLen, end = it.end + prevLen) }
        val mergedSpans = prevBlock.spans + shiftedCurrentSpans

        val updatedPrev = prevBlock.copy(text = mergedText, spans = mergedSpans)
        val newBlocks = currentDoc.blocks.toMutableList()
        newBlocks[blockIndex - 1] = updatedPrev
        newBlocks.removeAt(blockIndex)

        val updatedDoc = currentDoc.copy(blocks = newBlocks, updatedAt = System.currentTimeMillis())
        vm.executeCommand(
            object : EditorCommand {
                override val description: String = "Merge Paragraphs"
                override fun apply(document: RichDocument): RichDocument = updatedDoc
                override fun revert(document: RichDocument): RichDocument = currentDoc
            }
        )
        vm.setActiveBlock(blockIndex - 1)
        vm.updateSelection(TextRange(prevLen))
    }
}
