package com.example

import com.example.data.model.HeadingStyle
import com.example.core.layout.MarginPreset
import com.example.core.layout.PageLayoutConfig
import com.example.core.layout.PageOrientation
import com.example.core.layout.PageSize
import com.example.core.layout.PaginationEngine
import com.example.core.layout.TableOfContentsPageItem
import com.example.core.typography.SignatureUtils
import com.example.data.model.DocumentRevisionSnapshot
import com.example.data.model.ParagraphBlock
import com.example.data.model.RichDocument
import com.example.data.model.SignatureBlock
import com.example.data.model.TableOfContentsBlock
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class FiveGatePipelineTest {

    @Test
    fun testGate1_SignatureStrokeEncodingAndDecodingParity() {
        // User drew 2 strokes on signature pad
        val stroke1 = listOf(Pair(10f, 20f), Pair(50f, 60f), Pair(100f, 120f))
        val stroke2 = listOf(Pair(120f, 30f), Pair(180f, 90f))
        val originalStrokes = listOf(stroke1, stroke2)

        val canvasWidth = 200f
        val canvasHeight = 150f

        val encoded = SignatureUtils.encodeNormalizedStrokes(originalStrokes, canvasWidth, canvasHeight)
        assertTrue("Encoded string must contain normalized points", encoded.contains(";"))
        assertTrue("Encoded string must separate multiple strokes", encoded.contains("|"))

        // Reconstruct at PDF / Canvas target scale
        val targetWidth = 400f
        val targetHeight = 300f
        val decoded = SignatureUtils.decodeNormalizedStrokes(encoded, targetWidth, targetHeight)

        assertEquals("Decoded strokes count matches original", 2, decoded.size)
        assertEquals("First stroke point count matches", 3, decoded[0].size)
        assertEquals("Second stroke point count matches", 2, decoded[1].size)

        // Bounding check on reconstructed coordinates
        val firstX = decoded[0][0].first
        val firstY = decoded[0][0].second
        assertEquals(20f, firstX, 1.0f) // (10/200) * 400 = 20
        assertEquals(40f, firstY, 1.0f) // (20/150) * 300 = 40
    }

    @Test
    fun testGate2And3_PaginationEngineResolvesAccurateTocPageNumbers() {
        val blocks = listOf(
            TableOfContentsBlock(title = "Table of Contents"),
            ParagraphBlock(text = "Chapter 1: Foundations", headingStyle = HeadingStyle.HEADING_1),
            ParagraphBlock(text = "This is a detailed paragraph explaining foundational concepts in the system.\n".repeat(25)),
            com.example.data.model.PageBreakBlock(),
            ParagraphBlock(text = "Chapter 2: Advanced Processing", headingStyle = HeadingStyle.HEADING_1),
            ParagraphBlock(text = "Content belonging strictly to chapter 2 on the next page.")
        )

        val doc = RichDocument(
            title = "Test Multi-Page Document",
            blocks = blocks,
            layoutConfig = PageLayoutConfig(
                pageSize = PageSize.A4,
                marginPreset = MarginPreset.NORMAL
            )
        )

        val pages = PaginationEngine.paginate(doc)
        assertTrue("Document must paginate into at least 2 pages", pages.size >= 2)

        val pageMap = PaginationEngine.computeHeadingPageMap(doc)
        assertTrue("Heading map must identify Chapter 1", pageMap.isNotEmpty())

        val ch1Id = blocks[1].id
        val ch2Id = blocks[4].id

        assertEquals("Chapter 1 is on page 1", 1, pageMap[ch1Id])
        assertEquals("Chapter 2 is on page 2 after page break", 2, pageMap[ch2Id])

        // Verify TableOfContentsPageItem has resolvedEntries populated
        val firstPage = pages[0]
        val tocItem = firstPage.items.filterIsInstance<TableOfContentsPageItem>().firstOrNull()
        assertNotNull("TOC item must exist on page 1", tocItem)
        assertTrue("TOC item has resolved entries", tocItem!!.resolvedEntries.isNotEmpty())
        assertEquals("Chapter 2 in TOC points to page 2", 2, tocItem.resolvedEntries.first { it.title.contains("Chapter 2") }.pageNumber)
    }

    @Test
    fun testGate4_TwoColumnLayoutParity() {
        val layout = PageLayoutConfig(columnCount = 2)
        assertEquals(2, layout.columnCount)

        val doc = RichDocument(
            title = "Newspaper Layout",
            layoutConfig = layout,
            blocks = listOf(
                ParagraphBlock(text = "Left column item"),
                ParagraphBlock(text = "Right column item")
            )
        )

        val pages = PaginationEngine.paginate(doc)
        assertEquals(2, pages.first().layout.columnCount)
    }

    @Test
    fun testGate5_PasswordSecurityLockLifecycle() {
        val doc = RichDocument(
            title = "Confidential Report",
            passwordHash = "5e884898da28047151d0e56f8dc6292773603d0d6aabbdd62a11ef721d1542d8" // sha256("password")
        )

        assertTrue("Document is password protected", doc.isPasswordProtected)
        assertFalse("Document password hash is not empty", doc.passwordHash.isNullOrBlank())
    }
}
