package com.example.editor.engine

import com.example.data.model.InlineSpan

/**
 * Mathematical interval algebra for Rich Text inline span ranges.
 * Handles boundary splitting, range mutations, continuous typing span expansion,
 * and adjacent span normalization.
 */
object SpanOperations {

    /**
     * Inspects active formatting at a specific cursor offset or across a selection range.
     */
    fun queryFormatting(spans: List<InlineSpan>, start: Int, end: Int): ActiveFormatting {
        return ActiveFormatting.queryFormatting(spans, start, end)
    }

    /**
     * Clears all formatting from spans in target range [start, end].
     * If start == end, clears all spans in the entire paragraph.
     */
    fun clearFormatting(spans: List<InlineSpan>, start: Int, end: Int, textLength: Int): List<InlineSpan> {
        val s = start.coerceIn(0, textLength)
        val e = end.coerceIn(s, textLength)
        if (s == e) {
            // No selection -> clear entire paragraph
            return emptyList()
        }
        val sliced = sliceSpansAtBoundaries(spans, s, e)
        return sliced.filter { it.end <= s || it.start >= e }
    }

    /**
     * Applies a style mutation to a target range [rangeStart, rangeEnd].
     * If the style is already universally active over the entire range, it is toggled off;
     * otherwise, it is applied over the range, filling any gaps.
     */
    fun applyOrToggleStyle(
        currentSpans: List<InlineSpan>,
        rangeStart: Int,
        rangeEnd: Int,
        textLength: Int,
        mutator: (InlineSpan, Boolean) -> InlineSpan
    ): List<InlineSpan> {
        val s = rangeStart.coerceIn(0, textLength)
        val e = rangeEnd.coerceIn(s, textLength)
        if (s >= e) return currentSpans

        val overlapping = currentSpans.filter { it.start < e && it.end > s }
        var coveredLength = 0
        val allActive = overlapping.isNotEmpty() && overlapping.all { span ->
            val isAlreadyActive = (mutator(span, true) == span)
            if (isAlreadyActive) {
                val overlapStart = maxOf(s, span.start)
                val overlapEnd = minOf(e, span.end)
                coveredLength += maxOf(0, overlapEnd - overlapStart)
            }
            isAlreadyActive
        } && coveredLength >= (e - s)

        val shouldEnable = !allActive

        val slicedSpans = sliceSpansAtBoundaries(currentSpans, s, e)
        val outsideRange = slicedSpans.filter { it.end <= s || it.start >= e }
        val insideRange = slicedSpans.filter { it.start >= s && it.end <= e }.sortedBy { it.start }

        val result = mutableListOf<InlineSpan>()
        result.addAll(outsideRange)

        var cursor = s
        for (span in insideRange) {
            if (span.start > cursor && shouldEnable) {
                result.add(mutator(InlineSpan(start = cursor, end = span.start), true))
            }
            result.add(mutator(span, shouldEnable))
            cursor = maxOf(cursor, span.end)
        }

        if (cursor < e && shouldEnable) {
            result.add(mutator(InlineSpan(start = cursor, end = e), true))
        }

        return normalizeAndMerge(result)
    }

    /**
     * Directly applies a specific formatting attribute (e.g. text color, highlight, font family, font size)
     * over the target range [rangeStart, rangeEnd], preserving existing styles and filling any gaps.
     */
    fun applyExplicitStyle(
        currentSpans: List<InlineSpan>,
        rangeStart: Int,
        rangeEnd: Int,
        textLength: Int,
        mutator: (InlineSpan) -> InlineSpan
    ): List<InlineSpan> {
        val s = rangeStart.coerceIn(0, textLength)
        val e = rangeEnd.coerceIn(s, textLength)
        if (s >= e) return currentSpans

        val slicedSpans = sliceSpansAtBoundaries(currentSpans, s, e)
        val outsideRange = slicedSpans.filter { it.end <= s || it.start >= e }
        val insideRange = slicedSpans.filter { it.start >= s && it.end <= e }.sortedBy { it.start }

        val result = mutableListOf<InlineSpan>()
        result.addAll(outsideRange)

        var cursor = s
        for (span in insideRange) {
            if (span.start > cursor) {
                result.add(mutator(InlineSpan(start = cursor, end = span.start)))
            }
            result.add(mutator(span))
            cursor = maxOf(cursor, span.end)
        }

        if (cursor < e) {
            result.add(mutator(InlineSpan(start = cursor, end = e)))
        }

        return normalizeAndMerge(result)
    }

