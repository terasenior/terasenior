package com.terapia.terasenior.ui.patient

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
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
import com.terapia.terasenior.domain.model.patient.Patient
import com.terapia.terasenior.domain.model.patient.PatientStatus
import com.terapia.terasenior.ui.components.PaginationControls

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PatientListScreen(
    viewModel: PatientListViewModel,
    onPatientClick: (String) -> Unit,
    onAddPatientClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Gestión de Pacientes", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)) },
                actions = {
                    IconButton(onClick = { viewModel.loadPatients() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refrescar")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddPatientClick,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Añadir Paciente")
            }
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            when (val state = uiState) {
                is PatientListUiState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                is PatientListUiState.Error -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(state.message, color = MaterialTheme.colorScheme.error)
                    }
                }
                is PatientListUiState.Success -> {
                    // Barra de búsqueda y Filtros
                    Column(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = state.searchQuery,
                            onValueChange = viewModel::onSearchQueryChanged,
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("Buscar paciente...") },
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true
                        )
                        
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            FilterChip(
                                selected = state.statusFilter == null,
                                onClick = { viewModel.onStatusFilterChanged(null) },
                                label = { Text("Todos") }
                            )
                            PatientStatus.entries.forEach { s ->
                                val label = when(s) {
                                    PatientStatus.ACTIVE -> "Activo"
                                    PatientStatus.INACTIVE -> "Inactivo"
                                    PatientStatus.BAJA -> "Baja"
                                    PatientStatus.DISCHARGED -> "Alta"
                                }
                                FilterChip(
                                    selected = state.statusFilter == s,
                                    onClick = { viewModel.onStatusFilterChanged(s) },
                                    label = { Text(label) }
                                )
                            }
                        }
                    }

                    if (state.patients.isEmpty()) {
                        Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.SearchOff, contentDescription = null, modifier = Modifier.size(64.dp), tint = Color.LightGray)
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("No se encontraron pacientes.", color = Color.Gray)
                            }
                        }
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.weight(1f).fillMaxWidth()
                        ) {
                            items(state.patients) { patient ->
                                PatientCard(patient = patient, onClick = { onPatientClick(patient.id) })
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
    }
}

@Composable
private fun PatientCard(patient: Patient, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // NUEVO ICONO PROFESIONAL
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)),
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
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = patient.fullName,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    if (patient.nif != null || patient.externalId != null) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "(${listOfNotNull(patient.nif, patient.externalId).joinToString(" / ")})",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                    }
                }
                
                Surface(
                    color = when(patient.status) {
                        PatientStatus.ACTIVE -> Color(0xFFC8E6C9)
                        PatientStatus.INACTIVE -> Color(0xFFFFF9C4)
                        PatientStatus.BAJA -> Color(0xFFBDBDBD)
                        PatientStatus.DISCHARGED -> Color(0xFFB2EBF2)
                    },
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    val label = when(patient.status) {
                        PatientStatus.ACTIVE -> "Activo"
                        PatientStatus.INACTIVE -> "Inactivo"
                        PatientStatus.BAJA -> "Baja"
                        PatientStatus.DISCHARGED -> "Alta Terapéutica"
                    }
                    Text(
                        text = label,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = if (patient.status == PatientStatus.ACTIVE) Color(0xFF1B5E20) else Color.DarkGray
                        )
                    )
                }
            }
            
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.LightGray)
        }
    }
}
