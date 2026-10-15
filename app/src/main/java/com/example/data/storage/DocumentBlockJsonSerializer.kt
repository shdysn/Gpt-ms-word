package com.example.data.storage

import com.example.data.model.BlockElement
import com.example.data.model.ColumnBreakBlock
import com.example.data.model.DividerBlock
import com.example.data.model.DrawingBlock
import com.example.data.model.FootnoteBlock
import com.example.data.model.ImageBlock
import com.example.data.model.PageBreakBlock
import com.example.data.model.ParagraphBlock
import com.example.data.model.SectionBreakBlock
import com.example.data.model.ShapeBlock
import com.example.data.model.SignatureBlock
import com.example.data.model.TableBlock
import com.example.data.model.TableOfContentsBlock
import com.example.data.model.TextBoxBlock
import org.json.JSONArray
import org.json.JSONObject

/**
 * Handles JSON serialization of BlockElements into JSONObjects.
 */
object DocumentBlockJsonSerializer {

    fun serializeBlock(block: BlockElement): JSONObject {
        val blockObj = JSONObject()
        when (block) {
            is ParagraphBlock -> {
                blockObj.put("type", "paragraph")
                blockObj.put("id", block.id)
                blockObj.put("text", block.text)
                blockObj.put("alignment", block.alignment.name)
                blockObj.put("headingStyle", block.headingStyle.name)
                blockObj.put("listType", block.listType.name)
                blockObj.put("indentationLevel", block.indentationLevel)
                blockObj.put("isRtl", block.isRtl)
                blockObj.put("lineSpacing", block.lineSpacingMultiplier.toDouble())
                blockObj.put("firstLineIndentPt", block.firstLineIndentPt.toDouble())
                blockObj.put("hangingIndentPt", block.hangingIndentPt.toDouble())
                blockObj.put("spaceBeforePt", block.spaceBeforePt.toDouble())
                blockObj.put("spaceAfterPt", block.spaceAfterPt.toDouble())
                blockObj.put("leftIndentPt", block.leftIndentPt.toDouble())
                blockObj.put("dropCapLines", block.dropCapLines)
                blockObj.put("listLevel", block.listLevel)
                blockObj.put("isChecklist", block.isChecklist)
                blockObj.put("isChecked", block.isChecked)

                val spansArray = JSONArray()
                for (span in block.spans) {
                    val spanObj = JSONObject().apply {
                        put("start", span.start)
                        put("end", span.end)
                        put("isBold", span.isBold)
                        put("isItalic", span.isItalic)
                        put("isUnderline", span.isUnderline)
                        put("isStrikethrough", span.isStrikethrough)
                        put("isSuperscript", span.isSuperscript)
                        put("isSubscript", span.isSubscript)
                        span.fontSizeSp?.let { put("fontSizeSp", it.toDouble()) }
                        span.textColorHex?.let { put("textColorHex", it) }
                        span.highlightColorHex?.let { put("highlightColorHex", it) }
                        span.fontFamilyName?.let { put("fontFamilyName", it) }
                        span.linkUrl?.let { put("linkUrl", it) }
                        span.letterSpacingSp?.let { put("letterSpacingSp", it.toDouble()) }
                        put("underlineStyle", span.underlineStyle.name)
                        span.underlineColorHex?.let { put("underlineColorHex", it) }
                        put("strikethroughStyle", span.strikethroughStyle.name)
                        span.shadowDx?.let { put("shadowDx", it.toDouble()) }
                        span.shadowDy?.let { put("shadowDy", it.toDouble()) }
                        span.shadowRadius?.let { put("shadowRadius", it.toDouble()) }
                        span.shadowColorHex?.let { put("shadowColorHex", it) }
                        put("isOutline", span.isOutline)
                    }
                    spansArray.put(spanObj)
                }
                blockObj.put("spans", spansArray)
            }
            is TableBlock -> {
                blockObj.put("type", "table")
                blockObj.put("id", block.id)
                blockObj.put("rowCount", block.rowCount)
                blockObj.put("columnCount", block.columnCount)
                blockObj.put("hasBorders", block.hasBorders)
                blockObj.put("borderColorHex", block.borderColorHex)
                blockObj.put("tableWidthPercent", block.tableWidthPercent)
                blockObj.put("alignment", block.alignment.name)
                blockObj.put("hasHeaderRow", block.hasHeaderRow)
                blockObj.put("isZebraStriped", block.isZebraStriped)
                val cellsArray = JSONArray()
                for (row in block.cells) {
                    val rowArray = JSONArray()
                    for (cell in row) {
                        val cellObj = JSONObject().apply {
                            put("text", cell.text)
                            put("isBold", cell.isBold)
                            put("isItalic", cell.isItalic)
                            put("alignment", cell.alignment.name)
                            cell.backgroundColorHex?.let { put("bg", it) }
                            put("colSpan", cell.colSpan)
                            put("rowSpan", cell.rowSpan)
                            put("isMergedCovered", cell.isMergedCovered)
                            put("vAlign", cell.verticalAlignment.name)
                            cell.borderTopWidthPt?.let { put("bt", it.toDouble()) }
                            cell.borderBottomWidthPt?.let { put("bb", it.toDouble()) }
                            cell.borderLeftWidthPt?.let { put("bl", it.toDouble()) }
                            cell.borderRightWidthPt?.let { put("br", it.toDouble()) }
                        }
                        rowArray.put(cellObj)
                    }
                    cellsArray.put(rowArray)
                }
                blockObj.put("cells", cellsArray)
            }
            is ImageBlock -> {
                blockObj.put("type", "image")
                blockObj.put("id", block.id)
                blockObj.put("imageUri", block.imageUri)
                blockObj.put("caption", block.caption)
                blockObj.put("widthDp", block.widthDp)
                blockObj.put("heightDp", block.heightDp)
                blockObj.put("alignment", block.alignment.name)
                blockObj.put("aspectRatio", block.aspectRatio.toDouble())
                blockObj.put("filterMode", block.filterMode)
                blockObj.put("cornerRadiusDp", block.cornerRadiusDp)
                blockObj.put("borderThicknessDp", block.borderThicknessDp)
                blockObj.put("borderColorHex", block.borderColorHex)
                blockObj.put("rotationDegrees", block.rotationDegrees.toDouble())
                blockObj.put("wrapMode", block.wrapMode)
                blockObj.put("offsetXPt", block.offsetXPt.toDouble())
                blockObj.put("offsetYPt", block.offsetYPt.toDouble())
            }
            is FootnoteBlock -> {
                blockObj.put("type", "footnote")
                blockObj.put("id", block.id)
                blockObj.put("noteNumber", block.noteNumber)
                blockObj.put("referenceText", block.referenceText)
                blockObj.put("noteContent", block.noteContent)
            }
            is TableOfContentsBlock -> {
                blockObj.put("type", "table_of_contents")
                blockObj.put("id", block.id)
                blockObj.put("title", block.title)
            }
            is SignatureBlock -> {
                blockObj.put("type", "signature")
                blockObj.put("id", block.id)
                blockObj.put("svgPoints", block.svgPoints)
                blockObj.put("signerName", block.signerName)
                blockObj.put("signerTitle", block.signerTitle)
                blockObj.put("signedDate", block.signedDate)
                blockObj.put("strokeColorHex", block.strokeColorHex)
                blockObj.put("alignment", block.alignment.name)
            }
            is ShapeBlock -> {
                blockObj.put("type", "shape")
                blockObj.put("id", block.id)
                blockObj.put("shapeType", block.shapeType)
                blockObj.put("title", block.title)
                blockObj.put("content", block.content)
                blockObj.put("backgroundColorHex", block.backgroundColorHex)
                blockObj.put("borderColorHex", block.borderColorHex)
                blockObj.put("strokeWidthPt", block.strokeWidthPt.toDouble())
                blockObj.put("rotationDeg", block.rotationDeg.toDouble())
                blockObj.put("textColorHex", block.textColorHex)
                blockObj.put("widthDp", block.widthDp)
                blockObj.put("heightDp", block.heightDp)
            }
            is DrawingBlock -> {
                blockObj.put("type", "drawing")
                blockObj.put("id", block.id)
                blockObj.put("heightDp", block.heightDp)
                block.backgroundColorHex?.let { blockObj.put("backgroundColorHex", it) }
                val strokesArr = JSONArray()
                for (s in block.strokes) {
                    val sObj = JSONObject()
                    sObj.put("colorHex", s.colorHex)
                    sObj.put("strokeWidthPt", s.strokeWidthPt.toDouble())
                    sObj.put("isHighlighter", s.isHighlighter)
                    val ptsStr = s.points.joinToString(";") { "${it.first},${it.second}" }
                    sObj.put("points", ptsStr)
                    strokesArr.put(sObj)
                }
                blockObj.put("strokes", strokesArr)
            }
            is TextBoxBlock -> {
                blockObj.put("type", "textbox")
                blockObj.put("id", block.id)
                blockObj.put("text", block.text)
                blockObj.put("widthDp", block.widthDp)
                blockObj.put("heightDp", block.heightDp)
                blockObj.put("backgroundColorHex", block.backgroundColorHex)
                blockObj.put("borderColorHex", block.borderColorHex)
                blockObj.put("borderWidthPt", block.borderWidthPt.toDouble())
                blockObj.put("rotationDeg", block.rotationDeg.toDouble())
                blockObj.put("textColorHex", block.textColorHex)
                blockObj.put("offsetXPt", block.offsetXPt.toDouble())
                blockObj.put("offsetYPt", block.offsetYPt.toDouble())
            }
            is DividerBlock -> {
                blockObj.put("type", "divider")
                blockObj.put("id", block.id)
            }
            is PageBreakBlock -> {
                blockObj.put("type", "page_break")
                blockObj.put("id", block.id)
            }
            is ColumnBreakBlock -> {
                blockObj.put("type", "column_break")
                blockObj.put("id", block.id)
            }
            is SectionBreakBlock -> {
                blockObj.put("type", "section_break")
                blockObj.put("id", block.id)
                blockObj.put("sectionName", block.sectionName)
                blockObj.put("orientation", block.orientation)
                blockObj.put("isNextPage", block.isNextPage)
                blockObj.put("headerText", block.headerText)
                blockObj.put("footerText", block.footerText)
            }
        }
        return blockObj
    }
}
