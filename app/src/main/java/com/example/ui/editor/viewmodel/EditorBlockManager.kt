package com.example.ui.editor.viewmodel

import android.content.Context
import android.net.Uri
import androidx.compose.ui.text.TextRange
import com.example.data.model.ColumnBreakBlock
import com.example.data.model.DividerBlock
import com.example.data.model.DrawingStroke
import com.example.data.model.FootnoteBlock
import com.example.data.model.HeadingStyle
import com.example.data.model.PageBreakBlock
import com.example.data.model.ParagraphBlock
import com.example.data.model.SectionBreakBlock
import com.example.data.model.ShapeBlock
import com.example.data.model.TabAlignment
import com.example.data.model.TabStop
import com.example.data.model.TableOfContentsBlock
import com.example.data.model.TextAlignment
import com.example.data.model.TrackedChange
import com.example.editor.engine.DeleteBlockCommand
import com.example.editor.engine.InsertBlockCommand
import com.example.ui.editor.EditorViewModel

/**
 * Manages insertion, configuration, and deletion of document blocks,
 * delegating media and vector figures to [EditorMediaBlockManager].
 */
class EditorBlockManager(
    private val vm: EditorViewModel
) {
    internal val mediaManager = EditorMediaBlockManager(vm)

    fun insertParagraph() {
        val currentDoc = vm.currentDocument ?: return
        val activeIdx = vm.activeBlockIndex.value.coerceIn(0, (currentDoc.blocks.size - 1).coerceAtLeast(0))
        val block = currentDoc.blocks.getOrNull(activeIdx) as? ParagraphBlock
        if (block != null) {
            val sel = vm.selection.value
            val cursor = sel.start.coerceIn(0, block.text.length)
            vm.splitParagraph(activeIdx, cursor)
        } else {
            val newBlock = ParagraphBlock(
                alignment = if (vm.defaultRtl.value) TextAlignment.END else TextAlignment.START,
                isRtl = vm.defaultRtl.value
            )
            val newBlocks = currentDoc.blocks.toMutableList()
            newBlocks.add(activeIdx + 1, newBlock)
            vm.updateDocumentInternal(currentDoc.copy(blocks = newBlocks, updatedAt = System.currentTimeMillis()))
            vm.setActiveBlock(activeIdx + 1)
            vm.updateSelection(TextRange.Zero)
            vm.historyManager.scheduleDocumentAutosave()
        }
    }

    fun insertDivider() {
        val insertIndex = vm.activeBlockIndex.value + 1
        vm.executeCommand(InsertBlockCommand(index = insertIndex, block = DividerBlock()))
        vm.setActiveBlock(insertIndex)
    }

    fun insertPageBreak() {
        val insertIndex = vm.activeBlockIndex.value + 1
        vm.executeCommand(InsertBlockCommand(index = insertIndex, block = PageBreakBlock()))
        vm.setActiveBlock(insertIndex)
    }

    fun insertColumnBreak() {
        val insertIndex = vm.activeBlockIndex.value + 1
        vm.executeCommand(InsertBlockCommand(index = insertIndex, block = ColumnBreakBlock()))
        vm.setActiveBlock(insertIndex)
    }

    fun insertSectionBreak(orientation: String = "PORTRAIT", isNextPage: Boolean = true, sectionName: String = "Section") {
        val insertIndex = vm.activeBlockIndex.value + 1
        val sectionBlock = SectionBreakBlock(
            sectionName = sectionName,
            isNextPage = isNextPage,
            orientation = orientation
        )
        vm.executeCommand(InsertBlockCommand(index = insertIndex, block = sectionBlock))
        vm.setActiveBlock(insertIndex)
    }

    fun addTabStop(positionPt: Float, alignment: TabAlignment = TabAlignment.LEFT) {
        val currentDoc = vm.currentDocument ?: return
        val block = currentDoc.blocks.getOrNull(vm.activeBlockIndex.value) as? ParagraphBlock ?: return
        val newTab = TabStop(positionPt = positionPt, alignment = alignment)
        val updated = block.copy(tabStops = (block.tabStops + newTab).sortedBy { it.positionPt })
        val updatedBlocks = currentDoc.blocks.map { if (it.id == block.id) updated else it }
        val updatedDoc = currentDoc.copy(blocks = updatedBlocks, updatedAt = System.currentTimeMillis())
        vm.executeCommand(object : com.example.editor.engine.EditorCommand {
            override val description: String = "Add Tab Stop"
            override fun apply(document: com.example.data.model.RichDocument) = updatedDoc
            override fun revert(document: com.example.data.model.RichDocument) = currentDoc
        })
    }

    fun insertDateTimeStamp(formattedDate: String) {
        val insertIndex = vm.activeBlockIndex.value + 1
        val dateBlock = ParagraphBlock(text = formattedDate, headingStyle = HeadingStyle.SUBTITLE)
        vm.executeCommand(InsertBlockCommand(index = insertIndex, block = dateBlock))
        vm.setActiveBlock(insertIndex)
    }

    fun deleteCurrentBlock() {
        val currentDoc = vm.currentDocument ?: return
        if (currentDoc.blocks.size <= 1) return
        val index = vm.activeBlockIndex.value.coerceIn(0, currentDoc.blocks.size - 1)
        val block = currentDoc.blocks[index]
        vm.executeCommand(DeleteBlockCommand(index = index, deletedBlock = block))
        vm.setActiveBlock((index - 1).coerceAtLeast(0))
    }

    fun deleteBlockById(blockId: String) {
        val currentDoc = vm.currentDocument ?: return
        if (currentDoc.blocks.size <= 1) return
        val index = currentDoc.blocks.indexOfFirst { it.id == blockId }
        if (index != -1) {
            val block = currentDoc.blocks[index]
            vm.executeCommand(DeleteBlockCommand(index = index, deletedBlock = block))
            vm.setActiveBlock((index - 1).coerceAtLeast(0))
        }
    }

    fun insertTableOfContents() {
        val insertIndex = vm.activeBlockIndex.value.coerceIn(0, (vm.currentDocument?.blocks?.size ?: 0))
        vm.executeCommand(InsertBlockCommand(insertIndex, TableOfContentsBlock()))
        vm.showStatusMessage("Table of Contents inserted")
    }

    fun insertScannedOcrText(scannedText: String) {
        if (scannedText.isBlank()) return
        val insertIndex = (vm.activeBlockIndex.value + 1).coerceIn(0, (vm.currentDocument?.blocks?.size ?: 0))
        val paragraphs = scannedText.split("\n\n").filter { it.isNotBlank() }
        for ((idx, para) in paragraphs.withIndex()) {
            vm.executeCommand(InsertBlockCommand(insertIndex + idx, ParagraphBlock(text = para.trim())))
        }
        vm.showStatusMessage("OCR Scanned Text inserted (${paragraphs.size} paragraphs)")
    }

    fun insertFootnote(referenceText: String, noteContent: String) {
        val doc = vm.currentDocument ?: return
        val currentNotes = doc.blocks.filterIsInstance<FootnoteBlock>()
        val nextNoteNumber = currentNotes.size + 1
        val activeIndex = vm.activeBlockIndex.value
        val activeBlock = doc.blocks.getOrNull(activeIndex)

        if (activeBlock is ParagraphBlock) {
            val superscriptMark = when (nextNoteNumber) {
                1 -> "¹"
                2 -> "²"
                3 -> "³"
                4 -> "⁴"
                5 -> "⁵"
                else -> "[$nextNoteNumber]"
            }
            val updatedText = if (referenceText.isNotBlank() && activeBlock.text.contains(referenceText)) {
                activeBlock.text.replaceFirst(referenceText, "$referenceText$superscriptMark")
            } else {
                if (activeBlock.text.isBlank()) superscriptMark else "${activeBlock.text} $superscriptMark"
            }
            vm.updateParagraphText(activeIndex, updatedText)
        }

        val insertIndex = (activeIndex + 1).coerceIn(0, doc.blocks.size)
        val footnote = FootnoteBlock(
            noteNumber = nextNoteNumber,
            referenceText = referenceText.ifBlank { "Citation $nextNoteNumber" },
            noteContent = noteContent
        )
        vm.executeCommand(InsertBlockCommand(insertIndex, footnote))
        renumberFootnotes()
        vm.showStatusMessage("Footnote [$nextNoteNumber] linked & inserted")
    }

    fun renumberFootnotes() {
        val doc = vm.currentDocument ?: return
        var counter = 1
        val updatedBlocks = doc.blocks.map { block ->
            if (block is FootnoteBlock) {
                block.copy(noteNumber = counter++)
            } else block
        }
        vm.updateDocumentInternal(doc.copy(blocks = updatedBlocks))
    }

    fun refreshTableOfContents() {
        val doc = vm.currentDocument ?: return
        val hasToc = doc.blocks.any { it is TableOfContentsBlock }
        if (hasToc) {
            vm.updateDocumentInternal(doc.copy(updatedAt = System.currentTimeMillis()))
            vm.showStatusMessage("Table of Contents synchronized")
        }
    }

    fun applyAiRewrite(paragraphId: String, newText: String) {
        val doc = vm.currentDocument ?: return
        val blockIndex = doc.blocks.indexOfFirst { it.id == paragraphId }
        if (blockIndex == -1) return
        val oldBlock = doc.blocks[blockIndex] as? ParagraphBlock ?: return
        if (doc.isTrackChangesEnabled) {
            val record = TrackedChange(
                blockId = paragraphId,
                originalText = oldBlock.text,
                updatedText = newText,
                author = "AI Assistant",
                changeType = "POLISH"
            )
            vm.updateDocumentInternal(doc.copy(trackedChanges = doc.trackedChanges + record))
        }
        vm.updateParagraphText(blockIndex, newText)
        vm.showStatusMessage("Paragraph polished")
    }

    fun insertAiSummary(summaryText: String) {
        val insertIndex = (vm.activeBlockIndex.value + 1).coerceIn(0, (vm.currentDocument?.blocks?.size ?: 0))
        val summaryBlock = ShapeBlock(
            shapeType = "NOTE",
            title = "Executive Summary",
            content = summaryText,
            backgroundColorHex = "#EFF6FF",
            borderColorHex = "#3B82F6"
        )
        vm.executeCommand(InsertBlockCommand(insertIndex, summaryBlock))
        vm.showStatusMessage("Summary box inserted")
    }

    // Media Manager Delegations
    fun insertImageFromUri(context: Context, sourceUri: Uri, caption: String = "") = mediaManager.insertImageFromUri(context, sourceUri, caption)
    fun insertImage(uri: String, caption: String = "") = mediaManager.insertImage(uri, caption)
    fun updateImageProperties(blockId: String, widthDp: Int? = null, heightDp: Int? = null, alignment: TextAlignment? = null, caption: String? = null, aspectRatio: Float? = null, wrapMode: String? = null, filterMode: String? = null, cornerRadiusDp: Int? = null) =
        mediaManager.updateImageProperties(blockId, widthDp, heightDp, alignment, caption, aspectRatio, wrapMode, filterMode, cornerRadiusDp)
    fun updateImageWrapMode(blockId: String, wrapMode: String) = mediaManager.updateImageWrapMode(blockId, wrapMode)
    fun updateImageFormatting(blockId: String, filterMode: String, cornerRadiusDp: Int, borderThicknessDp: Int, borderColorHex: String, rotationDegrees: Float) =
        mediaManager.updateImageFormatting(blockId, filterMode, cornerRadiusDp, borderThicknessDp, borderColorHex, rotationDegrees)
    fun insertSignature(signerName: String, signerTitle: String, strokeHex: String, points: String) = mediaManager.insertSignature(signerName, signerTitle, strokeHex, points)
    fun insertShape(shapeType: String, title: String, content: String, bgHex: String, borderHex: String) = mediaManager.insertShape(shapeType, title, content, bgHex, borderHex)
    fun insertVectorShape(shapeType: String, title: String, content: String, bgHex: String, borderHex: String, strokeWidthPt: Float = 1.5f, rotationDeg: Float = 0f, widthDp: Int = 220, heightDp: Int = 110) =
        mediaManager.insertVectorShape(shapeType, title, content, bgHex, borderHex, strokeWidthPt, rotationDeg, widthDp, heightDp)
    fun updateShape(blockId: String, shapeType: String, title: String, content: String, bgHex: String, borderHex: String, strokeWidthPt: Float = 1.5f, rotationDeg: Float = 0f, widthDp: Int = 220, heightDp: Int = 110) =
        mediaManager.updateShape(blockId, shapeType, title, content, bgHex, borderHex, strokeWidthPt, rotationDeg, widthDp, heightDp)
    fun insertDrawing(heightDp: Int = 180) = mediaManager.insertDrawing(heightDp)
    fun updateDrawingStrokes(blockId: String, strokes: List<DrawingStroke>) = mediaManager.updateDrawingStrokes(blockId, strokes)
    fun insertTextBox(text: String = "Type text here...") = mediaManager.insertTextBox(text)
    fun updateTextBox(blockId: String, text: String, widthDp: Int = 180, heightDp: Int = 100, bgHex: String = "#FFFFFF", borderHex: String = "#2563EB") =
        mediaManager.updateTextBox(blockId, text, widthDp, heightDp, bgHex, borderHex)
    fun updateImagePositionAndSize(blockId: String, offsetX: Float, offsetY: Float, widthDp: Int, heightDp: Int) =
        mediaManager.updateImagePositionAndSize(blockId, offsetX, offsetY, widthDp, heightDp)
    fun updateTextBoxGeometry(blockId: String, offsetX: Float, offsetY: Float, widthDp: Int, heightDp: Int) =
        mediaManager.updateTextBoxGeometry(blockId, offsetX, offsetY, widthDp, heightDp)
}
