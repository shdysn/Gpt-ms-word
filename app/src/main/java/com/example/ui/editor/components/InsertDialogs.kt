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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
    onConfirm: (rows: Int, cols: Int) -> Unit
) {
    var rowsText by remember { mutableStateOf("3") }
    var colsText by remember { mutableStateOf("3") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Insert Table") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Specify table dimensions:",
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
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val r = rowsText.toIntOrNull()?.coerceIn(1, 10) ?: 3
                    val c = colsText.toIntOrNull()?.coerceIn(1, 6) ?: 3
                    onConfirm(r, c)
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
                            listOf(PageSize.A4, PageSize.LETTER, PageSize.LEGAL, PageSize.A5).forEach { ps ->
                                FilterChip(
                                    selected = selectedPageSize == ps,
                                    onClick = { selectedPageSize = ps },
                                    label = { Text(ps.name, fontSize = 11.sp) }
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
                                    onClick = { selectedOrientation = po },
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
