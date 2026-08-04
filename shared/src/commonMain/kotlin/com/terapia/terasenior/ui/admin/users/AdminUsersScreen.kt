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
                    Text(
                        "Gestión de Usuarios",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
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
                        onFilterChange = viewModel::onFilterChanged
                    )

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
                    onConfirm = { fullName, email, password, phone, role, entityId, isActive ->
                        viewModel.createUser(fullName, email, password, role, entityId, phone, isActive)
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

@Composable
private fun UserSearchBarAndFilters(
    query: String,
    onQueryChange: (String) -> Unit,
    selectedFilter: UserStatusFilter,
    onFilterChange: (UserStatusFilter) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)) {
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Buscar usuario...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            shape = RoundedCornerShape(12.dp),
            singleLine = true
        )
        Row(modifier = Modifier.padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            UserStatusFilter.entries.forEach { filter ->
                FilterChip(
                    selected = selectedFilter == filter,
                    onClick = { onFilterChange(filter) },
                    label = { Text(when(filter) {
                        UserStatusFilter.ALL -> "Todos"
                        UserStatusFilter.ACTIVE -> "Activos"
                        UserStatusFilter.INACTIVE -> "Inactivas"
                    })}
                )
            }
        }
    }
}

@Composable
private fun UserCard(
    user: UserProfile,
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
                modifier = Modifier.size(56.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Text(user.fullName.take(1).uppercase(), style = MaterialTheme.typography.headlineSmall)
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(user.fullName, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                Text(user.email, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                
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
                IconButton(onClick = onChangePassword) { Icon(Icons.Default.Lock, contentDescription = "Cambiar Contraseña", tint = MaterialTheme.colorScheme.primary) }
                IconButton(onClick = onEdit) { Icon(Icons.Default.Edit, contentDescription = "Editar", tint = MaterialTheme.colorScheme.secondary) }
                IconButton(onClick = onDelete) { Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = MaterialTheme.colorScheme.error) }
            }
        }
    }
}
