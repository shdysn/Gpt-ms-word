package com.example.ui.editor.components

import androidx.compose.runtime.Composable
import com.example.data.model.DocumentComment
import com.example.data.model.TrackedChange

// Re-exports for backward compatibility
@Composable
fun CommentsDialog(
    comments: List<DocumentComment>,
    activeBlockId: String?,
    onDismiss: () -> Unit,
    onAddComment: (author: String, text: String) -> Unit,
    onResolveComment: (commentId: String) -> Unit,
    onDeleteComment: (commentId: String) -> Unit
) = com.example.ui.editor.components.review.CommentsDialog(
    comments, activeBlockId, onDismiss, onAddComment, onResolveComment, onDeleteComment
)

@Composable
fun TrackChangesDialog(
    isTrackChangesEnabled: Boolean,
    trackedChanges: List<TrackedChange>,
    onDismiss: () -> Unit,
    onToggleTrackChanges: (Boolean) -> Unit,
    onAcceptChange: (changeId: String) -> Unit,
    onRejectChange: (changeId: String) -> Unit,
    onAcceptAll: () -> Unit,
    onRejectAll: () -> Unit
) = com.example.ui.editor.components.review.TrackChangesDialog(
    isTrackChangesEnabled, trackedChanges, onDismiss, onToggleTrackChanges,
    onAcceptChange, onRejectChange, onAcceptAll, onRejectAll
)
