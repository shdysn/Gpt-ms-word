package com.example.ui.editor.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.layout.PageLayoutConfig
import com.example.core.layout.PageOrientation

@Composable
fun DocumentRulerBar(
    layout: PageLayoutConfig,
    maxSheetWidth: Dp
) {
    val leftMarginDp = (layout.leftMarginMm * 1.25f).dp.coerceIn(16.dp, 48.dp)
    val rightMarginDp = (layout.rightMarginMm * 1.25f).dp.coerceIn(16.dp, 48.dp)
    val totalWidthMm = if (layout.orientation == PageOrientation.LANDSCAPE) {
        layout.pageSize.heightMm.toInt()
    } else {
        layout.pageSize.widthMm.toInt()
    }

    Surface(
        shape = RoundedCornerShape(4.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        border = androidx.compose.foundation.BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier
            .width(maxSheetWidth)
            .padding(bottom = 8.dp)
            .height(22.dp)
            .testTag("document_ruler_bar")
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left Margin Shaded Zone
            Box(
                modifier = Modifier
                    .width(leftMarginDp)
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "${layout.leftMarginMm.toInt()}",
                    fontSize = 8.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Printable Content Tick Marks (in cm)
            Row(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                val cmSteps = (totalWidthMm / 20).coerceIn(4, 12)
                for (i in 1..cmSteps) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "${i * 2}",
                            fontSize = 8.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Box(
                            modifier = Modifier
                                .width(1.dp)
                                .height(4.dp)
                                .background(MaterialTheme.colorScheme.onSurfaceVariant)
                        )
                    }
                }
            }

            // Right Margin Shaded Zone
            Box(
                modifier = Modifier
                    .width(rightMarginDp)
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "${layout.rightMarginMm.toInt()}",
                    fontSize = 8.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
