package com.example.ui.editor.components.bottomsheet

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val IconDarkColor = Color(0xFF333333)
private val OfficeBlue = Color(0xFF0F6CBD)
private val AccentRed = Color(0xFFD83B01)
private val AccentOrange = Color(0xFFED6C02)

@Composable
fun WordBlankPageIcon(modifier: Modifier = Modifier.size(24.dp)) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val pad = 2.dp.toPx()
        val corner = 5.dp.toPx()
        val path = Path().apply {
            moveTo(pad, pad)
            lineTo(w - pad - corner, pad)
            lineTo(w - pad, pad + corner)
            lineTo(w - pad, h - pad)
            lineTo(pad, h - pad)
            close()
        }
        drawPath(path, color = IconDarkColor, style = Stroke(width = 1.6.dp.toPx(), cap = StrokeCap.Round))
        val fold = Path().apply {
            moveTo(w - pad - corner, pad)
            lineTo(w - pad - corner, pad + corner)
            lineTo(w - pad, pad + corner)
        }
        drawPath(fold, color = IconDarkColor, style = Stroke(width = 1.4.dp.toPx(), cap = StrokeCap.Round))
    }
}

@Composable
fun WordScanDocIcon(modifier: Modifier = Modifier.size(24.dp)) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val sw = 1.6.dp.toPx()
        val c = 4.dp.toPx()
        drawRoundRect(color = IconDarkColor, topLeft = Offset(1.dp.toPx(), 4.dp.toPx()), size = Size(w - 2.dp.toPx(), h - 6.dp.toPx()), cornerRadius = CornerRadius(c, c), style = Stroke(width = sw))
        drawCircle(color = IconDarkColor, radius = 4.5.dp.toPx(), center = Offset(w / 2f, h / 2f + 1.dp.toPx()), style = Stroke(width = sw))
        drawCircle(color = IconDarkColor, radius = 1.5.dp.toPx(), center = Offset(w - 5.dp.toPx(), 7.5.dp.toPx()), style = Fill)
    }
}

@Composable
fun WordTableGridIcon(modifier: Modifier = Modifier.size(24.dp)) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val sw = 1.5.dp.toPx()
        val pad = 2.dp.toPx()
        val rectW = w - pad * 2
        val rectH = h - pad * 2
        drawRoundRect(color = IconDarkColor, topLeft = Offset(pad, pad), size = Size(rectW, rectH), cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx()), style = Stroke(width = sw))
        // horizontal rows
        val r1 = pad + rectH / 3f
        val r2 = pad + (rectH * 2f) / 3f
        drawLine(IconDarkColor, Offset(pad, r1), Offset(w - pad, r1), strokeWidth = sw)
        drawLine(IconDarkColor, Offset(pad, r2), Offset(w - pad, r2), strokeWidth = sw)
        // vertical columns
        val c1 = pad + rectW / 3f
        val c2 = pad + (rectW * 2f) / 3f
        drawLine(IconDarkColor, Offset(c1, pad), Offset(c1, h - pad), strokeWidth = sw)
        drawLine(IconDarkColor, Offset(c2, pad), Offset(c2, h - pad), strokeWidth = sw)
    }
}

@Composable
fun WordPicturesIcon(modifier: Modifier = Modifier.size(24.dp)) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val sw = 1.6.dp.toPx()
        val pad = 2.dp.toPx()
        drawRoundRect(color = IconDarkColor, topLeft = Offset(pad, pad + 1.dp.toPx()), size = Size(w - pad * 2, h - pad * 2 - 2.dp.toPx()), cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx()), style = Stroke(width = sw))
        drawCircle(color = OfficeBlue, radius = 2.4.dp.toPx(), center = Offset(pad + 5.dp.toPx(), pad + 6.dp.toPx()), style = Fill)
        val m = Path().apply {
            moveTo(pad + 2.dp.toPx(), h - pad - 2.dp.toPx())
            lineTo(pad + 7.dp.toPx(), pad + 10.dp.toPx())
            lineTo(pad + 12.dp.toPx(), h - pad - 3.dp.toPx())
            lineTo(pad + 15.dp.toPx(), pad + 8.dp.toPx())
            lineTo(w - pad - 2.dp.toPx(), h - pad - 2.dp.toPx())
        }
        drawPath(m, color = OfficeBlue, style = Stroke(width = sw, cap = StrokeCap.Round))
    }
}

@Composable
fun WordShapesCategoryIcon(modifier: Modifier = Modifier.size(24.dp)) {
    Canvas(modifier = modifier) {
        val sw = 1.6.dp.toPx()
        // Outlined square on left
        drawRect(color = OfficeBlue, topLeft = Offset(2.dp.toPx(), 4.dp.toPx()), size = Size(11.dp.toPx(), 11.dp.toPx()), style = Stroke(width = sw))
        // Outlined circle overlapping on right
        drawCircle(color = IconDarkColor, radius = 6.dp.toPx(), center = Offset(15.dp.toPx(), 13.dp.toPx()), style = Stroke(width = sw))
    }
}

