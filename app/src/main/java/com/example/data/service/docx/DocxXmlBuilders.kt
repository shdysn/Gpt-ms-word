package com.example.data.service.docx

import com.example.core.typography.BidiUtils
import com.example.data.model.DocumentComment
import com.example.data.model.FootnoteBlock
import com.example.data.model.RichDocument
import com.example.data.model.TextAlignment
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/**
 * Generates Open Packaging Convention (OPC) XML files (Content_Types, Rels, Styles, Numbering, Props, Headers, Footers).
 */
object DocxXmlBuilders {

    fun escapeXml(text: String): String {
        return text
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&apos;")
    }

    fun buildContentTypesXml(document: RichDocument, images: List<DocxBodyXmlBuilder.EmbeddedImage>): String {
        val sb = StringBuilder()
        sb.append("""<?xml version="1.0" encoding="UTF-8" standalone="yes"?>""")
        sb.append("""<Types xmlns="http://schemas.openxmlformats.org/package/2006/content-types">""")
        sb.append("""<Default Extension="rels" ContentType="application/vnd.openxmlformats-package.relationships+xml"/>""")
        sb.append("""<Default Extension="xml" ContentType="application/xml"/>""")
        val hasJpeg = images.any { it.contentType == "image/jpeg" }
        val hasPng = images.any { it.contentType == "image/png" }
        if (hasJpeg) sb.append("""<Default Extension="jpeg" ContentType="image/jpeg"/><Default Extension="jpg" ContentType="image/jpeg"/>""")
        if (hasPng || (!hasJpeg && images.isNotEmpty())) sb.append("""<Default Extension="png" ContentType="image/png"/>""")
        sb.append("""<Override PartName="/word/document.xml" ContentType="application/vnd.openxmlformats-officedocument.wordprocessingml.document.main+xml"/>""")
        sb.append("""<Override PartName="/word/styles.xml" ContentType="application/vnd.openxmlformats-officedocument.wordprocessingml.styles+xml"/>""")
        sb.append("""<Override PartName="/word/numbering.xml" ContentType="application/vnd.openxmlformats-officedocument.wordprocessingml.numbering+xml"/>""")
        sb.append("""<Override PartName="/word/fontTable.xml" ContentType="application/vnd.openxmlformats-officedocument.wordprocessingml.fontTable+xml"/>""")
        sb.append("""<Override PartName="/word/settings.xml" ContentType="application/vnd.openxmlformats-officedocument.wordprocessingml.settings+xml"/>""")
        sb.append("""<Override PartName="/word/webSettings.xml" ContentType="application/vnd.openxmlformats-officedocument.wordprocessingml.webSettings+xml"/>""")
        if (document.layoutConfig.showHeader && document.layoutConfig.headerText.isNotBlank()) {
            sb.append("""<Override PartName="/word/header1.xml" ContentType="application/vnd.openxmlformats-officedocument.wordprocessingml.header+xml"/>""")
        }
        if (document.layoutConfig.showFooter && document.layoutConfig.footerText.isNotBlank()) {
            sb.append("""<Override PartName="/word/footer1.xml" ContentType="application/vnd.openxmlformats-officedocument.wordprocessingml.footer+xml"/>""")
        }
        if (document.comments.isNotEmpty()) {
            sb.append("""<Override PartName="/word/comments.xml" ContentType="application/vnd.openxmlformats-officedocument.wordprocessingml.comments+xml"/>""")
        }
        if (document.blocks.any { it is FootnoteBlock }) {
            sb.append("""<Override PartName="/word/footnotes.xml" ContentType="application/vnd.openxmlformats-officedocument.wordprocessingml.footnotes+xml"/>""")
        }
        sb.append("""<Override PartName="/docProps/core.xml" ContentType="application/vnd.openxmlformats-package.core-properties+xml"/>""")
        sb.append("""<Override PartName="/docProps/app.xml" ContentType="application/vnd.openxmlformats-officedocument.extended-properties+xml"/>""")
        sb.append("""</Types>""")
        return sb.toString()
    }

    fun buildPackageRelsXml(): String {
        return """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
  <Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument" Target="word/document.xml"/>
  <Relationship Id="rId2" Type="http://schemas.openxmlformats.org/package/2006/relationships/metadata/core-properties" Target="docProps/core.xml"/>
  <Relationship Id="rId3" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/extended-properties" Target="docProps/app.xml"/>
</Relationships>"""
    }

