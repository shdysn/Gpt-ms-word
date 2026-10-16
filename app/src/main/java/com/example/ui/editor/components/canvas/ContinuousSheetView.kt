package com.example.ui.editor.components.canvas

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.unit.dp
import com.example.core.layout.DocumentPage
import com.example.data.model.BlockElement
import com.example.data.model.DrawingStroke
import com.example.data.model.ImageBlock
import com.example.data.model.ListType
import com.example.data.model.ParagraphBlock
import com.example.data.model.RichDocument
import com.example.data.model.TableBlock
import com.example.ui.editor.components.BlockItemView
import com.example.ui.editor.components.DocumentRulerBar
import com.example.ui.editor.components.isColorDarkHex

@Composable
fun ContinuousSheetView(
    document: RichDocument,
    paginatedPages: List<DocumentPage>,
    globalListNumbers: Map<Int, Int>,
    activeBlockIndex: Int,
    zoomScale: Float,
    showRuler: Boolean,
    onBlockSelected: (Int) -> Unit,
    onUpdateBlockText: (Int, String, TextRange) -> Unit,
    onSelectionChangedForBlock: (Int, TextRange) -> Unit,
    onSplitParagraph: (Int, Int) -> Unit,
    onDeleteBlockById: (String) -> Unit,
    onDeleteOrMergeWithPrevious: (Int) -> Unit,
    onEditTableProperties: (TableBlock) -> Unit,
    onEditTableCell: (TableBlock, Int, Int) -> Unit,
    onQuickAddTableRow: (String) -> Unit,
    onQuickDeleteTableRow: (String) -> Unit,
    onQuickAddTableCol: (String) -> Unit,
    onQuickDeleteTableCol: (String) -> Unit,
    onEditImageProperties: (ImageBlock) -> Unit,
    onRefreshTOC: () -> Unit,
    onToggleChecklistItem: (String) -> Unit,
    onUpdateDrawingStrokes: (String, List<DrawingStroke>) -> Unit = { _, _ -> },
    drawTool: String = "PEN",
    drawColorHex: String = "#0F172A",
    drawStrokeWidthPt: Float = 2.5f,
    onSetDrawTool: (String) -> Unit = {},
    onSetDrawColorHex: (String) -> Unit = {},
    onSetDrawStrokeWidthPt: (Float) -> Unit = {},
    isDocumentAllSelected: Boolean = false,
    onSelectAllDocument: () -> Unit = {},
    onDeleteAllDocument: () -> Unit = {},
    onReplaceAllDocumentWithText: (String) -> Unit = {}
) {
    val pageBgColor = try {
        Color(android.graphics.Color.parseColor(document.layoutConfig.pageBackgroundHex))
    } catch (_: Exception) { Color.White }
    val isDarkPage = isColorDarkHex(document.layoutConfig.pageBackgroundHex)

    if (showRuler) {
        val activePara = document.blocks.getOrNull(activeBlockIndex) as? ParagraphBlock
        DocumentRulerBar(
            layout = document.layoutConfig,
            maxSheetWidth = 920.dp,
            firstLineIndentPt = activePara?.firstLineIndentPt ?: 0f,
            hangingIndentPt = activePara?.hangingIndentPt ?: 0f,
            leftIndentPt = activePara?.leftIndentPt ?: 0f,
            tabStops = activePara?.tabStops ?: emptyList()
        )
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = pageBgColor
    ) {
        @Composable
        fun RenderContinuousBlock(index: Int, block: BlockElement) {
            val itemNumber = if (block is ParagraphBlock && block.listType == ListType.NUMBERED) {
                globalListNumbers[index] ?: 1
            } else {
                null
            }

            BlockItemView(
                block = block,
                itemNumber = itemNumber,
                isActive = index == activeBlockIndex,
                isDarkPage = isDarkPage,
                zoomScale = zoomScale,
                canDeleteBlock = document.blocks.size > 1,
                onFocus = { onBlockSelected(index) },
                onSelectionChanged = { sel -> onSelectionChangedForBlock(index, sel) },
                onTextChanged = { newText, sel -> onUpdateBlockText(index, newText, sel) },
                onEnterPressed = { cursorOffset -> onSplitParagraph(index, cursorOffset) },
                onDeleteBlock = { onDeleteBlockById(block.id) },
                onDeleteOrMergeWithPrevious = { onDeleteOrMergeWithPrevious(index) },
                onEditTableProperties = { if (block is TableBlock) onEditTableProperties(block) },
                onEditTableCell = { r, c -> if (block is TableBlock) onEditTableCell(block, r, c) },
                onAddTableRow = { if (block is TableBlock) onQuickAddTableRow(block.id) },
                onDeleteTableRow = { if (block is TableBlock) onQuickDeleteTableRow(block.id) },
                onAddTableCol = { if (block is TableBlock) onQuickAddTableCol(block.id) },
                onDeleteTableCol = { if (block is TableBlock) onQuickDeleteTableCol(block.id) },
                onEditImageProperties = { if (block is ImageBlock) onEditImageProperties(block) },
                documentBlocks = document.blocks,
                paginatedPages = paginatedPages,
                onNavigateToBlock = { onBlockSelected(it) },
                onRefreshTOC = onRefreshTOC,
                onToggleChecklistItem = onToggleChecklistItem,
                onUpdateDrawingStrokes = { strokes -> onUpdateDrawingStrokes(block.id, strokes) },
                drawTool = drawTool,
                drawColorHex = drawColorHex,
                drawStrokeWidthPt = drawStrokeWidthPt,
                onSetDrawTool = onSetDrawTool,
                onSetDrawColorHex = onSetDrawColorHex,
                onSetDrawStrokeWidthPt = onSetDrawStrokeWidthPt,
                isDocumentAllSelected = isDocumentAllSelected,
                onSelectAllDocument = onSelectAllDocument,
                onDeleteAllDocument = onDeleteAllDocument,
                onReplaceAllDocumentWithText = onReplaceAllDocumentWithText
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        if (document.layoutConfig.columnCount == 2 && document.blocks.size > 1) {
            val mid = (document.blocks.size + 1) / 2
            val leftBlocks = document.blocks.mapIndexed { idx, b -> idx to b }.take(mid)
            val rightBlocks = document.blocks.mapIndexed { idx, b -> idx to b }.drop(mid)
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    leftBlocks.forEach { (index, block) ->
                        RenderContinuousBlock(index, block)
                    }
                }
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .heightIn(min = 60.dp)
                        .background(if (isDarkPage) Color(0xFF334155) else Color(0xFFE2E8F0))
                )
                Column(modifier = Modifier.weight(1f)) {
                    rightBlocks.forEach { (index, block) ->
                        RenderContinuousBlock(index, block)
                    }
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                document.blocks.forEachIndexed { index, block ->
                    RenderContinuousBlock(index, block)
                }
            }
        }
    }
}
