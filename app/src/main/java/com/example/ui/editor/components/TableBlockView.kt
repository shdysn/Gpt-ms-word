package com.example.ui.editor.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.TableCell
import com.example.data.model.TableBlock
import com.example.data.model.TextAlignment
import com.example.ui.theme.WordPrimary

fun isColorDarkHex(hex: String?): Boolean {
    if (hex == null) return false
    return try {
        val parsed = android.graphics.Color.parseColor(hex)
        val r = android.graphics.Color.red(parsed) / 255.0
        val g = android.graphics.Color.green(parsed) / 255.0
        val b = android.graphics.Color.blue(parsed) / 255.0
        val luminance = 0.2126 * r + 0.7152 * g + 0.0722 * b
        luminance < 0.45
    } catch (_: Exception) {
        false
    }
}

@Composable
fun TableBlockView(
    table: TableBlock,
    startRow: Int = 0,
    endRow: Int = table.rowCount,
    isDarkPage: Boolean = false,
    zoomScale: Float = 1f,
    onEditProperties: () -> Unit = {},
    onEditCell: (row: Int, col: Int) -> Unit = { _, _ -> },
    onAddRow: () -> Unit = {},
    onDeleteRow: () -> Unit = {},
    onAddCol: () -> Unit = {},
    onDeleteCol: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val tableBorderColor = try {
        Color(android.graphics.Color.parseColor(table.borderColorHex))
    } catch (_: Exception) {
        Color(0xFFCBD5E1)
    }

    val tableAlignment = when (table.alignment) {
        TextAlignment.START -> Alignment.Start
        TextAlignment.END -> Alignment.End
        else -> Alignment.CenterHorizontally
    }

    val widthFraction = (table.tableWidthPercent.coerceIn(20, 100) / 100f)
    val safeStartRow = startRow.coerceIn(0, table.rowCount)
    val safeEndRow = endRow.coerceIn(safeStartRow, table.rowCount)

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = tableAlignment
    ) {
        // Table Header Quick Action Toolbar (shown on first slice of table)
        if (safeStartRow == 0) {
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

        // Table Body Grid (renders only rows in [safeStartRow, safeEndRow))
        Column(
            modifier = Modifier
                .fillMaxWidth(widthFraction)
                .border(
                    width = if (table.hasBorders) 1.dp else 0.5.dp,
                    color = if (table.hasBorders) tableBorderColor else Color(0xFFE2E8F0),
                    shape = RoundedCornerShape(
                        topStart = if (safeStartRow > 0) 4.dp else 0.dp,
                        topEnd = if (safeStartRow > 0) 4.dp else 0.dp,
                        bottomStart = 8.dp,
                        bottomEnd = 8.dp
                    )
                )
        ) {
            for (r in safeStartRow until safeEndRow) {
                Row(modifier = Modifier.fillMaxWidth()) {
                    for (c in 0 until table.columnCount) {
                        val cell = table.cells.getOrNull(r)?.getOrNull(c) ?: TableCell()

                        val cellBg = when {
                            cell.backgroundColorHex != null -> try {
                                Color(android.graphics.Color.parseColor(cell.backgroundColorHex))
                            } catch (_: Exception) { if (isDarkPage) Color(0xFF1E293B) else Color.White }
                            r == 0 && table.hasHeaderRow -> if (isDarkPage) Color(0xFF334155) else Color(0xFFF1F5F9)
                            table.isZebraStriped && r % 2 == 1 -> if (isDarkPage) Color(0xFF242F40) else Color(0xFFF8FAFC)
                            else -> if (isDarkPage) Color(0xFF1E293B) else Color.White
                        }

                        val cellTextAlign = when (cell.alignment) {
                            TextAlignment.CENTER -> TextAlign.Center
                            TextAlignment.END -> TextAlign.End
                            TextAlignment.JUSTIFY -> TextAlign.Justify
                            TextAlignment.START -> TextAlign.Start
                        }

                        val cellIsDark = if (cell.backgroundColorHex != null) {
                            isColorDarkHex(cell.backgroundColorHex)
                        } else {
                            isDarkPage
                        }

                        val colWeight = table.columnWidthWeights.getOrNull(c) ?: 1f
                        Box(
                            modifier = Modifier
                                .weight(colWeight.coerceAtLeast(0.05f))
                                .border(
                                    width = 0.5.dp,
                                    color = if (table.hasBorders) tableBorderColor else Color(0xFFE2E8F0)
                                )
                                .background(cellBg)
                                .clickable { onEditCell(r, c) }
                                .padding(8.dp)
                                .testTag("table_cell_${table.id}_${r}_$c"),
                            contentAlignment = when (cell.alignment) {
                                TextAlignment.CENTER -> Alignment.Center
                                TextAlignment.END -> Alignment.CenterEnd
                                else -> Alignment.CenterStart
                            }
                        ) {
                            Text(
                                text = cell.text.ifBlank { "—" },
                                fontWeight = if (cell.isBold || (r == 0 && table.hasHeaderRow)) FontWeight.Bold else FontWeight.Normal,
                                fontStyle = if (cell.isItalic) FontStyle.Italic else FontStyle.Normal,
                                fontSize = (12f * zoomScale).sp,
                                textAlign = cellTextAlign,
                                color = when {
                                    cell.text.isBlank() -> if (cellIsDark) Color(0xFF64748B) else Color.LightGray
                                    cellIsDark -> Color(0xFFF8FAFC)
                                    else -> Color(0xFF1E293B)
                                },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }
        }
    }
}
