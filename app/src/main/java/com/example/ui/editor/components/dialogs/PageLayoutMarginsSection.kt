package com.example.ui.editor.components.dialogs

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun MarginSteppersCard(
    topMarginMm: Float,
    onTopMarginChange: (Float) -> Unit,
    bottomMarginMm: Float,
    onBottomMarginChange: (Float) -> Unit,
    leftMarginMm: Float,
    onLeftMarginChange: (Float) -> Unit,
    rightMarginMm: Float,
    onRightMarginChange: (Float) -> Unit
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text("Margin Dimensions (mm)", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                MarginStepperItem(
                    label = "Top",
                    valueMm = topMarginMm,
                    modifier = Modifier.weight(1f),
                    onMinus = { onTopMarginChange((topMarginMm - 5f).coerceAtLeast(5f)) },
                    onPlus = { onTopMarginChange((topMarginMm + 5f).coerceAtMost(60f)) }
                )
                MarginStepperItem(
                    label = "Bottom",
                    valueMm = bottomMarginMm,
                    modifier = Modifier.weight(1f),
                    onMinus = { onBottomMarginChange((bottomMarginMm - 5f).coerceAtLeast(5f)) },
                    onPlus = { onBottomMarginChange((bottomMarginMm + 5f).coerceAtMost(60f)) }
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                MarginStepperItem(
                    label = "Left",
                    valueMm = leftMarginMm,
                    modifier = Modifier.weight(1f),
                    onMinus = { onLeftMarginChange((leftMarginMm - 5f).coerceAtLeast(5f)) },
                    onPlus = { onLeftMarginChange((leftMarginMm + 5f).coerceAtMost(60f)) }
                )
                MarginStepperItem(
                    label = "Right",
                    valueMm = rightMarginMm,
                    modifier = Modifier.weight(1f),
                    onMinus = { onRightMarginChange((rightMarginMm - 5f).coerceAtLeast(5f)) },
                    onPlus = { onRightMarginChange((rightMarginMm + 5f).coerceAtMost(60f)) }
                )
            }
        }
    }
}

@Composable
fun MarginStepperItem(
    label: String,
    valueMm: Float,
    modifier: Modifier = Modifier,
    onMinus: () -> Unit,
    onPlus: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(label, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("${valueMm.toInt()} mm", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier
                        .size(28.dp)
                        .clickable(onClick = onMinus)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Remove, contentDescription = "Decrease $label", modifier = Modifier.size(14.dp))
                    }
                }
                Spacer(modifier = Modifier.width(4.dp))
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier
                        .size(28.dp)
                        .clickable(onClick = onPlus)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Add, contentDescription = "Increase $label", modifier = Modifier.size(14.dp))
                    }
                }
            }
        }
    }
}
