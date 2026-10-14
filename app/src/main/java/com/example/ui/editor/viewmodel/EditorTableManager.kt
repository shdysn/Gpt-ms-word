package com.example.ui.editor.viewmodel

import com.example.data.model.CellVerticalAlignment
import com.example.data.model.TableCell
import com.example.data.model.TableBlock
import com.example.data.model.TextAlignment
import com.example.editor.engine.InsertBlockCommand
import com.example.editor.engine.MergeTableCellsCommand
import com.example.editor.engine.SplitTableCellCommand
import com.example.editor.engine.UpdateTableBlockCommand
import com.example.ui.editor.EditorViewModel

/**
 * Manages Table operations: row/column additions, structural modifications, merging, and cell formatting.
 */
class EditorTableManager(
    private val vm: EditorViewModel
) {
    internal val formatManager = EditorTableCellFormatManager(vm)

    fun insertTable(
        rows: Int,
        cols: Int,
        hasBorders: Boolean = true,
        borderColorHex: String = "#CBD5E1",
        tableWidthPercent: Int = 100
    ) {
        val insertIndex = vm.activeBlockIndex.value + 1
        val safeRows = rows.coerceIn(1, 15)
        val safeCols = cols.coerceIn(1, 10)
        val initialCells = List(safeRows) { r ->
            List(safeCols) { c ->
                TableCell(text = "", isBold = r == 0)
            }
        }
        val tableBlock = TableBlock(
            rowCount = safeRows,
            columnCount = safeCols,
            cells = initialCells,
            hasBorders = hasBorders,
            borderColorHex = borderColorHex,
            tableWidthPercent = tableWidthPercent
        )

        vm.executeCommand(InsertBlockCommand(index = insertIndex, block = tableBlock))
        vm.setActiveBlock(insertIndex)
    }

    fun addTableRow(blockId: String, afterRow: Int = -1) {
        val currentDoc = vm.currentDocument ?: return
        val oldTable = currentDoc.blocks.firstOrNull { it.id == blockId } as? TableBlock ?: return
        val newRow = List(oldTable.columnCount) { TableCell() }
        val mutableRows = oldTable.cells.toMutableList()
        val insertIndex = if (afterRow in 0 until mutableRows.size) afterRow + 1 else mutableRows.size
        mutableRows.add(insertIndex, newRow)
        val newTable = oldTable.copy(rowCount = mutableRows.size, cells = mutableRows)
        vm.executeCommand(
            UpdateTableBlockCommand(blockId, oldTable, newTable, "Add Table Row")
        )
    }

    fun deleteTableRow(blockId: String, rowIndex: Int) {
        val currentDoc = vm.currentDocument ?: return
        val oldTable = currentDoc.blocks.firstOrNull { it.id == blockId } as? TableBlock ?: return
        if (oldTable.rowCount <= 1) return
        val safeIndex = if (rowIndex in 0 until oldTable.rowCount) rowIndex else oldTable.rowCount - 1
        val mutableRows = oldTable.cells.toMutableList()
        mutableRows.removeAt(safeIndex)
        val newTable = oldTable.copy(rowCount = mutableRows.size, cells = mutableRows)
        vm.executeCommand(
            UpdateTableBlockCommand(blockId, oldTable, newTable, "Delete Table Row")
        )
    }

    fun addTableColumn(blockId: String, afterCol: Int = -1) {
        val currentDoc = vm.currentDocument ?: return
        val oldTable = currentDoc.blocks.firstOrNull { it.id == blockId } as? TableBlock ?: return
        val newColumnCount = oldTable.columnCount + 1
        val newCells = oldTable.cells.map { row ->
            val mutableRow = row.toMutableList()
            val insertIndex = if (afterCol in 0 until mutableRow.size) afterCol + 1 else mutableRow.size
            mutableRow.add(insertIndex, TableCell())
            mutableRow.toList()
        }
        val newTable = oldTable.copy(columnCount = newColumnCount, cells = newCells)
        vm.executeCommand(
            UpdateTableBlockCommand(blockId, oldTable, newTable, "Add Table Column")
        )
    }

    fun deleteTableColumn(blockId: String, colIndex: Int) {
        val currentDoc = vm.currentDocument ?: return
        val oldTable = currentDoc.blocks.firstOrNull { it.id == blockId } as? TableBlock ?: return
        if (oldTable.columnCount <= 1) return
        val safeCol = if (colIndex in 0 until oldTable.columnCount) colIndex else oldTable.columnCount - 1
        val newColumnCount = oldTable.columnCount - 1
        val newCells = oldTable.cells.map { row ->
            val mutableRow = row.toMutableList()
            mutableRow.removeAt(safeCol)
            mutableRow.toList()
        }
        val newTable = oldTable.copy(columnCount = newColumnCount, cells = newCells)
        vm.executeCommand(
            UpdateTableBlockCommand(blockId, oldTable, newTable, "Delete Table Column")
        )
    }

    fun mergeTableCells(tableId: String, row: Int, col: Int, span: Int = 2) {
        val currentDoc = vm.currentDocument ?: return
        val table = currentDoc.blocks.filterIsInstance<TableBlock>().firstOrNull { it.id == tableId } ?: return
        vm.executeCommand(
            MergeTableCellsCommand(tableId, table, row, col, span)
        )
        vm.showStatusMessage("Table cells merged")
    }

    fun splitTableCell(tableId: String, row: Int, col: Int) {
        val currentDoc = vm.currentDocument ?: return
        val table = currentDoc.blocks.filterIsInstance<TableBlock>().firstOrNull { it.id == tableId } ?: return
        vm.executeCommand(
            SplitTableCellCommand(tableId, table, row, col)
        )
        vm.showStatusMessage("Table cell split")
    }

    // Delegations to formatManager
    fun updateTableCell(blockId: String, row: Int, col: Int, text: String) =
        formatManager.updateTableCell(blockId, row, col, text)
    fun updateTableCellFormat(blockId: String, row: Int, col: Int, isBold: Boolean? = null, isItalic: Boolean? = null, backgroundColorHex: String? = null, alignment: TextAlignment? = null) =
        formatManager.updateTableCellFormat(blockId, row, col, isBold, isItalic, backgroundColorHex, alignment)
    fun updateTableProperties(blockId: String, hasBorders: Boolean? = null, borderColorHex: String? = null, tableWidthPercent: Int? = null, alignment: TextAlignment? = null) =
        formatManager.updateTableProperties(blockId, hasBorders, borderColorHex, tableWidthPercent, alignment)
    fun toggleTableHeaderRow(tableId: String) = formatManager.toggleTableHeaderRow(tableId)
    fun toggleTableZebraStriping(tableId: String) = formatManager.toggleTableZebraStriping(tableId)
    fun setTableCellBackground(tableId: String, row: Int, col: Int, bgHex: String?) = formatManager.setTableCellBackground(tableId, row, col, bgHex)
    fun updateTableCellFormatting(tableId: String, row: Int, col: Int, verticalAlignment: CellVerticalAlignment, bgHex: String?) =
        formatManager.updateTableCellFormatting(tableId, row, col, verticalAlignment, bgHex)
}
