package com.example.data.storage

import com.example.data.model.BlockElement
import com.example.data.model.CellVerticalAlignment
import com.example.data.model.ColumnBreakBlock
import com.example.data.model.DividerBlock
import com.example.data.model.DrawingBlock
import com.example.data.model.DrawingStroke
import com.example.data.model.FootnoteBlock
import com.example.data.model.ImageBlock
import com.example.data.model.PageBreakBlock
import com.example.data.model.ParagraphBlock
import com.example.data.model.SectionBreakBlock
import com.example.data.model.ShapeBlock
import com.example.data.model.SignatureBlock
import com.example.data.model.TableCell
import com.example.data.model.TableBlock
import com.example.data.model.TableOfContentsBlock
import com.example.data.model.TextBoxBlock
import com.example.data.model.TextAlignment
import org.json.JSONObject
import java.util.UUID

/**
 * Deserializes JSON objects into individual BlockElement instances.
 */
object DocumentBlockJsonDeserializer {

    fun deserializeBlock(bObj: JSONObject): BlockElement {
        val type = bObj.optString("type", "paragraph")
        val blockId = bObj.optString("id", "").ifBlank { UUID.randomUUID().toString() }
        return when (type) {
            "paragraph" -> DocumentParagraphJsonDeserializer.deserializeParagraph(bObj, blockId)
            "table" -> deserializeTable(bObj, blockId)
            "image" -> deserializeImage(bObj, blockId)
            "footnote" -> FootnoteBlock(
                id = blockId,
                noteNumber = bObj.optInt("noteNumber", 1),
                referenceText = bObj.optString("referenceText", ""),
                noteContent = bObj.optString("noteContent", "")
            )
            "table_of_contents" -> TableOfContentsBlock(
                id = blockId,
                title = bObj.optString("title", "Table of Contents")
            )
            "signature" -> {
                val sigAlign = try {
                    TextAlignment.valueOf(bObj.optString("alignment", "START"))
                } catch (_: Exception) { TextAlignment.START }
                SignatureBlock(
                    id = blockId,
                    svgPoints = bObj.optString("svgPoints", ""),
                    signerName = bObj.optString("signerName", ""),
                    signerTitle = bObj.optString("signerTitle", "Authorized Signatory"),
                    signedDate = bObj.optString("signedDate", ""),
                    strokeColorHex = bObj.optString("strokeColorHex", "#0F172A"),
                    alignment = sigAlign
                )
            }
            "shape" -> ShapeBlock(
                id = blockId,
                shapeType = bObj.optString("shapeType", "NOTE"),
                title = bObj.optString("title", "Important Note"),
                content = bObj.optString("content", ""),
                backgroundColorHex = bObj.optString("backgroundColorHex", "#EFF6FF"),
                borderColorHex = bObj.optString("borderColorHex", "#3B82F6"),
                strokeWidthPt = bObj.optDouble("strokeWidthPt", 1.5).toFloat(),
                rotationDeg = bObj.optDouble("rotationDeg", 0.0).toFloat(),
                textColorHex = bObj.optString("textColorHex", "#1E293B"),
                widthDp = bObj.optInt("widthDp", 220),
                heightDp = bObj.optInt("heightDp", 110)
            )
            "drawing" -> deserializeDrawing(bObj, blockId)
            "textbox" -> TextBoxBlock(
                id = blockId,
                text = bObj.optString("text", ""),
                widthDp = bObj.optInt("widthDp", 180),
                heightDp = bObj.optInt("heightDp", 100),
                backgroundColorHex = bObj.optString("backgroundColorHex", "#FFFFFF"),
                borderColorHex = bObj.optString("borderColorHex", "#2563EB"),
                borderWidthPt = bObj.optDouble("borderWidthPt", 1.5).toFloat(),
                rotationDeg = bObj.optDouble("rotationDeg", 0.0).toFloat(),
                textColorHex = bObj.optString("textColorHex", "#1E293B"),
                offsetXPt = bObj.optDouble("offsetXPt", 0.0).toFloat(),
                offsetYPt = bObj.optDouble("offsetYPt", 0.0).toFloat()
            )
            "divider" -> DividerBlock(id = blockId)
            "page_break" -> PageBreakBlock(id = blockId)
            "column_break" -> ColumnBreakBlock(id = blockId)
            "section_break" -> SectionBreakBlock(
                id = blockId,
                sectionName = bObj.optString("sectionName", "Section"),
                orientation = bObj.optString("orientation", "PORTRAIT"),
                isNextPage = bObj.optBoolean("isNextPage", true),
                headerText = bObj.optString("headerText", ""),
                footerText = bObj.optString("footerText", "")
            )
            else -> ParagraphBlock(id = blockId, text = bObj.optString("text", ""))
        }
    }

