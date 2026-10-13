package com.example.ui.editor.components

import android.content.Context
import androidx.activity.result.ActivityResultLauncher
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.ImageBlock
import com.example.data.model.ParagraphBlock
import com.example.data.model.RichDocument
import com.example.data.model.TableCell
import com.example.data.model.TableBlock
import com.example.ui.editor.EditorViewModel
import com.example.ui.theme.WordPrimary
import kotlinx.coroutines.launch

/**
 * Host component for all Editor Dialogs and Modals.
 * Decouples modal orchestration and lifecycle from the main canvas layout.
 */
@Composable
fun EditorDialogsHost(
    viewModel: EditorViewModel,
    document: RichDocument?,
    context: Context,
    actualPageCount: Int,
    activeParagraph: ParagraphBlock?,
    showTitleEditDialog: Boolean,
    onDismissTitleEdit: () -> Unit,
    showSaveOptionsDialog: Boolean,
    onDismissSaveOptions: () -> Unit,
    onOpenSaveAs: () -> Unit,
    showExportOptionsDialog: Boolean,
    onDismissExportOptions: () -> Unit,
    onOpenExportPdf: () -> Unit,
    showSaveAsDialog: Boolean,
    onDismissSaveAs: () -> Unit,
    showDiscardDialog: Boolean,
    onDismissDiscard: () -> Unit,
    onNavigateBack: () -> Unit,
    showTableDialog: Boolean,
    onDismissTableDialog: () -> Unit,
    selectedTableForProperties: TableBlock?,
    onDismissTableProperties: () -> Unit,
    selectedCellForEdit: Triple<TableBlock, Int, Int>?,
    onDismissCellEdit: () -> Unit,
    selectedImageForProperties: ImageBlock?,
    onDismissImageProperties: () -> Unit,
    showHeaderFooterDialog: Boolean,
    onDismissHeaderFooter: () -> Unit,
    showPageNumberDialog: Boolean,
    onDismissPageNumber: () -> Unit,
    showLinkDialog: Boolean,
    onDismissLinkDialog: () -> Unit,
    showLayoutDialog: Boolean,
    onDismissLayoutDialog: () -> Unit,
    showExportPdfDialog: Boolean,
    onDismissExportPdf: () -> Unit,
    onOpenPageSetupFromPdf: () -> Unit,
    showStatsDialog: Boolean,
    onDismissStatsDialog: () -> Unit,
    isDocumentLocked: Boolean,
    showSignatureDialog: Boolean,
    onDismissSignatureDialog: () -> Unit,
    showShapeDialog: Boolean,
    onDismissShapeDialog: () -> Unit,
    showWatermarkDialog: Boolean,
    onDismissWatermarkDialog: () -> Unit,
    showPasswordDialog: Boolean,
    onDismissPasswordDialog: () -> Unit,
    showVersionHistoryDialog: Boolean,
    onDismissVersionHistoryDialog: () -> Unit,
    showCommentsDialog: Boolean = false,
    onDismissCommentsDialog: () -> Unit = {},
    showTrackChangesDialog: Boolean = false,
    onDismissTrackChangesDialog: () -> Unit = {},
    showSmartWritingDialog: Boolean = false,
    onDismissSmartWritingDialog: () -> Unit = {},
    showFootnoteDialog: Boolean = false,
    onDismissFootnoteDialog: () -> Unit = {},
    showPageBordersDialog: Boolean = false,
    onDismissPageBordersDialog: () -> Unit = {},
    showPageColorDialog: Boolean = false,
    onDismissPageColorDialog: () -> Unit = {},
    safDocxSaveLauncher: ActivityResultLauncher<String>,
    safPdfSaveLauncher: ActivityResultLauncher<String>
) {
    val coroutineScope = rememberCoroutineScope()

    // Title Edit Dialog
    if (showTitleEditDialog && document != null) {
        EditTitleDialog(
            initialTitle = document.title,
            onDismiss = onDismissTitleEdit,
            onConfirm = { newTitle ->
                viewModel.updateTitle(newTitle)
                onDismissTitleEdit()
            }
        )
    }

    // Save Options Dialog
    if (showSaveOptionsDialog) {
        SaveOptionsDialog(
            onDismiss = onDismissSaveOptions,
            onQuickSave = {
                onDismissSaveOptions()
                viewModel.saveNow()
            },
            onSaveAs = {
                onDismissSaveOptions()
                onOpenSaveAs()
            },
            onSaveSafDocx = {
                onDismissSaveOptions()
                val safeTitle = (document?.title ?: "document").replace("[^a-zA-Z0-9.-]".toRegex(), "_")
                safDocxSaveLauncher.launch("$safeTitle.docx")
            }
        )
    }

    // Export Options Dialog
    if (showExportOptionsDialog) {
        ExportOptionsDialog(
            onDismiss = onDismissExportOptions,
            onExportPdf = {
                onDismissExportOptions()
                onOpenExportPdf()
            },
            onExportDocx = {
                onDismissExportOptions()
                viewModel.exportToDocx(context) { file ->
                    file?.let { shareDocxFile(context, it) }
                }
            },
            onExportPlainText = {
                onDismissExportOptions()
                viewModel.exportToPlainText(context) { file ->
                    file?.let { shareTextFile(context, it) }
                }
            }
        )
    }

    // Save As Dialog
    if (showSaveAsDialog) {
        SaveAsDialog(
            initialTitle = "${document?.title ?: "Document"} (Copy)",
            onDismiss = onDismissSaveAs,
            onConfirm = { copyTitle ->
                viewModel.saveAs(copyTitle) {
                    onDismissSaveAs()
                }
            }
        )
    }

    // Unsaved Changes Discard Warning Dialog
    if (showDiscardDialog) {
        DiscardChangesDialog(
            docTitle = document?.title ?: "this document",
            onDismiss = onDismissDiscard,
            onSaveAndExit = {
                coroutineScope.launch {
                    viewModel.flushSave()
                    onDismissDiscard()
                    onNavigateBack()
                }
            },
            onDiscard = {
                viewModel.dismissRecoveryDraft()
                onDismissDiscard()
                onNavigateBack()
            }
        )
    }

    // Insert Table Dialog
    if (showTableDialog) {
        InsertTableDialog(
            onDismiss = onDismissTableDialog,
            onConfirm = { r, c, hasBorders, widthPercent ->
                viewModel.insertTable(
                    rows = r,
                    cols = c,
                    hasBorders = hasBorders,
                    tableWidthPercent = widthPercent
                )
                onDismissTableDialog()
            }
        )
    }

    // Table Properties Dialog
    selectedTableForProperties?.let { table ->
        TablePropertiesDialog(
            table = table,
            onDismiss = onDismissTableProperties,
            onAddRow = { viewModel.addTableRow(table.id) },
            onDeleteRow = { viewModel.deleteTableRow(table.id, table.rowCount - 1) },
            onAddCol = { viewModel.addTableColumn(table.id) },
            onDeleteCol = { viewModel.deleteTableColumn(table.id, table.columnCount - 1) },
            onUpdateProperties = { hasBorders, borderColorHex, widthPercent, alignment ->
                viewModel.updateTableProperties(
                    blockId = table.id,
                    hasBorders = hasBorders,
                    borderColorHex = borderColorHex,
                    tableWidthPercent = widthPercent,
                    alignment = alignment
                )
            },
            onDeleteTable = {
                viewModel.deleteBlockById(table.id)
            }
        )
    }

    // Table Cell Edit Dialog
    selectedCellForEdit?.let { (table, row, col) ->
        val cell = table.cells.getOrNull(row)?.getOrNull(col) ?: TableCell()
        TableCellEditDialog(
            cell = cell,
            row = row,
            col = col,
            onDismiss = onDismissCellEdit,
            onConfirm = { text, isBold, isItalic, bgHex, alignment ->
                viewModel.updateTableCell(table.id, row, col, text)
                viewModel.updateTableCellFormat(
                    blockId = table.id,
                    row = row,
                    col = col,
                    isBold = isBold,
                    isItalic = isItalic,
                    backgroundColorHex = bgHex,
                    alignment = alignment
                )
                onDismissCellEdit()
            }
        )
    }

    // Image Properties Dialog
    selectedImageForProperties?.let { img ->
        ImagePropertiesDialog(
            imageBlock = img,
            onDismiss = onDismissImageProperties,
            onConfirm = { widthDp, heightDp, alignment, caption, ratio, wrapMode, filterMode, cornerRadius ->
                viewModel.updateImageProperties(
                    blockId = img.id,
                    widthDp = widthDp,
                    heightDp = heightDp,
                    alignment = alignment,
                    caption = caption,
                    aspectRatio = ratio,
                    wrapMode = wrapMode,
                    filterMode = filterMode,
                    cornerRadiusDp = cornerRadius
                )
            },
            onDeleteImage = {
                viewModel.deleteBlockById(img.id)
            }
        )
    }

    // Header & Footer Dialog
    if (showHeaderFooterDialog && document != null) {
        val layout = document.layoutConfig
        HeaderFooterEditDialog(
            currentHeader = layout.headerText,
            currentFooter = layout.footerText,
            showHeader = layout.showHeader,
            showFooter = layout.showFooter,
            onDismiss = onDismissHeaderFooter,
            onConfirm = { headerText, footerText, showHeader, showFooter ->
                viewModel.setHeaderFooter(headerText, footerText, showHeader, showFooter)
                onDismissHeaderFooter()
            }
        )
    }

    // Page Number Insert Dialog
    if (showPageNumberDialog) {
        PageNumberInsertDialog(
            onDismiss = onDismissPageNumber,
            onSelectPreset = { pattern, alignment ->
                viewModel.setPageNumberPreset(pattern, alignment)
                onDismissPageNumber()
            }
        )
    }

    // Insert Hyperlink Dialog
    if (showLinkDialog) {
        val activeFormatting by viewModel.activeFormatting.collectAsStateWithLifecycle()
        InsertHyperlinkDialog(
            initialUrl = activeFormatting.linkUrl ?: "https://",
            onDismiss = onDismissLinkDialog,
            onConfirm = { url ->
                viewModel.setHyperlink(url)
                onDismissLinkDialog()
            },
            onRemoveLink = {
                viewModel.setHyperlink(null)
                onDismissLinkDialog()
            }
        )
    }

    // Page Layout Dialog
    if (showLayoutDialog && document != null) {
        PageLayoutDialog(
            currentLayout = document.layoutConfig,
            onDismiss = onDismissLayoutDialog,
            onSaveConfig = { newConfig ->
                viewModel.updatePageLayoutConfig(newConfig)
                onDismissLayoutDialog()
            }
        )
    }

    // Export PDF Dialog
    if (showExportPdfDialog && document != null) {
        ExportPdfDialog(
            document = document,
            actualPageCount = actualPageCount,
            onDismiss = onDismissExportPdf,
            onSaveToStorage = {
                val safeTitle = (document.title).replace("[^a-zA-Z0-9.-]".toRegex(), "_")
                safPdfSaveLauncher.launch("$safeTitle.pdf")
            },
            onSharePdf = {
                viewModel.exportToPdf(context) { file ->
                    file?.let { sharePdfFile(context, it) }
                }
            },
            onPrint = {
                viewModel.printDocument(context)
            },
            onOpenPageSetup = onOpenPageSetupFromPdf
        )
    }

    // Document Statistics Dialog
    if (showStatsDialog) {
        DocumentStatisticsDialog(
            document = document,
            actualPageCount = actualPageCount,
            activeParagraph = activeParagraph,
            onDismiss = onDismissStatsDialog
        )
    }

    // Document Lock Barrier (Security PIN Lock)
    if (isDocumentLocked) {
        var unlockPasswordInput by remember { mutableStateOf("") }
        var unlockError by remember { mutableStateOf<String?>(null) }

        AlertDialog(
            onDismissRequest = { /* Require valid PIN or back navigation */ },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        tint = WordPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("Document Locked", fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        "This document is protected with a password PIN. Please enter the PIN to view and edit:",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    OutlinedTextField(
                        value = unlockPasswordInput,
                        onValueChange = {
                            unlockPasswordInput = it
                            unlockError = null
                        },
                        label = { Text("Enter PIN / Password") },
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true,
                        isError = unlockError != null,
                        modifier = Modifier.fillMaxWidth().testTag("unlock_pin_input")
                    )
                    unlockError?.let {
                        Text(it, color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val ok = viewModel.unlockDocument(unlockPasswordInput)
                        if (!ok) {
                            unlockError = "Incorrect PIN or Password"
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = WordPrimary),
                    modifier = Modifier.testTag("unlock_confirm_btn")
                ) {
                    Text("Unlock")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = onNavigateBack,
                    modifier = Modifier.testTag("unlock_cancel_btn")
                ) {
                    Text("Back to Home")
                }
            }
        )
    }

    // Digital Signature Dialog
    if (showSignatureDialog) {
        DigitalSignatureDialog(
            onDismiss = onDismissSignatureDialog,
            onConfirm = { signerName, signerTitle, strokeHex, points ->
                viewModel.insertSignature(signerName, signerTitle, strokeHex, points)
                onDismissSignatureDialog()
            }
        )
    }

    // Shape / Callout Box Dialog
    if (showShapeDialog) {
        ShapeInsertDialog(
            onDismiss = onDismissShapeDialog,
            onConfirm = { shapeType, title, content, bgHex, borderHex ->
                viewModel.insertShape(shapeType, title, content, bgHex, borderHex)
                onDismissShapeDialog()
            }
        )
    }

    // Document Watermark Dialog
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

    // Password Security Lock Dialog
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

    // Version History & Snapshots Dialog
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

    // Comments & Annotations Dialog
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

    // Track Changes & Revisions Dialog
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

    // AI Smart Writing Assistant Dialog
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

    // Insert Footnote Dialog
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

    // Page Borders Dialog
    if (showPageBordersDialog && document != null) {
        PageBordersDialog(
            initialStyle = document.layoutConfig.borderStyle,
            initialColorHex = document.layoutConfig.borderColorHex,
            onDismiss = onDismissPageBordersDialog,
            onApply = { style, colorHex ->
                viewModel.setPageBorder(style, colorHex)
            }
        )
    }

    // Page Color Tint Dialog
    if (showPageColorDialog && document != null) {
        PageColorDialog(
            initialColorHex = document.layoutConfig.pageBackgroundHex,
            onDismiss = onDismissPageColorDialog,
            onApply = { colorHex ->
                viewModel.setPageBackgroundColor(colorHex)
            }
        )
    }
}
