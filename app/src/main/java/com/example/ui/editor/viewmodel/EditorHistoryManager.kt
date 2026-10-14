package com.example.ui.editor.viewmodel

import androidx.lifecycle.viewModelScope
import com.example.core.error.DocumentResult
import com.example.data.model.DocumentRevisionSnapshot
import com.example.data.model.ParagraphBlock
import com.example.data.model.RichDocument
import com.example.editor.engine.AutosaveController
import com.example.editor.engine.AutosaveStatus
import com.example.editor.engine.EditorCommand
import com.example.editor.engine.UndoRedoManager
import com.example.ui.editor.EditorViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Manages Undo/Redo stack, autosave ticks, backgrounding flushes, and revision snapshots.
 */
class EditorHistoryManager(
    private val vm: EditorViewModel
) {
    internal val undoRedoManager = UndoRedoManager()

    internal val autosaveController = AutosaveController(
        scope = vm.viewModelScope,
        debounceMillis = 1500L,
        onPerformSave = {
            val doc = vm.currentDocument ?: return@AutosaveController false
            val res = vm.repository.saveDocument(doc)
            res.isSuccess
        },
        onPerformRecoverySnapshot = {
            val doc = vm.currentDocument ?: return@AutosaveController
            vm.repository.saveRecoveryDraft(doc)
        }
    )

    private val _canUndo = MutableStateFlow(false)
    val canUndo: StateFlow<Boolean> = _canUndo.asStateFlow()

    private val _canRedo = MutableStateFlow(false)
    val canRedo: StateFlow<Boolean> = _canRedo.asStateFlow()

    private val _recoveryAvailable = MutableStateFlow(false)
    val recoveryAvailable: StateFlow<Boolean> = _recoveryAvailable.asStateFlow()

    private val _revisionSnapshots = MutableStateFlow<List<DocumentRevisionSnapshot>>(emptyList())
    val revisionSnapshots: StateFlow<List<DocumentRevisionSnapshot>> = _revisionSnapshots.asStateFlow()

    fun updateUndoRedoStates() {
        _canUndo.value = undoRedoManager.canUndo
        _canRedo.value = undoRedoManager.canRedo
    }

    fun executeCommand(command: EditorCommand) {
        val currentDoc = vm.currentDocument ?: return
        val newDoc = undoRedoManager.execute(command, currentDoc)
        vm.updateDocumentInternal(newDoc)
        updateUndoRedoStates()
        scheduleDocumentAutosave()
    }

    fun undo() {
        val currentDoc = vm.currentDocument ?: return
        val (revertedDoc, _) = undoRedoManager.undo(currentDoc)
        vm.updateDocumentInternal(revertedDoc)
        updateUndoRedoStates()
        scheduleDocumentAutosave()
    }

    fun redo() {
        val currentDoc = vm.currentDocument ?: return
        val (reappliedDoc, _) = undoRedoManager.redo(currentDoc)
        vm.updateDocumentInternal(reappliedDoc)
        updateUndoRedoStates()
        scheduleDocumentAutosave()
    }

    fun scheduleDocumentAutosave() {
        if (vm.isAutosaveEnabled.value) {
            autosaveController.scheduleAutosave()
        } else {
            autosaveController.markDirty()
        }
    }

    fun saveNow() {
        autosaveController.triggerImmediateSave()
    }

    suspend fun flushSave(): Boolean {
        return autosaveController.flushImmediateSave()
    }

    fun onAppBackgrounded() {
        vm.viewModelScope.launch {
            flushSave()
        }
    }

    fun setRecoveryAvailable(available: Boolean) {
        _recoveryAvailable.value = available
    }

    fun restoreRecoveryDraft() {
        vm.viewModelScope.launch {
            when (val res = vm.repository.loadRecoveryDraft(vm.docId)) {
                is DocumentResult.Success -> {
                    vm.updateDocumentInternal(res.data)
                    _recoveryAvailable.value = false
                    vm.repository.clearRecoveryDraft(vm.docId)
                    autosaveController.markSaved()
                    vm.showStatusMessage("Auto-saved draft restored successfully")
                }
                is DocumentResult.Failure -> {
                    vm.showStatusMessage("Failed to restore recovery draft: ${res.error.message}")
                }
            }
        }
    }

    fun dismissRecoveryDraft() {
        vm.viewModelScope.launch {
            vm.repository.clearRecoveryDraft(vm.docId)
            _recoveryAvailable.value = false
        }
    }

    fun observeSnapshots() {
        vm.viewModelScope.launch {
            vm.repository.getSnapshots(vm.docId).collect { list ->
                _revisionSnapshots.value = list
            }
        }
    }

    fun createNamedSnapshot(label: String = "Manual Snapshot") {
        val doc = vm.currentDocument ?: return
        vm.viewModelScope.launch {
            val dateStr = SimpleDateFormat("MMM d, yyyy h:mm a", Locale.getDefault()).format(Date())
            val fullLabel = if (label.isNotBlank()) "$label ($dateStr)" else "Snapshot ($dateStr)"
            val snippet = doc.blocks.firstOrNull { it is ParagraphBlock }?.let { (it as ParagraphBlock).text.take(100) } ?: ""
            val snapshot = DocumentRevisionSnapshot(
                id = java.util.UUID.randomUUID().toString(),
                timestamp = System.currentTimeMillis(),
                label = fullLabel,
                wordCount = doc.wordCount,
                characterCount = doc.characterCount,
                snippet = snippet,
                snapshotDoc = doc
            )
            when (val res = vm.repository.saveSnapshot(vm.docId, snapshot)) {
                is DocumentResult.Success -> {
                    vm.showStatusMessage("Snapshot '$fullLabel' saved")
                }
                is DocumentResult.Failure -> {
                    vm.showStatusMessage("Failed to save snapshot: ${res.error.message}")
                }
            }
        }
    }

    fun restoreRevisionSnapshot(snapshotId: String) {
        vm.viewModelScope.launch {
            val snapshot = _revisionSnapshots.value.find { it.id == snapshotId }
            if (snapshot == null) {
                vm.showStatusMessage("Snapshot not found")
                return@launch
            }
            val restored = snapshot.snapshotDoc ?: when (val res = vm.repository.getSnapshot(snapshotId)) {
                is DocumentResult.Success -> res.data.snapshotDoc
                else -> null
            }
            if (restored != null) {
                createNamedSnapshot("Pre-restore Backup")
                vm.updateDocumentInternal(restored)
                scheduleDocumentAutosave()
                vm.showStatusMessage("Restored to: ${snapshot.label}")
            } else {
                vm.showStatusMessage("Failed to parse snapshot data")
            }
        }
    }
}
