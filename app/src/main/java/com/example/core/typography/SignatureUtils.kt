package com.example.core.typography

import java.util.Locale

/**
 * Utility for normalizing, encoding, and reconstructing vector signature strokes.
 * Converts freeform touch coordinates to normalized [0.0 .. 1.0] coordinates and back,
 * ensuring high fidelity rendering across Compose Canvas, PDF Canvas, and DOCX without distortion.
 */
object SignatureUtils {

    /**
     * Serializes multi-stroke vector coordinates into a compact string representation.
     * Format: "x1,y1;x2,y2|x3,y3;x4,y4" where coordinates are normalized to [0.0 .. 1.0].
     */
    fun encodeNormalizedStrokes(
        strokes: List<List<Pair<Float, Float>>>,
        canvasWidth: Float,
        canvasHeight: Float
    ): String {
        if (strokes.isEmpty()) return ""
        val safeW = if (canvasWidth > 0f) canvasWidth else 1f
        val safeH = if (canvasHeight > 0f) canvasHeight else 1f

        return strokes.joinToString("|") { stroke ->
            stroke.joinToString(";") { (x, y) ->
                val normX = (x / safeW).coerceIn(0f, 1f)
                val normY = (y / safeH).coerceIn(0f, 1f)
                "${"%.4f".format(Locale.US, normX)},${"%.4f".format(Locale.US, normY)}"
            }
        }
    }

    /**
     * Decodes normalized vector strokes into absolute coordinate pairs for a target dimension.
     */
    fun decodeNormalizedStrokes(
        encoded: String,
        targetWidth: Float,
        targetHeight: Float
    ): List<List<Pair<Float, Float>>> {
        if (encoded.isBlank() || !encoded.contains(",")) return emptyList()

        return try {
            encoded.split("|").mapNotNull { strokeSegment ->
                val points = strokeSegment.split(";").mapNotNull { pt ->
                    val coords = pt.split(",")
                    if (coords.size == 2) {
                        val nx = coords[0].toFloatOrNull() ?: return@mapNotNull null
                        val ny = coords[1].toFloatOrNull() ?: return@mapNotNull null
                        Pair(nx * targetWidth, ny * targetHeight)
                    } else null
                }
                if (points.isNotEmpty()) points else null
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    /**
     * Generates an SVG path data string (`d="..."`) for embedding into OpenXML or vector formats.
     */
    fun toSvgPathData(
        encoded: String,
        targetWidth: Float,
        targetHeight: Float
    ): String {
        val strokes = decodeNormalizedStrokes(encoded, targetWidth, targetHeight)
        if (strokes.isEmpty()) return ""

        val sb = StringBuilder()
        for (stroke in strokes) {
            if (stroke.isEmpty()) continue
            sb.append("M ${"%.1f".format(Locale.US, stroke.first().first)} ${"%.1f".format(Locale.US, stroke.first().second)} ")
            for (i in 1 until stroke.size) {
                sb.append("L ${"%.1f".format(Locale.US, stroke[i].first)} ${"%.1f".format(Locale.US, stroke[i].second)} ")
            }
        }
        return sb.toString().trim()
    }
}
