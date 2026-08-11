package com.terapia.terasenior.ui.therapy

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.terapia.terasenior.data.repository.results.SupabaseResultsRepository
import com.terapia.terasenior.domain.model.therapy.TherapySession
import com.terapia.terasenior.domain.model.therapy.TherapySessionExercise
import com.terapia.terasenior.domain.usecase.results.SaveActivityResultUseCase
import com.terapia.terasenior.treatment.ui.*
import com.terapia.terasenior.ui.components.accessibility.SpeechManager

@Composable
fun SessionRunnerScreen(
    viewModel: SessionRunnerViewModel,
    onFinished: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val speechManager = remember { SpeechManager() }

    Box(modifier = Modifier.fillMaxSize()) {
        when (val state = uiState) {
            is SessionRunnerUiState.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            is SessionRunnerUiState.Transition -> {
                TransitionView(
                    state = state,
                    onStart = { viewModel.startExercise(state.nextIndex) },
                    speechManager = speechManager
                )
            }
            is SessionRunnerUiState.Playing -> {
                val currentExercise = state.exercises[state.currentIndex]
                
                ExerciseRouter(
                    exercise = currentExercise,
                    patientId = state.session.patientId,
                    professionalId = state.session.therapistId,
                    appointmentId = state.session.appointmentId,
                    onExerciseCompleted = { viewModel.nextExercise() },
                    onAbort = { viewModel.abortSession() }
                )

                ProfessionalHUD(onShowPanel = { viewModel.toggleProfessionalPanel() })

                if (state.showProfessionalPanel) {
                    ProfessionalControlPanel(
                        onLogAssistance = { viewModel.logAssistance(it, null) },
                        onLogIncident = { viewModel.logIncident(it, null) },
                        onDismiss = { viewModel.toggleProfessionalPanel() }
                    )
                }
            }
            is SessionRunnerUiState.Summary -> {
                ClinicalValuationView(
                    session = state.session,
                    onSave = { p, f, n -> viewModel.finishSession(p, f, n) }
                )
            }
            is SessionRunnerUiState.Finished -> {
                onFinished()
            }
            is SessionRunnerUiState.Error -> {
                Box(modifier = Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
                    Text(state.message, color = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

@Composable
private fun ClinicalValuationView(
    session: TherapySession,
    onSave: (participation: String, fatigue: String, notes: String) -> Unit
) {
    var participation by remember { mutableStateOf("MEDIUM") }
    var fatigue by remember { mutableStateOf("NONE") }
    var notes by remember { mutableStateOf("") }

    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp).verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Cierre de Sesión Terapéutica", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black)
        Spacer(modifier = Modifier.height(8.dp))
        Text("Valoración profesional de la intervención.", color = Color.Gray)
        
        Spacer(modifier = Modifier.height(32.dp))

        Card(modifier = Modifier.fillMaxWidth().widthIn(max = 600.dp), shape = RoundedCornerShape(24.dp)) {
            Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(24.dp)) {
                
                Column {
                    Text("Nivel de Participación", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 8.dp)) {
                        FilterChip(selected = participation == "LOW", onClick = { participation = "LOW" }, label = { Text("Baja") })
                        FilterChip(selected = participation == "MEDIUM", onClick = { participation = "MEDIUM" }, label = { Text("Media") })
                        FilterChip(selected = participation == "HIGH", onClick = { participation = "HIGH" }, label = { Text("Alta") })
                    }
                }

                Column {
                    Text("Nivel de Fatiga", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 8.dp)) {
                        FilterChip(selected = fatigue == "NONE", onClick = { fatigue = "NONE" }, label = { Text("Ninguna") })
                        FilterChip(selected = fatigue == "MODERATE", onClick = { fatigue = "MODERATE" }, label = { Text("Moderada") })
                        FilterChip(selected = fatigue == "HIGH", onClick = { fatigue = "HIGH" }, label = { Text("Alta") })
                    }
                }

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Observaciones Clínicas") },
                    placeholder = { Text("Ej: Ha necesitado apoyo verbal constante en el cálculo...") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 4,
                    shape = RoundedCornerShape(16.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(48.dp))

        Button(
            onClick = { onSave(participation, fatigue, notes) },
            modifier = Modifier.height(64.dp).width(280.dp),
            shape = RoundedCornerShape(20.dp)
        ) {
            Icon(Icons.Default.Save, contentDescription = null)
            Spacer(modifier = Modifier.width(12.dp))
            Text("Finalizar y Guardar", fontSize = 18.sp)
        }
    }
}

@Composable
private fun ExerciseRouter(
    exercise: TherapySessionExercise,
    patientId: String?,
    professionalId: String,
    appointmentId: String?,
    onExerciseCompleted: () -> Unit,
    onAbort: () -> Unit
) {
    val resultsRepo = remember { SupabaseResultsRepository() }
    val saveUseCase = remember { SaveActivityResultUseCase(resultsRepo) }

    when (exercise.exerciseType) {
        "orientation_temporal" -> {
            val gameViewModel = remember { OrientationViewModel(saveUseCase) }
            LaunchedEffect(exercise.id) { gameViewModel.startNewGame(exercise.level) }
            val gameState by gameViewModel.uiState.collectAsState()
            LaunchedEffect(gameState.isCompleted) { if (gameState.isCompleted) onExerciseCompleted() }
            OrientationGame(viewModel = gameViewModel, patientId = patientId, professionalId = professionalId, appointmentId = appointmentId, onBack = onAbort)
        }
        "number_search" -> {
            val gameViewModel = remember { NumberSearchViewModel(saveUseCase) }
            LaunchedEffect(exercise.id) { gameViewModel.startNewGame(exercise.level) }
            val gameState by gameViewModel.uiState.collectAsState()
            LaunchedEffect(gameState.isCompleted) { if (gameState.isCompleted) onExerciseCompleted() }
            NumberSearchGame(viewModel = gameViewModel, patientId = patientId, professionalId = professionalId, appointmentId = appointmentId, onBack = onAbort)
        }
        "attention_spot_odd_one_out", "attention_different", "attention_equals_model", 
        "attention_positions", "attention_letters", "attention_numbers", 
        "attention_symbols", "attention_matrices", "attention_row_cancel", 
        "attention_consecutive", "attention_yes_no", "attention_dual_task", 
        "attention_count", "attention_word_search", "attention_differences", "attention_longest" -> {
            val gameViewModel = remember { VisualAttentionViewModel(saveUseCase) }
            LaunchedEffect(exercise.id) { 
                val variation = if (exercise.exerciseType == "attention_spot_odd_one_out") "attention_different" else exercise.exerciseType
                gameViewModel.startNewGame(variation, exercise.level) 
            }
            val gameState by gameViewModel.uiState.collectAsState()
            LaunchedEffect(gameState.isCompleted) { if (gameState.isCompleted) onExerciseCompleted() }
            VisualAttentionGame(viewModel = gameViewModel, patientId = patientId, professionalId = professionalId, appointmentId = appointmentId, onBack = onAbort)
        }
        "memory_pairs" -> {
            val gameViewModel = remember { PairsViewModel(saveUseCase) }
            LaunchedEffect(exercise.id) { gameViewModel.startNewGame(exercise.level) }
            val gameState by gameViewModel.uiState.collectAsState()
            LaunchedEffect(gameState.isCompleted) { if (gameState.isCompleted) onExerciseCompleted() }
            PairsGame(viewModel = gameViewModel, patientId = patientId, professionalId = professionalId, appointmentId = appointmentId, onBack = onAbort)
        }
        "memory_cultural", "memory_utility", "memory_needs", "memory_recent" -> {
            val gameViewModel = remember { MemoryViewModel(saveUseCase) }
            LaunchedEffect(exercise.id) {
                val type = when(exercise.exerciseType) {
                    "memory_cultural" -> MemoryType.CULTURAL
                    "memory_utility" -> MemoryType.UTILITY
                    "memory_needs" -> MemoryType.NEEDS
                    "memory_recent" -> MemoryType.RECENT
                    else -> MemoryType.CULTURAL
                }
                gameViewModel.startNewGame(type, exercise.level)
            }
            val gameState by gameViewModel.uiState.collectAsState()
            LaunchedEffect(gameState.isCompleted) { if (gameState.isCompleted) onExerciseCompleted() }
            MemoryGame(viewModel = gameViewModel, patientId = patientId, professionalId = professionalId, appointmentId = appointmentId, onBack = onAbort)
        }
        "language_word_image" -> {
            val gameViewModel = remember { WordImageViewModel(saveUseCase) }
            LaunchedEffect(exercise.id) { gameViewModel.startNewGame(exercise.level) }
            val gameState by gameViewModel.uiState.collectAsState()
            LaunchedEffect(gameState.isCompleted) { if (gameState.isCompleted) onExerciseCompleted() }
            WordImageGame(viewModel = gameViewModel, patientId = patientId, professionalId = professionalId, appointmentId = appointmentId, onBack = onAbort)
        }
        "language_denomination", "language_naming_objects" -> {
            val gameViewModel = remember { NamingObjectsViewModel(saveUseCase) }
            LaunchedEffect(exercise.id) { gameViewModel.startNewGame(exercise.level) }
            val gameState by gameViewModel.uiState.collectAsState()
            LaunchedEffect(gameState.isCompleted) { if (gameState.isCompleted) onExerciseCompleted() }
            NamingObjectsGame(viewModel = gameViewModel, patientId = patientId, professionalId = professionalId, appointmentId = appointmentId, onBack = onAbort)
        }
        "language_start_letter", "language_start_syllable", 
        "language_end_letter", "language_end_syllable", "language_complex_cluster", 
        "language_semantic_completion", "language_semantic_naming" -> {
            val gameViewModel = remember { LanguageViewModel(saveUseCase) }
            LaunchedEffect(exercise.id) { 
                gameViewModel.startNewGame(exercise.exerciseType, exercise.level, exercise.configuration) 
            }
            val gameState by gameViewModel.uiState.collectAsState()
            LaunchedEffect(gameState.isCompleted) { if (gameState.isCompleted) onExerciseCompleted() }
            LanguageGame(viewModel = gameViewModel, patientId = patientId, professionalId = professionalId, appointmentId = appointmentId, onBack = onAbort)
        }
        "language_semantic_category" -> {
            val gameViewModel = remember { SemanticCategoryViewModel(saveUseCase) }
            LaunchedEffect(exercise.id) { gameViewModel.startNewGame(exercise.level) }
            val gameState by gameViewModel.uiState.collectAsState()
            LaunchedEffect(gameState.isCompleted) { if (gameState.isCompleted) onExerciseCompleted() }
            SemanticCategoryGame(viewModel = gameViewModel, patientId = patientId, professionalId = professionalId, appointmentId = appointmentId, onBack = onAbort)
        }
        "calculation_simple" -> {
            val gameViewModel = remember { CalculationViewModel(saveUseCase) }
            LaunchedEffect(exercise.id) { gameViewModel.startNewGame(exercise.level) }
            val gameState by gameViewModel.uiState.collectAsState()
            LaunchedEffect(gameState.isCompleted) { if (gameState.isCompleted) onExerciseCompleted() }
            CalculationGame(viewModel = gameViewModel, patientId = patientId, professionalId = professionalId, appointmentId = appointmentId, onBack = onAbort)
        }
        "executive_color_shape_sequence" -> {
            val gameViewModel = remember { ColorShapeSequenceViewModel(saveUseCase) }
            LaunchedEffect(exercise.id) { gameViewModel.startNewGame(exercise.level) }
            val gameState by gameViewModel.uiState.collectAsState()
            LaunchedEffect(gameState.isCompleted) { if (gameState.isCompleted) onExerciseCompleted() }
            ColorShapeSequenceGame(viewModel = gameViewModel, patientId = patientId, professionalId = professionalId, appointmentId = appointmentId, onBack = onAbort)
        }
        "executive_planning_steps", "executive_shopping_list", "executive_money_calculation", 
        "executive_time_logic", "executive_logical_reasoning", "executive_analogies", 
        "executive_abstractions", "executive_intrusos", "executive_math_advanced" -> {
            val gameViewModel = remember { ExecutiveFunctionsViewModel(saveUseCase) }
            LaunchedEffect(exercise.id) { gameViewModel.startNewGame(exercise.exerciseType, exercise.level) }
            val gameState by gameViewModel.uiState.collectAsState()
            LaunchedEffect(gameState.isCompleted) { if (gameState.isCompleted) onExerciseCompleted() }
            ExecutiveFunctionsGame(viewModel = gameViewModel, patientId = patientId, professionalId = professionalId, appointmentId = appointmentId, onBack = onAbort)
        }
        "perception_color_identification" -> {
            val gameViewModel = remember { ColorIdentificationViewModel(saveUseCase) }
            LaunchedEffect(exercise.id) { gameViewModel.startNewGame(exercise.level) }
            val gameState by gameViewModel.uiState.collectAsState()
            LaunchedEffect(gameState.isCompleted) { if (gameState.isCompleted) onExerciseCompleted() }
            ColorIdentificationGame(viewModel = gameViewModel, patientId = patientId, professionalId = professionalId, appointmentId = appointmentId, onBack = onAbort)
        }
        "perception_size_ordering" -> {
            val gameViewModel = remember { SizeOrderingViewModel(saveUseCase) }
            LaunchedEffect(exercise.id) { gameViewModel.startNewGame(exercise.level) }
            val gameState by gameViewModel.uiState.collectAsState()
            LaunchedEffect(gameState.isCompleted) { if (gameState.isCompleted) onExerciseCompleted() }
            SizeOrderingGame(viewModel = gameViewModel, patientId = patientId, professionalId = professionalId, appointmentId = appointmentId, onBack = onAbort)
        }
        "perception_lateral_dominance", "perception_mirror", "perception_body_parts" -> {
            val gameViewModel = remember { PerceptionViewModel(saveUseCase) }
            LaunchedEffect(exercise.id) {
                val type = when(exercise.exerciseType) {
                    "perception_lateral_dominance" -> PerceptionType.LATERAL_DOMINANCE
                    "perception_mirror" -> PerceptionType.MIRROR
                    "perception_body_parts" -> PerceptionType.BODY_PARTS
                    else -> PerceptionType.LATERAL_DOMINANCE
                }
                gameViewModel.startNewGame(type, exercise.level)
            }
            val gameState by gameViewModel.uiState.collectAsState()
            LaunchedEffect(gameState.isCompleted) { if (gameState.isCompleted) onExerciseCompleted() }
            PerceptionGame(viewModel = gameViewModel, patientId = patientId, professionalId = professionalId, appointmentId = appointmentId, onBack = onAbort)
        }
        "perception_shape_fitting" -> {
            val gameViewModel = remember { ShapeFittingViewModel(saveUseCase) }
            LaunchedEffect(exercise.id) { gameViewModel.startNewGame(exercise.level) }
            val gameState by gameViewModel.uiState.collectAsState()
            LaunchedEffect(gameState.isCompleted) { if (gameState.isCompleted) onExerciseCompleted() }
            ShapeFittingGame(viewModel = gameViewModel, patientId = patientId, professionalId = professionalId, appointmentId = appointmentId, onBack = onAbort)
        }
        "literacy_tracing" -> {
            val gameViewModel = remember { TracingViewModel(saveUseCase) }
            LaunchedEffect(exercise.id) { gameViewModel.startNewGame(exercise.level) }
            val gameState by gameViewModel.uiState.collectAsState()
            LaunchedEffect(gameState.isCompleted) { if (gameState.isCompleted) onExerciseCompleted() }
            TracingGame(viewModel = gameViewModel, patientId = patientId, professionalId = professionalId, appointmentId = appointmentId, onBack = onAbort)
        }
        "literacy_tracing_basic", "literacy_complete_letters", "literacy_copy_words", "literacy_form_shapes" -> {
            val gameViewModel = remember { LiteracyViewModel(saveUseCase) }
            LaunchedEffect(exercise.id) { 
                val variation = when(exercise.exerciseType) {
                    "literacy_tracing_basic" -> LiteracyVariation.TRACING_BASIC
                    "literacy_complete_letters" -> LiteracyVariation.COMPLETE_LETTERS
                    "literacy_copy_words" -> LiteracyVariation.COPY_WORDS
                    "literacy_form_shapes" -> LiteracyVariation.FORM_SHAPES
                    else -> LiteracyVariation.TRACING_BASIC
                }
                gameViewModel.startNewGame(variation, exercise.level) 
            }
            val gameState by gameViewModel.uiState.collectAsState()
            LaunchedEffect(gameState.isCompleted) { if (gameState.isCompleted) onExerciseCompleted() }
            LiteracyGame(viewModel = gameViewModel, patientId = patientId, professionalId = professionalId, appointmentId = appointmentId, onBack = onAbort)
        }
    }
}

@Composable
private fun TransitionView(
    state: SessionRunnerUiState.Transition,
    onStart: () -> Unit,
    speechManager: SpeechManager
) {
    val message = if (state.isFirst) "¡Hola! Vamos a empezar la sesión." else "¡Muy bien! Vamos al siguiente ejercicio."
    val subMessage = "Siguiente actividad: ${state.nextExerciseName}"
    LaunchedEffect(Unit) { speechManager.speak("$message $subMessage") }
    Column(modifier = Modifier.fillMaxSize().padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Surface(color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f), shape = RoundedCornerShape(12.dp)) {
            Text("v1.3.16", modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), style = MaterialTheme.typography.labelSmall)
        }
        Spacer(modifier = Modifier.height(24.dp))
        Text(message, style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
        Spacer(modifier = Modifier.height(16.dp))
        Text(subMessage, style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.primary, textAlign = TextAlign.Center)
        Spacer(modifier = Modifier.height(48.dp))
        Button(onClick = onStart, modifier = Modifier.height(72.dp).width(240.dp), shape = RoundedCornerShape(24.dp)) {
            Icon(Icons.Default.PlayArrow, contentDescription = null)
            Spacer(modifier = Modifier.width(12.dp))
            Text("Empezar ahora", fontSize = 20.sp)
        }
    }
}

@Composable
private fun ProfessionalHUD(onShowPanel: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.BottomStart) {
        SmallFloatingActionButton(onClick = onShowPanel, containerColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f)) {
            Icon(Icons.Default.Settings, contentDescription = "Panel Terapeuta")
        }
    }
}
