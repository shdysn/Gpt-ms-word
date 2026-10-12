package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.core.error.DocumentResult
import com.example.core.layout.MarginPreset
import com.example.core.layout.PageBorderStyle
import com.example.core.layout.PageLayoutConfig
import com.example.core.layout.PageOrientation
import com.example.core.layout.PageSize
import com.example.core.layout.PaginationEngine
import com.example.core.typography.DocumentFontManager
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
import com.example.data.service.docx.DocxPackageReader
import com.example.data.service.docx.DocxPackageWriter
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.UUID

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class DocxWordFidelityTest {

    private lateinit var context: Context
    private lateinit var reader: DocxPackageReader
    private lateinit var writer: DocxPackageWriter
    private lateinit var pdfExportService: PdfExportServiceImpl

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        reader = DocxPackageReader(context)
        writer = DocxPackageWriter(context)
        pdfExportService = PdfExportServiceImpl()
    }

    // =========================================================================
    // 1. Simple One-Page Document Fidelity
    // =========================================================================
    @Test
    fun testSimpleOnePageDocumentFidelity() {
        val originalDoc = RichDocument(
            title = "Corporate Proposal",
            author = "Senior Consultant",
            layoutConfig = PageLayoutConfig(
                pageSize = PageSize.A4,
                orientation = PageOrientation.PORTRAIT,
                marginPreset = MarginPreset.NORMAL
            ),
            blocks = listOf(
                ParagraphBlock(
                    text = "Project Proposal",
                    headingStyle = HeadingStyle.TITLE,
                    alignment = TextAlignment.CENTER,
                    spaceBeforePt = 12f,
                    spaceAfterPt = 18f
                ),
                ParagraphBlock(
                    text = "This is a single page Microsoft Word document with bold text and italic styling.",
                    alignment = TextAlignment.JUSTIFY,
                    lineSpacingMultiplier = 1.15f,
                    spaceBeforePt = 0f,
                    spaceAfterPt = 6f,
                    spans = listOf(
                        InlineSpan(start = 48, end = 52, isBold = true),
                        InlineSpan(start = 62, end = 68, isItalic = true)
                    )
                )
            )
        )

        // Write to DOCX
        val out = ByteArrayOutputStream()
        writer.write(originalDoc, out)

        // Read back
        val result = reader.read(ByteArrayInputStream(out.toByteArray()))
        assertTrue("Import must succeed", result is DocumentResult.Success)
        val imported = (result as DocumentResult.Success).data.document

        assertEquals(PageSize.A4, imported.layoutConfig.pageSize)
        assertEquals(PageOrientation.PORTRAIT, imported.layoutConfig.orientation)
        assertEquals("Project Proposal", (imported.blocks[0] as ParagraphBlock).text)
        assertEquals(TextAlignment.CENTER, (imported.blocks[0] as ParagraphBlock).alignment)
        assertEquals(12f, (imported.blocks[0] as ParagraphBlock).spaceBeforePt, 0.5f)
        assertEquals(18f, (imported.blocks[0] as ParagraphBlock).spaceAfterPt, 0.5f)

        val p2 = imported.blocks[1] as ParagraphBlock
        assertEquals(TextAlignment.JUSTIFY, p2.alignment)
        val boldSpan = p2.spans.find { it.isBold }
        assertNotNull("Bold span must be preserved", boldSpan)

        // Pagination check: Exactly 1 page
        val pages = PaginationEngine.paginate(imported)
        assertEquals(1, pages.size)
        assertEquals(PageSize.A4.widthPt, pages[0].layout.pageWidthPt)
        assertEquals(PageSize.A4.heightPt, pages[0].layout.pageHeightPt)
    }

    // =========================================================================
    // 2. Multi-Page Document With Custom Margins
    // =========================================================================
    @Test
    fun testMultiPageDocumentDifferentMarginsFidelity() {
        val customLayout = PageLayoutConfig(
            pageSize = PageSize.LETTER,
            marginPreset = MarginPreset.CUSTOM,
            customTopMarginMm = 30f,
            customBottomMarginMm = 30f,
            customLeftMarginMm = 40f,
            customRightMarginMm = 20f
        )

        // Create enough content to naturally span multiple pages
        val blocks = mutableListOf<ParagraphBlock>()
        for (i in 1..25) {
            blocks.add(
                ParagraphBlock(
                    text = "Paragraph $i: Detailed analytical observations across multi-page Microsoft Word document formatting tests.",
                    spaceBeforePt = 4f,
                    spaceAfterPt = 8f
                )
            )
        }

        val doc = RichDocument(
            title = "Multi-Page Report",
            layoutConfig = customLayout,
            blocks = blocks
        )

        val out = ByteArrayOutputStream()
        writer.write(doc, out)

        val result = reader.read(ByteArrayInputStream(out.toByteArray()))
        assertTrue(result is DocumentResult.Success)
        val imported = (result as DocumentResult.Success).data.document

        assertEquals(30f, imported.layoutConfig.topMarginMm, 1f)
        assertEquals(30f, imported.layoutConfig.bottomMarginMm, 1f)
        assertEquals(40f, imported.layoutConfig.leftMarginMm, 1f)
        assertEquals(20f, imported.layoutConfig.rightMarginMm, 1f)

        val pages = PaginationEngine.paginate(imported)
        assertTrue("Document must paginate into multiple pages", pages.size >= 2)
        assertEquals(PageSize.LETTER, pages[0].layout.pageSize)
    }

    // =========================================================================
    // 3. Tables With Custom Column Widths, Borders, Shading and Images
    // =========================================================================
    @Test
    fun testTablesAndImagesFidelity() {
        val table = TableBlock(
            rowCount = 2,
            columnCount = 3,
            columnWidthWeights = listOf(0.2f, 0.5f, 0.3f), // 20%, 50%, 30%
            hasBorders = true,
            borderColorHex = "#475569",
            cells = listOf(
                listOf(
                    TableCell(text = "ID", isBold = true, backgroundColorHex = "#E2E8F0"),
                    TableCell(text = "Description", isBold = true, backgroundColorHex = "#E2E8F0"),
                    TableCell(text = "Status", isBold = true, backgroundColorHex = "#E2E8F0")
                ),
                listOf(
                    TableCell(text = "101"),
                    TableCell(text = "System Integration Testing"),
                    TableCell(text = "Completed")
                )
            )
        )

        val dummyImageFile = File(context.cacheDir, "test_fidelity_img.png").apply {
            writeBytes(ByteArray(128) { 0xFF.toByte() })
        }

        val image = ImageBlock(
            imageUri = dummyImageFile.absolutePath,
            caption = "System Architecture Diagram",
            widthDp = 320,
            heightDp = 200,
            aspectRatio = 1.6f
        )

        val doc = RichDocument(
            title = "Structured Specification",
            blocks = listOf(
                ParagraphBlock(text = "Tables & Images Showcase", headingStyle = HeadingStyle.HEADING_1),
                table,
                image
            )
        )

        val out = ByteArrayOutputStream()
        writer.write(doc, out)

        val result = reader.read(ByteArrayInputStream(out.toByteArray()))
        assertTrue(result is DocumentResult.Success)
        val imported = (result as DocumentResult.Success).data.document

        val importedTable = imported.blocks.filterIsInstance<TableBlock>().firstOrNull()
        assertNotNull("Table must be present", importedTable)
        assertEquals(3, importedTable!!.columnCount)
        assertEquals(3, importedTable.columnWidthWeights.size)
        // Check relative weights: col 1 should be roughly 20%, col 2 roughly 50%
        assertTrue("Col 1 width weight around 0.2", importedTable.columnWidthWeights[0] in 0.15f..0.25f)
        assertTrue("Col 2 width weight around 0.5", importedTable.columnWidthWeights[1] in 0.45f..0.55f)
        assertEquals("#E2E8F0", importedTable.cells[0][0].backgroundColorHex)

        val importedImage = imported.blocks.filterIsInstance<ImageBlock>().firstOrNull()
        assertNotNull("Image must be present", importedImage)
        assertEquals(320, importedImage!!.widthDp)
        assertEquals(200, importedImage.heightDp)

        dummyImageFile.delete()
    }

    // =========================================================================
    // 4. Headers, Footers, and Page Numbers
    // =========================================================================
    @Test
    fun testHeadersFootersAndPageNumbersFidelity() {
        val layout = PageLayoutConfig(
            pageSize = PageSize.A4,
            showHeader = true,
            headerText = "ACME Corporation Confidential",
            showFooter = true,
            footerText = "Page {page} of {pages}"
        )

        val doc = RichDocument(
            title = "Header Footer Document",
            layoutConfig = layout,
            blocks = listOf(
                ParagraphBlock(text = "Content page 1"),
                PageBreakBlock(),
                ParagraphBlock(text = "Content page 2")
            )
        )

        val out = ByteArrayOutputStream()
        writer.write(doc, out)

        val result = reader.read(ByteArrayInputStream(out.toByteArray()))
        assertTrue(result is DocumentResult.Success)
        val imported = (result as DocumentResult.Success).data.document

        assertTrue(imported.layoutConfig.showHeader)
        assertEquals("ACME Corporation Confidential", imported.layoutConfig.headerText)
        assertTrue(imported.layoutConfig.showFooter)

        val pages = PaginationEngine.paginate(imported)
        assertEquals(2, pages.size)
        assertEquals("ACME Corporation Confidential", pages[0].formattedHeader)
        assertEquals("Page 1 of 2", pages[0].formattedFooter)
        assertEquals("Page 2 of 2", pages[1].formattedFooter)
    }

    // =========================================================================
    // 5. Urdu Text with Nastaliq Font, RTL Direction, and Complex Script Sizes
    // =========================================================================
    @Test
    fun testUrduNastaliqFontAndRtlFidelity() {
        val urduText = "یہ ایک جامع دستاویز ہے جو مائیکروسافٹ ورڈ کی مطابقت کے لیے بنائی گئی ہے۔"
        val doc = RichDocument(
            title = "Urdu Nastaliq Test",
            blocks = listOf(
                ParagraphBlock(
                    text = urduText,
                    isRtl = true,
                    alignment = TextAlignment.END,
                    spans = listOf(
                        InlineSpan(
                            start = 0,
                            end = urduText.length,
                            fontFamilyName = "Noto Nastaliq Urdu",
                            fontSizeSp = 18f
                        )
                    )
                )
            )
        )

        val out = ByteArrayOutputStream()
        writer.write(doc, out)

        val result = reader.read(ByteArrayInputStream(out.toByteArray()))
        assertTrue(result is DocumentResult.Success)
        val imported = (result as DocumentResult.Success).data.document

        val p = imported.blocks[0] as ParagraphBlock
        assertTrue("Must preserve RTL flag", p.isRtl)
        assertEquals(urduText, p.text)
        val fontSpan = p.spans.firstOrNull()
        assertNotNull("Font span must be preserved", fontSpan)
        assertEquals("Noto Nastaliq Urdu", fontSpan!!.fontFamilyName)
        assertEquals(18f, fontSpan.fontSizeSp ?: 0f, 0.5f)

        // Verify typeface resolution
        val tf = DocumentFontManager.getTypeface(context, "Noto Nastaliq Urdu", true)
        assertNotNull("Typeface must be resolved", tf)
    }

    // =========================================================================
    // 6. Paper Sizes (Legal, A5, Letter, A4) & Page Borders
    // =========================================================================
    @Test
    fun testPaperSizesAndPageBordersFidelity() {
        // Legal paper test (8.5 x 14 in)
        val legalDoc = RichDocument(
            title = "Legal Brief",
            layoutConfig = PageLayoutConfig(
                pageSize = PageSize.LEGAL,
                orientation = PageOrientation.PORTRAIT,
                borderStyle = PageBorderStyle.DOUBLE,
                borderColorHex = "#1E293B"
            ),
            blocks = listOf(ParagraphBlock(text = "Legal Contract Clauses"))
        )

        val legalOut = ByteArrayOutputStream()
        writer.write(legalDoc, legalOut)

        val legalResult = reader.read(ByteArrayInputStream(legalOut.toByteArray()))
        assertTrue(legalResult is DocumentResult.Success)
        val importedLegal = (legalResult as DocumentResult.Success).data.document

        assertEquals("Must recognize LEGAL paper size distinct from LETTER", PageSize.LEGAL, importedLegal.layoutConfig.pageSize)
        assertEquals(PageBorderStyle.DOUBLE, importedLegal.layoutConfig.borderStyle)

        // A5 paper test
        val a5Doc = RichDocument(
            title = "Pocket Booklet",
            layoutConfig = PageLayoutConfig(pageSize = PageSize.A5),
            blocks = listOf(ParagraphBlock(text = "A5 Pocket Guide"))
        )
        val a5Out = ByteArrayOutputStream()
        writer.write(a5Doc, a5Out)
        val a5Result = reader.read(ByteArrayInputStream(a5Out.toByteArray()))
        assertTrue(a5Result is DocumentResult.Success)
        assertEquals(PageSize.A5, (a5Result as DocumentResult.Success).data.document.layoutConfig.pageSize)
    }

    // =========================================================================
    // 7. PDF and Print Layout Consistency
    // =========================================================================
    @Test
    fun testPdfExportAndPrintLayoutConsistency() {
        val doc = RichDocument(
            title = "Consistency Check",
            layoutConfig = PageLayoutConfig(
                pageSize = PageSize.A4,
                orientation = PageOrientation.PORTRAIT,
                marginPreset = MarginPreset.NORMAL
            ),
            blocks = listOf(
                ParagraphBlock(text = "Heading", headingStyle = HeadingStyle.HEADING_1),
                ParagraphBlock(text = "First paragraph content."),
                PageBreakBlock(),
                ParagraphBlock(text = "Second page content after page break.")
            )
        )

        // Paginate using PaginationEngine
        val previewPages = PaginationEngine.paginate(doc)
        assertEquals(2, previewPages.size)

        // Export to PDF
        val pdfFile = File(context.cacheDir, "test_consistency_${UUID.randomUUID()}.pdf")
        val exportJob = kotlinx.coroutines.runBlocking {
            pdfExportService.exportToPdf(context, doc, pdfFile)
        }
        assertTrue("PDF export must succeed", exportJob is DocumentResult.Success)
        assertTrue("Exported PDF file must exist and contain data", pdfFile.exists() && pdfFile.length() > 50)

        pdfFile.delete()
    }
}
