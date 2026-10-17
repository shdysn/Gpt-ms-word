package com.example.ui.editor.components.bottomsheet

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.OfficeFontFamily

private val OfficeBlue = Color(0xFF0078D4)
private val LineColor = Color(0xFF323130)

/**
 * 2-Tone Bullets Icon: 3 blue square dots + 3 horizontal lines (Screenshot 7).
 */
@Composable
fun WordBulletsIcon(modifier: Modifier = Modifier.size(20.dp)) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val stroke = 1.6.dp.toPx()
        val dotSize = 2.4.dp.toPx()
        val ySpacing = h / 4f

        for (i in 1..3) {
            val y = ySpacing * i
            // Blue bullet dot
            drawRect(
                color = OfficeBlue,
                topLeft = Offset(0f, y - dotSize / 2),
                size = androidx.compose.ui.geometry.Size(dotSize, dotSize)
            )
            // Horizontal line
            drawLine(
                color = LineColor,
                start = Offset(dotSize + 4.dp.toPx(), y),
                end = Offset(w, y),
                strokeWidth = stroke,
                cap = StrokeCap.Round
            )
        }
    }
}

/**
 * 2-Tone Numbering Icon: Blue 1, 2, 3 + 3 horizontal lines (Screenshot 7).
 */
@Composable
fun WordNumberingIcon(modifier: Modifier = Modifier.size(20.dp)) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val stroke = 1.6.dp.toPx()
        val ySpacing = h / 4f
        val leftTextWidth = 5.dp.toPx()

        for (i in 1..3) {
            val y = ySpacing * i
            drawLine(
                color = LineColor,
                start = Offset(leftTextWidth + 3.dp.toPx(), y),
                end = Offset(w, y),
                strokeWidth = stroke,
                cap = StrokeCap.Round
            )
        }
    }
}

/**
 * Decrease Indent Icon: horizontal lines with blue left arrow (Screenshot 7).
 */
@Composable
fun WordDecreaseIndentIcon(modifier: Modifier = Modifier.size(22.dp)) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val stroke = 1.5.dp.toPx()
        val ySpacing = h / 5f

        // 4 lines
        drawLine(LineColor, Offset(0f, ySpacing * 1), Offset(w, ySpacing * 1), strokeWidth = stroke, cap = StrokeCap.Round)
        drawLine(LineColor, Offset(w * 0.45f, ySpacing * 2), Offset(w, ySpacing * 2), strokeWidth = stroke, cap = StrokeCap.Round)
        drawLine(LineColor, Offset(w * 0.45f, ySpacing * 3), Offset(w, ySpacing * 3), strokeWidth = stroke, cap = StrokeCap.Round)
        drawLine(LineColor, Offset(0f, ySpacing * 4), Offset(w, ySpacing * 4), strokeWidth = stroke, cap = StrokeCap.Round)

        // Blue Left Arrow between lines 2 and 3
        val arrowY = (ySpacing * 2 + ySpacing * 3) / 2
        val arrowHeadX = 2.dp.toPx()
        val arrowTailX = w * 0.35f
        drawLine(OfficeBlue, Offset(arrowHeadX, arrowY), Offset(arrowTailX, arrowY), strokeWidth = 1.8.dp.toPx(), cap = StrokeCap.Round)
        drawLine(OfficeBlue, Offset(arrowHeadX, arrowY), Offset(arrowHeadX + 3.5.dp.toPx(), arrowY - 3.5.dp.toPx()), strokeWidth = 1.8.dp.toPx(), cap = StrokeCap.Round)
        drawLine(OfficeBlue, Offset(arrowHeadX, arrowY), Offset(arrowHeadX + 3.5.dp.toPx(), arrowY + 3.5.dp.toPx()), strokeWidth = 1.8.dp.toPx(), cap = StrokeCap.Round)
    }
}

/**
 * Increase Indent Icon: horizontal lines with blue right arrow (Screenshot 7).
 */
@Composable
fun WordIncreaseIndentIcon(modifier: Modifier = Modifier.size(22.dp)) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val stroke = 1.5.dp.toPx()
        val ySpacing = h / 5f

        // 4 lines
        drawLine(LineColor, Offset(0f, ySpacing * 1), Offset(w, ySpacing * 1), strokeWidth = stroke, cap = StrokeCap.Round)
        drawLine(LineColor, Offset(w * 0.45f, ySpacing * 2), Offset(w, ySpacing * 2), strokeWidth = stroke, cap = StrokeCap.Round)
        drawLine(LineColor, Offset(w * 0.45f, ySpacing * 3), Offset(w, ySpacing * 3), strokeWidth = stroke, cap = StrokeCap.Round)
        drawLine(LineColor, Offset(0f, ySpacing * 4), Offset(w, ySpacing * 4), strokeWidth = stroke, cap = StrokeCap.Round)

        // Blue Right Arrow
        val arrowY = (ySpacing * 2 + ySpacing * 3) / 2
        val arrowHeadX = w * 0.35f
        val arrowTailX = 2.dp.toPx()
        drawLine(OfficeBlue, Offset(arrowTailX, arrowY), Offset(arrowHeadX, arrowY), strokeWidth = 1.8.dp.toPx(), cap = StrokeCap.Round)
        drawLine(OfficeBlue, Offset(arrowHeadX, arrowY), Offset(arrowHeadX - 3.5.dp.toPx(), arrowY - 3.5.dp.toPx()), strokeWidth = 1.8.dp.toPx(), cap = StrokeCap.Round)
        drawLine(OfficeBlue, Offset(arrowHeadX, arrowY), Offset(arrowHeadX - 3.5.dp.toPx(), arrowY + 3.5.dp.toPx()), strokeWidth = 1.8.dp.toPx(), cap = StrokeCap.Round)
    }
}

