package com.terapia.terasenior.ui.therapy

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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.terapia.terasenior.domain.model.patient.Patient
import com.terapia.terasenior.domain.model.therapy.ExerciseConfig
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
    Column(modifier = Modifier.fillMaxSize().padding(24.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("Área Cognitiva", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        SelectionCard(title = "Orientación", description = "Tiempo, espacio y persona.", icon = Icons.Default.Event, onClick = { onCategorySelected("Orientación") })
        SelectionCard(title = "Atención", description = "Focalización y mantenimiento visual.", icon = Icons.Default.Visibility, onClick = { onCategorySelected("Atención") })
        SelectionCard(title = "Memoria", description = "Codificación y recuperación.", icon = Icons.Default.Psychology, onClick = { onCategorySelected("Memoria") })
        SelectionCard(title = "Lenguaje", description = "Fluidez y comprensión.", icon = Icons.Default.RecordVoiceOver, onClick = { onCategorySelected("Lenguaje") })
        SelectionCard(title = "Cálculo", description = "Operaciones y razonamiento.", icon = Icons.Default.Calculate, onClick = { onCategorySelected("Cálculo") })
        SelectionCard(title = "Funciones Ejecutivas", description = "Planificación y secuencias.", icon = Icons.Default.Settings, onClick = { onCategorySelected("Funciones Ejecutivas") })
        SelectionCard(title = "Percepción", description = "Gnosias y capacidades visoespaciales.", icon = Icons.Default.Extension, onClick = { onCategorySelected("Percepción") })
        SelectionCard(title = "Lectoescritura", description = "Grafomotricidad y trazos.", icon = Icons.Default.Edit, onClick = { onCategorySelected("Lectoescritura") })
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

        Column(modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            when(category) {
                "Orientación" -> {
                    ExerciseItem(
                        title = "Orientación Temporal",
                        isSelected = selectedExercises.any { config -> config.type == "orientation_temporal" },
                        onToggle = { onToggle("orientation_temporal", "Orientación", "Orientación", "Preguntas sobre el día, mes, año y estación.") }
                    )
                }
                "Atención" -> {
                    val attentionGames = listOf(
                        "number_search" to "Busca el Número",
                        "attention_different" to "Rodear el diferente",
                        "attention_equals_model" to "Rodear los iguales al modelo",
                        "attention_positions" to "Rodear posiciones iguales",
                        "attention_differences" to "Buscar diferencias",
                        "attention_letters" to "Rodear las letras iguales",
                        "attention_symbols" to "Rodear símbolos iguales",
                        "attention_matrices" to "Matrices (Animales/Símbolos)",
                        "attention_row_cancel" to "Tachado por filas con recuento",
                        "attention_consecutive" to "Rodear números consecutivos",
                        "attention_yes_no" to "Tachar una sí y otra no",
                        "attention_dual_task" to "Tarea Dual (Doble instrucción)",
                        "attention_count" to "Contar dibujos",
                        "attention_longest" to "Palabra/Cifra más larga",
                        "attention_missing_part" to "Parte del dibujo que falta",
                        "attention_word_search" to "Sopa de letras/números"
                    )
                    
                    attentionGames.forEach { (type, name) ->
                        ExerciseItem(
                            title = name,
                            isSelected = selectedExercises.any { config -> config.type == type },
                            onToggle = { onToggle(type, name, "Atención", "Entrenamiento de atención focalizada y selectiva.") }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
                "Memoria" -> {
                    ExerciseItem(
                        title = "Parejas de Memoria",
                        isSelected = selectedExercises.any { config -> config.type == "memory_pairs" },
                        onToggle = { onToggle("memory_pairs", "Parejas", "Memoria", "Encuentra las parejas de cartas iguales.") }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    ExerciseItem(
                        title = "Cultura General",
                        isSelected = selectedExercises.any { config -> config.type == "memory_cultural" },
                        onToggle = { onToggle("memory_cultural", "Cultura", "Memoria", "Preguntas sobre geografía e historia.") }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    ExerciseItem(
                        title = "Utilidad de Objetos",
                        isSelected = selectedExercises.any { config -> config.type == "memory_utility" },
                        onToggle = { onToggle("memory_utility", "Utilidad", "Memoria", "Relacionar objetos con su función.") }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    ExerciseItem(
                        title = "Necesidades para Tareas",
                        isSelected = selectedExercises.any { config -> config.type == "memory_needs" },
                        onToggle = { onToggle("memory_needs", "Necesidades", "Memoria", "Identificar qué se necesita para una tarea.") }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    ExerciseItem(
                        title = "Memoria Reciente",
                        isSelected = selectedExercises.any { config -> config.type == "memory_recent" },
                        onToggle = { onToggle("memory_recent", "Reciente", "Memoria", "Preguntas sobre eventos cercanos.") }
                    )
                }
                "Lenguaje" -> {
                    ExerciseItem(
                        title = "Vocabulario: Palabra-Imagen",
                        isSelected = selectedExercises.any { config -> config.type == "language_word_image" },
                        onToggle = { onToggle("language_word_image", "Vocabulario", "Lenguaje", "Identifica la imagen que corresponde a la palabra.") }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    ExerciseItem(
                        title = "Denominación de Objetos",
                        isSelected = selectedExercises.any { config -> config.type == "language_naming_objects" },
                        onToggle = { onToggle("language_naming_objects", "Denominación", "Lenguaje", "Elige el nombre correcto para la imagen mostrada.") }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    ExerciseItem(
                        title = "Clasificación Semántica",
                        isSelected = selectedExercises.any { config -> config.type == "language_semantic_category" },
                        onToggle = { onToggle("language_semantic_category", "Clasificación Semántica", "Lenguaje", "Agrupa los objetos según su familia o categoría.") }
                    )
                }
                "Cálculo" -> {
                    ExerciseItem(
                        title = "Cálculos Sencillos",
                        isSelected = selectedExercises.any { config -> config.type == "calculation_simple" },
                        onToggle = { onToggle("calculation_simple", "Cálculo Mental", "Cálculo", "Resuelve operaciones aritméticas básicas.") }
                    )
                }
                "Funciones Ejecutivas" -> {
                    ExerciseItem(
                        title = "Secuencias Lógicas",
                        isSelected = selectedExercises.any { config -> config.type == "executive_color_shape_sequence" },
                        onToggle = { onToggle("executive_color_shape_sequence", "Secuencias", "Funciones Ejecutivas", "Completar series de colores y formas.") }
                    )
                }
                "Percepción" -> {
                    ExerciseItem(
                        title = "Identificación de Colores",
                        isSelected = selectedExercises.any { config -> config.type == "perception_color_identification" },
                        onToggle = { onToggle("perception_color_identification", "Colores", "Percepción", "Toca el color que se indica por nombre.") }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    ExerciseItem(
                        title = "Orden de Tamaños",
                        isSelected = selectedExercises.any { config -> config.type == "perception_size_ordering" },
                        onToggle = { onToggle("perception_size_ordering", "Tamaños", "Percepción", "Ordena los objetos de menor a mayor tamaño.") }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    ExerciseItem(
                        title = "Dominancia Lateral (Izq/Der)",
                        isSelected = selectedExercises.any { config -> config.type == "perception_lateral_dominance" },
                        onToggle = { onToggle("perception_lateral_dominance", "Lateralidad", "Percepción", "Identificar izquierda y derecha.") }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    ExerciseItem(
                        title = "Imagen en Espejo",
                        isSelected = selectedExercises.any { config -> config.type == "perception_mirror" },
                        onToggle = { onToggle("perception_mirror", "Espejo", "Percepción", "Reconocer formas y letras reflejadas.") }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    ExerciseItem(
                        title = "Partes del Cuerpo",
                        isSelected = selectedExercises.any { config -> config.type == "perception_body_parts" },
                        onToggle = { onToggle("perception_body_parts", "Cuerpo Humano", "Percepción", "Identificar y nombrar partes del cuerpo.") }
                    )
                }
                "Lectoescritura" -> {
                    ExerciseItem(
                        title = "Trazos Básicos",
                        isSelected = selectedExercises.any { config -> config.type == "literacy_tracing" },
                        onToggle = { onToggle("literacy_tracing", "Trazos", "Lectoescritura", "Sigue las líneas punteadas con precisión.") }
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
