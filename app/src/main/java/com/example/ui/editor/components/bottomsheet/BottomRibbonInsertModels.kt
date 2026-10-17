package com.example.ui.editor.components.bottomsheet

/**
 * Submenus available inside the Microsoft Word Insert tab.
 */
enum class InsertSubmenu {
    ROOT,
    TABLE,
    PICTURES,
    SHAPES,
    LINK,
    PAGE_NUMBER
}

/**
 * Page number position representations for the 3x2 grid.
 */
enum class PageNumberSlot(
    val isHeader: Boolean,
    val alignmentName: String,
    val label: String
) {
    HEADER_LEFT(true, "START", "Top Left"),
    HEADER_CENTER(true, "CENTER", "Top Center"),
    HEADER_RIGHT(true, "END", "Top Right"),
    FOOTER_LEFT(false, "START", "Bottom Left"),
    FOOTER_CENTER(false, "CENTER", "Bottom Center"),
    FOOTER_RIGHT(false, "END", "Bottom Right")
}
