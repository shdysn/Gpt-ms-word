package com.example.editor.engine

import com.example.data.model.InlineSpan

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
    val underlineStyle: com.example.data.model.UnderlineStyle = com.example.data.model.UnderlineStyle.NONE,
    val underlineColorHex: String? = null,
    val strikethroughStyle: com.example.data.model.StrikethroughStyle = com.example.data.model.StrikethroughStyle.NONE,
    val shadowColorHex: String? = null,
    val isOutline: Boolean = false
)

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
            isUnderline = targetSpans.any { it.isUnderline || it.underlineStyle != com.example.data.model.UnderlineStyle.NONE },
            isStrikethrough = targetSpans.any { it.isStrikethrough || it.strikethroughStyle != com.example.data.model.StrikethroughStyle.NONE },
            isSuperscript = targetSpans.any { it.isSuperscript },
            isSubscript = targetSpans.any { it.isSubscript },
            fontSizeSp = targetSpans.lastOrNull { it.fontSizeSp != null }?.fontSizeSp,
            textColorHex = targetSpans.lastOrNull { it.textColorHex != null }?.textColorHex,
            highlightColorHex = targetSpans.lastOrNull { it.highlightColorHex != null }?.highlightColorHex,
            fontFamilyName = targetSpans.lastOrNull { it.fontFamilyName != null }?.fontFamilyName,
            linkUrl = targetSpans.lastOrNull { it.linkUrl != null }?.linkUrl,
            letterSpacingSp = targetSpans.lastOrNull { it.letterSpacingSp != null }?.letterSpacingSp,
            underlineStyle = targetSpans.lastOrNull { it.underlineStyle != com.example.data.model.UnderlineStyle.NONE }?.underlineStyle ?: com.example.data.model.UnderlineStyle.NONE,
            underlineColorHex = targetSpans.lastOrNull { it.underlineColorHex != null }?.underlineColorHex,
            strikethroughStyle = targetSpans.lastOrNull { it.strikethroughStyle != com.example.data.model.StrikethroughStyle.NONE }?.strikethroughStyle ?: com.example.data.model.StrikethroughStyle.NONE,
            shadowColorHex = targetSpans.lastOrNull { it.shadowColorHex != null }?.shadowColorHex,
            isOutline = targetSpans.any { it.isOutline }
        )
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
    private fun sliceSpansAtBoundaries(spans: List<InlineSpan>, s: Int, e: Int): List<InlineSpan> {
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
    private fun normalizeAndMerge(spans: List<InlineSpan>): List<InlineSpan> {
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
                current = next
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

    /**
     * Shifts and updates span boundaries when the underlying string content is modified.
     * Preserves formatting and applies pending formatting to freshly typed characters.
     */
    fun shiftSpansOnTextChange(
        spans: List<InlineSpan>,
        editOffset: Int,
        deletedLength: Int,
        insertedLength: Int,
        pendingFormatting: ActiveFormatting? = null
    ): List<InlineSpan> {
        val result = mutableListOf<InlineSpan>()
        val delta = insertedLength - deletedLength
        val editEnd = editOffset + deletedLength

        for (span in spans) {
            if (span.end <= editOffset) {
                // Strictly before edit: completely unchanged
                result.add(span)
            } else if (span.start >= editEnd) {
                // Strictly after edit: shift offsets by delta
                result.add(span.copy(start = span.start + delta, end = span.end + delta))
            } else {
                // Overlaps edit range
                val newStart = when {
                    span.start <= editOffset -> span.start
                    span.start < editEnd -> editOffset
                    else -> span.start + delta
                }

                val newEnd = when {
                    span.end <= editEnd -> editOffset + insertedLength
                    else -> span.end + delta
                }

                if (newStart < newEnd) {
                    result.add(span.copy(start = newStart, end = newEnd))
                }
            }
        }

        // If text was inserted and there was explicit pending formatting, add a span for the inserted text
        if (insertedLength > 0 && pendingFormatting != null && hasAnyFormatting(pendingFormatting)) {
            result.add(
                InlineSpan(
                    start = editOffset,
                    end = editOffset + insertedLength,
                    isBold = pendingFormatting.isBold,
                    isItalic = pendingFormatting.isItalic,
                    isUnderline = pendingFormatting.isUnderline,
                    isStrikethrough = pendingFormatting.isStrikethrough,
                    isSuperscript = pendingFormatting.isSuperscript,
                    isSubscript = pendingFormatting.isSubscript,
                    fontSizeSp = pendingFormatting.fontSizeSp,
                    textColorHex = pendingFormatting.textColorHex,
                    highlightColorHex = pendingFormatting.highlightColorHex,
                    fontFamilyName = pendingFormatting.fontFamilyName,
                    linkUrl = pendingFormatting.linkUrl
                )
            )
        }

        return normalizeAndMerge(result)
    }

    /**
     * Performs find and replace across text while accurately shifting all formatting
     * spans (Bold, Italic, Color, Hyperlinks) so styling remains on the correct characters.
     */
    fun replaceTextAndShiftSpans(
        text: String,
        spans: List<InlineSpan>,
        find: String,
        replace: String,
        ignoreCase: Boolean = true
    ): Pair<String, List<InlineSpan>> {
        if (find.isEmpty() || !text.contains(find, ignoreCase = ignoreCase)) {
            return Pair(text, spans)
        }

        val matchIndices = mutableListOf<Int>()
        var searchIdx = 0
        while (searchIdx <= text.length - find.length) {
            val idx = text.indexOf(find, searchIdx, ignoreCase = ignoreCase)
            if (idx == -1) break
            matchIndices.add(idx)
            searchIdx = idx + find.length
        }

        if (matchIndices.isEmpty()) return Pair(text, spans)

        var currentText = text
        var currentSpans = spans

        // Process replacements from right-to-left so earlier offsets are not mutated
        for (i in matchIndices.indices.reversed()) {
            val matchStart = matchIndices[i]
            val deletedLen = find.length
            val insertedLen = replace.length

            currentSpans = shiftSpansOnTextChange(
                spans = currentSpans,
                editOffset = matchStart,
                deletedLength = deletedLen,
                insertedLength = insertedLen,
                pendingFormatting = null
            )

            currentText = currentText.substring(0, matchStart) + replace + currentText.substring(matchStart + deletedLen)
        }

        return Pair(currentText, currentSpans)
    }

    private fun hasAnyFormatting(f: ActiveFormatting): Boolean {
        return f.isBold || f.isItalic || f.isUnderline || f.isStrikethrough ||
            f.isSuperscript || f.isSubscript || f.fontSizeSp != null ||
            f.textColorHex != null || f.highlightColorHex != null ||
            f.fontFamilyName != null || f.linkUrl != null
    }
}
