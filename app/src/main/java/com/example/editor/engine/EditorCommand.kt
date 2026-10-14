package com.example.editor.engine

import com.example.core.layout.PageLayoutConfig
import com.example.data.model.BlockElement
import com.example.data.model.HeadingStyle
import com.example.data.model.ImageBlock
import com.example.data.model.InlineSpan
import com.example.data.model.ParagraphBlock
import com.example.data.model.RichDocument
import com.example.data.model.TableBlock
import com.example.data.model.TextAlignment

/**
 * Interface for reversible document editor actions supporting robust Undo/Redo.
 */
interface EditorCommand {
    val description: String
    fun apply(document: RichDocument): RichDocument
    fun revert(document: RichDocument): RichDocument
}

/**
 * Command for text modifications inside a specific paragraph.
 */
data class UpdateParagraphTextCommand(
    val blockId: String,
    val oldText: String,
    val newText: String
) : EditorCommand {
    override val description: String = "Edit Text"

    override fun apply(document: RichDocument): RichDocument {
        val updatedBlocks = document.blocks.map { block ->
            if (block.id == blockId && block is ParagraphBlock) {
                block.copy(text = newText)
            } else block
        }
        return document.copy(blocks = updatedBlocks)
    }

    override fun revert(document: RichDocument): RichDocument {
        val updatedBlocks = document.blocks.map { block ->
            if (block.id == blockId && block is ParagraphBlock) {
                block.copy(text = oldText)
            } else block
        }
        return document.copy(blocks = updatedBlocks)
    }
}

/**
 * Command for changing paragraph text alignment (Left, Center, Right, Justify).
 */
data class ChangeAlignmentCommand(
    val blockId: String,
    val oldAlignment: TextAlignment,
    val newAlignment: TextAlignment
) : EditorCommand {
    override val description: String = "Change Alignment to $newAlignment"

    override fun apply(document: RichDocument): RichDocument {
        val updatedBlocks = document.blocks.map { block ->
            if (block.id == blockId && block is ParagraphBlock) {
                block.copy(alignment = newAlignment)
            } else block
        }
        return document.copy(blocks = updatedBlocks)
    }

    override fun revert(document: RichDocument): RichDocument {
        val updatedBlocks = document.blocks.map { block ->
            if (block.id == blockId && block is ParagraphBlock) {
                block.copy(alignment = oldAlignment)
            } else block
        }
        return document.copy(blocks = updatedBlocks)
    }
}

/**
 * Command for changing alignment of multiple paragraphs in a selection.
 */
data class ChangeMultipleParagraphsAlignmentCommand(
    val blockIds: List<String>,
    val oldAlignments: Map<String, TextAlignment>,
    val newAlignment: TextAlignment
) : EditorCommand {
    override val description: String = "Change Multiple Alignments to $newAlignment"

    override fun apply(document: RichDocument): RichDocument {
        val idSet = blockIds.toSet()
        val updatedBlocks = document.blocks.map { block ->
            if (block is ParagraphBlock && block.id in idSet) {
                block.copy(alignment = newAlignment)
            } else block
        }
        return document.copy(blocks = updatedBlocks)
    }

    override fun revert(document: RichDocument): RichDocument {
        val updatedBlocks = document.blocks.map { block ->
            if (block is ParagraphBlock && block.id in oldAlignments) {
                block.copy(alignment = oldAlignments[block.id] ?: block.alignment)
            } else block
        }
        return document.copy(blocks = updatedBlocks)
    }
}


/**
 * Command for changing heading style (Title, Heading 1, 2, 3, Normal).
 */
