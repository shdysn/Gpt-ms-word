package com.example.ui.editor.components

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.core.layout.DocumentPage
import com.example.data.model.RichDocument
import com.example.ui.editor.EditorDialogsState
import com.example.ui.editor.EditorViewMode
import com.example.ui.editor.EditorViewModel

@Composable
fun EditorCanvasContent(
    document: RichDocument?,
    viewModel: EditorViewModel,
    paginatedPages: List<DocumentPage>,
    viewMode: EditorViewMode,
    zoomPercent: Int,
    showRuler: Boolean,
    activeBlockIndex: Int,
    dState: EditorDialogsState,
    modifier: Modifier = Modifier
) {
    BoxWithConstraints(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.TopCenter
    ) {
        val isTabletOrExpanded = maxWidth > 600.dp

        val drawTool by viewModel.drawTool.collectAsStateWithLifecycle()
        val drawColorHex by viewModel.drawColorHex.collectAsStateWithLifecycle()
        val drawStrokeWidthPt by viewModel.drawStrokeWidthPt.collectAsStateWithLifecycle()

        if (document != null) {
            DocumentCanvas(
                document = document,
                paginatedPages = paginatedPages,
                viewMode = viewMode,
                zoomPercent = zoomPercent,
                showRuler = showRuler,
                isTabletOrExpanded = isTabletOrExpanded,
                containerWidthDp = maxWidth.value,
                activeBlockIndex = activeBlockIndex,
                onBlockSelected = viewModel::setActiveBlock,
                onUpdateBlockText = { index, text, sel -> viewModel.updateParagraphText(index, text, sel) },
                onUpdateSubSliceText = { index, startChar, endChar, text, sel ->
                    viewModel.updateParagraphSubSliceText(index, startChar, endChar, text, sel)
                },
                onSelectionChanged = viewModel::updateSelection,
                onSelectionChangedForBlock = viewModel::updateSelectionForBlock,
                onSplitParagraph = viewModel::splitParagraph,
                onInsertParagraph = viewModel::insertParagraph,
                onDeleteCurrentBlock = viewModel::deleteCurrentBlock,
                onDeleteBlockById = viewModel::deleteBlockById,
                onUpdateUnifiedText = { text, sel -> viewModel.updateUnifiedDocumentText(text, sel) },
                onDeleteOrMergeWithPrevious = { index -> viewModel.deleteOrMergeWithPrevious(index) },
                onEditTableProperties = { table -> dState.selectedTableForProperties = table },
                onEditTableCell = { table, r, c -> dState.selectedCellForEdit = Triple(table, r, c) },
                onQuickAddTableRow = { id -> viewModel.addTableRow(id) },
                onQuickDeleteTableRow = { id -> viewModel.deleteTableRow(id, -1) },
                onQuickAddTableCol = { id -> viewModel.addTableColumn(id) },
                onQuickDeleteTableCol = { id -> viewModel.deleteTableColumn(id, -1) },
                onEditImageProperties = { img -> dState.selectedImageForProperties = img },
                onOpenPageLayoutDialog = { dState.showLayoutDialog = true },
                onRefreshTOC = viewModel::refreshTableOfContents,
                onToggleChecklistItem = { viewModel.toggleChecklistItem(it) },
                onUpdateDrawingStrokes = { id, strokes -> viewModel.updateDrawingStrokes(id, strokes) },
                drawTool = drawTool,
                drawColorHex = drawColorHex,
                drawStrokeWidthPt = drawStrokeWidthPt,
                onSetDrawTool = viewModel::setDrawTool,
                onSetDrawColorHex = viewModel::setDrawColorHex,
                onSetDrawStrokeWidthPt = viewModel::setDrawStrokeWidthPt,
                onUpdateTextBox = { id, text -> viewModel.updateTextBox(id, text) },
                onUpdateTextBoxGeometry = { id, ox, oy, w, h -> viewModel.updateTextBoxGeometry(id, ox, oy, w, h) },
                onUpdateTextBoxColor = { id, bg, bdr ->
                    val curText = document.blocks.filterIsInstance<com.example.data.model.TextBoxBlock>().firstOrNull { it.id == id }?.text ?: ""
                    viewModel.updateTextBox(blockId = id, text = curText, bgHex = bg, borderHex = bdr)
                },
                onUpdateImagePositionAndSize = { id, ox, oy, w, h -> viewModel.updateImagePositionAndSize(id, ox, oy, w, h) },
                onEditShape = { shape -> dState.selectedShapeForEdit = shape }
            )
        }

        val selection by viewModel.selection.collectAsStateWithLifecycle()
        val activeFormatting by viewModel.activeFormatting.collectAsStateWithLifecycle()

        EditorQuickFormatBar(
            selection = selection,
            activeFormatting = activeFormatting,
            onToggleBold = { viewModel.toggleBold() },
            onToggleItalic = { viewModel.toggleItalic() },
            onToggleUnderline = { viewModel.toggleUnderline() },
            onToggleStrikethrough = { viewModel.toggleStrikethrough() },
            onHighlightClick = {
                if (activeFormatting.highlightColorHex != null) viewModel.setHighlightColor(null)
                else viewModel.setHighlightColor("#FEF08A")
            },
            onTextColorClick = {
                if (activeFormatting.textColorHex != null) viewModel.setTextColor(null)
                else viewModel.setTextColor("#185ABD")
            },
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}
