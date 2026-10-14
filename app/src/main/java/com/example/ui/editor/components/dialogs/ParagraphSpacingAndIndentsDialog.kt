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
import com.example.ui.theme.WordPrimary

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
