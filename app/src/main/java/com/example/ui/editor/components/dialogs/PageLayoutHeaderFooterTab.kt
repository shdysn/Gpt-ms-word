package com.example.ui.editor.components.dialogs

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.layout.PageNumberFormat

@Composable
fun PageLayoutHeaderFooterTab(
    showHeader: Boolean,
    onShowHeaderChange: (Boolean) -> Unit,
    headerText: String,
    onHeaderTextChange: (String) -> Unit,
    showFooter: Boolean,
    onShowFooterChange: (Boolean) -> Unit,
    footerText: String,
    onFooterTextChange: (String) -> Unit,
    differentFirstPage: Boolean,
    onDifferentFirstPageChange: (Boolean) -> Unit,
    pageNumberFormat: PageNumberFormat,
    onPageNumberFormatChange: (PageNumberFormat) -> Unit
) {
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
                    Switch(checked = showHeader, onCheckedChange = onShowHeaderChange)
                }
                if (showHeader) {
                    OutlinedTextField(
                        value = headerText,
                        onValueChange = onHeaderTextChange,
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
                    Switch(checked = showFooter, onCheckedChange = onShowFooterChange)
                }
                if (showFooter) {
                    OutlinedTextField(
                        value = footerText,
                        onValueChange = onFooterTextChange,
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
                        PageNumberFormat.entries.forEach { pnf ->
                            FilterChip(
                                selected = pageNumberFormat == pnf,
                                onClick = { onPageNumberFormatChange(pnf) },
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
                .clickable { onDifferentFirstPageChange(!differentFirstPage) }
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Checkbox(
                    checked = differentFirstPage,
                    onCheckedChange = onDifferentFirstPageChange
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
