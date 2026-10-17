package com.example.ui.editor.components.bottomsheet

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.editor.EditorViewModel

/**
 * Microsoft Word Mobile Table Insertion Submenu.
 * Displays row/column steppers and an "Insert Table" button.
 */
@Composable
fun BottomRibbonInsertTableSubmenu(
    viewModel: EditorViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var rows by remember { mutableIntStateOf(3) }
    var columns by remember { mutableIntStateOf(3) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        // Rows Stepper
        TableRowColumnStepper(
            icon = { WordTableRowIcon() },
            label = "Rows",
            value = rows,
            onDecrement = { if (rows > 1) rows-- },
            onIncrement = { if (rows < 20) rows++ }
        )

        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), thickness = 0.5.dp)

        // Columns Stepper
        TableRowColumnStepper(
            icon = { WordTableColumnIcon() },
            label = "Columns",
            value = columns,
            onDecrement = { if (columns > 1) columns-- },
            onIncrement = { if (columns < 12) columns++ }
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Blue "Insert Table" Button
        Button(
            onClick = {
                viewModel.insertTable(rows = rows, cols = columns)
                viewModel.showStatusMessage("Table ($rows × $columns) inserted")
                onBack()
            },
            shape = RoundedCornerShape(4.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F6CBD)),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("insert_table_confirm_button")
        ) {
            Text(
                text = "Insert Table",
                color = Color.White,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun TableRowColumnStepper(
    icon: @Composable () -> Unit,
    label: String,
    value: Int,
    onDecrement: () -> Unit,
    onIncrement: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp)
            .padding(horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.size(24.dp), contentAlignment = Alignment.Center) {
            icon()
        }

        Spacer(modifier = Modifier.width(16.dp))

        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )

        // Stepper: - value +
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Surface(
                modifier = Modifier
                    .size(36.dp)
                    .clickable(onClick = onDecrement),
                shape = RoundedCornerShape(4.dp),
                color = Color.Transparent
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text("—", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F6CBD))
                }
            }

            Box(modifier = Modifier.width(28.dp), contentAlignment = Alignment.Center) {
                Text(
                    text = value.toString(),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Surface(
                modifier = Modifier
                    .size(36.dp)
                    .clickable(onClick = onIncrement),
                shape = RoundedCornerShape(4.dp),
                color = Color.Transparent
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text("+", fontSize = 22.sp, fontWeight = FontWeight.Normal, color = Color(0xFF0F6CBD))
                }
            }
        }
    }
}
