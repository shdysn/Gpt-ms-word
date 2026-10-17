package com.example.ui.editor.components.bottomsheet

import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.PickVisualMediaRequest
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.editor.EditorDialogsState
import com.example.ui.editor.EditorViewModel

/**
 * Microsoft Word Mobile Insert Tab Pane.
 * Displays the complete 13-item Insert menu hierarchy and routes to submenus.
 */
@Composable
fun BottomRibbonInsertPane(
    viewModel: EditorViewModel,
    dState: EditorDialogsState,
    photoPickerLauncher: ActivityResultLauncher<PickVisualMediaRequest>,
    currentSubmenu: InsertSubmenu,
    onNavigateSubmenu: (InsertSubmenu) -> Unit,
    modifier: Modifier = Modifier
) {
    when (currentSubmenu) {
        InsertSubmenu.TABLE -> {
            BottomRibbonInsertTableSubmenu(
                viewModel = viewModel,
                onBack = { onNavigateSubmenu(InsertSubmenu.ROOT) },
                modifier = modifier
            )
        }
        InsertSubmenu.PICTURES -> {
            BottomRibbonInsertPicturesSubmenu(
                photoPickerLauncher = photoPickerLauncher,
                dState = dState,
                onBack = { onNavigateSubmenu(InsertSubmenu.ROOT) },
                modifier = modifier
            )
        }
        InsertSubmenu.SHAPES -> {
            BottomRibbonInsertShapesSubmenu(
                viewModel = viewModel,
                onBack = { onNavigateSubmenu(InsertSubmenu.ROOT) },
                modifier = modifier
            )
        }
        InsertSubmenu.LINK -> {
            BottomRibbonInsertLinkSubmenu(
                viewModel = viewModel,
                dState = dState,
                onBack = { onNavigateSubmenu(InsertSubmenu.ROOT) },
                modifier = modifier
            )
        }
        InsertSubmenu.PAGE_NUMBER -> {
            BottomRibbonInsertPageNumberSubmenu(
                viewModel = viewModel,
                onBack = { onNavigateSubmenu(InsertSubmenu.ROOT) },
                modifier = modifier
            )
        }
        InsertSubmenu.ROOT -> {
            Column(
                modifier = modifier
                    .fillMaxWidth()
                    .padding(vertical = 2.dp)
            ) {
                // 1. Blank Page
                InsertMenuRow(
                    icon = { WordBlankPageIcon() },
                    title = "Blank Page",
                    hasSubmenu = false,
                    onClick = {
                        viewModel.insertPageBreak()
                        viewModel.showStatusMessage("Blank page inserted")
                    }
                )

                // 2. Scan Document
                InsertMenuRow(
                    icon = { WordScanDocIcon() },
                    title = "Scan Document",
                    hasSubmenu = false,
                    onClick = { dState.showOcrScanDialog = true }
                )

                // 3. Table >
                InsertMenuRow(
                    icon = { WordTableGridIcon() },
                    title = "Table",
                    hasSubmenu = true,
                    onClick = { onNavigateSubmenu(InsertSubmenu.TABLE) }
                )

                // 4. Pictures >
                InsertMenuRow(
                    icon = { WordPicturesIcon() },
                    title = "Pictures",
                    hasSubmenu = true,
                    onClick = { onNavigateSubmenu(InsertSubmenu.PICTURES) }
                )

                // 5. Shapes >
                InsertMenuRow(
                    icon = { WordShapesCategoryIcon() },
                    title = "Shapes",
                    hasSubmenu = true,
                    onClick = { onNavigateSubmenu(InsertSubmenu.SHAPES) }
                )

                // 6. Text Box
                InsertMenuRow(
                    icon = { WordTextBoxIcon() },
                    title = "Text Box",
                    hasSubmenu = false,
                    onClick = {
                        viewModel.insertTextBox()
                        viewModel.showStatusMessage("Text box inserted")
                    }
                )

                // 7. Link >
                InsertMenuRow(
                    icon = { WordLinkChainIcon() },
                    title = "Link",
                    hasSubmenu = true,
                    onClick = { onNavigateSubmenu(InsertSubmenu.LINK) }
                )

                // 8. Comment
                InsertMenuRow(
                    icon = { WordCommentBubbleIcon() },
                    title = "Comment",
                    hasSubmenu = false,
                    onClick = { dState.showCommentsDialog = true }
                )

                // 9. Header & Footer
                InsertMenuRow(
                    icon = { WordHeaderFooterPageIcon() },
                    title = "Header & Footer",
                    hasSubmenu = false,
                    onClick = { dState.showHeaderFooterDialog = true }
                )

                // 10. Page Number >
                InsertMenuRow(
                    icon = { WordPageNumberPageIcon() },
                    title = "Page Number",
                    hasSubmenu = true,
                    onClick = { onNavigateSubmenu(InsertSubmenu.PAGE_NUMBER) }
                )

                // 11. Equation
                InsertMenuRow(
                    icon = { WordEquationPiIcon() },
                    title = "Equation",
                    hasSubmenu = false,
                    onClick = {
                        viewModel.insertTextBox("E = mc²")
                        viewModel.showStatusMessage("Equation placeholder inserted")
                    }
                )

                // 12. Footnote
                InsertMenuRow(
                    icon = { WordFootnoteAbIcon() },
                    title = "Footnote",
                    hasSubmenu = false,
                    onClick = { dState.showFootnoteDialog = true }
                )

                // 13. Endnote
                InsertMenuRow(
                    icon = { WordEndnoteIcon() },
                    title = "Endnote",
                    hasSubmenu = false,
                    onClick = {
                        viewModel.insertFootnote("Endnote", "Endnote reference text")
                        viewModel.showStatusMessage("Endnote inserted")
                    }
                )
            }
        }
    }
}

@Composable
private fun InsertMenuRow(
    icon: @Composable () -> Unit,
    title: String,
    hasSubmenu: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(50.dp)
            .clickable(onClick = onClick)
            .padding(horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.size(24.dp), contentAlignment = Alignment.Center) {
            icon()
        }

        Spacer(modifier = Modifier.width(16.dp))

        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            fontSize = 15.sp,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )

        if (hasSubmenu) {
            Icon(
                Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = "$title Submenu",
                tint = Color(0xFF757575),
                modifier = Modifier.size(20.dp)
            )
        }
    }
    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f), thickness = 0.5.dp)
}
