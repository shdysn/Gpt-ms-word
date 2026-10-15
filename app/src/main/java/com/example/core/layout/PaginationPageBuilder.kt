package com.example.core.layout

import com.example.data.model.HeadingStyle
import com.example.data.model.ParagraphBlock
import com.example.data.model.RichDocument

/**
 * Handles assembly and finalization of DocumentPages from raw page items,
 * including header/footer computation and Table of Contents resolution.
 */
object PaginationPageBuilder {

    /**
     * Builds final DocumentPage objects from raw page item lists.
     */
    fun buildPages(
        rawPages: List<List<PageItem>>,
        document: RichDocument,
        printableHeightPt: Float
    ): List<DocumentPage> {
        val layout = document.layoutConfig
        val totalPages = rawPages.size

        val pages = rawPages.mapIndexed { index, items ->
            val pageNumber = index + 1
            val isFirstPage = index == 0
            val skipHeaderFooter = layout.differentFirstPage && isFirstPage

            val effectiveHeaderText = if (layout.differentOddEvenPages && pageNumber % 2 == 0) {
                if (layout.headerEvenText.isNotBlank()) layout.headerEvenText else layout.headerText
            } else layout.headerText

            val effectiveFooterText = if (layout.differentOddEvenPages && pageNumber % 2 == 0) {
                if (layout.footerEvenText.isNotBlank()) layout.footerEvenText else layout.footerText
            } else layout.footerText

            val hasHeader = layout.showHeader && effectiveHeaderText.isNotBlank() && !skipHeaderFooter
            val hasFooter = layout.showFooter && effectiveFooterText.isNotBlank() && !skipHeaderFooter

            val headerText = if (hasHeader) {
                DocumentPage.formatHeaderFooterText(effectiveHeaderText, pageNumber, totalPages, layout, document.title, isHeader = true)
            } else ""

            val footerText = if (hasFooter) {
                DocumentPage.formatHeaderFooterText(effectiveFooterText, pageNumber, totalPages, layout, document.title, isHeader = false)
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

        val headingMap = extractHeadingPageMap(pages)

        val resolvedTocEntries = document.blocks.filterIsInstance<ParagraphBlock>()
            .filter { it.headingStyle != HeadingStyle.NORMAL && it.text.isNotBlank() }
            .map { hBlock ->
                TocHeadingEntry(
                    blockId = hBlock.id,
                    title = hBlock.text.trim(),
                    headingStyle = hBlock.headingStyle,
                    pageNumber = headingMap[hBlock.id] ?: 1
                )
            }

        return pages.map { page ->
            val hasToc = page.items.any { it is TableOfContentsPageItem }
            if (hasToc) {
                val updatedItems = page.items.map { item ->
                    if (item is TableOfContentsPageItem) {
                        item.copy(resolvedEntries = resolvedTocEntries)
                    } else item
                }
                page.copy(items = updatedItems)
            } else {
                page
            }
        }
    }

    /**
     * Extracts a map of block IDs to 1-based page numbers for all headings.
     */
    fun extractHeadingPageMap(pages: List<DocumentPage>): Map<String, Int> {
        val map = mutableMapOf<String, Int>()
        pages.forEach { page ->
            page.items.forEach { item ->
                if (item is ParagraphPageItem && item.originalBlock.headingStyle != HeadingStyle.NORMAL) {
                    map.putIfAbsent(item.originalBlock.id, page.pageNumber)
                }
            }
        }
        return map
    }
}
