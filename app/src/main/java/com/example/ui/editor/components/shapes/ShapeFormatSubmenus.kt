package com.example.ui.editor.components.shapes

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.RotateRight
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun FillColorSubmenu(currentHex: String, onSelectHex: (String) -> Unit) {
    val colors = listOf(
        "#41719C" to "Word Blue",
        "#1B365D" to "Navy",
        "#2E75B6" to "Light Blue",
        "#D97706" to "Amber",
        "#059669" to "Emerald",
        "#DC2626" to "Crimson",
        "#7C3AED" to "Purple",
        "#475569" to "Slate",
        "#EFF6FF" to "Soft Blue",
        "#FFFFFF" to "White",
        "#000000" to "Black"
    )
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("Fill: ", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF475569))
        colors.forEach { (hex, _) ->
            val color = try { Color(android.graphics.Color.parseColor(hex)) } catch (_: Exception) { Color.Gray }
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(color)
                    .border(
                        if (currentHex.equals(hex, ignoreCase = true)) 2.5.dp else 1.dp,
                        if (currentHex.equals(hex, ignoreCase = true)) Color(0xFF2563EB) else Color(0xFF94A3B8),
                        CircleShape
                    )
                    .clickable { onSelectHex(hex) }
            )
        }
    }
}

@Composable
fun OutlineSubmenu(currentBorderHex: String, currentStrokeWidth: Float, onSelect: (String, Float) -> Unit) {
    val outlineColors = listOf("#1B365D", "#41719C", "#0F172A", "#DC2626", "#059669", "#D97706", "#7C3AED")
    val weights = listOf(1f, 1.5f, 2.5f, 4f)

    Column(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Color: ", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF475569))
            outlineColors.forEach { hex ->
                val color = try { Color(android.graphics.Color.parseColor(hex)) } catch (_: Exception) { Color.Gray }
                Box(
                    modifier = Modifier
                        .size(26.dp)
                        .clip(CircleShape)
                        .background(color)
                        .border(1.5.dp, if (currentBorderHex.equals(hex, ignoreCase = true)) Color(0xFF2563EB) else Color(0xFFCBD5E1), CircleShape)
                        .clickable { onSelect(hex, currentStrokeWidth) }
                )
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Weight: ", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF475569))
            weights.forEach { w ->
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (currentStrokeWidth == w) Color(0xFFEFF6FF) else Color(0xFFF1F5F9))
                        .border(1.dp, if (currentStrokeWidth == w) Color(0xFF2563EB) else Color(0xFFCBD5E1), RoundedCornerShape(6.dp))
                        .clickable { onSelect(currentBorderHex, w) }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text("${w}pt", fontSize = 11.sp, color = if (currentStrokeWidth == w) Color(0xFF1E40AF) else Color(0xFF334155))
                }
            }
        }
    }
}

@Composable
fun ShapeStylesSubmenu(onSelect: (bg: String, border: String) -> Unit) {
    val styles = listOf(
        Pair("#41719C", "#1B365D"),
        Pair("#5B9BD5", "#41719C"),
        Pair("#ED7D31", "#C65911"),
        Pair("#70AD47", "#548235"),
        Pair("#7030A0", "#4B206B"),
        Pair("#EFF6FF", "#3B82F6"),
        Pair("#FFFFFF", "#1B365D")
    )
    Row(
        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("Preset: ", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF475569))
        styles.forEach { (bg, bdr) ->
            val bgCol = try { Color(android.graphics.Color.parseColor(bg)) } catch (_: Exception) { Color.Gray }
            val bdrCol = try { Color(android.graphics.Color.parseColor(bdr)) } catch (_: Exception) { Color.Black }
            Box(
                modifier = Modifier
                    .size(width = 38.dp, height = 24.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(bgCol)
                    .border(1.5.dp, bdrCol, RoundedCornerShape(4.dp))
                    .clickable { onSelect(bg, bdr) }
            )
        }
    }
}

@Composable
fun WrapTextSubmenu(onSelect: (String) -> Unit) {
    val options = listOf("In Line with Text", "Square", "Tight", "Behind Text", "In Front of Text")
    Row(
        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(8.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        options.forEach { opt ->
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color(0xFFF8FAFC))
                    .border(1.dp, Color(0xFFCBD5E1), RoundedCornerShape(6.dp))
                    .clickable { onSelect(opt) }
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(opt, fontSize = 11.sp, color = Color(0xFF334155))
            }
        }
    }
}

@Composable
fun ArrangeSubmenu(onRotate90: () -> Unit, onAction: (String) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(8.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .background(Color(0xFFEFF6FF))
                .border(1.dp, Color(0xFF3B82F6), RoundedCornerShape(6.dp))
                .clickable { onRotate90() }
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.RotateRight, contentDescription = null, tint = Color(0xFF1D4ED8), modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Rotate 90°", fontSize = 11.sp, color = Color(0xFF1D4ED8))
            }
        }
        listOf("Bring Forward", "Send Backward", "Flip Horizontal").forEach { act ->
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color(0xFFF8FAFC))
                    .border(1.dp, Color(0xFFCBD5E1), RoundedCornerShape(6.dp))
                    .clickable { onAction(act) }
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(act, fontSize = 11.sp, color = Color(0xFF334155))
            }
        }
    }
}