    fun buildDocumentRelsXml(
        document: RichDocument,
        images: List<DocxBodyXmlBuilder.EmbeddedImage>,
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
        if (document.comments.isNotEmpty()) {
            sb.append("""<Relationship Id="rIdComments" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/comments" Target="comments.xml"/>""")
        }
        if (document.blocks.any { it is FootnoteBlock }) {
            sb.append("""<Relationship Id="rIdFootnotes" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/footnotes" Target="footnotes.xml"/>""")
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

    fun buildDocPropsCoreXml(document: RichDocument): String {
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

    fun buildDocPropsAppXml(document: RichDocument): String {
        return """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Properties xmlns="http://schemas.openxmlformats.org/officeDocument/2006/extended-properties"
    xmlns:vt="http://schemas.openxmlformats.org/officeDocument/2006/docPropsVTypes">
  <Application>Smart Word Processor for Android</Application>
  <DocSecurity>0</DocSecurity>
  <Lines>${document.blocks.size}</Lines>
  <Paragraphs>${document.blocks.count { it is com.example.data.model.ParagraphBlock }}</Paragraphs>
  <Words>${document.wordCount}</Words>
  <Characters>${document.characterCount}</Characters>
  <Pages>${document.estimatedPageCount}</Pages>
</Properties>"""
    }

    fun buildStylesXml(): String {
        return """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<w:styles xmlns:w="http://schemas.openxmlformats.org/wordprocessingml/2006/main">
  <w:docDefaults>
    <w:rPrDefault>
      <w:rPr>
        <w:rFonts w:ascii="Calibri" w:hAnsi="Calibri" w:cs="Noto Nastaliq Urdu"/>
        <w:sz w:val="22"/><w:szCs w:val="24"/><w:lang w:val="en-US" w:bidi="ur-PK"/>
      </w:rPr>
    </w:rPrDefault>
    <w:pPrDefault>
      <w:pPr><w:spacing w:line="276" w:lineRule="auto" w:after="160"/></w:pPr>
    </w:pPrDefault>
  </w:docDefaults>
  <w:style w:type="paragraph" w:default="1" w:styleId="Normal"><w:name w:val="Normal"/><w:qFormat/></w:style>
  <w:style w:type="paragraph" w:styleId="Heading1"><w:name w:val="heading 1"/><w:basedOn w:val="Normal"/><w:qFormat/><w:pPr><w:keepNext/><w:spacing w:before="240" w:after="120"/></w:pPr><w:rPr><w:b/><w:color w:val="185ABD"/><w:sz w:val="32"/><w:szCs w:val="32"/></w:rPr></w:style>
  <w:style w:type="paragraph" w:styleId="Heading2"><w:name w:val="heading 2"/><w:basedOn w:val="Normal"/><w:qFormat/><w:pPr><w:keepNext/><w:spacing w:before="180" w:after="80"/></w:pPr><w:rPr><w:b/><w:color w:val="185ABD"/><w:sz w:val="26"/><w:szCs w:val="26"/></w:rPr></w:style>
  <w:style w:type="paragraph" w:styleId="Heading3"><w:name w:val="heading 3"/><w:basedOn w:val="Normal"/><w:qFormat/><w:pPr><w:keepNext/><w:spacing w:before="120" w:after="60"/></w:pPr><w:rPr><w:b/><w:sz w:val="24"/><w:szCs w:val="24"/></w:rPr></w:style>
  <w:style w:type="paragraph" w:styleId="Title"><w:name w:val="Title"/><w:basedOn w:val="Normal"/><w:qFormat/><w:pPr><w:spacing w:before="360" w:after="180"/><w:jc w:val="center"/></w:pPr><w:rPr><w:b/><w:color w:val="185ABD"/><w:sz w:val="48"/><w:szCs w:val="48"/></w:rPr></w:style>
  <w:style w:type="paragraph" w:styleId="Subtitle"><w:name w:val="Subtitle"/><w:basedOn w:val="Normal"/><w:qFormat/><w:pPr><w:spacing w:before="60" w:after="240"/><w:jc w:val="center"/></w:pPr><w:rPr><w:color w:val="5A6A85"/><w:sz w:val="26"/><w:szCs w:val="26"/></w:rPr></w:style>
  <w:style w:type="paragraph" w:styleId="Quote"><w:name w:val="Quote"/><w:basedOn w:val="Normal"/><w:qFormat/><w:pPr><w:ind w:left="720" w:right="720"/><w:spacing w:before="140" w:after="140"/></w:pPr><w:rPr><w:i/><w:color w:val="475569"/></w:rPr></w:style>
</w:styles>"""
    }

    fun buildNumberingXml(): String {
        return """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<w:numbering xmlns:w="http://schemas.openxmlformats.org/wordprocessingml/2006/main">
  <w:abstractNum w:abstractNumId="0">
    <w:multiLevelType w:val="hybridMultilevel"/>
    <w:lvl w:ilvl="0"><w:start w:val="1"/><w:numFmt w:val="bullet"/><w:lvlText w:val="•"/><w:lvlJc w:val="left"/><w:pPr><w:ind w:left="720" w:hanging="360"/></w:pPr><w:rPr><w:rFonts w:ascii="Symbol" w:hAnsi="Symbol" w:hint="default"/></w:rPr></w:lvl>
  </w:abstractNum>
  <w:abstractNum w:abstractNumId="1">
    <w:multiLevelType w:val="hybridMultilevel"/>
    <w:lvl w:ilvl="0"><w:start w:val="1"/><w:numFmt w:val="decimal"/><w:lvlText w:val="%1."/><w:lvlJc w:val="left"/><w:pPr><w:ind w:left="720" w:hanging="360"/></w:pPr></w:lvl>
  </w:abstractNum>
  <w:num w:numId="1"><w:abstractNumId w:val="0"/></w:num>
  <w:num w:numId="2"><w:abstractNumId w:val="1"/></w:num>
</w:numbering>"""
    }

    fun buildFontTableXml(): String = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<w:fonts xmlns:w="http://schemas.openxmlformats.org/wordprocessingml/2006/main">
  <w:font w:name="Calibri"><w:family w:val="swiss"/></w:font>
  <w:font w:name="Arial"><w:family w:val="swiss"/></w:font>
  <w:font w:name="Times New Roman"><w:family w:val="roman"/></w:font>
  <w:font w:name="Noto Nastaliq Urdu"><w:family w:val="auto"/></w:font>
  <w:font w:name="Noto Sans Arabic"><w:family w:val="auto"/></w:font>
</w:fonts>"""

    fun buildSettingsXml(): String = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<w:settings xmlns:w="http://schemas.openxmlformats.org/wordprocessingml/2006/main">
  <w:defaultTabStop w:val="720"/><w:characterSpacingControl w:val="doNotCompress"/>
  <w:compat><w:compatSetting w:name="compatibilityMode" w:uri="http://schemas.microsoft.com/office/word" w:val="15"/></w:compat>
</w:settings>"""

    fun buildWebSettingsXml(): String = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<w:webSettings xmlns:w="http://schemas.openxmlformats.org/wordprocessingml/2006/main"><w:allowPNG/><w:doNotSaveAsSingleFile/></w:webSettings>"""

    fun buildHeaderXml(document: RichDocument): String {
        val alignment = when (document.layoutConfig.headerAlignment) {
            TextAlignment.CENTER -> "center"
            TextAlignment.END -> "right"
            else -> "left"
        }
        val isRtl = BidiUtils.detectParagraphIsRtl(document.layoutConfig.headerText)
        return """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<w:hdr xmlns:w="http://schemas.openxmlformats.org/wordprocessingml/2006/main">
  <w:p><w:pPr><w:jc w:val="$alignment"/>${if (isRtl) "<w:bidi/>" else ""}</w:pPr>
    <w:r><w:rPr><w:sz w:val="18"/><w:color w:val="666666"/>${if (isRtl) "<w:rtl/>" else ""}</w:rPr>
      <w:t>${escapeXml(document.layoutConfig.headerText)}</w:t>
    </w:r>
  </w:p>
</w:hdr>"""
    }

    fun buildFooterXml(document: RichDocument): String {
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
        sb.append("""<w:p><w:pPr><w:jc w:val="$alignment"/>${if (isRtl) "<w:bidi/>" else ""}</w:pPr>""")

        if (rawFooter.contains("{page}")) {
            val parts = rawFooter.split("{page}")
            val prefix = parts.getOrNull(0) ?: ""
            val rest = parts.getOrNull(1) ?: ""
            if (prefix.isNotEmpty()) sb.append("""<w:r><w:rPr><w:sz w:val="18"/><w:color w:val="666666"/></w:rPr><w:t xml:space="preserve">${escapeXml(prefix)}</w:t></w:r>""")
            sb.append("""<w:fldSimple w:instr="PAGE"><w:r><w:rPr><w:sz w:val="18"/><w:color w:val="666666"/></w:rPr><w:t>1</w:t></w:r></w:fldSimple>""")
            if (rest.contains("{pages}")) {
                val subParts = rest.split("{pages}")
                val middle = subParts.getOrNull(0) ?: ""
                val suffix = subParts.getOrNull(1) ?: ""
                if (middle.isNotEmpty()) sb.append("""<w:r><w:rPr><w:sz w:val="18"/><w:color w:val="666666"/></w:rPr><w:t xml:space="preserve">${escapeXml(middle)}</w:t></w:r>""")
                sb.append("""<w:fldSimple w:instr="NUMPAGES"><w:r><w:rPr><w:sz w:val="18"/><w:color w:val="666666"/></w:rPr><w:t>1</w:t></w:r></w:fldSimple>""")
                if (suffix.isNotEmpty()) sb.append("""<w:r><w:rPr><w:sz w:val="18"/><w:color w:val="666666"/></w:rPr><w:t xml:space="preserve">${escapeXml(suffix)}</w:t></w:r>""")
            } else if (rest.isNotEmpty()) {
                sb.append("""<w:r><w:rPr><w:sz w:val="18"/><w:color w:val="666666"/></w:rPr><w:t xml:space="preserve">${escapeXml(rest)}</w:t></w:r>""")
            }
        } else {
            sb.append("""<w:r><w:rPr><w:sz w:val="18"/><w:color w:val="666666"/></w:rPr><w:t xml:space="preserve">${escapeXml(rawFooter)}</w:t></w:r>""")
        }
        sb.append("""</w:p></w:ftr>""")
        return sb.toString()
    }

    fun buildCommentsXml(comments: List<DocumentComment>): String {
        val isoFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
        val sb = StringBuilder()
        sb.append("""<?xml version="1.0" encoding="UTF-8" standalone="yes"?>""")
        sb.append("""<w:comments xmlns:w="http://schemas.openxmlformats.org/wordprocessingml/2006/main">""")
        comments.forEachIndexed { index, comment ->
            val dateStr = isoFormat.format(Date(comment.timestamp))
            sb.append("""<w:comment w:id="$index" w:author="${escapeXml(comment.author)}" w:date="$dateStr">""")
            sb.append("""<w:p><w:r><w:t>${escapeXml(comment.text)}</w:t></w:r></w:p></w:comment>""")
        }
        sb.append("""</w:comments>""")
        return sb.toString()
    }

    fun buildFootnotesXml(footnotes: List<FootnoteBlock>): String {
        val sb = StringBuilder()
        sb.append("""<?xml version="1.0" encoding="UTF-8" standalone="yes"?>""")
        sb.append("""<w:footnotes xmlns:w="http://schemas.openxmlformats.org/wordprocessingml/2006/main">""")
        sb.append("""<w:footnote w:type="separator" w:id="-1"><w:p><w:r><w:separator/></w:r></w:p></w:footnote>""")
        sb.append("""<w:footnote w:type="continuationSeparator" w:id="0"><w:p><w:r><w:continuationSeparator/></w:r></w:p></w:footnote>""")
        footnotes.forEachIndexed { index, fn ->
            val fnId = index + 1
            sb.append("""<w:footnote w:id="$fnId"><w:p>""")
            sb.append("""<w:r><w:rPr><w:rStyle w:val="FootnoteReference"/></w:rPr><w:footnoteRef/></w:r>""")
            sb.append("""<w:r><w:t xml:space="preserve"> ${escapeXml(fn.noteContent)}</w:t></w:r></w:p></w:footnote>""")
        }
        sb.append("""</w:footnotes>""")
        return sb.toString()
    }
}
