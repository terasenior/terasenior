package com.terapia.terasenior.treatment.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.random.Random

@Composable
fun NumberSearchGame(
    viewModel: NumberSearchViewModel,
    patientId: String?,
    professionalId: String?,
    appointmentId: String?,
    onBack: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Barra de Navegación
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.Close, contentDescription = "Cerrar")
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Busca el Número",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                    if (patientId == null) {
                        Text("Modo Práctica", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    }
                }
                Spacer(modifier = Modifier.width(48.dp))
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Panel del Objetivo
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "BUSCA ESTE NÚMERO:",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surface,
                        modifier = Modifier.size(100.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = state.targetNumber.toString(),
                                fontSize = 64.sp,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Cuadrícula de Juego
            val gridSize = state.gridSize
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                for (row in 0 until gridSize) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        for (col in 0 until gridSize) {
                            val index = row * gridSize + col
                            val cell = state.grid.getOrNull(index) ?: Cell(0)
                            NumberCell(
                                cell = cell,
                                onClick = {
                                    viewModel.onCellClicked(index, patientId, professionalId, appointmentId)
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Estado y Botón
            AnimatedVisibility(
                visible = state.isCompleted,
                enter = fadeIn() + expandVertically()
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                    colors = CardDefaults.cardColors(containerColor = if(state.isSaving) Color.LightGray else Color(0xFFC8E6C9)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    val message = if (state.isSaving) "Guardando resultados..." else "¡Excelente! Has encontrado todos los números."
                    Text(
                        text = message,
                        modifier = Modifier.padding(16.dp).fillMaxWidth(),
                        textAlign = TextAlign.Center,
                        color = if(state.isSaving) Color.DarkGray else Color(0xFF1B5E20),
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
                Text(if (state.isCompleted) "Siguiente Ejercicio" else "Reiniciar Ejercicio", fontSize = 18.sp)
            }
        }
    }
}

@Composable
private fun NumberCell(
    cell: Cell,
    onClick: () -> Unit
) {
    val backgroundColor = when {
        cell.isFound -> Color(0xFF81C784)
        cell.isWrong -> Color(0xFFFF8A80)
        else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    }

    val scale by animateFloatAsState(
        targetValue = if (cell.isFound) 1.05f else 1f
    )

    Box(
        modifier = Modifier
            .size(60.dp)
            .scale(scale)
            .clip(RoundedCornerShape(14.dp))
            .background(backgroundColor)
            .clickable(enabled = !cell.isFound && !cell.isWrong, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = cell.number.toString(),
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = when {
                cell.isFound -> Color.White
                cell.isWrong -> Color.White
                else -> MaterialTheme.colorScheme.onSurface
            }
        )
    }
}
