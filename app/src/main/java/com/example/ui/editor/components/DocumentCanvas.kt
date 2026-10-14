package com.example.ui.editor.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.layout.DocumentPage
import com.example.core.layout.PaginationEngine
import com.example.data.model.BlockElement
import com.example.data.model.ImageBlock
import com.example.data.model.ListType
import com.example.data.model.ParagraphBlock
import com.example.data.model.RichDocument
import com.example.data.model.TableBlock
import com.example.data.model.TextAlignment
import com.example.ui.editor.EditorViewMode
import com.example.ui.theme.WordPrimary

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
    onUpdateDrawingStrokes: (String, List<com.example.data.model.DrawingStroke>) -> Unit = { _, _ -> },
    onUpdateTextBox: (String, String) -> Unit = { _, _ -> },
    onEditShape: (com.example.data.model.ShapeBlock) -> Unit = {}
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
                    com.example.core.layout.PageBorderStyle.SINGLE_THIN -> androidx.compose.foundation.BorderStroke(1.dp, Color(try { android.graphics.Color.parseColor(page.layout.borderColorHex) } catch (_: Exception) { android.graphics.Color.LTGRAY }))
                    com.example.core.layout.PageBorderStyle.SINGLE_THICK -> androidx.compose.foundation.BorderStroke(2.dp, Color(try { android.graphics.Color.parseColor(page.layout.borderColorHex) } catch (_: Exception) { android.graphics.Color.LTGRAY }))
                    com.example.core.layout.PageBorderStyle.DOUBLE -> androidx.compose.foundation.BorderStroke(3.dp, Color(try { android.graphics.Color.parseColor(page.layout.borderColorHex) } catch (_: Exception) { android.graphics.Color.LTGRAY }))
                    com.example.core.layout.PageBorderStyle.DASHED -> androidx.compose.foundation.BorderStroke(1.5.dp, Color(try { android.graphics.Color.parseColor(page.layout.borderColorHex) } catch (_: Exception) { android.graphics.Color.LTGRAY }))
                    com.example.core.layout.PageBorderStyle.NONE -> null
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
                        // Watermark rotated in sheet background
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
                        // Header Area
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

                        // Content items
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
                            val contentItems = page.items.filter { it !is com.example.core.layout.FootnotePageItem }
                            val footnoteItems = page.items.filterIsInstance<com.example.core.layout.FootnotePageItem>()

                            @Composable
                            fun RenderPageItem(pageItem: com.example.core.layout.PageItem) {
                                when (pageItem) {
                                    is com.example.core.layout.ParagraphPageItem -> {
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
                                            onToggleChecklistItem = onToggleChecklistItem
                                        )
                                        Spacer(modifier = Modifier.height(6.dp))
                                    }

                                    is com.example.core.layout.TablePageItem -> {
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

                                    is com.example.core.layout.ImagePageItem -> {
                                        ImageBlockView(
                                            imageBlock = pageItem.originalBlock,
                                            zoomScale = zoomScale,
                                            onFocus = { onBlockSelected(pageItem.originalBlockIndex) },
                                            onEditProperties = { onEditImageProperties(pageItem.originalBlock) }
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                    }

                                    is com.example.core.layout.DividerPageItem -> {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 4.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            HorizontalDivider(
                                                color = if (isDarkPage) Color(0xFF475569) else Color(0xFFCBD5E1),
                                                thickness = 1.dp,
                                                modifier = Modifier.weight(1f)
                                            )
                                            if (document.blocks.size > 1) {
                                                IconButton(
                                                    onClick = { onDeleteBlockById(pageItem.originalBlock.id) },
                                                    modifier = Modifier.size(24.dp).testTag("delete_divider_${pageItem.originalBlock.id}")
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Delete,
                                                        contentDescription = "Delete Horizontal Line",
                                                        tint = if (isDarkPage) Color(0xFF94A3B8) else Color.Gray,
                                                        modifier = Modifier.size(14.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }

                                    is com.example.core.layout.PageBreakPageItem -> {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 4.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            HorizontalDivider(modifier = Modifier.weight(1f), color = WordPrimary.copy(alpha = 0.35f))
                                            Text(
                                                text = " PAGE BREAK ",
                                                fontSize = 9.sp,
                                                color = WordPrimary,
                                                fontWeight = FontWeight.Bold
                                            )
                                            HorizontalDivider(modifier = Modifier.weight(1f), color = WordPrimary.copy(alpha = 0.35f))
                                            if (document.blocks.size > 1) {
                                                IconButton(
                                                    onClick = { onDeleteBlockById(pageItem.originalBlock.id) },
                                                    modifier = Modifier.size(24.dp).testTag("delete_page_break_${pageItem.originalBlock.id}")
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Delete,
                                                        contentDescription = "Remove Page Break",
                                                        tint = WordPrimary,
                                                        modifier = Modifier.size(14.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }

                                    is com.example.core.layout.TableOfContentsPageItem -> {
                                        BlockItemView(
                                            block = pageItem.originalBlock,
                                            itemNumber = null,
                                            isActive = false,
                                            isDarkPage = isDarkPage,
                                            zoomScale = zoomScale,
                                            canDeleteBlock = document.blocks.size > 1,
                                            onFocus = { onBlockSelected(pageItem.originalBlockIndex) },
                                            onSelectionChanged = {},
                                            onTextChanged = { _, _ -> },
                                            onDeleteBlock = { onDeleteBlockById(pageItem.originalBlock.id) },
                                            documentBlocks = document.blocks,
                                            paginatedPages = paginatedPages,
                                            onNavigateToBlock = { onBlockSelected(it) },
                                            onRefreshTOC = onRefreshTOC
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                    }

                                    is com.example.core.layout.SignaturePageItem -> {
                                        BlockItemView(
                                            block = pageItem.originalBlock,
                                            itemNumber = null,
                                            isActive = false,
                                            isDarkPage = isDarkPage,
                                            zoomScale = zoomScale,
                                            canDeleteBlock = document.blocks.size > 1,
                                            onFocus = { onBlockSelected(pageItem.originalBlockIndex) },
                                            onSelectionChanged = {},
                                            onTextChanged = { _, _ -> },
                                            onDeleteBlock = { onDeleteBlockById(pageItem.originalBlock.id) }
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                    }

                                    is com.example.core.layout.ShapePageItem -> {
                                        BlockItemView(
                                            block = pageItem.originalBlock,
                                            itemNumber = null,
                                            isActive = false,
                                            isDarkPage = isDarkPage,
                                            zoomScale = zoomScale,
                                            canDeleteBlock = document.blocks.size > 1,
                                            onFocus = { onBlockSelected(pageItem.originalBlockIndex) },
                                            onSelectionChanged = {},
                                            onTextChanged = { _, _ -> },
                                            onDeleteBlock = { onDeleteBlockById(pageItem.originalBlock.id) }
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                    }

                                    is com.example.core.layout.FootnotePageItem -> {
                                        BlockItemView(
                                            block = pageItem.originalBlock,
                                            itemNumber = null,
                                            isActive = false,
                                            isDarkPage = isDarkPage,
                                            zoomScale = zoomScale,
                                            canDeleteBlock = document.blocks.size > 1,
                                            onFocus = { onBlockSelected(pageItem.originalBlockIndex) },
                                            onSelectionChanged = {},
                                            onTextChanged = { _, _ -> },
                                            onDeleteBlock = { onDeleteBlockById(pageItem.originalBlock.id) },
                                            documentBlocks = document.blocks,
                                            paginatedPages = paginatedPages,
                                            onNavigateToBlock = { onBlockSelected(it) },
                                            onRefreshTOC = onRefreshTOC
                                        )
                                        Spacer(modifier = Modifier.height(6.dp))
                                    }

                                    is com.example.core.layout.ColumnBreakPageItem -> {
                                        BlockItemView(
                                            block = pageItem.originalBlock,
                                            itemNumber = null,
                                            isActive = false,
                                            isDarkPage = isDarkPage,
                                            zoomScale = zoomScale,
                                            canDeleteBlock = document.blocks.size > 1,
                                            onFocus = { onBlockSelected(pageItem.originalBlockIndex) },
                                            onSelectionChanged = {},
                                            onTextChanged = { _, _ -> },
                                            onDeleteBlock = { onDeleteBlockById(pageItem.originalBlock.id) }
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                    }

                                    is com.example.core.layout.SectionBreakPageItem -> {
                                        BlockItemView(
                                            block = pageItem.originalBlock,
                                            itemNumber = null,
                                            isActive = false,
                                            isDarkPage = isDarkPage,
                                            zoomScale = zoomScale,
                                            canDeleteBlock = document.blocks.size > 1,
                                            onFocus = { onBlockSelected(pageItem.originalBlockIndex) },
                                            onSelectionChanged = {},
                                            onTextChanged = { _, _ -> },
                                            onDeleteBlock = { onDeleteBlockById(pageItem.originalBlock.id) }
                                        )
                                        Spacer(modifier = Modifier.height(6.dp))
                                    }
                                }
                            }

                            // 2-Column Newspaper Layout vs Standard 1-Column Layout
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
                                            RenderPageItem(item)
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
                                            RenderPageItem(item)
                                        }
                                    }
                                }
                            } else {
                                contentItems.forEach { item ->
                                    RenderPageItem(item)
                                }
                            }

                            // Docked Footnotes at bottom of page above footer
                            if (footnoteItems.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(16.dp))
                                HorizontalDivider(
                                    color = if (isDarkPage) Color(0xFF475569) else Color(0xFFCBD5E1),
                                    thickness = 1.dp,
                                    modifier = Modifier.width(90.dp)
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                footnoteItems.forEach { fnItem ->
                                    RenderPageItem(fnItem)
                                }
                            }
                        }

                        // Footer Area
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
        } else {
            // Continuous Mobile View
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
                        onToggleChecklistItem = onToggleChecklistItem
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
    }
}
