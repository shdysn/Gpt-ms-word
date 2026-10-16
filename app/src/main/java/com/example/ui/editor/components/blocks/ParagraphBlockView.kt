package com.example.ui.editor.components.blocks

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isShiftPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.typography.DocumentFontManager
import com.example.data.model.HeadingStyle
import com.example.data.model.ParagraphBlock
import com.example.data.model.TextAlignment
import com.example.editor.engine.EditorEngineIntegration
import com.example.ui.theme.WordPrimary

@Composable
fun ParagraphBlockView(
    block: ParagraphBlock,
    itemNumber: Int?,
    isActive: Boolean,
    isDarkPage: Boolean = false,
    zoomScale: Float = 1f,
    canDeleteBlock: Boolean = false,
    onFocus: () -> Unit,
    onSelectionChanged: (TextRange) -> Unit,
    onTextChanged: (String, TextRange) -> Unit,
    onEnterPressed: (Int) -> Unit = {},
    onDeleteOrMergeWithPrevious: () -> Unit = {},
    onToggleChecklistItem: (String) -> Unit = {}
) {
    val align = when (block.alignment) {
        TextAlignment.CENTER -> TextAlign.Center
        TextAlignment.END -> TextAlign.End
        TextAlignment.JUSTIFY -> TextAlign.Justify
        TextAlignment.START -> TextAlign.Start
    }

    val textDirection = if (block.isRtl) TextDirection.Rtl else TextDirection.Ltr

    val effectiveFontFamily = DocumentFontManager.getFontFamily(
        fontName = block.spans.firstOrNull { it.fontFamilyName != null }?.fontFamilyName,
        isRtl = block.isRtl
    )

    val unscaledFontSize = when (block.headingStyle) {
        HeadingStyle.TITLE -> if (block.isRtl) 19f else 22f
        HeadingStyle.SUBTITLE -> if (block.isRtl) 15f else 16f
        HeadingStyle.HEADING_1 -> if (block.isRtl) 16.5f else 18f
        HeadingStyle.HEADING_2 -> if (block.isRtl) 15f else 16f
        HeadingStyle.HEADING_3 -> if (block.isRtl) 13.5f else 14.5f
        HeadingStyle.QUOTE -> if (block.isRtl) 13f else 14f
        HeadingStyle.NORMAL -> if (block.isRtl) 13.5f else 14.5f
    }
    val baseFontSize = (unscaledFontSize * zoomScale).sp

    val effectiveLineHeight = if (block.isRtl) {
        (baseFontSize.value * (block.lineSpacingMultiplier.coerceAtLeast(1.0f) * 2.3f)).sp
    } else {
        (baseFontSize.value * (block.lineSpacingMultiplier.coerceAtLeast(1.0f) * 1.35f)).sp
    }

    val defaultTextColor = when {
        block.headingStyle == HeadingStyle.TITLE || block.headingStyle == HeadingStyle.HEADING_1 ->
            if (isDarkPage) Color(0xFF60A5FA) else WordPrimary
        isDarkPage -> Color(0xFFF8FAFC)
        else -> Color(0xFF1E293B)
    }

    var textFieldValue by remember(block.id) {
        mutableStateOf(TextFieldValue(text = block.text, selection = TextRange(block.text.length)))
    }

    if (textFieldValue.text != block.text) {
        val safeStart = textFieldValue.selection.start.coerceIn(0, block.text.length)
        val safeEnd = textFieldValue.selection.end.coerceIn(safeStart, block.text.length)
        textFieldValue = textFieldValue.copy(
            text = block.text,
            selection = TextRange(safeStart, safeEnd)
        )
    }

    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(isActive) {
        if (isActive) {
            try {
                focusRequester.requestFocus()
            } catch (_: Exception) {}
        }
    }

    LaunchedEffect(block.text) {
        if (textFieldValue.text != block.text) {
            val safeStart = textFieldValue.selection.start.coerceIn(0, block.text.length)
            val safeEnd = textFieldValue.selection.end.coerceIn(safeStart, block.text.length)
            textFieldValue = textFieldValue.copy(
                text = block.text,
                selection = TextRange(safeStart, safeEnd)
            )
        }
    }

    val richTransformation = remember(block.spans, block.isRtl, zoomScale) {
        VisualTransformation { inputAnnotated ->
            val annotated = EditorEngineIntegration.buildAnnotatedText(
                inputText = inputAnnotated.text,
                spans = block.spans,
                isRtl = block.isRtl,
                zoomScale = zoomScale
            )
            TransformedText(annotated, OffsetMapping.Identity)
        }
    }

    val indentPadding = if (block.leftIndentPt > 0f) {
        (block.leftIndentPt * zoomScale).dp
    } else {
        ((block.indentationLevel + block.listLevel) * 20 * zoomScale).dp
    }
    val spaceBeforeDp = (block.spaceBeforePt * zoomScale).dp
    val spaceAfterDp = (block.spaceAfterPt * zoomScale).dp

    CompositionLocalProvider(
        LocalLayoutDirection provides (if (block.isRtl) LayoutDirection.Rtl else LayoutDirection.Ltr)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = indentPadding, top = spaceBeforeDp, bottom = spaceAfterDp)
                .clickable(onClick = onFocus),
            verticalAlignment = Alignment.Top
        ) {
            ParagraphPrefixView(
                block = block,
                itemNumber = itemNumber,
                baseFontSize = baseFontSize,
                isDarkPage = isDarkPage,
                onToggleChecklistItem = onToggleChecklistItem
            )

            Column(modifier = Modifier.weight(1f)) {
                val textIndent = if (block.firstLineIndentPt != 0f || block.hangingIndentPt != 0f) {
                    androidx.compose.ui.text.style.TextIndent(
                        firstLine = (block.firstLineIndentPt * zoomScale).sp,
                        restLine = (block.hangingIndentPt * zoomScale).sp
                    )
                } else null

                BasicTextField(
                    value = textFieldValue,
                    onValueChange = { newTfv ->
                        if (newTfv.text.contains("\n")) {
                            val newlineIndex = newTfv.text.indexOf('\n')
                            val textBefore = newTfv.text.substring(0, newlineIndex)
                            textFieldValue = TextFieldValue(
                                text = textBefore,
                                selection = TextRange(textBefore.length)
                            )
                            onEnterPressed(newlineIndex)
                        } else {
                            textFieldValue = newTfv
                            onSelectionChanged(newTfv.selection)
                            if (newTfv.text != block.text) {
                                onTextChanged(newTfv.text, newTfv.selection)
                            }
                        }
                    },
                    visualTransformation = richTransformation,
                    textStyle = TextStyle(
                        fontSize = baseFontSize,
                        fontFamily = effectiveFontFamily,
                        fontWeight = if (block.headingStyle.isBold) FontWeight.Bold else FontWeight.Normal,
                        fontStyle = if (block.headingStyle == HeadingStyle.QUOTE) FontStyle.Italic else FontStyle.Normal,
                        color = if (block.isChecked) (if (isDarkPage) Color(0xFF64748B) else Color(0xFF94A3B8)) else defaultTextColor,
                        textAlign = align,
                        textDirection = textDirection,
                        lineHeight = effectiveLineHeight,
                        textIndent = textIndent,
                        textDecoration = if (block.isChecked) TextDecoration.LineThrough else null,
                        platformStyle = androidx.compose.ui.text.PlatformTextStyle(includeFontPadding = true),
                        lineHeightStyle = androidx.compose.ui.text.style.LineHeightStyle(
                            alignment = androidx.compose.ui.text.style.LineHeightStyle.Alignment.Center,
                            trim = androidx.compose.ui.text.style.LineHeightStyle.Trim.None
                        )
                    ),
                    cursorBrush = SolidColor(if (isDarkPage) Color(0xFF60A5FA) else WordPrimary),
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester)
                        .onPreviewKeyEvent { event ->
                            if (event.type == KeyEventType.KeyDown && event.key == Key.Backspace) {
                                if (textFieldValue.selection.collapsed && textFieldValue.selection.start == 0) {
                                    onDeleteOrMergeWithPrevious()
                                    true
                                } else {
                                    false
                                }
                            } else if (event.type == KeyEventType.KeyDown && (event.key == Key.Enter || event.key == Key.NumPadEnter)) {
                                if (!event.isShiftPressed) {
                                    val cursor = textFieldValue.selection.start.coerceIn(0, block.text.length)
                                    val textBefore = block.text.substring(0, cursor)
                                    textFieldValue = TextFieldValue(text = textBefore, selection = TextRange(cursor))
                                    onEnterPressed(cursor)
                                    true
                                } else {
                                    false
                                }
                            } else {
                                false
                            }
                        }
                        .onFocusChanged { focusState ->
                            if (focusState.isFocused) {
                                onFocus()
                            }
                        }
                        .testTag("paragraph_input_${block.id}"),
                    decorationBox = { innerTextField ->
                        Box(modifier = Modifier.fillMaxWidth()) {
                            if (textFieldValue.text.isEmpty() && !canDeleteBlock) {
                                Text(
                                    text = "Start typing...",
                                    style = TextStyle(
                                        fontSize = baseFontSize,
                                        fontFamily = effectiveFontFamily,
                                        color = if (isDarkPage) Color(0xFF64748B) else Color(0xFF94A3B8),
                                        textAlign = align,
                                        textDirection = textDirection,
                                        lineHeight = effectiveLineHeight
                                    )
                                )
                            }
                            innerTextField()
                        }
                    }
                )

                ParagraphPillsView(
                    block = block,
                    currentCursor = textFieldValue.selection.start,
                    currentText = textFieldValue.text,
                    isActive = isActive,
                    isDarkPage = isDarkPage,
                    onApplyTypoCorrection = { newText, newSel ->
                        textFieldValue = textFieldValue.copy(text = newText, selection = newSel)
                        onTextChanged(newText, newSel)
                    }
                )
            }
        }
    }
}