/**
 * LTR Paragraph Direction Icon: blue > followed by pilcrow ¶ (Screenshot 7).
 */
@Composable
fun WordLtrDirectionIcon(modifier: Modifier = Modifier.size(22.dp)) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Text(text = ">", color = OfficeBlue, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        Text(text = "¶", color = LineColor, fontSize = 17.sp, fontWeight = FontWeight.Bold)
    }
}

/**
 * RTL Paragraph Direction Icon: pilcrow ¶ followed by blue < (Screenshot 7).
 */
@Composable
fun WordRtlDirectionIcon(modifier: Modifier = Modifier.size(22.dp)) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Text(text = "¶", color = LineColor, fontSize = 17.sp, fontWeight = FontWeight.Bold)
        Text(text = "<", color = OfficeBlue, fontSize = 16.sp, fontWeight = FontWeight.Bold)
    }
}

/**
 * Align Left: 4 proportional horizontal lines (Screenshot 7).
 */
@Composable
fun WordAlignLeftIcon(modifier: Modifier = Modifier.size(20.dp)) {
    Canvas(modifier = modifier) {
        val stroke = 1.6.dp.toPx()
        val ySpacing = size.height / 5f
        drawLine(LineColor, Offset(0f, ySpacing * 1), Offset(size.width, ySpacing * 1), strokeWidth = stroke, cap = StrokeCap.Round)
        drawLine(LineColor, Offset(0f, ySpacing * 2), Offset(size.width * 0.7f, ySpacing * 2), strokeWidth = stroke, cap = StrokeCap.Round)
        drawLine(LineColor, Offset(0f, ySpacing * 3), Offset(size.width, ySpacing * 3), strokeWidth = stroke, cap = StrokeCap.Round)
        drawLine(LineColor, Offset(0f, ySpacing * 4), Offset(size.width * 0.5f, ySpacing * 4), strokeWidth = stroke, cap = StrokeCap.Round)
    }
}

/**
 * Align Center: 4 centered proportional horizontal lines (Screenshot 7).
 */
@Composable
fun WordAlignCenterIcon(modifier: Modifier = Modifier.size(20.dp)) {
    Canvas(modifier = modifier) {
        val stroke = 1.6.dp.toPx()
        val ySpacing = size.height / 5f
        val w = size.width
        drawLine(LineColor, Offset(0f, ySpacing * 1), Offset(w, ySpacing * 1), strokeWidth = stroke, cap = StrokeCap.Round)
        drawLine(LineColor, Offset(w * 0.15f, ySpacing * 2), Offset(w * 0.85f, ySpacing * 2), strokeWidth = stroke, cap = StrokeCap.Round)
        drawLine(LineColor, Offset(0f, ySpacing * 3), Offset(w, ySpacing * 3), strokeWidth = stroke, cap = StrokeCap.Round)
        drawLine(LineColor, Offset(w * 0.25f, ySpacing * 4), Offset(w * 0.75f, ySpacing * 4), strokeWidth = stroke, cap = StrokeCap.Round)
    }
}

/**
 * Align Right: 4 right-aligned proportional horizontal lines (Screenshot 7).
 */
@Composable
fun WordAlignRightIcon(modifier: Modifier = Modifier.size(20.dp)) {
    Canvas(modifier = modifier) {
        val stroke = 1.6.dp.toPx()
        val ySpacing = size.height / 5f
        val w = size.width
        drawLine(LineColor, Offset(0f, ySpacing * 1), Offset(w, ySpacing * 1), strokeWidth = stroke, cap = StrokeCap.Round)
        drawLine(LineColor, Offset(w * 0.3f, ySpacing * 2), Offset(w, ySpacing * 2), strokeWidth = stroke, cap = StrokeCap.Round)
        drawLine(LineColor, Offset(0f, ySpacing * 3), Offset(w, ySpacing * 3), strokeWidth = stroke, cap = StrokeCap.Round)
        drawLine(LineColor, Offset(w * 0.5f, ySpacing * 4), Offset(w, ySpacing * 4), strokeWidth = stroke, cap = StrokeCap.Round)
    }
}

/**
 * Align Justify: 4 full-width equal horizontal lines (Screenshot 7).
 */
@Composable
fun WordAlignJustifyIcon(modifier: Modifier = Modifier.size(20.dp)) {
    Canvas(modifier = modifier) {
        val stroke = 1.6.dp.toPx()
        val ySpacing = size.height / 5f
        val w = size.width
        for (i in 1..4) {
            drawLine(LineColor, Offset(0f, ySpacing * i), Offset(w, ySpacing * i), strokeWidth = stroke, cap = StrokeCap.Round)
        }
    }
}

/**
 * More Formatting Icon: X with blue ₂ subscript (Screenshot 6, 7).
 */
@Composable
fun WordMoreFormattingIcon(modifier: Modifier = Modifier.size(22.dp)) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.Center
    ) {
        Text(
            text = "X",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = LineColor
        )
        Text(
            text = "2",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = OfficeBlue
        )
    }
}
