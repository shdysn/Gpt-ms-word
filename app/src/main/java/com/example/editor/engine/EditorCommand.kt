package com.example.editor.engine

import com.example.data.model.RichDocument

/**
 * Interface for reversible document editor actions supporting robust Undo/Redo.
 */
interface EditorCommand {
    val description: String
    fun apply(document: RichDocument): RichDocument
    fun revert(document: RichDocument): RichDocument
}

/**
 * Text case types matching Microsoft Word Change Case options.
 */
enum class CaseType(val label: String) {
    SENTENCE("Sentence case"),
    LOWERCASE("lowercase"),
    UPPERCASE("UPPERCASE"),
    TITLE_CASE("Capitalize Each Word"),
    TOGGLE_CASE("tOGGLE cASE")
}

// Re-exports for 100% backward compatibility
typealias UpdateParagraphTextCommand = com.example.editor.engine.commands.UpdateParagraphTextCommand
typealias ChangeAlignmentCommand = com.example.editor.engine.commands.ChangeAlignmentCommand
typealias ChangeMultipleParagraphsAlignmentCommand = com.example.editor.engine.commands.ChangeMultipleParagraphsAlignmentCommand
typealias ChangeHeadingStyleCommand = com.example.editor.engine.commands.ChangeHeadingStyleCommand
typealias ToggleRtlCommand = com.example.editor.engine.commands.ToggleRtlCommand
typealias ApplyInlineSpanCommand = com.example.editor.engine.commands.ApplyInlineSpanCommand
typealias InsertBlockCommand = com.example.editor.engine.commands.InsertBlockCommand
typealias DeleteBlockCommand = com.example.editor.engine.commands.DeleteBlockCommand
typealias UpdateLayoutCommand = com.example.editor.engine.commands.UpdateLayoutCommand
typealias ChangeListTypeCommand = com.example.editor.engine.commands.ChangeListTypeCommand
typealias BatchReplaceTextCommand = com.example.editor.engine.commands.BatchReplaceTextCommand
typealias UpdateParagraphTextAndSpansCommand = com.example.editor.engine.commands.UpdateParagraphTextAndSpansCommand
typealias SetIndentationCommand = com.example.editor.engine.commands.SetIndentationCommand
typealias UpdateTableBlockCommand = com.example.editor.engine.commands.UpdateTableBlockCommand
typealias UpdateImageBlockCommand = com.example.editor.engine.commands.UpdateImageBlockCommand
typealias UpdateBlockCommand = com.example.editor.engine.commands.UpdateBlockCommand
typealias ClearFormattingCommand = com.example.editor.engine.commands.ClearFormattingCommand
typealias ChangeCaseCommand = com.example.editor.engine.commands.ChangeCaseCommand
typealias UpdateParagraphIndentsCommand = com.example.editor.engine.commands.UpdateParagraphIndentsCommand
typealias ToggleChecklistCommand = com.example.editor.engine.commands.ToggleChecklistCommand
typealias ToggleChecklistItemCommand = com.example.editor.engine.commands.ToggleChecklistItemCommand
typealias SetListLevelCommand = com.example.editor.engine.commands.SetListLevelCommand
typealias MergeTableCellsCommand = com.example.editor.engine.commands.MergeTableCellsCommand
typealias SplitTableCellCommand = com.example.editor.engine.commands.SplitTableCellCommand
typealias UpdateTableCellFormattingCommand = com.example.editor.engine.commands.UpdateTableCellFormattingCommand
typealias SaveCustomStyleCommand = com.example.editor.engine.commands.SaveCustomStyleCommand
typealias ApplyCustomStyleCommand = com.example.editor.engine.commands.ApplyCustomStyleCommand
