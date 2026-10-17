package com.example.ui.editor.components.bottomsheet

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ListType
import com.example.ui.theme.WordPrimary

/**
 * Bullets gallery preview cards matching MS Word Mobile (Screenshot 4).
 */
@Composable
fun BulletsSubmenuView(
    currentListType: ListType,
    onSelectBullet: (BulletGalleryStyle) -> Unit,
    modifier: Modifier = Modifier
) {
    val items = BulletGalleryStyle.entries
    val chunked = items.chunked(4)

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        chunked.forEach { rowItems ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                rowItems.forEach { style ->
                    val isSelected = when (style) {
                        BulletGalleryStyle.NONE -> currentListType == ListType.NONE
                        else -> currentListType == ListType.BULLET
                    }

                    BulletPreviewCard(
                        style = style,
                        isSelected = isSelected,
                        onClick = { onSelectBullet(style) },
                        modifier = Modifier.weight(1f)
                    )
                }
                // Fill remainder of row if incomplete
                if (rowItems.size < 4) {
                    repeat(4 - rowItems.size) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
private fun BulletPreviewCard(
    style: BulletGalleryStyle,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(6.dp),
        border = BorderStroke(
            width = if (isSelected) 2.dp else 1.dp,
            color = if (isSelected) WordPrimary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
        ),
        color = if (isSelected) WordPrimary.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surface,
        modifier = modifier
            .height(64.dp)
            .clip(RoundedCornerShape(6.dp))
            .clickable { onClick() }
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.padding(4.dp)
        ) {
            if (style == BulletGalleryStyle.NONE) {
                Text(
                    text = "None",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = if (isSelected) WordPrimary else MaterialTheme.colorScheme.onSurface
                )
            } else {
                Text(
                    text = style.symbol,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isSelected) WordPrimary else MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

/**
 * Numbering gallery preview cards matching MS Word Mobile (Screenshot 8).
 */
@Composable
fun NumberingSubmenuView(
    currentListType: ListType,
    onSelectNumbering: (NumberingGalleryStyle) -> Unit,
    modifier: Modifier = Modifier
) {
    val items = NumberingGalleryStyle.entries
    val chunked = items.chunked(4)

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        chunked.forEach { rowItems ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                rowItems.forEach { style ->
                    val isSelected = when (style) {
                        NumberingGalleryStyle.NONE -> currentListType == ListType.NONE
                        else -> currentListType == ListType.NUMBERED
                    }

                    NumberingPreviewCard(
                        style = style,
                        isSelected = isSelected,
                        onClick = { onSelectNumbering(style) },
                        modifier = Modifier.weight(1f)
                    )
                }
                if (rowItems.size < 4) {
                    repeat(4 - rowItems.size) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
private fun NumberingPreviewCard(
    style: NumberingGalleryStyle,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(6.dp),
        border = BorderStroke(
            width = if (isSelected) 2.dp else 1.dp,
            color = if (isSelected) WordPrimary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
        ),
        color = if (isSelected) WordPrimary.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surface,
        modifier = modifier
            .height(76.dp)
            .clip(RoundedCornerShape(6.dp))
            .clickable { onClick() }
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 6.dp)
        ) {
            if (style == NumberingGalleryStyle.NONE) {
                Text(
                    text = "None",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = if (isSelected) WordPrimary else MaterialTheme.colorScheme.onSurface
                )
            } else {
                Column(
                    verticalArrangement = Arrangement.spacedBy(3.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    NumberingPreviewLine(style.preview1)
                    NumberingPreviewLine(style.preview2)
                    NumberingPreviewLine(style.preview3)
                }
            }
        }
    }
}

@Composable
private fun NumberingPreviewLine(prefix: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = prefix,
            style = MaterialTheme.typography.labelSmall,
            fontSize = 9.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Box(
            modifier = Modifier
                .weight(1f)
                .height(1.dp)
                .background(MaterialTheme.colorScheme.outlineVariant)
        )
    }
}
