package com.example.data.service.docx

import android.content.Context
import android.net.Uri
import com.example.core.error.DocumentError
import com.example.core.error.DocumentResult
import com.example.core.layout.MarginPreset
import com.example.core.layout.PageLayoutConfig
import com.example.core.layout.PageOrientation
import com.example.core.layout.PageSize
import com.example.core.typography.BidiUtils
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
import org.w3c.dom.Document
import org.w3c.dom.Element
import org.w3c.dom.Node
import org.w3c.dom.NodeList
import java.io.ByteArrayInputStream
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.util.UUID
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import javax.xml.parsers.DocumentBuilderFactory

/**
 * Robust native parser for Microsoft Word (.docx) files.
 * Validates package integrity, parses typography, headings, tables, images,
 * and page layout properties, and flags unsupported advanced Word features.
 */
class DocxPackageReader(private val context: Context?) {

    private val docBuilderFactory = DocumentBuilderFactory.newInstance().apply {
        isNamespaceAware = true
        // XXE Prevention
        try {
            setFeature("http://apache.org/xml/features/disallow-doctype-decl", true)
            setFeature("http://xml.org/sax/features/external-general-entities", false)
            setFeature("http://xml.org/sax/features/external-parameter-entities", false)
        } catch (e: Exception) {
            // Features might be unavailable on some parser implementations
        }
    }

    fun read(inputStream: InputStream): DocumentResult<DocxImportResult> {
        return try {
            val zipEntries = mutableMapOf<String, ByteArray>()
            val zipIn = ZipInputStream(inputStream)
            var entry: ZipEntry? = zipIn.nextEntry

            var isZip = false
            while (entry != null) {
                isZip = true
                val name = entry.name
                // ZipSlip guard
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

            // 1. Detect unsupported advanced features
            if (zipEntries.keys.any { it.startsWith("word/vbaProject") || it.endsWith(".bin") }) {
                warnings.add("VBA macros detected in document were skipped for security.")
            }
            if (zipEntries.keys.any { it.startsWith("word/diagrams") }) {
                warnings.add("SmartArt diagrams in document were omitted.")
            }
            if (zipEntries.keys.any { it.startsWith("word/charts") }) {
                warnings.add("Charts were omitted; convert charts to images before importing for best results.")
            }
            if (zipEntries.keys.any { it.startsWith("word/comments") }) {
                warnings.add("Document comments were not imported.")
            }
            if (zipEntries.keys.any { it.startsWith("word/footnotes") || it.startsWith("word/endnotes") }) {
                warnings.add("Footnotes and endnotes were omitted.")
            }

            // 2. Parse metadata from docProps/core.xml
            var docTitle = "Imported Document"
            var docAuthor = "Author"
            zipEntries["docProps/core.xml"]?.let { coreBytes ->
                try {
                    val coreDom = parseXml(coreBytes)
                    val titleNodes = coreDom.getElementsByTagNameNS("http://purl.org/dc/elements/1.1/", "title")
                    if (titleNodes.length > 0 && titleNodes.item(0).textContent.isNotBlank()) {
                        docTitle = titleNodes.item(0).textContent.trim()
                    }
                    val creatorNodes = coreDom.getElementsByTagNameNS("http://purl.org/dc/elements/1.1/", "creator")
                    if (creatorNodes.length > 0 && creatorNodes.item(0).textContent.isNotBlank()) {
                        docAuthor = creatorNodes.item(0).textContent.trim()
                    }
                } catch (e: Exception) {
                    // Ignore metadata parse error
                }
            }

            // 3. Parse numbering definitions from word/numbering.xml
            val numIdToListType = mutableMapOf<String, ListType>()
            zipEntries["word/numbering.xml"]?.let { numBytes ->
                try {
                    val numDom = parseXml(numBytes)
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
                } catch (e: Exception) {
                    // Ignore numbering parsing error
                }
            }

            // 4. Parse relationships from word/_rels/document.xml.rels
            val relMap = mutableMapOf<String, String>() // rId -> target
            zipEntries["word/_rels/document.xml.rels"]?.let { relBytes ->
                try {
                    val relDom = parseXml(relBytes)
                    val relNodes = relDom.getElementsByTagName("Relationship")
                    for (i in 0 until relNodes.length) {
                        val el = relNodes.item(i) as? Element ?: continue
                        val id = el.getAttribute("Id")
                        val target = el.getAttribute("Target")
                        if (id.isNotBlank() && target.isNotBlank()) {
                            relMap[id] = target
                        }
                    }
                } catch (e: Exception) {
                    // Ignore rel parsing error
                }
            }

            // 5. Extract images from media/* into local cache
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

            // 6. Extract header and footer text
            var headerText = ""
            var footerText = "Page {page} of {pages}"

            for ((_, target) in relMap) {
                if (target.contains("header", ignoreCase = true)) {
                    val headerEntry = if (target.startsWith("/")) target.removePrefix("/") else "word/$target"
                    zipEntries[headerEntry]?.let { bytes ->
                        try {
                            val hDom = parseXml(bytes)
                            headerText = extractAllText(hDom).trim()
                        } catch (e: Exception) {}
                    }
                } else if (target.contains("footer", ignoreCase = true)) {
                    val footerEntry = if (target.startsWith("/")) target.removePrefix("/") else "word/$target"
                    zipEntries[footerEntry]?.let { bytes ->
                        try {
                            val fDom = parseXml(bytes)
                            val raw = extractAllText(fDom).trim()
                            if (raw.isNotBlank()) footerText = raw
                        } catch (e: Exception) {}
                    }
                }
            }

            // 7. Parse word/document.xml
            val docBytes = zipEntries["word/document.xml"]
                ?: return DocumentResult.Failure(DocumentError.CorruptDocumentError("Missing word/document.xml in DOCX archive."))

            val docDom = parseXml(docBytes)
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
                        val parsedBlock = parseParagraphElement(element, numIdToListType, relIdToLocalUri, warnings)
                        if (parsedBlock != null) {
                            blocks.add(parsedBlock)
                        }
                    }
                    "w:tbl" -> {
                        val parsedTable = parseTableElement(element)
                        blocks.add(parsedTable)
                    }
                    "w:sectPr" -> {
                        parsedLayoutConfig = parseSectionProperties(element, parsedLayoutConfig)
                    }
                }
            }

