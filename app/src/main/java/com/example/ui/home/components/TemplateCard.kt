package com.example.ui.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.repository.TemplateType
import com.example.ui.home.HomeFilter
import com.example.ui.theme.WordPrimary

@Composable
fun TemplateCard(
    template: TemplateType,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(130.dp)
            .height(170.dp)
            .clickable(onClick = onClick)
            .testTag("template_${template.name.lowercase()}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(10.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Simulated Paper Thumbnail
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(95.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color(0xFFF8F9FA))
                    .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(6.dp))
                    .padding(8.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.6f)
                            .height(6.dp)
                            .background(WordPrimary, RoundedCornerShape(3.dp))
                    )
                    Box(modifier = Modifier.fillMaxWidth().height(4.dp).background(Color(0xFFCBD5E1), RoundedCornerShape(2.dp)))
                    Box(modifier = Modifier.fillMaxWidth(0.85f).height(4.dp).background(Color(0xFFE2E8F0), RoundedCornerShape(2.dp)))
                    Box(modifier = Modifier.fillMaxWidth(0.7f).height(4.dp).background(Color(0xFFE2E8F0), RoundedCornerShape(2.dp)))
                }
            }

            Column {
                Text(
                    text = template.title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun EmptyDocumentsView(
    isSearching: Boolean,
    activeFilter: HomeFilter = HomeFilter.ALL,
    onClearFilter: () -> Unit = {},
    onCreateClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primaryContainer,
            modifier = Modifier.size(72.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = if (activeFilter == HomeFilter.PINNED && !isSearching) Icons.Default.PushPin else Icons.Default.Description,
                    contentDescription = null,
                    tint = WordPrimary,
                    modifier = Modifier.size(36.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        val emptyTitle = when {
            isSearching -> "No matching documents"
            activeFilter == HomeFilter.PINNED -> "No pinned documents"
            activeFilter == HomeFilter.RECENT -> "No recent documents"
            else -> "No documents yet"
        }

        val emptySubtitle = when {
            isSearching -> "Try a different search term"
            activeFilter == HomeFilter.PINNED -> "Pin important documents from their menu to access them quickly here."
            else -> "Create your first document or start from a template above."
        }

        Text(
            text = emptyTitle,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = emptySubtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(18.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            if (isSearching || activeFilter != HomeFilter.ALL) {
                OutlinedButton(onClick = onClearFilter) {
                    Text("Clear Filter & Show All")
                }
            }
            if (!isSearching && activeFilter != HomeFilter.PINNED) {
                Button(
                    onClick = onCreateClick,
                    colors = ButtonDefaults.buttonColors(containerColor = WordPrimary)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("New Document")
                }
            }
        }
    }
}
