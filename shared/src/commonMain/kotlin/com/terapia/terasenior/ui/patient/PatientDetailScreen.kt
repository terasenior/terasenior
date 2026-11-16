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
import com.terapia.terasenior.domain.model.patient.PatientDischarge
import com.terapia.terasenior.domain.model.patient.OccupationalHistory
import com.terapia.terasenior.domain.model.patient.OccupationalFollowUp
import com.terapia.terasenior.domain.model.results.ActivityResult
import com.terapia.terasenior.ui.components.PaginationControls
import com.terapia.terasenior.ui.components.LocalTeraseniorLayout
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
                    
                    ScrollableTabRow(selectedTabIndex = selectedTab) {
                        Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }, text = { Text("Perfil") })
                        Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }, text = { Text("Evolución") })
                        Tab(selected = selectedTab == 2, onClick = { selectedTab = 2 }, text = { Text("Valoración") })
                        Tab(selected = selectedTab == 3, onClick = { selectedTab = 3 }, text = { Text("Historial") })
                        Tab(selected = selectedTab == 4, onClick = { selectedTab = 4 }, text = { Text("Alta") })
                        Tab(selected = selectedTab == 5, onClick = { selectedTab = 5 }, text = { Text("Historia TO") })
                        Tab(selected = selectedTab == 6, onClick = { selectedTab = 6 }, text = { Text("Seguimiento TO") })
                    }

                    Box(modifier = Modifier.weight(1f)) {
                        when (selectedTab) {
                            0 -> PatientInfoTab(state)
                            1 -> PatientEvolutionTab(state)
                            2 -> PatientAssessmentTab(state, viewModel)
                            3 -> PatientHistoryTab(state, viewModel)
                            4 -> PatientDischargeTab(state, viewModel)
                            5 -> OccupationalHistoryTab(state, viewModel)
                            6 -> OccupationalFollowUpTab(state, viewModel)
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
    val layout = LocalTeraseniorLayout.current
    var mobility by remember(existing) { mutableStateOf(existing?.mobility.orEmpty()) }; var basic by remember(existing) { mutableStateOf(existing?.basicActivities.orEmpty()) }; var instrumental by remember(existing) { mutableStateOf(existing?.instrumentalActivities.orEmpty()) }; var cognitive by remember(existing) { mutableStateOf(existing?.cognitiveStatus.orEmpty()) }; var emotional by remember(existing) { mutableStateOf(existing?.emotionalStatus.orEmpty()) }; var risks by remember(existing) { mutableStateOf(existing?.risks.orEmpty()) }; var decision by remember(existing) { mutableStateOf(existing?.decisionCapacity.orEmpty()) }
    var section by remember { mutableStateOf(0) }
    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier
                .fillMaxWidth(if (layout.isCompactWidth) 0.96f else 0.88f)
                .fillMaxHeight(if (layout.isCompactHeight) 0.94f else 0.86f)
                .heightIn(max = 820.dp)
        ) {
            Column(Modifier.padding(if (layout.isCompactHeight) 16.dp else 24.dp)) {
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
private fun PatientDischargeTab(state: PatientDetailUiState.Success, viewModel: PatientDetailViewModel) {
    var showEditor by remember { mutableStateOf(false) }
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column {
                Text("Altas", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text("Registro asistencial y administrativo de finalización", style = MaterialTheme.typography.bodySmall)
            }
            Button(onClick = { showEditor = true }, enabled = !state.isUpdating) {
                Icon(Icons.Default.Add, null)
                Spacer(Modifier.width(6.dp))
                Text("Registrar alta")
            }
        }
        Spacer(Modifier.height(16.dp))
        if (state.discharges.isEmpty()) {
            Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))) {
                Text("Todavía no hay altas registradas para este paciente.", modifier = Modifier.padding(20.dp))
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(state.discharges, key = { it.id }) { discharge ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("Alta · ${discharge.dischargeDate}", fontWeight = FontWeight.Bold)
                            Text(discharge.dischargeReason, style = MaterialTheme.typography.bodyMedium)
                            if (discharge.isVoluntary) Text("Alta voluntaria", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelMedium)
                            Text("Registrada por ${discharge.authorName}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
    }
    if (showEditor) {
        PatientDischargeEditorDialog(
            patient = state.patient,
            onDismiss = { showEditor = false },
            onSave = { discharge ->
                viewModel.saveDischarge(discharge)
                showEditor = false
            }
        )
    }
}

@Composable
private fun PatientDischargeEditorDialog(
    patient: Patient,
    onDismiss: () -> Unit,
    onSave: (PatientDischarge) -> Unit
) {
    val layout = LocalTeraseniorLayout.current
    var assessmentDate by remember { mutableStateOf("") }
    var serviceArrivalDate by remember { mutableStateOf(patient.admissionDate ?: "") }
    var interventionStartDate by remember { mutableStateOf("") }
    var dischargeDate by remember { mutableStateOf("") }
    var programDuration by remember { mutableStateOf("") }
    var programFollowUp by remember { mutableStateOf("") }
    var reason by remember { mutableStateOf("") }
    var observations by remember { mutableStateOf("") }
    var isVoluntary by remember { mutableStateOf(false) }
    var declarantName by remember { mutableStateOf("") }
    var declarantDocumentId by remember { mutableStateOf("") }
    var declarantRelationship by remember { mutableStateOf("") }
    var declarationDate by remember { mutableStateOf("") }
    var validationError by remember { mutableStateOf<String?>(null) }

    fun nullable(value: String) = value.trim().ifBlank { null }
    fun isIsoDate(value: String) = value.isBlank() || Regex("\\d{4}-\\d{2}-\\d{2}").matches(value)
    fun validDates() = listOf(assessmentDate, serviceArrivalDate, interventionStartDate, dischargeDate, declarationDate).all(::isIsoDate)

    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxWidth(if (layout.isCompactWidth) 0.96f else 0.78f)
                .fillMaxHeight(if (layout.isCompactHeight) 0.94f else 0.88f)
                .heightIn(max = 860.dp)
        ) {
            Column(Modifier.padding(if (layout.isCompactHeight) 16.dp else 24.dp)) {
                Text("Registrar alta", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Text("Las fechas se introducen en formato AAAA-MM-DD.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(16.dp))
                Column(Modifier.weight(1f).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    DischargeField("Fecha de valoración CEDEC", assessmentDate) { assessmentDate = it }
                    DischargeField("Fecha de llegada al servicio", serviceArrivalDate) { serviceArrivalDate = it }
                    DischargeField("Fecha de inicio de intervención", interventionStartDate) { interventionStartDate = it }
                    DischargeField("Fecha de alta *", dischargeDate) { dischargeDate = it }
                    DischargeField("Temporalidad del programa", programDuration, minLines = 3) { programDuration = it }
                    DischargeField("Seguimiento del programa", programFollowUp, minLines = 3) { programFollowUp = it }
                    DischargeField("Motivo del alta *", reason, minLines = 3) { reason = it }
                    DischargeField("Observaciones", observations, minLines = 4) { observations = it }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = isVoluntary, onCheckedChange = { isVoluntary = it })
                        Text("Alta voluntaria")
                    }
                    if (isVoluntary) {
                        Text("Datos de la declaración voluntaria", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        DischargeField("Nombre de quien declara *", declarantName) { declarantName = it }
                        DischargeField("DNI/NIE de quien declara *", declarantDocumentId) { declarantDocumentId = it }
                        DischargeField("Relación con la persona beneficiaria *", declarantRelationship) { declarantRelationship = it }
                        DischargeField("Fecha de declaración *", declarationDate) { declarationDate = it }
                    }
                    validationError?.let { Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall) }
                }
                Spacer(Modifier.height(16.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("Cancelar") }
                    Spacer(Modifier.width(8.dp))
                    Button(onClick = {
                        validationError = when {
                            dischargeDate.isBlank() || reason.isBlank() -> "Indica la fecha y el motivo del alta."
                            !validDates() -> "Revisa el formato de las fechas: AAAA-MM-DD."
                            isVoluntary && listOf(declarantName, declarantDocumentId, declarantRelationship, declarationDate).any { it.isBlank() } -> "Completa todos los datos de la declaración voluntaria."
                            else -> null
                        }
                        if (validationError == null) onSave(PatientDischarge(
                            patientId = patient.id,
                            assessmentDate = nullable(assessmentDate), serviceArrivalDate = nullable(serviceArrivalDate),
                            interventionStartDate = nullable(interventionStartDate), dischargeDate = dischargeDate.trim(),
                            programDuration = nullable(programDuration), programFollowUp = nullable(programFollowUp),
                            dischargeReason = reason.trim(), observations = nullable(observations), isVoluntary = isVoluntary,
                            voluntaryDeclarantName = nullable(declarantName), voluntaryDeclarantDocumentId = nullable(declarantDocumentId),
                            voluntaryDeclarantRelationship = nullable(declarantRelationship), voluntaryDeclarationDate = nullable(declarationDate)
                        ))
                    }) {
                        Icon(Icons.Default.Save, null)
                        Spacer(Modifier.width(6.dp))
                        Text("Guardar alta")
                    }
                }
            }
        }
    }
}

@Composable
private fun DischargeField(label: String, value: String, minLines: Int = 1, onChange: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        label = { Text(label) },
        modifier = Modifier.fillMaxWidth(),
        minLines = minLines
    )
}

