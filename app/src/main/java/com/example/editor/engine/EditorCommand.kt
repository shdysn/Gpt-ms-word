package com.example.editor.engine

import com.example.core.layout.PageLayoutConfig
import com.example.data.model.BlockElement
import com.example.data.model.HeadingStyle
import com.example.data.model.ImageBlock
import com.example.data.model.InlineSpan
import com.example.data.model.ParagraphBlock
import com.example.data.model.RichDocument
import com.example.data.model.TableBlock
import com.example.data.model.TextAlignment

/**
 * Interface for reversible document editor actions supporting robust Undo/Redo.
 */
interface EditorCommand {
    val description: String
    fun apply(document: RichDocument): RichDocument
    fun revert(document: RichDocument): RichDocument
}

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

/**
 * Command for inserting a new block element (paragraph, table, divider, page break).
 */
data class InsertBlockCommand(
    val index: Int,
    val block: BlockElement
) : EditorCommand {
    override val description: String = "Insert Element"

    override fun apply(document: RichDocument): RichDocument {
        val mutable = document.blocks.toMutableList()
        val insertIndex = index.coerceIn(0, mutable.size)
        mutable.add(insertIndex, block)
        return document.copy(blocks = mutable)
    }

    override fun revert(document: RichDocument): RichDocument {
        val mutable = document.blocks.toMutableList()
        mutable.removeAll { it.id == block.id }
        if (mutable.isEmpty()) {
            mutable.add(ParagraphBlock())
        }
        return document.copy(blocks = mutable)
    }
}

/**
 * Command for deleting a block element.
 */
data class DeleteBlockCommand(
    val index: Int,
    val deletedBlock: BlockElement
) : EditorCommand {
    override val description: String = "Delete Element"

    override fun apply(document: RichDocument): RichDocument {
        val mutable = document.blocks.toMutableList()
        mutable.removeAll { it.id == deletedBlock.id }
        if (mutable.isEmpty()) {
            mutable.add(ParagraphBlock())
        }
        return document.copy(blocks = mutable)
    }

    override fun revert(document: RichDocument): RichDocument {
        val mutable = document.blocks.toMutableList()
        val insertIndex = index.coerceIn(0, mutable.size)
        mutable.add(insertIndex, deletedBlock)
        return document.copy(blocks = mutable)
    }
}

/**
 * Command for updating page layout configuration (Page Size, Margins, Orientation).
 */
data class UpdateLayoutCommand(
    val oldLayout: PageLayoutConfig,
    val newLayout: PageLayoutConfig
) : EditorCommand {
    override val description: String = "Update Page Layout"

    override fun apply(document: RichDocument): RichDocument {
        return document.copy(layoutConfig = newLayout)
    }

    override fun revert(document: RichDocument): RichDocument {
        return document.copy(layoutConfig = oldLayout)
    }
}

/**
 * Command for changing bullet / numbered list style.
 */
data class ChangeListTypeCommand(
    val blockId: String,
    val oldListType: com.example.data.model.ListType,
    val newListType: com.example.data.model.ListType
) : EditorCommand {
    override val description: String = "Change list style"

    override fun apply(document: RichDocument): RichDocument {
        val updated = document.blocks.map { block ->
            if (block.id == blockId && block is ParagraphBlock) {
                block.copy(listType = newListType)
            } else block
        }
        return document.copy(blocks = updated)
    }

    override fun revert(document: RichDocument): RichDocument {
        val updated = document.blocks.map { block ->
            if (block.id == blockId && block is ParagraphBlock) {
                block.copy(listType = oldListType)
            } else block
        }
        return document.copy(blocks = updated)
    }
}

/**
 * Command for updating document title.
 */
data class UpdateTitleCommand(
    val oldTitle: String,
    val newTitle: String
) : EditorCommand {
    override val description: String = "Rename Title to \"$newTitle\""

    override fun apply(document: RichDocument): RichDocument =
        document.copy(title = newTitle)

    override fun revert(document: RichDocument): RichDocument =
        document.copy(title = oldTitle)
}

/**
 * Command for replacing a single search match in a paragraph while preserving formatting.
 */
data class ReplaceSingleMatchCommand(
    val blockId: String,
    val oldText: String,
    val newText: String,
    val oldSpans: List<InlineSpan>,
    val newSpans: List<InlineSpan>,
    val matchWord: String,
    val replaceWord: String
) : EditorCommand {
    override val description: String = "Replace \"$matchWord\" with \"$replaceWord\""

    override fun apply(document: RichDocument): RichDocument {
        val updated = document.blocks.map { block ->
            if (block.id == blockId && block is ParagraphBlock) {
                block.copy(text = newText, spans = newSpans)
            } else block
        }
        return document.copy(blocks = updated)
    }

    override fun revert(document: RichDocument): RichDocument {
        val updated = document.blocks.map { block ->
            if (block.id == blockId && block is ParagraphBlock) {
                block.copy(text = oldText, spans = oldSpans)
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
 * Command for changing paragraph indentation level (0 to 6).
 */
data class SetIndentationCommand(
    val blockId: String,
    val oldLevel: Int,
    val newLevel: Int
) : EditorCommand {
    override val description: String = if (newLevel > oldLevel) "Increase Indent" else "Decrease Indent"

    override fun apply(document: RichDocument): RichDocument {
        val updated = document.blocks.map { block ->
            if (block.id == blockId && block is ParagraphBlock) {
                block.copy(indentationLevel = newLevel.coerceIn(0, 6))
            } else block
        }
        return document.copy(blocks = updated)
    }

    override fun revert(document: RichDocument): RichDocument {
        val updated = document.blocks.map { block ->
            if (block.id == blockId && block is ParagraphBlock) {
                block.copy(indentationLevel = oldLevel.coerceIn(0, 6))
            } else block
        }
        return document.copy(blocks = updated)
    }
}

/**
 * Command for table structure, sizing, border, and cell edits.
 */
data class UpdateTableBlockCommand(
    val blockId: String,
    val oldTable: TableBlock,
    val newTable: TableBlock,
    val actionDescription: String = "Edit Table"
) : EditorCommand {
    override val description: String = actionDescription

    override fun apply(document: RichDocument): RichDocument {
        val updated = document.blocks.map { block ->
            if (block.id == blockId && block is TableBlock) {
                newTable.copy(id = blockId)
            } else block
        }
        return document.copy(blocks = updated)
    }

    override fun revert(document: RichDocument): RichDocument {
        val updated = document.blocks.map { block ->
            if ((block.id == blockId || block.id == newTable.id) && block is TableBlock) {
                oldTable.copy(id = blockId)
            } else block
        }
        return document.copy(blocks = updated)
    }
}

/**
 * Command for image dimensions, alignment, and caption modifications.
 */
data class UpdateImageBlockCommand(
    val blockId: String,
    val oldImage: ImageBlock,
    val newImage: ImageBlock,
    val actionDescription: String = "Edit Image"
) : EditorCommand {
    override val description: String = actionDescription

    override fun apply(document: RichDocument): RichDocument {
        val updated = document.blocks.map { block ->
            if (block.id == blockId && block is ImageBlock) {
                newImage.copy(id = blockId)
            } else block
        }
        return document.copy(blocks = updated)
    }

    override fun revert(document: RichDocument): RichDocument {
        val updated = document.blocks.map { block ->
            if ((block.id == blockId || block.id == newImage.id) && block is ImageBlock) {
                oldImage.copy(id = blockId)
            } else block
        }
        return document.copy(blocks = updated)
    }
}


