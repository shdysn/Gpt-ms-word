package com.example.core.typography

import android.content.Context
import android.graphics.Typeface
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.core.content.res.ResourcesCompat
import com.example.R

/**
 * Manages font families, bundled Urdu/Arabic typefaces, and fallbacks
 * across Compose UI, Canvas rendering, and PDF export.
 */
object DocumentFontManager {

    val NotoNastaliqUrduFontFamily = FontFamily(
        Font(R.font.noto_nastaliq_urdu, FontWeight.Normal)
    )

    val NotoSansArabicFontFamily = FontFamily(
        Font(R.font.noto_sans_arabic, FontWeight.Normal)
    )

    /**
     * Font descriptors for ribbon selection.
     */
    data class FontOption(
        val id: String,
        val displayName: String,
        val isUrduOptimized: Boolean = false,
        val isBundled: Boolean = false
    )

    val AVAILABLE_FONTS = listOf(
        FontOption("Noto Nastaliq Urdu", "اردو نستعلیق (Noto Nastaliq Urdu - Bundled)", isUrduOptimized = true, isBundled = true),
        FontOption("Noto Sans Arabic", "عربی / اردو نسخ (Noto Sans Arabic - Bundled)", isUrduOptimized = true, isBundled = true),
        FontOption("Calibri", "Calibri (Word Standard)", isUrduOptimized = false),
        FontOption("Arial", "Arial (Clean Sans)", isUrduOptimized = false),
        FontOption("Times New Roman", "Times New Roman (Formal Serif)", isUrduOptimized = false),
        FontOption("Georgia", "Georgia (Editorial Serif)", isUrduOptimized = false),
        FontOption("Courier New", "Courier New (Monospace)", isUrduOptimized = false),
        FontOption("System Default", "System Default Font", isUrduOptimized = false)
    )

    /**
     * Returns the appropriate Compose [FontFamily] with automatic fallback for Urdu/Arabic content.
     */
    fun getFontFamily(fontName: String?, isRtl: Boolean): FontFamily {
        return when (fontName?.lowercase()) {
            "noto nastaliq urdu", "nastaliq", "jameel noori nastaliq" -> NotoNastaliqUrduFontFamily
            "noto sans arabic", "arabic sans", "naskh" -> NotoSansArabicFontFamily
            "arial", "roboto", "sans-serif" -> FontFamily.SansSerif
            "times new roman", "georgia", "serif" -> FontFamily.Serif
            "courier new", "monospace" -> FontFamily.Monospace
            "system default" -> FontFamily.Default
            else -> {
                if (isRtl) {
                    NotoNastaliqUrduFontFamily
                } else {
                    FontFamily.Default
                }
            }
        }
    }

    // Typeface caching for Canvas / PDF generation
    private val typefaceCache = mutableMapOf<String, Typeface>()

    /**
     * Resolves an Android native [Typeface] for PDF generation and Canvas rendering.
     * Uses bundled Noto Nastaliq Urdu and Noto Sans Arabic fonts when context is available.
     */
    fun getTypeface(
        context: Context?,
        fontName: String?,
        isRtl: Boolean,
        isBold: Boolean = false,
        isItalic: Boolean = false
    ): Typeface {
        val style = when {
            isBold && isItalic -> Typeface.BOLD_ITALIC
            isBold -> Typeface.BOLD
            isItalic -> Typeface.ITALIC
            else -> Typeface.NORMAL
        }

        val cacheKey = "${fontName ?: if (isRtl) "urdu_default" else "default"}_${style}"
        typefaceCache[cacheKey]?.let { return it }

        val resolvedTypeface = try {
            val isUrduNastaliq = fontName?.contains("nastaliq", ignoreCase = true) == true ||
                (isRtl && (fontName == null || fontName.isBlank() || fontName.equals("default", ignoreCase = true)))
            val isArabicSans = fontName?.contains("sans arabic", ignoreCase = true) == true

            if (context != null) {
                if (isUrduNastaliq) {
                    val base = ResourcesCompat.getFont(context, R.font.noto_nastaliq_urdu)
                    if (base != null) {
                        Typeface.create(base, style)
                    } else {
                        Typeface.create(Typeface.SERIF, style)
                    }
                } else if (isArabicSans) {
                    val base = ResourcesCompat.getFont(context, R.font.noto_sans_arabic)
                    if (base != null) {
                        Typeface.create(base, style)
                    } else {
                        Typeface.create(Typeface.SANS_SERIF, style)
                    }
                } else {
                    when (fontName?.lowercase()) {
                        "arial", "roboto" -> Typeface.create(Typeface.SANS_SERIF, style)
                        "times new roman", "georgia" -> Typeface.create(Typeface.SERIF, style)
                        "courier new" -> Typeface.create(Typeface.MONOSPACE, style)
                        else -> Typeface.create(Typeface.DEFAULT, style)
                    }
                }
            } else {
                when {
                    isRtl -> Typeface.create(Typeface.SERIF, style)
                    fontName?.contains("serif", ignoreCase = true) == true -> Typeface.create(Typeface.SERIF, style)
                    fontName?.contains("courier", ignoreCase = true) == true -> Typeface.create(Typeface.MONOSPACE, style)
                    else -> Typeface.create(Typeface.DEFAULT, style)
                }
            }
        } catch (e: Throwable) {
            Typeface.create(Typeface.DEFAULT, style)
        }

        typefaceCache[cacheKey] = resolvedTypeface
        return resolvedTypeface
    }
}