@Composable
private fun OccupationalHistoryTab(state: PatientDetailUiState.Success, viewModel: PatientDetailViewModel) {
    var showEditor by remember { mutableStateOf(false) }
    Column(modifier = Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column {
                Text("Historia de terapia ocupacional", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text("Información funcional, contextual y terapéutica", style = MaterialTheme.typography.bodySmall)
            }
            Button(onClick = { showEditor = true }, enabled = !state.isUpdating) {
                Icon(if (state.occupationalHistory == null) Icons.Default.Add else Icons.Default.Edit, null)
                Spacer(Modifier.width(6.dp))
                Text(if (state.occupationalHistory == null) "Crear" else "Editar")
            }
        }
        val history = state.occupationalHistory
        if (history == null) {
            Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))) {
                Text("Todavía no hay una historia de terapia ocupacional registrada.", modifier = Modifier.padding(20.dp))
            }
        } else {
            OccupationalHistorySummary(history)
        }
    }
    if (showEditor) OccupationalHistoryEditorDialog(
        patientId = state.patient.id,
        existing = state.occupationalHistory,
        onDismiss = { showEditor = false },
        onSave = { history -> viewModel.saveOccupationalHistory(history); showEditor = false }
    )
}

@Composable
private fun OccupationalHistorySummary(history: OccupationalHistory) {
    val groups = listOf(
        "Motivo de derivación" to history.referralReason,
        "Diagnóstico y antecedentes de salud" to listOfNotNull(history.medicalDiagnosis, history.relevantHealthHistory, history.medication).filter { it.isNotBlank() }.joinToString("\n"),
        "Autonomía y funcionamiento" to history.functionalAssessment,
        "Cognición, estado emocional y comunicación" to listOfNotNull(history.cognitiveEmotionalStatus, history.sensoryCommunication).filter { it.isNotBlank() }.joinToString("\n"),
        "Entorno y red de apoyo" to listOfNotNull(history.housingAndSupport, history.familySocialContext).filter { it.isNotBlank() }.joinToString("\n"),
        "Rutinas, ocupación e intereses" to listOfNotNull(history.dailyRoutines, history.occupationalHistory, history.interestsPreferences).filter { it.isNotBlank() }.joinToString("\n"),
        "Objetivos e intervención" to listOfNotNull(history.therapeuticGoals, history.interventionPlan).filter { it.isNotBlank() }.joinToString("\n"),
        "Observaciones" to history.observations
    )
    groups.filter { !it.second.isNullOrBlank() }.forEach { (title, value) ->
        CollapsibleCard(title = title, icon = Icons.Default.Description) { Text(value.orEmpty(), style = MaterialTheme.typography.bodyMedium) }
    }
}

