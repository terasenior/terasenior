package com.terapia.terasenior.treatment.ui

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalculationGame(
    viewModel: CalculationViewModel,
    patientId: String?,
    professionalId: String?,
    appointmentId: String?,
    onBack: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val speechManager = remember { SpeechManager() }
    val instruction = "¿Cuánto es ${state.operationText}?"

    LaunchedEffect(state.operationText) {
        if (state.operationText.isNotEmpty()) {
            val voiceText = state.operationText
                .replace("+", "más")
                .replace("-", "menos")
            speechManager.speak("Calcula. $voiceText")
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
            // Barra de Navegación
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = MaterialTheme.colorScheme.error)
                }
                Text("Cálculo Mental", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
                IconButton(onClick = { speechManager.speak(instruction.replace("+", "más").replace("-", "menos")) }) {
                    Icon(Icons.AutoMirrored.Filled.VolumeUp, contentDescription = "Repetir")
                }
            }

            Spacer(modifier = Modifier.height(48.dp))

            // Operación
            Card(
                modifier = Modifier.fillMaxWidth().widthIn(max = 500.dp),
                shape = RoundedCornerShape(32.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f))
            ) {
                Text(
                    text = "${state.operationText} = ?",
                    modifier = Modifier.padding(40.dp).fillMaxWidth(),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.displayLarge.copy(
                        fontWeight = FontWeight.Black,
                        fontSize = 80.sp
                    ),
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            // Opciones de Respuesta
            Row(
                modifier = Modifier.fillMaxWidth().widthIn(max = 600.dp).padding(bottom = 32.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                state.options.forEach { option ->
                    val isCorrect = state.isCorrect == true && option == state.correctAnswer
                    val isError = state.isCorrect == false && option != state.correctAnswer
                    
                    Button(
                        onClick = { viewModel.onAnswerSelected(option, patientId, professionalId, appointmentId) },
                        modifier = Modifier.weight(1f).height(80.dp),
                        shape = RoundedCornerShape(20.dp),
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
                        Text(option.toString(), fontSize = 32.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Botón Siguiente
            AnimatedVisibility(visible = state.isCompleted) {
                Button(
                    onClick = { viewModel.startNewGame(state.currentLevel) },
                    modifier = Modifier.fillMaxWidth().height(56.dp).widthIn(max = 400.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("Siguiente Operación")
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
