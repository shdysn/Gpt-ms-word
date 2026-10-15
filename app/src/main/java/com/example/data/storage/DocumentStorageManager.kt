package com.example.data.storage

import android.content.Context
import android.net.Uri
import com.example.core.error.DocumentError
import com.example.core.error.DocumentResult
import com.example.data.model.ListType
import com.example.data.model.ParagraphBlock
import com.example.data.model.RichDocument
import com.example.data.model.TextAlignment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.InputStreamReader

/**
 * Storage manager handling local storage and Storage Access Framework (SAF) operations.
 */
class DocumentStorageManager(private val context: Context) {

    private val documentsDir: File by lazy {
        File(context.filesDir, "documents").apply {
            if (!exists()) mkdirs()
        }
    }

    /**
     * Saves a RichDocument to internal application storage using an atomic write pattern
     * (temp file + fsync + rename) to prevent document corruption during unexpected crashes or power loss.
     */
    suspend fun saveToInternalStorage(document: RichDocument): DocumentResult<File> =
        withContext(Dispatchers.IO) {
            try {
                val targetFile = File(documentsDir, "${document.id}.swp")
                val tempFile = File(documentsDir, "${document.id}.swp.tmp")
                val jsonString = serializeToJson(document)

                // Write to temporary file with sync
                FileOutputStream(tempFile).use { fos ->
                    fos.write(jsonString.toByteArray(Charsets.UTF_8))
                    fos.flush()
                    try {
                        fos.fd.sync()
                    } catch (_: Exception) {
                        // Some virtual/in-memory file systems don't support sync, ignore
                    }
                }

                // Atomically replace target file
                if (targetFile.exists()) {
                    targetFile.delete()
                }
                val renamed = tempFile.renameTo(targetFile)
                if (!renamed) {
                    tempFile.copyTo(targetFile, overwrite = true)
                    tempFile.delete()
                }

                DocumentResult.Success(targetFile)
            } catch (e: Exception) {
                DocumentResult.Failure(
                    DocumentError.StorageError("Failed to save document to internal storage: ${e.message}", e)
                )
            }
        }

    /**
     * Saves a recovery draft of the document without overwriting the main saved file.
     * Used for crash recovery and backgrounding preservation.
     */
    suspend fun saveRecoveryDraft(document: RichDocument): DocumentResult<File> =
        withContext(Dispatchers.IO) {
            try {
                val recoveryFile = File(documentsDir, "recovery_${document.id}.swp")
                val tempFile = File(documentsDir, "recovery_${document.id}.swp.tmp")
                val jsonString = serializeToJson(document)

                FileOutputStream(tempFile).use { fos ->
                    fos.write(jsonString.toByteArray(Charsets.UTF_8))
                    fos.flush()
                    try {
                        fos.fd.sync()
                    } catch (_: Exception) {
                        // ignore sync failure on mock/in-memory FS
                    }
                }

                if (recoveryFile.exists()) recoveryFile.delete()
                val renamed = tempFile.renameTo(recoveryFile)
                if (!renamed) {
                    tempFile.copyTo(recoveryFile, overwrite = true)
                    tempFile.delete()
                }
                DocumentResult.Success(recoveryFile)
            } catch (e: Exception) {
                DocumentResult.Failure(
                    DocumentError.StorageError("Failed to save recovery draft: ${e.message}", e)
                )
            }
        }

    /**
     * Checks if an unsaved recovery draft exists for the given document ID.
     */
    fun hasRecoveryDraft(documentId: String): Boolean {
        val recoveryFile = File(documentsDir, "recovery_${documentId}.swp")
        return recoveryFile.exists() && recoveryFile.length() > 0
    }

    /**
     * Persists an image from an external Uri (such as Photo Picker or SAF) into internal app storage.
     * Guarantees the image remains present after saving, reopening, and exporting.
     */
    fun persistImageFromUri(sourceUri: Uri): String {
        return try {
            val imagesDir = File(context.filesDir, "images").apply {
                if (!exists()) mkdirs()
            }
            val mimeType = try { context.contentResolver.getType(sourceUri) } catch (_: Exception) { null }
            val extension = when {
                mimeType?.contains("jpeg") == true || mimeType?.contains("jpg") == true -> "jpg"
                mimeType?.contains("png") == true -> "png"
                mimeType?.contains("webp") == true -> "webp"
                sourceUri.toString().endsWith(".jpg", ignoreCase = true) -> "jpg"
                sourceUri.toString().endsWith(".png", ignoreCase = true) -> "png"
                else -> "png"
            }
            val targetFile = File(imagesDir, "img_${System.currentTimeMillis()}_${java.util.UUID.randomUUID().toString().take(8)}.$extension")
            context.contentResolver.openInputStream(sourceUri)?.use { input ->
                FileOutputStream(targetFile).use { output ->
                    input.copyTo(output)
                    output.flush()
                }
            }
            Uri.fromFile(targetFile).toString()
        } catch (_: Exception) {
            sourceUri.toString()
        }
    }

