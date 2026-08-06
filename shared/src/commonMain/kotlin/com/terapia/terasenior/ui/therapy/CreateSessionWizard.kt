package com.terapia.terasenior.ui.therapy

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.terapia.terasenior.domain.model.patient.Patient
import com.terapia.terasenior.domain.model.therapy.SessionMode

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateSessionWizard(
    viewModel: CreateSessionViewModel,
    therapistId: String,
    onSessionCreated: (String) -> Unit,
    onCancel: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.createdSessionId) {
        uiState.createdSessionId?.let { onSessionCreated(it) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Nueva Sesión") },
                navigationIcon = {
                    IconButton(onClick = {
                        if (uiState.currentStep == WizardStep.MODE_SELECTION) onCancel()
                        else viewModel.goBack()
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when (uiState.currentStep) {
                WizardStep.MODE_SELECTION -> ModeSelectionStep(onModeSelected = viewModel::onModeSelected)
                WizardStep.PATIENT_SELECTION -> PatientSelectionStep(
                    patients = uiState.patients,
                    isLoading = uiState.isLoading,
                    onPatientSelected = viewModel::onPatientSelected
                )
                WizardStep.CATEGORY_SELECTION -> CategorySelectionStep(onCategorySelected = viewModel::onCategorySelected)
                WizardStep.EXERCISE_SELECTION -> ExerciseSelectionStep(
                    category = uiState.selectedCategory ?: "",
                    selectedExercises = uiState.selectedExercises,
                    onToggle = viewModel::toggleExercise,
                    onNext = viewModel::goNextFromExercises
                )
                WizardStep.LEVEL_SELECTION -> LevelSelectionStep(onLevelSelected = viewModel::onLevelSelected)
                WizardStep.SUMMARY -> SessionSummaryStep(
                    uiState = uiState,
                    onCreateSession = { viewModel.createSession(therapistId) }
                )
            }
        }
    }
}

@Composable
private fun ModeSelectionStep(onModeSelected: (SessionMode) -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Modalidad de trabajo", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        SelectionCard(
            title = "Con Paciente",
            description = "Registro clínico y seguimiento.",
            icon = Icons.Default.Person,
            onClick = { onModeSelected(SessionMode.WITH_PATIENT) }
        )
        SelectionCard(
            title = "Sin Paciente",
            description = "Uso libre o demostración.",
            icon = Icons.Default.PersonOff,
            onClick = { onModeSelected(SessionMode.WITHOUT_PATIENT) }
        )
    }
}

@Composable
private fun PatientSelectionStep(patients: List<Patient>, isLoading: Boolean, onPatientSelected: (Patient) -> Unit) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Selecciona el paciente", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(patients) { patient ->
                    Card(onClick = { onPatientSelected(patient) }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
                        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(40.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primaryContainer), contentAlignment = Alignment.Center) {
                                Text(patient.firstName.take(1))
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(patient.fullName, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CategorySelectionStep(onCategorySelected: (String) -> Unit) {
    Column(modifier = Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("Área Cognitiva", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        SelectionCard(title = "Atención", description = "Focalización y mantenimiento visual.", icon = Icons.Default.Visibility, onClick = { onCategorySelected("Atención") })
        SelectionCard(title = "Memoria", description = "Codificación y recuperación.", icon = Icons.Default.Psychology, onClick = { onCategorySelected("Memoria") })
        SelectionCard(title = "Lenguaje", description = "Fluidez y comprensión.", icon = Icons.Default.RecordVoiceOver, onClick = { onCategorySelected("Lenguaje") })
        SelectionCard(title = "Cálculo", description = "Operaciones y razonamiento.", icon = Icons.Default.Calculate, onClick = { onCategorySelected("Cálculo") })
    }
}

@Composable
private fun ExerciseSelectionStep(
    category: String,
    selectedExercises: List<ExerciseConfig>,
    onToggle: (type: String, name: String, category: String, desc: String) -> Unit,
    onNext: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
        Text("Ejercicios de $category", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))

        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            when(category) {
                "Atención" -> {
                    ExerciseItem(
                        title = "Busca el Número",
                        isSelected = selectedExercises.any { it.type == "number_search" },
                        onToggle = { onToggle("number_search", "Busca el Número", "Atención", "Busca el número indicado en la cuadrícula.") }
                    )
                }
                "Memoria" -> {
                    ExerciseItem(
                        title = "Parejas (Próximamente)",
                        isSelected = false,
                        onToggle = { },
                        enabled = false
                    )
                }
                else -> {
                    Text("No hay ejercicios disponibles para esta categoría.", color = Color.Gray)
                }
            }
        }
        
        Button(onClick = onNext, modifier = Modifier.fillMaxWidth().height(56.dp), shape = RoundedCornerShape(16.dp), enabled = selectedExercises.isNotEmpty()) {
            Text("Siguiente")
        }
    }
}

@Composable
private fun ExerciseItem(title: String, isSelected: Boolean, onToggle: () -> Unit, enabled: Boolean = true) {
    Surface(
        onClick = if(enabled) onToggle else ({}),
        shape = RoundedCornerShape(16.dp),
        color = if(isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
        border = if(!isSelected) CardDefaults.outlinedCardBorder() else null,
        enabled = enabled,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Checkbox(checked = isSelected, onCheckedChange = { onToggle() }, enabled = enabled)
            Spacer(modifier = Modifier.width(12.dp))
            Text(title, color = if(enabled) Color.Unspecified else Color.Gray)
        }
    }
}

@Composable
private fun LevelSelectionStep(onLevelSelected: (Int) -> Unit) {
    Column(modifier = Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Nivel de Dificultad", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        (1..5).forEach { level ->
            Card(onClick = { onLevelSelected(level) }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text("Nivel $level", fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                    Icon(Icons.Default.ChevronRight, contentDescription = null)
                }
            }
        }
    }
}

@Composable
private fun SessionSummaryStep(uiState: CreateSessionUiState, onCreateSession: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("Confirmar Sesión", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))) {
            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                SummaryRow("Paciente", uiState.selectedPatient?.fullName ?: "Sesión Libre")
                SummaryRow("Ejercicios", "${uiState.selectedExercises.size} seleccionados")
            }
        }
        Spacer(modifier = Modifier.weight(1f))
        Button(onClick = onCreateSession, modifier = Modifier.fillMaxWidth().height(56.dp), shape = RoundedCornerShape(16.dp), enabled = !uiState.isLoading) {
            if (uiState.isLoading) CircularProgressIndicator(color = Color.White) else Text("Iniciar Sesión", fontSize = 18.sp)
        }
    }
}

@Composable
private fun SummaryRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
        Text(value, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun SelectionCard(title: String, description: String, icon: ImageVector, onClick: () -> Unit) {
    Card(onClick = onClick, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
        Row(modifier = Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(modifier = Modifier.size(48.dp), shape = CircleShape, color = MaterialTheme.colorScheme.primaryContainer) {
                Box(contentAlignment = Alignment.Center) { Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary) }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                Text(description, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            }
        }
    }
}
