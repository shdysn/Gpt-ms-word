package com.example.ui.editor.components.canvas

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.core.layout.DocumentPage
import com.example.data.model.ImageBlock
import com.example.data.model.ParagraphBlock
import com.example.data.model.RichDocument
import com.example.data.model.TableBlock
import com.example.ui.editor.components.DocumentRulerBar

@Composable
fun PaginatedSheetView(
    document: RichDocument,
    pagesToDisplay: List<DocumentPage>,
    globalListNumbers: Map<Int, Int>,
    activeBlockIndex: Int,
    zoomScale: Float,
    scaledMaxSheetWidth: Dp,
    showRuler: Boolean,
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
    onUpdateImagePositionAndSize: (String, Float, Float, Int, Int) -> Unit = { _, _, _, _, _ -> },
    onUpdateTextBox: (String, String) -> Unit = { _, _ -> },
    onUpdateTextBoxGeometry: (String, Float, Float, Int, Int) -> Unit = { _, _, _, _, _ -> },
    onUpdateTextBoxColor: (String, String, String) -> Unit = { _, _, _ -> }
) {
    pagesToDisplay.forEachIndexed { pageIdx, page ->
        if (showRuler && pageIdx == 0) {
            val activePara = document.blocks.getOrNull(activeBlockIndex) as? ParagraphBlock
            DocumentRulerBar(
                layout = document.layoutConfig,
                maxSheetWidth = scaledMaxSheetWidth,
                firstLineIndentPt = activePara?.firstLineIndentPt ?: 0f,
                hangingIndentPt = activePara?.hangingIndentPt ?: 0f,
                leftIndentPt = activePara?.leftIndentPt ?: 0f,
                tabStops = activePara?.tabStops ?: emptyList()
            )
        }

        SinglePageSheet(
            page = page,
            document = document,
            pagesToDisplay = pagesToDisplay,
            globalListNumbers = globalListNumbers,
            activeBlockIndex = activeBlockIndex,
            zoomScale = zoomScale,
            scaledMaxSheetWidth = scaledMaxSheetWidth,
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
            onToggleChecklistItem = onToggleChecklistItem,
            onUpdateImagePositionAndSize = onUpdateImagePositionAndSize,
            onUpdateTextBox = onUpdateTextBox,
            onUpdateTextBoxGeometry = onUpdateTextBoxGeometry,
            onUpdateTextBoxColor = onUpdateTextBoxColor
        )

        Spacer(modifier = Modifier.height(24.dp))
    }
}
