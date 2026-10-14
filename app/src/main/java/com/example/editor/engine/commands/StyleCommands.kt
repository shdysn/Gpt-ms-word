package com.example.editor.engine.commands

import com.example.data.model.HeadingStyle
import com.example.data.model.InlineSpan
import com.example.data.model.ParagraphBlock
import com.example.data.model.RichDocument
import com.example.data.model.TextAlignment
import com.example.editor.engine.EditorCommand

/**
 * Command for changing paragraph text alignment (Left, Center, Right, Justify).
 */
data class ChangeAlignmentCommand(
    val blockId: String,
    val oldAlignment: TextAlignment,
    val newAlignment: TextAlignment
) : EditorCommand {
    override val description: String = "Change Alignment to $newAlignment"

    override fun apply(document: RichDocument): RichDocument {
        val updatedBlocks = document.blocks.map { block ->
            if (block.id == blockId && block is ParagraphBlock) {
                block.copy(alignment = newAlignment)
            } else block
        }
        return document.copy(blocks = updatedBlocks)
    }

    override fun revert(document: RichDocument): RichDocument {
        val updatedBlocks = document.blocks.map { block ->
            if (block.id == blockId && block is ParagraphBlock) {
                block.copy(alignment = oldAlignment)
            } else block
        }
        return document.copy(blocks = updatedBlocks)
    }
}

/**
 * Command for changing alignment of multiple paragraphs in a selection.
 */
data class ChangeMultipleParagraphsAlignmentCommand(
    val blockIds: List<String>,
    val oldAlignments: Map<String, TextAlignment>,
    val newAlignment: TextAlignment
) : EditorCommand {
    override val description: String = "Change Multiple Alignments to $newAlignment"

    override fun apply(document: RichDocument): RichDocument {
        val idSet = blockIds.toSet()
        val updatedBlocks = document.blocks.map { block ->
            if (block is ParagraphBlock && block.id in idSet) {
                block.copy(alignment = newAlignment)
            } else block
        }
        return document.copy(blocks = updatedBlocks)
    }

    override fun revert(document: RichDocument): RichDocument {
        val updatedBlocks = document.blocks.map { block ->
            if (block is ParagraphBlock && block.id in oldAlignments) {
                block.copy(alignment = oldAlignments[block.id] ?: block.alignment)
            } else block
        }
        return document.copy(blocks = updatedBlocks)
    }
}

/**
 * Command for changing heading style (Title, Heading 1, 2, 3, Normal).
 */
data class ChangeHeadingStyleCommand(
    val blockId: String,
    val oldHeading: HeadingStyle,
    val newHeading: HeadingStyle
) : EditorCommand {
    override val description: String = "Set style to ${newHeading.displayName}"

    override fun apply(document: RichDocument): RichDocument {
        val updatedBlocks = document.blocks.map { block ->
            if (block.id == blockId && block is ParagraphBlock) {
                block.copy(headingStyle = newHeading)
            } else block
        }
        return document.copy(blocks = updatedBlocks)
    }

    override fun revert(document: RichDocument): RichDocument {
        val updatedBlocks = document.blocks.map { block ->
            if (block.id == blockId && block is ParagraphBlock) {
                block.copy(headingStyle = oldHeading)
            } else block
        }
        return document.copy(blocks = updatedBlocks)
    }
}

/**
 * Command for toggling Right-to-Left (Urdu / Arabic) directionality.
 */
data class ToggleRtlCommand(
    val blockId: String,
    val wasRtl: Boolean,
    val isRtl: Boolean
) : EditorCommand {
    override val description: String = if (isRtl) "Set Right-to-Left (RTL)" else "Set Left-to-Right (LTR)"

    override fun apply(document: RichDocument): RichDocument {
        val updatedBlocks = document.blocks.map { block ->
            if (block.id == blockId && block is ParagraphBlock) {
                block.copy(isRtl = isRtl)
            } else block
        }
        return document.copy(blocks = updatedBlocks)
    }

    override fun revert(document: RichDocument): RichDocument {
        val updatedBlocks = document.blocks.map { block ->
            if (block.id == blockId && block is ParagraphBlock) {
                block.copy(isRtl = wasRtl)
            } else block
        }
        return document.copy(blocks = updatedBlocks)
    }
}

/**
 * Command for applying inline formatting span (Bold, Italic, Underline, Color).
 */
data class ApplyInlineSpanCommand(
    val blockId: String,
    val oldSpans: List<InlineSpan>,
    val newSpans: List<InlineSpan>
) : EditorCommand {
    override val description: String = "Format Text"

    override fun apply(document: RichDocument): RichDocument {
        val updatedBlocks = document.blocks.map { block ->
            if (block.id == blockId && block is ParagraphBlock) {
                block.copy(spans = newSpans)
            } else block
        }
        return document.copy(blocks = updatedBlocks)
    }

    override fun revert(document: RichDocument): RichDocument {
        val updatedBlocks = document.blocks.map { block ->
            if (block.id == blockId && block is ParagraphBlock) {
                block.copy(spans = oldSpans)
            } else block
        }
        return document.copy(blocks = updatedBlocks)
    }
}
