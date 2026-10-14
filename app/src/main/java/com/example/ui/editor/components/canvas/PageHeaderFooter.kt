package com.example.ui.editor.components.canvas

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.layout.DocumentPage
import com.example.core.layout.PageLayoutConfig
import com.example.data.model.TextAlignment

@Composable
fun PageWatermark(
    layout: PageLayoutConfig,
    zoomScale: Float,
    modifier: Modifier = Modifier
) {
    if (layout.watermarkText.isNotBlank()) {
        val wmColor = try {
            Color(android.graphics.Color.parseColor(layout.watermarkColorHex))
        } catch (_: Exception) { Color(0x2594A3B8) }
        Text(
            text = layout.watermarkText,
            fontSize = (46f * zoomScale).sp,
            fontWeight = FontWeight.Bold,
            color = wmColor,
            modifier = modifier.graphicsLayer(rotationZ = layout.watermarkAngleDeg)
        )
    }
}

@Composable
fun PageHeaderView(
    page: DocumentPage,
    isDarkPage: Boolean
) {
    if (page.hasHeader && page.formattedHeader.isNotBlank()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = when (page.layout.headerAlignment) {
                TextAlignment.CENTER -> Arrangement.Center
                TextAlignment.END -> Arrangement.End
                else -> Arrangement.Start
            }
        ) {
            Text(
                text = page.formattedHeader,
                style = MaterialTheme.typography.labelSmall,
                color = if (isDarkPage) Color(0xFFCBD5E1) else Color.Gray
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        HorizontalDivider(
            color = if (isDarkPage) Color(0xFF334155) else Color(0xFFE5E7EB),
            thickness = 0.5.dp
        )
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
fun PageFooterView(
    page: DocumentPage,
    isDarkPage: Boolean
) {
    if (page.hasFooter && page.formattedFooter.isNotBlank()) {
        Spacer(modifier = Modifier.height(20.dp))
        HorizontalDivider(
            color = if (isDarkPage) Color(0xFF334155) else Color(0xFFE5E7EB),
            thickness = 0.5.dp
        )
        Spacer(modifier = Modifier.height(6.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = when (page.layout.footerAlignment) {
                TextAlignment.START -> Arrangement.Start
                TextAlignment.END -> Arrangement.End
                else -> Arrangement.Center
            }
        ) {
            Text(
                text = page.formattedFooter,
                style = MaterialTheme.typography.labelSmall,
                color = if (isDarkPage) Color(0xFFCBD5E1) else Color.Gray
            )
        }
    }
}
