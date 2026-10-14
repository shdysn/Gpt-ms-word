package com.example.ui.home

import com.example.core.error.DocumentResult
import com.example.data.model.DocumentEntity
import com.example.data.model.RichDocument
import com.example.data.repository.DocumentRepository
import com.example.data.repository.TemplateType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Handles template creation, seeding default documents, and document filtering/sorting.
 */
class HomeTemplateManager(
    private val repository: DocumentRepository
) {
    suspend fun initializeDefaultDocumentsIfEmpty() {
        repository.initializeDefaultDocumentsIfEmpty()
    }

    fun searchAndFilterDocuments(
        query: String,
        filter: HomeFilter,
        sortOrder: DocumentSortOrder
    ): Flow<List<DocumentEntity>> {
        val flow = if (query.isBlank()) {
            repository.getAllDocuments()
        } else {
            repository.searchDocuments(query.trim())
        }
        return flow.map { list ->
            HomeDocumentFilter.filterAndSort(list, filter, sortOrder)
        }
    }

    suspend fun createFromTemplate(
        title: String,
        template: TemplateType
    ): DocumentResult<RichDocument> {
        return repository.createDocument(title, template)
    }

    suspend fun duplicateDocument(id: String): DocumentResult<RichDocument> {
        return repository.duplicateDocument(id)
    }
}
