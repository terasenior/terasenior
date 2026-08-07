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
fun NamingObjectsGame(
    viewModel: NamingObjectsViewModel,
    patientId: String?,
    professionalId: String?,
    appointmentId: String?,
    onBack: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val speechManager = remember { SpeechManager() }
    val instruction = "¿Qué objeto es este?"

    LaunchedEffect(Unit) {
        speechManager.speak(instruction)
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
                        text = "Lenguaje",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "Denominación",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray
                    )
                }

                IconButton(onClick = { speechManager.speak(instruction) }) {
                    Icon(Icons.AutoMirrored.Filled.VolumeUp, contentDescription = "Repetir voz")
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Imagen del objeto
            Card(
                modifier = Modifier.size(200.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f))
            ) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    state.targetItem?.let { item ->
                        Icon(
                            imageVector = item.icon,
                            contentDescription = null,
                            modifier = Modifier.size(120.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Opciones de nombre
            val columns = when (state.currentLevel) {
                1 -> 1
                2 -> 1
                else -> 2
            }

            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(columns),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.widthIn(max = 600.dp)
                ) {
                    items(state.options) { name ->
                        val isCorrectSelection = state.isCorrect == true && name == state.targetItem?.name
                        val isWrongSelection = state.isCorrect == false && name != state.targetItem?.name // Simplificado

                        Button(
                            onClick = { viewModel.onOptionSelected(name, patientId, professionalId, appointmentId) },
                            modifier = Modifier.fillMaxWidth().height(64.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = when {
                                    isCorrectSelection -> Color(0xFF4CAF50)
                                    state.isCorrect == false && name == state.targetItem?.name -> Color(0xFF4CAF50).copy(alpha = 0.5f) // Mostrar la correcta si falló? No, mejor feedback simple.
                                    else -> MaterialTheme.colorScheme.secondaryContainer
                                },
                                contentColor = if (isCorrectSelection) Color.White else MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        ) {
                            Text(name, fontSize = 20.sp, fontWeight = FontWeight.Medium)
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
                        text = "¡Correcto! Es un(a) ${state.targetItem?.name}.",
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
                Text(if (state.isCompleted) "Siguiente" else "Otro objeto", fontSize = 18.sp)
            }
        }
    }
}
