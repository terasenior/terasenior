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
import com.terapia.terasenior.ui.therapy.ExerciseTranslationUtils

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
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp),
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
    Column(
        modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp, vertical = 16.dp).verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
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
    val scrollState = rememberScrollState()

    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp, vertical = 16.dp)) {
        Text("Ejercicios de $category", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(12.dp))

        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(end = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val exercises = when (category) {
                    "Orientación" -> listOf(
                        Triple("orientation_temporal", "Orientación Temporal", "Preguntas sobre el día, mes, año y estación.")
                    )
                    "Atención" -> listOf(
                        Triple("number_search", "Busca el Número", "Entrenamiento de atención focalizada."),
                        Triple("attention_different", "Rodear el diferente", "Buscar el elemento intruso."),
                        Triple("attention_equals_model", "Rodear los iguales al modelo", "Búsqueda visual selectiva."),
                        Triple("attention_positions", "Rodear posiciones iguales", "Orientación espacial."),
                        Triple("attention_differences", "Buscar diferencias", "Atención al detalle."),
                        Triple("attention_letters", "Rodear las letras iguales", "Búsqueda de grafemas."),
                        Triple("attention_symbols", "Rodear símbolos iguales", "Atención visual simbólica."),
                        Triple("attention_matrices", "Matrices (Animales/Símbolos)", "Atención en cuadrícula."),
                        Triple("attention_row_cancel", "Tachado por filas con recuento", "Cancelación y conteo."),
                        Triple("attention_consecutive", "Rodear números consecutivos", "Atención sostenida."),
                        Triple("attention_yes_no", "Tachar una sí y otra no", "Alternancia atencional."),
                        Triple("attention_dual_task", "Tarea Dual (Doble instrucción)", "Atención dividida."),
                        Triple("attention_count", "Contar dibujos", "Conteo visual."),
                        Triple("attention_longest", "Palabra/Cifra más larga", "Discriminación visual."),
                        Triple("attention_missing_part", "Parte del dibujo que falta", "Integración visual."),
                        Triple("attention_word_search", "Sopa de letras/números", "Búsqueda sistemática.")
                    ).sortedBy { it.second }
                    "Memoria" -> listOf(
                        Triple("memory_pairs", "Parejas de Memoria", "Encuentra las parejas de cartas iguales."),
                        Triple("memory_cultural", "Cultura General", "Preguntas sobre geografía e historia."),
                        Triple("memory_utility", "Utilidad de Objetos", "Relacionar objetos con su función."),
                        Triple("memory_needs", "Necesidades para Tareas", "Identificar qué se necesita para una tarea."),
                        Triple("memory_recent", "Memoria Reciente", "Preguntas sobre eventos cercanos.")
                    ).sortedBy { it.second }
                    "Lenguaje" -> listOf(
                        Triple("language_word_image", "Vocabulario: Palabra-Imagen", "Identifica la imagen que corresponde a la palabra."),
                        Triple("language_naming_objects", "Denominación de Objetos", "Elige el nombre correcto para la imagen mostrada."),
                        Triple("language_semantic_category", "Clasificación Semántica", "Agrupa los objetos según su familia o categoría.")
                    ).sortedBy { it.second }
                    "Cálculo" -> listOf(
                        Triple("calculation_simple", "Cálculos Sencillos", "Resuelve operaciones aritméticas básicas.")
                    )
                    "Funciones Ejecutivas" -> listOf(
                        Triple("executive_color_shape_sequence", "Secuencias Lógicas", "Completar series de colores y formas.")
                    )
                    "Percepción" -> listOf(
                        Triple("perception_color_identification", "Identificación de Colores", "Toca el color que se indica por nombre."),
                        Triple("perception_size_ordering", "Orden de Tamaños", "Ordena los objetos de menor a mayor tamaño."),
                        Triple("perception_lateral_dominance", "Dominancia Lateral (Izq/Der)", "Identificar izquierda y derecha."),
                        Triple("perception_mirror", "Imagen en Espejo", "Reconocer formas y letras reflejadas."),
                        Triple("perception_body_parts", "Partes del Cuerpo", "Identificar y nombrar partes del cuerpo."),
                        Triple("perception_shape_fitting", "Encaje de Formas", "Arrastra cada pieza hasta su silueta correspondiente.")
                    ).sortedBy { it.second }
                    "Lectoescritura" -> listOf(
                        Triple("literacy_tracing", "Trazos Básicos", "Sigue las líneas punteadas con precisión.")
                    )
                    else -> emptyList()
                }

                if (exercises.isEmpty()) {
                    Text("No hay ejercicios disponibles para esta categoría.", color = Color.Gray)
                } else {
                    exercises.forEach { (type, name, desc) ->
                        ExerciseItem(
                            title = name,
                            isSelected = selectedExercises.any { it.type == type },
                            onToggle = { onToggle(type, name, category, desc) }
                        )
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = onNext, modifier = Modifier.fillMaxWidth().height(56.dp), shape = RoundedCornerShape(16.dp), enabled = selectedExercises.isNotEmpty()) {
            Text("Siguiente")
        }
    }
}

@Composable
private fun ExerciseItem(title: String, isSelected: Boolean, onToggle: () -> Unit, enabled: Boolean = true) {
    Surface(
        onClick = if(enabled) onToggle else ({}),
        shape = RoundedCornerShape(12.dp),
        color = if(isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
        border = if(!isSelected) CardDefaults.outlinedCardBorder() else null,
        enabled = enabled,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Checkbox(checked = isSelected, onCheckedChange = { onToggle() }, enabled = enabled)
            Spacer(modifier = Modifier.width(12.dp))
            Text(title, style = MaterialTheme.typography.bodyMedium, color = if(enabled) Color.Unspecified else Color.Gray)
        }
    }
}

@Composable
private fun LevelSelectionStep(onLevelSelected: (Int) -> Unit) {
    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Nivel de Dificultad (GDS)", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        (1..5).forEach { level ->
            Card(onClick = { onLevelSelected(level) }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(ExerciseTranslationUtils.getGdsLabel(level), fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                    Icon(Icons.Default.ChevronRight, contentDescription = null)
                }
            }
        }
    }
}

@Composable
private fun SessionSummaryStep(uiState: CreateSessionUiState, onCreateSession: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
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
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(modifier = Modifier.size(40.dp), shape = CircleShape, color = MaterialTheme.colorScheme.primaryContainer) {
                Box(contentAlignment = Alignment.Center) { Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp)) }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                Text(description, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            }
        }
    }
}
