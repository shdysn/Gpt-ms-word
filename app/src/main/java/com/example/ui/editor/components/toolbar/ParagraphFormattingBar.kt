package com.example.ui.editor.components.toolbar

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.FormatAlignLeft
import androidx.compose.material.icons.automirrored.filled.FormatAlignRight
import androidx.compose.material.icons.automirrored.filled.FormatIndentDecrease
import androidx.compose.material.icons.automirrored.filled.FormatIndentIncrease
import androidx.compose.material.icons.automirrored.filled.FormatListBulleted
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FormatAlignCenter
import androidx.compose.material.icons.filled.FormatAlignJustify
import androidx.compose.material.icons.filled.FormatLineSpacing
import androidx.compose.material.icons.filled.FormatListNumbered
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CustomStyle
import com.example.data.model.HeadingStyle
import com.example.data.model.ListType
import com.example.data.model.ParagraphBlock
import com.example.data.model.TextAlignment
import com.example.editor.engine.ActiveFormatting
import com.example.ui.theme.WordPrimary
import kotlin.math.abs

@Composable
fun ParagraphFormattingBar(
    activeParagraph: ParagraphBlock?,
    activeFormatting: ActiveFormatting,
    onSetHeadingStyle: (HeadingStyle) -> Unit,
    onSetAlignment: (TextAlignment) -> Unit,
    onToggleListType: (ListType) -> Unit,
    onIncreaseIndent: () -> Unit,
    onDecreaseIndent: () -> Unit,
    onOpenLinkDialog: () -> Unit,
    onToggleRtl: () -> Unit,
    onSetLineSpacing: (Float) -> Unit,
    onOpenParagraphSpacingDialog: () -> Unit,
    onToggleChecklist: () -> Unit,
    customStyles: List<CustomStyle>,
    onApplyCustomStyle: (CustomStyle) -> Unit,
    onCreateCustomStyle: () -> Unit
) {
    var styleMenuExpanded by remember { mutableStateOf(false) }
    var lineSpacingMenuExpanded by remember { mutableStateOf(false) }

    val isRtl = activeParagraph?.isRtl == true
    val currentHeadingStyle = activeParagraph?.headingStyle ?: HeadingStyle.NORMAL
    val currentAlignment = activeParagraph?.alignment ?: TextAlignment.START
    val currentListType = activeParagraph?.listType ?: ListType.NONE

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        // Paragraph / Heading Style Selector
        Box {
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier
                    .height(34.dp)
                    .clickable { styleMenuExpanded = true }
                    .testTag("heading_style_selector")
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 8.dp)
                ) {
                    Text(
                        text = currentHeadingStyle.displayName,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = WordPrimary,
                        maxLines = 1,
                        softWrap = false
                    )
                    Icon(Icons.Default.ArrowDropDown, contentDescription = "Select Heading Style", modifier = Modifier.size(16.dp), tint = WordPrimary)
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
                                fontSize = style.defaultFontSizeSp.coerceIn(12f, 20f).sp,
                                fontWeight = if (style.isBold || currentHeadingStyle == style) FontWeight.Bold else FontWeight.Normal,
                                color = if (currentHeadingStyle == style) WordPrimary else MaterialTheme.colorScheme.onSurface
                            )
                        },
                        onClick = {
                            styleMenuExpanded = false
                            onSetHeadingStyle(style)
                        }
                    )
                }

                if (customStyles.isNotEmpty()) {
                    HorizontalDivider()
                    customStyles.forEach { cs ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = "★ ${cs.name}",
                                    fontSize = 13.sp,
                                    fontWeight = if (cs.isBold) FontWeight.Bold else FontWeight.Normal,
                                    color = WordPrimary
                                )
                            },
                            onClick = {
                                styleMenuExpanded = false
                                onApplyCustomStyle(cs)
                            }
                        )
                    }
                }

                HorizontalDivider()
                DropdownMenuItem(
                    text = {
                        Text(
                            text = "+ Create New Style...",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = WordPrimary
                        )
                    },
                    onClick = {
                        styleMenuExpanded = false
                        onCreateCustomStyle()
                    }
                )
            }
        }

        ToolbarDivider()

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

        Box {
            IconButton(
                onClick = { lineSpacingMenuExpanded = true },
                modifier = Modifier.size(36.dp).testTag("format_line_spacing")
            ) {
                Icon(
                    imageVector = Icons.Default.FormatLineSpacing,
                    contentDescription = "Line Spacing",
                    tint = if ((activeParagraph?.lineSpacingMultiplier ?: 1.25f) != 1.25f) WordPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            DropdownMenu(
                expanded = lineSpacingMenuExpanded,
                onDismissRequest = { lineSpacingMenuExpanded = false }
            ) {
                val spacingOptions = listOf(
                    1.0f to "1.0 (Single)",
                    1.15f to "1.15 Lines",
                    1.25f to "1.25 (Word Normal)",
                    1.5f to "1.5 Lines",
                    2.0f to "2.0 (Double Spacing)",
                    2.5f to "2.5 Lines",
                    3.0f to "3.0 Lines"
                )
                val currentSpacing = activeParagraph?.lineSpacingMultiplier ?: 1.25f
                spacingOptions.forEach { (mult, label) ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = label,
                                fontSize = 13.sp,
                                fontWeight = if (abs(currentSpacing - mult) < 0.05f) FontWeight.Bold else FontWeight.Normal,
                                color = if (abs(currentSpacing - mult) < 0.05f) WordPrimary else MaterialTheme.colorScheme.onSurface
                            )
                        },
                        onClick = {
                            lineSpacingMenuExpanded = false
                            onSetLineSpacing(mult)
                        }
                    )
                }
            }
        }

        ToolbarDivider()

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
        FormatToggleButton(
            icon = Icons.Default.CheckCircle,
            label = "Checklist",
            isActive = activeParagraph?.isChecklist == true,
            onClick = onToggleChecklist,
            tag = "list_checklist"
        )

        ToolbarDivider()

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
            isActive = (activeParagraph?.indentationLevel ?: 0) > 0 || (activeParagraph?.listLevel ?: 0) > 0,
            onClick = onIncreaseIndent,
            tag = "indent_increase"
        )

        IconButton(
            onClick = onOpenParagraphSpacingDialog,
            modifier = Modifier.size(36.dp).testTag("paragraph_spacing_indents_btn")
        ) {
            Icon(
                imageVector = Icons.Default.Tune,
                contentDescription = "First Line Indents & Spacing",
                tint = if ((activeParagraph?.firstLineIndentPt ?: 0f) > 0f || (activeParagraph?.hangingIndentPt ?: 0f) > 0f || (activeParagraph?.dropCapLines ?: 0) > 0) WordPrimary else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        ToolbarDivider()

        FormatToggleButton(
            icon = Icons.Default.Link,
            label = "Hyperlink",
            isActive = activeFormatting.linkUrl != null,
            onClick = onOpenLinkDialog,
            tag = "format_link"
        )

        ToolbarDivider()

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
