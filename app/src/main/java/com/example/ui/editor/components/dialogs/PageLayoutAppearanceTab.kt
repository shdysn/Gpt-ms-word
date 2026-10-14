package com.example.ui.editor.components.dialogs

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.layout.PageBackgroundPreset
import com.example.core.layout.PageBorderStyle
import com.example.ui.theme.WordPrimary

@Composable
fun PageLayoutAppearanceTab(
    pageBackgroundHex: String,
    onBackgroundHexChange: (String) -> Unit,
    borderStyle: PageBorderStyle,
    onBorderStyleChange: (PageBorderStyle) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        // Page Background Color Palette
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("Page Background Color", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                PageBackgroundPreset.entries.forEach { bg ->
                    val isSelected = pageBackgroundHex.equals(bg.colorHex, ignoreCase = true)
                    val color = try { Color(android.graphics.Color.parseColor(bg.colorHex)) } catch (e: Exception) { Color.White }
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.clickable { onBackgroundHexChange(bg.colorHex) }
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = color,
                            shadowElevation = 2.dp,
                            border = androidx.compose.foundation.BorderStroke(
                                width = if (isSelected) 2.dp else 1.dp,
                                color = if (isSelected) WordPrimary else Color.LightGray
                            ),
                            modifier = Modifier.size(38.dp)
                        ) {
                            if (isSelected) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Selected",
                                        tint = if (bg == PageBackgroundPreset.DARK) Color.White else WordPrimary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = bg.displayName,
                            fontSize = 10.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) WordPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // Page Border Style
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("Page Border", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                PageBorderStyle.entries.forEach { bs ->
                    FilterChip(
                        selected = borderStyle == bs,
                        onClick = { onBorderStyleChange(bs) },
                        label = {
                            Text(
                                text = bs.displayName,
                                fontSize = 11.sp,
                                fontWeight = if (borderStyle == bs) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    )
                }
            }
        }
    }
}
