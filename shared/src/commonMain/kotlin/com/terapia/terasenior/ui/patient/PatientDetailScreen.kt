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
import com.terapia.terasenior.domain.model.patient.Patient
import com.terapia.terasenior.domain.model.patient.PatientStatus
import com.terapia.terasenior.domain.model.patient.SupportLevel
import com.terapia.terasenior.domain.model.patient.TherapeuticProfile
import com.terapia.terasenior.domain.model.results.ActivityResult
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
        // NUEVO ICONO DE PERSONA
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
                Text("v1.1.6 • ID: ${patient.id.take(8)}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
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
        // Datos Administrativos
        CollapsibleCard(
            title = "Datos Administrativos",
            icon = Icons.Default.Business,
            initialExpanded = false
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                DetailRow(label = "NIF / DNI", value = state.patient.nif ?: "No registrado")
                DetailRow(label = "Nº Expediente", value = state.patient.externalId ?: "No asignado")
                DetailRow(label = "Fecha de Alta", value = DateUtils.toUserFormat(state.patient.admissionDate))
                DetailRow(label = "Fecha de Baja", value = DateUtils.toUserFormat(state.patient.dischargeDate))
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
                DetailRow(label = "Fecha de Nacimiento", value = DateUtils.toUserFormat(state.patient.birthDate))
            }
        }

        // Localización y Contacto
        CollapsibleCard(
            title = "Localización y Contacto",
            icon = Icons.Default.LocationOn
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                DetailRow(label = "Dirección", value = state.patient.address ?: "No registrada")
                DetailRow(label = "Población", value = "${state.patient.city ?: ""} ${state.patient.postalCode ?: ""}".trim().ifEmpty { "No registrada" })
                DetailRow(label = "Provincia", value = state.patient.province ?: "No registrada")
                DetailRow(label = "Teléfono", value = state.patient.phone ?: "No registrado")
            }
        }

        // Información Familiar
        CollapsibleCard(
            title = "Información Familiar",
            icon = Icons.Default.People
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text("Referente de Emergencia", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                DetailRow(label = "Nombre", value = state.patient.contactName ?: "No asignado")
                DetailRow(label = "Teléfono de Contacto", value = state.patient.contactPhone ?: "No asignado")
            }
        }
        
        // Notas Generales
        CollapsibleCard(
            title = "Observaciones Generales",
            icon = Icons.AutoMirrored.Filled.Assignment
        ) {
            Text(
                text = state.patient.notes?.ifBlank { "Sin observaciones." } ?: "Sin observaciones.",
                style = MaterialTheme.typography.bodyMedium
            )
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
            result.activityType.startsWith("executive") || result.activityType.startsWith("calculation") -> "Funciones Ejecutivas"
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
    
    var mobility by remember { mutableStateOf(profile.mobility ?: "") }
    var basicActivities by remember { mutableStateOf(profile.basicActivities ?: "") }
    var instrumentalActivities by remember { mutableStateOf(profile.instrumentalActivities ?: "") }
    var cognitiveStatus by remember { mutableStateOf(profile.cognitiveStatus ?: "") }
    var emotionalStatus by remember { mutableStateOf(profile.emotionalStatus ?: "") }
    var risks by remember { mutableStateOf(profile.risks ?: "") }
    var decisionCapacity by remember { mutableStateOf(profile.decisionCapacity ?: "") }

    var subTab by remember { mutableStateOf(0) }
    var hasChanges by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize()) {
        TabRow(selectedTabIndex = subTab, modifier = Modifier.fillMaxWidth()) {
            Tab(selected = subTab == 0, onClick = { subTab = 0 }, text = { Text("Funcional", fontSize = 12.sp) })
            Tab(selected = subTab == 1, onClick = { subTab = 1 }, text = { Text("Cognitivo", fontSize = 12.sp) })
            Tab(selected = subTab == 2, onClick = { subTab = 2 }, text = { Text("Riesgos", fontSize = 12.sp) })
        }

        Column(
            modifier = Modifier.weight(1f).padding(16.dp).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            when (subTab) {
                0 -> {
                    AssessmentField(label = "Movilidad", placeholder = "Marcha, equilibrio, transferencias...", value = mobility, onValueChange = { mobility = it; hasChanges = true })
                    AssessmentField(label = "Actividades Básicas", placeholder = "Alimentación, higiene, continencia...", value = basicActivities, onValueChange = { basicActivities = it; hasChanges = true })
                    AssessmentField(label = "Actividades Instrumentales", placeholder = "Medicación, dinero, teléfono...", value = instrumentalActivities, onValueChange = { instrumentalActivities = it; hasChanges = true })
                }
                1 -> {
                    AssessmentField(label = "Estado Cognitivo", placeholder = "Orientación, memoria, atención...", value = cognitiveStatus, onValueChange = { cognitiveStatus = it; hasChanges = true })
                    AssessmentField(label = "Estado Emocional", placeholder = "Ánimo, duelo, aislamiento...", value = emotionalStatus, onValueChange = { emotionalStatus = it; hasChanges = true })
                }
                2 -> {
                    AssessmentField(label = "Riesgos Detectados", placeholder = "Caídas, úlceras, desnutrición...", value = risks, onValueChange = { risks = it; hasChanges = true })
                    AssessmentField(label = "Capacidad de Decisión", placeholder = "Comprensión del tratamiento...", value = decisionCapacity, onValueChange = { decisionCapacity = it; hasChanges = true })
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = {
                    viewModel.updateClinicalProfile(profile.copy(
                        mobility = mobility, basicActivities = basicActivities, instrumentalActivities = instrumentalActivities,
                        cognitiveStatus = cognitiveStatus, emotionalStatus = emotionalStatus, risks = risks, decisionCapacity = decisionCapacity
                    ))
                    hasChanges = false
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                enabled = hasChanges && !state.isUpdating,
                shape = RoundedCornerShape(12.dp)
            ) {
                if (state.isUpdating) CircularProgressIndicator(modifier = Modifier.size(24.dp)) else Text("Guardar Cambios de Valoración")
            }
            
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
fun PatientHistoryTab(state: PatientDetailUiState.Success, viewModel: PatientDetailViewModel) {
    val allResults = state.rawResults.sortedByDescending { it.createdAt }
    val totalResults = allResults.size
    val totalPages = kotlin.math.ceil(totalResults.toDouble() / state.historyPageSize).toInt().coerceAtLeast(1)
    
    val startIndex = (state.historyPage - 1) * state.historyPageSize
    val paginatedResults = allResults.drop(startIndex).take(state.historyPageSize)

    if (allResults.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No hay actividades registradas todavía.", color = Color.Gray)
        }
    } else {
        Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            Text("Historial Completo de Ejercicios", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))
            
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(paginatedResults) { result ->
                    ExerciseHistoryRow(result)
                }
            }
            
            if (totalPages > 1) {
                PaginationControls(
                    currentPage = state.historyPage,
                    totalPages = totalPages,
                    onPageClick = { viewModel.setHistoryPage(it) }
                )
            }
        }
    }
}

