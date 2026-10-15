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

    fun createDocumentFromMarkdown(title: String, markdown: String): RichDocument {
        val lines = markdown.lines()
        if (lines.isEmpty() || markdown.isBlank()) {
            return RichDocument(title = title, blocks = listOf(ParagraphBlock()))
        }

        val blocks = mutableListOf<com.example.data.model.BlockElement>()
        var docTitle = title
        var titleFoundFromH1 = false
        val pendingTableRows = mutableListOf<List<String>>()

        fun flushPendingTable() {
            if (pendingTableRows.isNotEmpty()) {
                val nonDividerRows = pendingTableRows.filterNot { row ->
                    row.all { it.matches(Regex("^-+:?$")) || it.isBlank() }
                }
                if (nonDividerRows.isNotEmpty()) {
                    val maxCols = nonDividerRows.maxOf { it.size }.coerceAtLeast(1)
                    val tableCells = nonDividerRows.map { row ->
                        (0 until maxCols).map { c ->
                            com.example.data.model.TableCell(text = row.getOrNull(c) ?: "")
                        }
                    }
                    blocks.add(
                        com.example.data.model.TableBlock(
                            rowCount = tableCells.size,
                            columnCount = maxCols,
                            cells = tableCells
                        )
                    )
                }
                pendingTableRows.clear()
            }
        }

        for (rawLine in lines) {
            val trimmed = rawLine.trim()
            if (trimmed.isEmpty()) {
                flushPendingTable()
                continue
            }

            // Table row detection: | Col 1 | Col 2 |
            if (trimmed.startsWith("|") && trimmed.endsWith("|")) {
                val parts = trimmed.drop(1).dropLast(1).split("|").map { it.trim() }
                pendingTableRows.add(parts)
                continue
            } else {
                flushPendingTable()
            }

            // Divider: --- or ***
            if (trimmed == "---" || trimmed == "***" || trimmed == "___") {
                blocks.add(com.example.data.model.DividerBlock())
                continue
            }

            var heading = com.example.data.model.HeadingStyle.NORMAL
            var listType = ListType.NONE
            var isChecklist = false
            var isChecked = false
            var content = trimmed

            when {
                trimmed.startsWith("#### ") -> {
                    heading = com.example.data.model.HeadingStyle.HEADING_3
                    content = trimmed.removePrefix("#### ").trim()
                }
                trimmed.startsWith("### ") -> {
                    heading = com.example.data.model.HeadingStyle.HEADING_2
                    content = trimmed.removePrefix("### ").trim()
                }
                trimmed.startsWith("## ") -> {
                    heading = com.example.data.model.HeadingStyle.HEADING_1
                    content = trimmed.removePrefix("## ").trim()
                }
                trimmed.startsWith("# ") -> {
                    heading = com.example.data.model.HeadingStyle.TITLE
                    content = trimmed.removePrefix("# ").trim()
                    if (!titleFoundFromH1) {
                        docTitle = content
                        titleFoundFromH1 = true
                    }
                }
                trimmed.startsWith("> ") -> {
                    heading = com.example.data.model.HeadingStyle.QUOTE
                    content = trimmed.removePrefix("> ").trim()
                }
                trimmed.startsWith("- [x] ") || trimmed.startsWith("- [X] ") -> {
                    isChecklist = true
                    isChecked = true
                    content = trimmed.substring(6).trim()
                }
                trimmed.startsWith("- [ ] ") -> {
                    isChecklist = true
                    isChecked = false
                    content = trimmed.substring(6).trim()
                }
                trimmed.startsWith("* ") || trimmed.startsWith("- ") || trimmed.startsWith("• ") -> {
                    listType = ListType.BULLET
                    content = trimmed.substring(2).trim()
                }
                trimmed.matches(Regex("^[0-9]+\\.\\s+.*")) -> {
                    listType = ListType.NUMBERED
                    content = trimmed.replaceFirst(Regex("^[0-9]+\\.\\s+"), "").trim()
                }
            }

            val isUrduOrArabic = BidiUtils.detectParagraphIsRtl(content)
            blocks.add(
                ParagraphBlock(
                    text = content,
                    headingStyle = heading,
                    listType = listType,
                    isChecklist = isChecklist,
                    isChecked = isChecked,
                    isRtl = isUrduOrArabic,
                    alignment = if (isUrduOrArabic) TextAlignment.END else TextAlignment.START
                )
            )
        }
        flushPendingTable()

        return RichDocument(
            title = docTitle,
            blocks = if (blocks.isEmpty()) listOf(ParagraphBlock()) else blocks
        )
    }
}
