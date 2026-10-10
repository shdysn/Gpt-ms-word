package com.example.core.layout

/**
 * Standard page size specifications with ISO and ANSI dimensions.
 */
enum class PageSize(
    val displayName: String,
    val widthMm: Float,
    val heightMm: Float,
    val widthPt: Int, // 72 DPI (Standard PDF / PostScript points)
    val heightPt: Int
) {
    A4("A4 (210 × 297 mm)", 210f, 297f, 595, 842),
    LETTER("US Letter (8.5 × 11 in)", 215.9f, 279.4f, 612, 792),
    LEGAL("US Legal (8.5 × 14 in)", 215.9f, 355.6f, 612, 1008),
    A5("A5 (148 × 210 mm)", 148f, 210f, 420, 595),
    EXECUTIVE("Executive (7.25 × 10.5 in)", 184.1f, 266.7f, 522, 756)
}

/**
 * Page orientation.
 */
enum class PageOrientation {
    PORTRAIT,
    LANDSCAPE
}

/**
 * Margin presets inspired by Microsoft Word.
 */
enum class MarginPreset(
    val displayName: String,
    val topMm: Float,
    val bottomMm: Float,
    val leftMm: Float,
    val rightMm: Float
) {
    NORMAL("Normal (25.4 mm)", 25.4f, 25.4f, 25.4f, 25.4f),
    NARROW("Narrow (12.7 mm)", 12.7f, 12.7f, 12.7f, 12.7f),
    MODERATE("Moderate (Top/Bot 25.4, L/R 19.1 mm)", 25.4f, 25.4f, 19.1f, 19.1f),
    WIDE("Wide (Top/Bot 25.4, L/R 50.8 mm)", 25.4f, 25.4f, 50.8f, 50.8f)
}

/**
 * Custom page layout configuration.
 */
data class PageLayoutConfig(
    val pageSize: PageSize = PageSize.A4,
    val orientation: PageOrientation = PageOrientation.PORTRAIT,
    val marginPreset: MarginPreset = MarginPreset.NORMAL,
    val customTopMarginMm: Float? = null,
    val customBottomMarginMm: Float? = null,
    val customLeftMarginMm: Float? = null,
    val customRightMarginMm: Float? = null,
    val showHeader: Boolean = true,
    val showFooter: Boolean = true,
    val headerText: String = "",
    val footerText: String = "Page {page} of {pages}"
) {
    val topMarginMm: Float get() = customTopMarginMm ?: marginPreset.topMm
    val bottomMarginMm: Float get() = customBottomMarginMm ?: marginPreset.bottomMm
    val leftMarginMm: Float get() = customLeftMarginMm ?: marginPreset.leftMm
    val rightMarginMm: Float get() = customRightMarginMm ?: marginPreset.rightMm

    val pageWidthPt: Int
        get() = if (orientation == PageOrientation.PORTRAIT) pageSize.widthPt else pageSize.heightPt

    val pageHeightPt: Int
        get() = if (orientation == PageOrientation.PORTRAIT) pageSize.heightPt else pageSize.widthPt

    /**
     * Width to height aspect ratio for rendering canvas in Compose.
     */
    val aspectRatio: Float
        get() = pageWidthPt.toFloat() / pageHeightPt.toFloat()

    /**
     * Printable area in points.
     */
    val printableWidthPt: Float
        get() {
            val leftPt = (leftMarginMm / 25.4f) * 72f
            val rightPt = (rightMarginMm / 25.4f) * 72f
            return pageWidthPt - leftPt - rightPt
        }

    val printableHeightPt: Float
        get() {
            val topPt = (topMarginMm / 25.4f) * 72f
            val bottomPt = (bottomMarginMm / 25.4f) * 72f
            return pageHeightPt - topPt - bottomPt
        }
}