data class ChangeHeadingStyleCommand(
    val blockId: String,
    val oldHeading: HeadingStyle,
    val newHeading: HeadingStyle
) : EditorCommand {
    override val description: String = "Set style to ${newHeading.displayName}"

    override fun apply(document: RichDocument): RichDocument {
        val updatedBlocks = document.blocks.map { block ->
            if (block.id == blockId && block is ParagraphBlock) {
                block.copy(headingStyle = newHeading)
            } else block
        }
        return document.copy(blocks = updatedBlocks)
    }

    override fun revert(document: RichDocument): RichDocument {
        val updatedBlocks = document.blocks.map { block ->
            if (block.id == blockId && block is ParagraphBlock) {
                block.copy(headingStyle = oldHeading)
            } else block
        }
        return document.copy(blocks = updatedBlocks)
    }
}

/**
 * Command for toggling Right-to-Left (Urdu / Arabic) directionality.
 */
data class ToggleRtlCommand(
    val blockId: String,
    val wasRtl: Boolean,
    val isRtl: Boolean
) : EditorCommand {
    override val description: String = if (isRtl) "Set Right-to-Left (RTL)" else "Set Left-to-Right (LTR)"

    override fun apply(document: RichDocument): RichDocument {
        val updatedBlocks = document.blocks.map { block ->
            if (block.id == blockId && block is ParagraphBlock) {
                block.copy(isRtl = isRtl)
            } else block
        }
        return document.copy(blocks = updatedBlocks)
    }

    override fun revert(document: RichDocument): RichDocument {
        val updatedBlocks = document.blocks.map { block ->
            if (block.id == blockId && block is ParagraphBlock) {
                block.copy(isRtl = wasRtl)
            } else block
        }
        return document.copy(blocks = updatedBlocks)
    }
}

/**
 * Command for applying inline formatting span (Bold, Italic, Underline, Color).
 */
data class ApplyInlineSpanCommand(
    val blockId: String,
    val oldSpans: List<InlineSpan>,
    val newSpans: List<InlineSpan>
) : EditorCommand {
    override val description: String = "Format Text"

    override fun apply(document: RichDocument): RichDocument {
        val updatedBlocks = document.blocks.map { block ->
            if (block.id == blockId && block is ParagraphBlock) {
                block.copy(spans = newSpans)
            } else block
        }
        return document.copy(blocks = updatedBlocks)
    }

    override fun revert(document: RichDocument): RichDocument {
        val updatedBlocks = document.blocks.map { block ->
            if (block.id == blockId && block is ParagraphBlock) {
                block.copy(spans = oldSpans)
            } else block
        }
        return document.copy(blocks = updatedBlocks)
    }
}

/**
 * Command for inserting a new block element (paragraph, table, divider, page break).
 */
data class InsertBlockCommand(
    val index: Int,
    val block: BlockElement
) : EditorCommand {
    override val description: String = "Insert Element"

    override fun apply(document: RichDocument): RichDocument {
        val mutable = document.blocks.toMutableList()
        val insertIndex = index.coerceIn(0, mutable.size)
        mutable.add(insertIndex, block)
        return document.copy(blocks = mutable)
    }

    override fun revert(document: RichDocument): RichDocument {
        val mutable = document.blocks.toMutableList()
        mutable.removeAll { it.id == block.id }
        if (mutable.isEmpty()) {
            mutable.add(ParagraphBlock())
        }
        return document.copy(blocks = mutable)
    }
}

/**
 * Command for deleting a block element.
 */
data class DeleteBlockCommand(
    val index: Int,
    val deletedBlock: BlockElement
) : EditorCommand {
    override val description: String = "Delete Element"

    override fun apply(document: RichDocument): RichDocument {
        val mutable = document.blocks.toMutableList()
        mutable.removeAll { it.id == deletedBlock.id }
        if (mutable.isEmpty()) {
            mutable.add(ParagraphBlock())
        }
        return document.copy(blocks = mutable)
    }

    override fun revert(document: RichDocument): RichDocument {
        val mutable = document.blocks.toMutableList()
        val insertIndex = index.coerceIn(0, mutable.size)
        mutable.add(insertIndex, deletedBlock)
        return document.copy(blocks = mutable)
    }
}

