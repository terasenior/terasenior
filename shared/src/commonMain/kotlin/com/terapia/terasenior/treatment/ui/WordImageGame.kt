package com.terapia.terasenior.treatment.ui

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.terapia.terasenior.ui.components.accessibility.SpeechManager
import io.kamel.image.KamelImage
import io.kamel.image.asyncPainterResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WordImageGame(
    viewModel: WordImageViewModel,
    patientId: String?,
    professionalId: String?,
    appointmentId: String?,
    onBack: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val speechManager = remember { SpeechManager() }
    val instruction = "Busca la imagen de: ${state.targetItem?.name}"

    // Voz automática al cargar
    LaunchedEffect(state.targetItem) {
        state.targetItem?.let {
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
                        text = "Vocabulario",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "Asociación Palabra-Imagen",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray
                    )
                }

                IconButton(onClick = { speechManager.speak(instruction) }) {
                    Icon(Icons.AutoMirrored.Filled.VolumeUp, contentDescription = "Repetir voz")
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Palabra Objetivo (Grande y clara)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f))
            ) {
                Text(
                    text = state.targetItem?.name?.uppercase() ?: "",
                    modifier = Modifier.padding(24.dp).fillMaxWidth(),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.displayMedium.copy(fontWeight = FontWeight.Black),
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Cuadrícula de Opciones (v1.3.3 - Responsive)
            val columns = if (state.currentLevel <= 2) 2 else 3

            Box(modifier = Modifier.weight(1f).widthIn(max = 800.dp), contentAlignment = Alignment.Center) {
                ResponsiveGrid(items = state.options, columns = columns, spacing = 16.dp) { _, item, size ->
                    OptionCard(
                        item = item,
                        size = size,
                        isCorrect = if (state.isCorrect == true && item == state.targetItem) true else null,
                        onClick = { viewModel.onOptionSelected(item, patientId, professionalId, appointmentId) }
                    )
                }
            }

            // Feedback Visual
            AnimatedVisibility(visible = state.isCompleted) {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFC8E6C9))
                ) {
                    Text(
                        text = "¡Muy bien! Has identificado el objeto.",
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
                Text(if (state.isCompleted) "Siguiente Objeto" else "Cambiar Palabra", fontSize = 18.sp)
            }
        }
    }
}

@Composable
private fun OptionCard(
    item: WordImageItem,
    size: androidx.compose.ui.unit.Dp,
    isCorrect: Boolean?,
    onClick: () -> Unit
) {
    val borderColor = when (isCorrect) {
        true -> Color(0xFF4CAF50)
        false -> Color(0xFFF44336)
        else -> Color.Transparent
    }

    Surface(
        onClick = onClick,
        modifier = Modifier.size(size),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp,
        border = if (isCorrect != null) androidx.compose.foundation.BorderStroke(4.dp, borderColor) else null
    ) {
        Box(contentAlignment = Alignment.Center) {
            if (!item.imageUrl.isNullOrBlank()) {
                KamelImage(
                    resource = { asyncPainterResource(item.imageUrl!!) },
                    contentDescription = item.name,
                    modifier = Modifier.fillMaxSize().padding(8.dp).clip(RoundedCornerShape(12.dp)),
                    onLoading = { CircularProgressIndicator() },
                    onFailure = { Icon(item.icon, null, modifier = Modifier.size(size * 0.6f), tint = Color.Gray) }
                )
            } else {
                Icon(
                    imageVector = item.icon,
                    contentDescription = item.name,
                    modifier = Modifier.size(size * 0.6f),
                    tint = if (isCorrect == true) Color(0xFF2E7D32) else MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}
