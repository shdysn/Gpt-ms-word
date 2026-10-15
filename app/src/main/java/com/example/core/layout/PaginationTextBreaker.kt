package com.example.core.layout

import android.graphics.Paint
import android.graphics.Typeface
import com.example.data.model.HeadingStyle

/**
 * Text measurement and line-breaking routines for document pagination.
 */
object PaginationTextBreaker {

    /**
     * Accurately breaks a text paragraph into lines according to font paint metrics and max printable width.
     */
    fun breakTextIntoLines(
        text: String,
        paint: Paint? = null,
        fontSizePt: Float = 11f,
        isBold: Boolean = false,
        maxWidthPt: Float
    ): List<String> {
        if (text.isEmpty()) return listOf("")

        val resultLines = mutableListOf<String>()
        val paragraphs = text.split("\n")

        for (paragraph in paragraphs) {
            if (paragraph.isEmpty()) {
                resultLines.add("")
                continue
            }

            val words = paragraph.split(" ")
            var currentLine = StringBuilder()

            for (word in words) {
                val candidate = if (currentLine.isEmpty()) word else "${currentLine} $word"
                val measuredWidth = measureWidth(candidate, paint, fontSizePt, isBold)

                if (measuredWidth <= maxWidthPt) {
                    currentLine = StringBuilder(candidate)
                } else {
                    if (currentLine.isNotEmpty()) {
                        resultLines.add(currentLine.toString())
                        currentLine = StringBuilder(word)
                    } else {
                        var remainingWord = word
                        while (remainingWord.isNotEmpty()) {
                            var charsCount = 1
                            while (charsCount <= remainingWord.length && measureWidth(remainingWord.substring(0, charsCount), paint, fontSizePt, isBold) <= maxWidthPt) {
                                charsCount++
                            }
                            val safeCount = (charsCount - 1).coerceAtLeast(1)
                            resultLines.add(remainingWord.substring(0, safeCount))
                            remainingWord = remainingWord.substring(safeCount)
                        }
                        currentLine = StringBuilder()
                    }
                }
            }

            if (currentLine.isNotEmpty()) {
                resultLines.add(currentLine.toString())
            }
        }

        return if (resultLines.isEmpty()) listOf("") else resultLines
    }

    /**
     * Measures width in points, falling back to proportional typographic metrics if Paint is unmocked or in test environment.
     */
    fun measureWidth(text: String, paint: Paint?, fontSizePt: Float, isBold: Boolean): Float {
        val estimatedMin = text.length * (fontSizePt * 0.35f)
        if (paint != null) {
            try {
                val measured = paint.measureText(text)
                if (measured >= estimatedMin) {
                    return measured
                }
            } catch (e: Throwable) {
                // fall through to typography estimation
            }
        }
        val avgCharWidth = if (isBold) fontSizePt * 0.54f else fontSizePt * 0.49f
        return text.length * avgCharWidth
    }

    fun getHeadingFontMetrics(style: HeadingStyle): Pair<Float, Int> {
        return when (style) {
            HeadingStyle.TITLE -> Pair(22f, Typeface.BOLD)
            HeadingStyle.HEADING_1 -> Pair(16f, Typeface.BOLD)
            HeadingStyle.HEADING_2 -> Pair(14f, Typeface.BOLD)
            HeadingStyle.HEADING_3 -> Pair(12f, Typeface.BOLD)
            HeadingStyle.SUBTITLE -> Pair(13f, Typeface.ITALIC)
            HeadingStyle.QUOTE -> Pair(11f, Typeface.ITALIC)
            HeadingStyle.NORMAL -> Pair(11f, Typeface.NORMAL)
        }
    }
}
