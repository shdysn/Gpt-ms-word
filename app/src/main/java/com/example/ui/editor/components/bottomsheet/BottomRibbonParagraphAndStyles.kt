package com.example.ui.editor.components.bottomsheet

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.FormatAlignLeft
import androidx.compose.material.icons.automirrored.filled.FormatIndentDecrease
import androidx.compose.material.icons.automirrored.filled.FormatIndentIncrease
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
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
import androidx.compose.ui.unit.dp
import com.example.data.model.ParagraphBlock

/**
 * Paragraph formatting menu matching MS Word Mobile (Screenshot 5).
 */
@Composable
fun ParagraphSubmenuView(
    activeParagraph: ParagraphBlock?,
    onNavigate: (HomeSubmenu) -> Unit,
    onToggleSpaceBefore: () -> Unit,
    onToggleSpaceAfter: () -> Unit,
    modifier: Modifier = Modifier
) {
    val spaceBefore = activeParagraph?.spaceBeforePt ?: 0f
    val spaceAfter = activeParagraph?.spaceAfterPt ?: 0f
    val lineSpacing = activeParagraph?.lineSpacingMultiplier ?: 1.15f

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        WordRowButton(
            title = "Special Indent",
            customLeadingContent = { WordIncreaseIndentIcon() },
            hasChevron = true,
            onClick = { onNavigate(HomeSubmenu.SPECIAL_INDENT) }
        )

        WordRowButton(
            title = "Line Spacing:",
            value = String.format("%.2f", lineSpacing).removeSuffix("0").removeSuffix("."),
            hasChevron = true,
            onClick = { onNavigate(HomeSubmenu.LINE_SPACING) }
        )

        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))

        WordRowButton(
            title = if (spaceBefore > 0f) "Remove Space Before Paragraph" else "Add Space Before Paragraph",
            icon = Icons.Default.ArrowDownward,
            onClick = onToggleSpaceBefore
        )

        WordRowButton(
            title = if (spaceAfter > 0f) "Remove Space After Paragraph" else "Add Space After Paragraph",
            icon = Icons.Default.ArrowUpward,
            onClick = onToggleSpaceAfter
        )
    }
}

/**
 * Special Indent options matching MS Word Mobile (Screenshot 3).
 */
@Composable
fun SpecialIndentSubmenuView(
    activeParagraph: ParagraphBlock?,
    onSetIndent: (firstLine: Float, hanging: Float) -> Unit,
    modifier: Modifier = Modifier
) {
    val fIndent = activeParagraph?.firstLineIndentPt ?: 0f
    val hIndent = activeParagraph?.hangingIndentPt ?: 0f

    val isNone = fIndent == 0f && hIndent == 0f
    val isFirstLine = fIndent > 0f
    val isHanging = hIndent > 0f

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        WordRowButton(
            title = "None",
            customLeadingContent = { WordAlignLeftIcon() },
            isSelected = isNone,
            onClick = { onSetIndent(0f, 0f) }
        )
        WordRowButton(
            title = "First Line",
            customLeadingContent = { WordIncreaseIndentIcon() },
            isSelected = isFirstLine,
            onClick = { onSetIndent(24f, 0f) }
        )
        WordRowButton(
            title = "Hanging",
            customLeadingContent = { WordDecreaseIndentIcon() },
            isSelected = isHanging,
            onClick = { onSetIndent(0f, 24f) }
        )
    }
}

/**
 * Line spacing selection matching MS Word Mobile (Screenshot 1).
 */
@Composable
fun LineSpacingSubmenuView(
    currentSpacing: Float,
    onSelectSpacing: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    val options = listOf(1.0f, 1.15f, 1.5f, 2.0f, 2.5f, 3.0f)

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        options.forEach { spacing ->
            val isSelected = Math.abs(currentSpacing - spacing) < 0.05f
            WordRowButton(
                title = spacing.toString().removeSuffix(".0"),
                isSelected = isSelected,
                onClick = { onSelectSpacing(spacing) }
            )
        }
    }
}

@Composable
fun WordRowButton(
    title: String,
    icon: ImageVector? = null,
    customLeadingContent: (@Composable () -> Unit)? = null,
    value: String? = null,
    hasChevron: Boolean = false,
    isSelected: Boolean = false,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(4.dp),
        color = if (isSelected) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f) else Color.Transparent,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(4.dp))
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (customLeadingContent != null) {
                Box(modifier = Modifier.size(24.dp), contentAlignment = Alignment.Center) {
                    customLeadingContent()
                }
                Spacer(modifier = Modifier.width(14.dp))
            } else if (icon != null) {
                Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(14.dp))
            }
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )
            if (value != null) {
                Text(
                    text = value,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(6.dp))
            }
            if (hasChevron) {
                Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f), modifier = Modifier.size(18.dp))
            }
        }
    }
}
