package com.terapia.terasenior.ui.patient

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import com.terapia.terasenior.domain.model.results.ActivityResult
import com.terapia.terasenior.domain.model.therapy.PatientSessionHistory
import com.terapia.terasenior.ui.therapy.ExerciseTranslationUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PatientDetailScreen(
    viewModel: PatientDetailViewModel,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedTab by remember { mutableStateOf(0) }
    var showEditDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Ficha del Paciente") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    if (uiState is PatientDetailUiState.Success) {
                        IconButton(onClick = { showEditDialog = true }) {
                            Icon(Icons.Default.Edit, contentDescription = "Editar Paciente")
                        }
                    }
                }
            )
        }
    ) { padding ->
        when (val state = uiState) {
            is PatientDetailUiState.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            is PatientDetailUiState.Error -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(state.message, color = MaterialTheme.colorScheme.error)
                }
            }
            is PatientDetailUiState.Success -> {
                Column(modifier = Modifier.fillMaxSize().padding(padding)) {
                    PatientHeader(state.patient, onEditClick = { showEditDialog = true })
                    
                    PrimaryTabRow(selectedTabIndex = selectedTab) {
                        Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }, text = { Text("Perfil") })
                        Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }, text = { Text("Evolución") })
                        Tab(selected = selectedTab == 2, onClick = { selectedTab = 2 }, text = { Text("Historial") })
                    }

                    Box(modifier = Modifier.weight(1f)) {
                        when (selectedTab) {
                            0 -> PatientInfoTab(state)
                            1 -> PatientEvolutionTab(state)
                            2 -> PatientHistoryTab(state)
                        }
                    }
                }

                if (showEditDialog) {
                    EditPatientDialog(
                        patient = state.patient,
                        onDismiss = { showEditDialog = false },
                        onConfirm = { updated ->
                            viewModel.updatePatient(updated)
                            showEditDialog = false
                        },
                        isLoading = state.isUpdating
                    )
                }
            }
        }
    }
}

@Composable
fun PatientHeader(patient: Patient, onEditClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(24.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(64.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Text(patient.firstName.take(1), style = MaterialTheme.typography.headlineMedium)
        }
        Spacer(modifier = Modifier.width(20.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(patient.fullName, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("v1.0.6 • ID: ${patient.id.take(8)}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            }
        }
        IconButton(onClick = onEditClick) {
            Icon(Icons.Default.Edit, contentDescription = "Editar Paciente", tint = MaterialTheme.colorScheme.primary)
        }
    }
}

@Composable
fun PatientHistoryTab(state: PatientDetailUiState.Success) {
    if (state.sessionsHistory.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No hay sesiones registradas todavía.", color = Color.Gray)
        }
    } else {
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(state.sessionsHistory) { history ->
                SessionHistoryCard(history)
            }
        }
    }
}

@Composable
fun SessionHistoryCard(history: PatientSessionHistory) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Event, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = history.session.createdAt.take(10), // Fecha YYYY-MM-DD
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.AccessTime, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color.Gray)
                        Spacer(modifier = Modifier.width(4.dp))
                        val start = history.session.startedAt?.takeLast(8)?.take(5) ?: "--:--"
                        val end = history.session.finishedAt?.takeLast(8)?.take(5) ?: "--:--"
                        Text("$start - $end", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    }
                }
                
                // Badge de Valoración Profesional
                Surface(
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Star, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color(0xFFF57C00))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(history.session.valuation.toString(), style = MaterialTheme.typography.labelSmall)
                    }
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

            // RESULTADOS AGRUPADOS POR CATEGORÍA
            history.groupedByCategory.forEach { (category, results) ->
                CategoryResultGroup(category, results)
                Spacer(modifier = Modifier.height(12.dp))
            }

            if (history.session.therapistNotes?.isNotBlank() == true) {
                Spacer(modifier = Modifier.height(8.dp))
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Observaciones del Terapeuta:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                        Text(history.session.therapistNotes, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }
}

