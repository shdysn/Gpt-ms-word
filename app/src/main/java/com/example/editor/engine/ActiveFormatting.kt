package com.example.editor.engine

import com.example.data.model.InlineSpan
import com.example.data.model.StrikethroughStyle
import com.example.data.model.UnderlineStyle

/**
 * Snapshot of active character formatting attributes at cursor or across a selection.
 */
data class ActiveFormatting(
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
    val linkUrl: String? = null,
    val letterSpacingSp: Float? = null,
    val underlineStyle: UnderlineStyle = UnderlineStyle.NONE,
    val underlineColorHex: String? = null,
    val strikethroughStyle: StrikethroughStyle = StrikethroughStyle.NONE,
    val shadowColorHex: String? = null,
    val isOutline: Boolean = false
) {
    fun hasAnyFormatting(): Boolean {
        return isBold || isItalic || isUnderline || isStrikethrough ||
            isSuperscript || isSubscript || fontSizeSp != null ||
            textColorHex != null || highlightColorHex != null ||
            fontFamilyName != null || linkUrl != null
    }

    companion object {
        /**
         * Inspects active formatting at a specific cursor offset or across a selection range.
         */
        fun queryFormatting(spans: List<InlineSpan>, start: Int, end: Int): ActiveFormatting {
            val targetSpans = if (start == end) {
                // Cursor position: pick spans covering the character before or at the cursor
                spans.filter { it.start <= start && it.end >= start }
            } else {
                // Range selection: pick spans overlapping the selection
                spans.filter { it.start < end && it.end > start }
            }

            if (targetSpans.isEmpty()) return ActiveFormatting()

            return ActiveFormatting(
                isBold = targetSpans.any { it.isBold },
                isItalic = targetSpans.any { it.isItalic },
                isUnderline = targetSpans.any { it.isUnderline || it.underlineStyle != UnderlineStyle.NONE },
                isStrikethrough = targetSpans.any { it.isStrikethrough || it.strikethroughStyle != StrikethroughStyle.NONE },
                isSuperscript = targetSpans.any { it.isSuperscript },
                isSubscript = targetSpans.any { it.isSubscript },
                fontSizeSp = targetSpans.lastOrNull { it.fontSizeSp != null }?.fontSizeSp,
                textColorHex = targetSpans.lastOrNull { it.textColorHex != null }?.textColorHex,
                highlightColorHex = targetSpans.lastOrNull { it.highlightColorHex != null }?.highlightColorHex,
                fontFamilyName = targetSpans.lastOrNull { it.fontFamilyName != null }?.fontFamilyName,
                linkUrl = targetSpans.lastOrNull { it.linkUrl != null }?.linkUrl,
                letterSpacingSp = targetSpans.lastOrNull { it.letterSpacingSp != null }?.letterSpacingSp,
                underlineStyle = targetSpans.lastOrNull { it.underlineStyle != UnderlineStyle.NONE }?.underlineStyle ?: UnderlineStyle.NONE,
                underlineColorHex = targetSpans.lastOrNull { it.underlineColorHex != null }?.underlineColorHex,
                strikethroughStyle = targetSpans.lastOrNull { it.strikethroughStyle != StrikethroughStyle.NONE }?.strikethroughStyle ?: StrikethroughStyle.NONE,
                shadowColorHex = targetSpans.lastOrNull { it.shadowColorHex != null }?.shadowColorHex,
                isOutline = targetSpans.any { it.isOutline }
            )
        }
    }
}
