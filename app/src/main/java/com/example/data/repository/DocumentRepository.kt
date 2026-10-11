package com.example.data.repository

import android.content.Context
import android.net.Uri
import com.example.core.error.DocumentResult
import com.example.data.model.DocumentEntity
import com.example.data.model.RichDocument
import kotlinx.coroutines.flow.Flow
import java.io.File

/**
 * Starter template types available in Smart Word Processor.
 */
enum class TemplateType(val title: String, val description: String) {
    BLANK("Blank document", "Start from scratch with clean A4 layout"),
    BUSINESS_LETTER("Letter", "Formal letter format with address, salutation, body, and closing"),
    PROJECT_REPORT("Report", "Cover layout, executive summary, numbered sections, and data table"),
    APPLICATION("Application", "Formal job, leave, or academic application format"),
    URDU_DOCUMENT("Simple Urdu document", "Urdu Nastaliq RTL document with title, paragraphs, and closing"),
    MEETING_MINUTES("Meeting Minutes", "Attendees, agenda items, decisions, and action tables"),
    PAGINATION_SHOWCASE("Multi-Page Specification", "Comprehensive multi-page whitepaper with page breaks, tables, and headers")
}

/**
 * Primary repository interface abstracting document persistence, storage, and lifecycle.
 */
interface DocumentRepository {

    fun getAllDocuments(): Flow<List<DocumentEntity>>

    fun searchDocuments(query: String): Flow<List<DocumentEntity>>

    suspend fun getDocument(id: String): DocumentResult<RichDocument>

    suspend fun saveDocument(document: RichDocument): DocumentResult<Unit>

    suspend fun createDocument(title: String, template: TemplateType = TemplateType.BLANK): DocumentResult<RichDocument>

    suspend fun duplicateDocument(id: String): DocumentResult<RichDocument>

    suspend fun renameDocument(id: String, newTitle: String): DocumentResult<Unit>

    suspend fun deleteDocument(id: String): DocumentResult<Unit>

    suspend fun togglePinned(id: String, isPinned: Boolean): DocumentResult<Unit>

    suspend fun toggleFavorite(id: String, isFavorite: Boolean): DocumentResult<Unit>

    suspend fun exportToPdf(context: Context, document: RichDocument, outputFile: File): DocumentResult<File>

    suspend fun exportPdfToUri(context: Context, document: RichDocument, targetUri: Uri): DocumentResult<Unit>

    suspend fun exportToDocx(context: Context, document: RichDocument, outputFile: File): DocumentResult<File>

    suspend fun exportDocxToUri(context: Context, document: RichDocument, targetUri: Uri): DocumentResult<Unit>

    suspend fun exportToPlainText(context: Context, document: RichDocument, outputFile: File): DocumentResult<File>

    fun printDocument(context: Context, document: RichDocument)

    suspend fun importFromUri(context: Context, uri: Uri): DocumentResult<RichDocument>

    suspend fun importDocxFromUri(context: Context, uri: Uri): DocumentResult<com.example.data.service.docx.DocxImportResult>

    suspend fun saveDocumentAs(originalDoc: RichDocument, newTitle: String): DocumentResult<RichDocument>

    fun hasRecoveryDraft(id: String): Boolean

    suspend fun loadRecoveryDraft(id: String): DocumentResult<RichDocument>

    suspend fun saveRecoveryDraft(document: RichDocument): DocumentResult<Unit>

    suspend fun clearRecoveryDraft(id: String): DocumentResult<Unit>

    suspend fun initializeDefaultDocumentsIfEmpty()
}
