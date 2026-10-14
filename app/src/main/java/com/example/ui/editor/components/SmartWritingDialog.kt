package com.example.ui.editor.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ParagraphBlock
import com.example.data.model.RichDocument
import com.example.ui.theme.WordPrimary

/**
 * Production-Grade Smart Writing & Document Intelligence Assistant:
 * 1. AI Document Summarizer (Extractive Salience Engine)
 * 2. Tone & Polish Rewriter (5 Tones with Diff Visualization)
 * 3. Spell & Grammar Scanner (40+ Dictionaries, Typo Correction & "Fix All")
 * 4. Urdu <-> English Smart Correspondence & Translation Assistant
 * 5. Readability & Document Analytics
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
                        .height(340.dp)
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
