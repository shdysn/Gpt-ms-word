package com.example.ui.editor.viewmodel

import com.example.data.model.DocumentComment
import com.example.data.model.ParagraphBlock
import com.example.data.model.TrackedChange
import com.example.ui.editor.EditorViewModel
import java.util.UUID

/**
 * Manages document reviews: collaborative comments and revision change tracking.
 */
class EditorReviewManager(
    private val vm: EditorViewModel
) {
    fun addComment(blockId: String, author: String, text: String) {
        val currentDoc = vm.currentDocument ?: return
        val comment = DocumentComment(
            id = UUID.randomUUID().toString(),
            blockId = blockId,
            author = author.ifBlank { "Author" },
            text = text,
            timestamp = System.currentTimeMillis()
        )
        vm.updateDocumentInternal(currentDoc.copy(comments = currentDoc.comments + comment))
        vm.historyManager.scheduleDocumentAutosave()
        vm.showStatusMessage("Comment added")
    }

    fun resolveComment(commentId: String) {
        val currentDoc = vm.currentDocument ?: return
        val updated = currentDoc.comments.map {
            if (it.id == commentId) it.copy(isResolved = true) else it
        }
        vm.updateDocumentInternal(currentDoc.copy(comments = updated))
        vm.historyManager.scheduleDocumentAutosave()
        vm.showStatusMessage("Comment resolved")
    }

    fun deleteComment(commentId: String) {
        val currentDoc = vm.currentDocument ?: return
        val updated = currentDoc.comments.filter { it.id != commentId }
        vm.updateDocumentInternal(currentDoc.copy(comments = updated))
        vm.historyManager.scheduleDocumentAutosave()
        vm.showStatusMessage("Comment deleted")
    }

    fun toggleTrackChanges(enabled: Boolean? = null) {
        val currentDoc = vm.currentDocument ?: return
        val newState = enabled ?: !currentDoc.isTrackChangesEnabled
        vm.updateDocumentInternal(currentDoc.copy(isTrackChangesEnabled = newState))
        vm.historyManager.scheduleDocumentAutosave()
        vm.showStatusMessage(if (newState) "Track Changes enabled" else "Track Changes paused")
    }

    fun acceptChange(changeId: String) {
        val currentDoc = vm.currentDocument ?: return
        val change = currentDoc.trackedChanges.find { it.id == changeId } ?: return
        val remaining = currentDoc.trackedChanges.filter { it.id != changeId }
        val updatedBlocks = currentDoc.blocks.map { block ->
            if (block.id == change.blockId && block is ParagraphBlock) {
                block.copy(text = change.updatedText)
            } else block
        }
        vm.updateDocumentInternal(currentDoc.copy(blocks = updatedBlocks, trackedChanges = remaining))
        vm.historyManager.scheduleDocumentAutosave()
        vm.showStatusMessage("Change accepted")
    }

    fun rejectChange(changeId: String) {
        val currentDoc = vm.currentDocument ?: return
        val change = currentDoc.trackedChanges.find { it.id == changeId } ?: return
        val remaining = currentDoc.trackedChanges.filter { it.id != changeId }
        val updatedBlocks = currentDoc.blocks.map { block ->
            if (block.id == change.blockId && block is ParagraphBlock) {
                block.copy(text = change.originalText)
            } else block
        }
        vm.updateDocumentInternal(currentDoc.copy(blocks = updatedBlocks, trackedChanges = remaining))
        vm.historyManager.scheduleDocumentAutosave()
        vm.showStatusMessage("Change rejected")
    }

    fun acceptAllChanges() {
        val currentDoc = vm.currentDocument ?: return
        vm.updateDocumentInternal(currentDoc.copy(trackedChanges = emptyList()))
        vm.historyManager.scheduleDocumentAutosave()
        vm.showStatusMessage("All changes accepted")
    }

    fun rejectAllChanges() {
        val currentDoc = vm.currentDocument ?: return
        var doc = currentDoc
        currentDoc.trackedChanges.reversed().forEach { change ->
            val block = doc.blocks.find { it.id == change.blockId } as? ParagraphBlock
            if (block != null) {
                val updatedBlocks = doc.blocks.map { if (it.id == block.id) block.copy(text = change.originalText) else it }
                doc = doc.copy(blocks = updatedBlocks)
            }
        }
        vm.updateDocumentInternal(doc.copy(trackedChanges = emptyList()))
        vm.historyManager.scheduleDocumentAutosave()
        vm.showStatusMessage("All changes rejected")
    }
}
