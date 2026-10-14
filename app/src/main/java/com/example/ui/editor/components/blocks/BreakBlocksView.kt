package com.example.ui.editor.components.blocks

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ColumnBreakBlock
import com.example.data.model.DividerBlock
import com.example.data.model.PageBreakBlock
import com.example.data.model.SectionBreakBlock
import com.example.ui.theme.WordPrimary

@Composable
fun DividerBlockItem(
    block: DividerBlock,
    isDarkPage: Boolean,
    canDeleteBlock: Boolean,
    onDeleteBlock: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        HorizontalDivider(
            color = if (isDarkPage) Color(0xFF475569) else Color(0xFFCBD5E1),
            thickness = 1.dp,
            modifier = Modifier.weight(1f)
        )
        if (canDeleteBlock) {
            IconButton(
                onClick = onDeleteBlock,
                modifier = Modifier
                    .size(24.dp)
                    .testTag("delete_divider_${block.id}")
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete Horizontal Line",
                    tint = if (isDarkPage) Color(0xFF94A3B8) else Color.Gray,
                    modifier = Modifier.size(14.dp)
                )
            }
        }
    }
}

@Composable
fun PageBreakBlockItem(
    block: PageBreakBlock,
    canDeleteBlock: Boolean,
    onDeleteBlock: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        HorizontalDivider(modifier = Modifier.weight(1f), color = WordPrimary.copy(alpha = 0.4f))
        Text(
            text = " PAGE BREAK ",
            fontSize = 10.sp,
            color = WordPrimary,
            fontWeight = FontWeight.Bold
        )
        HorizontalDivider(modifier = Modifier.weight(1f), color = WordPrimary.copy(alpha = 0.4f))
        if (canDeleteBlock) {
            IconButton(
                onClick = onDeleteBlock,
                modifier = Modifier
                    .size(24.dp)
                    .testTag("delete_page_break_${block.id}")
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Remove Page Break",
                    tint = WordPrimary,
                    modifier = Modifier.size(14.dp)
                )
            }
        }
    }
}

@Composable
fun ColumnBreakBlockItem(
    block: ColumnBreakBlock,
    canDeleteBlock: Boolean,
    onDeleteBlock: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        HorizontalDivider(modifier = Modifier.weight(1f), color = Color(0xFF0284C7).copy(alpha = 0.4f))
        Text(
            text = " COLUMN BREAK ",
            fontSize = 10.sp,
            color = Color(0xFF0284C7),
            fontWeight = FontWeight.Bold
        )
        HorizontalDivider(modifier = Modifier.weight(1f), color = Color(0xFF0284C7).copy(alpha = 0.4f))
        if (canDeleteBlock) {
            IconButton(
                onClick = onDeleteBlock,
                modifier = Modifier
                    .size(24.dp)
                    .testTag("delete_col_break_${block.id}")
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Remove Column Break",
                    tint = Color(0xFF0284C7),
                    modifier = Modifier.size(14.dp)
                )
            }
        }
    }
}

@Composable
fun SectionBreakBlockItem(
    block: SectionBreakBlock,
    canDeleteBlock: Boolean,
    onDeleteBlock: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(4.dp),
        color = Color(0xFF6366F1).copy(alpha = 0.08f),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF6366F1).copy(alpha = 0.35f)),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "§ SECTION BREAK: ${block.sectionName} (${block.orientation}, ${if (block.isNextPage) "Next Page" else "Continuous"})",
                fontSize = 10.sp,
                color = Color(0xFF6366F1),
                fontWeight = FontWeight.SemiBold
            )
            if (canDeleteBlock) {
                IconButton(
                    onClick = onDeleteBlock,
                    modifier = Modifier
                        .size(20.dp)
                        .testTag("delete_section_break_${block.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Remove Section Break",
                        tint = Color(0xFF6366F1),
                        modifier = Modifier.size(13.dp)
                    )
                }
            }
        }
    }
}
