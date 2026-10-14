package com.example.ui.editor.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.unit.dp
import com.example.core.layout.DocumentPage
import com.example.core.layout.PaginationEngine
import com.example.data.model.DrawingStroke
import com.example.data.model.ImageBlock
import com.example.data.model.ListType
import com.example.data.model.ParagraphBlock
import com.example.data.model.RichDocument
import com.example.data.model.ShapeBlock
import com.example.data.model.TableBlock
import com.example.ui.editor.EditorViewMode
import com.example.ui.editor.components.canvas.ContinuousSheetView
import com.example.ui.editor.components.canvas.PaginatedSheetView

@Composable
fun DocumentCanvas(
    document: RichDocument,
    paginatedPages: List<DocumentPage>,
    viewMode: EditorViewMode,
    zoomPercent: Int,
    showRuler: Boolean = true,
    isTabletOrExpanded: Boolean,
    activeBlockIndex: Int,
    onBlockSelected: (Int) -> Unit,
    onUpdateBlockText: (Int, String, TextRange) -> Unit,
    onUpdateSubSliceText: (Int, Int, Int, String, TextRange) -> Unit = { idx, _, _, text, sel ->
        onUpdateBlockText(idx, text, sel)
    },
    onSelectionChanged: (TextRange) -> Unit,
    onInsertParagraph: () -> Unit,
    onDeleteCurrentBlock: () -> Unit,
    onDeleteBlockById: (String) -> Unit = {},
    onUpdateUnifiedText: (String, TextRange) -> Unit = { _, _ -> },
    onDeleteOrMergeWithPrevious: (Int) -> Unit = {},
    onEditTableProperties: (TableBlock) -> Unit = {},
    onEditTableCell: (TableBlock, Int, Int) -> Unit = { _, _, _ -> },
    onQuickAddTableRow: (String) -> Unit = {},
    onQuickDeleteTableRow: (String) -> Unit = {},
    onQuickAddTableCol: (String) -> Unit = {},
    onQuickDeleteTableCol: (String) -> Unit = {},
    onEditImageProperties: (ImageBlock) -> Unit = {},
    onOpenPageLayoutDialog: () -> Unit = {},
    onSelectionChangedForBlock: (Int, TextRange) -> Unit = { _, _ -> },
    onSplitParagraph: (Int, Int) -> Unit = { _, _ -> },
    onRefreshTOC: () -> Unit = {},
    onToggleChecklistItem: (String) -> Unit = {},
    onUpdateDrawingStrokes: (String, List<DrawingStroke>) -> Unit = { _, _ -> },
    onUpdateTextBox: (String, String) -> Unit = { _, _ -> },
    onEditShape: (ShapeBlock) -> Unit = {}
) {
    val scrollState = rememberScrollState()
    val zoomScale = (zoomPercent / 100f).coerceIn(0.5f, 2.0f)

    val globalListNumbers = remember(document.blocks) {
        val map = mutableMapOf<Int, Int>()
        var counter = 1
        document.blocks.forEachIndexed { idx, block ->
            if (block is ParagraphBlock && block.listType == ListType.NUMBERED) {
                map[idx] = counter++
            } else {
                counter = 1
            }
        }
        map
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(if (isTabletOrExpanded) 28.dp else 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (viewMode == EditorViewMode.PAGINATED_A4) {
            val pagesToDisplay = if (paginatedPages.isNotEmpty()) {
                paginatedPages
            } else {
                PaginationEngine.paginate(document)
            }

            val a4PortraitReferenceWidth = if (isTabletOrExpanded) 720f else 580f
            val baseSheetWidth = (document.layoutConfig.pageWidthPt.toFloat() / 595f) * a4PortraitReferenceWidth
            val scaledMaxSheetWidth = (baseSheetWidth * zoomScale).dp

            PaginatedSheetView(
                document = document,
                pagesToDisplay = pagesToDisplay,
                globalListNumbers = globalListNumbers,
                activeBlockIndex = activeBlockIndex,
                zoomScale = zoomScale,
                scaledMaxSheetWidth = scaledMaxSheetWidth,
                showRuler = showRuler,
                onBlockSelected = onBlockSelected,
                onUpdateBlockText = onUpdateBlockText,
                onUpdateSubSliceText = onUpdateSubSliceText,
                onSelectionChangedForBlock = onSelectionChangedForBlock,
                onSplitParagraph = onSplitParagraph,
                onDeleteBlockById = onDeleteBlockById,
                onDeleteOrMergeWithPrevious = onDeleteOrMergeWithPrevious,
                onEditTableProperties = onEditTableProperties,
                onEditTableCell = onEditTableCell,
                onQuickAddTableRow = onQuickAddTableRow,
                onQuickDeleteTableRow = onQuickDeleteTableRow,
                onQuickAddTableCol = onQuickAddTableCol,
                onQuickDeleteTableCol = onQuickDeleteTableCol,
                onEditImageProperties = onEditImageProperties,
                onRefreshTOC = onRefreshTOC,
                onToggleChecklistItem = onToggleChecklistItem
            )
        } else {
            ContinuousSheetView(
                document = document,
                paginatedPages = paginatedPages,
                globalListNumbers = globalListNumbers,
                activeBlockIndex = activeBlockIndex,
                zoomScale = zoomScale,
                showRuler = showRuler,
                onBlockSelected = onBlockSelected,
                onUpdateBlockText = onUpdateBlockText,
                onSelectionChangedForBlock = onSelectionChangedForBlock,
                onSplitParagraph = onSplitParagraph,
                onDeleteBlockById = onDeleteBlockById,
                onDeleteOrMergeWithPrevious = onDeleteOrMergeWithPrevious,
                onEditTableProperties = onEditTableProperties,
                onEditTableCell = onEditTableCell,
                onQuickAddTableRow = onQuickAddTableRow,
                onQuickDeleteTableRow = onQuickDeleteTableRow,
                onQuickAddTableCol = onQuickAddTableCol,
                onQuickDeleteTableCol = onQuickDeleteTableCol,
                onEditImageProperties = onEditImageProperties,
                onRefreshTOC = onRefreshTOC,
                onToggleChecklistItem = onToggleChecklistItem
            )
        }
    }
}
