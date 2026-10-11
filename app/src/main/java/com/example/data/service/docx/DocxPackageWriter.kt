package com.example.data.service.docx

import android.content.Context
import android.net.Uri
import com.example.core.layout.PageOrientation
import com.example.core.layout.PageSize
import com.example.core.typography.BidiUtils
import com.example.data.model.DividerBlock
import com.example.data.model.HeadingStyle
import com.example.data.model.ImageBlock
import com.example.data.model.InlineSpan
import com.example.data.model.ListType
import com.example.data.model.PageBreakBlock
import com.example.data.model.ParagraphBlock
import com.example.data.model.RichDocument
import com.example.data.model.TableBlock
import com.example.data.model.TextAlignment
import java.io.BufferedOutputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.InputStream
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

/**
 * Builds standard ECMA-376 / ISO/IEC 29500 Open Packaging Convention (OPC) DOCX packages.
 * Fully compatible with Microsoft Word, LibreOffice, Google Docs, and Apple Pages.
 */
class DocxPackageWriter(private val context: Context?) {

    private data class EmbeddedImage(
        val relId: String,
        val entryPath: String,
        val contentType: String,
        val data: ByteArray,
        val widthEmu: Long,
        val heightEmu: Long
    )

    fun write(document: RichDocument, outputStream: OutputStream) {
        val zipOut = ZipOutputStream(BufferedOutputStream(outputStream))

        // 1. Collect and prepare embedded images
        val embeddedImages = mutableListOf<EmbeddedImage>()
        var imgCounter = 1
        for (block in document.blocks) {
            if (block is ImageBlock) {
                val imgData = loadImageData(block.imageUri)
                if (imgData != null) {
                    val ext = if (block.imageUri.endsWith(".jpg", ignoreCase = true) || block.imageUri.endsWith(".jpeg", ignoreCase = true)) "jpeg" else "png"
                    val contentType = if (ext == "jpeg") "image/jpeg" else "image/png"
                    val relId = "rIdImg$imgCounter"
                    val entryPath = "word/media/image$imgCounter.$ext"
                    // 1 dp ~ 1 pt ~ 12700 EMUs
                    val widthEmu = (block.widthDp.coerceIn(40, 600) * 12700L)
                    val heightEmu = (block.heightDp.coerceIn(40, 600) * 12700L)
                    embeddedImages.add(
                        EmbeddedImage(
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

        // 1b. Collect external hyperlinks
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

        // 2. Write [Content_Types].xml
        writeZipEntry(zipOut, "[Content_Types].xml", buildContentTypesXml(document, embeddedImages))

        // 3. Write _rels/.rels
        writeZipEntry(zipOut, "_rels/.rels", buildPackageRelsXml())

        // 4. Write docProps/core.xml and docProps/app.xml
        writeZipEntry(zipOut, "docProps/core.xml", buildDocPropsCoreXml(document))
        writeZipEntry(zipOut, "docProps/app.xml", buildDocPropsAppXml(document))

        // 5. Write word/_rels/document.xml.rels
        writeZipEntry(zipOut, "word/_rels/document.xml.rels", buildDocumentRelsXml(document, embeddedImages, hyperlinkMap))

        // 6. Write word/styles.xml
        writeZipEntry(zipOut, "word/styles.xml", buildStylesXml())

        // 7. Write word/numbering.xml
        writeZipEntry(zipOut, "word/numbering.xml", buildNumberingXml())

        // 8. Write header and footer if enabled
        if (document.layoutConfig.showHeader && document.layoutConfig.headerText.isNotBlank()) {
            writeZipEntry(zipOut, "word/header1.xml", buildHeaderXml(document))
        }
        if (document.layoutConfig.showFooter && document.layoutConfig.footerText.isNotBlank()) {
            writeZipEntry(zipOut, "word/footer1.xml", buildFooterXml(document))
        }

        // 9. Write embedded image binaries
        for (img in embeddedImages) {
            writeZipEntryBytes(zipOut, img.entryPath, img.data)
        }

        // 10. Write word/document.xml
        writeZipEntry(zipOut, "word/document.xml", buildDocumentXml(document, embeddedImages, hyperlinkMap))

        zipOut.flush()
        zipOut.finish()
    }

    private fun writeZipEntry(zipOut: ZipOutputStream, entryName: String, content: String) {
        val entry = ZipEntry(entryName)
        zipOut.putNextEntry(entry)
        val bytes = content.toByteArray(Charsets.UTF_8)
        zipOut.write(bytes, 0, bytes.size)
        zipOut.closeEntry()
    }

    private fun writeZipEntryBytes(zipOut: ZipOutputStream, entryName: String, bytes: ByteArray) {
        val entry = ZipEntry(entryName)
        zipOut.putNextEntry(entry)
        zipOut.write(bytes, 0, bytes.size)
        zipOut.closeEntry()
    }

    private fun loadImageData(uriStr: String): ByteArray? {
        if (uriStr.isBlank()) return null
        return try {
            if (context != null && uriStr.startsWith("content://")) {
                context.contentResolver.openInputStream(Uri.parse(uriStr))?.use { it.readBytes() }
            } else if (uriStr.startsWith("file://")) {
                val file = File(Uri.parse(uriStr).path ?: "")
                if (file.exists()) file.readBytes() else null
            } else {
                val file = File(uriStr)
                if (file.exists()) file.readBytes() else null
            }
        } catch (e: Exception) {
            null
        }
    }

    // ----------------------------------------------------
    // XML Builders
    // ----------------------------------------------------

    private fun buildContentTypesXml(document: RichDocument, images: List<EmbeddedImage>): String {
        val hasPng = images.any { it.contentType == "image/png" }
        val hasJpeg = images.any { it.contentType == "image/jpeg" }

        val sb = StringBuilder()
        sb.append("""<?xml version="1.0" encoding="UTF-8" standalone="yes"?>""")
        sb.append("""<Types xmlns="http://schemas.openxmlformats.org/package/2006/content-types">""")
        sb.append("""<Default Extension="rels" ContentType="application/vnd.openxmlformats-package.relationships+xml"/>""")
        sb.append("""<Default Extension="xml" ContentType="application/xml"/>""")
        if (hasPng) sb.append("""<Default Extension="png" ContentType="image/png"/>""")
        if (hasJpeg) {
            sb.append("""<Default Extension="jpeg" ContentType="image/jpeg"/>""")
            sb.append("""<Default Extension="jpg" ContentType="image/jpeg"/>""")
        }
        sb.append("""<Override PartName="/word/document.xml" ContentType="application/vnd.openxmlformats-officedocument.wordprocessingml.document.main+xml"/>""")
        sb.append("""<Override PartName="/word/styles.xml" ContentType="application/vnd.openxmlformats-officedocument.wordprocessingml.styles+xml"/>""")
        sb.append("""<Override PartName="/word/numbering.xml" ContentType="application/vnd.openxmlformats-officedocument.wordprocessingml.numbering+xml"/>""")
        if (document.layoutConfig.showHeader && document.layoutConfig.headerText.isNotBlank()) {
            sb.append("""<Override PartName="/word/header1.xml" ContentType="application/vnd.openxmlformats-officedocument.wordprocessingml.header+xml"/>""")
        }
        if (document.layoutConfig.showFooter && document.layoutConfig.footerText.isNotBlank()) {
            sb.append("""<Override PartName="/word/footer1.xml" ContentType="application/vnd.openxmlformats-officedocument.wordprocessingml.footer+xml"/>""")
        }
        sb.append("""<Override PartName="/docProps/core.xml" ContentType="application/vnd.openxmlformats-package.core-properties+xml"/>""")
        sb.append("""<Override PartName="/docProps/app.xml" ContentType="application/vnd.openxmlformats-officedocument.extended-properties+xml"/>""")
        sb.append("""</Types>""")
        return sb.toString()
    }

    private fun buildPackageRelsXml(): String {
        return """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
  <Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument" Target="word/document.xml"/>
  <Relationship Id="rId2" Type="http://schemas.openxmlformats.org/package/2006/relationships/metadata/core-properties" Target="docProps/core.xml"/>
  <Relationship Id="rId3" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/extended-properties" Target="docProps/app.xml"/>
</Relationships>"""
    }

    private fun buildDocumentRelsXml(
        document: RichDocument,
        images: List<EmbeddedImage>,
        hyperlinks: Map<String, String> = emptyMap()
    ): String {
        val sb = StringBuilder()
        sb.append("""<?xml version="1.0" encoding="UTF-8" standalone="yes"?>""")
        sb.append("""<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">""")
        sb.append("""<Relationship Id="rIdStyles" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/styles" Target="styles.xml"/>""")
        sb.append("""<Relationship Id="rIdNum" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/numbering" Target="numbering.xml"/>""")
        if (document.layoutConfig.showHeader && document.layoutConfig.headerText.isNotBlank()) {
            sb.append("""<Relationship Id="rIdHeader1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/header" Target="header1.xml"/>""")
        }
        if (document.layoutConfig.showFooter && document.layoutConfig.footerText.isNotBlank()) {
            sb.append("""<Relationship Id="rIdFooter1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/footer" Target="footer1.xml"/>""")
        }
        for (img in images) {
            val relativeTarget = img.entryPath.removePrefix("word/")
            sb.append("""<Relationship Id="${img.relId}" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/image" Target="$relativeTarget"/>""")
        }
        for ((url, relId) in hyperlinks) {
            sb.append("""<Relationship Id="$relId" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/hyperlink" Target="${escapeXml(url)}" TargetMode="External"/>""")
        }
        sb.append("""</Relationships>""")
        return sb.toString()
    }

    private fun buildDocPropsCoreXml(document: RichDocument): String {
        val isoFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
        val createdStr = isoFormat.format(Date(document.createdAt))
        val updatedStr = isoFormat.format(Date(document.updatedAt))

        return """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<cp:coreProperties xmlns:cp="http://schemas.openxmlformats.org/package/2006/metadata/core-properties"
    xmlns:dc="http://purl.org/dc/elements/1.1/"
    xmlns:dcterms="http://purl.org/dc/terms/"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
  <dc:title>${escapeXml(document.title)}</dc:title>
  <dc:creator>${escapeXml(document.author.ifBlank { "Smart Word Processor" })}</dc:creator>
  <cp:lastModifiedBy>${escapeXml(document.author.ifBlank { "Smart Word Processor" })}</cp:lastModifiedBy>
  <dcterms:created xsi:type="dcterms:W3CDTF">$createdStr</dcterms:created>
  <dcterms:modified xsi:type="dcterms:W3CDTF">$updatedStr</dcterms:modified>
</cp:coreProperties>"""
    }

    private fun buildDocPropsAppXml(document: RichDocument): String {
        return """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Properties xmlns="http://schemas.openxmlformats.org/officeDocument/2006/extended-properties"
    xmlns:vt="http://schemas.openxmlformats.org/officeDocument/2006/docPropsVTypes">
  <Application>Smart Word Processor for Android</Application>
  <DocSecurity>0</DocSecurity>
  <Lines>${document.blocks.size}</Lines>
  <Paragraphs>${document.blocks.count { it is ParagraphBlock }}</Paragraphs>
  <Words>${document.wordCount}</Words>
  <Characters>${document.characterCount}</Characters>
  <Pages>${document.estimatedPageCount}</Pages>
</Properties>"""
    }

    private fun buildStylesXml(): String {
        return """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<w:styles xmlns:w="http://schemas.openxmlformats.org/wordprocessingml/2006/main">
  <w:docDefaults>
    <w:rPrDefault>
      <w:rPr>
        <w:rFonts w:ascii="Calibri" w:hAnsi="Calibri" w:cs="Noto Nastaliq Urdu"/>
        <w:sz w:val="22"/>
        <w:szCs w:val="24"/>
        <w:lang w:val="en-US" w:bidi="ur-PK"/>
      </w:rPr>
    </w:rPrDefault>
    <w:pPrDefault>
      <w:pPr>
        <w:spacing w:line="276" w:lineRule="auto" w:after="160"/>
      </w:pPr>
    </w:pPrDefault>
  </w:docDefaults>

  <w:style w:type="paragraph" w:default="1" w:styleId="Normal">
    <w:name w:val="Normal"/>
    <w:qFormat/>
  </w:style>

  <w:style w:type="paragraph" w:styleId="Heading1">
    <w:name w:val="heading 1"/>
    <w:basedOn w:val="Normal"/>
    <w:next w:val="Normal"/>
    <w:qFormat/>
    <w:pPr>
      <w:keepNext/>
      <w:spacing w:before="240" w:after="120"/>
    </w:pPr>
    <w:rPr>
      <w:b/>
      <w:color w:val="185ABD"/>
      <w:sz w:val="32"/>
      <w:szCs w:val="32"/>
    </w:rPr>
  </w:style>

  <w:style w:type="paragraph" w:styleId="Heading2">
    <w:name w:val="heading 2"/>
    <w:basedOn w:val="Normal"/>
    <w:next w:val="Normal"/>
    <w:qFormat/>
    <w:pPr>
      <w:keepNext/>
      <w:spacing w:before="180" w:after="80"/>
    </w:pPr>
    <w:rPr>
      <w:b/>
      <w:color w:val="185ABD"/>
      <w:sz w:val="26"/>
      <w:szCs w:val="26"/>
    </w:rPr>
  </w:style>

  <w:style w:type="paragraph" w:styleId="Heading3">
    <w:name w:val="heading 3"/>
    <w:basedOn w:val="Normal"/>
    <w:next w:val="Normal"/>
    <w:qFormat/>
    <w:pPr>
      <w:keepNext/>
      <w:spacing w:before="120" w:after="60"/>
    </w:pPr>
    <w:rPr>
      <w:b/>
      <w:sz w:val="24"/>
      <w:szCs w:val="24"/>
    </w:rPr>
  </w:style>

  <w:style w:type="paragraph" w:styleId="Title">
    <w:name w:val="Title"/>
    <w:basedOn w:val="Normal"/>
    <w:qFormat/>
    <w:pPr>
      <w:spacing w:before="360" w:after="180"/>
      <w:jc w:val="center"/>
    </w:pPr>
    <w:rPr>
      <w:b/>
      <w:color w:val="185ABD"/>
      <w:sz w:val="48"/>
      <w:szCs w:val="48"/>
    </w:rPr>
  </w:style>

  <w:style w:type="paragraph" w:styleId="Subtitle">
    <w:name w:val="Subtitle"/>
    <w:basedOn w:val="Normal"/>
    <w:qFormat/>
    <w:pPr>
      <w:spacing w:before="60" w:after="240"/>
      <w:jc w:val="center"/>
    </w:pPr>
    <w:rPr>
      <w:color w:val="5A6A85"/>
      <w:sz w:val="26"/>
      <w:szCs w:val="26"/>
    </w:rPr>
  </w:style>

  <w:style w:type="paragraph" w:styleId="Quote">
    <w:name w:val="Quote"/>
    <w:basedOn w:val="Normal"/>
    <w:qFormat/>
    <w:pPr>
      <w:ind w:left="720" w:right="720"/>
      <w:spacing w:before="140" w:after="140"/>
    </w:pPr>
    <w:rPr>
      <w:i/>
      <w:color w:val="475569"/>
    </w:rPr>
  </w:style>
</w:styles>"""
    }

    private fun buildNumberingXml(): String {
        return """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<w:numbering xmlns:w="http://schemas.openxmlformats.org/wordprocessingml/2006/main">
  <!-- Bullet List Abstract Definition (Id: 0) -->
  <w:abstractNum w:abstractNumId="0">
    <w:multiLevelType w:val="hybridMultilevel"/>
    <w:lvl w:ilvl="0">
      <w:start w:val="1"/>
      <w:numFmt w:val="bullet"/>
      <w:lvlText w:val="•"/>
      <w:lvlJc w:val="left"/>
      <w:pPr>
        <w:ind w:left="720" w:hanging="360"/>
      </w:pPr>
      <w:rPr>
        <w:rFonts w:ascii="Symbol" w:hAnsi="Symbol" w:hint="default"/>
      </w:rPr>
    </w:lvl>
  </w:abstractNum>

  <!-- Numbered List Abstract Definition (Id: 1) -->
  <w:abstractNum w:abstractNumId="1">
    <w:multiLevelType w:val="hybridMultilevel"/>
    <w:lvl w:ilvl="0">
      <w:start w:val="1"/>
      <w:numFmt w:val="decimal"/>
      <w:lvlText w:val="%1."/>
      <w:lvlJc w:val="left"/>
      <w:pPr>
        <w:ind w:left="720" w:hanging="360"/>
      </w:pPr>
    </w:lvl>
  </w:abstractNum>

  <!-- Concrete Instances -->
  <w:num w:numId="1">
    <w:abstractNumId w:val="0"/>
  </w:num>
  <w:num w:numId="2">
    <w:abstractNumId w:val="1"/>
  </w:num>
</w:numbering>"""
    }

    private fun buildHeaderXml(document: RichDocument): String {
        val alignment = when (document.layoutConfig.headerAlignment) {
            TextAlignment.CENTER -> "center"
            TextAlignment.END -> "right"
            else -> "left"
        }
        val isRtl = BidiUtils.detectParagraphIsRtl(document.layoutConfig.headerText)
        return """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<w:hdr xmlns:w="http://schemas.openxmlformats.org/wordprocessingml/2006/main">
  <w:p>
    <w:pPr>
      <w:jc w:val="$alignment"/>
      ${if (isRtl) "<w:bidi/>" else ""}
    </w:pPr>
    <w:r>
      <w:rPr>
        <w:sz w:val="18"/>
        <w:color w:val="666666"/>
        ${if (isRtl) "<w:rtl/>" else ""}
      </w:rPr>
      <w:t>${escapeXml(document.layoutConfig.headerText)}</w:t>
    </w:r>
  </w:p>
</w:hdr>"""
    }

    private fun buildFooterXml(document: RichDocument): String {
        val alignment = when (document.layoutConfig.footerAlignment) {
            TextAlignment.START -> "left"
            TextAlignment.END -> "right"
            else -> "center"
        }
        val rawFooter = document.layoutConfig.footerText
        val isRtl = BidiUtils.detectParagraphIsRtl(rawFooter)

        val sb = StringBuilder()
        sb.append("""<?xml version="1.0" encoding="UTF-8" standalone="yes"?>""")
        sb.append("""<w:ftr xmlns:w="http://schemas.openxmlformats.org/wordprocessingml/2006/main">""")
        sb.append("""<w:p><w:pPr><w:jc w:val="$alignment"/>""")
        if (isRtl) sb.append("""<w:bidi/>""")
        sb.append("""</w:pPr>""")

        if (rawFooter.contains("{page}")) {
            val parts = rawFooter.split("{page}")
            val prefix = parts.getOrNull(0) ?: ""
            val rest = parts.getOrNull(1) ?: ""

            if (prefix.isNotEmpty()) {
                sb.append("""<w:r><w:rPr><w:sz w:val="18"/><w:color w:val="666666"/></w:rPr><w:t xml:space="preserve">${escapeXml(prefix)}</w:t></w:r>""")
            }
            sb.append("""<w:fldSimple w:instr="PAGE"><w:r><w:rPr><w:sz w:val="18"/><w:color w:val="666666"/></w:rPr><w:t>1</w:t></w:r></w:fldSimple>""")

            if (rest.contains("{pages}")) {
                val subParts = rest.split("{pages}")
                val middle = subParts.getOrNull(0) ?: ""
                val suffix = subParts.getOrNull(1) ?: ""

                if (middle.isNotEmpty()) {
                    sb.append("""<w:r><w:rPr><w:sz w:val="18"/><w:color w:val="666666"/></w:rPr><w:t xml:space="preserve">${escapeXml(middle)}</w:t></w:r>""")
                }
                sb.append("""<w:fldSimple w:instr="NUMPAGES"><w:r><w:rPr><w:sz w:val="18"/><w:color w:val="666666"/></w:rPr><w:t>1</w:t></w:r></w:fldSimple>""")
                if (suffix.isNotEmpty()) {
                    sb.append("""<w:r><w:rPr><w:sz w:val="18"/><w:color w:val="666666"/></w:rPr><w:t xml:space="preserve">${escapeXml(suffix)}</w:t></w:r>""")
                }
            } else if (rest.isNotEmpty()) {
                sb.append("""<w:r><w:rPr><w:sz w:val="18"/><w:color w:val="666666"/></w:rPr><w:t xml:space="preserve">${escapeXml(rest)}</w:t></w:r>""")
            }
        } else {
            sb.append("""<w:r><w:rPr><w:sz w:val="18"/><w:color w:val="666666"/></w:rPr><w:t xml:space="preserve">${escapeXml(rawFooter)}</w:t></w:r>""")
        }

        sb.append("""</w:p></w:ftr>""")
        return sb.toString()
    }

    private fun buildDocumentXml(
        document: RichDocument,
        images: List<EmbeddedImage>,
        hyperlinks: Map<String, String> = emptyMap()
    ): String {
        val sb = StringBuilder()
        sb.append("""<?xml version="1.0" encoding="UTF-8" standalone="yes"?>""")
        sb.append("""<w:document xmlns:w="http://schemas.openxmlformats.org/wordprocessingml/2006/main" """)
        sb.append("""xmlns:r="http://schemas.openxmlformats.org/officeDocument/2006/relationships" """)
        sb.append("""xmlns:wp="http://schemas.openxmlformats.org/drawingml/2006/wordprocessingDrawing" """)
        sb.append("""xmlns:a="http://schemas.openxmlformats.org/drawingml/2006/main" """)
        sb.append("""xmlns:pic="http://schemas.openxmlformats.org/drawingml/2006/picture">""")
        sb.append("""<w:body>""")

        var imgIdx = 0
        for (block in document.blocks) {
            when (block) {
                is ParagraphBlock -> {
                    sb.append(buildParagraphXml(block, hyperlinks))
                }
                is TableBlock -> {
                    sb.append(buildTableXml(block))
                }
                is ImageBlock -> {
                    val embeddedImg = images.getOrNull(imgIdx)
                    if (embeddedImg != null) {
                        sb.append(buildImageXml(block, embeddedImg))
                        imgIdx++
                    }
                }
                is DividerBlock -> {
                    sb.append("""<w:p><w:pPr><w:pBdr><w:bottom w:val="single" w:sz="6" w:space="1" w:color="CBD5E1"/></w:pBdr><w:spacing w:after="240"/></w:pPr></w:p>""")
                }
                is PageBreakBlock -> {
                    sb.append("""<w:p><w:r><w:br w:type="page"/></w:r></w:p>""")
                }
            }
        }

        // Section Properties (<w:sectPr>) for page setup
        sb.append(buildSectionPropertiesXml(document))

        sb.append("""</w:body></w:document>""")
        return sb.toString()
    }

    private fun buildParagraphXml(block: ParagraphBlock, hyperlinks: Map<String, String> = emptyMap()): String {
        val sb = StringBuilder()
        sb.append("<w:p>")

        // Paragraph Properties
        val isRtl = block.isRtl || BidiUtils.detectParagraphIsRtl(block.text)
        sb.append("<w:pPr>")

        // Style
        val styleVal = when (block.headingStyle) {
            HeadingStyle.TITLE -> "Title"
            HeadingStyle.SUBTITLE -> "Subtitle"
            HeadingStyle.HEADING_1 -> "Heading1"
            HeadingStyle.HEADING_2 -> "Heading2"
            HeadingStyle.HEADING_3 -> "Heading3"
            HeadingStyle.QUOTE -> "Quote"
            HeadingStyle.NORMAL -> "Normal"
        }
        sb.append("""<w:pStyle w:val="$styleVal"/>""")

        // Alignment
        val jcVal = when (block.alignment) {
            TextAlignment.START -> if (isRtl) "right" else "left"
            TextAlignment.CENTER -> "center"
            TextAlignment.END -> if (isRtl) "left" else "right"
            TextAlignment.JUSTIFY -> "both"
        }
        sb.append("""<w:jc w:val="$jcVal"/>""")

        // BiDi
        if (isRtl) {
            sb.append("<w:bidi/>")
        }

        // Lists
        when (block.listType) {
            ListType.BULLET -> {
                sb.append("""<w:numPr><w:ilvl w:val="0"/><w:numId w:val="1"/></w:numPr>""")
            }
            ListType.NUMBERED -> {
                sb.append("""<w:numPr><w:ilvl w:val="0"/><w:numId w:val="2"/></w:numPr>""")
            }
            ListType.NONE -> Unit
        }

        // Indentation
        if (block.indentationLevel > 0) {
            val leftTwips = block.indentationLevel * 720
            sb.append("""<w:ind w:left="$leftTwips"/>""")
        }

        // Line Spacing
        if (block.lineSpacingMultiplier != 1.0f) {
            val lineTwips = (240 * block.lineSpacingMultiplier).toInt()
            sb.append("""<w:spacing w:line="$lineTwips" w:lineRule="auto"/>""")
        }

        sb.append("</w:pPr>")

        // Runs with segmented spans
        val runs = segmentParagraphRuns(block.text, block.spans)
        for (run in runs) {
            val runSb = StringBuilder()
            runSb.append("<w:r>")
            runSb.append("<w:rPr>")

            if (run.isBold) runSb.append("<w:b/>")
            if (run.isItalic) runSb.append("<w:i/>")
            if (run.isUnderline) runSb.append("""<w:u w:val="single"/>""")
            if (run.isStrikethrough) runSb.append("<w:strike/>")
            if (run.isSuperscript) runSb.append("""<w:vertAlign w:val="superscript"/>""")
            if (run.isSubscript) runSb.append("""<w:vertAlign w:val="subscript"/>""")

            if (run.fontSizeSp != null) {
                val halfPts = (run.fontSizeSp * 2).toInt()
                runSb.append("""<w:sz w:val="$halfPts"/>""")
                runSb.append("""<w:szCs w:val="$halfPts"/>""")
            }

            if (run.textColorHex != null) {
                val cleanHex = run.textColorHex.removePrefix("#")
                runSb.append("""<w:color w:val="$cleanHex"/>""")
            }

            if (run.highlightColorHex != null) {
                val highlightName = mapColorToWordHighlight(run.highlightColorHex)
                runSb.append("""<w:highlight w:val="$highlightName"/>""")
            }

            if (run.fontFamilyName != null) {
                val font = escapeXml(run.fontFamilyName)
                runSb.append("""<w:rFonts w:ascii="$font" w:hAnsi="$font" w:cs="$font"/>""")
            }

            if (isRtl) {
                runSb.append("<w:rtl/>")
            }

            runSb.append("</w:rPr>")
            runSb.append("""<w:t xml:space="preserve">${escapeXml(run.text)}</w:t>""")
            runSb.append("</w:r>")

            val relId = run.linkUrl?.let { hyperlinks[it] }
            if (relId != null) {
                sb.append("""<w:hyperlink r:id="$relId">""")
                sb.append(runSb)
                sb.append("""</w:hyperlink>""")
            } else {
                sb.append(runSb)
            }
        }

        sb.append("</w:p>")
        return sb.toString()
    }

    private fun buildTableXml(block: TableBlock): String {
        val sb = StringBuilder()
        sb.append("<w:tbl>")

        val pctVal = (block.tableWidthPercent.coerceIn(20, 100) * 50)
        val tableAlign = when (block.alignment) {
            TextAlignment.START -> "left"
            TextAlignment.END -> "right"
            else -> "center"
        }
        val borderHex = block.borderColorHex.removePrefix("#")
        val bordersXml = if (block.hasBorders) {
            """  <w:tblBorders>
    <w:top w:val="single" w:sz="4" w:space="0" w:color="$borderHex"/>
    <w:left w:val="single" w:sz="4" w:space="0" w:color="$borderHex"/>
    <w:bottom w:val="single" w:sz="4" w:space="0" w:color="$borderHex"/>
    <w:right w:val="single" w:sz="4" w:space="0" w:color="$borderHex"/>
    <w:insideH w:val="single" w:sz="4" w:space="0" w:color="E2E8F0"/>
    <w:insideV w:val="single" w:sz="4" w:space="0" w:color="E2E8F0"/>
  </w:tblBorders>"""
        } else {
            """  <w:tblBorders>
    <w:top w:val="none"/>
    <w:left w:val="none"/>
    <w:bottom w:val="none"/>
    <w:right w:val="none"/>
    <w:insideH w:val="none"/>
    <w:insideV w:val="none"/>
  </w:tblBorders>"""
        }

        sb.append("""<w:tblPr>
  <w:tblW w:w="$pctVal" w:type="pct"/>
  <w:jc w:val="$tableAlign"/>
$bordersXml
</w:tblPr>""")

        for (r in 0 until block.rowCount) {
            val row = block.cells.getOrNull(r) ?: emptyList()
            sb.append("<w:tr>")
            for (c in 0 until block.columnCount) {
                val cell = row.getOrNull(c)
                sb.append("<w:tc>")
                sb.append("<w:tcPr>")
                if (cell?.backgroundColorHex != null) {
                    val cleanHex = cell.backgroundColorHex.removePrefix("#")
                    sb.append("""<w:shd w:val="clear" w:color="auto" w:fill="$cleanHex"/>""")
                }
                sb.append("</w:tcPr>")

                val cellText = cell?.text ?: ""
                val isCellBold = cell?.isBold ?: (r == 0)
                val isRtl = BidiUtils.detectParagraphIsRtl(cellText)
                val cellAlign = when (cell?.alignment ?: TextAlignment.START) {
                    TextAlignment.CENTER -> "center"
                    TextAlignment.END -> if (isRtl) "left" else "right"
                    TextAlignment.JUSTIFY -> "both"
                    TextAlignment.START -> if (isRtl) "right" else "left"
                }

                sb.append("<w:p>")
                sb.append("<w:pPr>")
                sb.append("""<w:jc w:val="$cellAlign"/>""")
                if (isRtl) sb.append("<w:bidi/>")
                sb.append("</w:pPr>")
                sb.append("<w:r>")
                sb.append("<w:rPr>")
                if (isCellBold) sb.append("<w:b/>")
                if (cell?.isItalic == true) sb.append("<w:i/>")
                if (isRtl) sb.append("<w:rtl/>")
                sb.append("</w:rPr>")
                sb.append("""<w:t xml:space="preserve">${escapeXml(cellText)}</w:t>""")
                sb.append("</w:r>")
                sb.append("</w:p>")

                sb.append("</w:tc>")
            }
            sb.append("</w:tr>")
        }

        sb.append("</w:tbl>")
        return sb.toString()
    }

    private fun buildImageXml(block: ImageBlock, img: EmbeddedImage): String {
        val cx = img.widthEmu
        val cy = img.heightEmu
        val imgAlign = when (block.alignment) {
            TextAlignment.START -> "left"
            TextAlignment.END -> "right"
            else -> "center"
        }
        return """<w:p>
  <w:pPr><w:jc w:val="$imgAlign"/></w:pPr>
  <w:r>
    <w:drawing>
      <wp:inline distT="0" distB="0" distL="0" distR="0">
        <wp:extent cx="$cx" cy="$cy"/>
        <wp:docPr id="1" name="${escapeXml(block.caption.ifBlank { "Picture" })}"/>
        <a:graphic>
          <a:graphicData uri="http://schemas.openxmlformats.org/drawingml/2006/picture">
            <pic:pic>
              <pic:nvPicPr>
                <pic:cNvPr id="0" name="Picture"/>
                <pic:cNvPicPr/>
              </pic:nvPicPr>
              <pic:blipFill>
                <a:blip r:embed="${img.relId}"/>
                <a:stretch><a:fillRect/></a:stretch>
              </pic:blipFill>
              <pic:spPr>
                <a:xfrm><a:off x="0" y="0"/><a:ext cx="$cx" cy="$cy"/></a:xfrm>
                <a:prstGeom prst="rect"><a:avLst/></a:prstGeom>
              </pic:spPr>
            </pic:pic>
          </a:graphicData>
        </a:graphic>
      </wp:inline>
    </w:drawing>
  </w:r>
  ${if (block.caption.isNotBlank()) "<w:p><w:pPr><w:jc w:val=\"$imgAlign\"/></w:pPr><w:r><w:rPr><w:i/><w:sz w:val=\"18\"/><w:color w:val=\"666666\"/></w:rPr><w:t xml:space=\"preserve\">${escapeXml(block.caption)}</w:t></w:r></w:p>" else ""}
</w:p>"""
    }

    private fun buildSectionPropertiesXml(document: RichDocument): String {
        val layout = document.layoutConfig
        val isLandscape = layout.orientation == PageOrientation.LANDSCAPE

        // 1 pt = 20 twips
        // Dimensions in twips:
        val widthTwips = when (layout.pageSize) {
            PageSize.A4 -> 11906
            PageSize.LETTER -> 12240
            PageSize.LEGAL -> 12240
            PageSize.A5 -> 8390
            PageSize.EXECUTIVE -> 10440
        }
        val heightTwips = when (layout.pageSize) {
            PageSize.A4 -> 16838
            PageSize.LETTER -> 15840
            PageSize.LEGAL -> 20160
            PageSize.A5 -> 11906
            PageSize.EXECUTIVE -> 15120
        }

        val effWidth = if (isLandscape) heightTwips else widthTwips
        val effHeight = if (isLandscape) widthTwips else heightTwips

        // Margins in twips: mm / 25.4 * 1440
        val topTwips = ((layout.topMarginMm / 25.4f) * 1440f).toInt()
        val bottomTwips = ((layout.bottomMarginMm / 25.4f) * 1440f).toInt()
        val leftTwips = ((layout.leftMarginMm / 25.4f) * 1440f).toInt()
        val rightTwips = ((layout.rightMarginMm / 25.4f) * 1440f).toInt()

        val sb = StringBuilder()
        sb.append("<w:sectPr>")

        if (layout.showHeader && layout.headerText.isNotBlank()) {
            sb.append("""<w:headerReference w:type="default" r:id="rIdHeader1"/>""")
        }
        if (layout.showFooter && layout.footerText.isNotBlank()) {
            sb.append("""<w:footerReference w:type="default" r:id="rIdFooter1"/>""")
        }

        sb.append("""<w:pgSz w:w="$effWidth" w:h="$effHeight" ${if (isLandscape) """w:orient="landscape"""" else ""}/>""")
        sb.append("""<w:pgMar w:top="$topTwips" w:right="$rightTwips" w:bottom="$bottomTwips" w:left="$leftTwips" w:header="720" w:footer="720" w:gutter="0"/>""")

        sb.append("</w:sectPr>")
        return sb.toString()
    }

    // ----------------------------------------------------
    // Paragraph Span Segmentation Engine
    // ----------------------------------------------------

    private data class FormattedRunSlice(
        val text: String,
        val isBold: Boolean = false,
        val isItalic: Boolean = false,
        val isUnderline: Boolean = false,
        val isStrikethrough: Boolean = false,
        val isSuperscript: Boolean = false,
        val isSubscript: Boolean = false,
        val fontSizeSp: Float? = null,
        val textColorHex: String? = null,
        val highlightColorHex: String? = null,
        val fontFamilyName: String? = null,
        val linkUrl: String? = null
    )

    private fun segmentParagraphRuns(text: String, spans: List<InlineSpan>): List<FormattedRunSlice> {
        if (text.isEmpty()) return listOf(FormattedRunSlice(""))
        if (spans.isEmpty()) return listOf(FormattedRunSlice(text))

        val boundaries = sortedSetOf(0, text.length)
        for (span in spans) {
            boundaries.add(span.start.coerceIn(0, text.length))
            boundaries.add(span.end.coerceIn(0, text.length))
        }

        val boundaryList = boundaries.toList()
        val slices = mutableListOf<FormattedRunSlice>()

        for (i in 0 until boundaryList.size - 1) {
            val start = boundaryList[i]
            val end = boundaryList[i + 1]
            if (start >= end) continue

            val sliceText = text.substring(start, end)
            val matchingSpans = spans.filter { it.start <= start && it.end >= end }

            var isBold = false
            var isItalic = false
            var isUnderline = false
            var isStrikethrough = false
            var isSuperscript = false
            var isSubscript = false
            var fontSizeSp: Float? = null
            var textColorHex: String? = null
            var highlightColorHex: String? = null
            var fontFamilyName: String? = null
            var linkUrl: String? = null

            for (span in matchingSpans) {
                if (span.isBold) isBold = true
                if (span.isItalic) isItalic = true
                if (span.isUnderline) isUnderline = true
                if (span.isStrikethrough) isStrikethrough = true
                if (span.isSuperscript) isSuperscript = true
                if (span.isSubscript) isSubscript = true
                if (span.fontSizeSp != null) fontSizeSp = span.fontSizeSp
                if (span.textColorHex != null) textColorHex = span.textColorHex
                if (span.highlightColorHex != null) highlightColorHex = span.highlightColorHex
                if (span.fontFamilyName != null) fontFamilyName = span.fontFamilyName
                if (span.linkUrl != null) linkUrl = span.linkUrl
            }

            slices.add(
                FormattedRunSlice(
                    text = sliceText,
                    isBold = isBold,
                    isItalic = isItalic,
                    isUnderline = isUnderline,
                    isStrikethrough = isStrikethrough,
                    isSuperscript = isSuperscript,
                    isSubscript = isSubscript,
                    fontSizeSp = fontSizeSp,
                    textColorHex = textColorHex,
                    highlightColorHex = highlightColorHex,
                    fontFamilyName = fontFamilyName,
                    linkUrl = linkUrl
                )
            )
        }

        return slices
    }

    private fun mapColorToWordHighlight(hex: String): String {
        val clean = hex.removePrefix("#").uppercase()
        return when (clean) {
            "FFFF00" -> "yellow"
            "00FF00" -> "green"
            "00FFFF" -> "cyan"
            "FF00FF" -> "magenta"
            "0000FF" -> "blue"
            "FF0000" -> "red"
            "808080" -> "darkGray"
            "C0C0C0" -> "lightGray"
            else -> "yellow"
        }
    }

    private fun escapeXml(str: String): String {
        return str
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&apos;")
    }
}