            if (blocks.isEmpty()) {
                blocks.add(ParagraphBlock(text = ""))
            }

            val richDoc = RichDocument(
                id = UUID.randomUUID().toString(),
                title = docTitle,
                author = docAuthor,
                layoutConfig = parsedLayoutConfig,
                blocks = blocks
            )

            val compNotes = mutableListOf<String>()
            compNotes.add("Imported ${richDoc.blocks.size} blocks, ${richDoc.wordCount} words.")
            if (warnings.isNotEmpty()) {
                compNotes.add("${warnings.size} unsupported features noted.")
            }

            DocumentResult.Success(
                DocxImportResult(
                    document = richDoc,
                    warnings = warnings,
                    compatibilityNotes = compNotes
                )
            )
        } catch (e: Exception) {
            DocumentResult.Failure(
                DocumentError.CorruptDocumentError("Failed to parse DOCX package: ${e.message}")
            )
        }
    }

    private fun parseXml(bytes: ByteArray): Document {
        return docBuilderFactory.newDocumentBuilder().parse(ByteArrayInputStream(bytes))
    }

    private fun extractAllText(dom: Document): String {
        val tNodes = dom.getElementsByTagName("w:t")
        val sb = StringBuilder()
        for (i in 0 until tNodes.length) {
            sb.append(tNodes.item(i).textContent)
        }
        return sb.toString()
    }

    private fun parseParagraphElement(
        pElement: Element,
        numberingMap: Map<String, ListType>,
        relIdToLocalUri: Map<String, String>,
        warnings: MutableList<String>
    ): BlockElement? {
        // 1. Check for page break
        val brNodes = pElement.getElementsByTagName("w:br")
        for (i in 0 until brNodes.length) {
            val br = brNodes.item(i) as? Element
            if (br?.getAttribute("w:type") == "page") {
                return PageBreakBlock()
            }
        }

        // 2. Check for drawing / image
        val drawingNodes = pElement.getElementsByTagName("w:drawing")
        if (drawingNodes.length > 0) {
            val blipNodes = pElement.getElementsByTagName("a:blip")
            for (i in 0 until blipNodes.length) {
                val blip = blipNodes.item(i) as? Element
                val embedRelId = blip?.getAttribute("r:embed")
                if (!embedRelId.isNullOrBlank() && relIdToLocalUri.containsKey(embedRelId)) {
                    val localUri = relIdToLocalUri[embedRelId]!!
                    val docPr = pElement.getElementsByTagName("wp:docPr").item(0) as? Element
                    val caption = docPr?.getAttribute("name") ?: ""
                    return ImageBlock(imageUri = localUri, caption = caption)
                }
            }
        }

        // 3. Check for horizontal border (Divider)
        val pBdrNodes = pElement.getElementsByTagName("w:pBdr")
        if (pBdrNodes.length > 0) {
            val bottom = (pBdrNodes.item(0) as? Element)?.getElementsByTagName("w:bottom")
            if (bottom != null && bottom.length > 0) {
                return DividerBlock()
            }
        }

        // 4. Standard Paragraph
        val pPr = pElement.getElementsByTagName("w:pPr").item(0) as? Element

        // Alignment
        var alignment = TextAlignment.START
        val jc = pPr?.getElementsByTagName("w:jc")?.item(0) as? Element
        jc?.getAttribute("w:val")?.let { jcVal ->
            alignment = when (jcVal) {
                "center" -> TextAlignment.CENTER
                "right" -> TextAlignment.END
                "both" -> TextAlignment.JUSTIFY
                else -> TextAlignment.START
            }
        }

        // Heading Style
        var headingStyle = HeadingStyle.NORMAL
        val pStyle = pPr?.getElementsByTagName("w:pStyle")?.item(0) as? Element
        pStyle?.getAttribute("w:val")?.let { sVal ->
            headingStyle = when (sVal.lowercase()) {
                "title" -> HeadingStyle.TITLE
                "subtitle" -> HeadingStyle.SUBTITLE
                "heading1", "heading 1" -> HeadingStyle.HEADING_1
                "heading2", "heading 2" -> HeadingStyle.HEADING_2
                "heading3", "heading 3" -> HeadingStyle.HEADING_3
                "quote" -> HeadingStyle.QUOTE
                else -> HeadingStyle.NORMAL
            }
        }

        // List Type
        var listType = ListType.NONE
        val numPr = pPr?.getElementsByTagName("w:numPr")?.item(0) as? Element
        if (numPr != null) {
            val numId = (numPr.getElementsByTagName("w:numId").item(0) as? Element)?.getAttribute("w:val")
            if (numId != null) {
                listType = numberingMap[numId] ?: ListType.BULLET
            }
        }

        // Indentation Level
        var indentationLevel = 0
        val ind = pPr?.getElementsByTagName("w:ind")?.item(0) as? Element
        ind?.getAttribute("w:left")?.toIntOrNull()?.let { twips ->
            indentationLevel = (twips / 720).coerceIn(0, 5)
        }

        // Line Spacing
        var lineSpacing = 1.25f
        val spacing = pPr?.getElementsByTagName("w:spacing")?.item(0) as? Element
        spacing?.getAttribute("w:line")?.toIntOrNull()?.let { lineTwips ->
            lineSpacing = (lineTwips / 240f).coerceIn(1.0f, 3.0f)
        }

        // BiDi / RTL
        var isRtl = pPr?.getElementsByTagName("w:bidi")?.length ?: 0 > 0

        // Parse text runs and spans
        val fullTextBuilder = StringBuilder()
        val spans = mutableListOf<InlineSpan>()

        val childNodes = pElement.childNodes
        for (i in 0 until childNodes.length) {
            val node = childNodes.item(i)
            if (node.nodeType != Node.ELEMENT_NODE) continue
            val el = node as Element

            if (el.tagName == "w:r") {
                val rPr = el.getElementsByTagName("w:rPr").item(0) as? Element

                val isBold = rPr?.getElementsByTagName("w:b")?.length ?: 0 > 0
                val isItalic = rPr?.getElementsByTagName("w:i")?.length ?: 0 > 0
                val isUnderline = rPr?.getElementsByTagName("w:u")?.length ?: 0 > 0
                val isStrike = rPr?.getElementsByTagName("w:strike")?.length ?: 0 > 0

                var isSuperscript = false
                var isSubscript = false
                val vertAlign = rPr?.getElementsByTagName("w:vertAlign")?.item(0) as? Element
                vertAlign?.getAttribute("w:val")?.let {
                    if (it == "superscript") isSuperscript = true
                    if (it == "subscript") isSubscript = true
                }

                var fontSizeSp: Float? = null
                val sz = rPr?.getElementsByTagName("w:sz")?.item(0) as? Element
                sz?.getAttribute("w:val")?.toIntOrNull()?.let { halfPts ->
                    fontSizeSp = halfPts / 2f
                }

                var textColorHex: String? = null
                val color = rPr?.getElementsByTagName("w:color")?.item(0) as? Element
                color?.getAttribute("w:val")?.let { hex ->
                    if (hex != "auto" && hex.isNotBlank()) {
                        textColorHex = "#$hex"
                    }
                }

                var highlightHex: String? = null
                val highlight = rPr?.getElementsByTagName("w:highlight")?.item(0) as? Element
                highlight?.getAttribute("w:val")?.let { hVal ->
                    highlightHex = mapWordHighlightToHex(hVal)
                }

                var fontName: String? = null
                val rFonts = rPr?.getElementsByTagName("w:rFonts")?.item(0) as? Element
                rFonts?.getAttribute("w:ascii")?.let { f ->
                    if (f.isNotBlank()) fontName = f
                }

                if (rPr?.getElementsByTagName("w:rtl")?.length ?: 0 > 0) {
                    isRtl = true
                }

                // Collect run text
                val tNodes = el.getElementsByTagName("w:t")
                val runTextBuilder = StringBuilder()
                for (j in 0 until tNodes.length) {
                    runTextBuilder.append(tNodes.item(j).textContent)
                }
                val runText = runTextBuilder.toString()

                if (runText.isNotEmpty()) {
                    val runStart = fullTextBuilder.length
                    val runEnd = runStart + runText.length
                    fullTextBuilder.append(runText)

                    val hasFormatting = isBold || isItalic || isUnderline || isStrike ||
                            isSuperscript || isSubscript || fontSizeSp != null ||
                            textColorHex != null || highlightHex != null || fontName != null

                    if (hasFormatting) {
                        spans.add(
                            InlineSpan(
                                start = runStart,
                                end = runEnd,
                                isBold = isBold,
                                isItalic = isItalic,
                                isUnderline = isUnderline,
                                isStrikethrough = isStrike,
                                isSuperscript = isSuperscript,
                                isSubscript = isSubscript,
                                fontSizeSp = fontSizeSp,
                                textColorHex = textColorHex,
                                highlightColorHex = highlightHex,
                                fontFamilyName = fontName
                            )
                        )
                    }
                }
            } else if (el.tagName == "w:hyperlink") {
                val tNodes = el.getElementsByTagName("w:t")
                val linkText = StringBuilder()
                for (k in 0 until tNodes.length) {
                    linkText.append(tNodes.item(k).textContent)
                }
                val text = linkText.toString()
                if (text.isNotEmpty()) {
                    val runStart = fullTextBuilder.length
                    val runEnd = runStart + text.length
                    fullTextBuilder.append(text)
                    spans.add(
                        InlineSpan(
                            start = runStart,
                            end = runEnd,
                            isUnderline = true,
                            textColorHex = "#185ABD",
                            linkUrl = "https://external.link"
                        )
                    )
                }
            }
        }

        val text = fullTextBuilder.toString()
        if (!isRtl && BidiUtils.detectParagraphIsRtl(text)) {
            isRtl = true
        }

        return ParagraphBlock(
            text = text,
            spans = spans,
            alignment = alignment,
            headingStyle = headingStyle,
            listType = listType,
            indentationLevel = indentationLevel,
            isRtl = isRtl,
            lineSpacingMultiplier = lineSpacing
        )
    }

    private fun parseTableElement(tblElement: Element): TableBlock {
        val rows = mutableListOf<List<TableCell>>()
        val trNodes = tblElement.getElementsByTagName("w:tr")

        val tblPr = tblElement.getElementsByTagName("w:tblPr").item(0) as? Element
        val tblJc = (tblPr?.getElementsByTagName("w:jc")?.item(0) as? Element)?.getAttribute("w:val")
        val tableAlignment = when (tblJc) {
            "left" -> TextAlignment.START
            "right" -> TextAlignment.END
            else -> TextAlignment.CENTER
        }
        val tblBorders = tblPr?.getElementsByTagName("w:tblBorders")?.item(0) as? Element
        val hasBorders = if (tblBorders != null) {
            val topVal = (tblBorders.getElementsByTagName("w:top").item(0) as? Element)?.getAttribute("w:val")
            topVal != "none"
        } else true

        var maxCols = 0
        for (r in 0 until trNodes.length) {
            val tr = trNodes.item(r) as? Element ?: continue
            val tcNodes = tr.getElementsByTagName("w:tc")
            val cellList = mutableListOf<TableCell>()

            for (c in 0 until tcNodes.length) {
                val tc = tcNodes.item(c) as? Element ?: continue
                val textNodes = tc.getElementsByTagName("w:t")
                val textBuilder = StringBuilder()
                for (t in 0 until textNodes.length) {
                    textBuilder.append(textNodes.item(t).textContent)
                }
                val cellText = textBuilder.toString()
                val isBold = tc.getElementsByTagName("w:b").length > 0
                val isItalic = tc.getElementsByTagName("w:i").length > 0
                val jcNode = tc.getElementsByTagName("w:jc").item(0) as? Element
                val cellAlign = when (jcNode?.getAttribute("w:val")) {
                    "center" -> TextAlignment.CENTER
                    "right" -> TextAlignment.END
                    "both" -> TextAlignment.JUSTIFY
                    else -> TextAlignment.START
                }

                var bgHex: String? = null
                val shd = tc.getElementsByTagName("w:shd").item(0) as? Element
                shd?.getAttribute("w:fill")?.let { fill ->
                    if (fill != "auto" && fill.isNotBlank()) {
                        bgHex = "#$fill"
                    }
                }

                cellList.add(
                    TableCell(
                        text = cellText,
                        isBold = isBold,
                        isItalic = isItalic,
                        backgroundColorHex = bgHex,
                        alignment = cellAlign
                    )
                )
            }
            if (cellList.size > maxCols) maxCols = cellList.size
            rows.add(cellList)
        }

        val rowCount = rows.size.coerceAtLeast(1)
        val colCount = maxCols.coerceAtLeast(1)

        // Pad uneven rows to rectangular grid
        val normalizedRows = rows.map { row ->
            if (row.size < colCount) {
                row + List(colCount - row.size) { TableCell() }
            } else row
        }

        return TableBlock(
            rowCount = rowCount,
            columnCount = colCount,
            cells = normalizedRows,
            hasBorders = hasBorders,
            alignment = tableAlignment
        )
    }

    private fun parseSectionProperties(sectPr: Element, currentConfig: PageLayoutConfig): PageLayoutConfig {
        var pageSize = currentConfig.pageSize
        var orientation = currentConfig.orientation
        var topMm: Float? = null
        var bottomMm: Float? = null
        var leftMm: Float? = null
        var rightMm: Float? = null

        val pgSz = sectPr.getElementsByTagName("w:pgSz").item(0) as? Element
        if (pgSz != null) {
            val w = pgSz.getAttribute("w:w").toIntOrNull() ?: 11906
            val h = pgSz.getAttribute("w:h").toIntOrNull() ?: 16838
            val orient = pgSz.getAttribute("w:orient")
            orientation = if (orient == "landscape") PageOrientation.LANDSCAPE else PageOrientation.PORTRAIT

            val effectiveW = if (orientation == PageOrientation.LANDSCAPE) h else w
            pageSize = when {
                effectiveW in 11800..12050 -> PageSize.A4
                effectiveW in 12100..12350 -> PageSize.LETTER
                effectiveW in 8200..8500 -> PageSize.A5
                effectiveW in 10300..10600 -> PageSize.EXECUTIVE
                else -> PageSize.A4
            }
        }

        val pgMar = sectPr.getElementsByTagName("w:pgMar").item(0) as? Element
        if (pgMar != null) {
            pgMar.getAttribute("w:top").toIntOrNull()?.let { topMm = (it / 1440f) * 25.4f }
            pgMar.getAttribute("w:bottom").toIntOrNull()?.let { bottomMm = (it / 1440f) * 25.4f }
            pgMar.getAttribute("w:left").toIntOrNull()?.let { leftMm = (it / 1440f) * 25.4f }
            pgMar.getAttribute("w:right").toIntOrNull()?.let { rightMm = (it / 1440f) * 25.4f }
        }

        return currentConfig.copy(
            pageSize = pageSize,
            orientation = orientation,
            marginPreset = MarginPreset.CUSTOM,
            customTopMarginMm = topMm,
            customBottomMarginMm = bottomMm,
            customLeftMarginMm = leftMm,
            customRightMarginMm = rightMm
        )
    }

    private fun mapWordHighlightToHex(name: String): String {
        return when (name.lowercase()) {
            "yellow" -> "#FFFF00"
            "green" -> "#00FF00"
            "cyan" -> "#00FFFF"
            "magenta" -> "#FF00FF"
            "blue" -> "#0000FF"
            "red" -> "#FF0000"
            "darkgray" -> "#808080"
            "lightgray" -> "#D3D3D3"
            else -> "#FFFF00"
        }
    }
}