/**
 * Command for updating page layout configuration (Page Size, Margins, Orientation).
 */
data class UpdateLayoutCommand(
    val oldLayout: PageLayoutConfig,
    val newLayout: PageLayoutConfig
) : EditorCommand {
    override val description: String = "Update Page Layout"

    override fun apply(document: RichDocument): RichDocument {
        return document.copy(layoutConfig = newLayout)
    }

    override fun revert(document: RichDocument): RichDocument {
        return document.copy(layoutConfig = oldLayout)
    }
}

/**
 * Command for changing bullet / numbered list style.
 */
data class ChangeListTypeCommand(
    val blockId: String,
    val oldListType: com.example.data.model.ListType,
    val newListType: com.example.data.model.ListType
) : EditorCommand {
    override val description: String = "Change list style"

    override fun apply(document: RichDocument): RichDocument {
        val updated = document.blocks.map { block ->
            if (block.id == blockId && block is ParagraphBlock) {
                block.copy(listType = newListType)
            } else block
        }
        return document.copy(blocks = updated)
    }

    override fun revert(document: RichDocument): RichDocument {
        val updated = document.blocks.map { block ->
            if (block.id == blockId && block is ParagraphBlock) {
                block.copy(listType = oldListType)
            } else block
        }
        return document.copy(blocks = updated)
    }
}

/**
 * Command for in-document Find and Replace operations across paragraphs.
 */
data class BatchReplaceTextCommand(
    val oldDocument: RichDocument,
    val newDocument: RichDocument,
    val replacementsCount: Int
) : EditorCommand {
    override val description: String = "Replace $replacementsCount occurrences"

    override fun apply(document: RichDocument): RichDocument = newDocument
    override fun revert(document: RichDocument): RichDocument = oldDocument
}

/**
 * Command for updating paragraph text while shifting/updating inline styling spans.
 */
data class UpdateParagraphTextAndSpansCommand(
    val blockId: String,
    val oldText: String,
    val newText: String,
    val oldSpans: List<InlineSpan>,
    val newSpans: List<InlineSpan>,
    val wasRtl: Boolean? = null,
    val isRtl: Boolean? = null
) : EditorCommand {
    override val description: String = "Edit Text"

    override fun apply(document: RichDocument): RichDocument {
        val updated = document.blocks.map { block ->
            if (block.id == blockId && block is ParagraphBlock) {
                block.copy(
                    text = newText,
                    spans = newSpans,
                    isRtl = isRtl ?: block.isRtl
                )
            } else block
        }
        return document.copy(blocks = updated)
    }

    override fun revert(document: RichDocument): RichDocument {
        val updated = document.blocks.map { block ->
            if (block.id == blockId && block is ParagraphBlock) {
                block.copy(
                    text = oldText,
                    spans = oldSpans,
                    isRtl = wasRtl ?: block.isRtl
                )
            } else block
        }
        return document.copy(blocks = updated)
    }
}

/**
 * Command for changing paragraph indentation level (0 to 6).
 */
data class SetIndentationCommand(
    val blockId: String,
    val oldLevel: Int,
    val newLevel: Int
) : EditorCommand {
    override val description: String = if (newLevel > oldLevel) "Increase Indent" else "Decrease Indent"

    override fun apply(document: RichDocument): RichDocument {
        val updated = document.blocks.map { block ->
            if (block.id == blockId && block is ParagraphBlock) {
                block.copy(indentationLevel = newLevel.coerceIn(0, 6))
            } else block
        }
        return document.copy(blocks = updated)
    }

    override fun revert(document: RichDocument): RichDocument {
        val updated = document.blocks.map { block ->
            if (block.id == blockId && block is ParagraphBlock) {
                block.copy(indentationLevel = oldLevel.coerceIn(0, 6))
            } else block
        }
        return document.copy(blocks = updated)
    }
}

