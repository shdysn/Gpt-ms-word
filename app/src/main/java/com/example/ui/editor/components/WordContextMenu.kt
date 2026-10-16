package com.example.ui.editor.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.FormatPaint
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Modern Microsoft Word Mobile style contextual menu.
 * Displays floating action bar with [Cut | Copy | Paste ⌵ | Select All]
 * and expandable Paste dropdown with "Paste" and "Keep Source Formatting".
 */
@Composable
fun WordContextMenu(
    isVisible: Boolean,
    canCut: Boolean,
    canCopy: Boolean,
    onCut: () -> Unit,
    onCopy: () -> Unit,
    onPaste: () -> Unit,
    onPasteKeepFormatting: () -> Unit,
    onSelectAll: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showPasteOptions by remember(isVisible) { mutableStateOf(false) }

    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn() + scaleIn(initialScale = 0.92f),
        exit = fadeOut() + scaleOut(targetScale = 0.92f),
        modifier = modifier
    ) {
        val dividerColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)

        if (!showPasteOptions) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp,
                shadowElevation = 8.dp,
                border = BorderStroke(1.dp, dividerColor),
                modifier = Modifier.testTag("word_floating_context_bar").height(44.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    ContextMenuItemButton(text = "Cut", enabled = canCut, testTag = "context_menu_cut") {
                        onCut(); onDismiss()
                    }
                    VerticalDivider(color = dividerColor)
                    ContextMenuItemButton(text = "Copy", enabled = canCopy, testTag = "context_menu_copy") {
                        onCopy(); onDismiss()
                    }
                    VerticalDivider(color = dividerColor)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .clickable { onPaste(); onDismiss() }
                                .padding(horizontal = 12.dp, vertical = 10.dp)
                                .testTag("context_menu_paste")
                        ) {
                            Text(
                                text = "Paste",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium, fontSize = 14.sp),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Box(modifier = Modifier.width(1.dp).height(16.dp).background(dividerColor))
                        Box(
                            modifier = Modifier
                                .clickable { showPasteOptions = true }
                                .padding(horizontal = 8.dp, vertical = 10.dp)
                                .testTag("context_menu_paste_dropdown")
                        ) {
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowDown,
                                contentDescription = "More Paste Options",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                    VerticalDivider(color = dividerColor)
                    ContextMenuItemButton(text = "Select All", enabled = true, testTag = "context_menu_select_all") {
                        onSelectAll(); onDismiss()
                    }
                }
            }
        } else {
            Card(
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                border = BorderStroke(1.dp, dividerColor),
                modifier = Modifier.widthIn(min = 260.dp, max = 290.dp).testTag("word_paste_options_card")
            ) {
                Column(modifier = Modifier.padding(bottom = 6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 6.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { showPasteOptions = false }, modifier = Modifier.size(36.dp).testTag("paste_option_back")) {
                            Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", modifier = Modifier.size(18.dp))
                        }
                        Box(modifier = Modifier.width(1.dp).height(18.dp).background(dividerColor))
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Paste",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold, fontSize = 16.sp),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    HorizontalDivider(color = dividerColor)
                    PasteOptionRow(
                        icon = Icons.Default.ContentPaste,
                        tint = Color(0xFFD97706),
                        label = "Paste",
                        testTag = "paste_option_standard",
                        onClick = { onPaste(); onDismiss() }
                    )
                    PasteOptionRowWithBrush(
                        label = "Keep Source Formatting",
                        testTag = "paste_option_keep_formatting",
                        onClick = { onPasteKeepFormatting(); onDismiss() }
                    )
                }
            }
        }
    }
}

@Composable
private fun PasteOptionRow(icon: androidx.compose.ui.graphics.vector.ImageVector, tint: Color, label: String, testTag: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(horizontal = 16.dp, vertical = 12.dp).testTag(testTag),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(imageVector = icon, contentDescription = label, tint = tint, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.width(16.dp))
        Text(text = label, style = MaterialTheme.typography.bodyLarge.copy(fontSize = 15.sp), color = MaterialTheme.colorScheme.onSurface)
    }
}

@Composable
private fun PasteOptionRowWithBrush(label: String, testTag: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(horizontal = 16.dp, vertical = 12.dp).testTag(testTag),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.size(24.dp)) {
            Icon(imageVector = Icons.Default.ContentPaste, contentDescription = label, tint = Color(0xFFD97706), modifier = Modifier.size(24.dp))
            Icon(imageVector = Icons.Default.FormatPaint, contentDescription = null, tint = Color(0xFF2563EB), modifier = Modifier.size(13.dp).align(Alignment.BottomEnd))
        }
        Spacer(modifier = Modifier.width(16.dp))
        Text(text = label, style = MaterialTheme.typography.bodyLarge.copy(fontSize = 15.sp), color = MaterialTheme.colorScheme.onSurface)
    }
}

@Composable
private fun ContextMenuItemButton(text: String, enabled: Boolean, testTag: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier.clickable(enabled = enabled, onClick = onClick).padding(horizontal = 14.dp, vertical = 10.dp).testTag(testTag)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium, fontSize = 14.sp),
            color = if (enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
        )
    }
}

@Composable
private fun VerticalDivider(color: Color) {
    Box(modifier = Modifier.width(1.dp).height(20.dp).background(color))
}
