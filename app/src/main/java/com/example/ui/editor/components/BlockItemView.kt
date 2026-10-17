package com.example.ui.editor.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.text.TextRange
import com.example.core.layout.DocumentPage
import com.example.data.model.BlockElement
import com.example.data.model.ColumnBreakBlock
import com.example.data.model.DividerBlock
import com.example.data.model.DrawingBlock
import com.example.data.model.DrawingStroke
import com.example.data.model.FootnoteBlock
import com.example.data.model.ImageBlock
import com.example.data.model.PageBreakBlock
import com.example.data.model.ParagraphBlock
import com.example.data.model.SectionBreakBlock
import com.example.data.model.ShapeBlock
import com.example.data.model.SignatureBlock
import com.example.data.model.TableBlock
import com.example.data.model.TableOfContentsBlock
import com.example.data.model.TextBoxBlock
import com.example.ui.editor.components.blocks.ColumnBreakBlockItem
import com.example.ui.editor.components.blocks.DividerBlockItem
import com.example.ui.editor.components.blocks.FootnoteBlockItem
import com.example.ui.editor.components.blocks.PageBreakBlockItem
import com.example.ui.editor.components.blocks.ParagraphBlockView
import com.example.ui.editor.components.blocks.SectionBreakBlockItem
import com.example.ui.editor.components.blocks.SignatureBlockItem
import com.example.ui.editor.components.blocks.TocBlockItem

