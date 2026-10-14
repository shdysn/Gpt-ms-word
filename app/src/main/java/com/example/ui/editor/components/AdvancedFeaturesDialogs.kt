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

/**
 * Dialog for Character Typography, Letter Spacing (Tracking), Shadows, and Advanced Underline/Strikethrough.
 */
@Composable
fun TypographyAndEffectsDialog(
    initialLetterSpacingSp: Float?,
    initialUnderlineStyle: com.example.data.model.UnderlineStyle,
    initialUnderlineColorHex: String?,
    initialStrikethroughStyle: com.example.data.model.StrikethroughStyle,
    initialShadowColorHex: String?,
    onDismiss: () -> Unit,
    onApply: (
        letterSpacingSp: Float?,
        underlineStyle: com.example.data.model.UnderlineStyle,
        underlineColorHex: String?,
        strikethroughStyle: com.example.data.model.StrikethroughStyle,
        shadowColorHex: String?
    ) -> Unit
) {
    var letterSpacing by remember { mutableStateOf(initialLetterSpacingSp ?: 0f) }
    var underlineStyle by remember { mutableStateOf(initialUnderlineStyle) }
    var underlineColorHex by remember { mutableStateOf(initialUnderlineColorHex ?: "#185ABD") }
    var strikethroughStyle by remember { mutableStateOf(initialStrikethroughStyle) }
    var shadowType by remember { mutableStateOf(if (initialShadowColorHex != null) "SHADOW" else "NONE") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Text Effects & Typography", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Letter Spacing (Tracking)
                Text("Character Spacing (Tracking):", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf(-1.5f to "Condensed", 0f to "Normal", 1.5f to "Expanded", 3f to "Very Wide").forEach { (spacing, label) ->
                        FilterChip(
                            selected = letterSpacing == spacing,
                            onClick = { letterSpacing = spacing },
                            label = { Text(label, fontSize = 11.sp) }
                        )
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                // Advanced Underline
                Text("Underline Style:", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf(
                        com.example.data.model.UnderlineStyle.NONE to "None",
                        com.example.data.model.UnderlineStyle.SINGLE to "Single",
                        com.example.data.model.UnderlineStyle.DOUBLE to "Double",
                        com.example.data.model.UnderlineStyle.WAVY to "Wavy",
                        com.example.data.model.UnderlineStyle.DOTTED to "Dotted"
                    ).forEach { (style, label) ->
                        FilterChip(
                            selected = underlineStyle == style,
                            onClick = { underlineStyle = style },
                            label = { Text(label, fontSize = 11.sp) }
                        )
                    }
                }

                // Strikethrough Style
                Text("Strikethrough Style:", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf(
                        com.example.data.model.StrikethroughStyle.NONE to "None",
                        com.example.data.model.StrikethroughStyle.SINGLE to "Single",
                        com.example.data.model.StrikethroughStyle.DOUBLE to "Double"
                    ).forEach { (style, label) ->
                        FilterChip(
                            selected = strikethroughStyle == style,
                            onClick = { strikethroughStyle = style },
                            label = { Text(label, fontSize = 11.sp) }
                        )
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                // Text Shadow & Glow
                Text("Text Shadow / Glow:", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf("NONE" to "None", "SHADOW" to "Subtle Shadow", "GLOW" to "Blue Glow").forEach { (type, label) ->
                        FilterChip(
                            selected = shadowType == type,
                            onClick = { shadowType = type },
                            label = { Text(label, fontSize = 11.sp) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val shadowHex = when (shadowType) {
                        "SHADOW" -> "#80000000"
                        "GLOW" -> "#802563EB"
                        else -> null
                    }
                    onApply(
                        if (letterSpacing == 0f) null else letterSpacing,
                        underlineStyle,
                        if (underlineStyle == com.example.data.model.UnderlineStyle.NONE) null else underlineColorHex,
                        strikethroughStyle,
                        shadowHex
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = WordPrimary)
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
 * Dialog for First Line Indent, Hanging Indent, Paragraph Spacing (Before/After), and Drop Cap.
 */
@Composable
fun ParagraphSpacingAndIndentsDialog(
    initialFirstLinePt: Float,
    initialHangingPt: Float,
    initialSpaceBeforePt: Float,
    initialSpaceAfterPt: Float,
    initialDropCapLines: Int,
    onDismiss: () -> Unit,
    onApply: (firstLinePt: Float, hangingPt: Float, spaceBeforePt: Float, spaceAfterPt: Float, dropCapLines: Int) -> Unit
) {
    var firstLinePt by remember { mutableStateOf(initialFirstLinePt) }
    var hangingPt by remember { mutableStateOf(initialHangingPt) }
    var spaceBeforePt by remember { mutableStateOf(initialSpaceBeforePt) }
    var spaceAfterPt by remember { mutableStateOf(initialSpaceAfterPt) }
    var dropCapLines by remember { mutableStateOf(initialDropCapLines) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Paragraph Indents & Spacing", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // First Line Indent vs Hanging Indent
                Text("Special Indentation:", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    FilterChip(
                        selected = firstLinePt == 0f && hangingPt == 0f,
                        onClick = { firstLinePt = 0f; hangingPt = 0f },
                        label = { Text("None", fontSize = 11.sp) }
                    )
                    FilterChip(
                        selected = firstLinePt > 0f,
                        onClick = { firstLinePt = 36f; hangingPt = 0f }, // 0.5 inch = 36 points
                        label = { Text("First Line (0.5 in)", fontSize = 11.sp) }
                    )
                    FilterChip(
                        selected = hangingPt > 0f,
                        onClick = { hangingPt = 36f; firstLinePt = 0f },
                        label = { Text("Hanging Indent", fontSize = 11.sp) }
                    )
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                // Spacing Before
                Text("Space Before Paragraph:", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf(0f to "0 pt", 6f to "6 pt", 12f to "12 pt", 18f to "18 pt").forEach { (pt, label) ->
                        FilterChip(
                            selected = spaceBeforePt == pt,
                            onClick = { spaceBeforePt = pt },
                            label = { Text(label, fontSize = 11.sp) }
                        )
                    }
                }

                // Spacing After
                Text("Space After Paragraph:", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf(0f to "0 pt", 6f to "6 pt", 12f to "12 pt", 18f to "18 pt").forEach { (pt, label) ->
                        FilterChip(
                            selected = spaceAfterPt == pt,
                            onClick = { spaceAfterPt = pt },
                            label = { Text(label, fontSize = 11.sp) }
                        )
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                // Drop Cap
                Text("Drop Cap (First Big Letter):", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    FilterChip(
                        selected = dropCapLines == 0,
                        onClick = { dropCapLines = 0 },
                        label = { Text("None", fontSize = 11.sp) }
                    )
                    FilterChip(
                        selected = dropCapLines == 3,
                        onClick = { dropCapLines = 3 },
                        label = { Text("Dropped (3 Lines)", fontSize = 11.sp) }
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onApply(firstLinePt, hangingPt, spaceBeforePt, spaceAfterPt, dropCapLines) },
                colors = ButtonDefaults.buttonColors(containerColor = WordPrimary)
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
 * Dialog for creating and saving a custom named style.
 */
@Composable
fun CreateCustomStyleDialog(
    initialStyle: com.example.data.model.CustomStyle,
    onDismiss: () -> Unit,
    onSave: (com.example.data.model.CustomStyle) -> Unit
) {
    var styleName by remember { mutableStateOf(initialStyle.name) }
    var fontSize by remember { mutableStateOf(initialStyle.fontSizeSp) }
    var isBold by remember { mutableStateOf(initialStyle.isBold) }
    var isItalic by remember { mutableStateOf(initialStyle.isItalic) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create New Style", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = styleName,
                    onValueChange = { styleName = it },
                    label = { Text("Style Name (e.g. My Quote, Summary)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Text("Font Size: ${fontSize.toInt()} pt", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf(11f, 13f, 15f, 18f, 22f).forEach { sz ->
                        FilterChip(
                            selected = fontSize == sz,
                            onClick = { fontSize = sz },
                            label = { Text("${sz.toInt()} pt", fontSize = 11.sp) }
                        )
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = isBold,
                        onClick = { isBold = !isBold },
                        label = { Text("Bold", fontWeight = FontWeight.Bold) }
                    )
                    FilterChip(
                        selected = isItalic,
                        onClick = { isItalic = !isItalic },
                        label = { Text("Italic", fontStyle = androidx.compose.ui.text.font.FontStyle.Italic) }
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (styleName.isNotBlank()) {
                        onSave(initialStyle.copy(name = styleName.trim(), fontSizeSp = fontSize, isBold = isBold, isItalic = isItalic))
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = WordPrimary)
            ) {
                Text("Save Style")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

/**
 * Dialog for Table Cell Properties (Vertical Alignment, Shading, Merge/Split).
 */
@Composable
fun TableCellPropertiesDialog(
    cell: com.example.data.model.TableCell,
    onDismiss: () -> Unit,
    onApply: (vAlign: com.example.data.model.CellVerticalAlignment, bgColorHex: String?) -> Unit,
    onMergeCells: () -> Unit,
    onSplitCell: () -> Unit
) {
    var vAlign by remember { mutableStateOf(cell.verticalAlignment) }
    var bgColorHex by remember { mutableStateOf(cell.backgroundColorHex) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Cell Formatting & Alignment", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Vertical Alignment in Cell:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf(
                        com.example.data.model.CellVerticalAlignment.TOP to "Top",
                        com.example.data.model.CellVerticalAlignment.CENTER to "Center",
                        com.example.data.model.CellVerticalAlignment.BOTTOM to "Bottom"
                    ).forEach { (align, label) ->
                        FilterChip(
                            selected = vAlign == align,
                            onClick = { vAlign = align },
                            label = { Text(label, fontSize = 11.sp) }
                        )
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                Text("Cell Shading (Background):", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf(
                        null to "None",
                        "#F1F5F9" to "Light Gray",
                        "#EFF6FF" to "Blue Tint",
                        "#FEF3C7" to "Warm Yellow"
                    ).forEach { (hex, name) ->
                        FilterChip(
                            selected = bgColorHex == hex,
                            onClick = { bgColorHex = hex },
                            label = { Text(name, fontSize = 11.sp) }
                        )
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                Text("Cell Merge & Split:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = { onMergeCells(); onDismiss() }) {
                        Text("Merge with Next Col", fontSize = 11.sp)
                    }
                    if (cell.colSpan > 1) {
                        OutlinedButton(onClick = { onSplitCell(); onDismiss() }) {
                            Text("Split Cell", fontSize = 11.sp)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onApply(vAlign, bgColorHex) },
                colors = ButtonDefaults.buttonColors(containerColor = WordPrimary)
            ) {
                Text("Apply")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
