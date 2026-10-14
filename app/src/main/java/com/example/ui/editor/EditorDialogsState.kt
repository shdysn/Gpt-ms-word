package com.example.ui.editor

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.data.model.ImageBlock
import com.example.data.model.TableBlock

class EditorDialogsState {
    var showTitleEditDialog by mutableStateOf(false)
    var showSaveOptionsDialog by mutableStateOf(false)
    var showExportOptionsDialog by mutableStateOf(false)
    var showSaveAsDialog by mutableStateOf(false)
    var showDiscardDialog by mutableStateOf(false)
    var showTableDialog by mutableStateOf(false)
    var showLinkDialog by mutableStateOf(false)
    var showLayoutDialog by mutableStateOf(false)
    var showExportPdfDialog by mutableStateOf(false)
    var showStatsDialog by mutableStateOf(false)
    var showHeaderFooterDialog by mutableStateOf(false)
    var showPageNumberDialog by mutableStateOf(false)
    var showSignatureDialog by mutableStateOf(false)
    var showShapeDialog by mutableStateOf(false)
    var showWatermarkDialog by mutableStateOf(false)
    var showPasswordDialog by mutableStateOf(false)
    var showVersionHistoryDialog by mutableStateOf(false)
    var showCommentsDialog by mutableStateOf(false)
    var showTrackChangesDialog by mutableStateOf(false)
    var showSmartWritingDialog by mutableStateOf(false)
    var showFootnoteDialog by mutableStateOf(false)
    var showPageBordersDialog by mutableStateOf(false)
    var showPageColorDialog by mutableStateOf(false)
    var selectedTableForProperties by mutableStateOf<TableBlock?>(null)
    var selectedCellForEdit by mutableStateOf<Triple<TableBlock, Int, Int>?>(null)
    var selectedCellForProperties by mutableStateOf<Triple<TableBlock, Int, Int>?>(null)
    var selectedImageForProperties by mutableStateOf<ImageBlock?>(null)
    var showTypographyEffectsDialog by mutableStateOf(false)
    var showParagraphSpacingDialog by mutableStateOf(false)
    var showCreateCustomStyleDialog by mutableStateOf(false)
    var showOutlineSheet by mutableStateOf(false)
}

@Composable
fun rememberEditorDialogsState(): EditorDialogsState {
    return remember { EditorDialogsState() }
}
