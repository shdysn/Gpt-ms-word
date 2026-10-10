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

    @Query("SELECT COUNT(*) FROM documents")
    suspend fun getDocumentCount(): Int
}
