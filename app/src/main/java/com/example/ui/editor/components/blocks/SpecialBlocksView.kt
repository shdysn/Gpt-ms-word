package com.example.ui.editor.components.blocks

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ListAlt
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.layout.DocumentPage
import com.example.data.model.BlockElement
import com.example.data.model.FootnoteBlock
import com.example.data.model.HeadingStyle
import com.example.data.model.ParagraphBlock
import com.example.data.model.TableOfContentsBlock
import com.example.ui.theme.WordPrimary

@Composable
fun TocBlockItem(
    block: TableOfContentsBlock,
    isDarkPage: Boolean,
    zoomScale: Float,
    canDeleteBlock: Boolean,
    documentBlocks: List<BlockElement>,
    paginatedPages: List<DocumentPage>,
    onRefreshTOC: () -> Unit,
    onNavigateToBlock: (Int) -> Unit,
    onDeleteBlock: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = if (isDarkPage) Color(0xFF1E293B) else Color(0xFFF8FAFC),
        border = androidx.compose.foundation.BorderStroke(1.dp, if (isDarkPage) Color(0xFF334155) else Color(0xFFE2E8F0)),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .testTag("toc_block_${block.id}")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ListAlt,
                    contentDescription = null,
                    tint = WordPrimary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = block.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = (16f * zoomScale).sp,
                    color = if (isDarkPage) Color(0xFFF1F5F9) else Color(0xFF0F172A),
                    modifier = Modifier.weight(1f)
                )
                IconButton(
                    onClick = onRefreshTOC,
                    modifier = Modifier.size(24.dp).testTag("refresh_toc_btn")
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = "Refresh Table of Contents", tint = WordPrimary, modifier = Modifier.size(16.dp))
                }
                Spacer(modifier = Modifier.width(4.dp))
                if (canDeleteBlock) {
                    IconButton(onClick = onDeleteBlock, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete TOC", tint = Color.Gray, modifier = Modifier.size(14.dp))
                    }
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = if (isDarkPage) Color(0xFF334155) else Color(0xFFE2E8F0))
            Spacer(modifier = Modifier.height(8.dp))

            val headings = documentBlocks.filterIsInstance<ParagraphBlock>().filter { it.headingStyle != HeadingStyle.NORMAL && it.text.isNotBlank() }
            if (headings.isEmpty()) {
                Text(
                    text = "No headings found in document. Set paragraphs to Heading 1, 2, or 3 to populate.",
                    fontSize = 12.sp,
                    color = Color.Gray,
                    fontStyle = FontStyle.Italic
                )
            } else {
                headings.forEachIndexed { hIdx, hBlock ->
                    val indentDp = when (hBlock.headingStyle) {
                        HeadingStyle.TITLE, HeadingStyle.HEADING_1 -> 0.dp
                        HeadingStyle.HEADING_2 -> 14.dp
                        else -> 28.dp
                    }
                    val targetBlockIndex = documentBlocks.indexOf(hBlock)
                    val realPageNumber = paginatedPages.firstOrNull { page ->
                        page.items.any { it.originalBlockIndex == targetBlockIndex }
                    }?.pageNumber ?: (hIdx + 1)

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = indentDp, top = 4.dp, bottom = 4.dp)
                            .clickable(enabled = targetBlockIndex >= 0) {
                                if (targetBlockIndex >= 0) {
                                    onNavigateToBlock(targetBlockIndex)
                                }
                            }
                            .testTag("toc_item_$hIdx"),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = hBlock.text.take(40),
                            fontSize = (13f * zoomScale).sp,
                            fontWeight = if (hBlock.headingStyle == HeadingStyle.TITLE || hBlock.headingStyle == HeadingStyle.HEADING_1) FontWeight.Bold else FontWeight.Normal,
                            color = if (isDarkPage) Color(0xFF93C5FD) else Color(0xFF1E40AF),
                            modifier = Modifier.weight(1f, fill = false)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = ". . . . . . . . . . . . . . . . . . . .",
                            color = if (isDarkPage) Color(0xFF475569) else Color(0xFFCBD5E1),
                            fontSize = 11.sp,
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "$realPageNumber",
                            fontSize = (12f * zoomScale).sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isDarkPage) Color(0xFF94A3B8) else Color(0xFF64748B)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun FootnoteBlockItem(
    block: FootnoteBlock,
    isDarkPage: Boolean,
    zoomScale: Float,
    canDeleteBlock: Boolean,
    onDeleteBlock: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(6.dp),
        color = if (isDarkPage) Color(0xFF1E293B) else Color(0xFFF8FAFC),
        border = androidx.compose.foundation.BorderStroke(1.dp, if (isDarkPage) Color(0xFF334155) else Color(0xFFE2E8F0)),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .testTag("footnote_block_${block.id}")
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            HorizontalDivider(
                thickness = 1.dp,
                color = if (isDarkPage) Color(0xFF475569) else Color(0xFFCBD5E1),
                modifier = Modifier.width(80.dp)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = "[${block.noteNumber}] ",
                    fontWeight = FontWeight.Bold,
                    fontSize = (11f * zoomScale).sp,
                    color = WordPrimary
                )
                Column(modifier = Modifier.weight(1f)) {
                    if (block.referenceText.isNotBlank()) {
                        Text(
                            text = "Ref: \"${block.referenceText}\"",
                            fontSize = (10f * zoomScale).sp,
                            fontStyle = FontStyle.Italic,
                            color = Color.Gray
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                    }
                    Text(
                        text = block.noteContent,
                        fontSize = (11f * zoomScale).sp,
                        color = if (isDarkPage) Color(0xFFE2E8F0) else Color(0xFF334155)
                    )
                }
                if (canDeleteBlock) {
                    IconButton(onClick = onDeleteBlock, modifier = Modifier.size(20.dp)) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete Footnote", tint = Color.Gray, modifier = Modifier.size(13.dp))
                    }
                }
            }
        }
    }
}
