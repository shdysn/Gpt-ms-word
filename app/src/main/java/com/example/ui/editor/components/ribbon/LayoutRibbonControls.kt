package com.example.ui.editor.components.ribbon

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BorderAll
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Opacity
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.VerticalSplit
import androidx.compose.material.icons.filled.ViewColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.core.layout.MarginPreset
import com.example.core.layout.PageLayoutConfig
import com.example.core.layout.PageOrientation
import com.example.ui.editor.EditorViewModel

@Composable
fun LayoutRibbonControls(
    viewModel: EditorViewModel,
    onOpenWatermarkDialog: () -> Unit,
    onOpenLayoutDialog: () -> Unit,
    onOpenPageBordersDialog: () -> Unit = {},
    onOpenPageColorDialog: () -> Unit = {}
) {
    val document by viewModel.document.collectAsStateWithLifecycle()
    val layout = document?.layoutConfig ?: PageLayoutConfig()

    // Orientation Toggles
    RibbonToggleButton(
        icon = Icons.Default.Description,
        label = "Portrait",
        isSelected = layout.orientation == PageOrientation.PORTRAIT,
        onClick = { viewModel.updatePageLayout(orientation = PageOrientation.PORTRAIT) }
    )

    RibbonToggleButton(
        icon = Icons.Default.PhoneAndroid,
        label = "Landscape",
        isSelected = layout.orientation == PageOrientation.LANDSCAPE,
        onClick = { viewModel.updatePageLayout(orientation = PageOrientation.LANDSCAPE) }
    )

    Box(modifier = Modifier.height(20.dp).width(1.dp).background(MaterialTheme.colorScheme.outlineVariant))

    // Columns: 1 or 2 Columns
    RibbonToggleButton(
        icon = Icons.Default.Description,
        label = "1 Column",
        isSelected = layout.columnCount == 1,
        onClick = { viewModel.setColumnCount(1) }
    )

    RibbonToggleButton(
        icon = Icons.Default.ViewColumn,
        label = "2 Columns",
        isSelected = layout.columnCount == 2,
        onClick = { viewModel.setColumnCount(2) }
    )

    RibbonActionButton(
        icon = Icons.Default.VerticalSplit,
        label = "Column Break",
        onClick = { viewModel.insertColumnBreak() }
    )

    RibbonActionButton(
        icon = Icons.Default.ViewColumn,
        label = "Section Break",
        onClick = { viewModel.insertSectionBreak() }
    )

    Box(modifier = Modifier.height(20.dp).width(1.dp).background(MaterialTheme.colorScheme.outlineVariant))

    // Page Borders & Color
    RibbonActionButton(
        icon = Icons.Default.BorderAll,
        label = "Page Borders",
        onClick = onOpenPageBordersDialog
    )

    RibbonActionButton(
        icon = Icons.Default.Palette,
        label = "Page Color",
        onClick = onOpenPageColorDialog
    )

    Box(modifier = Modifier.height(20.dp).width(1.dp).background(MaterialTheme.colorScheme.outlineVariant))

    // Watermark
    val hasWatermark = layout.watermarkText.isNotBlank()
    RibbonToggleButton(
        icon = Icons.Default.Opacity,
        label = if (hasWatermark) "Watermark (${layout.watermarkText.take(8)})" else "Watermark",
        isSelected = hasWatermark,
        onClick = onOpenWatermarkDialog
    )

    Box(modifier = Modifier.height(20.dp).width(1.dp).background(MaterialTheme.colorScheme.outlineVariant))

    // Quick Margins
    RibbonToggleButton(
        icon = Icons.Default.BorderAll,
        label = "Normal Margins",
        isSelected = layout.marginPreset == MarginPreset.NORMAL,
        onClick = { viewModel.updatePageLayout(marginPreset = MarginPreset.NORMAL) }
    )

    RibbonToggleButton(
        icon = Icons.Default.BorderAll,
        label = "Narrow Margins",
        isSelected = layout.marginPreset == MarginPreset.NARROW,
        onClick = { viewModel.updatePageLayout(marginPreset = MarginPreset.NARROW) }
    )

    Box(modifier = Modifier.height(20.dp).width(1.dp).background(MaterialTheme.colorScheme.outlineVariant))

    // Full Page Setup Dialog
    RibbonActionButton(
        icon = Icons.Default.Tune,
        label = "Page Setup",
        onClick = onOpenLayoutDialog
    )
}
