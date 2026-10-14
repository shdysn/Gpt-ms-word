package com.example.editor.engine.commands

import com.example.data.model.CustomStyle
import com.example.data.model.HeadingStyle
import com.example.data.model.InlineSpan
import com.example.data.model.ListType
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

/**
 * Command for changing bullet / numbered list style.
 */
data class ChangeListTypeCommand(
    val blockId: String,
    val oldListType: ListType,
    val newListType: ListType
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
 * Command for toggling checklist mode on a paragraph.
 */
data class ToggleChecklistCommand(
    val blockId: String,
    val oldParagraph: ParagraphBlock
) : EditorCommand {
    override val description: String = "Toggle Checklist"

    override fun apply(document: RichDocument): RichDocument {
        val updated = document.blocks.map { block ->
            if (block.id == blockId && block is ParagraphBlock) {
                val newChecklistState = !block.isChecklist
                block.copy(
                    isChecklist = newChecklistState,
                    isChecked = if (newChecklistState) false else block.isChecked,
                    listType = if (newChecklistState) ListType.NONE else block.listType
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

/**
 * Command for toggling a checklist item's checked status.
 */
data class ToggleChecklistItemCommand(
    val blockId: String,
    val wasChecked: Boolean
) : EditorCommand {
    override val description: String = if (wasChecked) "Uncheck item" else "Check item"

    override fun apply(document: RichDocument): RichDocument {
        val updated = document.blocks.map { block ->
            if (block.id == blockId && block is ParagraphBlock) {
                block.copy(isChecked = !wasChecked)
            } else block
        }
        return document.copy(blocks = updated)
    }

    override fun revert(document: RichDocument): RichDocument {
        val updated = document.blocks.map { block ->
            if (block.id == blockId && block is ParagraphBlock) {
                block.copy(isChecked = wasChecked)
            } else block
        }
        return document.copy(blocks = updated)
    }
}

/**
 * Command for changing multi-level hierarchical list level (indentation level 0..4).
 */
data class SetListLevelCommand(
    val blockId: String,
    val oldLevel: Int,
    val newLevel: Int
) : EditorCommand {
    override val description: String = "Change List Level to $newLevel"

    override fun apply(document: RichDocument): RichDocument {
        val updated = document.blocks.map { block ->
            if (block.id == blockId && block is ParagraphBlock) {
                block.copy(listLevel = newLevel.coerceIn(0, 4))
            } else block
        }
        return document.copy(blocks = updated)
    }

    override fun revert(document: RichDocument): RichDocument {
        val updated = document.blocks.map { block ->
            if (block.id == blockId && block is ParagraphBlock) {
                block.copy(listLevel = oldLevel)
            } else block
        }
        return document.copy(blocks = updated)
    }
}

/**
 * Command for saving a custom named style into the document gallery.
 */
data class SaveCustomStyleCommand(
    val customStyle: CustomStyle
) : EditorCommand {
    override val description: String = "Create Custom Style '${customStyle.name}'"

    override fun apply(document: RichDocument): RichDocument {
        val existing = document.customStyles.filter { it.name != customStyle.name }
        return document.copy(customStyles = existing + customStyle)
    }

    override fun revert(document: RichDocument): RichDocument {
        val filtered = document.customStyles.filter { it.id != customStyle.id }
        return document.copy(customStyles = filtered)
    }
}

/**
 * Command for applying a custom named style to a target paragraph block.
 */
data class ApplyCustomStyleCommand(
    val blockId: String,
    val oldParagraph: ParagraphBlock,
    val style: CustomStyle
) : EditorCommand {
    override val description: String = "Apply Style '${style.name}'"

    override fun apply(document: RichDocument): RichDocument {
        val updated = document.blocks.map { block ->
            if (block.id == blockId && block is ParagraphBlock) {
                val baseSpan = InlineSpan(
                    start = 0,
                    end = block.text.length,
                    isBold = style.isBold,
                    isItalic = style.isItalic,
                    fontSizeSp = style.fontSizeSp,
                    textColorHex = style.textColorHex
                )
                block.copy(
                    spans = listOf(baseSpan),
                    spaceBeforePt = style.spaceBeforePt,
                    spaceAfterPt = style.spaceAfterPt,
                    lineSpacingMultiplier = style.lineSpacingMultiplier,
                    firstLineIndentPt = style.firstLineIndentPt
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
