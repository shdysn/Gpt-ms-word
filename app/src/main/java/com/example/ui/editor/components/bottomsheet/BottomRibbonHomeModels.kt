package com.example.ui.editor.components.bottomsheet

/**
 * Submenus within the Microsoft Word Mobile Home Tab bottom sheet.
 * Matches the exact hierarchical navigation shown in MS Word Mobile.
 */
enum class HomeSubmenu {
    ROOT,
    FONT_FAMILY,
    FONT_COLOR,
    HIGHLIGHT_COLOR,
    MORE_FORMATTING,
    CHANGE_CASE,
    BULLETS,
    NUMBERING,
    PARAGRAPH_FORMATTING,
    SPECIAL_INDENT,
    LINE_SPACING,
    STYLES
}

/**
 * Bullet style preview options matching MS Word Bullets gallery.
 */
enum class BulletGalleryStyle(val label: String, val symbol: String) {
    NONE("None", ""),
    SOLID_CIRCLE("Dot", "•"),
    HOLLOW_CIRCLE("Circle", "○"),
    SOLID_SQUARE("Square", "■"),
    DIAMONDS("Diamond", "❖"),
    ARROWHEAD("Arrow", "➢"),
    CHECKMARK("Check", "✓")
}

/**
 * Numbering style preview options matching MS Word Numbering gallery.
 */
enum class NumberingGalleryStyle(val label: String, val preview1: String, val preview2: String, val preview3: String) {
    NONE("None", "", "", ""),
    DECIMAL_DOT("1.", "1.", "2.", "3."),
    DECIMAL_PAREN("1)", "1)", "2)", "3)"),
    ROMAN_UPPER("I.", "I.", "II.", "III."),
    ALPHA_UPPER("A.", "A.", "B.", "C."),
    ALPHA_PAREN("a)", "a)", "b)", "c)"),
    ALPHA_DOT("a.", "a.", "b.", "c."),
    ROMAN_LOWER("i.", "i.", "ii.", "iii.")
}

/**
 * Standard colors defined in MS Word Mobile palette.
 */
object WordColorPalette {
    // 10 Theme Base Columns with 5 tint/shade steps each (10x5 grid)
    val THEME_COLORS_GRID = listOf(
        // Col 1: White / Light greys
        listOf("#FFFFFF", "#F2F2F2", "#D9D9D9", "#BFBFBF", "#7F7F7F"),
        // Col 2: Black / Dark greys
        listOf("#000000", "#7F7F7F", "#595959", "#3F3F3F", "#262626"),
        // Col 3: Warm Grey / Tan
        listOf("#EEECE1", "#DDD9C3", "#C4BD97", "#938953", "#494429"),
        // Col 4: Navy Blue
        listOf("#1F497D", "#DCE6F1", "#B8CCE4", "#95B3D7", "#366092"),
        // Col 5: Accent Blue
        listOf("#4F81BD", "#DBE5F1", "#B8CCE4", "#95B3D7", "#366092"),
        // Col 6: Orange / Coral
        listOf("#F79646", "#FDEADA", "#FBD5B5", "#FAC08F", "#E36C09"),
        // Col 7: Purple / Violet
        listOf("#8064A2", "#E4DFEC", "#CCC1DA", "#B2A2C7", "#5F497A"),
        // Col 8: Teal / Aqua
        listOf("#4BACC6", "#DBEEF4", "#B7DDE8", "#92CDDC", "#31859B"),
        // Col 9: Sky Blue
        listOf("#1F497D", "#C6D9F1", "#8DB3E2", "#548DD4", "#17365D"),
        // Col 10: Olive / Green
        listOf("#9BBB59", "#EAF1DD", "#D7E3BC", "#C3D69B", "#76923C")
    )

    // Standard 10 vibrant colors
    val STANDARD_COLORS = listOf(
        "#C00000", // Dark Red
        "#FF0000", // Red
        "#FFC000", // Orange
        "#FFFF00", // Yellow
        "#92D050", // Light Green
        "#00B050", // Green
        "#00B0F0", // Light Blue
        "#0070C0", // Blue
        "#002060", // Dark Blue
        "#7030A0"  // Purple
    )

    // Highlight marker colors
    val HIGHLIGHT_COLORS = listOf(
        "#FFFF00", // Yellow
        "#00FF00", // Bright Green
        "#00FFFF", // Cyan
        "#FF00FF", // Magenta
        "#0000FF", // Blue
        "#FF0000", // Red
        "#000080", // Dark Blue
        "#008080", // Teal
        "#008000", // Green
        "#800080", // Violet
        "#800000", // Maroon
        "#808000", // Olive
        "#808080", // Grey
        "#C0C0C0"  // Light Grey
    )
}
