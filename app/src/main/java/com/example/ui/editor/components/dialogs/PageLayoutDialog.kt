package com.example.ui.editor.components.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.layout.MarginPreset
import com.example.core.layout.PageLayoutConfig
import com.example.core.layout.PageOrientation
import com.example.ui.theme.WordPrimary

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
                                            .height(1.5.dp)
                                            .background(Color.Gray.copy(alpha = 0.3f))
                                    )
                                    if (showFooter) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth(0.5f)
                                                .height(2.dp)
                                                .background(WordPrimary.copy(alpha = 0.5f))
                                                .align(Alignment.CenterHorizontally)
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
                    0 -> PageLayoutPaperTab(
                        selectedOrientation = selectedOrientation,
                        onOrientationChange = { selectedOrientation = it },
                        selectedPageSize = selectedPageSize,
                        onPageSizeChange = { selectedPageSize = it },
                        selectedMargins = selectedMargins,
                        onMarginsPresetChange = { mp, t, b, l, r ->
                            selectedMargins = mp
                            topMarginMm = t
                            bottomMarginMm = b
                            leftMarginMm = l
                            rightMarginMm = r
                        },
                        topMarginMm = topMarginMm,
                        onTopMarginChange = {
                            selectedMargins = MarginPreset.CUSTOM
                            topMarginMm = it
                        },
                        bottomMarginMm = bottomMarginMm,
                        onBottomMarginChange = {
                            selectedMargins = MarginPreset.CUSTOM
                            bottomMarginMm = it
                        },
                        leftMarginMm = leftMarginMm,
                        onLeftMarginChange = {
                            selectedMargins = MarginPreset.CUSTOM
                            leftMarginMm = it
                        },
                        rightMarginMm = rightMarginMm,
                        onRightMarginChange = {
                            selectedMargins = MarginPreset.CUSTOM
                            rightMarginMm = it
                        }
                    )
                    1 -> PageLayoutAppearanceTab(
                        pageBackgroundHex = pageBackgroundHex,
                        onBackgroundHexChange = { pageBackgroundHex = it },
                        borderStyle = borderStyle,
                        onBorderStyleChange = { borderStyle = it }
                    )
                    2 -> PageLayoutHeaderFooterTab(
                        showHeader = showHeader,
                        onShowHeaderChange = { showHeader = it },
                        headerText = headerText,
                        onHeaderTextChange = { headerText = it },
                        showFooter = showFooter,
                        onShowFooterChange = { showFooter = it },
                        footerText = footerText,
                        onFooterTextChange = { footerText = it },
                        differentFirstPage = differentFirstPage,
                        onDifferentFirstPageChange = { differentFirstPage = it },
                        pageNumberFormat = pageNumberFormat,
                        onPageNumberFormatChange = { pageNumberFormat = it }
                    )
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
