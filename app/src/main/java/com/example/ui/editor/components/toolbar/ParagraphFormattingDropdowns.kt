package com.example.ui.editor.components.toolbar

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.FormatLineSpacing
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
import com.example.ui.theme.WordPrimary
import kotlin.math.abs

@Composable
fun HeadingStyleDropdown(
    currentHeadingStyle: HeadingStyle,
    customStyles: List<CustomStyle>,
    onSetHeadingStyle: (HeadingStyle) -> Unit,
    onApplyCustomStyle: (CustomStyle) -> Unit,
    onCreateCustomStyle: () -> Unit,
    modifier: Modifier = Modifier
) {
    var styleMenuExpanded by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
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
}

@Composable
fun LineSpacingDropdown(
    currentSpacing: Float,
    onSetLineSpacing: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    var lineSpacingMenuExpanded by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        IconButton(
            onClick = { lineSpacingMenuExpanded = true },
            modifier = Modifier.size(36.dp).testTag("format_line_spacing")
        ) {
            Icon(
                imageVector = Icons.Default.FormatLineSpacing,
                contentDescription = "Line Spacing",
                tint = if (currentSpacing != 1.25f) WordPrimary else MaterialTheme.colorScheme.onSurfaceVariant
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
}
