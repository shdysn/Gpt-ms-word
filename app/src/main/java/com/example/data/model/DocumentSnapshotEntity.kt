package com.example.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Room Entity representing a persistent document revision snapshot.
 * Ensures version history survives app restarts and process recreation.
 */
@Entity(
    tableName = "document_snapshots",
    foreignKeys = [
        ForeignKey(
            entity = DocumentEntity::class,
            parentColumns = ["id"],
            childColumns = ["documentId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["documentId"])]
)
data class DocumentSnapshotEntity(
    @PrimaryKey
    val id: String,
    val documentId: String,
    val timestamp: Long,
    val label: String,
    val wordCount: Int,
    val characterCount: Int,
    val snippet: String,
    val documentJson: String
)
