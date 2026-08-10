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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.terapia.terasenior.ui.components.accessibility.SpeechManager
import io.kamel.image.KamelImage
import io.kamel.image.asyncPainterResource

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

    LaunchedEffect(Unit) { speechManager.speak(instruction) }
    DisposableEffect(Unit) { onDispose { speechManager.stop() } }

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(modifier = Modifier.fillMaxSize().padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                IconButton(onClick = onBack, colors = IconButtonDefaults.iconButtonColors(containerColor = MaterialTheme.colorScheme.errorContainer)) {
                    Icon(Icons.Default.Close, null, tint = MaterialTheme.colorScheme.error)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Lenguaje y Evocación", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
                    Text("Denominación de Objetos", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                }
                IconButton(onClick = { speechManager.speak(instruction) }) { Icon(Icons.AutoMirrored.Filled.VolumeUp, null) }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Imagen del objeto (v1.3.3 - Imagen Real)
            Card(modifier = Modifier.size(240.dp), shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(8.dp)) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    state.targetItem?.let { item ->
                        KamelImage(
                            resource = { asyncPainterResource(item.imageUrl ?: "") },
                            contentDescription = item.name,
                            modifier = Modifier.fillMaxSize().padding(12.dp).clip(RoundedCornerShape(16.dp)),
                            onLoading = { CircularProgressIndicator() },
                            onFailure = {
                                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                                    Icon(item.icon, null, modifier = Modifier.size(80.dp), tint = MaterialTheme.colorScheme.primary)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(item.name, style = MaterialTheme.typography.labelMedium, color = Color.Gray)
                                }
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Opciones de nombre (v1.3.3 - Responsive)
            val columns = if (state.options.size <= 3) 1 else 2
            Box(modifier = Modifier.weight(1f).widthIn(max = 600.dp), contentAlignment = Alignment.Center) {
                ResponsiveGrid(items = state.options, columns = columns, spacing = 16.dp) { _, name, size ->
                    val isCorrect = state.isCorrect == true && name == state.targetItem?.name
                    Button(
                        onClick = { viewModel.onOptionSelected(name, patientId, professionalId, appointmentId) },
                        modifier = Modifier.fillMaxWidth().height(size.coerceAtMost(80.dp)),
                        shape = RoundedCornerShape(24.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isCorrect) Color(0xFF4CAF50) else MaterialTheme.colorScheme.secondaryContainer,
                            contentColor = if (isCorrect) Color.White else MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    ) {
                        Text(name, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            AnimatedVisibility(visible = state.isCompleted) {
                Card(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFFC8E6C9))) {
                    Text("¡Correcto! Es un(a) ${state.targetItem?.name}.", modifier = Modifier.padding(16.dp).fillMaxWidth(), color = Color(0xFF1B5E20), fontWeight = FontWeight.Bold)
                }
            }

            Button(onClick = { viewModel.startNewGame(state.currentLevel) }, modifier = Modifier.fillMaxWidth().height(56.dp), shape = RoundedCornerShape(16.dp), enabled = !state.isSaving) {
                Text(if (state.isCompleted) "Siguiente" else "Otro objeto", fontSize = 18.sp)
            }
        }
    }
}
