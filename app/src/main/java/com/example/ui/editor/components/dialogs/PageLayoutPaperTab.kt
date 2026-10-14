package com.example.ui.editor.components.dialogs

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.FilterChip
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
import com.example.core.layout.MarginPreset
import com.example.core.layout.PageOrientation
import com.example.core.layout.PageSize
import com.example.ui.theme.WordPrimary

@Composable
fun PageLayoutPaperTab(
    selectedOrientation: PageOrientation,
    onOrientationChange: (PageOrientation) -> Unit,
    selectedPageSize: PageSize,
    onPageSizeChange: (PageSize) -> Unit,
    selectedMargins: MarginPreset,
    onMarginsPresetChange: (MarginPreset, Float, Float, Float, Float) -> Unit,
    topMarginMm: Float,
    onTopMarginChange: (Float) -> Unit,
    bottomMarginMm: Float,
    onBottomMarginChange: (Float) -> Unit,
    leftMarginMm: Float,
    onLeftMarginChange: (Float) -> Unit,
    rightMarginMm: Float,
    onRightMarginChange: (Float) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        // Orientation Cards
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("Orientation", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                val isPortrait = selectedOrientation == PageOrientation.PORTRAIT
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (isPortrait) WordPrimary.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                    border = androidx.compose.foundation.BorderStroke(
                        if (isPortrait) 1.5.dp else 0.5.dp,
                        if (isPortrait) WordPrimary else MaterialTheme.colorScheme.outlineVariant
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onOrientationChange(PageOrientation.PORTRAIT) }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(14.dp, 20.dp)
                                .border(
                                    1.5.dp,
                                    if (isPortrait) WordPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    RoundedCornerShape(2.dp)
                                )
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Portrait",
                            fontSize = 12.sp,
                            fontWeight = if (isPortrait) FontWeight.Bold else FontWeight.Medium,
                            color = if (isPortrait) WordPrimary else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                val isLandscape = selectedOrientation == PageOrientation.LANDSCAPE
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (isLandscape) WordPrimary.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                    border = androidx.compose.foundation.BorderStroke(
                        if (isLandscape) 1.5.dp else 0.5.dp,
                        if (isLandscape) WordPrimary else MaterialTheme.colorScheme.outlineVariant
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onOrientationChange(PageOrientation.LANDSCAPE) }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(20.dp, 14.dp)
                                .border(
                                    1.5.dp,
                                    if (isLandscape) WordPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    RoundedCornerShape(2.dp)
                                )
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Landscape",
                            fontSize = 12.sp,
                            fontWeight = if (isLandscape) FontWeight.Bold else FontWeight.Medium,
                            color = if (isLandscape) WordPrimary else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }

        // Paper Size Selector
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("Paper Size", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(PageSize.A4, PageSize.LETTER, PageSize.LEGAL, PageSize.A5, PageSize.EXECUTIVE).forEach { ps ->
                    val isSelected = selectedPageSize == ps
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isSelected) WordPrimary.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                        border = androidx.compose.foundation.BorderStroke(
                            if (isSelected) 1.dp else 0.5.dp,
                            if (isSelected) WordPrimary else MaterialTheme.colorScheme.outlineVariant
                        ),
                        modifier = Modifier.clickable { onPageSizeChange(ps) }
                    ) {
                        Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp)) {
                            Text(
                                text = ps.name,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) WordPrimary else MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "${ps.widthMm.toInt()}×${ps.heightMm.toInt()} mm",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        // Margins Presets
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("Margins Preset", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(MarginPreset.NORMAL, MarginPreset.NARROW, MarginPreset.MODERATE, MarginPreset.WIDE, MarginPreset.CUSTOM).forEach { mp ->
                    val isSelected = selectedMargins == mp
                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            if (mp != MarginPreset.CUSTOM) {
                                onMarginsPresetChange(mp, mp.topMm, mp.bottomMm, mp.leftMm, mp.rightMm)
                            } else {
                                onMarginsPresetChange(mp, topMarginMm, bottomMarginMm, leftMarginMm, rightMarginMm)
                            }
                        },
                        label = {
                            Text(
                                text = mp.displayName.substringBefore(" ("),
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Margin Steppers Card
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                border = androidx.compose.foundation.BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant),
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
    }
}

@Composable
private fun MarginStepperItem(
    label: String,
    valueMm: Float,
    modifier: Modifier = Modifier,
    onMinus: () -> Unit,
    onPlus: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant),
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
