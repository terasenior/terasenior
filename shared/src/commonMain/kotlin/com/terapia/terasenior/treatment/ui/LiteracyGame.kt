package com.terapia.terasenior.treatment.ui

import androidx.compose.animation.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.terapia.terasenior.ui.components.accessibility.SpeechManager
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LiteracyGame(
    viewModel: LiteracyViewModel,
    patientId: String?,
    professionalId: String?,
    appointmentId: String?,
    onBack: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val speechManager = remember { SpeechManager() }

    LaunchedEffect(state.instruction) {
        if (state.instruction.isNotEmpty()) {
            speechManager.speak(state.instruction)
        }
    }

    DisposableEffect(Unit) {
        onDispose { speechManager.stop() }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(
                    onClick = onBack,
                    colors = IconButtonDefaults.iconButtonColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = MaterialTheme.colorScheme.error)
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Lectoescritura",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = state.variation.name.replace("_", " "),
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray
                    )
                }

                IconButton(onClick = { speechManager.speak(state.instruction) }) {
                    Icon(Icons.AutoMirrored.Filled.VolumeUp, contentDescription = "Repetir voz")
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Game Content
            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                when (state.variation) {
                    LiteracyVariation.TRACING_BASIC -> TracingLayout(state, viewModel, patientId, professionalId, appointmentId)
                    LiteracyVariation.FORM_SHAPES -> FormShapesLayout(state, viewModel, patientId, professionalId, appointmentId)
                    LiteracyVariation.COMPLETE_LETTERS -> CompleteLettersLayout(state, viewModel, patientId, professionalId, appointmentId)
                    LiteracyVariation.COPY_WORDS -> CopyWordsLayout(state, viewModel, patientId, professionalId, appointmentId)
                }
            }

            // Feedback
            AnimatedVisibility(visible = state.isCorrect != null) {
                val color = if (state.isCorrect == true) Color(0xFF4CAF50) else MaterialTheme.colorScheme.error
                val text = if (state.isCorrect == true) "¡Muy bien!" else "Inténtalo de nuevo"
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                    colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f))
                ) {
                    Text(
                        text = text,
                        modifier = Modifier.padding(16.dp).fillMaxWidth(),
                        textAlign = TextAlign.Center,
                        color = color,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            if (state.isCompleted) {
                Button(
                    onClick = { viewModel.startNewGame(state.variation, state.currentLevel + 1) },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    enabled = !state.isSaving
                ) {
                    Text("Siguiente Ejercicio", fontSize = 18.sp)
                }
            }
        }
    }
}

@Composable
private fun TracingLayout(
    state: LiteracyUiState,
    viewModel: LiteracyViewModel,
    patientId: String?,
    professionalId: String?,
    appointmentId: String?
) {
    CanvasDrawingArea(state, viewModel, isTracing = true, patientId, professionalId, appointmentId)
}

@Composable
private fun FormShapesLayout(
    state: LiteracyUiState,
    viewModel: LiteracyViewModel,
    patientId: String?,
    professionalId: String?,
    appointmentId: String?
) {
    CanvasDrawingArea(state, viewModel, isTracing = false, patientId, professionalId, appointmentId)
}

