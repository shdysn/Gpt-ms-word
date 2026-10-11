package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.core.error.DocumentResult
import com.example.core.layout.MarginPreset
import com.example.core.layout.PageLayoutConfig
import com.example.core.layout.PageOrientation
import com.example.core.layout.PageSize
import com.example.data.local.AppDatabase
import com.example.data.model.HeadingStyle
import com.example.data.model.InlineSpan
import com.example.data.model.ListType
import com.example.data.model.ParagraphBlock
import com.example.data.model.RichDocument
import com.example.data.model.TableCell
import com.example.data.model.TableBlock
import com.example.data.model.TextAlignment
import com.example.data.repository.DocumentRepositoryImpl
import com.example.data.repository.TemplateType
import com.example.data.service.DocumentExportService
import com.example.data.service.DocumentImportService
import com.example.data.storage.DocumentStorageManager
import com.example.editor.engine.AutosaveController
import com.example.editor.engine.AutosaveStatus
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File
import java.util.UUID

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class DocumentPersistenceRobolectricTest {

    private lateinit var context: Context
    private lateinit var database: AppDatabase
    private lateinit var storageManager: DocumentStorageManager
    private lateinit var repository: DocumentRepositoryImpl

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        storageManager = DocumentStorageManager(context)

        // Mock export and import services for repository testing
        val dummyExportService = object : DocumentExportService {
            override suspend fun exportToPdf(context: Context, document: RichDocument, outputFile: File) =
                DocumentResult.Success(outputFile)
            override fun printDocument(context: Context, document: RichDocument, jobName: String) {}
            override suspend fun exportToDocx(context: Context, document: RichDocument, outputFile: File) =
                DocumentResult.Success(outputFile)
            override suspend fun exportToPlainText(context: Context, document: RichDocument, outputFile: File) =
                DocumentResult.Success(outputFile)
            override suspend fun exportPdfToUri(context: Context, document: RichDocument, targetUri: android.net.Uri) =
                DocumentResult.Success(Unit)
        }
        val dummyImportService = object : DocumentImportService {
            override suspend fun importDocumentFromUri(context: Context, uri: android.net.Uri) =
                DocumentResult.Success(RichDocument())
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
    fun testCreateAndLoadDocumentPreservesContentAndMetadata() = runTest {
        val createResult = repository.createDocument("Annual Report 2026", TemplateType.BLANK)
        assertTrue("Document creation should succeed", createResult is DocumentResult.Success)
        val createdDoc = (createResult as DocumentResult.Success).data

        assertEquals("Annual Report 2026", createdDoc.title)

        // Verify Room entity
        val entity = database.documentDao().getDocumentById(createdDoc.id)
        assertNotNull("Entity should exist in Room", entity)
        assertEquals("Annual Report 2026", entity!!.title)

        // Verify loaded from storage
        val loadResult = repository.getDocument(createdDoc.id)
        assertTrue("Loading document should succeed", loadResult is DocumentResult.Success)
        val loadedDoc = (loadResult as DocumentResult.Success).data
        assertEquals(createdDoc.id, loadedDoc.id)
        assertEquals("Annual Report 2026", loadedDoc.title)
    }

    @Test
    fun testSavePreservesRichTextFormattingTablesAndLayout() = runTest {
        val docId = UUID.randomUUID().toString()
        val customLayout = PageLayoutConfig(
            pageSize = PageSize.LETTER,
            orientation = PageOrientation.LANDSCAPE,
            marginPreset = MarginPreset.WIDE
        )

        val p1 = ParagraphBlock(
            id = "p1",
            text = "Financial Summary and Quarterly Metrics",
            headingStyle = HeadingStyle.TITLE,
            alignment = TextAlignment.CENTER,
            spans = listOf(
                InlineSpan(start = 0, end = 9, isBold = true, textColorHex = "#185ABD"),
                InlineSpan(start = 14, end = 21, isItalic = true, isUnderline = true, fontSizeSp = 20f)
            )
        )

        val table = TableBlock(
            id = "tbl1",
            rowCount = 2,
            columnCount = 2,
            cells = listOf(
                listOf(TableCell("Q1 Revenue", isBold = true, backgroundColorHex = "#F1F5F9"), TableCell("$1.2M")),
                listOf(TableCell("Q2 Revenue", isBold = true, backgroundColorHex = "#F1F5F9"), TableCell("$1.8M"))
            )
        )

        val richDoc = RichDocument(
            id = docId,
            title = "Formatted Metrics",
            layoutConfig = customLayout,
            blocks = listOf(p1, table)
        )

        val saveRes = repository.saveDocument(richDoc)
        assertTrue("Saving rich document should succeed", saveRes.isSuccess)

        // Load document back
        val loadRes = repository.getDocument(docId)
        assertTrue("Loading rich document should succeed", loadRes is DocumentResult.Success)
        val loadedDoc = (loadRes as DocumentResult.Success).data

        // Verify layout preservation
        assertEquals(PageSize.LETTER, loadedDoc.layoutConfig.pageSize)
        assertEquals(PageOrientation.LANDSCAPE, loadedDoc.layoutConfig.orientation)
        assertEquals(MarginPreset.WIDE, loadedDoc.layoutConfig.marginPreset)

        // Verify blocks preservation
        assertEquals(2, loadedDoc.blocks.size)
        val loadedP1 = loadedDoc.blocks[0] as ParagraphBlock
        assertEquals("Financial Summary and Quarterly Metrics", loadedP1.text)
        assertEquals(HeadingStyle.TITLE, loadedP1.headingStyle)
        assertEquals(TextAlignment.CENTER, loadedP1.alignment)
        assertEquals(2, loadedP1.spans.size)
        assertTrue("First span must be bold", loadedP1.spans[0].isBold)
        assertEquals("#185ABD", loadedP1.spans[0].textColorHex)
        assertTrue("Second span must be italic and underlined", loadedP1.spans[1].isItalic && loadedP1.spans[1].isUnderline)

        // Verify table preservation
        val loadedTable = loadedDoc.blocks[1] as TableBlock
        assertEquals(2, loadedTable.rowCount)
        assertEquals(2, loadedTable.columnCount)
        assertEquals("Q1 Revenue", loadedTable.cells[0][0].text)
        assertTrue(loadedTable.cells[0][0].isBold)
        assertEquals("$1.2M", loadedTable.cells[0][1].text)
    }

    @Test
    fun testSaveAndLoadAllPageLayoutSettingsPreserved() = runTest {
        val docId = UUID.randomUUID().toString()
        val advancedLayout = PageLayoutConfig(
            pageSize = PageSize.LEGAL,
            orientation = PageOrientation.LANDSCAPE,
            marginPreset = MarginPreset.CUSTOM,
            customTopMarginMm = 18f,
            customBottomMarginMm = 22f,
            customLeftMarginMm = 35f,
            customRightMarginMm = 35f,
            showHeader = true,
            showFooter = true,
            headerText = "Confidential - Legal Department",
            footerText = "Document {page} of {pages}",
            pageBackgroundHex = "#FFFDF7",
            borderStyle = com.example.core.layout.PageBorderStyle.DOUBLE,
            borderColorHex = "#185ABD",
            pageNumberFormat = com.example.core.layout.PageNumberFormat.ROMAN_LOWER,
            pageNumberStart = 2,
            differentFirstPage = true
        )

        val doc = RichDocument(
            id = docId,
            title = "Legal Brief",
            layoutConfig = advancedLayout,
            blocks = listOf(
                ParagraphBlock(text = "Agreement between parties...", headingStyle = HeadingStyle.NORMAL)
            )
        )

        val saveRes = repository.saveDocument(doc)
        assertTrue("Save must succeed", saveRes.isSuccess)

        val loadRes = repository.getDocument(docId)
        assertTrue("Load must succeed", loadRes is DocumentResult.Success)
        val loaded = (loadRes as DocumentResult.Success).data
        val l = loaded.layoutConfig

        assertEquals(PageSize.LEGAL, l.pageSize)
        assertEquals(PageOrientation.LANDSCAPE, l.orientation)
        assertEquals(MarginPreset.CUSTOM, l.marginPreset)
        assertEquals(18f, l.customTopMarginMm)
        assertEquals(22f, l.customBottomMarginMm)
        assertEquals(35f, l.customLeftMarginMm)
        assertEquals(35f, l.customRightMarginMm)
        assertEquals(18f, l.topMarginMm)
        assertEquals(35f, l.leftMarginMm)
        assertTrue(l.showHeader)
        assertTrue(l.showFooter)
        assertEquals("Confidential - Legal Department", l.headerText)
        assertEquals("Document {page} of {pages}", l.footerText)
        assertEquals("#FFFDF7", l.pageBackgroundHex)
        assertEquals(com.example.core.layout.PageBorderStyle.DOUBLE, l.borderStyle)
        assertEquals("#185ABD", l.borderColorHex)
        assertEquals(com.example.core.layout.PageNumberFormat.ROMAN_LOWER, l.pageNumberFormat)
        assertEquals(2, l.pageNumberStart)
        assertTrue(l.differentFirstPage)
    }

    @Test
    fun testRenamingDocumentDoesNotDestroyContent() = runTest {
        val createRes = repository.createDocument("Original Name", TemplateType.BLANK)
        val doc = (createRes as DocumentResult.Success).data

        // Add some rich content
        val modifiedDoc = doc.copy(
            blocks = listOf(
                ParagraphBlock(id = "p1", text = "Important confidential notes that must not be deleted on rename")
            )
        )
        repository.saveDocument(modifiedDoc)

        // Rename document
        val renameRes = repository.renameDocument(doc.id, "Renamed Name")
        assertTrue(renameRes.isSuccess)

        // Verify Room metadata has new title
        val entity = database.documentDao().getDocumentById(doc.id)
        assertEquals("Renamed Name", entity!!.title)

        // Verify document content was completely preserved
        val loaded = (repository.getDocument(doc.id) as DocumentResult.Success).data
        assertEquals("Renamed Name", loaded.title)
        assertEquals(1, loaded.blocks.size)
        assertEquals(
            "Important confidential notes that must not be deleted on rename",
            (loaded.blocks[0] as ParagraphBlock).text
        )
    }

    @Test
    fun testSaveAsCreatesIndependentDocument() = runTest {
        val createRes = repository.createDocument("Master Template", TemplateType.BLANK)
        val original = (createRes as DocumentResult.Success).data
        val updatedOriginal = original.copy(
            blocks = listOf(ParagraphBlock(text = "Base boilerplate"))
        )
        repository.saveDocument(updatedOriginal)

        // Save As
        val saveAsRes = repository.saveDocumentAs(updatedOriginal, "Branch Document")
        assertTrue(saveAsRes is DocumentResult.Success)
        val copyDoc = (saveAsRes as DocumentResult.Success).data

        // Verify copy has distinct ID and new title
        assertTrue("Copy must have new UUID", copyDoc.id != original.id)
        assertEquals("Branch Document", copyDoc.title)
        assertEquals("Base boilerplate", (copyDoc.blocks[0] as ParagraphBlock).text)

        // Verify both exist in Room
        assertNotNull(database.documentDao().getDocumentById(original.id))
        assertNotNull(database.documentDao().getDocumentById(copyDoc.id))
    }

    @Test
    fun testDuplicateDocumentCreatesCopyPreservingFormatting() = runTest {
        val createRes = repository.createDocument("Original Doc", TemplateType.BLANK)
        val doc = (createRes as DocumentResult.Success).data

        val duplicateRes = repository.duplicateDocument(doc.id)
        assertTrue(duplicateRes is DocumentResult.Success)
        val dupDoc = (duplicateRes as DocumentResult.Success).data

        assertEquals("Original Doc (Copy)", dupDoc.title)
        assertTrue(dupDoc.id != doc.id)

        // Ensure duplicate exists in storage
        val loadedDup = repository.getDocument(dupDoc.id)
        assertTrue(loadedDup is DocumentResult.Success)
    }

    @Test
    fun testDeleteDocumentRemovesFromRoomAndStorage() = runTest {
        val createRes = repository.createDocument("To Be Deleted", TemplateType.BLANK)
        val doc = (createRes as DocumentResult.Success).data

        // Confirm existence
        assertNotNull(database.documentDao().getDocumentById(doc.id))

        // Delete
        val delRes = repository.deleteDocument(doc.id)
        assertTrue(delRes.isSuccess)

        // Confirm removal from Room
        assertNull(database.documentDao().getDocumentById(doc.id))

        // Confirm removal from storage
        val loadRes = repository.getDocument(doc.id)
        assertTrue("Loading deleted document should fail", loadRes is DocumentResult.Failure)
    }

    @Test
    fun testCrashRecoveryDraftRestorationWorkflow() = runTest {
        val createRes = repository.createDocument("Crash Protected Doc", TemplateType.BLANK)
        val doc = (createRes as DocumentResult.Success).data

        assertFalse("Initially should not have recovery draft", repository.hasRecoveryDraft(doc.id))

        // Simulate unsaved edits and recovery draft snapshot
        val draftDoc = doc.copy(
            blocks = listOf(ParagraphBlock(text = "Unsaved work before sudden app crash"))
        )
        repository.saveRecoveryDraft(draftDoc)

        // Verify recovery draft is detected
        assertTrue("Recovery draft should now exist", repository.hasRecoveryDraft(doc.id))

        // Load recovery draft
        val recoveryRes = repository.loadRecoveryDraft(doc.id)
        assertTrue(recoveryRes is DocumentResult.Success)
        val recoveredDoc = (recoveryRes as DocumentResult.Success).data
        assertEquals("Unsaved work before sudden app crash", (recoveredDoc.blocks[0] as ParagraphBlock).text)

        // Clean save should clear the recovery draft
        repository.saveDocument(recoveredDoc)
        assertFalse("Clean save must clear recovery draft", repository.hasRecoveryDraft(doc.id))
    }

    @Test
    fun testSearchAndSorting() = runTest {
        val docA = repository.createDocument("Apple Project", TemplateType.BLANK)
        val docB = repository.createDocument("Banana Plan", TemplateType.BLANK)
        val docC = repository.createDocument("Carrot Report", TemplateType.BLANK)

        // Search test
        val searchResults = repository.searchDocuments("Banana").first()
        assertEquals(1, searchResults.size)
        assertEquals("Banana Plan", searchResults[0].title)

        // Room DAO title ascending sort
        val titleAsc = database.documentDao().getAllDocumentsByTitleAsc().first()
        assertTrue(titleAsc.size >= 3)
        val titles = titleAsc.map { it.title }
        assertTrue(titles.indexOf("Apple Project") < titles.indexOf("Banana Plan"))
        assertTrue(titles.indexOf("Banana Plan") < titles.indexOf("Carrot Report"))
    }

    @Test
    fun testAutosaveControllerTracksDirtyAndFlushes() = runTest {
        val testScope = TestScope()
        var saveCallCount = 0

        val autosave = AutosaveController(
            scope = testScope,
            debounceMillis = 100L,
            onPerformSave = {
                saveCallCount++
                true
            }
        )

        assertEquals(AutosaveStatus.IDLE, autosave.status.value)
        assertFalse(autosave.isDirty)

        // Schedule autosave
        autosave.scheduleAutosave()
        assertEquals(AutosaveStatus.DIRTY, autosave.status.value)
        assertTrue(autosave.isDirty)

        // Flush immediate save
        val flushed = autosave.flushImmediateSave()
        assertTrue("Flush should succeed", flushed)
        assertEquals(1, saveCallCount)
        assertEquals(AutosaveStatus.SAVED, autosave.status.value)
        assertFalse(autosave.isDirty)
    }
}
