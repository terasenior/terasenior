package com.terapia.terasenior.treatment.ui

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.terapia.terasenior.ui.components.accessibility.SpeechManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ColorIdentificationGame(
    viewModel: ColorIdentificationViewModel,
    patientId: String?,
    professionalId: String?,
    appointmentId: String?,
    onBack: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val speechManager = remember { SpeechManager() }
    val instruction = "Toca el color: ${state.targetColor?.name ?: ""}"

    LaunchedEffect(state.targetColor) {
        state.targetColor?.let {
            speechManager.speak(instruction)
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(
                    onClick = onBack,
                    colors = IconButtonDefaults.iconButtonColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Finalizar", tint = MaterialTheme.colorScheme.error)
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Percepción",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "Identificación de Colores",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray
                    )
                }

                IconButton(onClick = { speechManager.speak(instruction) }) {
                    Icon(Icons.AutoMirrored.Filled.VolumeUp, contentDescription = "Repetir voz")
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Nombre del color objetivo
            Card(
                modifier = Modifier.fillMaxWidth().widthIn(max = 600.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f))
            ) {
                Text(
                    text = state.targetColor?.name?.uppercase() ?: "",
                    modifier = Modifier.padding(24.dp).fillMaxWidth(),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.displayMedium.copy(fontWeight = FontWeight.Black),
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Cuadrícula de colores
            val columns = when (state.currentLevel) {
                1 -> 2
                2 -> 2
                3 -> 2
                else -> 3
            }

            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(columns),
                    horizontalArrangement = Arrangement.spacedBy(24.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp),
                    contentPadding = PaddingValues(16.dp),
                    modifier = Modifier.widthIn(max = 600.dp)
                ) {
                    items(state.options) { item ->
                        val isCorrectSelection = state.isCorrect == true && item == state.targetColor
                        
                        Box(
                            modifier = Modifier
                                .aspectRatio(1f)
                                .clip(RoundedCornerShape(24.dp))
                                .background(item.color)
                                .border(
                                    width = if (isCorrectSelection) 6.dp else 1.dp,
                                    color = if (isCorrectSelection) Color(0xFF4CAF50) else Color.LightGray,
                                    shape = RoundedCornerShape(24.dp)
                                )
                                .clickable { viewModel.onColorSelected(item, patientId, professionalId, appointmentId) }
                        ) {
                            if (isCorrectSelection) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    modifier = Modifier.align(Alignment.Center).size(64.dp),
                                    tint = Color.White
                                )
                            }
                        }
                    }
                }
            }

            AnimatedVisibility(visible = state.isCompleted) {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFC8E6C9))
                ) {
                    Text(
                        text = "¡Muy bien! Has identificado el color ${state.targetColor?.name}.",
                        modifier = Modifier.padding(16.dp).fillMaxWidth(),
                        textAlign = TextAlign.Center,
                        color = Color(0xFF1B5E20),
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Button(
                onClick = { viewModel.startNewGame(state.currentLevel) },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp),
                enabled = !state.isSaving
            ) {
                Text(if (state.isCompleted) "Siguiente Color" else "Reiniciar", fontSize = 18.sp)
            }
        }
    }
}
