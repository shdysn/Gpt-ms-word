package com.example.ui.editor.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.TableBlock
import com.example.ui.theme.WordPrimary

@Composable
fun TableHeaderBar(
    table: TableBlock,
    widthFraction: Float,
    onAddRow: () -> Unit,
    onDeleteRow: () -> Unit,
    onAddCol: () -> Unit,
    onDeleteCol: () -> Unit,
    onEditProperties: () -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
        shape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp),
        modifier = Modifier.fillMaxWidth(widthFraction)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.TableChart,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = WordPrimary
                )
                Text(
                    text = "Table (${table.rowCount}×${table.columnCount})",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = WordPrimary.copy(alpha = 0.12f),
                    modifier = Modifier
                        .clickable(onClick = onAddRow)
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                        .testTag("quick_add_row_${table.id}")
                ) {
                    Text("+ Row", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = WordPrimary)
                }

                if (table.rowCount > 1) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.7f),
                        modifier = Modifier
                            .clickable(onClick = onDeleteRow)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                            .testTag("quick_del_row_${table.id}")
                    ) {
                        Text("- Row", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onErrorContainer)
                    }
                }

                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = WordPrimary.copy(alpha = 0.12f),
                    modifier = Modifier
                        .clickable(onClick = onAddCol)
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                        .testTag("quick_add_col_${table.id}")
                ) {
                    Text("+ Col", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = WordPrimary)
                }

                if (table.columnCount > 1) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.7f),
                        modifier = Modifier
                            .clickable(onClick = onDeleteCol)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                            .testTag("quick_del_col_${table.id}")
                    ) {
                        Text("- Col", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onErrorContainer)
                    }
                }

                IconButton(
                    onClick = onEditProperties,
                    modifier = Modifier.size(24.dp).testTag("table_properties_btn_${table.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Tune,
                        contentDescription = "Table Properties",
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
