package com.example.ui.editor.viewmodel

import com.example.data.model.CellVerticalAlignment
import com.example.data.model.TableBlock
import com.example.data.model.TextAlignment
import com.example.editor.engine.UpdateTableCellFormattingCommand
import com.example.editor.engine.UpdateTableBlockCommand
import com.example.ui.editor.EditorViewModel

/**
 * Manages table cell formatting, background colors, header rows, and table styling properties.
 */
class EditorTableCellFormatManager(
    private val vm: EditorViewModel
) {
    fun updateTableCell(blockId: String, row: Int, col: Int, text: String) {
        val currentDoc = vm.currentDocument ?: return
        val oldTable = currentDoc.blocks.firstOrNull { it.id == blockId } as? TableBlock ?: return
        val newCells = oldTable.cells.mapIndexed { rIndex, rowList ->
            if (rIndex == row) {
                rowList.mapIndexed { cIndex, cell ->
                    if (cIndex == col) cell.copy(text = text) else cell
                }
            } else rowList
        }
        val newTable = oldTable.copy(cells = newCells)
        vm.executeCommand(
            UpdateTableBlockCommand(
                blockId = blockId,
                oldTable = oldTable,
                newTable = newTable,
                actionDescription = "Edit Cell Text"
            )
        )
    }

    fun updateTableCellFormat(
        blockId: String,
        row: Int,
        col: Int,
        isBold: Boolean? = null,
        isItalic: Boolean? = null,
        backgroundColorHex: String? = null,
        alignment: TextAlignment? = null
    ) {
        val currentDoc = vm.currentDocument ?: return
        val oldTable = currentDoc.blocks.firstOrNull { it.id == blockId } as? TableBlock ?: return
        val newCells = oldTable.cells.mapIndexed { rIndex, rowList ->
            if (rIndex == row) {
                rowList.mapIndexed { cIndex, cell ->
                    if (cIndex == col) {
                        cell.copy(
                            isBold = isBold ?: cell.isBold,
                            isItalic = isItalic ?: cell.isItalic,
                            backgroundColorHex = backgroundColorHex ?: cell.backgroundColorHex,
                            alignment = alignment ?: cell.alignment
                        )
                    } else cell
                }
            } else rowList
        }
        val newTable = oldTable.copy(cells = newCells)
        vm.executeCommand(
            UpdateTableBlockCommand(
                blockId = blockId,
                oldTable = oldTable,
                newTable = newTable,
                actionDescription = "Format Cell"
            )
        )
    }

    fun updateTableProperties(
        blockId: String,
        hasBorders: Boolean? = null,
        borderColorHex: String? = null,
        tableWidthPercent: Int? = null,
        alignment: TextAlignment? = null
    ) {
        val currentDoc = vm.currentDocument ?: return
        val oldTable = currentDoc.blocks.firstOrNull { it.id == blockId } as? TableBlock ?: return
        val newTable = oldTable.copy(
            hasBorders = hasBorders ?: oldTable.hasBorders,
            borderColorHex = borderColorHex ?: oldTable.borderColorHex,
            tableWidthPercent = tableWidthPercent ?: oldTable.tableWidthPercent,
            alignment = alignment ?: oldTable.alignment
        )
        vm.executeCommand(
            UpdateTableBlockCommand(
                blockId = blockId,
                oldTable = oldTable,
                newTable = newTable,
                actionDescription = "Update Table Properties"
            )
        )
    }

    fun toggleTableHeaderRow(tableId: String) {
        val doc = vm.currentDocument ?: return
        val currentTable = doc.blocks.filterIsInstance<TableBlock>().firstOrNull { it.id == tableId } ?: return
        val updated = currentTable.copy(hasHeaderRow = !currentTable.hasHeaderRow)
        vm.executeCommand(UpdateTableBlockCommand(currentTable.id, currentTable, updated))
    }

    fun toggleTableZebraStriping(tableId: String) {
        val doc = vm.currentDocument ?: return
        val currentTable = doc.blocks.filterIsInstance<TableBlock>().firstOrNull { it.id == tableId } ?: return
        val updated = currentTable.copy(isZebraStriped = !currentTable.isZebraStriped)
        vm.executeCommand(UpdateTableBlockCommand(currentTable.id, currentTable, updated))
    }

    fun setTableCellBackground(tableId: String, row: Int, col: Int, bgHex: String?) {
        val doc = vm.currentDocument ?: return
        val currentTable = doc.blocks.filterIsInstance<TableBlock>().firstOrNull { it.id == tableId } ?: return
        val updatedCells = currentTable.cells.mapIndexed { r, rList ->
            rList.mapIndexed { c, cell ->
                if (r == row && c == col) cell.copy(backgroundColorHex = bgHex) else cell
            }
        }
        val updated = currentTable.copy(cells = updatedCells)
        vm.executeCommand(UpdateTableBlockCommand(currentTable.id, currentTable, updated))
    }

    fun updateTableCellFormatting(
        tableId: String,
        row: Int,
        col: Int,
        verticalAlignment: CellVerticalAlignment,
        bgHex: String?
    ) {
        val currentDoc = vm.currentDocument ?: return
        val table = currentDoc.blocks.filterIsInstance<TableBlock>().firstOrNull { it.id == tableId } ?: return
        vm.executeCommand(
            UpdateTableCellFormattingCommand(
                blockId = tableId,
                oldTable = table,
                row = row,
                col = col,
                verticalAlignment = verticalAlignment,
                backgroundColorHex = bgHex
            )
        )
        vm.showStatusMessage("Cell properties updated")
    }
}
