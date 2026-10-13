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
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
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

    val rowsInt = rowsText.toIntOrNull() ?: 3
    val colsInt = colsText.toIntOrNull() ?: 3

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = WordPrimary.copy(alpha = 0.12f),
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.TableChart,
                            contentDescription = null,
                            tint = WordPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text("Insert Table", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text("Customize grid dimensions & width", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
                // Dimensions section with stepper buttons
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                    border = androidx.compose.foundation.BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("Dimensions", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)

                        // Rows Stepper & Input
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("Rows (1–10)", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                                Text("Current: $rowsInt", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    shape = CircleShape,
                                    color = MaterialTheme.colorScheme.surface,
                                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clickable {
                                            val newR = (rowsInt - 1).coerceAtLeast(1)
                                            rowsText = newR.toString()
                                        }
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(Icons.Default.Remove, contentDescription = "Decrease rows", modifier = Modifier.size(16.dp))
                                    }
                                }
                                OutlinedTextField(
                                    value = rowsText,
                                    onValueChange = { rowsText = it.filter { ch -> ch.isDigit() }.take(2) },
                                    singleLine = true,
                                    textStyle = androidx.compose.ui.text.TextStyle(
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    modifier = Modifier
                                        .width(64.dp)
                                        .padding(horizontal = 6.dp)
                                        .testTag("table_rows_input")
                                )
                                Surface(
                                    shape = CircleShape,
                                    color = MaterialTheme.colorScheme.surface,
                                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clickable {
                                            val newR = (rowsInt + 1).coerceAtMost(10)
                                            rowsText = newR.toString()
                                        }
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(Icons.Default.Add, contentDescription = "Increase rows", modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                        }

                        // Columns Stepper & Input
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("Columns (1–6)", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                                Text("Current: $colsInt", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    shape = CircleShape,
                                    color = MaterialTheme.colorScheme.surface,
                                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clickable {
                                            val newC = (colsInt - 1).coerceAtLeast(1)
                                            colsText = newC.toString()
                                        }
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(Icons.Default.Remove, contentDescription = "Decrease columns", modifier = Modifier.size(16.dp))
                                    }
                                }
                                OutlinedTextField(
                                    value = colsText,
                                    onValueChange = { colsText = it.filter { ch -> ch.isDigit() }.take(1) },
                                    singleLine = true,
                                    textStyle = androidx.compose.ui.text.TextStyle(
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    modifier = Modifier
                                        .width(64.dp)
                                        .padding(horizontal = 6.dp)
                                        .testTag("table_cols_input")
                                )
                                Surface(
                                    shape = CircleShape,
                                    color = MaterialTheme.colorScheme.surface,
                                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clickable {
                                            val newC = (colsInt + 1).coerceAtMost(6)
                                            colsText = newC.toString()
                                        }
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(Icons.Default.Add, contentDescription = "Increase columns", modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                        }
                    }
                }

                // Table Width Options
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Table Width:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(50, 75, 100).forEach { w ->
                            FilterChip(
                                selected = selectedWidthPercent == w,
                                onClick = { selectedWidthPercent = w },
                                label = { Text("$w% Width", fontSize = 11.sp) }
                            )
                        }
                    }
                }

                // Border Toggle Card
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { hasBorders = !hasBorders }
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Checkbox(
                            checked = hasBorders,
                            onCheckedChange = { hasBorders = it },
                            modifier = Modifier.testTag("table_borders_checkbox")
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Column {
                            Text("Include Table Grid Borders", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                            Text("Draw subtle border grid lines between cells", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
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
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.testTag("confirm_insert_table")
            ) {
                Text("Insert Table")
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
 * Designed with a modern, professional aesthetic and an interactive live paper preview.
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
    val tabTitles = listOf("Paper & Margins", "Appearance", "Header & Footer")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = WordPrimary.copy(alpha = 0.12f),
                        modifier = Modifier.size(38.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Tune,
                                contentDescription = null,
                                tint = WordPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text("Page Setup", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Text(
                            text = "${selectedPageSize.displayName.substringBefore(" (")} • ${selectedOrientation.name.lowercase().replaceFirstChar { it.uppercase() }}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Interactive Live Paper Thumbnail Preview Card
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                    border = androidx.compose.foundation.BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        val isPortrait = selectedOrientation == PageOrientation.PORTRAIT
                        val paperWidth = if (isPortrait) 68.dp else 98.dp
                        val paperHeight = if (isPortrait) 96.dp else 68.dp
                        val bgColor = try { Color(android.graphics.Color.parseColor(pageBackgroundHex)) } catch (e: Exception) { Color.White }
                        val bdrColor = try { Color(android.graphics.Color.parseColor(borderColorHex)) } catch (e: Exception) { Color(0xFFCBD5E1) }

                        // Miniature Sheet
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = bgColor,
                            shadowElevation = 3.dp,
                            border = androidx.compose.foundation.BorderStroke(
                                width = if (borderStyle != com.example.core.layout.PageBorderStyle.NONE) 1.5.dp else 0.5.dp,
                                color = if (borderStyle != com.example.core.layout.PageBorderStyle.NONE) bdrColor else MaterialTheme.colorScheme.outlineVariant
                            ),
                            modifier = Modifier.size(paperWidth, paperHeight)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(
                                        top = (topMarginMm / 6f).dp.coerceIn(4.dp, 16.dp),
                                        bottom = (bottomMarginMm / 6f).dp.coerceIn(4.dp, 16.dp),
                                        start = (leftMarginMm / 6f).dp.coerceIn(4.dp, 16.dp),
                                        end = (rightMarginMm / 6f).dp.coerceIn(4.dp, 16.dp)
                                    )
                                    .border(0.5.dp, WordPrimary.copy(alpha = 0.35f), RoundedCornerShape(2.dp))
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(2.dp),
                                    verticalArrangement = Arrangement.SpaceBetween
                                ) {
                                    if (showHeader) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth(0.7f)
                                                .height(2.dp)
                                                .background(WordPrimary.copy(alpha = 0.5f))
                                        )
                                    } else {
                                        Spacer(modifier = Modifier.height(2.dp))
                                    }
                                    // Text mock line
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth(0.9f)
                                            .height(2.dp)
                                            .background(Color.Gray.copy(alpha = 0.25f))
                                    )
                                    if (showFooter) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth(0.5f)
                                                .height(2.dp)
                                                .background(WordPrimary.copy(alpha = 0.5f))
                                        )
                                    } else {
                                        Spacer(modifier = Modifier.height(2.dp))
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "${selectedPageSize.displayName} • ${selectedMargins.displayName.substringBefore(" ")} Margins",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Section Tabs
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = WordPrimary,
                    divider = {},
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                            color = WordPrimary,
                            height = 2.5.dp
                        )
                    }
                ) {
                    tabTitles.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            text = {
                                Text(
                                    text = title,
                                    fontSize = 12.sp,
                                    fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Medium,
                                    color = if (selectedTab == index) WordPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        )
                    }
                }

                when (selectedTab) {
                    0 -> {
                        // Paper & Margins Section
                        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                            // Orientation Cards
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text("Orientation", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    // Portrait Card
                                    val isPortrait = selectedOrientation == PageOrientation.PORTRAIT
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = if (isPortrait) WordPrimary.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                        border = androidx.compose.foundation.BorderStroke(
                                            if (isPortrait) 1.5.dp else 0.5.dp,
                                            if (isPortrait) WordPrimary else MaterialTheme.colorScheme.outlineVariant
                                        ),
                                        modifier = Modifier
                                            .weight(1f)
                                            .clickable { selectedOrientation = PageOrientation.PORTRAIT }
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(14.dp, 20.dp)
                                                    .border(
                                                        1.5.dp,
                                                        if (isPortrait) WordPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                                        RoundedCornerShape(2.dp)
                                                    )
                                            )
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Text(
                                                text = "Portrait",
                                                fontSize = 12.sp,
                                                fontWeight = if (isPortrait) FontWeight.Bold else FontWeight.Medium,
                                                color = if (isPortrait) WordPrimary else MaterialTheme.colorScheme.onSurface
                                            )
                                        }
                                    }

                                    // Landscape Card
                                    val isLandscape = selectedOrientation == PageOrientation.LANDSCAPE
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = if (isLandscape) WordPrimary.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                        border = androidx.compose.foundation.BorderStroke(
                                            if (isLandscape) 1.5.dp else 0.5.dp,
                                            if (isLandscape) WordPrimary else MaterialTheme.colorScheme.outlineVariant
                                        ),
                                        modifier = Modifier
                                            .weight(1f)
                                            .clickable { selectedOrientation = PageOrientation.LANDSCAPE }
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(20.dp, 14.dp)
                                                    .border(
                                                        1.5.dp,
                                                        if (isLandscape) WordPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                                        RoundedCornerShape(2.dp)
                                                    )
                                            )
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Text(
                                                text = "Landscape",
                                                fontSize = 12.sp,
                                                fontWeight = if (isLandscape) FontWeight.Bold else FontWeight.Medium,
                                                color = if (isLandscape) WordPrimary else MaterialTheme.colorScheme.onSurface
                                            )
                                        }
                                    }
                                }
                            }

                            // Paper Size Selector
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text("Paper Size", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .horizontalScroll(rememberScrollState()),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    listOf(PageSize.A4, PageSize.LETTER, PageSize.LEGAL, PageSize.A5, PageSize.EXECUTIVE).forEach { ps ->
                                        val isSelected = selectedPageSize == ps
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = if (isSelected) WordPrimary.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                            border = androidx.compose.foundation.BorderStroke(
                                                if (isSelected) 1.dp else 0.5.dp,
                                                if (isSelected) WordPrimary else MaterialTheme.colorScheme.outlineVariant
                                            ),
                                            modifier = Modifier.clickable { selectedPageSize = ps }
                                        ) {
                                            Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp)) {
                                                Text(
                                                    text = ps.name,
                                                    fontSize = 12.sp,
                                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                    color = if (isSelected) WordPrimary else MaterialTheme.colorScheme.onSurface
                                                )
                                                Text(
                                                    text = "${ps.widthMm.toInt()}×${ps.heightMm.toInt()} mm",
                                                    fontSize = 10.sp,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            // Margins Presets
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text("Margins Preset", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .horizontalScroll(rememberScrollState()),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    listOf(MarginPreset.NORMAL, MarginPreset.NARROW, MarginPreset.MODERATE, MarginPreset.WIDE, MarginPreset.CUSTOM).forEach { mp ->
                                        val isSelected = selectedMargins == mp
                                        FilterChip(
                                            selected = isSelected,
                                            onClick = {
                                                selectedMargins = mp
                                                if (mp != MarginPreset.CUSTOM) {
                                                    topMarginMm = mp.topMm
                                                    bottomMarginMm = mp.bottomMm
                                                    leftMarginMm = mp.leftMm
                                                    rightMarginMm = mp.rightMm
                                                }
                                            },
                                            label = {
                                                Text(
                                                    text = mp.displayName.substringBefore(" ("),
                                                    fontSize = 11.sp,
                                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                                )
                                            }
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(4.dp))

                                // Margin Steppers Card
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                                    border = androidx.compose.foundation.BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(
                                        modifier = Modifier.padding(12.dp),
                                        verticalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Text("Margin Dimensions (mm)", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                                        ) {
                                            // Top Margin Stepper
                                            MarginStepperItem(
                                                label = "Top",
                                                valueMm = topMarginMm,
                                                modifier = Modifier.weight(1f),
                                                onMinus = {
                                                    selectedMargins = MarginPreset.CUSTOM
                                                    topMarginMm = (topMarginMm - 5f).coerceAtLeast(5f)
                                                },
                                                onPlus = {
                                                    selectedMargins = MarginPreset.CUSTOM
                                                    topMarginMm = (topMarginMm + 5f).coerceAtMost(60f)
                                                }
                                            )
                                            // Bottom Margin Stepper
                                            MarginStepperItem(
                                                label = "Bottom",
                                                valueMm = bottomMarginMm,
                                                modifier = Modifier.weight(1f),
                                                onMinus = {
                                                    selectedMargins = MarginPreset.CUSTOM
                                                    bottomMarginMm = (bottomMarginMm - 5f).coerceAtLeast(5f)
                                                },
                                                onPlus = {
                                                    selectedMargins = MarginPreset.CUSTOM
                                                    bottomMarginMm = (bottomMarginMm + 5f).coerceAtMost(60f)
                                                }
                                            )
                                        }

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                                        ) {
                                            // Left Margin Stepper
                                            MarginStepperItem(
                                                label = "Left",
                                                valueMm = leftMarginMm,
                                                modifier = Modifier.weight(1f),
                                                onMinus = {
                                                    selectedMargins = MarginPreset.CUSTOM
                                                    leftMarginMm = (leftMarginMm - 5f).coerceAtLeast(5f)
                                                },
                                                onPlus = {
                                                    selectedMargins = MarginPreset.CUSTOM
                                                    leftMarginMm = (leftMarginMm + 5f).coerceAtMost(60f)
                                                }
                                            )
                                            // Right Margin Stepper
                                            MarginStepperItem(
                                                label = "Right",
                                                valueMm = rightMarginMm,
                                                modifier = Modifier.weight(1f),
                                                onMinus = {
                                                    selectedMargins = MarginPreset.CUSTOM
                                                    rightMarginMm = (rightMarginMm - 5f).coerceAtLeast(5f)
                                                },
                                                onPlus = {
                                                    selectedMargins = MarginPreset.CUSTOM
                                                    rightMarginMm = (rightMarginMm + 5f).coerceAtMost(60f)
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    1 -> {
                        // Appearance Section
                        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                            // Page Background Color Palette
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text("Page Background Color", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .horizontalScroll(rememberScrollState()),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    com.example.core.layout.PageBackgroundPreset.entries.forEach { bg ->
                                        val isSelected = pageBackgroundHex.equals(bg.colorHex, ignoreCase = true)
                                        val color = try { Color(android.graphics.Color.parseColor(bg.colorHex)) } catch (e: Exception) { Color.White }
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            modifier = Modifier.clickable { pageBackgroundHex = bg.colorHex }
                                        ) {
                                            Surface(
                                                shape = CircleShape,
                                                color = color,
                                                shadowElevation = 2.dp,
                                                border = androidx.compose.foundation.BorderStroke(
                                                    width = if (isSelected) 2.dp else 1.dp,
                                                    color = if (isSelected) WordPrimary else Color.LightGray
                                                ),
                                                modifier = Modifier.size(38.dp)
                                            ) {
                                                if (isSelected) {
                                                    Box(contentAlignment = Alignment.Center) {
                                                        Icon(
                                                            imageVector = Icons.Default.Check,
                                                            contentDescription = "Selected",
                                                            tint = if (bg == com.example.core.layout.PageBackgroundPreset.DARK) Color.White else WordPrimary,
                                                            modifier = Modifier.size(18.dp)
                                                        )
                                                    }
                                                }
                                            }
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = bg.displayName,
                                                fontSize = 10.sp,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                color = if (isSelected) WordPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }
                            }

                            // Page Border Style
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text("Page Border", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .horizontalScroll(rememberScrollState()),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    com.example.core.layout.PageBorderStyle.entries.forEach { bs ->
                                        FilterChip(
                                            selected = borderStyle == bs,
                                            onClick = { borderStyle = bs },
                                            label = {
                                                Text(
                                                    text = bs.displayName,
                                                    fontSize = 11.sp,
                                                    fontWeight = if (borderStyle == bs) FontWeight.Bold else FontWeight.Normal
                                                )
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }

                    2 -> {
                        // Header & Footer Section
                        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                            // Header Toggle & Template Card
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                                border = androidx.compose.foundation.BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text("Document Header", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                            Text("Shown at the top of document pages", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
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
                                }
                            }

                            // Footer Toggle & Template Card
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                                border = androidx.compose.foundation.BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text("Document Footer & Numbering", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                            Text("Shown at the bottom of document pages", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
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

                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text("Page Numbering Format:", fontSize = 11.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
                                }
                            }

                            // Different First Page Checkbox Card
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { differentFirstPage = !differentFirstPage }
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    androidx.compose.material3.Checkbox(
                                        checked = differentFirstPage,
                                        onCheckedChange = { differentFirstPage = it }
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Column {
                                        Text("Different First Page", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                                        Text("Omit header and footer on the first cover page", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                            }
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
                colors = ButtonDefaults.buttonColors(containerColor = WordPrimary),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Apply Changes")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
private fun MarginStepperItem(
    label: String,
    valueMm: Float,
    modifier: Modifier = Modifier,
    onMinus: () -> Unit,
    onPlus: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(label, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("${valueMm.toInt()} mm", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier
                        .size(28.dp)
                        .clickable(onClick = onMinus)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Remove, contentDescription = "Decrease $label", modifier = Modifier.size(14.dp))
                    }
                }
                Spacer(modifier = Modifier.width(4.dp))
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier
                        .size(28.dp)
                        .clickable(onClick = onPlus)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Add, contentDescription = "Increase $label", modifier = Modifier.size(14.dp))
                    }
                }
            }
        }
    }
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
                            val finalUrl = if (!trimmed.startsWith("http://", ignoreCase = true) &&
                                !trimmed.startsWith("https://", ignoreCase = true) &&
                                !trimmed.startsWith("mailto:", ignoreCase = true)
                            ) {
                                "https://$trimmed"
                            } else {
                                trimmed
                            }
                            onConfirm(finalUrl)
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
    onConfirm: (widthDp: Int, heightDp: Int, alignment: TextAlignment, caption: String, aspectRatio: Float, wrapMode: String, filterMode: String, cornerRadius: Int) -> Unit,
    onDeleteImage: () -> Unit
) {
    var widthDp by remember { mutableStateOf(imageBlock.widthDp.toFloat()) }
    var customHeightDp by remember { mutableStateOf(imageBlock.heightDp.toFloat()) }
    var maintainAspectRatio by remember { mutableStateOf(true) }
    var alignment by remember { mutableStateOf(imageBlock.alignment) }
    var captionText by remember { mutableStateOf(imageBlock.caption) }
    var wrapMode by remember { mutableStateOf(imageBlock.wrapMode) }
    var filterMode by remember { mutableStateOf(imageBlock.filterMode) }
    var cornerRadius by remember { mutableStateOf(imageBlock.cornerRadiusDp) }

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
                Text("Text Wrapping:", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf(
                        "IN_LINE" to "In Line",
                        "SQUARE" to "Square",
                        "BEHIND_TEXT" to "Behind",
                        "IN_FRONT" to "In Front"
                    ).forEach { (mode, label) ->
                        FilterChip(
                            selected = wrapMode == mode,
                            onClick = { wrapMode = mode },
                            label = { Text(label, fontSize = 11.sp) }
                        )
                    }
                }

                HorizontalDivider()

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

                Text("Visual Filter:", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("NORMAL" to "Original", "GRAYSCALE" to "Grayscale", "SEPIA" to "Sepia").forEach { (fMode, label) ->
                        FilterChip(
                            selected = filterMode == fMode,
                            onClick = { filterMode = fMode },
                            label = { Text(label, fontSize = 11.sp) }
                        )
                    }
                }

                Text("Corner Radius: $cornerRadius dp", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(0 to "Sharp (0)", 8 to "Rounded (8)", 16 to "Card (16)", 28 to "Soft (28)").forEach { (rad, label) ->
                        FilterChip(
                            selected = cornerRadius == rad,
                            onClick = { cornerRadius = rad },
                            label = { Text(label, fontSize = 11.sp) }
                        )
                    }
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
                    onConfirm(widthDp.toInt(), computedHeight, alignment, captionText, effectiveRatio, wrapMode, filterMode, cornerRadius)
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