    /**
     * Splits spans that cross the boundary points 's' or 'e'.
     */
    fun sliceSpansAtBoundaries(spans: List<InlineSpan>, s: Int, e: Int): List<InlineSpan> {
        val sliced = mutableListOf<InlineSpan>()
        for (span in spans) {
            if (span.start < s && span.end > s) {
                // Crosses 's'
                sliced.add(span.copy(end = s))
                if (span.end > e) {
                    // Also crosses 'e'
                    sliced.add(span.copy(start = s, end = e))
                    sliced.add(span.copy(start = e))
                } else {
                    sliced.add(span.copy(start = s))
                }
            } else if (span.start < e && span.end > e) {
                // Crosses 'e'
                sliced.add(span.copy(end = e))
                sliced.add(span.copy(start = e))
            } else {
                sliced.add(span)
            }
        }
        return sliced.filter { it.start < it.end }
    }

    /**
     * Merges adjacent spans with identical formatting attributes and removes unformatted/empty spans.
     */
    fun normalizeAndMerge(spans: List<InlineSpan>): List<InlineSpan> {
        val valid = spans.filter { it.start < it.end && hasAnyFormatting(it) }.sortedBy { it.start }
        if (valid.isEmpty()) return emptyList()

        val merged = mutableListOf<InlineSpan>()
        var current = valid[0]

        for (i in 1 until valid.size) {
            val next = valid[i]
            if (current.end == next.start && hasIdenticalFormatting(current, next)) {
                current = current.copy(end = next.end)
            } else {
                merged.add(current)
            }
        }
        merged.add(current)
        return merged
    }

    fun hasAnyFormatting(span: InlineSpan): Boolean {
        return span.isBold || span.isItalic || span.isUnderline || span.isStrikethrough ||
            span.isSuperscript || span.isSubscript || span.fontSizeSp != null ||
            span.textColorHex != null || span.highlightColorHex != null ||
            span.fontFamilyName != null || span.linkUrl != null
    }

    private fun hasIdenticalFormatting(a: InlineSpan, b: InlineSpan): Boolean {
        return a.isBold == b.isBold &&
            a.isItalic == b.isItalic &&
            a.isUnderline == b.isUnderline &&
            a.isStrikethrough == b.isStrikethrough &&
            a.isSuperscript == b.isSuperscript &&
            a.isSubscript == b.isSubscript &&
            a.fontSizeSp == b.fontSizeSp &&
            a.textColorHex == b.textColorHex &&
            a.highlightColorHex == b.highlightColorHex &&
            a.fontFamilyName == b.fontFamilyName &&
            a.linkUrl == b.linkUrl
    }

    fun shiftSpansOnTextChange(
        spans: List<InlineSpan>,
        editOffset: Int,
        deletedLength: Int,
        insertedLength: Int,
        pendingFormatting: ActiveFormatting? = null
    ): List<InlineSpan> {
        return SpanShiftOperations.shiftSpansOnTextChange(
            spans, editOffset, deletedLength, insertedLength, pendingFormatting
        )
    }

    fun replaceTextAndShiftSpans(
        text: String,
        spans: List<InlineSpan>,
        find: String,
        replace: String,
        ignoreCase: Boolean = true
    ): Pair<String, List<InlineSpan>> {
        return SpanShiftOperations.replaceTextAndShiftSpans(
            text, spans, find, replace, ignoreCase
        )
    }
}