@Composable
private fun CanvasDrawingArea(
    state: LiteracyUiState,
    viewModel: LiteracyViewModel,
    isTracing: Boolean,
    patientId: String?,
    professionalId: String?,
    appointmentId: String?
) {
    Card(
        modifier = Modifier.fillMaxSize().padding(8.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            val primaryColor = MaterialTheme.colorScheme.primary
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(state.isCompleted) {
                        if (!state.isCompleted) {
                            detectDragGestures(
                                onDragStart = { offset -> viewModel.onDrawPoint(offset) },
                                onDrag = { change, _ -> viewModel.onDrawPoint(change.position) },
                                onDragEnd = { viewModel.completeCanvasTask(patientId, professionalId, appointmentId) }
                            )
                        }
                    }
            ) {
                val canvasWidth = size.width
                val canvasHeight = size.height
                
                // Define guide path and dots based on level and variation
                val targetPoints = mutableListOf<Offset>()
                if (isTracing) {
                    // Tracing guide
                    val path = Path()
                    when (state.currentLevel % 3) {
                        1 -> { // Horizontal
                            path.moveTo(canvasWidth * 0.1f, canvasHeight * 0.5f)
                            path.lineTo(canvasWidth * 0.9f, canvasHeight * 0.5f)
                            for (i in 0..20) targetPoints.add(Offset(canvasWidth * (0.1f + 0.04f * i), canvasHeight * 0.5f))
                        }
                        2 -> { // Vertical
                            path.moveTo(canvasWidth * 0.5f, canvasHeight * 0.1f)
                            path.lineTo(canvasWidth * 0.5f, canvasHeight * 0.9f)
                            for (i in 0..20) targetPoints.add(Offset(canvasWidth * 0.5f, canvasHeight * (0.1f + 0.04f * i)))
                        }
                        0 -> { // Circle
                            val center = Offset(canvasWidth * 0.5f, canvasHeight * 0.5f)
                            val radius = canvasWidth.coerceAtMost(canvasHeight) * 0.35f
                            for (i in 0..360 step 10) {
                                val angle = i * PI / 180
                                val x = center.x + radius * cos(angle).toFloat()
                                val y = center.y + radius * sin(angle).toFloat()
                                if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
                                targetPoints.add(Offset(x, y))
                            }
                        }
                    }
                    drawPath(
                        path = path,
                        color = Color.LightGray,
                        style = Stroke(width = 8f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(20f, 20f), 0f))
                    )
                } else {
                    // Shape dots
                    when (state.currentLevel % 3) {
                        1 -> { // Triangle
                            targetPoints.add(Offset(canvasWidth * 0.5f, canvasHeight * 0.2f))
                            targetPoints.add(Offset(canvasWidth * 0.2f, canvasHeight * 0.8f))
                            targetPoints.add(Offset(canvasWidth * 0.8f, canvasHeight * 0.8f))
                        }
                        2 -> { // Square
                            targetPoints.add(Offset(canvasWidth * 0.2f, canvasHeight * 0.2f))
                            targetPoints.add(Offset(canvasWidth * 0.8f, canvasHeight * 0.2f))
                            targetPoints.add(Offset(canvasWidth * 0.8f, canvasHeight * 0.8f))
                            targetPoints.add(Offset(canvasWidth * 0.2f, canvasHeight * 0.8f))
                        }
                        0 -> { // Diamond
                            targetPoints.add(Offset(canvasWidth * 0.5f, canvasHeight * 0.1f))
                            targetPoints.add(Offset(canvasWidth * 0.9f, canvasHeight * 0.5f))
                            targetPoints.add(Offset(canvasWidth * 0.5f, canvasHeight * 0.9f))
                            targetPoints.add(Offset(canvasWidth * 0.1f, canvasHeight * 0.5f))
                        }
                    }
                    targetPoints.forEach { drawCircle(color = Color.LightGray, radius = 10f, center = it) }
                }
                
                viewModel.setTargetPoints(targetPoints)

                // User path
                if (state.userPoints.size > 1) {
                    val userPath = Path()
                    userPath.moveTo(state.userPoints.first().x, state.userPoints.first().y)
                    state.userPoints.drop(1).forEach { userPath.lineTo(it.x, it.y) }
                    drawPath(
                        path = userPath,
                        color = if (state.isCorrect == true) Color(0xFF4CAF50) else primaryColor,
                        style = Stroke(width = 6f, cap = StrokeCap.Round)
                    )
                }
            }

            if (!state.isCompleted) {
                IconButton(
                    onClick = { viewModel.clearDrawing() },
                    modifier = Modifier.align(Alignment.TopEnd).padding(16.dp),
                    colors = IconButtonDefaults.iconButtonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Icon(Icons.Default.Delete, contentDescription = "Borrar")
                }
            }
        }
    }
}

@Composable
private fun CompleteLettersLayout(
    state: LiteracyUiState,
    viewModel: LiteracyViewModel,
    patientId: String?,
    professionalId: String?,
    appointmentId: String?
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Card(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            shape = RoundedCornerShape(32.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f))
        ) {
            Text(
                text = state.prompt,
                modifier = Modifier.padding(32.dp).fillMaxWidth(),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.displayLarge.copy(fontWeight = FontWeight.Black, letterSpacing = 8.sp),
                color = MaterialTheme.colorScheme.primary
            )
        }

        Spacer(modifier = Modifier.height(48.dp))

        // Cuadrícula de opciones (v1.3.3 - Responsive)
        Box(modifier = Modifier.fillMaxWidth().heightIn(max = 250.dp)) {
            ResponsiveGrid(items = state.options, columns = 2, spacing = 16.dp) { _, option, size ->
                val isSelected = state.isCompleted && option == state.targetValue
                Button(
                    onClick = { viewModel.onOptionSelected(option, patientId, professionalId, appointmentId) },
                    modifier = Modifier.height(size),
                    shape = RoundedCornerShape(24.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isSelected) Color(0xFF4CAF50) else MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                ) {
                    Text(option, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun CopyWordsLayout(
    state: LiteracyUiState,
    viewModel: LiteracyViewModel,
    patientId: String?,
    professionalId: String?,
    appointmentId: String?
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = state.prompt,
            style = MaterialTheme.typography.displayMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.secondary
        )
        
        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = state.userInput,
            onValueChange = { viewModel.onUserInputChange(it) },
            label = { Text("Escribe la palabra") },
            modifier = Modifier.fillMaxWidth(0.8f),
            textStyle = MaterialTheme.typography.headlineMedium.copy(textAlign = TextAlign.Center),
            singleLine = true,
            shape = RoundedCornerShape(20.dp),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { viewModel.validateTextInput(patientId, professionalId, appointmentId) })
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = { viewModel.validateTextInput(patientId, professionalId, appointmentId) },
            modifier = Modifier.fillMaxWidth(0.6f).height(64.dp),
            shape = RoundedCornerShape(16.dp),
            enabled = state.userInput.isNotEmpty() && !state.isCompleted
        ) {
            Icon(Icons.Default.Check, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Comprobar palabra", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }
    }
}
