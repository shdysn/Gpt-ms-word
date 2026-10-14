package com.example.ui.editor.components.dialogs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.WordPrimary
import java.util.Locale

/**
 * Dialog for setting and removing document watermark.
 */
@Composable
fun WatermarkDialog(
    initialText: String,
    onDismiss: () -> Unit,
    onApply: (watermarkText: String, colorHex: String, angleDeg: Float) -> Unit,
    onRemove: () -> Unit
) {
    var watermarkText by remember { mutableStateOf(initialText) }
    var selectedAngle by remember { mutableStateOf(-45f) }

    val presets = listOf("CONFIDENTIAL", "DRAFT", "DO NOT COPY", "URGENT", "SAMPLE")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.WaterDrop, contentDescription = null, tint = WordPrimary)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Document Watermark")
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Watermark Presets:", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    presets.take(3).forEach { p ->
                        FilterChip(
                            selected = watermarkText == p,
                            onClick = { watermarkText = p },
                            label = { Text(p, fontSize = 10.sp) }
                        )
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    presets.drop(3).forEach { p ->
                        FilterChip(
                            selected = watermarkText == p,
                            onClick = { watermarkText = p },
                            label = { Text(p, fontSize = 10.sp) }
                        )
                    }
                }

                OutlinedTextField(
                    value = watermarkText,
                    onValueChange = { watermarkText = it },
                    label = { Text("Watermark Text") },
                    placeholder = { Text("e.g. CONFIDENTIAL / خفیہ") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("Orientation:", fontSize = 12.sp)
                    FilterChip(
                        selected = selectedAngle == -45f,
                        onClick = { selectedAngle = -45f },
                        label = { Text("Diagonal (-45°)") }
                    )
                    FilterChip(
                        selected = selectedAngle == 0f,
                        onClick = { selectedAngle = 0f },
                        label = { Text("Horizontal (0°)") }
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onApply(watermarkText.trim().uppercase(Locale.ROOT), "#2594A3B8", selectedAngle)
                },
                colors = ButtonDefaults.buttonColors(containerColor = WordPrimary)
            ) {
                Text("Apply")
            }
        },
        dismissButton = {
            Row {
                if (initialText.isNotBlank()) {
                    TextButton(onClick = onRemove) {
                        Text("Remove", color = MaterialTheme.colorScheme.error)
                    }
                }
                TextButton(onClick = onDismiss) { Text("Cancel") }
            }
        }
    )
}
