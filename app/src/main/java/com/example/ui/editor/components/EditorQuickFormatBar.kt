package com.example.ui.editor.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FormatBold
import androidx.compose.material.icons.filled.FormatColorText
import androidx.compose.material.icons.filled.FormatItalic
import androidx.compose.material.icons.filled.FormatStrikethrough
import androidx.compose.material.icons.filled.FormatUnderlined
import androidx.compose.material.icons.filled.Highlight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.unit.dp
import com.example.editor.engine.ActiveFormatting
import com.example.ui.theme.WordPrimary

@Composable
fun EditorQuickFormatBar(
    selection: TextRange,
    activeFormatting: ActiveFormatting,
    onToggleBold: () -> Unit,
    onToggleItalic: () -> Unit,
    onToggleUnderline: () -> Unit,
    onToggleStrikethrough: () -> Unit,
    onHighlightClick: () -> Unit,
    onTextColorClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.padding(bottom = 16.dp)
    ) {
        AnimatedVisibility(
            visible = !selection.collapsed && selection.min < selection.max,
            enter = fadeIn() + slideInVertically(initialOffsetY = { it }),
            exit = fadeOut() + slideOutVertically(targetOffsetY = { it })
        ) {
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                shadowElevation = 6.dp,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    IconButton(
                        onClick = onToggleBold,
                        modifier = Modifier.size(36.dp).testTag("quick_format_bold")
                    ) {
                        Icon(
                            imageVector = Icons.Default.FormatBold,
                            contentDescription = "Bold",
                            tint = if (activeFormatting.isBold) WordPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(
                        onClick = onToggleItalic,
                        modifier = Modifier.size(36.dp).testTag("quick_format_italic")
                    ) {
                        Icon(
                            imageVector = Icons.Default.FormatItalic,
                            contentDescription = "Italic",
                            tint = if (activeFormatting.isItalic) WordPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(
                        onClick = onToggleUnderline,
                        modifier = Modifier.size(36.dp).testTag("quick_format_underline")
                    ) {
                        Icon(
                            imageVector = Icons.Default.FormatUnderlined,
                            contentDescription = "Underline",
                            tint = if (activeFormatting.isUnderline) WordPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(
                        onClick = onToggleStrikethrough,
                        modifier = Modifier.size(36.dp).testTag("quick_format_strikethrough")
                    ) {
                        Icon(
                            imageVector = Icons.Default.FormatStrikethrough,
                            contentDescription = "Strikethrough",
                            tint = if (activeFormatting.isStrikethrough) WordPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(
                        onClick = onHighlightClick,
                        modifier = Modifier.size(36.dp).testTag("quick_format_highlight")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Highlight,
                            contentDescription = "Highlight",
                            tint = if (activeFormatting.highlightColorHex != null) Color(0xFFEAB308) else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(
                        onClick = onTextColorClick,
                        modifier = Modifier.size(36.dp).testTag("quick_format_color")
                    ) {
                        Icon(
                            imageVector = Icons.Default.FormatColorText,
                            contentDescription = "Color",
                            tint = if (activeFormatting.textColorHex != null) Color(0xFF185ABD) else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}
