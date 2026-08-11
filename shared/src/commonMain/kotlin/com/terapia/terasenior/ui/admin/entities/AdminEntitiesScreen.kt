package com.terapia.terasenior.ui.admin.entities

import androidx.compose.foundation.background
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.terapia.terasenior.domain.model.admin.Entity
import com.terapia.terasenior.ui.admin.AdminEntitiesUiState
import com.terapia.terasenior.ui.admin.AdminEntitiesViewModel
import com.terapia.terasenior.ui.admin.EntityStatusFilter
import com.terapia.terasenior.ui.components.PaginationControls
import com.terapia.terasenior.util.DateUtils
import io.kamel.image.KamelImage
import io.kamel.image.asyncPainterResource

@OptIn(ExperimentalMaterial3Api::class, kotlin.time.ExperimentalTime::class)
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
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            "Gestión de Centros",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "v1.3.17",
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when (val state = uiState) {
                is AdminEntitiesUiState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
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
                    SearchBarAndFilters(
                        query = state.searchQuery,
                        onQueryChange = viewModel::onSearchQueryChanged,
                        selectedFilter = state.selectedFilter,
                        onFilterChange = viewModel::onFilterChanged
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
                                Text(text = error, modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.onErrorContainer)
                                IconButton(onClick = { viewModel.clearError() }) {
                                    Icon(Icons.Default.Close, contentDescription = "Cerrar")
                                }
                            }
                        }
                    }

                    if (state.entities.isEmpty()) {
                        EmptyState(isSearch = state.searchQuery.isNotEmpty() || state.selectedFilter != EntityStatusFilter.ALL)
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxWidth().weight(1f),
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
                        
                        // CONTROLES DE PAGINACIÓN
                        if (state.totalPages > 1) {
                            PaginationControls(
                                currentPage = state.currentPage,
                                totalPages = state.totalPages,
                                onPageClick = { viewModel.onPageChanged(it) }
                            )
                        }
                    }
                }
            }
        }

        if (showCreateDialog) {
            CreateEntityDialog(
                onDismiss = { showCreateDialog = false },
                onConfirm = { name, cif, address, licenseExpiry, logoUrl ->
                    viewModel.createEntity(name, cif, address, licenseExpiry, logoUrl)
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
private fun SearchBarAndFilters(
    query: String,
    onQueryChange: (String) -> Unit,
    selectedFilter: EntityStatusFilter,
    onFilterChange: (EntityStatusFilter) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Buscar por nombre o CIF...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            trailingIcon = {
                if (query.isNotEmpty()) {
                    IconButton(onClick = { onQueryChange("") }) {
                        Icon(Icons.Default.Clear, contentDescription = "Limpiar")
                    }
                }
            },
            shape = RoundedCornerShape(12.dp),
            singleLine = true
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            EntityStatusFilter.entries.forEach { filter ->
                FilterChip(
                    selected = selectedFilter == filter,
                    onClick = { onFilterChange(filter) },
                    label = {
                        Text(
                            when (filter) {
                                EntityStatusFilter.ALL -> "Todas"
                                EntityStatusFilter.ACTIVE -> "Activas"
                                EntityStatusFilter.INACTIVE -> "Inactivas"
                            }
                        )
                    }
                )
            }
        }
    }
}

@Composable
private fun EmptyState(isSearch: Boolean) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = if (isSearch) Icons.Default.SearchOff else Icons.Default.Business,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = if (isSearch) "No se encontraron resultados para la búsqueda" else "No hay centros registrados todavía.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
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
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // LOGO DEL CENTRO (Imagen o Icono)
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (!entity.logoUrl.isNullOrBlank()) {
                            KamelImage(
                                resource = { asyncPainterResource(entity.logoUrl!!) },
                                contentDescription = "Logo",
                                modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(6.dp)),
                                onLoading = { CircularProgressIndicator(modifier = Modifier.size(16.dp)) },
                                onFailure = {
                                    Icon(Icons.Default.Business, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
                                }
                            )
                        } else {
                            Icon(Icons.Default.Business, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
                        }
                    }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = entity.name,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary,
                        maxLines = 1
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    val isActive = entity.status == "ACTIVE"
                    Surface(
                        color = if (isActive) Color(0xFFE8F5E9) else Color(0xFFFFEBEE),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = if (isActive) "ACTIVO" else "INACTIVO",
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp),
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp, fontWeight = FontWeight.Bold, color = if (isActive) Color(0xFF2E7D32) else Color(0xFFC62828))
                        )
                    }
                }
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("CIF: ${entity.cif}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    if (!entity.address.isNullOrBlank()) {
                        Text(" • ", color = Color.Gray)
                        Icon(Icons.Default.LocationOn, null, modifier = Modifier.size(10.dp), tint = Color.Gray)
                        Text(entity.address.take(20) + (if(entity.address.length > 20) "..." else ""), style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    }
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Edit, null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(18.dp))
                }
                IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}
