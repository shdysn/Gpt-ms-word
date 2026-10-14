package com.example.ui.editor.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ListAlt
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.layout.DocumentPage
import com.example.data.model.BlockElement
import com.example.data.model.HeadingStyle
import com.example.data.model.ParagraphBlock
import com.example.ui.theme.WordPrimary

fun collectOutlineHeadings(
    blocks: List<BlockElement>,
    paginatedPages: List<DocumentPage>
): List<OutlineHeadingItem> {
    val list = mutableListOf<OutlineHeadingItem>()
    blocks.forEachIndexed { idx, block ->
        if (block is ParagraphBlock && block.headingStyle != HeadingStyle.NORMAL && block.headingStyle != HeadingStyle.QUOTE) {
            val title = block.text.trim().ifBlank { "(Untitled Heading)" }
            val pIdx = paginatedPages.indexOfFirst { page ->
                page.items.any { it.originalBlockIndex == idx }
            }.coerceAtLeast(0)

            var sectionWords = 0
            for (nextIdx in (idx + 1) until blocks.size) {
                val nextB = blocks[nextIdx]
                if (nextB is ParagraphBlock && nextB.headingStyle != HeadingStyle.NORMAL && nextB.headingStyle != HeadingStyle.QUOTE) {
                    break
                }
                if (nextB is ParagraphBlock) {
                    sectionWords += nextB.text.split(Regex("\\s+")).count { it.isNotBlank() }
                }
            }

            list.add(
                OutlineHeadingItem(
                    blockIndex = idx,
                    title = title,
                    style = block.headingStyle,
                    pageNumber = pIdx + 1,
                    wordCount = sectionWords
                )
            )
        }
    }
    return list
}

@Composable
fun OutlineEmptyState(isEmpty: Boolean) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ListAlt,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.outline,
                modifier = Modifier.size(36.dp)
            )
            Text(
                text = if (isEmpty) "No Headings in Document" else "No matching headings found",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = if (isEmpty) {
                    "Format paragraphs as Title, Heading 1, 2, or 3 from the Home ribbon to build a quick clickable navigation tree."
                } else {
                    "Try searching for a different keyword."
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontStyle = FontStyle.Italic
            )
        }
    }
}

@Composable
fun OutlineHeadingCard(
    item: OutlineHeadingItem,
    isCurrent: Boolean,
    onClick: () -> Unit
) {
    val startPadding = when (item.style) {
        HeadingStyle.TITLE, HeadingStyle.HEADING_1 -> 0.dp
        HeadingStyle.HEADING_2 -> 16.dp
        HeadingStyle.HEADING_3 -> 28.dp
        HeadingStyle.SUBTITLE -> 8.dp
        else -> 0.dp
    }

    val (badgeLabel, badgeColor, badgeTextColor) = when (item.style) {
        HeadingStyle.TITLE -> Triple("TITLE", Color(0xFF1E3A8A), Color.White)
        HeadingStyle.HEADING_1 -> Triple("H1", WordPrimary, Color.White)
        HeadingStyle.HEADING_2 -> Triple("H2", Color(0xFF0D9488), Color.White)
        HeadingStyle.HEADING_3 -> Triple("H3", Color(0xFF7C3AED), Color.White)
        HeadingStyle.SUBTITLE -> Triple("SUB", Color(0xFF64748B), Color.White)
        else -> Triple("TXT", Color.Gray, Color.White)
    }

    Card(
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isCurrent) WordPrimary.copy(alpha = 0.09f) else MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(
            width = if (isCurrent) 1.5.dp else 0.5.dp,
            color = if (isCurrent) WordPrimary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = startPadding)
            .clickable(onClick = onClick)
            .testTag("outline_item_${item.blockIndex}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = badgeColor
                ) {
                    Text(
                        text = badgeLabel,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = badgeTextColor,
                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Text(
                    text = item.title,
                    style = when (item.style) {
                        HeadingStyle.TITLE, HeadingStyle.HEADING_1 -> MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                        HeadingStyle.HEADING_2 -> MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                        else -> MaterialTheme.typography.bodySmall
                    },
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = if (isCurrent) WordPrimary else MaterialTheme.colorScheme.onSurface
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                if (item.wordCount > 0) {
                    Text(
                        text = "${item.wordCount}w",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Text(
                        text = "p. ${item.pageNumber}",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun OutlineStatChip(
    icon: ImageVector,
    label: String,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(13.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
