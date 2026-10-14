package com.example.ui.editor.components.ribbon

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Comment
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.editor.EditorViewModel

@Composable
fun ReviewRibbonControls(
    viewModel: EditorViewModel,
    context: Context,
    onOpenPasswordDialog: () -> Unit,
    onOpenVersionHistoryDialog: () -> Unit,
    onOpenStatsDialog: () -> Unit,
    onStartVoiceTyping: () -> Unit,
    onOpenCommentsDialog: () -> Unit = {},
    onOpenTrackChangesDialog: () -> Unit = {},
    onOpenSmartWritingDialog: () -> Unit = {}
) {
    val document by viewModel.document.collectAsStateWithLifecycle()
    val isSpeaking by viewModel.isSpeaking.collectAsStateWithLifecycle()
    val speechRate by viewModel.speechRate.collectAsStateWithLifecycle()
    val snapshots by viewModel.revisionSnapshots.collectAsStateWithLifecycle()
    val isProtected = document?.isPasswordProtected == true
    val commentsCount = document?.comments?.size ?: 0
    val isTracking = document?.isTrackChangesEnabled == true

    // AI Smart Writing Assistant
    RibbonActionButton(
        icon = Icons.Default.AutoAwesome,
        label = "AI Assistant",
        onClick = onOpenSmartWritingDialog
    )

    Box(modifier = Modifier.height(20.dp).width(1.dp).background(MaterialTheme.colorScheme.outlineVariant))

    // Comments & Annotations
    RibbonActionButton(
        icon = Icons.Default.Comment,
        label = if (commentsCount > 0) "Comments ($commentsCount)" else "Comments",
        onClick = onOpenCommentsDialog
    )

    // Track Changes
    RibbonToggleButton(
        icon = Icons.Default.EditNote,
        label = if (isTracking) "Tracking (ON)" else "Track Changes",
        isSelected = isTracking,
        onClick = onOpenTrackChangesDialog
    )

    Box(modifier = Modifier.height(20.dp).width(1.dp).background(MaterialTheme.colorScheme.outlineVariant))

    // Text-to-Speech (Read Aloud)
    if (isSpeaking) {
        RibbonActionButton(
            icon = Icons.Default.Stop,
            label = "Stop Reading",
            onClick = { viewModel.stopReadAloud() }
        )
    } else {
        RibbonActionButton(
            icon = Icons.AutoMirrored.Filled.VolumeUp,
            label = "Read Aloud",
            onClick = { viewModel.startReadAloud(context) }
        )
    }

    // TTS Speed Rate toggle
    RibbonActionButton(
        icon = Icons.Default.Speed,
        label = "${speechRate}x Speed",
        onClick = { viewModel.cycleSpeechRate() }
    )

    Box(modifier = Modifier.height(20.dp).width(1.dp).background(MaterialTheme.colorScheme.outlineVariant))

    // Voice Typing (Dictation / Speech-to-Text)
    RibbonActionButton(
        icon = Icons.Default.Mic,
        label = "Voice Typing",
        onClick = onStartVoiceTyping
    )

    Box(modifier = Modifier.height(20.dp).width(1.dp).background(MaterialTheme.colorScheme.outlineVariant))

    // Document Security Lock
    RibbonToggleButton(
        icon = if (isProtected) Icons.Default.Lock else Icons.Default.LockOpen,
        label = if (isProtected) "Protected PIN" else "Password Lock",
        isSelected = isProtected,
        onClick = onOpenPasswordDialog
    )

    // Version History Snapshots
    RibbonActionButton(
        icon = Icons.Default.History,
        label = if (snapshots.isNotEmpty()) "History (${snapshots.size})" else "Version History",
        onClick = onOpenVersionHistoryDialog
    )

    Box(modifier = Modifier.height(20.dp).width(1.dp).background(MaterialTheme.colorScheme.outlineVariant))

    // Word Count & Statistics
    RibbonActionButton(
        icon = Icons.Default.BarChart,
        label = "Document Stats",
        onClick = onOpenStatsDialog
    )
}
