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
     */
    fun execute(command: EditorCommand, currentDoc: RichDocument): RichDocument {
        val newDoc = command.apply(currentDoc)
        undoStack.addLast(command)
        if (undoStack.size > maxHistorySize) {
            undoStack.removeFirst()
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
