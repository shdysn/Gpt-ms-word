package com.example.editor.engine.commands

import com.example.data.model.CellVerticalAlignment
import com.example.data.model.RichDocument
import com.example.data.model.TableBlock
import com.example.editor.engine.EditorCommand

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
 * Command for merging adjacent table cells horizontally or vertically.
 */
data class MergeTableCellsCommand(
    val blockId: String,
    val oldTable: TableBlock,
    val row: Int,
    val col: Int,
    val horizontalSpan: Int = 2
) : EditorCommand {
    override val description: String = "Merge Table Cells"

    override fun apply(document: RichDocument): RichDocument {
        val updated = document.blocks.map { block ->
            if (block.id == blockId && block is TableBlock) {
                val newCells = block.cells.mapIndexed { r, rowList ->
                    rowList.mapIndexed { c, cell ->
                        if (r == row && c == col) {
                            val combinedText = (col until minOf(block.columnCount, col + horizontalSpan))
                                .map { block.cells[row][it].text }
                                .filter { it.isNotBlank() }
                                .joinToString(" ")
                            cell.copy(colSpan = horizontalSpan, text = combinedText, isMergedCovered = false)
                        } else if (r == row && c > col && c < col + horizontalSpan) {
                            cell.copy(isMergedCovered = true, text = "")
                        } else cell
                    }
                }
                block.copy(cells = newCells)
            } else block
        }
        return document.copy(blocks = updated)
    }

    override fun revert(document: RichDocument): RichDocument {
        val updated = document.blocks.map { block ->
            if (block.id == blockId && block is TableBlock) {
                oldTable
            } else block
        }
        return document.copy(blocks = updated)
    }
}

/**
 * Command for splitting a previously merged cell back to 1x1.
 */
data class SplitTableCellCommand(
    val blockId: String,
    val oldTable: TableBlock,
    val row: Int,
    val col: Int
) : EditorCommand {
    override val description: String = "Split Table Cell"

    override fun apply(document: RichDocument): RichDocument {
        val updated = document.blocks.map { block ->
            if (block.id == blockId && block is TableBlock) {
                val currentCell = block.cells.getOrNull(row)?.getOrNull(col) ?: return@map block
                val span = currentCell.colSpan
                val newCells = block.cells.mapIndexed { r, rowList ->
                    rowList.mapIndexed { c, cell ->
                        if (r == row && c >= col && c < col + span) {
                            cell.copy(colSpan = 1, rowSpan = 1, isMergedCovered = false)
                        } else cell
                    }
                }
                block.copy(cells = newCells)
            } else block
        }
        return document.copy(blocks = updated)
    }

    override fun revert(document: RichDocument): RichDocument {
        val updated = document.blocks.map { block ->
            if (block.id == blockId && block is TableBlock) {
                oldTable
            } else block
        }
        return document.copy(blocks = updated)
    }
}

/**
 * Command for updating individual cell formatting (shading, vertical alignment, padding).
 */
data class UpdateTableCellFormattingCommand(
    val blockId: String,
    val oldTable: TableBlock,
    val row: Int,
    val col: Int,
    val verticalAlignment: CellVerticalAlignment,
    val backgroundColorHex: String?
) : EditorCommand {
    override val description: String = "Format Table Cell"

    override fun apply(document: RichDocument): RichDocument {
        val updated = document.blocks.map { block ->
            if (block.id == blockId && block is TableBlock) {
                val newCells = block.cells.mapIndexed { r, rowList ->
                    rowList.mapIndexed { c, cell ->
                        if (r == row && c == col) {
                            cell.copy(
                                verticalAlignment = verticalAlignment,
                                backgroundColorHex = backgroundColorHex
                            )
                        } else cell
                    }
                }
                block.copy(cells = newCells)
            } else block
        }
        return document.copy(blocks = updated)
    }

    override fun revert(document: RichDocument): RichDocument {
        val updated = document.blocks.map { block ->
            if (block.id == blockId && block is TableBlock) {
                oldTable
            } else block
        }
        return document.copy(blocks = updated)
    }
}
