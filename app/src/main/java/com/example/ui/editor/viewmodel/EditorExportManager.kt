package com.example.ui.editor.viewmodel

import android.content.Context
import android.net.Uri
import android.print.PrintDocumentAdapter
import androidx.lifecycle.viewModelScope
import com.example.core.error.DocumentResult
import com.example.data.model.RichDocument
import com.example.ui.editor.EditorViewModel
import kotlinx.coroutines.launch
import java.io.File

/**
 * Manages document export pipelines (PDF, DOCX, EPUB, TXT, Markdown) and printing.
 */
class EditorExportManager(
    private val vm: EditorViewModel
) {
    fun saveAs(newTitle: String, onComplete: (RichDocument?) -> Unit) {
        val currentDoc = vm.currentDocument ?: return
        vm.viewModelScope.launch {
            when (val res = vm.repository.saveDocumentAs(currentDoc, newTitle)) {
                is DocumentResult.Success -> {
                    vm.showStatusMessage("Saved copy as '${res.data.title}'")
                    onComplete(res.data)
                }
                is DocumentResult.Failure -> {
                    vm.showStatusMessage("Save As failed: ${res.error.message}")
                    onComplete(null)
                }
            }
        }
    }

    fun renameDocument(newTitle: String) {
        val currentDoc = vm.currentDocument ?: return
        val trimmed = newTitle.trim().ifBlank { "Untitled Document" }
        val updated = currentDoc.copy(title = trimmed)
        vm.updateDocumentInternal(updated)
        vm.viewModelScope.launch {
            vm.repository.renameDocument(currentDoc.id, trimmed)
            vm.historyManager.autosaveController.triggerImmediateSave()
        }
    }

    fun exportToPdf(context: Context, onComplete: (File?) -> Unit) {
        val doc = vm.currentDocument ?: return
        if (vm.isDocumentLocked.value) {
            vm.showStatusMessage("Document is locked. Enter PIN to export.")
            onComplete(null)
            return
        }
        vm.viewModelScope.launch {
            val exportDir = File(context.cacheDir, "exports").apply { mkdirs() }
            val sanitized = doc.title.replace("[^a-zA-Z0-9.-]".toRegex(), "_")
            val outFile = File(exportDir, "$sanitized.pdf")
            when (val res = vm.repository.exportToPdf(context, doc, outFile)) {
                is DocumentResult.Success -> {
                    vm.showStatusMessage("PDF Exported: ${outFile.name}")
                    onComplete(outFile)
                }
                is DocumentResult.Failure -> {
                    vm.showStatusMessage("PDF Error: ${res.error.message}")
                    onComplete(null)
                }
            }
        }
    }

    fun exportToPlainText(context: Context, onComplete: (File?) -> Unit) {
        val doc = vm.currentDocument ?: return
        if (vm.isDocumentLocked.value) {
            vm.showStatusMessage("Document is locked. Enter PIN to export.")
            onComplete(null)
            return
        }
        vm.viewModelScope.launch {
            val exportDir = File(context.cacheDir, "exports").apply { mkdirs() }
            val sanitized = doc.title.replace("[^a-zA-Z0-9.-]".toRegex(), "_")
            val outFile = File(exportDir, "$sanitized.txt")
            when (val res = vm.repository.exportToPlainText(context, doc, outFile)) {
                is DocumentResult.Success -> {
                    vm.showStatusMessage("Text Exported: ${outFile.name}")
                    onComplete(outFile)
                }
                is DocumentResult.Failure -> {
                    vm.showStatusMessage("Text Export Error: ${res.error.message}")
                    onComplete(null)
                }
            }
        }
    }

    fun exportToDocx(context: Context, onComplete: (File?) -> Unit) {
        val doc = vm.currentDocument ?: return
        if (vm.isDocumentLocked.value) {
            vm.showStatusMessage("Document is locked. Enter PIN to export.")
            onComplete(null)
            return
        }
        vm.viewModelScope.launch {
            val exportDir = File(context.cacheDir, "exports").apply { mkdirs() }
            val sanitized = doc.title.replace("[^a-zA-Z0-9.-]".toRegex(), "_")
            val outFile = File(exportDir, "$sanitized.docx")
            when (val res = vm.repository.exportToDocx(context, doc, outFile)) {
                is DocumentResult.Success -> {
                    vm.showStatusMessage("Word Document (.docx) Exported: ${outFile.name}")
                    onComplete(outFile)
                }
                is DocumentResult.Failure -> {
                    vm.showStatusMessage("DOCX Error: ${res.error.message}")
                    onComplete(null)
                }
            }
        }
    }

    fun exportDocxToSafUri(context: Context, uri: Uri) {
        val doc = vm.currentDocument ?: return
        if (vm.isDocumentLocked.value) {
            vm.showStatusMessage("Document is locked. Enter PIN to export.")
            return
        }
        vm.viewModelScope.launch {
            when (val res = vm.repository.exportDocxToUri(context, doc, uri)) {
                is DocumentResult.Success -> {
                    vm.showStatusMessage("Word Document (.docx) saved to external storage")
                }
                is DocumentResult.Failure -> {
                    vm.showStatusMessage("Save DOCX failed: ${res.error.message}")
                }
            }
        }
    }

    fun exportPdfToSafUri(context: Context, uri: Uri) {
        val doc = vm.currentDocument ?: return
        if (vm.isDocumentLocked.value) {
            vm.showStatusMessage("Document is locked. Enter PIN to export.")
            return
        }
        vm.viewModelScope.launch {
            when (val res = vm.repository.exportPdfToUri(context, doc, uri)) {
                is DocumentResult.Success -> {
                    vm.showStatusMessage("PDF saved to external storage")
                }
                is DocumentResult.Failure -> {
                    vm.showStatusMessage("Save to SAF failed: ${res.error.message}")
                }
            }
        }
    }

    fun printDocument(context: Context) {
        val doc = vm.currentDocument ?: return
        vm.repository.printDocument(context, doc)
    }

    fun createPrintDocumentAdapter(context: Context): PrintDocumentAdapter? {
        val doc = vm.currentDocument ?: return null
        return vm.repository.createPrintDocumentAdapter(context, doc)
    }

    fun exportToMarkdown(context: Context, onComplete: (File?) -> Unit) {
        val doc = vm.currentDocument ?: return
        vm.viewModelScope.launch {
            val exportDir = File(context.cacheDir, "exports").apply { mkdirs() }
            val sanitized = doc.title.replace("[^a-zA-Z0-9.-]".toRegex(), "_")
            val outFile = File(exportDir, "$sanitized.md")
            when (val res = vm.repository.exportToMarkdown(context, doc, outFile)) {
                is DocumentResult.Success -> {
                    vm.showStatusMessage("Markdown (.md) Exported: ${outFile.name}")
                    onComplete(outFile)
                }
                is DocumentResult.Failure -> {
                    vm.showStatusMessage("Markdown Error: ${res.error.message}")
                    onComplete(null)
                }
            }
        }
    }

    fun exportToEpub(context: Context, onComplete: (File?) -> Unit) {
        val doc = vm.currentDocument ?: return
        vm.viewModelScope.launch {
            val exportDir = File(context.cacheDir, "exports").apply { mkdirs() }
            val sanitized = doc.title.replace("[^a-zA-Z0-9.-]".toRegex(), "_")
            val outFile = File(exportDir, "$sanitized.epub")
            when (val res = vm.repository.exportToEpub(context, doc, outFile)) {
                is DocumentResult.Success -> {
                    vm.showStatusMessage("EPUB E-Book Exported: ${outFile.name}")
                    onComplete(outFile)
                }
                is DocumentResult.Failure -> {
                    vm.showStatusMessage("EPUB Error: ${res.error.message}")
                    onComplete(null)
                }
            }
        }
    }
}
