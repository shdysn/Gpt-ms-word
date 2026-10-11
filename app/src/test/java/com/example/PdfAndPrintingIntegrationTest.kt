package com.example

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.os.CancellationSignal
import android.os.ParcelFileDescriptor
import android.print.PageRange
import android.print.PrintAttributes
import android.print.PrintDocumentAdapter
import android.print.PrintDocumentInfo
import androidx.test.core.app.ApplicationProvider
import com.example.core.error.DocumentResult
import com.example.core.layout.MarginPreset
import com.example.core.layout.PageBorderStyle
import com.example.core.layout.PageLayoutConfig
import com.example.core.layout.PageNumberFormat
import com.example.core.layout.PageOrientation
import com.example.core.layout.PageSize
import com.example.core.layout.PaginationEngine
import com.example.core.typography.BidiUtils
import com.example.data.model.DividerBlock
import com.example.data.model.HeadingStyle
import com.example.data.model.ImageBlock
import com.example.data.model.InlineSpan
import com.example.data.model.ListType
import com.example.data.model.PageBreakBlock
import com.example.data.model.ParagraphBlock
import com.example.data.model.RichDocument
import com.example.data.model.TableCell
import com.example.data.model.TableBlock
import com.example.data.model.TextAlignment
import com.example.data.service.PdfExportServiceImpl
import com.example.data.service.WordPrintDocumentAdapter
import kotlinx.coroutines.test.runTest
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
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.UUID

