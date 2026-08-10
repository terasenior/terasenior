package com.terapia.terasenior.treatment.ui

import androidx.compose.animation.*
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.terapia.terasenior.ui.components.accessibility.SpeechManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SemanticCategoryGame(
    viewModel: SemanticCategoryViewModel,
    patientId: String?,
    professionalId: String?,
    appointmentId: String?,
    onBack: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val speechManager = remember { SpeechManager() }
    val instruction = "Busca todos los elementos de la categoría: ${state.targetCategory}"

    LaunchedEffect(state.targetCategory) {
        if (state.targetCategory.isNotEmpty()) {
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
            // Barra de Navegación
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = MaterialTheme.colorScheme.error)
                }
                
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Clasificación", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
                    Text("Busca: ${state.targetCategory}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                }

                IconButton(onClick = { speechManager.speak(instruction) }) {
                    Icon(Icons.AutoMirrored.Filled.VolumeUp, contentDescription = "Repetir voz")
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Instrucción Visual
            Card(
                modifier = Modifier.fillMaxWidth().widthIn(max = 600.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Text(
                    text = "¿Cuáles son ${state.targetCategory}?",
                    modifier = Modifier.padding(20.dp).fillMaxWidth(),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Cuadrícula (v1.3.3 - Responsive)
            Box(modifier = Modifier.weight(1f).widthIn(max = 800.dp), contentAlignment = Alignment.Center) {
                ResponsiveGrid(items = state.items, columns = 3, spacing = 16.dp) { index, item, size ->
                    val isError = state.errorIndex == index
                    Surface(
                        onClick = { viewModel.onItemClicked(index, patientId, professionalId, appointmentId) },
                        modifier = Modifier.size(size),
                        shape = RoundedCornerShape(24.dp),
                        color = if (isError) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.surface,
                        tonalElevation = 2.dp,
                        border = if (isError) androidx.compose.foundation.BorderStroke(4.dp, Color.Red) else null
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(item.icon, contentDescription = item.name, modifier = Modifier.size(size * 0.6f))
                        }
                    }
                }
            }

            // Botón
            Button(
                onClick = { viewModel.startNewGame(state.currentLevel) },
                modifier = Modifier.fillMaxWidth().height(56.dp).widthIn(max = 400.dp),
                shape = RoundedCornerShape(16.dp),
                enabled = !state.isSaving
            ) {
                Text(if (state.isCompleted) "Siguiente" else "Cambiar Categoría")
            }
        }
    }
}
