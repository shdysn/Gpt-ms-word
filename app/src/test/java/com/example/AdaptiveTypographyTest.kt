package com.example

import com.example.core.typography.AdaptiveFontManager
import com.example.data.model.HeadingStyle
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AdaptiveTypographyTest {

    @Test
    fun testScreenTierClassification() {
        assertEquals(AdaptiveFontManager.MobileScreenTier.COMPACT, AdaptiveFontManager.getScreenTier(320f))
        assertEquals(AdaptiveFontManager.MobileScreenTier.COMPACT, AdaptiveFontManager.getScreenTier(350f))
        assertEquals(AdaptiveFontManager.MobileScreenTier.STANDARD, AdaptiveFontManager.getScreenTier(360f))
        assertEquals(AdaptiveFontManager.MobileScreenTier.STANDARD, AdaptiveFontManager.getScreenTier(390f))
        assertEquals(AdaptiveFontManager.MobileScreenTier.STANDARD, AdaptiveFontManager.getScreenTier(411f))
        assertEquals(AdaptiveFontManager.MobileScreenTier.LARGE, AdaptiveFontManager.getScreenTier(412f))
        assertEquals(AdaptiveFontManager.MobileScreenTier.LARGE, AdaptiveFontManager.getScreenTier(540f))
        assertEquals(AdaptiveFontManager.MobileScreenTier.TABLET, AdaptiveFontManager.getScreenTier(600f))
        assertEquals(AdaptiveFontManager.MobileScreenTier.TABLET, AdaptiveFontManager.getScreenTier(800f))
    }

    @Test
    fun testUrduOpticalSizingLargerThanLatin() {
        // Urdu Nastaliq has a smaller optical x-height, so its nominal font size must be larger than Latin
        val urduNormal = AdaptiveFontManager.getAdaptiveFontSizeSp(HeadingStyle.NORMAL, isRtl = true, containerWidthDp = 390f)
        val latinNormal = AdaptiveFontManager.getAdaptiveFontSizeSp(HeadingStyle.NORMAL, isRtl = false, containerWidthDp = 390f)

        assertTrue("Urdu normal text must be larger than Latin normal text for readability", urduNormal > latinNormal)
        assertTrue("Urdu normal text on standard mobile should be >= 18sp", urduNormal >= 18f)

        val urduTitle = AdaptiveFontManager.getAdaptiveFontSizeSp(HeadingStyle.TITLE, isRtl = true, containerWidthDp = 390f)
        val latinTitle = AdaptiveFontManager.getAdaptiveFontSizeSp(HeadingStyle.TITLE, isRtl = false, containerWidthDp = 390f)
        assertTrue("Urdu title should be prominent", urduTitle >= latinTitle)
    }

    @Test
    fun testScalingAcrossDifferentSizesOfMobiles() {
        val compactUrdu = AdaptiveFontManager.getAdaptiveFontSizeSp(HeadingStyle.NORMAL, isRtl = true, containerWidthDp = 340f)
        val standardUrdu = AdaptiveFontManager.getAdaptiveFontSizeSp(HeadingStyle.NORMAL, isRtl = true, containerWidthDp = 390f)
        val largeUrdu = AdaptiveFontManager.getAdaptiveFontSizeSp(HeadingStyle.NORMAL, isRtl = true, containerWidthDp = 440f)

        assertTrue("Compact phone font size should be slightly scaled down to prevent premature wrapping", compactUrdu < standardUrdu)
        assertTrue("Large phone font size should be scaled up for comfortable reading", largeUrdu > standardUrdu)
    }

    @Test
    fun testLineHeightForUrduAerabAndLigatures() {
        val fontSize = 19f
        val urduLineHeight = AdaptiveFontManager.getAdaptiveLineHeightSp(fontSize, isRtl = true, lineSpacingMultiplier = 1.0f)
        val latinLineHeight = AdaptiveFontManager.getAdaptiveLineHeightSp(fontSize, isRtl = false, lineSpacingMultiplier = 1.0f)

        assertTrue("Urdu line height must accommodate tall Nastaliq ascenders and descenders", urduLineHeight >= fontSize * 2.0f)
        assertTrue("Urdu line height must exceed Latin line height", urduLineHeight > latinLineHeight)
    }

    @Test
    fun testSpanFontSizeOpticalBoostForUrdu() {
        // If an explicit small font size (e.g. 14pt) is set on Urdu, it should receive an optical boost
        val boostedUrdu = AdaptiveFontManager.getAdaptiveSpanFontSizeSp(14f, isRtl = true, containerWidthDp = 390f)
        val standardLatin = AdaptiveFontManager.getAdaptiveSpanFontSizeSp(14f, isRtl = false, containerWidthDp = 390f)

        assertTrue("Small span font sizes in Urdu must receive an optical legibility boost", boostedUrdu > standardLatin)
    }
}
