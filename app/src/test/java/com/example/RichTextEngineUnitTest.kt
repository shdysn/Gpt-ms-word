package com.example

import com.example.core.layout.PageLayoutConfig
import com.example.data.model.HeadingStyle
import com.example.data.model.InlineSpan
import com.example.data.model.ListType
import com.example.data.model.ParagraphBlock
import com.example.data.model.RichDocument
import com.example.data.model.TextAlignment
import com.example.editor.engine.ActiveFormatting
import com.example.editor.engine.ApplyInlineSpanCommand
import com.example.editor.engine.ChangeAlignmentCommand
import com.example.editor.engine.ChangeHeadingStyleCommand
import com.example.editor.engine.ChangeListTypeCommand
import com.example.editor.engine.EditorEngineIntegration
import com.example.editor.engine.SetIndentationCommand
import com.example.editor.engine.SpanOperations
import com.example.editor.engine.UndoRedoManager
import com.example.editor.engine.UpdateParagraphTextAndSpansCommand
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit test suite validating the Smart Word Processor rich-text editing engine:
 * - Word-level selection formatting
 * - Heterogeneous multi-style paragraphs
 * - Continuous typing after formatting
 * - Selection text replacement with span shifting
 * - Command pattern undo / redo history
 * - Multi-paragraph document structure
 */
class RichTextEngineUnitTest {

    @Test
    fun testSelectWordAndMakeItBold() {
        val originalText = "Hello world from Smart Word"
        // Target word "world" is from index 6 to 11
        val start = 6
        val end = 11

        val initialSpans = emptyList<InlineSpan>()
        val boldSpans = SpanOperations.applyOrToggleStyle(
            currentSpans = initialSpans,
            rangeStart = start,
            rangeEnd = end,
            textLength = originalText.length
        ) { span, enable ->
            span.copy(isBold = enable)
        }

        assertEquals(1, boldSpans.size)
        val span = boldSpans[0]
        assertEquals(start, span.start)
        assertEquals(end, span.end)
        assertTrue("Selected word should be bold", span.isBold)
        assertFalse("Selected word should not be italic", span.isItalic)

        // Query formatting at cursor inside the word
        val midQuery = SpanOperations.queryFormatting(boldSpans, 8, 8)
        assertTrue("Cursor at index 8 should report active bold", midQuery.isBold)

        // Query formatting outside the word
        val outsideQuery = SpanOperations.queryFormatting(boldSpans, 2, 2)
        assertFalse("Cursor at index 2 should not report bold", outsideQuery.isBold)

        // Toggling bold again on the exact same selection should turn it off
        val unboldSpans = SpanOperations.applyOrToggleStyle(
            currentSpans = boldSpans,
            rangeStart = start,
            rangeEnd = end,
            textLength = originalText.length
        ) { s, enable ->
            s.copy(isBold = enable)
        }
        assertTrue("Toggling bold off should leave no active styled spans", unboldSpans.isEmpty())
    }

    @Test
    fun testApplyDifferentFormattingToDifferentPartsOfParagraph() {
        val paragraphText = "The quick brown fox jumps over the lazy dog"
        var spans = emptyList<InlineSpan>()

        // 1. Make "quick" (4..9) Bold
        spans = SpanOperations.applyOrToggleStyle(spans, 4, 9, paragraphText.length) { s, enable ->
            s.copy(isBold = enable)
        }

        // 2. Make "brown" (10..15) Italic
        spans = SpanOperations.applyOrToggleStyle(spans, 10, 15, paragraphText.length) { s, enable ->
            s.copy(isItalic = enable)
        }

        // 3. Set text color "#185ABD" (Word Blue) on "fox" (16..19)
        spans = SpanOperations.applyExplicitStyle(spans, 16, 19, paragraphText.length) { s ->
            s.copy(textColorHex = "#185ABD")
        }

        // 4. Add hyperlink and underline to "lazy dog" (35..43)
        spans = SpanOperations.applyExplicitStyle(spans, 35, 43, paragraphText.length) { s ->
            s.copy(linkUrl = "https://example.com", isUnderline = true)
        }

        assertEquals(4, spans.size)

        // Verify "quick"
        val quickSpan = spans.find { it.start == 4 && it.end == 9 }
        assertNotNull(quickSpan)
        assertTrue(quickSpan!!.isBold)
        assertFalse(quickSpan.isItalic)

        // Verify "brown"
        val brownSpan = spans.find { it.start == 10 && it.end == 15 }
        assertNotNull(brownSpan)
        assertTrue(brownSpan!!.isItalic)
        assertFalse(brownSpan.isBold)

        // Verify "fox"
        val foxSpan = spans.find { it.start == 16 && it.end == 19 }
        assertNotNull(foxSpan)
        assertEquals("#185ABD", foxSpan!!.textColorHex)

        // Verify "lazy dog"
        val linkSpan = spans.find { it.start == 35 && it.end == 43 }
        assertNotNull(linkSpan)
        assertEquals("https://example.com", linkSpan!!.linkUrl)
        assertTrue(linkSpan.isUnderline)

        // Verify AnnotatedString generation does not crash and accurately encodes styles
        val block = ParagraphBlock(
            text = paragraphText,
            spans = spans
        )
        val annotated = EditorEngineIntegration.buildAnnotatedParagraph(block)
        assertEquals(paragraphText, annotated.text)
    }

