package com.example.ui.editor.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
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

    val baseFontSize = (15f * zoomScale).sp
    val defaultTextColor = if (isDarkPage) Color(0xFFF8FAFC) else Color(0xFF1E293B)

    val isRtl = remember(paragraphs, fullText) {
        paragraphs.any { it.isRtl } || BidiUtils.detectParagraphIsRtl(fullText)
    }
    val textDirection = if (isRtl) TextDirection.Rtl else TextDirection.Ltr

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
        Box(
            modifier = modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
        ) {
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
                    lineHeight = baseFontSize * (if (isRtl) 1.4f else 1.25f)
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
