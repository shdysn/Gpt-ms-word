package com.example.ui.editor

import android.content.Context
import android.net.Uri
import android.print.PrintDocumentAdapter
import androidx.compose.ui.text.TextRange
import androidx.lifecycle.ViewModel
import com.example.core.layout.MarginPreset
import com.example.core.layout.PageBorderStyle
import com.example.core.layout.PageLayoutConfig
import com.example.core.layout.PageOrientation
import com.example.core.layout.PageSize
import com.example.data.model.TextAlignment
import com.example.data.local.PreferencesManager
import com.example.data.model.BlockElement
import com.example.data.model.CustomStyle
import com.example.data.model.DrawingStroke
import com.example.data.model.HeadingStyle
import com.example.data.model.ListType
import com.example.data.model.RichDocument
import com.example.data.model.TabAlignment
import com.example.data.repository.DocumentRepository
import com.example.editor.engine.ActiveFormatting
import com.example.editor.engine.CaseType
import com.example.editor.engine.EditorCommand
import com.example.ui.editor.viewmodel.EditorBlockManager
import com.example.ui.editor.viewmodel.EditorBlockUtils
import com.example.ui.editor.viewmodel.EditorExportManager
import com.example.ui.editor.viewmodel.EditorFormattingManager
import com.example.ui.editor.viewmodel.EditorHistoryManager
import com.example.ui.editor.viewmodel.EditorReviewAndSecurityManager
import com.example.ui.editor.viewmodel.EditorTableManager
import com.example.ui.editor.viewmodel.EditorTextCanvasManager
import java.io.File

/**
 * Base coordinator hosting domain managers and forwarding editor actions.
 */
