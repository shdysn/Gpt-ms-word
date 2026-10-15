package com.example.data.service

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.text.Layout
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.StaticLayout
import android.text.TextDirectionHeuristics
import android.text.TextPaint
import android.text.style.AbsoluteSizeSpan
import android.text.style.BackgroundColorSpan
import android.text.style.ForegroundColorSpan
import android.text.style.StrikethroughSpan
import android.text.style.StyleSpan
import android.text.style.SubscriptSpan
import android.text.style.SuperscriptSpan
import android.text.style.UnderlineSpan
import com.example.core.layout.PageItem
import com.example.core.typography.BidiUtils
import com.example.core.typography.DocumentFontManager
import com.example.data.model.BlockElement
import com.example.data.model.HeadingStyle
import com.example.data.model.ListType
import com.example.data.model.ParagraphBlock
import com.example.data.model.RichDocument
import com.example.data.model.TextAlignment

/**
 * Handles Canvas drawing of individual document page elements (paragraphs, tables, images, shapes, signatures).
 */
object PdfPageItemRenderer {

    fun renderItem(
        context: Context?,
        canvas: Canvas,
        item: PageItem,
        document: RichDocument,
        cursorY: Float,
        leftMargin: Float,
        rightMargin: Float,
        pageWidth: Float,
        contentWidth: Float,
        textPaint: TextPaint,
        dividerPaint: Paint,
        headerFooterPaint: Paint
    ): Float {
        var newY = cursorY
        when (item) {
            is com.example.core.layout.ParagraphPageItem -> {
                val block = item.originalBlock
                val isRtl = block.isRtl || BidiUtils.detectParagraphIsRtl(block.text)

                val fontName = block.spans.firstOrNull { it.fontFamilyName != null }?.fontFamilyName
                val isBaseBold = block.headingStyle.isBold
                val isBaseItalic = block.headingStyle == HeadingStyle.QUOTE || block.headingStyle == HeadingStyle.SUBTITLE

                val typeface = DocumentFontManager.getTypeface(
                    context = context,
                    fontName = fontName,
                    isRtl = isRtl,
                    isBold = isBaseBold,
                    isItalic = isBaseItalic
                )

                val pSize = when (block.headingStyle) {
                    HeadingStyle.TITLE -> 22f
                    HeadingStyle.HEADING_1 -> 17f
                    HeadingStyle.HEADING_2 -> 14f
                    HeadingStyle.HEADING_3 -> 12.5f
                    HeadingStyle.SUBTITLE, HeadingStyle.QUOTE -> 11f
                    HeadingStyle.NORMAL -> 11f
                }

                textPaint.textSize = pSize
                textPaint.typeface = typeface
                textPaint.color = when (block.headingStyle) {
                    HeadingStyle.TITLE, HeadingStyle.HEADING_1 -> Color.rgb(24, 90, 189)
                    HeadingStyle.HEADING_2 -> Color.rgb(30, 41, 59)
                    HeadingStyle.HEADING_3 -> Color.rgb(51, 65, 85)
                    HeadingStyle.SUBTITLE, HeadingStyle.QUOTE -> Color.rgb(71, 85, 105)
                    HeadingStyle.NORMAL -> Color.rgb(30, 30, 30)
                }

                val prefix = when (block.listType) {
                    ListType.BULLET -> "• "
                    ListType.NUMBERED -> {
                        val listNum = computeNumberedListIndex(document.blocks, item.originalBlockIndex)
                        BidiUtils.formatListNumber(listNum, isRtl, false, indentationLevel = block.indentationLevel)
                    }
                    ListType.NONE -> ""
                }

                val displayText = if (!item.isContinuation && prefix.isNotEmpty()) {
                    prefix + item.lineSliceText
                } else {
                    item.lineSliceText
                }

                val prefixOffset = if (!item.isContinuation && prefix.isNotEmpty()) prefix.length else 0
                val spannable = SpannableStringBuilder(displayText)

                for (span in block.spans) {
                    val sliceStart = (span.start - item.startChar + prefixOffset).coerceIn(prefixOffset, displayText.length)
                    val sliceEnd = (span.end - item.startChar + prefixOffset).coerceIn(sliceStart, displayText.length)
                    if (sliceStart >= sliceEnd) continue

                    if (span.isBold && span.isItalic) {
                        spannable.setSpan(StyleSpan(Typeface.BOLD_ITALIC), sliceStart, sliceEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                    } else if (span.isBold) {
                        spannable.setSpan(StyleSpan(Typeface.BOLD), sliceStart, sliceEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                    } else if (span.isItalic) {
                        spannable.setSpan(StyleSpan(Typeface.ITALIC), sliceStart, sliceEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                    }

                    if (span.isUnderline || span.linkUrl != null) {
                        spannable.setSpan(UnderlineSpan(), sliceStart, sliceEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                    }
                    if (span.isStrikethrough) spannable.setSpan(StrikethroughSpan(), sliceStart, sliceEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                    if (span.isSuperscript) spannable.setSpan(SuperscriptSpan(), sliceStart, sliceEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                    if (span.isSubscript) spannable.setSpan(SubscriptSpan(), sliceStart, sliceEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                    span.fontSizeSp?.let { sizeSp -> spannable.setSpan(AbsoluteSizeSpan(sizeSp.toInt(), true), sliceStart, sliceEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE) }

                    span.textColorHex?.let { hex ->
                        try {
                            spannable.setSpan(ForegroundColorSpan(Color.parseColor(hex)), sliceStart, sliceEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                        } catch (_: Exception) {}
                    } ?: run {
                        if (span.linkUrl != null) spannable.setSpan(ForegroundColorSpan(Color.rgb(24, 90, 189)), sliceStart, sliceEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                    }

                    span.highlightColorHex?.let { hex ->
                        try {
                            spannable.setSpan(BackgroundColorSpan(Color.parseColor(hex)), sliceStart, sliceEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                        } catch (_: Exception) {}
                    }
                }

                val textDirection = if (isRtl) TextDirectionHeuristics.RTL else TextDirectionHeuristics.FIRSTSTRONG_LTR
                val layoutAlignment = when (block.alignment) {
                    TextAlignment.CENTER -> Layout.Alignment.ALIGN_CENTER
                    TextAlignment.END -> if (isRtl) Layout.Alignment.ALIGN_NORMAL else Layout.Alignment.ALIGN_OPPOSITE
                    else -> Layout.Alignment.ALIGN_NORMAL
                }

                val indentPt = if (block.leftIndentPt > 0f) block.leftIndentPt else (block.indentationLevel * 18f)
                val effectiveWidth = (contentWidth - indentPt).toInt().coerceAtLeast(100)

                val staticLayout = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                    StaticLayout.Builder.obtain(spannable, 0, spannable.length, textPaint, effectiveWidth)
                        .setTextDirection(textDirection)
                        .setAlignment(layoutAlignment)
                        .setLineSpacing(2f, block.lineSpacingMultiplier)
                        .build()
                } else {
                    @Suppress("DEPRECATION")
                    StaticLayout(spannable, textPaint, effectiveWidth, layoutAlignment, block.lineSpacingMultiplier, 2f, false)
                }

                newY += block.spaceBeforePt
                canvas.save()
                val drawX = if (isRtl) leftMargin else (leftMargin + indentPt)
                canvas.translate(drawX, newY)
                staticLayout.draw(canvas)
                canvas.restore()

                val afterSpacing = if (block.spaceAfterPt > 0f) block.spaceAfterPt else 6f
                newY += staticLayout.height + afterSpacing
            }

            is com.example.core.layout.DividerPageItem -> {
                newY += 4f
                canvas.drawLine(leftMargin, newY, pageWidth - rightMargin, newY, dividerPaint)
                newY += 12f
            }

            is com.example.core.layout.TablePageItem -> {
                newY = PdfTableAndImageRenderer.renderTableItem(context, canvas, item, newY, leftMargin, rightMargin, pageWidth, contentWidth, textPaint)
            }

            is com.example.core.layout.ImagePageItem -> {
                newY = PdfTableAndImageRenderer.renderImageItem(context, canvas, item, newY, leftMargin, rightMargin, pageWidth, contentWidth, dividerPaint, headerFooterPaint)
            }

            is com.example.core.layout.TableOfContentsPageItem -> {
                newY = PdfSpecialItemRenderer.renderTocItem(canvas, item, document, newY, leftMargin, rightMargin, pageWidth)
            }

            is com.example.core.layout.SignaturePageItem -> {
                newY = PdfSpecialItemRenderer.renderSignatureItem(canvas, item, newY, leftMargin, rightMargin, pageWidth, contentWidth)
            }

            is com.example.core.layout.DrawingPageItem -> {
                newY = PdfSpecialItemRenderer.renderDrawingItem(canvas, item, newY, leftMargin, contentWidth)
            }

            is com.example.core.layout.TextBoxPageItem -> {
                newY = PdfSpecialItemRenderer.renderTextBoxItem(canvas, item, newY, leftMargin, contentWidth)
            }

            is com.example.core.layout.ShapePageItem -> {
                newY = PdfSpecialItemRenderer.renderShapeItem(canvas, item, newY, leftMargin, contentWidth)
            }

            is com.example.core.layout.FootnotePageItem -> {
                val fn = item.originalBlock
                val fnDividerPaint = Paint().apply { color = Color.rgb(203, 213, 225); strokeWidth = 1f }
                canvas.drawLine(leftMargin, newY + 4f, leftMargin + 60f, newY + 4f, fnDividerPaint)
                newY += 10f
                val fnPaint = Paint().apply { isAntiAlias = true; color = Color.rgb(71, 85, 105); textSize = 8.5f }
                val noteLabel = "[${fn.noteNumber}] ${fn.referenceText.ifBlank { "" }}: ${fn.noteContent}"
                canvas.drawText(noteLabel, leftMargin, newY + 8f, fnPaint)
                newY += 18f
            }

            is com.example.core.layout.PageBreakPageItem -> Unit
            is com.example.core.layout.ColumnBreakPageItem -> newY += item.heightPt
            is com.example.core.layout.SectionBreakPageItem -> newY += item.heightPt
        }
        return newY
    }

    private fun computeNumberedListIndex(blocks: List<BlockElement>, targetIndex: Int): Int {
        var count = 1
        for (i in (targetIndex - 1) downTo 0) {
            val prev = blocks.getOrNull(i) as? ParagraphBlock
            if (prev != null && prev.listType == ListType.NUMBERED) count++ else break
        }
        return count
    }
}