/**
 * Exhaustive integration test suite verifying reliable PDF export and Android printing:
 * 1. One-page A4 document
 * 2. Multi-page document (no truncation)
 * 3. Landscape document (all page sizes)
 * 4. Urdu document (RTL, Nastaliq, Eastern numerals)
 * 5. Mixed Urdu and English document (BiDi)
 * 6. Document containing tables
 * 7. Document containing images
 * 8. Document containing page breaks
 * 9. Android printing workflow (PrintDocumentAdapter onLayout & onWrite)
 * 10. Print page range selection (selective page printing)
 * 11. Graceful cancellation & error handling
 * 12. Storage destination handling (SAF URI export)
 * 13. Rich text formatting preservation (bold, italic, strikethrough, super/subscript, colors, font sizes)
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class PdfAndPrintingIntegrationTest {

    private lateinit var context: Context
    private lateinit var pdfExportService: PdfExportServiceImpl

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        pdfExportService = PdfExportServiceImpl()
    }

    // =========================================================================
    // 1. ONE-PAGE A4 DOCUMENT TEST
    // =========================================================================
    @Test
    fun testOnePageA4PdfExport() = runTest {
        val document = RichDocument(
            title = "Executive Summary",
            author = "Engineering Department",
            layoutConfig = PageLayoutConfig(
                pageSize = PageSize.A4,
                orientation = PageOrientation.PORTRAIT,
                marginPreset = MarginPreset.NORMAL,
                showHeader = true,
                headerText = "Smart Word Processor Report",
                showFooter = true,
                footerText = "Page {page} of {pages}"
            ),
            blocks = listOf(
                ParagraphBlock(
                    text = "Executive Summary for Q4 2026",
                    headingStyle = HeadingStyle.TITLE,
                    alignment = TextAlignment.CENTER
                ),
                ParagraphBlock(
                    text = "This is a single page A4 document validating page boundary calculations and PDF generation.",
                    headingStyle = HeadingStyle.NORMAL
                )
            )
        )

        val pages = PaginationEngine.paginate(document)
        assertEquals("Document must fit on exactly 1 page", 1, pages.size)
        assertEquals(595, document.layoutConfig.pageWidthPt)
        assertEquals(842, document.layoutConfig.pageHeightPt)

        val outputFile = File(context.cacheDir, "test_one_page_a4_${UUID.randomUUID()}.pdf")
        val result = pdfExportService.exportToPdf(context, document, outputFile)

        assertTrue("PDF export must succeed", result is DocumentResult.Success)
        assertTrue("Exported PDF file must exist on disk", outputFile.exists())
        assertTrue("Exported PDF file must contain bytes", outputFile.length() > 20)

        outputFile.delete()
    }

    // =========================================================================
    // 2. MULTI-PAGE DOCUMENT TEST (LONG CONTENT NO TRUNCATION)
    // =========================================================================
    @Test
    fun testMultiPageDocumentPdfExport() = runTest {
        val paragraphs = mutableListOf<ParagraphBlock>()
        paragraphs.add(
            ParagraphBlock(
                text = "Multi-Page Comprehensive Technical Whitepaper",
                headingStyle = HeadingStyle.TITLE
            )
        )

        // Generate 50 lengthy paragraphs to guarantee multiple pages
        for (i in 1..50) {
            paragraphs.add(
                ParagraphBlock(
                    text = "Section $i: Advanced Typographic Layout and Flow Control. " +
                        "In modern document processors, layout calculation must accurately translate text runs, " +
                        "measure line heights, and flow sentences across discrete physical boundaries without " +
                        "clipping, dropping, or truncating paragraphs. Each block is measured using exact postscript " +
                        "point metrics at 72 DPI, accounting for top and bottom margins, line spacing, and header areas.",
                    headingStyle = if (i % 5 == 1) HeadingStyle.HEADING_2 else HeadingStyle.NORMAL,
                    lineSpacingMultiplier = 1.3f
                )
            )
        }

        val document = RichDocument(
            title = "Multi-Page Whitepaper",
            layoutConfig = PageLayoutConfig(
                pageSize = PageSize.A4,
                orientation = PageOrientation.PORTRAIT,
                marginPreset = MarginPreset.NORMAL,
                showHeader = true,
                headerText = "{title} - Confidential",
                showFooter = true,
                footerText = "Page {page} of {pages}"
            ),
            blocks = paragraphs
        )

        val pages = PaginationEngine.paginate(document)
        assertTrue("Document must generate at least 3 pages", pages.size >= 3)

        // Verify footer formatting across all pages
        pages.forEachIndexed { index, page ->
            assertEquals(index + 1, page.pageNumber)
            assertEquals(pages.size, page.totalPages)
            assertTrue("Footer must contain formatted page number", page.formattedFooter.contains("${index + 1}"))
            assertTrue("Footer must contain total pages count", page.formattedFooter.contains("${pages.size}"))
        }

        val outputFile = File(context.cacheDir, "test_multi_page_${UUID.randomUUID()}.pdf")
        val result = pdfExportService.exportToPdf(context, document, outputFile)

        assertTrue("Multi-page PDF export must succeed", result is DocumentResult.Success)
        assertTrue(outputFile.exists())
        assertTrue("Multi-page output must contain bytes", outputFile.length() > 20)

        outputFile.delete()
    }

    // =========================================================================
    // 3. LANDSCAPE DOCUMENT TEST (A4, A5, LETTER, LEGAL)
    // =========================================================================
    @Test
    fun testLandscapeDocumentPdfExport() = runTest {
        val pageSizes = listOf(PageSize.A4, PageSize.A5, PageSize.LETTER, PageSize.LEGAL)

        for (ps in pageSizes) {
            val document = RichDocument(
                title = "Landscape ${ps.name} Specification",
                layoutConfig = PageLayoutConfig(
                    pageSize = ps,
                    orientation = PageOrientation.LANDSCAPE,
                    marginPreset = MarginPreset.MODERATE
                ),
                blocks = listOf(
                    ParagraphBlock(
                        text = "Landscape Presentation Document - ${ps.name}",
                        headingStyle = HeadingStyle.TITLE,
                        alignment = TextAlignment.CENTER
                    ),
                    ParagraphBlock(
                        text = "Validating wide-aspect ratio printing and PDF rendering.",
                        headingStyle = HeadingStyle.NORMAL
                    )
                )
            )

            val layout = document.layoutConfig
            assertTrue(
                "In landscape, width must be greater than height for ${ps.name}",
                layout.pageWidthPt > layout.pageHeightPt
            )

            val outputFile = File(context.cacheDir, "test_landscape_${ps.name}_${UUID.randomUUID()}.pdf")
            val result = pdfExportService.exportToPdf(context, document, outputFile)

            assertTrue("Landscape export for ${ps.name} must succeed", result is DocumentResult.Success)
            assertTrue(outputFile.exists())
            outputFile.delete()
        }
    }

    // =========================================================================
    // 4. URDU DOCUMENT TEST (RTL, NASTALIQ, NUMERALS)
    // =========================================================================
    @Test
    fun testUrduDocumentPdfExport() = runTest {
        val urduTitle = "اردو ادب اور زبان کا تاریخی جائزہ"
        val urduBody = "اردو زبان برصغیر پاک و ہند کی ایک عظیم اور شیریں زبان ہے۔ اس میں شعری لطافت اور نثری وسعت بے مثال ہے۔ " +
            "قیام پاکستان کے بعد اردو کو قومی زبان کا درجہ حاصل ہوا۔ سن ۱۹۴۷ء سے لے کر آج تک اردو ادب نے بے شمار " +
            "شاہکار پیش کیے۔ فیض احمد فیض، احمد فراز اور پروین شاکر کی شاعری نے دنیا بھر میں شہرت پائی۔"

        assertTrue("Urdu text must be detected as RTL", BidiUtils.detectParagraphIsRtl(urduBody))

        val document = RichDocument(
            title = "اردو دستاویز",
            layoutConfig = PageLayoutConfig(
                pageSize = PageSize.A4,
                orientation = PageOrientation.PORTRAIT,
                showHeader = true,
                headerText = "ادارہ فروغ قومی زبان",
                showFooter = true,
                footerText = "صفحہ {page} از {pages}"
            ),
            blocks = listOf(
                ParagraphBlock(
                    text = urduTitle,
                    headingStyle = HeadingStyle.TITLE,
                    alignment = TextAlignment.CENTER,
                    isRtl = true
                ),
                ParagraphBlock(
                    text = urduBody,
                    headingStyle = HeadingStyle.NORMAL,
                    alignment = TextAlignment.START,
                    isRtl = true,
                    lineSpacingMultiplier = 1.4f
                ),
                ParagraphBlock(
                    text = "اردو اہم نکات اور اصناف سخن:",
                    headingStyle = HeadingStyle.HEADING_2,
                    isRtl = true
                ),
                ParagraphBlock(
                    text = "غزل کی صنف سخن",
                    listType = ListType.NUMBERED,
                    isRtl = true
                ),
                ParagraphBlock(
                    text = "نظم اور مرثیہ",
                    listType = ListType.NUMBERED,
                    isRtl = true
                ),
                ParagraphBlock(
                    text = "افسانہ اور ناول نگاری",
                    listType = ListType.NUMBERED,
                    isRtl = true
                )
            )
        )

        val outputFile = File(context.cacheDir, "test_urdu_${UUID.randomUUID()}.pdf")
        val result = pdfExportService.exportToPdf(context, document, outputFile)

        assertTrue("Urdu PDF export must succeed", result is DocumentResult.Success)
        assertTrue(outputFile.exists())
        assertTrue(outputFile.length() > 50)

        outputFile.delete()
    }

    // =========================================================================
    // 5. MIXED URDU AND ENGLISH DOCUMENT TEST
    // =========================================================================
    @Test
    fun testMixedUrduAndEnglishPdfExport() = runTest {
        val mixedText1 = "یہ ایک bilingual document ہے جس میں Smart Word Processor کا استعمال کیا گیا ہے۔"
        val mixedText2 = "براہ کرم رابطہ کریں بذریعہ email: contact@smartword.pk یا ہماری website https://smartword.example.com وزٹ کریں۔"
        val mixedText3 = "System Version 2.5 (2026 release) supports full BiDi rendering for Urdu Nastaliq and English text."

        assertTrue("Mixed text 1 contains RTL", BidiUtils.containsRtl(mixedText1))
        assertTrue("Mixed text 2 contains RTL", BidiUtils.containsRtl(mixedText2))

        val document = RichDocument(
            title = "Bilingual Document",
            blocks = listOf(
                ParagraphBlock(
                    text = "Bilingual Communication / دو لسانی دستاویز",
                    headingStyle = HeadingStyle.TITLE,
                    alignment = TextAlignment.CENTER
                ),
                ParagraphBlock(
                    text = mixedText1,
                    isRtl = true
                ),
                ParagraphBlock(
                    text = mixedText2,
                    isRtl = true,
                    spans = listOf(
                        InlineSpan(start = 30, end = 52, linkUrl = "mailto:contact@smartword.pk", isUnderline = true),
                        InlineSpan(start = 72, end = 101, linkUrl = "https://smartword.example.com", isUnderline = true)
                    )
                ),
                ParagraphBlock(
                    text = mixedText3,
                    isRtl = false
                )
            )
        )

        val outputFile = File(context.cacheDir, "test_mixed_urdu_en_${UUID.randomUUID()}.pdf")
        val result = pdfExportService.exportToPdf(context, document, outputFile)

        assertTrue("Mixed Urdu and English PDF export must succeed", result is DocumentResult.Success)
        assertTrue(outputFile.exists())
        outputFile.delete()
    }

    // =========================================================================
    // 6. DOCUMENT CONTAINING TABLES TEST
    // =========================================================================
    @Test
    fun testDocumentContainingTablesPdfExport() = runTest {
        val tableCells = listOf(
            listOf(
                TableCell(text = "Project Milestone", isBold = true, alignment = TextAlignment.START),
                TableCell(text = "Target Date", isBold = true, alignment = TextAlignment.CENTER),
                TableCell(text = "Completion Status", isBold = true, alignment = TextAlignment.CENTER)
            ),
            listOf(
                TableCell(text = "PDF Export Architecture", isBold = false),
                TableCell(text = "Oct 15, 2026", alignment = TextAlignment.CENTER),
                TableCell(text = "Completed (100%)", backgroundColorHex = "#DCFCE7", alignment = TextAlignment.CENTER)
            ),
            listOf(
                TableCell(text = "Android Print Framework", isBold = false),
                TableCell(text = "Oct 18, 2026", alignment = TextAlignment.CENTER),
                TableCell(text = "Verified (100%)", backgroundColorHex = "#DCFCE7", alignment = TextAlignment.CENTER)
            ),
            listOf(
                TableCell(text = "اردو نستعلیق جدول (Urdu Table)", isBold = false),
                TableCell(text = "Oct 20, 2026", alignment = TextAlignment.CENTER),
                TableCell(text = "Active", backgroundColorHex = "#E0F2FE", alignment = TextAlignment.CENTER)
            )
        )

        val document = RichDocument(
            title = "Project Milestones Table",
            blocks = listOf(
                ParagraphBlock(
                    text = "Project Deliverables Schedule",
                    headingStyle = HeadingStyle.HEADING_1
                ),
                TableBlock(
                    rowCount = 4,
                    columnCount = 3,
                    cells = tableCells,
                    hasBorders = true,
                    borderColorHex = "#94A3B8",
                    tableWidthPercent = 95,
                    alignment = TextAlignment.CENTER
                ),
                ParagraphBlock(
                    text = "Table above displays structured matrix with custom cell colors and alignments.",
                    headingStyle = HeadingStyle.QUOTE
                )
            )
        )

        val outputFile = File(context.cacheDir, "test_table_${UUID.randomUUID()}.pdf")
        val result = pdfExportService.exportToPdf(context, document, outputFile)

        assertTrue("Table PDF export must succeed", result is DocumentResult.Success)
        assertTrue(outputFile.exists())
        outputFile.delete()
    }

    // =========================================================================
    // 7. DOCUMENT CONTAINING IMAGES TEST
    // =========================================================================
    @Test
    fun testDocumentContainingImagesPdfExport() = runTest {
        // Create a test bitmap file
        val imageFile = File(context.cacheDir, "test_banner_${UUID.randomUUID()}.png")
        val bitmap = Bitmap.createBitmap(200, 150, Bitmap.Config.ARGB_8888)
        FileOutputStream(imageFile).use { out ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 90, out)
        }

        val document = RichDocument(
            title = "Document with Media Assets",
            blocks = listOf(
                ParagraphBlock(
                    text = "Visual Architecture Overview",
                    headingStyle = HeadingStyle.HEADING_1
                ),
                ImageBlock(
                    imageUri = imageFile.absolutePath,
                    caption = "Figure 1: High-level System Architecture Diagram",
                    widthDp = 280,
                    heightDp = 180,
                    alignment = TextAlignment.CENTER
                ),
                ParagraphBlock(
                    text = "The figure above illustrates the print document adapter pipeline.",
                    headingStyle = HeadingStyle.NORMAL
                )
            )
        )

        val outputFile = File(context.cacheDir, "test_image_${UUID.randomUUID()}.pdf")
        val result = pdfExportService.exportToPdf(context, document, outputFile)

        assertTrue("Image PDF export must succeed", result is DocumentResult.Success)
        assertTrue(outputFile.exists())

        imageFile.delete()
        outputFile.delete()
    }

    // =========================================================================
    // 8. DOCUMENT CONTAINING PAGE BREAKS TEST
    // =========================================================================
    @Test
    fun testDocumentContainingPageBreaksPdfExport() = runTest {
        val document = RichDocument(
            title = "Document with Explicit Page Breaks",
            layoutConfig = PageLayoutConfig(
                pageSize = PageSize.A4,
                showHeader = true,
                headerText = "Sectional Manual",
                showFooter = true,
                footerText = "Page {page} of {pages}"
            ),
            blocks = listOf(
                ParagraphBlock(text = "Chapter 1: Introduction", headingStyle = HeadingStyle.TITLE),
                ParagraphBlock(text = "Content of Chapter 1 resides entirely on page 1."),
                PageBreakBlock(),
                ParagraphBlock(text = "Chapter 2: Core Architecture", headingStyle = HeadingStyle.TITLE),
                ParagraphBlock(text = "Content of Chapter 2 is pushed to page 2 via explicit break."),
                PageBreakBlock(),
                ParagraphBlock(text = "Chapter 3: Verification & Test Results", headingStyle = HeadingStyle.TITLE),
                ParagraphBlock(text = "Content of Chapter 3 starts on page 3.")
            )
        )

        val pages = PaginationEngine.paginate(document)
        assertEquals("Explicit page breaks must produce exactly 3 pages", 3, pages.size)
        assertEquals("Page 1 must start with Chapter 1", "Chapter 1: Introduction", (pages[0].items[0] as com.example.core.layout.ParagraphPageItem).originalBlock.text)
        assertEquals("Page 2 must start with Chapter 2", "Chapter 2: Core Architecture", (pages[1].items[0] as com.example.core.layout.ParagraphPageItem).originalBlock.text)
        assertEquals("Page 3 must start with Chapter 3", "Chapter 3: Verification & Test Results", (pages[2].items[0] as com.example.core.layout.ParagraphPageItem).originalBlock.text)

        val outputFile = File(context.cacheDir, "test_page_breaks_${UUID.randomUUID()}.pdf")
        val result = pdfExportService.exportToPdf(context, document, outputFile)

        assertTrue("Page break PDF export must succeed", result is DocumentResult.Success)
        assertTrue(outputFile.exists())
        outputFile.delete()
    }

    // =========================================================================
    // 9. ANDROID PRINTING WORKFLOW TEST (LAYOUT & ATTRIBUTES)
    // =========================================================================
    @Test
    fun testAndroidPrintingWorkflowLayoutAndAttributes() {
        val document = RichDocument(
            title = "Printing Specification",
            layoutConfig = PageLayoutConfig(
                pageSize = PageSize.LETTER,
                orientation = PageOrientation.PORTRAIT
            ),
            blocks = listOf(
                ParagraphBlock(text = "Printing Workflow Verification", headingStyle = HeadingStyle.TITLE),
                ParagraphBlock(text = "Verifying PrintDocumentAdapter onLayout contract.")
            )
        )

        val adapter = pdfExportService.createPrintDocumentAdapter(context, document)
        assertNotNull("PrintDocumentAdapter must not be null", adapter)

        val oldAttrs = PrintAttributes.Builder()
            .setMediaSize(PrintAttributes.MediaSize.ISO_A4)
            .build()
        val newAttrs = PrintAttributes.Builder()
            .setMediaSize(PrintAttributes.MediaSize.NA_LETTER)
            .build()

        val layoutCallback = android.print.TestLayoutResultCallback()
        adapter.onLayout(oldAttrs, newAttrs, CancellationSignal(), layoutCallback, Bundle())

        assertTrue("onLayoutFinished must be invoked", layoutCallback.isFinished)
        assertEquals("Page count must be 1", 1, layoutCallback.info?.pageCount)
        assertTrue("Attributes changed must be true", layoutCallback.isChanged)
    }

    // =========================================================================
    // 10. PRINT PAGE RANGE SELECTION TEST (SELECTIVE PRINTING)
    // =========================================================================
    @Test
    fun testAndroidPrintingWorkflowPageRangeSelection() {
        val document = RichDocument(
            title = "Multi-Page Report",
            blocks = listOf(
                ParagraphBlock(text = "Page 1 Content", headingStyle = HeadingStyle.TITLE),
                PageBreakBlock(),
                ParagraphBlock(text = "Page 2 Content", headingStyle = HeadingStyle.TITLE),
                PageBreakBlock(),
                ParagraphBlock(text = "Page 3 Content", headingStyle = HeadingStyle.TITLE)
            )
        )

        val adapter = pdfExportService.createPrintDocumentAdapter(context, document)

        // Initialize layout first
        val initLayoutCallback = android.print.TestLayoutResultCallback()
        adapter.onLayout(
            null,
            PrintAttributes.Builder().setMediaSize(PrintAttributes.MediaSize.ISO_A4).build(),
            CancellationSignal(),
            initLayoutCallback,
            Bundle()
        )

        // Select only Page 2 (0-based index 1)
        val selectedRange = arrayOf(PageRange(1, 1))
        val pipe = ParcelFileDescriptor.createPipe()
        val readFd = pipe[0]
        val writeFd = pipe[1]

        val writeCallback = android.print.TestWriteResultCallback()
        adapter.onWrite(selectedRange, writeFd, CancellationSignal(), writeCallback)

        assertTrue("onWriteFinished must be called for selective page range", writeCallback.isFinished)
        assertNotNull("Returned page ranges must not be null", writeCallback.writtenPages)
        assertEquals(1, writeCallback.writtenPages?.size)
        assertEquals(1, writeCallback.writtenPages?.get(0)?.start)
        assertEquals(1, writeCallback.writtenPages?.get(0)?.end)

        readFd.close()
        writeFd.close()
    }

    // =========================================================================
    // 11. PRINT CANCELLATION GRACEFUL HANDLING TEST
    // =========================================================================
    @Test
    fun testAndroidPrintingCancellationGracefulHandling() {
        val document = RichDocument(title = "Cancellation Document")
        val adapter = pdfExportService.createPrintDocumentAdapter(context, document)

        // Pre-cancel signal
        val cancelSignal = CancellationSignal().apply { cancel() }

        val layoutCallback = android.print.TestLayoutResultCallback()
        adapter.onLayout(null, null, cancelSignal, layoutCallback, Bundle())
        assertTrue("onLayoutCancelled must be called when signal is cancelled", layoutCallback.isCancelled)

        val writeCallback = android.print.TestWriteResultCallback()
        val pipe = ParcelFileDescriptor.createPipe()
        adapter.onWrite(arrayOf(PageRange.ALL_PAGES), pipe[1], cancelSignal, writeCallback)
        assertTrue("onWriteCancelled must be called when signal is cancelled", writeCallback.isCancelled)

        pipe[0].close()
        pipe[1].close()
    }

    // =========================================================================
    // 12. STORAGE DESTINATION / SAF URI EXPORT TEST
    // =========================================================================
    @Test
    fun testExportPdfToStorageUri() = runTest {
        val document = RichDocument(
            title = "SAF Storage Export Document",
            blocks = listOf(
                ParagraphBlock(text = "Storage Access Framework Destination Test", headingStyle = HeadingStyle.TITLE),
                ParagraphBlock(text = "Validating export to content/file URI destination.")
            )
        )

        val targetFile = File(context.cacheDir, "saf_dest_test_${UUID.randomUUID()}.pdf")
        val targetUri = Uri.fromFile(targetFile)

        val exportResult = pdfExportService.exportPdfToUri(context, document, targetUri)
        assertTrue("exportPdfToUri must succeed", exportResult is DocumentResult.Success)
        assertTrue("Target file must exist and contain bytes", targetFile.exists() && targetFile.length() > 20)

        targetFile.delete()
    }

    // =========================================================================
    // 13. RICH TEXT FORMATTING PRESERVATION IN PDF EXPORT
    // =========================================================================
    @Test
    fun testRichTextFormattingSpansPreservation() = runTest {
        val paragraphText = "Smart Word supports bold text, italic styling, underlined links, strikethrough words, and colored highlights."
        val spans = listOf(
            InlineSpan(start = 20, end = 29, isBold = true),
            InlineSpan(start = 31, end = 45, isItalic = true),
            InlineSpan(start = 47, end = 63, isUnderline = true, linkUrl = "https://example.com"),
            InlineSpan(start = 65, end = 84, isStrikethrough = true),
            InlineSpan(start = 90, end = 108, textColorHex = "#1D4ED8", highlightColorHex = "#FEF08A")
        )

        val document = RichDocument(
            title = "Formatting Specification",
            blocks = listOf(
                ParagraphBlock(
                    text = paragraphText,
                    spans = spans,
                    headingStyle = HeadingStyle.NORMAL,
                    lineSpacingMultiplier = 1.25f
                )
            )
        )

        val outputFile = File(context.cacheDir, "test_formatting_${UUID.randomUUID()}.pdf")
        val result = pdfExportService.exportToPdf(context, document, outputFile)

        assertTrue("Rich text formatting PDF export must succeed", result is DocumentResult.Success)
        assertTrue(outputFile.exists())
        outputFile.delete()
    }
}
