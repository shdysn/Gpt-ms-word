package com.example.ui.editor.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.TextBoxBlock

/**
 * Floating / Draggable Text Box component.
 */
@Composable
fun TextBoxBlockView(
    block: TextBoxBlock,
    zoomScale: Float = 1f,
    onTextChanged: (String) -> Unit,
    onDeleteBlock: () -> Unit
) {
    var textValue by remember(block.text) { mutableStateOf(block.text) }

    val bgCol = try { Color(android.graphics.Color.parseColor(block.backgroundColorHex)) } catch (_: Exception) { Color.White }
    val borderCol = try { Color(android.graphics.Color.parseColor(block.borderColorHex)) } catch (_: Exception) { Color(0xFF2563EB) }
    val textCol = try { Color(android.graphics.Color.parseColor(block.textColorHex)) } catch (_: Exception) { Color(0xFF1E293B) }

    val widthDp = (block.widthDp * zoomScale).dp
    val minHeightDp = (block.heightDp * zoomScale).dp

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            shape = RoundedCornerShape(4.dp),
            color = bgCol,
            border = BorderStroke((block.borderWidthPt * zoomScale).dp, borderCol),
            modifier = Modifier
                .width(widthDp)
                .height(minHeightDp)
                .testTag("text_box_block_${block.id}")
        ) {
            Column(modifier = Modifier.padding(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.TextFields,
                        contentDescription = null,
                        tint = borderCol,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Text Box",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = borderCol,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = onDeleteBlock, modifier = Modifier.size(20.dp)) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Delete Text Box",
                            tint = Color.Gray,
                            modifier = Modifier.size(12.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                BasicTextField(
                    value = textValue,
                    onValueChange = {
                        textValue = it
                        onTextChanged(it)
                    },
                    textStyle = TextStyle(
                        fontSize = (12f * zoomScale).sp,
                        color = textCol,
                        lineHeight = (16f * zoomScale).sp
                    ),
                    cursorBrush = SolidColor(borderCol),
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .testTag("text_box_input_${block.id}")
                )
            }
        }
    }
}
