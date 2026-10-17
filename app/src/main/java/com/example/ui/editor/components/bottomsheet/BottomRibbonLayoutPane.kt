package com.example.ui.editor.components.bottomsheet

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BorderAll
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.DashboardCustomize
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.ScreenRotation
import androidx.compose.material.icons.filled.ViewColumn
import androidx.compose.material.icons.filled.WaterDamage
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.editor.EditorDialogsState
import com.example.ui.editor.EditorViewModel
import com.example.ui.theme.WordPrimary

/**
 * Categorized vertical layout for the Layout tab in the Bottom Ribbon Drawer.
 * Groups page dimensions, orientation, multi-column setups, and backgrounds.
 */
@Composable
fun BottomRibbonLayoutPane(
    viewModel: EditorViewModel,
    dState: EditorDialogsState,
    modifier: Modifier = Modifier
) {
    val document by viewModel.document.collectAsStateWithLifecycle()
    val layout = document?.layoutConfig ?: com.example.core.layout.PageLayoutConfig()
    val isTwoColumns = layout.columnCount == 2

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Category 1: Page Geometry & Setup
        LayoutCategoryGroup(title = "Page Geometry & Margins") {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                LayoutTileCard(
                    icon = Icons.Default.Description,
                    title = "Page Setup",
                    subtitle = "A4, Letter & Margins",
                    onClick = { dState.showLayoutDialog = true },
                    modifier = Modifier.weight(1f)
                )
                LayoutTileCard(
                    icon = Icons.Default.ViewColumn,
                    title = if (isTwoColumns) "2 Columns (On)" else "Columns (1 Col)",
                    subtitle = "Side-by-side text",
                    isActive = isTwoColumns,
                    onClick = { viewModel.setColumnCount(if (isTwoColumns) 1 else 2) },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Category 2: Page Background & Watermark
        LayoutCategoryGroup(title = "Background & Decorative") {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                LayoutTileCard(
                    icon = Icons.Default.Layers,
                    title = "Watermark",
                    subtitle = "CONFIDENTIAL / DRAFT",
                    onClick = { dState.showWatermarkDialog = true },
                    modifier = Modifier.weight(1f)
                )
                LayoutTileCard(
                    icon = Icons.Default.ColorLens,
                    title = "Page Color",
                    subtitle = "Paper tint & themes",
                    onClick = { dState.showPageColorDialog = true },
                    modifier = Modifier.weight(1f)
                )
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                LayoutTileCard(
                    icon = Icons.Default.BorderAll,
                    title = "Page Borders",
                    subtitle = "Box & shadow frames",
                    onClick = { dState.showPageBordersDialog = true },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun LayoutCategoryGroup(
    title: String,
    content: @Composable () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = WordPrimary
        )
        content()
    }
}

@Composable
private fun LayoutTileCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    isActive: Boolean = false,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, if (isActive) WordPrimary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
        color = if (isActive) WordPrimary.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = if (isActive) WordPrimary else WordPrimary.copy(alpha = 0.12f),
                modifier = Modifier.size(36.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icon, contentDescription = null, tint = if (isActive) androidx.compose.ui.graphics.Color.White else WordPrimary, modifier = Modifier.size(20.dp))
                }
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                Text(subtitle, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
