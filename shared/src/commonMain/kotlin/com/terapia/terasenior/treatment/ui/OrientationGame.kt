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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrientationGame(
    viewModel: OrientationViewModel,
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
            // Barra de Navegación Profesional
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = MaterialTheme.colorScheme.error)
                }
                Text("Orientación de Realidad", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
                IconButton(onClick = { speechManager.speak(state.questionText) }) {
                    Icon(Icons.AutoMirrored.Filled.VolumeUp, contentDescription = "Repetir")
                }
            }

            Spacer(modifier = Modifier.height(48.dp))

            // Pregunta (v1.3.39: Ajuste de altura para visibilidad de botones)
            Card(
                modifier = Modifier.fillMaxWidth().widthIn(max = 600.dp).padding(horizontal = 16.dp),
                shape = RoundedCornerShape(32.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                    Text(
                        text = if (state.questionText.isBlank()) "Cargando..." else state.questionText,
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Black),
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Opciones (v1.3.39: Rejilla optimizada y siempre visible)
            Box(modifier = Modifier.fillMaxWidth().weight(1f).widthIn(max = 800.dp)) {
                if (state.options.isEmpty() && !state.isCompleted) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                } else if (!state.isCompleted) {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        contentPadding = PaddingValues(16.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(state.options) { option ->
                            val isCorrect = state.isCorrect == true && option == state.correctAnswer
                            val isError = state.isCorrect == false && option != state.correctAnswer

                            Button(
                                onClick = { viewModel.onOptionSelected(option, patientId, professionalId, appointmentId) },
                                modifier = Modifier.height(80.dp).fillMaxWidth(),
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
                                Text(option, fontSize = 22.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                            }
                        }
                    }
                }
            }

            // Feedback de Éxito
            AnimatedVisibility(visible = state.isCompleted) {
                Card(
                    modifier = Modifier.fillMaxWidth().widthIn(max = 500.dp).padding(bottom = 16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFC8E6C9)),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Text(
                        text = "¡Excelente! Has completado la orientación de hoy.",
                        modifier = Modifier.padding(20.dp).fillMaxWidth(),
                        textAlign = TextAlign.Center,
                        color = Color(0xFF1B5E20),
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            if (state.isCompleted) {
                Button(
                    onClick = onBack,
                    modifier = Modifier.fillMaxWidth().height(64.dp).widthIn(max = 400.dp),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Text("Volver al Plan", fontSize = 20.sp)
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
