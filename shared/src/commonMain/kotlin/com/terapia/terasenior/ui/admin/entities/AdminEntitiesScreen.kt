package com.terapia.terasenior.ui.admin.entities

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.terapia.terasenior.domain.model.admin.Entity
import com.terapia.terasenior.ui.admin.AdminEntitiesUiState
import com.terapia.terasenior.ui.admin.AdminEntitiesViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminEntitiesScreen(
    viewModel: AdminEntitiesViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    var showCreateDialog by remember { mutableStateOf(false) }
    var entityToEdit by remember { mutableStateOf<Entity?>(null) }
    var entityToDelete by remember { mutableStateOf<Entity?>(null) }
    var hasDependencies by remember { mutableStateOf<Boolean?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Gestión de Centros",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                },
                actions = {
                    IconButton(onClick = { viewModel.loadEntities() }) {
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
                Icon(Icons.Default.Add, contentDescription = "Añadir Centro")
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when (val state = uiState) {
                is AdminEntitiesUiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }

                is AdminEntitiesUiState.Error -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = state.message,
                            color = MaterialTheme.colorScheme.error,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            modifier = Modifier.padding(24.dp)
                        )
                        androidx.compose.material3.Button(onClick = { viewModel.loadEntities() }) {
                            androidx.compose.material3.Text("Reintentar")
                        }
                    }
                }

                is AdminEntitiesUiState.Success -> {
                    if (state.entities.isEmpty()) {
                        Text(
                            text = "No hay centros registrados todavía.",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(state.entities) { entity ->
                                EntityCard(
                                    entity = entity,
                                    onEdit = { entityToEdit = entity },
                                    onDelete = { 
                                        entityToDelete = entity
                                        hasDependencies = null
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }

        // Diálogos
        if (showCreateDialog) {
            CreateEntityDialog(
                onDismiss = { showCreateDialog = false },
                onConfirm = { name, cif, address ->
                    viewModel.createEntity(name, cif, address)
                    showCreateDialog = false
                }
            )
        }

        entityToEdit?.let { entity ->
            EditEntityDialog(
                entity = entity,
                onDismiss = { entityToEdit = null },
                onConfirm = { updated ->
                    viewModel.updateEntity(updated)
                    entityToEdit = null
                }
            )
        }

        entityToDelete?.let { entity ->
            LaunchedEffect(entity.id) {
                hasDependencies = viewModel.checkDependencies(entity.id)
            }

            DeleteEntityDialog(
                entityName = entity.name,
                hasDependencies = hasDependencies,
                onDismiss = { entityToDelete = null },
                onDelete = {
                    viewModel.deleteEntity(entity.id)
                    entityToDelete = null
                },
                onDeactivate = {
                    viewModel.deactivateEntity(entity.id)
                    entityToDelete = null
                }
            )
        }
    }
}

@Composable
private fun EntityCard(
    entity: Entity,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = entity.name,
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f)
                )
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Badge de Estado
                    val isActive = entity.status == "ACTIVE"
                    Surface(
                        color = if (isActive) Color(0xFFC8E6C9) else Color(0xFFFFCDD2),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = if (isActive) "ACTIVO" else "INACTIVO",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = if (isActive) Color(0xFF1B5E20) else Color(0xFFB71C1C)
                            )
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    IconButton(onClick = onEdit) {
                        Icon(Icons.Default.Edit, contentDescription = "Editar", tint = MaterialTheme.colorScheme.secondary)
                    }
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = MaterialTheme.colorScheme.error)
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "CIF: ${entity.cif}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (!entity.address.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = entity.address,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
