package com.example.ui.editor.components.ribbon

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ListAlt
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.SquareFoot
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material.icons.filled.ZoomOut
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.editor.EditorViewModel
import com.example.ui.editor.EditorViewMode

@Composable
fun ViewRibbonControls(
    viewModel: EditorViewModel,
    onOpenLayoutDialog: () -> Unit = {},
    onOpenOutline: () -> Unit = {}
) {
    val currentViewMode by viewModel.viewMode.collectAsStateWithLifecycle()
    val zoomPercent by viewModel.zoomPercent.collectAsStateWithLifecycle()
    val canvasPaperMode by viewModel.canvasPaperMode.collectAsStateWithLifecycle()
    val showRuler by viewModel.showRuler.collectAsStateWithLifecycle()
    val showWordCount by viewModel.showWordCount.collectAsStateWithLifecycle()

    RibbonActionButton(
        icon = Icons.AutoMirrored.Filled.ListAlt,
        label = "Document Outline",
        onClick = onOpenOutline
    )

    Box(modifier = Modifier.height(20.dp).width(1.dp).background(MaterialTheme.colorScheme.outlineVariant))

    // Canvas Paper Theme
    RibbonToggleButton(
        icon = Icons.Default.LightMode,
        label = "White Sheet",
        isSelected = canvasPaperMode == "WHITE",
        onClick = { viewModel.setCanvasPaperMode("WHITE") }
    )

    RibbonToggleButton(
        icon = Icons.Default.Palette,
        label = "Eye-Care Sepia",
        isSelected = canvasPaperMode == "SEPIA",
        onClick = { viewModel.setCanvasPaperMode("SEPIA") }
    )

    RibbonToggleButton(
        icon = Icons.Default.DarkMode,
        label = "Night / Dark",
        isSelected = canvasPaperMode == "DARK",
        onClick = { viewModel.setCanvasPaperMode("DARK") }
    )

    Box(modifier = Modifier.height(20.dp).width(1.dp).background(MaterialTheme.colorScheme.outlineVariant))

    RibbonToggleButton(
        icon = Icons.Default.Description,
        label = "Page View",
        isSelected = currentViewMode == EditorViewMode.PAGINATED_A4,
        onClick = { viewModel.setViewMode(EditorViewMode.PAGINATED_A4) },
        tag = "view_mode_a4"
    )

    RibbonToggleButton(
        icon = Icons.Default.PhoneAndroid,
        label = "Mobile Continuous",
        isSelected = currentViewMode == EditorViewMode.CONTINUOUS_MOBILE,
        onClick = { viewModel.setViewMode(EditorViewMode.CONTINUOUS_MOBILE) },
        tag = "view_mode_mobile"
    )

    Box(modifier = Modifier.height(20.dp).width(1.dp).background(MaterialTheme.colorScheme.outlineVariant))

    // Ruler toggle
    RibbonToggleButton(
        icon = Icons.Default.SquareFoot,
        label = "Ruler",
        isSelected = showRuler,
        onClick = { viewModel.toggleRuler() }
    )

    // Word count status toggle
    RibbonToggleButton(
        icon = Icons.Default.BarChart,
        label = "Word Count",
        isSelected = showWordCount,
        onClick = { viewModel.toggleWordCount() }
    )

    Box(modifier = Modifier.height(20.dp).width(1.dp).background(MaterialTheme.colorScheme.outlineVariant))

    RibbonActionButton(
        icon = Icons.Default.Tune,
        label = "Page Setup",
        onClick = onOpenLayoutDialog
    )

    Box(modifier = Modifier.height(20.dp).width(1.dp).background(MaterialTheme.colorScheme.outlineVariant))

    Surface(
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier.height(34.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 4.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clickable { viewModel.setZoomPercent(zoomPercent - 10) }
                    .testTag("zoom_out_button"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.ZoomOut,
                    contentDescription = "Zoom Out",
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
            Text(
                text = "$zoomPercent%",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier
                    .padding(horizontal = 6.dp)
                    .testTag("zoom_percent_text")
            )
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clickable { viewModel.setZoomPercent(zoomPercent + 10) }
                    .testTag("zoom_in_button"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.ZoomIn,
                    contentDescription = "Zoom In",
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}
