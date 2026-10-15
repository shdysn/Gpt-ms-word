package com.example.data.repository

import com.example.core.layout.MarginPreset
import com.example.core.layout.PageLayoutConfig
import com.example.core.layout.PageSize
import com.example.data.model.DividerBlock
import com.example.data.model.HeadingStyle
import com.example.data.model.InlineSpan
import com.example.data.model.ListType
import com.example.data.model.ParagraphBlock
import com.example.data.model.RichDocument
import com.example.data.model.TextAlignment

/**
 * Urdu multilingual starter template demonstrating RTL typography, mixed English, and Nastaliq typesetting.
 */
object DocumentUrduTemplate {

    fun createUrduDocumentTemplate(id: String, title: String, timestamp: Long): RichDocument {
        val blocks = listOf(
            ParagraphBlock(
                text = "اردو ادب اور جدید ورڈ پروسیسنگ (Smart Word Processor)",
                alignment = TextAlignment.CENTER,
                headingStyle = HeadingStyle.TITLE,
                isRtl = true,
                spans = listOf(
                    InlineSpan(start = 0, end = 31, isBold = true, textColorHex = "#185ABD"),
                    InlineSpan(start = 32, end = 53, isBold = true, isUnderline = true, textColorHex = "#0F4C81")
                )
            ),
            ParagraphBlock(
                text = "تحریر: ڈاکٹر محمد علیم | شعبہ اردو زبان و کمپیوٹر سائنس",
                alignment = TextAlignment.CENTER,
                headingStyle = HeadingStyle.SUBTITLE,
                isRtl = true
            ),
            DividerBlock(),
            ParagraphBlock(
                text = "1. مکمل اردو پیراگراف (Complete Urdu Paragraph)",
                headingStyle = HeadingStyle.HEADING_1,
                isRtl = true,
                alignment = TextAlignment.START
            ),
            ParagraphBlock(
                text = "اردو زبان برصغیر پاک و ہند کی ایک عظیم اور شیریں زبان ہے۔ اس میں شعری لطافت اور نثری وسعت بے مثال ہے۔ سمارٹ ورڈ پروسیسر میں نستعلیق خطاطی اور مکمل اعراب (زبر، زیر، پیش) کی حفاظت کی گئی ہے۔",
                alignment = TextAlignment.JUSTIFY,
                isRtl = true,
                lineSpacingMultiplier = 1.4f
            ),
            ParagraphBlock(
                text = "2. Complete English Paragraph",
                headingStyle = HeadingStyle.HEADING_1,
                isRtl = false,
                alignment = TextAlignment.START
            ),
            ParagraphBlock(
                text = "Smart Word Processor is engineered to deliver enterprise-grade typography and seamless multilingual document interchange. It guarantees full compliance with the Unicode Bidirectional Algorithm without destructive string reversals.",
                alignment = TextAlignment.JUSTIFY,
                isRtl = false,
                lineSpacingMultiplier = 1.25f
            ),
            ParagraphBlock(
                text = "3. اردو اور انگریزی کا باہمی ملاپ (Mixed Urdu and English)",
                headingStyle = HeadingStyle.HEADING_1,
                isRtl = true,
                alignment = TextAlignment.START
            ),
            ParagraphBlock(
                text = "جدید دور میں Artificial Intelligence اور Machine Learning نے زبانوں کی تحقیق کو انتہائی آسان بنا دیا ہے۔ ہم Smart Word Processor کا استعمال کر کے دونوں زبانوں میں ہم آہنگی پیدا کر سکتے ہیں۔",
                alignment = TextAlignment.JUSTIFY,
                isRtl = true,
                lineSpacingMultiplier = 1.4f
            ),
            ParagraphBlock(
                text = "4. اردو تحریر میں اعداد (Numbers within Urdu Text)",
                headingStyle = HeadingStyle.HEADING_1,
                isRtl = true,
                alignment = TextAlignment.START
            ),
            ParagraphBlock(
                text = "پاکستان 14 اگست 1947 کو وجود میں آیا۔ آج 2026 میں ہم ڈیجیٹل دور میں ترقی کر رہے ہیں جبکہ روایتی اعداد جیسے ۱، ۲، ۳ اور ۴ بھی کامیابی سے لکھے جا سکتے ہیں۔",
                alignment = TextAlignment.JUSTIFY,
                isRtl = true,
                lineSpacingMultiplier = 1.4f
            ),
            ParagraphBlock(
                text = "5. ای میل پتے اور ویب لنکس (Email Addresses and URLs)",
                headingStyle = HeadingStyle.HEADING_1,
                isRtl = true,
                alignment = TextAlignment.START
            ),
            ParagraphBlock(
                text = "کسی بھی مسئلے یا رائے کے لیے براہ کرم info@smartword.org پر ای میل بھیجیں یا ہماری ویب سائٹ https://smartword.org ملاحظہ فرمائیں۔",
                alignment = TextAlignment.JUSTIFY,
                isRtl = true,
                lineSpacingMultiplier = 1.4f,
                spans = listOf(
                    InlineSpan(start = 38, end = 57, isUnderline = true, textColorHex = "#185ABD", linkUrl = "mailto:info@smartword.org"),
                    InlineSpan(start = 82, end = 103, isUnderline = true, textColorHex = "#185ABD", linkUrl = "https://smartword.org")
                )
            ),
            ParagraphBlock(
                text = "6. اردو بلٹ لسٹ اور فہرست (Urdu Bullet and Numbered Lists)",
                headingStyle = HeadingStyle.HEADING_1,
                isRtl = true,
                alignment = TextAlignment.START
            ),
            ParagraphBlock(
                text = "دائیں سے بائیں خطاطی اور کرسر کی درست سمت بندی",
                alignment = TextAlignment.START,
                isRtl = true,
                listType = ListType.BULLET,
                lineSpacingMultiplier = 1.35f
            ),
            ParagraphBlock(
                text = "بنڈل شدہ جمیل نوری و نوٹو نستعلیق فونٹ کے ذریعے مکمل ڈسپلے",
                alignment = TextAlignment.START,
                isRtl = true,
                listType = ListType.BULLET,
                lineSpacingMultiplier = 1.35f
            ),
            ParagraphBlock(
                text = "پی ڈی ایف پرنٹنگ اور اے فور صفحات پر خط کی خوبصورتی",
                alignment = TextAlignment.START,
                isRtl = true,
                listType = ListType.BULLET,
                lineSpacingMultiplier = 1.35f
            ),
            ParagraphBlock(
                text = "7. جلی اور خط کشیدہ اردو تحریر (Bold and Underlined Urdu)",
                headingStyle = HeadingStyle.HEADING_1,
                isRtl = true,
                alignment = TextAlignment.START
            ),
            ParagraphBlock(
                text = "یہ عبارت خاص طور پر جلی حروف اور خط کشیدہ انداز میں پیش کی گئی ہے تاکہ واضح رہے۔",
                alignment = TextAlignment.START,
                isRtl = true,
                lineSpacingMultiplier = 1.4f,
                spans = listOf(
                    InlineSpan(start = 22, end = 31, isBold = true, textColorHex = "#B91C1C"),
                    InlineSpan(start = 36, end = 49, isUnderline = true, isBold = true, textColorHex = "#1D4ED8")
                )
            ),
            DividerBlock(),
            ParagraphBlock(
                text = "تمام حقوق برائے سمارٹ ورڈ پروسیسر محفوظ ہیں • نسخہ 1.0",
                alignment = TextAlignment.CENTER,
                headingStyle = HeadingStyle.QUOTE,
                isRtl = true
            )
        )

        return RichDocument(
            id = id,
            title = title.ifBlank { "اردو اور انگریزی دستاویز (Multilingual Document)" },
            author = "ڈاکٹر محمد علیم",
            createdAt = timestamp,
            updatedAt = timestamp,
            layoutConfig = PageLayoutConfig(
                pageSize = PageSize.A4,
                marginPreset = MarginPreset.NORMAL,
                headerText = "سمارٹ ورڈ پروسیسر • Smart Word Processor",
                footerText = "صفحہ {page} از {pages}"
            ),
            blocks = blocks
        )
    }
}
