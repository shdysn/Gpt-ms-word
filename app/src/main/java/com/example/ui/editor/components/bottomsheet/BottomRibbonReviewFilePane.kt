package com.example.ui.editor.components.bottomsheet

import android.content.Context
import androidx.activity.result.ActivityResultLauncher
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Comment
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.editor.EditorDialogsState
import com.example.ui.editor.EditorViewModel
import com.example.ui.editor.RibbonTab
import com.example.ui.theme.WordPrimary

/**
 * Handles the Review and File tabs within the Bottom Ribbon Drawer.
 * Renders categorized action cards with distinct icons and clear descriptions.
 */
@Composable
fun BottomRibbonReviewFilePane(
    activeTab: RibbonTab,
    viewModel: EditorViewModel,
    dState: EditorDialogsState,
    context: Context,
    safDocxSaveLauncher: ActivityResultLauncher<String>?,
    modifier: Modifier = Modifier
) {
    val isSpeaking by viewModel.isSpeaking.collectAsStateWithLifecycle()

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        when (activeTab) {
            RibbonTab.REVIEW -> {
                ReviewTabContent(
                    viewModel = viewModel,
                    dState = dState,
                    context = context,
                    isSpeaking = isSpeaking
                )
            }
            RibbonTab.FILE -> {
                FileTabContent(
                    viewModel = viewModel,
                    dState = dState,
                    context = context,
                    safDocxSaveLauncher = safDocxSaveLauncher
                )
            }
            RibbonTab.VIEW, RibbonTab.DRAW -> {
                BottomRibbonViewDrawPane(
                    activeTab = activeTab,
                    viewModel = viewModel,
                    dState = dState
                )
            }
            else -> {}
        }
    }
}

@Composable
private fun ReviewTabContent(
    viewModel: EditorViewModel,
    dState: EditorDialogsState,
    context: Context,
    isSpeaking: Boolean
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Smart Proofing & AI Tools", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = WordPrimary)
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ActionTileCard(
                icon = Icons.Default.AutoAwesome,
                title = "Smart Writing",
                subtitle = "Grammar & Urdu translation",
                onClick = { dState.showSmartWritingDialog = true },
                modifier = Modifier.weight(1f)
            )
            ActionTileCard(
                icon = Icons.AutoMirrored.Filled.VolumeUp,
                title = if (isSpeaking) "Stop Reading" else "Read Aloud",
                subtitle = "Full document speech",
                isActive = isSpeaking,
                onClick = {
                    if (isSpeaking) viewModel.stopReadAloud()
                    else viewModel.startReadAloud(context)
                },
                modifier = Modifier.weight(1f)
            )
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ActionTileCard(
                icon = Icons.Default.Calculate,
                title = "Word Count",
                subtitle = "Statistics & metrics",
                onClick = { dState.showStatsDialog = true },
                modifier = Modifier.weight(1f)
            )
            ActionTileCard(
                icon = Icons.AutoMirrored.Filled.Comment,
                title = "Comments",
                subtitle = "Add review remarks",
                onClick = { dState.showCommentsDialog = true },
                modifier = Modifier.weight(1f)
            )
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ActionTileCard(
                icon = Icons.Default.Lock,
                title = "Password",
                subtitle = "Protect document",
                onClick = { dState.showPasswordDialog = true },
                modifier = Modifier.weight(1f)
            )
            ActionTileCard(
                icon = Icons.Default.History,
                title = "Version History",
                subtitle = "Revert earlier drafts",
                onClick = { dState.showVersionHistoryDialog = true },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun FileTabContent(
    viewModel: EditorViewModel,
    dState: EditorDialogsState,
    context: Context,
    safDocxSaveLauncher: ActivityResultLauncher<String>?
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Save & Export Options", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = WordPrimary)
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ActionTileCard(
                icon = Icons.Default.Save,
                title = "Quick Save",
                subtitle = "Save changes now",
                onClick = { viewModel.saveNow() },
                modifier = Modifier.weight(1f)
            )
            ActionTileCard(
                icon = Icons.Default.Save,
                title = "Save As...",
                subtitle = "New title or copy",
                onClick = { dState.showSaveAsDialog = true },
                modifier = Modifier.weight(1f)
            )
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ActionTileCard(
                icon = Icons.Default.Description,
                title = "Save DOCX",
                subtitle = "Microsoft Word file",
                onClick = {
                    safDocxSaveLauncher?.launch("${viewModel.document.value?.title ?: "Document"}.docx")
                },
                modifier = Modifier.weight(1f)
            )
            ActionTileCard(
                icon = Icons.Default.Share,
                title = "Export...",
                subtitle = "PDF, DOCX, TXT, EPUB",
                onClick = { dState.showExportOptionsDialog = true },
                modifier = Modifier.weight(1f)
            )
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ActionTileCard(
                icon = Icons.Default.Print,
                title = "Print",
                subtitle = "Android system print",
                onClick = { viewModel.printDocument(context) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}
