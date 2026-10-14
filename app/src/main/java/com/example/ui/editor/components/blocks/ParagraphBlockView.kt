package com.example.ui.editor.components.blocks

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Spellcheck
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isShiftPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalContext
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
import com.example.core.typography.BidiUtils
import com.example.core.typography.DocumentFontManager
import com.example.data.model.HeadingStyle
import com.example.data.model.ListType
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
        HeadingStyle.TITLE -> 26f
        HeadingStyle.SUBTITLE -> 18f
        HeadingStyle.HEADING_1 -> 22f
        HeadingStyle.HEADING_2 -> 18f
        HeadingStyle.HEADING_3 -> 16f
        HeadingStyle.QUOTE -> 15f
        HeadingStyle.NORMAL -> 15f
    }
    val baseFontSize = (unscaledFontSize * zoomScale).sp

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
                        lineHeight = baseFontSize * (if (block.isRtl) block.lineSpacingMultiplier.coerceAtLeast(1.4f) else block.lineSpacingMultiplier),
                        textIndent = textIndent,
                        textDecoration = if (block.isChecked) TextDecoration.LineThrough else null
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
                                        textDirection = textDirection
                                    )
                                )
                            }
                            innerTextField()
                        }
                    }
                )

                val currentCursor = textFieldValue.selection.start
                val activeLinkSpan = remember(currentCursor, block.spans) {
                    block.spans.firstOrNull { span ->
                        span.linkUrl != null && currentCursor in span.start..span.end
                    }
                }
                if (activeLinkSpan?.linkUrl != null && isActive) {
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

                val activeTypo = remember(currentCursor, textFieldValue.text) {
                    EditorEngineIntegration.findTypos(textFieldValue.text).firstOrNull { (start, end, _) ->
                        currentCursor in start..end
                    }
                }
                if (activeTypo != null && isActive) {
                    val (tStart, tEnd, suggestion) = activeTypo
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = if (isDarkPage) Color(0xFF3B1E1E) else Color(0xFFFEF2F2),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFEF4444)),
                        shadowElevation = 2.dp,
                        modifier = Modifier
                            .padding(top = 4.dp, bottom = 4.dp)
                            .clickable {
                                val newText = textFieldValue.text.replaceRange(tStart, tEnd, suggestion)
                                val newSel = TextRange(tStart + suggestion.length)
                                textFieldValue = textFieldValue.copy(text = newText, selection = newSel)
                                onTextChanged(newText, newSel)
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
        }
    }
}
