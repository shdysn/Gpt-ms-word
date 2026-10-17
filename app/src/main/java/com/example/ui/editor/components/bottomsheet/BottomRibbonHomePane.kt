package com.example.ui.editor.components.bottomsheet

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.ListType
import com.example.data.model.ParagraphBlock
import com.example.ui.editor.EditorDialogsState
import com.example.ui.editor.EditorViewModel

/**
 * Orchestrator for Home tab within the Bottom Ribbon Drawer.
 * Renders either the root Home menu or active submenus matching MS Word Mobile.
 */
@Composable
fun BottomRibbonHomePane(
    viewModel: EditorViewModel,
    activeParagraph: ParagraphBlock?,
    dState: EditorDialogsState,
    currentSubmenu: HomeSubmenu,
    onNavigateSubmenu: (HomeSubmenu) -> Unit,
    modifier: Modifier = Modifier
) {
    val formatting by viewModel.activeFormatting.collectAsStateWithLifecycle()

    Box(modifier = modifier.fillMaxWidth()) {
        when (currentSubmenu) {
            HomeSubmenu.ROOT, HomeSubmenu.FONT_FAMILY -> {
                BottomRibbonHomeRootView(
                    formatting = formatting,
                    activeParagraph = activeParagraph,
                    onNavigate = onNavigateSubmenu,
                    onToggleBold = viewModel::toggleBold,
                    onToggleItalic = viewModel::toggleItalic,
                    onToggleUnderline = viewModel::toggleUnderline,
                    onToggleStrikethrough = viewModel::toggleStrikethrough,
                    onClearFormatting = viewModel::clearFormatting,
                    onSetAlignment = viewModel::setAlignment,
                    onToggleRtl = viewModel::toggleRtl,
                    onIncreaseIndent = viewModel::increaseIndent,
                    onDecreaseIndent = viewModel::decreaseIndent,
                    onOpenFind = viewModel::toggleFindReplace,
                    onSetFontFamily = viewModel::setFontFamily,
                    onSetFontSize = viewModel::setFontSize
                )
            }
            HomeSubmenu.FONT_COLOR -> {
                FontColorSubmenuView(
                    selectedColorHex = formatting.textColorHex,
                    onSelectColor = { hex ->
                        viewModel.setTextColor(hex)
                        onNavigateSubmenu(HomeSubmenu.ROOT)
                    }
                )
            }
            HomeSubmenu.HIGHLIGHT_COLOR -> {
                HighlightColorSubmenuView(
                    selectedColorHex = formatting.highlightColorHex,
                    onSelectColor = { hex ->
                        viewModel.setHighlightColor(hex)
                        onNavigateSubmenu(HomeSubmenu.ROOT)
                    }
                )
            }
            HomeSubmenu.BULLETS -> {
                BulletsSubmenuView(
                    currentListType = activeParagraph?.listType ?: ListType.NONE,
                    onSelectBullet = { style ->
                        if (style == BulletGalleryStyle.NONE) {
                            if (activeParagraph?.listType == ListType.BULLET) viewModel.toggleListType(ListType.BULLET)
                        } else {
                            viewModel.toggleListType(ListType.BULLET)
                        }
                        onNavigateSubmenu(HomeSubmenu.ROOT)
                    }
                )
            }
            HomeSubmenu.NUMBERING -> {
                NumberingSubmenuView(
                    currentListType = activeParagraph?.listType ?: ListType.NONE,
                    onSelectNumbering = { style ->
                        if (style == NumberingGalleryStyle.NONE) {
                            if (activeParagraph?.listType == ListType.NUMBERED) viewModel.toggleListType(ListType.NUMBERED)
                        } else {
                            viewModel.toggleListType(ListType.NUMBERED)
                        }
                        onNavigateSubmenu(HomeSubmenu.ROOT)
                    }
                )
            }
            HomeSubmenu.PARAGRAPH_FORMATTING -> {
                ParagraphSubmenuView(
                    activeParagraph = activeParagraph,
                    onNavigate = onNavigateSubmenu,
                    onToggleSpaceBefore = {
                        val before = if ((activeParagraph?.spaceBeforePt ?: 0f) > 0f) 0f else 12f
                        viewModel.updateParagraphIndents(
                            fIndent = activeParagraph?.firstLineIndentPt ?: 0f,
                            hIndent = activeParagraph?.hangingIndentPt ?: 0f,
                            sBefore = before,
                            sAfter = activeParagraph?.spaceAfterPt ?: 0f
                        )
                    },
                    onToggleSpaceAfter = {
                        val after = if ((activeParagraph?.spaceAfterPt ?: 0f) > 0f) 0f else 8f
                        viewModel.updateParagraphIndents(
                            fIndent = activeParagraph?.firstLineIndentPt ?: 0f,
                            hIndent = activeParagraph?.hangingIndentPt ?: 0f,
                            sBefore = activeParagraph?.spaceBeforePt ?: 0f,
                            sAfter = after
                        )
                    }
                )
            }
            HomeSubmenu.SPECIAL_INDENT -> {
                SpecialIndentSubmenuView(
                    activeParagraph = activeParagraph,
                    onSetIndent = { firstLine, hanging ->
                        viewModel.updateParagraphIndents(
                            fIndent = firstLine,
                            hIndent = hanging,
                            sBefore = activeParagraph?.spaceBeforePt ?: 0f,
                            sAfter = activeParagraph?.spaceAfterPt ?: 0f
                        )
                        onNavigateSubmenu(HomeSubmenu.PARAGRAPH_FORMATTING)
                    }
                )
            }
            HomeSubmenu.LINE_SPACING -> {
                LineSpacingSubmenuView(
                    currentSpacing = activeParagraph?.lineSpacingMultiplier ?: 1.15f,
                    onSelectSpacing = { spacing ->
                        viewModel.setLineSpacing(spacing)
                        onNavigateSubmenu(HomeSubmenu.PARAGRAPH_FORMATTING)
                    }
                )
            }
            HomeSubmenu.STYLES -> {
                StylesSubmenuView(
                    currentStyle = activeParagraph?.headingStyle ?: com.example.data.model.HeadingStyle.NORMAL,
                    onSelectStyle = { style ->
                        viewModel.setHeadingStyle(style)
                        onNavigateSubmenu(HomeSubmenu.ROOT)
                    }
                )
            }
            HomeSubmenu.MORE_FORMATTING -> {
                MoreFormattingSubmenuView(
                    isSubscript = formatting.isSubscript,
                    isSuperscript = formatting.isSuperscript,
                    onToggleSubscript = viewModel::toggleSubscript,
                    onToggleSuperscript = viewModel::toggleSuperscript,
                    onNavigateToChangeCase = { onNavigateSubmenu(HomeSubmenu.CHANGE_CASE) }
                )
            }
            HomeSubmenu.CHANGE_CASE -> {
                ChangeCaseSubmenuView(
                    onSelectCase = { caseType ->
                        viewModel.changeCase(caseType)
                        onNavigateSubmenu(HomeSubmenu.MORE_FORMATTING)
                    }
                )
            }
        }
    }
}
