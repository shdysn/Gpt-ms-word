package com.example.data.service.docx

import com.example.data.model.TableCell
import com.example.data.model.TableBlock
import com.example.data.model.TextAlignment
import org.w3c.dom.Element

/**
 * Parses WordprocessingML `w:tbl` XML elements into native TableBlock representations.
 */
object DocxTableParser {

    fun parseTableElement(tblElement: Element): TableBlock {
        val rows = mutableListOf<List<TableCell>>()
        val trNodes = tblElement.getElementsByTagName("w:tr")

        val tblPr = tblElement.getElementsByTagName("w:tblPr").item(0) as? Element
        val tblJc = (tblPr?.getElementsByTagName("w:jc")?.item(0) as? Element)?.getAttribute("w:val")
        val tableAlignment = when (tblJc) {
            "left" -> TextAlignment.START
            "right" -> TextAlignment.END
            else -> TextAlignment.CENTER
        }
        val tblBorders = tblPr?.getElementsByTagName("w:tblBorders")?.item(0) as? Element
        val hasBorders = if (tblBorders != null) {
            val topVal = (tblBorders.getElementsByTagName("w:top").item(0) as? Element)?.getAttribute("w:val")
            topVal != "none"
        } else true

        // Extract column widths from w:tblGrid
        val tblGrid = tblElement.getElementsByTagName("w:tblGrid").item(0) as? Element
        val gridCols = tblGrid?.getElementsByTagName("w:gridCol")
        val parsedColWidths = mutableListOf<Float>()
        if (gridCols != null && gridCols.length > 0) {
            for (g in 0 until gridCols.length) {
                val colEl = gridCols.item(g) as? Element
                val twips = colEl?.getAttribute("w:w")?.toFloatOrNull() ?: 1440f
                parsedColWidths.add(twips / 20f)
            }
        }

        var maxCols = 0
        for (r in 0 until trNodes.length) {
            val tr = trNodes.item(r) as? Element ?: continue
            val tcNodes = tr.getElementsByTagName("w:tc")
            val cellList = mutableListOf<TableCell>()

            for (c in 0 until tcNodes.length) {
                val tc = tcNodes.item(c) as? Element ?: continue
                val textNodes = tc.getElementsByTagName("w:t")
                val textBuilder = StringBuilder()
                for (t in 0 until textNodes.length) {
                    textBuilder.append(textNodes.item(t).textContent)
                }
                val cellText = textBuilder.toString()
                val isBold = tc.getElementsByTagName("w:b").length > 0
                val isItalic = tc.getElementsByTagName("w:i").length > 0
                val jcNode = tc.getElementsByTagName("w:jc").item(0) as? Element
                val cellAlign = when (jcNode?.getAttribute("w:val")) {
                    "center" -> TextAlignment.CENTER
                    "right" -> TextAlignment.END
                    "both" -> TextAlignment.JUSTIFY
                    else -> TextAlignment.START
                }

                var bgHex: String? = null
                val shd = tc.getElementsByTagName("w:shd").item(0) as? Element
                shd?.getAttribute("w:fill")?.let { fill ->
                    if (fill != "auto" && fill.isNotBlank()) {
                        bgHex = "#$fill"
                    }
                }

                val tcPr = tc.getElementsByTagName("w:tcPr").item(0) as? Element
                val tcW = tcPr?.getElementsByTagName("w:tcW")?.item(0) as? Element
                val cellW = tcW?.getAttribute("w:w")?.toFloatOrNull()?.let { it / 20f }

                cellList.add(
                    TableCell(
                        text = cellText,
                        isBold = isBold,
                        isItalic = isItalic,
                        backgroundColorHex = bgHex,
                        alignment = cellAlign,
                        widthPt = cellW
                    )
                )
            }
            if (cellList.size > maxCols) maxCols = cellList.size
            rows.add(cellList)
        }

        val rowCount = rows.size.coerceAtLeast(1)
        val colCount = maxCols.coerceAtLeast(1)

        val columnWidthWeights = if (parsedColWidths.size == colCount && parsedColWidths.sum() > 0f) {
            val total = parsedColWidths.sum()
            parsedColWidths.map { it / total }
        } else {
            List(colCount) { 1f / colCount.toFloat() }
        }

        val normalizedRows = rows.map { row ->
            if (row.size < colCount) {
                row + List(colCount - row.size) { TableCell() }
            } else row
        }

        return TableBlock(
            rowCount = rowCount,
            columnCount = colCount,
            cells = normalizedRows,
            hasBorders = hasBorders,
            alignment = tableAlignment,
            columnWidthWeights = columnWidthWeights
        )
    }
}
