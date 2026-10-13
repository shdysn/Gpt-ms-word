package com.example.core.typography

import java.text.Bidi

/**
 * Unicode Bidirectional (BiDi) text utilities for Urdu, Arabic-script, and English mixed content.
 * Follows Unicode Standard Annex #9 (UAX #9) without visual string reversal hacks.
 */
object BidiUtils {

    /**
     * Set of Unicode blocks characteristic of Urdu and Arabic script:
     * - Arabic (0600–06FF)
     * - Arabic Supplement (0750–077F)
     * - Arabic Extended-A (08A0–08FF)
     * - Arabic Presentation Forms-A (FB50–FDFF)
     * - Arabic Presentation Forms-B (FE70–FEFF)
     * - Hebrew (0590–05FF)
     */
    fun isRtlChar(ch: Char): Boolean {
        val directionality = Character.getDirectionality(ch)
        return directionality == Character.DIRECTIONALITY_RIGHT_TO_LEFT ||
            directionality == Character.DIRECTIONALITY_RIGHT_TO_LEFT_ARABIC
    }

    /**
     * Checks if the given text contains any Right-to-Left (Urdu, Arabic, Persian) characters.
     */
    fun containsRtl(text: String): Boolean {
        for (i in 0 until text.length) {
            if (isRtlChar(text[i])) return true
        }
        return false
    }

    /**
     * Detects if the paragraph's primary script direction is Right-to-Left.
     * Uses first-strong directional character heuristic with fallback to overall character counts.
     */
    fun detectParagraphIsRtl(text: String): Boolean {
        if (text.isBlank()) return false

        // Check first strong directional character
        for (i in 0 until text.length) {
            val ch = text[i]
            val dir = Character.getDirectionality(ch)
            if (dir == Character.DIRECTIONALITY_RIGHT_TO_LEFT ||
                dir == Character.DIRECTIONALITY_RIGHT_TO_LEFT_ARABIC
            ) {
                return true
            }
            if (dir == Character.DIRECTIONALITY_LEFT_TO_RIGHT) {
                return false
            }
        }

        // Fallback: ratio count
        var rtlCount = 0
        var ltrCount = 0
        for (i in 0 until text.length) {
            val ch = text[i]
            val dir = Character.getDirectionality(ch)
            when (dir) {
                Character.DIRECTIONALITY_RIGHT_TO_LEFT,
                Character.DIRECTIONALITY_RIGHT_TO_LEFT_ARABIC -> rtlCount++
                Character.DIRECTIONALITY_LEFT_TO_RIGHT -> ltrCount++
            }
        }
        return rtlCount > ltrCount
    }

    /**
     * Converts ASCII digits in an integer to Eastern Arabic-Indic / Urdu digits (۰-۹).
     */
    fun toUrduNumerals(number: Int): String {
        val urduDigits = charArrayOf('۰', '۱', '۲', '۳', '۴', '۵', '۶', '۷', '۸', '۹')
        val str = number.toString()
        val sb = java.lang.StringBuilder(str.length)
        for (ch in str) {
            if (ch in '0'..'9') {
                sb.append(urduDigits[ch - '0'])
            } else {
                sb.append(ch)
            }
        }
        return sb.toString()
    }

    /**
     * Formats list numbering for a given item index:
     * Supports hierarchical multi-level outlines according to indentationLevel (1., a), i)...)
     * In RTL mode, supports traditional Urdu/Arabic punctuation and numerals.
     */
    fun formatListNumber(
        number: Int,
        isRtl: Boolean,
        useUrduNumerals: Boolean = false,
        indentationLevel: Int = 0
    ): String {
        val safeIndent = indentationLevel.coerceAtLeast(0)
        return if (isRtl) {
            val numStr = if (useUrduNumerals) toUrduNumerals(number) else number.toString()
            when (safeIndent % 3) {
                1 -> "(${numStr})۔ "
                2 -> "• "
                else -> "$numStr۔ "
            }
        } else {
            when (safeIndent % 3) {
                1 -> {
                    val char = ('a'.code + ((number - 1) % 26)).toChar()
                    "$char) "
                }
                2 -> {
                    val roman = toRoman(number)
                    "$roman) "
                }
                else -> "$number. "
            }
        }
    }

    private fun toRoman(n: Int): String {
        val num = n.coerceIn(1, 50)
        val romanMap = listOf(
            10 to "x", 9 to "ix", 5 to "v", 4 to "iv", 1 to "i"
        )
        val sb = StringBuilder()
        var remaining = num
        for ((value, sym) in romanMap) {
            while (remaining >= value) {
                sb.append(sym)
                remaining -= value
            }
        }
        return sb.toString()
    }
}

