package com.terapia.terasenior.ui.patient

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.terapia.terasenior.domain.model.admin.UserProfile
import com.terapia.terasenior.domain.model.patient.Patient
import com.terapia.terasenior.domain.model.patient.PatientStatus
import com.terapia.terasenior.domain.model.patient.SupportLevel
import com.terapia.terasenior.domain.model.patient.TherapeuticProfile
import com.terapia.terasenior.domain.model.patient.PatientAssessment
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
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Ficha del Paciente")
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "v1.3.49",
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                },
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
                Text("v1.3.49 • ID: ${patient.id.take(8)}", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
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
    var editing by remember { mutableStateOf<PatientAssessment?>(null) }
    var creating by remember { mutableStateOf(false) }
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column { Text("Valoraciones", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold); Text("Historial clínico con autoría y trazabilidad", style = MaterialTheme.typography.bodySmall) }
            Button(onClick = { creating = true }, enabled = !state.isUpdating) { Icon(Icons.Default.Add, null); Spacer(Modifier.width(6.dp)); Text("Nueva valoración") }
        }
        Spacer(Modifier.height(12.dp))
        if (state.assessments.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("Todavía no hay valoraciones registradas.") }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                itemsIndexed(state.assessments, key = { _, assessment -> assessment.id }) { index, assessment ->
                    Card(onClick = { editing = assessment }, modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = if (assessment.status == "ACTIVE") MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceVariant)) {
                        Row(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Surface(shape = CircleShape, color = MaterialTheme.colorScheme.primaryContainer) { Text("${index + 1}", modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp), fontWeight = FontWeight.Bold) }
                            Spacer(Modifier.width(14.dp))
                            Column(Modifier.weight(1f)) { Text("Valoración ${index + 1}", fontWeight = FontWeight.Bold); Text(if (assessment.status == "ACTIVE") "Activa · ${assessment.createdAt.take(10)}" else "Dada de baja · ${assessment.discontinuedAt?.take(10).orEmpty()}", style = MaterialTheme.typography.bodySmall, color = if (assessment.status == "ACTIVE") MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.error) }
                            if (assessment.status == "ACTIVE") { IconButton(onClick = { editing = assessment }) { Icon(Icons.Default.Edit, "Modificar valoración") }; IconButton(onClick = { viewModel.discontinueAssessment(assessment) }) { Icon(Icons.Default.Block, "Dar de baja valoración", tint = MaterialTheme.colorScheme.error) } }
                        }
                    }
                }
            }
        }
    }
    if (creating || editing != null) AssessmentEditorDialog(editing, state.patient.id, onDismiss = { creating = false; editing = null }, onSave = { viewModel.saveAssessment(it); creating = false; editing = null })
}

@Composable
private fun AssessmentSummary(label: String, value: String?) { if (!value.isNullOrBlank()) Text("$label: $value", style = MaterialTheme.typography.bodySmall) }

@Composable
private fun AssessmentEditorDialog(existing: PatientAssessment?, patientId: String, onDismiss: () -> Unit, onSave: (PatientAssessment) -> Unit) {
    var mobility by remember(existing) { mutableStateOf(existing?.mobility.orEmpty()) }; var basic by remember(existing) { mutableStateOf(existing?.basicActivities.orEmpty()) }; var instrumental by remember(existing) { mutableStateOf(existing?.instrumentalActivities.orEmpty()) }; var cognitive by remember(existing) { mutableStateOf(existing?.cognitiveStatus.orEmpty()) }; var emotional by remember(existing) { mutableStateOf(existing?.emotionalStatus.orEmpty()) }; var risks by remember(existing) { mutableStateOf(existing?.risks.orEmpty()) }; var decision by remember(existing) { mutableStateOf(existing?.decisionCapacity.orEmpty()) }
    var section by remember { mutableStateOf(0) }
    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(shape = RoundedCornerShape(24.dp), color = MaterialTheme.colorScheme.surface, modifier = Modifier.fillMaxWidth(0.88f).heightIn(min = 620.dp, max = 820.dp)) {
            Column(Modifier.padding(24.dp)) {
                Text(if (existing == null) "Nueva valoración" else "Modificar valoración", style = MaterialTheme.typography.headlineSmall)
                Spacer(Modifier.height(16.dp))
                TabRow(selectedTabIndex = section) {
                    listOf("Funcional", "Cognitivo", "Riesgos", "Historial").forEachIndexed { index, title -> Tab(selected = section == index, onClick = { section = index }, text = { Text(title) }) }
                }
                Spacer(Modifier.height(16.dp))
                Column(Modifier.weight(1f).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    when (section) {
                        0 -> { AssessmentEditorField("Movilidad", mobility) { mobility = it }; AssessmentEditorField("Actividades básicas", basic) { basic = it }; AssessmentEditorField("Actividades instrumentales", instrumental) { instrumental = it } }
                        1 -> { AssessmentEditorField("Estado cognitivo", cognitive) { cognitive = it }; AssessmentEditorField("Estado emocional", emotional) { emotional = it } }
                        2 -> { AssessmentEditorField("Riesgos detectados", risks) { risks = it }; AssessmentEditorField("Capacidad de decisión", decision) { decision = it } }
                        else -> Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text("Trazabilidad de la valoración", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text("Creada por: ${existing?.authorName ?: "Se asignará al guardar"}")
                            Text("Puesto: ${existing?.authorRole ?: "Profesional"}")
                            if (existing != null) { Text("Fecha de creación: ${existing.createdAt.take(16).replace('T', ' ')}"); Text("Última modificación: ${existing.updatedAt.take(16).replace('T', ' ')}"); existing.updatedByName?.let { Text("Modificada por: $it${existing.updatedByRole?.let { role -> " · $role" }.orEmpty()}") }; existing.discontinuedAt?.let { Text("Baja: ${it.take(16).replace('T', ' ')} · ${existing.discontinuedByName.orEmpty()}", color = MaterialTheme.colorScheme.error) } } else Text("El historial se registrará al guardar esta valoración.")
                        }
                    }
                }
                Spacer(Modifier.height(16.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("Cancelar") }
                    Spacer(Modifier.width(8.dp))
                    Button(onClick = { onSave((existing ?: PatientAssessment("", patientId, "", "", "")).copy(mobility = mobility.ifBlank { null }, basicActivities = basic.ifBlank { null }, instrumentalActivities = instrumental.ifBlank { null }, cognitiveStatus = cognitive.ifBlank { null }, emotionalStatus = emotional.ifBlank { null }, risks = risks.ifBlank { null }, decisionCapacity = decision.ifBlank { null })) }) { Icon(Icons.Default.Save, null); Spacer(Modifier.width(6.dp)); Text("Guardar valoración") }
                }
            }
        }
    }
}

