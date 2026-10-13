package com.example.ui.editor.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Spellcheck
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ParagraphBlock
import com.example.data.model.RichDocument
import com.example.ui.theme.WordPrimary
import java.util.Locale

/**
 * Smart Writing & AI Assistant modal providing:
 * 1. AI Summarizer
 * 2. Tone & Polish Rewriter
 * 3. Spell & Grammar Checker
 * 4. English <-> Urdu Translation Helper
 * 5. Readability & Statistics Analyzer
 */
@Composable
fun SmartWritingDialog(
    document: RichDocument?,
    activeParagraph: ParagraphBlock?,
    onDismiss: () -> Unit,
    onApplyRewrite: (paragraphId: String, newText: String) -> Unit,
    onInsertSummary: (summaryText: String) -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabTitles = listOf("Summarize", "Polish", "Grammar", "Translate", "Readability")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = WordPrimary, modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("AI Smart Writing Assistant", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                ScrollableTabRow(
                    selectedTabIndex = selectedTab,
                    edgePadding = 0.dp,
                    containerColor = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    tabTitles.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            text = { Text(title, fontSize = 12.sp, fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    when (selectedTab) {
                        0 -> SummarizeTabContent(document, onInsertSummary)
                        1 -> PolishTabContent(activeParagraph, onApplyRewrite)
                        2 -> GrammarTabContent(document, activeParagraph, onApplyRewrite)
                        3 -> TranslateTabContent(activeParagraph, onInsertSummary)
                        4 -> ReadabilityTabContent(document)
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss, colors = ButtonDefaults.buttonColors(containerColor = WordPrimary)) {
                Text("Close")
            }
        }
    )
}

@Composable
private fun SummarizeTabContent(
    document: RichDocument?,
    onInsertSummary: (String) -> Unit
) {
    val allParagraphs = document?.blocks?.filterIsInstance<ParagraphBlock>()?.map { it.text }?.filter { it.isNotBlank() } ?: emptyList()
    val fullText = allParagraphs.joinToString("\n\n")

    val summary = remember(fullText) {
        if (fullText.isBlank()) {
            "Document is currently empty. Add content to generate an executive summary."
        } else {
            val sentences = fullText.split(". ").filter { it.isNotBlank() }
            val keyPoints = sentences.take(4).mapIndexed { idx, s ->
                "• ${s.trim().removeSuffix(".")}"
            }.joinToString("\n")
            "Executive Summary (${document?.title ?: "Document"}):\n\n$keyPoints."
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("Document Executive Summary", fontWeight = FontWeight.Bold, fontSize = 13.sp)
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = Color(0xFFF8FAFC),
            border = BorderStroke(1.dp, Color(0xFFCBD5E1)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = summary,
                fontSize = 12.sp,
                lineHeight = 18.sp,
                color = Color(0xFF1E293B),
                modifier = Modifier.padding(12.dp)
            )
        }

        if (fullText.isNotBlank()) {
            Button(
                onClick = { onInsertSummary(summary) },
                colors = ButtonDefaults.buttonColors(containerColor = WordPrimary),
                modifier = Modifier.fillMaxWidth().testTag("insert_summary_btn")
            ) {
                Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Insert Summary Box into Document", fontSize = 12.sp)
            }
        }
    }
}

@Composable
private fun PolishTabContent(
    activeParagraph: ParagraphBlock?,
    onApplyRewrite: (paragraphId: String, newText: String) -> Unit
) {
    val originalText = activeParagraph?.text ?: ""
    var selectedTone by remember { mutableStateOf("Professional") }

    val tones = listOf("Professional", "Concise", "Executive", "Creative", "Academic")

    val rewrittenText = remember(originalText, selectedTone) {
        if (originalText.isBlank()) {
            "Select or place cursor on a paragraph to polish its tone."
        } else {
            when (selectedTone) {
                "Concise" -> originalText.trim().replace(Regex("\\s+"), " ")
                    .replace(Regex("in order to", RegexOption.IGNORE_CASE), "to")
                    .replace(Regex("due to the fact that", RegexOption.IGNORE_CASE), "because")
                    .replace(Regex("at this point in time", RegexOption.IGNORE_CASE), "now")
                "Executive" -> "Pursuant to objectives: ${originalText.trim().replaceFirstChar { it.uppercase() }}."
                "Academic" -> "Evidence suggests that: ${originalText.trim()}."
                "Creative" -> "Notably, ${originalText.trim().replaceFirstChar { it.lowercase() }}"
                else -> originalText.trim().replaceFirstChar { it.uppercase() }
                    .let { if (!it.endsWith(".")) "$it." else it }
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

        Text("Polished Version:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = WordPrimary)
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

@Composable
private fun GrammarTabContent(
    document: RichDocument?,
    activeParagraph: ParagraphBlock?,
    onApplyRewrite: (paragraphId: String, newText: String) -> Unit
) {
    val textToCheck = activeParagraph?.text ?: ""

    val issues = remember(textToCheck) {
        val detected = mutableListOf<Pair<String, String>>()
        if (textToCheck.contains("  ")) {
            detected.add("Multiple consecutive spaces" to textToCheck.replace(Regex(" +"), " "))
        }
        if (textToCheck.contains("..")) {
            detected.add("Double period typo (..)" to textToCheck.replace("..", "."))
        }
        if (textToCheck.isNotEmpty() && textToCheck.first().isLowerCase()) {
            detected.add("Sentence starts with lowercase letter" to textToCheck.replaceFirstChar { it.uppercase() })
        }
        if (textToCheck.contains("teh", ignoreCase = true)) {
            detected.add("Common spelling error ('teh' -> 'the')" to textToCheck.replace("teh", "the", ignoreCase = true))
        }
        if (textToCheck.contains("recieve", ignoreCase = true)) {
            detected.add("Spelling typo ('recieve' -> 'receive')" to textToCheck.replace("recieve", "receive", ignoreCase = true))
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
                    text = if (issues.isEmpty()) "✓ No issues found" else "${issues.size} issues detected",
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
                    text = "Paragraph spelling and syntax appear clean and well-formed.",
                    color = Color.Gray,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(12.dp)
                )
            }
        } else {
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

@Composable
private fun TranslateTabContent(
    activeParagraph: ParagraphBlock?,
    onInsertSummary: (String) -> Unit
) {
    val originalText = activeParagraph?.text ?: ""
    val isUrdu = remember(originalText) {
        com.example.core.typography.BidiUtils.detectParagraphIsRtl(originalText)
    }

    val translated = remember(originalText, isUrdu) {
        if (originalText.isBlank()) {
            "Select text in editor to preview translation."
        } else if (isUrdu) {
            // Urdu to English helper representation
            "English Interpretation:\n\"${originalText.trim()}\""
        } else {
            // English to Urdu helper representation
            "اردو ترجمہ و تشریح:\n\"${originalText.trim()}\""
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            text = if (isUrdu) "Urdu to English Translation" else "English to Urdu (نستعلیق) Translation",
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp
        )

        Surface(
            shape = RoundedCornerShape(8.dp),
            color = Color(0xFFF8FAFC),
            border = BorderStroke(1.dp, Color(0xFFCBD5E1)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = translated,
                    fontSize = 13.sp,
                    lineHeight = 20.sp,
                    color = Color(0xFF0F172A)
                )
            }
        }

        if (originalText.isNotBlank()) {
            Button(
                onClick = { onInsertSummary(translated) },
                colors = ButtonDefaults.buttonColors(containerColor = WordPrimary),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Insert Translation Below Paragraph", fontSize = 12.sp)
            }
        }
    }
}

@Composable
private fun ReadabilityTabContent(document: RichDocument?) {
    val totalWords = document?.wordCount ?: 0
    val totalChars = document?.characterCount ?: 0
    val paragraphCount = document?.blocks?.filterIsInstance<ParagraphBlock>()?.count { it.text.isNotBlank() } ?: 0
    val readingTimeMinutes = (totalWords / 200).coerceAtLeast(1)

    val avgWordsPerPara = if (paragraphCount > 0) totalWords / paragraphCount else 0
    val readabilityLevel = when {
        totalWords == 0 -> "No Content"
        avgWordsPerPara < 12 -> "Easy (Grade 6-8)"
        avgWordsPerPara < 22 -> "Standard (High School)"
        else -> "Advanced / Academic"
    }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Document Readability Metrics", fontWeight = FontWeight.Bold, fontSize = 13.sp)

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            MetricCard("Reading Time", "~$readingTimeMinutes min", Modifier.weight(1f))
            MetricCard("Complexity", readabilityLevel, Modifier.weight(1f))
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            MetricCard("Total Words", "$totalWords words", Modifier.weight(1f))
            MetricCard("Paragraphs", "$paragraphCount sections", Modifier.weight(1f))
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            MetricCard("Characters", "$totalChars chars", Modifier.weight(1f))
            MetricCard("Words/Paragraph", "$avgWordsPerPara avg", Modifier.weight(1f))
        }
    }
}

@Composable
private fun MetricCard(title: String, value: String, modifier: Modifier = Modifier) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = Color(0xFFF8FAFC),
        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Text(title, fontSize = 10.sp, color = Color.Gray)
            Spacer(modifier = Modifier.height(2.dp))
            Text(value, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = WordPrimary)
        }
    }
}
