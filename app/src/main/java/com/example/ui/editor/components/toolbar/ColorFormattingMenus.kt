package com.example.ui.editor.components.toolbar

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FormatColorFill
import androidx.compose.material.icons.filled.FormatColorText
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.WordPrimary

@Composable
fun TextColorSelector(
    onSetTextColor: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var textColorMenuExpanded by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        IconButton(
            onClick = { textColorMenuExpanded = true },
            modifier = Modifier.size(36.dp).testTag("format_text_color")
        ) {
            Icon(Icons.Default.FormatColorText, contentDescription = "Text Color", tint = WordPrimary)
        }

        DropdownMenu(
            expanded = textColorMenuExpanded,
            onDismissRequest = { textColorMenuExpanded = false }
        ) {
            val colors = listOf(
                "#000000" to "Black",
                "#185ABD" to "Word Blue",
                "#C00000" to "Dark Red",
                "#107C41" to "Office Green",
                "#7030A0" to "Purple",
                "#7F7F7F" to "Gray"
            )
            colors.forEach { (hex, name) ->
                DropdownMenuItem(
                    leadingIcon = {
                        Box(
                            modifier = Modifier
                                .size(16.dp)
                                .background(Color(android.graphics.Color.parseColor(hex)), CircleShape)
                        )
                    },
                    text = { Text(name, fontSize = 13.sp) },
                    onClick = {
                        textColorMenuExpanded = false
                        onSetTextColor(hex)
                    }
                )
            }
        }
    }
}

@Composable
fun HighlightColorSelector(
    onSetHighlightColor: (String?) -> Unit,
    modifier: Modifier = Modifier
) {
    var highlightMenuExpanded by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        IconButton(
            onClick = { highlightMenuExpanded = true },
            modifier = Modifier.size(36.dp).testTag("format_highlight_color")
        ) {
            Icon(Icons.Default.FormatColorFill, contentDescription = "Highlight Color", tint = Color(0xFFD83B01))
        }

        DropdownMenu(
            expanded = highlightMenuExpanded,
            onDismissRequest = { highlightMenuExpanded = false }
        ) {
            DropdownMenuItem(
                text = { Text("No Color", fontSize = 13.sp) },
                onClick = {
                    highlightMenuExpanded = false
                    onSetHighlightColor(null)
                }
            )
            val highlights = listOf(
                "#FFF275" to "Yellow Highlight",
                "#A8F387" to "Bright Green",
                "#94ECF8" to "Cyan Highlight",
                "#FFB7DF" to "Pink Highlight"
            )
            highlights.forEach { (hex, name) ->
                DropdownMenuItem(
                    leadingIcon = {
                        Box(
                            modifier = Modifier
                                .size(16.dp)
                                .background(Color(android.graphics.Color.parseColor(hex)), RoundedCornerShape(2.dp))
                        )
                    },
                    text = { Text(name, fontSize = 13.sp) },
                    onClick = {
                        highlightMenuExpanded = false
                        onSetHighlightColor(hex)
                    }
                )
            }
        }
    }
}
