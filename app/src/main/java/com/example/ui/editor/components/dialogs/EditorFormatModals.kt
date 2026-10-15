package com.example.ui.editor.components.dialogs

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.CustomStyle
import com.example.data.model.ParagraphBlock
import com.example.data.model.RichDocument
import com.example.data.model.TableCell
import com.example.data.model.TableBlock
import com.example.ui.editor.EditorViewModel
import com.example.ui.editor.components.PageBordersDialog
import com.example.ui.editor.components.PageColorDialog

@Composable
fun EditorFormatModals(
    viewModel: EditorViewModel,
    document: RichDocument?,
    activeParagraph: ParagraphBlock?,
    showPageBordersDialog: Boolean,
    onDismissPageBordersDialog: () -> Unit,
    showPageColorDialog: Boolean,
    onDismissPageColorDialog: () -> Unit,
    showTypographyEffectsDialog: Boolean,
    onDismissTypographyEffectsDialog: () -> Unit,
    showParagraphSpacingDialog: Boolean,
    onDismissParagraphSpacingDialog: () -> Unit,
    showCreateCustomStyleDialog: Boolean,
    onDismissCreateCustomStyleDialog: () -> Unit,
    selectedCellForProperties: Triple<TableBlock, Int, Int>?,
    onDismissCellProperties: () -> Unit
) {
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

    if (showPageColorDialog && document != null) {
        PageColorDialog(
            initialColorHex = document.layoutConfig.pageBackgroundHex,
            onDismiss = onDismissPageColorDialog,
            onApply = { colorHex ->
                viewModel.setPageBackgroundColor(colorHex)
            }
        )
    }

    if (showTypographyEffectsDialog) {
        val activeFormatting by viewModel.activeFormatting.collectAsStateWithLifecycle()
        TypographyAndEffectsDialog(
            initialLetterSpacingSp = activeFormatting.letterSpacingSp,
            initialUnderlineStyle = activeFormatting.underlineStyle,
            initialUnderlineColorHex = activeFormatting.underlineColorHex,
            initialStrikethroughStyle = activeFormatting.strikethroughStyle,
            initialShadowColorHex = activeFormatting.shadowColorHex,
            onDismiss = onDismissTypographyEffectsDialog,
            onApply = { letterSpacingSp, underlineStyle, underlineColorHex, strikethroughStyle, shadowColorHex ->
                viewModel.applyTypographyEffects(letterSpacingSp, underlineStyle, underlineColorHex, strikethroughStyle, shadowColorHex)
                onDismissTypographyEffectsDialog()
            }
        )
    }

    if (showParagraphSpacingDialog && activeParagraph != null) {
        ParagraphSpacingAndIndentsDialog(
            initialFirstLinePt = activeParagraph.firstLineIndentPt,
            initialHangingPt = activeParagraph.hangingIndentPt,
            initialSpaceBeforePt = activeParagraph.spaceBeforePt,
            initialSpaceAfterPt = activeParagraph.spaceAfterPt,
            initialDropCapLines = activeParagraph.dropCapLines,
            onDismiss = onDismissParagraphSpacingDialog,
            onApply = { firstLinePt, hangingPt, spaceBeforePt, spaceAfterPt, dropCapLines ->
                viewModel.updateParagraphIndents(firstLinePt, hangingPt, spaceBeforePt, spaceAfterPt, dropCapLines)
                onDismissParagraphSpacingDialog()
            }
        )
    }

    if (showCreateCustomStyleDialog && activeParagraph != null) {
        val activeFormatting by viewModel.activeFormatting.collectAsStateWithLifecycle()
        val templateStyle = CustomStyle(
            name = "My Style",
            fontSizeSp = activeFormatting.fontSizeSp ?: 15f,
            isBold = activeFormatting.isBold,
            isItalic = activeFormatting.isItalic,
            textColorHex = activeFormatting.textColorHex,
            spaceBeforePt = activeParagraph.spaceBeforePt,
            spaceAfterPt = activeParagraph.spaceAfterPt,
            lineSpacingMultiplier = activeParagraph.lineSpacingMultiplier,
            firstLineIndentPt = activeParagraph.firstLineIndentPt
        )
        CreateCustomStyleDialog(
            initialStyle = templateStyle,
            onDismiss = onDismissCreateCustomStyleDialog,
            onSave = { style ->
                viewModel.saveCustomStyle(style)
                onDismissCreateCustomStyleDialog()
            }
        )
    }

    selectedCellForProperties?.let { (table, r, c) ->
        val cell = table.cells.getOrNull(r)?.getOrNull(c) ?: TableCell()
        TableCellPropertiesDialog(
            cell = cell,
            onDismiss = onDismissCellProperties,
            onApply = { vAlign, bgHex ->
                viewModel.updateTableCellFormatting(table.id, r, c, vAlign, bgHex)
                onDismissCellProperties()
            },
            onMergeCells = {
                viewModel.mergeTableCells(table.id, r, c, 2)
                onDismissCellProperties()
            },
            onSplitCell = {
                viewModel.splitTableCell(table.id, r, c)
                onDismissCellProperties()
            }
        )
    }
}
