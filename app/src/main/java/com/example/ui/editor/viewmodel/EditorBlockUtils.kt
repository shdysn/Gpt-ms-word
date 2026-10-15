package com.example.ui.editor.viewmodel

import com.example.data.model.BlockElement
import com.example.data.model.ParagraphBlock
import java.util.UUID

object EditorBlockUtils {

    fun normalizeParagraphBlocks(blocks: List<BlockElement>): List<BlockElement> {
        val result = mutableListOf<BlockElement>()
        for (block in blocks) {
            if (block is ParagraphBlock && block.text.contains("\n")) {
                val lines = block.text.split("\n")
                var offset = 0
                lines.forEachIndexed { idx, line ->
                    val lineStart = offset
                    val lineEnd = offset + line.length
                    val lineSpans = block.spans.mapNotNull { span ->
                        val s = (span.start - lineStart).coerceIn(0, line.length)
                        val e = (span.end - lineStart).coerceIn(0, line.length)
                        if (s < e && span.start < lineEnd && span.end > lineStart) span.copy(start = s, end = e) else null
                    }
                    result.add(
                        block.copy(
                            id = if (idx == 0) block.id else UUID.randomUUID().toString(),
                            text = line,
                            spans = lineSpans
                        )
                    )
                    offset = lineEnd + 1
                }
            } else {
                result.add(block)
            }
        }
        return if (result.isEmpty()) listOf(ParagraphBlock()) else result
    }

    fun findActiveParagraphBlockIndex(blocks: List<BlockElement>, offset: Int): Int {
        var accumulated = 0
        var lastParagraphIndex = 0
        blocks.forEachIndexed { index, block ->
            if (block is ParagraphBlock) {
                lastParagraphIndex = index
                val len = block.text.length
                if (offset <= accumulated + len) return index
                accumulated += len + 1
            }
        }
        return lastParagraphIndex
    }

    fun findParagraphBlockIndicesForRange(blocks: List<BlockElement>, startOffset: Int, endOffset: Int): List<Int> {
        val min = minOf(startOffset, endOffset)
        val max = maxOf(startOffset, endOffset)
        if (min == max) return listOf(findActiveParagraphBlockIndex(blocks, min))
        val result = mutableListOf<Int>()
        var accumulated = 0
        blocks.forEachIndexed { index, block ->
            if (block is ParagraphBlock) {
                val blockStart = accumulated
                val blockEnd = accumulated + block.text.length
                if (blockStart <= max && (blockEnd >= min || (blockStart == blockEnd && blockStart == min))) {
                    result.add(index)
                }
                accumulated = blockEnd + 1
            }
        }
        return if (result.isEmpty()) listOf(findActiveParagraphBlockIndex(blocks, min)) else result
    }

    fun createParagraphsFromOcrText(rawText: String, isRtl: Boolean): List<ParagraphBlock> {
        if (rawText.isBlank()) return emptyList()
        val lines = rawText.split("\n\n", "\n")
        return lines.mapNotNull { line ->
            val trimmed = line.trim()
            if (trimmed.isEmpty()) null
            else {
                val rtl = isRtl || com.example.core.typography.BidiUtils.detectParagraphIsRtl(trimmed)
                ParagraphBlock(
                    text = trimmed,
                    isRtl = rtl,
                    alignment = if (rtl) com.example.data.model.TextAlignment.END else com.example.data.model.TextAlignment.START
                )
            }
        }
    }
}
