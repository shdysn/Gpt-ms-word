package com.example.ui.editor.components.dialogs

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Draw
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.typography.SignatureUtils
import com.example.ui.theme.WordPrimary

/**
 * Dialog for capturing and inserting handwritten digital signatures.
 */
@Composable
fun DigitalSignatureDialog(
    onDismiss: () -> Unit,
    onConfirm: (signerName: String, signerTitle: String, strokeColorHex: String, pointsSummary: String) -> Unit
) {
    var signerName by remember { mutableStateOf("") }
    var signerTitle by remember { mutableStateOf("Authorized Signatory") }
    var selectedInkHex by remember { mutableStateOf("#0F172A") }

    val strokes = remember { mutableStateListOf<List<Offset>>() }
    var currentStroke by remember { mutableStateOf<List<Offset>>(emptyList()) }

    var canvasWidth by remember { mutableStateOf(300f) }
    var canvasHeight by remember { mutableStateOf(160f) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Draw, contentDescription = null, tint = WordPrimary)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Insert Digital Signature", style = MaterialTheme.typography.titleLarge)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Sign using your finger or stylus below:",
                    style = MaterialTheme.typography.bodyMedium
                )

                // Drawing Pad Canvas
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFF8FAFC))
                        .border(1.5.dp, Color(0xFFCBD5E1), RoundedCornerShape(8.dp))
                        .pointerInput(Unit) {
                            detectDragGestures(
                                onDragStart = { offset ->
                                    currentStroke = listOf(offset)
                                },
                                onDrag = { change, _ ->
                                    change.consume()
                                    currentStroke = currentStroke + change.position
                                },
                                onDragEnd = {
                                    if (currentStroke.isNotEmpty()) {
                                        strokes.add(currentStroke)
                                        currentStroke = emptyList()
                                    }
                                }
                            )
                        }
                        .testTag("signature_canvas")
                ) {
                    Canvas(modifier = Modifier.fillMaxWidth().height(160.dp)) {
                        canvasWidth = size.width
                        canvasHeight = size.height

                        val inkColor = try {
                            Color(android.graphics.Color.parseColor(selectedInkHex))
                        } catch (_: Exception) { Color(0xFF0F172A) }

                        // Draw baseline
                        drawLine(
                            color = Color(0xFFE2E8F0),
                            start = Offset(20f, size.height - 30f),
                            end = Offset(size.width - 20f, size.height - 30f),
                            strokeWidth = 2f
                        )

                        // Draw past strokes
                        for (stroke in strokes) {
                            if (stroke.size > 1) {
                                val path = Path().apply {
                                    moveTo(stroke.first().x, stroke.first().y)
                                    for (i in 1 until stroke.size) {
                                        lineTo(stroke[i].x, stroke[i].y)
                                    }
                                }
                                drawPath(
                                    path = path,
                                    color = inkColor,
                                    style = Stroke(width = 4f, cap = StrokeCap.Round, join = StrokeJoin.Round)
                                )
                            }
                        }

                        // Draw current active stroke
                        if (currentStroke.size > 1) {
                            val path = Path().apply {
                                moveTo(currentStroke.first().x, currentStroke.first().y)
                                for (i in 1 until currentStroke.size) {
                                    lineTo(currentStroke[i].x, currentStroke[i].y)
                                }
                            }
                            drawPath(
                                path = path,
                                color = inkColor,
                                style = Stroke(width = 4f, cap = StrokeCap.Round, join = StrokeJoin.Round)
                            )
                        }
                    }

                    // Clear button overlay
                    IconButton(
                        onClick = {
                            strokes.clear()
                            currentStroke = emptyList()
                        },
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(4.dp)
                            .testTag("clear_signature_button")
                    ) {
                        Icon(Icons.Default.Clear, contentDescription = "Clear Pad", tint = Color.Gray)
                    }
                }

                // Ink selection
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("Ink Color:", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                    val inks = listOf("#0F172A" to "Black", "#1E3A8A" to "Navy Blue", "#047857" to "Green")
                    inks.forEach { (hex, label) ->
                        FilterChip(
                            selected = selectedInkHex == hex,
                            onClick = { selectedInkHex = hex },
                            label = { Text(label, fontSize = 11.sp) }
                        )
                    }
                }

                OutlinedTextField(
                    value = signerName,
                    onValueChange = { signerName = it },
                    label = { Text("Signer Name") },
                    placeholder = { Text("e.g. John Doe / ایڈووکیٹ علی") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("signer_name_input")
                )

                OutlinedTextField(
                    value = signerTitle,
                    onValueChange = { signerTitle = it },
                    label = { Text("Designation / Title") },
                    placeholder = { Text("e.g. Managing Director, Author") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("signer_title_input")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val strokesData = strokes.map { stroke ->
                        stroke.map { Pair(it.x, it.y) }
                    }
                    val encodedPoints = SignatureUtils.encodeNormalizedStrokes(
                        strokes = strokesData,
                        canvasWidth = if (canvasWidth > 0f) canvasWidth else 300f,
                        canvasHeight = if (canvasHeight > 0f) canvasHeight else 160f
                    )
                    onConfirm(
                        signerName.trim(),
                        signerTitle.trim().ifBlank { "Authorized Signatory" },
                        selectedInkHex,
                        encodedPoints
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = WordPrimary),
                modifier = Modifier.testTag("insert_signature_btn")
            ) {
                Text("Insert Signature")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
