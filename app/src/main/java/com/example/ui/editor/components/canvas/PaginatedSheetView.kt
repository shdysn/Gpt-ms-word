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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.layout.DocumentPage
import com.example.core.layout.FootnotePageItem
import com.example.core.layout.PageBorderStyle
import com.example.data.model.ImageBlock
import com.example.data.model.ParagraphBlock
import com.example.data.model.RichDocument
import com.example.data.model.TableBlock
import com.example.data.model.TextAlignment
import com.example.ui.editor.components.BlockItemView
import com.example.ui.editor.components.DocumentRulerBar
import com.example.ui.editor.components.isColorDarkHex

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
    onToggleChecklistItem: (String) -> Unit
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

        val pageBgColor = try {
            Color(android.graphics.Color.parseColor(page.layout.pageBackgroundHex))
        } catch (_: Exception) { Color.White }
        val isDarkPage = isColorDarkHex(page.layout.pageBackgroundHex)

        val borderStroke = when (page.layout.borderStyle) {
            PageBorderStyle.SINGLE_THIN -> androidx.compose.foundation.BorderStroke(1.dp, Color(try { android.graphics.Color.parseColor(page.layout.borderColorHex) } catch (_: Exception) { android.graphics.Color.LTGRAY }))
            PageBorderStyle.SINGLE_THICK -> androidx.compose.foundation.BorderStroke(2.dp, Color(try { android.graphics.Color.parseColor(page.layout.borderColorHex) } catch (_: Exception) { android.graphics.Color.LTGRAY }))
            PageBorderStyle.DOUBLE -> androidx.compose.foundation.BorderStroke(3.dp, Color(try { android.graphics.Color.parseColor(page.layout.borderColorHex) } catch (_: Exception) { android.graphics.Color.LTGRAY }))
            PageBorderStyle.DASHED -> androidx.compose.foundation.BorderStroke(1.5.dp, Color(try { android.graphics.Color.parseColor(page.layout.borderColorHex) } catch (_: Exception) { android.graphics.Color.LTGRAY }))
            PageBorderStyle.NONE -> null
        }

        val sheetHeightDp = (scaledMaxSheetWidth.value / page.layout.aspectRatio).dp
        val leftMarginDp = ((page.layout.leftMarginPt / page.layout.pageWidthPt) * scaledMaxSheetWidth.value).dp
        val rightMarginDp = ((page.layout.rightMarginPt / page.layout.pageWidthPt) * scaledMaxSheetWidth.value).dp
        val topMarginDp = ((page.layout.topMarginPt / page.layout.pageHeightPt) * sheetHeightDp.value).dp
        val bottomMarginDp = ((page.layout.bottomMarginPt / page.layout.pageHeightPt) * sheetHeightDp.value).dp

        Surface(
            modifier = Modifier
                .width(scaledMaxSheetWidth)
                .heightIn(min = sheetHeightDp)
                .shadow(2.dp, RoundedCornerShape(0.dp)),
            shape = RoundedCornerShape(0.dp),
            color = pageBgColor,
            border = borderStroke
        ) {
            Box(modifier = Modifier.fillMaxWidth()) {
                if (page.layout.watermarkText.isNotBlank()) {
                    val wmColor = try {
                        Color(android.graphics.Color.parseColor(page.layout.watermarkColorHex))
                    } catch (_: Exception) { Color(0x2594A3B8) }
                    Text(
                        text = page.layout.watermarkText,
                        fontSize = (46f * zoomScale).sp,
                        fontWeight = FontWeight.Bold,
                        color = wmColor,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .graphicsLayer(rotationZ = page.layout.watermarkAngleDeg)
                    )
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            start = leftMarginDp,
                            end = rightMarginDp,
                            top = topMarginDp,
                            bottom = bottomMarginDp
                        )
                ) {
                    if (page.hasHeader && page.formattedHeader.isNotBlank()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = when (page.layout.headerAlignment) {
                                TextAlignment.CENTER -> Arrangement.Center
                                TextAlignment.END -> Arrangement.End
                                else -> Arrangement.Start
                            }
                        ) {
                            Text(
                                text = page.formattedHeader,
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isDarkPage) Color(0xFFCBD5E1) else Color.Gray
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        HorizontalDivider(
                            color = if (isDarkPage) Color(0xFF334155) else Color(0xFFE5E7EB),
                            thickness = 0.5.dp
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    if (page.items.isEmpty()) {
                        val dummyBlock = document.blocks.firstOrNull() as? ParagraphBlock ?: ParagraphBlock()
                        BlockItemView(
                            block = dummyBlock,
                            itemNumber = null,
                            isActive = true,
                            isDarkPage = isDarkPage,
                            zoomScale = zoomScale,
                            canDeleteBlock = false,
                            onFocus = { onBlockSelected(0) },
                            onSelectionChanged = { sel -> onSelectionChangedForBlock(0, sel) },
                            onTextChanged = { newText, sel -> onUpdateBlockText(0, newText, sel) },
                            onEnterPressed = { cursorOffset -> onSplitParagraph(0, cursorOffset) },
                            onDeleteBlock = {},
                            onDeleteOrMergeWithPrevious = {}
                        )
                    } else {
                        val contentItems = page.items.filter { it !is FootnotePageItem }
                        val footnoteItems = page.items.filterIsInstance<FootnotePageItem>()

                        val isTwoColumn = page.layout.columnCount == 2
                        if (isTwoColumn && contentItems.size > 1) {
                            val mid = (contentItems.size + 1) / 2
                            val leftItems = contentItems.take(mid)
                            val rightItems = contentItems.drop(mid)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    leftItems.forEach { item ->
                                        PaginatedItemRenderer(
                                            pageItem = item,
                                            document = document,
                                            paginatedPages = pagesToDisplay,
                                            globalListNumbers = globalListNumbers,
                                            activeBlockIndex = activeBlockIndex,
                                            isDarkPage = isDarkPage,
                                            zoomScale = zoomScale,
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
                                    }
                                }
                                Box(
                                    modifier = Modifier
                                        .width(1.dp)
                                        .heightIn(min = 60.dp)
                                        .background(if (isDarkPage) Color(0xFF334155) else Color(0xFFE2E8F0))
                                )
                                Column(modifier = Modifier.weight(1f)) {
                                    rightItems.forEach { item ->
                                        PaginatedItemRenderer(
                                            pageItem = item,
                                            document = document,
                                            paginatedPages = pagesToDisplay,
                                            globalListNumbers = globalListNumbers,
                                            activeBlockIndex = activeBlockIndex,
                                            isDarkPage = isDarkPage,
                                            zoomScale = zoomScale,
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
                                    }
                                }
                            }
                        } else {
                            contentItems.forEach { item ->
                                PaginatedItemRenderer(
                                    pageItem = item,
                                    document = document,
                                    paginatedPages = pagesToDisplay,
                                    globalListNumbers = globalListNumbers,
                                    activeBlockIndex = activeBlockIndex,
                                    isDarkPage = isDarkPage,
                                    zoomScale = zoomScale,
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
                            }
                        }

                        if (footnoteItems.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(16.dp))
                            HorizontalDivider(
                                color = if (isDarkPage) Color(0xFF475569) else Color(0xFFCBD5E1),
                                thickness = 1.dp,
                                modifier = Modifier.width(90.dp)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            footnoteItems.forEach { fnItem ->
                                PaginatedItemRenderer(
                                    pageItem = fnItem,
                                    document = document,
                                    paginatedPages = pagesToDisplay,
                                    globalListNumbers = globalListNumbers,
                                    activeBlockIndex = activeBlockIndex,
                                    isDarkPage = isDarkPage,
                                    zoomScale = zoomScale,
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
                            }
                        }
                    }

                    if (page.hasFooter && page.formattedFooter.isNotBlank()) {
                        Spacer(modifier = Modifier.height(20.dp))
                        HorizontalDivider(
                            color = if (isDarkPage) Color(0xFF334155) else Color(0xFFE5E7EB),
                            thickness = 0.5.dp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = when (page.layout.footerAlignment) {
                                TextAlignment.START -> Arrangement.Start
                                TextAlignment.END -> Arrangement.End
                                else -> Arrangement.Center
                            }
                        ) {
                            Text(
                                text = page.formattedFooter,
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isDarkPage) Color(0xFFCBD5E1) else Color.Gray
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}
