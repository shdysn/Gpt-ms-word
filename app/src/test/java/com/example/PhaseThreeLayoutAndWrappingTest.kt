package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.core.layout.DocumentPage
import com.example.core.layout.PageLayoutConfig
import com.example.core.layout.PageNumberFormat
import com.example.core.layout.PaginationEngine
import com.example.data.model.ImageBlock
import com.example.data.model.ParagraphBlock
import com.example.data.model.RichDocument
import com.example.data.model.TextBoxBlock
import com.example.data.storage.DocumentStorageManager
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class PhaseThreeLayoutAndWrappingTest {

    private lateinit var context: Context
    private lateinit var storageManager: DocumentStorageManager

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        storageManager = DocumentStorageManager(context)
    }

    @Test
    fun testDifferentFirstPageHeaderSuppression() {
        val layout = PageLayoutConfig(
            showHeader = true,
            showFooter = true,
            headerText = "Confidential Report",
            footerText = "Page {page} of {pages}",
            differentFirstPage = true
        )

        // First page should suppress header & footer
        val headerPage1 = DocumentPage.formatHeaderFooterText(
            template = layout.headerText,
            pageNumber = 1,
            totalPages = 3,
            layout = layout,
            documentTitle = "Q4 Annual Review",
            isHeader = true
        )
        val footerPage1 = DocumentPage.formatHeaderFooterText(
            template = layout.footerText,
            pageNumber = 1,
            totalPages = 3,
            layout = layout,
            documentTitle = "Q4 Annual Review",
            isHeader = false
        )
        assertEquals("", headerPage1)
        assertEquals("", footerPage1)

        // Subsequent page should show header & footer
        val headerPage2 = DocumentPage.formatHeaderFooterText(
            template = layout.headerText,
            pageNumber = 2,
            totalPages = 3,
            layout = layout,
            documentTitle = "Q4 Annual Review",
            isHeader = true
        )
        val footerPage2 = DocumentPage.formatHeaderFooterText(
            template = layout.footerText,
            pageNumber = 2,
            totalPages = 3,
            layout = layout,
            documentTitle = "Q4 Annual Review",
            isHeader = false
        )
        assertEquals("Confidential Report", headerPage2)
        assertEquals("Page 2 of 3", footerPage2)
    }

    @Test
    fun testDifferentOddAndEvenPagesHeaderFooter() {
        val layout = PageLayoutConfig(
            showHeader = true,
            showFooter = true,
            headerText = "Odd Page: Book Title",
            footerText = "Page {page}",
            differentOddEvenPages = true,
            headerEvenText = "Even Page: Chapter 1",
            footerEvenText = "{page} | Word Edition"
        )

        // Page 1 (Odd): should use odd header/footer
        val headerOdd = DocumentPage.formatHeaderFooterText(
            template = layout.headerText,
            pageNumber = 1,
            totalPages = 4,
            layout = layout,
            documentTitle = "Book",
            isHeader = true
        )
        val footerOdd = DocumentPage.formatHeaderFooterText(
            template = layout.footerText,
            pageNumber = 1,
            totalPages = 4,
            layout = layout,
            documentTitle = "Book",
            isHeader = false
        )
        assertEquals("Odd Page: Book Title", headerOdd)
        assertEquals("Page 1", footerOdd)

        // Page 2 (Even): should use even header/footer
        val headerEven = DocumentPage.formatHeaderFooterText(
            template = layout.headerText,
            pageNumber = 2,
            totalPages = 4,
            layout = layout,
            documentTitle = "Book",
            isHeader = true
        )
        val footerEven = DocumentPage.formatHeaderFooterText(
            template = layout.footerText,
            pageNumber = 2,
            totalPages = 4,
            layout = layout,
            documentTitle = "Book",
            isHeader = false
        )
        assertEquals("Even Page: Chapter 1", headerEven)
        assertEquals("2 | Word Edition", footerEven)
    }

    @Test
    fun testExtendedPageNumberFormats() {
        val layoutArabic = PageLayoutConfig(pageNumberFormat = PageNumberFormat.ARABIC_NUMERALS)
        assertEquals("5", layoutArabic.formatPageNumber(5, 10))

        val layoutRomanLower = PageLayoutConfig(pageNumberFormat = PageNumberFormat.ROMAN_LOWER)
        assertEquals("iv", layoutRomanLower.formatPageNumber(4, 10))
        assertEquals("ix", layoutRomanLower.formatPageNumber(9, 10))

        val layoutRomanUpper = PageLayoutConfig(pageNumberFormat = PageNumberFormat.ROMAN_UPPER)
        assertEquals("XIV", layoutRomanUpper.formatPageNumber(14, 20))

        val layoutLetters = PageLayoutConfig(pageNumberFormat = PageNumberFormat.LETTERS)
        assertEquals("A", layoutLetters.formatPageNumber(1, 10))
        assertEquals("C", layoutLetters.formatPageNumber(3, 10))

        val layoutPageXofY = PageLayoutConfig(pageNumberFormat = PageNumberFormat.PAGE_X_OF_Y)
        assertEquals("Page 3 of 8", layoutPageXofY.formatPageNumber(3, 8))
    }

    @Test
    fun testFloatingTextBoxSerializationAndGeometry() {
        val textBox = TextBoxBlock(
            text = "Floating Callout Note",
            widthDp = 220,
            heightDp = 120,
            backgroundColorHex = "#EFF6FF",
            borderColorHex = "#1D4ED8",
            borderWidthPt = 2.0f,
            rotationDeg = 5.0f,
            textColorHex = "#1E3A8A",
            offsetXPt = 45f,
            offsetYPt = 80f
        )
        val doc = RichDocument(
            id = "doc-tb-test",
            title = "Text Box Geometry Test",
            blocks = listOf(ParagraphBlock(text = "Paragraph before text box"), textBox)
        )

        val json = storageManager.serializeDocument(doc)
        val restored = storageManager.deserializeDocument(json)

        assertNotNull(restored)
        val restoredTb = restored?.blocks?.filterIsInstance<TextBoxBlock>()?.firstOrNull()
        assertNotNull(restoredTb)
        assertEquals("Floating Callout Note", restoredTb?.text)
        assertEquals(220, restoredTb?.widthDp)
        assertEquals(120, restoredTb?.heightDp)
        assertEquals("#EFF6FF", restoredTb?.backgroundColorHex)
        assertEquals("#1D4ED8", restoredTb?.borderColorHex)
        assertEquals(45f, restoredTb?.offsetXPt ?: 0f, 0.01f)
        assertEquals(80f, restoredTb?.offsetYPt ?: 0f, 0.01f)
    }

    @Test
    fun testImageWrappingModesAndOffsetSerialization() {
        val imageBlock = ImageBlock(
            imageUri = "content://media/external/images/media/100",
            caption = "Quarterly Chart",
            widthDp = 300,
            heightDp = 200,
            wrapMode = "SQUARE",
            offsetXPt = 25f,
            offsetYPt = 50f
        )
        val doc = RichDocument(
            id = "doc-img-test",
            title = "Image Wrap Test",
            blocks = listOf(imageBlock)
        )

        val json = storageManager.serializeDocument(doc)
        val restored = storageManager.deserializeDocument(json)

        assertNotNull(restored)
        val restoredImg = restored?.blocks?.filterIsInstance<ImageBlock>()?.firstOrNull()
        assertNotNull(restoredImg)
        assertEquals("SQUARE", restoredImg?.wrapMode)
        assertEquals(25f, restoredImg?.offsetXPt ?: 0f, 0.01f)
        assertEquals(50f, restoredImg?.offsetYPt ?: 0f, 0.01f)
        assertEquals(300, restoredImg?.widthDp)
        assertEquals(200, restoredImg?.heightDp)
    }

    @Test
    fun testFullPageLayoutConfigRoundTrip() {
        val layout = PageLayoutConfig(
            differentFirstPage = true,
            differentOddEvenPages = true,
            headerText = "Header Odd",
            headerEvenText = "Header Even",
            footerText = "Footer Odd",
            footerEvenText = "Footer Even",
            pageNumberFormat = PageNumberFormat.PAGE_X_OF_Y
        )
        val doc = RichDocument(id = "layout-doc", title = "Layout Roundtrip", layoutConfig = layout)

        val json = storageManager.serializeDocument(doc)
        val restored = storageManager.deserializeDocument(json)

        assertNotNull(restored)
        val rLayout = restored?.layoutConfig
        assertNotNull(rLayout)
        assertTrue(rLayout?.differentFirstPage == true)
        assertTrue(rLayout?.differentOddEvenPages == true)
        assertEquals("Header Odd", rLayout?.headerText)
        assertEquals("Header Even", rLayout?.headerEvenText)
        assertEquals("Footer Odd", rLayout?.footerText)
        assertEquals("Footer Even", rLayout?.footerEvenText)
        assertEquals(PageNumberFormat.PAGE_X_OF_Y, rLayout?.pageNumberFormat)
    }

    @Test
    fun testPaginationWithFloatingBehindAndInFrontImages() {
        val behindImage = ImageBlock(
            imageUri = "content://media/sample1",
            wrapMode = "BEHIND_TEXT"
        )
        val inFrontImage = ImageBlock(
            imageUri = "content://media/sample2",
            wrapMode = "IN_FRONT_OF_TEXT"
        )
        val paragraphs = (1..6).map {
            ParagraphBlock(text = "Paragraph line $it with rich text content for multi-line layout test.")
        }
        val doc = RichDocument(
            blocks = listOf(behindImage) + paragraphs + listOf(inFrontImage)
        )

        val pages = PaginationEngine.paginate(doc)
        assertTrue(pages.isNotEmpty())
        assertEquals(1, pages.size)
    }
}
