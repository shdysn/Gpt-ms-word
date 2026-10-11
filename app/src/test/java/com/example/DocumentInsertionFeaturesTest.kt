package com.example

import android.content.Context
import android.net.Uri
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.core.error.DocumentResult
import com.example.core.layout.PageLayoutConfig
import com.example.core.layout.PageOrientation
import com.example.core.layout.PageSize
import com.example.data.local.AppDatabase
import com.example.data.model.DividerBlock
import com.example.data.model.ImageBlock
import com.example.data.model.InlineSpan
import com.example.data.model.PageBreakBlock
import com.example.data.model.ParagraphBlock
import com.example.data.model.RichDocument
import com.example.data.model.TableCell
import com.example.data.model.TableBlock
import com.example.data.model.TextAlignment
import com.example.data.repository.DocumentRepositoryImpl
import com.example.data.repository.TemplateType
import com.example.data.service.DocumentExportService
import com.example.data.service.DocumentImportService
import com.example.data.service.docx.DocxImportResult
import com.example.data.service.docx.DocxService
import com.example.data.service.docx.DocxServiceImpl
import com.example.data.storage.DocumentStorageManager
import com.example.editor.engine.UpdateImageBlockCommand
import com.example.editor.engine.UpdateTableBlockCommand
import kotlinx.coroutines.ExperimentalCoroutinesApi
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
import java.io.FileOutputStream
import java.util.UUID

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class DocumentInsertionFeaturesTest {

    private lateinit var context: Context
    private lateinit var database: AppDatabase
    private lateinit var storageManager: DocumentStorageManager
    private lateinit var repository: DocumentRepositoryImpl
    private lateinit var docxService: DocxService

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        storageManager = DocumentStorageManager(context)
        docxService = DocxServiceImpl()

        val dummyExportService = object : DocumentExportService {
            override suspend fun exportToPdf(context: Context, document: RichDocument, outputFile: File) =
                DocumentResult.Success(outputFile)
            override fun printDocument(context: Context, document: RichDocument, jobName: String) {}
            override suspend fun exportToDocx(context: Context, document: RichDocument, outputFile: File) =
                docxService.exportToDocx(context, document, outputFile)
            override suspend fun exportToPlainText(context: Context, document: RichDocument, outputFile: File) =
                DocumentResult.Success(outputFile)
            override suspend fun exportPdfToUri(context: Context, document: RichDocument, targetUri: Uri) =
                DocumentResult.Success(Unit)
            override suspend fun exportDocxToUri(context: Context, document: RichDocument, targetUri: Uri) =
                DocumentResult.Success(Unit)
        }
        val dummyImportService = object : DocumentImportService {
            override suspend fun importDocumentFromUri(context: Context, uri: Uri) =
                DocumentResult.Success(RichDocument())
            override suspend fun importDocxFromUri(context: Context, uri: Uri) =
                docxService.importFromUri(context, uri)
        }

        repository = DocumentRepositoryImpl(
            documentDao = database.documentDao(),
            storageManager = storageManager,
            exportService = dummyExportService,
            importService = dummyImportService
        )
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun testTableCreationAndStructuralModifications() {
        // 1. Initial 2x3 table
        val initialTable = TableBlock(
            rowCount = 2,
            columnCount = 3,
            cells = List(2) { r ->
                List(3) { c ->
                    TableCell(text = "R${r}C${c}", isBold = r == 0)
                }
            },
            hasBorders = true,
            borderColorHex = "#CBD5E1",
            tableWidthPercent = 100,
            alignment = TextAlignment.CENTER
        )

        assertEquals(2, initialTable.rowCount)
        assertEquals(3, initialTable.columnCount)
        assertEquals("R0C0", initialTable.cells[0][0].text)
        assertTrue(initialTable.cells[0][0].isBold)
        assertFalse(initialTable.cells[1][0].isBold)

        // 2. Add Row
        val newRow = List(initialTable.columnCount) { TableCell(text = "NewRow") }
        val tableWithNewRow = initialTable.copy(
            rowCount = initialTable.rowCount + 1,
            cells = initialTable.cells + listOf(newRow)
        )
        assertEquals(3, tableWithNewRow.rowCount)
        assertEquals("NewRow", tableWithNewRow.cells[2][0].text)

        // 3. Delete Row
        val tableAfterDeleteRow = tableWithNewRow.copy(
            rowCount = tableWithNewRow.rowCount - 1,
            cells = tableWithNewRow.cells.dropLast(1)
        )
        assertEquals(2, tableAfterDeleteRow.rowCount)

        // 4. Add Column
        val tableWithNewCol = tableAfterDeleteRow.copy(
            columnCount = tableAfterDeleteRow.columnCount + 1,
            cells = tableAfterDeleteRow.cells.map { row ->
                row + TableCell(text = "ColAdded")
            }
        )
        assertEquals(4, tableWithNewCol.columnCount)
        assertEquals("ColAdded", tableWithNewCol.cells[0][3].text)

        // 5. Delete Column
        val tableAfterDeleteCol = tableWithNewCol.copy(
            columnCount = tableWithNewCol.columnCount - 1,
            cells = tableWithNewCol.cells.map { row ->
                row.dropLast(1)
            }
        )
        assertEquals(3, tableAfterDeleteCol.columnCount)
    }

    @Test
    fun testTableCellFormattingAndStyling() {
        val cell = TableCell(
            text = "Total Revenue",
            isBold = true,
            isItalic = true,
            backgroundColorHex = "#E0F2FE",
            alignment = TextAlignment.END
        )

        assertEquals("Total Revenue", cell.text)
        assertTrue(cell.isBold)
        assertTrue(cell.isItalic)
        assertEquals("#E0F2FE", cell.backgroundColorHex)
        assertEquals(TextAlignment.END, cell.alignment)
    }

    @Test
    fun testTablePersistenceSaveAndReload() = runTest {
        val testDocId = UUID.randomUUID().toString()
        val customTable = TableBlock(
            rowCount = 3,
            columnCount = 2,
            cells = listOf(
                listOf(
                    TableCell(text = "Item", isBold = true, backgroundColorHex = "#F1F5F9", alignment = TextAlignment.START),
                    TableCell(text = "Amount ($)", isBold = true, backgroundColorHex = "#F1F5F9", alignment = TextAlignment.END)
                ),
                listOf(
                    TableCell(text = "Consulting", isBold = false, isItalic = false, alignment = TextAlignment.START),
                    TableCell(text = "1,500.00", isBold = false, isItalic = false, alignment = TextAlignment.END)
                ),
                listOf(
                    TableCell(text = "Development", isBold = false, isItalic = false, alignment = TextAlignment.START),
                    TableCell(text = "3,250.00", isBold = false, isItalic = false, alignment = TextAlignment.END)
                )
            ),
            hasBorders = true,
            borderColorHex = "#185ABD",
            tableWidthPercent = 75,
            alignment = TextAlignment.CENTER
        )

        val doc = RichDocument(
            id = testDocId,
            title = "Financial Summary",
            blocks = listOf(
                ParagraphBlock(text = "Financial Report Summary", headingStyle = com.example.data.model.HeadingStyle.HEADING_1),
                customTable
            )
        )

        // Save
        val saveResult = storageManager.saveToInternalStorage(doc)
        assertTrue("Save should succeed", saveResult is DocumentResult.Success)

        // Load
        val loadResult = storageManager.loadFromInternalStorage(testDocId)
        assertTrue("Load should succeed", loadResult is DocumentResult.Success)
        val loadedDoc = (loadResult as DocumentResult.Success<RichDocument>).data

        assertEquals("Financial Summary", loadedDoc.title)
        assertEquals(2, loadedDoc.blocks.size)

        val loadedTable = loadedDoc.blocks[1] as TableBlock
        assertEquals(3, loadedTable.rowCount)
        assertEquals(2, loadedTable.columnCount)
        assertEquals(75, loadedTable.tableWidthPercent)
        assertEquals(TextAlignment.CENTER, loadedTable.alignment)
        assertTrue(loadedTable.hasBorders)
        assertEquals("#185ABD", loadedTable.borderColorHex)

        // Verify cell formatting was fully preserved
        val headerItem = loadedTable.cells[0][0]
        assertEquals("Item", headerItem.text)
        assertTrue(headerItem.isBold)
        assertEquals("#F1F5F9", headerItem.backgroundColorHex)
        assertEquals(TextAlignment.START, headerItem.alignment)

        val headerAmount = loadedTable.cells[0][1]
        assertEquals("Amount ($)", headerAmount.text)
        assertEquals(TextAlignment.END, headerAmount.alignment)

        val row1Val = loadedTable.cells[1][1]
        assertEquals("1,500.00", row1Val.text)
        assertFalse(row1Val.isBold)
        assertEquals(TextAlignment.END, row1Val.alignment)
    }

    @Test
    fun testImagePersistenceAndProperties() = runTest {
        // Create a test image file
        val tempDir = File(context.cacheDir, "test_images").apply { mkdirs() }
        val dummyImageFile = File(tempDir, "sample_photo.png")
        FileOutputStream(dummyImageFile).use { out ->
            out.write("fake_png_binary_data_header_bytes".toByteArray())
        }

        val sourceUri = Uri.fromFile(dummyImageFile)
        val persistentUriString = storageManager.persistImageFromUri(sourceUri)
        assertTrue("Persistent image URI should be created", persistentUriString.isNotBlank())
        assertTrue("Image should be stored in internal images dir", persistentUriString.contains("images"))

        val imageBlock = ImageBlock(
            imageUri = persistentUriString,
            caption = "Product Prototype 2026",
            widthDp = 300,
            heightDp = 200,
            alignment = TextAlignment.CENTER,
            aspectRatio = 1.5f
        )

        val testDocId = UUID.randomUUID().toString()
        val doc = RichDocument(
            id = testDocId,
            title = "Design Specification",
            blocks = listOf(
                ParagraphBlock(text = "Specification document"),
                imageBlock
            )
        )

        // Save
        val saveResult = storageManager.saveToInternalStorage(doc)
        assertTrue(saveResult is DocumentResult.Success)

        // Reload
        val loadResult = storageManager.loadFromInternalStorage(testDocId)
        assertTrue(loadResult is DocumentResult.Success)
        val loadedDoc = (loadResult as DocumentResult.Success<RichDocument>).data

        assertEquals(2, loadedDoc.blocks.size)
        val loadedImage = loadedDoc.blocks[1] as ImageBlock
        assertEquals(persistentUriString, loadedImage.imageUri)
        assertEquals("Product Prototype 2026", loadedImage.caption)
        assertEquals(300, loadedImage.widthDp)
        assertEquals(200, loadedImage.heightDp)
        assertEquals(TextAlignment.CENTER, loadedImage.alignment)
        assertEquals(1.5f, loadedImage.aspectRatio, 0.01f)
    }

    @Test
    fun testDividerAndPageBreakPersistence() = runTest {
        val testDocId = UUID.randomUUID().toString()
        val doc = RichDocument(
            id = testDocId,
            title = "Multi-Section Document",
            blocks = listOf(
                ParagraphBlock(text = "Section 1"),
                DividerBlock(),
                ParagraphBlock(text = "Section 1 Subtext"),
                PageBreakBlock(),
                ParagraphBlock(text = "Section 2 on Page 2")
            )
        )

        val saveResult = storageManager.saveToInternalStorage(doc)
        assertTrue(saveResult is DocumentResult.Success)

        val loadResult = storageManager.loadFromInternalStorage(testDocId)
        assertTrue(loadResult is DocumentResult.Success)
        val loadedDoc = (loadResult as DocumentResult.Success<RichDocument>).data

        assertEquals(5, loadedDoc.blocks.size)
        assertTrue(loadedDoc.blocks[1] is DividerBlock)
        assertTrue(loadedDoc.blocks[3] is PageBreakBlock)
    }

    @Test
    fun testHeaderFooterAndPageNumberingPersistence() = runTest {
        val testDocId = UUID.randomUUID().toString()
        val layout = PageLayoutConfig(
            pageSize = PageSize.A4,
            orientation = PageOrientation.PORTRAIT,
            showHeader = true,
            showFooter = true,
            headerText = "Confidential - Company Internal",
            footerText = "Page {page} of {pages}",
            headerAlignment = TextAlignment.START,
            footerAlignment = TextAlignment.CENTER
        )

        val doc = RichDocument(
            id = testDocId,
            title = "Report with Headers",
            layoutConfig = layout,
            blocks = listOf(ParagraphBlock(text = "Document body"))
        )

        val saveResult = storageManager.saveToInternalStorage(doc)
        assertTrue(saveResult is DocumentResult.Success)

        val loadResult = storageManager.loadFromInternalStorage(testDocId)
        assertTrue(loadResult is DocumentResult.Success)
        val loadedDoc = (loadResult as DocumentResult.Success<RichDocument>).data

        assertTrue(loadedDoc.layoutConfig.showHeader)
        assertTrue(loadedDoc.layoutConfig.showFooter)
        assertEquals("Confidential - Company Internal", loadedDoc.layoutConfig.headerText)
        assertEquals("Page {page} of {pages}", loadedDoc.layoutConfig.footerText)
        assertEquals(TextAlignment.START, loadedDoc.layoutConfig.headerAlignment)
        assertEquals(TextAlignment.CENTER, loadedDoc.layoutConfig.footerAlignment)
    }

    @Test
    fun testHyperlinkFormattingPersistence() = runTest {
        val testDocId = UUID.randomUUID().toString()
        val paragraphWithLink = ParagraphBlock(
            text = "Visit our website for more details.",
            spans = listOf(
                InlineSpan(
                    start = 10,
                    end = 17,
                    isUnderline = true,
                    linkUrl = "https://www.example.com",
                    textColorHex = "#185ABD"
                )
            )
        )

        val doc = RichDocument(
            id = testDocId,
            title = "Hyperlink Document",
            blocks = listOf(paragraphWithLink)
        )

        val saveResult = storageManager.saveToInternalStorage(doc)
        assertTrue(saveResult is DocumentResult.Success)

        val loadResult = storageManager.loadFromInternalStorage(testDocId)
        assertTrue(loadResult is DocumentResult.Success)
        val loadedDoc = (loadResult as DocumentResult.Success<RichDocument>).data

        val loadedPara = loadedDoc.blocks[0] as ParagraphBlock
        assertEquals(1, loadedPara.spans.size)
        val span = loadedPara.spans[0]
        assertEquals("https://www.example.com", span.linkUrl)
        assertTrue(span.isUnderline)
        assertEquals("#185ABD", span.textColorHex)
    }

    @Test
    fun testTableDocxExportAndImportIntegrity() = runTest {
        val table = TableBlock(
            rowCount = 2,
            columnCount = 2,
            cells = listOf(
                listOf(
                    TableCell(text = "Quarter", isBold = true, alignment = TextAlignment.START),
                    TableCell(text = "Growth", isBold = true, alignment = TextAlignment.END)
                ),
                listOf(
                    TableCell(text = "Q1 2026", isBold = false, alignment = TextAlignment.START),
                    TableCell(text = "+14.8%", isBold = false, alignment = TextAlignment.END)
                )
            ),
            hasBorders = true
        )

        val doc = RichDocument(
            title = "Docx Table Export Test",
            blocks = listOf(
                ParagraphBlock(text = "Introductory Statement"),
                table
            )
        )

        val exportFile = File(context.cacheDir, "test_table_export.docx")
        val exportResult = docxService.exportToDocx(context, doc, exportFile)
        assertTrue("Docx export should succeed", exportResult is DocumentResult.Success)
        assertTrue("Exported file should exist and be non-empty", exportFile.exists() && exportFile.length() > 0)

        // Read back the exported DOCX
        val importResult = docxService.importFromUri(context, Uri.fromFile(exportFile))
        assertTrue("Docx import should succeed", importResult is DocumentResult.Success)
        val importedDoc = (importResult as DocumentResult.Success<DocxImportResult>).data.document

        val importedTable = importedDoc.blocks.filterIsInstance<TableBlock>().firstOrNull()
        assertNotNull("Imported doc should contain the exported table", importedTable)
        assertEquals(2, importedTable!!.rowCount)
        assertEquals(2, importedTable.columnCount)
        assertEquals("Quarter", importedTable.cells[0][0].text)
        assertEquals("Growth", importedTable.cells[0][1].text)
        assertEquals("Q1 2026", importedTable.cells[1][0].text)
        assertEquals("+14.8%", importedTable.cells[1][1].text)
    }

    @Test
    fun testEditorCommandUndoRedoForTable() {
        val oldTable = TableBlock(
            rowCount = 1,
            columnCount = 1,
            cells = listOf(listOf(TableCell(text = "Original")))
        )
        val newTable = oldTable.copy(
            rowCount = 2,
            columnCount = 1,
            cells = listOf(
                listOf(TableCell(text = "Original")),
                listOf(TableCell(text = "Appended Row"))
            )
        )

        val doc = RichDocument(blocks = listOf(oldTable))
        val command = UpdateTableBlockCommand(
            blockId = oldTable.id,
            oldTable = oldTable,
            newTable = newTable,
            actionDescription = "Add Row"
        )

        // Apply command
        val docAfterApply = command.apply(doc)
        val appliedTable = docAfterApply.blocks[0] as TableBlock
        assertEquals(2, appliedTable.rowCount)
        assertEquals("Appended Row", appliedTable.cells[1][0].text)

        // Revert command
        val docAfterRevert = command.revert(docAfterApply)
        val revertedTable = docAfterRevert.blocks[0] as TableBlock
        assertEquals(1, revertedTable.rowCount)
        assertEquals("Original", revertedTable.cells[0][0].text)
    }
}
