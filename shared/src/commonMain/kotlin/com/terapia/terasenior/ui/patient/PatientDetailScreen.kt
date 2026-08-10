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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.terapia.terasenior.domain.model.admin.UserProfile
import com.terapia.terasenior.domain.model.patient.Patient
import com.terapia.terasenior.domain.model.patient.PatientStatus
import com.terapia.terasenior.domain.model.patient.SupportLevel
import com.terapia.terasenior.domain.model.patient.TherapeuticProfile
import com.terapia.terasenior.domain.model.results.ActivityResult
import com.terapia.terasenior.ui.components.PaginationControls
import com.terapia.terasenior.ui.therapy.ExerciseTranslationUtils
import com.terapia.terasenior.util.DateUtils
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PatientDetailScreen(
    viewModel: PatientDetailViewModel,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedTab by remember { mutableStateOf(0) }
    var showEditDialog by remember { mutableStateOf(false) }
    var showTransferDialog by remember { mutableStateOf(false) }

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
                        IconButton(onClick = { showTransferDialog = true }) {
                            Icon(Icons.Default.SyncAlt, contentDescription = "Traspasar Paciente")
                        }
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
                        Tab(selected = selectedTab == 2, onClick = { selectedTab = 2 }, text = { Text("Valoración") })
                        Tab(selected = selectedTab == 3, onClick = { selectedTab = 3 }, text = { Text("Historial") })
                    }

                    Box(modifier = Modifier.weight(1f)) {
                        when (selectedTab) {
                            0 -> PatientInfoTab(state)
                            1 -> PatientEvolutionTab(state)
                            2 -> PatientAssessmentTab(state, viewModel)
                            3 -> PatientHistoryTab(state, viewModel)
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

                if (showTransferDialog) {
                    TransferPatientDialog(
                        currentTherapistId = state.patient.assignedTherapistId,
                        professionals = state.entityProfessionals,
                        onDismiss = { showTransferDialog = false },
                        onConfirm = { newId ->
                            viewModel.transferPatient(newId)
                            showTransferDialog = false
                        }
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
            modifier = Modifier.size(64.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.AccountCircle,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        }
        Spacer(modifier = Modifier.width(20.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(patient.fullName, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("v1.2.4 • ID: ${patient.id.take(8)}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            }
        }
        IconButton(onClick = onEditClick) {
            Icon(Icons.Default.Edit, contentDescription = "Editar Paciente", tint = MaterialTheme.colorScheme.primary)
        }
    }
}

@Composable
fun PatientInfoTab(state: PatientDetailUiState.Success) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp).verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        CollapsibleCard(title = "Datos Administrativos", icon = Icons.Default.Business) {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                DetailRow(label = "NIF / DNI", value = state.patient.nif ?: "No registrado")
                DetailRow(label = "Nº Expediente", value = state.patient.externalId ?: "No asignado")
                DetailRow(label = "Fecha de Alta", value = DateUtils.toUserFormat(state.patient.admissionDate))
                DetailRow(label = "Fecha de Baja", value = DateUtils.toUserFormat(state.patient.dischargeDate))
                DetailRow(label = "Estado Actual", value = state.patient.status.name)
            }
        }

        CollapsibleCard(title = "Información Personal", icon = Icons.Default.Person) {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                DetailRow(label = "Apellidos", value = state.patient.lastName)
                DetailRow(label = "Nombre", value = state.patient.firstName)
                DetailRow(label = "Nombre Preferido", value = state.patient.preferredName ?: "Igual al nombre")
                DetailRow(label = "Fecha de Nacimiento", value = DateUtils.toUserFormat(state.patient.birthDate))
            }
        }

        CollapsibleCard(title = "Localización y Contacto", icon = Icons.Default.LocationOn) {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                DetailRow(label = "Dirección", value = state.patient.address ?: "No registrada")
                DetailRow(label = "Población", value = "${state.patient.city ?: ""} ${state.patient.postalCode ?: ""}".trim().ifEmpty { "No registrada" })
                DetailRow(label = "Provincia", value = state.patient.province ?: "No registrada")
                DetailRow(label = "Teléfono", value = state.patient.phone ?: "No registrado")
            }
        }

        CollapsibleCard(title = "Información Familiar", icon = Icons.Default.People) {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text("Referente de Emergencia", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                DetailRow(label = "Nombre", value = state.patient.contactName ?: "No asignado")
                DetailRow(label = "Teléfono de Contacto", value = state.patient.contactPhone ?: "No asignado")
            }
        }

        CollapsibleCard(title = "Equipo de Terapeutas", icon = Icons.Default.Group) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (state.treatedBy.isEmpty()) {
                    Text("No hay registros de sesiones todavía.", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                } else {
                    state.treatedBy.forEach { prof ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.AccountCircle, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.Gray)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(prof.fullName, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }
        }
        
        CollapsibleCard(title = "Observaciones Generales", icon = Icons.AutoMirrored.Filled.Assignment) {
            Text(text = state.patient.notes?.ifBlank { "Sin observaciones." } ?: "Sin observaciones.", style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
fun PatientEvolutionTab(state: PatientDetailUiState.Success) {
    val allResults = state.rawResults.sortedByDescending { it.createdAt }
    val totalExercises = allResults.size
    val averageScore = if (totalExercises > 0) allResults.map { it.score }.average().toInt() else 0
    
    val categoryStats = allResults.groupBy { result ->
        when {
            result.activityType.startsWith("orientation") -> "Orientación"
            result.activityType.startsWith("attention") || result.activityType == "number_search" -> "Atención"
            result.activityType.startsWith("memory") -> "Memoria"
            result.activityType.startsWith("language") -> "Lenguaje"
            result.activityType.startsWith("executive") || result.activityType.startsWith("calculation") -> "FF.EE."
            result.activityType.startsWith("perception") -> "Percepción"
            result.activityType.startsWith("literacy") -> "Lectoescritura"
            else -> "Otros"
        }
    }.mapValues { (_, results) -> results.map { it.score }.average().toInt() }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(20.dp)) {
        Text("Análisis de Rendimiento Global", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Card(modifier = Modifier.weight(1f), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f))) {
                Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Total Actividades", style = MaterialTheme.typography.labelSmall)
                    Text(totalExercises.toString(), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black)
                }
            }
            Card(modifier = Modifier.weight(1f), colors = CardDefaults.cardColors(containerColor = if(averageScore > 70) Color(0xFFC8E6C9) else Color(0xFFFFEBEE))) {
                Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Media de Acierto", style = MaterialTheme.typography.labelSmall)
                    Text("$averageScore%", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black, color = if(averageScore > 70) Color(0xFF1B5E20) else Color.Red)
                }
            }
        }

        if (categoryStats.isNotEmpty()) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Rendimiento por Área Cognitiva", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    categoryStats.forEach { (category, score) ->
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(category, style = MaterialTheme.typography.bodySmall)
                                Text("$score%", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                            }
                            LinearProgressIndicator(
                                progress = { score / 100f },
                                modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                                color = if(score > 70) Color(0xFF4CAF50) else if(score > 40) Color(0xFFFFC107) else Color.Red,
                                trackColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        }
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun PatientAssessmentTab(state: PatientDetailUiState.Success, viewModel: PatientDetailViewModel) {
    val profile = state.therapeuticProfile ?: TherapeuticProfile(state.patient.id, SupportLevel.NONE, null, null, null, null, null)
    
    var mobility by remember(profile) { mutableStateOf(profile.mobility ?: "") }
    var basicActivities by remember(profile) { mutableStateOf(profile.basicActivities ?: "") }
    var instrumentalActivities by remember(profile) { mutableStateOf(profile.instrumentalActivities ?: "") }
    var cognitiveStatus by remember(profile) { mutableStateOf(profile.cognitiveStatus ?: "") }
    var emotionalStatus by remember(profile) { mutableStateOf(profile.emotionalStatus ?: "") }
    var risks by remember(profile) { mutableStateOf(profile.risks ?: "") }
    var decisionCapacity by remember(profile) { mutableStateOf(profile.decisionCapacity ?: "") }

    var subTab by remember { mutableStateOf(0) }
    var hasChanges by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize()) {
        Surface(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), modifier = Modifier.fillMaxWidth()) {
            Row(modifier = Modifier.padding(8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("FUNCIONAL", "COGNITIVO", "RIESGOS").forEachIndexed { index, title ->
                    FilterChip(selected = subTab == index, onClick = { subTab = index }, label = { Text(title, fontSize = 11.sp, fontWeight = FontWeight.Bold) }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(8.dp))
                }
            }
        }

        Box(modifier = Modifier.weight(1f)) {
            Column(modifier = Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(20.dp)) {
                when (subTab) {
                    0 -> {
                        AssessmentField(label = "Movilidad", placeholder = "Marcha, equilibrio...", value = mobility, onValueChange = { mobility = it; hasChanges = true })
                        AssessmentField(label = "Actividades Básicas", placeholder = "Higiene, alimentación...", value = basicActivities, onValueChange = { basicActivities = it; hasChanges = true })
                        AssessmentField(label = "Actividades Instrumentales", placeholder = "Medicación, dinero...", value = instrumentalActivities, onValueChange = { instrumentalActivities = it; hasChanges = true })
                    }
                    1 -> {
                        AssessmentField(label = "Estado Cognitivo", placeholder = "Memoria, orientación...", value = cognitiveStatus, onValueChange = { cognitiveStatus = it; hasChanges = true })
                        AssessmentField(label = "Estado Emocional", placeholder = "Ánimo, duelo...", value = emotionalStatus, onValueChange = { emotionalStatus = it; hasChanges = true })
                    }
                    2 -> {
                        AssessmentField(label = "Riesgos Detectados", placeholder = "Caídas, desnutrición...", value = risks, onValueChange = { risks = it; hasChanges = true })
                        AssessmentField(label = "Capacidad de Decisión", placeholder = "Comprensión tratamiento...", value = decisionCapacity, onValueChange = { decisionCapacity = it; hasChanges = true })
                    }
                }
                Spacer(modifier = Modifier.height(80.dp))
            }

            Surface(modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth(), tonalElevation = 4.dp, shadowElevation = 8.dp) {
                Button(
                    onClick = {
                        viewModel.updateClinicalProfile(profile.copy(
                            mobility = mobility, basicActivities = basicActivities, instrumentalActivities = instrumentalActivities,
                            cognitiveStatus = cognitiveStatus, emotionalStatus = emotionalStatus, risks = risks, decisionCapacity = decisionCapacity
                        ))
                        hasChanges = false
                    },
                    modifier = Modifier.fillMaxWidth().padding(16.dp).height(50.dp),
                    enabled = hasChanges && !state.isUpdating,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (state.isUpdating) CircularProgressIndicator(modifier = Modifier.size(20.dp))
                    else { Icon(Icons.Default.Save, null); Spacer(Modifier.width(8.dp)); Text("Guardar Valoración") }
                }
            }
        }
    }
}

