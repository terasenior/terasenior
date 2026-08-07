package com.terapia.terasenior.treatment.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.terapia.terasenior.ui.components.accessibility.SpeechManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PairsGame(
    viewModel: PairsViewModel,
    patientId: String?,
    professionalId: String?,
    appointmentId: String?,
    description: String = "Encuentra todas las parejas de figuras ocultas.",
    onBack: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val speechManager = remember { SpeechManager() }

    LaunchedEffect(Unit) {
        speechManager.speak(description)
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
                IconButton(
                    onClick = onBack,
                    colors = IconButtonDefaults.iconButtonColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Finalizar", tint = MaterialTheme.colorScheme.error)
                }
                
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Encuentra Parejas",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                    Text("Parejas: ${state.pairsFound} / ${state.totalPairs}", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                }

                IconButton(onClick = { speechManager.speak(description) }) {
                    Icon(Icons.AutoMirrored.Filled.VolumeUp, contentDescription = "Repetir instrucción")
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Cuadrícula de Cartas (Adaptativa y con límites de tamaño)
            val columns = when {
                state.currentLevel <= 2 -> 2
                state.currentLevel <= 4 -> 4
                else -> 5
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .widthIn(max = 700.dp) // Ancho máximo para que no se estire demasiado en horizontal
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(columns),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(8.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    itemsIndexed(state.cards) { index, card ->
                        CardItem(
                            card = card,
                            onClick = { viewModel.onCardClicked(index, patientId, professionalId, appointmentId) }
                        )
                    }
                }
            }

            // Estado y Botón
            AnimatedVisibility(visible = state.isCompleted) {
                Card(
                    modifier = Modifier.fillMaxWidth().widthIn(max = 500.dp).padding(bottom = 16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFC8E6C9)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        text = "¡Fantástico! Has encontrado todas las parejas.",
                        modifier = Modifier.padding(16.dp).fillMaxWidth(),
                        textAlign = TextAlign.Center,
                        color = Color(0xFF1B5E20),
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Button(
                onClick = { viewModel.startNewGame(state.currentLevel) },
                modifier = Modifier.fillMaxWidth().height(64.dp).widthIn(max = 400.dp),
                shape = RoundedCornerShape(20.dp),
                enabled = !state.isSaving
            ) {
                Text(if (state.isCompleted) "Siguiente Ejercicio" else "Reiniciar Ejercicio", fontSize = 18.sp)
            }
        }
    }
}

@Composable
private fun CardItem(card: MemoryCard, onClick: () -> Unit) {
    val rotation by animateFloatAsState(
        targetValue = if (card.isFlipped || card.isMatched) 180f else 0f,
        animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing)
    )

    Surface(
        modifier = Modifier
            .aspectRatio(1f)
            .graphicsLayer {
                rotationY = rotation
                cameraDistance = 12f * density
            }
            .clickable(enabled = !card.isFlipped && !card.isMatched, onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        color = if (rotation <= 90f) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
        tonalElevation = 2.dp
    ) {
        Box(contentAlignment = Alignment.Center) {
            if (rotation > 90f) {
                Icon(
                    imageVector = card.icon,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp).graphicsLayer { rotationY = 180f },
                    tint = if (card.isMatched) Color(0xFF2E7D32) else MaterialTheme.colorScheme.primary
                )
            } else {
                Text(
                    text = "?",
                    fontSize = 32.sp,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
            
            if (card.isMatched) {
                Box(
                    modifier = Modifier.fillMaxSize().background(Color.White.copy(alpha = 0.2f))
                )
            }
        }
    }
}