    @Test
    fun testContinueTypingAfterApplyingFormatting() {
        // User starts with "Hello " and toggles Bold on before typing "world"
        val initialText = "Hello "
        val pending = ActiveFormatting(isBold = true, textColorHex = "#185ABD")

        // User types "world" at offset 6 (length 5)
        val editOffset = 6
        val deletedLength = 0
        val insertedLength = 5

        val updatedSpans = SpanOperations.shiftSpansOnTextChange(
            spans = emptyList(),
            editOffset = editOffset,
            deletedLength = deletedLength,
            insertedLength = insertedLength,
            pendingFormatting = pending
        )

        assertEquals(1, updatedSpans.size)
        val typedSpan = updatedSpans[0]
        assertEquals(6, typedSpan.start)
        assertEquals(11, typedSpan.end)
        assertTrue(typedSpan.isBold)
        assertEquals("#185ABD", typedSpan.textColorHex)
    }

    @Test
    fun testSelectAndReplaceTextWithSpanShifting() {
        // Initial: "Hello powerful word processor"
        // Spans: "powerful" (6..14) is bold, "processor" (20..29) is italic
        val initialSpans = listOf(
            InlineSpan(start = 6, end = 14, isBold = true),
            InlineSpan(start = 20, end = 29, isItalic = true)
        )

        // User replaces "powerful" (length 8) with "fast" (length 4) at offset 6
        // editOffset = 6, deletedLength = 8, insertedLength = 4 -> delta = -4
        val shiftedSpans = SpanOperations.shiftSpansOnTextChange(
            spans = initialSpans,
            editOffset = 6,
            deletedLength = 8,
            insertedLength = 4
        )

        // "processor" was originally at 20..29, after -4 delta it should shift to 16..25
        val processorSpan = shiftedSpans.find { it.isItalic }
        assertNotNull(processorSpan)
        assertEquals(16, processorSpan!!.start)
        assertEquals(25, processorSpan.end)
    }

    @Test
    fun testUndoAndRedoChanges() {
        val undoRedoManager = UndoRedoManager(maxHistorySize = 50)
        val initialDoc = RichDocument(
            title = "Undo Test",
            blocks = listOf(
                ParagraphBlock(id = "p1", text = "First draft content", spans = emptyList())
            )
        )

        assertFalse("Cannot undo initially", undoRedoManager.canUndo)
        assertFalse("Cannot redo initially", undoRedoManager.canRedo)

        // Step 1: Update text
        val textCommand = UpdateParagraphTextAndSpansCommand(
            blockId = "p1",
            oldText = "First draft content",
            newText = "Second draft content",
            oldSpans = emptyList(),
            newSpans = emptyList()
        )
        val docAfterText = undoRedoManager.execute(textCommand, initialDoc)
        assertEquals("Second draft content", (docAfterText.blocks[0] as ParagraphBlock).text)
        assertTrue(undoRedoManager.canUndo)

        // Step 2: Apply bold to "Second" (0..6)
        val boldSpans = listOf(InlineSpan(start = 0, end = 6, isBold = true))
        val spanCommand = ApplyInlineSpanCommand(
            blockId = "p1",
            oldSpans = emptyList(),
            newSpans = boldSpans
        )
        val docAfterBold = undoRedoManager.execute(spanCommand, docAfterText)
        assertTrue((docAfterBold.blocks[0] as ParagraphBlock).spans[0].isBold)

        // Undo 1: Reverts bold
        val (undoneDoc1, _) = undoRedoManager.undo(docAfterBold)
        assertEquals(0, (undoneDoc1.blocks[0] as ParagraphBlock).spans.size)
        assertEquals("Second draft content", (undoneDoc1.blocks[0] as ParagraphBlock).text)
        assertTrue(undoRedoManager.canRedo)

        // Undo 2: Reverts text
        val (undoneDoc2, _) = undoRedoManager.undo(undoneDoc1)
        assertEquals("First draft content", (undoneDoc2.blocks[0] as ParagraphBlock).text)
        assertFalse("Cannot undo beyond initial", undoRedoManager.canUndo)

        // Redo 1: Restores text
        val (redoneDoc1, _) = undoRedoManager.redo(undoneDoc2)
        assertEquals("Second draft content", (redoneDoc1.blocks[0] as ParagraphBlock).text)

        // Redo 2: Restores bold
        val (redoneDoc2, _) = undoRedoManager.redo(redoneDoc1)
        assertTrue((redoneDoc2.blocks[0] as ParagraphBlock).spans[0].isBold)
        assertFalse("Cannot redo beyond latest", undoRedoManager.canRedo)
    }

