package com.example.ui.editor.components.bottomsheet

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Subscript
import androidx.compose.material.icons.filled.Superscript
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.HeadingStyle
import com.example.editor.engine.CaseType

/**
 * Styles gallery matching MS Word Mobile (Screenshot 2).
 */
@Composable
fun StylesSubmenuView(
    currentStyle: HeadingStyle,
    onSelectStyle: (HeadingStyle) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        StyleItemRow(
            label = "Emphasis",
            isSelected = false,
            textStyle = { Text("Emphasis", fontStyle = FontStyle.Italic, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface) },
            onClick = { onSelectStyle(HeadingStyle.NORMAL) }
        )

        StyleItemRow(
            label = "Heading 1",
            isSelected = currentStyle == HeadingStyle.HEADING_1,
            textStyle = { Text("Heading 1", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = Color(0xFF2E75B6)) },
            onClick = { onSelectStyle(HeadingStyle.HEADING_1) }
        )

        StyleItemRow(
            label = "Heading 2",
            isSelected = currentStyle == HeadingStyle.HEADING_2,
            textStyle = { Text("Heading 2", fontWeight = FontWeight.SemiBold, fontSize = 17.sp, color = Color(0xFF2E75B6)) },
            onClick = { onSelectStyle(HeadingStyle.HEADING_2) }
        )

        StyleItemRow(
            label = "Normal",
            isSelected = currentStyle == HeadingStyle.NORMAL,
            textStyle = { Text("Normal", fontSize = 15.sp, color = MaterialTheme.colorScheme.onSurface) },
            onClick = { onSelectStyle(HeadingStyle.NORMAL) }
        )

        StyleItemRow(
            label = "Strong",
            isSelected = false,
            textStyle = { Text("Strong", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = MaterialTheme.colorScheme.onSurface) },
            onClick = { onSelectStyle(HeadingStyle.NORMAL) }
        )

        StyleItemRow(
            label = "Subtitle",
            isSelected = currentStyle == HeadingStyle.SUBTITLE,
            textStyle = { Text("Subtitle", fontSize = 14.sp, color = Color(0xFF595959)) },
            onClick = { onSelectStyle(HeadingStyle.SUBTITLE) }
        )

        StyleItemRow(
            label = "Title",
            isSelected = currentStyle == HeadingStyle.TITLE,
            textStyle = {
                Column {
                    Text("Title", fontWeight = FontWeight.Bold, fontSize = 22.sp, color = MaterialTheme.colorScheme.onSurface)
                    Box(modifier = Modifier.width(120.dp).height(2.dp).background(Color(0xFF595959)))
                }
            },
            onClick = { onSelectStyle(HeadingStyle.TITLE) }
        )
    }
}

/**
 * More Formatting submenu matching MS Word Mobile (Screenshot 6).
 */
@Composable
fun MoreFormattingSubmenuView(
    isSubscript: Boolean,
    isSuperscript: Boolean,
    onToggleSubscript: () -> Unit,
    onToggleSuperscript: () -> Unit,
    onNavigateToChangeCase: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        WordRowButton(
            title = "Subscript",
            icon = Icons.Default.Subscript,
            isSelected = isSubscript,
            onClick = onToggleSubscript
        )

        WordRowButton(
            title = "Superscript",
            icon = Icons.Default.Superscript,
            isSelected = isSuperscript,
            onClick = onToggleSuperscript
        )

        WordRowButton(
            title = "Change Case",
            icon = Icons.Default.TextFields,
            hasChevron = true,
            onClick = onNavigateToChangeCase
        )
    }
}

/**
 * Change Case submenu matching MS Word Mobile.
 */
@Composable
fun ChangeCaseSubmenuView(
    onSelectCase: (CaseType) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        WordRowButton(title = "Sentence case.", onClick = { onSelectCase(CaseType.SENTENCE) })
        WordRowButton(title = "lowercase", onClick = { onSelectCase(CaseType.LOWERCASE) })
        WordRowButton(title = "UPPERCASE", onClick = { onSelectCase(CaseType.UPPERCASE) })
        WordRowButton(title = "Capitalize Each Word", onClick = { onSelectCase(CaseType.TITLE_CASE) })
        WordRowButton(title = "tOGGLE cASE", onClick = { onSelectCase(CaseType.TOGGLE_CASE) })
    }
}

@Composable
private fun StyleItemRow(
    label: String,
    isSelected: Boolean,
    textStyle: @Composable () -> Unit,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(4.dp),
        border = if (isSelected) BorderStroke(1.dp, MaterialTheme.colorScheme.outline) else null,
        color = Color.Transparent,
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(4.dp)).clickable { onClick() }
    ) {
        Box(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
            textStyle()
        }
    }
}