@Composable
private fun AssessmentEditorField(label: String, value: String, onChange: (String) -> Unit) { OutlinedTextField(value = value, onValueChange = onChange, label = { Text(label) }, modifier = Modifier.fillMaxWidth(), minLines = 7, colors = OutlinedTextFieldDefaults.colors(focusedContainerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.22f), unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))) }

@Composable
fun PatientHistoryTab(state: PatientDetailUiState.Success, viewModel: PatientDetailViewModel) {
    val history = state.sessionsHistory.sortedByDescending { it.session.createdAt }
    val totalPages = kotlin.math.ceil(history.size.toDouble() / state.historyPageSize).toInt().coerceAtLeast(1)
    val paginated = history.drop((state.historyPage - 1) * state.historyPageSize).take(state.historyPageSize)

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Historial Clínico de Sesiones", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(16.dp))
        
        LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            items(paginated) { sessionHistory ->
                SessionHistoryCard(sessionHistory)
            }
        }
        
        if (totalPages > 1) {
            PaginationControls(currentPage = state.historyPage, totalPages = totalPages, onPageClick = { viewModel.setHistoryPage(it) })
        }
    }
}

@Composable
private fun SessionHistoryCard(history: com.terapia.terasenior.domain.model.therapy.PatientSessionHistory) {
    val session = history.session
    val date = session.createdAt.take(10)
    var showResultsDialog by remember(session.id) { mutableStateOf(false) }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = DateUtils.toUserFormat(date),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = session.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                if (session.valuation > 0) {
                    Row {
                        repeat(5) { i ->
                            Icon(
                                Icons.Default.Star,
                                null,
                                modifier = Modifier.size(16.dp),
                                tint = if (i < session.valuation) Color(0xFFFFC107) else Color.LightGray
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Datos de la sesión (v1.3.46)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Participación", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    Text(participationLabel(session.participationLevel), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text("Fatiga", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    Text(fatigueLabel(session.fatigueLevel), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text("Rendimiento", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    Text("${session.totalHits} ac. / ${session.totalErrors} err.", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                }
                Column(modifier = Modifier.weight(1f)) {
                    val mins = session.totalDurationSeconds / 60
                    val secs = session.totalDurationSeconds % 60
                    Text("Tiempo", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    Text("${mins}m ${secs}s", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                }
            }

            if (!session.therapistNotes.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(12.dp))
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = session.therapistNotes,
                        modifier = Modifier.padding(12.dp),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            if (history.results.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                OutlinedButton(onClick = { showResultsDialog = true }) {
                    Icon(Icons.AutoMirrored.Filled.Assignment, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Resultados del ejercicio")
                }
            }
        }
    }

    if (showResultsDialog) {
        ExerciseResultsDialog(history = history, onDismiss = { showResultsDialog = false })
    }
}

@Composable
private fun ExerciseResultsDialog(
    history: com.terapia.terasenior.domain.model.therapy.PatientSessionHistory,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Resultados del ejercicio") },
        text = {
            Column(
                modifier = Modifier.heightIn(max = 420.dp).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                history.groupedByCategory.entries.sortedBy { it.key }.forEach { (area, results) ->
                    Text(area, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    results.forEach { result ->
                        val hits = if (result.score > 0) 1 else 0
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(ExerciseTranslationUtils.getDisplayName(result.activityType), modifier = Modifier.weight(1f))
                            Text("$hits aciertos · ${result.errorsCount} fallos", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Cerrar") } }
    )
}

private fun participationLabel(value: String?): String = when (value) {
    "LOW" -> "Baja"
    "MEDIUM" -> "Media"
    "HIGH" -> "Alta"
    else -> "No registrada"
}

private fun fatigueLabel(value: String?): String = when (value) {
    "NONE" -> "Ninguna"
    "MODERATE" -> "Moderada"
    "HIGH" -> "Alta"
    else -> "No registrada"
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
    Column(modifier = Modifier.fillMaxWidth().widthIn(max = 800.dp).padding(horizontal = 4.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(label, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text(placeholder, fontSize = 14.sp) },
            modifier = Modifier.fillMaxWidth().heightIn(min = 120.dp),
            shape = RoundedCornerShape(12.dp),
            minLines = 4,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
        )
    }
}