abstract class EditorCoordinator(
    val docId: String,
    val repository: DocumentRepository,
    val preferencesManager: PreferencesManager
) : ViewModel() {

    internal abstract val selfViewModel: EditorViewModel

    internal val historyManager by lazy { EditorHistoryManager(selfViewModel) }
    internal val formattingManager by lazy { EditorFormattingManager(selfViewModel) }
    internal val tableManager by lazy { EditorTableManager(selfViewModel) }
    internal val textCanvasManager by lazy { EditorTextCanvasManager(selfViewModel) }
    internal val blockManager by lazy { EditorBlockManager(selfViewModel) }
    internal val exportManager by lazy { EditorExportManager(selfViewModel) }
    internal val reviewManager by lazy { EditorReviewAndSecurityManager(selfViewModel) }

    fun executeCommand(command: EditorCommand) = historyManager.executeCommand(command)
    fun undo() = historyManager.undo()
    fun redo() = historyManager.redo()
    fun saveNow() = historyManager.saveNow()
    suspend fun flushSave() = historyManager.flushSave()
    fun onAppBackgrounded() = historyManager.onAppBackgrounded()
    fun restoreRecoveryDraft() = historyManager.restoreRecoveryDraft()
    fun dismissRecoveryDraft() = historyManager.dismissRecoveryDraft()
    fun createNamedSnapshot(label: String = "Manual Snapshot") = historyManager.createNamedSnapshot(label)
    fun restoreRevisionSnapshot(snapshotId: String) = historyManager.restoreRevisionSnapshot(snapshotId)

    fun findActiveParagraphBlockIndex(blocks: List<BlockElement>, offset: Int) = EditorBlockUtils.findActiveParagraphBlockIndex(blocks, offset)
    fun findParagraphBlockIndicesForRange(blocks: List<BlockElement>, start: Int, end: Int) = EditorBlockUtils.findParagraphBlockIndicesForRange(blocks, start, end)

    fun updateParagraphSubSliceText(index: Int, start: Int, end: Int, text: String, sel: TextRange) = textCanvasManager.updateParagraphSubSliceText(index, start, end, text, sel)
    fun updateParagraphText(index: Int, text: String, sel: TextRange = TextRange(text.length)) = textCanvasManager.updateParagraphText(index, text, sel)
    fun splitParagraph(index: Int, offset: Int) = textCanvasManager.splitParagraph(index, offset)
    fun updateUnifiedDocumentText(text: String, sel: TextRange = TextRange(text.length)) = textCanvasManager.updateUnifiedDocumentText(text, sel)
    fun deleteOrMergeWithPrevious(index: Int) = textCanvasManager.deleteOrMergeWithPrevious(index)
    fun appendVoiceTypingText(spokenText: String) = textCanvasManager.appendVoiceTypingText(spokenText)

    fun setAlignment(alignment: TextAlignment) = formattingManager.setAlignment(alignment)
    fun setLineSpacing(multiplier: Float) = formattingManager.setLineSpacing(multiplier)
    fun toggleFormatPainter() = formattingManager.toggleFormatPainter()
    fun applyFormatPainterIfActive(targetIndex: Int) = formattingManager.applyFormatPainterIfActive(targetIndex)
    fun setHeadingStyle(heading: HeadingStyle) = formattingManager.setHeadingStyle(heading)
    fun toggleRtl() = formattingManager.toggleRtl()
    fun toggleBold() = formattingManager.toggleBold()
    fun toggleItalic() = formattingManager.toggleItalic()
    fun toggleUnderline() = formattingManager.toggleUnderline()
    fun toggleStrikethrough() = formattingManager.toggleStrikethrough()
    fun toggleSuperscript() = formattingManager.toggleSuperscript()
    fun toggleSubscript() = formattingManager.toggleSubscript()
    fun setTextColor(colorHex: String?) = formattingManager.setTextColor(colorHex)
    fun setHighlightColor(colorHex: String?) = formattingManager.setHighlightColor(colorHex)
    fun setFontFamily(fontName: String) = formattingManager.setFontFamily(fontName)
    fun setFontSize(sizeSp: Float) = formattingManager.setFontSize(sizeSp)
    fun setHyperlink(url: String?) = formattingManager.setHyperlink(url)
    fun toggleListType(targetType: ListType) = formattingManager.toggleListType(targetType)
    fun increaseIndent() = formattingManager.increaseIndent()
    fun decreaseIndent() = formattingManager.decreaseIndent()
    fun clearFormatting() = formattingManager.clearFormatting()
    fun changeCase(caseType: CaseType) = formattingManager.changeCase(caseType)
    fun setLetterSpacing(spacingSp: Float?) = formattingManager.setLetterSpacing(spacingSp)
    fun applyTypographyEffects(letterSpacingSp: Float? = null, uStyle: com.example.data.model.UnderlineStyle = com.example.data.model.UnderlineStyle.NONE, uHex: String? = null, sStyle: com.example.data.model.StrikethroughStyle = com.example.data.model.StrikethroughStyle.NONE, shHex: String? = null, dx: Float? = null, dy: Float? = null, r: Float? = null, outline: Boolean = false) =
        formattingManager.applyTypographyEffects(letterSpacingSp, uStyle, uHex, sStyle, shHex, dx, dy, r, outline)
    fun updateParagraphIndents(fIndent: Float = 0f, hIndent: Float = 0f, sBefore: Float = 0f, sAfter: Float = 0f, dropCapLines: Int = 0) =
        formattingManager.updateParagraphIndents(fIndent, hIndent, sBefore, sAfter, dropCapLines)
    fun toggleChecklist() = formattingManager.toggleChecklist()
    fun toggleChecklistItem(paragraphId: String) = formattingManager.toggleChecklistItem(paragraphId)
    fun setListLevel(level: Int) = formattingManager.setListLevel(level)
    fun saveCustomStyle(style: CustomStyle) = formattingManager.saveCustomStyle(style)
    fun applyCustomStyle(style: CustomStyle) = formattingManager.applyCustomStyle(style)

    fun insertTable(rows: Int, cols: Int, hasBorders: Boolean = true, borderColorHex: String = "#CBD5E1", tableWidthPercent: Int = 100) =
        tableManager.insertTable(rows, cols, hasBorders, borderColorHex, tableWidthPercent)
    fun updateTableCell(blockId: String, row: Int, col: Int, text: String) = tableManager.updateTableCell(blockId, row, col, text)
    fun updateTableCellFormat(blockId: String, row: Int, col: Int, isBold: Boolean? = null, isItalic: Boolean? = null, backgroundColorHex: String? = null, alignment: TextAlignment? = null) =
        tableManager.updateTableCellFormat(blockId, row, col, isBold, isItalic, backgroundColorHex, alignment)
    fun addTableRow(blockId: String, afterRow: Int = -1) = tableManager.addTableRow(blockId, afterRow)
    fun deleteTableRow(blockId: String, rowIndex: Int) = tableManager.deleteTableRow(blockId, rowIndex)
    fun addTableColumn(blockId: String, afterCol: Int = -1) = tableManager.addTableColumn(blockId, afterCol)
    fun deleteTableColumn(blockId: String, colIndex: Int) = tableManager.deleteTableColumn(blockId, colIndex)
    fun updateTableProperties(blockId: String, hasBorders: Boolean? = null, borderColorHex: String? = null, tableWidthPercent: Int? = null, alignment: TextAlignment? = null) =
        tableManager.updateTableProperties(blockId, hasBorders, borderColorHex, tableWidthPercent, alignment)
    fun toggleTableHeaderRow(tableId: String) = tableManager.toggleTableHeaderRow(tableId)
    fun toggleTableZebraStriping(tableId: String) = tableManager.toggleTableZebraStriping(tableId)
    fun setTableCellBackground(tableId: String, row: Int, col: Int, bgHex: String?) = tableManager.setTableCellBackground(tableId, row, col, bgHex)
    fun mergeTableCells(tableId: String, row: Int, col: Int, span: Int = 2) = tableManager.mergeTableCells(tableId, row, col, span)
    fun splitTableCell(tableId: String, row: Int, col: Int) = tableManager.splitTableCell(tableId, row, col)
    fun updateTableCellFormatting(tableId: String, row: Int, col: Int, verticalAlignment: com.example.data.model.CellVerticalAlignment, bgHex: String?) =
        tableManager.updateTableCellFormatting(tableId, row, col, verticalAlignment, bgHex)

    fun insertParagraph() = blockManager.insertParagraph()
    fun insertDivider() = blockManager.insertDivider()
    fun insertPageBreak() = blockManager.insertPageBreak()
    fun insertColumnBreak() = blockManager.insertColumnBreak()
    fun insertSectionBreak(orientation: String = "PORTRAIT", isNextPage: Boolean = true, sectionName: String = "Section") =
        blockManager.insertSectionBreak(orientation, isNextPage, sectionName)
    fun addTabStop(positionPt: Float, alignment: TabAlignment = TabAlignment.LEFT) = blockManager.addTabStop(positionPt, alignment)
    fun insertDateTimeStamp(formattedDate: String) = blockManager.insertDateTimeStamp(formattedDate)
    fun insertImageFromUri(context: Context, sourceUri: Uri, caption: String = "") = blockManager.insertImageFromUri(context, sourceUri, caption)
    fun insertImage(uri: String, caption: String = "") = blockManager.insertImage(uri, caption)
    fun updateImageProperties(blockId: String, widthDp: Int? = null, heightDp: Int? = null, alignment: TextAlignment? = null, caption: String? = null, aspectRatio: Float? = null, wrapMode: String? = null, filterMode: String? = null, cornerRadiusDp: Int? = null) =
        blockManager.updateImageProperties(blockId, widthDp, heightDp, alignment, caption, aspectRatio, wrapMode, filterMode, cornerRadiusDp)
    fun updateImageWrapMode(blockId: String, wrapMode: String) = blockManager.updateImageWrapMode(blockId, wrapMode)
    fun updateImageFormatting(blockId: String, filterMode: String, cornerRadiusDp: Int, borderThicknessDp: Int, borderColorHex: String, rotationDegrees: Float) =
        blockManager.updateImageFormatting(blockId, filterMode, cornerRadiusDp, borderThicknessDp, borderColorHex, rotationDegrees)
    fun deleteCurrentBlock() = blockManager.deleteCurrentBlock()
    fun deleteBlockById(blockId: String) = blockManager.deleteBlockById(blockId)
    fun insertTableOfContents() = blockManager.insertTableOfContents()
    fun insertSignature(signerName: String, signerTitle: String, strokeHex: String, points: String) = blockManager.insertSignature(signerName, signerTitle, strokeHex, points)
    fun insertShape(shapeType: String, title: String, content: String, bgHex: String, borderHex: String) = blockManager.insertShape(shapeType, title, content, bgHex, borderHex)
    fun insertVectorShape(shapeType: String, title: String, content: String, bgHex: String, borderHex: String, strokeWidthPt: Float = 1.5f, widthDp: Int = 220, heightDp: Int = 110) =
        blockManager.insertVectorShape(shapeType, title, content, bgHex, borderHex, strokeWidthPt, widthDp, heightDp)
    fun insertDrawing(heightDp: Int = 180) = blockManager.insertDrawing(heightDp)
    fun updateDrawingStrokes(blockId: String, strokes: List<DrawingStroke>) = blockManager.updateDrawingStrokes(blockId, strokes)
    fun insertTextBox(text: String = "Type text here...") = blockManager.insertTextBox(text)
    fun updateTextBox(blockId: String, text: String, widthDp: Int = 180, heightDp: Int = 100, bgHex: String = "#FFFFFF", borderHex: String = "#2563EB") =
        blockManager.updateTextBox(blockId, text, widthDp, heightDp, bgHex, borderHex)
    fun insertScannedOcrText(scannedText: String) = blockManager.insertScannedOcrText(scannedText)
    fun insertFootnote(referenceText: String, noteContent: String) = blockManager.insertFootnote(referenceText, noteContent)
    fun renumberFootnotes() = blockManager.renumberFootnotes()
    fun refreshTableOfContents() = blockManager.refreshTableOfContents()
    fun applyAiRewrite(paragraphId: String, newText: String) = blockManager.applyAiRewrite(paragraphId, newText)
    fun insertAiSummary(summaryText: String) = blockManager.insertAiSummary(summaryText)

    fun saveAs(newTitle: String, onComplete: (RichDocument?) -> Unit) = exportManager.saveAs(newTitle, onComplete)
    fun renameDocument(newTitle: String) = exportManager.renameDocument(newTitle)
    fun exportToPdf(context: Context, onComplete: (File?) -> Unit) = exportManager.exportToPdf(context, onComplete)
    fun exportToPlainText(context: Context, onComplete: (File?) -> Unit) = exportManager.exportToPlainText(context, onComplete)
    fun exportToDocx(context: Context, onComplete: (File?) -> Unit) = exportManager.exportToDocx(context, onComplete)
    fun exportDocxToSafUri(context: Context, uri: Uri) = exportManager.exportDocxToSafUri(context, uri)
    fun exportPdfToSafUri(context: Context, uri: Uri) = exportManager.exportPdfToSafUri(context, uri)
    fun printDocument(context: Context) = exportManager.printDocument(context)
    fun createPrintDocumentAdapter(context: Context): PrintDocumentAdapter? = exportManager.createPrintDocumentAdapter(context)
    fun exportToMarkdown(context: Context, onComplete: (File?) -> Unit) = exportManager.exportToMarkdown(context, onComplete)
    fun exportToEpub(context: Context, onComplete: (File?) -> Unit) = exportManager.exportToEpub(context, onComplete)

    fun startReadAloud(context: Context) = reviewManager.startReadAloud(context)
    fun stopReadAloud() = reviewManager.stopReadAloud()
    fun setTtsSpeechRate(rate: Float) = reviewManager.setTtsSpeechRate(rate)
    fun cycleSpeechRate() = reviewManager.cycleSpeechRate()
    fun setWatermark(text: String, colorHex: String = "#2594A3B8", angleDeg: Float = -45f) = reviewManager.setWatermark(text, colorHex, angleDeg)
    fun removeWatermark() = reviewManager.removeWatermark()
    fun setColumnCount(columns: Int) = reviewManager.setColumnCount(columns)
    fun setDocumentPassword(password: String) = reviewManager.setDocumentPassword(password)
    fun removeDocumentPassword() = reviewManager.removeDocumentPassword()
    fun verifyPassword(input: String): Boolean = reviewManager.verifyPassword(input)
    fun unlockDocument(password: String): Boolean = reviewManager.unlockDocument(password)
    fun setHeaderFooter(headerText: String? = null, footerText: String? = null, showHeader: Boolean? = null, showFooter: Boolean? = null) =
        reviewManager.setHeaderFooter(headerText, footerText, showHeader, showFooter)
    fun setPageNumberPreset(pattern: String, alignment: TextAlignment) = reviewManager.setPageNumberPreset(pattern, alignment)
    fun updateHeaderFooterPolicy(diffFirst: Boolean? = null, diffOddEven: Boolean? = null, hEven: String? = null, fEven: String? = null) =
        reviewManager.updateHeaderFooterPolicy(diffFirst, diffOddEven, hEven, fEven)
    fun updatePageLayoutConfig(newLayout: PageLayoutConfig) = reviewManager.updatePageLayoutConfig(newLayout)
    fun updatePageLayout(size: PageSize? = null, orientation: PageOrientation? = null, margins: MarginPreset? = null, marginPreset: MarginPreset? = null, top: Float? = null, bottom: Float? = null, left: Float? = null, right: Float? = null) =
        reviewManager.updatePageLayout(size, orientation, margins, marginPreset, top, bottom, left, right)
    fun setPageBorder(style: PageBorderStyle, colorHex: String) = reviewManager.setPageBorder(style, colorHex)
    fun setPageBackgroundColor(bgHex: String) = reviewManager.setPageBackgroundColor(bgHex)
    fun addComment(blockId: String, author: String, text: String) = reviewManager.addComment(blockId, author, text)
    fun resolveComment(commentId: String) = reviewManager.resolveComment(commentId)
    fun deleteComment(commentId: String) = reviewManager.deleteComment(commentId)
    fun toggleTrackChanges(enabled: Boolean? = null) = reviewManager.toggleTrackChanges(enabled)
    fun acceptChange(changeId: String) = reviewManager.acceptChange(changeId)
    fun rejectChange(changeId: String) = reviewManager.rejectChange(changeId)
    fun acceptAllChanges() = reviewManager.acceptAllChanges()
    fun rejectAllChanges() = reviewManager.rejectAllChanges()
    fun toggleFindReplace() = reviewManager.toggleFindReplace()
    fun updateFindQuery(query: String) = reviewManager.updateFindQuery(query)
    fun updateReplaceQuery(query: String) = reviewManager.updateReplaceQuery(query)
    fun replaceAll() = reviewManager.replaceAll()
}
