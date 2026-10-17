package com.example.ui.editor.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.editor.engine.ActiveFormatting
import com.example.ui.theme.OfficeFontFamily

private val ActiveCircleBg = Color(0xFFE0E0E0)
private val MoreButtonBg = Color(0xFFE8E8E8)
private val TextDark = Color(0xFF262626)

/**
 * Modern floating pill toolbar docked directly above the Android soft keyboard.
 * Exactly matches Microsoft Word Mobile (Screenshot: B, I, U, Highlight, Font Color, •••).
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
    onToggleStrikethrough: () -> Unit = {},
    onHighlightClick: () -> Unit,
    onTextColorClick: () -> Unit,
    onToggleExpandRibbon: () -> Unit = {},
    onDeselect: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val hasSelection = (!selection.collapsed && selection.min < selection.max) || isDocumentAllSelected
    val isVisible = (isKeyboardVisible || hasSelection) && !isRibbonExpanded

    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn() + slideInVertically(initialOffsetY = { it }),
        exit = fadeOut() + slideOutVertically(targetOffsetY = { it }),
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 6.dp)
                .testTag("keyboard_accessory_bar"),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                shape = RoundedCornerShape(32.dp),
                color = Color.White,
                tonalElevation = 4.dp,
                shadowElevation = 10.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .clip(RoundedCornerShape(32.dp))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 1. Bold (B)
                    QuickTextButton(
                        label = "B",
                        isActive = activeFormatting.isBold,
                        fontWeight = FontWeight.Bold,
                        onClick = onToggleBold,
                        testTag = "quick_format_bold"
                    )

                    // 2. Italic (I)
                    QuickTextButton(
                        label = "I",
                        isActive = activeFormatting.isItalic,
                        fontStyle = FontStyle.Italic,
                        onClick = onToggleItalic,
                        testTag = "quick_format_italic"
                    )

                    // 3. Underline (U)
                    QuickTextButton(
                        label = "U",
                        isActive = activeFormatting.isUnderline,
                        textDecoration = TextDecoration.Underline,
                        onClick = onToggleUnderline,
                        testTag = "quick_format_underline"
                    )

                    // 4. Highlight Marker
                    QuickIconButton(
                        isActive = activeFormatting.highlightColorHex != null,
                        onClick = onHighlightClick,
                        testTag = "quick_format_highlight"
                    ) {
                        QuickHighlightContent(colorHex = activeFormatting.highlightColorHex)
                    }

                    // 5. Font Color (A)
                    QuickIconButton(
                        isActive = activeFormatting.textColorHex != null,
                        onClick = onTextColorClick,
                        testTag = "quick_format_color"
                    ) {
                        QuickTextColorContent(colorHex = activeFormatting.textColorHex)
                    }

                    // 6. Overflow / More Button (•••)
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(MoreButtonBg)
                            .clickable { onToggleExpandRibbon() }
                            .testTag("quick_format_expand_ribbon"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreHoriz,
                            contentDescription = "Open More Formatting",
                            tint = TextDark,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun QuickTextButton(
    label: String,
    isActive: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    fontWeight: FontWeight = FontWeight.Normal,
    fontStyle: FontStyle = FontStyle.Normal,
    textDecoration: TextDecoration? = null,
    testTag: String = ""
) {
    Box(
        modifier = modifier
            .size(42.dp)
            .clip(CircleShape)
            .background(if (isActive) ActiveCircleBg else Color.Transparent)
            .clickable { onClick() }
            .testTag(testTag),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontSize = 18.sp,
            fontWeight = fontWeight,
            fontStyle = fontStyle,
            fontFamily = OfficeFontFamily,
            textDecoration = textDecoration,
            color = TextDark
        )
    }
}

@Composable
private fun QuickIconButton(
    isActive: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    testTag: String = "",
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .size(42.dp)
            .clip(CircleShape)
            .background(if (isActive) ActiveCircleBg else Color.Transparent)
            .clickable { onClick() }
            .testTag(testTag),
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}

@Composable
private fun QuickHighlightContent(colorHex: String?) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(1.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Edit,
            contentDescription = "Highlight",
            tint = TextDark,
            modifier = Modifier.size(19.dp)
        )
        val barColor = if (colorHex != null) {
            try { Color(android.graphics.Color.parseColor(colorHex)) } catch (_: Exception) { Color(0xFFEAB308) }
        } else {
            Color(0xFF262626)
        }
        Box(
            modifier = Modifier
                .width(16.dp)
                .height(3.5.dp)
                .clip(RoundedCornerShape(1.dp))
                .background(barColor)
        )
    }
}

@Composable
private fun QuickTextColorContent(colorHex: String?) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(1.dp)
    ) {
        Text(
            text = "A",
            fontSize = 17.sp,
            fontWeight = FontWeight.SemiBold,
            fontFamily = OfficeFontFamily,
            color = TextDark
        )
        val barColor = if (colorHex != null) {
            try { Color(android.graphics.Color.parseColor(colorHex)) } catch (_: Exception) { Color(0xFFDC2626) }
        } else {
            Color(0xFFDC2626)
        }
        Box(
            modifier = Modifier
                .width(16.dp)
                .height(3.5.dp)
                .clip(RoundedCornerShape(1.dp))
                .background(barColor)
        )
    }
}
