package com.example.data.service.docx

import com.example.core.typography.BidiUtils
import com.example.data.model.BlockElement
import com.example.data.model.DividerBlock
import com.example.data.model.HeadingStyle
import com.example.data.model.ImageBlock
import com.example.data.model.InlineSpan
import com.example.data.model.ListType
import com.example.data.model.PageBreakBlock
import com.example.data.model.ParagraphBlock
import com.example.data.model.TextAlignment
import org.w3c.dom.Element
import org.w3c.dom.Node

/**
 * Parses WordprocessingML `w:p` XML elements into native ParagraphBlock or special block elements.
 */
object DocxParagraphParser {

    fun parseParagraphElement(
        pElement: Element,
        numberingMap: Map<String, ListType>,
        relIdToLocalUri: Map<String, String>,
        relMap: Map<String, String>,
        warnings: MutableList<String>
    ): BlockElement? {
        val brNodes = pElement.getElementsByTagName("w:br")
        for (i in 0 until brNodes.length) {
            val br = brNodes.item(i) as? Element
            if (br?.getAttribute("w:type") == "page") {
                return PageBreakBlock()
            }
        }

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

                    val extentEl = pElement.getElementsByTagName("wp:extent").item(0) as? Element
                    val cx = extentEl?.getAttribute("cx")?.toLongOrNull()
                    val cy = extentEl?.getAttribute("cy")?.toLongOrNull()
                    val widthDp = if (cx != null && cx > 0L) (cx / 12700L).toInt().coerceIn(30, 800) else 240
                    val heightDp = if (cy != null && cy > 0L) (cy / 12700L).toInt().coerceIn(30, 800) else 180
                    val aspectRatio = if (heightDp > 0) widthDp.toFloat() / heightDp.toFloat() else 1.333f

                    return ImageBlock(
                        imageUri = localUri,
                        caption = caption,
                        widthDp = widthDp,
                        heightDp = heightDp,
                        aspectRatio = aspectRatio
                    )
                }
            }
        }

        val pBdrNodes = pElement.getElementsByTagName("w:pBdr")
        if (pBdrNodes.length > 0) {
            val bottom = (pBdrNodes.item(0) as? Element)?.getElementsByTagName("w:bottom")
            if (bottom != null && bottom.length > 0) {
                return DividerBlock()
            }
        }

        val pPr = pElement.getElementsByTagName("w:pPr").item(0) as? Element

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

        var listType = ListType.NONE
        val numPr = pPr?.getElementsByTagName("w:numPr")?.item(0) as? Element
        if (numPr != null) {
            val numId = (numPr.getElementsByTagName("w:numId").item(0) as? Element)?.getAttribute("w:val")
            if (numId != null) {
                listType = numberingMap[numId] ?: ListType.BULLET
            }
        }

        var indentationLevel = 0
        var leftIndentPt = 0f
        val ind = pPr?.getElementsByTagName("w:ind")?.item(0) as? Element
        if (ind != null) {
            ind.getAttribute("w:left")?.toIntOrNull()?.let { twips ->
                indentationLevel = (twips / 720).coerceIn(0, 5)
                leftIndentPt = twips / 20f
            }
            if (leftIndentPt == 0f) {
                ind.getAttribute("w:firstLine")?.toIntOrNull()?.let { twips ->
                    leftIndentPt = twips / 20f
                }
            }
        }

        var lineSpacing = 1.25f
        var spaceBeforePt = 0f
        var spaceAfterPt = 0f
        val spacing = pPr?.getElementsByTagName("w:spacing")?.item(0) as? Element
        if (spacing != null) {
            spacing.getAttribute("w:before")?.toIntOrNull()?.let { twips ->
                spaceBeforePt = twips / 20f
            }
            spacing.getAttribute("w:after")?.toIntOrNull()?.let { twips ->
                spaceAfterPt = twips / 20f
            }
            spacing.getAttribute("w:line")?.toIntOrNull()?.let { lineTwips ->
                lineSpacing = (lineTwips / 240f).coerceIn(1.0f, 3.0f)
            }
        }

        var isRtl = pPr?.getElementsByTagName("w:bidi")?.length ?: 0 > 0

        val fullTextBuilder = StringBuilder()
        val spans = mutableListOf<InlineSpan>()

        val childNodes = pElement.childNodes
        for (i in 0 until childNodes.length) {
            val node = childNodes.item(i)
            if (node.nodeType != Node.ELEMENT_NODE) continue
            val el = node as Element

            if (el.tagName == "w:r") {
                DocxRunParser.parseRun(
                    el = el,
                    fullTextBuilder = fullTextBuilder,
                    spans = spans,
                    onRtlDetected = { isRtl = true }
                )
            } else if (el.tagName == "w:hyperlink") {
                DocxRunParser.parseHyperlink(
                    el = el,
                    relMap = relMap,
                    fullTextBuilder = fullTextBuilder,
                    spans = spans
                )
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
            lineSpacingMultiplier = lineSpacing,
            spaceBeforePt = spaceBeforePt,
            spaceAfterPt = spaceAfterPt,
            leftIndentPt = leftIndentPt
        )
    }
}
