package com.example.data.service.docx

import com.example.core.layout.PageBorderStyle
import com.example.core.layout.PageOrientation
import com.example.core.layout.PageSize
import com.example.data.model.RichDocument

/**
 * Builds the `<w:sectPr>` section properties element for page margins, sizing, borders, headers and footers.
 */
object DocxSectionXmlBuilder {

    fun buildSectionPropertiesXml(document: RichDocument): String {
        val layout = document.layoutConfig
        val isLandscape = layout.orientation == PageOrientation.LANDSCAPE
        val (widthTwips, heightTwips) = when (layout.pageSize) {
            PageSize.A4 -> 11906 to 16838
            PageSize.LETTER -> 12240 to 15840
            PageSize.LEGAL -> 12240 to 20160
            PageSize.A5 -> 8390 to 11906
            PageSize.EXECUTIVE -> 10440 to 15120
        }
        val effWidth = if (isLandscape) heightTwips else widthTwips
        val effHeight = if (isLandscape) widthTwips else heightTwips
        val topTwips = ((layout.topMarginMm / 25.4f) * 1440f).toInt()
        val bottomTwips = ((layout.bottomMarginMm / 25.4f) * 1440f).toInt()
        val leftTwips = ((layout.leftMarginMm / 25.4f) * 1440f).toInt()
        val rightTwips = ((layout.rightMarginMm / 25.4f) * 1440f).toInt()

        val sb = StringBuilder()
        sb.append("<w:sectPr>")
        if (layout.showHeader && layout.headerText.isNotBlank()) sb.append("""<w:headerReference w:type="default" r:id="rIdHeader1"/>""")
        if (layout.showFooter && layout.footerText.isNotBlank()) sb.append("""<w:footerReference w:type="default" r:id="rIdFooter1"/>""")
        sb.append("""<w:pgSz w:w="$effWidth" w:h="$effHeight" ${if (isLandscape) """w:orient="landscape"""" else ""}/>""")
        sb.append("""<w:pgMar w:top="$topTwips" w:right="$rightTwips" w:bottom="$bottomTwips" w:left="$leftTwips" w:header="720" w:footer="720" w:gutter="0"/>""")
        if (layout.columnCount > 1) sb.append("""<w:cols w:num="${layout.columnCount}" w:space="720" w:equalWidth="1"/>""")

        if (layout.borderStyle != PageBorderStyle.NONE) {
            val borderVal = when (layout.borderStyle) {
                PageBorderStyle.SINGLE_THIN -> "single"
                PageBorderStyle.SINGLE_THICK -> "thick"
                PageBorderStyle.DOUBLE -> "double"
                PageBorderStyle.DASHED -> "dashed"
                PageBorderStyle.NONE -> "none"
            }
            val cleanBorderHex = layout.borderColorHex.removePrefix("#")
            val szVal = (layout.borderWidthPt * 8).toInt().coerceIn(2, 96)
            sb.append("""<w:pgBorders w:offsetFrom="page"><w:top w:val="$borderVal" w:sz="$szVal" w:space="24" w:color="$cleanBorderHex"/><w:left w:val="$borderVal" w:sz="$szVal" w:space="24" w:color="$cleanBorderHex"/><w:bottom w:val="$borderVal" w:sz="$szVal" w:space="24" w:color="$cleanBorderHex"/><w:right w:val="$borderVal" w:sz="$szVal" w:space="24" w:color="$cleanBorderHex"/></w:pgBorders>""")
        }
        sb.append("</w:sectPr>")
        return sb.toString()
    }
}