    @Test
    fun testEditDocumentContainingSeveralParagraphs() {
        val p1 = ParagraphBlock(
            id = "p1",
            text = "Smart Word Processor Overview",
            headingStyle = HeadingStyle.TITLE,
            alignment = TextAlignment.CENTER
        )
        val p2 = ParagraphBlock(
            id = "p2",
            text = "First feature point: full rich text formatting support",
            listType = ListType.BULLET,
            indentationLevel = 1
        )
        val p3 = ParagraphBlock(
            id = "p3",
            text = "Second feature point: multi-paragraph document editing",
            listType = ListType.NUMBERED,
            indentationLevel = 1
        )

        val doc = RichDocument(
            title = "Multi-Paragraph Test",
            blocks = listOf(p1, p2, p3)
        )

        assertEquals(3, doc.blocks.size)

        // Edit only p2
        val updatedP2 = p2.copy(text = "Updated feature point with custom text")
        val updatedDoc = doc.copy(
            blocks = doc.blocks.map { if (it.id == p2.id) updatedP2 else it }
        )

        // Ensure p1 and p3 are unmodified
        val retrievedP1 = updatedDoc.blocks[0] as ParagraphBlock
        val retrievedP2 = updatedDoc.blocks[1] as ParagraphBlock
        val retrievedP3 = updatedDoc.blocks[2] as ParagraphBlock

        assertEquals("Smart Word Processor Overview", retrievedP1.text)
        assertEquals(HeadingStyle.TITLE, retrievedP1.headingStyle)
        assertEquals(TextAlignment.CENTER, retrievedP1.alignment)

        assertEquals("Updated feature point with custom text", retrievedP2.text)
        assertEquals(ListType.BULLET, retrievedP2.listType)

        assertEquals("Second feature point: multi-paragraph document editing", retrievedP3.text)
        assertEquals(ListType.NUMBERED, retrievedP3.listType)
    }

    @Test
    fun testParagraphStyleInAnnotatedString() {
        val annotated = androidx.compose.ui.text.buildAnnotatedString {
            append("First Line\n\nRight Line")
            addStyle(androidx.compose.ui.text.ParagraphStyle(textAlign = androidx.compose.ui.text.style.TextAlign.Start), 0, 10)
            addStyle(androidx.compose.ui.text.ParagraphStyle(textAlign = androidx.compose.ui.text.style.TextAlign.Center), 11, 11)
            addStyle(androidx.compose.ui.text.ParagraphStyle(textAlign = androidx.compose.ui.text.style.TextAlign.End), 12, 22)
        }

        assertEquals(3, annotated.paragraphStyles.size)
        assertEquals(androidx.compose.ui.text.style.TextAlign.Start, annotated.paragraphStyles[0].item.textAlign)
        assertEquals(androidx.compose.ui.text.style.TextAlign.Center, annotated.paragraphStyles[1].item.textAlign)
        assertEquals(androidx.compose.ui.text.style.TextAlign.End, annotated.paragraphStyles[2].item.textAlign)
    }

    @Test
    fun testBuildAnnotatedTextWithParagraphBlocksDifferentAlignments() {
        val p1 = ParagraphBlock(id = "p1", text = "Left Heading", alignment = TextAlignment.START)
        val p2 = ParagraphBlock(id = "p2", text = "Centered Subtitle", alignment = TextAlignment.CENTER)
        val p3 = ParagraphBlock(id = "p3", text = "Right Footer", alignment = TextAlignment.END)

        val text = "Left Heading\nCentered Subtitle\nRight Footer"
        val annotated = EditorEngineIntegration.buildAnnotatedTextWithParagraphBlocks(
            text = text,
            paragraphs = listOf(p1, p2, p3)
        )

        assertEquals(3, annotated.paragraphStyles.size)
        assertEquals(androidx.compose.ui.text.style.TextAlign.Start, annotated.paragraphStyles[0].item.textAlign)
        assertEquals(androidx.compose.ui.text.style.TextAlign.Center, annotated.paragraphStyles[1].item.textAlign)
        assertEquals(androidx.compose.ui.text.style.TextAlign.End, annotated.paragraphStyles[2].item.textAlign)
    }