@Composable
private fun OccupationalHistoryEditorDialog(
    patientId: String,
    existing: OccupationalHistory?,
    onDismiss: () -> Unit,
    onSave: (OccupationalHistory) -> Unit
) {
    val layout = LocalTeraseniorLayout.current
    var referralReason by remember(existing) { mutableStateOf(existing?.referralReason.orEmpty()) }
    var medicalDiagnosis by remember(existing) { mutableStateOf(existing?.medicalDiagnosis.orEmpty()) }
    var relevantHealthHistory by remember(existing) { mutableStateOf(existing?.relevantHealthHistory.orEmpty()) }
    var medication by remember(existing) { mutableStateOf(existing?.medication.orEmpty()) }
    var functionalAssessment by remember(existing) { mutableStateOf(existing?.functionalAssessment.orEmpty()) }
    var cognitiveEmotionalStatus by remember(existing) { mutableStateOf(existing?.cognitiveEmotionalStatus.orEmpty()) }
    var sensoryCommunication by remember(existing) { mutableStateOf(existing?.sensoryCommunication.orEmpty()) }
    var housingAndSupport by remember(existing) { mutableStateOf(existing?.housingAndSupport.orEmpty()) }
    var familySocialContext by remember(existing) { mutableStateOf(existing?.familySocialContext.orEmpty()) }
    var dailyRoutines by remember(existing) { mutableStateOf(existing?.dailyRoutines.orEmpty()) }
    var occupationalHistory by remember(existing) { mutableStateOf(existing?.occupationalHistory.orEmpty()) }
    var interestsPreferences by remember(existing) { mutableStateOf(existing?.interestsPreferences.orEmpty()) }
    var therapeuticGoals by remember(existing) { mutableStateOf(existing?.therapeuticGoals.orEmpty()) }
    var interventionPlan by remember(existing) { mutableStateOf(existing?.interventionPlan.orEmpty()) }
    var observations by remember(existing) { mutableStateOf(existing?.observations.orEmpty()) }
    var section by remember { mutableStateOf(0) }
    fun nullable(value: String) = value.trim().ifBlank { null }

    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxWidth(if (layout.isCompactWidth) 0.96f else 0.88f)
                .fillMaxHeight(if (layout.isCompactHeight) 0.94f else 0.88f)
                .heightIn(max = 860.dp)
        ) {
            Column(Modifier.padding(if (layout.isCompactHeight) 16.dp else 24.dp)) {
                Text("Historia de terapia ocupacional", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(16.dp))
                TabRow(selectedTabIndex = section) {
                    listOf("Salud", "Función", "Entorno", "Plan").forEachIndexed { index, title ->
                        Tab(selected = section == index, onClick = { section = index }, text = { Text(title) })
                    }
                }
                Spacer(Modifier.height(16.dp))
                Column(Modifier.weight(1f).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    when (section) {
                        0 -> {
                            DischargeField("Motivo de derivación", referralReason, 3) { referralReason = it }
                            DischargeField("Diagnóstico médico relevante", medicalDiagnosis, 3) { medicalDiagnosis = it }
                            DischargeField("Antecedentes de salud relevantes", relevantHealthHistory, 4) { relevantHealthHistory = it }
                            DischargeField("Medicación relevante", medication, 3) { medication = it }
                        }
                        1 -> {
                            DischargeField("Valoración funcional y autonomía", functionalAssessment, 5) { functionalAssessment = it }
                            DischargeField("Estado cognitivo y emocional", cognitiveEmotionalStatus, 4) { cognitiveEmotionalStatus = it }
                            DischargeField("Comunicación y aspectos sensoriales", sensoryCommunication, 4) { sensoryCommunication = it }
                        }
                        2 -> {
                            DischargeField("Vivienda y apoyos", housingAndSupport, 4) { housingAndSupport = it }
                            DischargeField("Contexto familiar y social", familySocialContext, 4) { familySocialContext = it }
                            DischargeField("Rutinas diarias", dailyRoutines, 4) { dailyRoutines = it }
                            DischargeField("Historia ocupacional", occupationalHistory, 4) { occupationalHistory = it }
                            DischargeField("Intereses y preferencias", interestsPreferences, 4) { interestsPreferences = it }
                        }
                        else -> {
                            DischargeField("Objetivos terapéuticos", therapeuticGoals, 5) { therapeuticGoals = it }
                            DischargeField("Plan de intervención", interventionPlan, 5) { interventionPlan = it }
                            DischargeField("Observaciones", observations, 5) { observations = it }
                        }
                    }
                }
                Spacer(Modifier.height(16.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("Cancelar") }
                    Spacer(Modifier.width(8.dp))
                    Button(onClick = { onSave(OccupationalHistory(
                        patientId = patientId, referralReason = nullable(referralReason), medicalDiagnosis = nullable(medicalDiagnosis),
                        relevantHealthHistory = nullable(relevantHealthHistory), medication = nullable(medication),
                        functionalAssessment = nullable(functionalAssessment), cognitiveEmotionalStatus = nullable(cognitiveEmotionalStatus),
                        sensoryCommunication = nullable(sensoryCommunication), housingAndSupport = nullable(housingAndSupport),
                        familySocialContext = nullable(familySocialContext), dailyRoutines = nullable(dailyRoutines),
                        occupationalHistory = nullable(occupationalHistory), interestsPreferences = nullable(interestsPreferences),
                        therapeuticGoals = nullable(therapeuticGoals), interventionPlan = nullable(interventionPlan), observations = nullable(observations)
                    )) }) { Icon(Icons.Default.Save, null); Spacer(Modifier.width(6.dp)); Text("Guardar historia") }
                }
            }
        }
    }
}