@Composable
private fun ExerciseHistoryRow(result: ActivityResult) {
    val dateTimeParts = result.createdAt.split("T")
    val date = dateTimeParts.getOrNull(0) ?: "---"
    val time = dateTimeParts.getOrNull(1)?.take(5) ?: "--:--"

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            val category = when {
                result.activityType.startsWith("orientation") -> "Orientación"
                result.activityType.startsWith("attention") || result.activityType == "number_search" -> "Atención"
                result.activityType.startsWith("memory") -> "Memoria"
                result.activityType.startsWith("language") -> "Lenguaje"
                result.activityType.startsWith("executive") || result.activityType.startsWith("calculation") -> "FF.EE."
                result.activityType.startsWith("perception") -> "Percepción"
                result.activityType.startsWith("literacy") -> "Lectoescritura"
                else -> "Otros"
            }

            Box(
                modifier = Modifier.size(40.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = when(category) {
                        "Orientación" -> Icons.Default.Explore
                        "Atención" -> Icons.Default.Visibility
                        "Memoria" -> Icons.Default.Psychology
                        "Lenguaje" -> Icons.Default.Translate
                        "FF.EE." -> Icons.Default.Calculate
                        "Percepción" -> Icons.Default.RemoveRedEye
                        "Lectoescritura" -> Icons.Default.EditNote
                        else -> Icons.Default.Extension
                    },
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(text = ExerciseTranslationUtils.getDisplayName(result.activityType), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "${DateUtils.toUserFormat(date)} • $time", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f), shape = RoundedCornerShape(4.dp)) {
                        Text(text = category, modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp), style = MaterialTheme.typography.labelSmall, fontSize = 9.sp)
                    }
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(text = "${result.score}%", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black, color = if(result.score > 70) Color(0xFF2E7D32) else if(result.score > 40) Color(0xFFF57C00) else Color.Red)
                Text(text = "Nivel ${result.difficultyLevel.takeLast(1)}", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
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
        colors = CardDefaults.cardColors(containerColor = if (expanded) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f) else MaterialTheme.colorScheme.surface),
        border = if (!expanded) BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant) else null
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth().clickable { expanded = !expanded }.padding(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(12.dp))
                Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                Icon(imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore, contentDescription = null, tint = Color.Gray)
            }
            AnimatedVisibility(visible = expanded, enter = expandVertically() + fadeIn(), exit = shrinkVertically() + fadeOut()) {
                Column(modifier = Modifier.padding(start = 20.dp, end = 20.dp, bottom = 20.dp)) { content() }
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
private fun AssessmentField(label: String, placeholder: String, value: String, onValueChange: (String) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(label, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text(placeholder, fontSize = 14.sp) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            minLines = 3,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
        )
    }
}
