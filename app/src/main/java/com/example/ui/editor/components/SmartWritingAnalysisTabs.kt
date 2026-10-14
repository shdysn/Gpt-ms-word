package com.example.ui.editor.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
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
import com.example.data.model.ParagraphBlock
import com.example.data.model.RichDocument
import com.example.ui.theme.WordPrimary
import java.util.Locale

@Composable
fun SummarizeTabContent(
    document: RichDocument?,
    onInsertSummary: (String) -> Unit
) {
    val allParagraphs = document?.blocks?.filterIsInstance<ParagraphBlock>()?.map { it.text }?.filter { it.isNotBlank() } ?: emptyList()
    val fullText = allParagraphs.joinToString("\n\n")

    val summary = remember(fullText) {
        if (fullText.isBlank()) {
            "Document is currently empty. Add content to generate an executive summary."
        } else {
            val rawSentences = fullText.split(Regex("[.!?]\\s+|\n+")).map { it.trim() }.filter { it.length > 15 }
            if (rawSentences.isEmpty()) {
                fullText.take(150)
            } else {
                val stopWords = setOf("the", "is", "at", "which", "on", "a", "an", "and", "or", "in", "to", "for", "with", "as", "by", "of", "from", "it", "this", "that", "are", "was", "were", "be", "has", "have", "had")
                val wordFreq = mutableMapOf<String, Int>()
                val words = fullText.lowercase(Locale.ENGLISH).split(Regex("[\\W_]+")).filter { it.length > 2 && it !in stopWords }
                words.forEach { w -> wordFreq[w] = (wordFreq[w] ?: 0) + 1 }

                val scoredSentences = rawSentences.mapIndexed { idx, sent ->
                    val sentWords = sent.lowercase(Locale.ENGLISH).split(Regex("[\\W_]+")).filter { it in wordFreq }
                    val score = sentWords.sumOf { wordFreq[it] ?: 0 }.toDouble() / (sentWords.size.coerceAtLeast(1) + 2.0) + (if (idx == 0) 3.0 else 0.0)
                    sent to score
                }.sortedByDescending { it.second }

                val selectedKeyPoints = scoredSentences.take(4).map { it.first }.distinct()
                val bullets = selectedKeyPoints.joinToString("\n") { "• ${it.removeSuffix(".")}" }
                "Executive Summary (${document?.title ?: "Document"}):\n\n$bullets."
            }
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Document Intelligence Summary", fontWeight = FontWeight.Bold, fontSize = 13.sp)
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFFEFF6FF)
            ) {
                Text(
                    text = "${allParagraphs.size} Paragraphs Analyzed",
                    fontSize = 11.sp,
                    color = WordPrimary,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

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
fun ReadabilityTabContent(document: RichDocument?) {
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
        Text("Document Readability & Analytics", fontWeight = FontWeight.Bold, fontSize = 13.sp)

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            MetricCard("Silent Reading Time", "~$readingTimeMinutes min", Modifier.weight(1f))
            MetricCard("Complexity Index", readabilityLevel, Modifier.weight(1f))
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            MetricCard("Total Word Count", "$totalWords words", Modifier.weight(1f))
            MetricCard("Paragraph Sections", "$paragraphCount sections", Modifier.weight(1f))
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            MetricCard("Character Count", "$totalChars chars", Modifier.weight(1f))
            MetricCard("Density / Section", "$avgWordsPerPara avg words", Modifier.weight(1f))
        }
    }
}

@Composable
fun MetricCard(title: String, value: String, modifier: Modifier = Modifier) {
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
