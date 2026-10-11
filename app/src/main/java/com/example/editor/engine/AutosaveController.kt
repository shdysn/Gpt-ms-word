package com.example.editor.engine

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Lifecycle states of the autosave background pipeline.
 */
enum class AutosaveStatus(val label: String) {
    IDLE("Ready"),
    DIRTY("Unsaved changes"),
    SAVING("Saving..."),
    SAVED("Saved"),
    ERROR("Save failed")
}

/**
 * Debounced background autosave controller.
 * Handles background non-blocking persistence, dirty tracking,
 * crash recovery snapshots, and lifecycle flushing.
 */
class AutosaveController(
    private val scope: CoroutineScope,
    private val debounceMillis: Long = 1500L,
    private val onPerformSave: suspend () -> Boolean,
    private val onPerformRecoverySnapshot: (suspend () -> Unit)? = null
) {
    private val _status = MutableStateFlow(AutosaveStatus.IDLE)
    val status: StateFlow<AutosaveStatus> = _status.asStateFlow()

    private val saveMutex = Mutex()
    private var saveJob: Job? = null
    private var recoveryJob: Job? = null

    /**
     * True if the document has modifications that haven't yet been cleanly persisted.
     */
    val isDirty: Boolean
        get() = _status.value == AutosaveStatus.DIRTY || _status.value == AutosaveStatus.ERROR

    /**
     * Flags document as dirty and schedules a debounced save.
     * Also schedules a quick recovery draft snapshot so unexpected process kills retain work.
     */
    fun scheduleAutosave() {
        _status.value = AutosaveStatus.DIRTY

        // Write recovery draft quickly (300ms) to preserve draft during crash/kill
        onPerformRecoverySnapshot?.let { snapshotFunc ->
            recoveryJob?.cancel()
            recoveryJob = scope.launch {
                delay(300L)
                try {
                    snapshotFunc()
                } catch (e: Exception) {
                    // Ignored in draft snapshot
                }
            }
        }

        // Schedule debounced full persistence
        saveJob?.cancel()
        saveJob = scope.launch {
            delay(debounceMillis)
            executeSaveInternal()
        }
    }

    /**
     * Executes immediate save without debounce delay asynchronously.
     */
    fun triggerImmediateSave() {
        saveJob?.cancel()
        scope.launch {
            executeSaveInternal()
        }
    }

    /**
     * Suspends until immediate save completes and returns boolean success.
     * Ideal for onPause, onStop, or navigation exit flows.
     */
    suspend fun flushImmediateSave(): Boolean {
        saveJob?.cancel()
        return executeSaveInternal()
    }

    private suspend fun executeSaveInternal(): Boolean = saveMutex.withLock {
        _status.value = AutosaveStatus.SAVING
        return try {
            val success = onPerformSave()
            if (success) {
                _status.value = AutosaveStatus.SAVED
                true
            } else {
                _status.value = AutosaveStatus.ERROR
                false
            }
        } catch (e: Exception) {
            _status.value = AutosaveStatus.ERROR
            false
        }
    }

    fun markSaved() {
        _status.value = AutosaveStatus.SAVED
    }

    fun markDirty() {
        _status.value = AutosaveStatus.DIRTY
    }
}
