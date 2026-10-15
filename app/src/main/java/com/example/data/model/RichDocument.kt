package com.example.data.model

import com.example.core.layout.PageLayoutConfig
import java.util.UUID

/**
 * Complete in-memory model of a Rich Document.
 */
data class RichDocument(
    val id: String = UUID.randomUUID().toString(),
    val title: String = "Untitled Document",
    val author: String = "Author",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val layoutConfig: PageLayoutConfig = PageLayoutConfig(),
    val blocks: List<BlockElement> = listOf(ParagraphBlock()),
    val passwordHash: String? = null,
    val comments: List<DocumentComment> = emptyList(),
    val trackedChanges: List<TrackedChange> = emptyList(),
    val isTrackChangesEnabled: Boolean = false,
    val customStyles: List<CustomStyle> = emptyList()
) {
    val wordCount: Int
        get() = blocks.filterIsInstance<ParagraphBlock>().sumOf { block ->
            block.text.trim().split("\\s+".toRegex()).count { it.isNotBlank() }
        }

    val characterCount: Int
        get() = blocks.filterIsInstance<ParagraphBlock>().sumOf { it.text.length }

    val isPasswordProtected: Boolean
        get() = !passwordHash.isNullOrBlank()

    val plainTextSnippet: String
        get() = blocks.filterIsInstance<ParagraphBlock>()
            .map { it.text }
            .firstOrNull { it.isNotBlank() }
            ?.take(160) ?: "Empty document"

    val estimatedPageCount: Int
        get() = com.example.core.layout.PaginationEngine.paginate(this).size
}
