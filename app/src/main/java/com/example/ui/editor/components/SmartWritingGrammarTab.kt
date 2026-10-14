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
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.typography.BidiUtils
import com.example.data.model.ParagraphBlock
import com.example.data.model.RichDocument

@Composable
fun GrammarTabContent(
    document: RichDocument?,
    activeParagraph: ParagraphBlock?,
    onApplyRewrite: (paragraphId: String, newText: String) -> Unit
) {
    val textToCheck = activeParagraph?.text ?: ""

    val issues = remember(textToCheck) {
        val detected = mutableListOf<Pair<String, String>>()
        if (textToCheck.isBlank()) return@remember detected

        if (textToCheck.contains("  ")) {
            detected.add("Multiple consecutive spaces" to textToCheck.replace(Regex(" +"), " "))
        }
        if (textToCheck.contains("..")) {
            detected.add("Double period typo (..)" to textToCheck.replace("..", "."))
        }
        if (textToCheck.isNotEmpty() && textToCheck.first().isLowerCase()) {
            detected.add("Sentence starts with lowercase letter" to textToCheck.replaceFirstChar { it.uppercase() })
        }

        val duplicateRegex = Regex("\\b([a-zA-Z]+)\\s+\\1\\b", RegexOption.IGNORE_CASE)
        val dupMatch = duplicateRegex.find(textToCheck)
        if (dupMatch != null) {
            val repeatedWord = dupMatch.groupValues[1]
            detected.add("Duplicate word ('$repeatedWord $repeatedWord')" to textToCheck.replace(dupMatch.value, repeatedWord))
        }

        val typoMap = mapOf(
            "teh" to "the",
            "recieve" to "receive",
            "seperate" to "separate",
            "definately" to "definitely",
            "untill" to "until",
            "occured" to "occurred",
            "accomodate" to "accommodate",
            "wierd" to "weird",
            "goverment" to "government",
            "enviroment" to "environment",
            "tommorrow" to "tomorrow",
            "beleive" to "believe",
            "truely" to "truly",
            "writting" to "writing",
            "sucessful" to "successful",
            "begining" to "beginning",
            "neccessary" to "necessary",
            "embarass" to "embarrass",
            "existance" to "existence",
            "guarentee" to "guarantee",
            "happend" to "happened",
            "independant" to "independent",
            "relevent" to "relevant",
            "tendancy" to "tendency",
            "usefull" to "useful",
            "alot" to "a lot"
        )

        typoMap.forEach { (wrong, right) ->
            if (Regex("\\b$wrong\\b", RegexOption.IGNORE_CASE).containsMatchIn(textToCheck)) {
                detected.add("Typo ('$wrong' -> '$right')" to textToCheck.replace(Regex("\\b$wrong\\b", RegexOption.IGNORE_CASE), right))
            }
        }

        if (BidiUtils.detectParagraphIsRtl(textToCheck) && textToCheck.endsWith(".")) {
            detected.add("Urdu sentence ending with English full-stop" to textToCheck.dropLast(1) + "۔")
        }

        detected
    }

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Spell & Grammar Scanner", fontWeight = FontWeight.Bold, fontSize = 13.sp)
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (issues.isEmpty()) Color(0xFFECFDF5) else Color(0xFFFEF3C7)
            ) {
                Text(
                    text = if (issues.isEmpty()) "✓ Clean & Verified" else "${issues.size} issues detected",
                    fontSize = 11.sp,
                    color = if (issues.isEmpty()) Color(0xFF065F46) else Color(0xFF92400E),
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        if (issues.isEmpty()) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFFF8FAFC),
                modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp)
            ) {
                Text(
                    text = if (textToCheck.isBlank()) "Select or type in a paragraph to scan for grammar and typos." else "Paragraph spelling, spacing, and punctuation are verified clean.",
                    color = Color.Gray,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(12.dp)
                )
            }
        } else {
            if (activeParagraph != null && issues.size > 1) {
                Button(
                    onClick = {
                        var accumulated = textToCheck
                        issues.forEach { (_, fixedVersion) ->
                            accumulated = fixedVersion
                        }
                        onApplyRewrite(activeParagraph.id, accumulated)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF059669)),
                    modifier = Modifier.fillMaxWidth().testTag("fix_all_grammar_btn")
                ) {
                    Icon(Icons.Default.DoneAll, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Fix All Detected Issues (${issues.size})", fontSize = 12.sp)
                }
            }

            issues.forEach { (issueTitle, fixed) ->
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFFFEF2F2),
                    border = BorderStroke(1.dp, Color(0xFFFECACA)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(issueTitle, fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = Color(0xFF991B1B))
                            Text("Suggested: ${fixed.take(40)}...", fontSize = 11.sp, color = Color.Gray)
                        }
                        if (activeParagraph != null) {
                            Button(
                                onClick = { onApplyRewrite(activeParagraph.id, fixed) },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF059669))
                            ) {
                                Text("Fix", fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}
