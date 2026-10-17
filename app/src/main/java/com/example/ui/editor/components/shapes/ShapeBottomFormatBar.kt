package com.example.ui.editor.components.shapes

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FormatColorFill
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.WrapText
import androidx.compose.material.icons.outlined.BorderColor
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.data.model.ShapeBlock

/**
 * Floating MS Word Mobile Shape Formatting Bar displayed at the bottom of the screen
 * when a shape is selected: [ Styles | Fill | Outline | Wrap Text | Arrange | ··· ]
 */
@Composable
fun ShapeBottomFormatBar(
    shape: ShapeBlock,
    onUpdateColors: (bgHex: String, borderHex: String, strokeWidthPt: Float) -> Unit,
    onRotate90: () -> Unit,
    onOpenFullProperties: () -> Unit,
    onStatusMessage: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var activeSubmenu by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // --- Optional Expanded Submenu (Color Picker / Weight / Wrap) ---
        if (activeSubmenu != null) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color.White,
                shadowElevation = 6.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 6.dp)
                    .border(1.dp, Color(0xFFCBD5E1), RoundedCornerShape(12.dp))
            ) {
                when (activeSubmenu) {
                    "FILL" -> FillColorSubmenu(shape.backgroundColorHex) { hex ->
                        onUpdateColors(hex, shape.borderColorHex, shape.strokeWidthPt)
                        activeSubmenu = null
                    }
                    "OUTLINE" -> OutlineSubmenu(shape.borderColorHex, shape.strokeWidthPt) { col, w ->
                        onUpdateColors(shape.backgroundColorHex, col, w)
                        activeSubmenu = null
                    }
                    "STYLES" -> ShapeStylesSubmenu { bg, bdr ->
                        onUpdateColors(bg, bdr, 1.5f)
                        activeSubmenu = null
                    }
                    "WRAP" -> WrapTextSubmenu { wrapMode ->
                        onStatusMessage("Text Wrap set to $wrapMode")
                        activeSubmenu = null
                    }
                    "ARRANGE" -> ArrangeSubmenu(onRotate90 = onRotate90) { action ->
                        onStatusMessage("Shape: $action")
                        activeSubmenu = null
                    }
                }
            }
        }

        // --- Main Floating Rounded Pill Bar ---
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = Color.White,
            shadowElevation = 6.dp,
            modifier = Modifier
                .border(1.dp, Color(0xFFCBD5E1), RoundedCornerShape(28.dp))
                .testTag("shape_bottom_format_bar")
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                BottomBarIcon(
                    icon = Icons.Default.Palette,
                    label = "Styles",
                    isActive = activeSubmenu == "STYLES",
                    onClick = { activeSubmenu = if (activeSubmenu == "STYLES") null else "STYLES" }
                )
                BottomBarIcon(
                    icon = Icons.Default.FormatColorFill,
                    label = "Fill",
                    isActive = activeSubmenu == "FILL",
                    onClick = { activeSubmenu = if (activeSubmenu == "FILL") null else "FILL" }
                )
                BottomBarIcon(
                    icon = Icons.Outlined.BorderColor,
                    label = "Outline",
                    isActive = activeSubmenu == "OUTLINE",
                    onClick = { activeSubmenu = if (activeSubmenu == "OUTLINE") null else "OUTLINE" }
                )
                BottomBarIcon(
                    icon = Icons.Default.WrapText,
                    label = "Wrap",
                    isActive = activeSubmenu == "WRAP",
                    onClick = { activeSubmenu = if (activeSubmenu == "WRAP") null else "WRAP" }
                )
                BottomBarIcon(
                    icon = Icons.Default.Layers,
                    label = "Arrange",
                    isActive = activeSubmenu == "ARRANGE",
                    onClick = { activeSubmenu = if (activeSubmenu == "ARRANGE") null else "ARRANGE" }
                )
                BottomBarIcon(
                    icon = Icons.Default.MoreHoriz,
                    label = "More",
                    isActive = false,
                    onClick = onOpenFullProperties
                )
            }
        }
    }
}

@Composable
private fun BottomBarIcon(
    icon: ImageVector,
    label: String,
    isActive: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(42.dp)
            .clip(CircleShape)
            .background(if (isActive) Color(0xFFE2E8F0) else Color.Transparent)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (isActive) Color(0xFF1E40AF) else Color(0xFF334155),
            modifier = Modifier.size(20.dp)
        )
    }
}
