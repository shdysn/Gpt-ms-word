package com.example.core.typography

import com.example.data.model.HeadingStyle

/**
 * Provides responsive, screen-adaptive typography metrics tailored for
 * different sizes of mobiles (compact, standard, phablet/large) and tablets,
 * with specialized optical sizing for Urdu Nastaliq and Arabic scripts.
 */
object AdaptiveFontManager {

    enum class MobileScreenTier {
        COMPACT,  // < 360dp (compact phones, split screen)
        STANDARD, // 360dp - 411dp (standard mobile devices)
        LARGE,    // 412dp - 599dp (phablets, large mobile screens)
        TABLET    // >= 600dp (foldables unfolded, tablets)
    }

    fun getScreenTier(widthDp: Float): MobileScreenTier {
        return when {
            widthDp <= 0f -> MobileScreenTier.STANDARD
            widthDp < 360f -> MobileScreenTier.COMPACT
            widthDp < 412f -> MobileScreenTier.STANDARD
            widthDp < 600f -> MobileScreenTier.LARGE
            else -> MobileScreenTier.TABLET
        }
    }

    /**
     * Scaling multiplier based on mobile screen width so typography remains
     * perfectly balanced and legible across diverse device form factors.
     */
    fun getMobileScaleFactor(widthDp: Float): Float {
        return when (getScreenTier(widthDp)) {
            MobileScreenTier.COMPACT -> 0.94f
            MobileScreenTier.STANDARD -> 1.0f
            MobileScreenTier.LARGE -> 1.06f
            MobileScreenTier.TABLET -> 1.12f
        }
    }

    /**
     * Resolves the unscaled base font size (in sp) for a heading style and script direction,
     * calibrated for different mobile screen sizes.
     *
     * Urdu / Nastaliq optical size requires ~25-35% larger nominal point size than Latin
     * to achieve equivalent visual clarity and legibility, matching modern mobile publishing standards.
     */
    fun getAdaptiveFontSizeSp(
        headingStyle: HeadingStyle,
        isRtl: Boolean,
        containerWidthDp: Float = 390f
    ): Float {
        val factor = getMobileScaleFactor(containerWidthDp)
        val rawSp = if (isRtl) {
            when (headingStyle) {
                HeadingStyle.TITLE -> 28f
                HeadingStyle.SUBTITLE -> 21f
                HeadingStyle.HEADING_1 -> 25f
                HeadingStyle.HEADING_2 -> 22f
                HeadingStyle.HEADING_3 -> 20f
                HeadingStyle.QUOTE -> 18.5f
                HeadingStyle.NORMAL -> 19f
            }
        } else {
            when (headingStyle) {
                HeadingStyle.TITLE -> 25f
                HeadingStyle.SUBTITLE -> 18f
                HeadingStyle.HEADING_1 -> 22f
                HeadingStyle.HEADING_2 -> 18f
                HeadingStyle.HEADING_3 -> 16f
                HeadingStyle.QUOTE -> 15f
                HeadingStyle.NORMAL -> 15.5f
            }
        }
        return rawSp * factor
    }

    /**
     * Computes line height ensuring adequate vertical space for Urdu Nastaliq
     * ascenders, descenders, and aerab (diacritics/nuktas) without line collisions.
     */
    fun getAdaptiveLineHeightSp(
        baseFontSizeSp: Float,
        isRtl: Boolean,
        lineSpacingMultiplier: Float = 1.0f
    ): Float {
        val spacing = lineSpacingMultiplier.coerceAtLeast(1.0f)
        return if (isRtl) {
            baseFontSizeSp * spacing * 2.15f
        } else {
            baseFontSizeSp * spacing * 1.35f
        }
    }

    /**
     * Scales an inline span's explicit font size adaptively for mobile screens and script.
     */
    fun getAdaptiveSpanFontSizeSp(
        rawSizeSp: Float,
        isRtl: Boolean,
        containerWidthDp: Float = 390f
    ): Float {
        val factor = getMobileScaleFactor(containerWidthDp)
        val adjusted = if (isRtl && rawSizeSp < 17f) {
            (rawSizeSp * 1.25f).coerceAtLeast(17f)
        } else {
            rawSizeSp
        }
        return adjusted * factor
    }
}
