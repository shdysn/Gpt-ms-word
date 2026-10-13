package com.example.ui.editor.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Draw
import androidx.compose.material.icons.filled.FormatQuote
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.WordPrimary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Interactive touch canvas for signing and drawing digital signatures.
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
                    val encodedPoints = com.example.core.typography.SignatureUtils.encodeNormalizedStrokes(
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

/**
 * Dialog for inserting Callouts / Shape Note blocks into the document.
 */
@Composable
fun ShapeInsertDialog(
    onDismiss: () -> Unit,
    onConfirm: (shapeType: String, title: String, content: String, bgHex: String, borderHex: String) -> Unit
) {
    var selectedType by remember { mutableStateOf("NOTE") }
    var shapeTitle by remember { mutableStateOf("Important Note") }
    var shapeContent by remember { mutableStateOf("") }

    val presetColors = when (selectedType) {
        "WARNING" -> "#FEF3C7" to "#D97706"
        "SUCCESS" -> "#ECFDF5" to "#059669"
        "QUOTE" -> "#F3E8FF" to "#7C3AED"
        else -> "#EFF6FF" to "#3B82F6"
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Insert Callout / Shape Box") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Select Shape Style:", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    FilterChip(
                        selected = selectedType == "NOTE",
                        onClick = {
                            selectedType = "NOTE"
                            if (shapeTitle in listOf("Warning", "Tip", "Quote")) shapeTitle = "Important Note"
                        },
                        label = { Text("Note") }
                    )
                    FilterChip(
                        selected = selectedType == "WARNING",
                        onClick = {
                            selectedType = "WARNING"
                            shapeTitle = "Warning"
                        },
                        label = { Text("Warning") }
                    )
                    FilterChip(
                        selected = selectedType == "SUCCESS",
                        onClick = {
                            selectedType = "SUCCESS"
                            shapeTitle = "Tip"
                        },
                        label = { Text("Tip / Key") }
                    )
                    FilterChip(
                        selected = selectedType == "QUOTE",
                        onClick = {
                            selectedType = "QUOTE"
                            shapeTitle = "Quote"
                        },
                        label = { Text("Quote") }
                    )
                }

                OutlinedTextField(
                    value = shapeTitle,
                    onValueChange = { shapeTitle = it },
                    label = { Text("Title / Header") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = shapeContent,
                    onValueChange = { shapeContent = it },
                    label = { Text("Content Text") },
                    placeholder = { Text("Enter note, citation, or announcement details...") },
                    minLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm(
                        selectedType,
                        shapeTitle.trim().ifBlank { "Note" },
                        shapeContent.trim(),
                        presetColors.first,
                        presetColors.second
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = WordPrimary)
            ) {
                Text("Insert Shape")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

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

/**
 * Dialog for setting or removing document PIN passcode lock.
 */
@Composable
fun DocumentPasswordDialog(
    isCurrentlyLocked: Boolean,
    onDismiss: () -> Unit,
    onSetPassword: (String) -> Unit,
    onRemovePassword: () -> Unit
) {
    var passwordInput by remember { mutableStateOf("") }
    var confirmPasswordInput by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    if (isCurrentlyLocked) Icons.Default.Lock else Icons.Default.LockOpen,
                    contentDescription = null,
                    tint = WordPrimary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(if (isCurrentlyLocked) "Document Security Lock" else "Protect Document with PIN")
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                if (isCurrentlyLocked) {
                    Text(
                        "This document is currently protected with a password PIN. Anyone opening it must enter the PIN.",
                        fontSize = 13.sp
                    )
                    Button(
                        onClick = onRemovePassword,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Remove Password Protection")
                    }
                } else {
                    Text(
                        "Set a 4-8 character PIN or password to restrict access to this document:",
                        fontSize = 13.sp
                    )
                    OutlinedTextField(
                        value = passwordInput,
                        onValueChange = {
                            passwordInput = it
                            errorMessage = null
                        },
                        label = { Text("Enter PIN / Password") },
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = confirmPasswordInput,
                        onValueChange = {
                            confirmPasswordInput = it
                            errorMessage = null
                        },
                        label = { Text("Confirm PIN / Password") },
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    errorMessage?.let {
                        Text(it, color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                    }
                }
            }
        },
        confirmButton = {
            if (!isCurrentlyLocked) {
                Button(
                    onClick = {
                        if (passwordInput.length < 4) {
                            errorMessage = "PIN must be at least 4 characters"
                        } else if (passwordInput != confirmPasswordInput) {
                            errorMessage = "Passwords do not match"
                        } else {
                            onSetPassword(passwordInput)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = WordPrimary)
                ) {
                    Text("Lock Document")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Close") }
        }
    )
}

/**
 * Model representing a saved revision snapshot.
 */
typealias DocumentRevisionSnapshot = com.example.data.model.DocumentRevisionSnapshot

/**
 * Dialog for browsing and restoring document revision snapshots.
 */
@Composable
fun VersionHistoryDialog(
    revisions: List<DocumentRevisionSnapshot>,
    onDismiss: () -> Unit,
    onCreateSnapshot: () -> Unit,
    onRestoreRevision: (String) -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("MMM d, yyyy • h:mm a", Locale.getDefault()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.History, contentDescription = null, tint = WordPrimary)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Version History & Snapshots")
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Previous Revisions (${revisions.size})", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    TextButton(onClick = onCreateSnapshot) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Snapshot Now", fontSize = 11.sp)
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))

                if (revisions.isEmpty()) {
                    Text(
                        "No revision snapshots saved yet. Tap 'Snapshot Now' to save the current milestone.",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.height(240.dp)
                    ) {
                        items(revisions) { rev ->
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(rev.label, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                        Text(dateFormat.format(Date(rev.timestamp)), fontSize = 11.sp, color = Color.Gray)
                                        Text("${rev.wordCount} words • ${rev.snippet.take(40)}...", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    OutlinedButton(
                                        onClick = { onRestoreRevision(rev.id) },
                                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                                    ) {
                                        Icon(Icons.Default.RestartAlt, contentDescription = null, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Restore", fontSize = 11.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss, colors = ButtonDefaults.buttonColors(containerColor = WordPrimary)) {
                Text("Done")
            }
        }
    )
}