@Composable
fun WordTextBoxIcon(modifier: Modifier = Modifier.size(24.dp)) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val sw = 1.4.dp.toPx()
        val pad = 2.dp.toPx()
        drawRoundRect(color = OfficeBlue, topLeft = Offset(pad, pad + 1.dp.toPx()), size = Size(w - pad * 2, h - pad * 2 - 2.dp.toPx()), cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx()), style = Stroke(width = sw))
    }
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Text("A", color = OfficeBlue, fontSize = 13.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.SansSerif)
    }
}

@Composable
fun WordLinkChainIcon(modifier: Modifier = Modifier.size(24.dp)) {
    Canvas(modifier = modifier) {
        val sw = 1.8.dp.toPx()
        val p1 = Path().apply {
            moveTo(6.dp.toPx(), 14.dp.toPx())
            lineTo(4.dp.toPx(), 16.dp.toPx())
            arcTo(androidx.compose.ui.geometry.Rect(2.dp.toPx(), 14.dp.toPx(), 8.dp.toPx(), 20.dp.toPx()), 90f, 180f, false)
            lineTo(9.dp.toPx(), 15.dp.toPx())
        }
        drawPath(p1, color = IconDarkColor, style = Stroke(width = sw, cap = StrokeCap.Round))
        drawLine(IconDarkColor, Offset(9.dp.toPx(), 15.dp.toPx()), Offset(15.dp.toPx(), 9.dp.toPx()), strokeWidth = sw, cap = StrokeCap.Round)
        val p2 = Path().apply {
            moveTo(15.dp.toPx(), 9.dp.toPx())
            lineTo(18.dp.toPx(), 6.dp.toPx())
            arcTo(androidx.compose.ui.geometry.Rect(16.dp.toPx(), 4.dp.toPx(), 22.dp.toPx(), 10.dp.toPx()), 270f, 180f, false)
            lineTo(15.dp.toPx(), 9.dp.toPx())
        }
        drawPath(p2, color = IconDarkColor, style = Stroke(width = sw, cap = StrokeCap.Round))
    }
}

@Composable
fun WordCommentBubbleIcon(modifier: Modifier = Modifier.size(24.dp)) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val sw = 1.6.dp.toPx()
        val path = Path().apply {
            moveTo(3.dp.toPx(), 5.dp.toPx())
            lineTo(w - 3.dp.toPx(), 5.dp.toPx())
            lineTo(w - 3.dp.toPx(), h - 8.dp.toPx())
            lineTo(9.dp.toPx(), h - 8.dp.toPx())
            lineTo(4.dp.toPx(), h - 3.dp.toPx())
            lineTo(4.dp.toPx(), h - 8.dp.toPx())
            lineTo(3.dp.toPx(), h - 8.dp.toPx())
            close()
        }
        drawPath(path, color = IconDarkColor, style = Stroke(width = sw, cap = StrokeCap.Round))
        // Green plus symbol inside/near
        val px = 8.dp.toPx()
        val py = 9.dp.toPx()
        val len = 4.dp.toPx()
        drawLine(Color(0xFF107C41), Offset(px - len / 2f, py), Offset(px + len / 2f, py), strokeWidth = 1.6.dp.toPx(), cap = StrokeCap.Round)
        drawLine(Color(0xFF107C41), Offset(px, py - len / 2f), Offset(px, py + len / 2f), strokeWidth = 1.6.dp.toPx(), cap = StrokeCap.Round)
    }
}

@Composable
fun WordHeaderFooterPageIcon(modifier: Modifier = Modifier.size(24.dp)) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val pad = 3.dp.toPx()
        drawRoundRect(color = IconDarkColor, topLeft = Offset(pad, pad), size = Size(w - pad * 2, h - pad * 2), cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx()), style = Stroke(width = 1.5.dp.toPx()))
        // Top orange/gold bar
        drawRect(color = AccentOrange, topLeft = Offset(pad + 2.dp.toPx(), pad + 2.dp.toPx()), size = Size(w - pad * 2 - 4.dp.toPx(), 3.dp.toPx()))
        // Bottom orange/gold bar
        drawRect(color = AccentOrange, topLeft = Offset(pad + 2.dp.toPx(), h - pad - 5.dp.toPx()), size = Size(w - pad * 2 - 4.dp.toPx(), 3.dp.toPx()))
    }
}

