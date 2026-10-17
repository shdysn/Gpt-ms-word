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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.FormatAlignCenter
import androidx.compose.material.icons.filled.FormatAlignJustify
import androidx.compose.material.icons.filled.FormatBold
import androidx.compose.material.icons.filled.FormatClear
import androidx.compose.material.icons.filled.FormatColorText
import androidx.compose.material.icons.filled.FormatItalic
import androidx.compose.material.icons.filled.FormatListNumbered
import androidx.compose.material.icons.filled.FormatPaint
import androidx.compose.material.icons.filled.FormatStrikethrough
import androidx.compose.material.icons.filled.FormatUnderlined
import androidx.compose.material.icons.filled.Highlight
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Subscript
import androidx.compose.material.icons.filled.Superscript
import androidx.compose.material.icons.filled.SwapHoriz
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.HeadingStyle
import com.example.data.model.ListType
import com.example.data.model.ParagraphBlock
import com.example.data.model.TextAlignment
import com.example.ui.editor.EditorDialogsState
import com.example.ui.editor.EditorViewModel
import com.example.ui.theme.WordPrimary

/**
 * Categorized vertical layout for the Home tab in the Bottom Ribbon Drawer.
 * Neatly groups typography, styles, colors, alignment, lists, and headings into
 * spacious, thumb-friendly sections without ugly horizontal overcrowding.
 */
@Composable
fun BottomRibbonHomePane(
    viewModel: EditorViewModel,
    activeParagraph: ParagraphBlock?,
    dState: EditorDialogsState,
    modifier: Modifier = Modifier
) {
    val formatting by viewModel.activeFormatting.collectAsStateWithLifecycle()
    val isFormatPainterActive by viewModel.isFormatPainterActive.collectAsStateWithLifecycle()

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Section 1: Font Family & Font Size
        FontAndSizeSection(
            fontFamily = formatting.fontFamilyName ?: "Calibri",
            fontSize = formatting.fontSizeSp ?: 15f,
            onSetFontFamily = viewModel::setFontFamily,
            onSetFontSize = viewModel::setFontSize
        )

        // Section 2: Character Styles (Bold, Italic, Underline, Strikethrough, Super/Sub, Clear)
        CharacterStylesGrid(
            isBold = formatting.isBold,
            isItalic = formatting.isItalic,
            isUnderline = formatting.isUnderline,
            isStrikethrough = formatting.isStrikethrough,
            isSuperscript = formatting.isSuperscript,
            isSubscript = formatting.isSubscript,
            isFormatPainterActive = isFormatPainterActive,
            onToggleBold = viewModel::toggleBold,
            onToggleItalic = viewModel::toggleItalic,
            onToggleUnderline = viewModel::toggleUnderline,
            onToggleStrikethrough = viewModel::toggleStrikethrough,
            onToggleSuperscript = viewModel::toggleSuperscript,
            onToggleSubscript = viewModel::toggleSubscript,
            onToggleFormatPainter = viewModel::toggleFormatPainter,
            onClearFormatting = viewModel::clearFormatting
        )

        // Section 3: Colors (Text Color & Highlight)
        ColorsSection(
            textColorHex = formatting.textColorHex,
            highlightColorHex = formatting.highlightColorHex,
            onSetTextColor = viewModel::setTextColor,
            onSetHighlightColor = viewModel::setHighlightColor
        )

        // Section 4: Paragraph Alignment & RTL / LTR Direction
        AlignmentAndDirectionSection(
            alignment = activeParagraph?.alignment ?: TextAlignment.START,
            isRtl = activeParagraph?.isRtl ?: false,
            onSetAlignment = viewModel::setAlignment,
            onToggleRtl = viewModel::toggleRtl
        )

        // Section 5: Lists & Indentation
        ListsAndIndentsSection(
            listType = activeParagraph?.listType ?: ListType.NONE,
            onToggleListType = viewModel::toggleListType,
            onToggleChecklist = viewModel::toggleChecklist,
            onIncreaseIndent = viewModel::increaseIndent,
            onDecreaseIndent = viewModel::decreaseIndent
        )

        // Section 6: Heading Styles
        HeadingsSection(
            currentStyle = activeParagraph?.headingStyle ?: HeadingStyle.NORMAL,
            onSelectStyle = viewModel::setHeadingStyle
        )
    }
}

