package com.example.ui.editor.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ParagraphBlock
import com.example.ui.theme.WordPrimary

@Composable
fun PolishTabContent(
    activeParagraph: ParagraphBlock?,
    onApplyRewrite: (paragraphId: String, newText: String) -> Unit
) {
    val originalText = activeParagraph?.text ?: ""
    var selectedTone by remember { mutableStateOf("Professional") }

    val tones = listOf("Professional", "Concise", "Executive", "Academic", "Creative")

    val rewrittenText = remember(originalText, selectedTone) {
        if (originalText.isBlank()) {
            "Select or place cursor on a paragraph to polish its tone."
        } else {
            when (selectedTone) {
                "Concise" -> {
                    var result = originalText.trim().replace(Regex("\\s+"), " ")
                    val wordinessMap = mapOf(
                        Regex("in order to", RegexOption.IGNORE_CASE) to "to",
                        Regex("due to the fact that", RegexOption.IGNORE_CASE) to "because",
                        Regex("at this point in time", RegexOption.IGNORE_CASE) to "now",
                        Regex("for the purpose of", RegexOption.IGNORE_CASE) to "for",
                        Regex("a large number of", RegexOption.IGNORE_CASE) to "many",
                        Regex("prior to", RegexOption.IGNORE_CASE) to "before",
                        Regex("in the event that", RegexOption.IGNORE_CASE) to "if",
                        Regex("with reference to", RegexOption.IGNORE_CASE) to "regarding",
                        Regex("it should be noted that", RegexOption.IGNORE_CASE) to "",
                        Regex("take into consideration", RegexOption.IGNORE_CASE) to "consider",
                        Regex("has the capability to", RegexOption.IGNORE_CASE) to "can"
                    )
                    wordinessMap.forEach { (pattern, replacement) ->
                        result = result.replace(pattern, replacement)
                    }
                    result.trim().replace(Regex("\\s+"), " ")
                }
                "Executive" -> {
                    val trimmed = originalText.trim().replaceFirstChar { it.uppercase() }
                    "Key Objective & Action Item:\n$trimmed"
                }
                "Academic" -> {
                    val trimmed = originalText.trim().replaceFirstChar { it.lowercase() }
                    "Empirical evidence and systematic review demonstrate that $trimmed"
                }
                "Creative" -> {
                    val trimmed = originalText.trim().replaceFirstChar { it.lowercase() }
                    "Notably, as observed across the domain, $trimmed"
                }
                else -> {
                    val trimmed = originalText.trim().replaceFirstChar { it.uppercase() }
                    if (!trimmed.endsWith(".") && !trimmed.endsWith("!") && !trimmed.endsWith("?")) "$trimmed." else trimmed
                }
            }
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("Target Tone:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            tones.forEach { tone ->
                FilterChip(
                    selected = selectedTone == tone,
                    onClick = { selectedTone = tone },
                    label = { Text(tone, fontSize = 11.sp) }
                )
            }
        }

        Text("Original Text:", fontSize = 11.sp, color = Color.Gray)
        Surface(
            shape = RoundedCornerShape(6.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = originalText.ifBlank { "(No active paragraph text)" },
                fontSize = 11.sp,
                fontStyle = FontStyle.Italic,
                color = Color.Gray,
                modifier = Modifier.padding(8.dp)
            )
        }

        Text("Polished Version ($selectedTone):", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = WordPrimary)
        Surface(
            shape = RoundedCornerShape(6.dp),
            color = Color(0xFFEFF6FF),
            border = BorderStroke(1.dp, Color(0xFF93C5FD)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = rewrittenText,
                fontSize = 12.sp,
                lineHeight = 18.sp,
                color = Color(0xFF1E293B),
                modifier = Modifier.padding(10.dp)
            )
        }

        if (originalText.isNotBlank() && activeParagraph != null) {
            Button(
                onClick = { onApplyRewrite(activeParagraph.id, rewrittenText) },
                colors = ButtonDefaults.buttonColors(containerColor = WordPrimary),
                modifier = Modifier.fillMaxWidth().testTag("apply_rewrite_btn")
            ) {
                Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Apply Polish to Paragraph", fontSize = 12.sp)
            }
        }
    }
}
