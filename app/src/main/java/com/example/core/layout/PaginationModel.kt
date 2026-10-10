package com.example.core.layout

/**
 * Model representing page breakdown and pagination calculations.
 */
data class PageSlice(
    val pageNumber: Int,
    val totalPages: Int,
    val startBlockIndex: Int,
    val endBlockIndex: Int,
    val pageLayout: PageLayoutConfig
)

/**
 * Page number formatting style.
 */
enum class PageNumberFormat {
    ARABIC_NUMERALS, // 1, 2, 3...
    ROMAN_LOWER,     // i, ii, iii...
    ROMAN_UPPER,     // I, II, III...
    LETTERS          // A, B, C...
}

/**
 * Header and footer layout configuration.
 */
data class HeaderFooterModel(
    val leftContent: String = "",
    val centerContent: String = "",
    val rightContent: String = "",
    val isDifferentFirstPage: Boolean = false,
    val isDifferentOddEven: Boolean = false,
    val pageNumberFormat: PageNumberFormat = PageNumberFormat.ARABIC_NUMERALS
)
