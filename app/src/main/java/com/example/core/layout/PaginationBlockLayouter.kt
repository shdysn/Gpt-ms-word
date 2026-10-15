package com.example.core.layout

import com.example.data.model.ParagraphBlock
import com.example.data.model.TableBlock

/**
 * Handles element slicing across page boundaries (e.g. multi-page paragraphs and table row splitting).
 */
object PaginationBlockLayouter {

    /**
     * Slices an overflowing paragraph across multiple physical pages.
     */
    fun paginateParagraphAcrossPages(
        blockIndex: Int,
        block: ParagraphBlock,
        lines: List<String>,
        lineHeight: Float,
        paragraphSpacing: Float,
        printableHeightPt: Float,
        onAddItem: (PageItem) -> Unit,
        onPageFilled: () -> Unit,
        onHeightAdded: (Float) -> Unit,
        isInitialContinuation: Boolean = false,
        initialCharOffset: Int = 0
    ) {
        var lineIndex = 0
        var isContinuation = isInitialContinuation
        var runningCharOffset = initialCharOffset

        while (lineIndex < lines.size) {
            val linesThatFit = (printableHeightPt / lineHeight).toInt().coerceAtLeast(1)
            val endLine = (lineIndex + linesThatFit).coerceAtMost(lines.size)
            val chunkLines = lines.subList(lineIndex, endLine)
            val chunkText = chunkLines.joinToString("\n")
            val isLastChunk = endLine == lines.size
            val chunkHeight = (chunkLines.size * lineHeight) + (if (isLastChunk) paragraphSpacing else 0f)

            val chunkStartChar = runningCharOffset.coerceIn(0, block.text.length)
            val chunkCharLen = chunkLines.sumOf { it.length } + (chunkLines.size - 1).coerceAtLeast(0)
            val chunkEndChar = if (isLastChunk) {
                block.text.length
            } else {
                (chunkStartChar + chunkCharLen).coerceIn(chunkStartChar, block.text.length)
            }

            onAddItem(
                ParagraphPageItem(
                    originalBlockIndex = blockIndex,
                    originalBlock = block,
                    lineSliceText = chunkText,
                    startChar = chunkStartChar,
                    endChar = chunkEndChar,
                    isContinuation = isContinuation,
                    hasContinuation = !isLastChunk,
                    heightPt = chunkHeight
                )
            )
            onHeightAdded(chunkHeight)

            runningCharOffset = (chunkEndChar + 1).coerceAtMost(block.text.length)
            lineIndex = endLine
            isContinuation = true

            if (lineIndex < lines.size) {
                onPageFilled()
            }
        }
    }

    /**
     * Slices large tables across pages when exceeding available space.
     */
    fun sliceTableRows(
        blockIndex: Int,
        block: TableBlock,
        printableHeightPt: Float,
        currentHeight: Float,
        headerRowHeight: Float = 24f,
        dataRowHeight: Float = 20f,
        onAddItem: (TablePageItem) -> Unit,
        onCommitNewPage: () -> Unit
    ): Float {
        var runningHeight = currentHeight
        var startRow = 0
        while (startRow < block.rowCount) {
            val availableH = (printableHeightPt - runningHeight).coerceAtLeast(headerRowHeight)
            val rowsThatFit = if (startRow == 0) {
                ((availableH - headerRowHeight) / dataRowHeight).toInt().coerceAtLeast(1) + 1
            } else {
                (availableH / dataRowHeight).toInt().coerceAtLeast(1)
            }
            val endRow = (startRow + rowsThatFit).coerceAtMost(block.rowCount)
            val sliceRowCount = endRow - startRow
            val sliceHeight = if (startRow == 0) {
                headerRowHeight + ((sliceRowCount - 1).coerceAtLeast(0) * dataRowHeight)
            } else {
                sliceRowCount * dataRowHeight
            }

            onAddItem(
                TablePageItem(
                    originalBlockIndex = blockIndex,
                    originalBlock = block,
                    startRow = startRow,
                    endRow = endRow,
                    heightPt = sliceHeight
                )
            )
            runningHeight += sliceHeight
            startRow = endRow
            if (startRow < block.rowCount) {
                onCommitNewPage()
                runningHeight = 0f
            }
        }
        return runningHeight
    }
}
