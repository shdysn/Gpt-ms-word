package com.example.ui.editor.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
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
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.FormatAlignCenter
import androidx.compose.material.icons.filled.FormatAlignJustify
import androidx.compose.material.icons.filled.FormatBold
import androidx.compose.material.icons.filled.FormatColorFill
import androidx.compose.material.icons.filled.FormatColorText
import androidx.compose.material.icons.filled.FormatItalic
import androidx.compose.material.icons.filled.FormatListNumbered
import androidx.compose.material.icons.filled.FormatStrikethrough
import androidx.compose.material.icons.filled.FormatUnderlined
import androidx.compose.material.icons.filled.Link
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.HeadingStyle
import com.example.data.model.ListType
import com.example.data.model.ParagraphBlock
import com.example.data.model.TextAlignment
import com.example.editor.engine.ActiveFormatting
import com.example.ui.theme.WordPrimary

/**
 * Reusable formatting toolbar providing all Microsoft Word paragraph
 * and character formatting options with touch-accessible targets,
 * selection-aware active indicators, and continuous typing state.
 */
@Composable
fun FormattingToolbar(
    activeParagraph: ParagraphBlock?,
    activeFormatting: ActiveFormatting,
    onSetHeadingStyle: (HeadingStyle) -> Unit,
    onToggleBold: () -> Unit,
    onToggleItalic: () -> Unit,
    onToggleUnderline: () -> Unit,
    onToggleStrikethrough: () -> Unit,
    onToggleSuperscript: () -> Unit,
    onToggleSubscript: () -> Unit,
    onSetFontFamily: (String) -> Unit,
    onSetFontSize: (Float) -> Unit,
    onSetTextColor: (String) -> Unit,
    onSetHighlightColor: (String?) -> Unit,
    onSetAlignment: (TextAlignment) -> Unit,
    onToggleListType: (ListType) -> Unit,
    onIncreaseIndent: () -> Unit,
    onDecreaseIndent: () -> Unit,
    onOpenLinkDialog: () -> Unit,
    onToggleRtl: () -> Unit,
    modifier: Modifier = Modifier
) {
    var styleMenuExpanded by remember { mutableStateOf(false) }
    var fontMenuExpanded by remember { mutableStateOf(false) }
    var sizeMenuExpanded by remember { mutableStateOf(false) }
    var textColorMenuExpanded by remember { mutableStateOf(false) }
    var highlightMenuExpanded by remember { mutableStateOf(false) }

    val isRtl = activeParagraph?.isRtl == true
    val fontOptions = com.example.core.typography.DocumentFontManager.AVAILABLE_FONTS
    val defaultFontForDisplay = if (isRtl) "Noto Nastaliq Urdu" else "Calibri"
    val currentFontDisplay = activeFormatting.fontFamilyName ?: defaultFontForDisplay
    val currentHeadingStyle = activeParagraph?.headingStyle ?: HeadingStyle.NORMAL

    val fontSizes = listOf(10f, 11f, 12f, 14f, 16f, 18f, 20f, 24f, 28f, 36f)

    val isBold = activeFormatting.isBold || activeParagraph?.headingStyle?.isBold == true
    val isItalic = activeFormatting.isItalic
    val isUnderline = activeFormatting.isUnderline
    val isStrikethrough = activeFormatting.isStrikethrough
    val isSuperscript = activeFormatting.isSuperscript
    val isSubscript = activeFormatting.isSubscript
    val currentAlignment = activeParagraph?.alignment ?: TextAlignment.START
    val currentListType = activeParagraph?.listType ?: ListType.NONE

    Surface(
        color = MaterialTheme.colorScheme.surface,
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Heading Style & Preset Selector (Word Styles)
            Box {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = if (currentHeadingStyle != HeadingStyle.NORMAL) WordPrimary.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier
                        .clickable { styleMenuExpanded = true }
                        .testTag("heading_style_selector")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = currentHeadingStyle.displayName,
                            fontSize = 12.sp,
                            fontWeight = if (currentHeadingStyle != HeadingStyle.NORMAL) FontWeight.Bold else FontWeight.Medium,
                            color = if (currentHeadingStyle != HeadingStyle.NORMAL) WordPrimary else MaterialTheme.colorScheme.onSurface
                        )
                        Icon(Icons.Default.ArrowDropDown, contentDescription = null, modifier = Modifier.size(16.dp))
                    }
                }

                DropdownMenu(
                    expanded = styleMenuExpanded,
                    onDismissRequest = { styleMenuExpanded = false }
                ) {
                    HeadingStyle.entries.forEach { style ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = style.displayName,
                                    fontSize = when (style) {
                                        HeadingStyle.TITLE -> 17.sp
                                        HeadingStyle.HEADING_1 -> 16.sp
                                        HeadingStyle.HEADING_2 -> 15.sp
                                        else -> 13.sp
                                    },
                                    fontWeight = if (style.isBold) FontWeight.Bold else FontWeight.Normal,
                                    fontStyle = if (style == HeadingStyle.QUOTE) androidx.compose.ui.text.font.FontStyle.Italic else androidx.compose.ui.text.font.FontStyle.Normal,
                                    color = if (currentHeadingStyle == style) WordPrimary else MaterialTheme.colorScheme.onSurface
                                )
                            },
                            trailingIcon = if (currentHeadingStyle == style) {
                                { Icon(androidx.compose.material.icons.Icons.Default.Check, contentDescription = null, tint = WordPrimary, modifier = Modifier.size(16.dp)) }
                            } else null,
                            onClick = {
                                styleMenuExpanded = false
                                onSetHeadingStyle(style)
                            }
                        )
                    }
                }
            }

            ToolbarDivider()

            // Font Family Selector
            Box {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier
                        .clickable { fontMenuExpanded = true }
                        .testTag("font_family_selector")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = currentFontDisplay,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Icon(Icons.Default.ArrowDropDown, contentDescription = null, modifier = Modifier.size(16.dp))
                    }
                }

                DropdownMenu(
                    expanded = fontMenuExpanded,
                    onDismissRequest = { fontMenuExpanded = false }
                ) {
                    fontOptions.forEach { opt ->
                        DropdownMenuItem(
                            text = {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(opt.displayName, fontSize = 13.sp)
                                    if (opt.isUrduOptimized) {
                                        Surface(
                                            color = WordPrimary.copy(alpha = 0.12f),
                                            shape = RoundedCornerShape(4.dp)
                                        ) {
                                            Text(
                                                text = "اردو",
                                                color = WordPrimary,
                                                fontSize = 10.sp,
                                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                            )
                                        }
                                    }
                                }
                            },
                            onClick = {
                                fontMenuExpanded = false
                                onSetFontFamily(opt.id)
                            }
                        )
                    }
                }
            }

            // Font Size Selector
            Box {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier
                        .clickable { sizeMenuExpanded = true }
                        .testTag("font_size_selector")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                    ) {
                        val currentSize = activeFormatting.fontSizeSp ?: 11f
                        Text(
                            text = "${currentSize.toInt()} pt",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Icon(Icons.Default.ArrowDropDown, contentDescription = null, modifier = Modifier.size(16.dp))
                    }
                }

                DropdownMenu(
                    expanded = sizeMenuExpanded,
                    onDismissRequest = { sizeMenuExpanded = false }
                ) {
                    fontSizes.forEach { sz ->
                        DropdownMenuItem(
                            text = { Text("${sz.toInt()} pt", fontSize = 13.sp) },
                            onClick = {
                                sizeMenuExpanded = false
                                onSetFontSize(sz)
                            }
                        )
                    }
                }
            }

            ToolbarDivider()

            // Bold / Italic / Underline / Strikethrough
            FormatToggleButton(
                icon = Icons.Default.FormatBold,
                label = "Bold",
                isActive = isBold,
                onClick = onToggleBold,
                tag = "format_bold"
            )
            FormatToggleButton(
                icon = Icons.Default.FormatItalic,
                label = "Italic",
                isActive = isItalic,
                onClick = onToggleItalic,
                tag = "format_italic"
            )
            FormatToggleButton(
                icon = Icons.Default.FormatUnderlined,
                label = "Underline",
                isActive = isUnderline,
                onClick = onToggleUnderline,
                tag = "format_underline"
            )
            FormatToggleButton(
                icon = Icons.Default.FormatStrikethrough,
                label = "Strikethrough",
                isActive = isStrikethrough,
                onClick = onToggleStrikethrough,
                tag = "format_strikethrough"
            )

            // Superscript & Subscript
            FormatTextToggleButton(
                text = "X²",
                label = "Superscript",
                isActive = isSuperscript,
                onClick = onToggleSuperscript,
                tag = "format_superscript"
            )
            FormatTextToggleButton(
                text = "X₂",
                label = "Subscript",
                isActive = isSubscript,
                onClick = onToggleSubscript,
                tag = "format_subscript"
            )

            ToolbarDivider()

            // Text Color Picker
            Box {
                IconButton(
                    onClick = { textColorMenuExpanded = true },
                    modifier = Modifier.size(36.dp).testTag("format_text_color")
                ) {
                    Icon(Icons.Default.FormatColorText, contentDescription = "Text Color", tint = WordPrimary)
                }

                DropdownMenu(
                    expanded = textColorMenuExpanded,
                    onDismissRequest = { textColorMenuExpanded = false }
                ) {
                    val colors = listOf(
                        "#000000" to "Black",
                        "#185ABD" to "Word Blue",
                        "#C00000" to "Dark Red",
                        "#107C41" to "Office Green",
                        "#7030A0" to "Purple",
                        "#7F7F7F" to "Gray"
                    )
                    colors.forEach { (hex, name) ->
                        DropdownMenuItem(
                            leadingIcon = {
                                Box(
                                    modifier = Modifier
                                        .size(16.dp)
                                        .background(Color(android.graphics.Color.parseColor(hex)), CircleShape)
                                )
                            },
                            text = { Text(name, fontSize = 13.sp) },
                            onClick = {
                                textColorMenuExpanded = false
                                onSetTextColor(hex)
                            }
                        )
                    }
                }
            }

            // Highlight Color Picker
            Box {
                IconButton(
                    onClick = { highlightMenuExpanded = true },
                    modifier = Modifier.size(36.dp).testTag("format_highlight_color")
                ) {
                    Icon(Icons.Default.FormatColorFill, contentDescription = "Highlight Color", tint = Color(0xFFD83B01))
                }

                DropdownMenu(
                    expanded = highlightMenuExpanded,
                    onDismissRequest = { highlightMenuExpanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("No Color", fontSize = 13.sp) },
                        onClick = {
                            highlightMenuExpanded = false
                            onSetHighlightColor(null)
                        }
                    )
                    val highlights = listOf(
                        "#FFF275" to "Yellow Highlight",
                        "#A8F387" to "Bright Green",
                        "#94ECF8" to "Cyan Highlight",
                        "#FFB7DF" to "Pink Highlight"
                    )
                    highlights.forEach { (hex, name) ->
                        DropdownMenuItem(
                            leadingIcon = {
                                Box(
                                    modifier = Modifier
                                        .size(16.dp)
                                        .background(Color(android.graphics.Color.parseColor(hex)), RoundedCornerShape(2.dp))
                                )
                            },
                            text = { Text(name, fontSize = 13.sp) },
                            onClick = {
                                highlightMenuExpanded = false
                                onSetHighlightColor(hex)
                            }
                        )
                    }
                }
            }

            ToolbarDivider()

            // Alignments
            FormatToggleButton(
                icon = Icons.AutoMirrored.Filled.FormatAlignLeft,
                label = "Align Left",
                isActive = if (isRtl) currentAlignment == TextAlignment.END else currentAlignment == TextAlignment.START,
                onClick = { onSetAlignment(if (isRtl) TextAlignment.END else TextAlignment.START) },
                tag = "align_left"
            )
            FormatToggleButton(
                icon = Icons.Default.FormatAlignCenter,
                label = "Center",
                isActive = currentAlignment == TextAlignment.CENTER,
                onClick = { onSetAlignment(TextAlignment.CENTER) },
                tag = "align_center"
            )
            FormatToggleButton(
                icon = Icons.AutoMirrored.Filled.FormatAlignRight,
                label = "Align Right",
                isActive = if (isRtl) currentAlignment == TextAlignment.START else currentAlignment == TextAlignment.END,
                onClick = { onSetAlignment(if (isRtl) TextAlignment.START else TextAlignment.END) },
                tag = "align_right"
            )
            FormatToggleButton(
                icon = Icons.Default.FormatAlignJustify,
                label = "Justify",
                isActive = currentAlignment == TextAlignment.JUSTIFY,
                onClick = { onSetAlignment(TextAlignment.JUSTIFY) },
                tag = "align_justify"
            )

            ToolbarDivider()

            // Bulleted & Numbered Lists
            FormatToggleButton(
                icon = Icons.AutoMirrored.Filled.FormatListBulleted,
                label = "Bulleted List",
                isActive = currentListType == ListType.BULLET,
                onClick = { onToggleListType(ListType.BULLET) },
                tag = "list_bullet"
            )
            FormatToggleButton(
                icon = Icons.Default.FormatListNumbered,
                label = "Numbered List",
                isActive = currentListType == ListType.NUMBERED,
                onClick = { onToggleListType(ListType.NUMBERED) },
                tag = "list_numbered"
            )

            // Indentation
            FormatToggleButton(
                icon = Icons.AutoMirrored.Filled.FormatIndentDecrease,
                label = "Decrease Indent",
                isActive = false,
                onClick = onDecreaseIndent,
                tag = "indent_decrease"
            )
            FormatToggleButton(
                icon = Icons.AutoMirrored.Filled.FormatIndentIncrease,
                label = "Increase Indent",
                isActive = (activeParagraph?.indentationLevel ?: 0) > 0,
                onClick = onIncreaseIndent,
                tag = "indent_increase"
            )

            // Hyperlink
            FormatToggleButton(
                icon = Icons.Default.Link,
                label = "Hyperlink",
                isActive = activeFormatting.linkUrl != null,
                onClick = onOpenLinkDialog,
                tag = "format_link"
            )

            ToolbarDivider()

            // Urdu / RTL Direction Toggle
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = if (isRtl) WordPrimary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant,
                border = if (isRtl) androidx.compose.foundation.BorderStroke(1.5.dp, WordPrimary) else null,
                modifier = Modifier
                    .clickable(onClick = onToggleRtl)
                    .testTag("toggle_urdu_rtl")
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = if (isRtl) "اردو (RTL)" else "English (LTR)",
                        fontSize = 12.sp,
                        fontWeight = if (isRtl) FontWeight.Bold else FontWeight.Medium,
                        color = if (isRtl) WordPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun FormatToggleButton(
    icon: ImageVector,
    label: String,
    isActive: Boolean,
    onClick: () -> Unit,
    tag: String
) {
    Surface(
        shape = RoundedCornerShape(6.dp),
        color = if (isActive) WordPrimary.copy(alpha = 0.15f) else Color.Transparent,
        border = if (isActive) androidx.compose.foundation.BorderStroke(1.dp, WordPrimary) else null,
        modifier = Modifier
            .size(36.dp)
            .clickable(onClick = onClick)
            .testTag(tag)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (isActive) WordPrimary else MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
fun FormatTextToggleButton(
    text: String,
    label: String,
    isActive: Boolean,
    onClick: () -> Unit,
    tag: String
) {
    Surface(
        shape = RoundedCornerShape(6.dp),
        color = if (isActive) WordPrimary.copy(alpha = 0.15f) else Color.Transparent,
        border = if (isActive) androidx.compose.foundation.BorderStroke(1.dp, WordPrimary) else null,
        modifier = Modifier
            .size(36.dp)
            .clickable(onClick = onClick)
            .testTag(tag)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = text,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = if (isActive) WordPrimary else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun ToolbarDivider() {
    Box(
        modifier = Modifier
            .height(20.dp)
            .width(1.dp)
            .background(Color(0xFFE2E8F0))
    )
}