/**
 * Command for table structure, sizing, border, and cell edits.
 */
data class UpdateTableBlockCommand(
    val blockId: String,
    val oldTable: TableBlock,
    val newTable: TableBlock,
    val actionDescription: String = "Edit Table"
) : EditorCommand {
    override val description: String = actionDescription

    override fun apply(document: RichDocument): RichDocument {
        val updated = document.blocks.map { block ->
            if (block.id == blockId && block is TableBlock) {
                newTable.copy(id = blockId)
            } else block
        }
        return document.copy(blocks = updated)
    }

    override fun revert(document: RichDocument): RichDocument {
        val updated = document.blocks.map { block ->
            if ((block.id == blockId || block.id == newTable.id) && block is TableBlock) {
                oldTable.copy(id = blockId)
            } else block
        }
        return document.copy(blocks = updated)
    }
}

/**
 * Command for image dimensions, alignment, and caption modifications.
 */
data class UpdateImageBlockCommand(
    val blockId: String,
    val oldImage: ImageBlock,
    val newImage: ImageBlock,
    val actionDescription: String = "Edit Image"
) : EditorCommand {
    override val description: String = actionDescription

    override fun apply(document: RichDocument): RichDocument {
        val updated = document.blocks.map { block ->
            if (block.id == blockId && block is ImageBlock) {
                newImage.copy(id = blockId)
            } else block
        }
        return document.copy(blocks = updated)
    }

    override fun revert(document: RichDocument): RichDocument {
        val updated = document.blocks.map { block ->
            if ((block.id == blockId || block.id == newImage.id) && block is ImageBlock) {
                oldImage.copy(id = blockId)
            } else block
        }
        return document.copy(blocks = updated)
    }
}

/**
 * Command for updating an arbitrary block element in place.
 */
data class UpdateBlockCommand(
    val blockId: String,
    val newBlock: BlockElement
) : EditorCommand {
    override val description: String = "Update Block"

    override fun apply(document: RichDocument): RichDocument {
        val updated = document.blocks.map { block ->
            if (block.id == blockId) newBlock else block
        }
        return document.copy(blocks = updated)
    }

    override fun revert(document: RichDocument): RichDocument {
        return document
    }
}

/**
 * Command for clearing all character formatting and resetting heading style.
 */
