package com.example.ui.home

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.error.DocumentResult
import com.example.data.local.PreferencesManager
import com.example.data.model.DocumentEntity
import com.example.data.repository.DocumentRepository
import com.example.data.repository.TemplateType
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File

class HomeViewModel(
    private val repository: DocumentRepository,
    private val preferencesManager: PreferencesManager
) : ViewModel() {

    private val exportImportHelper = HomeExportImportHelper(repository)
    private val templateManager = HomeTemplateManager(repository)

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _activeFilter = MutableStateFlow(HomeFilter.ALL)
    val activeFilter: StateFlow<HomeFilter> = _activeFilter.asStateFlow()

    private val _sortOrder = MutableStateFlow(DocumentSortOrder.MODIFIED_DESC)
    val sortOrder: StateFlow<DocumentSortOrder> = _sortOrder.asStateFlow()

    private val _isGridView = MutableStateFlow(false)
    val isGridView: StateFlow<Boolean> = _isGridView.asStateFlow()

    private val _infoMessage = MutableStateFlow<String?>(null)
    val infoMessage: StateFlow<String?> = _infoMessage.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val documents: StateFlow<List<DocumentEntity>> = combine(
        _searchQuery,
        _activeFilter,
        _sortOrder
    ) { query, filter, sortOrder ->
        Triple(query, filter, sortOrder)
    }.flatMapLatest { (query, filter, sortOrder) ->
        templateManager.searchAndFilterDocuments(query, filter, sortOrder)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val isAutosaveEnabled: StateFlow<Boolean> = preferencesManager.isAutosaveEnabled.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = true
    )

    val defaultPageSize: StateFlow<String> = preferencesManager.defaultPageSize.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = "A4"
    )

    val defaultRtl: StateFlow<Boolean> = preferencesManager.defaultRtl.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = false
    )

    val showWordCount: StateFlow<Boolean> = preferencesManager.showWordCount.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = true
    )

    val showRuler: StateFlow<Boolean> = preferencesManager.showRuler.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = true
    )

    init {
        viewModelScope.launch {
            templateManager.initializeDefaultDocumentsIfEmpty()
        }
        viewModelScope.launch {
            preferencesManager.sortOrder.collect { orderName ->
                try {
                    _sortOrder.value = DocumentSortOrder.valueOf(orderName)
                } catch (e: Exception) {
                    _sortOrder.value = DocumentSortOrder.MODIFIED_DESC
                }
            }
        }
    }

    fun setAutosaveEnabled(enabled: Boolean) = viewModelScope.launch { preferencesManager.setAutosaveEnabled(enabled) }
    fun setDefaultRtl(isRtl: Boolean) = viewModelScope.launch { preferencesManager.setDefaultRtl(isRtl) }
    fun setShowWordCount(show: Boolean) = viewModelScope.launch { preferencesManager.setShowWordCount(show) }
    fun setShowRuler(show: Boolean) = viewModelScope.launch { preferencesManager.setShowRuler(show) }
    fun onSearchQueryChanged(query: String) { _searchQuery.value = query }
    fun onFilterChanged(filter: HomeFilter) { _activeFilter.value = filter }
    fun onSortOrderChanged(order: DocumentSortOrder) {
        _sortOrder.value = order
        viewModelScope.launch { preferencesManager.setSortOrder(order.name) }
    }
    fun toggleViewMode() { _isGridView.value = !_isGridView.value }
    fun clearInfoMessage() { _infoMessage.value = null }

    fun createDocument(title: String, template: TemplateType, onCreated: (String) -> Unit) {
        viewModelScope.launch {
            when (val res = templateManager.createFromTemplate(title, template)) {
                is DocumentResult.Success -> onCreated(res.data.id)
                is DocumentResult.Failure -> _infoMessage.value = "Failed to create document: ${res.error.message}"
            }
        }
    }

    fun duplicateDocument(id: String) {
        viewModelScope.launch {
            when (val res = templateManager.duplicateDocument(id)) {
                is DocumentResult.Success -> _infoMessage.value = "Duplicated '${res.data.title}'"
                is DocumentResult.Failure -> _infoMessage.value = "Duplicate failed: ${res.error.message}"
            }
        }
    }

    fun renameDocument(id: String, newTitle: String) = viewModelScope.launch { repository.renameDocument(id, newTitle) }
    fun deleteDocument(id: String) = viewModelScope.launch {
        repository.deleteDocument(id)
        _infoMessage.value = "Document deleted"
    }
    fun togglePinned(id: String, currentPinned: Boolean) = viewModelScope.launch { repository.togglePinned(id, !currentPinned) }

    fun exportDocumentToPdf(context: Context, docId: String, onComplete: (File?) -> Unit) = viewModelScope.launch {
        val file = exportImportHelper.exportPdf(context, docId) { msg -> _infoMessage.value = msg }
        onComplete(file)
    }

    fun exportDocumentToDocx(context: Context, docId: String, onComplete: (File?) -> Unit) = viewModelScope.launch {
        val file = exportImportHelper.exportDocx(context, docId) { msg -> _infoMessage.value = msg }
        onComplete(file)
    }

    fun printDocument(context: Context, docId: String) = viewModelScope.launch {
        exportImportHelper.printDocument(context, docId) { err -> _infoMessage.value = err }
    }

    fun importDocument(context: Context, uri: Uri, onImported: (String) -> Unit) = viewModelScope.launch {
        exportImportHelper.importFromUri(
            context, uri,
            onSuccess = { id, title ->
                _infoMessage.value = "Imported '$title'"
                onImported(id)
            },
            onError = { err -> _infoMessage.value = err }
        )
    }
}
