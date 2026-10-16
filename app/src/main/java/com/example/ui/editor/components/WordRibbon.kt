package com.example.ui.editor.components

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircleOutline
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Gesture
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.RateReview
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.CustomStyle
import com.example.data.model.ParagraphBlock
import com.example.editor.engine.CaseType
import com.example.ui.editor.EditorViewModel
import com.example.ui.editor.RibbonTab
import com.example.ui.theme.WordPrimary

// Re-export ribbon buttons for backward compatibility
@Composable
fun RibbonActionButton(icon: ImageVector, label: String, onClick: () -> Unit) =
    com.example.ui.editor.components.ribbon.RibbonActionButton(icon, label, onClick)

@Composable
fun RibbonToggleButton(icon: ImageVector, label: String, isSelected: Boolean, onClick: () -> Unit, tag: String = "") =
    com.example.ui.editor.components.ribbon.RibbonToggleButton(icon, label, isSelected, onClick, tag)

/** WordRibbon container component with modern tab dropdown selector. */
@Composable
fun WordRibbon(
    activeTab: RibbonTab,
    onTabSelected: (RibbonTab) -> Unit,
    isRibbonVisible: Boolean,
    onToggleRibbon: () -> Unit,
    activeParagraph: ParagraphBlock?,
    viewModel: EditorViewModel,
    context: Context,
    onOpenTableDialog: () -> Unit,
    onOpenPhotoPicker: () -> Unit,
    onOpenLinkDialog: () -> Unit,
    onOpenWatermarkDialog: () -> Unit,
    onOpenLayoutDialog: () -> Unit,
    onOpenPasswordDialog: () -> Unit,
    onOpenVersionHistoryDialog: () -> Unit,
    onOpenStatsDialog: () -> Unit,
    onOpenHeaderFooterDialog: () -> Unit,
    onOpenPageNumberDialog: () -> Unit,
    onStartVoiceTyping: () -> Unit,
    onOpenSignatureDialog: () -> Unit,
    onOpenShapeDialog: () -> Unit,
    onOpenCommentsDialog: () -> Unit = {}, onOpenTrackChangesDialog: () -> Unit = {},
    onOpenPageBordersDialog: () -> Unit = {}, onOpenPageColorDialog: () -> Unit = {},
    onOpenFootnoteDialog: () -> Unit = {}, onOpenSmartWritingDialog: () -> Unit = {},
    onOpenOcrScanDialog: () -> Unit = {}, onOpenOutline: () -> Unit = {},
    onClearFormatting: () -> Unit = {}, onChangeCase: (CaseType) -> Unit = {},
    onOpenTypographyEffectsDialog: () -> Unit = {}, onOpenParagraphSpacingDialog: () -> Unit = {},
    onToggleChecklist: () -> Unit = {}, onApplyCustomStyle: (CustomStyle) -> Unit = {},
    onCreateCustomStyle: () -> Unit = {}, onQuickSave: () -> Unit = {},
    onSaveAs: () -> Unit = {}, onSaveSafDocx: () -> Unit = {},
    onOpenExportOptions: () -> Unit = {}, onPrint: () -> Unit = {},
    onToggleFindReplace: () -> Unit = {}, modifier: Modifier = Modifier
) {
    val activeFormatting by viewModel.activeFormatting.collectAsStateWithLifecycle()
    val isFormatPainterActive by viewModel.isFormatPainterActive.collectAsStateWithLifecycle()
    val currentDocument by viewModel.document.collectAsStateWithLifecycle()

    AnimatedVisibility(
        visible = isRibbonVisible,
        enter = expandVertically() + fadeIn(),
        exit = shrinkVertically() + fadeOut()
    ) {
        Surface(
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 2.dp,
            modifier = modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Ribbon Header Bar with Modern Tab Dropdown Selector (including File tab)
                var isTabDropdownOpen by remember { mutableStateOf(false) }
                val arrowRotation by animateFloatAsState(
                    targetValue = if (isTabDropdownOpen) 180f else 0f,
                    label = "tabArrowRotation"
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f))
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Box {
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = WordPrimary.copy(alpha = 0.12f),
                            border = BorderStroke(1.dp, WordPrimary.copy(alpha = 0.4f)),
                            modifier = Modifier
                                .clickable { isTabDropdownOpen = true }
                                .testTag("ribbon_tab_dropdown_button")
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                            ) {
                                val (currentIcon, currentTitle) = getRibbonTabMetadata(activeTab)
                                Icon(
                                    imageVector = currentIcon,
                                    contentDescription = null,
                                    tint = WordPrimary,
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = currentTitle,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = WordPrimary
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    imageVector = Icons.Default.ArrowDropDown,
                                    contentDescription = "Select Ribbon Tab",
                                    tint = WordPrimary,
                                    modifier = Modifier
                                        .size(18.dp)
                                        .rotate(arrowRotation)
                                )
                            }
                        }

                        DropdownMenu(
                            expanded = isTabDropdownOpen,
                            onDismissRequest = { isTabDropdownOpen = false },
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            RibbonTab.entries.forEach { tab ->
                                val isSelected = activeTab == tab
                                val (tabIcon, tabTitle) = getRibbonTabMetadata(tab)
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = tabTitle,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isSelected) WordPrimary else MaterialTheme.colorScheme.onSurface
                                        )
                                    },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = tabIcon,
                                            contentDescription = null,
                                            tint = if (isSelected) WordPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    },
                                    trailingIcon = if (isSelected) {
                                        {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = "Selected",
                                                tint = WordPrimary,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    } else null,
                                    onClick = {
                                        isTabDropdownOpen = false
                                        onTabSelected(tab)
                                    },
                                    modifier = Modifier.testTag("ribbon_tab_item_${tab.name.lowercase()}")
                                )
                            }
                        }
                    }

                    // Collapse ribbon quick button
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                        modifier = Modifier
                            .clickable { onToggleRibbon() }
                            .testTag("ribbon_collapse_button")
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowUp,
                                contentDescription = "Collapse Ribbon",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f), thickness = 0.5.dp)

                WordRibbonContent(
                    activeTab = activeTab,
                    activeParagraph = activeParagraph,
                    activeFormatting = activeFormatting,
                    isFormatPainterActive = isFormatPainterActive,
                    viewModel = viewModel,
                    context = context,
                    customStyles = currentDocument?.customStyles ?: emptyList(),
                    onOpenTableDialog = onOpenTableDialog,
                    onOpenPhotoPicker = onOpenPhotoPicker,
                    onOpenLinkDialog = onOpenLinkDialog,
                    onOpenWatermarkDialog = onOpenWatermarkDialog,
                    onOpenLayoutDialog = onOpenLayoutDialog,
                    onOpenPasswordDialog = onOpenPasswordDialog,
                    onOpenVersionHistoryDialog = onOpenVersionHistoryDialog,
                    onOpenStatsDialog = onOpenStatsDialog,
                    onOpenHeaderFooterDialog = onOpenHeaderFooterDialog,
                    onOpenPageNumberDialog = onOpenPageNumberDialog,
                    onStartVoiceTyping = onStartVoiceTyping,
                    onOpenSignatureDialog = onOpenSignatureDialog,
                    onOpenShapeDialog = onOpenShapeDialog,
                    onOpenCommentsDialog = onOpenCommentsDialog,
                    onOpenTrackChangesDialog = onOpenTrackChangesDialog,
                    onOpenPageBordersDialog = onOpenPageBordersDialog,
                    onOpenPageColorDialog = onOpenPageColorDialog,
                    onOpenFootnoteDialog = onOpenFootnoteDialog,
                    onOpenSmartWritingDialog = onOpenSmartWritingDialog,
                    onOpenOcrScanDialog = onOpenOcrScanDialog,
                    onOpenOutline = onOpenOutline,
                    onClearFormatting = onClearFormatting,
                    onChangeCase = onChangeCase,
                    onOpenTypographyEffectsDialog = onOpenTypographyEffectsDialog,
                    onOpenParagraphSpacingDialog = onOpenParagraphSpacingDialog,
                    onToggleChecklist = onToggleChecklist,
                    onApplyCustomStyle = onApplyCustomStyle,
                    onCreateCustomStyle = onCreateCustomStyle,
                    onTabSelected = onTabSelected,
                    onQuickSave = onQuickSave,
                    onSaveAs = onSaveAs,
                    onSaveSafDocx = onSaveSafDocx,
                    onOpenExportOptions = onOpenExportOptions,
                    onPrint = onPrint,
                    onToggleFindReplace = onToggleFindReplace
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 0.5.dp)
            }
        }
    }
}
