package com.terapia.terasenior.treatment.ui

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import io.kamel.image.KamelImage
import io.kamel.image.asyncPainterResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PairsGame(
    viewModel: PairsViewModel,
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
            modifier = Modifier.fillMaxSize().padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                IconButton(onClick = onBack, colors = IconButtonDefaults.iconButtonColors(containerColor = MaterialTheme.colorScheme.errorContainer)) {
                    Icon(Icons.Default.Close, contentDescription = "Finalizar", tint = MaterialTheme.colorScheme.error)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Parejas de Memoria", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
                    Text("Nivel ${state.currentLevel} • Parejas: ${state.pairsFound}/${state.totalPairs}", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                }
                Box(modifier = Modifier.size(48.dp))
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Game Grid (v1.3.3 - Responsive)
            BoxWithConstraints(modifier = Modifier.weight(1f).fillMaxWidth()) {
                val availableWidth = maxWidth
                val availableHeight = maxHeight
                
                val gridSize = if (state.totalPairs <= 4) 2 else if (state.totalPairs <= 8) 4 else 5
                val numRows = kotlin.math.ceil(state.cards.size.toDouble() / gridSize).toInt()
                
                val cellWidth = (availableWidth - (12.dp * (gridSize + 1))) / gridSize
                val cellHeight = (availableHeight - (12.dp * (numRows + 1))) / numRows
                val itemSize = minOf(cellWidth, cellHeight)

                LazyVerticalGrid(
                    columns = GridCells.Fixed(gridSize),
                    horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally),
                    verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically),
                    contentPadding = PaddingValues(8.dp),
                    modifier = Modifier.fillMaxSize(),
                    userScrollEnabled = false
                ) {
                    itemsIndexed(state.cards) { index, card ->
                        MemoryCardItem(
                            card = card,
                            size = itemSize,
                            onClick = { viewModel.onCardClicked(index, patientId, professionalId, appointmentId) }
                        )
                    }
                }
            }

            // Feedback
            AnimatedVisibility(visible = state.isCompleted) {
                Card(modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFFC8E6C9))) {
                    Text("¡Enhorabuena! Has encontrado todas las parejas.", modifier = Modifier.padding(16.dp).fillMaxWidth(), textAlign = TextAlign.Center, color = Color(0xFF1B5E20), fontWeight = FontWeight.Bold)
                }
            }

            Button(
                onClick = { viewModel.startNewGame(state.currentLevel) },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp),
                enabled = !state.isSaving
            ) {
                Text(if (state.isCompleted) "Siguiente Ejercicio" else "Reiniciar", fontSize = 18.sp)
            }
        }
    }
}

@Composable
fun MemoryCardItem(card: MemoryCard, size: androidx.compose.ui.unit.Dp, onClick: () -> Unit) {
    val isVisible = card.isFlipped || card.isMatched

    Surface(
        onClick = onClick,
        modifier = Modifier.size(size),
        shape = RoundedCornerShape(16.dp),
        color = if (isVisible) Color.White else MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
        tonalElevation = 4.dp,
        border = if (card.isMatched) BorderStroke(3.dp, Color(0xFF4CAF50)) else null
    ) {
        Box(contentAlignment = Alignment.Center) {
            if (isVisible) {
                if (card.isRealImage && card.imageUrl != null) {
                    KamelImage(
                        resource = { asyncPainterResource(card.imageUrl) },
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize().padding(8.dp).clip(RoundedCornerShape(8.dp)),
                        onLoading = { CircularProgressIndicator(modifier = Modifier.size(24.dp)) },
                        onFailure = { Icon(Icons.Default.BrokenImage, null, tint = Color.Gray) }
                    )
                } else if (card.icon != null) {
                    Icon(imageVector = card.icon, contentDescription = null, modifier = Modifier.size(size * 0.6f), tint = MaterialTheme.colorScheme.primary)
                }
            } else {
                Icon(imageVector = Icons.Default.QuestionMark, contentDescription = null, modifier = Modifier.size(size * 0.5f), tint = Color.White)
            }
        }
    }
}
