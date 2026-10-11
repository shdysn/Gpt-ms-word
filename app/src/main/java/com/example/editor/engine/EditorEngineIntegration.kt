package com.example.editor.engine

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.sp
import com.example.data.model.InlineSpan
import com.example.data.model.ParagraphBlock
import com.example.data.model.RichDocument

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
     * Converts a domain ParagraphBlock model into a Jetpack Compose AnnotatedString,
     * applying all inline styling spans (Bold, Italic, Underline, Colors, Spans).
     */
    fun buildAnnotatedParagraph(paragraph: ParagraphBlock): AnnotatedString {
        return buildAnnotatedString {
            append(paragraph.text)

            for (span in paragraph.spans) {
                val safeStart = span.start.coerceIn(0, paragraph.text.length)
                val safeEnd = span.end.coerceIn(safeStart, paragraph.text.length)
                if (safeStart >= safeEnd) continue

                val fontFamily = when (span.fontFamilyName) {
                    "Arial" -> androidx.compose.ui.text.font.FontFamily.SansSerif
                    "Times New Roman", "Georgia" -> androidx.compose.ui.text.font.FontFamily.Serif
                    "Courier New" -> androidx.compose.ui.text.font.FontFamily.Monospace
                    "Nastaliq" -> androidx.compose.ui.text.font.FontFamily.Cursive
                    else -> null
                }

                val baselineShift = when {
                    span.isSuperscript -> androidx.compose.ui.text.style.BaselineShift.Superscript
                    span.isSubscript -> androidx.compose.ui.text.style.BaselineShift.Subscript
                    else -> null
                }

                val computedFontSize = when {
                    span.fontSizeSp != null -> span.fontSizeSp.sp
                    span.isSuperscript || span.isSubscript -> 10.sp
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

    /**
     * Inspects text for Right-to-Left character presence (Urdu, Arabic, Persian, Hebrew).
     */
    fun detectScriptDirection(text: String): Boolean {
        var rtlCount = 0
        var ltrCount = 0
        for (char in text) {
            when (Character.getDirectionality(char)) {
                Character.DIRECTIONALITY_RIGHT_TO_LEFT,
                Character.DIRECTIONALITY_RIGHT_TO_LEFT_ARABIC -> rtlCount++
                Character.DIRECTIONALITY_LEFT_TO_RIGHT -> ltrCount++
            }
        }
        return rtlCount > ltrCount
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
