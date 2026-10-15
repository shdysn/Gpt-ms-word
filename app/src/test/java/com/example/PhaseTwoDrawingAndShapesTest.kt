package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.DrawingBlock
import com.example.data.model.DrawingStroke
import com.example.data.model.ParagraphBlock
import com.example.data.model.RichDocument
import com.example.data.model.ShapeBlock
import com.example.data.storage.DocumentStorageManager
import com.example.ui.editor.components.dialogs.VectorShapePresets
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class PhaseTwoDrawingAndShapesTest {

    private lateinit var context: Context
    private lateinit var storageManager: DocumentStorageManager

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        storageManager = DocumentStorageManager(context)
    }

    @Test
    fun testDrawingBlockSerializationRoundTrip() {
        val strokes = listOf(
            DrawingStroke(
                points = listOf(10f to 20f, 15f to 25f, 20f to 30f),
                colorHex = "#1E40AF",
                strokeWidthPt = 3.5f,
                isHighlighter = false
            ),
            DrawingStroke(
                points = listOf(50f to 60f, 70f to 80f),
                colorHex = "#FEF08A",
                strokeWidthPt = 7.0f,
                isHighlighter = true
            )
        )
        val drawingBlock = DrawingBlock(
            strokes = strokes,
            heightDp = 220,
            backgroundColorHex = "#FFFFFF"
        )
        val doc = RichDocument(
            id = "doc-drawing-test",
            title = "Inking Test Document",
            blocks = listOf(ParagraphBlock(text = "Header text"), drawingBlock)
        )

        val json = storageManager.serializeDocument(doc)
        val restoredDoc = storageManager.deserializeDocument(json)

        assertNotNull(restoredDoc)
        val restoredDrawing = restoredDoc?.blocks?.filterIsInstance<DrawingBlock>()?.firstOrNull()
        assertNotNull(restoredDrawing)
        assertEquals(2, restoredDrawing?.strokes?.size)

        val s1 = restoredDrawing?.strokes?.get(0)
        assertEquals("#1E40AF", s1?.colorHex)
        assertEquals(3.5f, s1?.strokeWidthPt ?: 0f, 0.01f)
        assertEquals(false, s1?.isHighlighter)
        assertEquals(3, s1?.points?.size)

        val s2 = restoredDrawing?.strokes?.get(1)
        assertEquals("#FEF08A", s2?.colorHex)
        assertEquals(7.0f, s2?.strokeWidthPt ?: 0f, 0.01f)
        assertEquals(true, s2?.isHighlighter)
        assertEquals(2, s2?.points?.size)
    }

    @Test
    fun testVectorShapeBlockSerializationRoundTrip() {
        val shape = ShapeBlock(
            shapeType = "STAR",
            title = "Feature Highlight",
            content = "This is a 5-pointed geometric vector star with rotation",
            backgroundColorHex = "#FEF3C7",
            borderColorHex = "#F59E0B",
            textColorHex = "#78350F",
            strokeWidthPt = 2.0f,
            rotationDeg = 45f,
            widthDp = 240,
            heightDp = 120
        )
        val doc = RichDocument(
            id = "doc-shape-test",
            title = "Vector Shapes Test",
            blocks = listOf(shape)
        )

        val json = storageManager.serializeDocument(doc)
        val restoredDoc = storageManager.deserializeDocument(json)

        assertNotNull(restoredDoc)
        val restoredShape = restoredDoc?.blocks?.filterIsInstance<ShapeBlock>()?.firstOrNull()
        assertNotNull(restoredShape)
        assertEquals("STAR", restoredShape?.shapeType)
        assertEquals("Feature Highlight", restoredShape?.title)
        assertEquals("This is a 5-pointed geometric vector star with rotation", restoredShape?.content)
        assertEquals("#FEF3C7", restoredShape?.backgroundColorHex)
        assertEquals("#F59E0B", restoredShape?.borderColorHex)
        assertEquals("#78350F", restoredShape?.textColorHex)
        assertEquals(2.0f, restoredShape?.strokeWidthPt ?: 0f, 0.01f)
        assertEquals(45f, restoredShape?.rotationDeg ?: 0f, 0.01f)
        assertEquals(240, restoredShape?.widthDp)
        assertEquals(120, restoredShape?.heightDp)
    }

    @Test
    fun testVectorShapePresetsIntegrity() {
        val shapes = VectorShapePresets.shapes
        assertTrue(shapes.any { it.type == "STAR" })
        assertTrue(shapes.any { it.type == "DIAMOND" })
        assertTrue(shapes.any { it.type == "OVAL" })
        assertTrue(shapes.any { it.type == "ARROW_RIGHT" })
        assertTrue(shapes.any { it.type == "ARROW_LEFT" })
        assertTrue(shapes.any { it.type == "CALLOUT" })
        assertTrue(shapes.any { it.type == "NOTE" })
        assertTrue(shapes.any { it.type == "WARNING" })

        val themes = VectorShapePresets.colorThemes
        assertTrue(themes.isNotEmpty())
        assertTrue(themes.any { it.third == "Blue" })
        assertTrue(themes.any { it.third == "Amber" })
    }
}
