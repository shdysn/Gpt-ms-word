package com.example.data.repository

import android.content.Context
import android.net.Uri
import com.example.core.error.DocumentResult
import com.example.data.local.DocumentDao
import com.example.data.model.DocumentEntity
import com.example.data.model.DocumentRevisionSnapshot
import com.example.data.model.RichDocument
import com.example.data.service.DocumentExportService
import com.example.data.service.DocumentImportService
import com.example.data.storage.DocumentStorageManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID

class DocumentRepositoryImpl(
    private val documentDao: DocumentDao,
    private val storageManager: DocumentStorageManager,
    private val exportService: DocumentExportService,
    private val importService: DocumentImportService
) : DocumentRepository {

    private val snapshotManager = DocumentSnapshotManager(documentDao, storageManager)

    override fun getAllDocuments(): Flow<List<DocumentEntity>> {
        return documentDao.getAllDocuments()
    }

    override fun searchDocuments(query: String): Flow<List<DocumentEntity>> {
        return documentDao.searchDocuments(query)
    }

    override suspend fun getDocument(id: String): DocumentResult<RichDocument> {
        return storageManager.loadFromInternalStorage(id)
    }

    override suspend fun saveDocument(document: RichDocument): DocumentResult<Unit> =
        withContext(Dispatchers.IO) {
            val now = System.currentTimeMillis()
            val updatedDoc = document.copy(updatedAt = now)

            val saveResult = storageManager.saveToInternalStorage(updatedDoc)
            if (saveResult is DocumentResult.Failure) {
                return@withContext DocumentResult.Failure(saveResult.error)
            }

            val entity = DocumentEntity(
                id = updatedDoc.id,
                title = updatedDoc.title.ifBlank { "Untitled Document" },
                snippet = updatedDoc.plainTextSnippet,
                wordCount = updatedDoc.wordCount,
                characterCount = updatedDoc.characterCount,
                pageCount = updatedDoc.estimatedPageCount,
                createdAt = updatedDoc.createdAt,
                updatedAt = now,
                filePath = "documents/${updatedDoc.id}.swp",
                pageSize = updatedDoc.layoutConfig.pageSize.name,
                orientation = updatedDoc.layoutConfig.orientation.name,
                marginPreset = updatedDoc.layoutConfig.marginPreset.name,
                hasRecoveryDraft = false,
                isPasswordProtected = updatedDoc.isPasswordProtected
            )

            documentDao.insertOrUpdateDocument(entity)
            storageManager.clearRecoveryDraft(updatedDoc.id)
            DocumentResult.Success(Unit)
        }

    override suspend fun saveDocumentAs(
        originalDoc: RichDocument,
        newTitle: String
    ): DocumentResult<RichDocument> = withContext(Dispatchers.IO) {
        val newId = UUID.randomUUID().toString()
        val now = System.currentTimeMillis()
        val title = newTitle.ifBlank { "${originalDoc.title} (Copy)" }

        val newDoc = originalDoc.copy(
            id = newId,
            title = title,
            createdAt = now,
            updatedAt = now
        )

        val saveRes = saveDocument(newDoc)
        if (saveRes is DocumentResult.Failure) {
            return@withContext DocumentResult.Failure(saveRes.error)
        }
        DocumentResult.Success(newDoc)
    }

    override fun hasRecoveryDraft(id: String): Boolean {
        return storageManager.hasRecoveryDraft(id)
    }

    override suspend fun loadRecoveryDraft(id: String): DocumentResult<RichDocument> =
        withContext(Dispatchers.IO) {
            storageManager.loadRecoveryDraft(id)
        }

    override suspend fun saveRecoveryDraft(document: RichDocument): DocumentResult<Unit> =
        withContext(Dispatchers.IO) {
            val res = storageManager.saveRecoveryDraft(document)
            if (res is DocumentResult.Success) {
                documentDao.setHasRecoveryDraft(document.id, true)
                DocumentResult.Success(Unit)
            } else if (res is DocumentResult.Failure) {
                DocumentResult.Failure(res.error)
            } else {
                DocumentResult.Success(Unit)
            }
        }

    override suspend fun clearRecoveryDraft(id: String): DocumentResult<Unit> =
        withContext(Dispatchers.IO) {
            storageManager.clearRecoveryDraft(id)
            documentDao.setHasRecoveryDraft(id, false)
            DocumentResult.Success(Unit)
        }

    override suspend fun createDocument(
        title: String,
        template: TemplateType
    ): DocumentResult<RichDocument> = withContext(Dispatchers.IO) {
        val newId = UUID.randomUUID().toString()
        val now = System.currentTimeMillis()
        val doc = DocumentTemplateFactory.createTemplate(template, newId, title, now)

        val saveRes = saveDocument(doc)
        if (saveRes is DocumentResult.Failure) {
            return@withContext DocumentResult.Failure(saveRes.error)
        }
        DocumentResult.Success(doc)
    }

    override suspend fun duplicateDocument(id: String): DocumentResult<RichDocument> =
        withContext(Dispatchers.IO) {
            when (val originalRes = getDocument(id)) {
                is DocumentResult.Failure -> originalRes
                is DocumentResult.Success -> {
                    val original = originalRes.data
                    val newId = UUID.randomUUID().toString()
                    val now = System.currentTimeMillis()
                    val duplicated = original.copy(
                        id = newId,
                        title = "${original.title} (Copy)",
                        createdAt = now,
                        updatedAt = now
                    )
                    saveDocument(duplicated)
                    DocumentResult.Success(duplicated)
                }
            }
        }

    override suspend fun renameDocument(id: String, newTitle: String): DocumentResult<Unit> =
        withContext(Dispatchers.IO) {
            val now = System.currentTimeMillis()
            documentDao.renameDocument(id, newTitle, now)
            val docRes = storageManager.loadFromInternalStorage(id)
            if (docRes is DocumentResult.Success) {
                val updated = docRes.data.copy(title = newTitle, updatedAt = now)
                storageManager.saveToInternalStorage(updated)
            }
            DocumentResult.Success(Unit)
        }

    override suspend fun deleteDocument(id: String): DocumentResult<Unit> =
        withContext(Dispatchers.IO) {
            documentDao.deleteDocumentById(id)
            storageManager.deleteFromInternalStorage(id)
            DocumentResult.Success(Unit)
        }

    override suspend fun togglePinned(id: String, isPinned: Boolean): DocumentResult<Unit> =
        withContext(Dispatchers.IO) {
            documentDao.setPinned(id, isPinned)
            DocumentResult.Success(Unit)
        }

    override suspend fun toggleFavorite(id: String, isFavorite: Boolean): DocumentResult<Unit> =
        withContext(Dispatchers.IO) {
            documentDao.setFavorite(id, isFavorite)
            DocumentResult.Success(Unit)
        }

    override suspend fun exportToPdf(context: Context, document: RichDocument, outputFile: File): DocumentResult<File> =
        exportService.exportToPdf(context, document, outputFile)

    override suspend fun exportPdfToUri(context: Context, document: RichDocument, targetUri: Uri): DocumentResult<Unit> =
        exportService.exportPdfToUri(context, document, targetUri)

    override suspend fun exportToDocx(context: Context, document: RichDocument, outputFile: File): DocumentResult<File> =
        exportService.exportToDocx(context, document, outputFile)

    override suspend fun exportDocxToUri(context: Context, document: RichDocument, targetUri: Uri): DocumentResult<Unit> =
        exportService.exportDocxToUri(context, document, targetUri)

    override suspend fun exportToPlainText(context: Context, document: RichDocument, outputFile: File): DocumentResult<File> =
        exportService.exportToPlainText(context, document, outputFile)

    override suspend fun exportToMarkdown(context: Context, document: RichDocument, outputFile: File): DocumentResult<File> =
        exportService.exportToMarkdown(context, document, outputFile)

    override suspend fun exportMarkdownToUri(context: Context, document: RichDocument, targetUri: Uri): DocumentResult<Unit> =
        exportService.exportMarkdownToUri(context, document, targetUri)

    override suspend fun exportToEpub(context: Context, document: RichDocument, outputFile: File): DocumentResult<File> =
        exportService.exportToEpub(context, document, outputFile)

    override suspend fun exportEpubToUri(context: Context, document: RichDocument, targetUri: Uri): DocumentResult<Unit> =
        exportService.exportEpubToUri(context, document, targetUri)

    override fun printDocument(context: Context, document: RichDocument) {
        exportService.printDocument(context, document)
    }

    override fun createPrintDocumentAdapter(context: Context, document: RichDocument): android.print.PrintDocumentAdapter {
        return exportService.createPrintDocumentAdapter(context, document)
    }

    override suspend fun importFromUri(context: Context, uri: Uri): DocumentResult<RichDocument> =
        withContext(Dispatchers.IO) {
            val importedResult = importService.importDocumentFromUri(context, uri)
            if (importedResult is DocumentResult.Success) {
                val doc = importedResult.data.copy(
                    id = UUID.randomUUID().toString(),
                    createdAt = System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis()
                )
                saveDocument(doc)
                DocumentResult.Success(doc)
            } else {
                importedResult
            }
        }

    override suspend fun importDocxFromUri(
        context: Context,
        uri: Uri
    ): DocumentResult<com.example.data.service.docx.DocxImportResult> = withContext(Dispatchers.IO) {
        val importedResult = importService.importDocxFromUri(context, uri)
        if (importedResult is DocumentResult.Success) {
            val doc = importedResult.data.document.copy(
                id = UUID.randomUUID().toString(),
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
            saveDocument(doc)
            DocumentResult.Success(importedResult.data.copy(document = doc))
        } else {
            importedResult
        }
    }

    override suspend fun initializeDefaultDocumentsIfEmpty() = withContext(Dispatchers.IO) {
        val count = documentDao.getDocumentCount()
        if (count == 0) {
            createDocument("Untitled Document", TemplateType.BLANK)
        }
    }

    override fun getSnapshots(documentId: String): Flow<List<DocumentRevisionSnapshot>> =
        snapshotManager.getSnapshots(documentId)

    override suspend fun saveSnapshot(documentId: String, snapshot: DocumentRevisionSnapshot): DocumentResult<Unit> =
        snapshotManager.saveSnapshot(documentId, snapshot) { getDocument(documentId) }

    override suspend fun getSnapshot(snapshotId: String): DocumentResult<DocumentRevisionSnapshot> =
        snapshotManager.getSnapshot(snapshotId)

    override suspend fun deleteSnapshot(snapshotId: String): DocumentResult<Unit> =
        snapshotManager.deleteSnapshot(snapshotId)
}
