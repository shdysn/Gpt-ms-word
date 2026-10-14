package com.example.editor.engine.commands

import com.example.data.model.HeadingStyle
import com.example.data.model.InlineSpan
import com.example.data.model.ParagraphBlock
import com.example.data.model.RichDocument
import com.example.editor.engine.CaseType
import com.example.editor.engine.EditorCommand
import com.example.editor.engine.SpanOperations

/**
 * Command for text modifications inside a specific paragraph.
 */
data class UpdateParagraphTextCommand(
    val blockId: String,
    val oldText: String,
    val newText: String
) : EditorCommand {
    override val description: String = "Edit Text"

    override fun apply(document: RichDocument): RichDocument {
        val updatedBlocks = document.blocks.map { block ->
            if (block.id == blockId && block is ParagraphBlock) {
                block.copy(text = newText)
            } else block
        }
        return document.copy(blocks = updatedBlocks)
    }

    override fun revert(document: RichDocument): RichDocument {
        val updatedBlocks = document.blocks.map { block ->
            if (block.id == blockId && block is ParagraphBlock) {
                block.copy(text = oldText)
            } else block
        }
        return document.copy(blocks = updatedBlocks)
    }
}

/**
 * Command for updating paragraph text while shifting/updating inline styling spans.
 */
data class UpdateParagraphTextAndSpansCommand(
    val blockId: String,
    val oldText: String,
    val newText: String,
    val oldSpans: List<InlineSpan>,
    val newSpans: List<InlineSpan>,
    val wasRtl: Boolean? = null,
    val isRtl: Boolean? = null
) : EditorCommand {
    override val description: String = "Edit Text"

    override fun apply(document: RichDocument): RichDocument {
        val updated = document.blocks.map { block ->
            if (block.id == blockId && block is ParagraphBlock) {
                block.copy(
                    text = newText,
                    spans = newSpans,
                    isRtl = isRtl ?: block.isRtl
                )
            } else block
        }
        return document.copy(blocks = updated)
    }

    override fun revert(document: RichDocument): RichDocument {
        val updated = document.blocks.map { block ->
            if (block.id == blockId && block is ParagraphBlock) {
                block.copy(
                    text = oldText,
                    spans = oldSpans,
                    isRtl = wasRtl ?: block.isRtl
                )
            } else block
        }
        return document.copy(blocks = updated)
    }
}

/**
 * Command for in-document Find and Replace operations across paragraphs.
 */
data class BatchReplaceTextCommand(
    val oldDocument: RichDocument,
    val newDocument: RichDocument,
    val replacementsCount: Int
) : EditorCommand {
    override val description: String = "Replace $replacementsCount occurrences"

    override fun apply(document: RichDocument): RichDocument = newDocument
    override fun revert(document: RichDocument): RichDocument = oldDocument
}

/**
 * Command for clearing all character formatting and resetting heading style.
 */
data class ClearFormattingCommand(
    val blockId: String,
    val selectionStart: Int,
    val selectionEnd: Int,
    val oldParagraph: ParagraphBlock
) : EditorCommand {
    override val description: String = "Clear Formatting"

    override fun apply(document: RichDocument): RichDocument {
        val updated = document.blocks.map { block ->
            if (block.id == blockId && block is ParagraphBlock) {
                val newSpans = SpanOperations.clearFormatting(
                    spans = block.spans,
                    start = selectionStart,
                    end = selectionEnd,
                    textLength = block.text.length
                )
                val newHeading = if (selectionStart == selectionEnd || (selectionStart <= 0 && selectionEnd >= block.text.length)) {
                    HeadingStyle.NORMAL
                } else {
                    block.headingStyle
                }
                block.copy(spans = newSpans, headingStyle = newHeading)
            } else block
        }
        return document.copy(blocks = updated)
    }

    override fun revert(document: RichDocument): RichDocument {
        val updated = document.blocks.map { block ->
            if (block.id == blockId && block is ParagraphBlock) {
                oldParagraph
            } else block
        }
        return document.copy(blocks = updated)
    }
}

/**
 * Command for modifying text casing within a selection.
 */
data class ChangeCaseCommand(
    val blockId: String,
    val selectionStart: Int,
    val selectionEnd: Int,
    val caseType: CaseType,
    val oldParagraph: ParagraphBlock
) : EditorCommand {
    override val description: String = "Change Case to ${caseType.label}"

    override fun apply(document: RichDocument): RichDocument {
        val updated = document.blocks.map { block ->
            if (block.id == blockId && block is ParagraphBlock) {
                val text = block.text
                val s = if (selectionStart == selectionEnd) 0 else selectionStart.coerceIn(0, text.length)
                val e = if (selectionStart == selectionEnd) text.length else selectionEnd.coerceIn(s, text.length)
                if (s >= e) return@map block

                val targetSub = text.substring(s, e)
                val transformedSub = when (caseType) {
                    CaseType.SENTENCE -> {
                        if (targetSub.isNotEmpty()) {
                            targetSub.take(1).uppercase() + targetSub.drop(1).lowercase()
                        } else targetSub
                    }
                    CaseType.LOWERCASE -> targetSub.lowercase()
                    CaseType.UPPERCASE -> targetSub.uppercase()
                    CaseType.TITLE_CASE -> {
                        targetSub.split(" ").joinToString(" ") { word ->
                            if (word.isNotEmpty()) word.take(1).uppercase() + word.drop(1).lowercase() else word
                        }
                    }
                    CaseType.TOGGLE_CASE -> {
                        targetSub.map { ch ->
                            if (ch.isUpperCase()) ch.lowercaseChar() else ch.uppercaseChar()
                        }.joinToString("")
                    }
                }

                val newFullText = text.substring(0, s) + transformedSub + text.substring(e)
                block.copy(text = newFullText)
            } else block
        }
        return document.copy(blocks = updated)
    }

    override fun revert(document: RichDocument): RichDocument {
        val updated = document.blocks.map { block ->
            if (block.id == blockId && block is ParagraphBlock) {
                oldParagraph
            } else block
        }
        return document.copy(blocks = updated)
    }
}

/**
 * Command for modifying paragraph indents (first line, hanging, before/after spacing, drop cap).
 */
data class UpdateParagraphIndentsCommand(
    val blockId: String,
    val oldParagraph: ParagraphBlock,
    val newFirstLineIndentPt: Float = oldParagraph.firstLineIndentPt,
    val newHangingIndentPt: Float = oldParagraph.hangingIndentPt,
    val newSpaceBeforePt: Float = oldParagraph.spaceBeforePt,
    val newSpaceAfterPt: Float = oldParagraph.spaceAfterPt,
    val newDropCapLines: Int = oldParagraph.dropCapLines
) : EditorCommand {
    override val description: String = "Update Paragraph Indents & Spacing"

    override fun apply(document: RichDocument): RichDocument {
        val updated = document.blocks.map { block ->
            if (block.id == blockId && block is ParagraphBlock) {
                block.copy(
                    firstLineIndentPt = newFirstLineIndentPt,
                    hangingIndentPt = newHangingIndentPt,
                    spaceBeforePt = newSpaceBeforePt,
                    spaceAfterPt = newSpaceAfterPt,
                    dropCapLines = newDropCapLines
                )
            } else block
        }
        return document.copy(blocks = updated)
    }

    override fun revert(document: RichDocument): RichDocument {
        val updated = document.blocks.map { block ->
            if (block.id == blockId && block is ParagraphBlock) {
                oldParagraph
            } else block
        }
        return document.copy(blocks = updated)
    }
}
