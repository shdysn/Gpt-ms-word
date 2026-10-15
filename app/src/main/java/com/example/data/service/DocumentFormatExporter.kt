package com.example.data.service

import android.content.Context
import android.net.Uri
import com.example.core.error.DocumentError
import com.example.core.error.DocumentResult
import com.example.data.model.DividerBlock
import com.example.data.model.FootnoteBlock
import com.example.data.model.HeadingStyle
import com.example.data.model.ImageBlock
import com.example.data.model.ListType
import com.example.data.model.ParagraphBlock
import com.example.data.model.RichDocument
import com.example.data.model.ShapeBlock
import com.example.data.model.SignatureBlock
import com.example.data.model.TableBlock
import com.example.data.model.TextBoxBlock
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.util.zip.CRC32
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

/**
 * Handles exporting RichDocument to PlainText, Markdown, and EPUB formats.
 */
object DocumentFormatExporter {

    suspend fun exportToPlainText(
        context: Context,
        document: RichDocument,
        outputFile: File
    ): DocumentResult<File> = withContext(Dispatchers.IO) {
        try {
            outputFile.parentFile?.mkdirs()
            val text = document.blocks.filterIsInstance<ParagraphBlock>()
                .joinToString("\n\n") { it.text }
            outputFile.writeText(text)
            DocumentResult.Success(outputFile)
        } catch (e: Exception) {
            DocumentResult.Failure(DocumentError.ExportError("Plain text export failed: ${e.message}", "TXT", e))
        }
    }

    suspend fun exportToMarkdown(
        context: Context,
        document: RichDocument,
        outputFile: File
    ): DocumentResult<File> = withContext(Dispatchers.IO) {
        try {
            outputFile.parentFile?.mkdirs()
            val mdContent = generateMarkdown(document)
            outputFile.writeText(mdContent)
            DocumentResult.Success(outputFile)
        } catch (e: Exception) {
            DocumentResult.Failure(DocumentError.ExportError("Markdown export failed: ${e.message}", "MD", e))
        }
    }

    suspend fun exportMarkdownToUri(
        context: Context,
        document: RichDocument,
        targetUri: Uri
    ): DocumentResult<Unit> = withContext(Dispatchers.IO) {
        try {
            val mdContent = generateMarkdown(document)
            context.contentResolver.openOutputStream(targetUri)?.use { out ->
                out.write(mdContent.toByteArray(Charsets.UTF_8))
                out.flush()
            } ?: return@withContext DocumentResult.Failure(DocumentError.ExportError("Could not open output stream for URI", "MD"))
            DocumentResult.Success(Unit)
        } catch (e: Exception) {
            DocumentResult.Failure(DocumentError.ExportError("Export to SAF URI failed: ${e.message}", "MD", e))
        }
    }

    suspend fun exportToEpub(
        context: Context,
        document: RichDocument,
        outputFile: File
    ): DocumentResult<File> = withContext(Dispatchers.IO) {
        try {
            outputFile.parentFile?.mkdirs()
            FileOutputStream(outputFile).use { fos ->
                writeEpubPackage(document, fos)
            }
            DocumentResult.Success(outputFile)
        } catch (e: Exception) {
            DocumentResult.Failure(DocumentError.ExportError("EPUB export failed: ${e.message}", "EPUB", e))
        }
    }

    suspend fun exportEpubToUri(
        context: Context,
        document: RichDocument,
        targetUri: Uri
    ): DocumentResult<Unit> = withContext(Dispatchers.IO) {
        try {
            context.contentResolver.openOutputStream(targetUri)?.use { out ->
                writeEpubPackage(document, out)
            } ?: return@withContext DocumentResult.Failure(DocumentError.ExportError("Could not open output stream for EPUB URI", "EPUB"))
            DocumentResult.Success(Unit)
        } catch (e: Exception) {
            DocumentResult.Failure(DocumentError.ExportError("EPUB SAF URI export failed: ${e.message}", "EPUB", e))
        }
    }

