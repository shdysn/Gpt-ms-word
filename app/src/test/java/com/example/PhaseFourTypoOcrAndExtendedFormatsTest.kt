package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.DividerBlock
import com.example.data.model.HeadingStyle
import com.example.data.model.ListType
import com.example.data.model.ParagraphBlock
import com.example.data.model.RichDocument
import com.example.data.model.TableBlock
import com.example.data.service.MarkdownAndEpubExportHelper
import com.example.data.storage.DocumentPlainTextImporter
import com.example.data.storage.DocumentStorageManager
import com.example.editor.engine.ContinuousAnnotatedTextBuilder
import com.example.editor.engine.SpellCheckEngine
import com.example.ui.editor.viewmodel.EditorBlockUtils
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.ByteArrayOutputStream
import java.util.zip.ZipInputStream

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class PhaseFourTypoOcrAndExtendedFormatsTest {

    private lateinit var context: Context
    private lateinit var storageManager: DocumentStorageManager

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        storageManager = DocumentStorageManager(context)
    }

    @Test
    fun testSpellCheckEngineTyposDetection() {
        val sample = "Please check teh calender before you recieve the package."
        val typos = SpellCheckEngine.findTypos(sample)
        assertTrue(typos.isNotEmpty())
        assertEquals(3, typos.size)

        val words = typos.map { sample.substring(it.first, it.second) }
        val suggestions = typos.map { it.third }

        assertTrue(words.contains("teh"))
        assertTrue(suggestions.contains("the"))

        assertTrue(words.contains("calender"))
        assertTrue(suggestions.contains("calendar"))

        assertTrue(words.contains("recieve"))
        assertTrue(suggestions.contains("receive"))
    }

    @Test
    fun testContinuousAnnotatedTextBuilderTypoUnderlines() {
        val text = "This is teh first line of text."
        val paragraphs = listOf(ParagraphBlock(text = text))
        val annotated = ContinuousAnnotatedTextBuilder.buildAnnotatedTextWithParagraphBlocks(text, paragraphs)

        val spellcheckAnnotations = annotated.getStringAnnotations("SPELLCHECK", 0, text.length)
        assertTrue(spellcheckAnnotations.isNotEmpty())
        val typoAnn = spellcheckAnnotations.first()
        assertEquals("the", typoAnn.item)
        assertEquals(8, typoAnn.start)
        assertEquals(11, typoAnn.end)
    }

    @Test
    fun testMarkdownParsingHeadingsAndLists() {
        val markdown = """
            # Project Milestone & Report
            
            ## Key Objectives
            
            > Important directive from leadership.
            
            - [x] Phase 1: Watermarks & Outline
            - [x] Phase 2: Drawing & Shapes
            - [x] Phase 3: Layout & Wrapping
            - [ ] Phase 4: OCR & Extended Formats
            
            * Fast compilation
            * High fidelity
            
            1. First step
            2. Second step
            
            ---
        """.trimIndent()

        val doc = DocumentPlainTextImporter.createDocumentFromMarkdown("Test MD", markdown)
        assertEquals("Project Milestone & Report", doc.title)
        assertTrue(doc.blocks.isNotEmpty())

        val pBlocks = doc.blocks.filterIsInstance<ParagraphBlock>()
        val h1 = pBlocks.firstOrNull { it.headingStyle == HeadingStyle.HEADING_1 }
        assertNotNull(h1)
        assertEquals("Key Objectives", h1?.text)

        val quote = pBlocks.firstOrNull { it.headingStyle == HeadingStyle.QUOTE }
        assertNotNull(quote)
        assertEquals("Important directive from leadership.", quote?.text)

        val checkedItem = pBlocks.firstOrNull { it.isChecklist && it.isChecked }
        assertNotNull(checkedItem)
        assertEquals("Phase 1: Watermarks & Outline", checkedItem?.text)

        val uncheckedItem = pBlocks.firstOrNull { it.isChecklist && !it.isChecked }
        assertNotNull(uncheckedItem)
        assertEquals("Phase 4: OCR & Extended Formats", uncheckedItem?.text)

        val bullet = pBlocks.firstOrNull { it.listType == ListType.BULLET }
        assertNotNull(bullet)
        assertEquals("Fast compilation", bullet?.text)

        val numbered = pBlocks.firstOrNull { it.listType == ListType.NUMBERED }
        assertNotNull(numbered)
        assertEquals("First step", numbered?.text)

        val divider = doc.blocks.filterIsInstance<DividerBlock>().firstOrNull()
        assertNotNull(divider)
    }

    @Test
    fun testMarkdownTableParsing() {
        val markdown = """
            # Document With Table
            
            | Item | Status | Priority |
            | --- | --- | --- |
            | Spellcheck | Done | High |
            | OCR Scanner | Done | Critical |
            | EPUB Export | Done | Medium |
        """.trimIndent()

        val doc = DocumentPlainTextImporter.createDocumentFromMarkdown("Table Test", markdown)
        val table = doc.blocks.filterIsInstance<TableBlock>().firstOrNull()
        assertNotNull(table)
        assertEquals(4, table?.rowCount)
        assertEquals(3, table?.columnCount)
        assertEquals("Item", table?.cells?.get(0)?.get(0)?.text)
        assertEquals("Status", table?.cells?.get(0)?.get(1)?.text)
        assertEquals("Priority", table?.cells?.get(0)?.get(2)?.text)
        assertEquals("OCR Scanner", table?.cells?.get(2)?.get(0)?.text)
    }

    @Test
    fun testMarkdownExportAndImportRoundTrip() {
        val originalDoc = RichDocument(
            id = "md-test-doc",
            title = "Quarterly Overview",
            blocks = listOf(
                ParagraphBlock(text = "Executive Summary", headingStyle = HeadingStyle.HEADING_1),
                ParagraphBlock(text = "All goals were achieved on schedule.", headingStyle = HeadingStyle.NORMAL),
                DividerBlock(),
                ParagraphBlock(text = "Review completed", isChecklist = true, isChecked = true)
            )
        )

        val markdownStr = MarkdownAndEpubExportHelper.generateMarkdown(originalDoc)
        assertTrue(markdownStr.contains("# Quarterly Overview"))
        assertTrue(markdownStr.contains("## Executive Summary"))
        assertTrue(markdownStr.contains("- [x] Review completed"))

        val restoredDoc = DocumentPlainTextImporter.createDocumentFromMarkdown("Imported", markdownStr)
        assertEquals("Quarterly Overview", restoredDoc.title)
        assertTrue(restoredDoc.blocks.any { it is ParagraphBlock && it.text == "Executive Summary" })
    }

    @Test
    fun testEpubPackageGenerationAndZipEntries() {
        val doc = RichDocument(
            id = "epub-test-doc",
            title = "Android Word Architecture",
            author = "Engineering Team",
            blocks = listOf(
                ParagraphBlock(text = "Chapter 1: Modern Layouts", headingStyle = HeadingStyle.HEADING_1),
                ParagraphBlock(text = "This chapter discusses word processing rendering inside Android.")
            )
        )

        val baos = ByteArrayOutputStream()
        MarkdownAndEpubExportHelper.writeEpubPackage(doc, baos)
        val bytes = baos.toByteArray()
        assertTrue(bytes.isNotEmpty())

        // Validate standard EPUB ZIP structure
        val zis = ZipInputStream(bytes.inputStream())
        val entries = mutableListOf<String>()
        var entry = zis.nextEntry
        while (entry != null) {
            entries.add(entry.name)
            zis.closeEntry()
            entry = zis.nextEntry
        }

        assertTrue(entries.contains("mimetype"))
        assertTrue(entries.contains("META-INF/container.xml"))
        assertTrue(entries.contains("OEBPS/content.opf"))
        assertTrue(entries.contains("OEBPS/document.xhtml"))
    }

    @Test
    fun testOcrScannedTextParagraphCreation() {
        val ocrRaw = """
            MEMORANDUM & MINUTES
            
            1. The committee convened to evaluate overall performance.
            2. All department benchmarks met criteria.
            
            Adjourned at 5:00 PM.
        """.trimIndent()

        val paragraphs = EditorBlockUtils.createParagraphsFromOcrText(ocrRaw, isRtl = false)
        assertEquals(4, paragraphs.size)
        assertEquals("MEMORANDUM & MINUTES", paragraphs[0].text)
        assertEquals("1. The committee convened to evaluate overall performance.", paragraphs[1].text)
        assertEquals("Adjourned at 5:00 PM.", paragraphs[3].text)
    }
}
