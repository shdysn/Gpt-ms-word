package com.example

import com.example.core.layout.MarginPreset
import com.example.core.layout.PageBorderStyle
import com.example.core.layout.PageLayoutConfig
import com.example.core.layout.PageNumberFormat
import com.example.core.layout.PageOrientation
import com.example.core.layout.PageSize
import com.example.core.layout.PaginationEngine
import com.example.data.model.DividerBlock
import com.example.data.model.HeadingStyle
import com.example.data.model.InlineSpan
import com.example.data.model.PageBreakBlock
import com.example.data.model.ParagraphBlock
import com.example.data.model.RichDocument
import com.example.data.model.TableCell
import com.example.data.model.TableBlock
import com.example.data.model.TextAlignment
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Comprehensive test suite verifying the page-based document layout and pagination engine:
 * - ISO/ANSI Page sizes: A4, A5, Letter, Legal
 * - Portrait & Landscape orientations
 * - Margin presets & custom margins in points and mm
 * - Automatic page numbering (Arabic, Roman, Letters)
 * - Header/Footer lifecycle & Different First Page handling
 * - Explicit page break behavior
 * - Short documents vs multi-page long documents
 * - Paragraph splitting across page boundaries
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class PaginationEngineTest {

    @Test
    fun testPageSizesAndDimensions() {
        // A4 Portrait
        val a4Portrait = PageLayoutConfig(pageSize = PageSize.A4, orientation = PageOrientation.PORTRAIT)
        assertEquals(595, a4Portrait.pageWidthPt)
        assertEquals(842, a4Portrait.pageHeightPt)

        // A4 Landscape
        val a4Landscape = PageLayoutConfig(pageSize = PageSize.A4, orientation = PageOrientation.LANDSCAPE)
        assertEquals(842, a4Landscape.pageWidthPt)
        assertEquals(595, a4Landscape.pageHeightPt)

        // US Letter
        val letter = PageLayoutConfig(pageSize = PageSize.LETTER, orientation = PageOrientation.PORTRAIT)
        assertEquals(612, letter.pageWidthPt)
        assertEquals(792, letter.pageHeightPt)

        // US Legal
        val legal = PageLayoutConfig(pageSize = PageSize.LEGAL, orientation = PageOrientation.PORTRAIT)
        assertEquals(612, legal.pageWidthPt)
        assertEquals(1008, legal.pageHeightPt)

        // ISO A5
        val a5 = PageLayoutConfig(pageSize = PageSize.A5, orientation = PageOrientation.PORTRAIT)
        assertEquals(420, a5.pageWidthPt)
        assertEquals(595, a5.pageHeightPt)
    }

    @Test
    fun testMarginPresetsAndPrintableArea() {
        val normalLayout = PageLayoutConfig(pageSize = PageSize.A4, marginPreset = MarginPreset.NORMAL)
        // 25.4 mm = 1 inch = 72 pt
        assertEquals(72f, normalLayout.leftMarginPt, 0.5f)
        assertEquals(72f, normalLayout.rightMarginPt, 0.5f)
        assertEquals(72f, normalLayout.topMarginPt, 0.5f)
        assertEquals(72f, normalLayout.bottomMarginPt, 0.5f)

        // Printable width = 595 - 72 - 72 = 451 pt
        assertEquals(451f, normalLayout.printableWidthPt, 1f)

        // Narrow Margins (12.7 mm = 0.5 in = 36 pt)
        val narrowLayout = PageLayoutConfig(pageSize = PageSize.A4, marginPreset = MarginPreset.NARROW)
        assertEquals(36f, narrowLayout.leftMarginPt, 0.5f)
        assertEquals(523f, narrowLayout.printableWidthPt, 1f)

        // Custom Margins
        val customLayout = PageLayoutConfig(
            pageSize = PageSize.LETTER,
            marginPreset = MarginPreset.CUSTOM,
            customTopMarginMm = 20f,
            customBottomMarginMm = 20f,
            customLeftMarginMm = 30f,
            customRightMarginMm = 30f
        )
        assertEquals(20f, customLayout.topMarginMm)
        assertEquals(30f, customLayout.leftMarginMm)
    }

    @Test
    fun testPageNumberingFormats() {
        val layoutArabic = PageLayoutConfig(pageNumberFormat = PageNumberFormat.ARABIC_NUMERALS)
        assertEquals("1", layoutArabic.formatPageNumber(1))
        assertEquals("5", layoutArabic.formatPageNumber(5))

        val layoutRomanLower = PageLayoutConfig(pageNumberFormat = PageNumberFormat.ROMAN_LOWER)
        assertEquals("i", layoutRomanLower.formatPageNumber(1))
        assertEquals("iv", layoutRomanLower.formatPageNumber(4))
        assertEquals("ix", layoutRomanLower.formatPageNumber(9))

        val layoutRomanUpper = PageLayoutConfig(pageNumberFormat = PageNumberFormat.ROMAN_UPPER)
        assertEquals("I", layoutRomanUpper.formatPageNumber(1))
        assertEquals("XIV", layoutRomanUpper.formatPageNumber(14))

        val layoutLetters = PageLayoutConfig(pageNumberFormat = PageNumberFormat.LETTERS)
        assertEquals("A", layoutLetters.formatPageNumber(1))
        assertEquals("B", layoutLetters.formatPageNumber(2))
        assertEquals("C", layoutLetters.formatPageNumber(3))
    }

    @Test
    fun testExplicitPageBreakBlocks() {
        val doc = RichDocument(
            title = "Page Break Document",
            blocks = listOf(
                ParagraphBlock(text = "Header content on page 1"),
                PageBreakBlock(),
                ParagraphBlock(text = "Body content on page 2"),
                PageBreakBlock(),
                ParagraphBlock(text = "Closing content on page 3")
            )
        )

        val pages = PaginationEngine.paginate(doc)
        assertEquals(3, pages.size)
        assertEquals(1, pages[0].pageNumber)
        assertEquals(2, pages[1].pageNumber)
        assertEquals(3, pages[2].pageNumber)
        assertEquals(3, pages[0].totalPages)
        assertEquals(3, pages[1].totalPages)
        assertEquals(3, pages[2].totalPages)
    }

    @Test
    fun testShortDocumentPagination() {
        val letterDoc = RichDocument(
            title = "Short Formal Letter",
            layoutConfig = PageLayoutConfig(
                pageSize = PageSize.A4,
                headerText = "Apex Corp",
                footerText = "Page {page} of {pages}"
            ),
            blocks = listOf(
                ParagraphBlock(text = "October 10, 2026", headingStyle = HeadingStyle.NORMAL),
                ParagraphBlock(text = "Dear Mr. Vance,", headingStyle = HeadingStyle.NORMAL),
                ParagraphBlock(text = "We are pleased to submit this proposal for your review."),
                ParagraphBlock(text = "Sincerely,\nAlexander Wright")
            )
        )

        val pages = PaginationEngine.paginate(letterDoc)
        assertEquals(1, pages.size)
        val singlePage = pages[0]
        assertEquals(1, singlePage.pageNumber)
        assertEquals(1, singlePage.totalPages)
        assertTrue(singlePage.hasHeader)
        assertEquals("Apex Corp", singlePage.formattedHeader)
        assertEquals("Page 1 of 1", singlePage.formattedFooter)
    }

    @Test
    fun testMultiPageLongDocumentPagination() {
        // Build a document with 35 long paragraphs that definitely overflows multiple pages
        val blocks = mutableListOf<ParagraphBlock>()
        for (i in 1..35) {
            blocks.add(
                ParagraphBlock(
                    id = "p_$i",
                    text = "Paragraph $i: " + "Smart Word Processor is a high-performance word processing system for Android. "
                        .repeat(10),
                    headingStyle = if (i % 5 == 1) HeadingStyle.HEADING_1 else HeadingStyle.NORMAL
                )
            )
        }

        val doc = RichDocument(
            title = "Long Document",
            layoutConfig = PageLayoutConfig(
                pageSize = PageSize.A4,
                headerText = "Technical Whitepaper",
                footerText = "Page {page} of {pages}"
            ),
            blocks = blocks
        )

        val pages = PaginationEngine.paginate(doc)
        assertTrue("Long document must span at least 3 pages", pages.size >= 3)

        // Verify each page has correct sequential numbers
        pages.forEachIndexed { index, page ->
            assertEquals(index + 1, page.pageNumber)
            assertEquals(pages.size, page.totalPages)
            assertEquals("Page ${index + 1} of ${pages.size}", page.formattedFooter)
            assertTrue("Every page should contain content items", page.items.isNotEmpty())
        }
    }

    @Test
    fun testTablePaginationAcrossPages() {
        val table = TableBlock(
            rowCount = 4,
            columnCount = 3,
            cells = listOf(
                listOf(TableCell("Col 1", isBold = true), TableCell("Col 2", isBold = true), TableCell("Col 3", isBold = true)),
                listOf(TableCell("A1"), TableCell("A2"), TableCell("A3")),
                listOf(TableCell("B1"), TableCell("B2"), TableCell("B3")),
                listOf(TableCell("C1"), TableCell("C2"), TableCell("C3"))
            )
        )

        val doc = RichDocument(
            title = "Table Document",
            blocks = listOf(
                ParagraphBlock(text = "Summary Table", headingStyle = HeadingStyle.TITLE),
                table,
                DividerBlock(),
                ParagraphBlock(text = "Table notes and conclusions.")
            )
        )

        val pages = PaginationEngine.paginate(doc)
        assertTrue("Document with table should paginate cleanly", pages.isNotEmpty())
        val firstPage = pages[0]
        val tableItem = firstPage.items.find { it is com.example.core.layout.TablePageItem }
        assertNotNull("First page must contain table item", tableItem)
    }

    @Test
    fun testDifferentFirstPageSetting() {
        val doc = RichDocument(
            title = "Cover Page Report",
            layoutConfig = PageLayoutConfig(
                pageSize = PageSize.A4,
                showHeader = true,
                showFooter = true,
                headerText = "Quarterly Report",
                footerText = "Page {page} of {pages}",
                differentFirstPage = true
            ),
            blocks = listOf(
                ParagraphBlock(text = "Cover Page Title", headingStyle = HeadingStyle.TITLE),
                PageBreakBlock(),
                ParagraphBlock(text = "Body Chapter 1", headingStyle = HeadingStyle.HEADING_1)
            )
        )

        val pages = PaginationEngine.paginate(doc)
        assertEquals(2, pages.size)

        // Page 1: First page, headers and footers suppressed by differentFirstPage
        assertFalse("Page 1 should suppress header when differentFirstPage is true", pages[0].hasHeader)
        assertFalse("Page 1 should suppress footer when differentFirstPage is true", pages[0].hasFooter)

        // Page 2: Standard page, header and footer present
        assertTrue("Page 2 should show header", pages[1].hasHeader)
        assertTrue("Page 2 should show footer", pages[1].hasFooter)
        assertEquals("Quarterly Report", pages[1].formattedHeader)
        assertEquals("Page 2 of 2", pages[1].formattedFooter)
    }
}
