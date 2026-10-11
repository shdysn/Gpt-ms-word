package com.example.data.service.docx

import android.content.Context
import android.net.Uri
import com.example.core.error.DocumentError
import com.example.core.error.DocumentResult
import com.example.data.model.RichDocument
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream

interface DocxService {
    suspend fun exportToDocx(context: Context, document: RichDocument, outputFile: File): DocumentResult<File>
    suspend fun exportDocxToUri(context: Context, document: RichDocument, targetUri: Uri): DocumentResult<Unit>
    suspend fun importFromUri(context: Context, uri: Uri): DocumentResult<DocxImportResult>
    suspend fun importFromFile(context: Context, file: File): DocumentResult<DocxImportResult>
    fun isDocxFile(context: Context, uri: Uri): Boolean
}

class DocxServiceImpl : DocxService {

    override suspend fun exportToDocx(
        context: Context,
        document: RichDocument,
        outputFile: File
    ): DocumentResult<File> = withContext(Dispatchers.IO) {
        try {
            outputFile.parentFile?.mkdirs()
            // Write to a temporary file first, then atomically replace to protect existing files
            val tempFile = File(outputFile.parentFile, "${outputFile.name}.tmp_${System.currentTimeMillis()}")
            FileOutputStream(tempFile).use { fos ->
                val writer = DocxPackageWriter(context)
                writer.write(document, fos)
                fos.flush()
            }
            if (outputFile.exists()) {
                outputFile.delete()
            }
            if (!tempFile.renameTo(outputFile)) {
                tempFile.copyTo(outputFile, overwrite = true)
                tempFile.delete()
            }
            DocumentResult.Success(outputFile)
        } catch (e: Exception) {
            DocumentResult.Failure(
                DocumentError.ExportError("DOCX export failed: ${e.message}", "DOCX", e)
            )
        }
    }

    override suspend fun exportDocxToUri(
        context: Context,
        document: RichDocument,
        targetUri: Uri
    ): DocumentResult<Unit> = withContext(Dispatchers.IO) {
        try {
            val pfd = context.contentResolver.openFileDescriptor(targetUri, "w")
                ?: return@withContext DocumentResult.Failure(
                    DocumentError.ExportError("Could not open file descriptor for URI: $targetUri", "DOCX")
                )
            pfd.use { descriptor ->
                FileOutputStream(descriptor.fileDescriptor).use { outputStream ->
                    val writer = DocxPackageWriter(context)
                    writer.write(document, outputStream)
                    outputStream.flush()
                }
            }
            DocumentResult.Success(Unit)
        } catch (e: Exception) {
            DocumentResult.Failure(
                DocumentError.ExportError("Save DOCX to SAF URI failed: ${e.message}", "DOCX", e)
            )
        }
    }

    override suspend fun importFromUri(
        context: Context,
        uri: Uri
    ): DocumentResult<DocxImportResult> = withContext(Dispatchers.IO) {
        try {
            val inputStream = context.contentResolver.openInputStream(uri)
                ?: return@withContext DocumentResult.Failure(
                    DocumentError.StorageError("Could not open input stream for URI: $uri")
                )
            inputStream.use { stream ->
                val reader = DocxPackageReader(context)
                reader.read(stream)
            }
        } catch (e: Exception) {
            DocumentResult.Failure(
                DocumentError.StorageError("DOCX import from URI failed: ${e.message}", e)
            )
        }
    }

    override suspend fun importFromFile(
        context: Context,
        file: File
    ): DocumentResult<DocxImportResult> = withContext(Dispatchers.IO) {
        try {
            if (!file.exists()) {
                return@withContext DocumentResult.Failure(
                    DocumentError.DocumentNotFound("File not found: ${file.absolutePath}")
                )
            }
            FileInputStream(file).use { fis ->
                val reader = DocxPackageReader(context)
                reader.read(fis)
            }
        } catch (e: Exception) {
            DocumentResult.Failure(
                DocumentError.StorageError("DOCX import from file failed: ${e.message}", e)
            )
        }
    }

    override fun isDocxFile(context: Context, uri: Uri): Boolean {
        // Check MIME type or filename or ZIP header magic (PK\x03\x04)
        return try {
            val type = context.contentResolver.getType(uri)
            if (type == "application/vnd.openxmlformats-officedocument.wordprocessingml.document") {
                return true
            }
            val uriStr = uri.toString().lowercase()
            if (uriStr.endsWith(".docx")) return true

            context.contentResolver.openInputStream(uri)?.use { stream ->
                val header = ByteArray(4)
                val read = stream.read(header)
                read == 4 && header[0] == 0x50.toByte() && header[1] == 0x4B.toByte() &&
                        header[2] == 0x03.toByte() && header[3] == 0x04.toByte()
            } ?: false
        } catch (e: Exception) {
            false
        }
    }
}
