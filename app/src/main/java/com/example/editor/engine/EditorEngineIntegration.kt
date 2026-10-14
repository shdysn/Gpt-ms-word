package com.example.editor.engine

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.ParagraphStyle
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.sp
import com.example.data.model.HeadingStyle
import com.example.data.model.InlineSpan
import com.example.data.model.ParagraphBlock
import com.example.data.model.RichDocument
import com.example.data.model.TextAlignment


/**
 * Architectural specification and bridge between Jetpack Compose UI
 * and the low-level Document Word Processing Engine.
 *
 * ARCHITECTURAL GAP ANALYSIS:
 * 1. Jetpack Compose [BasicTextField] / [TextField]:
 *    - Designed primarily for input of linear text strings.
 *    - Lacks native support for inline arbitrary blocks (tables, flowable images, line-breaks with orphan control).
 *    - Does not natively support multi-page canvas slicing or A4 physical page breaking.
 * 2. Word-Processing Engine Requirements:
 *    - Line-breaking algorithm (Knuth-Plass or greedy paragraph layout with hyphenation).
 *    - BiDi Text Layout (Urdu Nastaliq, Arabic, Hebrew right-to-left layout combined with Latin LTR numbers).
 *    - Physical Page Partitioning (A4 / Letter printable area height constraints, header/footer repeats).
 *    - Selection & Caret handles across heterogeneous block elements (paragraphs + tables + figures).
 *    - OpenXML / DOCX and PDF translation layers.
 *
 * This component provides the integration contract and converters to bridge Compose UI
 * with the underlying word processing engine.
 */
object EditorEngineIntegration {

