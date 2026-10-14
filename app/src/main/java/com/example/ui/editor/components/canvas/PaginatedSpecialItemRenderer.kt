package com.example.ui.editor.components.canvas

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.layout.ColumnBreakPageItem
import com.example.core.layout.DividerPageItem
import com.example.core.layout.DocumentPage
import com.example.core.layout.DrawingPageItem
import com.example.core.layout.FootnotePageItem
import com.example.core.layout.PageBreakPageItem
import com.example.core.layout.PageItem
import com.example.core.layout.SectionBreakPageItem
import com.example.core.layout.ShapePageItem
import com.example.core.layout.SignaturePageItem
import com.example.core.layout.TableOfContentsPageItem
import com.example.core.layout.TextBoxPageItem
import com.example.data.model.RichDocument
import com.example.ui.editor.components.BlockItemView
import com.example.ui.theme.WordPrimary

@Composable
fun PaginatedSpecialItemRenderer(
    pageItem: PageItem,
    document: RichDocument,
    paginatedPages: List<DocumentPage>,
    isDarkPage: Boolean,
    zoomScale: Float,
    onBlockSelected: (Int) -> Unit,
    onDeleteBlockById: (String) -> Unit,
    onRefreshTOC: () -> Unit
) {
    when (pageItem) {
        is DividerPageItem -> {
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

        is PageBreakPageItem -> {
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

        is TableOfContentsPageItem -> {
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

        is SignaturePageItem -> {
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

        is ShapePageItem -> {
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

        is DrawingPageItem -> {
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

        is TextBoxPageItem -> {
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

        is FootnotePageItem -> {
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

        is ColumnBreakPageItem -> {
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

        is SectionBreakPageItem -> {
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

        else -> Unit
    }
}
