package com.example.data.service.docx

import android.content.Context
import android.net.Uri
import com.example.core.error.DocumentError
import com.example.core.error.DocumentResult
import com.example.core.layout.PageLayoutConfig
import com.example.data.model.BlockElement
import com.example.data.model.DocumentComment
import com.example.data.model.FootnoteBlock
import com.example.data.model.ListType
import com.example.data.model.ParagraphBlock
import com.example.data.model.RichDocument
import org.w3c.dom.Element
import org.w3c.dom.Node
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.util.UUID
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

/**
 * Robust native parser for Microsoft Word (.docx) files.
 * Validates package integrity, parses typography, headings, tables, images,
 * and page layout properties, and flags unsupported advanced Word features.
 */
class DocxPackageReader(private val context: Context?) {

    fun read(inputStream: InputStream): DocumentResult<DocxImportResult> {
        return try {
            val zipEntries = mutableMapOf<String, ByteArray>()
            val zipIn = ZipInputStream(inputStream)
            var entry: ZipEntry? = zipIn.nextEntry

            var isZip = false
            while (entry != null) {
                isZip = true
                val name = entry.name
                if (!name.contains("..")) {
                    zipEntries[name] = zipIn.readBytes()
                }
                zipIn.closeEntry()
                entry = zipIn.nextEntry
            }

            if (!isZip || (!zipEntries.containsKey("word/document.xml") && !zipEntries.containsKey("[Content_Types].xml"))) {
                return DocumentResult.Failure(
                    DocumentError.CorruptDocumentError("The selected file is not a valid Microsoft Word (.docx) document package.")
                )
            }

            val warnings = mutableListOf<String>()

            if (zipEntries.keys.any { it.startsWith("word/vbaProject") || it.endsWith(".bin") }) {
                warnings.add("VBA macros detected in document were skipped for security.")
            }
            if (zipEntries.keys.any { it.startsWith("word/diagrams") }) {
                warnings.add("SmartArt diagrams in document were omitted.")
            }
            if (zipEntries.keys.any { it.startsWith("word/charts") }) {
                warnings.add("Charts were omitted; convert charts to images before importing for best results.")
            }

            var docTitle = "Imported Document"
            var docAuthor = "Author"
            zipEntries["docProps/core.xml"]?.let { coreBytes ->
                try {
                    val coreDom = DocxXmlUtils.parseXml(coreBytes)
                    val titleNodes = coreDom.getElementsByTagNameNS("http://purl.org/dc/elements/1.1/", "title")
                    if (titleNodes.length > 0 && titleNodes.item(0).textContent.isNotBlank()) {
                        docTitle = titleNodes.item(0).textContent.trim()
                    }
                    val creatorNodes = coreDom.getElementsByTagNameNS("http://purl.org/dc/elements/1.1/", "creator")
                    if (creatorNodes.length > 0 && creatorNodes.item(0).textContent.isNotBlank()) {
                        docAuthor = creatorNodes.item(0).textContent.trim()
                    }
                } catch (_: Exception) {}
            }

            val numIdToListType = mutableMapOf<String, ListType>()
            zipEntries["word/numbering.xml"]?.let { numBytes ->
                try {
                    val numDom = DocxXmlUtils.parseXml(numBytes)
                    val abstractNums = mutableMapOf<String, ListType>()
                    val absNodes = numDom.getElementsByTagName("w:abstractNum")
                    for (i in 0 until absNodes.length) {
                        val el = absNodes.item(i) as? Element ?: continue
                        val absId = el.getAttribute("w:abstractNumId")
                        val numFmts = el.getElementsByTagName("w:numFmt")
                        var lType = ListType.BULLET
                        if (numFmts.length > 0) {
                            val fmtVal = (numFmts.item(0) as? Element)?.getAttribute("w:val") ?: ""
                            if (fmtVal == "decimal" || fmtVal.contains("digit")) {
                                lType = ListType.NUMBERED
                            }
                        }
                        abstractNums[absId] = lType
                    }

                    val nums = numDom.getElementsByTagName("w:num")
                    for (i in 0 until nums.length) {
                        val el = nums.item(i) as? Element ?: continue
                        val numId = el.getAttribute("w:numId")
                        val absRef = (el.getElementsByTagName("w:abstractNumId").item(0) as? Element)?.getAttribute("w:val")
                        if (absRef != null && abstractNums.containsKey(absRef)) {
                            numIdToListType[numId] = abstractNums[absRef]!!
                        }
                    }
                } catch (_: Exception) {}
            }

            val relMap = mutableMapOf<String, String>()
            zipEntries["word/_rels/document.xml.rels"]?.let { relBytes ->
                try {
                    val relDom = DocxXmlUtils.parseXml(relBytes)
                    val relNodes = relDom.getElementsByTagName("Relationship")
                    for (i in 0 until relNodes.length) {
                        val el = relNodes.item(i) as? Element ?: continue
                        val id = el.getAttribute("Id")
                        val target = el.getAttribute("Target")
                        if (id.isNotBlank() && target.isNotBlank()) {
                            relMap[id] = target
                        }
                    }
                } catch (_: Exception) {}
            }

            val relIdToLocalUri = mutableMapOf<String, String>()
            val imageCacheDir = context?.let { File(it.filesDir, "docx_images").apply { mkdirs() } }

            for ((relId, target) in relMap) {
                val cleanTarget = if (target.startsWith("/")) target.removePrefix("/") else "word/$target"
                val mediaBytes = zipEntries[cleanTarget] ?: zipEntries[target]
                if (mediaBytes != null && imageCacheDir != null) {
                    val ext = target.substringAfterLast('.', "png")
                    val imgFile = File(imageCacheDir, "img_${UUID.randomUUID()}.$ext")
                    FileOutputStream(imgFile).use { it.write(mediaBytes) }
                    relIdToLocalUri[relId] = Uri.fromFile(imgFile).toString()
                }
            }

            var headerText = ""
            var footerText = ""

            for ((_, target) in relMap) {
                if (target.contains("header", ignoreCase = true)) {
                    val headerEntry = if (target.startsWith("/")) target.removePrefix("/") else "word/$target"
                    zipEntries[headerEntry]?.let { bytes ->
                        try {
                            val hDom = DocxXmlUtils.parseXml(bytes)
                            headerText = DocxXmlUtils.extractHeaderFooterText(hDom).trim()
                        } catch (_: Exception) {}
                    }
                } else if (target.contains("footer", ignoreCase = true)) {
                    val footerEntry = if (target.startsWith("/")) target.removePrefix("/") else "word/$target"
                    zipEntries[footerEntry]?.let { bytes ->
                        try {
                            val fDom = DocxXmlUtils.parseXml(bytes)
                            val raw = DocxXmlUtils.extractHeaderFooterText(fDom).trim()
                            if (raw.isNotBlank()) footerText = raw
                        } catch (_: Exception) {}
                    }
                }
            }

            val docBytes = zipEntries["word/document.xml"]
                ?: return DocumentResult.Failure(DocumentError.CorruptDocumentError("Missing word/document.xml in DOCX archive."))

            val docDom = DocxXmlUtils.parseXml(docBytes)
            val body = docDom.getElementsByTagName("w:body").item(0) as? Element
                ?: return DocumentResult.Failure(DocumentError.CorruptDocumentError("Missing w:body element in document.xml."))

            val blocks = mutableListOf<BlockElement>()
            var parsedLayoutConfig = PageLayoutConfig(
                headerText = headerText,
                footerText = footerText,
                showHeader = headerText.isNotBlank(),
                showFooter = footerText.isNotBlank()
            )

            val childNodes = body.childNodes
            for (i in 0 until childNodes.length) {
                val node = childNodes.item(i)
                if (node.nodeType != Node.ELEMENT_NODE) continue
                val element = node as Element

                when (element.tagName) {
                    "w:p" -> {
                        val parsedBlock = DocxParagraphParser.parseParagraphElement(element, numIdToListType, relIdToLocalUri, relMap, warnings)
                        if (parsedBlock != null) blocks.add(parsedBlock)
                    }
                    "w:tbl" -> {
                        blocks.add(DocxTableParser.parseTableElement(element))
                    }
                    "w:sectPr" -> {
                        parsedLayoutConfig = DocxSectionParser.parseSectionProperties(element, parsedLayoutConfig)
                    }
                }
            }

            zipEntries["word/footnotes.xml"]?.let { fnBytes ->
                try {
                    val fnDom = DocxXmlUtils.parseXml(fnBytes)
                    val fnNodes = fnDom.getElementsByTagName("w:footnote")
                    for (i in 0 until fnNodes.length) {
                        val fnElem = fnNodes.item(i) as? Element ?: continue
                        val fnType = fnElem.getAttribute("w:type")
                        if (fnType == "separator" || fnType == "continuationSeparator") continue
                        val fnIdStr = fnElem.getAttribute("w:id")
                        val fnId = fnIdStr.toIntOrNull() ?: (i + 1)
                        val text = DocxXmlUtils.extractElementText(fnElem).trim()
                        if (text.isNotBlank()) {
                            blocks.add(FootnoteBlock(noteNumber = fnId, referenceText = "", noteContent = text))
                        }
                    }
                } catch (_: Exception) {}
            }

            val parsedComments = mutableListOf<DocumentComment>()
            zipEntries["word/comments.xml"]?.let { commentBytes ->
                try {
                    val commentsDom = DocxXmlUtils.parseXml(commentBytes)
                    val commentNodes = commentsDom.getElementsByTagName("w:comment")
                    for (i in 0 until commentNodes.length) {
                        val commentElem = commentNodes.item(i) as? Element ?: continue
                        val cId = commentElem.getAttribute("w:id")
                        val author = commentElem.getAttribute("w:author").ifBlank { "Reviewer" }
                        val text = DocxXmlUtils.extractElementText(commentElem).trim()
                        if (text.isNotBlank()) {
                            parsedComments.add(
                                DocumentComment(
                                    id = cId.ifBlank { UUID.randomUUID().toString() },
                                    blockId = blocks.firstOrNull()?.id ?: "",
                                    author = author,
                                    text = text
                                )
                            )
                        }
                    }
                } catch (_: Exception) {}
            }

            if (blocks.isEmpty()) blocks.add(ParagraphBlock(text = ""))

            val richDoc = RichDocument(
                id = UUID.randomUUID().toString(),
                title = docTitle,
                author = docAuthor,
                layoutConfig = parsedLayoutConfig,
                blocks = blocks,
                comments = parsedComments
            )

            val compNotes = mutableListOf<String>()
            compNotes.add("Imported ${richDoc.blocks.size} blocks, ${richDoc.wordCount} words.")
            if (warnings.isNotEmpty()) compNotes.add("${warnings.size} unsupported features noted.")

            DocumentResult.Success(
                DocxImportResult(document = richDoc, warnings = warnings, compatibilityNotes = compNotes)
            )
        } catch (e: Exception) {
            DocumentResult.Failure(DocumentError.CorruptDocumentError("Failed to parse DOCX package: ${e.message}"))
        }
    }
}
