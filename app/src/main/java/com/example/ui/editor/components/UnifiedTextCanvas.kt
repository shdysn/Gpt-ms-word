package com.example.ui.editor.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Spellcheck
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.typography.BidiUtils
import com.example.core.typography.DocumentFontManager
import com.example.data.model.ParagraphBlock
import com.example.data.model.RichDocument
import com.example.editor.engine.EditorEngineIntegration
import com.example.ui.theme.WordPrimary

/**
 * Unified Text Canvas:
 * Provides a clean, distraction-free, paper-native continuous writing canvas without
 * separated block boundaries. Users can type fluidly with Enter creating new lines,
 * and can delete all text down to an empty clean canvas using Backspace.
 */
@Composable
fun UnifiedTextCanvas(
    document: RichDocument,
    zoomScale: Float = 1f,
    isDarkPage: Boolean = false,
    onSelectionChanged: (TextRange) -> Unit = {},
    onUpdateUnifiedText: (String, TextRange) -> Unit = { _, _ -> },
    modifier: Modifier = Modifier
) {
    val paragraphs = remember(document.blocks) {
        document.blocks.filterIsInstance<ParagraphBlock>()
    }
    val firstParagraph = paragraphs.firstOrNull() ?: ParagraphBlock()
    val fullText = remember(paragraphs) {
        paragraphs.joinToString("\n") { it.text }
    }

    var textFieldValue by remember(document.id) {
        mutableStateOf(TextFieldValue(text = fullText, selection = TextRange(fullText.length)))
    }

    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(document.id) {
        try {
            focusRequester.requestFocus()
        } catch (_: Exception) {}
    }

    LaunchedEffect(fullText) {
        if (textFieldValue.text != fullText) {
            val safeStart = textFieldValue.selection.start.coerceIn(0, fullText.length)
            val safeEnd = textFieldValue.selection.end.coerceIn(0, fullText.length)
            textFieldValue = textFieldValue.copy(
                text = fullText,
                selection = TextRange(safeStart, safeEnd)
            )
        }
    }

    val isRtl = remember(paragraphs, fullText) {
        paragraphs.any { it.isRtl } || BidiUtils.detectParagraphIsRtl(fullText)
    }
    val textDirection = if (isRtl) TextDirection.Rtl else TextDirection.Ltr

    val unscaledFontSize = if (isRtl) 13.5f else 14.5f
    val baseFontSize = (unscaledFontSize * zoomScale).sp
    val effectiveLineHeight = if (isRtl) {
        (baseFontSize.value * 2.3f).sp
    } else {
        (baseFontSize.value * 1.35f).sp
    }
    val defaultTextColor = if (isDarkPage) Color(0xFFF8FAFC) else Color(0xFF1E293B)

    val effectiveFontFamily = DocumentFontManager.getFontFamily(
        fontName = paragraphs.firstOrNull { it.spans.any { sp -> sp.fontFamilyName != null } }
            ?.spans?.firstOrNull { it.fontFamilyName != null }?.fontFamilyName,
        isRtl = isRtl
    )

    val richTransformation = remember(paragraphs, isDarkPage, zoomScale) {
        VisualTransformation { inputText ->
            val annotated = EditorEngineIntegration.buildAnnotatedTextWithParagraphBlocks(
                text = inputText.text,
                paragraphs = paragraphs,
                zoomScale = zoomScale
            )
            TransformedText(annotated, OffsetMapping.Identity)
        }
    }

    CompositionLocalProvider(
        LocalLayoutDirection provides (if (isRtl) LayoutDirection.Rtl else LayoutDirection.Ltr)
    ) {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
        ) {
            // Live spellcheck typo suggestion pill (Phase 4)
            val activeTypo = remember(textFieldValue.selection.start, textFieldValue.text) {
                val cur = textFieldValue.selection.start
                com.example.editor.engine.SpellCheckEngine.findTypos(textFieldValue.text).firstOrNull { (start, end, _) ->
                    cur in start..end
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
                        .padding(bottom = 6.dp)
                        .clickable {
                            val newText = textFieldValue.text.replaceRange(tStart, tEnd, suggestion)
                            val newSel = TextRange(tStart + suggestion.length)
                            textFieldValue = textFieldValue.copy(text = newText, selection = newSel)
                            onUpdateUnifiedText(newText, newSel)
                        }
                        .testTag("unified_spellcheck_pill")
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

            BasicTextField(
                value = textFieldValue,
                onValueChange = { newTfv ->
                    textFieldValue = newTfv
                    onSelectionChanged(newTfv.selection)
                    if (newTfv.text != fullText) {
                        onUpdateUnifiedText(newTfv.text, newTfv.selection)
                    }
                },
                visualTransformation = richTransformation,
                textStyle = TextStyle(
                    fontSize = baseFontSize,
                    fontFamily = effectiveFontFamily,
                    color = defaultTextColor,
                    textAlign = TextAlign.Unspecified,
                    textDirection = textDirection,
                    lineHeight = effectiveLineHeight,
                    platformStyle = androidx.compose.ui.text.PlatformTextStyle(includeFontPadding = true),
                    lineHeightStyle = androidx.compose.ui.text.style.LineHeightStyle(
                        alignment = androidx.compose.ui.text.style.LineHeightStyle.Alignment.Center,
                        trim = androidx.compose.ui.text.style.LineHeightStyle.Trim.None
                    )
                ),
                cursorBrush = SolidColor(if (isDarkPage) Color(0xFF60A5FA) else WordPrimary),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 350.dp)
                    .focusRequester(focusRequester)
                    .testTag("unified_text_canvas")
                    .testTag("paragraph_input_${firstParagraph.id}"),
                decorationBox = { innerTextField ->
                    Box(modifier = Modifier.fillMaxWidth()) {
                        if (textFieldValue.text.isEmpty()) {
                            Text(
                                text = "Start typing...",
                                style = TextStyle(
                                    fontSize = baseFontSize,
                                    fontFamily = effectiveFontFamily,
                                    color = if (isDarkPage) Color(0xFF64748B) else Color(0xFF94A3B8),
                                    textAlign = TextAlign.Start,
                                    textDirection = textDirection
                                )
                            )
                        }
                        innerTextField()
                    }
                }
            )
        }
    }
}
