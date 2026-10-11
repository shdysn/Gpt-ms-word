package com.example.data.model

import com.example.core.layout.PageLayoutConfig
import java.util.UUID

/**
 * Text alignment options matching Microsoft Word ribbon.
 */
enum class TextAlignment {
    START,
    CENTER,
    END,
    JUSTIFY
}

/**
 * Heading styles for semantic hierarchy.
 */
enum class HeadingStyle(val displayName: String, val defaultFontSizeSp: Float, val isBold: Boolean) {
    NORMAL("Normal", 15f, false),
    TITLE("Title", 28f, true),
    SUBTITLE("Subtitle", 19f, false),
    HEADING_1("Heading 1", 22f, true),
    HEADING_2("Heading 2", 18f, true),
    HEADING_3("Heading 3", 16f, true),
    QUOTE("Quote", 15f, false)
}

/**
 * List bullet / numbering format.
 */
enum class ListType {
    NONE,
    BULLET,
    NUMBERED
}

/**
 * Inline style span applied to sub-ranges of text in a paragraph.
 */
data class InlineSpan(
    val start: Int,
    val end: Int,
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
    val linkUrl: String? = null
)

/**
 * Sealed interface for all block elements in a Word document.
 */
sealed interface BlockElement {
    val id: String
}

/**
 * Paragraph element containing text, inline spans, and paragraph-level formatting.
 */
data class ParagraphBlock(
    override val id: String = UUID.randomUUID().toString(),
    val text: String = "",
    val spans: List<InlineSpan> = emptyList(),
    val alignment: TextAlignment = TextAlignment.START,
    val headingStyle: HeadingStyle = HeadingStyle.NORMAL,
    val listType: ListType = ListType.NONE,
    val indentationLevel: Int = 0,
    val isRtl: Boolean = false, // True for Urdu, Arabic, Persian, Hebrew
    val lineSpacingMultiplier: Float = 1.25f
) : BlockElement

/**
 * Table cell model.
 */
data class TableCell(
    val text: String = "",
    val isBold: Boolean = false,
    val isItalic: Boolean = false,
    val backgroundColorHex: String? = null,
    val alignment: TextAlignment = TextAlignment.START
)

/**
 * Table block element for multi-column structured data.
 */
data class TableBlock(
    override val id: String = UUID.randomUUID().toString(),
    val rowCount: Int = 2,
    val columnCount: Int = 2,
    val cells: List<List<TableCell>> = List(rowCount) { List(columnCount) { TableCell() } },
    val hasBorders: Boolean = true,
    val borderColorHex: String = "#CBD5E1",
    val tableWidthPercent: Int = 100,
    val alignment: TextAlignment = TextAlignment.CENTER
) : BlockElement

/**
 * Image block element.
 */
data class ImageBlock(
    override val id: String = UUID.randomUUID().toString(),
    val imageUri: String,
    val caption: String = "",
    val widthDp: Int = 240,
    val heightDp: Int = 180,
    val alignment: TextAlignment = TextAlignment.CENTER,
    val aspectRatio: Float = 1.333f
) : BlockElement

/**
 * Horizontal rule/divider block.
 */
data class DividerBlock(
    override val id: String = UUID.randomUUID().toString()
) : BlockElement

/**
 * Explicit page break block.
 */
data class PageBreakBlock(
    override val id: String = UUID.randomUUID().toString()
) : BlockElement

/**
 * Complete in-memory model of a Rich Document.
 */
data class RichDocument(
    val id: String = UUID.randomUUID().toString(),
    val title: String = "Untitled Document",
    val author: String = "Author",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val layoutConfig: PageLayoutConfig = PageLayoutConfig(),
    val blocks: List<BlockElement> = listOf(ParagraphBlock())
) {
    val wordCount: Int
        get() {
            var words = blocks.filterIsInstance<ParagraphBlock>().sumOf { block ->
                if (block.text.isBlank()) 0
                else block.text.trim().split("\\s+".toRegex()).count { it.isNotBlank() }
            }
            blocks.filterIsInstance<TableBlock>().forEach { table ->
                table.cells.forEach { row ->
                    row.forEach { cell ->
                        if (cell.text.isNotBlank()) {
                            words += cell.text.trim().split("\\s+".toRegex()).count { it.isNotBlank() }
                        }
                    }
                }
            }
            return words
        }

    val characterCount: Int
        get() {
            var chars = blocks.filterIsInstance<ParagraphBlock>().sumOf { it.text.length }
            blocks.filterIsInstance<TableBlock>().forEach { table ->
                table.cells.forEach { row ->
                    row.forEach { cell ->
                        chars += cell.text.length
                    }
                }
            }
            return chars
        }

    val characterCountNoSpaces: Int
        get() {
            var chars = blocks.filterIsInstance<ParagraphBlock>().sumOf { it.text.count { c -> !c.isWhitespace() } }
            blocks.filterIsInstance<TableBlock>().forEach { table ->
                table.cells.forEach { row ->
                    row.forEach { cell ->
                        chars += cell.text.count { c -> !c.isWhitespace() }
                    }
                }
            }
            return chars
        }

    val paragraphCount: Int
        get() = blocks.count { it is ParagraphBlock }

    val plainTextSnippet: String
        get() = blocks.filterIsInstance<ParagraphBlock>()
            .map { it.text }
            .firstOrNull { it.isNotBlank() }
            ?.take(160) ?: "Empty document"

    /**
     * Exact page count based on layout dimensions, margins, and pagination engine.
     */
    val estimatedPageCount: Int
        get() = com.example.core.layout.PaginationEngine.paginate(this).size
}
