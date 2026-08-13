package com.terapia.terasenior.ui.admin.users

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.terapia.terasenior.domain.model.admin.Entity
import com.terapia.terasenior.domain.model.admin.UserProfile
import com.terapia.terasenior.models.UserRole
import com.terapia.terasenior.ui.admin.AdminUsersUiState
import com.terapia.terasenior.ui.admin.AdminUsersViewModel
import com.terapia.terasenior.ui.admin.UserStatusFilter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminUsersScreen(
    viewModel: AdminUsersViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    var showCreateDialog by remember { mutableStateOf(false) }
    var userToEdit by remember { mutableStateOf<UserProfile?>(null) }
    var userToDelete by remember { mutableStateOf<UserProfile?>(null) }
    var userToChangePassword by remember { mutableStateOf<UserProfile?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            "Gestión de Usuarios",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "v1.3.23",
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.loadUsers() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Recargar")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showCreateDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Añadir Usuario")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding)
        ) {
            when (val state = uiState) {
                is AdminUsersUiState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }

                is AdminUsersUiState.Error -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(text = state.message, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(24.dp))
                        Button(onClick = { viewModel.loadUsers() }) { Text("Reintentar") }
                    }
                }

                is AdminUsersUiState.Success -> {
                    UserSearchBarAndFilters(
                        query = state.searchQuery,
                        onQueryChange = viewModel::onSearchQueryChanged,
                        selectedFilter = state.selectedFilter,
                        selectedEntityId = state.selectedEntityFilter,
                        entities = state.entities,
                        onFilterChange = viewModel::onFilterChanged,
                        onEntityFilterChange = viewModel::onEntityFilterChanged
                    )

                    state.errorMessage?.let { error ->
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = error, modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.onErrorContainer, style = MaterialTheme.typography.bodySmall)
                                IconButton(onClick = { viewModel.clearError() }) {
                                    Icon(Icons.Default.Close, contentDescription = "Cerrar", modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }

                    if (state.users.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("No se encontraron usuarios.")
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(state.users) { user ->
                                UserCard(
                                    user = user,
                                    entityName = state.entities.find { it.id == user.entityId }?.name,
                                    onEdit = { userToEdit = user },
                                    onDelete = { userToDelete = user },
                                    onChangePassword = { userToChangePassword = user }
                                )
                            }
                        }
                    }
                }
            }
        }

        // Diálogos
        if (showCreateDialog) {
            val state = uiState
            if (state is AdminUsersUiState.Success) {
                CreateUserDialog(
                    entities = state.entities,
                    onDismiss = { showCreateDialog = false },
                    onConfirm = { fullName, email, password, phone, role, entityId, isActive, centerName ->
                        viewModel.createUser(fullName, email, password, role, entityId, phone, isActive, centerName)
                        showCreateDialog = false
                    }
                )
            }
        }

        userToEdit?.let { user ->
            val state = uiState
            if (state is AdminUsersUiState.Success) {
                EditUserDialog(
                    user = user,
                    entities = state.entities,
                    onDismiss = { userToEdit = null },
                    onConfirm = { updated ->
                        viewModel.updateUser(updated)
                        userToEdit = null
                    }
                )
            }
        }

        userToDelete?.let { user ->
            AlertDialog(
                onDismissRequest = { userToDelete = null },
                title = { Text("Eliminar Usuario") },
                text = { Text("¿Estás seguro de que deseas eliminar el perfil de ${user.fullName}?") },
                confirmButton = {
                    Button(
                        onClick = { viewModel.deleteUser(user.id); userToDelete = null },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) { Text("Eliminar") }
                },
                dismissButton = { TextButton(onClick = { userToDelete = null }) { Text("Cancelar") } }
            )
        }

        userToChangePassword?.let { user ->
            ChangePasswordDialog(
                userEmail = user.email,
                onDismiss = { userToChangePassword = null },
                onConfirm = { newPass ->
                    viewModel.changePassword(newPass)
                    userToChangePassword = null
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun UserSearchBarAndFilters(
    query: String,
    onQueryChange: (String) -> Unit,
    selectedFilter: UserStatusFilter,
    selectedEntityId: String?,
    entities: List<Entity>,
    onFilterChange: (UserStatusFilter) -> Unit,
    onEntityFilterChange: (String?) -> Unit
) {
    var entityExpanded by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Buscar por nombre o email...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            shape = RoundedCornerShape(12.dp),
            singleLine = true
        )

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            // Filtro de Entidad
            Box(modifier = Modifier.weight(1f)) {
                val selectedEntityName = entities.find { it.id == selectedEntityId }?.name ?: "Todos los Centros"
                FilterChip(
                    selected = selectedEntityId != null,
                    onClick = { entityExpanded = true },
                    label = { Text(selectedEntityName) },
                    trailingIcon = { Icon(Icons.Default.ArrowDropDown, contentDescription = null) }
                )
                DropdownMenu(expanded = entityExpanded, onDismissRequest = { entityExpanded = false }) {
                    DropdownMenuItem(text = { Text("Todos los Centros") }, onClick = { onEntityFilterChange(null); entityExpanded = false })
                    entities.forEach { entity ->
                        DropdownMenuItem(text = { Text(entity.name) }, onClick = { onEntityFilterChange(entity.id); entityExpanded = false })
                    }
                }
            }

            // Filtros de Estado
            UserStatusFilter.entries.forEach { filter ->
                FilterChip(
                    selected = selectedFilter == filter,
                    onClick = { onFilterChange(filter) },
                    label = { Text(when(filter) {
                        UserStatusFilter.ALL -> "Todos"
                        UserStatusFilter.ACTIVE -> "Activos"
                        UserStatusFilter.INACTIVE -> "Inactivos"
                    })}
                )
            }
        }
    }
}

@Composable
private fun UserCard(
    user: UserProfile,
    entityName: String?,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onChangePassword: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(56.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.AccountCircle,
                    contentDescription = null,
                    modifier = Modifier.size(32.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(user.fullName, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                Text(user.email, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                
                if (entityName != null) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 2.dp)) {
                        Icon(Icons.Default.Business, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color.Gray)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = buildString {
                                append(entityName)
                                if (!user.centerName.isNullOrBlank()) {
                                    append(" - ")
                                    append(user.centerName)
                                }
                            },
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.Gray
                        )
                    }
                }

                Row(modifier = Modifier.padding(top = 4.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Surface(
                        color = when(user.role) {
                            UserRole.SUPER_ADMIN -> Color(0xFFE1BEE7)
                            UserRole.ADMIN_CENTRO -> Color(0xFFB2EBF2)
                            else -> Color(0xFFC8E6C9)
                        },
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(user.role.name, modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp), style = MaterialTheme.typography.labelSmall)
                    }
                    if (!user.isActive) {
                        Surface(color = Color(0xFFFFCDD2), shape = RoundedCornerShape(8.dp)) {
                            Text("INACTIVO", modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp), style = MaterialTheme.typography.labelSmall, color = Color.Red)
                        }
                    }
                }
            }

            Row {
                IconButton(onClick = onChangePassword) { Icon(Icons.Default.Lock, contentDescription = "Pass", tint = MaterialTheme.colorScheme.primary) }
                IconButton(onClick = onEdit) { Icon(Icons.Default.Edit, contentDescription = "Edit", tint = MaterialTheme.colorScheme.secondary) }
                IconButton(onClick = onDelete) { Icon(Icons.Default.Delete, contentDescription = "Del", tint = MaterialTheme.colorScheme.error) }
            }
        }
    }
}
