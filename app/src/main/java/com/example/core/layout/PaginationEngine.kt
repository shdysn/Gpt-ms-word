package com.example.core.layout

import android.graphics.Paint
import android.graphics.Typeface
import com.example.data.model.DividerBlock
import com.example.data.model.HeadingStyle
import com.example.data.model.ImageBlock
import com.example.data.model.PageBreakBlock
import com.example.data.model.ParagraphBlock
import com.example.data.model.RichDocument
import com.example.data.model.TableBlock

/**
 * High-performance typographic pagination engine for Smart Word Processor.
 *
 * Divides rich-text document blocks into discrete pages based on:
 * - ISO/ANSI Paper sizes (A4, A5, Letter, Legal) & Orientation (Portrait/Landscape)
 * - Exact margins (top, bottom, left, right in PostScript points)
 * - Font metrics and line wrapping across printable content width
 * - Paragraph boundaries and line-level continuation splitting
 * - Block elements: Images, Tables, Dividers, and Explicit Page Breaks
 * - Header and footer reserved areas and dynamic page numbering tokens
 */
object PaginationEngine {

    /**
     * Splits a document into a list of physical DocumentPage representations.
     */
    fun paginate(document: RichDocument): List<DocumentPage> {
        val layout = document.layoutConfig
        val printableWidthPt = layout.printableWidthPt
        val printableHeightPt = layout.printableHeightPt

        val paint = try {
            Paint().apply {
                isAntiAlias = true
            }
        } catch (e: Throwable) {
            null
        }

        val rawPages = mutableListOf<MutableList<PageItem>>()
        var currentPage = mutableListOf<PageItem>()
        var currentHeight = 0f

        fun commitCurrentPageAndStartNew() {
            rawPages.add(currentPage)
            currentPage = mutableListOf()
            currentHeight = 0f
        }

        for ((blockIndex, block) in document.blocks.withIndex()) {
            when (block) {
                is PageBreakBlock -> {
                    // Explicit page break: finish the current page immediately
                    currentPage.add(PageBreakPageItem(originalBlockIndex = blockIndex, originalBlock = block))
                    commitCurrentPageAndStartNew()
                }

                is DividerBlock -> {
                    val dividerHeight = 16f
                    if (currentHeight + dividerHeight > printableHeightPt && currentPage.isNotEmpty()) {
                        commitCurrentPageAndStartNew()
                    }
                    currentPage.add(
                        DividerPageItem(
                            originalBlockIndex = blockIndex,
                            originalBlock = block,
                            heightPt = dividerHeight
                        )
                    )
                    currentHeight += dividerHeight
                }

                is ImageBlock -> {
                    // Proportional height in points + caption spacing
                    val imgHeight = ((block.heightDp * 72f) / 160f).coerceIn(60f, printableHeightPt * 0.65f) + 16f
                    if (currentHeight + imgHeight > printableHeightPt && currentPage.isNotEmpty()) {
                        commitCurrentPageAndStartNew()
                    }
                    currentPage.add(
                        ImagePageItem(
                            originalBlockIndex = blockIndex,
                            originalBlock = block,
                            heightPt = imgHeight
                        )
                    )
                    currentHeight += imgHeight
                }

                is TableBlock -> {
                    val headerRowHeight = 24f
                    val dataRowHeight = 20f
                    val totalTableHeight = headerRowHeight + ((block.rowCount - 1).coerceAtLeast(0) * dataRowHeight) + 10f

                    if (currentHeight + totalTableHeight <= printableHeightPt) {
                        // Entire table fits on current page
                        currentPage.add(
                            TablePageItem(
                                originalBlockIndex = blockIndex,
                                originalBlock = block,
                                startRow = 0,
                                endRow = block.rowCount,
                                heightPt = totalTableHeight
                            )
                        )
                        currentHeight += totalTableHeight
                    } else if (currentPage.isNotEmpty() && totalTableHeight <= printableHeightPt) {
                        // Table does not fit on current page, but fits on a fresh page
                        commitCurrentPageAndStartNew()
                        currentPage.add(
                            TablePageItem(
                                originalBlockIndex = blockIndex,
                                originalBlock = block,
                                startRow = 0,
                                endRow = block.rowCount,
                                heightPt = totalTableHeight
                            )
                        )
                        currentHeight += totalTableHeight
                    } else {
                        // Table is too tall for remaining space or an entire page: split rows across pages
                        var startRow = 0
                        while (startRow < block.rowCount) {
                            val availableH = (printableHeightPt - currentHeight).coerceAtLeast(headerRowHeight)
                            var rowsThatFit = if (startRow == 0) {
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

                            currentPage.add(
                                TablePageItem(
                                    originalBlockIndex = blockIndex,
                                    originalBlock = block,
                                    startRow = startRow,
                                    endRow = endRow,
                                    heightPt = sliceHeight
                                )
                            )
                            currentHeight += sliceHeight
                            startRow = endRow
                            if (startRow < block.rowCount) {
                                commitCurrentPageAndStartNew()
                            }
                        }
                    }
                }

                is ParagraphBlock -> {
                    val (fontSizePt, typefaceStyle) = getHeadingFontMetrics(block.headingStyle)
                    if (paint != null) {
                        try {
                            paint.textSize = fontSizePt
                            paint.typeface = Typeface.create(Typeface.DEFAULT, typefaceStyle)
                        } catch (e: Throwable) {}
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
                    val lines = breakTextIntoLines(block.text, paint, fontSizePt, isBold, effectivePrintableWidth)
                    val totalParagraphHeight = spaceBefore + (lines.size * lineHeight) + paragraphSpacing

                    if (currentHeight + totalParagraphHeight <= printableHeightPt) {
                        // Entire paragraph fits on current page
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
                        currentHeight += totalParagraphHeight
                    } else {
                        // Paragraph overflows
                        val availableSpace = printableHeightPt - currentHeight
                        val linesThatFit = (availableSpace / lineHeight).toInt()

                        if (currentPage.isNotEmpty() && linesThatFit < 2) {
                            // Avoid orphan first line: move entire paragraph to a fresh page
                            commitCurrentPageAndStartNew()
                            paginateParagraphAcrossPages(
                                blockIndex = blockIndex,
                                block = block,
                                lines = lines,
                                lineHeight = lineHeight,
                                paragraphSpacing = paragraphSpacing,
                                printableHeightPt = printableHeightPt,
                                onAddItem = { currentPage.add(it) },
                                onPageFilled = {
                                    commitCurrentPageAndStartNew()
                                },
                                onHeightAdded = { currentHeight += it },
                                initialCharOffset = 0
                            )
                        } else if (linesThatFit >= 2) {
                            // Split paragraph across current page and subsequent page(s)
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
                            commitCurrentPageAndStartNew()

                            val remainingLines = lines.subList(linesThatFit, lines.size)
                            val nextCharOffset = (firstEndChar + 1).coerceAtMost(block.text.length)
                            paginateParagraphAcrossPages(
                                blockIndex = blockIndex,
                                block = block,
                                lines = remainingLines,
                                lineHeight = lineHeight,
                                paragraphSpacing = paragraphSpacing,
                                printableHeightPt = printableHeightPt,
                                onAddItem = { currentPage.add(it) },
                                onPageFilled = {
                                    commitCurrentPageAndStartNew()
                                },
                                onHeightAdded = { currentHeight += it },
                                isInitialContinuation = true,
                                initialCharOffset = nextCharOffset
                            )
                        } else {
                            // Even on empty page, paragraph is taller than an entire page
                            paginateParagraphAcrossPages(
                                blockIndex = blockIndex,
                                block = block,
                                lines = lines,
                                lineHeight = lineHeight,
                                paragraphSpacing = paragraphSpacing,
                                printableHeightPt = printableHeightPt,
                                onAddItem = { currentPage.add(it) },
                                onPageFilled = {
                                    commitCurrentPageAndStartNew()
                                },
                                onHeightAdded = { currentHeight += it },
                                initialCharOffset = 0
                            )
                        }
                    }
                }

                is com.example.data.model.TableOfContentsBlock -> {
                    val tocHeight = 120f
                    if (currentHeight + tocHeight > printableHeightPt && currentPage.isNotEmpty()) {
                        commitCurrentPageAndStartNew()
                    }
                    currentPage.add(
                        TableOfContentsPageItem(
                            originalBlockIndex = blockIndex,
                            originalBlock = block,
                            heightPt = tocHeight
                        )
                    )
                    currentHeight += tocHeight
                }

                is com.example.data.model.SignatureBlock -> {
                    val sigHeight = 90f
                    if (currentHeight + sigHeight > printableHeightPt && currentPage.isNotEmpty()) {
                        commitCurrentPageAndStartNew()
                    }
                    currentPage.add(
                        SignaturePageItem(
                            originalBlockIndex = blockIndex,
                            originalBlock = block,
                            heightPt = sigHeight
                        )
                    )
                    currentHeight += sigHeight
                }

                is com.example.data.model.ShapeBlock -> {
                    val shapeHeight = 75f
                    if (currentHeight + shapeHeight > printableHeightPt && currentPage.isNotEmpty()) {
                        commitCurrentPageAndStartNew()
                    }
                    currentPage.add(
                        ShapePageItem(
                            originalBlockIndex = blockIndex,
                            originalBlock = block,
                            heightPt = shapeHeight
                        )
                    )
                    currentHeight += shapeHeight
                }
            }
        }

        // Commit final page if not empty
        if (currentPage.isNotEmpty()) {
            rawPages.add(currentPage)
        }

        // Ensure at least 1 page exists even for empty documents
        if (rawPages.isEmpty()) {
            val emptyBlock = document.blocks.firstOrNull() as? ParagraphBlock ?: ParagraphBlock()
            rawPages.add(
                mutableListOf(
                    ParagraphPageItem(
                        originalBlockIndex = 0,
                        originalBlock = emptyBlock,
                        lineSliceText = "",
                        startChar = 0,
                        endChar = 0,
                        isContinuation = false,
                        hasContinuation = false,
                        heightPt = 16f
                    )
                )
            )
        }

        val totalPages = rawPages.size
        return rawPages.mapIndexed { index, items ->
            val pageNumber = index + 1
            val isFirstPage = index == 0
            val skipHeaderFooter = layout.differentFirstPage && isFirstPage

            val hasHeader = layout.showHeader && layout.headerText.isNotBlank() && !skipHeaderFooter
            val hasFooter = layout.showFooter && layout.footerText.isNotBlank() && !skipHeaderFooter

            val headerText = if (hasHeader) {
                DocumentPage.formatHeaderFooterText(layout.headerText, pageNumber, totalPages, layout, document.title)
            } else ""

            val footerText = if (hasFooter) {
                DocumentPage.formatHeaderFooterText(layout.footerText, pageNumber, totalPages, layout, document.title)
            } else ""

            val usedHeight = items.sumOf { it.heightPt.toDouble() }.toFloat()

            DocumentPage(
                pageIndex = index,
                pageNumber = pageNumber,
                totalPages = totalPages,
                layout = layout,
                items = items,
                formattedHeader = headerText,
                formattedFooter = footerText,
                hasHeader = hasHeader,
                hasFooter = hasFooter,
                usedContentHeightPt = usedHeight,
                availableContentHeightPt = printableHeightPt
            )
        }
    }

    private fun paginateParagraphAcrossPages(
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
                        // Single word exceeds line width: break word by characters
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
    private fun measureWidth(text: String, paint: Paint?, fontSizePt: Float, isBold: Boolean): Float {
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

    private fun getHeadingFontMetrics(style: HeadingStyle): Pair<Float, Int> {
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
