package com.example.ui.editor.components.bottomsheet

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.automirrored.filled.FormatAlignLeft
import androidx.compose.material.icons.automirrored.filled.FormatAlignRight
import androidx.compose.material.icons.automirrored.filled.FormatIndentDecrease
import androidx.compose.material.icons.automirrored.filled.FormatIndentIncrease
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.FormatAlignCenter
import androidx.compose.material.icons.filled.FormatAlignJustify
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.TextAlignment

/**
 * 4-Segment button row: B, I, U, ab (Screenshot 11).
 */
@Composable
fun BasicFormatButtonRow(
    isBold: Boolean,
    isItalic: Boolean,
    isUnderline: Boolean,
    isStrikethrough: Boolean,
    onToggleBold: () -> Unit,
    onToggleItalic: () -> Unit,
    onToggleUnderline: () -> Unit,
    onToggleStrikethrough: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth().height(42.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        SegmentedButton(
            label = "B",
            fontWeight = FontWeight.Bold,
            isSelected = isBold,
            onClick = onToggleBold,
            modifier = Modifier.weight(1f)
        )
        SegmentedButton(
            label = "I",
            fontStyle = FontStyle.Italic,
            isSelected = isItalic,
            onClick = onToggleItalic,
            modifier = Modifier.weight(1f)
        )
        SegmentedButton(
            label = "U",
            textDecoration = TextDecoration.Underline,
            isSelected = isUnderline,
            onClick = onToggleUnderline,
            modifier = Modifier.weight(1f)
        )
        SegmentedButton(
            label = "ab",
            textDecoration = TextDecoration.LineThrough,
            isSelected = isStrikethrough,
            onClick = onToggleStrikethrough,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun SegmentedButton(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    fontWeight: FontWeight = FontWeight.Normal,
    fontStyle: FontStyle = FontStyle.Normal,
    textDecoration: TextDecoration? = null
) {
    Surface(
        shape = RoundedCornerShape(2.dp),
        color = if (isSelected) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.9f) else Color.Transparent,
        modifier = modifier.clip(RoundedCornerShape(2.dp)).clickable { onClick() }
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = label,
                fontSize = 17.sp,
                fontWeight = fontWeight,
                fontStyle = fontStyle,
                textDecoration = textDecoration,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

/**
 * 4-Button row: Decrease Indent, Increase Indent, LTR (>¶), RTL (¶<) (Screenshot 7).
 */
@Composable
fun IndentAndDirectionButtonRow(
    isRtl: Boolean,
    onDecreaseIndent: () -> Unit,
    onIncreaseIndent: () -> Unit,
    onToggleRtl: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth().height(42.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        IconButtonWithBg(icon = Icons.AutoMirrored.Filled.FormatIndentDecrease, isSelected = false, onClick = onDecreaseIndent, modifier = Modifier.weight(1f))
        IconButtonWithBg(icon = Icons.AutoMirrored.Filled.FormatIndentIncrease, isSelected = false, onClick = onIncreaseIndent, modifier = Modifier.weight(1f))

        Surface(
            shape = RoundedCornerShape(2.dp),
            color = if (!isRtl) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.9f) else Color.Transparent,
            modifier = Modifier.weight(1f).clip(RoundedCornerShape(2.dp)).clickable { if (isRtl) onToggleRtl() }
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(text = ">¶", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            }
        }

        Surface(
            shape = RoundedCornerShape(2.dp),
            color = if (isRtl) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.9f) else Color.Transparent,
            modifier = Modifier.weight(1f).clip(RoundedCornerShape(2.dp)).clickable { if (!isRtl) onToggleRtl() }
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(text = "¶<", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            }
        }
    }
}

/**
 * 4-Button row: Align Left, Center, Align Right, Justify (Screenshot 7).
 */
@Composable
fun AlignmentButtonRow(
    currentAlignment: TextAlignment,
    onSetAlignment: (TextAlignment) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth().height(42.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        IconButtonWithBg(icon = Icons.AutoMirrored.Filled.FormatAlignLeft, isSelected = currentAlignment == TextAlignment.START, onClick = { onSetAlignment(TextAlignment.START) }, modifier = Modifier.weight(1f))
        IconButtonWithBg(icon = Icons.Default.FormatAlignCenter, isSelected = currentAlignment == TextAlignment.CENTER, onClick = { onSetAlignment(TextAlignment.CENTER) }, modifier = Modifier.weight(1f))
        IconButtonWithBg(icon = Icons.AutoMirrored.Filled.FormatAlignRight, isSelected = currentAlignment == TextAlignment.END, onClick = { onSetAlignment(TextAlignment.END) }, modifier = Modifier.weight(1f))
        IconButtonWithBg(icon = Icons.Default.FormatAlignJustify, isSelected = currentAlignment == TextAlignment.JUSTIFY, onClick = { onSetAlignment(TextAlignment.JUSTIFY) }, modifier = Modifier.weight(1f))
    }
}

@Composable
private fun IconButtonWithBg(
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(2.dp),
        color = if (isSelected) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.9f) else Color.Transparent,
        modifier = modifier.clip(RoundedCornerShape(2.dp)).clickable { onClick() }
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(imageVector = icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(20.dp))
        }
    }
}

/**
 * List row item with leading icon, title, optional underline bar, and trailing chevron (Screenshot 11).
 */
@Composable
fun HomeMenuItem(
    icon: ImageVector,
    title: String,
    hasChevron: Boolean,
    badgeColorHex: String? = null,
    underlineColorHex: String? = null,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(4.dp),
        color = Color.Transparent,
        modifier = modifier.fillMaxWidth().clip(RoundedCornerShape(4.dp)).clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 11.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(modifier = Modifier.size(24.dp), contentAlignment = Alignment.Center) {
                Icon(imageVector = icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
                if (underlineColorHex != null) {
                    val color = try { Color(android.graphics.Color.parseColor(underlineColorHex)) } catch (_: Exception) { Color.Red }
                    Box(modifier = Modifier.align(Alignment.BottomCenter).width(16.dp).height(3.dp).background(color))
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )

            if (hasChevron) {
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