    private fun deserializeTable(bObj: JSONObject, blockId: String): TableBlock {
        val rowCount = bObj.optInt("rowCount", 2)
        val columnCount = bObj.optInt("columnCount", 2)
        val hasBorders = bObj.optBoolean("hasBorders", true)
        val borderColorHex = bObj.optString("borderColorHex", "#CBD5E1")
        val tableWidthPercent = bObj.optInt("tableWidthPercent", 100)
        val alignment = try {
            TextAlignment.valueOf(bObj.optString("alignment", "CENTER"))
        } catch (_: Exception) { TextAlignment.CENTER }
        val hasHeaderRow = bObj.optBoolean("hasHeaderRow", true)
        val isZebraStriped = bObj.optBoolean("isZebraStriped", false)

        val cells = mutableListOf<List<TableCell>>()
        val cellsArr = bObj.optJSONArray("cells")
        if (cellsArr != null) {
            for (r in 0 until cellsArr.length()) {
                val rowArr = cellsArr.getJSONArray(r)
                val row = mutableListOf<TableCell>()
                for (c in 0 until rowArr.length()) {
                    val cObj = rowArr.getJSONObject(c)
                    val cellAlign = try {
                        TextAlignment.valueOf(cObj.optString("alignment", "START"))
                    } catch (_: Exception) { TextAlignment.START }
                    val vAlign = try {
                        CellVerticalAlignment.valueOf(cObj.optString("vAlign", "TOP"))
                    } catch (_: Exception) { CellVerticalAlignment.TOP }
                    row.add(
                        TableCell(
                            text = cObj.optString("text", ""),
                            isBold = cObj.optBoolean("isBold", false),
                            isItalic = cObj.optBoolean("isItalic", false),
                            backgroundColorHex = if (cObj.has("bg") && !cObj.isNull("bg")) cObj.getString("bg") else null,
                            alignment = cellAlign,
                            colSpan = cObj.optInt("colSpan", 1),
                            rowSpan = cObj.optInt("rowSpan", 1),
                            isMergedCovered = cObj.optBoolean("isMergedCovered", false),
                            verticalAlignment = vAlign,
                            borderTopWidthPt = if (cObj.has("bt")) cObj.getDouble("bt").toFloat() else null,
                            borderBottomWidthPt = if (cObj.has("bb")) cObj.getDouble("bb").toFloat() else null,
                            borderLeftWidthPt = if (cObj.has("bl")) cObj.getDouble("bl").toFloat() else null,
                            borderRightWidthPt = if (cObj.has("br")) cObj.getDouble("br").toFloat() else null
                        )
                    )
                }
                cells.add(row)
            }
        }
        return TableBlock(
            id = blockId,
            rowCount = rowCount,
            columnCount = columnCount,
            cells = cells,
            hasBorders = hasBorders,
            borderColorHex = borderColorHex,
            tableWidthPercent = tableWidthPercent,
            alignment = alignment,
            hasHeaderRow = hasHeaderRow,
            isZebraStriped = isZebraStriped
        )
    }

    private fun deserializeImage(bObj: JSONObject, blockId: String): ImageBlock {
        val imgAlign = try {
            TextAlignment.valueOf(bObj.optString("alignment", "CENTER"))
        } catch (_: Exception) { TextAlignment.CENTER }
        return ImageBlock(
            id = blockId,
            imageUri = bObj.optString("imageUri", ""),
            caption = bObj.optString("caption", ""),
            widthDp = bObj.optInt("widthDp", 240),
            heightDp = bObj.optInt("heightDp", 180),
            alignment = imgAlign,
            aspectRatio = bObj.optDouble("aspectRatio", 1.333).toFloat(),
            filterMode = bObj.optString("filterMode", "NORMAL"),
            cornerRadiusDp = bObj.optInt("cornerRadiusDp", 8),
            borderThicknessDp = bObj.optInt("borderThicknessDp", 0),
            borderColorHex = bObj.optString("borderColorHex", "#CBD5E1"),
            rotationDegrees = bObj.optDouble("rotationDegrees", 0.0).toFloat(),
            wrapMode = bObj.optString("wrapMode", "IN_LINE"),
            offsetXPt = bObj.optDouble("offsetXPt", 0.0).toFloat(),
            offsetYPt = bObj.optDouble("offsetYPt", 0.0).toFloat()
        )
    }

    private fun deserializeDrawing(bObj: JSONObject, blockId: String): DrawingBlock {
        val strokesList = mutableListOf<DrawingStroke>()
        val strokesArr = bObj.optJSONArray("strokes")
        if (strokesArr != null) {
            for (si in 0 until strokesArr.length()) {
                val sObj = strokesArr.getJSONObject(si)
                val colorHex = sObj.optString("colorHex", "#000000")
                val strokeWidthPt = sObj.optDouble("strokeWidthPt", 2.0).toFloat()
                val isHighlighter = sObj.optBoolean("isHighlighter", false)
                val ptsStr = sObj.optString("points", "")
                val points = if (ptsStr.isNotBlank()) {
                    ptsStr.split(";").mapNotNull { p ->
                        val parts = p.split(",")
                        if (parts.size == 2) {
                            val x = parts[0].toFloatOrNull() ?: 0f
                            val y = parts[1].toFloatOrNull() ?: 0f
                            x to y
                        } else null
                    }
                } else emptyList()
                strokesList.add(DrawingStroke(points, colorHex, strokeWidthPt, isHighlighter))
            }
        }
        return DrawingBlock(
            id = blockId,
            strokes = strokesList,
            heightDp = bObj.optInt("heightDp", 180),
            backgroundColorHex = if (bObj.has("backgroundColorHex")) bObj.optString("backgroundColorHex") else null
        )
    }
}
