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

/**
 * In-document Search & Replace bar allowing quick text search,
 * match counting, and batch replacement.
 */
@Composable
fun FindReplaceBar(
    isVisible: Boolean,
    findQuery: String,
    replaceQuery: String,
    matchCount: Int,
    onFindQueryChanged: (String) -> Unit,
    onReplaceQueryChanged: (String) -> Unit,
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
            shadowElevation = 3.dp,
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
                        textStyle = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp),
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("find_input_field")
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    if (findQuery.isNotBlank()) {
                        Surface(
                            color = if (matchCount > 0) WordPrimary.copy(alpha = 0.12f) else MaterialTheme.colorScheme.errorContainer,
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = if (matchCount > 0) "$matchCount found" else "0 found",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (matchCount > 0) WordPrimary else MaterialTheme.colorScheme.error,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    IconButton(
                        onClick = onClose,
                        modifier = Modifier.testTag("close_find_replace")
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
                        textStyle = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp),
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("replace_input_field")
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = onReplaceAll,
                        enabled = findQuery.isNotBlank() && matchCount > 0,
                        colors = ButtonDefaults.buttonColors(containerColor = WordPrimary),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .height(48.dp)
                            .testTag("replace_all_button")
                    ) {
                        Text("Replace All", fontSize = 12.sp)
                    }
                }
            }
        }
    }
}
