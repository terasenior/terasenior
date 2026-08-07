package com.terapia.terasenior.treatment.ui

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.terapia.terasenior.ui.components.accessibility.SpeechManager

@Composable
fun PerceptionGame(
    viewModel: PerceptionViewModel,
    patientId: String?,
    professionalId: String?,
    appointmentId: String?,
    onBack: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val speechManager = remember { SpeechManager() }

    LaunchedEffect(state.questionText) {
        if (state.questionText.isNotEmpty()) {
            speechManager.speak(state.questionText)
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
                IconButton(onClick = onBack) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Cerrar",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(32.dp)
                    )
                }
                val title = when(state.currentType) {
                    PerceptionType.LATERAL_DOMINANCE -> "Dominancia Lateral"
                    PerceptionType.MIRROR -> "Imagen en Espejo"
                    PerceptionType.BODY_PARTS -> "Partes del Cuerpo"
                }
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )
                IconButton(onClick = { speechManager.speak(state.questionText) }) {
                    Icon(Icons.AutoMirrored.Filled.VolumeUp, contentDescription = "Repetir")
                }
            }

            LinearProgressIndicator(
                progress = { (state.currentStep.toFloat() / state.totalSteps.toFloat()).coerceIn(0f, 1f) },
                modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.primaryContainer,
                strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Pregunta
            Card(
                modifier = Modifier.fillMaxWidth().widthIn(max = 700.dp),
                shape = RoundedCornerShape(32.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)
            ) {
                Text(
                    text = state.questionText,
                    modifier = Modifier.padding(32.dp).fillMaxWidth(),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Black),
                    color = MaterialTheme.colorScheme.onTertiaryContainer
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            // Opciones
            Box(modifier = Modifier.fillMaxWidth().widthIn(max = 900.dp)) {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(16.dp)
                ) {
                    items(state.options) { option ->
                        val isCorrect = state.isCorrect == true && option == state.correctAnswer
                        val isError = state.isCorrect == false && option != state.correctAnswer

                        Button(
                            onClick = { viewModel.onOptionSelected(option, patientId, professionalId, appointmentId) },
                            modifier = Modifier.height(110.dp).fillMaxWidth(),
                            shape = RoundedCornerShape(24.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = when {
                                    isCorrect -> Color(0xFF4CAF50)
                                    isError -> Color(0xFFF44336)
                                    else -> MaterialTheme.colorScheme.secondaryContainer
                                },
                                contentColor = when {
                                    isCorrect || isError -> Color.White
                                    else -> MaterialTheme.colorScheme.onSecondaryContainer
                                }
                            )
                        ) {
                            Text(
                                text = option,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center,
                                lineHeight = 28.sp
                            )
                        }
                    }
                }
            }

            // Completion Feedback
            AnimatedVisibility(
                visible = state.isCompleted,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth().widthIn(max = 600.dp).padding(bottom = 16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFC8E6C9)),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp).fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "¡Excelente! Has terminado los ejercicios de percepción.",
                            textAlign = TextAlign.Center,
                            color = Color(0xFF1B5E20),
                            fontWeight = FontWeight.Bold,
                            fontSize = 22.sp
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = onBack,
                            modifier = Modifier.height(56.dp).fillMaxWidth(0.7f),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))
                        ) {
                            Text("Finalizar", fontSize = 18.sp)
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
