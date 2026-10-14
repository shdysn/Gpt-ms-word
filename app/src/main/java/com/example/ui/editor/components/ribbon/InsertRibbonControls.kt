package com.example.ui.editor.components.ribbon

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ListAlt
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.DocumentScanner
import androidx.compose.material.icons.filled.Draw
import androidx.compose.material.icons.filled.FormatListNumbered
import androidx.compose.material.icons.filled.HorizontalRule
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.filled.Title
import androidx.compose.material.icons.filled.VerticalSplit
import androidx.compose.material.icons.filled.ViewColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.ui.editor.EditorViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun InsertRibbonControls(
    viewModel: EditorViewModel,
    onOpenTableDialog: () -> Unit,
    onOpenPhotoPicker: () -> Unit,
    onOpenLinkDialog: () -> Unit,
    onOpenHeaderFooterDialog: () -> Unit,
    onOpenPageNumberDialog: () -> Unit,
    onOpenSignatureDialog: () -> Unit,
    onOpenShapeDialog: () -> Unit,
    onOpenFootnoteDialog: () -> Unit = {},
    onOpenOcrScanDialog: () -> Unit = {}
) {
    RibbonActionButton(
        icon = Icons.Default.Add,
        label = "Paragraph",
        onClick = { viewModel.insertParagraph() }
    )

    RibbonActionButton(
        icon = Icons.Default.TableChart,
        label = "Table",
        onClick = onOpenTableDialog
    )

    RibbonActionButton(
        icon = Icons.Default.Image,
        label = "Picture",
        onClick = onOpenPhotoPicker
    )

    RibbonActionButton(
        icon = Icons.Default.DocumentScanner,
        label = "Scan Doc (OCR)",
        onClick = onOpenOcrScanDialog
    )

    RibbonActionButton(
        icon = Icons.Default.TextFields,
        label = "Text Box",
        onClick = { viewModel.insertTextBox() }
    )

    RibbonActionButton(
        icon = Icons.Default.Draw,
        label = "Inking Canvas",
        onClick = { viewModel.insertDrawing() }
    )

    RibbonActionButton(
        icon = Icons.AutoMirrored.Filled.ListAlt,
        label = "Table of Contents",
        onClick = { viewModel.insertTableOfContents() }
    )

    RibbonActionButton(
        icon = Icons.Default.FormatListNumbered,
        label = "Footnote",
        onClick = onOpenFootnoteDialog
    )

    RibbonActionButton(
        icon = Icons.Default.Draw,
        label = "Signature",
        onClick = onOpenSignatureDialog
    )

    RibbonActionButton(
        icon = Icons.Default.Category,
        label = "Vector Shapes",
        onClick = onOpenShapeDialog
    )

    RibbonActionButton(
        icon = Icons.Default.HorizontalRule,
        label = "Horizontal Line",
        onClick = { viewModel.insertDivider() }
    )

    RibbonActionButton(
        icon = Icons.Default.Title,
        label = "Page Break",
        onClick = { viewModel.insertPageBreak() }
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

    Box(
        modifier = Modifier
            .height(20.dp)
            .width(1.dp)
            .background(MaterialTheme.colorScheme.outlineVariant)
    )

    RibbonActionButton(
        icon = Icons.Default.Link,
        label = "Hyperlink",
        onClick = onOpenLinkDialog
    )

    RibbonActionButton(
        icon = Icons.Default.VerticalSplit,
        label = "Header & Footer",
        onClick = onOpenHeaderFooterDialog
    )

    RibbonActionButton(
        icon = Icons.Default.FormatListNumbered,
        label = "Page Numbers",
        onClick = onOpenPageNumberDialog
    )

    Box(
        modifier = Modifier
            .height(20.dp)
            .width(1.dp)
            .background(MaterialTheme.colorScheme.outlineVariant)
    )

    val dateFormat = remember { SimpleDateFormat("MMMM d, yyyy", Locale.getDefault()) }
    RibbonActionButton(
        icon = Icons.Default.CalendarMonth,
        label = "Date & Time",
        onClick = {
            val dateStr = dateFormat.format(Date())
            viewModel.insertDateTimeStamp(dateStr)
        }
    )
}
