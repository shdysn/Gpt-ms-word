package com.example.data.storage

import com.example.core.typography.BidiUtils
import com.example.data.model.ListType
import com.example.data.model.ParagraphBlock
import com.example.data.model.RichDocument
import com.example.data.model.TextAlignment

/**
 * Creates RichDocument instances from plain text, parsing bullets, numbering, and RTL script direction.
 */
object DocumentPlainTextImporter {

    fun createDocumentFromPlainText(title: String, text: String): RichDocument {
        val lines = text.lines()
        val blocks = if (lines.isEmpty() || text.isBlank()) {
            listOf(ParagraphBlock())
        } else {
            lines.map { rawLine ->
                val trimmed = rawLine.trim()
                var listType = ListType.NONE
                var lineContent = rawLine

                val bulletPrefixes = listOf("• ", "* ", "- ", "— ")
                val matchedBullet = bulletPrefixes.firstOrNull { trimmed.startsWith(it) }
                if (matchedBullet != null) {
                    listType = ListType.BULLET
                    lineContent = trimmed.removePrefix(matchedBullet).trim()
                } else {
                    val numberRegex = Regex("^([0-9]+|[۰-۹]+)[\\.\\-۔]\\s*(.*)$")
                    val matchResult = numberRegex.find(trimmed)
                    if (matchResult != null) {
                        listType = ListType.NUMBERED
                        lineContent = matchResult.groupValues[2].trim()
                    }
                }

                val isUrduOrArabic = BidiUtils.detectParagraphIsRtl(lineContent.ifBlank { rawLine })
                ParagraphBlock(
                    text = lineContent,
                    isRtl = isUrduOrArabic,
                    listType = listType,
                    alignment = TextAlignment.START
                )
            }
        }
        return RichDocument(
            title = title,
            blocks = blocks
        )
    }
}
