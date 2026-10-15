package com.example.core.layout

import android.graphics.Paint
import android.graphics.Typeface
import com.example.data.model.HeadingStyle
import com.example.data.model.ParagraphBlock

/**
 * Handles paragraph measurement, line breaking, and pagination insertion across pages.
 */
object PaginationParagraphLayouter {

    fun layoutParagraph(
        blockIndex: Int,
        block: ParagraphBlock,
        layout: PageLayoutConfig,
        printableWidthPt: Float,
        printableHeightPt: Float,
        paint: Paint?,
        currentHeight: Float,
        currentPage: MutableList<PageItem>,
        onCommitNewPage: () -> Unit
    ): Float {
        var runningHeight = currentHeight
        val (fontSizePt, typefaceStyle) = PaginationTextBreaker.getHeadingFontMetrics(block.headingStyle)
        if (paint != null) {
            try {
                paint.textSize = fontSizePt
                paint.typeface = Typeface.create(Typeface.DEFAULT, typefaceStyle)
            } catch (_: Throwable) {}
        }

        val lineHeight = fontSizePt * block.lineSpacingMultiplier + 2f
        val paragraphSpacing = if (block.spaceAfterPt > 0f) {
            block.spaceAfterPt
        } else {
            when (block.headingStyle) {
                HeadingStyle.TITLE -> 10f
                HeadingStyle.HEADING_1 -> 8f
                HeadingStyle.HEADING_2 -> 6f
                else -> 5f
            }
        }
        val spaceBefore = block.spaceBeforePt
        val indentPt = if (block.leftIndentPt > 0f) block.leftIndentPt else (block.indentationLevel * 18f)
        val colCount = layout.columnCount.coerceIn(1, 2)
        val baseWidth = if (colCount == 2) (printableWidthPt - 18f) / 2f else printableWidthPt
        val effectivePrintableWidth = (baseWidth - indentPt).coerceAtLeast(72f)

        val isBold = block.headingStyle == HeadingStyle.TITLE || block.headingStyle == HeadingStyle.HEADING_1 || block.headingStyle == HeadingStyle.HEADING_2
        val lines = PaginationTextBreaker.breakTextIntoLines(block.text, paint, fontSizePt, isBold, effectivePrintableWidth)
        val totalParagraphHeight = spaceBefore + (lines.size * lineHeight) + paragraphSpacing

        if (runningHeight + totalParagraphHeight <= printableHeightPt) {
            currentPage.add(
                ParagraphPageItem(
                    originalBlockIndex = blockIndex,
                    originalBlock = block,
                    lineSliceText = block.text,
                    startChar = 0,
                    endChar = block.text.length,
                    isContinuation = false,
                    hasContinuation = false,
                    heightPt = totalParagraphHeight
                )
            )
            runningHeight += totalParagraphHeight
        } else {
            val availableSpace = printableHeightPt - runningHeight
            val linesThatFit = (availableSpace / lineHeight).toInt()

            if (currentPage.isNotEmpty() && linesThatFit < 2) {
                onCommitNewPage()
                runningHeight = 0f
                PaginationBlockLayouter.paginateParagraphAcrossPages(
                    blockIndex = blockIndex,
                    block = block,
                    lines = lines,
                    lineHeight = lineHeight,
                    paragraphSpacing = paragraphSpacing,
                    printableHeightPt = printableHeightPt,
                    onAddItem = { currentPage.add(it) },
                    onPageFilled = { onCommitNewPage(); runningHeight = 0f },
                    onHeightAdded = { runningHeight += it },
                    initialCharOffset = 0
                )
            } else if (linesThatFit >= 2) {
                val firstLines = lines.subList(0, linesThatFit)
                val firstText = firstLines.joinToString(" ")
                val firstEndChar = firstText.length.coerceAtMost(block.text.length)
                val firstHeight = firstLines.size * lineHeight

                currentPage.add(
                    ParagraphPageItem(
                        originalBlockIndex = blockIndex,
                        originalBlock = block,
                        lineSliceText = firstText,
                        startChar = 0,
                        endChar = firstEndChar,
                        isContinuation = false,
                        hasContinuation = true,
                        heightPt = firstHeight
                    )
                )
                onCommitNewPage()
                runningHeight = 0f

                val remainingLines = lines.subList(linesThatFit, lines.size)
                val nextCharOffset = (firstEndChar + 1).coerceAtMost(block.text.length)
                PaginationBlockLayouter.paginateParagraphAcrossPages(
                    blockIndex = blockIndex,
                    block = block,
                    lines = remainingLines,
                    lineHeight = lineHeight,
                    paragraphSpacing = paragraphSpacing,
                    printableHeightPt = printableHeightPt,
                    onAddItem = { currentPage.add(it) },
                    onPageFilled = { onCommitNewPage(); runningHeight = 0f },
                    onHeightAdded = { runningHeight += it },
                    isInitialContinuation = true,
                    initialCharOffset = nextCharOffset
                )
            } else {
                PaginationBlockLayouter.paginateParagraphAcrossPages(
                    blockIndex = blockIndex,
                    block = block,
                    lines = lines,
                    lineHeight = lineHeight,
                    paragraphSpacing = paragraphSpacing,
                    printableHeightPt = printableHeightPt,
                    onAddItem = { currentPage.add(it) },
                    onPageFilled = { onCommitNewPage(); runningHeight = 0f },
                    onHeightAdded = { runningHeight += it },
                    initialCharOffset = 0
                )
            }
        }
        return runningHeight
    }
}
