package com.example.ui.editor.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.data.model.CustomStyle
import com.example.data.model.HeadingStyle
import com.example.data.model.ListType
import com.example.data.model.ParagraphBlock
import com.example.data.model.TextAlignment
import com.example.editor.engine.ActiveFormatting
import com.example.editor.engine.CaseType
import com.example.ui.editor.components.toolbar.CharacterFormattingBar
import com.example.ui.editor.components.toolbar.ParagraphFormattingBar

// Re-export toolbar components for backwards compatibility
@Composable
fun FormatToggleButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    isActive: Boolean,
    onClick: () -> Unit,
    tag: String
) = com.example.ui.editor.components.toolbar.FormatToggleButton(icon, label, isActive, onClick, tag)

@Composable
fun FormatTextToggleButton(
    text: String,
    label: String,
    isActive: Boolean,
    onClick: () -> Unit,
    tag: String
) = com.example.ui.editor.components.toolbar.FormatTextToggleButton(text, label, isActive, onClick, tag)

@Composable
fun ToolbarDivider() = com.example.ui.editor.components.toolbar.ToolbarDivider()

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
    onSetLineSpacing: (Float) -> Unit = {},
    isFormatPainterActive: Boolean = false,
    onToggleFormatPainter: () -> Unit = {},
    onClearFormatting: () -> Unit = {},
    onChangeCase: (CaseType) -> Unit = {},
    onOpenTypographyEffectsDialog: () -> Unit = {},
    onOpenParagraphSpacingDialog: () -> Unit = {},
    onToggleChecklist: () -> Unit = {},
    customStyles: List<CustomStyle> = emptyList(),
    onApplyCustomStyle: (CustomStyle) -> Unit = {},
    onCreateCustomStyle: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Row 1: Font & Character Styling Bar
            CharacterFormattingBar(
                activeParagraph = activeParagraph,
                activeFormatting = activeFormatting,
                onToggleBold = onToggleBold,
                onToggleItalic = onToggleItalic,
                onToggleUnderline = onToggleUnderline,
                onToggleStrikethrough = onToggleStrikethrough,
                onToggleSuperscript = onToggleSuperscript,
                onToggleSubscript = onToggleSubscript,
                onSetFontFamily = onSetFontFamily,
                onSetFontSize = onSetFontSize,
                onSetTextColor = onSetTextColor,
                onSetHighlightColor = onSetHighlightColor,
                isFormatPainterActive = isFormatPainterActive,
                onToggleFormatPainter = onToggleFormatPainter,
                onClearFormatting = onClearFormatting,
                onChangeCase = onChangeCase,
                onOpenTypographyEffectsDialog = onOpenTypographyEffectsDialog
            )

            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                thickness = 0.5.dp
            )

            // Row 2: Paragraph, Alignment, Lists & Utilities Bar
            ParagraphFormattingBar(
                activeParagraph = activeParagraph,
                activeFormatting = activeFormatting,
                onSetHeadingStyle = onSetHeadingStyle,
                onSetAlignment = onSetAlignment,
                onToggleListType = onToggleListType,
                onIncreaseIndent = onIncreaseIndent,
                onDecreaseIndent = onDecreaseIndent,
                onOpenLinkDialog = onOpenLinkDialog,
                onToggleRtl = onToggleRtl,
                onSetLineSpacing = onSetLineSpacing,
                onOpenParagraphSpacingDialog = onOpenParagraphSpacingDialog,
                onToggleChecklist = onToggleChecklist,
                customStyles = customStyles,
                onApplyCustomStyle = onApplyCustomStyle,
                onCreateCustomStyle = onCreateCustomStyle
            )
        }
    }
}
