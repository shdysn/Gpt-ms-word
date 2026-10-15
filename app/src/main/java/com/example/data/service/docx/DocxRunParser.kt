package com.example.data.service.docx

import com.example.data.model.InlineSpan
import org.w3c.dom.Element

/**
 * Parses individual `w:r` runs and `w:hyperlink` XML elements inside a Word paragraph.
 */
object DocxRunParser {

    fun parseRun(
        el: Element,
        fullTextBuilder: StringBuilder,
        spans: MutableList<InlineSpan>,
        onRtlDetected: () -> Unit
    ) {
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

        val isRunRtl = (rPr?.getElementsByTagName("w:rtl")?.length ?: 0) > 0 ||
                (rPr?.getElementsByTagName("w:cs")?.length ?: 0) > 0
        if (isRunRtl) onRtlDetected()

        var fontSizeSp: Float? = null
        val sz = rPr?.getElementsByTagName("w:sz")?.item(0) as? Element
        val szCs = rPr?.getElementsByTagName("w:szCs")?.item(0) as? Element
        val halfPts = sz?.getAttribute("w:val")?.toIntOrNull()
            ?: if (isRunRtl) szCs?.getAttribute("w:val")?.toIntOrNull() else null
        if (halfPts != null) fontSizeSp = halfPts / 2f

        var textColorHex: String? = null
        val color = rPr?.getElementsByTagName("w:color")?.item(0) as? Element
        color?.getAttribute("w:val")?.let { hex ->
            if (hex != "auto" && hex.isNotBlank()) textColorHex = "#$hex"
        }

        var highlightHex: String? = null
        val highlight = rPr?.getElementsByTagName("w:highlight")?.item(0) as? Element
        highlight?.getAttribute("w:val")?.let { hVal ->
            highlightHex = mapWordHighlightToHex(hVal)
        }

        var fontName: String? = null
        val rFonts = rPr?.getElementsByTagName("w:rFonts")?.item(0) as? Element
        if (rFonts != null) {
            val csFont = rFonts.getAttribute("w:cs").takeIf { it.isNotBlank() }
            val asciiFont = rFonts.getAttribute("w:ascii").takeIf { it.isNotBlank() }
            val hAnsiFont = rFonts.getAttribute("w:hAnsi").takeIf { it.isNotBlank() }
            fontName = if (isRunRtl && csFont != null) csFont else (asciiFont ?: hAnsiFont ?: csFont)
        }

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
    }

    fun parseHyperlink(
        el: Element,
        relMap: Map<String, String>,
        fullTextBuilder: StringBuilder,
        spans: MutableList<InlineSpan>
    ) {
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
            val relId = el.getAttribute("r:id").ifBlank {
                el.getAttributeNS("http://schemas.openxmlformats.org/officeDocument/2006/relationships", "id")
            }
            val targetUrl = relMap[relId]?.takeIf { it.isNotBlank() }
                ?: el.getAttribute("w:anchor").takeIf { it.isNotBlank() }
                ?: "https://external.link"
            spans.add(
                InlineSpan(
                    start = runStart,
                    end = runEnd,
                    isUnderline = true,
                    textColorHex = "#185ABD",
                    linkUrl = targetUrl
                )
            )
        }
    }

    fun mapWordHighlightToHex(name: String): String {
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
