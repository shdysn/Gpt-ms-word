package com.example.data.service.docx

import com.example.core.layout.MarginPreset
import com.example.core.layout.PageBorderStyle
import com.example.core.layout.PageLayoutConfig
import com.example.core.layout.PageOrientation
import com.example.core.layout.PageSize
import org.w3c.dom.Element

/**
 * Parses WordprocessingML `w:sectPr` XML elements into native PageLayoutConfig representations.
 */
object DocxSectionParser {

    fun parseSectionProperties(sectPr: Element, currentConfig: PageLayoutConfig): PageLayoutConfig {
        var pageSize = currentConfig.pageSize
        var orientation = currentConfig.orientation
        var topMm: Float? = null
        var bottomMm: Float? = null
        var leftMm: Float? = null
        var rightMm: Float? = null

        val pgSz = sectPr.getElementsByTagName("w:pgSz").item(0) as? Element
        if (pgSz != null) {
            val w = pgSz.getAttribute("w:w").toIntOrNull() ?: 11906
            val h = pgSz.getAttribute("w:h").toIntOrNull() ?: 16838
            val orient = pgSz.getAttribute("w:orient")
            orientation = if (orient == "landscape") PageOrientation.LANDSCAPE else PageOrientation.PORTRAIT

            val effectiveW = if (orientation == PageOrientation.LANDSCAPE) h else w
            val effectiveH = if (orientation == PageOrientation.LANDSCAPE) w else h
            pageSize = when {
                effectiveW in 11800..12050 -> PageSize.A4
                effectiveW in 12100..12350 -> if (effectiveH > 18000) PageSize.LEGAL else PageSize.LETTER
                effectiveW in 8200..8500 -> PageSize.A5
                effectiveW in 10300..10600 -> PageSize.EXECUTIVE
                else -> PageSize.A4
            }
        }

        val pgMar = sectPr.getElementsByTagName("w:pgMar").item(0) as? Element
        if (pgMar != null) {
            pgMar.getAttribute("w:top").toIntOrNull()?.let { topMm = (it / 1440f) * 25.4f }
            pgMar.getAttribute("w:bottom").toIntOrNull()?.let { bottomMm = (it / 1440f) * 25.4f }
            pgMar.getAttribute("w:left").toIntOrNull()?.let { leftMm = (it / 1440f) * 25.4f }
            pgMar.getAttribute("w:right").toIntOrNull()?.let { rightMm = (it / 1440f) * 25.4f }
        }

        var borderStyle = currentConfig.borderStyle
        var borderColor = currentConfig.borderColorHex
        var borderWidthPt = currentConfig.borderWidthPt
        val pgBorders = sectPr.getElementsByTagName("w:pgBorders").item(0) as? Element
        if (pgBorders != null) {
            val topBorder = pgBorders.getElementsByTagName("w:top").item(0) as? Element
            if (topBorder != null) {
                val borderVal = topBorder.getAttribute("w:val")
                val colorVal = topBorder.getAttribute("w:color")
                if (colorVal != null && colorVal != "auto" && colorVal.isNotBlank()) {
                    borderColor = "#$colorVal"
                }
                val szVal = topBorder.getAttribute("w:sz").toIntOrNull()
                if (szVal != null && szVal > 0) {
                    borderWidthPt = szVal / 8f
                }
                borderStyle = when (borderVal) {
                    "single" -> if (borderWidthPt >= 2f) PageBorderStyle.SINGLE_THICK else PageBorderStyle.SINGLE_THIN
                    "thick" -> PageBorderStyle.SINGLE_THICK
                    "double" -> PageBorderStyle.DOUBLE
                    "dashed" -> PageBorderStyle.DASHED
                    "none" -> PageBorderStyle.NONE
                    else -> PageBorderStyle.SINGLE_THIN
                }
            }
        }

        val colsEl = sectPr.getElementsByTagName("w:cols").item(0) as? Element
        val parsedColCount = colsEl?.getAttribute("w:num")?.toIntOrNull() ?: 1

        return currentConfig.copy(
            pageSize = pageSize,
            orientation = orientation,
            marginPreset = MarginPreset.CUSTOM,
            customTopMarginMm = topMm,
            customBottomMarginMm = bottomMm,
            customLeftMarginMm = leftMm,
            customRightMarginMm = rightMm,
            borderStyle = borderStyle,
            borderColorHex = borderColor,
            borderWidthPt = borderWidthPt,
            columnCount = parsedColCount.coerceIn(1, 2)
        )
    }
}
