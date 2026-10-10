package com.example.data.service

import android.content.Context
import android.net.Uri
import com.example.core.error.DocumentError
import com.example.core.error.DocumentResult
import com.example.data.model.RichDocument
import com.example.data.storage.DocumentStorageManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Service interface for importing external documents into Smart Word Processor.
 */
interface DocumentImportService {
    suspend fun importDocumentFromUri(context: Context, uri: Uri): DocumentResult<RichDocument>
}

class DocumentImportServiceImpl(
    private val storageManager: DocumentStorageManager
) : DocumentImportService {

    override suspend fun importDocumentFromUri(context: Context, uri: Uri): DocumentResult<RichDocument> =
        withContext(Dispatchers.IO) {
            try {
                storageManager.importFromSafUri(uri)
            } catch (e: Exception) {
                DocumentResult.Failure(
                    DocumentError.StorageError("Import failed: ${e.message}", e)
                )
            }
        }
}