    /**
     * Converts raw text and inline spans into a Jetpack Compose AnnotatedString,
     * guaranteeing exact length alignment with input text for VisualTransformation.
     */
    fun buildAnnotatedText(
        inputText: String,
        spans: List<InlineSpan>,
        isRtl: Boolean,
        zoomScale: Float = 1f
    ): AnnotatedString {
        return buildAnnotatedString {
            append(inputText)

            for (span in spans) {
                val safeStart = span.start.coerceIn(0, inputText.length)
                val safeEnd = span.end.coerceIn(safeStart, inputText.length)
                if (safeStart >= safeEnd) continue

                val fontFamily = span.fontFamilyName?.let {
                    com.example.core.typography.DocumentFontManager.getFontFamily(it, isRtl)
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
                val hasUnderline = span.isUnderline || isLink || span.underlineStyle != com.example.data.model.UnderlineStyle.NONE
                val hasStrikethrough = span.isStrikethrough || span.strikethroughStyle != com.example.data.model.StrikethroughStyle.NONE

                val decoration = when {
                    hasUnderline && hasStrikethrough -> TextDecoration.combine(
                        listOf(TextDecoration.Underline, TextDecoration.LineThrough)
                    )
                    hasUnderline -> TextDecoration.Underline
                    hasStrikethrough -> TextDecoration.LineThrough
                    else -> null
                }

                val letterSpacing = span.letterSpacingSp?.let { (it * zoomScale).sp } ?: androidx.compose.ui.unit.TextUnit.Unspecified

                val shadow = if (span.shadowColorHex != null || span.shadowDx != null || span.shadowDy != null) {
                    androidx.compose.ui.graphics.Shadow(
                        color = span.shadowColorHex?.let { parseColorSafe(it) } ?: androidx.compose.ui.graphics.Color(0x80000000),
                        offset = androidx.compose.ui.geometry.Offset((span.shadowDx ?: 2f) * zoomScale, (span.shadowDy ?: 2f) * zoomScale),
                        blurRadius = (span.shadowRadius ?: 3f) * zoomScale
                    )
                } else null

                val spanStyle = SpanStyle(
                    fontWeight = if (span.isBold) FontWeight.Bold else null,
                    fontStyle = if (span.isItalic) FontStyle.Italic else null,
                    textDecoration = decoration,
                    fontSize = computedFontSize,
                    fontFamily = fontFamily,
                    baselineShift = baselineShift,
                    letterSpacing = letterSpacing,
                    shadow = shadow,
                    color = span.textColorHex?.let { parseColorSafe(it) } ?: if (isLink) androidx.compose.ui.graphics.Color(0xFF185ABD) else androidx.compose.ui.graphics.Color.Unspecified,
                    background = span.highlightColorHex?.let { parseColorSafe(it) } ?: androidx.compose.ui.graphics.Color.Transparent
                )

                addStyle(spanStyle, safeStart, safeEnd)
                if (span.linkUrl != null) {
                    addStringAnnotation(tag = "URL", annotation = span.linkUrl, start = safeStart, end = safeEnd)
                }
            }

            // Real-time typo underline squiggles (Phase 4 requirement)
            val typos = findTypos(inputText)
            for ((start, end, suggestion) in typos) {
                val safeStart = start.coerceIn(0, inputText.length)
                val safeEnd = end.coerceIn(safeStart, inputText.length)
                if (safeStart < safeEnd) {
                    addStyle(
                        SpanStyle(
                            textDecoration = TextDecoration.Underline,
                            color = androidx.compose.ui.graphics.Color(0xFFDC2626) // Crimson Red typo indicator
                        ),
                        safeStart,
                        safeEnd
                    )
                    addStringAnnotation("SPELLCHECK", suggestion, safeStart, safeEnd)
                }
            }
        }
    }

    val COMMON_TYPOS: Map<String, String> = mapOf(
        "teh" to "the",
        "recieve" to "receive",
        "seperate" to "separate",
        "definately" to "definitely",
        "untill" to "until",
        "occured" to "occurred",
        "accomodate" to "accommodate",
        "wierd" to "weird",
        "goverment" to "government",
        "enviroment" to "environment",
        "tommorrow" to "tomorrow",
        "beleive" to "believe",
        "truely" to "truly",
        "writting" to "writing",
        "sucessful" to "successful",
        "begining" to "beginning",
        "neccessary" to "necessary",
        "embarass" to "embarrass",
        "existance" to "existence",
        "guarentee" to "guarantee",
        "happend" to "happened",
        "independant" to "independent",
        "relevent" to "relevant",
        "tendancy" to "tendency",
        "usefull" to "useful",
        "alot" to "a lot"
    )

    fun findTypos(text: String): List<Triple<Int, Int, String>> {
        if (text.isBlank()) return emptyList()
        val typos = mutableListOf<Triple<Int, Int, String>>()
        val wordRegex = Regex("\\b[a-zA-Z]+\\b")
        for (match in wordRegex.findAll(text)) {
            val word = match.value.lowercase(java.util.Locale.ROOT)
            val suggestion = COMMON_TYPOS[word]
            if (suggestion != null) {
                typos.add(Triple(match.range.first, match.range.last + 1, suggestion))
            }
        }
        return typos
    }

    /**
     * Builds paragraph layout style incorporating text alignment, direction, line spacing,
     * and First Line / Hanging indents.
     */
    fun buildParagraphStyle(paragraph: ParagraphBlock, zoomScale: Float = 1f): ParagraphStyle {
        val firstLineIndent = (paragraph.firstLineIndentPt * zoomScale).sp
        val restLineIndent = (paragraph.hangingIndentPt * zoomScale).sp
        val indent = if (paragraph.firstLineIndentPt != 0f || paragraph.hangingIndentPt != 0f) {
            androidx.compose.ui.text.style.TextIndent(firstLine = firstLineIndent, restLine = restLineIndent)
        } else null

        return ParagraphStyle(
            textAlign = when (paragraph.alignment) {
                TextAlignment.START -> TextAlign.Start
                TextAlignment.CENTER -> TextAlign.Center
                TextAlignment.END -> TextAlign.End
                TextAlignment.JUSTIFY -> TextAlign.Justify
            },
            textDirection = if (paragraph.isRtl) TextDirection.Rtl else TextDirection.Ltr,
            lineHeight = (18f * paragraph.lineSpacingMultiplier * zoomScale).sp,
            textIndent = indent
        )
    }

    /**
     * Converts a domain ParagraphBlock model into a Jetpack Compose AnnotatedString,
     * applying all inline styling spans (Bold, Italic, Underline, Colors, Spans).
     */
    fun buildAnnotatedParagraph(paragraph: ParagraphBlock, zoomScale: Float = 1f): AnnotatedString {
        return buildAnnotatedText(
            inputText = paragraph.text,
            spans = paragraph.spans,
            isRtl = paragraph.isRtl,
            zoomScale = zoomScale
        )
    }

    /**
     * Inspects text for Right-to-Left character presence (Urdu, Arabic, Persian, Hebrew).
     */
    fun detectScriptDirection(text: String): Boolean {
        return com.example.core.typography.BidiUtils.detectParagraphIsRtl(text)
    }

    /**
     * Calculates page pagination points for a given document on an A4 sheet.
     */
    fun calculatePageBreakIndices(document: RichDocument): List<Int> {
        val breakIndices = mutableListOf(0)
        var accumulatedHeightPt = 0f
        val pagePrintableHeight = document.layoutConfig.printableHeightPt

        document.blocks.forEachIndexed { index, block ->
            val blockHeight = when (block) {
                is ParagraphBlock -> {
                    val lines = (block.text.length / 80).coerceAtLeast(1)
                    lines * (block.headingStyle.defaultFontSizeSp * block.lineSpacingMultiplier + 6f)
                }
                is com.example.data.model.TableBlock -> block.rowCount * 24f
                is com.example.data.model.DividerBlock -> 16f
                is com.example.data.model.PageBreakBlock -> pagePrintableHeight
                else -> 40f
            }

            if (block is com.example.data.model.PageBreakBlock || accumulatedHeightPt + blockHeight > pagePrintableHeight) {
                if (index > 0) {
                    breakIndices.add(index)
                    accumulatedHeightPt = blockHeight
                }
            } else {
                accumulatedHeightPt += blockHeight
            }
        }
        return breakIndices
    }

    data class ParagraphRange(val lineIndex: Int, val start: Int, val end: Int, val text: String)

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

    /**
     * Converts continuous document text into a Jetpack Compose AnnotatedString with
     * paragraph-specific ParagraphStyle (alignment, text direction, line height)
     * and inline styles for each paragraph block delimited by newline characters.
     */
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
                val isRtl = block.isRtl || com.example.core.typography.BidiUtils.detectParagraphIsRtl(range.text)

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
                        com.example.core.typography.DocumentFontManager.getFontFamily(it, isRtl)
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
                        color = span.textColorHex?.let { parseColorSafe(it) } ?: if (isLink) androidx.compose.ui.graphics.Color(0xFF185ABD) else androidx.compose.ui.graphics.Color.Unspecified,
                        background = span.highlightColorHex?.let { parseColorSafe(it) } ?: androidx.compose.ui.graphics.Color.Transparent
                    )

                    addStyle(spanStyle, safeStart, safeEnd)
                    if (span.linkUrl != null) {
                        addStringAnnotation(tag = "URL", annotation = span.linkUrl, start = safeStart, end = safeEnd)
                    }
                }
            }
        }
    }

    private fun parseColorSafe(hex: String): androidx.compose.ui.graphics.Color? {
        return try {
            val clean = hex.removePrefix("#")
            val colorLong = clean.toLong(16)
            if (clean.length == 6) {
                androidx.compose.ui.graphics.Color(0xFF000000 or colorLong)
            } else {
                androidx.compose.ui.graphics.Color(colorLong)
            }
        } catch (e: Exception) {
            null
        }
    }
}
