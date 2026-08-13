package com.terapia.terasenior.ui.admin.users

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.terapia.terasenior.domain.model.admin.Entity
import com.terapia.terasenior.domain.model.admin.UserProfile
import com.terapia.terasenior.models.UserRole

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditUserDialog(
    user: UserProfile,
    entities: List<Entity>,
    onDismiss: () -> Unit,
    onConfirm: (UserProfile) -> Unit
) {
    var fullName by remember { mutableStateOf(user.fullName) }
    var phone by remember { mutableStateOf(user.phone ?: "") }
    var centerName by remember { mutableStateOf(user.centerName ?: "") }
    var selectedRole by remember { mutableStateOf(user.role) }
    var selectedEntityId by remember { mutableStateOf(user.entityId) }
    var isActive by remember { mutableStateOf(user.isActive) }
    
    var roleExpanded by remember { mutableStateOf(false) }
    var entityExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Editar Usuario") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp).verticalScroll(rememberScrollState()),
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
                    value = user.email,
                    onValueChange = { },
                    label = { Text("Correo Electrónico (No editable)") },
                    enabled = false,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Teléfono") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = centerName,
                    onValueChange = { centerName = it },
                    label = { Text("Centro / Lugar de trabajo") },
                    placeholder = { Text("Ej: Centro de Salud de Miajadas") },
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
                        trailingIcon = { Text("▼") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        enabled = false,
                        colors = OutlinedTextFieldDefaults.colors(
                            disabledTextColor = MaterialTheme.colorScheme.onSurface,
                            disabledBorderColor = MaterialTheme.colorScheme.outline
                        )
                    )
                    Box(modifier = Modifier.matchParentSize().clickable { roleExpanded = true })
                    DropdownMenu(expanded = roleExpanded, onDismissRequest = { roleExpanded = false }) {
                        UserRole.entries.forEach { role ->
                            DropdownMenuItem(text = { Text(role.name) }, onClick = { selectedRole = role; roleExpanded = false })
                        }
                    }
                }

                // Selector de Centro
                if (selectedRole != UserRole.SUPER_ADMIN) {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        val entityName = entities.find { it.id == selectedEntityId }?.name ?: "Seleccionar Centro"
                        OutlinedTextField(
                            value = entityName,
                            onValueChange = {},
                            label = { Text("Centro") },
                            readOnly = true,
                            trailingIcon = { Text("▼") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            enabled = false,
                            colors = OutlinedTextFieldDefaults.colors(
                                disabledTextColor = MaterialTheme.colorScheme.onSurface,
                                disabledBorderColor = MaterialTheme.colorScheme.outline
                            )
                        )
                        Box(modifier = Modifier.matchParentSize().clickable { entityExpanded = true })
                        DropdownMenu(expanded = entityExpanded, onDismissRequest = { entityExpanded = false }) {
                            entities.forEach { entity ->
                                DropdownMenuItem(text = { Text(entity.name) }, onClick = { selectedEntityId = entity.id; entityExpanded = false })
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Usuario Activo")
                    Switch(checked = isActive, onCheckedChange = { isActive = it })
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm(user.copy(
                        fullName = fullName,
                        phone = phone,
                        role = selectedRole,
                        entityId = if (selectedRole == UserRole.SUPER_ADMIN) null else selectedEntityId,
                        isActive = isActive,
                        centerName = centerName.ifBlank { null }
                    ))
                },
                enabled = fullName.isNotBlank()
            ) { Text("Guardar") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}
