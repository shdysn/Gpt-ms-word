package com.example.data.service.docx

import android.content.Context
import android.net.Uri
import com.example.data.model.FootnoteBlock
import com.example.data.model.ImageBlock
import com.example.data.model.ParagraphBlock
import com.example.data.model.RichDocument
import java.io.BufferedOutputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.InputStream
import java.io.OutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

/**
 * Builds standard ECMA-376 / ISO/IEC 29500 Open Packaging Convention (OPC) DOCX packages.
 */
class DocxPackageWriter(private val context: Context?) {

    fun write(document: RichDocument, outputStream: OutputStream) {
        val zipOut = ZipOutputStream(BufferedOutputStream(outputStream))

        val embeddedImages = mutableListOf<DocxBodyXmlBuilder.EmbeddedImage>()
        var imgCounter = 1
        for (block in document.blocks) {
            if (block is ImageBlock) {
                val imgData = loadImageData(block.imageUri)
                if (imgData != null) {
                    val ext = if (block.imageUri.endsWith(".jpg", ignoreCase = true) || block.imageUri.endsWith(".jpeg", ignoreCase = true)) "jpeg" else "png"
                    val contentType = if (ext == "jpeg") "image/jpeg" else "image/png"
                    val relId = "rIdImg$imgCounter"
                    val entryPath = "word/media/image$imgCounter.$ext"
                    val widthEmu = (block.widthDp.coerceIn(40, 600) * 12700L)
                    val heightEmu = (block.heightDp.coerceIn(40, 600) * 12700L)
                    embeddedImages.add(
                        DocxBodyXmlBuilder.EmbeddedImage(
                            relId = relId,
                            entryPath = entryPath,
                            contentType = contentType,
                            data = imgData,
                            widthEmu = widthEmu,
                            heightEmu = heightEmu
                        )
                    )
                    imgCounter++
                }
            }
        }

        val hyperlinkMap = mutableMapOf<String, String>()
        var linkCounter = 1
        for (block in document.blocks) {
            if (block is ParagraphBlock) {
                for (span in block.spans) {
                    val url = span.linkUrl
                    if (!url.isNullOrBlank() && !hyperlinkMap.containsKey(url)) {
                        hyperlinkMap[url] = "rIdLink$linkCounter"
                        linkCounter++
                    }
                }
            }
        }

        writeZipEntry(zipOut, "[Content_Types].xml", DocxXmlBuilders.buildContentTypesXml(document, embeddedImages))
        writeZipEntry(zipOut, "_rels/.rels", DocxXmlBuilders.buildPackageRelsXml())
        writeZipEntry(zipOut, "word/_rels/document.xml.rels", DocxXmlBuilders.buildDocumentRelsXml(document, embeddedImages, hyperlinkMap))
        writeZipEntry(zipOut, "docProps/core.xml", DocxXmlBuilders.buildDocPropsCoreXml(document))
        writeZipEntry(zipOut, "docProps/app.xml", DocxXmlBuilders.buildDocPropsAppXml(document))
        writeZipEntry(zipOut, "word/styles.xml", DocxXmlBuilders.buildStylesXml())
        writeZipEntry(zipOut, "word/numbering.xml", DocxXmlBuilders.buildNumberingXml())
        writeZipEntry(zipOut, "word/fontTable.xml", DocxXmlBuilders.buildFontTableXml())
        writeZipEntry(zipOut, "word/settings.xml", DocxXmlBuilders.buildSettingsXml())
        writeZipEntry(zipOut, "word/webSettings.xml", DocxXmlBuilders.buildWebSettingsXml())

        if (document.layoutConfig.showHeader && document.layoutConfig.headerText.isNotBlank()) {
            writeZipEntry(zipOut, "word/header1.xml", DocxXmlBuilders.buildHeaderXml(document))
        }

        if (document.layoutConfig.showFooter && document.layoutConfig.footerText.isNotBlank()) {
            writeZipEntry(zipOut, "word/footer1.xml", DocxXmlBuilders.buildFooterXml(document))
        }

        if (document.comments.isNotEmpty()) {
            writeZipEntry(zipOut, "word/comments.xml", DocxXmlBuilders.buildCommentsXml(document.comments))
        }

        val footnotes = document.blocks.filterIsInstance<FootnoteBlock>()
        if (footnotes.isNotEmpty()) {
            writeZipEntry(zipOut, "word/footnotes.xml", DocxXmlBuilders.buildFootnotesXml(footnotes))
        }

        for (img in embeddedImages) {
            writeBinaryZipEntry(zipOut, img.entryPath, img.data)
        }

        writeZipEntry(zipOut, "word/document.xml", DocxBodyXmlBuilder.buildDocumentXml(document, embeddedImages, hyperlinkMap))

        zipOut.finish()
        zipOut.flush()
    }

    private fun writeZipEntry(zipOut: ZipOutputStream, entryPath: String, xmlContent: String) {
        val entry = ZipEntry(entryPath)
        zipOut.putNextEntry(entry)
        val bytes = xmlContent.toByteArray(Charsets.UTF_8)
        zipOut.write(bytes)
        zipOut.closeEntry()
    }

    private fun writeBinaryZipEntry(zipOut: ZipOutputStream, entryPath: String, data: ByteArray) {
        val entry = ZipEntry(entryPath)
        zipOut.putNextEntry(entry)
        zipOut.write(data)
        zipOut.closeEntry()
    }

    private fun loadImageData(uriString: String): ByteArray? {
        if (uriString.isBlank()) return null
        return try {
            val uri = Uri.parse(uriString)
            when (uri.scheme) {
                "file" -> {
                    uri.path?.let { File(it).takeIf { f -> f.exists() }?.readBytes() }
                }
                "content" -> {
                    context?.contentResolver?.openInputStream(uri)?.use { readAllBytes(it) }
                }
                "android.resource" -> {
                    context?.contentResolver?.openInputStream(uri)?.use { readAllBytes(it) }
                }
                else -> {
                    val file = File(uriString)
                    if (file.exists()) file.readBytes() else null
                }
            }
        } catch (_: Exception) {
            null
        }
    }

    private fun readAllBytes(stream: InputStream): ByteArray {
        val buffer = ByteArray(8192)
        val baos = ByteArrayOutputStream()
        var bytesRead: Int
        while (stream.read(buffer).also { bytesRead = it } != -1) {
            baos.write(buffer, 0, bytesRead)
        }
        return baos.toByteArray()
    }
}
