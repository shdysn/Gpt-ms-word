package com.example.ui.home.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.repository.TemplateType
import com.example.ui.theme.WordPrimary

@Composable
fun HomeTemplatesCarousel(
    onOpenMoreTemplates: () -> Unit,
    onSelectTemplate: (TemplateType) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.padding(top = 4.dp, bottom = 2.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Start from a template",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp
            )
            TextButton(
                onClick = onOpenMoreTemplates,
                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp)
            ) {
                Text("More templates", color = WordPrimary, fontSize = 12.sp)
            }
        }

        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(TemplateType.entries.toTypedArray()) { template ->
                TemplateCard(
                    template = template,
                    onClick = { onSelectTemplate(template) }
                )
            }
        }
    }
}
