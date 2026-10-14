package com.example.ui.editor.viewmodel

import com.example.data.model.CustomStyle
import com.example.data.model.HeadingStyle
import com.example.data.model.InlineSpan
import com.example.data.model.ListType
import com.example.data.model.ParagraphBlock
import com.example.data.model.TextAlignment
import com.example.editor.engine.SpanOperations
import com.example.editor.engine.commands.ApplyCustomStyleCommand
import com.example.editor.engine.commands.ChangeAlignmentCommand
import com.example.editor.engine.commands.ChangeHeadingStyleCommand
import com.example.editor.engine.commands.ChangeListTypeCommand
import com.example.editor.engine.commands.ChangeMultipleParagraphsAlignmentCommand
import com.example.editor.engine.commands.SaveCustomStyleCommand
import com.example.editor.engine.commands.SetIndentationCommand
import com.example.editor.engine.commands.SetListLevelCommand
import com.example.editor.engine.commands.ToggleChecklistCommand
import com.example.editor.engine.commands.ToggleChecklistItemCommand
import com.example.editor.engine.commands.ToggleRtlCommand
import com.example.ui.editor.EditorViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Manages paragraph alignments, line spacing, indents, lists, headings, and format painter.
 */
class EditorParagraphFormattingManager(
    private val vm: EditorViewModel
) {
    private val _isFormatPainterActive = MutableStateFlow(false)
    val isFormatPainterActive: StateFlow<Boolean> = _isFormatPainterActive.asStateFlow()

    private var _copiedFormatting: CopiedFormatting? = null

    fun setAlignment(alignment: TextAlignment) {
        val currentDoc = vm.currentDocument ?: return
        val sel = vm.selection.value
        val targetIndices = vm.findParagraphBlockIndicesForRange(currentDoc.blocks, sel.min, sel.max)
        if (targetIndices.size > 1) {
            val targetBlocks = targetIndices.mapNotNull { currentDoc.blocks.getOrNull(it) as? ParagraphBlock }
            val oldAlignments = targetBlocks.associate { it.id to it.alignment }
            vm.executeCommand(ChangeMultipleParagraphsAlignmentCommand(targetBlocks.map { it.id }, oldAlignments, alignment))
        } else {
            val block = currentDoc.blocks.getOrNull(vm.activeBlockIndex.value) as? ParagraphBlock ?: return
            vm.executeCommand(ChangeAlignmentCommand(block.id, block.alignment, alignment))
        }
    }

    fun setLineSpacing(multiplier: Float) {
        val currentDoc = vm.currentDocument ?: return
        val block = currentDoc.blocks.getOrNull(vm.activeBlockIndex.value) as? ParagraphBlock ?: return
        val updatedBlocks = currentDoc.blocks.map { b ->
            if (b.id == block.id && b is ParagraphBlock) b.copy(lineSpacingMultiplier = multiplier.coerceIn(1.0f, 3.0f)) else b
        }
        val updatedDoc = currentDoc.copy(blocks = updatedBlocks, updatedAt = System.currentTimeMillis())
        vm.executeCommand(object : com.example.editor.engine.EditorCommand {
            override val description: String = "Set Line Spacing to ${multiplier}x"
            override fun apply(document: com.example.data.model.RichDocument) = updatedDoc
            override fun revert(document: com.example.data.model.RichDocument) = currentDoc
        })
    }

    fun toggleFormatPainter() {
        if (_isFormatPainterActive.value) {
            _isFormatPainterActive.value = false
            _copiedFormatting = null
            vm.showStatusMessage("Format Painter cancelled")
        } else {
            val currentDoc = vm.currentDocument ?: return
            val block = currentDoc.blocks.getOrNull(vm.activeBlockIndex.value) as? ParagraphBlock ?: return
            val sel = vm.selection.value
            val active = if (!sel.collapsed && sel.min < sel.max) {
                SpanOperations.queryFormatting(block.spans, sel.min, sel.max)
            } else {
                SpanOperations.queryFormatting(block.spans, sel.start, sel.end)
            }
            _copiedFormatting = CopiedFormatting(
                isBold = active.isBold,
                isItalic = active.isItalic,
                isUnderline = active.isUnderline,
                isStrikethrough = active.isStrikethrough,
                textColorHex = active.textColorHex,
                highlightColorHex = active.highlightColorHex,
                fontFamilyName = active.fontFamilyName,
                fontSizeSp = active.fontSizeSp,
                alignment = block.alignment,
                headingStyle = block.headingStyle,
                lineSpacingMultiplier = block.lineSpacingMultiplier
            )
            _isFormatPainterActive.value = true
            vm.showStatusMessage("Format copied. Tap text to apply")
        }
    }

    fun applyFormatPainterIfActive(targetBlockIndex: Int) {
        val fmt = _copiedFormatting ?: return
        val currentDoc = vm.currentDocument ?: return
        val block = currentDoc.blocks.getOrNull(targetBlockIndex) as? ParagraphBlock ?: return
        val newSpans = listOf(
            InlineSpan(
                start = 0,
                end = block.text.length,
                isBold = fmt.isBold,
                isItalic = fmt.isItalic,
                isUnderline = fmt.isUnderline,
                isStrikethrough = fmt.isStrikethrough,
                textColorHex = fmt.textColorHex,
                highlightColorHex = fmt.highlightColorHex,
                fontFamilyName = fmt.fontFamilyName,
                fontSizeSp = fmt.fontSizeSp
            )
        )
        val updatedBlock = block.copy(
            spans = newSpans,
            alignment = fmt.alignment,
            headingStyle = fmt.headingStyle,
            lineSpacingMultiplier = fmt.lineSpacingMultiplier
        )
        val updatedBlocks = currentDoc.blocks.toMutableList()
        updatedBlocks[targetBlockIndex] = updatedBlock
        val updatedDoc = currentDoc.copy(blocks = updatedBlocks, updatedAt = System.currentTimeMillis())
        vm.executeCommand(object : com.example.editor.engine.EditorCommand {
            override val description: String = "Apply Format Painter"
            override fun apply(document: com.example.data.model.RichDocument) = updatedDoc
            override fun revert(document: com.example.data.model.RichDocument) = currentDoc
        })
        _isFormatPainterActive.value = false
        _copiedFormatting = null
    }

    fun setHeadingStyle(heading: HeadingStyle) {
        val currentDoc = vm.currentDocument ?: return
        val block = currentDoc.blocks.getOrNull(vm.activeBlockIndex.value) as? ParagraphBlock ?: return
        vm.executeCommand(ChangeHeadingStyleCommand(block.id, block.headingStyle, heading))
    }

    fun toggleRtl() {
        val currentDoc = vm.currentDocument ?: return
        val block = currentDoc.blocks.getOrNull(vm.activeBlockIndex.value) as? ParagraphBlock ?: return
        vm.executeCommand(ToggleRtlCommand(block.id, block.isRtl, !block.isRtl))
    }

    fun toggleListType(targetType: ListType) {
        val currentDoc = vm.currentDocument ?: return
        val block = currentDoc.blocks.getOrNull(vm.activeBlockIndex.value) as? ParagraphBlock ?: return
        val newType = if (block.listType == targetType) ListType.NONE else targetType
        vm.executeCommand(ChangeListTypeCommand(block.id, block.listType, newType))
    }

    fun increaseIndent() {
        val currentDoc = vm.currentDocument ?: return
        val block = currentDoc.blocks.getOrNull(vm.activeBlockIndex.value) as? ParagraphBlock ?: return
        if (block.indentationLevel < 6) {
            vm.executeCommand(SetIndentationCommand(block.id, block.indentationLevel, block.indentationLevel + 1))
        }
    }

    fun decreaseIndent() {
        val currentDoc = vm.currentDocument ?: return
        val block = currentDoc.blocks.getOrNull(vm.activeBlockIndex.value) as? ParagraphBlock ?: return
        if (block.indentationLevel > 0) {
            vm.executeCommand(SetIndentationCommand(block.id, block.indentationLevel, block.indentationLevel - 1))
        }
    }

    fun updateParagraphIndents(
        firstLineIndentPt: Float = 0f,
        hangingIndentPt: Float = 0f,
        spaceBeforePt: Float = 0f,
        spaceAfterPt: Float = 0f,
        dropCapLines: Int = 0
    ) {
        val currentDoc = vm.currentDocument ?: return
        val block = currentDoc.blocks.getOrNull(vm.activeBlockIndex.value) as? ParagraphBlock ?: return
        val updated = block.copy(
            firstLineIndentPt = firstLineIndentPt,
            hangingIndentPt = hangingIndentPt,
            spaceBeforePt = spaceBeforePt,
            spaceAfterPt = spaceAfterPt,
            dropCapLines = dropCapLines
        )
        val updatedBlocks = currentDoc.blocks.map { if (it.id == block.id) updated else it }
        val updatedDoc = currentDoc.copy(blocks = updatedBlocks, updatedAt = System.currentTimeMillis())
        vm.executeCommand(object : com.example.editor.engine.EditorCommand {
            override val description: String = "Update Indents & Spacing"
            override fun apply(document: com.example.data.model.RichDocument) = updatedDoc
            override fun revert(document: com.example.data.model.RichDocument) = currentDoc
        })
    }

    fun toggleChecklist() {
        val currentDoc = vm.currentDocument ?: return
        val block = currentDoc.blocks.getOrNull(vm.activeBlockIndex.value) as? ParagraphBlock ?: return
        vm.executeCommand(ToggleChecklistCommand(block.id, block))
    }

    fun toggleChecklistItem(paragraphId: String) {
        val currentDoc = vm.currentDocument ?: return
        val block = currentDoc.blocks.find { it.id == paragraphId } as? ParagraphBlock ?: return
        vm.executeCommand(ToggleChecklistItemCommand(block.id, block.isChecked))
    }

    fun setListLevel(level: Int) {
        val currentDoc = vm.currentDocument ?: return
        val block = currentDoc.blocks.getOrNull(vm.activeBlockIndex.value) as? ParagraphBlock ?: return
        vm.executeCommand(SetListLevelCommand(block.id, block.listLevel, level))
    }

    fun saveCustomStyle(style: CustomStyle) {
        vm.executeCommand(SaveCustomStyleCommand(style))
        vm.showStatusMessage("Saved style: ${style.name}")
    }

    fun applyCustomStyle(style: CustomStyle) {
        val currentDoc = vm.currentDocument ?: return
        val block = currentDoc.blocks.getOrNull(vm.activeBlockIndex.value) as? ParagraphBlock ?: return
        vm.executeCommand(ApplyCustomStyleCommand(block.id, block, style))
        vm.showStatusMessage("Applied style: ${style.name}")
    }
}
