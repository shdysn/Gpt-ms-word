package com.example.ui.editor.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import com.example.data.model.ImageBlock
import com.example.data.model.TableBlock
import com.example.data.model.TableCell
import com.example.data.model.TextAlignment
import com.example.data.model.RichDocument
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.layout.MarginPreset
import com.example.core.layout.PageLayoutConfig
import com.example.core.layout.PageOrientation
import com.example.core.layout.PageSize
import com.example.ui.theme.WordPrimary

/**
 * Dialog for inserting structured tables into the document.
 */
@Composable
fun InsertTableDialog(
    onDismiss: () -> Unit,
    onConfirm: (rows: Int, cols: Int, hasBorders: Boolean, widthPercent: Int) -> Unit
) {
    var rowsText by remember { mutableStateOf("3") }
    var colsText by remember { mutableStateOf("3") }
    var hasBorders by remember { mutableStateOf(true) }
    var selectedWidthPercent by remember { mutableStateOf(100) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Insert Table") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Specify table dimensions and styling:",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                OutlinedTextField(
                    value = rowsText,
                    onValueChange = { rowsText = it },
                    label = { Text("Number of Rows (1-10)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("table_rows_input")
                )
                OutlinedTextField(
                    value = colsText,
                    onValueChange = { colsText = it },
                    label = { Text("Number of Columns (1-6)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("table_cols_input")
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { hasBorders = !hasBorders }
                ) {
                    Checkbox(
                        checked = hasBorders,
                        onCheckedChange = { hasBorders = it },
                        modifier = Modifier.testTag("table_borders_checkbox")
                    )
                    Text("Include table borders", fontSize = 13.sp)
                }
                Text("Table Width:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(50, 75, 100).forEach { w ->
                        FilterChip(
                            selected = selectedWidthPercent == w,
                            onClick = { selectedWidthPercent = w },
                            label = { Text("$w%", fontSize = 11.sp) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val r = rowsText.toIntOrNull()?.coerceIn(1, 10) ?: 3
                    val c = colsText.toIntOrNull()?.coerceIn(1, 6) ?: 3
                    onConfirm(r, c, hasBorders, selectedWidthPercent)
                },
                colors = ButtonDefaults.buttonColors(containerColor = WordPrimary),
                modifier = Modifier.testTag("confirm_insert_table")
            ) {
                Text("Insert")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

/**
 * Comprehensive Dialog for configuring page layout, paper size (A4, A5, Letter, Legal),
 * orientation, margins, page background, page borders, headers/footers, and page numbering.
 */
@Composable
fun PageLayoutDialog(
    currentLayout: PageLayoutConfig,
    onDismiss: () -> Unit,
    onSaveConfig: (PageLayoutConfig) -> Unit
) {
    var selectedPageSize by remember { mutableStateOf(currentLayout.pageSize) }
    var selectedOrientation by remember { mutableStateOf(currentLayout.orientation) }
    var selectedMargins by remember { mutableStateOf(currentLayout.marginPreset) }

    var topMarginMm by remember { mutableStateOf(currentLayout.topMarginMm) }
    var bottomMarginMm by remember { mutableStateOf(currentLayout.bottomMarginMm) }
    var leftMarginMm by remember { mutableStateOf(currentLayout.leftMarginMm) }
    var rightMarginMm by remember { mutableStateOf(currentLayout.rightMarginMm) }

    var showHeader by remember { mutableStateOf(currentLayout.showHeader) }
    var showFooter by remember { mutableStateOf(currentLayout.showFooter) }
    var headerText by remember { mutableStateOf(currentLayout.headerText) }
    var footerText by remember { mutableStateOf(currentLayout.footerText) }
    var differentFirstPage by remember { mutableStateOf(currentLayout.differentFirstPage) }

    var pageBackgroundHex by remember { mutableStateOf(currentLayout.pageBackgroundHex) }
    var borderStyle by remember { mutableStateOf(currentLayout.borderStyle) }
    var borderColorHex by remember { mutableStateOf(currentLayout.borderColorHex) }

    var pageNumberFormat by remember { mutableStateOf(currentLayout.pageNumberFormat) }
    var pageNumberStart by remember { mutableStateOf(currentLayout.pageNumberStart) }

    var selectedTab by remember { mutableStateOf(0) }
    val tabTitles = listOf("Page & Margins", "Appearance", "Header & Footer")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text("Page Setup", style = MaterialTheme.typography.titleLarge)
                Text(
                    text = "${selectedPageSize.name} • ${selectedOrientation.name.lowercase().replaceFirstChar { it.uppercase() }}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                // Section Tabs
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = WordPrimary
                ) {
                    tabTitles.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            text = { Text(title, fontSize = 11.sp, fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                when (selectedTab) {
                    0 -> {
                        // Paper Size
                        Text("Paper Size", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            listOf(PageSize.A4, PageSize.LETTER, PageSize.LEGAL, PageSize.A5, PageSize.EXECUTIVE).forEach { ps ->
                                FilterChip(
                                    selected = selectedPageSize == ps,
                                    onClick = {
                                        selectedPageSize = ps
                                        onSaveConfig(
                                            currentLayout.copy(
                                                pageSize = ps,
                                                orientation = selectedOrientation,
                                                marginPreset = selectedMargins
                                            )
                                        )
                                    },
                                    label = { Text(ps.displayName.substringBefore(" ("), fontSize = 11.sp, fontWeight = if (selectedPageSize == ps) FontWeight.Bold else FontWeight.Normal) }
                                )
                            }
                        }

                        // Dimension Details Card for Selected Size
                        Spacer(modifier = Modifier.height(6.dp))
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(
                                    text = "Selected: ${selectedPageSize.displayName}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Dimensions: ${selectedPageSize.fullDimensionsText(selectedOrientation)}",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Print Points (72 DPI): ${if (selectedOrientation == PageOrientation.PORTRAIT) selectedPageSize.widthPt else selectedPageSize.heightPt} × ${if (selectedOrientation == PageOrientation.PORTRAIT) selectedPageSize.heightPt else selectedPageSize.widthPt} pt",
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        HorizontalDivider()
                        Spacer(modifier = Modifier.height(8.dp))

                        // Orientation
                        Text("Orientation", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf(PageOrientation.PORTRAIT, PageOrientation.LANDSCAPE).forEach { po ->
                                FilterChip(
                                    selected = selectedOrientation == po,
                                    onClick = {
                                        selectedOrientation = po
                                        onSaveConfig(
                                            currentLayout.copy(
                                                pageSize = selectedPageSize,
                                                orientation = po,
                                                marginPreset = selectedMargins
                                            )
                                        )
                                    },
                                    label = { Text(po.name.lowercase().replaceFirstChar { it.uppercase() }, fontSize = 11.sp) }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        HorizontalDivider()
                        Spacer(modifier = Modifier.height(8.dp))

                        // Margins Presets
                        Text("Margins Preset", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            listOf(MarginPreset.NORMAL, MarginPreset.NARROW, MarginPreset.MODERATE, MarginPreset.WIDE, MarginPreset.CUSTOM).forEach { mp ->
                                FilterChip(
                                    selected = selectedMargins == mp,
                                    onClick = {
                                        selectedMargins = mp
                                        if (mp != MarginPreset.CUSTOM) {
                                            topMarginMm = mp.topMm
                                            bottomMarginMm = mp.bottomMm
                                            leftMarginMm = mp.leftMm
                                            rightMarginMm = mp.rightMm
                                        }
                                    },
                                    label = { Text(mp.displayName.split(" ")[0], fontSize = 11.sp) }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        // Margin dimension inputs / sliders
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Top: ${topMarginMm.toInt()} mm", fontSize = 11.sp, color = Color.Gray)
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Button(
                                        onClick = {
                                            selectedMargins = MarginPreset.CUSTOM
                                            topMarginMm = (topMarginMm - 5f).coerceAtLeast(5f)
                                        },
                                        contentPadding = PaddingValues(4.dp),
                                        modifier = Modifier.size(28.dp)
                                    ) { Text("-", fontSize = 12.sp) }
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Button(
                                        onClick = {
                                            selectedMargins = MarginPreset.CUSTOM
                                            topMarginMm = (topMarginMm + 5f).coerceAtMost(60f)
                                        },
                                        contentPadding = PaddingValues(4.dp),
                                        modifier = Modifier.size(28.dp)
                                    ) { Text("+", fontSize = 12.sp) }
                                }
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Bottom: ${bottomMarginMm.toInt()} mm", fontSize = 11.sp, color = Color.Gray)
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Button(
                                        onClick = {
                                            selectedMargins = MarginPreset.CUSTOM
                                            bottomMarginMm = (bottomMarginMm - 5f).coerceAtLeast(5f)
                                        },
                                        contentPadding = PaddingValues(4.dp),
                                        modifier = Modifier.size(28.dp)
                                    ) { Text("-", fontSize = 12.sp) }
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Button(
                                        onClick = {
                                            selectedMargins = MarginPreset.CUSTOM
                                            bottomMarginMm = (bottomMarginMm + 5f).coerceAtMost(60f)
                                        },
                                        contentPadding = PaddingValues(4.dp),
                                        modifier = Modifier.size(28.dp)
                                    ) { Text("+", fontSize = 12.sp) }
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Left: ${leftMarginMm.toInt()} mm", fontSize = 11.sp, color = Color.Gray)
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Button(
                                        onClick = {
                                            selectedMargins = MarginPreset.CUSTOM
                                            leftMarginMm = (leftMarginMm - 5f).coerceAtLeast(5f)
                                        },
                                        contentPadding = PaddingValues(4.dp),
                                        modifier = Modifier.size(28.dp)
                                    ) { Text("-", fontSize = 12.sp) }
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Button(
                                        onClick = {
                                            selectedMargins = MarginPreset.CUSTOM
                                            leftMarginMm = (leftMarginMm + 5f).coerceAtMost(60f)
                                        },
                                        contentPadding = PaddingValues(4.dp),
                                        modifier = Modifier.size(28.dp)
                                    ) { Text("+", fontSize = 12.sp) }
                                }
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Right: ${rightMarginMm.toInt()} mm", fontSize = 11.sp, color = Color.Gray)
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Button(
                                        onClick = {
                                            selectedMargins = MarginPreset.CUSTOM
                                            rightMarginMm = (rightMarginMm - 5f).coerceAtLeast(5f)
                                        },
                                        contentPadding = PaddingValues(4.dp),
                                        modifier = Modifier.size(28.dp)
                                    ) { Text("-", fontSize = 12.sp) }
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Button(
                                        onClick = {
                                            selectedMargins = MarginPreset.CUSTOM
                                            rightMarginMm = (rightMarginMm + 5f).coerceAtMost(60f)
                                        },
                                        contentPadding = PaddingValues(4.dp),
                                        modifier = Modifier.size(28.dp)
                                    ) { Text("+", fontSize = 12.sp) }
                                }
                            }
                        }
                    }

                    1 -> {
                        // Background Colors
                        Text("Page Background", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            com.example.core.layout.PageBackgroundPreset.entries.forEach { bg ->
                                val color = try { Color(android.graphics.Color.parseColor(bg.colorHex)) } catch (e: Exception) { Color.White }
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.clickable { pageBackgroundHex = bg.colorHex }
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(color)
                                            .border(
                                                width = if (pageBackgroundHex.equals(bg.colorHex, ignoreCase = true)) 2.dp else 1.dp,
                                                color = if (pageBackgroundHex.equals(bg.colorHex, ignoreCase = true)) WordPrimary else Color.LightGray,
                                                shape = CircleShape
                                            )
                                    )
                                    Text(bg.displayName, fontSize = 10.sp, maxLines = 1)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))
                        HorizontalDivider()
                        Spacer(modifier = Modifier.height(8.dp))

                        // Page Borders
                        Text("Page Border", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                        Spacer(modifier = Modifier.height(6.dp))
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            com.example.core.layout.PageBorderStyle.entries.forEach { bs ->
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { borderStyle = bs }
                                        .padding(vertical = 4.dp)
                                ) {
                                    FilterChip(
                                        selected = borderStyle == bs,
                                        onClick = { borderStyle = bs },
                                        label = { Text(bs.displayName, fontSize = 11.sp) }
                                    )
                                }
                            }
                        }
                    }

                    2 -> {
                        // Header & Footer
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Show Header", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                            androidx.compose.material3.Switch(checked = showHeader, onCheckedChange = { showHeader = it })
                        }
                        if (showHeader) {
                            OutlinedTextField(
                                value = headerText,
                                onValueChange = { headerText = it },
                                label = { Text("Header Text (use {title})") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        HorizontalDivider()
                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Show Footer & Page Numbering", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                            androidx.compose.material3.Switch(checked = showFooter, onCheckedChange = { showFooter = it })
                        }
                        if (showFooter) {
                            OutlinedTextField(
                                value = footerText,
                                onValueChange = { footerText = it },
                                label = { Text("Footer Template (use {page} & {pages})") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Page Numbering Format", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                com.example.core.layout.PageNumberFormat.entries.forEach { pnf ->
                                    FilterChip(
                                        selected = pageNumberFormat == pnf,
                                        onClick = { pageNumberFormat = pnf },
                                        label = { Text(pnf.displayName, fontSize = 11.sp) }
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable { differentFirstPage = !differentFirstPage }
                        ) {
                            androidx.compose.material3.Checkbox(
                                checked = differentFirstPage,
                                onCheckedChange = { differentFirstPage = it }
                            )
                            Text("Different First Page (omit header on cover)", fontSize = 12.sp)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val updated = currentLayout.copy(
                        pageSize = selectedPageSize,
                        orientation = selectedOrientation,
                        marginPreset = selectedMargins,
                        customTopMarginMm = if (selectedMargins == MarginPreset.CUSTOM) topMarginMm else null,
                        customBottomMarginMm = if (selectedMargins == MarginPreset.CUSTOM) bottomMarginMm else null,
                        customLeftMarginMm = if (selectedMargins == MarginPreset.CUSTOM) leftMarginMm else null,
                        customRightMarginMm = if (selectedMargins == MarginPreset.CUSTOM) rightMarginMm else null,
                        showHeader = showHeader,
                        showFooter = showFooter,
                        headerText = headerText,
                        footerText = footerText,
                        differentFirstPage = differentFirstPage,
                        pageBackgroundHex = pageBackgroundHex,
                        borderStyle = borderStyle,
                        borderColorHex = borderColorHex,
                        pageNumberFormat = pageNumberFormat,
                        pageNumberStart = pageNumberStart
                    )
                    onSaveConfig(updated)
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = WordPrimary)
            ) {
                Text("Apply")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

/**
 * Dialog for inserting or updating a web hyperlink on the selected text or document.
 */
@Composable
fun InsertHyperlinkDialog(
    initialUrl: String = "https://",
    onDismiss: () -> Unit,
    onConfirm: (url: String) -> Unit,
    onRemoveLink: () -> Unit
) {
    var urlText by remember { mutableStateOf(if (initialUrl.isBlank()) "https://" else initialUrl) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Insert Hyperlink") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Enter web URL for the selected text:",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                OutlinedTextField(
                    value = urlText,
                    onValueChange = { urlText = it },
                    label = { Text("Web URL") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("hyperlink_url_input")
                )
            }
        },
        confirmButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (initialUrl.isNotBlank() && initialUrl != "https://") {
                    TextButton(
                        onClick = {
                            onRemoveLink()
                            onDismiss()
                        }
                    ) {
                        Text("Remove Link", color = MaterialTheme.colorScheme.error)
                    }
                }
                Button(
                    onClick = {
                        val trimmed = urlText.trim()
                        if (trimmed.isNotBlank()) {
                            onConfirm(trimmed)
                        }
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = WordPrimary),
                    modifier = Modifier.testTag("confirm_hyperlink_button")
                ) {
                    Text("Apply")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

/**
 * Dialog for editing cell contents and cell-level formatting (bold, italic, alignment, background).
 */
@Composable
fun TableCellEditDialog(
    cell: TableCell,
    row: Int,
    col: Int,
    onDismiss: () -> Unit,
    onConfirm: (text: String, isBold: Boolean, isItalic: Boolean, bgHex: String?, alignment: TextAlignment) -> Unit
) {
    var text by remember { mutableStateOf(cell.text) }
    var isBold by remember { mutableStateOf(cell.isBold) }
    var isItalic by remember { mutableStateOf(cell.isItalic) }
    var alignment by remember { mutableStateOf(cell.alignment) }
    var bgHex by remember { mutableStateOf(cell.backgroundColorHex) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Cell (${row + 1}, ${col + 1})") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    label = { Text("Cell Text") },
                    modifier = Modifier.fillMaxWidth().testTag("cell_text_input")
                )
                Text("Formatting & Style:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    FilterChip(
                        selected = isBold,
                        onClick = { isBold = !isBold },
                        label = { Text("Bold", fontWeight = FontWeight.Bold) }
                    )
                    FilterChip(
                        selected = isItalic,
                        onClick = { isItalic = !isItalic },
                        label = { Text("Italic", fontStyle = androidx.compose.ui.text.font.FontStyle.Italic) }
                    )
                }
                Text("Cell Alignment:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf(
                        TextAlignment.START to "Left",
                        TextAlignment.CENTER to "Center",
                        TextAlignment.END to "Right"
                    ).forEach { (align, name) ->
                        FilterChip(
                            selected = alignment == align,
                            onClick = { alignment = align },
                            label = { Text(name, fontSize = 11.sp) }
                        )
                    }
                }
                Text("Cell Shading:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(
                        null to "None",
                        "#F1F5F9" to "Light Gray",
                        "#E0F2FE" to "Soft Blue",
                        "#FEF9C3" to "Soft Yellow",
                        "#DCFCE7" to "Soft Green"
                    ).forEach { (hex, name) ->
                        FilterChip(
                            selected = bgHex == hex,
                            onClick = { bgHex = hex },
                            label = { Text(name, fontSize = 10.sp) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm(text, isBold, isItalic, bgHex, alignment)
                },
                colors = ButtonDefaults.buttonColors(containerColor = WordPrimary),
                modifier = Modifier.testTag("confirm_cell_edit")
            ) {
                Text("Done")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

/**
 * Dialog for configuring table properties: row/col addition and deletion, borders, and width.
 */
@Composable
fun TablePropertiesDialog(
    table: TableBlock,
    onDismiss: () -> Unit,
    onAddRow: () -> Unit,
    onDeleteRow: () -> Unit,
    onAddCol: () -> Unit,
    onDeleteCol: () -> Unit,
    onUpdateProperties: (hasBorders: Boolean, borderColorHex: String, widthPercent: Int, alignment: TextAlignment) -> Unit,
    onDeleteTable: () -> Unit
) {
    var hasBorders by remember { mutableStateOf(table.hasBorders) }
    var borderColorHex by remember { mutableStateOf(table.borderColorHex) }
    var widthPercent by remember { mutableStateOf(table.tableWidthPercent) }
    var alignment by remember { mutableStateOf(table.alignment) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Table Options (${table.rowCount} × ${table.columnCount})") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("Rows & Columns:", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onAddRow,
                        modifier = Modifier.weight(1f).testTag("add_table_row_btn"),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer, contentColor = MaterialTheme.colorScheme.onSecondaryContainer)
                    ) { Text("+ Row", fontSize = 12.sp) }

                    Button(
                        onClick = onDeleteRow,
                        enabled = table.rowCount > 1,
                        modifier = Modifier.weight(1f).testTag("delete_table_row_btn"),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer, contentColor = MaterialTheme.colorScheme.onSecondaryContainer)
                    ) { Text("- Row", fontSize = 12.sp) }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onAddCol,
                        modifier = Modifier.weight(1f).testTag("add_table_col_btn"),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer, contentColor = MaterialTheme.colorScheme.onSecondaryContainer)
                    ) { Text("+ Column", fontSize = 12.sp) }

                    Button(
                        onClick = onDeleteCol,
                        enabled = table.columnCount > 1,
                        modifier = Modifier.weight(1f).testTag("delete_table_col_btn"),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer, contentColor = MaterialTheme.colorScheme.onSecondaryContainer)
                    ) { Text("- Column", fontSize = 12.sp) }
                }

                HorizontalDivider()

                Text("Table Borders:", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { hasBorders = !hasBorders }
                ) {
                    Checkbox(checked = hasBorders, onCheckedChange = { hasBorders = it })
                    Text("Display borders", fontSize = 13.sp)
                }

                if (hasBorders) {
                    Text("Border Color:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(
                            "#CBD5E1" to "Classic Gray",
                            "#185ABD" to "Word Blue",
                            "#000000" to "Black",
                            "#EF4444" to "Red"
                        ).forEach { (hex, name) ->
                            FilterChip(
                                selected = borderColorHex.equals(hex, ignoreCase = true),
                                onClick = { borderColorHex = hex },
                                label = { Text(name, fontSize = 10.sp) }
                            )
                        }
                    }
                }

                HorizontalDivider()

                Text("Table Width & Sizing:", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(50, 75, 100).forEach { w ->
                        FilterChip(
                            selected = widthPercent == w,
                            onClick = { widthPercent = w },
                            label = { Text("$w% Width", fontSize = 11.sp) }
                        )
                    }
                }

                Text("Table Alignment:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(
                        TextAlignment.START to "Left",
                        TextAlignment.CENTER to "Center",
                        TextAlignment.END to "Right"
                    ).forEach { (align, name) ->
                        FilterChip(
                            selected = alignment == align,
                            onClick = { alignment = align },
                            label = { Text(name, fontSize = 11.sp) }
                        )
                    }
                }

                HorizontalDivider()

                Button(
                    onClick = {
                        onDeleteTable()
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.errorContainer, contentColor = MaterialTheme.colorScheme.onErrorContainer),
                    modifier = Modifier.fillMaxWidth().testTag("delete_table_btn")
                ) {
                    Text("Delete Entire Table")
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onUpdateProperties(hasBorders, borderColorHex, widthPercent, alignment)
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = WordPrimary),
                modifier = Modifier.testTag("apply_table_properties")
            ) {
                Text("Apply")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Close") }
        }
    )
}

/**
 * Dialog for image sizing, maintaining aspect ratio, alignment, and captions.
 */
@Composable
fun ImagePropertiesDialog(
    imageBlock: ImageBlock,
    onDismiss: () -> Unit,
    onConfirm: (widthDp: Int, heightDp: Int, alignment: TextAlignment, caption: String, aspectRatio: Float) -> Unit,
    onDeleteImage: () -> Unit
) {
    var widthDp by remember { mutableStateOf(imageBlock.widthDp.toFloat()) }
    var customHeightDp by remember { mutableStateOf(imageBlock.heightDp.toFloat()) }
    var maintainAspectRatio by remember { mutableStateOf(true) }
    var alignment by remember { mutableStateOf(imageBlock.alignment) }
    var captionText by remember { mutableStateOf(imageBlock.caption) }

    val baseRatio = if (imageBlock.aspectRatio > 0) imageBlock.aspectRatio else 1.333f
    val computedHeight = if (maintainAspectRatio) (widthDp / baseRatio).toInt() else customHeightDp.toInt()
    val effectiveRatio = if (maintainAspectRatio) baseRatio else (widthDp / customHeightDp.coerceAtLeast(1f))

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Picture Format & Sizing") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("Image Width: ${widthDp.toInt()} dp", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                Slider(
                    value = widthDp,
                    onValueChange = {
                        widthDp = it
                        if (maintainAspectRatio) {
                            customHeightDp = (it / baseRatio).coerceIn(60f, 600f)
                        }
                    },
                    valueRange = 80f..460f,
                    steps = 19,
                    modifier = Modifier.testTag("image_width_slider")
                )

                Text("Size Presets:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(120f to "Small", 220f to "Medium", 320f to "Large", 420f to "Full").forEach { (sz, label) ->
                        FilterChip(
                            selected = (widthDp - sz).let { it in -20f..20f },
                            onClick = {
                                widthDp = sz
                                if (maintainAspectRatio) {
                                    customHeightDp = (sz / baseRatio).coerceIn(60f, 600f)
                                }
                            },
                            label = { Text(label, fontSize = 11.sp) }
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable {
                        maintainAspectRatio = !maintainAspectRatio
                        if (maintainAspectRatio) {
                            customHeightDp = (widthDp / baseRatio).coerceIn(60f, 600f)
                        }
                    }
                ) {
                    Checkbox(
                        checked = maintainAspectRatio,
                        onCheckedChange = {
                            maintainAspectRatio = it
                            if (it) {
                                customHeightDp = (widthDp / baseRatio).coerceIn(60f, 600f)
                            }
                        },
                        modifier = Modifier.testTag("aspect_ratio_checkbox")
                    )
                    Text("Lock aspect ratio (Height: $computedHeight dp)", fontSize = 12.sp)
                }

                if (!maintainAspectRatio) {
                    Text("Custom Height: ${customHeightDp.toInt()} dp", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                    Slider(
                        value = customHeightDp,
                        onValueChange = { customHeightDp = it },
                        valueRange = 60f..460f,
                        steps = 19,
                        modifier = Modifier.testTag("image_height_slider")
                    )
                }

                HorizontalDivider()

                Text("Alignment:", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(
                        TextAlignment.START to "Left",
                        TextAlignment.CENTER to "Center",
                        TextAlignment.END to "Right"
                    ).forEach { (align, label) ->
                        FilterChip(
                            selected = alignment == align,
                            onClick = { alignment = align },
                            label = { Text(label, fontSize = 11.sp) }
                        )
                    }
                }

                HorizontalDivider()

                OutlinedTextField(
                    value = captionText,
                    onValueChange = { captionText = it },
                    label = { Text("Caption (Optional)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("image_caption_input")
                )

                Button(
                    onClick = {
                        onDeleteImage()
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.errorContainer, contentColor = MaterialTheme.colorScheme.onErrorContainer),
                    modifier = Modifier.fillMaxWidth().testTag("delete_image_btn")
                ) {
                    Text("Delete Image")
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm(widthDp.toInt(), computedHeight, alignment, captionText, effectiveRatio)
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = WordPrimary),
                modifier = Modifier.testTag("apply_image_properties")
            ) {
                Text("Apply")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

/**
 * Dialog for inserting header and footer content.
 */
@Composable
fun HeaderFooterEditDialog(
    currentHeader: String,
    currentFooter: String,
    showHeader: Boolean,
    showFooter: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (headerText: String, footerText: String, showHeader: Boolean, showFooter: Boolean) -> Unit
) {
    var header by remember { mutableStateOf(currentHeader) }
    var footer by remember { mutableStateOf(currentFooter) }
    var isHeaderVisible by remember { mutableStateOf(showHeader) }
    var isFooterVisible by remember { mutableStateOf(showFooter) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Header & Footer Content") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Enable Header", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                    Switch(checked = isHeaderVisible, onCheckedChange = { isHeaderVisible = it })
                }
                if (isHeaderVisible) {
                    OutlinedTextField(
                        value = header,
                        onValueChange = { header = it },
                        label = { Text("Header Text (e.g. Document Title)") },
                        modifier = Modifier.fillMaxWidth().testTag("header_text_input")
                    )
                }

                HorizontalDivider()

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Enable Footer", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                    Switch(checked = isFooterVisible, onCheckedChange = { isFooterVisible = it })
                }
                if (isFooterVisible) {
                    OutlinedTextField(
                        value = footer,
                        onValueChange = { footer = it },
                        label = { Text("Footer Text (use {page} & {pages})") },
                        modifier = Modifier.fillMaxWidth().testTag("footer_text_input")
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm(header, footer, isHeaderVisible, isFooterVisible)
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = WordPrimary),
                modifier = Modifier.testTag("apply_header_footer")
            ) {
                Text("Apply")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

/**
 * Dialog for choosing page number position and template.
 */
@Composable
fun PageNumberInsertDialog(
    onDismiss: () -> Unit,
    onSelectPreset: (pattern: String, alignment: TextAlignment) -> Unit
) {
    val presets = listOf(
        Triple("Page {page} of {pages}", TextAlignment.CENTER, "Page 1 of 5 (Bottom Center)"),
        Triple("Page {page}", TextAlignment.END, "Page 1 (Bottom Right)"),
        Triple("{page}", TextAlignment.CENTER, "1 (Bottom Center)"),
        Triple("Page {page}", TextAlignment.START, "Page 1 (Bottom Left)")
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Insert Page Numbers") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Select page number style and position:")
                presets.forEach { (pattern, alignment, label) ->
                    Surface(
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(6.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onSelectPreset(pattern, alignment)
                                onDismiss()
                            }
                    ) {
                        Text(
                            text = label,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

/**
 * Dialog for choosing PDF export destination and inspecting document page settings.
 */
@Composable
fun ExportPdfDialog(
    document: RichDocument,
    actualPageCount: Int = document.estimatedPageCount,
    onDismiss: () -> Unit,
    onSaveToStorage: () -> Unit,
    onSharePdf: () -> Unit,
    onPrint: () -> Unit,
    onOpenPageSetup: () -> Unit
) {
    val layout = document.layoutConfig
    val orientationName = if (layout.orientation == PageOrientation.LANDSCAPE) "Landscape" else "Portrait"
    val marginName = layout.marginPreset.displayName.split(" ")[0]
    val estimatedPages = actualPageCount.coerceAtLeast(1)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.PictureAsPdf,
                    contentDescription = null,
                    tint = WordPrimary,
                    modifier = Modifier.size(28.dp)
                )
                Column {
                    Text("Export Document to PDF", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(
                        text = document.title.ifBlank { "Untitled Document" },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Layout summary card
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Page Configuration",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            TextButton(
                                onClick = {
                                    onDismiss()
                                    onOpenPageSetup()
                                },
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Icon(Icons.Default.Tune, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Page Setup...", fontSize = 11.sp)
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Surface(
                                shape = androidx.compose.foundation.shape.RoundedCornerShape(4.dp),
                                color = WordPrimary.copy(alpha = 0.1f)
                            ) {
                                Text(
                                    text = layout.pageSize.name,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = WordPrimary,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                            Surface(
                                shape = androidx.compose.foundation.shape.RoundedCornerShape(4.dp),
                                color = MaterialTheme.colorScheme.surface
                            ) {
                                Text(
                                    text = orientationName,
                                    fontSize = 11.sp,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                            Surface(
                                shape = androidx.compose.foundation.shape.RoundedCornerShape(4.dp),
                                color = MaterialTheme.colorScheme.surface
                            ) {
                                Text(
                                    text = "$marginName Margins",
                                    fontSize = 11.sp,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                            Surface(
                                shape = androidx.compose.foundation.shape.RoundedCornerShape(4.dp),
                                color = MaterialTheme.colorScheme.surface
                            ) {
                                Text(
                                    text = "$estimatedPages Page${if (estimatedPages != 1) "s" else ""}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }

                Text("Choose Destination:", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)

                // Option 1: Storage Access Framework / Device Storage
                Surface(
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            onDismiss()
                            onSaveToStorage()
                        }
                        .testTag("export_dest_storage")
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Folder,
                            contentDescription = null,
                            tint = WordPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Save to Device / Storage (SAF)", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                            Text("Select exact folder and filename in Android Files or Google Drive", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }

                // Option 2: Share Sheet
                Surface(
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            onDismiss()
                            onSharePdf()
                        }
                        .testTag("export_dest_share")
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = null,
                            tint = Color(0xFF16A34A),
                            modifier = Modifier.size(24.dp)
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Share via App", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                            Text("Send PDF via Gmail, WhatsApp, Drive, Bluetooth, or messaging", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }

                // Option 3: Android Print Workflow
                Surface(
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            onDismiss()
                            onPrint()
                        }
                        .testTag("export_dest_print")
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Print,
                            contentDescription = null,
                            tint = Color(0xFF0284C7),
                            modifier = Modifier.size(24.dp)
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Print Preview & System Printing", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                            Text("System print preview, page range selection, and physical printing", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Close") }
        }
    )
}
