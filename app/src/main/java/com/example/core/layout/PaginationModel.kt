package com.example.core.layout

import com.example.data.model.DividerBlock
import com.example.data.model.ImageBlock
import com.example.data.model.PageBreakBlock
import com.example.data.model.ParagraphBlock
import com.example.data.model.TableBlock

/**
 * Individual content unit rendered on a paginated page.
 */
sealed interface PageItem {
    val originalBlockIndex: Int
    val heightPt: Float
}

/**
 * Paragraph or split portion of a paragraph appearing on a page.
 */
data class ParagraphPageItem(
    override val originalBlockIndex: Int,
    val originalBlock: ParagraphBlock,
    val lineSliceText: String,
    val startChar: Int,
    val endChar: Int,
    val isContinuation: Boolean, // True if this paragraph started on a previous page
    val hasContinuation: Boolean, // True if this paragraph continues onto the next page
    override val heightPt: Float
) : PageItem

/**
 * Table (or subset of rows of a table) rendered on a page.
 */
data class TablePageItem(
    override val originalBlockIndex: Int,
    val originalBlock: TableBlock,
    val startRow: Int,
    val endRow: Int,
    override val heightPt: Float
) : PageItem

/**
 * Image element rendered on a page.
 */
data class ImagePageItem(
    override val originalBlockIndex: Int,
    val originalBlock: ImageBlock,
    override val heightPt: Float
) : PageItem

/**
 * Horizontal rule / divider rendered on a page.
 */
data class DividerPageItem(
    override val originalBlockIndex: Int,
    val originalBlock: DividerBlock,
    override val heightPt: Float = 16f
) : PageItem

/**
 * Marker for an explicit page break.
 */
data class PageBreakPageItem(
    override val originalBlockIndex: Int,
    val originalBlock: PageBreakBlock,
    override val heightPt: Float = 0f
) : PageItem

/**
 * Marker for an explicit column break.
 */
data class ColumnBreakPageItem(
    override val originalBlockIndex: Int,
    val originalBlock: com.example.data.model.ColumnBreakBlock,
    override val heightPt: Float = 16f
) : PageItem

/**
 * Marker for an explicit section break.
 */
data class SectionBreakPageItem(
    override val originalBlockIndex: Int,
    val originalBlock: com.example.data.model.SectionBreakBlock,
    override val heightPt: Float = 24f
) : PageItem

/**
 * Resolved heading entry for Table of Contents navigation.
 */
data class TocHeadingEntry(
    val blockId: String,
    val title: String,
    val headingStyle: com.example.data.model.HeadingStyle,
    val pageNumber: Int
)

/**
 * Table of Contents rendered on a page.
 */
data class TableOfContentsPageItem(
    override val originalBlockIndex: Int,
    val originalBlock: com.example.data.model.TableOfContentsBlock,
    val resolvedEntries: List<TocHeadingEntry> = emptyList(),
    override val heightPt: Float = 120f
) : PageItem

/**
 * Verified Digital Signature rendered on a page.
 */
data class SignaturePageItem(
    override val originalBlockIndex: Int,
    val originalBlock: com.example.data.model.SignatureBlock,
    override val heightPt: Float = 90f
) : PageItem

/**
 * Stylized shape / callout note rendered on a page.
 */
data class ShapePageItem(
    override val originalBlockIndex: Int,
    val originalBlock: com.example.data.model.ShapeBlock,
    override val heightPt: Float = 75f
) : PageItem

/**
 * Footnote item rendered on a paginated page.
 */
data class FootnotePageItem(
    override val originalBlockIndex: Int,
    val originalBlock: com.example.data.model.FootnoteBlock,
    override val heightPt: Float = 36f
) : PageItem

/**
 * Physical page breakdown containing laid out items, header, and footer.
 */
data class DocumentPage(
    val pageIndex: Int, // 0-based
    val pageNumber: Int, // 1-based
    val totalPages: Int,
    val layout: PageLayoutConfig,
    val items: List<PageItem> = emptyList(),
    val formattedHeader: String = "",
    val formattedFooter: String = "",
    val hasHeader: Boolean = false,
    val hasFooter: Boolean = false,
    val usedContentHeightPt: Float = 0f,
    val availableContentHeightPt: Float = 0f
) {
    /**
     * Resolves tokenized header/footer strings such as "Page {page} of {pages}"
     */
    companion object {
        fun formatHeaderFooterText(
            template: String,
            pageNumber: Int,
            totalPages: Int,
            layout: PageLayoutConfig,
            documentTitle: String = ""
        ): String {
            val formattedPageNum = layout.formatPageNumber(pageNumber)
            val formattedTotalPages = layout.formatPageNumber(totalPages)
            return template
                .replace("{page}", formattedPageNum)
                .replace("{pages}", formattedTotalPages)
                .replace("{title}", documentTitle)
        }
    }
}
