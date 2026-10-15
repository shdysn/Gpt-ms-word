package com.example.data.model

import java.util.UUID

/**
 * Sealed interface for all block elements in a Word document.
 */
sealed interface BlockElement {
    val id: String
}

/**
 * Table cell model.
 */
data class TableCell(
    val text: String = "",
    val isBold: Boolean = false,
    val isItalic: Boolean = false,
    val backgroundColorHex: String? = null,
    val alignment: TextAlignment = TextAlignment.START,
    val widthPt: Float? = null,
    val colSpan: Int = 1,
    val rowSpan: Int = 1,
    val isMergedCovered: Boolean = false,
    val verticalAlignment: CellVerticalAlignment = CellVerticalAlignment.TOP,
    val borderTopWidthPt: Float? = null,
    val borderBottomWidthPt: Float? = null,
    val borderLeftWidthPt: Float? = null,
    val borderRightWidthPt: Float? = null,
    val paddingPt: Float = 6f
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
    val filterMode: String = "NORMAL",
    val cornerRadiusDp: Int = 8,
    val borderThicknessDp: Int = 0,
    val borderColorHex: String = "#CBD5E1",
    val rotationDegrees: Float = 0f,
    val wrapMode: String = "IN_LINE",
    val offsetXPt: Float = 0f,
    val offsetYPt: Float = 0f
) : BlockElement

/**
 * Freehand drawing / stylus stroke model.
 */
data class DrawingStroke(
    val points: List<Pair<Float, Float>>,
    val colorHex: String = "#000000",
    val strokeWidthPt: Float = 2f,
    val isHighlighter: Boolean = false
)

/**
 * Freehand drawing canvas block with touch/stylus strokes.
 */
data class DrawingBlock(
    override val id: String = UUID.randomUUID().toString(),
    val strokes: List<DrawingStroke> = emptyList(),
    val heightDp: Int = 180,
    val backgroundColorHex: String? = null
) : BlockElement

/**
 * Floating / draggable text box container.
 */
data class TextBoxBlock(
    override val id: String = UUID.randomUUID().toString(),
    val text: String = "",
    val widthDp: Int = 180,
    val heightDp: Int = 100,
    val backgroundColorHex: String = "#FFFFFF",
    val borderColorHex: String = "#2563EB",
    val borderWidthPt: Float = 1.5f,
    val rotationDeg: Float = 0f,
    val textColorHex: String = "#1E293B"
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
    val changeType: String = "EDIT"
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
    val svgPoints: String = "",
    val signerName: String = "",
    val signerTitle: String = "Authorized Signatory",
    val signedDate: String = "",
    val strokeColorHex: String = "#0F172A",
    val alignment: TextAlignment = TextAlignment.START
) : BlockElement

/**
 * Stylized shape / callout note / geometric vector shape block.
 */
data class ShapeBlock(
    override val id: String = UUID.randomUUID().toString(),
    val shapeType: String = "NOTE",
    val title: String = "Important Note",
    val content: String = "",
    val backgroundColorHex: String = "#EFF6FF",
    val borderColorHex: String = "#3B82F6",
    val strokeWidthPt: Float = 1.5f,
    val rotationDeg: Float = 0f,
    val textColorHex: String = "#1E293B",
    val widthDp: Int = 220,
    val heightDp: Int = 110
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
 * Explicit column break block for multi-column layouts.
 */
data class ColumnBreakBlock(
    override val id: String = UUID.randomUUID().toString()
) : BlockElement

/**
 * Section break block allowing independent page orientations, headers, and footers.
 */
data class SectionBreakBlock(
    override val id: String = UUID.randomUUID().toString(),
    val sectionName: String = "Section",
    val orientation: String = "PORTRAIT",
    val isNextPage: Boolean = true,
    val headerText: String = "",
    val footerText: String = ""
) : BlockElement
