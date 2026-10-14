package com.example.ui.editor.components.dialogs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.WordPrimary

/**
 * Dialog for setting or removing document PIN passcode lock.
 */
@Composable
fun DocumentPasswordDialog(
    isCurrentlyLocked: Boolean,
    onDismiss: () -> Unit,
    onSetPassword: (String) -> Unit,
    onRemovePassword: () -> Unit
) {
    var passwordInput by remember { mutableStateOf("") }
    var confirmPasswordInput by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    if (isCurrentlyLocked) Icons.Default.Lock else Icons.Default.LockOpen,
                    contentDescription = null,
                    tint = WordPrimary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(if (isCurrentlyLocked) "Document Security Lock" else "Protect Document with PIN")
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                if (isCurrentlyLocked) {
                    Text(
                        "This document is currently protected with a password PIN. Anyone opening it must enter the PIN.",
                        fontSize = 13.sp
                    )
                    Button(
                        onClick = onRemovePassword,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Remove Password Protection")
                    }
                } else {
                    Text(
                        "Set a 4-8 character PIN or password to restrict access to this document:",
                        fontSize = 13.sp
                    )
                    OutlinedTextField(
                        value = passwordInput,
                        onValueChange = {
                            passwordInput = it
                            errorMessage = null
                        },
                        label = { Text("Enter PIN / Password") },
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = confirmPasswordInput,
                        onValueChange = {
                            confirmPasswordInput = it
                            errorMessage = null
                        },
                        label = { Text("Confirm PIN / Password") },
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    errorMessage?.let {
                        Text(it, color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                    }
                }
            }
        },
        confirmButton = {
            if (!isCurrentlyLocked) {
                Button(
                    onClick = {
                        if (passwordInput.length < 4) {
                            errorMessage = "PIN must be at least 4 characters"
                        } else if (passwordInput != confirmPasswordInput) {
                            errorMessage = "Passwords do not match"
                        } else {
                            onSetPassword(passwordInput)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = WordPrimary)
                ) {
                    Text("Lock Document")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Close") }
        }
    )
}
