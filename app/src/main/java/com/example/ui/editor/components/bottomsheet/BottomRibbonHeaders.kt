package com.example.ui.editor.components.bottomsheet

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Redo
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.editor.RibbonTab
import com.example.ui.editor.components.getRibbonTabMetadata
import com.example.ui.theme.WordPrimary

/**
 * Top header bar for MS Word Mobile submenus (< | Title ... ▾).
 */
@Composable
fun SubmenuHeaderBar(
    title: String,
    onBack: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBack, modifier = Modifier.size(40.dp)) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color(0xFF1F497D))
        }

        VerticalDivider(modifier = Modifier.height(20.dp).padding(horizontal = 2.dp), color = MaterialTheme.colorScheme.outlineVariant)

        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF1F497D),
            modifier = Modifier
                .weight(1f)
                .padding(start = 12.dp)
                .clickable { onBack() }
        )

        IconButton(onClick = onDismiss, modifier = Modifier.size(40.dp)) {
            Icon(Icons.Default.ArrowDropDown, contentDescription = "Close", tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun SubmenuHeaderBar(
    submenu: HomeSubmenu,
    onBack: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val title = when (submenu) {
        HomeSubmenu.FONT_COLOR -> "Font Color"
        HomeSubmenu.HIGHLIGHT_COLOR -> "Highlight"
        HomeSubmenu.MORE_FORMATTING -> "More Formatting"
        HomeSubmenu.CHANGE_CASE -> "Change Case"
        HomeSubmenu.BULLETS -> "Bullets"
        HomeSubmenu.NUMBERING -> "Numbering"
        HomeSubmenu.PARAGRAPH_FORMATTING -> "Paragraph"
        HomeSubmenu.SPECIAL_INDENT -> "Special Indent"
        HomeSubmenu.LINE_SPACING -> "Line Spacing:"
        HomeSubmenu.STYLES -> "Styles"
        HomeSubmenu.FONT_FAMILY -> "Font"
        HomeSubmenu.ROOT -> "Home"
    }
    SubmenuHeaderBar(title = title, onBack = onBack, onDismiss = onDismiss, modifier = modifier)
}

/**
 * Top header bar for main ribbon (Tab ⬍ + Lightbulb + Undo + Redo + Collapse).
 */
@Composable
fun MainRibbonHeaderBar(
    activeTab: RibbonTab,
    onTabSelected: (RibbonTab) -> Unit,
    canUndo: Boolean,
    canRedo: Boolean,
    onUndo: () -> Unit,
    onRedo: () -> Unit,
    onOpenFind: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isDropdownOpen by remember { mutableStateOf(false) }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Tab Selector (e.g. Home ⬍)
        Box {
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { isDropdownOpen = true }
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val (_, tabTitle) = getRibbonTabMetadata(activeTab)
                Text(
                    text = tabTitle,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1F497D)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Icon(Icons.Default.ArrowDropDown, contentDescription = "Switch Tab", tint = Color(0xFF1F497D))
            }

            DropdownMenu(expanded = isDropdownOpen, onDismissRequest = { isDropdownOpen = false }) {
                RibbonTab.entries.forEach { tab ->
                    val isSelected = activeTab == tab
                    val (tabIcon, tabTitle) = getRibbonTabMetadata(tab)
                    DropdownMenuItem(
                        text = { Text(tabTitle, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium) },
                        leadingIcon = { Icon(tabIcon, contentDescription = null, tint = if (isSelected) WordPrimary else MaterialTheme.colorScheme.onSurfaceVariant) },
                        trailingIcon = if (isSelected) { { Icon(Icons.Default.Check, contentDescription = null, tint = WordPrimary, modifier = Modifier.size(16.dp)) } } else null,
                        onClick = {
                            isDropdownOpen = false
                            onTabSelected(tab)
                        }
                    )
                }
            }
        }

        // Action Icons (Lightbulb, Undo, Redo, Collapse)
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onOpenFind, modifier = Modifier.size(36.dp)) {
                Icon(Icons.Default.Lightbulb, contentDescription = "Smart Search", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
            }
            IconButton(onClick = onUndo, enabled = canUndo, modifier = Modifier.size(36.dp)) {
                Icon(Icons.AutoMirrored.Filled.Undo, contentDescription = "Undo", tint = if (canUndo) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f), modifier = Modifier.size(20.dp))
            }
            IconButton(onClick = onRedo, enabled = canRedo, modifier = Modifier.size(36.dp)) {
                Icon(Icons.AutoMirrored.Filled.Redo, contentDescription = "Redo", tint = if (canRedo) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f), modifier = Modifier.size(20.dp))
            }
            IconButton(onClick = onDismiss, modifier = Modifier.size(36.dp)) {
                Icon(Icons.Default.ArrowDropDown, contentDescription = "Collapse", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(24.dp))
            }
        }
    }
}
