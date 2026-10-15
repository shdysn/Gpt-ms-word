package com.example.data.storage

import com.example.core.layout.MarginPreset
import com.example.core.layout.PageBorderStyle
import com.example.core.layout.PageLayoutConfig
import com.example.core.layout.PageNumberFormat
import com.example.core.layout.PageOrientation
import com.example.core.layout.PageSize
import com.example.data.model.BlockElement
import com.example.data.model.CustomStyle
import com.example.data.model.DocumentComment
import com.example.data.model.ParagraphBlock
import com.example.data.model.RichDocument
import com.example.data.model.TextAlignment
import com.example.data.model.TrackedChange
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

/**
 * Handles deserialization of RichDocument from JSON strings.
 */
object DocumentJsonDeserializer {

    fun deserialize(jsonStr: String): RichDocument {
        val root = JSONObject(jsonStr)
        val id = root.optString("id", "")
        val title = root.optString("title", "Untitled Document")
        val author = root.optString("author", "Author")
        val createdAt = root.optLong("createdAt", System.currentTimeMillis())
        val updatedAt = root.optLong("updatedAt", System.currentTimeMillis())

        val layoutConfig = if (root.has("layoutConfig")) {
            val lObj = root.getJSONObject("layoutConfig")
            val pageSize = try {
                PageSize.valueOf(lObj.optString("pageSize", "A4"))
            } catch (_: Exception) { PageSize.A4 }
            val orientation = try {
                PageOrientation.valueOf(lObj.optString("orientation", "PORTRAIT"))
            } catch (_: Exception) { PageOrientation.PORTRAIT }
            val marginPreset = try {
                MarginPreset.valueOf(lObj.optString("marginPreset", "NORMAL"))
            } catch (_: Exception) { MarginPreset.NORMAL }
            val borderStyle = try {
                PageBorderStyle.valueOf(lObj.optString("borderStyle", "NONE"))
            } catch (_: Exception) { PageBorderStyle.NONE }
            val numberFormat = try {
                PageNumberFormat.valueOf(lObj.optString("pageNumberFormat", "ARABIC_NUMERALS"))
            } catch (_: Exception) { PageNumberFormat.ARABIC_NUMERALS }
            val headerAlign = try {
                TextAlignment.valueOf(lObj.optString("headerAlignment", "START"))
            } catch (_: Exception) { TextAlignment.START }
            val footerAlign = try {
                TextAlignment.valueOf(lObj.optString("footerAlignment", "CENTER"))
            } catch (_: Exception) { TextAlignment.CENTER }

            val customTop = if (lObj.has("customTopMarginMm")) lObj.getDouble("customTopMarginMm").toFloat() else null
            val customBottom = if (lObj.has("customBottomMarginMm")) lObj.getDouble("customBottomMarginMm").toFloat() else null
            val customLeft = if (lObj.has("customLeftMarginMm")) lObj.getDouble("customLeftMarginMm").toFloat() else null
            val customRight = if (lObj.has("customRightMarginMm")) lObj.getDouble("customRightMarginMm").toFloat() else null

            PageLayoutConfig(
                pageSize = pageSize,
                orientation = orientation,
                marginPreset = marginPreset,
                customTopMarginMm = customTop,
                customBottomMarginMm = customBottom,
                customLeftMarginMm = customLeft,
                customRightMarginMm = customRight,
                showHeader = lObj.optBoolean("showHeader", true),
                showFooter = lObj.optBoolean("showFooter", true),
                headerText = lObj.optString("headerText", ""),
                footerText = lObj.optString("footerText", ""),
                headerAlignment = headerAlign,
                footerAlignment = footerAlign,
                pageBackgroundHex = lObj.optString("pageBackgroundHex", "#FFFFFF"),
                borderStyle = borderStyle,
                borderColorHex = lObj.optString("borderColorHex", "#CBD5E1"),
                borderWidthPt = lObj.optDouble("borderWidthPt", 1.0).toFloat(),
                pageNumberFormat = numberFormat,
                pageNumberStart = lObj.optInt("pageNumberStart", 1),
                differentFirstPage = lObj.optBoolean("differentFirstPage", false),
                differentOddEvenPages = lObj.optBoolean("differentOddEvenPages", false),
                headerEvenText = lObj.optString("headerEvenText", ""),
                footerEvenText = lObj.optString("footerEvenText", ""),
                watermarkText = lObj.optString("watermarkText", ""),
                watermarkColorHex = lObj.optString("watermarkColorHex", "#2594A3B8"),
                watermarkAngleDeg = lObj.optDouble("watermarkAngleDeg", -45.0).toFloat(),
                columnCount = lObj.optInt("columnCount", 1)
            )
        } else {
            PageLayoutConfig()
        }

        val passwordHash = if (root.has("passwordHash") && !root.isNull("passwordHash")) root.getString("passwordHash") else null

        val blocks = mutableListOf<BlockElement>()
        val blocksArray = root.optJSONArray("blocks") ?: JSONArray()
        for (i in 0 until blocksArray.length()) {
            val bObj = blocksArray.getJSONObject(i)
            blocks.add(DocumentBlockJsonDeserializer.deserializeBlock(bObj))
        }

        if (blocks.isEmpty()) {
            blocks.add(ParagraphBlock())
        }

        // Custom Styles deserialization
        val customStyles = mutableListOf<CustomStyle>()
        val stylesArr = root.optJSONArray("customStyles")
        if (stylesArr != null) {
            for (i in 0 until stylesArr.length()) {
                val sObj = stylesArr.getJSONObject(i)
                customStyles.add(
                    CustomStyle(
                        id = sObj.optString("id", UUID.randomUUID().toString()),
                        name = sObj.optString("name", "Custom Style"),
                        fontSizeSp = sObj.optDouble("fontSizeSp", 15.0).toFloat(),
                        isBold = sObj.optBoolean("isBold", false),
                        isItalic = sObj.optBoolean("isItalic", false),
                        textColorHex = if (sObj.has("textColorHex")) sObj.getString("textColorHex") else null,
                        spaceBeforePt = sObj.optDouble("spaceBeforePt", 0.0).toFloat(),
                        spaceAfterPt = sObj.optDouble("spaceAfterPt", 0.0).toFloat(),
                        lineSpacingMultiplier = sObj.optDouble("lineSpacingMultiplier", 1.25).toFloat(),
                        firstLineIndentPt = sObj.optDouble("firstLineIndentPt", 0.0).toFloat()
                    )
                )
            }
        }

        // Review & Tracking deserialization
        val isTrackChangesEnabled = root.optBoolean("isTrackChangesEnabled", false)
        val comments = mutableListOf<DocumentComment>()
        val commentsArr = root.optJSONArray("comments")
        if (commentsArr != null) {
            for (i in 0 until commentsArr.length()) {
                val cObj = commentsArr.getJSONObject(i)
                comments.add(
                    DocumentComment(
                        id = cObj.optString("id", UUID.randomUUID().toString()),
                        blockId = cObj.optString("blockId", ""),
                        author = cObj.optString("author", "Reviewer"),
                        text = cObj.optString("text", ""),
                        timestamp = cObj.optLong("timestamp", System.currentTimeMillis()),
                        isResolved = cObj.optBoolean("isResolved", false)
                    )
                )
            }
        }

        val trackedChanges = mutableListOf<TrackedChange>()
        val changesArr = root.optJSONArray("trackedChanges")
        if (changesArr != null) {
            for (i in 0 until changesArr.length()) {
                val tcObj = changesArr.getJSONObject(i)
                trackedChanges.add(
                    TrackedChange(
                        id = tcObj.optString("id", UUID.randomUUID().toString()),
                        blockId = tcObj.optString("blockId", ""),
                        originalText = tcObj.optString("originalText", ""),
                        updatedText = tcObj.optString("updatedText", ""),
                        author = tcObj.optString("author", "Editor"),
                        timestamp = tcObj.optLong("timestamp", System.currentTimeMillis()),
                        changeType = tcObj.optString("changeType", "EDIT")
                    )
                )
            }
        }

        return RichDocument(
            id = id.ifBlank { UUID.randomUUID().toString() },
            title = title,
            author = author,
            createdAt = createdAt,
            updatedAt = updatedAt,
            layoutConfig = layoutConfig,
            blocks = blocks,
            passwordHash = passwordHash,
            comments = comments,
            trackedChanges = trackedChanges,
            isTrackChangesEnabled = isTrackChangesEnabled,
            customStyles = customStyles
        )
    }
}