@Composable
fun BlockItemView(
    block: BlockElement,
    itemNumber: Int?,
    isActive: Boolean,
    isDarkPage: Boolean = false,
    zoomScale: Float = 1f,
    canDeleteBlock: Boolean = false,
    isDocumentAllSelected: Boolean = false,
    onFocus: () -> Unit,
    onSelectionChanged: (TextRange) -> Unit,
    onTextChanged: (String, TextRange) -> Unit,
    onEnterPressed: (Int) -> Unit = {},
    onDeleteBlock: () -> Unit = {},
    onDeleteOrMergeWithPrevious: () -> Unit = {},
    onEditTableProperties: () -> Unit = {},
    onEditTableCell: (Int, Int) -> Unit = { _, _ -> },
    onAddTableRow: () -> Unit = {},
    onDeleteTableRow: () -> Unit = {},
    onAddTableCol: () -> Unit = {},
    onDeleteTableCol: () -> Unit = {},
    onEditImageProperties: () -> Unit = {},
    documentBlocks: List<BlockElement> = emptyList(),
    paginatedPages: List<DocumentPage> = emptyList(),
    onNavigateToBlock: (Int) -> Unit = {},
    onRefreshTOC: () -> Unit = {},
    onToggleChecklistItem: (String) -> Unit = {},
    onUpdateDrawingStrokes: (List<DrawingStroke>) -> Unit = {},
    drawTool: String = "PEN",
    drawColorHex: String = "#0F172A",
    drawStrokeWidthPt: Float = 2.5f,
    onSetDrawTool: (String) -> Unit = {},
    onSetDrawColorHex: (String) -> Unit = {},
    onSetDrawStrokeWidthPt: (Float) -> Unit = {},
    onUpdateTextBox: (String) -> Unit = {},
    onUpdateTextBoxGeometry: (String, Float, Float, Int, Int) -> Unit = { _, _, _, _, _ -> },
    onUpdateTextBoxColor: (String, String, String) -> Unit = { _, _, _ -> },
    onUpdateImagePositionAndSize: (String, Float, Float, Int, Int) -> Unit = { _, _, _, _, _ -> },
    onEditShape: () -> Unit = {},
    onUpdateShapeGeometry: (String, Int, Int, Float) -> Unit = { _, _, _, _ -> },
    onUpdateShapeText: (String, String, String) -> Unit = { _, _, _ -> },
    onDuplicateShape: (String) -> Unit = {},
    onCutShape: (String) -> Unit = {},
    onCopyShape: (String) -> Unit = {},
    onAddCommentForBlock: (String) -> Unit = {},
    onSelectAllDocument: () -> Unit = {},
    onDeleteAllDocument: () -> Unit = {},
    onReplaceAllDocumentWithText: (String) -> Unit = {},
    onLongPress: (Offset) -> Unit = {}
) {
    when (block) {
        is ParagraphBlock -> {
            ParagraphBlockView(
                block = block,
                itemNumber = itemNumber,
                isActive = isActive,
                isDarkPage = isDarkPage,
                zoomScale = zoomScale,
                canDeleteBlock = canDeleteBlock,
                isDocumentAllSelected = isDocumentAllSelected,
                onFocus = onFocus,
                onSelectionChanged = onSelectionChanged,
                onTextChanged = onTextChanged,
                onEnterPressed = onEnterPressed,
                onDeleteOrMergeWithPrevious = onDeleteOrMergeWithPrevious,
                onToggleChecklistItem = onToggleChecklistItem,
                onSelectAllDocument = onSelectAllDocument,
                onDeleteAllDocument = onDeleteAllDocument,
                onReplaceAllDocumentWithText = onReplaceAllDocumentWithText,
                onLongPress = onLongPress
            )
        }
        is TableBlock -> {
            TableBlockView(
                table = block,
                isDarkPage = isDarkPage,
                zoomScale = zoomScale,
                onEditProperties = onEditTableProperties,
                onEditCell = onEditTableCell,
                onAddRow = onAddTableRow,
                onDeleteRow = onDeleteTableRow,
                onAddCol = onAddTableCol,
                onDeleteCol = onDeleteTableCol
            )
        }
        is ImageBlock -> {
            ImageBlockView(
                imageBlock = block,
                zoomScale = zoomScale,
                onFocus = onFocus,
                onEditProperties = onEditImageProperties,
                onPositionAndSizeChanged = { ox, oy, w, h -> onUpdateImagePositionAndSize(block.id, ox, oy, w, h) }
            )
        }
        is DividerBlock -> {
            DividerBlockItem(
                block = block,
                isDarkPage = isDarkPage,
                canDeleteBlock = canDeleteBlock,
                onDeleteBlock = onDeleteBlock
            )
        }
        is PageBreakBlock -> {
            PageBreakBlockItem(
                block = block,
                canDeleteBlock = canDeleteBlock,
                onDeleteBlock = onDeleteBlock
            )
        }
        is ColumnBreakBlock -> {
            ColumnBreakBlockItem(
                block = block,
                canDeleteBlock = canDeleteBlock,
                onDeleteBlock = onDeleteBlock
            )
        }
        is SectionBreakBlock -> {
            SectionBreakBlockItem(
                block = block,
                canDeleteBlock = canDeleteBlock,
                onDeleteBlock = onDeleteBlock
            )
        }
        is TableOfContentsBlock -> {
            TocBlockItem(
                block = block,
                isDarkPage = isDarkPage,
                zoomScale = zoomScale,
                canDeleteBlock = canDeleteBlock,
                documentBlocks = documentBlocks,
                paginatedPages = paginatedPages,
                onRefreshTOC = onRefreshTOC,
                onNavigateToBlock = onNavigateToBlock,
                onDeleteBlock = onDeleteBlock
            )
        }
        is SignatureBlock -> {
            SignatureBlockItem(
                block = block,
                isDarkPage = isDarkPage,
                zoomScale = zoomScale,
                canDeleteBlock = canDeleteBlock,
                onDeleteBlock = onDeleteBlock
            )
        }
        is ShapeBlock -> {
            ShapeBlockView(
                block = block,
                isActive = isActive,
                zoomScale = zoomScale,
                onFocus = onFocus,
                onEditShape = onEditShape,
                onDeleteShape = onDeleteBlock,
                onDuplicateShape = { onDuplicateShape(block.id) },
                onCutShape = { onCutShape(block.id) },
                onCopyShape = { onCopyShape(block.id) },
                onAddComment = { onAddCommentForBlock(block.id) },
                onUpdateGeometry = { w, h, rot -> onUpdateShapeGeometry(block.id, w, h, rot) },
                onUpdateText = { t, c -> onUpdateShapeText(block.id, t, c) }
            )
        }
        is DrawingBlock -> {
            DrawingBlockView(
                block = block,
                isDarkPage = isDarkPage,
                zoomScale = zoomScale,
                currentTool = drawTool,
                currentColorHex = drawColorHex,
                currentStrokeWidthPt = drawStrokeWidthPt,
                onToolChanged = onSetDrawTool,
                onColorChanged = onSetDrawColorHex,
                onStrokeWidthChanged = onSetDrawStrokeWidthPt,
                onStrokesChanged = onUpdateDrawingStrokes,
                onDeleteBlock = onDeleteBlock
            )
        }
        is TextBoxBlock -> {
            TextBoxBlockView(
                block = block,
                zoomScale = zoomScale,
                onTextChanged = onUpdateTextBox,
                onDeleteBlock = onDeleteBlock,
                onGeometryChanged = { ox, oy, w, h -> onUpdateTextBoxGeometry(block.id, ox, oy, w, h) },
                onColorChanged = { bg, bdr -> onUpdateTextBoxColor(block.id, bg, bdr) }
            )
        }
        is FootnoteBlock -> {
            FootnoteBlockItem(
                block = block,
                isDarkPage = isDarkPage,
                zoomScale = zoomScale,
                canDeleteBlock = canDeleteBlock,
                onDeleteBlock = onDeleteBlock
            )
        }
    }
}
