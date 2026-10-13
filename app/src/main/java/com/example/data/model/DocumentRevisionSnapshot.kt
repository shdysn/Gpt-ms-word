package com.example.data.model

/**
 * Clean data model representing a user or auto-saved document revision snapshot.
 */
data class DocumentRevisionSnapshot(
    val id: String,
    val timestamp: Long,
    val label: String,
    val wordCount: Int,
    val characterCount: Int,
    val snippet: String,
    val snapshotDoc: RichDocument? = null
)
