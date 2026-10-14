package com.example.ui.editor.components.toolbar

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FormatAlignCenter
import androidx.compose.material.icons.filled.FormatAlignJustify
import androidx.compose.material.icons.filled.FormatListNumbered
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
    val isRtl = activeParagraph?.isRtl == true
    val currentHeadingStyle = activeParagraph?.headingStyle ?: HeadingStyle.NORMAL
    val currentAlignment = activeParagraph?.alignment ?: TextAlignment.START
    val currentListType = activeParagraph?.listType ?: ListType.NONE
    val currentSpacing = activeParagraph?.lineSpacingMultiplier ?: 1.25f

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        // Paragraph / Heading Style Selector
        HeadingStyleDropdown(
            currentHeadingStyle = currentHeadingStyle,
            customStyles = customStyles,
            onSetHeadingStyle = onSetHeadingStyle,
            onApplyCustomStyle = onApplyCustomStyle,
            onCreateCustomStyle = onCreateCustomStyle
        )

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

        LineSpacingDropdown(
            currentSpacing = currentSpacing,
            onSetLineSpacing = onSetLineSpacing
        )

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
