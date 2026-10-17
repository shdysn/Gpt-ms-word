package com.example.ui.editor.components.bottomsheet

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.FormatColorReset
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.WordPrimary

/**
 * 10x5 Theme Colors Grid and 10 Standard Colors matching MS Word Mobile (Screenshot 9).
 */
@Composable
fun FontColorSubmenuView(
    selectedColorHex: String?,
    onSelectColor: (String?) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Automatic (Default Black) Row
        Surface(
            shape = RoundedCornerShape(8.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .clickable { onSelectColor("#000000") }
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color.Black)
                )
                Text(
                    text = "Automatic",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        // 10 Columns x 5 Rows Theme Colors
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // There are 5 rows
            for (rowIndex in 0 until 5) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    for (colIndex in 0 until 10) {
                        val hex = WordColorPalette.THEME_COLORS_GRID[colIndex][rowIndex]
                        ColorSwatch(
                            hex = hex,
                            isSelected = selectedColorHex?.equals(hex, ignoreCase = true) == true,
                            onClick = { onSelectColor(hex) }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Standard Colors Header Strip
        Surface(
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "Standard Colors",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
            )
        }

        // Row of 10 Standard Colors
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            WordColorPalette.STANDARD_COLORS.forEach { hex ->
                ColorSwatch(
                    hex = hex,
                    isSelected = selectedColorHex?.equals(hex, ignoreCase = true) == true,
                    onClick = { onSelectColor(hex) }
                )
            }
        }
    }
}

/**
 * Highlight Color Submenu Grid with "No Color" matching MS Word Mobile.
 */
@Composable
fun HighlightColorSubmenuView(
    selectedColorHex: String?,
    onSelectColor: (String?) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // "No Color" Row
        Surface(
            shape = RoundedCornerShape(8.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .clickable { onSelectColor(null) }
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.FormatColorReset,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = "No Fill",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        // Grid of Highlight Colors (2 rows of 7 colors)
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val chunked = WordColorPalette.HIGHLIGHT_COLORS.chunked(7)
            chunked.forEach { rowColors ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    rowColors.forEach { hex ->
                        ColorSwatch(
                            hex = hex,
                            isSelected = selectedColorHex?.equals(hex, ignoreCase = true) == true,
                            onClick = { onSelectColor(hex) },
                            sizeDp = 34
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ColorSwatch(
    hex: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    sizeDp: Int = 26
) {
    val color = try {
        Color(android.graphics.Color.parseColor(hex))
    } catch (_: Exception) {
        Color.DarkGray
    }

    Box(
        modifier = Modifier
            .size(sizeDp.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(color)
            .border(
                width = if (isSelected) 2.dp else 0.5.dp,
                color = if (isSelected) WordPrimary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                shape = RoundedCornerShape(4.dp)
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        if (isSelected) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                tint = if (color.red * 0.299 + color.green * 0.587 + color.blue * 0.114 > 0.5) Color.Black else Color.White,
                modifier = Modifier.size(14.dp)
            )
        }
    }
}
