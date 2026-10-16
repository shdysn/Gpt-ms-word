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
import com.example.core.typography.AdaptiveFontManager
import com.example.core.typography.BidiUtils
import com.example.core.typography.DocumentFontManager
import com.example.data.model.InlineSpan
import com.example.data.model.ParagraphBlock
import com.example.data.model.RichDocument
import com.example.data.model.TextAlignment

/**
 * Architectural bridge between Jetpack Compose UI
 * and the low-level Document Word Processing Engine.
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
        zoomScale: Float = 1f,
        containerWidthDp: Float = 390f
    ): AnnotatedString {
        return buildAnnotatedString {
            append(inputText)

            for (span in spans) {
                val safeStart = span.start.coerceIn(0, inputText.length)
                val safeEnd = span.end.coerceIn(safeStart, inputText.length)
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
                    span.fontSizeSp != null -> (AdaptiveFontManager.getAdaptiveSpanFontSizeSp(span.fontSizeSp, isRtl, containerWidthDp) * zoomScale).sp
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

            // Real-time typo underline squiggles
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

    val COMMON_TYPOS: Map<String, String> get() = SpellCheckEngine.COMMON_TYPOS

    fun findTypos(text: String): List<Triple<Int, Int, String>> = SpellCheckEngine.findTypos(text)

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

        val isRtl = paragraph.isRtl
        val effectiveLineHeight = if (isRtl) {
            (28f * paragraph.lineSpacingMultiplier * zoomScale).sp
        } else {
            (18f * paragraph.lineSpacingMultiplier * zoomScale).sp
        }

        return ParagraphStyle(
            textAlign = when (paragraph.alignment) {
                TextAlignment.START -> TextAlign.Start
                TextAlignment.CENTER -> TextAlign.Center
                TextAlignment.END -> TextAlign.End
                TextAlignment.JUSTIFY -> TextAlign.Justify
            },
            textDirection = if (isRtl) TextDirection.Rtl else TextDirection.Ltr,
            lineHeight = effectiveLineHeight,
            textIndent = indent,
            platformStyle = androidx.compose.ui.text.PlatformParagraphStyle(includeFontPadding = true),
            lineHeightStyle = androidx.compose.ui.text.style.LineHeightStyle(
                alignment = androidx.compose.ui.text.style.LineHeightStyle.Alignment.Center,
                trim = androidx.compose.ui.text.style.LineHeightStyle.Trim.None
            )
        )
    }

    /**
     * Converts a domain ParagraphBlock model into a Jetpack Compose AnnotatedString,
     * applying all inline styling spans.
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
        return BidiUtils.detectParagraphIsRtl(text)
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

    fun getParagraphRanges(text: String) = ContinuousAnnotatedTextBuilder.getParagraphRanges(text)

    fun buildAnnotatedTextWithParagraphBlocks(
        text: String,
        paragraphs: List<ParagraphBlock>,
        zoomScale: Float = 1f,
        containerWidthDp: Float = 390f
    ): AnnotatedString {
        return ContinuousAnnotatedTextBuilder.buildAnnotatedTextWithParagraphBlocks(text, paragraphs, zoomScale, containerWidthDp)
    }

    fun parseColorSafe(hex: String): androidx.compose.ui.graphics.Color? {
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
