package com.example.ui.editor.components.canvas

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.unit.dp
import com.example.core.layout.DocumentPage
import com.example.core.layout.ImagePageItem
import com.example.core.layout.PageItem
import com.example.core.layout.ParagraphPageItem
import com.example.core.layout.TablePageItem
import com.example.data.model.DrawingStroke
import com.example.data.model.ImageBlock
import com.example.data.model.ListType
import com.example.data.model.ParagraphBlock
import com.example.data.model.RichDocument
import com.example.data.model.TableBlock
import com.example.ui.editor.components.BlockItemView
import com.example.ui.editor.components.ImageBlockView
import com.example.ui.editor.components.TableBlockView

@Composable
fun PaginatedItemRenderer(
    pageItem: PageItem,
    document: RichDocument,
    paginatedPages: List<DocumentPage>,
    globalListNumbers: Map<Int, Int>,
    activeBlockIndex: Int,
    isDarkPage: Boolean,
    zoomScale: Float,
    onBlockSelected: (Int) -> Unit,
    onUpdateBlockText: (Int, String, TextRange) -> Unit,
    onUpdateSubSliceText: (Int, Int, Int, String, TextRange) -> Unit,
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
    onUpdateImagePositionAndSize: (String, Float, Float, Int, Int) -> Unit = { _, _, _, _, _ -> },
    onUpdateTextBox: (String, String) -> Unit = { _, _ -> },
    onUpdateTextBoxGeometry: (String, Float, Float, Int, Int) -> Unit = { _, _, _, _, _ -> },
    onUpdateTextBoxColor: (String, String, String) -> Unit = { _, _, _ -> },
    isDocumentAllSelected: Boolean = false,
    onSelectAllDocument: () -> Unit = {},
    onDeleteAllDocument: () -> Unit = {},
    onReplaceAllDocumentWithText: (String) -> Unit = {},
    onLongPress: () -> Unit = {}
) {
    when (pageItem) {
        is ParagraphPageItem -> {
            val block = (document.blocks.getOrNull(pageItem.originalBlockIndex) as? ParagraphBlock) ?: pageItem.originalBlock
            val isSplitSlice = pageItem.startChar > 0 || (pageItem.endChar > 0 && pageItem.endChar < block.text.length)
            val sliceStart = if (isSplitSlice) pageItem.startChar.coerceIn(0, block.text.length) else 0
            val sliceEnd = if (isSplitSlice) pageItem.endChar.coerceIn(sliceStart, block.text.length) else block.text.length

            val displayBlock = remember(block, isSplitSlice, sliceStart, sliceEnd) {
                if (isSplitSlice) {
                    val subSpans = block.spans.mapNotNull { span ->
                        val spStart = (span.start - sliceStart).coerceAtLeast(0)
                        val spEnd = (span.end - sliceStart).coerceAtMost(sliceEnd - sliceStart)
                        if (spStart < spEnd) span.copy(start = spStart, end = spEnd) else null
                    }
                    block.copy(
                        text = block.text.substring(sliceStart, sliceEnd),
                        spans = subSpans,
                        listType = if (pageItem.isContinuation) ListType.NONE else block.listType
                    )
                } else {
                    block
                }
            }

            val itemNumber = if (!pageItem.isContinuation && block.listType == ListType.NUMBERED) {
                globalListNumbers[pageItem.originalBlockIndex] ?: 1
            } else {
                null
            }

            BlockItemView(
                block = displayBlock,
                itemNumber = itemNumber,
                isActive = pageItem.originalBlockIndex == activeBlockIndex,
                isDarkPage = isDarkPage,
                zoomScale = zoomScale,
                canDeleteBlock = document.blocks.size > 1,
                onFocus = { onBlockSelected(pageItem.originalBlockIndex) },
                onSelectionChanged = { sel ->
                    onSelectionChangedForBlock(
                        pageItem.originalBlockIndex,
                        TextRange(
                            (sliceStart + sel.start).coerceIn(0, block.text.length),
                            (sliceStart + sel.end).coerceIn(0, block.text.length)
                        )
                    )
                },
                onTextChanged = { newText, sel ->
                    if (isSplitSlice) {
                        onUpdateSubSliceText(pageItem.originalBlockIndex, sliceStart, sliceEnd, newText, sel)
                    } else {
                        onUpdateBlockText(pageItem.originalBlockIndex, newText, sel)
                    }
                },
                onEnterPressed = { cursorOffset ->
                    onSplitParagraph(pageItem.originalBlockIndex, sliceStart + cursorOffset)
                },
                onDeleteBlock = { onDeleteBlockById(block.id) },
                onDeleteOrMergeWithPrevious = { onDeleteOrMergeWithPrevious(pageItem.originalBlockIndex) },
                documentBlocks = document.blocks,
                paginatedPages = paginatedPages,
                onNavigateToBlock = { onBlockSelected(it) },
                onRefreshTOC = onRefreshTOC,
                onToggleChecklistItem = onToggleChecklistItem,
                isDocumentAllSelected = isDocumentAllSelected,
                onSelectAllDocument = onSelectAllDocument,
                onDeleteAllDocument = onDeleteAllDocument,
                onReplaceAllDocumentWithText = onReplaceAllDocumentWithText,
                onLongPress = onLongPress
            )
            Spacer(modifier = Modifier.height(6.dp))
        }

        is TablePageItem -> {
            TableBlockView(
                table = pageItem.originalBlock,
                startRow = pageItem.startRow,
                endRow = pageItem.endRow,
                isDarkPage = isDarkPage,
                zoomScale = zoomScale,
                onEditProperties = { onEditTableProperties(pageItem.originalBlock) },
                onEditCell = { r, c -> onEditTableCell(pageItem.originalBlock, r, c) },
                onAddRow = { onQuickAddTableRow(pageItem.originalBlock.id) },
                onDeleteRow = { onQuickDeleteTableRow(pageItem.originalBlock.id) },
                onAddCol = { onQuickAddTableCol(pageItem.originalBlock.id) },
                onDeleteCol = { onQuickDeleteTableCol(pageItem.originalBlock.id) }
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        is ImagePageItem -> {
            ImageBlockView(
                imageBlock = pageItem.originalBlock,
                zoomScale = zoomScale,
                onFocus = { onBlockSelected(pageItem.originalBlockIndex) },
                onEditProperties = { onEditImageProperties(pageItem.originalBlock) },
                onPositionAndSizeChanged = { ox, oy, w, h -> onUpdateImagePositionAndSize(pageItem.originalBlock.id, ox, oy, w, h) }
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        else -> {
            PaginatedSpecialItemRenderer(
                pageItem = pageItem,
                document = document,
                paginatedPages = paginatedPages,
                isDarkPage = isDarkPage,
                zoomScale = zoomScale,
                activeBlockIndex = activeBlockIndex,
                onBlockSelected = onBlockSelected,
                onDeleteBlockById = onDeleteBlockById,
                onRefreshTOC = onRefreshTOC,
                onUpdateDrawingStrokes = onUpdateDrawingStrokes,
                drawTool = drawTool,
                drawColorHex = drawColorHex,
                drawStrokeWidthPt = drawStrokeWidthPt,
                onSetDrawTool = onSetDrawTool,
                onSetDrawColorHex = onSetDrawColorHex,
                onSetDrawStrokeWidthPt = onSetDrawStrokeWidthPt,
                onUpdateTextBox = onUpdateTextBox,
                onUpdateTextBoxGeometry = onUpdateTextBoxGeometry,
                onUpdateTextBoxColor = onUpdateTextBoxColor
            )
        }
    }
}
