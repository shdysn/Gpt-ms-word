package com.example.ui.editor.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FindReplace
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.WordPrimary

import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.IconButtonDefaults

/**
 * In-document Search & Replace bar allowing quick text search,
 * case-sensitive toggle, match navigation, single replace, and batch replacement.
 */
@Composable
fun FindReplaceBar(
    isVisible: Boolean,
    findQuery: String,
    replaceQuery: String,
    matchCount: Int,
    currentMatchIndex: Int = 0,
    isCaseSensitive: Boolean = false,
    onFindQueryChanged: (String) -> Unit,
    onReplaceQueryChanged: (String) -> Unit,
    onToggleCaseSensitive: () -> Unit = {},
    onFindNext: () -> Unit = {},
    onFindPrevious: () -> Unit = {},
    onReplaceNext: () -> Unit = {},
    onReplaceAll: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        Surface(
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 4.dp,
            shape = RoundedCornerShape(bottomStart = 12.dp, bottomEnd = 12.dp),
            modifier = modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 8.dp)
            ) {
                // Find Row
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = findQuery,
                        onValueChange = onFindQueryChanged,
                        placeholder = { Text("Find in document...", fontSize = 13.sp) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp)) },
                        trailingIcon = {
                            // Match Case Toggle Button (Aa)
                            IconButton(
                                onClick = onToggleCaseSensitive,
                                modifier = Modifier
                                    .size(32.dp)
                                    .testTag("toggle_case_sensitive"),
                                colors = if (isCaseSensitive) IconButtonDefaults.iconButtonColors(containerColor = WordPrimary.copy(alpha = 0.15f)) else IconButtonDefaults.iconButtonColors()
                            ) {
                                Text(
                                    text = "Aa",
                                    fontSize = 12.sp,
                                    fontWeight = if (isCaseSensitive) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isCaseSensitive) WordPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        },
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("find_input_field")
                    )

                    Spacer(modifier = Modifier.width(6.dp))

                    if (findQuery.isNotBlank()) {
                        Surface(
                            color = if (matchCount > 0) WordPrimary.copy(alpha = 0.12f) else Color(0xFFFFEBEE),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = if (matchCount > 0) "$currentMatchIndex of $matchCount" else "0 found",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (matchCount > 0) WordPrimary else Color.Red,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(2.dp))

                        // Previous Match Button
                        IconButton(
                            onClick = onFindPrevious,
                            enabled = matchCount > 0,
                            modifier = Modifier.size(32.dp).testTag("find_previous_match")
                        ) {
                            Icon(Icons.Default.KeyboardArrowUp, contentDescription = "Previous Match", modifier = Modifier.size(20.dp))
                        }

                        // Next Match Button
                        IconButton(
                            onClick = onFindNext,
                            enabled = matchCount > 0,
                            modifier = Modifier.size(32.dp).testTag("find_next_match")
                        ) {
                            Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Next Match", modifier = Modifier.size(20.dp))
                        }
                    }

                    IconButton(
                        onClick = onClose,
                        modifier = Modifier.size(34.dp).testTag("close_find_replace")
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close Find and Replace")
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Replace Row
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = replaceQuery,
                        onValueChange = onReplaceQueryChanged,
                        placeholder = { Text("Replace with...", fontSize = 13.sp) },
                        leadingIcon = { Icon(Icons.Default.FindReplace, contentDescription = null, modifier = Modifier.size(18.dp)) },
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("replace_input_field")
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    // Replace Single Match Button
                    OutlinedButton(
                        onClick = onReplaceNext,
                        enabled = findQuery.isNotBlank() && matchCount > 0,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .height(44.dp)
                            .testTag("replace_single_button")
                    ) {
                        Text("Replace", fontSize = 12.sp, color = if (findQuery.isNotBlank() && matchCount > 0) WordPrimary else MaterialTheme.colorScheme.outline)
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    // Replace All Matches Button
                    Button(
                        onClick = onReplaceAll,
                        enabled = findQuery.isNotBlank() && matchCount > 0,
                        colors = ButtonDefaults.buttonColors(containerColor = WordPrimary),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .height(44.dp)
                            .testTag("replace_all_button")
                    ) {
                        Text("Replace All", fontSize = 12.sp)
                    }
                }
            }
        }
    }
}
