package com.example.data.service

import android.content.Context
import android.net.Uri
import com.example.core.error.DocumentError
import com.example.core.error.DocumentResult
import com.example.data.model.RichDocument
import com.example.data.service.docx.DocxImportResult
import com.example.data.service.docx.DocxService
import com.example.data.service.docx.DocxServiceImpl
import com.example.data.storage.DocumentStorageManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Service interface for importing external documents into Smart Word Processor.
 */
interface DocumentImportService {
    suspend fun importDocumentFromUri(context: Context, uri: Uri): DocumentResult<RichDocument>
    suspend fun importDocxFromUri(context: Context, uri: Uri): DocumentResult<DocxImportResult>
}

class DocumentImportServiceImpl(
    private val storageManager: DocumentStorageManager,
    private val docxService: DocxService = DocxServiceImpl()
) : DocumentImportService {

    override suspend fun importDocumentFromUri(context: Context, uri: Uri): DocumentResult<RichDocument> =
        withContext(Dispatchers.IO) {
            try {
                if (docxService.isDocxFile(context, uri)) {
                    when (val docxResult = docxService.importFromUri(context, uri)) {
                        is DocumentResult.Success -> DocumentResult.Success(docxResult.data.document)
                        is DocumentResult.Failure -> DocumentResult.Failure(docxResult.error)
                    }
                } else {
                    storageManager.importFromSafUri(uri)
                }
            } catch (e: Exception) {
                DocumentResult.Failure(
                    DocumentError.StorageError("Import failed: ${e.message}", e)
                )
            }
        }

    override suspend fun importDocxFromUri(context: Context, uri: Uri): DocumentResult<DocxImportResult> =
        withContext(Dispatchers.IO) {
            docxService.importFromUri(context, uri)
        }
}
