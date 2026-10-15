package com.example.data.service.docx

import com.example.core.typography.BidiUtils
import com.example.data.model.TableBlock
import com.example.data.model.TextAlignment

/**
 * Generates OpenXML markup for tables and table cells in DOCX files.
 */
object DocxTableXmlBuilder {

    fun buildTableXml(block: TableBlock): String {
        val sb = StringBuilder()
        sb.append("<w:tbl>")
        val pctVal = (block.tableWidthPercent.coerceIn(20, 100) * 50)
        val tableAlign = when (block.alignment) {
            TextAlignment.START -> "left"
            TextAlignment.END -> "right"
            else -> "center"
        }
        val borderHex = block.borderColorHex.removePrefix("#")
        val bordersXml = if (block.hasBorders) {
            """  <w:tblBorders><w:top w:val="single" w:sz="4" w:space="0" w:color="$borderHex"/><w:left w:val="single" w:sz="4" w:space="0" w:color="$borderHex"/><w:bottom w:val="single" w:sz="4" w:space="0" w:color="$borderHex"/><w:right w:val="single" w:sz="4" w:space="0" w:color="$borderHex"/><w:insideH w:val="single" w:sz="4" w:space="0" w:color="E2E8F0"/><w:insideV w:val="single" w:sz="4" w:space="0" w:color="E2E8F0"/></w:tblBorders>"""
        } else {
            """  <w:tblBorders><w:top w:val="none"/><w:left w:val="none"/><w:bottom w:val="none"/><w:right w:val="none"/><w:insideH w:val="none"/><w:insideV w:val="none"/></w:tblBorders>"""
        }

        sb.append("""<w:tblPr><w:tblW w:w="$pctVal" w:type="pct"/><w:jc w:val="$tableAlign"/>$bordersXml</w:tblPr>""")

        val colWeights = if (block.columnWidthWeights.size == block.columnCount && block.columnWidthWeights.sum() > 0f) {
            val total = block.columnWidthWeights.sum()
            block.columnWidthWeights.map { it / total }
        } else {
            List(block.columnCount) { 1f / block.columnCount.toFloat() }
        }
        val totalTableTwips = (9000 * (block.tableWidthPercent.coerceIn(20, 100) / 100f)).toInt()

        sb.append("<w:tblGrid>")
        for (w in colWeights) {
            val colTwips = (totalTableTwips * w).toInt().coerceAtLeast(720)
            sb.append("""<w:gridCol w:w="$colTwips"/>""")
        }
        sb.append("</w:tblGrid>")

        for (r in 0 until block.rowCount) {
            val row = block.cells.getOrNull(r) ?: emptyList()
            sb.append("<w:tr>")
            for (c in 0 until block.columnCount) {
                val cell = row.getOrNull(c)
                val colTwips = (totalTableTwips * colWeights.getOrElse(c) { 1f / block.columnCount }).toInt().coerceAtLeast(720)
                sb.append("<w:tc><w:tcPr>")
                sb.append("""<w:tcW w:w="$colTwips" w:type="dxa"/>""")
                if (cell?.backgroundColorHex != null) {
                    val cleanHex = cell.backgroundColorHex.removePrefix("#")
                    sb.append("""<w:shd w:val="clear" w:color="auto" w:fill="$cleanHex"/>""")
                }
                sb.append("</w:tcPr>")

                val cellText = cell?.text ?: ""
                val isCellBold = cell?.isBold ?: (r == 0)
                val isRtl = BidiUtils.detectParagraphIsRtl(cellText)
                val cellAlign = when (cell?.alignment ?: TextAlignment.START) {
                    TextAlignment.CENTER -> "center"
                    TextAlignment.END -> if (isRtl) "left" else "right"
                    TextAlignment.JUSTIFY -> "both"
                    TextAlignment.START -> if (isRtl) "right" else "left"
                }

                sb.append("""<w:p><w:pPr><w:jc w:val="$cellAlign"/>""")
                if (isRtl) sb.append("<w:bidi/>")
                sb.append("</w:pPr><w:r><w:rPr>")
                if (isCellBold) sb.append("<w:b/>")
                if (cell?.isItalic == true) sb.append("<w:i/>")
                if (isRtl) sb.append("<w:rtl/>")
                sb.append("</w:rPr>")
                sb.append("""<w:t xml:space="preserve">${DocxXmlBuilders.escapeXml(cellText)}</w:t></w:r></w:p></w:tc>""")
            }
            sb.append("</w:tr>")
        }

        sb.append("</w:tbl>")
        return sb.toString()
    }
}