@Composable
fun PatientHistoryTab(state: PatientDetailUiState.Success, viewModel: PatientDetailViewModel) {
    val allResults = state.rawResults.sortedByDescending { it.createdAt }
    val totalPages = kotlin.math.ceil(allResults.size.toDouble() / state.historyPageSize).toInt().coerceAtLeast(1)
    val paginated = allResults.drop((state.historyPage - 1) * state.historyPageSize).take(state.historyPageSize)

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Historial Completo", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(16.dp))
        LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(paginated) { ExerciseHistoryRow(it) }
        }
        if (totalPages > 1) {
            PaginationControls(currentPage = state.historyPage, totalPages = totalPages, onPageClick = { viewModel.setHistoryPage(it) })
        }
    }
}

@Composable
private fun ExerciseHistoryRow(result: ActivityResult) {
    val date = result.createdAt.take(10)
    val time = result.createdAt.drop(11).take(5)
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Psychology, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(40.dp))
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(ExerciseTranslationUtils.getDisplayName(result.activityType), fontWeight = FontWeight.Bold)
                Text("${DateUtils.toUserFormat(date)} • $time", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            }
            Text("${result.score}%", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black, color = if(result.score > 70) Color(0xFF2E7D32) else Color.Red)
        }
    }
}

@Composable
fun TransferPatientDialog(currentTherapistId: String?, professionals: List<UserProfile>, onDismiss: () -> Unit, onConfirm: (String) -> Unit) {
    var selectedId by remember { mutableStateOf(currentTherapistId ?: "") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Traspasar Paciente") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                professionals.forEach { prof ->
                    Row(modifier = Modifier.fillMaxWidth().clickable { selectedId = prof.id }.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(selected = selectedId == prof.id, onClick = { selectedId = prof.id })
                        Text(prof.fullName, modifier = Modifier.padding(start = 8.dp))
                    }
                }
            }
        },
        confirmButton = { Button(onClick = { onConfirm(selectedId) }, enabled = selectedId.isNotBlank()) { Text("Confirmar") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } },
        shape = RoundedCornerShape(24.dp)
    )
}

@Composable
private fun CollapsibleCard(title: String, icon: androidx.compose.ui.graphics.vector.ImageVector, initialExpanded: Boolean = false, content: @Composable () -> Unit) {
    var expanded by remember { mutableStateOf(initialExpanded) }
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = if (expanded) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f) else MaterialTheme.colorScheme.surface), border = if (!expanded) BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant) else null) {
        Column {
            Row(modifier = Modifier.fillMaxWidth().clickable { expanded = !expanded }.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(12.dp)); Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                Icon(if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore, null, tint = Color.Gray)
            }
            AnimatedVisibility(visible = expanded) { Column(modifier = Modifier.padding(start = 20.dp, end = 20.dp, bottom = 20.dp)) { content() } }
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
private fun AssessmentField(label: String, placeholder: String, value: String, onValueChange: (String) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(label, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        OutlinedTextField(value = value, onValueChange = onValueChange, placeholder = { Text(placeholder, fontSize = 14.sp) }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), minLines = 3, keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next))
    }
}
