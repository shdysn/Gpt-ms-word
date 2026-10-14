package com.example.ui.editor.components.dialogs

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.layout.MarginPreset
import com.example.core.layout.PageBorderStyle
import com.example.core.layout.PageOrientation
import com.example.core.layout.PageSize
import com.example.ui.theme.WordPrimary

@Composable
fun PageLayoutPreviewCard(
    selectedPageSize: PageSize,
    selectedOrientation: PageOrientation,
    selectedMargins: MarginPreset,
    pageBackgroundHex: String,
    borderStyle: PageBorderStyle,
    borderColorHex: String,
    topMarginMm: Float,
    bottomMarginMm: Float,
    leftMarginMm: Float,
    rightMarginMm: Float,
    showHeader: Boolean,
    showFooter: Boolean
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val isPortrait = selectedOrientation == PageOrientation.PORTRAIT
            val paperWidth = if (isPortrait) 68.dp else 98.dp
            val paperHeight = if (isPortrait) 96.dp else 68.dp
            val bgColor = try { Color(android.graphics.Color.parseColor(pageBackgroundHex)) } catch (e: Exception) { Color.White }
            val bdrColor = try { Color(android.graphics.Color.parseColor(borderColorHex)) } catch (e: Exception) { Color(0xFFCBD5E1) }

            // Miniature Sheet
            Surface(
                shape = RoundedCornerShape(4.dp),
                color = bgColor,
                shadowElevation = 3.dp,
                border = BorderStroke(
                    width = if (borderStyle != PageBorderStyle.NONE) 1.5.dp else 0.5.dp,
                    color = if (borderStyle != PageBorderStyle.NONE) bdrColor else MaterialTheme.colorScheme.outlineVariant
                ),
                modifier = Modifier.size(paperWidth, paperHeight)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(
                            top = (topMarginMm / 6f).dp.coerceIn(4.dp, 16.dp),
                            bottom = (bottomMarginMm / 6f).dp.coerceIn(4.dp, 16.dp),
                            start = (leftMarginMm / 6f).dp.coerceIn(4.dp, 16.dp),
                            end = (rightMarginMm / 6f).dp.coerceIn(4.dp, 16.dp)
                        )
                        .border(0.5.dp, WordPrimary.copy(alpha = 0.35f), RoundedCornerShape(2.dp))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(2.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        if (showHeader) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(0.7f)
                                    .height(2.dp)
                                    .background(WordPrimary.copy(alpha = 0.5f))
                            )
                        } else {
                            Spacer(modifier = Modifier.height(2.dp))
                        }
                        // Text mock line
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.9f)
                                .height(1.5.dp)
                                .background(Color.Gray.copy(alpha = 0.3f))
                        )
                        if (showFooter) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(0.5f)
                                    .height(2.dp)
                                    .background(WordPrimary.copy(alpha = 0.5f))
                                    .align(Alignment.CenterHorizontally)
                            )
                        } else {
                            Spacer(modifier = Modifier.height(2.dp))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "${selectedPageSize.displayName} • ${selectedMargins.displayName.substringBefore(" ")} Margins",
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
