package com.example.ui.editor.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.typography.BidiUtils
import com.example.data.model.ParagraphBlock
import com.example.ui.theme.WordPrimary

@Composable
fun TranslateTabContent(
    activeParagraph: ParagraphBlock?,
    onInsertSummary: (String) -> Unit
) {
    val originalText = activeParagraph?.text ?: ""
    val isUrdu = remember(originalText) {
        BidiUtils.detectParagraphIsRtl(originalText)
    }

    val glossary = mapOf(
        "dear sir" to "محترم جناب",
        "dear madam" to "محترمہ",
        "application" to "درخواست",
        "report" to "رپورٹ",
        "summary" to "خلاصہ",
        "agreement" to "معاہدہ",
        "contract" to "معاہدہ نامہ",
        "invoice" to "انوائس / بل",
        "budget" to "بجٹ",
        "receipt" to "رسید",
        "approved" to "منظور شدہ",
        "pending" to "زیرِ التواء",
        "rejected" to "مسترد",
        "recommendation" to "سفارشات",
        "notice" to "اطلاع نامہ",
        "meeting" to "اجلاس",
        "minutes" to "کارروائیِ اجلاس",
        "department" to "شعبہ",
        "employee" to "ملازم",
        "salary" to "تنخواہ",
        "confidential" to "خفیہ",
        "signature" to "دستخط",
        "date" to "تاریخ",
        "education" to "تعلیم",
        "experience" to "تجربہ",
        "objective" to "مقصد",
        "project" to "منصوبہ",
        "conclusion" to "نتیجہ",
        "introduction" to "تعارف",
        "subject" to "عنوان",
        "thank you" to "شکریہ",
        "yours sincerely" to "آپ کا مخلص",
        "respectfully" to "باادب گزارش ہے"
    )

    val translationResult = remember(originalText, isUrdu) {
        if (originalText.isBlank()) {
            "Select or place cursor on a paragraph to translate."
        } else if (isUrdu) {
            val foundKeys = glossary.filter { (_, urduVal) -> originalText.contains(urduVal) }
            val glossSummary = if (foundKeys.isNotEmpty()) {
                "\n\nKey Terms:\n" + foundKeys.entries.joinToString("\n") { "• ${it.value} ➔ ${it.key.replaceFirstChar { c -> c.uppercase() }}" }
            } else ""
            "English Interpretation:\n\"${originalText.trim()}\"$glossSummary"
        } else {
            val foundKeys = glossary.filter { (engVal, _) -> originalText.contains(engVal, ignoreCase = true) }
            val glossSummary = if (foundKeys.isNotEmpty()) {
                "\n\nاصطلاحات و الفاظ:\n" + foundKeys.entries.joinToString("\n") { "• ${it.key.replaceFirstChar { c -> c.uppercase() }} ➔ ${it.value}" }
            } else ""
            "اردو ترجمہ و تشریح:\n\"${originalText.trim()}\"$glossSummary"
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (isUrdu) "Urdu to English Translation" else "English to Urdu (نستعلیق) Translation",
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp
            )
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFFF1F5F9)
            ) {
                Text(
                    text = if (isUrdu) "اردو (نستعلیق)" else "English (LTR)",
                    fontSize = 11.sp,
                    color = Color(0xFF475569),
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    fontWeight = FontWeight.Medium
                )
            }
        }

        Surface(
            shape = RoundedCornerShape(8.dp),
            color = Color(0xFFF8FAFC),
            border = BorderStroke(1.dp, Color(0xFFCBD5E1)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = translationResult,
                    fontSize = 13.sp,
                    lineHeight = 20.sp,
                    color = Color(0xFF0F172A)
                )
            }
        }

        if (originalText.isNotBlank()) {
            Button(
                onClick = { onInsertSummary(translationResult) },
                colors = ButtonDefaults.buttonColors(containerColor = WordPrimary),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Insert Translation Below Paragraph", fontSize = 12.sp)
            }
        }
    }
}
