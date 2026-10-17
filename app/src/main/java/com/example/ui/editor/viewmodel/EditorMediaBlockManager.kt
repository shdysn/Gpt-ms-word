package com.example.ui.editor.viewmodel

import android.content.Context
import android.net.Uri
import com.example.data.model.DrawingBlock
import com.example.data.model.DrawingStroke
import com.example.data.model.ImageBlock
import com.example.data.model.ShapeBlock
import com.example.data.model.SignatureBlock
import com.example.data.model.TextBoxBlock
import com.example.data.model.TextAlignment
import com.example.data.storage.DocumentStorageManager
import com.example.editor.engine.InsertBlockCommand
import com.example.editor.engine.UpdateBlockCommand
import com.example.editor.engine.UpdateImageBlockCommand
import com.example.ui.editor.EditorViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Manages media and graphics blocks: images, shapes, canvas inking, signatures, and floating text boxes.
 */
class EditorMediaBlockManager(
    private val vm: EditorViewModel
) {
    fun insertImageFromUri(context: Context, sourceUri: Uri, caption: String = "") {
        val storageManager = DocumentStorageManager(context)
        val persistentUri = storageManager.persistImageFromUri(sourceUri)
        insertImage(persistentUri, caption)
    }

    fun insertImage(uri: String, caption: String = "") {
        val insertIndex = vm.activeBlockIndex.value + 1
        val imageBlock = ImageBlock(imageUri = uri, caption = caption)
        vm.executeCommand(InsertBlockCommand(index = insertIndex, block = imageBlock))
        vm.setActiveBlock(insertIndex)
    }

    fun updateImageProperties(
        blockId: String,
        widthDp: Int? = null,
        heightDp: Int? = null,
        alignment: TextAlignment? = null,
        caption: String? = null,
        aspectRatio: Float? = null,
        wrapMode: String? = null,
        filterMode: String? = null,
        cornerRadiusDp: Int? = null
    ) {
        val currentDoc = vm.currentDocument ?: return
        val oldBlock = currentDoc.blocks.firstOrNull { it.id == blockId } as? ImageBlock ?: return
        val newWidth = widthDp ?: oldBlock.widthDp
        val newRatio = aspectRatio ?: oldBlock.aspectRatio
        val newHeight = heightDp ?: if (widthDp != null && newRatio > 0) (widthDp / newRatio).toInt() else oldBlock.heightDp

        val newBlock = oldBlock.copy(
            widthDp = newWidth.coerceIn(60, 600),
            heightDp = newHeight.coerceIn(60, 600),
            alignment = alignment ?: oldBlock.alignment,
            caption = caption ?: oldBlock.caption,
            aspectRatio = newRatio,
            wrapMode = wrapMode ?: oldBlock.wrapMode,
            filterMode = filterMode ?: oldBlock.filterMode,
            cornerRadiusDp = cornerRadiusDp ?: oldBlock.cornerRadiusDp
        )

        vm.executeCommand(
            UpdateImageBlockCommand(blockId, oldBlock, newBlock, "Format Picture Properties")
        )
    }

    fun updateImageWrapMode(blockId: String, wrapMode: String) {
        val doc = vm.currentDocument ?: return
        val currentImg = doc.blocks.filterIsInstance<ImageBlock>().firstOrNull { it.id == blockId } ?: return
        val updated = currentImg.copy(wrapMode = wrapMode)
        vm.executeCommand(UpdateImageBlockCommand(blockId, currentImg, updated))
    }

    fun updateImagePositionAndSize(blockId: String, offsetX: Float, offsetY: Float, widthDp: Int, heightDp: Int) {
        val doc = vm.currentDocument ?: return
        val currentImg = doc.blocks.filterIsInstance<ImageBlock>().firstOrNull { it.id == blockId } ?: return
        val updated = currentImg.copy(
            offsetXPt = offsetX,
            offsetYPt = offsetY,
            widthDp = widthDp.coerceIn(60, 600),
            heightDp = heightDp.coerceIn(60, 600)
        )
        vm.executeCommand(UpdateImageBlockCommand(blockId, currentImg, updated))
    }

    fun updateImageFormatting(
        blockId: String,
        filterMode: String,
        cornerRadiusDp: Int,
        borderThicknessDp: Int,
        borderColorHex: String,
        rotationDegrees: Float
    ) {
        val doc = vm.currentDocument ?: return
        val currentImg = doc.blocks.filterIsInstance<ImageBlock>().firstOrNull { it.id == blockId } ?: return
        val updatedImg = currentImg.copy(
            filterMode = filterMode,
            cornerRadiusDp = cornerRadiusDp,
            borderThicknessDp = borderThicknessDp,
            borderColorHex = borderColorHex,
            rotationDegrees = rotationDegrees
        )
        vm.executeCommand(UpdateImageBlockCommand(currentImg.id, currentImg, updatedImg))
        vm.showStatusMessage("Image properties updated")
    }

    fun insertSignature(signerName: String, signerTitle: String, strokeHex: String, points: String) {
        val insertIndex = (vm.activeBlockIndex.value + 1).coerceIn(0, (vm.currentDocument?.blocks?.size ?: 0))
        val dateStr = SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(Date())
        val sigBlock = SignatureBlock(
            svgPoints = points,
            signerName = signerName,
            signerTitle = signerTitle,
            signedDate = dateStr,
            strokeColorHex = strokeHex
        )
        vm.executeCommand(InsertBlockCommand(insertIndex, sigBlock))
        vm.showStatusMessage("Digital Signature inserted")
    }

    fun insertShape(
        shapeType: String,
        title: String = "",
        content: String = "",
        bgHex: String = "#41719C",
        borderHex: String = "#1B365D",
        strokeWidthPt: Float = 1.5f,
        rotationDeg: Float = 0f,
        widthDp: Int = -1,
        heightDp: Int = -1
    ) {
        val (defW, defH) = getDefaultShapeDimensions(shapeType)
        val finalW = if (widthDp > 0) widthDp else defW
        val finalH = if (heightDp > 0) heightDp else defH
        insertVectorShape(shapeType, title, content, bgHex, borderHex, strokeWidthPt, rotationDeg, finalW, finalH)
    }

    fun insertVectorShape(
        shapeType: String,
        title: String,
        content: String,
        bgHex: String,
        borderHex: String,
        strokeWidthPt: Float = 1.5f,
        rotationDeg: Float = 0f,
        widthDp: Int = 220,
        heightDp: Int = 110
    ) {
        val insertIndex = (vm.activeBlockIndex.value + 1).coerceIn(0, (vm.currentDocument?.blocks?.size ?: 0))
        val shapeBlock = ShapeBlock(
            shapeType = shapeType,
            title = title,
            content = content,
            backgroundColorHex = bgHex,
            borderColorHex = borderHex,
            strokeWidthPt = strokeWidthPt,
            rotationDeg = rotationDeg,
            widthDp = widthDp,
            heightDp = heightDp
        )
        vm.executeCommand(InsertBlockCommand(insertIndex, shapeBlock))
        vm.setActiveBlock(insertIndex)
        vm.showStatusMessage("Shape inserted")
    }

    private fun getDefaultShapeDimensions(shapeType: String): Pair<Int, Int> {
        val upper = shapeType.uppercase()
        return when {
            upper == "LINE" || upper == "ARROW_LINE" || upper == "DOUBLE_ARROW" -> 260 to 24
            upper == "ELBOW" || upper == "CURVE" -> 160 to 80
            upper == "OVAL" || upper == "FLOW_CONN" -> 120 to 120
            upper in listOf("TRIANGLE", "RIGHT_TRIANGLE", "DIAMOND", "FLOW_DECISION", "PENTAGON", "HEXAGON", "OCTAGON", "CROSS", "CUBE", "HEART", "LIGHTNING", "SUN", "MOON", "CLOUD") -> 120 to 120
            upper.startsWith("STAR") || upper == "EXPLOSION" -> 120 to 120
            upper == "ARROW_UP" || upper == "ARROW_DOWN" || upper == "ARROW_UPDOWN" -> 80 to 140
            upper == "ARROW_QUAD" -> 130 to 130
            upper.startsWith("ARROW_") -> 160 to 70
            upper in listOf("PLUS", "MINUS", "MULTIPLY", "DIVIDE", "EQUAL", "NOT_EQUAL") -> 90 to 90
            upper in listOf("RIBBON_UP", "RIBBON_DOWN", "SCROLL", "BANNER_WAVE") -> 220 to 80
            upper.startsWith("CALLOUT") || upper in listOf("NOTE", "WARNING", "SUCCESS", "QUOTE") -> 180 to 100
            else -> 180 to 90
        }
    }

    fun updateShape(
        blockId: String,
        shapeType: String,
        title: String,
        content: String,
        bgHex: String,
        borderHex: String,
        strokeWidthPt: Float = 1.5f,
        rotationDeg: Float = 0f,
        widthDp: Int = 220,
        heightDp: Int = 110
    ) {
        val doc = vm.currentDocument ?: return
        val currentShape = doc.blocks.filterIsInstance<ShapeBlock>().firstOrNull { it.id == blockId } ?: return
        val updated = currentShape.copy(
            shapeType = shapeType,
            title = title,
            content = content,
            backgroundColorHex = bgHex,
            borderColorHex = borderHex,
            strokeWidthPt = strokeWidthPt,
            rotationDeg = rotationDeg,
            widthDp = widthDp,
            heightDp = heightDp
        )
        vm.executeCommand(UpdateBlockCommand(blockId, updated))
        vm.showStatusMessage("Shape ($shapeType) updated")
    }

    fun insertDrawing(heightDp: Int = 180) {
        val insertIndex = (vm.activeBlockIndex.value + 1).coerceIn(0, (vm.currentDocument?.blocks?.size ?: 0))
        val drawingBlock = DrawingBlock(heightDp = heightDp, backgroundColorHex = null)
        vm.executeCommand(InsertBlockCommand(insertIndex, drawingBlock))
        vm.setActiveBlock(insertIndex)
        vm.showStatusMessage("Inking Canvas ready - Draw with touch or pen")
    }

    fun updateDrawingStrokes(blockId: String, strokes: List<DrawingStroke>) {
        val doc = vm.currentDocument ?: return
        val currentDrawing = doc.blocks.filterIsInstance<DrawingBlock>().firstOrNull { it.id == blockId } ?: return
        val updated = currentDrawing.copy(strokes = strokes)
        vm.executeCommand(UpdateBlockCommand(blockId, updated))
    }

    fun insertTextBox(text: String = "Type text here...") {
        val insertIndex = (vm.activeBlockIndex.value + 1).coerceIn(0, (vm.currentDocument?.blocks?.size ?: 0))
        val tbBlock = TextBoxBlock(
            text = text,
            widthDp = 180,
            heightDp = 100,
            backgroundColorHex = "#FFFFFF",
            borderColorHex = "#2563EB",
            borderWidthPt = 1.5f
        )
        vm.executeCommand(InsertBlockCommand(insertIndex, tbBlock))
        vm.showStatusMessage("Floating Text Box inserted")
    }

    fun updateTextBox(
        blockId: String,
        text: String,
        widthDp: Int = 180,
        heightDp: Int = 100,
        bgHex: String = "#FFFFFF",
        borderHex: String = "#2563EB"
    ) {
        val doc = vm.currentDocument ?: return
        val block = doc.blocks.filterIsInstance<TextBoxBlock>().firstOrNull { it.id == blockId } ?: return
        val updated = block.copy(
            text = text,
            widthDp = widthDp,
            heightDp = heightDp,
            backgroundColorHex = bgHex,
            borderColorHex = borderHex
        )
        vm.executeCommand(UpdateBlockCommand(blockId, updated))
    }

    fun updateTextBoxGeometry(
        blockId: String,
        offsetX: Float,
        offsetY: Float,
        widthDp: Int,
        heightDp: Int
    ) {
        val doc = vm.currentDocument ?: return
        val block = doc.blocks.filterIsInstance<TextBoxBlock>().firstOrNull { it.id == blockId } ?: return
        val updated = block.copy(
            offsetXPt = offsetX,
            offsetYPt = offsetY,
            widthDp = widthDp.coerceIn(80, 500),
            heightDp = heightDp.coerceIn(50, 400)
        )
        vm.executeCommand(UpdateBlockCommand(blockId, updated))
    }
}
