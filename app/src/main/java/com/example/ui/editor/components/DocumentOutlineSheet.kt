package com.example.ui.editor.components

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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.layout.DocumentPage
import com.example.data.model.HeadingStyle
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

    val headingItems = remember(document.blocks, paginatedPages) {
        collectOutlineHeadings(document.blocks, paginatedPages)
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

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlineStatChip(icon = Icons.Default.Description, label = "$totalWords words", modifier = Modifier.weight(1f))
                OutlineStatChip(icon = Icons.AutoMirrored.Filled.MenuBook, label = "${paginatedPages.size.coerceAtLeast(1)} pages", modifier = Modifier.weight(1f))
                OutlineStatChip(icon = Icons.Default.Timer, label = "~$estimatedMinutes min", modifier = Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.height(14.dp))

            if (headingItems.size > 4) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Filter headings...", fontSize = 13.sp) },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = null, tint = MaterialTheme.colorScheme.outline, modifier = Modifier.size(18.dp))
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
                    modifier = Modifier.fillMaxWidth().height(48.dp).testTag("outline_search_input")
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(8.dp))

            if (filteredItems.isEmpty()) {
                OutlineEmptyState(isEmpty = headingItems.isEmpty())
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(filteredItems, key = { it.blockIndex }) { item ->
                        val isCurrent = item.blockIndex == activeBlockIndex
                        OutlineHeadingCard(
                            item = item,
                            isCurrent = isCurrent,
                            onClick = {
                                onNavigateToHeading(item.blockIndex, (item.pageNumber - 1).coerceAtLeast(0))
                                onDismiss()
                            }
                        )
                    }
                }
            }
        }
    }
}
