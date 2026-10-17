package com.example.ui.editor.components.bottomsheet

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.FormatAlignLeft
import androidx.compose.material.icons.automirrored.filled.FormatListBulleted
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FormatClear
import androidx.compose.material.icons.filled.FormatColorText
import androidx.compose.material.icons.filled.FormatListNumbered
import androidx.compose.material.icons.filled.FormatPaint
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Subscript
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.ParagraphBlock
import com.example.data.model.TextAlignment
import com.example.editor.engine.ActiveFormatting

/**
 * Root Home menu matching MS Word Mobile (Screenshots 7, 10, 11).
 */
@Composable
fun BottomRibbonHomeRootView(
    formatting: ActiveFormatting,
    activeParagraph: ParagraphBlock?,
    onNavigate: (HomeSubmenu) -> Unit,
    onToggleBold: () -> Unit,
    onToggleItalic: () -> Unit,
    onToggleUnderline: () -> Unit,
    onToggleStrikethrough: () -> Unit,
    onClearFormatting: () -> Unit,
    onSetAlignment: (TextAlignment) -> Unit,
    onToggleRtl: () -> Unit,
    onIncreaseIndent: () -> Unit,
    onDecreaseIndent: () -> Unit,
    onOpenFind: () -> Unit,
    onSetFontFamily: (String) -> Unit,
    onSetFontSize: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Row 1: Font Family (Left) & Font Size (Right)
        FontAndSizeSelectorRow(
            fontFamily = formatting.fontFamilyName ?: "Arial",
            fontSize = formatting.fontSizeSp ?: 11f,
            onSetFontFamily = onSetFontFamily,
            onSetFontSize = onSetFontSize
        )

        // Row 2: B, I, U, ab segmented buttons (4 equal segments)
        BasicFormatButtonRow(
            isBold = formatting.isBold,
            isItalic = formatting.isItalic,
            isUnderline = formatting.isUnderline,
            isStrikethrough = formatting.isStrikethrough,
            onToggleBold = onToggleBold,
            onToggleItalic = onToggleItalic,
            onToggleUnderline = onToggleUnderline,
            onToggleStrikethrough = onToggleStrikethrough
        )

        // Indent & Direction (Decrease, Increase, LTR >¶, RTL ¶<)
        IndentAndDirectionButtonRow(
            isRtl = activeParagraph?.isRtl ?: false,
            onDecreaseIndent = onDecreaseIndent,
            onIncreaseIndent = onIncreaseIndent,
            onToggleRtl = onToggleRtl
        )

        // Text Alignment (Left, Center, Right, Justify)
        AlignmentButtonRow(
            currentAlignment = activeParagraph?.alignment ?: TextAlignment.START,
            onSetAlignment = onSetAlignment
        )

        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f), thickness = 0.5.dp)

        // Menu Items: Highlight, Font Color, Clear Formatting, More Formatting
        HomeMenuItem(
            icon = Icons.Default.Edit,
            title = "Highlight",
            hasChevron = true,
            badgeColorHex = formatting.highlightColorHex,
            onClick = { onNavigate(HomeSubmenu.HIGHLIGHT_COLOR) }
        )

        HomeMenuItem(
            icon = Icons.Default.FormatColorText,
            title = "Font Color",
            hasChevron = true,
            underlineColorHex = formatting.textColorHex ?: "#DC2626",
            onClick = { onNavigate(HomeSubmenu.FONT_COLOR) }
        )

        HomeMenuItem(
            icon = Icons.Default.FormatClear,
            title = "Clear Formatting",
            hasChevron = false,
            onClick = onClearFormatting
        )

        HomeMenuItem(
            icon = Icons.Default.Subscript,
            title = "More Formatting",
            hasChevron = true,
            onClick = { onNavigate(HomeSubmenu.MORE_FORMATTING) }
        )

        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f), thickness = 0.5.dp)

        // Bullets & Numbering rows
        HomeMenuItem(
            icon = Icons.AutoMirrored.Filled.FormatListBulleted,
            title = "Bullets",
            hasChevron = true,
            onClick = { onNavigate(HomeSubmenu.BULLETS) }
        )

        HomeMenuItem(
            icon = Icons.Default.FormatListNumbered,
            title = "Numbering",
            hasChevron = true,
            onClick = { onNavigate(HomeSubmenu.NUMBERING) }
        )

        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f), thickness = 0.5.dp)

        // Paragraph Formatting & Styles & Find
        HomeMenuItem(
            icon = Icons.AutoMirrored.Filled.FormatAlignLeft,
            title = "Paragraph Formatting",
            hasChevron = true,
            onClick = { onNavigate(HomeSubmenu.PARAGRAPH_FORMATTING) }
        )

        HomeMenuItem(
            icon = Icons.Default.FormatPaint,
            title = "Styles",
            hasChevron = true,
            onClick = { onNavigate(HomeSubmenu.STYLES) }
        )

        HomeMenuItem(
            icon = Icons.Default.Search,
            title = "Find",
            hasChevron = false,
            onClick = onOpenFind
        )
    }
}

@Composable
private fun FontAndSizeSelectorRow(
    fontFamily: String,
    fontSize: Float,
    onSetFontFamily: (String) -> Unit,
    onSetFontSize: (Float) -> Unit
) {
    var showFontMenu by remember { mutableStateOf(false) }
    var showSizeMenu by remember { mutableStateOf(false) }

    val fonts = listOf("Arial", "Calibri", "Times New Roman", "Georgia", "Noto Nastaliq Urdu", "Roboto")
    val sizes = listOf(8f, 9f, 10f, 11f, 12f, 14f, 16f, 18f, 20f, 24f, 28f, 32f, 36f, 48f, 72f)

    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Font Family Selector
        Box {
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .clickable { showFontMenu = true }
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = fontFamily,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Normal,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.width(4.dp))
                Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            DropdownMenu(expanded = showFontMenu, onDismissRequest = { showFontMenu = false }) {
                fonts.forEach { font ->
                    DropdownMenuItem(
                        text = { Text(font, fontWeight = if (font.equals(fontFamily, true)) FontWeight.Bold else FontWeight.Normal) },
                        onClick = {
                            onSetFontFamily(font)
                            showFontMenu = false
                        }
                    )
                }
            }
        }

        // Font Size Selector
        Box {
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .clickable { showSizeMenu = true }
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${fontSize.toInt()}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Normal,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.width(4.dp))
                Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            DropdownMenu(expanded = showSizeMenu, onDismissRequest = { showSizeMenu = false }) {
                sizes.forEach { size ->
                    DropdownMenuItem(
                        text = { Text("${size.toInt()}") },
                        onClick = {
                            onSetFontSize(size)
                            showSizeMenu = false
                        }
                    )
                }
            }
        }
    }
}
