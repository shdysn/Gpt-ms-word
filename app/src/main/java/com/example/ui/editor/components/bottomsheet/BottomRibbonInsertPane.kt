package com.example.ui.editor.components.bottomsheet

import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.InsertDriveFile
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.DocumentScanner
import androidx.compose.material.icons.filled.Draw
import androidx.compose.material.icons.filled.FormatQuote
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Numbers
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material.icons.filled.ViewHeadline
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.editor.EditorDialogsState
import com.example.ui.editor.EditorViewModel
import com.example.ui.theme.WordPrimary

/**
 * Categorized vertical layout for the Insert tab in the Bottom Ribbon Drawer.
 * Groups elements into Media, Structure, and Document Headers/Footers.
 */
@Composable
fun BottomRibbonInsertPane(
    viewModel: EditorViewModel,
    dState: EditorDialogsState,
    photoPickerLauncher: ActivityResultLauncher<PickVisualMediaRequest>,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Category 1: Tables, Media & Visuals
        InsertCategoryGroup(title = "Media & Elements") {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                InsertTileCard(
                    icon = Icons.Default.TableChart,
                    title = "Table",
                    subtitle = "Rows & cols",
                    onClick = { dState.showTableDialog = true },
                    modifier = Modifier.weight(1f)
                )
                InsertTileCard(
                    icon = Icons.Default.Image,
                    title = "Picture",
                    subtitle = "From gallery",
                    onClick = {
                        photoPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                    modifier = Modifier.weight(1f)
                )
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                InsertTileCard(
                    icon = Icons.Default.DocumentScanner,
                    title = "Scan OCR",
                    subtitle = "Text from photo",
                    onClick = { dState.showOcrScanDialog = true },
                    modifier = Modifier.weight(1f)
                )
                InsertTileCard(
                    icon = Icons.Default.Category,
                    title = "Shapes",
                    subtitle = "Arrows & boxes",
                    onClick = { dState.showShapeDialog = true },
                    modifier = Modifier.weight(1f)
                )
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                InsertTileCard(
                    icon = Icons.Default.Draw,
                    title = "Signature",
                    subtitle = "Digital sign",
                    onClick = { dState.showSignatureDialog = true },
                    modifier = Modifier.weight(1f)
                )
                InsertTileCard(
                    icon = Icons.Default.Link,
                    title = "Link",
                    subtitle = "Web URL",
                    onClick = { dState.showLinkDialog = true },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Category 2: Document Structure & Pages
        InsertCategoryGroup(title = "Pages & Breaks") {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                InsertTileCard(
                    icon = Icons.AutoMirrored.Filled.InsertDriveFile,
                    title = "Page Break",
                    subtitle = "Start next page",
                    onClick = { viewModel.insertPageBreak() },
                    modifier = Modifier.weight(1f)
                )
                InsertTileCard(
                    icon = Icons.Default.ViewHeadline,
                    title = "Header & Footer",
                    subtitle = "Top & bottom notes",
                    onClick = { dState.showHeaderFooterDialog = true },
                    modifier = Modifier.weight(1f)
                )
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                InsertTileCard(
                    icon = Icons.Default.Numbers,
                    title = "Page Numbers",
                    subtitle = "Page 1 of N",
                    onClick = { dState.showPageNumberDialog = true },
                    modifier = Modifier.weight(1f)
                )
                InsertTileCard(
                    icon = Icons.Default.FormatQuote,
                    title = "Footnote",
                    subtitle = "Citation note",
                    onClick = { dState.showFootnoteDialog = true },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun InsertCategoryGroup(
    title: String,
    content: @Composable () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = WordPrimary
        )
        content()
    }
}

@Composable
private fun InsertTileCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = WordPrimary.copy(alpha = 0.12f),
                modifier = Modifier.size(36.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icon, contentDescription = null, tint = WordPrimary, modifier = Modifier.size(20.dp))
                }
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                Text(subtitle, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
