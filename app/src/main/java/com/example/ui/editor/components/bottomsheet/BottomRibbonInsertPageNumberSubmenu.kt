package com.example.ui.editor.components.bottomsheet

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.layout.PageNumberFormat
import com.example.data.model.TextAlignment
import com.example.ui.editor.EditorViewModel

/**
 * Microsoft Word Mobile Page Number Submenu.
 * Displays a 3x2 grid of mini page cards, number format selector,
 * "Include Page Count" toggle, and "Remove Page Numbers" action.
 */
@Composable
fun BottomRibbonInsertPageNumberSubmenu(
    viewModel: EditorViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var includePageCount by remember { mutableStateOf(false) }
    var selectedFormatIndex by remember { mutableStateOf(0) }
    var isFormatDropdownOpen by remember { mutableStateOf(false) }
    var selectedSlot by remember { mutableStateOf(PageNumberSlot.FOOTER_RIGHT) }

    val formats = listOf("1, 2, 3, ...", "a, b, c, ...", "A, B, C, ...", "i, ii, iii, ...", "I, II, III, ...")

    fun applySlot(slot: PageNumberSlot) {
        selectedSlot = slot
        val pattern = if (includePageCount) "Page {page} of {pages}" else "{page}"
        val alignment = when (slot.alignmentName) {
            "START" -> TextAlignment.START
            "CENTER" -> TextAlignment.CENTER
            else -> TextAlignment.END
        }
        if (slot.isHeader) {
            viewModel.setHeaderFooter(headerText = pattern, showHeader = true, footerText = "", showFooter = false)
        } else {
            viewModel.setPageNumberPreset(pattern, alignment)
        }
        viewModel.showStatusMessage("Page number set to ${slot.label}")
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // 3x2 Grid of Mini Page Cards
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            MiniPageCard(slot = PageNumberSlot.HEADER_LEFT, isSelected = selectedSlot == PageNumberSlot.HEADER_LEFT, onClick = { applySlot(PageNumberSlot.HEADER_LEFT) })
            MiniPageCard(slot = PageNumberSlot.HEADER_CENTER, isSelected = selectedSlot == PageNumberSlot.HEADER_CENTER, onClick = { applySlot(PageNumberSlot.HEADER_CENTER) })
            MiniPageCard(slot = PageNumberSlot.HEADER_RIGHT, isSelected = selectedSlot == PageNumberSlot.HEADER_RIGHT, onClick = { applySlot(PageNumberSlot.HEADER_RIGHT) })
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            MiniPageCard(slot = PageNumberSlot.FOOTER_LEFT, isSelected = selectedSlot == PageNumberSlot.FOOTER_LEFT, onClick = { applySlot(PageNumberSlot.FOOTER_LEFT) })
            MiniPageCard(slot = PageNumberSlot.FOOTER_CENTER, isSelected = selectedSlot == PageNumberSlot.FOOTER_CENTER, onClick = { applySlot(PageNumberSlot.FOOTER_CENTER) })
            MiniPageCard(slot = PageNumberSlot.FOOTER_RIGHT, isSelected = selectedSlot == PageNumberSlot.FOOTER_RIGHT, onClick = { applySlot(PageNumberSlot.FOOTER_RIGHT) })
        }

        Spacer(modifier = Modifier.height(4.dp))
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), thickness = 0.5.dp)

        // Number Format: 1, 2, 3, ... >
        Box(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isFormatDropdownOpen = true }
                    .padding(horizontal = 4.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Number Format:",
                        fontSize = 12.sp,
                        color = Color(0xFF666666)
                    )
                    Text(
                        text = formats[selectedFormatIndex],
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Icon(
                    Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = "Change Format",
                    tint = Color(0xFF757575),
                    modifier = Modifier.size(20.dp)
                )
            }

            DropdownMenu(
                expanded = isFormatDropdownOpen,
                onDismissRequest = { isFormatDropdownOpen = false }
            ) {
                formats.forEachIndexed { index, format ->
                    DropdownMenuItem(
                        text = { Text(format, fontWeight = if (index == selectedFormatIndex) FontWeight.Bold else FontWeight.Normal) },
                        onClick = {
                            selectedFormatIndex = index
                            isFormatDropdownOpen = false
                            val pnf = when (index) {
                                1 -> PageNumberFormat.LETTERS
                                2 -> PageNumberFormat.LETTERS
                                3 -> PageNumberFormat.ROMAN_LOWER
                                4 -> PageNumberFormat.ROMAN_UPPER
                                else -> PageNumberFormat.ARABIC_NUMERALS
                            }
                            val currentLayout = viewModel.currentDocument?.layoutConfig ?: com.example.core.layout.PageLayoutConfig()
                            viewModel.updatePageLayoutConfig(currentLayout.copy(pageNumberFormat = pnf))
                            viewModel.showStatusMessage("Format updated: $format")
                        }
                    )
                }
            }
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), thickness = 0.5.dp)

        // Include Page Count Checkbox Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    includePageCount = !includePageCount
                    applySlot(selectedSlot)
                }
                .padding(horizontal = 4.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = includePageCount,
                onCheckedChange = {
                    includePageCount = it
                    applySlot(selectedSlot)
                },
                colors = CheckboxDefaults.colors(checkedColor = Color(0xFF0F6CBD))
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Include Page Count",
                style = MaterialTheme.typography.bodyLarge,
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), thickness = 0.5.dp)

        // Remove Page Numbers Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    viewModel.setHeaderFooter(headerText = "", footerText = "", showHeader = false, showFooter = false)
                    viewModel.showStatusMessage("Page numbers removed")
                    onBack()
                }
                .padding(horizontal = 4.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(modifier = Modifier.size(24.dp), contentAlignment = Alignment.Center) {
                WordRemovePageNumberIcon()
            }
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = "Remove Page Numbers",
                style = MaterialTheme.typography.bodyLarge,
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun MiniPageCard(
    slot: PageNumberSlot,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val borderColor = if (isSelected) Color(0xFF0F6CBD) else Color(0xFF444444)
    val bgColor = if (isSelected) Color(0xFFEFF6FF) else Color.White

    Surface(
        shape = RoundedCornerShape(2.dp),
        color = bgColor,
        border = BorderStroke(if (isSelected) 2.dp else 1.dp, borderColor),
        modifier = Modifier
            .size(width = 68.dp, height = 78.dp)
            .clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 6.dp, vertical = 6.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            if (slot.isHeader) {
                // Header Page Number
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = when (slot.alignmentName) {
                    "START" -> Alignment.CenterStart
                    "CENTER" -> Alignment.Center
                    else -> Alignment.CenterEnd
                }) {
                    Text("#", color = Color(0xFF0078D4), fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
                MiniTextLines()
            } else {
                // Footer Page Number
                MiniTextLines()
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = when (slot.alignmentName) {
                    "START" -> Alignment.CenterStart
                    "CENTER" -> Alignment.Center
                    else -> Alignment.CenterEnd
                }) {
                    Text("#", color = Color(0xFF0078D4), fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun MiniTextLines() {
    Canvas(modifier = Modifier.fillMaxWidth().height(26.dp)) {
        val lineCol = Color(0xFF757575)
        val stroke = 1.3.dp.toPx()
        val ySpacing = size.height / 4f
        for (i in 1..3) {
            drawLine(lineCol, Offset(0f, ySpacing * i), Offset(size.width, ySpacing * i), strokeWidth = stroke, cap = StrokeCap.Round)
        }
    }
}
