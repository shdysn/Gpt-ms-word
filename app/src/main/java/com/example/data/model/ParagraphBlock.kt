package com.example.data.model

import java.util.UUID

/**
 * Inline style span applied to sub-ranges of text in a paragraph.
 */
data class InlineSpan(
    val start: Int,
    val end: Int,
    val isBold: Boolean = false,
    val isItalic: Boolean = false,
    val isUnderline: Boolean = false,
    val isStrikethrough: Boolean = false,
    val isSuperscript: Boolean = false,
    val isSubscript: Boolean = false,
    val fontSizeSp: Float? = null,
    val textColorHex: String? = null,
    val highlightColorHex: String? = null,
    val fontFamilyName: String? = null,
    val linkUrl: String? = null,
    val letterSpacingSp: Float? = null,
    val underlineStyle: UnderlineStyle = if (isUnderline) UnderlineStyle.SINGLE else UnderlineStyle.NONE,
    val underlineColorHex: String? = null,
    val strikethroughStyle: StrikethroughStyle = if (isStrikethrough) StrikethroughStyle.SINGLE else StrikethroughStyle.NONE,
    val shadowDx: Float? = null,
    val shadowDy: Float? = null,
    val shadowRadius: Float? = null,
    val shadowColorHex: String? = null,
    val isOutline: Boolean = false
)

/**
 * Paragraph element containing text, inline spans, and paragraph-level formatting.
 */
data class ParagraphBlock(
    override val id: String = UUID.randomUUID().toString(),
    val text: String = "",
    val spans: List<InlineSpan> = emptyList(),
    val alignment: TextAlignment = TextAlignment.START,
    val headingStyle: HeadingStyle = HeadingStyle.NORMAL,
    val listType: ListType = ListType.NONE,
    val indentationLevel: Int = 0,
    val isRtl: Boolean = false,
    val lineSpacingMultiplier: Float = 1.25f,
    val spaceBeforePt: Float = 0f,
    val spaceAfterPt: Float = 0f,
    val leftIndentPt: Float = 0f,
    val firstLineIndentPt: Float = 0f,
    val hangingIndentPt: Float = 0f,
    val dropCapLines: Int = 0,
    val listLevel: Int = 0,
    val isChecklist: Boolean = false,
    val isChecked: Boolean = false,
    val tabStops: List<TabStop> = emptyList()
) : BlockElement
