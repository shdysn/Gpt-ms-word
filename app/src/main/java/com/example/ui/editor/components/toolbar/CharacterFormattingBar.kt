package com.example.ui.editor.components.toolbar

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FormatBold
import androidx.compose.material.icons.filled.FormatItalic
import androidx.compose.material.icons.filled.FormatPaint
import androidx.compose.material.icons.filled.FormatStrikethrough
import androidx.compose.material.icons.filled.FormatUnderlined
import androidx.compose.material.icons.filled.Title
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
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
    val isRtl = activeParagraph?.isRtl == true
    val defaultFontForDisplay = if (isRtl) "Noto Nastaliq Urdu" else "Calibri"
    val currentFontDisplay = activeFormatting.fontFamilyName ?: defaultFontForDisplay
    val currentSize = activeFormatting.fontSizeSp ?: 11f

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
        FontFamilySelector(
            currentFontDisplay = currentFontDisplay,
            onSetFontFamily = onSetFontFamily
        )

        // Font Size Selector
        FontSizeSelector(
            currentSize = currentSize,
            onSetFontSize = onSetFontSize
        )

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

        CaseSelector(onChangeCase = onChangeCase)

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

        TextColorSelector(onSetTextColor = onSetTextColor)
        HighlightColorSelector(onSetHighlightColor = onSetHighlightColor)

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
