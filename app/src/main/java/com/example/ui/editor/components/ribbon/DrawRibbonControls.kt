package com.example.ui.editor.components.ribbon

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Draw
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Highlight
import androidx.compose.material.icons.filled.HorizontalRule
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.editor.EditorViewModel
import com.example.ui.theme.WordPrimary

@Composable
fun DrawRibbonControls(
    viewModel: EditorViewModel,
    onInsertDrawing: () -> Unit = { viewModel.insertDrawing() }
) {
    val currentTool by viewModel.drawTool.collectAsStateWithLifecycle()
    val currentColorHex by viewModel.drawColorHex.collectAsStateWithLifecycle()
    val currentStrokeWidthPt by viewModel.drawStrokeWidthPt.collectAsStateWithLifecycle()

    RibbonToggleButton(
        icon = Icons.Default.Edit,
        label = "Pen",
        isSelected = currentTool == "PEN",
        onClick = { viewModel.setDrawTool("PEN") }
    )

    RibbonToggleButton(
        icon = Icons.Default.Highlight,
        label = "Highlighter",
        isSelected = currentTool == "HIGHLIGHTER",
        onClick = { viewModel.setDrawTool("HIGHLIGHTER") }
    )

    RibbonToggleButton(
        icon = Icons.Default.Clear,
        label = "Eraser",
        isSelected = currentTool == "ERASER",
        onClick = { viewModel.setDrawTool("ERASER") }
    )

    Box(modifier = Modifier.height(20.dp).width(1.dp).background(MaterialTheme.colorScheme.outlineVariant))

    listOf(1f to "Thin (1pt)", 2.5f to "Medium (2.5pt)", 4.5f to "Thick (4.5pt)", 7f to "Broad (7pt)").forEach { (w, label) ->
        RibbonToggleButton(
            icon = Icons.Default.HorizontalRule,
            label = label,
            isSelected = currentStrokeWidthPt == w,
            onClick = { viewModel.setDrawStrokeWidthPt(w) }
        )
    }

    Box(modifier = Modifier.height(20.dp).width(1.dp).background(MaterialTheme.colorScheme.outlineVariant))

    val swatches = listOf(
        "#0F172A" to "Black",
        "#1E40AF" to "Navy",
        "#DC2626" to "Red",
        "#059669" to "Green",
        "#D97706" to "Amber",
        "#7C3AED" to "Purple",
        "#FBBF24" to "Yellow High"
    )
    swatches.forEach { (hex, name) ->
        Surface(
            shape = RoundedCornerShape(6.dp),
            color = if (currentColorHex == hex) WordPrimary.copy(alpha = 0.15f) else Color.Transparent,
            border = if (currentColorHex == hex) BorderStroke(1.dp, WordPrimary) else null,
            modifier = Modifier
                .height(34.dp)
                .clickable {
                    viewModel.setDrawColorHex(hex)
                    if (currentTool == "ERASER") viewModel.setDrawTool("PEN")
                }
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 8.dp)
            ) {
                val c = try { Color(android.graphics.Color.parseColor(hex)) } catch (_: Exception) { Color.Black }
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .clip(CircleShape)
                        .background(c)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = name,
                    fontSize = 11.sp,
                    fontWeight = if (currentColorHex == hex) FontWeight.Bold else FontWeight.Normal,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }

    Box(modifier = Modifier.height(20.dp).width(1.dp).background(MaterialTheme.colorScheme.outlineVariant))

    RibbonActionButton(
        icon = Icons.Default.Draw,
        label = "Inking Canvas",
        onClick = onInsertDrawing
    )
}
