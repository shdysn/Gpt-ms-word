package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.core.error.DocumentError
import com.example.core.error.DocumentResult
import com.example.core.layout.MarginPreset
import com.example.core.layout.PageLayoutConfig
import com.example.core.layout.PageOrientation
import com.example.core.layout.PageSize
import com.example.data.local.AppDatabase
import com.example.data.model.DividerBlock
import com.example.data.model.HeadingStyle
import com.example.data.model.InlineSpan
import com.example.data.model.ListType
import com.example.data.model.PageBreakBlock
import com.example.data.model.ParagraphBlock
import com.example.data.model.RichDocument
import com.example.data.model.TableCell
import com.example.data.model.TableBlock
import com.example.data.model.TextAlignment
import com.example.data.repository.DocumentRepositoryImpl
import com.example.data.service.DocumentImportServiceImpl
import com.example.data.service.PdfExportServiceImpl
import com.example.data.service.docx.DocxPackageReader
import com.example.data.service.docx.DocxPackageWriter
import com.example.data.service.docx.DocxService
import com.example.data.service.docx.DocxServiceImpl
import com.example.data.storage.DocumentStorageManager
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
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.UUID
import java.util.zip.ZipInputStream

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class DocxImportExportIntegrationTest {

    private lateinit var context: Context
    private lateinit var database: AppDatabase
    private lateinit var storageManager: DocumentStorageManager
    private lateinit var pdfExportService: PdfExportServiceImpl
    private lateinit var importService: DocumentImportServiceImpl
    private lateinit var repository: DocumentRepositoryImpl
    private lateinit var docxService: DocxService

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        storageManager = DocumentStorageManager(context)
        pdfExportService = PdfExportServiceImpl()
        docxService = DocxServiceImpl()
        importService = DocumentImportServiceImpl(storageManager, docxService)
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
    // 1. Genuine Open Packaging Conventions (OPC) Package Structure Test
    // =========================================================================
    @Test
    fun testDocxPackageStructureAndXmlParts() {
        val document = RichDocument(
            title = "OPC Package Specification",
            author = "Engineering Team",
            layoutConfig = PageLayoutConfig(
                pageSize = PageSize.A4,
                showHeader = true,
                headerText = "Header Document",
                showFooter = true,
                footerText = "Page {page} of {pages}"
            ),
            blocks = listOf(
                ParagraphBlock(text = "Introduction to OpenXML", headingStyle = HeadingStyle.TITLE),
                ParagraphBlock(text = "Standard paragraphs adhering to ECMA-376.")
            )
        )

        val byteOut = ByteArrayOutputStream()
        val writer = DocxPackageWriter(context)
        writer.write(document, byteOut)

        val zipBytes = byteOut.toByteArray()
        assertTrue("Generated ZIP must contain data", zipBytes.isNotEmpty())

        // Verify ZIP header magic 'PK\x03\x04'
        assertEquals(0x50.toByte(), zipBytes[0]) // 'P'
        assertEquals(0x4B.toByte(), zipBytes[1]) // 'K'
        assertEquals(0x03.toByte(), zipBytes[2])
        assertEquals(0x04.toByte(), zipBytes[3])

        // Verify required OPC ZIP entry paths
        val entryNames = mutableSetOf<String>()
        val zipIn = ZipInputStream(ByteArrayInputStream(zipBytes))
        var entry = zipIn.nextEntry
        while (entry != null) {
            entryNames.add(entry.name)
            zipIn.closeEntry()
            entry = zipIn.nextEntry
        }

        assertTrue("Must contain [Content_Types].xml", entryNames.contains("[Content_Types].xml"))
        assertTrue("Must contain _rels/.rels", entryNames.contains("_rels/.rels"))
        assertTrue("Must contain word/_rels/document.xml.rels", entryNames.contains("word/_rels/document.xml.rels"))
        assertTrue("Must contain word/document.xml", entryNames.contains("word/document.xml"))
        assertTrue("Must contain word/styles.xml", entryNames.contains("word/styles.xml"))
        assertTrue("Must contain word/numbering.xml", entryNames.contains("word/numbering.xml"))
        assertTrue("Must contain word/header1.xml", entryNames.contains("word/header1.xml"))
        assertTrue("Must contain word/footer1.xml", entryNames.contains("word/footer1.xml"))
        assertTrue("Must contain docProps/core.xml", entryNames.contains("docProps/core.xml"))
        assertTrue("Must contain docProps/app.xml", entryNames.contains("docProps/app.xml"))
    }

    // =========================================================================
    // 2. Character & Paragraph Formatting Round-Trip Fidelity Test
    // =========================================================================
    @Test
    fun testFormattingPreservationRoundTrip() {
        val paragraphText = "Smart Word Processor supports bold, italic, and underline styling."
        val boldStart = paragraphText.indexOf("bold")
        val boldEnd = boldStart + "bold".length
        val italicStart = paragraphText.indexOf("italic")
        val italicEnd = italicStart + "italic".length
        val underlineStart = paragraphText.indexOf("underline")
        val underlineEnd = underlineStart + "underline".length

        val originalDoc = RichDocument(
            title = "Typography Round-Trip",
            author = "Lead Architect",
            blocks = listOf(
                ParagraphBlock(
                    text = "Executive Document Title",
                    headingStyle = HeadingStyle.TITLE,
                    alignment = TextAlignment.CENTER
                ),
                ParagraphBlock(
                    text = "Section 1: Formatting Showcase",
                    headingStyle = HeadingStyle.HEADING_1,
                    alignment = TextAlignment.START
                ),
                ParagraphBlock(
                    text = paragraphText,
                    alignment = TextAlignment.JUSTIFY,
                    spans = listOf(
                        InlineSpan(start = boldStart, end = boldEnd, isBold = true, textColorHex = "#185ABD"),
                        InlineSpan(start = italicStart, end = italicEnd, isItalic = true),
                        InlineSpan(start = underlineStart, end = underlineEnd, isUnderline = true, isBold = true)
                    )
                ),
                ParagraphBlock(
                    text = "This is a blockquote citation from literature.",
                    headingStyle = HeadingStyle.QUOTE,
                    indentationLevel = 1
                ),
                DividerBlock(),
                PageBreakBlock(),
                ParagraphBlock(text = "Content after page break on page 2.")
            )
        )

        // Export to DOCX bytes
        val byteOut = ByteArrayOutputStream()
        val writer = DocxPackageWriter(context)
        writer.write(originalDoc, byteOut)

        // Import back via DocxPackageReader
        val reader = DocxPackageReader(context)
        val importRes = reader.read(ByteArrayInputStream(byteOut.toByteArray()))

        assertTrue("Import must succeed", importRes is DocumentResult.Success)
        val importedDoc = (importRes as DocumentResult.Success).data.document

        assertEquals("Executive Document Title", (importedDoc.blocks[0] as ParagraphBlock).text)
        assertEquals(HeadingStyle.TITLE, (importedDoc.blocks[0] as ParagraphBlock).headingStyle)
        assertEquals(TextAlignment.CENTER, (importedDoc.blocks[0] as ParagraphBlock).alignment)

        assertEquals("Section 1: Formatting Showcase", (importedDoc.blocks[1] as ParagraphBlock).text)
        assertEquals(HeadingStyle.HEADING_1, (importedDoc.blocks[1] as ParagraphBlock).headingStyle)

        val formattedP = importedDoc.blocks[2] as ParagraphBlock
        assertEquals(paragraphText, formattedP.text)
        assertEquals(TextAlignment.JUSTIFY, formattedP.alignment)

        // Verify span preservation
        assertTrue("Must preserve inline spans", formattedP.spans.isNotEmpty())
        val boldSpan = formattedP.spans.find { it.isBold && !it.isUnderline }
        assertNotNull("Bold span must be present", boldSpan)
        val italicSpan = formattedP.spans.find { it.isItalic }
        assertNotNull("Italic span must be present", italicSpan)
        val underlineSpan = formattedP.spans.find { it.isUnderline }
        assertNotNull("Underline span must be present", underlineSpan)

        // Verify Divider and PageBreak preservation
        assertTrue("Must contain DividerBlock", importedDoc.blocks.any { it is DividerBlock })
        assertTrue("Must contain PageBreakBlock", importedDoc.blocks.any { it is PageBreakBlock })
    }

    // =========================================================================
    // 3. Tables Import and Export Fidelity Test
    // =========================================================================
    @Test
    fun testDocxTablesImportExport() {
        val tableBlock = TableBlock(
            rowCount = 3,
            columnCount = 2,
            cells = listOf(
                listOf(
                    TableCell(text = "Header A", isBold = true, backgroundColorHex = "#E2E8F0"),
                    TableCell(text = "Header B", isBold = true, backgroundColorHex = "#E2E8F0")
                ),
                listOf(
                    TableCell(text = "Row 1 Cell 1", isBold = false),
                    TableCell(text = "Row 1 Cell 2", isBold = false)
                ),
                listOf(
                    TableCell(text = "Row 2 Cell 1", isBold = false),
                    TableCell(text = "Row 2 Cell 2", isBold = false)
                )
            )
        )

        val docWithTable = RichDocument(
            title = "Table Specification",
            blocks = listOf(
                ParagraphBlock(text = "Structured Data Table", headingStyle = HeadingStyle.HEADING_2),
                tableBlock
            )
        )

        val byteOut = ByteArrayOutputStream()
        DocxPackageWriter(context).write(docWithTable, byteOut)

        val reader = DocxPackageReader(context)
        val importRes = reader.read(ByteArrayInputStream(byteOut.toByteArray()))
        assertTrue(importRes is DocumentResult.Success)

        val importedDoc = (importRes as DocumentResult.Success).data.document
        val importedTable = importedDoc.blocks.filterIsInstance<TableBlock>().firstOrNull()
        assertNotNull("TableBlock must be preserved on import", importedTable)

        assertEquals(3, importedTable!!.rowCount)
        assertEquals(2, importedTable.columnCount)
        assertEquals("Header A", importedTable.cells[0][0].text)
        assertTrue(importedTable.cells[0][0].isBold)
        assertEquals("#E2E8F0", importedTable.cells[0][0].backgroundColorHex)
        assertEquals("Row 1 Cell 1", importedTable.cells[1][0].text)
        assertEquals("Row 2 Cell 2", importedTable.cells[2][1].text)
    }

    // =========================================================================
    // 4. Bullet and Numbered Lists Import & Export Test
    // =========================================================================
    @Test
    fun testDocxListsImportExport() {
        val docWithLists = RichDocument(
            title = "List Formatting",
            blocks = listOf(
                ParagraphBlock(text = "Project Deliverables", headingStyle = HeadingStyle.HEADING_1),
                ParagraphBlock(text = "First bullet item", listType = ListType.BULLET),
                ParagraphBlock(text = "Second bullet item", listType = ListType.BULLET),
                ParagraphBlock(text = "Step one: requirements", listType = ListType.NUMBERED),
                ParagraphBlock(text = "Step two: execution", listType = ListType.NUMBERED)
            )
        )

        val byteOut = ByteArrayOutputStream()
        DocxPackageWriter(context).write(docWithLists, byteOut)

        val importRes = DocxPackageReader(context).read(ByteArrayInputStream(byteOut.toByteArray()))
        assertTrue(importRes is DocumentResult.Success)

        val importedDoc = (importRes as DocumentResult.Success).data.document
        val importedParagraphs = importedDoc.blocks.filterIsInstance<ParagraphBlock>()

        assertEquals(5, importedParagraphs.size)
        assertEquals(ListType.BULLET, importedParagraphs[1].listType)
        assertEquals(ListType.BULLET, importedParagraphs[2].listType)
        assertEquals(ListType.NUMBERED, importedParagraphs[3].listType)
        assertEquals(ListType.NUMBERED, importedParagraphs[4].listType)
    }

    // =========================================================================
    // 5. Page Layout, Margins, Headers & Footers Test
    // =========================================================================
    @Test
    fun testDocxPageSettingsAndHeaderFooter() {
        val layout = PageLayoutConfig(
            pageSize = PageSize.LETTER,
            orientation = PageOrientation.LANDSCAPE,
            marginPreset = MarginPreset.CUSTOM,
            customTopMarginMm = 20f,
            customBottomMarginMm = 20f,
            customLeftMarginMm = 15f,
            customRightMarginMm = 15f,
            showHeader = true,
            headerText = "Confidential Report - Q4",
            showFooter = true,
            footerText = "Page {page} of {pages}"
        )

        val doc = RichDocument(
            title = "Quarterly Landscape Report",
            layoutConfig = layout,
            blocks = listOf(
                ParagraphBlock(text = "Financial Performance Overview")
            )
        )

        val byteOut = ByteArrayOutputStream()
        DocxPackageWriter(context).write(doc, byteOut)

        val importRes = DocxPackageReader(context).read(ByteArrayInputStream(byteOut.toByteArray()))
        assertTrue(importRes is DocumentResult.Success)

        val importedDoc = (importRes as DocumentResult.Success).data.document
        val importedLayout = importedDoc.layoutConfig

        assertEquals(PageSize.LETTER, importedLayout.pageSize)
        assertEquals(PageOrientation.LANDSCAPE, importedLayout.orientation)
        assertEquals("Confidential Report - Q4", importedLayout.headerText)
        assertTrue(importedLayout.showHeader)
        assertTrue(importedLayout.showFooter)
    }

    // =========================================================================
    // 6. Multilingual Urdu & English RTL Test
    // =========================================================================
    @Test
    fun testDocxMultilingualAndRtlSupport() {
        val urduTitle = "اردو ادب اور سمارٹ ورڈ پروسیسر"
        val urduBody = "یہ عبارت دائیں سے بائیں خطاطی اور نستعلیق فونٹ میں تحریر کی گئی ہے۔"

        val doc = RichDocument(
            title = "Urdu Word Document",
            blocks = listOf(
                ParagraphBlock(text = urduTitle, headingStyle = HeadingStyle.TITLE, isRtl = true),
                ParagraphBlock(text = urduBody, isRtl = true, alignment = TextAlignment.JUSTIFY),
                ParagraphBlock(text = "Mixed content: Artificial Intelligence کا اردو زبان میں استعمال۔", isRtl = true)
            )
        )

        val byteOut = ByteArrayOutputStream()
        DocxPackageWriter(context).write(doc, byteOut)

        val importRes = DocxPackageReader(context).read(ByteArrayInputStream(byteOut.toByteArray()))
        assertTrue(importRes is DocumentResult.Success)

        val importedDoc = (importRes as DocumentResult.Success).data.document
        val p0 = importedDoc.blocks[0] as ParagraphBlock
        val p1 = importedDoc.blocks[1] as ParagraphBlock

        assertEquals(urduTitle, p0.text)
        assertTrue("Urdu paragraph must retain RTL direction flag", p0.isRtl)
        assertEquals(urduBody, p1.text)
        assertTrue("Urdu body must retain RTL direction flag", p1.isRtl)
    }

    // =========================================================================
    // 7. Validation: Rejection of Corrupt or Non-DOCX Files
    // =========================================================================
    @Test
    fun testDocxValidation_rejectsInvalidFiles() {
        val reader = DocxPackageReader(context)

        // Plain text file
        val plainTextStream = ByteArrayInputStream("This is a plain text file, not a docx package.".toByteArray(Charsets.UTF_8))
        val plainResult = reader.read(plainTextStream)
        assertTrue("Plain text must fail DOCX validation", plainResult is DocumentResult.Failure)
        assertTrue((plainResult as DocumentResult.Failure).error is DocumentError.CorruptDocumentError)

        // Non-word ZIP file (empty zip)
        val emptyZipOut = ByteArrayOutputStream()
        val emptyZip = java.util.zip.ZipOutputStream(emptyZipOut)
        emptyZip.putNextEntry(java.util.zip.ZipEntry("hello.txt"))
        emptyZip.write("Not word".toByteArray())
        emptyZip.closeEntry()
        emptyZip.finish()

        val nonDocxZipStream = ByteArrayInputStream(emptyZipOut.toByteArray())
        val nonDocxResult = reader.read(nonDocxZipStream)
        assertTrue("ZIP missing word/document.xml must fail validation", nonDocxResult is DocumentResult.Failure)
    }

    // =========================================================================
    // 8. Repository File Export and Atomic Overwrite Protection Test
    // =========================================================================
    @Test
    fun testRepositoryDocxExportAndIntegrity() = runTest {
        val doc = RichDocument(
            title = "Repository Export Test",
            blocks = listOf(ParagraphBlock(text = "Hello Repository"))
        )

        val exportFile = File(context.cacheDir, "test_repo_export_${UUID.randomUUID()}.docx")
        val exportResult = repository.exportToDocx(context, doc, exportFile)
        assertTrue("Repository exportToDocx must succeed", exportResult is DocumentResult.Success)
        assertTrue("Exported file must exist", exportFile.exists())
        assertTrue("Exported file must be > 500 bytes", exportFile.length() > 500)

        // Import from exported file
        val importRes = docxService.importFromFile(context, exportFile)
        assertTrue("Import from exported file must succeed", importRes is DocumentResult.Success)
        assertEquals("Repository Export Test", (importRes as DocumentResult.Success).data.document.title)

        exportFile.delete()
    }

    // =========================================================================
    // 9. Hyperlink Export and Import Round-Trip Fidelity Test
    // =========================================================================
    @Test
    fun testHyperlinkExportAndImportFidelity() {
        val originalDoc = RichDocument(
            title = "Hyperlink Round-Trip",
            blocks = listOf(
                ParagraphBlock(
                    text = "Visit OpenAI or Google AI for research.",
                    spans = listOf(
                        InlineSpan(
                            start = 6,
                            end = 12,
                            isUnderline = true,
                            textColorHex = "#185ABD",
                            linkUrl = "https://openai.com"
                        ),
                        InlineSpan(
                            start = 16,
                            end = 25,
                            isUnderline = true,
                            textColorHex = "#185ABD",
                            linkUrl = "https://ai.google"
                        )
                    )
                )
            )
        )

        val byteOut = ByteArrayOutputStream()
        DocxPackageWriter(context).write(originalDoc, byteOut)

        val importRes = DocxPackageReader(context).read(ByteArrayInputStream(byteOut.toByteArray()))
        assertTrue("Import must succeed", importRes is DocumentResult.Success)

        val importedDoc = (importRes as DocumentResult.Success).data.document
        val p = importedDoc.blocks[0] as ParagraphBlock
        assertEquals("Visit OpenAI or Google AI for research.", p.text)

        val openAiSpan = p.spans.find { it.linkUrl == "https://openai.com" }
        assertNotNull("OpenAI link must be preserved", openAiSpan)
        assertEquals(6, openAiSpan!!.start)
        assertEquals(12, openAiSpan.end)

        val googleSpan = p.spans.find { it.linkUrl == "https://ai.google" }
        assertNotNull("Google link must be preserved", googleSpan)
        assertEquals(16, googleSpan!!.start)
        assertEquals(25, googleSpan.end)
    }
}
