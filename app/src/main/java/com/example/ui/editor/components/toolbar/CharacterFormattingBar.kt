package com.example.ui.editor.components.toolbar

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.FormatBold
import androidx.compose.material.icons.filled.FormatColorFill
import androidx.compose.material.icons.filled.FormatColorText
import androidx.compose.material.icons.filled.FormatItalic
import androidx.compose.material.icons.filled.FormatPaint
import androidx.compose.material.icons.filled.FormatStrikethrough
import androidx.compose.material.icons.filled.FormatUnderlined
import androidx.compose.material.icons.filled.Title
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.typography.DocumentFontManager
import com.example.data.model.ParagraphBlock
import com.example.editor.engine.ActiveFormatting
import com.example.editor.engine.CaseType
import com.example.ui.theme.WordPrimary

@Composable
fun CharacterFormattingBar(
    activeParagraph: ParagraphBlock?,
    activeFormatting: ActiveFormatting,
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
    isFormatPainterActive: Boolean,
    onToggleFormatPainter: () -> Unit,
    onClearFormatting: () -> Unit,
    onChangeCase: (CaseType) -> Unit,
    onOpenTypographyEffectsDialog: () -> Unit
) {
    var fontMenuExpanded by remember { mutableStateOf(false) }
    var sizeMenuExpanded by remember { mutableStateOf(false) }
    var textColorMenuExpanded by remember { mutableStateOf(false) }
    var highlightMenuExpanded by remember { mutableStateOf(false) }
    var caseMenuExpanded by remember { mutableStateOf(false) }

    val isRtl = activeParagraph?.isRtl == true
    val fontOptions = DocumentFontManager.AVAILABLE_FONTS
    val defaultFontForDisplay = if (isRtl) "Noto Nastaliq Urdu" else "Calibri"
    val currentFontDisplay = activeFormatting.fontFamilyName ?: defaultFontForDisplay
    val fontSizes = listOf(10f, 11f, 12f, 14f, 16f, 18f, 20f, 24f, 28f, 36f)

    val isBold = activeFormatting.isBold || activeParagraph?.headingStyle?.isBold == true
    val isItalic = activeFormatting.isItalic
    val isUnderline = activeFormatting.isUnderline
    val isStrikethrough = activeFormatting.isStrikethrough
    val isSuperscript = activeFormatting.isSuperscript
    val isSubscript = activeFormatting.isSubscript

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        // Font Family Selector
        Box {
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier
                    .height(34.dp)
                    .clickable { fontMenuExpanded = true }
                    .testTag("font_family_selector")
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 8.dp)
                ) {
                    Text(
                        text = currentFontDisplay,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        softWrap = false
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
                    .height(34.dp)
                    .clickable { sizeMenuExpanded = true }
                    .testTag("font_size_selector")
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 8.dp)
                ) {
                    val currentSize = activeFormatting.fontSizeSp ?: 11f
                    Text(
                        text = "${currentSize.toInt()} pt",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        softWrap = false
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

        IconButton(
            onClick = onClearFormatting,
            modifier = Modifier.size(36.dp).testTag("format_clear_btn")
        ) {
            Icon(
                imageVector = Icons.Default.FormatPaint,
                contentDescription = "Clear All Formatting",
                tint = Color(0xFFEF4444)
            )
        }

        Box {
            FormatTextToggleButton(
                text = "Aa",
                label = "Change Case",
                isActive = false,
                onClick = { caseMenuExpanded = true },
                tag = "format_change_case"
            )

            DropdownMenu(
                expanded = caseMenuExpanded,
                onDismissRequest = { caseMenuExpanded = false }
            ) {
                CaseType.entries.forEach { caseType ->
                    DropdownMenuItem(
                        text = { Text(caseType.label, fontSize = 13.sp) },
                        onClick = {
                            caseMenuExpanded = false
                            onChangeCase(caseType)
                        }
                    )
                }
            }
        }

        IconButton(
            onClick = onOpenTypographyEffectsDialog,
            modifier = Modifier.size(36.dp).testTag("format_typography_effects_btn")
        ) {
            Icon(
                imageVector = Icons.Default.Title,
                contentDescription = "Text Effects & Spacing",
                tint = WordPrimary
            )
        }

        ToolbarDivider()

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

        FormatToggleButton(
            icon = Icons.Default.FormatPaint,
            label = if (isFormatPainterActive) "Painter (Active)" else "Format Painter",
            isActive = isFormatPainterActive,
            onClick = onToggleFormatPainter,
            tag = "format_painter"
        )
    }
}
