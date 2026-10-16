package com.example.ui.editor.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
    maxSheetWidth: Dp,
    firstLineIndentPt: Float = 0f,
    hangingIndentPt: Float = 0f,
    leftIndentPt: Float = 0f,
    tabStops: List<com.example.data.model.TabStop> = emptyList(),
    onUpdateIndents: ((firstLinePt: Float, hangingPt: Float, leftPt: Float) -> Unit)? = null,
    onAddTabStop: ((positionPt: Float) -> Unit)? = null
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
        color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier
            .width(maxSheetWidth)
            .padding(bottom = 6.dp)
            .height(26.dp)
            .testTag("document_ruler_bar")
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
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

            // Interactive Indent Markers (First Line: top triangle, Hanging: bottom triangle)
            val firstLineMarkerOffset = (leftMarginDp + (firstLineIndentPt * 0.75f).dp).coerceIn(leftMarginDp, maxSheetWidth - rightMarginDp)
            val hangingMarkerOffset = (leftMarginDp + (hangingIndentPt * 0.75f).dp).coerceIn(leftMarginDp, maxSheetWidth - rightMarginDp)

            // Top Triangle Marker (First Line Indent)
            Box(
                modifier = Modifier
                    .padding(start = firstLineMarkerOffset)
                    .align(Alignment.TopStart)
                    .size(width = 8.dp, height = 7.dp)
                    .background(com.example.ui.theme.WordPrimary, shape = RoundedCornerShape(bottomStart = 3.dp, bottomEnd = 3.dp))
            )

            // Bottom Triangle Marker (Hanging Indent)
            Box(
                modifier = Modifier
                    .padding(start = hangingMarkerOffset)
                    .align(Alignment.BottomStart)
                    .size(width = 8.dp, height = 7.dp)
                    .background(com.example.ui.theme.WordPrimary, shape = RoundedCornerShape(topStart = 3.dp, topEnd = 3.dp))
            )

            // Tab Stops Markers
            for (tab in tabStops) {
                val tabOffset = (leftMarginDp + (tab.positionPt * 0.75f).dp).coerceIn(leftMarginDp, maxSheetWidth - rightMarginDp)
                Box(
                    modifier = Modifier
                        .padding(start = tabOffset)
                        .align(Alignment.BottomStart)
                        .size(width = 4.dp, height = 9.dp)
                        .background(MaterialTheme.colorScheme.primary)
                )
            }
        }
    }
}
