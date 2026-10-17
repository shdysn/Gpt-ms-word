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
import androidx.compose.material.icons.automirrored.filled.ListAlt
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Straighten
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
import com.example.ui.editor.EditorViewMode
import com.example.ui.editor.RibbonTab
import com.example.ui.editor.components.ribbon.DrawRibbonControls
import com.example.ui.theme.WordPrimary

/**
 * Handles the View and Draw tabs within the Bottom Ribbon Drawer.
 */
@Composable
fun BottomRibbonViewDrawPane(
    activeTab: RibbonTab,
    viewModel: EditorViewModel,
    dState: EditorDialogsState,
    modifier: Modifier = Modifier
) {
    val viewMode by viewModel.viewMode.collectAsStateWithLifecycle()
    val showRuler by viewModel.showRuler.collectAsStateWithLifecycle()

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (activeTab == RibbonTab.VIEW) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("View & Navigation", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = WordPrimary)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ActionTileCard(
                        icon = if (viewMode == EditorViewMode.PAGINATED_A4) Icons.Default.PhoneAndroid else Icons.Default.Description,
                        title = if (viewMode == EditorViewMode.PAGINATED_A4) "Mobile View" else "A4 Page View",
                        subtitle = if (viewMode == EditorViewMode.PAGINATED_A4) "Switch to reflow" else "Switch to paginated",
                        onClick = viewModel::toggleViewMode,
                        modifier = Modifier.weight(1f)
                    )
                    ActionTileCard(
                        icon = Icons.AutoMirrored.Filled.ListAlt,
                        title = "Outline",
                        subtitle = "Jump to headings",
                        onClick = { dState.showOutlineSheet = true },
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ActionTileCard(
                        icon = Icons.Default.Straighten,
                        title = if (showRuler) "Hide Ruler" else "Show Ruler",
                        subtitle = "Page margin guide",
                        isActive = showRuler,
                        onClick = viewModel::toggleRuler,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        } else if (activeTab == RibbonTab.DRAW) {
            DrawRibbonControls(viewModel = viewModel)
        }
    }
}

@Composable
fun ActionTileCard(
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
