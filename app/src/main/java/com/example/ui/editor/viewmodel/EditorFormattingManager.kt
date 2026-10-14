package com.example.ui.editor.viewmodel

import androidx.compose.ui.text.TextRange
import com.example.data.model.CustomStyle
import com.example.data.model.HeadingStyle
import com.example.data.model.InlineSpan
import com.example.data.model.ListType
import com.example.data.model.ParagraphBlock
import com.example.data.model.TextAlignment
import com.example.editor.engine.ActiveFormatting
import com.example.editor.engine.CaseType
import com.example.editor.engine.SpanOperations
import com.example.editor.engine.commands.ApplyInlineSpanCommand
import com.example.ui.editor.EditorViewModel
import kotlinx.coroutines.flow.StateFlow

data class CopiedFormatting(
    val isBold: Boolean = false,
    val isItalic: Boolean = false,
    val isUnderline: Boolean = false,
    val isStrikethrough: Boolean = false,
    val textColorHex: String? = null,
    val highlightColorHex: String? = null,
    val fontFamilyName: String? = null,
    val fontSizeSp: Float? = null,
    val alignment: TextAlignment = TextAlignment.START,
    val headingStyle: HeadingStyle = HeadingStyle.NORMAL,
    val lineSpacingMultiplier: Float = 1.25f
)

/**
 * Manages character and inline formatting, font selections, typography effects,
 * delegating paragraph properties to [EditorParagraphFormattingManager].
 */
