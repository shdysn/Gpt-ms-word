package com.example.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.data.local.PreferencesManager
import com.example.data.repository.DocumentRepository

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

class HomeViewModelFactory(
    private val repository: DocumentRepository,
    private val preferencesManager: PreferencesManager
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return HomeViewModel(repository, preferencesManager) as T
    }
}
