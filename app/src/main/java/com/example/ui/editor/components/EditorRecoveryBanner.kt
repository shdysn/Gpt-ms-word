package com.example.ui.editor.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Crash and unexpected process restart recovery banner component.
 * Warns the user when an unsaved recovery draft exists from a previous editing session.
 */
@Composable
fun EditorRecoveryBanner(
    onRestore: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = Color(0xFFFFF3CD),
        border = BorderStroke(1.dp, Color(0xFFFFEEBA)),
        shape = RoundedCornerShape(8.dp),
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .testTag("recovery_banner")
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(10.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = null,
                tint = Color(0xFF856404),
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Unsaved Recovery Draft Found",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = Color(0xFF856404)
                )
                Text(
                    text = "A draft from an earlier session is available for restoration.",
                    fontSize = 11.sp,
                    color = Color(0xFF856404)
                )
            }
            Spacer(modifier = Modifier.width(6.dp))
            Button(
                onClick = onRestore,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF856404)),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                modifier = Modifier.testTag("restore_recovery_draft_button")
            ) {
                Text("Restore", fontSize = 11.sp, color = Color.White)
            }
            Spacer(modifier = Modifier.width(4.dp))
            TextButton(
                onClick = onDismiss,
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                modifier = Modifier.testTag("dismiss_recovery_draft_button")
            ) {
                Text("Dismiss", fontSize = 11.sp, color = Color(0xFF856404))
            }
        }
    }
}
