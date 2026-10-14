package com.example.ui.editor.components.blocks

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Draw
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.typography.SignatureUtils
import com.example.data.model.SignatureBlock
import com.example.ui.theme.WordPrimary

@Composable
fun SignatureBlockItem(
    block: SignatureBlock,
    isDarkPage: Boolean,
    zoomScale: Float,
    canDeleteBlock: Boolean,
    onDeleteBlock: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = if (isDarkPage) Color(0xFF1E293B) else Color.White,
        border = androidx.compose.foundation.BorderStroke(1.dp, if (isDarkPage) Color(0xFF334155) else Color(0xFFE2E8F0)),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .testTag("signature_block_${block.id}")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Draw,
                    contentDescription = null,
                    tint = WordPrimary,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Verified Digital Signature",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = WordPrimary,
                    modifier = Modifier.weight(1f)
                )
                if (canDeleteBlock) {
                    IconButton(onClick = onDeleteBlock, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete Signature", tint = Color.Gray, modifier = Modifier.size(14.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            val inkColor = try {
                Color(android.graphics.Color.parseColor(block.strokeColorHex))
            } catch (_: Exception) {
                Color(0xFF0F172A)
            }

            val decodedStrokes = remember(block.svgPoints) {
                SignatureUtils.decodeNormalizedStrokes(
                    encoded = block.svgPoints,
                    targetWidth = 240f,
                    targetHeight = 65f
                )
            }

            if (decodedStrokes.isNotEmpty()) {
                Canvas(
                    modifier = Modifier
                        .width((240f * zoomScale).dp)
                        .height((65f * zoomScale).dp)
                        .padding(vertical = 2.dp)
                ) {
                    for (stroke in decodedStrokes) {
                        if (stroke.size > 1) {
                            val path = Path().apply {
                                moveTo(stroke.first().first * zoomScale, stroke.first().second * zoomScale)
                                for (i in 1 until stroke.size) {
                                    lineTo(stroke[i].first * zoomScale, stroke[i].second * zoomScale)
                                }
                            }
                            drawPath(
                                path = path,
                                color = inkColor,
                                style = Stroke(
                                    width = 3.5f * zoomScale,
                                    cap = StrokeCap.Round,
                                    join = StrokeJoin.Round
                                )
                            )
                        }
                    }
                }
            } else {
                Text(
                    text = block.signerName.ifBlank { "Authorized Signature" },
                    fontSize = (20f * zoomScale).sp,
                    fontStyle = FontStyle.Italic,
                    fontWeight = FontWeight.Medium,
                    color = inkColor
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            HorizontalDivider(
                thickness = 1.5.dp,
                color = inkColor,
                modifier = Modifier.width(220.dp)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = block.signerTitle,
                fontSize = (11f * zoomScale).sp,
                color = if (isDarkPage) Color(0xFFCBD5E1) else Color(0xFF475569)
            )
            if (block.signedDate.isNotBlank()) {
                Text(
                    text = "Signed: ${block.signedDate}",
                    fontSize = (10f * zoomScale).sp,
                    color = Color.Gray
                )
            }
        }
    }
}