    @Test
    fun testParagraphAlignmentChangeOnlyAffectsTargetParagraph() {
        val p1 = ParagraphBlock(id = "p1", text = "First Paragraph", alignment = TextAlignment.START)
        val p2 = ParagraphBlock(id = "p2", text = "Second Paragraph", alignment = TextAlignment.START)
        val doc = RichDocument(blocks = listOf(p1, p2))

        val manager = UndoRedoManager()
        val command = ChangeAlignmentCommand(blockId = "p2", oldAlignment = p2.alignment, newAlignment = TextAlignment.CENTER)
        val updatedDoc = manager.execute(command, doc)

        val r1 = updatedDoc.blocks[0] as ParagraphBlock
        val r2 = updatedDoc.blocks[1] as ParagraphBlock

        // First paragraph should STILL be START
        assertEquals(TextAlignment.START, r1.alignment)
        // Only second paragraph should be CENTER
        assertEquals(TextAlignment.CENTER, r2.alignment)
    }

    @Test
    fun testMultiParagraphIndependentAlignments() {
        val p1 = ParagraphBlock(id = "p1", text = "Left Aligned Line", alignment = TextAlignment.START)
        val p2 = ParagraphBlock(id = "p2", text = "Center Aligned Line", alignment = TextAlignment.CENTER)
        val p3 = ParagraphBlock(id = "p3", text = "Right Aligned Line", alignment = TextAlignment.END)
        val doc = RichDocument(blocks = listOf(p1, p2, p3))

        val manager = UndoRedoManager()
        // Change p1 to JUSTIFY
        val docAfterP1 = manager.execute(
            ChangeAlignmentCommand(blockId = "p1", oldAlignment = p1.alignment, newAlignment = TextAlignment.JUSTIFY),
            doc
        )

        val res1 = docAfterP1.blocks[0] as ParagraphBlock
        val res2 = docAfterP1.blocks[1] as ParagraphBlock
        val res3 = docAfterP1.blocks[2] as ParagraphBlock

        // p1 changed to JUSTIFY
        assertEquals(TextAlignment.JUSTIFY, res1.alignment)
        // p2 is still CENTER
        assertEquals(TextAlignment.CENTER, res2.alignment)
        // p3 is still END
        assertEquals(TextAlignment.END, res3.alignment)
    }

    @Test
    fun testParagraphSplittingSpansShiftCorrectly() {
        // "Hello Beautiful World" where "Beautiful" (indices 6..15) is bold
        val p = ParagraphBlock(
            id = "p1",
            text = "Hello Beautiful World",
            alignment = TextAlignment.CENTER,
            spans = listOf(InlineSpan(start = 6, end = 15, isBold = true))
        )
        // Split at cursor index 6 (right before "Beautiful")
        val cursor = 6
        val text1 = p.text.substring(0, cursor)
        val text2 = p.text.substring(cursor)

        val spans1 = p.spans.mapNotNull { span ->
            val s = span.start.coerceIn(0, cursor)
            val e = span.end.coerceIn(0, cursor)
            if (s < e) span.copy(start = s, end = e) else null
        }
        val spans2 = p.spans.mapNotNull { span ->
            if (span.end > cursor) {
                val s = (span.start - cursor).coerceAtLeast(0)
                val e = (span.end - cursor).coerceAtLeast(0)
                if (s < e) span.copy(start = s, end = e) else null
            } else null
        }

        assertEquals("Hello ", text1)
        assertEquals("Beautiful World", text2)
        assertEquals(0, spans1.size)
        assertEquals(1, spans2.size)
        assertEquals(0, spans2[0].start)
        assertEquals(9, spans2[0].end) // "Beautiful" length is 9
        assertTrue(spans2[0].isBold)
    }

    @Test
    fun testAnnotatedTextGuaranteesExactLength() {
        val spans = listOf(
            InlineSpan(start = 0, end = 5, isBold = true),
            InlineSpan(start = 10, end = 25, isItalic = true)
        )
        // Shorter input text than spans
        val shortText = "Hi"
        val annotatedShort = EditorEngineIntegration.buildAnnotatedText(shortText, spans, isRtl = false)
        assertEquals(shortText.length, annotatedShort.length)

        // Longer input text than spans
        val longText = "This is a much longer text that exceeds the span definitions completely."
        val annotatedLong = EditorEngineIntegration.buildAnnotatedText(longText, spans, isRtl = false)
        assertEquals(longText.length, annotatedLong.length)

        // Empty text
        val emptyText = ""
        val annotatedEmpty = EditorEngineIntegration.buildAnnotatedText(emptyText, spans, isRtl = false)
        assertEquals(0, annotatedEmpty.length)
    }
}


