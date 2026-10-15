package com.example.data.repository

import com.example.core.error.DocumentError
import com.example.core.error.DocumentResult
import com.example.data.local.DocumentDao
import com.example.data.model.DocumentRevisionSnapshot
import com.example.data.model.DocumentSnapshotEntity
import com.example.data.model.RichDocument
import com.example.data.storage.DocumentStorageManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Handles creation, query, retrieval, and deletion of document revision snapshots.
 */
class DocumentSnapshotManager(
    private val documentDao: DocumentDao,
    private val storageManager: DocumentStorageManager
) {

    fun getSnapshots(documentId: String): Flow<List<DocumentRevisionSnapshot>> {
        return documentDao.getSnapshotsForDocument(documentId).map { entities ->
            entities.map { entity ->
                val doc = try {
                    storageManager.deserializeFromJson(entity.documentJson)
                } catch (_: Exception) {
                    null
                }
                DocumentRevisionSnapshot(
                    id = entity.id,
                    timestamp = entity.timestamp,
                    label = entity.label,
                    wordCount = entity.wordCount,
                    characterCount = entity.characterCount,
                    snippet = entity.snippet,
                    snapshotDoc = doc
                )
            }
        }
    }

    suspend fun saveSnapshot(
        documentId: String,
        snapshot: DocumentRevisionSnapshot,
        fallbackDocProvider: suspend () -> DocumentResult<RichDocument>
    ): DocumentResult<Unit> {
        val snapDoc = snapshot.snapshotDoc ?: run {
            when (val res = fallbackDocProvider()) {
                is DocumentResult.Success -> res.data
                is DocumentResult.Failure -> return DocumentResult.Failure(res.error)
            }
        }
        val jsonStr = storageManager.serializeToJson(snapDoc)
        val entity = DocumentSnapshotEntity(
            id = snapshot.id,
            documentId = documentId,
            timestamp = snapshot.timestamp,
            label = snapshot.label,
            wordCount = snapshot.wordCount,
            characterCount = snapshot.characterCount,
            snippet = snapshot.snippet,
            documentJson = jsonStr
        )
        documentDao.insertSnapshot(entity)
        return DocumentResult.Success(Unit)
    }

    suspend fun getSnapshot(snapshotId: String): DocumentResult<DocumentRevisionSnapshot> {
        val entity = documentDao.getSnapshotById(snapshotId)
            ?: return DocumentResult.Failure(DocumentError.DocumentNotFound(snapshotId))
        val doc = try {
            storageManager.deserializeFromJson(entity.documentJson)
        } catch (e: Exception) {
            return DocumentResult.Failure(DocumentError.CorruptDocumentError("Failed to decode snapshot document: ${e.message}"))
        }
        return DocumentResult.Success(
            DocumentRevisionSnapshot(
                id = entity.id,
                timestamp = entity.timestamp,
                label = entity.label,
                wordCount = entity.wordCount,
                characterCount = entity.characterCount,
                snippet = entity.snippet,
                snapshotDoc = doc
            )
        )
    }

    suspend fun deleteSnapshot(snapshotId: String): DocumentResult<Unit> {
        documentDao.deleteSnapshotById(snapshotId)
        return DocumentResult.Success(Unit)
    }
}
