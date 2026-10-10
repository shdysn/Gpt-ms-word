package com.example.editor.engine

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Lifecycle states of the autosave background pipeline.
 */
enum class AutosaveStatus {
    IDLE,
    DIRTY,
    SAVING,
    SAVED,
    ERROR
}

/**
 * Debounced background autosave controller.
 */
class AutosaveController(
    private val scope: CoroutineScope,
    private val debounceMillis: Long = 2000L,
    private val onPerformSave: suspend () -> Boolean
) {
    private val _status = MutableStateFlow(AutosaveStatus.IDLE)
    val status: StateFlow<AutosaveStatus> = _status.asStateFlow()

    private var saveJob: Job? = null

    /**
     * Flags document as dirty and schedules a debounced save.
     */
    fun scheduleAutosave() {
        _status.value = AutosaveStatus.DIRTY
        saveJob?.cancel()
        saveJob = scope.launch {
            delay(debounceMillis)
            executeSaveNow()
        }
    }

    /**
     * Executes immediate save without debounce delay.
     */
    fun triggerImmediateSave() {
        saveJob?.cancel()
        scope.launch {
            executeSaveNow()
        }
    }

    private suspend fun executeSaveNow() {
        _status.value = AutosaveStatus.SAVING
        val success = onPerformSave()
        _status.value = if (success) AutosaveStatus.SAVED else AutosaveStatus.ERROR
    }

    fun markSaved() {
        _status.value = AutosaveStatus.SAVED
    }
}
