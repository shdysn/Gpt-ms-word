package com.example.ui.home

import android.content.Context
import android.net.Uri
import com.example.core.error.DocumentResult
import com.example.data.repository.DocumentRepository
import java.io.File

/**
 * Handles PDF/DOCX exporting, printing, and document importing for the Home screen.
 */
class HomeExportImportHelper(
    private val repository: DocumentRepository
) {
    suspend fun exportPdf(context: Context, docId: String, onMessage: (String) -> Unit): File? {
        return when (val docRes = repository.getDocument(docId)) {
            is DocumentResult.Success -> {
                val exportDir = File(context.cacheDir, "exports").apply { mkdirs() }
                val sanitizedTitle = docRes.data.title.replace("[^a-zA-Z0-9.-]".toRegex(), "_")
                val outFile = File(exportDir, "$sanitizedTitle.pdf")
                when (val exportRes = repository.exportToPdf(context, docRes.data, outFile)) {
                    is DocumentResult.Success -> {
                        onMessage("PDF exported: ${outFile.name}")
                        outFile
                    }
                    is DocumentResult.Failure -> {
                        onMessage("PDF export error: ${exportRes.error.message}")
                        null
                    }
                }
            }
            is DocumentResult.Failure -> {
                onMessage("Could not load document: ${docRes.error.message}")
                null
            }
        }
    }

    suspend fun exportDocx(context: Context, docId: String, onMessage: (String) -> Unit): File? {
        return when (val docRes = repository.getDocument(docId)) {
            is DocumentResult.Success -> {
                val exportDir = File(context.cacheDir, "exports").apply { mkdirs() }
                val sanitizedTitle = docRes.data.title.replace("[^a-zA-Z0-9.-]".toRegex(), "_")
                val outFile = File(exportDir, "$sanitizedTitle.docx")
                when (val exportRes = repository.exportToDocx(context, docRes.data, outFile)) {
                    is DocumentResult.Success -> {
                        onMessage("Word Document (.docx) exported: ${outFile.name}")
                        outFile
                    }
                    is DocumentResult.Failure -> {
                        onMessage("DOCX export error: ${exportRes.error.message}")
                        null
                    }
                }
            }
            is DocumentResult.Failure -> {
                onMessage("Could not load document: ${docRes.error.message}")
                null
            }
        }
    }

    suspend fun printDocument(context: Context, docId: String, onError: (String) -> Unit) {
        when (val docRes = repository.getDocument(docId)) {
            is DocumentResult.Success -> {
                repository.printDocument(context, docRes.data)
            }
            is DocumentResult.Failure -> {
                onError("Could not load document for print: ${docRes.error.message}")
            }
        }
    }

    suspend fun importFromUri(context: Context, uri: Uri, onSuccess: (String, String) -> Unit, onError: (String) -> Unit) {
        when (val res = repository.importFromUri(context, uri)) {
            is DocumentResult.Success -> {
                onSuccess(res.data.id, res.data.title)
            }
            is DocumentResult.Failure -> {
                onError("Import failed: ${res.error.message}")
            }
        }
    }
}
