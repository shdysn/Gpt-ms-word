package com.example.ui.editor.components

import android.content.Context
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.PickVisualMediaRequest
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextRange
import com.example.data.model.ParagraphBlock
import com.example.editor.engine.ActiveFormatting
import com.example.ui.editor.EditorDialogsState
import com.example.ui.editor.EditorViewModel
import com.example.ui.editor.RibbonTab
import com.example.ui.editor.components.bottomsheet.BottomRibbonDrawer

/**
 * Encapsulates the keyboard accessory Quick Format Bar and the Expandable
 * Bottom Ribbon Drawer docked at the bottom of the screen.
 */
@Composable
fun EditorQuickActionsArea(
    viewModel: EditorViewModel,
    dState: EditorDialogsState,
    context: Context,
    selection: TextRange,
    isDocumentAllSelected: Boolean,
    isImeVisible: Boolean,
    isBottomRibbonExpanded: Boolean,
    activeRibbonTab: RibbonTab,
    activeFormatting: ActiveFormatting,
    activeParagraph: ParagraphBlock?,
    onToggleExpandRibbon: () -> Unit,
    onDismissRibbon: () -> Unit,
    photoPickerLauncher: ActivityResultLauncher<PickVisualMediaRequest>,
    safDocxSaveLauncher: ActivityResultLauncher<String>?,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        val currentDoc = viewModel.currentDocument
        val activeIndex = viewModel.activeBlockIndex.value
        val activeShape = currentDoc?.blocks?.getOrNull(activeIndex) as? com.example.data.model.ShapeBlock

        if (activeShape != null && !isBottomRibbonExpanded) {
            com.example.ui.editor.components.shapes.ShapeBottomFormatBar(
                shape = activeShape,
                onUpdateColors = { bg, bdr, strokeW ->
                    viewModel.updateShape(
                        blockId = activeShape.id,
                        shapeType = activeShape.shapeType,
                        title = activeShape.title,
                        content = activeShape.content,
                        bgHex = bg,
                        borderHex = bdr,
                        strokeWidthPt = strokeW,
                        rotationDeg = activeShape.rotationDeg,
                        widthDp = activeShape.widthDp,
                        heightDp = activeShape.heightDp
                    )
                },
                onRotate90 = {
                    val newRot = (activeShape.rotationDeg + 90f) % 360f
                    viewModel.updateShape(
                        blockId = activeShape.id,
                        shapeType = activeShape.shapeType,
                        title = activeShape.title,
                        content = activeShape.content,
                        bgHex = activeShape.backgroundColorHex,
                        borderHex = activeShape.borderColorHex,
                        strokeWidthPt = activeShape.strokeWidthPt,
                        rotationDeg = newRot,
                        widthDp = activeShape.widthDp,
                        heightDp = activeShape.heightDp
                    )
                },
                onOpenFullProperties = {
                    dState.selectedShapeForEdit = activeShape
                },
                onStatusMessage = { viewModel.showStatusMessage(it) }
            )
        } else {
            EditorQuickFormatBar(
                selection = selection,
                isDocumentAllSelected = isDocumentAllSelected,
                isKeyboardVisible = isImeVisible,
                isRibbonExpanded = isBottomRibbonExpanded,
                activeFormatting = activeFormatting,
                canCut = isDocumentAllSelected || (!selection.collapsed && selection.min < selection.max),
                canCopy = isDocumentAllSelected || (!selection.collapsed && selection.min < selection.max) || (activeParagraph?.text?.isNotEmpty() == true),
                canPaste = true,
                onCut = { viewModel.cutSelection(context) },
                onCopy = { viewModel.copySelection(context) },
                onPaste = { viewModel.pasteFromClipboard(context) },
                onSelectAll = { viewModel.selectAllDocument() },
                onToggleBold = { viewModel.toggleBold() },
                onToggleItalic = { viewModel.toggleItalic() },
                onToggleUnderline = { viewModel.toggleUnderline() },
                onToggleStrikethrough = { viewModel.toggleStrikethrough() },
                onHighlightClick = {
                    if (activeFormatting.highlightColorHex != null) viewModel.setHighlightColor(null)
                    else viewModel.setHighlightColor("#FEF08A")
                },
                onTextColorClick = {
                    if (activeFormatting.textColorHex != null) viewModel.setTextColor(null)
                    else viewModel.setTextColor("#185ABD")
                },
                onToggleExpandRibbon = onToggleExpandRibbon,
                onDeselect = { viewModel.clearDocumentSelection() }
            )
        }

        BottomRibbonDrawer(
            isExpanded = isBottomRibbonExpanded,
            activeTab = activeRibbonTab,
            onTabSelected = viewModel::selectRibbonTab,
            onDismiss = onDismissRibbon,
            viewModel = viewModel,
            dState = dState,
            activeParagraph = activeParagraph,
            context = context,
            photoPickerLauncher = photoPickerLauncher,
            safDocxSaveLauncher = safDocxSaveLauncher
        )
    }
}