@Composable
private fun OccupationalFollowUpTab(state: PatientDetailUiState.Success, viewModel: PatientDetailViewModel) {
    var showEditor by remember { mutableStateOf(false) }
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column {
                Text("Seguimiento de terapia ocupacional", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text("Registros fechados de indicaciones, intervención y evolución", style = MaterialTheme.typography.bodySmall)
            }
            Button(onClick = { showEditor = true }, enabled = !state.isUpdating) {
                Icon(Icons.Default.Add, null)
                Spacer(Modifier.width(6.dp))
                Text("Nuevo seguimiento")
            }
        }
        Spacer(Modifier.height(16.dp))
        if (state.occupationalFollowUps.isEmpty()) {
            Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))) {
                Text("Todavía no hay seguimientos registrados.", modifier = Modifier.padding(20.dp))
            }
        } else {
            LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(state.occupationalFollowUps, key = { it.id }) { followUp ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                            Text("Seguimiento · ${followUp.followUpDate}", fontWeight = FontWeight.Bold)
                            FollowUpSummary("Indicaciones", followUp.neuropsychologicalIndications)
                            FollowUpSummary("Objetivos", followUp.treatmentObjectives)
                            FollowUpSummary("Intervención", followUp.intervention)
                            FollowUpSummary("Observaciones", followUp.observations)
                            Text("Profesional: ${followUp.signatureName ?: followUp.authorName}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
    }
    if (showEditor) OccupationalFollowUpEditorDialog(
        patientId = state.patient.id,
        onDismiss = { showEditor = false },
        onSave = { followUp -> viewModel.saveOccupationalFollowUp(followUp); showEditor = false }
    )
}

@Composable
private fun FollowUpSummary(label: String, value: String?) {
    if (!value.isNullOrBlank()) Text("$label: $value", style = MaterialTheme.typography.bodyMedium)
}

@Composable
private fun OccupationalFollowUpEditorDialog(
    patientId: String,
    onDismiss: () -> Unit,
    onSave: (OccupationalFollowUp) -> Unit
) {
    val layout = LocalTeraseniorLayout.current
    var followUpDate by remember { mutableStateOf("") }
    var indications by remember { mutableStateOf("") }
    var objectives by remember { mutableStateOf("") }
    var intervention by remember { mutableStateOf("") }
    var observations by remember { mutableStateOf("") }
    var signatureName by remember { mutableStateOf("") }
    var validationError by remember { mutableStateOf<String?>(null) }
    fun nullable(value: String) = value.trim().ifBlank { null }

    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxWidth(if (layout.isCompactWidth) 0.96f else 0.78f)
                .fillMaxHeight(if (layout.isCompactHeight) 0.94f else 0.82f)
                .heightIn(max = 760.dp)
        ) {
            Column(Modifier.padding(if (layout.isCompactHeight) 16.dp else 24.dp)) {
                Text("Nuevo seguimiento", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Text("Fecha en formato AAAA-MM-DD.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(16.dp))
                Column(Modifier.weight(1f).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    DischargeField("Fecha *", followUpDate) { followUpDate = it }
                    DischargeField("Indicaciones de neuropsicología", indications, 4) { indications = it }
                    DischargeField("Objetivos de tratamiento", objectives, 4) { objectives = it }
                    DischargeField("Intervención realizada", intervention, 5) { intervention = it }
                    DischargeField("Observaciones", observations, 5) { observations = it }
                    DischargeField("Nombre para firma", signatureName) { signatureName = it }
                    validationError?.let { Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall) }
                }
                Spacer(Modifier.height(16.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("Cancelar") }
                    Spacer(Modifier.width(8.dp))
                    Button(onClick = {
                        validationError = when {
                            !Regex("\\d{4}-\\d{2}-\\d{2}").matches(followUpDate.trim()) -> "Indica una fecha válida en formato AAAA-MM-DD."
                            else -> null
                        }
                        if (validationError == null) onSave(OccupationalFollowUp(
                            patientId = patientId, followUpDate = followUpDate.trim(),
                            neuropsychologicalIndications = nullable(indications), treatmentObjectives = nullable(objectives),
                            intervention = nullable(intervention), observations = nullable(observations), signatureName = nullable(signatureName)
                        ))
                    }) { Icon(Icons.Default.Save, null); Spacer(Modifier.width(6.dp)); Text("Guardar seguimiento") }
                }
            }
        }
    }
}

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
