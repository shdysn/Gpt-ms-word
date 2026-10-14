package com.example.editor.engine.commands

import com.example.data.model.CustomStyle
import com.example.data.model.InlineSpan
import com.example.data.model.ListType
import com.example.data.model.ParagraphBlock
import com.example.data.model.RichDocument
import com.example.editor.engine.EditorCommand

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
