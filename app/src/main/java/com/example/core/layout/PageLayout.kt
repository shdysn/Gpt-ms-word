package com.example.core.layout

import com.example.data.model.TextAlignment
import java.util.Locale

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
    EXECUTIVE("Executive (7.25 × 10.5 in)", 184.1f, 266.7f, 522, 756);

    val widthInches: Float get() = widthMm / 25.4f
    val heightInches: Float get() = heightMm / 25.4f

    /**
     * Formatted millimeter dimensions (e.g. "210 × 297 mm").
     */
    fun formattedDimensionsMm(orientation: PageOrientation = PageOrientation.PORTRAIT): String {
        val w = if (orientation == PageOrientation.PORTRAIT) widthMm else heightMm
        val h = if (orientation == PageOrientation.PORTRAIT) heightMm else widthMm
        return "${"%.1f".format(Locale.US, w).removeSuffix(".0")} × ${"%.1f".format(Locale.US, h).removeSuffix(".0")} mm"
    }

    /**
     * Formatted inch dimensions (e.g. "8.5 × 11.0 in").
     */
    fun formattedDimensionsInches(orientation: PageOrientation = PageOrientation.PORTRAIT): String {
        val w = if (orientation == PageOrientation.PORTRAIT) widthInches else heightInches
        val h = if (orientation == PageOrientation.PORTRAIT) heightInches else widthInches
        return "${"%.1f".format(Locale.US, w)} × ${"%.1f".format(Locale.US, h)} in"
    }

    /**
     * Full dual-unit dimension label (e.g. "210 × 297 mm (8.3 × 11.7 in)").
     */
    fun fullDimensionsText(orientation: PageOrientation = PageOrientation.PORTRAIT): String {
        return "${formattedDimensionsMm(orientation)} • ${formattedDimensionsInches(orientation)}"
    }
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
    WIDE("Wide (Top/Bot 25.4, L/R 50.8 mm)", 25.4f, 25.4f, 50.8f, 50.8f),
    CUSTOM("Custom", 25.4f, 25.4f, 25.4f, 25.4f)
}

/**
 * Decorative and print page borders.
 */
enum class PageBorderStyle(val displayName: String, val strokeWidthPt: Float) {
    NONE("None", 0f),
    SINGLE_THIN("Single Thin (1 pt)", 1f),
    SINGLE_THICK("Single Thick (2 pt)", 2f),
    DOUBLE("Double Line (3 pt)", 3f),
    DASHED("Dashed Line (1.5 pt)", 1.5f)
}

/**
 * Page background color presets.
 */
enum class PageBackgroundPreset(val displayName: String, val colorHex: String) {
    WHITE("White", "#FFFFFF"),
    IVORY("Ivory", "#FFFDF7"),
    CREAM("Cream", "#FFFFF0"),
    SOFT_BLUE("Soft Blue", "#F0F4F8"),
    LIGHT_GRAY("Light Gray", "#F8F9FA"),
    PARCHMENT("Parchment", "#F5EFE0"),
    DARK("Dark / Night", "#1E1E1E")
}

/**
 * Page number formatting style.
 */
enum class PageNumberFormat(val displayName: String) {
    ARABIC_NUMERALS("1, 2, 3..."),
    ROMAN_LOWER("i, ii, iii..."),
    ROMAN_UPPER("I, II, III..."),
    LETTERS("A, B, C...")
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
    val footerText: String = "",
    val headerAlignment: TextAlignment = TextAlignment.START,
    val footerAlignment: TextAlignment = TextAlignment.CENTER,
    val pageBackgroundHex: String = "#FFFFFF",
    val borderStyle: PageBorderStyle = PageBorderStyle.NONE,
    val borderColorHex: String = "#CBD5E1",
    val borderWidthPt: Float = 1f,
    val pageNumberFormat: PageNumberFormat = PageNumberFormat.ARABIC_NUMERALS,
    val pageNumberStart: Int = 1,
    val differentFirstPage: Boolean = false
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
     * Formatted dimension summary (e.g. "210 × 297 mm • 8.3 × 11.7 in").
     */
    val formattedDimensions: String
        get() = pageSize.fullDimensionsText(orientation)

    /**
     * Short paper size and dimension label (e.g. "A4 (210 × 297 mm)").
     */
    val shortDimensionsLabel: String
        get() = "${pageSize.name} (${pageSize.formattedDimensionsMm(orientation)})"

    /**
     * Human-readable orientation string (e.g. "Portrait" / "Landscape").
     */
    val orientationLabel: String
        get() = orientation.name.lowercase().replaceFirstChar { it.uppercase() }

    /**
     * Comprehensive layout summary.
     */
    val fullSpecificationSummary: String
        get() = "${pageSize.displayName.substringBefore(" (")} • $formattedDimensions • $orientationLabel"

    /**
     * Margins in PostScript points (72 DPI).
     */
    val topMarginPt: Float get() = (topMarginMm / 25.4f) * 72f
    val bottomMarginPt: Float get() = (bottomMarginMm / 25.4f) * 72f
    val leftMarginPt: Float get() = (leftMarginMm / 25.4f) * 72f
    val rightMarginPt: Float get() = (rightMarginMm / 25.4f) * 72f

    /**
     * Printable area in points.
     */
    val printableWidthPt: Float
        get() = (pageWidthPt - leftMarginPt - rightMarginPt).coerceAtLeast(72f)

    val printableHeightPt: Float
        get() {
            val headerReserved = if (showHeader && headerText.isNotBlank()) 24f else 0f
            val footerReserved = if (showFooter && footerText.isNotBlank()) 24f else 0f
            return (pageHeightPt - topMarginPt - bottomMarginPt - headerReserved - footerReserved).coerceAtLeast(100f)
        }

    /**
     * Formats a page number integer into the configured format (Arabic, Roman, Letters).
     */
    fun formatPageNumber(pageNumber: Int): String {
        val num = pageNumber + (pageNumberStart - 1)
        if (num <= 0) return num.toString()
        return when (pageNumberFormat) {
            PageNumberFormat.ARABIC_NUMERALS -> num.toString()
            PageNumberFormat.ROMAN_LOWER -> toRomanNumeral(num).lowercase()
            PageNumberFormat.ROMAN_UPPER -> toRomanNumeral(num)
            PageNumberFormat.LETTERS -> toAlphabetLetter(num)
        }
    }

    private fun toRomanNumeral(number: Int): String {
        if (number <= 0) return number.toString()
        val values = intArrayOf(1000, 900, 500, 400, 100, 90, 50, 40, 10, 9, 5, 4, 1)
        val numerals = arrayOf("M", "CM", "D", "CD", "C", "XC", "L", "XL", "X", "IX", "V", "IV", "I")
        val result = StringBuilder()
        var rem = number
        for (i in values.indices) {
            while (rem >= values[i]) {
                result.append(numerals[i])
                rem -= values[i]
            }
        }
        return result.toString()
    }

    private fun toAlphabetLetter(number: Int): String {
        if (number <= 0) return number.toString()
        val sb = StringBuilder()
        var n = number
        while (n > 0) {
            n--
            sb.append(('A'.code + (n % 26)).toChar())
            n /= 26
        }
        return sb.reverse().toString()
    }
}
