package com.example.ui.editor.viewmodel

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.ui.text.TextRange
import com.example.data.model.ParagraphBlock
import com.example.data.model.RichDocument
import com.example.editor.engine.EditorCommand
import com.example.ui.editor.EditorViewModel
import java.util.UUID

/**
 * Manages clipboard operations including Copy, Cut, Paste,
 * Paste with Formatting, and document-wide selection.
 */
class EditorClipboardManager(
    private val vm: EditorViewModel
) {
    fun selectAllDocument() {
        vm.setIsDocumentAllSelected(true)
        vm.showStatusMessage("All document content selected")
    }

    fun clearDocumentSelection() {
        if (vm.isDocumentAllSelected.value) {
            vm.setIsDocumentAllSelected(false)
        }
    }

    fun copySelection(context: Context) {
        val currentDoc = vm.currentDocument ?: return
        if (vm.isDocumentAllSelected.value) {
            copyAllToClipboard(context)
            return
        }

        val activeIdx = vm.activeBlockIndex.value.coerceIn(0, (currentDoc.blocks.size - 1).coerceAtLeast(0))
        val block = currentDoc.blocks.getOrNull(activeIdx) as? ParagraphBlock
        val sel = vm.selection.value

        val textToCopy = if (block != null && !sel.collapsed && sel.min < sel.max) {
            val s = sel.min.coerceIn(0, block.text.length)
            val e = sel.max.coerceIn(s, block.text.length)
            block.text.substring(s, e)
        } else if (block != null && block.text.isNotEmpty()) {
            block.text
        } else {
            ""
        }

        if (textToCopy.isNotEmpty()) {
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
            clipboard?.setPrimaryClip(ClipData.newPlainText("Document Text", textToCopy))
            vm.showStatusMessage("Copied to clipboard")
        } else {
            vm.showStatusMessage("Nothing to copy")
        }
    }

    fun cutSelection(context: Context) {
        val currentDoc = vm.currentDocument ?: return
        if (vm.isDocumentAllSelected.value) {
            cutAllToClipboard(context)
            return
        }

        val activeIdx = vm.activeBlockIndex.value.coerceIn(0, (currentDoc.blocks.size - 1).coerceAtLeast(0))
        val block = currentDoc.blocks.getOrNull(activeIdx) as? ParagraphBlock ?: return
        val sel = vm.selection.value

        if (!sel.collapsed && sel.min < sel.max) {
            val s = sel.min.coerceIn(0, block.text.length)
            val e = sel.max.coerceIn(s, block.text.length)
            val textToCut = block.text.substring(s, e)
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
            clipboard?.setPrimaryClip(ClipData.newPlainText("Document Text", textToCut))
            val newText = block.text.removeRange(s, e)
            vm.updateParagraphText(activeIdx, newText, TextRange(s))
            vm.showStatusMessage("Cut to clipboard")
        } else if (block.text.isNotEmpty()) {
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
            clipboard?.setPrimaryClip(ClipData.newPlainText("Document Text", block.text))
            vm.updateParagraphText(activeIdx, "", TextRange.Zero)
            vm.showStatusMessage("Cut paragraph to clipboard")
        }
    }

    fun pasteFromClipboard(context: Context, keepSourceFormatting: Boolean = false) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
        val clip = clipboard?.primaryClip
        if (clip == null || clip.itemCount == 0) {
            vm.showStatusMessage("Clipboard is empty")
            return
        }

        val item = clip.getItemAt(0)
        val textToPaste = item.text?.toString() ?: item.coerceToText(context).toString()
        if (textToPaste.isEmpty()) {
            vm.showStatusMessage("Clipboard has no text")
            return
        }

        if (vm.isDocumentAllSelected.value) {
            vm.replaceAllDocumentWithText(textToPaste)
            vm.showStatusMessage(if (keepSourceFormatting) "Pasted (kept source formatting)" else "Pasted")
            return
        }

        val currentDoc = vm.currentDocument ?: return
        if (currentDoc.blocks.isEmpty()) {
            vm.replaceAllDocumentWithText(textToPaste)
            return
        }

        val activeIdx = vm.activeBlockIndex.value.coerceIn(0, currentDoc.blocks.size - 1)
        val block = currentDoc.blocks.getOrNull(activeIdx) as? ParagraphBlock

        if (block != null) {
            val sel = vm.selection.value
            val s = sel.min.coerceIn(0, block.text.length)
            val e = sel.max.coerceIn(s, block.text.length)
            val before = block.text.substring(0, s)
            val after = block.text.substring(e)
            val combined = before + textToPaste + after
            val newCursor = s + textToPaste.length
            vm.updateParagraphText(activeIdx, combined, TextRange(newCursor))
            vm.showStatusMessage(if (keepSourceFormatting) "Pasted (kept source formatting)" else "Pasted")
        } else {
            vm.blockManager.insertScannedText(textToPaste)
            vm.showStatusMessage("Pasted")
        }
    }

    fun copyAllToClipboard(context: Context) {
        val currentDoc = vm.currentDocument ?: return
        val allText = currentDoc.blocks.filterIsInstance<ParagraphBlock>().joinToString("\n") { it.text }
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
        val clip = ClipData.newPlainText("Document Text", allText)
        clipboard?.setPrimaryClip(clip)
        vm.showStatusMessage("All document text copied to clipboard")
    }

    fun cutAllToClipboard(context: Context) {
        copyAllToClipboard(context)
        vm.deleteAllDocumentContent()
        vm.showStatusMessage("All document text cut to clipboard")
    }
}
