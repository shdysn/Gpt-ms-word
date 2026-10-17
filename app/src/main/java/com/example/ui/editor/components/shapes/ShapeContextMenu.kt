package com.example.ui.editor.components.shapes

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ShapeBlock

/**
 * Floating Context Action Bar displayed right above the selected shape in MS Word style:
 * [ Cut | Copy | ▼ | Delete | Add Text | New Comment ]
 */
@Composable
fun ShapeContextMenu(
    shape: ShapeBlock,
    onCut: () -> Unit,
    onCopy: () -> Unit,
    onDuplicate: () -> Unit,
    onDelete: () -> Unit,
    onUpdateText: (title: String, content: String) -> Unit,
    onAddComment: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showMoreDropdown by remember { mutableStateOf(false) }
    var showTextDialog by remember { mutableStateOf(false) }

    Surface(
        shape = RoundedCornerShape(8.dp),
        color = Color.White,
        shadowElevation = 4.dp,
        modifier = modifier
            .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(8.dp))
            .testTag("shape_context_menu")
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
        ) {
            ShapeContextItem(label = "Cut", onClick = onCut)
            ContextDivider()
            ShapeContextItem(label = "Copy", onClick = onCopy)
            ContextDivider()

            // Dropdown trigger ▼
            Box {
                Box(
                    modifier = Modifier
                        .clickable { showMoreDropdown = true }
                        .padding(horizontal = 6.dp, vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = "More Actions",
                        tint = Color(0xFF334155),
                        modifier = Modifier.size(18.dp)
                    )
                }
                DropdownMenu(
                    expanded = showMoreDropdown,
                    onDismissRequest = { showMoreDropdown = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Duplicate Shape", fontSize = 13.sp) },
                        onClick = {
                            showMoreDropdown = false
                            onDuplicate()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Paste Here", fontSize = 13.sp) },
                        onClick = { showMoreDropdown = false }
                    )
                }
            }
            ContextDivider()

            ShapeContextItem(label = "Delete", onClick = onDelete, textColor = Color(0xFFDC2626))
            ContextDivider()

            val textActionLabel = if (shape.title.isNotBlank() || shape.content.isNotBlank()) "Edit Text" else "Add Text"
            ShapeContextItem(label = textActionLabel, onClick = { showTextDialog = true })
            ContextDivider()

            ShapeContextItem(label = "New Comment", onClick = onAddComment)
        }
    }

    if (showTextDialog) {
        ShapeEditTextDialog(
            initialTitle = shape.title,
            initialContent = shape.content,
            onDismiss = { showTextDialog = false },
            onConfirm = { t, c ->
                onUpdateText(t, c)
                showTextDialog = false
            }
        )
    }
}

@Composable
private fun ShapeContextItem(
    label: String,
    onClick: () -> Unit,
    textColor: Color = Color(0xFF1E293B)
) {
    Box(
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = textColor
        )
    }
}

@Composable
private fun ContextDivider() {
    Box(
        modifier = Modifier
            .size(width = 1.dp, height = 18.dp)
            .background(Color(0xFFE2E8F0))
    )
}

@Composable
private fun ShapeEditTextDialog(
    initialTitle: String,
    initialContent: String,
    onDismiss: () -> Unit,
    onConfirm: (title: String, content: String) -> Unit
) {
    var title by remember { mutableStateOf(initialTitle) }
    var content by remember { mutableStateOf(initialContent) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Shape Text", fontSize = 16.sp, fontWeight = FontWeight.Bold) },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title / Heading") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = content,
                    onValueChange = { content = it },
                    label = { Text("Body Text") },
                    maxLines = 4,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(title.trim(), content.trim()) }) {
                Text("Apply")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
