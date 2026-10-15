package com.example.data.storage

import com.example.data.model.RichDocument
import org.json.JSONArray
import org.json.JSONObject

/**
 * Handles JSON serialization of RichDocument and delegates block elements to DocumentBlockJsonSerializer.
 */
object DocumentJsonSerializer {

    fun serialize(doc: RichDocument): String {
        val root = JSONObject().apply {
            put("id", doc.id)
            put("title", doc.title)
            put("author", doc.author)
            put("createdAt", doc.createdAt)
            put("updatedAt", doc.updatedAt)

            // Layout
            val layoutObj = JSONObject().apply {
                put("pageSize", doc.layoutConfig.pageSize.name)
                put("orientation", doc.layoutConfig.orientation.name)
                put("marginPreset", doc.layoutConfig.marginPreset.name)
                doc.layoutConfig.customTopMarginMm?.let { put("customTopMarginMm", it.toDouble()) }
                doc.layoutConfig.customBottomMarginMm?.let { put("customBottomMarginMm", it.toDouble()) }
                doc.layoutConfig.customLeftMarginMm?.let { put("customLeftMarginMm", it.toDouble()) }
                doc.layoutConfig.customRightMarginMm?.let { put("customRightMarginMm", it.toDouble()) }
                put("showHeader", doc.layoutConfig.showHeader)
                put("showFooter", doc.layoutConfig.showFooter)
                put("headerText", doc.layoutConfig.headerText)
                put("footerText", doc.layoutConfig.footerText)
                put("headerAlignment", doc.layoutConfig.headerAlignment.name)
                put("footerAlignment", doc.layoutConfig.footerAlignment.name)
                put("pageBackgroundHex", doc.layoutConfig.pageBackgroundHex)
                put("borderStyle", doc.layoutConfig.borderStyle.name)
                put("borderColorHex", doc.layoutConfig.borderColorHex)
                put("borderWidthPt", doc.layoutConfig.borderWidthPt.toDouble())
                put("pageNumberFormat", doc.layoutConfig.pageNumberFormat.name)
                put("pageNumberStart", doc.layoutConfig.pageNumberStart)
                put("differentFirstPage", doc.layoutConfig.differentFirstPage)
                put("differentOddEvenPages", doc.layoutConfig.differentOddEvenPages)
                put("headerEvenText", doc.layoutConfig.headerEvenText)
                put("footerEvenText", doc.layoutConfig.footerEvenText)
                put("watermarkText", doc.layoutConfig.watermarkText)
                put("watermarkColorHex", doc.layoutConfig.watermarkColorHex)
                put("watermarkAngleDeg", doc.layoutConfig.watermarkAngleDeg.toDouble())
                put("columnCount", doc.layoutConfig.columnCount)
            }
            put("layoutConfig", layoutObj)
            doc.passwordHash?.let { put("passwordHash", it) }

            // Blocks
            val blocksArray = JSONArray()
            for (block in doc.blocks) {
                blocksArray.put(DocumentBlockJsonSerializer.serializeBlock(block))
            }
            put("blocks", blocksArray)

            // Custom Styles
            val stylesArr = JSONArray()
            for (cs in doc.customStyles) {
                val sObj = JSONObject().apply {
                    put("id", cs.id)
                    put("name", cs.name)
                    put("fontSizeSp", cs.fontSizeSp.toDouble())
                    put("isBold", cs.isBold)
                    put("isItalic", cs.isItalic)
                    cs.textColorHex?.let { put("textColorHex", it) }
                    put("spaceBeforePt", cs.spaceBeforePt.toDouble())
                    put("spaceAfterPt", cs.spaceAfterPt.toDouble())
                    put("lineSpacingMultiplier", cs.lineSpacingMultiplier.toDouble())
                    put("firstLineIndentPt", cs.firstLineIndentPt.toDouble())
                }
                stylesArr.put(sObj)
            }
            put("customStyles", stylesArr)

            // Review & Tracking
            put("isTrackChangesEnabled", doc.isTrackChangesEnabled)
            val commentsArr = JSONArray()
            for (c in doc.comments) {
                val cObj = JSONObject().apply {
                    put("id", c.id)
                    put("blockId", c.blockId)
                    put("author", c.author)
                    put("text", c.text)
                    put("timestamp", c.timestamp)
                    put("isResolved", c.isResolved)
                }
                commentsArr.put(cObj)
            }
            put("comments", commentsArr)

            val changesArr = JSONArray()
            for (tc in doc.trackedChanges) {
                val tcObj = JSONObject().apply {
                    put("id", tc.id)
                    put("blockId", tc.blockId)
                    put("originalText", tc.originalText)
                    put("updatedText", tc.updatedText)
                    put("author", tc.author)
                    put("timestamp", tc.timestamp)
                    put("changeType", tc.changeType)
                }
                changesArr.put(tcObj)
            }
            put("trackedChanges", changesArr)
        }
        return root.toString(2)
    }
}
