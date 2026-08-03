package com.terapia.terasenior.ui.admin.entities

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun DeleteEntityDialog(
    entityName: String,
    hasDependencies: Boolean?, // null mientras carga
    onDismiss: () -> Unit,
    onDelete: () -> Unit,
    onDeactivate: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Eliminar Centro", style = MaterialTheme.typography.headlineSmall) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (hasDependencies == null) {
                    Box(modifier = Modifier.fillMaxWidth().height(60.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp))
                    }
                } else if (hasDependencies) {
                    Text(
                        text = "Esta entidad tiene usuarios o pacientes asociados. No se puede eliminar por completo para mantener la integridad de los datos.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "Puedes desactivar el centro y a todos sus usuarios para impedirles el acceso.",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                } else {
                    Text(
                        text = "¿Estás seguro de que deseas eliminar definitivamente el centro '$entityName'?",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "Esta acción no se puede deshacer.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        },
        confirmButton = {
            if (hasDependencies == false) {
                Button(
                    onClick = onDelete,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Eliminar Definitivamente")
                }
            } else if (hasDependencies == true) {
                Button(onClick = onDeactivate) {
                    Text("Desactivar Centro y Usuarios")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}
