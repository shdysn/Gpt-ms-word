package com.example.editor.engine

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.ParagraphStyle
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.sp
import com.example.core.typography.BidiUtils
import com.example.core.typography.DocumentFontManager
import com.example.data.model.HeadingStyle
import com.example.data.model.ParagraphBlock
import com.example.data.model.TextAlignment

data class ParagraphRange(val lineIndex: Int, val start: Int, val end: Int, val text: String)

/**
 * Builds Jetpack Compose AnnotatedStrings from continuous document text with paragraph-specific
 * styles, alignments, directions, and inline spans across multi-paragraph spans.
 */
object ContinuousAnnotatedTextBuilder {

    fun getParagraphRanges(text: String): List<ParagraphRange> {
        val ranges = mutableListOf<ParagraphRange>()
        var start = 0
        var lineIndex = 0
        while (start <= text.length) {
            val newlineIndex = text.indexOf('\n', start)
            if (newlineIndex == -1) {
                val end = text.length
                ranges.add(ParagraphRange(lineIndex, start, end, text.substring(start, end)))
                break
            } else {
                ranges.add(ParagraphRange(lineIndex, start, newlineIndex, text.substring(start, newlineIndex)))
                start = newlineIndex + 1
                lineIndex++
            }
        }
        return ranges
    }

    fun buildAnnotatedTextWithParagraphBlocks(
        text: String,
        paragraphs: List<ParagraphBlock>,
        zoomScale: Float = 1f
    ): AnnotatedString {
        val ranges = getParagraphRanges(text)
        return buildAnnotatedString {
            append(text)

            for (range in ranges) {
                val block = paragraphs.getOrNull(range.lineIndex) ?: paragraphs.lastOrNull() ?: ParagraphBlock()
                val isRtl = block.isRtl || BidiUtils.detectParagraphIsRtl(range.text)

                val align = when (block.alignment) {
                    TextAlignment.CENTER -> TextAlign.Center
                    TextAlignment.END -> TextAlign.End
                    TextAlignment.JUSTIFY -> TextAlign.Justify
                    TextAlignment.START -> TextAlign.Start
                }

                val textDir = if (isRtl) TextDirection.Rtl else TextDirection.Ltr

                val lineSpacingMult = if (isRtl) block.lineSpacingMultiplier.coerceAtLeast(1.4f) else block.lineSpacingMultiplier
                val unscaledFontSize = when (block.headingStyle) {
                    HeadingStyle.TITLE -> 26f
                    HeadingStyle.SUBTITLE -> 18f
                    HeadingStyle.HEADING_1 -> 22f
                    HeadingStyle.HEADING_2 -> 18f
                    HeadingStyle.HEADING_3 -> 16f
                    HeadingStyle.QUOTE -> 15f
                    HeadingStyle.NORMAL -> 15f
                }
                val baseFontSize = (unscaledFontSize * zoomScale).sp

                // Add ParagraphStyle for this specific paragraph range
                addStyle(
                    style = ParagraphStyle(
                        textAlign = align,
                        textDirection = textDir,
                        lineHeight = baseFontSize * lineSpacingMult
                    ),
                    start = range.start,
                    end = range.end
                )

                // Heading base span style (e.g. bold for Title/Headings)
                if (block.headingStyle != HeadingStyle.NORMAL && range.start < range.end) {
                    val headingSpan = SpanStyle(
                        fontWeight = if (block.headingStyle.isBold) FontWeight.Bold else null,
                        fontStyle = if (block.headingStyle == HeadingStyle.QUOTE) FontStyle.Italic else null,
                        fontSize = baseFontSize
                    )
                    addStyle(headingSpan, range.start, range.end)
                }

                // Add inline spans mapped to this line's range
                for (span in block.spans) {
                    val safeStart = (range.start + span.start).coerceIn(range.start, range.end)
                    val safeEnd = (range.start + span.end).coerceIn(safeStart, range.end)
                    if (safeStart >= safeEnd) continue

                    val fontFamily = span.fontFamilyName?.let {
                        DocumentFontManager.getFontFamily(it, isRtl)
                    }

                    val baselineShift = when {
                        span.isSuperscript -> androidx.compose.ui.text.style.BaselineShift.Superscript
                        span.isSubscript -> androidx.compose.ui.text.style.BaselineShift.Subscript
                        else -> null
                    }

                    val computedFontSize = when {
                        span.fontSizeSp != null -> (span.fontSizeSp * zoomScale).sp
                        span.isSuperscript || span.isSubscript -> (10f * zoomScale).sp
                        else -> androidx.compose.ui.unit.TextUnit.Unspecified
                    }

                    val isLink = span.linkUrl != null
                    val decoration = when {
                        (span.isUnderline || isLink) && span.isStrikethrough -> TextDecoration.combine(
                            listOf(TextDecoration.Underline, TextDecoration.LineThrough)
                        )
                        span.isUnderline || isLink -> TextDecoration.Underline
                        span.isStrikethrough -> TextDecoration.LineThrough
                        else -> null
                    }

                    val spanStyle = SpanStyle(
                        fontWeight = if (span.isBold) FontWeight.Bold else null,
                        fontStyle = if (span.isItalic) FontStyle.Italic else null,
                        textDecoration = decoration,
                        fontSize = computedFontSize,
                        fontFamily = fontFamily,
                        baselineShift = baselineShift,
                        color = span.textColorHex?.let { EditorEngineIntegration.parseColorSafe(it) } ?: if (isLink) androidx.compose.ui.graphics.Color(0xFF185ABD) else androidx.compose.ui.graphics.Color.Unspecified,
                        background = span.highlightColorHex?.let { EditorEngineIntegration.parseColorSafe(it) } ?: androidx.compose.ui.graphics.Color.Transparent
                    )

                    addStyle(spanStyle, safeStart, safeEnd)
                    if (span.linkUrl != null) {
                        addStringAnnotation(tag = "URL", annotation = span.linkUrl, start = safeStart, end = safeEnd)
                    }
                }
            }

            // Real-time typo underline squiggles across continuous document text (Phase 4)
            val typos = SpellCheckEngine.findTypos(text)
            for ((start, end, suggestion) in typos) {
                val safeStart = start.coerceIn(0, text.length)
                val safeEnd = end.coerceIn(safeStart, text.length)
                if (safeStart < safeEnd) {
                    addStyle(
                        SpanStyle(
                            textDecoration = TextDecoration.Underline,
                            color = androidx.compose.ui.graphics.Color(0xFFDC2626)
                        ),
                        safeStart,
                        safeEnd
                    )
                    addStringAnnotation("SPELLCHECK", suggestion, safeStart, safeEnd)
                }
            }
        }
    }
}
