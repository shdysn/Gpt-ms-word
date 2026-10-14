package com.example.editor.engine

import com.example.data.model.InlineSpan

/**
 * Handles span shifting, expansion, and range mutations upon text insertion,
 * deletion, and find & replace operations.
 */
object SpanShiftOperations {

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
        if (insertedLength > 0 && pendingFormatting != null && pendingFormatting.hasAnyFormatting()) {
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

        return SpanOperations.normalizeAndMerge(result)
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
}
