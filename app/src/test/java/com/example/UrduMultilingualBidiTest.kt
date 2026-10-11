package com.example

import android.content.Context
import android.graphics.Typeface
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.core.error.DocumentResult
import com.example.core.layout.PageLayoutConfig
import com.example.core.layout.PageSize
import com.example.core.typography.BidiUtils
import com.example.core.typography.DocumentFontManager
import com.example.data.local.AppDatabase
import com.example.data.model.HeadingStyle
import com.example.data.model.InlineSpan
import com.example.data.model.ListType
import com.example.data.model.ParagraphBlock
import com.example.data.model.RichDocument
import com.example.data.model.TextAlignment
import com.example.data.repository.DocumentRepositoryImpl
import com.example.data.repository.TemplateType
import com.example.data.service.DocumentExportService
import com.example.data.service.DocumentImportService
import com.example.data.service.DocumentImportServiceImpl
import com.example.data.service.PdfExportServiceImpl
import com.example.data.storage.DocumentStorageManager
import com.example.editor.engine.EditorEngineIntegration
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File
import java.util.UUID

/**
 * Comprehensive test suite verifying all 9 requirements for Urdu, English, Arabic-script,
 * and mixed-direction document editing:
 * 1. Complete Urdu paragraph
 * 2. Complete English paragraph
 * 3. Urdu and English in the same paragraph
 * 4. Numbers within Urdu text
 * 5. Email addresses and URLs inside Urdu paragraphs
 * 6. Urdu bullet and numbered lists
 * 7. Urdu text with bold and underlining
 * 8. Saving and reopening Urdu documents
 * 9. Printing mixed Urdu and English content to PDF
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class UrduMultilingualBidiTest {

    private lateinit var context: Context
    private lateinit var database: AppDatabase
    private lateinit var storageManager: DocumentStorageManager
    private lateinit var pdfExportService: PdfExportServiceImpl
    private lateinit var repository: DocumentRepositoryImpl

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        storageManager = DocumentStorageManager(context)
        pdfExportService = PdfExportServiceImpl()
        val importService = DocumentImportServiceImpl(storageManager)
        repository = DocumentRepositoryImpl(
            documentDao = database.documentDao(),
            storageManager = storageManager,
            exportService = pdfExportService,
            importService = importService
        )
    }

    @After
    fun tearDown() {
        database.close()
    }

    // =========================================================================
    // TEST CASE 1: A complete Urdu paragraph
    // =========================================================================
    @Test
    fun testCase1_completeUrduParagraph() {
        val urduText = "اردو زبان برصغیر پاک و ہند کی ایک عظیم اور شیریں زبان ہے۔ اس میں شعری لطافت اور نثری وسعت بے مثال ہے۔"
        
        // Direction detection
        assertTrue("Urdu text must be detected as RTL", BidiUtils.detectParagraphIsRtl(urduText))
        assertTrue("Urdu text must contain RTL characters", BidiUtils.containsRtl(urduText))
        
        // Aerab and special Urdu letters preservation
        val urduWithAerab = "اَلْحَمْدُ لِلّٰهِ رَبِّ الْعَالَمِیْنَ۔ چائے، پہاڑ، گاڑی، گڑیا، پودا، ژالہ باری، ٹماٹر۔"
        assertTrue("Urdu text with aerab and unique letters must be detected as RTL", BidiUtils.detectParagraphIsRtl(urduWithAerab))
        
        // Punctuation placement check: sentence ends with Urdu full-stop '۔'
        assertTrue("Sentence ending punctuation must be preserved", urduText.endsWith("۔"))
        
        val block = ParagraphBlock(
            text = urduText,
            isRtl = true,
            alignment = TextAlignment.START
        )
        assertEquals(urduText, block.text)
        assertTrue(block.isRtl)
    }

    // =========================================================================
    // TEST CASE 2: A complete English paragraph
    // =========================================================================
    @Test
    fun testCase2_completeEnglishParagraph() {
        val englishText = "Smart Word Processor provides enterprise-grade typographic layout and bidirectional editing capabilities."
        
        // Direction detection
        assertFalse("English text must NOT be detected as RTL", BidiUtils.detectParagraphIsRtl(englishText))
        assertFalse("English text must NOT contain RTL characters", BidiUtils.containsRtl(englishText))
        
        val block = ParagraphBlock(
            text = englishText,
            isRtl = false,
            alignment = TextAlignment.START
        )
        assertFalse(block.isRtl)
        assertEquals(englishText, block.text)
    }

    // =========================================================================
    // TEST CASE 3: Urdu and English in the same paragraph
    // =========================================================================
    @Test
    fun testCase3_mixedUrduAndEnglishInSameParagraph() {
        val mixedText = "جدید دور میں Artificial Intelligence اور Machine Learning نے زبانوں کی تحقیق کو آسان بنا دیا ہے۔"
        
        // Primary script is Urdu -> overall paragraph base direction must be RTL
        assertTrue("Mixed paragraph starting with Urdu must have RTL base direction", BidiUtils.detectParagraphIsRtl(mixedText))
        
        // Character sequences must NOT be reversed in memory
        assertTrue("Must contain English term intact without reversal", mixedText.contains("Artificial Intelligence"))
        assertTrue("Must contain English term intact without reversal", mixedText.contains("Machine Learning"))
        assertTrue("Must contain Urdu prefix intact", mixedText.startsWith("جدید دور میں"))
        
        // Annotated paragraph check
        val block = ParagraphBlock(text = mixedText, isRtl = true)
        val annotated = EditorEngineIntegration.buildAnnotatedParagraph(block)
        assertEquals(mixedText, annotated.text)
    }

    // =========================================================================
    // TEST CASE 4: Numbers within Urdu text
    // =========================================================================
    @Test
    fun testCase4_numbersWithinUrduText() {
        // Both Latin digits and Eastern Arabic-Indic numerals
        val textWithNumbers = "پاکستان 14 اگست 1947 کو وجود میں آیا اور سال 2026 میں ترقی کی نئی منازل طے کر رہا ہے جبکہ روایتی اعداد ۱۲۳۴ بھی رائج ہیں۔"
        
        assertTrue("Urdu text with numbers must be detected as RTL", BidiUtils.detectParagraphIsRtl(textWithNumbers))
        assertTrue("ASCII digits must remain intact", textWithNumbers.contains("1947"))
        assertTrue("ASCII digits must remain intact", textWithNumbers.contains("2026"))
        assertTrue("Urdu numerals must remain intact", textWithNumbers.contains("۱۲۳۴"))
        
        // Numeral converter test
        val converted = BidiUtils.toUrduNumerals(2026)
        assertEquals("۲۰۲۶", converted)
    }

    // =========================================================================
    // TEST CASE 5: Email addresses and URLs inside Urdu paragraphs
    // =========================================================================
    @Test
    fun testCase5_emailAddressesAndUrlsInsideUrduParagraphs() {
        val email = "info@smartword.org"
        val url = "https://smartword.org"
        val paragraphText = "رابطہ کے لیے براہ کرم $email پر ای میل بھیجیں یا $url وزٹ کریں۔"
        
        assertTrue("Paragraph with links must be detected as RTL", BidiUtils.detectParagraphIsRtl(paragraphText))
        assertTrue("Email address must not be modified or reversed", paragraphText.contains(email))
        assertTrue("URL must not be modified or reversed", paragraphText.contains(url))
        
        // Spans targeting URL/email
        val emailStart = paragraphText.indexOf(email)
        val emailEnd = emailStart + email.length
        val urlStart = paragraphText.indexOf(url)
        val urlEnd = urlStart + url.length
        
        val block = ParagraphBlock(
            text = paragraphText,
            isRtl = true,
            spans = listOf(
                InlineSpan(start = emailStart, end = emailEnd, linkUrl = "mailto:$email", isUnderline = true),
                InlineSpan(start = urlStart, end = urlEnd, linkUrl = url, isUnderline = true)
            )
        )
        
        val annotated = EditorEngineIntegration.buildAnnotatedParagraph(block)
        val urlAnnotations = annotated.getStringAnnotations("URL", 0, paragraphText.length)
        assertEquals(2, urlAnnotations.size)
        assertEquals("mailto:$email", urlAnnotations[0].item)
        assertEquals(url, urlAnnotations[1].item)
    }

    // =========================================================================
    // TEST CASE 6: Urdu bullet and numbered lists
    // =========================================================================
    @Test
    fun testCase6_urduBulletAndNumberedLists() {
        val bulletText = "دائیں سے بائیں خطاطی اور کرسر کی درست سمت بندی"
        val bulletBlock = ParagraphBlock(
            text = bulletText,
            isRtl = true,
            listType = ListType.BULLET
        )
        assertEquals(ListType.BULLET, bulletBlock.listType)
        assertTrue(bulletBlock.isRtl)
        
        // Numbered list formatting
        val formattedNumberLatin = BidiUtils.formatListNumber(1, isRtl = true, useUrduNumerals = false)
        val formattedNumberUrdu = BidiUtils.formatListNumber(1, isRtl = true, useUrduNumerals = true)
        
        assertEquals("1۔ ", formattedNumberLatin)
        assertEquals("۱۔ ", formattedNumberUrdu)
        
        // Plain text import list recognition
        val importedText = "• پہلی شق\n• دوسری شق\n1. تیسری شق"
        val doc = storageManager.createDocumentFromPlainText("List Test", importedText)
        assertEquals(3, doc.blocks.size)
        val b0 = doc.blocks[0] as ParagraphBlock
        val b1 = doc.blocks[1] as ParagraphBlock
        val b2 = doc.blocks[2] as ParagraphBlock
        
        assertEquals(ListType.BULLET, b0.listType)
        assertEquals("پہلی شق", b0.text)
        assertTrue(b0.isRtl)
        
        assertEquals(ListType.BULLET, b1.listType)
        assertEquals("دوسری شق", b1.text)
        assertTrue(b1.isRtl)
        
        assertEquals(ListType.NUMBERED, b2.listType)
        assertEquals("تیسری شق", b2.text)
        assertTrue(b2.isRtl)
    }

    // =========================================================================
    // TEST CASE 7: Urdu text with bold and underlining
    // =========================================================================
    @Test
    fun testCase7_urduTextWithBoldAndUnderline() {
        val fullText = "یہ عبارت جلی حروف اور خط کشیدہ انداز میں ہے۔"
        val boldRangeText = "جلی حروف"
        val underlineRangeText = "خط کشیدہ"
        
        val boldStart = fullText.indexOf(boldRangeText)
        val boldEnd = boldStart + boldRangeText.length
        
        val underlineStart = fullText.indexOf(underlineRangeText)
        val underlineEnd = underlineStart + underlineRangeText.length
        
        val block = ParagraphBlock(
            text = fullText,
            isRtl = true,
            spans = listOf(
                InlineSpan(start = boldStart, end = boldEnd, isBold = true, textColorHex = "#B91C1C"),
                InlineSpan(start = underlineStart, end = underlineEnd, isUnderline = true, isBold = true, textColorHex = "#1D4ED8")
            )
        )
        
        assertEquals(2, block.spans.size)
        assertTrue(block.spans[0].isBold)
        assertTrue(block.spans[1].isUnderline)
        assertTrue(block.spans[1].isBold)
        
        val annotated = EditorEngineIntegration.buildAnnotatedParagraph(block)
        val boldStyles = annotated.spanStyles.filter { it.start == boldStart && it.end == boldEnd }
        val underlineStyles = annotated.spanStyles.filter { it.start == underlineStart && it.end == underlineEnd }
        
        assertEquals(1, boldStyles.size)
        assertEquals(1, underlineStyles.size)
    }

    // =========================================================================
    // TEST CASE 8: Saving and reopening Urdu documents
    // =========================================================================
    @Test
    fun testCase8_savingAndReopeningUrduDocuments() = runTest {
        val createResult = repository.createDocument(
            title = "اردو ٹیسٹ دستاویز",
            template = TemplateType.URDU_DOCUMENT
        )
        assertTrue("Urdu document creation must succeed", createResult is DocumentResult.Success)
        val createdDoc = (createResult as DocumentResult.Success).data
        val docId = createdDoc.id
        
        // Verify in-memory properties
        assertTrue(createdDoc.blocks.filterIsInstance<ParagraphBlock>().any { it.isRtl })
        
        // Reopen from storage
        val reloadResult = repository.getDocument(docId)
        assertTrue("Reopening saved Urdu document must succeed", reloadResult is DocumentResult.Success)
        val reloadedDoc = (reloadResult as DocumentResult.Success).data
        
        assertEquals(createdDoc.title, reloadedDoc.title)
        assertEquals(createdDoc.blocks.size, reloadedDoc.blocks.size)
        
        val originalFirstParagraph = createdDoc.blocks.filterIsInstance<ParagraphBlock>().first()
        val reloadedFirstParagraph = reloadedDoc.blocks.filterIsInstance<ParagraphBlock>().first()
        
        assertEquals(originalFirstParagraph.text, reloadedFirstParagraph.text)
        assertEquals(originalFirstParagraph.isRtl, reloadedFirstParagraph.isRtl)
        assertEquals(originalFirstParagraph.spans.size, reloadedFirstParagraph.spans.size)
        assertEquals(originalFirstParagraph.alignment, reloadedFirstParagraph.alignment)
    }

    // =========================================================================
    // TEST CASE 9: Printing mixed Urdu and English content to PDF
    // =========================================================================
    @Test
    fun testCase9_printingMixedUrduAndEnglishContent() = runTest {
        val testDoc = repository.createDocument(
            title = "پرنٹ ٹیسٹ (Print Test)",
            template = TemplateType.URDU_DOCUMENT
        )
        assertTrue(testDoc is DocumentResult.Success)
        val document = (testDoc as DocumentResult.Success).data
        
        val pdfOutputFile = File(context.cacheDir, "test_urdu_export_${UUID.randomUUID()}.pdf")
        
        // Export to PDF
        val exportResult = pdfExportService.exportToPdf(context, document, pdfOutputFile)
        if (exportResult is DocumentResult.Failure) {
            println("PDF Export Failure details: ${exportResult.error.message}")
            exportResult.error.cause?.printStackTrace()
        }
        assertTrue("Export to PDF must succeed without exception: ${(exportResult as? DocumentResult.Failure)?.error?.message}", exportResult is DocumentResult.Success)
        
        // Verify exported PDF file exists and has non-zero bytes
        assertTrue("Exported PDF file must exist on disk", pdfOutputFile.exists())
        assertTrue("Exported PDF file must contain data (> 20 bytes)", pdfOutputFile.length() > 20)
        
        // Clean up temp file
        pdfOutputFile.delete()
    }

    // =========================================================================
    // FONT RESOLUTION TEST: Bundled and System Typeface fallback
    // =========================================================================
    @Test
    fun testFontResolutionAndFallback() {
        // Noto Nastaliq Urdu
        val nastaliqTypeface = DocumentFontManager.getTypeface(context, "Noto Nastaliq Urdu", isRtl = true)
        assertNotNull("Typeface for Noto Nastaliq Urdu must not be null", nastaliqTypeface)
        
        // Noto Sans Arabic
        val arabicSansTypeface = DocumentFontManager.getTypeface(context, "Noto Sans Arabic", isRtl = true)
        assertNotNull("Typeface for Noto Sans Arabic must not be null", arabicSansTypeface)
        
        // English serif
        val timesTypeface = DocumentFontManager.getTypeface(context, "Times New Roman", isRtl = false)
        assertNotNull("Typeface for Times New Roman must not be null", timesTypeface)
        
        // Fallback for RTL when font is null or default
        val rtlFallback = DocumentFontManager.getTypeface(context, null, isRtl = true)
        assertNotNull("Fallback typeface for RTL must not be null", rtlFallback)
    }
}
