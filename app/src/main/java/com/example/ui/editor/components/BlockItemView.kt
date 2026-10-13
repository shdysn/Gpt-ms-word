package com.example.ui.editor.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Draw
import androidx.compose.material.icons.filled.FormatQuote
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.ListAlt
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import android.content.Intent
import android.net.Uri
import androidx.compose.ui.platform.LocalContext
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
    onEditImageProperties: () -> Unit = {},
    documentBlocks: List<BlockElement> = emptyList(),
    paginatedPages: List<com.example.core.layout.DocumentPage> = emptyList(),
    onNavigateToBlock: (Int) -> Unit = {},
    onRefreshTOC: () -> Unit = {}
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
                                useUrduNumerals = false,
                                indentationLevel = block.indentationLevel
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

                    Column(
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

                        // Contextual Hyperlink Action Pill
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
                                    Icon(
                                        imageVector = Icons.Default.Link,
                                        contentDescription = null,
                                        tint = Color(0xFF2563EB),
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Open Link: ${activeLinkSpan.linkUrl}",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = Color(0xFF2563EB)
                                    )
                                }
                            }
                        }
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

        is com.example.data.model.TableOfContentsBlock -> {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = if (isDarkPage) Color(0xFF1E293B) else Color(0xFFF8FAFC),
                border = androidx.compose.foundation.BorderStroke(1.dp, if (isDarkPage) Color(0xFF334155) else Color(0xFFE2E8F0)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .testTag("toc_block_${block.id}")
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.ListAlt,
                            contentDescription = null,
                            tint = WordPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = block.title,
                            fontWeight = FontWeight.Bold,
                            fontSize = (16f * zoomScale).sp,
                            color = if (isDarkPage) Color(0xFFF1F5F9) else Color(0xFF0F172A),
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(
                            onClick = onRefreshTOC,
                            modifier = Modifier.size(24.dp).testTag("refresh_toc_btn")
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = "Refresh Table of Contents", tint = WordPrimary, modifier = Modifier.size(16.dp))
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                        if (canDeleteBlock) {
                            IconButton(onClick = onDeleteBlock, modifier = Modifier.size(24.dp)) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete TOC", tint = Color.Gray, modifier = Modifier.size(14.dp))
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    HorizontalDivider(color = if (isDarkPage) Color(0xFF334155) else Color(0xFFE2E8F0))
                    Spacer(modifier = Modifier.height(8.dp))

                    val headings = documentBlocks.filterIsInstance<ParagraphBlock>().filter { it.headingStyle != HeadingStyle.NORMAL && it.text.isNotBlank() }
                    if (headings.isEmpty()) {
                        Text(
                            text = "No headings found in document. Set paragraphs to Heading 1, 2, or 3 to populate.",
                            fontSize = 12.sp,
                            color = Color.Gray,
                            fontStyle = FontStyle.Italic
                        )
                    } else {
                        headings.forEachIndexed { hIdx, hBlock ->
                            val indentDp = when (hBlock.headingStyle) {
                                HeadingStyle.TITLE, HeadingStyle.HEADING_1 -> 0.dp
                                HeadingStyle.HEADING_2 -> 14.dp
                                else -> 28.dp
                            }
                            val targetBlockIndex = documentBlocks.indexOf(hBlock)
                            val realPageNumber = paginatedPages.firstOrNull { page ->
                                page.items.any { it.originalBlockIndex == targetBlockIndex }
                            }?.pageNumber ?: (hIdx + 1)

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(start = indentDp, top = 4.dp, bottom = 4.dp)
                                    .clickable(enabled = targetBlockIndex >= 0) {
                                        if (targetBlockIndex >= 0) {
                                            onNavigateToBlock(targetBlockIndex)
                                        }
                                    }
                                    .testTag("toc_item_$hIdx"),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = hBlock.text.take(40),
                                    fontSize = (13f * zoomScale).sp,
                                    fontWeight = if (hBlock.headingStyle == HeadingStyle.TITLE || hBlock.headingStyle == HeadingStyle.HEADING_1) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isDarkPage) Color(0xFF93C5FD) else Color(0xFF1E40AF),
                                    modifier = Modifier.weight(1f, fill = false)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = ". . . . . . . . . . . . . . . . . . . .",
                                    color = if (isDarkPage) Color(0xFF475569) else Color(0xFFCBD5E1),
                                    fontSize = 11.sp,
                                    modifier = Modifier.weight(1f)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "$realPageNumber",
                                    fontSize = (12f * zoomScale).sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (isDarkPage) Color(0xFF94A3B8) else Color(0xFF64748B)
                                )
                            }
                        }
                    }
                }
            }
        }

        is com.example.data.model.SignatureBlock -> {
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
                        com.example.core.typography.SignatureUtils.decodeNormalizedStrokes(
                            encoded = block.svgPoints,
                            targetWidth = 240f,
                            targetHeight = 65f
                        )
                    }

                    if (decodedStrokes.isNotEmpty()) {
                        androidx.compose.foundation.Canvas(
                            modifier = Modifier
                                .width((240f * zoomScale).dp)
                                .height((65f * zoomScale).dp)
                                .padding(vertical = 2.dp)
                        ) {
                            for (stroke in decodedStrokes) {
                                if (stroke.size > 1) {
                                    val path = androidx.compose.ui.graphics.Path().apply {
                                        moveTo(stroke.first().first * zoomScale, stroke.first().second * zoomScale)
                                        for (i in 1 until stroke.size) {
                                            lineTo(stroke[i].first * zoomScale, stroke[i].second * zoomScale)
                                        }
                                    }
                                    drawPath(
                                        path = path,
                                        color = inkColor,
                                        style = androidx.compose.ui.graphics.drawscope.Stroke(
                                            width = 3.5f * zoomScale,
                                            cap = androidx.compose.ui.graphics.StrokeCap.Round,
                                            join = androidx.compose.ui.graphics.StrokeJoin.Round
                                        )
                                    )
                                }
                            }
                        }
                    } else {
                        // Signature Cursive Line fallback
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

        is com.example.data.model.ShapeBlock -> {
            val bgCol = try { Color(android.graphics.Color.parseColor(block.backgroundColorHex)) } catch (_: Exception) { Color(0xFFEFF6FF) }
            val borderCol = try { Color(android.graphics.Color.parseColor(block.borderColorHex)) } catch (_: Exception) { Color(0xFF3B82F6) }
            val shapeIcon = when (block.shapeType) {
                "WARNING" -> Icons.Default.Warning
                "SUCCESS" -> Icons.Default.CheckCircle
                "QUOTE" -> Icons.Default.FormatQuote
                else -> Icons.Default.Info
            }

            Surface(
                shape = RoundedCornerShape(8.dp),
                color = if (isDarkPage) Color(0xFF1E293B) else bgCol,
                border = androidx.compose.foundation.BorderStroke(1.5.dp, borderCol),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp)
                    .testTag("shape_block_${block.id}")
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = shapeIcon,
                        contentDescription = null,
                        tint = borderCol,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = block.title,
                            fontWeight = FontWeight.Bold,
                            fontSize = (13f * zoomScale).sp,
                            color = borderCol
                        )
                        if (block.content.isNotBlank()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = block.content,
                                fontSize = (12f * zoomScale).sp,
                                color = if (isDarkPage) Color(0xFFE2E8F0) else Color(0xFF1E293B)
                            )
                        }
                    }
                    if (canDeleteBlock) {
                        IconButton(onClick = onDeleteBlock, modifier = Modifier.size(22.dp)) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete Shape", tint = Color.Gray, modifier = Modifier.size(13.dp))
                        }
                    }
                }
            }
        }

        is com.example.data.model.FootnoteBlock -> {
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = if (isDarkPage) Color(0xFF1E293B) else Color(0xFFF8FAFC),
                border = androidx.compose.foundation.BorderStroke(1.dp, if (isDarkPage) Color(0xFF334155) else Color(0xFFE2E8F0)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .testTag("footnote_block_${block.id}")
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    HorizontalDivider(
                        thickness = 1.dp,
                        color = if (isDarkPage) Color(0xFF475569) else Color(0xFFCBD5E1),
                        modifier = Modifier.width(80.dp)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(
                            text = "[${block.noteNumber}] ",
                            fontWeight = FontWeight.Bold,
                            fontSize = (11f * zoomScale).sp,
                            color = WordPrimary
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            if (block.referenceText.isNotBlank()) {
                                Text(
                                    text = "Ref: \"${block.referenceText}\"",
                                    fontSize = (10f * zoomScale).sp,
                                    fontStyle = FontStyle.Italic,
                                    color = Color.Gray
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                            }
                            Text(
                                text = block.noteContent,
                                fontSize = (11f * zoomScale).sp,
                                color = if (isDarkPage) Color(0xFFE2E8F0) else Color(0xFF334155)
                            )
                        }
                        if (canDeleteBlock) {
                            IconButton(onClick = onDeleteBlock, modifier = Modifier.size(20.dp)) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete Footnote", tint = Color.Gray, modifier = Modifier.size(13.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}
