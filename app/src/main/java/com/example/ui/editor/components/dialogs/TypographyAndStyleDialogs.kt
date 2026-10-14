package com.example.ui.editor.components.dialogs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CustomStyle
import com.example.data.model.StrikethroughStyle
import com.example.data.model.UnderlineStyle
import com.example.ui.theme.WordPrimary

/**
 * Dialog for Character Typography, Letter Spacing (Tracking), Shadows, and Advanced Underline/Strikethrough.
 */
@Composable
fun TypographyAndEffectsDialog(
    initialLetterSpacingSp: Float?,
    initialUnderlineStyle: UnderlineStyle,
    initialUnderlineColorHex: String?,
    initialStrikethroughStyle: StrikethroughStyle,
    initialShadowColorHex: String?,
    onDismiss: () -> Unit,
    onApply: (
        letterSpacingSp: Float?,
        underlineStyle: UnderlineStyle,
        underlineColorHex: String?,
        strikethroughStyle: StrikethroughStyle,
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
                        UnderlineStyle.NONE to "None",
                        UnderlineStyle.SINGLE to "Single",
                        UnderlineStyle.DOUBLE to "Double",
                        UnderlineStyle.WAVY to "Wavy",
                        UnderlineStyle.DOTTED to "Dotted"
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
                        StrikethroughStyle.NONE to "None",
                        StrikethroughStyle.SINGLE to "Single",
                        StrikethroughStyle.DOUBLE to "Double"
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
                        if (underlineStyle == UnderlineStyle.NONE) null else underlineColorHex,
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
    initialStyle: CustomStyle,
    onDismiss: () -> Unit,
    onSave: (CustomStyle) -> Unit
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
