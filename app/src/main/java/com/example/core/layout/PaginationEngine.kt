package com.example.core.layout

import android.graphics.Paint
import com.example.data.model.DividerBlock
import com.example.data.model.ImageBlock
import com.example.data.model.PageBreakBlock
import com.example.data.model.ParagraphBlock
import com.example.data.model.RichDocument
import com.example.data.model.TableBlock

/**
 * High-performance typographic pagination engine for Smart Word Processor.
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
            Paint().apply { isAntiAlias = true }
        } catch (_: Throwable) {
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
                    currentPage.add(PageBreakPageItem(originalBlockIndex = blockIndex, originalBlock = block))
                    commitCurrentPageAndStartNew()
                }

                is com.example.data.model.ColumnBreakBlock -> {
                    currentPage.add(ColumnBreakPageItem(originalBlockIndex = blockIndex, originalBlock = block))
                    commitCurrentPageAndStartNew()
                }

                is com.example.data.model.SectionBreakBlock -> {
                    currentPage.add(SectionBreakPageItem(originalBlockIndex = blockIndex, originalBlock = block))
                    if (block.isNextPage) {
                        commitCurrentPageAndStartNew()
                    }
                }

                is DividerBlock -> {
                    val dividerHeight = 16f
                    if (currentHeight + dividerHeight > printableHeightPt && currentPage.isNotEmpty()) {
                        commitCurrentPageAndStartNew()
                    }
                    currentPage.add(
                        DividerPageItem(originalBlockIndex = blockIndex, originalBlock = block, heightPt = dividerHeight)
                    )
                    currentHeight += dividerHeight
                }

                is ImageBlock -> {
                    val imgHeight = ((block.heightDp * 72f) / 160f).coerceIn(60f, printableHeightPt * 0.65f) + 16f
                    if (currentHeight + imgHeight > printableHeightPt && currentPage.isNotEmpty()) {
                        commitCurrentPageAndStartNew()
                    }
                    currentPage.add(
                        ImagePageItem(originalBlockIndex = blockIndex, originalBlock = block, heightPt = imgHeight)
                    )
                    currentHeight += imgHeight
                }

                is TableBlock -> {
                    val headerRowHeight = 24f
                    val dataRowHeight = 20f
                    val totalTableHeight = headerRowHeight + ((block.rowCount - 1).coerceAtLeast(0) * dataRowHeight) + 10f

                    if (currentHeight + totalTableHeight <= printableHeightPt) {
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
                        currentHeight = PaginationBlockLayouter.sliceTableRows(
                            blockIndex = blockIndex,
                            block = block,
                            printableHeightPt = printableHeightPt,
                            currentHeight = currentHeight,
                            headerRowHeight = headerRowHeight,
                            dataRowHeight = dataRowHeight,
                            onAddItem = { currentPage.add(it) },
                            onCommitNewPage = { commitCurrentPageAndStartNew() }
                        )
                    }
                }

                is ParagraphBlock -> {
                    currentHeight = PaginationParagraphLayouter.layoutParagraph(
                        blockIndex = blockIndex,
                        block = block,
                        layout = layout,
                        printableWidthPt = printableWidthPt,
                        printableHeightPt = printableHeightPt,
                        paint = paint,
                        currentHeight = currentHeight,
                        currentPage = currentPage,
                        onCommitNewPage = { commitCurrentPageAndStartNew() }
                    )
                }

                is com.example.data.model.TableOfContentsBlock -> {
                    val tocHeight = 120f
                    if (currentHeight + tocHeight > printableHeightPt && currentPage.isNotEmpty()) commitCurrentPageAndStartNew()
                    currentPage.add(TableOfContentsPageItem(originalBlockIndex = blockIndex, originalBlock = block, heightPt = tocHeight))
                    currentHeight += tocHeight
                }

                is com.example.data.model.SignatureBlock -> {
                    val sigHeight = 90f
                    if (currentHeight + sigHeight > printableHeightPt && currentPage.isNotEmpty()) commitCurrentPageAndStartNew()
                    currentPage.add(SignaturePageItem(originalBlockIndex = blockIndex, originalBlock = block, heightPt = sigHeight))
                    currentHeight += sigHeight
                }

                is com.example.data.model.ShapeBlock -> {
                    val shapeHeight = 75f
                    if (currentHeight + shapeHeight > printableHeightPt && currentPage.isNotEmpty()) commitCurrentPageAndStartNew()
                    currentPage.add(ShapePageItem(originalBlockIndex = blockIndex, originalBlock = block, heightPt = shapeHeight))
                    currentHeight += shapeHeight
                }

                is com.example.data.model.DrawingBlock -> {
                    val drawHeight = ((block.heightDp * 72f) / 160f).coerceIn(60f, printableHeightPt * 0.5f)
                    if (currentHeight + drawHeight > printableHeightPt && currentPage.isNotEmpty()) commitCurrentPageAndStartNew()
                    currentPage.add(DrawingPageItem(originalBlockIndex = blockIndex, originalBlock = block, heightPt = drawHeight))
                    currentHeight += drawHeight
                }

                is com.example.data.model.TextBoxBlock -> {
                    val tbHeight = ((block.heightDp * 72f) / 160f).coerceIn(40f, printableHeightPt * 0.4f)
                    if (currentHeight + tbHeight > printableHeightPt && currentPage.isNotEmpty()) commitCurrentPageAndStartNew()
                    currentPage.add(TextBoxPageItem(originalBlockIndex = blockIndex, originalBlock = block, heightPt = tbHeight))
                    currentHeight += tbHeight
                }

                is com.example.data.model.FootnoteBlock -> {
                    val fnHeight = 36f
                    if (currentHeight + fnHeight > printableHeightPt && currentPage.isNotEmpty()) commitCurrentPageAndStartNew()
                    currentPage.add(FootnotePageItem(originalBlockIndex = blockIndex, originalBlock = block, heightPt = fnHeight))
                    currentHeight += fnHeight
                }
            }
        }

        if (currentPage.isNotEmpty()) rawPages.add(currentPage)

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

        return PaginationPageBuilder.buildPages(rawPages, document, printableHeightPt)
    }

    fun computeHeadingPageMap(document: RichDocument): Map<String, Int> {
        val pages = paginate(document)
        return PaginationPageBuilder.extractHeadingPageMap(pages)
    }

    fun breakTextIntoLines(
        text: String,
        paint: Paint? = null,
        fontSizePt: Float = 11f,
        isBold: Boolean = false,
        maxWidthPt: Float
    ): List<String> {
        return PaginationTextBreaker.breakTextIntoLines(text, paint, fontSizePt, isBold, maxWidthPt)
    }
}
