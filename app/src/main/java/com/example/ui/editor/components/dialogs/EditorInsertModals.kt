package com.example.ui.editor.components.dialogs

import android.content.Context
import androidx.activity.result.ActivityResultLauncher
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.ImageBlock
import com.example.data.model.RichDocument
import com.example.data.model.TableCell
import com.example.data.model.TableBlock
import com.example.ui.editor.EditorViewModel
import com.example.ui.editor.components.sharePdfFile

@Composable
fun EditorInsertModals(
    viewModel: EditorViewModel,
    document: RichDocument?,
    context: Context,
    actualPageCount: Int,
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
    showOcrScanDialog: Boolean = false,
    onDismissOcrScanDialog: () -> Unit = {},
    safPdfSaveLauncher: ActivityResultLauncher<String>
) {
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

    if (showPageNumberDialog) {
        PageNumberInsertDialog(
            onDismiss = onDismissPageNumber,
            onSelectPreset = { pattern, alignment ->
                viewModel.setPageNumberPreset(pattern, alignment)
                onDismissPageNumber()
            }
        )
    }

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

    if (showOcrScanDialog) {
        com.example.ui.editor.components.OcrScanDialog(
            onDismiss = onDismissOcrScanDialog,
            onInsertScannedText = { text ->
                viewModel.insertScannedText(text)
                onDismissOcrScanDialog()
            }
        )
    }
}
