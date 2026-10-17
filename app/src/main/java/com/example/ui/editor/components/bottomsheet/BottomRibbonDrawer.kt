package com.example.ui.editor.components.bottomsheet

import android.content.Context
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.PickVisualMediaRequest
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.ParagraphBlock
import com.example.ui.editor.EditorDialogsState
import com.example.ui.editor.EditorViewModel
import com.example.ui.editor.RibbonTab

/**
 * Modern Bottom Ribbon Drawer docked at the bottom of the screen.
 * Implements exact MS Word Mobile hierarchical navigation with submenus and back-stack.
 */
@Composable
fun BottomRibbonDrawer(
    isExpanded: Boolean,
    activeTab: RibbonTab,
    onTabSelected: (RibbonTab) -> Unit,
    onDismiss: () -> Unit,
    viewModel: EditorViewModel,
    dState: EditorDialogsState,
    activeParagraph: ParagraphBlock?,
    context: Context,
    photoPickerLauncher: ActivityResultLauncher<PickVisualMediaRequest>,
    safDocxSaveLauncher: ActivityResultLauncher<String>?,
    modifier: Modifier = Modifier
) {
    var homeSubmenu by remember { mutableStateOf(HomeSubmenu.ROOT) }
    val canUndo by viewModel.canUndo.collectAsStateWithLifecycle()
    val canRedo by viewModel.canRedo.collectAsStateWithLifecycle()

    AnimatedVisibility(
        visible = isExpanded,
        enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
        modifier = modifier
    ) {
        Surface(
            shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            shadowElevation = 16.dp,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 280.dp, max = 380.dp)
                .testTag("bottom_ribbon_drawer")
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Drag Handle
                Box(
                    modifier = Modifier.fillMaxWidth().padding(top = 6.dp, bottom = 2.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(modifier = Modifier.width(36.dp).height(4.dp).clip(CircleShape).background(MaterialTheme.colorScheme.outlineVariant))
                }

                // Header: Either Submenu Back Navigation (< | Title) OR Main Tab Navigation (Tab ⬍ + Undo + Redo)
                if (activeTab == RibbonTab.HOME && homeSubmenu != HomeSubmenu.ROOT) {
                    SubmenuHeaderBar(
                        submenu = homeSubmenu,
                        onBack = {
                            homeSubmenu = when (homeSubmenu) {
                                HomeSubmenu.SPECIAL_INDENT, HomeSubmenu.LINE_SPACING -> HomeSubmenu.PARAGRAPH_FORMATTING
                                HomeSubmenu.CHANGE_CASE -> HomeSubmenu.MORE_FORMATTING
                                else -> HomeSubmenu.ROOT
                            }
                        },
                        onDismiss = onDismiss
                    )
                } else {
                    MainRibbonHeaderBar(
                        activeTab = activeTab,
                        onTabSelected = { tab ->
                            homeSubmenu = HomeSubmenu.ROOT
                            onTabSelected(tab)
                        },
                        canUndo = canUndo,
                        canRedo = canRedo,
                        onUndo = viewModel::undo,
                        onRedo = viewModel::redo,
                        onOpenFind = viewModel::toggleFindReplace,
                        onDismiss = onDismiss
                    )
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), thickness = 0.5.dp)

                // Body
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                ) {
                    when (activeTab) {
                        RibbonTab.HOME -> {
                            BottomRibbonHomePane(
                                viewModel = viewModel,
                                activeParagraph = activeParagraph,
                                dState = dState,
                                currentSubmenu = homeSubmenu,
                                onNavigateSubmenu = { homeSubmenu = it }
                            )
                        }
                        RibbonTab.INSERT -> {
                            BottomRibbonInsertPane(viewModel = viewModel, dState = dState, photoPickerLauncher = photoPickerLauncher)
                        }
                        RibbonTab.LAYOUT -> {
                            BottomRibbonLayoutPane(viewModel = viewModel, dState = dState)
                        }
                        RibbonTab.REVIEW, RibbonTab.FILE, RibbonTab.VIEW, RibbonTab.DRAW -> {
                            BottomRibbonReviewFilePane(activeTab = activeTab, viewModel = viewModel, dState = dState, context = context, safDocxSaveLauncher = safDocxSaveLauncher)
                        }
                    }
                }
            }
        }
    }
}