@Composable
fun WordPageNumberPageIcon(modifier: Modifier = Modifier.size(24.dp)) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.matchParentSize()) {
            val pad = 3.dp.toPx()
            drawRoundRect(color = IconDarkColor, topLeft = Offset(pad, pad), size = Size(size.width - pad * 2, size.height - pad * 2), cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx()), style = Stroke(width = 1.5.dp.toPx()))
        }
        Text("#", color = OfficeBlue, fontSize = 13.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun WordEquationPiIcon(modifier: Modifier = Modifier.size(24.dp)) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Text("π", color = IconDarkColor, fontSize = 18.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun WordFootnoteAbIcon(modifier: Modifier = Modifier.size(24.dp)) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        androidx.compose.foundation.layout.Row(verticalAlignment = Alignment.Top) {
            Text("ab", color = IconDarkColor, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            Text("1", color = AccentRed, fontSize = 10.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun WordEndnoteIcon(modifier: Modifier = Modifier.size(24.dp)) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.matchParentSize()) {
            drawRoundRect(color = IconDarkColor, topLeft = Offset(2.dp.toPx(), 2.dp.toPx()), size = Size(14.dp.toPx(), 18.dp.toPx()), cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx()), style = Stroke(width = 1.4.dp.toPx()))
        }
        Text("i", color = OfficeBlue, fontSize = 11.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun WordTableRowIcon(modifier: Modifier = Modifier.size(24.dp)) {
    Canvas(modifier = modifier) {
        val sw = 1.5.dp.toPx()
        drawRect(color = IconDarkColor, topLeft = Offset(8.dp.toPx(), 3.dp.toPx()), size = Size(14.dp.toPx(), 18.dp.toPx()), style = Stroke(width = sw))
        drawLine(IconDarkColor, Offset(8.dp.toPx(), 9.dp.toPx()), Offset(22.dp.toPx(), 9.dp.toPx()), strokeWidth = sw)
        drawLine(IconDarkColor, Offset(8.dp.toPx(), 15.dp.toPx()), Offset(22.dp.toPx(), 15.dp.toPx()), strokeWidth = sw)
        // Arrow pointing into row
        drawLine(OfficeBlue, Offset(1.dp.toPx(), 12.dp.toPx()), Offset(11.dp.toPx(), 12.dp.toPx()), strokeWidth = 1.8.dp.toPx(), cap = StrokeCap.Round)
        drawLine(OfficeBlue, Offset(8.dp.toPx(), 9.dp.toPx()), Offset(11.dp.toPx(), 12.dp.toPx()), strokeWidth = 1.8.dp.toPx(), cap = StrokeCap.Round)
        drawLine(OfficeBlue, Offset(8.dp.toPx(), 15.dp.toPx()), Offset(11.dp.toPx(), 12.dp.toPx()), strokeWidth = 1.8.dp.toPx(), cap = StrokeCap.Round)
    }
}

@Composable
fun WordTableColumnIcon(modifier: Modifier = Modifier.size(24.dp)) {
    Canvas(modifier = modifier) {
        val sw = 1.5.dp.toPx()
        drawRect(color = IconDarkColor, topLeft = Offset(3.dp.toPx(), 8.dp.toPx()), size = Size(18.dp.toPx(), 14.dp.toPx()), style = Stroke(width = sw))
        drawLine(IconDarkColor, Offset(9.dp.toPx(), 8.dp.toPx()), Offset(9.dp.toPx(), 22.dp.toPx()), strokeWidth = sw)
        drawLine(IconDarkColor, Offset(15.dp.toPx(), 8.dp.toPx()), Offset(15.dp.toPx(), 22.dp.toPx()), strokeWidth = sw)
        // Arrow pointing up into column
        drawLine(OfficeBlue, Offset(12.dp.toPx(), 23.dp.toPx()), Offset(12.dp.toPx(), 13.dp.toPx()), strokeWidth = 1.8.dp.toPx(), cap = StrokeCap.Round)
        drawLine(OfficeBlue, Offset(9.dp.toPx(), 16.dp.toPx()), Offset(12.dp.toPx(), 13.dp.toPx()), strokeWidth = 1.8.dp.toPx(), cap = StrokeCap.Round)
        drawLine(OfficeBlue, Offset(15.dp.toPx(), 16.dp.toPx()), Offset(12.dp.toPx(), 13.dp.toPx()), strokeWidth = 1.8.dp.toPx(), cap = StrokeCap.Round)
    }
}

@Composable
fun WordRemovePageNumberIcon(modifier: Modifier = Modifier.size(24.dp)) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.matchParentSize()) {
            val pad = 3.dp.toPx()
            drawRoundRect(color = IconDarkColor, topLeft = Offset(pad, pad), size = Size(size.width - pad * 2, size.height - pad * 2), cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx()), style = Stroke(width = 1.4.dp.toPx()))
            // Red cross over corner
            val cx = size.width - 4.dp.toPx()
            val cy = size.height - 4.dp.toPx()
            val s = 4.dp.toPx()
            drawLine(AccentRed, Offset(cx - s, cy - s), Offset(cx + s, cy + s), strokeWidth = 2.dp.toPx(), cap = StrokeCap.Round)
            drawLine(AccentRed, Offset(cx - s, cy + s), Offset(cx + s, cy - s), strokeWidth = 2.dp.toPx(), cap = StrokeCap.Round)
        }
        Text("#", color = IconDarkColor.copy(alpha = 0.6f), fontSize = 12.sp, fontWeight = FontWeight.Bold)
    }
}
