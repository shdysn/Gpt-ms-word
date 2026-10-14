package com.example.ui.editor.components.dialogs

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.example.data.model.TextAlignment
import com.example.ui.theme.WordPrimary

/**
 * Dialog for inserting or updating a web hyperlink on the selected text or document.
 */
@Composable
fun InsertHyperlinkDialog(
    initialUrl: String = "https://",
    onDismiss: () -> Unit,
    onConfirm: (url: String) -> Unit,
    onRemoveLink: () -> Unit
) {
    var urlText by remember { mutableStateOf(if (initialUrl.isBlank()) "https://" else initialUrl) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Insert Hyperlink") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Enter web URL for the selected text:",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                OutlinedTextField(
                    value = urlText,
                    onValueChange = { urlText = it },
                    label = { Text("Web URL") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("hyperlink_url_input")
                )
            }
        },
        confirmButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (initialUrl.isNotBlank() && initialUrl != "https://") {
                    TextButton(
                        onClick = {
                            onRemoveLink()
                            onDismiss()
                        }
                    ) {
                        Text("Remove Link", color = MaterialTheme.colorScheme.error)
                    }
                }
                Button(
                    onClick = {
                        val trimmed = urlText.trim()
                        if (trimmed.isNotBlank()) {
                            val finalUrl = if (!trimmed.startsWith("http://", ignoreCase = true) &&
                                !trimmed.startsWith("https://", ignoreCase = true) &&
                                !trimmed.startsWith("mailto:", ignoreCase = true)
                            ) {
                                "https://$trimmed"
                            } else {
                                trimmed
                            }
                            onConfirm(finalUrl)
                        }
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = WordPrimary),
                    modifier = Modifier.testTag("confirm_hyperlink_button")
                ) {
                    Text("Apply")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

/**
 * Dialog for inserting header and footer content.
 */
@Composable
fun HeaderFooterEditDialog(
    currentHeader: String,
    currentFooter: String,
    showHeader: Boolean,
    showFooter: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (headerText: String, footerText: String, showHeader: Boolean, showFooter: Boolean) -> Unit
) {
    var header by remember { mutableStateOf(currentHeader) }
    var footer by remember { mutableStateOf(currentFooter) }
    var isHeaderVisible by remember { mutableStateOf(showHeader) }
    var isFooterVisible by remember { mutableStateOf(showFooter) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Header & Footer Content") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Enable Header", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                    Switch(checked = isHeaderVisible, onCheckedChange = { isHeaderVisible = it })
                }
                if (isHeaderVisible) {
                    OutlinedTextField(
                        value = header,
                        onValueChange = { header = it },
                        label = { Text("Header Text (e.g. Document Title)") },
                        modifier = Modifier.fillMaxWidth().testTag("header_text_input")
                    )
                }

                HorizontalDivider()

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Enable Footer", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                    Switch(checked = isFooterVisible, onCheckedChange = { isFooterVisible = it })
                }
                if (isFooterVisible) {
                    OutlinedTextField(
                        value = footer,
                        onValueChange = { footer = it },
                        label = { Text("Footer Text (use {page} & {pages})") },
                        modifier = Modifier.fillMaxWidth().testTag("footer_text_input")
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm(header, footer, isHeaderVisible, isFooterVisible)
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = WordPrimary),
                modifier = Modifier.testTag("apply_header_footer")
            ) {
                Text("Apply")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

/**
 * Dialog for choosing page number position and template.
 */
@Composable
fun PageNumberInsertDialog(
    onDismiss: () -> Unit,
    onSelectPreset: (pattern: String, alignment: TextAlignment) -> Unit
) {
    val presets = listOf(
        Triple("Page {page} of {pages}", TextAlignment.CENTER, "Page 1 of 5 (Bottom Center)"),
        Triple("Page {page}", TextAlignment.END, "Page 1 (Bottom Right)"),
        Triple("{page}", TextAlignment.CENTER, "1 (Bottom Center)"),
        Triple("Page {page}", TextAlignment.START, "Page 1 (Bottom Left)")
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Insert Page Numbers") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Select page number style and position:")
                presets.forEach { (pattern, alignment, label) ->
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onSelectPreset(pattern, alignment)
                                onDismiss()
                            }
                    ) {
                        Text(
                            text = label,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