    fun generateMarkdown(document: RichDocument): String {
        val sb = StringBuilder()
        sb.append("# ${document.title}\n\n")

        for (block in document.blocks) {
            when (block) {
                is ParagraphBlock -> {
                    val prefix = when (block.headingStyle) {
                        HeadingStyle.TITLE -> "# "
                        HeadingStyle.SUBTITLE -> "### "
                        HeadingStyle.HEADING_1 -> "## "
                        HeadingStyle.HEADING_2 -> "### "
                        HeadingStyle.HEADING_3 -> "#### "
                        HeadingStyle.QUOTE -> "> "
                        HeadingStyle.NORMAL -> when (block.listType) {
                            ListType.BULLET -> "* "
                            ListType.NUMBERED -> "1. "
                            ListType.NONE -> if (block.isChecklist) (if (block.isChecked) "- [x] " else "- [ ] ") else ""
                        }
                    }
                    sb.append(prefix).append(block.text).append("\n\n")
                }
                is TableBlock -> {
                    if (block.cells.isNotEmpty()) {
                        val header = block.cells.first()
                        sb.append("| ").append(header.joinToString(" | ") { it.text.ifBlank { " " } }).append(" |\n")
                        sb.append("| ").append(header.joinToString(" | ") { "---" }).append(" |\n")
                        for (r in 1 until block.cells.size) {
                            val row = block.cells[r]
                            sb.append("| ").append(row.joinToString(" | ") { it.text.ifBlank { " " } }).append(" |\n")
                        }
                        sb.append("\n")
                    }
                }
                is ImageBlock -> sb.append("![${block.caption.ifBlank { "Image" }}](${block.imageUri})\n\n")
                is DividerBlock -> sb.append("---\n\n")
                is ShapeBlock -> {
                    sb.append("> **[${block.shapeType}] ${block.title}**\n")
                    if (block.content.isNotBlank()) sb.append("> ${block.content}\n")
                    sb.append("\n")
                }
                is SignatureBlock -> {
                    sb.append("*Digital Signature:* **${block.signerName}** — ${block.signerTitle}")
                    if (block.signedDate.isNotBlank()) sb.append(" (${block.signedDate})")
                    sb.append("\n\n")
                }
                is FootnoteBlock -> sb.append("[^${block.noteNumber}]: ${block.noteContent}\n\n")
                is TextBoxBlock -> sb.append("> 💬 **${block.text}**\n\n")
                else -> Unit
            }
        }
        return sb.toString()
    }

    fun writeEpubPackage(document: RichDocument, out: OutputStream) {
        val zos = ZipOutputStream(out)

        fun addFile(path: String, content: String) {
            val entry = ZipEntry(path)
            zos.putNextEntry(entry)
            zos.write(content.toByteArray(Charsets.UTF_8))
            zos.closeEntry()
        }

        val mimeEntry = ZipEntry("mimetype").apply {
            method = ZipEntry.STORED
            val bytes = "application/epub+zip".toByteArray(Charsets.US_ASCII)
            size = bytes.size.toLong()
            compressedSize = bytes.size.toLong()
            val crc = CRC32()
            crc.update(bytes)
            setCrc(crc.value)
        }
        zos.putNextEntry(mimeEntry)
        zos.write("application/epub+zip".toByteArray(Charsets.US_ASCII))
        zos.closeEntry()

        addFile(
            "META-INF/container.xml",
            """<?xml version="1.0" encoding="UTF-8"?><container version="1.0" xmlns="urn:oasis:names:tc:opendocument:xmlns:container"><rootfiles><rootfile full-path="OEBPS/content.opf" media-type="application/oebps-package+xml"/></rootfiles></container>"""
        )

        addFile(
            "OEBPS/content.opf",
            """<?xml version="1.0" encoding="UTF-8"?><package xmlns="http://www.idpf.org/2007/opf" version="3.0" unique-identifier="pub-id"><metadata xmlns:dc="http://purl.org/dc/elements/1.1/"><dc:title>${document.title}</dc:title><dc:creator>${document.author}</dc:creator><dc:identifier id="pub-id">${document.id}</dc:identifier><dc:language>en</dc:language></metadata><manifest><item id="doc" href="document.xhtml" media-type="application/xhtml+xml"/></manifest><spine><itemref idref="doc"/></spine></package>"""
        )

        val xhtmlSb = StringBuilder()
        xhtmlSb.append("""<?xml version="1.0" encoding="UTF-8"?><!DOCTYPE html><html xmlns="http://www.w3.org/1999/xhtml"><head><title>${document.title}</title><style>body { font-family: sans-serif; line-height: 1.5; margin: 5%; } h1 { color: #1e40af; } h2 { color: #1e3a8a; } blockquote { border-left: 3px solid #3b82f6; padding-left: 10px; color: #475569; } table { border-collapse: collapse; width: 100%; margin: 1em 0; } th, td { border: 1px solid #cbd5e1; padding: 6px; }</style></head><body><h1>${document.title}</h1>""")
        for (block in document.blocks) {
            when (block) {
                is ParagraphBlock -> {
                    val tag = when (block.headingStyle) {
                        HeadingStyle.TITLE, HeadingStyle.HEADING_1 -> "h2"
                        HeadingStyle.HEADING_2 -> "h3"
                        HeadingStyle.HEADING_3 -> "h4"
                        HeadingStyle.QUOTE -> "blockquote"
                        else -> "p"
                    }
                    val esc = block.text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
                    xhtmlSb.append("<$tag>$esc</$tag>\n")
                }
                is TableBlock -> {
                    xhtmlSb.append("<table>\n")
                    for (row in block.cells) {
                        xhtmlSb.append("<tr>")
                        for (cell in row) {
                            val esc = cell.text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
                            xhtmlSb.append("<td>$esc</td>")
                        }
                        xhtmlSb.append("</tr>\n")
                    }
                    xhtmlSb.append("</table>\n")
                }
                is DividerBlock -> xhtmlSb.append("<hr/>\n")
                is TextBoxBlock -> {
                    val esc = block.text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
                    xhtmlSb.append("<div style=\"border:1px solid #2563eb;padding:8px;margin:8px 0;\">$esc</div>\n")
                }
                else -> Unit
            }
        }
        xhtmlSb.append("</body>\n</html>")
        addFile("OEBPS/document.xhtml", xhtmlSb.toString())

        zos.finish()
        zos.flush()
    }
}