@Composable
fun CategoryResultGroup(category: String, results: List<ActivityResult>) {
    Column {
        Text(
            text = category,
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.primary
        )
        results.forEach { result ->
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = ExerciseTranslationUtils.getDisplayName(result.activityType),
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.weight(1f)
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${result.score}%",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Black),
                        color = if(result.score > 70) Color(0xFF2E7D32) else Color.Red
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "${result.durationSeconds}s",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray
                    )
                }
            }
        }
    }
}

@Composable
fun PatientInfoTab(state: PatientDetailUiState.Success) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp).verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Datos Administrativos
        CollapsibleCard(
            title = "Datos Administrativos",
            icon = Icons.Default.Business,
            initialExpanded = false
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                DetailRow(label = "NIF / DNI", value = state.patient.nif ?: "No registrado")
                DetailRow(label = "Nº Expediente", value = state.patient.externalId ?: "No asignado")
                DetailRow(label = "Fecha de Alta", value = state.patient.admissionDate ?: "No registrada")
                DetailRow(label = "Fecha de Baja", value = state.patient.dischargeDate ?: "N/A")
                DetailRow(
                    label = "Estado Actual",
                    value = when(state.patient.status) {
                        PatientStatus.ACTIVE -> "Activo"
                        PatientStatus.INACTIVE -> "Inactivo"
                        PatientStatus.BAJA -> "Baja"
                        PatientStatus.DISCHARGED -> "Alta Terapéutica"
                    }
                )
            }
        }

        // Datos Personales
        CollapsibleCard(
            title = "Información Personal",
            icon = Icons.Default.Person
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                DetailRow(label = "Apellidos", value = state.patient.lastName)
                DetailRow(label = "Nombre", value = state.patient.firstName)
                DetailRow(label = "Nombre Preferido", value = state.patient.preferredName ?: "Igual al nombre")
                DetailRow(label = "Fecha de Nacimiento", value = state.patient.birthDate ?: "Desconocida")
            }
        }

        // Contacto y Emergencia
        CollapsibleCard(
            title = "Contacto y Emergencia",
            icon = Icons.Default.Phone
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                DetailRow(label = "Dirección", value = state.patient.address ?: "No registrada")
                DetailRow(label = "Teléfono", value = state.patient.phone ?: "No registrado")
                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                Text("Persona de Referencia", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                DetailRow(label = "Nombre", value = state.patient.contactName ?: "No asignado")
                DetailRow(label = "Teléfono de Contacto", value = state.patient.contactPhone ?: "No asignado")
            }
        }
    }
}

@Composable
private fun CollapsibleCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    initialExpanded: Boolean = false,
    content: @Composable () -> Unit
) {
    var expanded by remember { mutableStateOf(initialExpanded) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (expanded) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f) 
                             else MaterialTheme.colorScheme.surface
        ),
        border = if (!expanded) BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant) else null
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded }
                    .padding(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(12.dp))
                Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                Icon(
                    imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = if (expanded) "Colapsar" else "Expandir",
                    tint = Color.Gray
                )
            }
            
            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(modifier = Modifier.padding(start = 20.dp, end = 20.dp, bottom = 20.dp)) {
                    content()
                }
            }
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
fun PatientEvolutionTab(state: PatientDetailUiState.Success) {
    val allResults = state.rawResults.sortedByDescending { it.createdAt }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Tendencia de Resultados", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))

        if (allResults.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No hay datos suficientes para mostrar la evolución.", color = Color.Gray)
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(allResults.take(20)) { result ->
                    ResultEvolutionRow(result)
                }
            }
        }
    }
}

@Composable
private fun ResultEvolutionRow(result: ActivityResult) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = ExerciseTranslationUtils.getDisplayName(result.activityType),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = result.createdAt.take(10),
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray
                )
            }
            
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${result.score}%",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Black,
                    color = if(result.score > 70) Color(0xFF2E7D32) else Color.Red
                )
                Text(
                    text = "Nivel ${result.difficultyLevel.takeLast(1)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray
                )
            }
        }
    }
}