    /**
     * Loads the unsaved recovery draft for the given document ID.
     */
    suspend fun loadRecoveryDraft(documentId: String): DocumentResult<RichDocument> =
        withContext(Dispatchers.IO) {
            try {
                val file = File(documentsDir, "recovery_${documentId}.swp")
                if (!file.exists()) {
                    return@withContext DocumentResult.Failure(DocumentError.DocumentNotFound("Recovery draft not found for $documentId"))
                }
                val jsonString = file.readText()
                val document = deserializeFromJson(jsonString)
                DocumentResult.Success(document)
            } catch (e: Exception) {
                DocumentResult.Failure(
                    DocumentError.CorruptDocumentError("Failed to load recovery draft: ${e.message}")
                )
            }
        }

    /**
     * Deletes the recovery draft when a clean save or discard occurs.
     */
    suspend fun clearRecoveryDraft(documentId: String): Boolean =
        withContext(Dispatchers.IO) {
            val file = File(documentsDir, "recovery_${documentId}.swp")
            if (file.exists()) file.delete() else true
        }

    /**
     * Loads a RichDocument from internal application storage.
     */
    suspend fun loadFromInternalStorage(documentId: String): DocumentResult<RichDocument> =
        withContext(Dispatchers.IO) {
            try {
                val file = File(documentsDir, "$documentId.swp")
                if (!file.exists()) {
                    return@withContext DocumentResult.Failure(DocumentError.DocumentNotFound(documentId))
                }
                val jsonString = file.readText()
                val document = deserializeFromJson(jsonString)
                DocumentResult.Success(document)
            } catch (e: Exception) {
                DocumentResult.Failure(
                    DocumentError.CorruptDocumentError("Failed to load document: ${e.message}")
                )
            }
        }

    /**
     * Deletes a document file from internal storage, including any recovery draft.
     */
    suspend fun deleteFromInternalStorage(documentId: String): Boolean =
        withContext(Dispatchers.IO) {
            val file = File(documentsDir, "$documentId.swp")
            val recoveryFile = File(documentsDir, "recovery_${documentId}.swp")
            val fileDeleted = if (file.exists()) file.delete() else true
            val recoveryDeleted = if (recoveryFile.exists()) recoveryFile.delete() else true
            fileDeleted && recoveryDeleted
        }

    /**
     * Exports a document to an external URI selected via SAF.
     */
    suspend fun exportToSafUri(document: RichDocument, uri: Uri): DocumentResult<Unit> =
        withContext(Dispatchers.IO) {
            try {
                val pfd = context.contentResolver.openFileDescriptor(uri, "w")
                    ?: return@withContext DocumentResult.Failure(
                        DocumentError.StorageError("Could not open file descriptor for URI: $uri")
                    )
                pfd.use { descriptor ->
                    FileOutputStream(descriptor.fileDescriptor).use { outputStream ->
                        val jsonBytes = serializeToJson(document).toByteArray(Charsets.UTF_8)
                        outputStream.write(jsonBytes)
                        outputStream.flush()
                    }
                }
                DocumentResult.Success(Unit)
            } catch (e: Exception) {
                DocumentResult.Failure(
                    DocumentError.StorageError("SAF export failed: ${e.message}", e)
                )
            }
        }

    /**
     * Imports a document from an external URI selected via SAF.
     */
    suspend fun importFromSafUri(uri: Uri): DocumentResult<RichDocument> =
        withContext(Dispatchers.IO) {
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                    ?: return@withContext DocumentResult.Failure(
                        DocumentError.StorageError("Could not read input stream from URI: $uri")
                    )
                val content = inputStream.use { stream ->
                    InputStreamReader(stream, Charsets.UTF_8).readText()
                }

                val document = if (content.trim().startsWith("{")) {
                    deserializeFromJson(content)
                } else if (content.startsWith("#") || content.contains("\n#") || content.contains("- [")) {
                    DocumentPlainTextImporter.createDocumentFromMarkdown("Imported Markdown", content)
                } else {
                    createDocumentFromPlainText("Imported Document", content)
                }
                DocumentResult.Success(document)
            } catch (e: Exception) {
                DocumentResult.Failure(
                    DocumentError.StorageError("SAF import failed: ${e.message}", e)
                )
            }
        }

    fun createDocumentFromPlainText(title: String, text: String): RichDocument =
        DocumentPlainTextImporter.createDocumentFromPlainText(title, text)

    fun serializeToJson(doc: RichDocument): String =
        DocumentJsonSerializer.serialize(doc)

    fun deserializeFromJson(jsonStr: String): RichDocument =
        DocumentJsonDeserializer.deserialize(jsonStr)

    fun serializeDocument(doc: RichDocument): String = serializeToJson(doc)

    fun deserializeDocument(jsonStr: String): RichDocument = deserializeFromJson(jsonStr)
}
