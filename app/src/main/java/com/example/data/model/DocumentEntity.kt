package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room Entity representing document metadata and local index.
 */
@Entity(tableName = "documents")
data class DocumentEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val snippet: String,
    val wordCount: Int,
    val characterCount: Int,
    val pageCount: Int,
    val createdAt: Long,
    val updatedAt: Long,
    val isPinned: Boolean = false,
    val isFavorite: Boolean = false,
    val filePath: String, // Relative path in internal storage, e.g. "documents/{id}.swp"
    val formatType: String = DocumentFormatType.SWP_NATIVE.name,
    val pageSize: String = "A4",
    val orientation: String = "PORTRAIT",
    val marginPreset: String = "NORMAL"
)
