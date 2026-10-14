package com.example.ui.editor.components.review

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.TrackedChange
import com.example.ui.theme.WordPrimary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun TrackChangesDialog(
    isTrackChangesEnabled: Boolean,
    trackedChanges: List<TrackedChange>,
    onDismiss: () -> Unit,
    onToggleTrackChanges: (Boolean) -> Unit,
    onAcceptChange: (changeId: String) -> Unit,
    onRejectChange: (changeId: String) -> Unit,
    onAcceptAll: () -> Unit,
    onRejectAll: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("h:mm a • MMM d", Locale.getDefault()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.EditNote, contentDescription = null, tint = WordPrimary, modifier = Modifier.size(26.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Track Changes & Revisions", style = MaterialTheme.typography.titleMedium)
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (isTrackChangesEnabled) Color(0xFFECFDF5) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    border = BorderStroke(1.dp, if (isTrackChangesEnabled) Color(0xFF6EE7B7) else MaterialTheme.colorScheme.outlineVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Track Changes Mode", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text(
                                if (isTrackChangesEnabled) "Active: Records every edit and insertion" else "Disabled: Normal direct editing",
                                fontSize = 11.sp,
                                color = if (isTrackChangesEnabled) Color(0xFF065F46) else Color.Gray
                            )
                        }
                        Switch(
                            checked = isTrackChangesEnabled,
                            onCheckedChange = onToggleTrackChanges
                        )
                    }
                }

                if (trackedChanges.isNotEmpty()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = onAcceptAll,
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF059669))
                        ) {
                            Text("Accept All (${trackedChanges.size})", fontSize = 11.sp)
                        }
                        OutlinedButton(
                            onClick = onRejectAll,
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                        ) {
                            Text("Reject All", fontSize = 11.sp)
                        }
                    }
                }

                HorizontalDivider()

                if (trackedChanges.isEmpty()) {
                    Box(modifier = Modifier.fillMaxWidth().height(100.dp), contentAlignment = Alignment.Center) {
                        Text(
                            text = if (isTrackChangesEnabled) "No tracked changes recorded yet. Edits will appear here." else "Enable Track Changes above to begin recording revisions.",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth().height(220.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(trackedChanges) { tc ->
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFFF8FAFC),
                                border = BorderStroke(1.dp, Color(0xFFCBD5E1)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "${tc.author} • ${tc.changeType}",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            color = WordPrimary
                                        )
                                        Text(
                                            text = dateFormat.format(Date(tc.timestamp)),
                                            fontSize = 10.sp,
                                            color = Color.Gray
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(4.dp))
                                    if (tc.originalText.isNotBlank()) {
                                        Text(
                                            text = "- ${tc.originalText.take(60)}",
                                            fontSize = 11.sp,
                                            color = Color(0xFFDC2626)
                                        )
                                    }
                                    Text(
                                        text = "+ ${tc.updatedText.take(60)}",
                                        fontSize = 11.sp,
                                        color = Color(0xFF16A34A)
                                    )

                                    Spacer(modifier = Modifier.height(6.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.End,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        TextButton(onClick = { onRejectChange(tc.id) }) {
                                            Text("Reject", color = MaterialTheme.colorScheme.error, fontSize = 11.sp)
                                        }
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Button(
                                            onClick = { onAcceptChange(tc.id) },
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF059669))
                                        ) {
                                            Text("Accept", fontSize = 11.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss, colors = ButtonDefaults.buttonColors(containerColor = WordPrimary)) {
                Text("Close")
            }
        }
    )
}
