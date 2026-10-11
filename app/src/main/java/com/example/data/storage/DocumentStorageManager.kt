package com.example.data.storage

import android.content.Context
import android.net.Uri
import com.example.core.error.DocumentError
import com.example.core.error.DocumentResult
import com.example.core.layout.MarginPreset
import com.example.core.layout.PageLayoutConfig
import com.example.core.layout.PageOrientation
import com.example.core.layout.PageSize
import com.example.data.model.BlockElement
import com.example.data.model.DividerBlock
import com.example.data.model.HeadingStyle
import com.example.data.model.ImageBlock
import com.example.data.model.InlineSpan
import com.example.data.model.ListType
import com.example.data.model.PageBreakBlock
import com.example.data.model.ParagraphBlock
import com.example.data.model.RichDocument
import com.example.data.model.TableCell
import com.example.data.model.TableBlock
import com.example.data.model.TextAlignment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
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
                    } catch (e: Exception) {
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
                    } catch (e: Exception) {
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
                } else {
                    // Plain text fallback
                    createDocumentFromPlainText("Imported Document", content)
                }
                DocumentResult.Success(document)
            } catch (e: Exception) {
                DocumentResult.Failure(
                    DocumentError.StorageError("SAF import failed: ${e.message}", e)
                )
            }
        }

    /**
     * Creates a new RichDocument from raw plain text.
     */
    fun createDocumentFromPlainText(title: String, text: String): RichDocument {
        val lines = text.lines()
        val blocks = if (lines.isEmpty() || text.isBlank()) {
            listOf(ParagraphBlock())
        } else {
            lines.map { line ->
                val trimmed = line.trim()
                val isUrduOrArabic = isTextRtl(trimmed)
                ParagraphBlock(
                    text = line,
                    isRtl = isUrduOrArabic
                )
            }
        }
        return RichDocument(
            title = title,
            blocks = blocks
        )
    }

    /**
     * Detection helper for Right-to-Left script (Urdu, Arabic, Hebrew).
     */
    private fun isTextRtl(text: String): Boolean {
        for (char in text) {
            val directionality = Character.getDirectionality(char)
            if (directionality == Character.DIRECTIONALITY_RIGHT_TO_LEFT ||
                directionality == Character.DIRECTIONALITY_RIGHT_TO_LEFT_ARABIC
            ) {
                return true
            }
        }
        return false
    }

    // ----------------------------------------------------
    // JSON Serialization & Deserialization
    // ----------------------------------------------------

    private fun serializeToJson(doc: RichDocument): String {
        val root = JSONObject().apply {
            put("id", doc.id)
            put("title", doc.title)
            put("author", doc.author)
            put("createdAt", doc.createdAt)
            put("updatedAt", doc.updatedAt)

            // Layout
            val layoutObj = JSONObject().apply {
                put("pageSize", doc.layoutConfig.pageSize.name)
                put("orientation", doc.layoutConfig.orientation.name)
                put("marginPreset", doc.layoutConfig.marginPreset.name)
                doc.layoutConfig.customTopMarginMm?.let { put("customTopMarginMm", it.toDouble()) }
                doc.layoutConfig.customBottomMarginMm?.let { put("customBottomMarginMm", it.toDouble()) }
                doc.layoutConfig.customLeftMarginMm?.let { put("customLeftMarginMm", it.toDouble()) }
                doc.layoutConfig.customRightMarginMm?.let { put("customRightMarginMm", it.toDouble()) }
                put("showHeader", doc.layoutConfig.showHeader)
                put("showFooter", doc.layoutConfig.showFooter)
                put("headerText", doc.layoutConfig.headerText)
                put("footerText", doc.layoutConfig.footerText)
                put("headerAlignment", doc.layoutConfig.headerAlignment.name)
                put("footerAlignment", doc.layoutConfig.footerAlignment.name)
                put("pageBackgroundHex", doc.layoutConfig.pageBackgroundHex)
                put("borderStyle", doc.layoutConfig.borderStyle.name)
                put("borderColorHex", doc.layoutConfig.borderColorHex)
                put("borderWidthPt", doc.layoutConfig.borderWidthPt.toDouble())
                put("pageNumberFormat", doc.layoutConfig.pageNumberFormat.name)
                put("pageNumberStart", doc.layoutConfig.pageNumberStart)
                put("differentFirstPage", doc.layoutConfig.differentFirstPage)
            }
            put("layoutConfig", layoutObj)

            // Blocks
            val blocksArray = JSONArray()
            for (block in doc.blocks) {
                val blockObj = JSONObject()
                when (block) {
                    is ParagraphBlock -> {
                        blockObj.put("type", "paragraph")
                        blockObj.put("id", block.id)
                        blockObj.put("text", block.text)
                        blockObj.put("alignment", block.alignment.name)
                        blockObj.put("headingStyle", block.headingStyle.name)
                        blockObj.put("listType", block.listType.name)
                        blockObj.put("indentationLevel", block.indentationLevel)
                        blockObj.put("isRtl", block.isRtl)
                        blockObj.put("lineSpacing", block.lineSpacingMultiplier.toDouble())

                        val spansArray = JSONArray()
                        for (span in block.spans) {
                            val spanObj = JSONObject().apply {
                                put("start", span.start)
                                put("end", span.end)
                                put("isBold", span.isBold)
                                put("isItalic", span.isItalic)
                                put("isUnderline", span.isUnderline)
                                put("isStrikethrough", span.isStrikethrough)
                                put("isSuperscript", span.isSuperscript)
                                put("isSubscript", span.isSubscript)
                                span.fontSizeSp?.let { put("fontSizeSp", it.toDouble()) }
                                span.textColorHex?.let { put("textColorHex", it) }
                                span.highlightColorHex?.let { put("highlightColorHex", it) }
                                span.fontFamilyName?.let { put("fontFamilyName", it) }
                                span.linkUrl?.let { put("linkUrl", it) }
                            }
                            spansArray.put(spanObj)
                        }
                        blockObj.put("spans", spansArray)
                    }
                    is TableBlock -> {
                        blockObj.put("type", "table")
                        blockObj.put("id", block.id)
                        blockObj.put("rowCount", block.rowCount)
                        blockObj.put("columnCount", block.columnCount)
                        val cellsArray = JSONArray()
                        for (row in block.cells) {
                            val rowArray = JSONArray()
                            for (cell in row) {
                                val cellObj = JSONObject().apply {
                                    put("text", cell.text)
                                    put("isBold", cell.isBold)
                                    cell.backgroundColorHex?.let { put("bg", it) }
                                }
                                rowArray.put(cellObj)
                            }
                            cellsArray.put(rowArray)
                        }
                        blockObj.put("cells", cellsArray)
                    }
                    is ImageBlock -> {
                        blockObj.put("type", "image")
                        blockObj.put("id", block.id)
                        blockObj.put("imageUri", block.imageUri)
                        blockObj.put("caption", block.caption)
                        blockObj.put("widthDp", block.widthDp)
                        blockObj.put("heightDp", block.heightDp)
                    }
                    is DividerBlock -> {
                        blockObj.put("type", "divider")
                        blockObj.put("id", block.id)
                    }
                    is PageBreakBlock -> {
                        blockObj.put("type", "page_break")
                        blockObj.put("id", block.id)
                    }
                }
                blocksArray.put(blockObj)
            }
            put("blocks", blocksArray)
        }
        return root.toString(2)
    }

    private fun deserializeFromJson(jsonStr: String): RichDocument {
        val root = JSONObject(jsonStr)
        val id = root.optString("id", "")
        val title = root.optString("title", "Untitled Document")
        val author = root.optString("author", "Author")
        val createdAt = root.optLong("createdAt", System.currentTimeMillis())
        val updatedAt = root.optLong("updatedAt", System.currentTimeMillis())

        val layoutConfig = if (root.has("layoutConfig")) {
            val lObj = root.getJSONObject("layoutConfig")
            val pageSize = try {
                PageSize.valueOf(lObj.optString("pageSize", "A4"))
            } catch (e: Exception) { PageSize.A4 }
            val orientation = try {
                PageOrientation.valueOf(lObj.optString("orientation", "PORTRAIT"))
            } catch (e: Exception) { PageOrientation.PORTRAIT }
            val marginPreset = try {
                MarginPreset.valueOf(lObj.optString("marginPreset", "NORMAL"))
            } catch (e: Exception) { MarginPreset.NORMAL }
            val borderStyle = try {
                com.example.core.layout.PageBorderStyle.valueOf(lObj.optString("borderStyle", "NONE"))
            } catch (e: Exception) { com.example.core.layout.PageBorderStyle.NONE }
            val numberFormat = try {
                com.example.core.layout.PageNumberFormat.valueOf(lObj.optString("pageNumberFormat", "ARABIC_NUMERALS"))
            } catch (e: Exception) { com.example.core.layout.PageNumberFormat.ARABIC_NUMERALS }
            val headerAlign = try {
                TextAlignment.valueOf(lObj.optString("headerAlignment", "START"))
            } catch (e: Exception) { TextAlignment.START }
            val footerAlign = try {
                TextAlignment.valueOf(lObj.optString("footerAlignment", "CENTER"))
            } catch (e: Exception) { TextAlignment.CENTER }

            val customTop = if (lObj.has("customTopMarginMm")) lObj.getDouble("customTopMarginMm").toFloat() else null
            val customBottom = if (lObj.has("customBottomMarginMm")) lObj.getDouble("customBottomMarginMm").toFloat() else null
            val customLeft = if (lObj.has("customLeftMarginMm")) lObj.getDouble("customLeftMarginMm").toFloat() else null
            val customRight = if (lObj.has("customRightMarginMm")) lObj.getDouble("customRightMarginMm").toFloat() else null

            PageLayoutConfig(
                pageSize = pageSize,
                orientation = orientation,
                marginPreset = marginPreset,
                customTopMarginMm = customTop,
                customBottomMarginMm = customBottom,
                customLeftMarginMm = customLeft,
                customRightMarginMm = customRight,
                showHeader = lObj.optBoolean("showHeader", true),
                showFooter = lObj.optBoolean("showFooter", true),
                headerText = lObj.optString("headerText", ""),
                footerText = lObj.optString("footerText", "Page {page} of {pages}"),
                headerAlignment = headerAlign,
                footerAlignment = footerAlign,
                pageBackgroundHex = lObj.optString("pageBackgroundHex", "#FFFFFF"),
                borderStyle = borderStyle,
                borderColorHex = lObj.optString("borderColorHex", "#CBD5E1"),
                borderWidthPt = lObj.optDouble("borderWidthPt", 1.0).toFloat(),
                pageNumberFormat = numberFormat,
                pageNumberStart = lObj.optInt("pageNumberStart", 1),
                differentFirstPage = lObj.optBoolean("differentFirstPage", false)
            )
        } else {
            PageLayoutConfig()
        }

        val blocks = mutableListOf<BlockElement>()
        val blocksArray = root.optJSONArray("blocks") ?: JSONArray()
        for (i in 0 until blocksArray.length()) {
            val bObj = blocksArray.getJSONObject(i)
            val type = bObj.optString("type", "paragraph")
            val blockId = bObj.optString("id", "")
            when (type) {
                "paragraph" -> {
                    val text = bObj.optString("text", "")
                    val alignment = try {
                        TextAlignment.valueOf(bObj.optString("alignment", "START"))
                    } catch (e: Exception) { TextAlignment.START }
                    val heading = try {
                        HeadingStyle.valueOf(bObj.optString("headingStyle", "NORMAL"))
                    } catch (e: Exception) { HeadingStyle.NORMAL }
                    val listType = try {
                        ListType.valueOf(bObj.optString("listType", "NONE"))
                    } catch (e: Exception) { ListType.NONE }
                    val indentationLevel = bObj.optInt("indentationLevel", 0)
                    val isRtl = bObj.optBoolean("isRtl", false)
                    val lineSpacing = bObj.optDouble("lineSpacing", 1.25).toFloat()

                    val spans = mutableListOf<InlineSpan>()
                    val spansArr = bObj.optJSONArray("spans")
                    if (spansArr != null) {
                        for (j in 0 until spansArr.length()) {
                            val sObj = spansArr.getJSONObject(j)
                            spans.add(
                                InlineSpan(
                                    start = sObj.optInt("start", 0),
                                    end = sObj.optInt("end", 0),
                                    isBold = sObj.optBoolean("isBold", false),
                                    isItalic = sObj.optBoolean("isItalic", false),
                                    isUnderline = sObj.optBoolean("isUnderline", false),
                                    isStrikethrough = sObj.optBoolean("isStrikethrough", false),
                                    isSuperscript = sObj.optBoolean("isSuperscript", false),
                                    isSubscript = sObj.optBoolean("isSubscript", false),
                                    fontSizeSp = if (sObj.has("fontSizeSp")) sObj.getDouble("fontSizeSp").toFloat() else null,
                                    textColorHex = if (sObj.has("textColorHex")) sObj.getString("textColorHex") else null,
                                    highlightColorHex = if (sObj.has("highlightColorHex")) sObj.getString("highlightColorHex") else null,
                                    fontFamilyName = if (sObj.has("fontFamilyName")) sObj.getString("fontFamilyName") else null,
                                    linkUrl = if (sObj.has("linkUrl")) sObj.getString("linkUrl") else null
                                )
                            )
                        }
                    }

                    blocks.add(
                        ParagraphBlock(
                            id = blockId.ifBlank { java.util.UUID.randomUUID().toString() },
                            text = text,
                            spans = spans,
                            alignment = alignment,
                            headingStyle = heading,
                            listType = listType,
                            indentationLevel = indentationLevel,
                            isRtl = isRtl,
                            lineSpacingMultiplier = lineSpacing
                        )
                    )
                }
                "table" -> {
                    val rowCount = bObj.optInt("rowCount", 2)
                    val columnCount = bObj.optInt("columnCount", 2)
                    val cells = mutableListOf<List<TableCell>>()
                    val cellsArr = bObj.optJSONArray("cells")
                    if (cellsArr != null) {
                        for (r in 0 until cellsArr.length()) {
                            val rowArr = cellsArr.getJSONArray(r)
                            val row = mutableListOf<TableCell>()
                            for (c in 0 until rowArr.length()) {
                                val cObj = rowArr.getJSONObject(c)
                                row.add(
                                    TableCell(
                                        text = cObj.optString("text", ""),
                                        isBold = cObj.optBoolean("isBold", false),
                                        backgroundColorHex = if (cObj.has("bg") && !cObj.isNull("bg")) cObj.getString("bg") else null
                                    )
                                )
                            }
                            cells.add(row)
                        }
                    }
                    blocks.add(TableBlock(id = blockId, rowCount = rowCount, columnCount = columnCount, cells = cells))
                }
                "image" -> {
                    blocks.add(
                        ImageBlock(
                            id = blockId,
                            imageUri = bObj.optString("imageUri", ""),
                            caption = bObj.optString("caption", ""),
                            widthDp = bObj.optInt("widthDp", 240),
                            heightDp = bObj.optInt("heightDp", 180)
                        )
                    )
                }
                "divider" -> blocks.add(DividerBlock(id = blockId))
                "page_break" -> blocks.add(PageBreakBlock(id = blockId))
            }
        }

        if (blocks.isEmpty()) {
            blocks.add(ParagraphBlock())
        }

        return RichDocument(
            id = id.ifBlank { java.util.UUID.randomUUID().toString() },
            title = title,
            author = author,
            createdAt = createdAt,
            updatedAt = updatedAt,
            layoutConfig = layoutConfig,
            blocks = blocks
        )
    }
}
