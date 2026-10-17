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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.ParagraphBlock
import com.example.ui.editor.EditorDialogsState
import com.example.ui.editor.EditorViewModel
import com.example.ui.editor.RibbonTab
import com.example.ui.editor.components.getRibbonTabMetadata
import com.example.ui.theme.WordPrimary

/**
 * Modern Bottom Ribbon Drawer docked at the bottom of the screen in place of the software keyboard.
 * Replaces cumbersome desktop ribbon headers with an intuitive, one-thumb Material 3 bottom drawer
 * inspired by Microsoft Word Mobile.
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
    AnimatedVisibility(
        visible = isExpanded,
        enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
        modifier = modifier
    ) {
        Surface(
            shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            shadowElevation = 16.dp,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 280.dp, max = 360.dp)
                .testTag("bottom_ribbon_drawer")
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Header: Drag Handle & Navigation
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
                        .padding(bottom = 6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp, bottom = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .width(36.dp)
                                .height(4.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.outlineVariant)
                        )
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Ribbon Tab Selector (Horizontal Scrollable Pills)
                        Row(
                            modifier = Modifier
                                .weight(1f)
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RibbonTab.entries.forEach { tab ->
                                val isSelected = activeTab == tab
                                val (tabIcon, tabTitle) = getRibbonTabMetadata(tab)
                                Surface(
                                    shape = RoundedCornerShape(16.dp),
                                    color = if (isSelected) WordPrimary else Color.Transparent,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(16.dp))
                                        .clickable { onTabSelected(tab) }
                                        .testTag("bottom_tab_${tab.name.lowercase()}")
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = tabIcon,
                                            contentDescription = null,
                                            tint = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(15.dp)
                                        )
                                        Spacer(modifier = Modifier.width(5.dp))
                                        Text(
                                            text = tabTitle,
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }

                        // Close / Collapse Button
                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .size(36.dp)
                                .testTag("close_bottom_ribbon_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowDown,
                                contentDescription = "Close Tools Panel",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), thickness = 0.5.dp)

                // Scrollable Body for Categorized Tools
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    when (activeTab) {
                        RibbonTab.HOME -> {
                            BottomRibbonHomePane(
                                viewModel = viewModel,
                                activeParagraph = activeParagraph,
                                dState = dState
                            )
                        }
                        RibbonTab.INSERT -> {
                            BottomRibbonInsertPane(
                                viewModel = viewModel,
                                dState = dState,
                                photoPickerLauncher = photoPickerLauncher
                            )
                        }
                        RibbonTab.LAYOUT -> {
                            BottomRibbonLayoutPane(
                                viewModel = viewModel,
                                dState = dState
                            )
                        }
                        RibbonTab.REVIEW, RibbonTab.FILE, RibbonTab.VIEW, RibbonTab.DRAW -> {
                            BottomRibbonReviewFilePane(
                                activeTab = activeTab,
                                viewModel = viewModel,
                                dState = dState,
                                context = context,
                                safDocxSaveLauncher = safDocxSaveLauncher
                            )
                        }
                    }
                }
            }
        }
    }
}
