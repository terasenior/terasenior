package com.terapia.terasenior.ui.admin.users

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.terapia.terasenior.domain.model.admin.Entity
import com.terapia.terasenior.models.UserRole

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateUserDialog(
    entities: List<Entity>,
    onDismiss: () -> Unit,
    onConfirm: (fullName: String, email: String, phone: String, role: UserRole, entityId: String?) -> Unit
) {
    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var selectedRole by remember { mutableStateOf(UserRole.TERAPEUTA) }
    var selectedEntity by remember { mutableStateOf<Entity?>(null) }
    
    var roleExpanded by remember { mutableStateOf(false) }
    var entityExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Añadir Nuevo Usuario") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = fullName,
                    onValueChange = { fullName = it },
                    label = { Text("Nombre Completo") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Correo Electrónico") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Teléfono (Opcional)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                // Selector de Rol
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = selectedRole.name,
                        onValueChange = {},
                        label = { Text("Rol") },
                        readOnly = true,
                        trailingIcon = { Text("▼", modifier = Modifier.padding(end = 8.dp)) },
                        modifier = Modifier.fillMaxWidth().clickable { roleExpanded = true },
                        shape = RoundedCornerShape(12.dp),
                        enabled = false, // Para capturar el click en el Box/Modifier
                        colors = OutlinedTextFieldDefaults.colors(
                            disabledTextColor = MaterialTheme.colorScheme.onSurface,
                            disabledBorderColor = MaterialTheme.colorScheme.outline,
                            disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                    // Capa invisible para el click
                    Box(modifier = Modifier.matchParentSize().clickable { roleExpanded = true })
                    
                    DropdownMenu(
                        expanded = roleExpanded,
                        onDismissRequest = { roleExpanded = false },
                        modifier = Modifier.fillMaxWidth(0.8f)
                    ) {
                        UserRole.entries.forEach { role ->
                            DropdownMenuItem(
                                text = { Text(role.name) },
                                onClick = {
                                    selectedRole = role
                                    roleExpanded = false
                                }
                            )
                        }
                    }
                }

                // Selector de Centro
                if (selectedRole != UserRole.SUPER_ADMIN) {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = selectedEntity?.name ?: "Seleccionar Centro",
                            onValueChange = {},
                            label = { Text("Centro / Entidad") },
                            readOnly = true,
                            trailingIcon = { Text("▼", modifier = Modifier.padding(end = 8.dp)) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            enabled = false,
                            colors = OutlinedTextFieldDefaults.colors(
                                disabledTextColor = if (selectedEntity != null) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                                disabledBorderColor = MaterialTheme.colorScheme.outline,
                                disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                        Box(modifier = Modifier.matchParentSize().clickable { entityExpanded = true })

                        DropdownMenu(
                            expanded = entityExpanded,
                            onDismissRequest = { entityExpanded = false },
                            modifier = Modifier.fillMaxWidth(0.8f)
                        ) {
                            entities.forEach { entity ->
                                DropdownMenuItem(
                                    text = { Text(entity.name) },
                                    onClick = {
                                        selectedEntity = entity
                                        entityExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm(fullName, email, phone, selectedRole, selectedEntity?.id)
                },
                enabled = fullName.isNotBlank() && email.contains("@") && 
                          (selectedRole == UserRole.SUPER_ADMIN || selectedEntity != null),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Guardar")
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
