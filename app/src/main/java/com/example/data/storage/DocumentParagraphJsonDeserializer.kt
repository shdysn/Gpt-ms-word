package com.example.data.storage

import com.example.data.model.HeadingStyle
import com.example.data.model.InlineSpan
import com.example.data.model.ListType
import com.example.data.model.ParagraphBlock
import com.example.data.model.StrikethroughStyle
import com.example.data.model.TextAlignment
import com.example.data.model.UnderlineStyle
import org.json.JSONObject

/**
 * Handles JSON deserialization of ParagraphBlock and its rich formatting spans.
 */
object DocumentParagraphJsonDeserializer {

    fun deserializeParagraph(bObj: JSONObject, blockId: String): ParagraphBlock {
        val text = bObj.optString("text", "")
        val alignment = try {
            TextAlignment.valueOf(bObj.optString("alignment", "START"))
        } catch (_: Exception) { TextAlignment.START }
        val heading = try {
            HeadingStyle.valueOf(bObj.optString("headingStyle", "NORMAL"))
        } catch (_: Exception) { HeadingStyle.NORMAL }
        val listType = try {
            ListType.valueOf(bObj.optString("listType", "NONE"))
        } catch (_: Exception) { ListType.NONE }

        val spans = mutableListOf<InlineSpan>()
        val spansArr = bObj.optJSONArray("spans")
        if (spansArr != null) {
            for (j in 0 until spansArr.length()) {
                val sObj = spansArr.getJSONObject(j)
                val isUnderline = sObj.optBoolean("isUnderline", false)
                val uStyleStr = sObj.optString("underlineStyle", if (isUnderline) "SINGLE" else "NONE")
                val uStyle = try {
                    UnderlineStyle.valueOf(uStyleStr)
                } catch (_: Exception) {
                    if (isUnderline) UnderlineStyle.SINGLE else UnderlineStyle.NONE
                }

                val isStrikethrough = sObj.optBoolean("isStrikethrough", false)
                val sStyleStr = sObj.optString("strikethroughStyle", if (isStrikethrough) "SINGLE" else "NONE")
                val sStyle = try {
                    StrikethroughStyle.valueOf(sStyleStr)
                } catch (_: Exception) {
                    if (isStrikethrough) StrikethroughStyle.SINGLE else StrikethroughStyle.NONE
                }

                spans.add(
                    InlineSpan(
                        start = sObj.optInt("start", 0),
                        end = sObj.optInt("end", 0),
                        isBold = sObj.optBoolean("isBold", false),
                        isItalic = sObj.optBoolean("isItalic", false),
                        isUnderline = isUnderline || (uStyle != UnderlineStyle.NONE),
                        isStrikethrough = isStrikethrough || (sStyle != StrikethroughStyle.NONE),
                        isSuperscript = sObj.optBoolean("isSuperscript", false),
                        isSubscript = sObj.optBoolean("isSubscript", false),
                        fontSizeSp = if (sObj.has("fontSizeSp")) sObj.getDouble("fontSizeSp").toFloat() else null,
                        textColorHex = if (sObj.has("textColorHex")) sObj.getString("textColorHex") else null,
                        highlightColorHex = if (sObj.has("highlightColorHex")) sObj.getString("highlightColorHex") else null,
                        fontFamilyName = if (sObj.has("fontFamilyName")) sObj.getString("fontFamilyName") else null,
                        linkUrl = if (sObj.has("linkUrl")) sObj.getString("linkUrl") else null,
                        letterSpacingSp = if (sObj.has("letterSpacingSp")) sObj.getDouble("letterSpacingSp").toFloat() else null,
                        underlineStyle = uStyle,
                        underlineColorHex = if (sObj.has("underlineColorHex")) sObj.getString("underlineColorHex") else null,
                        strikethroughStyle = sStyle,
                        shadowDx = if (sObj.has("shadowDx")) sObj.getDouble("shadowDx").toFloat() else null,
                        shadowDy = if (sObj.has("shadowDy")) sObj.getDouble("shadowDy").toFloat() else null,
                        shadowRadius = if (sObj.has("shadowRadius")) sObj.getDouble("shadowRadius").toFloat() else null,
                        shadowColorHex = if (sObj.has("shadowColorHex")) sObj.getString("shadowColorHex") else null,
                        isOutline = sObj.optBoolean("isOutline", false)
                    )
                )
            }
        }

        return ParagraphBlock(
            id = blockId,
            text = text,
            spans = spans,
            alignment = alignment,
            headingStyle = heading,
            listType = listType,
            indentationLevel = bObj.optInt("indentationLevel", 0),
            isRtl = bObj.optBoolean("isRtl", false),
            lineSpacingMultiplier = bObj.optDouble("lineSpacing", 1.25).toFloat(),
            firstLineIndentPt = bObj.optDouble("firstLineIndentPt", 0.0).toFloat(),
            hangingIndentPt = bObj.optDouble("hangingIndentPt", 0.0).toFloat(),
            spaceBeforePt = bObj.optDouble("spaceBeforePt", 0.0).toFloat(),
            spaceAfterPt = bObj.optDouble("spaceAfterPt", 0.0).toFloat(),
            leftIndentPt = bObj.optDouble("leftIndentPt", 0.0).toFloat(),
            dropCapLines = bObj.optInt("dropCapLines", 0),
            listLevel = bObj.optInt("listLevel", 0),
            isChecklist = bObj.optBoolean("isChecklist", false),
            isChecked = bObj.optBoolean("isChecked", false)
        )
    }
}
