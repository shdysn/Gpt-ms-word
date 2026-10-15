package com.example.ui.editor.components.dialogs

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.CustomStyle
import com.example.data.model.ParagraphBlock
import com.example.data.model.RichDocument
import com.example.data.model.ShapeBlock
import com.example.data.model.TableCell
import com.example.data.model.TableBlock
import com.example.ui.editor.EditorViewModel
import com.example.ui.editor.components.CommentsDialog
import com.example.ui.editor.components.DocumentStatisticsDialog
import com.example.ui.editor.components.InsertFootnoteDialog
import com.example.ui.editor.components.PageBordersDialog
import com.example.ui.editor.components.PageColorDialog
import com.example.ui.editor.components.SmartWritingDialog
import com.example.ui.editor.components.TrackChangesDialog

@Composable
fun EditorAdvancedModals(
    viewModel: EditorViewModel,
    document: RichDocument?,
    actualPageCount: Int,
    activeParagraph: ParagraphBlock?,
    showStatsDialog: Boolean,
    onDismissStatsDialog: () -> Unit,
    showSignatureDialog: Boolean,
    onDismissSignatureDialog: () -> Unit,
    showShapeDialog: Boolean,
    selectedShapeForEdit: ShapeBlock? = null,
    onDismissShapeDialog: () -> Unit,
    showWatermarkDialog: Boolean,
    onDismissWatermarkDialog: () -> Unit,
    showPasswordDialog: Boolean,
    onDismissPasswordDialog: () -> Unit,
    showVersionHistoryDialog: Boolean,
    onDismissVersionHistoryDialog: () -> Unit,
    showCommentsDialog: Boolean,
    onDismissCommentsDialog: () -> Unit,
    showTrackChangesDialog: Boolean,
    onDismissTrackChangesDialog: () -> Unit,
    showSmartWritingDialog: Boolean,
    onDismissSmartWritingDialog: () -> Unit,
    showFootnoteDialog: Boolean,
    onDismissFootnoteDialog: () -> Unit,
    showPageBordersDialog: Boolean,
    onDismissPageBordersDialog: () -> Unit,
    showPageColorDialog: Boolean,
    onDismissPageColorDialog: () -> Unit,
    showTypographyEffectsDialog: Boolean,
    onDismissTypographyEffectsDialog: () -> Unit,
    showParagraphSpacingDialog: Boolean,
    onDismissParagraphSpacingDialog: () -> Unit,
    showCreateCustomStyleDialog: Boolean,
    onDismissCreateCustomStyleDialog: () -> Unit,
    selectedCellForProperties: Triple<TableBlock, Int, Int>?,
    onDismissCellProperties: () -> Unit
) {
    if (showStatsDialog) {
        DocumentStatisticsDialog(
            document = document,
            actualPageCount = actualPageCount,
            activeParagraph = activeParagraph,
            onDismiss = onDismissStatsDialog
        )
    }

    if (showSignatureDialog) {
        DigitalSignatureDialog(
            onDismiss = onDismissSignatureDialog,
            onConfirm = { signerName, signerTitle, strokeHex, points ->
                viewModel.insertSignature(signerName, signerTitle, strokeHex, points)
                onDismissSignatureDialog()
            }
        )
    }

    if (showShapeDialog || selectedShapeForEdit != null) {
        VectorShapeGalleryDialog(
            shapeToEdit = selectedShapeForEdit,
            onDismiss = onDismissShapeDialog,
            onConfirm = { shapeType, title, content, bgHex, borderHex, strokeWidthPt, rotationDeg, widthDp, heightDp ->
                if (selectedShapeForEdit != null) {
                    viewModel.updateShape(
                        blockId = selectedShapeForEdit.id,
                        shapeType = shapeType,
                        title = title,
                        content = content,
                        bgHex = bgHex,
                        borderHex = borderHex,
                        strokeWidthPt = strokeWidthPt,
                        rotationDeg = rotationDeg,
                        widthDp = widthDp,
                        heightDp = heightDp
                    )
                } else {
                    viewModel.insertVectorShape(
                        shapeType = shapeType,
                        title = title,
                        content = content,
                        bgHex = bgHex,
                        borderHex = borderHex,
                        strokeWidthPt = strokeWidthPt,
                        rotationDeg = rotationDeg,
                        widthDp = widthDp,
                        heightDp = heightDp
                    )
                }
                onDismissShapeDialog()
            }
        )
    }

    if (showWatermarkDialog && document != null) {
        WatermarkDialog(
            initialText = document.layoutConfig.watermarkText,
            onDismiss = onDismissWatermarkDialog,
            onApply = { text, colorHex, angle ->
                viewModel.setWatermark(text, colorHex, angle)
                onDismissWatermarkDialog()
            },
            onRemove = {
                viewModel.removeWatermark()
                onDismissWatermarkDialog()
            }
        )
    }

    if (showPasswordDialog && document != null) {
        DocumentPasswordDialog(
            isCurrentlyLocked = document.isPasswordProtected,
            onDismiss = onDismissPasswordDialog,
            onSetPassword = { pwd ->
                viewModel.setDocumentPassword(pwd)
                onDismissPasswordDialog()
            },
            onRemovePassword = {
                viewModel.removeDocumentPassword()
                onDismissPasswordDialog()
            }
        )
    }

    if (showVersionHistoryDialog) {
        val revisions by viewModel.revisionSnapshots.collectAsStateWithLifecycle()
        VersionHistoryDialog(
            revisions = revisions,
            onDismiss = onDismissVersionHistoryDialog,
            onCreateSnapshot = {
                val label = "Version ${revisions.size + 1}"
                viewModel.createNamedSnapshot(label)
            },
            onRestoreRevision = { revId ->
                viewModel.restoreRevisionSnapshot(revId)
                onDismissVersionHistoryDialog()
            }
        )
    }

    if (showCommentsDialog && document != null) {
        val activeBlock = activeParagraph
        CommentsDialog(
            comments = document.comments,
            activeBlockId = activeBlock?.id,
            onDismiss = onDismissCommentsDialog,
            onAddComment = { author, text ->
                viewModel.addComment(
                    blockId = activeBlock?.id ?: document.blocks.firstOrNull()?.id ?: "",
                    author = author,
                    text = text
                )
            },
            onResolveComment = { commentId ->
                viewModel.resolveComment(commentId)
            },
            onDeleteComment = { commentId ->
                viewModel.deleteComment(commentId)
            }
        )
    }

    if (showTrackChangesDialog && document != null) {
        TrackChangesDialog(
            isTrackChangesEnabled = document.isTrackChangesEnabled,
            trackedChanges = document.trackedChanges,
            onDismiss = onDismissTrackChangesDialog,
            onToggleTrackChanges = { enabled ->
                viewModel.toggleTrackChanges(enabled)
            },
            onAcceptChange = { changeId ->
                viewModel.acceptChange(changeId)
            },
            onRejectChange = { changeId ->
                viewModel.rejectChange(changeId)
            },
            onAcceptAll = {
                viewModel.acceptAllChanges()
            },
            onRejectAll = {
                viewModel.rejectAllChanges()
            }
        )
    }

    if (showSmartWritingDialog) {
        SmartWritingDialog(
            document = document,
            activeParagraph = activeParagraph,
            onDismiss = onDismissSmartWritingDialog,
            onApplyRewrite = { paragraphId, newText ->
                viewModel.applyAiRewrite(paragraphId, newText)
            },
            onInsertSummary = { summaryText ->
                viewModel.insertAiSummary(summaryText)
            }
        )
    }

    if (showFootnoteDialog) {
        InsertFootnoteDialog(
            defaultReferenceText = activeParagraph?.text?.take(30) ?: "",
            onDismiss = onDismissFootnoteDialog,
            onConfirm = { refText, noteContent ->
                viewModel.insertFootnote(refText, noteContent)
                onDismissFootnoteDialog()
            }
        )
    }

    EditorFormatModals(
        viewModel = viewModel,
        document = document,
        activeParagraph = activeParagraph,
        showPageBordersDialog = showPageBordersDialog,
        onDismissPageBordersDialog = onDismissPageBordersDialog,
        showPageColorDialog = showPageColorDialog,
        onDismissPageColorDialog = onDismissPageColorDialog,
        showTypographyEffectsDialog = showTypographyEffectsDialog,
        onDismissTypographyEffectsDialog = onDismissTypographyEffectsDialog,
        showParagraphSpacingDialog = showParagraphSpacingDialog,
        onDismissParagraphSpacingDialog = onDismissParagraphSpacingDialog,
        showCreateCustomStyleDialog = showCreateCustomStyleDialog,
        onDismissCreateCustomStyleDialog = onDismissCreateCustomStyleDialog,
        selectedCellForProperties = selectedCellForProperties,
        onDismissCellProperties = onDismissCellProperties
    )
}
