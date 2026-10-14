package com.example.ui.editor.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ListAlt
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.layout.DocumentPage
import com.example.data.model.HeadingStyle
import com.example.data.model.ParagraphBlock
import com.example.data.model.RichDocument
import com.example.ui.theme.WordPrimary

data class OutlineHeadingItem(
    val blockIndex: Int,
    val title: String,
    val style: HeadingStyle,
    val pageNumber: Int,
    val wordCount: Int
)

/**
 * Interactive Document Outline Navigation Sheet.
 * Displays semantic headings hierarchy (Title, H1, H2, H3), section word counts,
 * page numbers, and reading time with 1-click jump navigation.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DocumentOutlineSheet(
    document: RichDocument,
    paginatedPages: List<DocumentPage>,
    activeBlockIndex: Int,
    onDismiss: () -> Unit,
    onNavigateToHeading: (blockIndex: Int, pageIndex: Int) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var searchQuery by remember { mutableStateOf("") }

    // Collect all headings from document blocks with accurate page numbers
    val headingItems = remember(document.blocks, paginatedPages) {
        val list = mutableListOf<OutlineHeadingItem>()
        val blocks = document.blocks

        blocks.forEachIndexed { idx, block ->
            if (block is ParagraphBlock && block.headingStyle != HeadingStyle.NORMAL && block.headingStyle != HeadingStyle.QUOTE) {
                val title = block.text.trim().ifBlank { "(Untitled Heading)" }

                // Find corresponding page in paginatedPages
                val pIdx = paginatedPages.indexOfFirst { page ->
                    page.items.any { it.originalBlockIndex == idx }
                }.coerceAtLeast(0)

                // Calculate approximate word count for this heading's section
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
        list
    }

    val filteredItems = remember(headingItems, searchQuery) {
        if (searchQuery.isBlank()) {
            headingItems
        } else {
            headingItems.filter { it.title.contains(searchQuery, ignoreCase = true) }
        }
    }

    val totalWords = document.wordCount
    val estimatedMinutes = (totalWords / 200).coerceAtLeast(1)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = Modifier.testTag("document_outline_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = WordPrimary.copy(alpha = 0.12f),
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ListAlt,
                                contentDescription = null,
                                tint = WordPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Document Outline",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${headingItems.size} headings • $totalWords words • ~$estimatedMinutes min read",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.testTag("close_outline_btn")
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Close Outline")
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Quick Stats Banner
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatChip(
                    icon = Icons.Default.Description,
                    label = "$totalWords words",
                    modifier = Modifier.weight(1f)
                )
                StatChip(
                    icon = Icons.AutoMirrored.Filled.MenuBook,
                    label = "${paginatedPages.size.coerceAtLeast(1)} pages",
                    modifier = Modifier.weight(1f)
                )
                StatChip(
                    icon = Icons.Default.Timer,
                    label = "~$estimatedMinutes min",
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Search filter if there are several headings
            if (headingItems.size > 4) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Filter headings...", fontSize = 13.sp) },
                    leadingIcon = {
                        Icon(
                            Icons.Default.Search,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.outline,
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    trailingIcon = if (searchQuery.isNotEmpty()) {
                        {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Close, contentDescription = "Clear", modifier = Modifier.size(16.dp))
                            }
                        }
                    } else null,
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("outline_search_input")
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(8.dp))

            // Headings List or Empty State
            if (filteredItems.isEmpty()) {
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
                            text = if (headingItems.isEmpty()) "No Headings in Document" else "No matching headings found",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = if (headingItems.isEmpty()) {
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
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(filteredItems, key = { it.blockIndex }) { item ->
                        val isCurrent = item.blockIndex == activeBlockIndex

                        // Indentation by heading level
                        val startPadding = when (item.style) {
                            HeadingStyle.TITLE -> 0.dp
                            HeadingStyle.HEADING_1 -> 0.dp
                            HeadingStyle.HEADING_2 -> 16.dp
                            HeadingStyle.HEADING_3 -> 28.dp
                            HeadingStyle.SUBTITLE -> 8.dp
                            else -> 0.dp
                        }

                        // Badge color and label
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
                                containerColor = if (isCurrent) {
                                    WordPrimary.copy(alpha = 0.09f)
                                } else {
                                    MaterialTheme.colorScheme.surface
                                }
                            ),
                            border = BorderStroke(
                                width = if (isCurrent) 1.5.dp else 0.5.dp,
                                color = if (isCurrent) WordPrimary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = startPadding)
                                .clickable {
                                    onNavigateToHeading(item.blockIndex, (item.pageNumber - 1).coerceAtLeast(0))
                                    onDismiss()
                                }
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
                }
            }
        }
    }
}

@Composable
private fun StatChip(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
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
