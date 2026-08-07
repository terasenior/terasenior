package com.terapia.terasenior.treatment.ui

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
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
fun SizeOrderingGame(
    viewModel: SizeOrderingViewModel,
    patientId: String?,
    professionalId: String?,
    appointmentId: String?,
    onBack: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val speechManager = remember { SpeechManager() }
    val instruction = "Ordena las figuras de la más pequeña a la más grande."

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
                        text = "Percepción",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "Orden de Tamaños",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray
                    )
                }

                IconButton(onClick = { speechManager.speak(instruction) }) {
                    Icon(Icons.AutoMirrored.Filled.VolumeUp, contentDescription = "Repetir voz")
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Área de juego
            Box(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    state.items.forEach { item ->
                        val isSelected = state.selectedIds.contains(item.id)
                        
                        Box(
                            modifier = Modifier
                                .size(item.sizeDp.dp + 40.dp) // Un poco más para el padding/click
                                .clip(RoundedCornerShape(16.dp))
                                .background(if (isSelected) Color(0xFFC8E6C9) else Color.Transparent)
                                .border(
                                    width = 2.dp,
                                    color = if (isSelected) Color(0xFF4CAF50) else Color.Transparent,
                                    shape = RoundedCornerShape(16.dp)
                                )
                                .clickable(enabled = !isSelected) { 
                                    viewModel.onItemClicked(item, patientId, professionalId, appointmentId) 
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = null,
                                modifier = Modifier.size(item.sizeDp.dp),
                                tint = if (isSelected) Color(0xFF2E7D32) else MaterialTheme.colorScheme.primary
                            )
                            
                            if (isSelected) {
                                val order = state.selectedIds.indexOf(item.id) + 1
                                Surface(
                                    modifier = Modifier.align(Alignment.TopEnd).padding(4.dp).size(24.dp),
                                    shape = androidx.compose.foundation.shape.CircleShape,
                                    color = Color(0xFF2E7D32)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(order.toString(), color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
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
                        text = "¡Muy bien! Has ordenado las figuras correctamente.",
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
                Text(if (state.isCompleted) "Siguiente" else "Reiniciar", fontSize = 18.sp)
            }
        }
    }
}
