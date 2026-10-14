package com.example.editor.engine.commands

import com.example.core.layout.PageLayoutConfig
import com.example.data.model.BlockElement
import com.example.data.model.ImageBlock
import com.example.data.model.ParagraphBlock
import com.example.data.model.RichDocument
import com.example.editor.engine.EditorCommand

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

/**
 * Command for updating an arbitrary block element in place.
 */
data class UpdateBlockCommand(
    val blockId: String,
    val newBlock: BlockElement
) : EditorCommand {
    override val description: String = "Update Block"

    override fun apply(document: RichDocument): RichDocument {
        val updated = document.blocks.map { block ->
            if (block.id == blockId) newBlock else block
        }
        return document.copy(blocks = updated)
    }

    override fun revert(document: RichDocument): RichDocument {
        return document
    }
}
