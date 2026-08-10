package com.terapia.terasenior.ui.admin.entities

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.terapia.terasenior.domain.model.admin.Entity

@Composable
fun EditEntityDialog(
    entity: Entity,
    onDismiss: () -> Unit,
    onConfirm: (Entity) -> Unit
) {
    var name by remember { mutableStateOf(entity.name) }
    var cif by remember { mutableStateOf(entity.cif) }
    var address by remember { mutableStateOf(entity.address ?: "") }
    var logoUrl by remember { mutableStateOf(entity.logoUrl ?: "") }
    var status by remember { mutableStateOf(entity.status) }
    var licenseExpiry by remember { mutableStateOf(entity.licenseExpiresAt?.take(10) ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Editar Centro", style = MaterialTheme.typography.headlineSmall) },
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
                    label = { Text("Nombre del Centro") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = cif,
                    onValueChange = { cif = it },
                    label = { Text("CIF") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = logoUrl,
                    onValueChange = { logoUrl = it },
                    label = { Text("URL del Logo") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("Dirección") },
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

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = status == "ACTIVE",
                        onClick = { status = "ACTIVE" },
                        label = { Text("ACTIVO") }
                    )
                    FilterChip(
                        selected = status == "INACTIVE",
                        onClick = { status = "INACTIVE" },
                        label = { Text("INACTIVO") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    )
                }

                if (status == "INACTIVE") {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "⚠️ Advertencia: Al desactivar esta entidad, se desactivará automáticamente el acceso a todos los usuarios vinculados.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val finalExpiry = if (licenseExpiry.isBlank()) null else licenseExpiry
                    val finalLogo = if (logoUrl.isBlank()) null else logoUrl
                    onConfirm(entity.copy(
                        name = name, 
                        cif = cif, 
                        address = address, 
                        status = status,
                        licenseExpiresAt = finalExpiry,
                        logoUrl = finalLogo
                    ))
                },
                enabled = name.isNotBlank() && cif.isNotBlank()
            ) {
                Text("Guardar Cambios")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        },
        shape = RoundedCornerShape(24.dp)
    )
}
