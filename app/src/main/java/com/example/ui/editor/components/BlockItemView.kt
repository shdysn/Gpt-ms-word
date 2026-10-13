package com.example.ui.editor.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.typography.BidiUtils
import com.example.core.typography.DocumentFontManager
import com.example.data.model.BlockElement
import com.example.data.model.DividerBlock
import com.example.data.model.HeadingStyle
import com.example.data.model.ImageBlock
import com.example.data.model.ListType
import com.example.data.model.PageBreakBlock
import com.example.data.model.ParagraphBlock
import com.example.data.model.TableBlock
import com.example.data.model.TextAlignment
import com.example.editor.engine.EditorEngineIntegration
import com.example.ui.theme.WordPrimary

@Composable
fun BlockItemView(
    block: BlockElement,
    itemNumber: Int?,
    isActive: Boolean,
    isDarkPage: Boolean = false,
    zoomScale: Float = 1f,
    canDeleteBlock: Boolean = false,
    onFocus: () -> Unit,
    onSelectionChanged: (TextRange) -> Unit,
    onTextChanged: (String, TextRange) -> Unit,
    onEnterPressed: (Int) -> Unit = {},
    onDeleteBlock: () -> Unit = {},
    onDeleteOrMergeWithPrevious: () -> Unit = {},
    onEditTableProperties: () -> Unit = {},
    onEditTableCell: (Int, Int) -> Unit = { _, _ -> },
    onAddTableRow: () -> Unit = {},
    onDeleteTableRow: () -> Unit = {},
    onAddTableCol: () -> Unit = {},
    onDeleteTableCol: () -> Unit = {},
    onEditImageProperties: () -> Unit = {}
) {
    when (block) {
        is ParagraphBlock -> {
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

            // Stateful TextFieldValue to accurately capture selection, cursor, and text deltas
            var textFieldValue by remember(block.id) {
                mutableStateOf(TextFieldValue(text = block.text, selection = TextRange(block.text.length)))
            }

            // Keep textFieldValue in sync synchronously if block.text changed externally or on block split
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

            // Sync with external updates
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

            val indentPadding = if (block.leftIndentPt > 0f) (block.leftIndentPt * zoomScale).dp else (block.indentationLevel * 20 * zoomScale).dp
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
                    // List prefix
                    when (block.listType) {
                        ListType.BULLET -> {
                            Text(
                                text = "• ",
                                fontWeight = FontWeight.Bold,
                                fontSize = baseFontSize,
                                color = if (isDarkPage) Color(0xFF60A5FA) else WordPrimary,
                                modifier = Modifier.padding(end = 6.dp)
                            )
                        }
                        ListType.NUMBERED -> {
                            val formattedNumber = BidiUtils.formatListNumber(
                                number = itemNumber ?: 1,
                                isRtl = block.isRtl,
                                useUrduNumerals = false
                            )
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

                    Box(
                        modifier = Modifier.weight(1f)
                    ) {
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
                                color = defaultTextColor,
                                textAlign = align,
                                textDirection = textDirection,
                                lineHeight = baseFontSize * (if (block.isRtl) block.lineSpacingMultiplier.coerceAtLeast(1.4f) else block.lineSpacingMultiplier)
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
                    }
                }
            }
        }

        is TableBlock -> {
            TableBlockView(
                table = block,
                isDarkPage = isDarkPage,
                zoomScale = zoomScale,
                onEditProperties = onEditTableProperties,
                onEditCell = onEditTableCell,
                onAddRow = onAddTableRow,
                onDeleteRow = onDeleteTableRow,
                onAddCol = onAddTableCol,
                onDeleteCol = onDeleteTableCol
            )
        }

        is ImageBlock -> {
            ImageBlockView(
                imageBlock = block,
                zoomScale = zoomScale,
                onFocus = onFocus,
                onEditProperties = onEditImageProperties
            )
        }

        is DividerBlock -> {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                HorizontalDivider(
                    color = if (isDarkPage) Color(0xFF475569) else Color(0xFFCBD5E1),
                    thickness = 1.dp,
                    modifier = Modifier.weight(1f)
                )
                if (canDeleteBlock) {
                    IconButton(
                        onClick = onDeleteBlock,
                        modifier = Modifier.size(24.dp).testTag("delete_divider_${block.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete Horizontal Line",
                            tint = if (isDarkPage) Color(0xFF94A3B8) else Color.Gray,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
        }

        is PageBreakBlock -> {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                HorizontalDivider(modifier = Modifier.weight(1f), color = WordPrimary.copy(alpha = 0.4f))
                Text(
                    text = " PAGE BREAK ",
                    fontSize = 10.sp,
                    color = WordPrimary,
                    fontWeight = FontWeight.Bold
                )
                HorizontalDivider(modifier = Modifier.weight(1f), color = WordPrimary.copy(alpha = 0.4f))
                if (canDeleteBlock) {
                    IconButton(
                        onClick = onDeleteBlock,
                        modifier = Modifier.size(24.dp).testTag("delete_page_break_${block.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Remove Page Break",
                            tint = WordPrimary,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
        }
    }
}
