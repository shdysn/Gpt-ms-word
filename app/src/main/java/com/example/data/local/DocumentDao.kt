package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.DocumentEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DocumentDao {

    @Query("SELECT * FROM documents ORDER BY isPinned DESC, updatedAt DESC")
    fun getAllDocuments(): Flow<List<DocumentEntity>>

    @Query("SELECT * FROM documents WHERE id = :id")
    suspend fun getDocumentById(id: String): DocumentEntity?

    @Query("SELECT * FROM documents WHERE id = :id")
    fun observeDocumentById(id: String): Flow<DocumentEntity?>

    @Query("SELECT * FROM documents WHERE title LIKE '%' || :query || '%' OR snippet LIKE '%' || :query || '%' ORDER BY isPinned DESC, updatedAt DESC")
    fun searchDocuments(query: String): Flow<List<DocumentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateDocument(document: DocumentEntity)

    @Update
    suspend fun updateDocument(document: DocumentEntity)

    @Query("DELETE FROM documents WHERE id = :id")
    suspend fun deleteDocumentById(id: String)

    @Query("UPDATE documents SET isPinned = :isPinned WHERE id = :id")
    suspend fun setPinned(id: String, isPinned: Boolean)

    @Query("UPDATE documents SET isFavorite = :isFavorite WHERE id = :id")
    suspend fun setFavorite(id: String, isFavorite: Boolean)

    @Query("UPDATE documents SET title = :newTitle, updatedAt = :updatedAt WHERE id = :id")
    suspend fun renameDocument(id: String, newTitle: String, updatedAt: Long)

    @Query("SELECT * FROM documents ORDER BY isPinned DESC, updatedAt ASC")
    fun getAllDocumentsByModifiedAsc(): Flow<List<DocumentEntity>>

    @Query("SELECT * FROM documents ORDER BY isPinned DESC, title COLLATE NOCASE ASC")
    fun getAllDocumentsByTitleAsc(): Flow<List<DocumentEntity>>

    @Query("SELECT * FROM documents ORDER BY isPinned DESC, title COLLATE NOCASE DESC")
    fun getAllDocumentsByTitleDesc(): Flow<List<DocumentEntity>>

    @Query("UPDATE documents SET hasRecoveryDraft = :hasRecovery WHERE id = :id")
    suspend fun setHasRecoveryDraft(id: String, hasRecovery: Boolean)

    @Query("SELECT COUNT(*) FROM documents")
    suspend fun getDocumentCount(): Int

    // ----------------------------------------------------
    // Document Revision Snapshots
    // ----------------------------------------------------

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSnapshot(snapshot: com.example.data.model.DocumentSnapshotEntity)

    @Query("SELECT * FROM document_snapshots WHERE documentId = :documentId ORDER BY timestamp DESC")
    fun getSnapshotsForDocument(documentId: String): Flow<List<com.example.data.model.DocumentSnapshotEntity>>

    @Query("SELECT * FROM document_snapshots WHERE id = :snapshotId")
    suspend fun getSnapshotById(snapshotId: String): com.example.data.model.DocumentSnapshotEntity?

    @Query("DELETE FROM document_snapshots WHERE id = :snapshotId")
    suspend fun deleteSnapshotById(snapshotId: String)

    @Query("DELETE FROM document_snapshots WHERE documentId = :documentId")
    suspend fun deleteSnapshotsForDocument(documentId: String)
}