class EditorFormattingManager(
    private val vm: EditorViewModel
) {
    internal val paragraphManager = EditorParagraphFormattingManager(vm)
    val isFormatPainterActive: StateFlow<Boolean> get() = paragraphManager.isFormatPainterActive

    private fun applyStyleToSelectionOrPending(
        mutator: (InlineSpan, Boolean) -> InlineSpan
    ) {
        val currentDoc = vm.currentDocument ?: return
        val blockIndex = vm.activeBlockIndex.value
        val block = currentDoc.blocks.getOrNull(blockIndex) as? ParagraphBlock ?: return
        val sel = vm.selection.value

        if (!sel.collapsed && sel.min < sel.max) {
            val updatedSpans = SpanOperations.applyOrToggleStyle(
                currentSpans = block.spans,
                rangeStart = sel.min,
                rangeEnd = sel.max,
                textLength = block.text.length,
                mutator = mutator
            )
            vm.executeCommand(
                ApplyInlineSpanCommand(
                    blockId = block.id,
                    oldSpans = block.spans,
                    newSpans = updatedSpans
                )
            )
        } else {
            val dummySpan = InlineSpan(
                start = 0,
                end = 1,
                isBold = vm.pendingFormatting.value.isBold,
                isItalic = vm.pendingFormatting.value.isItalic,
                isUnderline = vm.pendingFormatting.value.isUnderline,
                isStrikethrough = vm.pendingFormatting.value.isStrikethrough,
                isSuperscript = vm.pendingFormatting.value.isSuperscript,
                isSubscript = vm.pendingFormatting.value.isSubscript
            )
            val toggled = mutator(dummySpan, true)
            vm.updatePendingFormatting {
                it.copy(
                    isBold = toggled.isBold,
                    isItalic = toggled.isItalic,
                    isUnderline = toggled.isUnderline,
                    isStrikethrough = toggled.isStrikethrough,
                    isSuperscript = toggled.isSuperscript,
                    isSubscript = toggled.isSubscript
                )
            }
        }
    }

    private fun applyExplicitStyle(mutator: (InlineSpan) -> InlineSpan) {
        val currentDoc = vm.currentDocument ?: return
        val block = currentDoc.blocks.getOrNull(vm.activeBlockIndex.value) as? ParagraphBlock ?: return
        val sel = vm.selection.value
        if (!sel.collapsed && sel.min < sel.max) {
            val updated = SpanOperations.applyExplicitStyle(
                currentSpans = block.spans,
                rangeStart = sel.min,
                rangeEnd = sel.max,
                textLength = block.text.length,
                mutator = mutator
            )
            vm.executeCommand(ApplyInlineSpanCommand(block.id, block.spans, updated))
        }
    }

    fun toggleBold() { applyStyleToSelectionOrPending { span, enable -> span.copy(isBold = enable) } }
    fun toggleItalic() { applyStyleToSelectionOrPending { span, enable -> span.copy(isItalic = enable) } }
    fun toggleUnderline() { applyStyleToSelectionOrPending { span, enable -> span.copy(isUnderline = enable) } }
    fun toggleStrikethrough() { applyStyleToSelectionOrPending { span, enable -> span.copy(isStrikethrough = enable) } }
    fun toggleSuperscript() {
        applyStyleToSelectionOrPending { span, enable ->
            span.copy(isSuperscript = enable, isSubscript = if (enable) false else span.isSubscript)
        }
    }
    fun toggleSubscript() {
        applyStyleToSelectionOrPending { span, enable ->
            span.copy(isSubscript = enable, isSuperscript = if (enable) false else span.isSuperscript)
        }
    }

    fun setTextColor(colorHex: String?) {
        applyExplicitStyle { span -> span.copy(textColorHex = colorHex) }
        vm.updatePendingFormatting { it.copy(textColorHex = colorHex) }
    }

    fun setHighlightColor(colorHex: String?) {
        applyExplicitStyle { span -> span.copy(highlightColorHex = colorHex) }
        vm.updatePendingFormatting { it.copy(highlightColorHex = colorHex) }
    }

    fun setFontFamily(fontName: String) {
        applyExplicitStyle { span -> span.copy(fontFamilyName = fontName) }
        vm.updatePendingFormatting { it.copy(fontFamilyName = fontName) }
    }

    fun setFontSize(sizeSp: Float) {
        applyExplicitStyle { span -> span.copy(fontSizeSp = sizeSp) }
        vm.updatePendingFormatting { it.copy(fontSizeSp = sizeSp) }
    }

    fun setHyperlink(url: String?) {
        applyExplicitStyle { span -> span.copy(linkUrl = url) }
    }

    fun clearFormatting() {
        val currentDoc = vm.currentDocument ?: return
        val block = currentDoc.blocks.getOrNull(vm.activeBlockIndex.value) as? ParagraphBlock ?: return
        val sel = vm.selection.value
        val cleared = SpanOperations.clearFormatting(block.spans, sel.min, sel.max, block.text.length)
        vm.executeCommand(ApplyInlineSpanCommand(block.id, block.spans, cleared))
        vm.updatePendingFormatting { ActiveFormatting() }
    }

    fun changeCase(caseType: CaseType) {
        val currentDoc = vm.currentDocument ?: return
        val block = currentDoc.blocks.getOrNull(vm.activeBlockIndex.value) as? ParagraphBlock ?: return
        val sel = vm.selection.value
        val s = sel.min.coerceIn(0, block.text.length)
        val e = sel.max.coerceIn(0, block.text.length)
        if (s >= e) return
        val target = block.text.substring(s, e)
        val transformed = when (caseType) {
            CaseType.UPPERCASE -> target.uppercase()
            CaseType.LOWERCASE -> target.lowercase()
            CaseType.TITLE_CASE -> target.split(" ").joinToString(" ") { it.replaceFirstChar { c -> c.uppercase() } }
            CaseType.SENTENCE -> target.replaceFirstChar { it.uppercase() }
            CaseType.TOGGLE_CASE -> target.map { if (it.isUpperCase()) it.lowercaseChar() else it.uppercaseChar() }.joinToString("")
        }
        val newText = block.text.substring(0, s) + transformed + block.text.substring(e)
        vm.updateParagraphText(vm.activeBlockIndex.value, newText, TextRange(s, s + transformed.length))
    }

    fun setLetterSpacing(letterSpacingSp: Float?) {
        applyExplicitStyle { span -> span.copy(letterSpacingSp = letterSpacingSp) }
        vm.updatePendingFormatting { it.copy(letterSpacingSp = letterSpacingSp) }
    }

    fun applyTypographyEffects(
        letterSpacingSp: Float? = null,
        underlineStyle: com.example.data.model.UnderlineStyle = com.example.data.model.UnderlineStyle.NONE,
        underlineColorHex: String? = null,
        strikethroughStyle: com.example.data.model.StrikethroughStyle = com.example.data.model.StrikethroughStyle.NONE,
        shadowColorHex: String? = null,
        shadowDx: Float? = null,
        shadowDy: Float? = null,
        shadowRadius: Float? = null,
        isOutline: Boolean = false
    ) {
        applyExplicitStyle { span ->
            span.copy(
                underlineStyle = underlineStyle,
                underlineColorHex = underlineColorHex,
                strikethroughStyle = strikethroughStyle,
                shadowColorHex = shadowColorHex,
                shadowDx = shadowDx,
                shadowDy = shadowDy,
                shadowRadius = shadowRadius,
                isOutline = isOutline
            )
        }
        vm.updatePendingFormatting {
            it.copy(
                underlineStyle = underlineStyle,
                underlineColorHex = underlineColorHex,
                strikethroughStyle = strikethroughStyle,
                shadowColorHex = shadowColorHex,
                isOutline = isOutline
            )
        }
    }

    // Paragraph Formatting Delegations
    fun setAlignment(alignment: TextAlignment) = paragraphManager.setAlignment(alignment)
    fun setLineSpacing(multiplier: Float) = paragraphManager.setLineSpacing(multiplier)
    fun toggleFormatPainter() = paragraphManager.toggleFormatPainter()
    fun applyFormatPainterIfActive(targetBlockIndex: Int) = paragraphManager.applyFormatPainterIfActive(targetBlockIndex)
    fun setHeadingStyle(heading: HeadingStyle) = paragraphManager.setHeadingStyle(heading)
    fun toggleRtl() = paragraphManager.toggleRtl()
    fun toggleListType(targetType: ListType) = paragraphManager.toggleListType(targetType)
    fun increaseIndent() = paragraphManager.increaseIndent()
    fun decreaseIndent() = paragraphManager.decreaseIndent()
    fun updateParagraphIndents(firstLineIndentPt: Float = 0f, hangingIndentPt: Float = 0f, spaceBeforePt: Float = 0f, spaceAfterPt: Float = 0f, dropCapLines: Int = 0) =
        paragraphManager.updateParagraphIndents(firstLineIndentPt, hangingIndentPt, spaceBeforePt, spaceAfterPt, dropCapLines)
    fun toggleChecklist() = paragraphManager.toggleChecklist()
    fun toggleChecklistItem(paragraphId: String) = paragraphManager.toggleChecklistItem(paragraphId)
    fun setListLevel(level: Int) = paragraphManager.setListLevel(level)
    fun saveCustomStyle(style: CustomStyle) = paragraphManager.saveCustomStyle(style)
    fun applyCustomStyle(style: CustomStyle) = paragraphManager.applyCustomStyle(style)
}
