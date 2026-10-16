package com.example.ui.editor.components.ribbon

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ListAlt
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.FormatListNumbered
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.SaveAs
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.ui.editor.EditorViewModel
import com.example.ui.editor.RibbonTab

/**
 * File Ribbon Controls:
 * Houses core document operations (Save, Save As, Export, Print, Page Setup,
 * Outline, Find & Replace, Statistics) directly inside the ribbon toolbar.
 */
@Composable
fun FileRibbonControls(
    viewModel: EditorViewModel,
    onTabSelected: (RibbonTab) -> Unit,
    onSaveNow: () -> Unit,
    onOpenSaveAs: () -> Unit,
    onSaveSafDocx: () -> Unit = {},
    onOpenExportOptions: () -> Unit,
    onOpenLayoutDialog: () -> Unit,
    onPrint: () -> Unit,
    onToggleFindReplace: () -> Unit,
    onOpenOutline: () -> Unit,
    onOpenStatsDialog: () -> Unit
) {
    // Quick back to Home ribbon
    RibbonActionButton(
        icon = Icons.AutoMirrored.Filled.ArrowBack,
        label = "Home",
        onClick = { onTabSelected(RibbonTab.HOME) }
    )

    Box(modifier = Modifier.height(20.dp).width(1.dp).background(MaterialTheme.colorScheme.outlineVariant))

    // Save
    RibbonActionButton(
        icon = Icons.Default.Save,
        label = "Save",
        onClick = onSaveNow
    )

    // Save As
    RibbonActionButton(
        icon = Icons.Default.SaveAs,
        label = "Save As",
        onClick = onOpenSaveAs
    )

    // Save as DOCX
    RibbonActionButton(
        icon = Icons.Default.Description,
        label = "Save DOCX",
        onClick = onSaveSafDocx
    )

    Box(modifier = Modifier.height(20.dp).width(1.dp).background(MaterialTheme.colorScheme.outlineVariant))

    // Export Document
    RibbonActionButton(
        icon = Icons.Default.Share,
        label = "Export...",
        onClick = onOpenExportOptions
    )

    // Print
    RibbonActionButton(
        icon = Icons.Default.Print,
        label = "Print",
        onClick = onPrint
    )

    Box(modifier = Modifier.height(20.dp).width(1.dp).background(MaterialTheme.colorScheme.outlineVariant))

    // Page Setup
    RibbonActionButton(
        icon = Icons.Default.Tune,
        label = "Page Setup",
        onClick = onOpenLayoutDialog
    )

    // Find & Replace
    RibbonActionButton(
        icon = Icons.Default.Search,
        label = "Find & Replace",
        onClick = onToggleFindReplace
    )

    // Document Outline
    RibbonActionButton(
        icon = Icons.AutoMirrored.Filled.ListAlt,
        label = "Outline",
        onClick = onOpenOutline
    )

    // Document Statistics
    RibbonActionButton(
        icon = Icons.Default.FormatListNumbered,
        label = "Statistics",
        onClick = onOpenStatsDialog
    )
}
