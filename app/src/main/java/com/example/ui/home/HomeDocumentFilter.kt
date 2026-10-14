package com.example.ui.home

import com.example.data.model.DocumentEntity

object HomeDocumentFilter {
    fun filterAndSort(
        list: List<DocumentEntity>,
        filter: HomeFilter,
        sortOrder: DocumentSortOrder
    ): List<DocumentEntity> {
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

        return if (filter == HomeFilter.RECENT) {
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
}
