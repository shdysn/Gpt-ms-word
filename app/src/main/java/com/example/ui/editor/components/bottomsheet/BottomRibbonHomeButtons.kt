package com.example.ui.editor.components.bottomsheet

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.HorizontalDivider
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
import com.example.ui.theme.OfficeFontFamily

private val SelectedCellColor = Color(0xFFD6D6D6)

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
        SegmentedButton("B", isBold, onToggleBold, Modifier.weight(1f), FontWeight.Bold)
        SegmentedButton("I", isItalic, onToggleItalic, Modifier.weight(1f), fontStyle = FontStyle.Italic)
        SegmentedButton("U", isUnderline, onToggleUnderline, Modifier.weight(1f), textDecoration = TextDecoration.Underline)
        SegmentedButton("ab", isStrikethrough, onToggleStrikethrough, Modifier.weight(1f), textDecoration = TextDecoration.LineThrough)
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
        color = if (isSelected) SelectedCellColor else Color.Transparent,
        modifier = modifier.fillMaxHeight().clickable { onClick() }
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = label,
                fontSize = 17.sp,
                fontWeight = fontWeight,
                fontStyle = fontStyle,
                fontFamily = OfficeFontFamily,
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
        WordGridCell(isSelected = false, onClick = onDecreaseIndent, modifier = Modifier.weight(1f)) {
            WordDecreaseIndentIcon()
        }
        WordGridCell(isSelected = false, onClick = onIncreaseIndent, modifier = Modifier.weight(1f)) {
            WordIncreaseIndentIcon()
        }
        WordGridCell(isSelected = !isRtl, onClick = { if (isRtl) onToggleRtl() }, modifier = Modifier.weight(1f)) {
            WordLtrDirectionIcon()
        }
        WordGridCell(isSelected = isRtl, onClick = { if (!isRtl) onToggleRtl() }, modifier = Modifier.weight(1f)) {
            WordRtlDirectionIcon()
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
        WordGridCell(isSelected = currentAlignment == TextAlignment.START, onClick = { onSetAlignment(TextAlignment.START) }, modifier = Modifier.weight(1f)) {
            WordAlignLeftIcon()
        }
        WordGridCell(isSelected = currentAlignment == TextAlignment.CENTER, onClick = { onSetAlignment(TextAlignment.CENTER) }, modifier = Modifier.weight(1f)) {
            WordAlignCenterIcon()
        }
        WordGridCell(isSelected = currentAlignment == TextAlignment.END, onClick = { onSetAlignment(TextAlignment.END) }, modifier = Modifier.weight(1f)) {
            WordAlignRightIcon()
        }
        WordGridCell(isSelected = currentAlignment == TextAlignment.JUSTIFY, onClick = { onSetAlignment(TextAlignment.JUSTIFY) }, modifier = Modifier.weight(1f)) {
            WordAlignJustifyIcon()
        }
    }
}

@Composable
private fun WordGridCell(
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Surface(
        color = if (isSelected) SelectedCellColor else Color.Transparent,
        modifier = modifier.fillMaxHeight().clickable { onClick() }
    ) {
        Box(contentAlignment = Alignment.Center) {
            content()
        }
    }
}

/**
 * List row item with leading icon/content, title, optional underline bar, and trailing chevron (Screenshot 11).
 */
@Composable
fun HomeMenuItem(
    icon: ImageVector? = null,
    title: String,
    hasChevron: Boolean,
    badgeColorHex: String? = null,
    underlineColorHex: String? = null,
    customLeadingContent: (@Composable () -> Unit)? = null,
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
                if (customLeadingContent != null) {
                    customLeadingContent()
                } else if (icon != null) {
                    Icon(imageVector = icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
                }
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
