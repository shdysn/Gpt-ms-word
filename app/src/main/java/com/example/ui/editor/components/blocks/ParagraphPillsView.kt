package com.example.ui.editor.components.blocks

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Spellcheck
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ParagraphBlock
import com.example.editor.engine.EditorEngineIntegration

@Composable
fun ParagraphPillsView(
    block: ParagraphBlock,
    currentCursor: Int,
    currentText: String,
    isActive: Boolean,
    isDarkPage: Boolean,
    onApplyTypoCorrection: (String, TextRange) -> Unit
) {
    if (!isActive) return

    val activeLinkSpan = remember(currentCursor, block.spans) {
        block.spans.firstOrNull { span ->
            span.linkUrl != null && currentCursor in span.start..span.end
        }
    }
    if (activeLinkSpan?.linkUrl != null) {
        val context = LocalContext.current
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = if (isDarkPage) Color(0xFF1E293B) else Color(0xFFEFF6FF),
            border = androidx.compose.foundation.BorderStroke(1.dp, if (isDarkPage) Color(0xFF3B82F6) else Color(0xFFBFDBFE)),
            shadowElevation = 2.dp,
            modifier = Modifier
                .padding(top = 4.dp, bottom = 4.dp)
                .clickable {
                    try {
                        val rawUrl = activeLinkSpan.linkUrl!!
                        val validUrl = if (rawUrl.startsWith("http://") || rawUrl.startsWith("https://") || rawUrl.startsWith("mailto:")) rawUrl else "https://$rawUrl"
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(validUrl))
                        context.startActivity(intent)
                    } catch (_: Exception) {}
                }
                .testTag("hyperlink_open_pill")
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Icon(imageVector = Icons.Default.Link, contentDescription = null, tint = Color(0xFF2563EB), modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = "Open Link: ${activeLinkSpan.linkUrl}", fontSize = 11.sp, fontWeight = FontWeight.Medium, color = Color(0xFF2563EB))
            }
        }
    }

    val activeTypo = remember(currentCursor, currentText) {
        EditorEngineIntegration.findTypos(currentText).firstOrNull { (start, end, _) ->
            currentCursor in start..end
        }
    }
    if (activeTypo != null) {
        val (tStart, tEnd, suggestion) = activeTypo
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = if (isDarkPage) Color(0xFF3B1E1E) else Color(0xFFFEF2F2),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFEF4444)),
            shadowElevation = 2.dp,
            modifier = Modifier
                .padding(top = 4.dp, bottom = 4.dp)
                .clickable {
                    val newText = currentText.replaceRange(tStart, tEnd, suggestion)
                    val newSel = TextRange(tStart + suggestion.length)
                    onApplyTypoCorrection(newText, newSel)
                }
                .testTag("spellcheck_suggestion_pill")
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Icon(imageVector = Icons.Default.Spellcheck, contentDescription = null, tint = Color(0xFFDC2626), modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = "Typo: Change to \"$suggestion\"", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFFDC2626))
            }
        }
    }
}
