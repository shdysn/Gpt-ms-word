package com.example.ui.home.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material3.AssistChip
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import com.example.ui.home.DocumentSortOrder
import com.example.ui.home.HomeFilter
import com.example.ui.theme.WordPrimary

@Composable
fun HomeFilterSortBar(
    activeFilter: HomeFilter,
    sortOrder: DocumentSortOrder,
    onFilterChanged: (HomeFilter) -> Unit,
    onSortOrderChanged: (DocumentSortOrder) -> Unit,
    modifier: Modifier = Modifier
) {
    var showSortMenu by remember { mutableStateOf(false) }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.weight(1f).horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            FilterChip(
                selected = activeFilter == HomeFilter.ALL,
                onClick = { onFilterChanged(HomeFilter.ALL) },
                label = { Text("All") },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
            FilterChip(
                selected = activeFilter == HomeFilter.RECENT,
                onClick = { onFilterChanged(HomeFilter.RECENT) },
                label = { Text("Recent") }
            )
            FilterChip(
                selected = activeFilter == HomeFilter.PINNED,
                onClick = { onFilterChanged(HomeFilter.PINNED) },
                label = { Text("Pinned") }
            )
            FilterChip(
                selected = activeFilter == HomeFilter.FAVORITES,
                onClick = { onFilterChanged(HomeFilter.FAVORITES) },
                label = { Text("Favorites") }
            )
        }

        // Sort Order Dropdown
        Box {
            AssistChip(
                onClick = { showSortMenu = true },
                label = { Text(sortOrder.displayName, fontSize = 12.sp) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Sort,
                        contentDescription = "Sort options",
                        modifier = Modifier.size(15.dp)
                    )
                },
                modifier = Modifier.testTag("btn_sort_documents")
            )

            DropdownMenu(
                expanded = showSortMenu,
                onDismissRequest = { showSortMenu = false }
            ) {
                DocumentSortOrder.entries.forEach { order ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = order.displayName,
                                fontWeight = if (sortOrder == order) FontWeight.Bold else FontWeight.Normal,
                                color = if (sortOrder == order) WordPrimary else MaterialTheme.colorScheme.onSurface
                            )
                        },
                        onClick = {
                            onSortOrderChanged(order)
                            showSortMenu = false
                        }
                    )
                }
            }
        }
    }
}
