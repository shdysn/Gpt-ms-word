package com.example.editor.engine

import com.example.data.model.RichDocument

/**
 * Manages the linear undo and redo history for the word processor.
 */
class UndoRedoManager(
    private val maxHistorySize: Int = 100
) {
    private val undoStack = ArrayDeque<EditorCommand>()
    private val redoStack = ArrayDeque<EditorCommand>()

    val canUndo: Boolean get() = undoStack.isNotEmpty()
    val canRedo: Boolean get() = redoStack.isNotEmpty()

    val lastUndoDescription: String?
        get() = undoStack.lastOrNull()?.description

    val lastRedoDescription: String?
        get() = redoStack.lastOrNull()?.description

    /**
     * Executes a command on the document and records it to the undo stack.
     * When coalesce is true and the last command is of the same type on the same target,
     * the commands are merged to provide natural word-level typing undo steps.
     */
    fun execute(command: EditorCommand, currentDoc: RichDocument, coalesce: Boolean = false): RichDocument {
        val newDoc = command.apply(currentDoc)
        val lastCommand = undoStack.lastOrNull()

        if (coalesce && lastCommand is UpdateParagraphTextAndSpansCommand && command is UpdateParagraphTextAndSpansCommand
            && lastCommand.blockId == command.blockId) {
            // Merge typing sequences
            undoStack.removeLast()
            val mergedCommand = UpdateParagraphTextAndSpansCommand(
                blockId = command.blockId,
                oldText = lastCommand.oldText,
                newText = command.newText,
                oldSpans = lastCommand.oldSpans,
                newSpans = command.newSpans,
                wasRtl = lastCommand.wasRtl,
                isRtl = command.isRtl
            )
            undoStack.addLast(mergedCommand)
        } else {
            undoStack.addLast(command)
            if (undoStack.size > maxHistorySize) {
                undoStack.removeFirst()
            }
        }
        redoStack.clear()
        return newDoc
    }

    /**
     * Undoes the last command.
     */
    fun undo(currentDoc: RichDocument): Pair<RichDocument, EditorCommand?> {
        if (undoStack.isEmpty()) return currentDoc to null
        val command = undoStack.removeLast()
        val revertedDoc = command.revert(currentDoc)
        redoStack.addLast(command)
        return revertedDoc to command
    }

    /**
     * Redoes the last undone command.
     */
    fun redo(currentDoc: RichDocument): Pair<RichDocument, EditorCommand?> {
        if (redoStack.isEmpty()) return currentDoc to null
        val command = redoStack.removeLast()
        val reappliedDoc = command.apply(currentDoc)
        undoStack.addLast(command)
        return reappliedDoc to command
    }

    /**
     * Clears all history stacks (e.g. on new document load).
     */
    fun clear() {
        undoStack.clear()
        redoStack.clear()
    }
}
