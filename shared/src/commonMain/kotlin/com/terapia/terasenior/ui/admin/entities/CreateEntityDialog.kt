package com.terapia.terasenior.ui.admin.entities

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun CreateEntityDialog(
    onDismiss: () -> Unit,
    onConfirm: (name: String, cif: String, address: String, licenseExpiry: String?, logoUrl: String?) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var cif by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var licenseExpiry by remember { mutableStateOf("") }
    var logoUrl by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Añadir Nuevo Centro",
                style = MaterialTheme.typography.headlineSmall
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nombre del Centro *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = cif,
                    onValueChange = { cif = it },
                    label = { Text("CIF / Identificación *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = logoUrl,
                    onValueChange = { logoUrl = it },
                    label = { Text("URL del Logo (Opcional)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("Dirección / Localización") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = licenseExpiry,
                    onValueChange = { licenseExpiry = it },
                    label = { Text("Vencimiento Licencia (AAAA-MM-DD)") },
                    placeholder = { Text("Ej: 2026-12-31") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank() && cif.isNotBlank()) {
                        val expiry = if (licenseExpiry.isBlank()) null else licenseExpiry
                        val logo = if (logoUrl.isBlank()) null else logoUrl
                        onConfirm(name, cif, address, expiry, logo)
                    }
                },
                enabled = name.isNotBlank() && cif.isNotBlank(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Crear Centro")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        },
        shape = RoundedCornerShape(24.dp)
    )
}
