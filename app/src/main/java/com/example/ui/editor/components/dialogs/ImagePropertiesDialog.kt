package com.example.ui.editor.components.dialogs

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
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
import com.example.data.model.ImageBlock
import com.example.data.model.TextAlignment
import com.example.ui.theme.WordPrimary

/**
 * Dialog for image sizing, maintaining aspect ratio, alignment, and captions.
 */
@Composable
fun ImagePropertiesDialog(
    imageBlock: ImageBlock,
    onDismiss: () -> Unit,
    onConfirm: (widthDp: Int, heightDp: Int, alignment: TextAlignment, caption: String, aspectRatio: Float, wrapMode: String, filterMode: String, cornerRadius: Int) -> Unit,
    onDeleteImage: () -> Unit
) {
    var widthDp by remember { mutableStateOf(imageBlock.widthDp.toFloat()) }
    var customHeightDp by remember { mutableStateOf(imageBlock.heightDp.toFloat()) }
    var maintainAspectRatio by remember { mutableStateOf(true) }
    var alignment by remember { mutableStateOf(imageBlock.alignment) }
    var captionText by remember { mutableStateOf(imageBlock.caption) }
    var wrapMode by remember { mutableStateOf(imageBlock.wrapMode) }
    var filterMode by remember { mutableStateOf(imageBlock.filterMode) }
    var cornerRadius by remember { mutableStateOf(imageBlock.cornerRadiusDp) }

    val baseRatio = if (imageBlock.aspectRatio > 0) imageBlock.aspectRatio else 1.333f
    val computedHeight = if (maintainAspectRatio) (widthDp / baseRatio).toInt() else customHeightDp.toInt()
    val effectiveRatio = if (maintainAspectRatio) baseRatio else (widthDp / customHeightDp.coerceAtLeast(1f))

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Picture Format & Sizing") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("Text Wrapping:", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf(
                        "IN_LINE" to "In Line",
                        "SQUARE" to "Square",
                        "BEHIND_TEXT" to "Behind",
                        "IN_FRONT" to "In Front"
                    ).forEach { (mode, label) ->
                        FilterChip(
                            selected = wrapMode == mode || (mode == "IN_FRONT" && wrapMode == "IN_FRONT_OF_TEXT"),
                            onClick = { wrapMode = mode },
                            label = { Text(label, fontSize = 11.sp) }
                        )
                    }
                }

                HorizontalDivider()

                Text("Image Width: ${widthDp.toInt()} dp", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                Slider(
                    value = widthDp,
                    onValueChange = {
                        widthDp = it
                        if (maintainAspectRatio) {
                            customHeightDp = (it / baseRatio).coerceIn(60f, 600f)
                        }
                    },
                    valueRange = 80f..460f,
                    steps = 19,
                    modifier = Modifier.testTag("image_width_slider")
                )

                Text("Size Presets:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(120f to "Small", 220f to "Medium", 320f to "Large", 420f to "Full").forEach { (sz, label) ->
                        FilterChip(
                            selected = (widthDp - sz).let { it in -20f..20f },
                            onClick = {
                                widthDp = sz
                                if (maintainAspectRatio) {
                                    customHeightDp = (sz / baseRatio).coerceIn(60f, 600f)
                                }
                            },
                            label = { Text(label, fontSize = 11.sp) }
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable {
                        maintainAspectRatio = !maintainAspectRatio
                        if (maintainAspectRatio) {
                            customHeightDp = (widthDp / baseRatio).coerceIn(60f, 600f)
                        }
                    }
                ) {
                    Checkbox(
                        checked = maintainAspectRatio,
                        onCheckedChange = {
                            maintainAspectRatio = it
                            if (it) {
                                customHeightDp = (widthDp / baseRatio).coerceIn(60f, 600f)
                            }
                        },
                        modifier = Modifier.testTag("aspect_ratio_checkbox")
                    )
                    Text("Lock aspect ratio (Height: $computedHeight dp)", fontSize = 12.sp)
                }

                if (!maintainAspectRatio) {
                    Text("Custom Height: ${customHeightDp.toInt()} dp", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                    Slider(
                        value = customHeightDp,
                        onValueChange = { customHeightDp = it },
                        valueRange = 60f..460f,
                        steps = 19,
                        modifier = Modifier.testTag("image_height_slider")
                    )
                }

                HorizontalDivider()

                Text("Visual Filter:", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("NORMAL" to "Original", "GRAYSCALE" to "Grayscale", "SEPIA" to "Sepia").forEach { (fMode, label) ->
                        FilterChip(
                            selected = filterMode == fMode,
                            onClick = { filterMode = fMode },
                            label = { Text(label, fontSize = 11.sp) }
                        )
                    }
                }

                Text("Corner Radius: $cornerRadius dp", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(0 to "Sharp (0)", 8 to "Rounded (8)", 16 to "Card (16)", 28 to "Soft (28)").forEach { (rad, label) ->
                        FilterChip(
                            selected = cornerRadius == rad,
                            onClick = { cornerRadius = rad },
                            label = { Text(label, fontSize = 11.sp) }
                        )
                    }
                }

                HorizontalDivider()

                Text("Alignment:", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(
                        TextAlignment.START to "Left",
                        TextAlignment.CENTER to "Center",
                        TextAlignment.END to "Right"
                    ).forEach { (align, label) ->
                        FilterChip(
                            selected = alignment == align,
                            onClick = { alignment = align },
                            label = { Text(label, fontSize = 11.sp) }
                        )
                    }
                }

                HorizontalDivider()

                OutlinedTextField(
                    value = captionText,
                    onValueChange = { captionText = it },
                    label = { Text("Caption (Optional)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("image_caption_input")
                )

                Button(
                    onClick = {
                        onDeleteImage()
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.errorContainer, contentColor = MaterialTheme.colorScheme.onErrorContainer),
                    modifier = Modifier.fillMaxWidth().testTag("delete_image_btn")
                ) {
                    Text("Delete Image")
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm(widthDp.toInt(), computedHeight, alignment, captionText, effectiveRatio, wrapMode, filterMode, cornerRadius)
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = WordPrimary),
                modifier = Modifier.testTag("apply_image_properties")
            ) {
                Text("Apply")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