@Composable
private fun FontAndSizeSection(
    fontFamily: String,
    fontSize: Float,
    onSetFontFamily: (String) -> Unit,
    onSetFontSize: (Float) -> Unit
) {
    val fonts = listOf(
        "Calibri" to "Calibri",
        "Arial" to "Arial",
        "Times New Roman" to "Times New Roman",
        "Noto Nastaliq Urdu" to "اردو نستعلیق",
        "Noto Sans Arabic" to "عربي",
        "Georgia" to "Georgia",
        "Roboto" to "Roboto"
    )
    var showFontMenu by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Font Family Selector
        Box(modifier = Modifier.weight(1f)) {
            Surface(
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showFontMenu = true }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    val displayName = fonts.firstOrNull { it.first.equals(fontFamily, ignoreCase = true) }?.second ?: fontFamily
                    Text(displayName, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium, maxLines = 1)
                    Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            DropdownMenu(expanded = showFontMenu, onDismissRequest = { showFontMenu = false }) {
                fonts.forEach { (key, label) ->
                    DropdownMenuItem(
                        text = { Text(label, fontWeight = if (key.equals(fontFamily, true)) FontWeight.Bold else FontWeight.Normal) },
                        trailingIcon = if (key.equals(fontFamily, true)) {
                            { Icon(Icons.Default.Check, contentDescription = null, tint = WordPrimary, modifier = Modifier.size(16.dp)) }
                        } else null,
                        onClick = {
                            onSetFontFamily(key)
                            showFontMenu = false
                        }
                    )
                }
            }
        }

        // Font Size Stepper
        Surface(
            shape = RoundedCornerShape(10.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
            ) {
                IconButton(onClick = { if (fontSize > 6f) onSetFontSize(fontSize - 1f) }, modifier = Modifier.size(34.dp)) {
                    Icon(Icons.Default.Remove, contentDescription = "Decrease Font Size", modifier = Modifier.size(16.dp))
                }
                Text(
                    text = "${fontSize.toInt()} pt",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 6.dp)
                )
                IconButton(onClick = { if (fontSize < 72f) onSetFontSize(fontSize + 1f) }, modifier = Modifier.size(34.dp)) {
                    Icon(Icons.Default.Add, contentDescription = "Increase Font Size", modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}

@Composable
private fun CharacterStylesGrid(
    isBold: Boolean, isItalic: Boolean, isUnderline: Boolean, isStrikethrough: Boolean,
    isSuperscript: Boolean, isSubscript: Boolean, isFormatPainterActive: Boolean,
    onToggleBold: () -> Unit, onToggleItalic: () -> Unit, onToggleUnderline: () -> Unit,
    onToggleStrikethrough: () -> Unit, onToggleSuperscript: () -> Unit, onToggleSubscript: () -> Unit,
    onToggleFormatPainter: () -> Unit, onClearFormatting: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        StyleToggleButton(icon = Icons.Default.FormatBold, label = "Bold", isActive = isBold, onClick = onToggleBold)
        StyleToggleButton(icon = Icons.Default.FormatItalic, label = "Italic", isActive = isItalic, onClick = onToggleItalic)
        StyleToggleButton(icon = Icons.Default.FormatUnderlined, label = "Underline", isActive = isUnderline, onClick = onToggleUnderline)
        StyleToggleButton(icon = Icons.Default.FormatStrikethrough, label = "Strikethrough", isActive = isStrikethrough, onClick = onToggleStrikethrough)
        StyleToggleButton(icon = Icons.Default.Superscript, label = "Superscript", isActive = isSuperscript, onClick = onToggleSuperscript)
        StyleToggleButton(icon = Icons.Default.Subscript, label = "Subscript", isActive = isSubscript, onClick = onToggleSubscript)
        StyleToggleButton(icon = Icons.Default.FormatPaint, label = "Painter", isActive = isFormatPainterActive, onClick = onToggleFormatPainter)
        StyleToggleButton(icon = Icons.Default.FormatClear, label = "Clear", isActive = false, onClick = onClearFormatting)
    }
}

@Composable
private fun StyleToggleButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    isActive: Boolean,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = if (isActive) WordPrimary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
        border = BorderStroke(1.dp, if (isActive) WordPrimary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
        modifier = Modifier.size(40.dp).clip(RoundedCornerShape(8.dp)).clickable { onClick() }
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
