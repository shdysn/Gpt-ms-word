package com.example.ui.editor.components.blocks

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.typography.BidiUtils
import com.example.data.model.ListType
import com.example.data.model.ParagraphBlock
import com.example.ui.theme.WordPrimary

@Composable
fun ParagraphPrefixView(
    block: ParagraphBlock,
    itemNumber: Int?,
    baseFontSize: TextUnit,
    isDarkPage: Boolean,
    onToggleChecklistItem: (String) -> Unit
) {
    if (block.isChecklist) {
        IconButton(
            onClick = { onToggleChecklistItem(block.id) },
            modifier = Modifier
                .size(24.dp)
                .padding(end = 4.dp)
                .testTag("checklist_checkbox_${block.id}")
        ) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = if (block.isChecked) "Completed" else "Todo",
                tint = if (block.isChecked) Color(0xFF10B981) else (if (isDarkPage) Color(0xFF64748B) else Color(0xFFCBD5E1)),
                modifier = Modifier.size(18.dp)
            )
        }
    }

    when (block.listType) {
        ListType.BULLET -> {
            val bulletChar = when (block.listLevel % 4) {
                1 -> "◦ "
                2 -> "▪ "
                3 -> "▫ "
                else -> "• "
            }
            Text(
                text = bulletChar,
                fontWeight = FontWeight.Bold,
                fontSize = baseFontSize,
                color = if (isDarkPage) Color(0xFF60A5FA) else WordPrimary,
                modifier = Modifier.padding(end = 6.dp)
            )
        }
        ListType.NUMBERED -> {
            val rawNum = itemNumber ?: 1
            val formattedNumber = when (block.listLevel % 3) {
                1 -> "${('a'.code + ((rawNum - 1) % 26)).toChar()}."
                2 -> when (rawNum % 5) {
                    1 -> "i."
                    2 -> "ii."
                    3 -> "iii."
                    4 -> "iv."
                    else -> "v."
                }
                else -> BidiUtils.formatListNumber(
                    number = rawNum,
                    isRtl = block.isRtl,
                    useUrduNumerals = false,
                    indentationLevel = block.indentationLevel
                )
            }
            Text(
                text = formattedNumber,
                fontWeight = FontWeight.SemiBold,
                fontSize = baseFontSize,
                color = if (isDarkPage) Color(0xFF60A5FA) else WordPrimary,
                modifier = Modifier.padding(end = 6.dp)
            )
        }
        ListType.NONE -> Unit
    }

    if (block.dropCapLines > 0 && block.text.isNotEmpty()) {
        Surface(
            shape = RoundedCornerShape(4.dp),
            color = WordPrimary.copy(alpha = 0.12f),
            border = androidx.compose.foundation.BorderStroke(1.dp, WordPrimary.copy(alpha = 0.35f)),
            modifier = Modifier.padding(end = 8.dp, top = 2.dp)
        ) {
            Text(
                text = block.text.take(1),
                fontSize = (baseFontSize.value * 2.2f).sp,
                fontWeight = FontWeight.Bold,
                color = WordPrimary,
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp)
            )
        }
    }
}
