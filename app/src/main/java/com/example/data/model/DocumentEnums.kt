package com.example.data.model

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
 * Underline styles matching Microsoft Word.
 */
enum class UnderlineStyle {
    NONE,
    SINGLE,
    DOUBLE,
    DOTTED,
    WAVY
}

/**
 * Strikethrough styles matching Microsoft Word.
 */
enum class StrikethroughStyle {
    NONE,
    SINGLE,
    DOUBLE
}

/**
 * Vertical alignment within table cells.
 */
enum class CellVerticalAlignment {
    TOP,
    CENTER,
    BOTTOM
}

/**
 * Tab Stop alignment and leader styles.
 */
enum class TabAlignment {
    LEFT,
    CENTER,
    RIGHT,
    DECIMAL
}

enum class TabLeader {
    NONE,
    DOT,
    HYPHEN,
    LINE
}

data class TabStop(
    val positionPt: Float,
    val alignment: TabAlignment = TabAlignment.LEFT,
    val leader: TabLeader = TabLeader.NONE
)

/**
 * Custom named style defined by the user.
 */
data class CustomStyle(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val fontSizeSp: Float = 15f,
    val isBold: Boolean = false,
    val isItalic: Boolean = false,
    val textColorHex: String? = null,
    val spaceBeforePt: Float = 0f,
    val spaceAfterPt: Float = 0f,
    val lineSpacingMultiplier: Float = 1.25f,
    val firstLineIndentPt: Float = 0f
)
