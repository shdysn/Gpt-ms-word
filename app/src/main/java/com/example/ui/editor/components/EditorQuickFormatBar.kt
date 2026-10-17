package com.example.ui.editor.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.FormatBold
import androidx.compose.material.icons.filled.FormatColorText
import androidx.compose.material.icons.filled.FormatItalic
import androidx.compose.material.icons.filled.FormatStrikethrough
import androidx.compose.material.icons.filled.FormatUnderlined
import androidx.compose.material.icons.filled.Highlight
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.unit.dp
import com.example.editor.engine.ActiveFormatting
import com.example.ui.theme.WordPrimary

/**
 * Modern Keyboard Accessory Bar docked directly above Android soft keyboard (IME).
 * Replaces intrusive floating popups with a unified Word Mobile style toolbar containing
 * clipboard actions (Cut, Copy, Paste, Select All) and quick character formatting, plus
 * a 3-dots expand button to toggle the bottom ribbon drawer.
 */
@Composable
fun EditorQuickFormatBar(
    selection: TextRange,
    isDocumentAllSelected: Boolean = false,
    isKeyboardVisible: Boolean = false,
    isRibbonExpanded: Boolean = false,
    activeFormatting: ActiveFormatting,
    canCut: Boolean = false,
    canCopy: Boolean = false,
    canPaste: Boolean = true,
    onCut: () -> Unit = {},
    onCopy: () -> Unit = {},
    onPaste: () -> Unit = {},
    onSelectAll: () -> Unit = {},
    onToggleBold: () -> Unit,
    onToggleItalic: () -> Unit,
    onToggleUnderline: () -> Unit,
    onToggleStrikethrough: () -> Unit,
    onHighlightClick: () -> Unit,
    onTextColorClick: () -> Unit,
    onToggleExpandRibbon: () -> Unit = {},
    onDeselect: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val hasSelection = (!selection.collapsed && selection.min < selection.max) || isDocumentAllSelected
    val isVisible = isKeyboardVisible || hasSelection || isRibbonExpanded

    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn() + slideInVertically(initialOffsetY = { it }),
        exit = fadeOut() + slideOutVertically(targetOffsetY = { it }),
        modifier = modifier
    ) {
        val dividerColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)

        Surface(
            shape = RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 3.dp,
            shadowElevation = 8.dp,
            border = BorderStroke(1.dp, dividerColor),
            modifier = Modifier.fillMaxWidth().testTag("keyboard_accessory_bar")
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 6.dp, vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                // Clipboard actions (Cut, Copy, Paste, Select All)
                if (hasSelection) {
                    IconButton(
                        onClick = onCut,
                        enabled = canCut,
                        modifier = Modifier.size(40.dp).testTag("quick_format_cut")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCut,
                            contentDescription = "Cut",
                            tint = if (canCut) WordPrimary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
                            modifier = Modifier.size(19.dp)
                        )
                    }
                    IconButton(
                        onClick = onCopy,
                        enabled = canCopy,
                        modifier = Modifier.size(40.dp).testTag("quick_format_copy")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Copy",
                            tint = if (canCopy) WordPrimary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
                            modifier = Modifier.size(19.dp)
                        )
                    }
                }

                IconButton(
                    onClick = onPaste,
                    enabled = canPaste,
                    modifier = Modifier.size(40.dp).testTag("quick_format_paste")
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentPaste,
                        contentDescription = "Paste",
                        tint = if (canPaste) Color(0xFFD97706) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
                        modifier = Modifier.size(19.dp)
                    )
                }

                IconButton(
                    onClick = onSelectAll,
                    modifier = Modifier.size(40.dp).testTag("quick_format_select_all")
                ) {
                    Icon(
                        imageVector = Icons.Default.SelectAll,
                        contentDescription = "Select All",
                        tint = if (isDocumentAllSelected) WordPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(19.dp)
                    )
                }

                VerticalDivider(
                    modifier = Modifier.height(20.dp).padding(horizontal = 2.dp),
                    color = dividerColor
                )

                // Character Formatting Toggles
                IconButton(
                    onClick = onToggleBold,
                    modifier = Modifier.size(40.dp).testTag("quick_format_bold")
                ) {
                    Icon(
                        imageVector = Icons.Default.FormatBold,
                        contentDescription = "Bold",
                        tint = if (activeFormatting.isBold) WordPrimary else MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(20.dp)
                    )
                }
                IconButton(
                    onClick = onToggleItalic,
                    modifier = Modifier.size(40.dp).testTag("quick_format_italic")
                ) {
                    Icon(
                        imageVector = Icons.Default.FormatItalic,
                        contentDescription = "Italic",
                        tint = if (activeFormatting.isItalic) WordPrimary else MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(20.dp)
                    )
                }
                IconButton(
                    onClick = onToggleUnderline,
                    modifier = Modifier.size(40.dp).testTag("quick_format_underline")
                ) {
                    Icon(
                        imageVector = Icons.Default.FormatUnderlined,
                        contentDescription = "Underline",
                        tint = if (activeFormatting.isUnderline) WordPrimary else MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(20.dp)
                    )
                }
                IconButton(
                    onClick = onToggleStrikethrough,
                    modifier = Modifier.size(40.dp).testTag("quick_format_strikethrough")
                ) {
                    Icon(
                        imageVector = Icons.Default.FormatStrikethrough,
                        contentDescription = "Strikethrough",
                        tint = if (activeFormatting.isStrikethrough) WordPrimary else MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(20.dp)
                    )
                }
                IconButton(
                    onClick = onHighlightClick,
                    modifier = Modifier.size(40.dp).testTag("quick_format_highlight")
                ) {
                    Icon(
                        imageVector = Icons.Default.Highlight,
                        contentDescription = "Highlight",
                        tint = if (activeFormatting.highlightColorHex != null) Color(0xFFEAB308) else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }
                IconButton(
                    onClick = onTextColorClick,
                    modifier = Modifier.size(40.dp).testTag("quick_format_color")
                ) {
                    Icon(
                        imageVector = Icons.Default.FormatColorText,
                        contentDescription = "Color",
                        tint = if (activeFormatting.textColorHex != null) Color(0xFF185ABD) else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }

                if (hasSelection) {
                    VerticalDivider(
                        modifier = Modifier.height(20.dp).padding(horizontal = 2.dp),
                        color = dividerColor
                    )
                    IconButton(
                        onClick = onDeselect,
                        modifier = Modifier.size(36.dp).testTag("quick_format_deselect")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Deselect",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                VerticalDivider(
                    modifier = Modifier.height(20.dp).padding(horizontal = 2.dp),
                    color = dividerColor
                )

                IconButton(
                    onClick = onToggleExpandRibbon,
                    modifier = Modifier.size(40.dp).testTag("quick_format_expand_ribbon")
                ) {
                    Icon(
                        imageVector = Icons.Default.MoreHoriz,
                        contentDescription = "Expand Ribbon Tools",
                        tint = if (isRibbonExpanded) WordPrimary else MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }
    }
}
