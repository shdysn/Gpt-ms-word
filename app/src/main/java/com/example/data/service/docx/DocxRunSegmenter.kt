package com.example.data.service.docx

import com.example.data.model.InlineSpan

/**
 * Represents a uniform slice of text within a paragraph with homogenous formatting runs.
 */
data class FormattedRunSlice(
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

/**
 * Utilities to segment rich text spans into discrete OpenXML run slices.
 */
object DocxRunSegmenter {

    fun segmentParagraphRuns(text: String, spans: List<InlineSpan>): List<FormattedRunSlice> {
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

    fun mapColorToWordHighlight(hex: String): String {
        return when (hex.removePrefix("#").uppercase()) {
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
}