data class ClearFormattingCommand(
    val blockId: String,
    val selectionStart: Int,
    val selectionEnd: Int,
    val oldParagraph: ParagraphBlock
) : EditorCommand {
    override val description: String = "Clear Formatting"

    override fun apply(document: RichDocument): RichDocument {
        val updated = document.blocks.map { block ->
            if (block.id == blockId && block is ParagraphBlock) {
                val newSpans = SpanOperations.clearFormatting(
                    spans = block.spans,
                    start = selectionStart,
                    end = selectionEnd,
                    textLength = block.text.length
                )
                // If entire paragraph or no selection, reset heading style to NORMAL
                val newHeading = if (selectionStart == selectionEnd || (selectionStart <= 0 && selectionEnd >= block.text.length)) {
                    HeadingStyle.NORMAL
                } else {
                    block.headingStyle
                }
                block.copy(spans = newSpans, headingStyle = newHeading)
            } else block
        }
        return document.copy(blocks = updated)
    }

    override fun revert(document: RichDocument): RichDocument {
        val updated = document.blocks.map { block ->
            if (block.id == blockId && block is ParagraphBlock) {
                oldParagraph
            } else block
        }
        return document.copy(blocks = updated)
    }
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

/**
 * Command for modifying text casing within a selection.
 */
data class ChangeCaseCommand(
    val blockId: String,
    val selectionStart: Int,
    val selectionEnd: Int,
    val caseType: CaseType,
    val oldParagraph: ParagraphBlock
) : EditorCommand {
    override val description: String = "Change Case to ${caseType.label}"

    override fun apply(document: RichDocument): RichDocument {
        val updated = document.blocks.map { block ->
            if (block.id == blockId && block is ParagraphBlock) {
                val text = block.text
                val s = if (selectionStart == selectionEnd) 0 else selectionStart.coerceIn(0, text.length)
                val e = if (selectionStart == selectionEnd) text.length else selectionEnd.coerceIn(s, text.length)
                if (s >= e) return@map block

                val targetSub = text.substring(s, e)
                val transformedSub = when (caseType) {
                    CaseType.SENTENCE -> {
                        if (targetSub.isNotEmpty()) {
                            targetSub.take(1).uppercase() + targetSub.drop(1).lowercase()
                        } else targetSub
                    }
                    CaseType.LOWERCASE -> targetSub.lowercase()
                    CaseType.UPPERCASE -> targetSub.uppercase()
                    CaseType.TITLE_CASE -> {
                        targetSub.split(" ").joinToString(" ") { word ->
                            if (word.isNotEmpty()) word.take(1).uppercase() + word.drop(1).lowercase() else word
                        }
                    }
                    CaseType.TOGGLE_CASE -> {
                        targetSub.map { ch ->
                            if (ch.isUpperCase()) ch.lowercaseChar() else ch.uppercaseChar()
                        }.joinToString("")
                    }
                }

                val newFullText = text.substring(0, s) + transformedSub + text.substring(e)
                block.copy(text = newFullText)
            } else block
        }
        return document.copy(blocks = updated)
    }

    override fun revert(document: RichDocument): RichDocument {
        val updated = document.blocks.map { block ->
            if (block.id == blockId && block is ParagraphBlock) {
                oldParagraph
            } else block
        }
        return document.copy(blocks = updated)
    }
}

/**
 * Command for modifying paragraph indents (first line, hanging, before/after spacing, drop cap).
 */
data class UpdateParagraphIndentsCommand(
    val blockId: String,
    val oldParagraph: ParagraphBlock,
    val newFirstLineIndentPt: Float = oldParagraph.firstLineIndentPt,
    val newHangingIndentPt: Float = oldParagraph.hangingIndentPt,
    val newSpaceBeforePt: Float = oldParagraph.spaceBeforePt,
    val newSpaceAfterPt: Float = oldParagraph.spaceAfterPt,
    val newDropCapLines: Int = oldParagraph.dropCapLines
) : EditorCommand {
    override val description: String = "Update Paragraph Indents & Spacing"

    override fun apply(document: RichDocument): RichDocument {
        val updated = document.blocks.map { block ->
            if (block.id == blockId && block is ParagraphBlock) {
                block.copy(
                    firstLineIndentPt = newFirstLineIndentPt,
                    hangingIndentPt = newHangingIndentPt,
                    spaceBeforePt = newSpaceBeforePt,
                    spaceAfterPt = newSpaceAfterPt,
                    dropCapLines = newDropCapLines
                )
            } else block
        }
        return document.copy(blocks = updated)
    }

    override fun revert(document: RichDocument): RichDocument {
        val updated = document.blocks.map { block ->
            if (block.id == blockId && block is ParagraphBlock) {
                oldParagraph
            } else block
        }
        return document.copy(blocks = updated)
    }
}

/**
 * Command for toggling checklist mode on a paragraph.
 */
data class ToggleChecklistCommand(
    val blockId: String,
    val oldParagraph: ParagraphBlock
) : EditorCommand {
    override val description: String = "Toggle Checklist"

    override fun apply(document: RichDocument): RichDocument {
        val updated = document.blocks.map { block ->
            if (block.id == blockId && block is ParagraphBlock) {
                val newChecklistState = !block.isChecklist
                block.copy(
                    isChecklist = newChecklistState,
                    isChecked = if (newChecklistState) false else block.isChecked,
                    listType = if (newChecklistState) com.example.data.model.ListType.NONE else block.listType
                )
            } else block
        }
        return document.copy(blocks = updated)
    }

    override fun revert(document: RichDocument): RichDocument {
        val updated = document.blocks.map { block ->
            if (block.id == blockId && block is ParagraphBlock) {
                oldParagraph
            } else block
        }
        return document.copy(blocks = updated)
    }
}

/**
 * Command for toggling a checklist item's checked status.
 */
data class ToggleChecklistItemCommand(
    val blockId: String,
    val wasChecked: Boolean
) : EditorCommand {
    override val description: String = if (wasChecked) "Uncheck item" else "Check item"

    override fun apply(document: RichDocument): RichDocument {
        val updated = document.blocks.map { block ->
            if (block.id == blockId && block is ParagraphBlock) {
                block.copy(isChecked = !wasChecked)
            } else block
        }
        return document.copy(blocks = updated)
    }

    override fun revert(document: RichDocument): RichDocument {
        val updated = document.blocks.map { block ->
            if (block.id == blockId && block is ParagraphBlock) {
                block.copy(isChecked = wasChecked)
            } else block
        }
        return document.copy(blocks = updated)
    }
}

/**
 * Command for changing multi-level hierarchical list level (indentation level 0..4).
 */
data class SetListLevelCommand(
    val blockId: String,
    val oldLevel: Int,
    val newLevel: Int
) : EditorCommand {
    override val description: String = "Change List Level to $newLevel"

    override fun apply(document: RichDocument): RichDocument {
        val updated = document.blocks.map { block ->
            if (block.id == blockId && block is ParagraphBlock) {
                block.copy(listLevel = newLevel.coerceIn(0, 4))
            } else block
        }
        return document.copy(blocks = updated)
    }

    override fun revert(document: RichDocument): RichDocument {
        val updated = document.blocks.map { block ->
            if (block.id == blockId && block is ParagraphBlock) {
                block.copy(listLevel = oldLevel)
            } else block
        }
        return document.copy(blocks = updated)
    }
}

/**
 * Command for merging adjacent table cells horizontally or vertically.
 */
data class MergeTableCellsCommand(
    val blockId: String,
    val oldTable: TableBlock,
    val row: Int,
    val col: Int,
    val horizontalSpan: Int = 2
) : EditorCommand {
    override val description: String = "Merge Table Cells"

    override fun apply(document: RichDocument): RichDocument {
        val updated = document.blocks.map { block ->
            if (block.id == blockId && block is TableBlock) {
                val newCells = block.cells.mapIndexed { r, rowList ->
                    rowList.mapIndexed { c, cell ->
                        if (r == row && c == col) {
                            val combinedText = (col until minOf(block.columnCount, col + horizontalSpan))
                                .map { block.cells[row][it].text }
                                .filter { it.isNotBlank() }
                                .joinToString(" ")
                            cell.copy(colSpan = horizontalSpan, text = combinedText, isMergedCovered = false)
                        } else if (r == row && c > col && c < col + horizontalSpan) {
                            cell.copy(isMergedCovered = true, text = "")
                        } else cell
                    }
                }
                block.copy(cells = newCells)
            } else block
        }
        return document.copy(blocks = updated)
    }

    override fun revert(document: RichDocument): RichDocument {
        val updated = document.blocks.map { block ->
            if (block.id == blockId && block is TableBlock) {
                oldTable
            } else block
        }
        return document.copy(blocks = updated)
    }
}

/**
 * Command for splitting a previously merged cell back to 1x1.
 */
data class SplitTableCellCommand(
    val blockId: String,
    val oldTable: TableBlock,
    val row: Int,
    val col: Int
) : EditorCommand {
    override val description: String = "Split Table Cell"

    override fun apply(document: RichDocument): RichDocument {
        val updated = document.blocks.map { block ->
            if (block.id == blockId && block is TableBlock) {
                val currentCell = block.cells.getOrNull(row)?.getOrNull(col) ?: return@map block
                val span = currentCell.colSpan
                val newCells = block.cells.mapIndexed { r, rowList ->
                    rowList.mapIndexed { c, cell ->
                        if (r == row && c >= col && c < col + span) {
                            cell.copy(colSpan = 1, rowSpan = 1, isMergedCovered = false)
                        } else cell
                    }
                }
                block.copy(cells = newCells)
            } else block
        }
        return document.copy(blocks = updated)
    }

    override fun revert(document: RichDocument): RichDocument {
        val updated = document.blocks.map { block ->
            if (block.id == blockId && block is TableBlock) {
                oldTable
            } else block
        }
        return document.copy(blocks = updated)
    }
}

/**
 * Command for updating individual cell formatting (shading, vertical alignment, padding).
 */
data class UpdateTableCellFormattingCommand(
    val blockId: String,
    val oldTable: TableBlock,
    val row: Int,
    val col: Int,
    val verticalAlignment: com.example.data.model.CellVerticalAlignment,
    val backgroundColorHex: String?
) : EditorCommand {
    override val description: String = "Format Table Cell"

    override fun apply(document: RichDocument): RichDocument {
        val updated = document.blocks.map { block ->
            if (block.id == blockId && block is TableBlock) {
                val newCells = block.cells.mapIndexed { r, rowList ->
                    rowList.mapIndexed { c, cell ->
                        if (r == row && c == col) {
                            cell.copy(
                                verticalAlignment = verticalAlignment,
                                backgroundColorHex = backgroundColorHex
                            )
                        } else cell
                    }
                }
                block.copy(cells = newCells)
            } else block
        }
        return document.copy(blocks = updated)
    }

    override fun revert(document: RichDocument): RichDocument {
        val updated = document.blocks.map { block ->
            if (block.id == blockId && block is TableBlock) {
                oldTable
            } else block
        }
        return document.copy(blocks = updated)
    }
}

/**
 * Command for saving a custom named style into the document gallery.
 */
data class SaveCustomStyleCommand(
    val customStyle: com.example.data.model.CustomStyle
) : EditorCommand {
    override val description: String = "Create Custom Style '${customStyle.name}'"

    override fun apply(document: RichDocument): RichDocument {
        val existing = document.customStyles.filter { it.name != customStyle.name }
        return document.copy(customStyles = existing + customStyle)
    }

    override fun revert(document: RichDocument): RichDocument {
        val filtered = document.customStyles.filter { it.id != customStyle.id }
        return document.copy(customStyles = filtered)
    }
}

/**
 * Command for applying a custom named style to a target paragraph block.
 */
data class ApplyCustomStyleCommand(
    val blockId: String,
    val oldParagraph: ParagraphBlock,
    val style: com.example.data.model.CustomStyle
) : EditorCommand {
    override val description: String = "Apply Style '${style.name}'"

    override fun apply(document: RichDocument): RichDocument {
        val updated = document.blocks.map { block ->
            if (block.id == blockId && block is ParagraphBlock) {
                val baseSpan = InlineSpan(
                    start = 0,
                    end = block.text.length,
                    isBold = style.isBold,
                    isItalic = style.isItalic,
                    fontSizeSp = style.fontSizeSp,
                    textColorHex = style.textColorHex
                )
                block.copy(
                    spans = listOf(baseSpan),
                    spaceBeforePt = style.spaceBeforePt,
                    spaceAfterPt = style.spaceAfterPt,
                    lineSpacingMultiplier = style.lineSpacingMultiplier,
                    firstLineIndentPt = style.firstLineIndentPt
                )
            } else block
        }
        return document.copy(blocks = updated)
    }

    override fun revert(document: RichDocument): RichDocument {
        val updated = document.blocks.map { block ->
            if (block.id == blockId && block is ParagraphBlock) {
                oldParagraph
            } else block
        }
        return document.copy(blocks = updated)
    }
}


