package com.example.ui.editor.components.bottomsheet

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.editor.EditorDialogsState
import com.example.ui.editor.EditorViewModel

/**
 * Microsoft Word Mobile Link Insertion Submenu.
 * Displays "Insert Link..." action and a list of Recent Items.
 */
@Composable
fun BottomRibbonInsertLinkSubmenu(
    viewModel: EditorViewModel,
    dState: EditorDialogsState,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val currentDocTitle = viewModel.currentDocument?.title ?: "Document"

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        // Insert Link... Action Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .clickable {
                    dState.showLinkDialog = true
                    onBack()
                }
                .padding(horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(modifier = Modifier.size(24.dp), contentAlignment = Alignment.Center) {
                WordLinkChainIcon()
            }
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = "Insert Link...",
                style = MaterialTheme.typography.bodyLarge,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        // Recent Items Section Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFEEEEEE))
                .padding(horizontal = 8.dp, vertical = 6.dp)
        ) {
            Text(
                text = "Recent Items",
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF666666)
            )
        }

        // Recent Item 1: Current Document
        RecentLinkItem(
            title = currentDocTitle,
            url = "https://smartword.office.com/docs/${viewModel.currentDocument?.id ?: "doc"}",
            onClick = {
                viewModel.setHyperlink("https://smartword.office.com/docs/${viewModel.currentDocument?.id ?: "doc"}")
                viewModel.showStatusMessage("Link to '$currentDocTitle' inserted")
                onBack()
            }
        )

        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), thickness = 0.5.dp)

        // Recent Item 2: Common Web Reference
        RecentLinkItem(
            title = "Project Notes & Specification",
            url = "https://office.live.com/view.aspx?resid=project_spec",
            onClick = {
                viewModel.setHyperlink("https://office.live.com/view.aspx?resid=project_spec")
                viewModel.showStatusMessage("Link inserted")
                onBack()
            }
        )
    }
}

@Composable
private fun RecentLinkItem(
    title: String,
    url: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 4.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Blue Word Doc Icon badge
        Surface(
            shape = RoundedCornerShape(4.dp),
            color = Color(0xFF0F6CBD),
            modifier = Modifier.size(32.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = "W",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = url,
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF666666),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
