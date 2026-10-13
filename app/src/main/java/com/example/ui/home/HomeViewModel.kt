package com.example.ui.home

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.core.error.DocumentResult
import com.example.data.local.PreferencesManager
import com.example.data.model.DocumentEntity
import com.example.data.model.RichDocument
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

import kotlinx.coroutines.flow.map

enum class HomeFilter {
    ALL,
    RECENT,
    PINNED,
    FAVORITES
}

enum class DocumentSortOrder(val displayName: String) {
    MODIFIED_DESC("Newest first"),
    MODIFIED_ASC("Oldest first"),
    TITLE_ASC("Title (A-Z)"),
    TITLE_DESC("Title (Z-A)")
}

class HomeViewModel(
    private val repository: DocumentRepository,
    private val preferencesManager: PreferencesManager
) : ViewModel() {

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
        val flow = if (query.isBlank()) {
            repository.getAllDocuments()
        } else {
            repository.searchDocuments(query.trim())
        }
        flow.map { list ->
            val filtered = when (filter) {
                HomeFilter.ALL -> list
                HomeFilter.RECENT -> {
                    val sevenDaysAgo = System.currentTimeMillis() - (7L * 24 * 60 * 60 * 1000)
                    val recentDocs = list.filter { it.updatedAt >= sevenDaysAgo }
                    if (recentDocs.isNotEmpty()) recentDocs.sortedByDescending { it.updatedAt }.take(10)
                    else list.sortedByDescending { it.updatedAt }.take(5)
                }
                HomeFilter.PINNED -> list.filter { it.isPinned }
                HomeFilter.FAVORITES -> list.filter { it.isFavorite }
            }
            if (filter == HomeFilter.RECENT) {
                when (sortOrder) {
                    DocumentSortOrder.MODIFIED_DESC -> filtered.sortedByDescending { it.updatedAt }
                    DocumentSortOrder.MODIFIED_ASC -> filtered.sortedBy { it.updatedAt }
                    DocumentSortOrder.TITLE_ASC -> filtered.sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it.title })
                    DocumentSortOrder.TITLE_DESC -> filtered.sortedWith(compareByDescending(String.CASE_INSENSITIVE_ORDER) { it.title })
                }
            } else {
                when (sortOrder) {
                    DocumentSortOrder.MODIFIED_DESC -> filtered.sortedWith(
                        compareByDescending<DocumentEntity> { it.isPinned }.thenByDescending { it.updatedAt }
                    )
                    DocumentSortOrder.MODIFIED_ASC -> filtered.sortedWith(
                        compareByDescending<DocumentEntity> { it.isPinned }.thenBy { it.updatedAt }
                    )
                    DocumentSortOrder.TITLE_ASC -> filtered.sortedWith(
                        compareByDescending<DocumentEntity> { it.isPinned }.thenBy(String.CASE_INSENSITIVE_ORDER) { it.title }
                    )
                    DocumentSortOrder.TITLE_DESC -> filtered.sortedWith(
                        compareByDescending<DocumentEntity> { it.isPinned }.thenByDescending(String.CASE_INSENSITIVE_ORDER) { it.title }
                    )
                }
            }
        }
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

    fun setAutosaveEnabled(enabled: Boolean) {
        viewModelScope.launch { preferencesManager.setAutosaveEnabled(enabled) }
    }

    fun setDefaultRtl(isRtl: Boolean) {
        viewModelScope.launch { preferencesManager.setDefaultRtl(isRtl) }
    }

    fun setShowWordCount(show: Boolean) {
        viewModelScope.launch { preferencesManager.setShowWordCount(show) }
    }

    fun setShowRuler(show: Boolean) {
        viewModelScope.launch { preferencesManager.setShowRuler(show) }
    }

    init {
        viewModelScope.launch {
            repository.initializeDefaultDocumentsIfEmpty()
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

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun onFilterChanged(filter: HomeFilter) {
        _activeFilter.value = filter
    }

    fun onSortOrderChanged(order: DocumentSortOrder) {
        _sortOrder.value = order
        viewModelScope.launch {
            preferencesManager.setSortOrder(order.name)
        }
    }

    fun toggleViewMode() {
        _isGridView.value = !_isGridView.value
    }

    fun clearInfoMessage() {
        _infoMessage.value = null
    }

    fun createDocument(title: String, template: TemplateType, onCreated: (String) -> Unit) {
        viewModelScope.launch {
            when (val res = repository.createDocument(title, template)) {
                is DocumentResult.Success -> {
                    onCreated(res.data.id)
                }
                is DocumentResult.Failure -> {
                    _infoMessage.value = "Failed to create document: ${res.error.message}"
                }
            }
        }
    }

    fun duplicateDocument(id: String) {
        viewModelScope.launch {
            when (val res = repository.duplicateDocument(id)) {
                is DocumentResult.Success -> {
                    _infoMessage.value = "Duplicated '${res.data.title}'"
                }
                is DocumentResult.Failure -> {
                    _infoMessage.value = "Duplicate failed: ${res.error.message}"
                }
            }
        }
    }

    fun renameDocument(id: String, newTitle: String) {
        viewModelScope.launch {
            repository.renameDocument(id, newTitle)
        }
    }

    fun deleteDocument(id: String) {
        viewModelScope.launch {
            repository.deleteDocument(id)
            _infoMessage.value = "Document deleted"
        }
    }

    fun togglePinned(id: String, currentPinned: Boolean) {
        viewModelScope.launch {
            repository.togglePinned(id, !currentPinned)
        }
    }

    fun exportDocumentToPdf(context: Context, docId: String, onComplete: (File?) -> Unit) {
        viewModelScope.launch {
            when (val docRes = repository.getDocument(docId)) {
                is DocumentResult.Success -> {
                    val exportDir = File(context.cacheDir, "exports").apply { mkdirs() }
                    val sanitizedTitle = docRes.data.title.replace("[^a-zA-Z0-9.-]".toRegex(), "_")
                    val outFile = File(exportDir, "$sanitizedTitle.pdf")
                    when (val exportRes = repository.exportToPdf(context, docRes.data, outFile)) {
                        is DocumentResult.Success -> {
                            _infoMessage.value = "PDF exported: ${outFile.name}"
                            onComplete(outFile)
                        }
                        is DocumentResult.Failure -> {
                            _infoMessage.value = "PDF export error: ${exportRes.error.message}"
                            onComplete(null)
                        }
                    }
                }
                is DocumentResult.Failure -> {
                    _infoMessage.value = "Could not load document: ${docRes.error.message}"
                    onComplete(null)
                }
            }
        }
    }

    fun exportDocumentToDocx(context: Context, docId: String, onComplete: (File?) -> Unit) {
        viewModelScope.launch {
            when (val docRes = repository.getDocument(docId)) {
                is DocumentResult.Success -> {
                    val exportDir = File(context.cacheDir, "exports").apply { mkdirs() }
                    val sanitizedTitle = docRes.data.title.replace("[^a-zA-Z0-9.-]".toRegex(), "_")
                    val outFile = File(exportDir, "$sanitizedTitle.docx")
                    when (val exportRes = repository.exportToDocx(context, docRes.data, outFile)) {
                        is DocumentResult.Success -> {
                            _infoMessage.value = "Word Document (.docx) exported: ${outFile.name}"
                            onComplete(outFile)
                        }
                        is DocumentResult.Failure -> {
                            _infoMessage.value = "DOCX export error: ${exportRes.error.message}"
                            onComplete(null)
                        }
                    }
                }
                is DocumentResult.Failure -> {
                    _infoMessage.value = "Could not load document: ${docRes.error.message}"
                    onComplete(null)
                }
            }
        }
    }

    fun printDocument(context: Context, docId: String) {
        viewModelScope.launch {
            when (val docRes = repository.getDocument(docId)) {
                is DocumentResult.Success -> {
                    repository.printDocument(context, docRes.data)
                }
                is DocumentResult.Failure -> {
                    _infoMessage.value = "Could not load document for print: ${docRes.error.message}"
                }
            }
        }
    }

    fun importDocument(context: Context, uri: Uri, onImported: (String) -> Unit) {
        viewModelScope.launch {
            when (val res = repository.importFromUri(context, uri)) {
                is DocumentResult.Success -> {
                    _infoMessage.value = "Imported '${res.data.title}'"
                    onImported(res.data.id)
                }
                is DocumentResult.Failure -> {
                    _infoMessage.value = "Import failed: ${res.error.message}"
                }
            }
        }
    }
}

class HomeViewModelFactory(
    private val repository: DocumentRepository,
    private val preferencesManager: PreferencesManager
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return HomeViewModel(repository, preferencesManager) as T
    }
}
