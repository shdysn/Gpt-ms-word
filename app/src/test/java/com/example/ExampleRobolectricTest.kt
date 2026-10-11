package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.core.layout.PageLayoutConfig
import com.example.core.layout.PageSize
import com.example.data.model.ParagraphBlock
import com.example.data.model.RichDocument
import com.example.editor.engine.UndoRedoManager
import com.example.editor.engine.UpdateParagraphTextCommand
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Smart Word", appName)
    }

    @Test
    fun `test document page layout dimensions`() {
        val config = PageLayoutConfig(pageSize = PageSize.A4)
        assertEquals(595, config.pageWidthPt)
        assertEquals(842, config.pageHeightPt)
        assertTrue(config.printableWidthPt > 0)
        assertTrue(config.printableHeightPt > 0)
    }

    @Test
    fun `test undo redo command workflow`() {
        val undoManager = UndoRedoManager()
        val initialDoc = RichDocument(
            blocks = listOf(ParagraphBlock(id = "p1", text = "Original Text"))
        )

        val command = UpdateParagraphTextCommand("p1", "Original Text", "Modified Text")
        val editedDoc = undoManager.execute(command, initialDoc)

        assertEquals("Modified Text", (editedDoc.blocks[0] as ParagraphBlock).text)
        assertTrue(undoManager.canUndo)

        val (undoneDoc, _) = undoManager.undo(editedDoc)
        assertEquals("Original Text", (undoneDoc.blocks[0] as ParagraphBlock).text)
        assertTrue(undoManager.canRedo)

        val (redoneDoc, _) = undoManager.redo(undoneDoc)
        assertEquals("Modified Text", (redoneDoc.blocks[0] as ParagraphBlock).text)
    }

    @Test
    fun `test change list type command`() {
        val undoManager = UndoRedoManager()
        val initialDoc = RichDocument(
            blocks = listOf(ParagraphBlock(id = "p1", text = "Item 1", listType = com.example.data.model.ListType.NONE))
        )

        val command = com.example.editor.engine.ChangeListTypeCommand(
            blockId = "p1",
            oldListType = com.example.data.model.ListType.NONE,
            newListType = com.example.data.model.ListType.BULLET
        )
        val bulletDoc = undoManager.execute(command, initialDoc)
        assertEquals(com.example.data.model.ListType.BULLET, (bulletDoc.blocks[0] as ParagraphBlock).listType)

        val (revertedDoc, _) = undoManager.undo(bulletDoc)
        assertEquals(com.example.data.model.ListType.NONE, (revertedDoc.blocks[0] as ParagraphBlock).listType)
    }

    @Test
    fun `test batch replace command`() {
        val initialDoc = RichDocument(
            blocks = listOf(ParagraphBlock(id = "p1", text = "Hello World"))
        )
        val replacedDoc = RichDocument(
            blocks = listOf(ParagraphBlock(id = "p1", text = "Hello Android"))
        )

        val command = com.example.editor.engine.BatchReplaceTextCommand(
            oldDocument = initialDoc,
            newDocument = replacedDoc,
            replacementsCount = 1
        )
        val undoManager = UndoRedoManager()
        val resultDoc = undoManager.execute(command, initialDoc)
        assertEquals("Hello Android", (resultDoc.blocks[0] as ParagraphBlock).text)

        val (undone, _) = undoManager.undo(resultDoc)
        assertEquals("Hello World", (undone.blocks[0] as ParagraphBlock).text)
    }
}
