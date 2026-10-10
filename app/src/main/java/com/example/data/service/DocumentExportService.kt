package com.example.data.service

import android.content.Context
import android.net.Uri
import com.example.core.error.DocumentResult
import com.example.data.model.RichDocument
import java.io.File

/**
 * Service interface for exporting documents to various distribution formats.
 */
interface DocumentExportService {

    /**
     * Exports a document as an ISO-compliant PDF document to internal cache or target file.
     */
    suspend fun exportToPdf(context: Context, document: RichDocument, outputFile: File): DocumentResult<File>

    /**
     * Direct print job dispatcher using Android's PrintManager framework.
     */
    fun printDocument(context: Context, document: RichDocument, jobName: String = document.title)

    /**
     * Interface hook for Microsoft Word (.docx) packaging (Phase 2 OpenXML integration).
     */
    suspend fun exportToDocx(context: Context, document: RichDocument, outputFile: File): DocumentResult<File>

    /**
     * Exports as standard UTF-8 plain text file.
     */
    suspend fun exportToPlainText(context: Context, document: RichDocument, outputFile: File): DocumentResult<File>

    /**
     * Writes document PDF directly into a Storage Access Framework (SAF) URI.
     */
    suspend fun exportPdfToUri(context: Context, document: RichDocument, targetUri: Uri): DocumentResult<Unit>
}
