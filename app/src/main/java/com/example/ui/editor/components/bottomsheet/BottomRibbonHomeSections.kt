package com.example.ui.editor.components.bottomsheet

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.FormatAlignLeft
import androidx.compose.material.icons.automirrored.filled.FormatAlignRight
import androidx.compose.material.icons.automirrored.filled.FormatIndentDecrease
import androidx.compose.material.icons.automirrored.filled.FormatIndentIncrease
import androidx.compose.material.icons.automirrored.filled.FormatListBulleted
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.FormatAlignCenter
import androidx.compose.material.icons.filled.FormatAlignJustify
import androidx.compose.material.icons.filled.FormatColorText
import androidx.compose.material.icons.filled.FormatListNumbered
import androidx.compose.material.icons.filled.Highlight
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.HeadingStyle
import com.example.data.model.ListType
import com.example.data.model.TextAlignment
import com.example.ui.theme.WordPrimary

@Composable
fun ColorsSection(
    textColorHex: String?,
    highlightColorHex: String?,
    onSetTextColor: (String) -> Unit,
    onSetHighlightColor: (String?) -> Unit
) {
    val textColors = listOf("#000000", "#185ABD", "#DC2626", "#16A34A", "#9333EA", "#D97706")
    val highlightColors = listOf("#FEF08A", "#BBF7D0", "#BAE6FD", "#FBCFE8", "#FED7AA")

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        // Text Color Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(Icons.Default.FormatColorText, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
            Text("Text Color", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold, modifier = Modifier.width(72.dp))
            Row(
                modifier = Modifier.weight(1f).horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                textColors.forEach { hex ->
                    val color = Color(android.graphics.Color.parseColor(hex))
                    val isSelected = textColorHex?.equals(hex, ignoreCase = true) == true
                    Box(
                        modifier = Modifier
                            .size(26.dp)
                            .clip(CircleShape)
                            .background(color)
                            .clickable { onSetTextColor(hex) }
                    ) {
                        if (isSelected) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(Color.White)
                                    .align(Alignment.Center)
                            )
                        }
                    }
                }
            }
        }

        // Highlight Color Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(Icons.Default.Highlight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
            Text("Highlight", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold, modifier = Modifier.width(72.dp))
            Row(
                modifier = Modifier.weight(1f).horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    modifier = Modifier.clip(RoundedCornerShape(12.dp)).clickable { onSetHighlightColor(null) }
                ) {
                    Text("None", style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                }
                highlightColors.forEach { hex ->
                    val color = Color(android.graphics.Color.parseColor(hex))
                    val isSelected = highlightColorHex?.equals(hex, ignoreCase = true) == true
                    Box(
                        modifier = Modifier
                            .size(26.dp)
                            .clip(CircleShape)
                            .background(color)
                            .clickable { onSetHighlightColor(hex) }
                    ) {
                        if (isSelected) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(Color.Black.copy(alpha = 0.6f))
                                    .align(Alignment.Center)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AlignmentAndDirectionSection(
    alignment: TextAlignment,
    isRtl: Boolean,
    onSetAlignment: (TextAlignment) -> Unit,
    onToggleRtl: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Alignment Group
        Surface(
            shape = RoundedCornerShape(10.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
        ) {
            Row(modifier = Modifier.padding(2.dp)) {
                listOf(
                    Triple(TextAlignment.START, Icons.AutoMirrored.Filled.FormatAlignLeft, "Left"),
                    Triple(TextAlignment.CENTER, Icons.Default.FormatAlignCenter, "Center"),
                    Triple(TextAlignment.END, Icons.AutoMirrored.Filled.FormatAlignRight, "Right"),
                    Triple(TextAlignment.JUSTIFY, Icons.Default.FormatAlignJustify, "Justify")
                ).forEach { (alignType, icon, desc) ->
                    val isSelected = alignment == alignType
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isSelected) WordPrimary else Color.Transparent,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { onSetAlignment(alignType) }
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(icon, contentDescription = desc, tint = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }
        }

        // RTL / LTR Toggle Button
        Surface(
            shape = RoundedCornerShape(10.dp),
            border = BorderStroke(1.dp, if (isRtl) WordPrimary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
            color = if (isRtl) WordPrimary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
            modifier = Modifier.clip(RoundedCornerShape(10.dp)).clickable { onToggleRtl() }
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(Icons.Default.SwapHoriz, contentDescription = null, tint = if (isRtl) WordPrimary else MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(18.dp))
                Text(if (isRtl) "RTL (اردو)" else "LTR (Eng)", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = if (isRtl) WordPrimary else MaterialTheme.colorScheme.onSurface)
            }
        }
    }
}

@Composable
fun ListsAndIndentsSection(
    listType: ListType,
    onToggleListType: (ListType) -> Unit,
    onToggleChecklist: () -> Unit,
    onIncreaseIndent: () -> Unit,
    onDecreaseIndent: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val isBullet = listType == ListType.BULLET
        val isNumbered = listType == ListType.NUMBERED

        ActionChip(icon = Icons.AutoMirrored.Filled.FormatListBulleted, label = "Bullets", isActive = isBullet) { onToggleListType(ListType.BULLET) }
        ActionChip(icon = Icons.Default.FormatListNumbered, label = "Numbered", isActive = isNumbered) { onToggleListType(ListType.NUMBERED) }
        ActionChip(icon = Icons.Default.Checklist, label = "Checklist", isActive = false) { onToggleChecklist() }
        ActionChip(icon = Icons.AutoMirrored.Filled.FormatIndentDecrease, label = "Decrease", isActive = false) { onDecreaseIndent() }
        ActionChip(icon = Icons.AutoMirrored.Filled.FormatIndentIncrease, label = "Increase", isActive = false) { onIncreaseIndent() }
    }
}

@Composable
fun HeadingsSection(
    currentStyle: HeadingStyle,
    onSelectStyle: (HeadingStyle) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        listOf(
            HeadingStyle.NORMAL to "Normal",
            HeadingStyle.HEADING_1 to "Heading 1",
            HeadingStyle.HEADING_2 to "Heading 2",
            HeadingStyle.HEADING_3 to "Heading 3",
            HeadingStyle.TITLE to "Title",
            HeadingStyle.SUBTITLE to "Subtitle"
        ).forEach { (style, label) ->
            val isSelected = currentStyle == style
            Surface(
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, if (isSelected) WordPrimary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                color = if (isSelected) WordPrimary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                modifier = Modifier.clip(RoundedCornerShape(12.dp)).clickable { onSelectStyle(style) }
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    color = if (isSelected) WordPrimary else MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
                )
            }
        }
    }
}

@Composable
private fun ActionChip(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    isActive: Boolean,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(1.dp, if (isActive) WordPrimary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
        color = if (isActive) WordPrimary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
        modifier = Modifier.clip(RoundedCornerShape(10.dp)).clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(icon, contentDescription = null, tint = if (isActive) WordPrimary else MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(16.dp))
            Text(label, style = MaterialTheme.typography.labelSmall, fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal, color = if (isActive) WordPrimary else MaterialTheme.colorScheme.onSurface)
        }
    }
}
