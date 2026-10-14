package com.example.ui.editor.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.Sync
import androidx.compose.material.icons.outlined.SyncProblem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ParagraphBlock
import com.example.data.model.RichDocument
import com.example.editor.engine.AutosaveStatus
import com.example.ui.theme.WordPrimary

@Composable
fun StatRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun AutosaveChip(status: AutosaveStatus) {
    val (text, color, icon) = when (status) {
        AutosaveStatus.IDLE -> Triple("Saved", Color(0xFF107C41), Icons.Default.Check)
        AutosaveStatus.DIRTY -> Triple("Editing", Color(0xFFD83B01), Icons.Outlined.Sync)
        AutosaveStatus.SAVING -> Triple("Saving...", Color(0xFF185ABD), Icons.Outlined.Sync)
        AutosaveStatus.SAVED -> Triple("Saved", Color(0xFF107C41), Icons.Default.Check)
        AutosaveStatus.ERROR -> Triple("Sync Error", Color.Red, Icons.Outlined.SyncProblem)
    }

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = color.copy(alpha = 0.12f),
        modifier = Modifier.padding(end = 4.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(13.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = text,
                color = color,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
fun EditorStatusBar(
    document: RichDocument?,
    currentPageNumber: Int = 1,
    totalPages: Int = document?.estimatedPageCount ?: 1,
    showWordCount: Boolean = true,
    autosaveStatus: AutosaveStatus,
    zoomPercent: Int,
    activeBlock: ParagraphBlock?,
    onOpenLayoutDialog: () -> Unit = {}
) {
    val totalWords = document?.wordCount ?: 0
    val totalChars = document?.characterCount ?: 0
    val readingMins = (totalWords / 200).coerceAtLeast(1)

    Surface(
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 4.dp,
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Clickable Page Number indicator
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.padding(end = 2.dp)
                ) {
                    Text(
                        text = "Page $currentPageNumber of $totalPages",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }

                if (showWordCount) {
                    Text(
                        text = "$totalWords words",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text("•", fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
                    Text(
                        text = "$totalChars chars",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text("•", fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
                    Text(
                        text = "~$readingMins min",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text("•", fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
                }

                Text(
                    text = if (activeBlock?.isRtl == true) "Urdu (RTL)" else "English (LTR)",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = WordPrimary
                )
            }

            Text(
                text = "$zoomPercent%",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
