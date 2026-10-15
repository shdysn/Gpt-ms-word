package com.example.ui.editor.components.toolbar

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.FormatColorFill
import androidx.compose.material.icons.filled.FormatColorText
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
import com.example.editor.engine.CaseType
import com.example.ui.theme.WordPrimary

@Composable
fun FontFamilySelector(
    currentFontDisplay: String,
    onSetFontFamily: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var fontMenuExpanded by remember { mutableStateOf(false) }
    val fontOptions = DocumentFontManager.AVAILABLE_FONTS

    Box(modifier = modifier) {
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
}

@Composable
fun FontSizeSelector(
    currentSize: Float,
    onSetFontSize: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    var sizeMenuExpanded by remember { mutableStateOf(false) }
    val fontSizes = listOf(10f, 11f, 12f, 14f, 16f, 18f, 20f, 24f, 28f, 36f)

    Box(modifier = modifier) {
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
}

@Composable
fun CaseSelector(
    onChangeCase: (CaseType) -> Unit,
    modifier: Modifier = Modifier
) {
    var caseMenuExpanded by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
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
}
