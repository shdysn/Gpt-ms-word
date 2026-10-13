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
    val lineSpacingMultiplier: Float = 1.25f,
    val spaceBeforePt: Float = 0f,
    val spaceAfterPt: Float = 0f,
    val leftIndentPt: Float = 0f
) : BlockElement

/**
 * Table cell model.
 */
data class TableCell(
    val text: String = "",
    val isBold: Boolean = false,
    val isItalic: Boolean = false,
    val backgroundColorHex: String? = null,
    val alignment: TextAlignment = TextAlignment.START,
    val widthPt: Float? = null
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
    val alignment: TextAlignment = TextAlignment.CENTER,
    val columnWidthWeights: List<Float> = emptyList(),
    val hasHeaderRow: Boolean = true,
    val isZebraStriped: Boolean = false
) : BlockElement

/**
 * Image block element with borders, filters, and rotation.
 */
data class ImageBlock(
    override val id: String = UUID.randomUUID().toString(),
    val imageUri: String,
    val caption: String = "",
    val widthDp: Int = 240,
    val heightDp: Int = 180,
    val alignment: TextAlignment = TextAlignment.CENTER,
    val aspectRatio: Float = 1.333f,
    val filterMode: String = "NORMAL", // NORMAL, GRAYSCALE, SEPIA
    val cornerRadiusDp: Int = 8,
    val borderThicknessDp: Int = 0,
    val borderColorHex: String = "#CBD5E1",
    val rotationDegrees: Float = 0f,
    val wrapMode: String = "IN_LINE" // IN_LINE, SQUARE, BEHIND_TEXT, IN_FRONT
) : BlockElement

/**
 * Footnote / Endnote block element rendered at page or section bottoms.
 */
data class FootnoteBlock(
    override val id: String = UUID.randomUUID().toString(),
    val noteNumber: Int = 1,
    val referenceText: String = "",
    val noteContent: String = ""
) : BlockElement

/**
 * Document comment / annotation for review collaboration.
 */
data class DocumentComment(
    val id: String = UUID.randomUUID().toString(),
    val blockId: String,
    val author: String = "Reviewer",
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isResolved: Boolean = false
)

/**
 * Tracked change record for document revision auditing.
 */
data class TrackedChange(
    val id: String = UUID.randomUUID().toString(),
    val blockId: String,
    val originalText: String,
    val updatedText: String,
    val author: String = "Editor",
    val timestamp: Long = System.currentTimeMillis(),
    val changeType: String = "EDIT" // INSERT, DELETE, EDIT
)

/**
 * Table of Contents block dynamically generated from document headings.
 */
data class TableOfContentsBlock(
    override val id: String = UUID.randomUUID().toString(),
    val title: String = "Table of Contents"
) : BlockElement

/**
 * Verified Digital Signature block with signer name, title, and date.
 */
data class SignatureBlock(
    override val id: String = UUID.randomUUID().toString(),
    val svgPoints: String = "", // serialized line points "x1,y1;x2,y2..."
    val signerName: String = "",
    val signerTitle: String = "Authorized Signatory",
    val signedDate: String = "",
    val strokeColorHex: String = "#0F172A",
    val alignment: TextAlignment = TextAlignment.START
) : BlockElement

/**
 * Stylized shape / callout note block (e.g., Note, Warning, Tip, Quote banner).
 */
data class ShapeBlock(
    override val id: String = UUID.randomUUID().toString(),
    val shapeType: String = "NOTE", // NOTE, WARNING, SUCCESS, QUOTE
    val title: String = "Important Note",
    val content: String = "",
    val backgroundColorHex: String = "#EFF6FF",
    val borderColorHex: String = "#3B82F6"
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
    val blocks: List<BlockElement> = listOf(ParagraphBlock()),
    val passwordHash: String? = null, // Non-null when document is password-protected
    val comments: List<DocumentComment> = emptyList(),
    val trackedChanges: List<TrackedChange> = emptyList(),
    val isTrackChangesEnabled: Boolean = false
) {
    val wordCount: Int
        get() = blocks.filterIsInstance<ParagraphBlock>().sumOf { block ->
            block.text.trim().split("\\s+".toRegex()).count { it.isNotBlank() }
        }

    val characterCount: Int
        get() = blocks.filterIsInstance<ParagraphBlock>().sumOf { it.text.length }

    val isPasswordProtected: Boolean
        get() = !passwordHash.isNullOrBlank()

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
