package com.terapia.terasenior.ui.admin.users

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.terapia.terasenior.ui.admin.PasswordChangeState

@Composable
fun ChangePasswordDialog(
    userEmail: String,
    state: PasswordChangeState,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    LaunchedEffect(state.succeeded) {
        if (state.succeeded) {
            password = ""
            confirmPassword = ""
        }
    }

    AlertDialog(
        onDismissRequest = { if (!state.isSubmitting) onDismiss() },
        title = { Text("Cambiar Contraseña") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("Cambiando contraseña para: $userEmail", style = MaterialTheme.typography.bodySmall)
                if (state.succeeded) {
                    Text("Contraseña actualizada correctamente.")
                } else {

                    OutlinedTextField(
                        value = password,
                        enabled = !state.isSubmitting,
                        onValueChange = { password = it },
                        label = { Text("Nueva Contraseña") },
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        supportingText = { Text("Mínimo 6 caracteres") }
                    )

                    OutlinedTextField(
                        value = confirmPassword,
                        enabled = !state.isSubmitting,
                        onValueChange = { confirmPassword = it },
                        label = { Text("Repetir Contraseña") },
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        isError = confirmPassword.isNotEmpty() && confirmPassword != password
                    )
                }
                state.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            }
        },
        confirmButton = {
            if (!state.succeeded) Button(
                onClick = { onConfirm(password) },
                enabled = !state.isSubmitting && password.length >= 6 && password == confirmPassword
            ) { Text(if (state.isSubmitting) "Actualizando…" else "Actualizar") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !state.isSubmitting) {
                Text(if (state.succeeded) "Cerrar" else "Cancelar")
            }
        }
    )
}
