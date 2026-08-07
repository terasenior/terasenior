package com.terapia.terasenior.treatment.ui

import androidx.compose.animation.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.terapia.terasenior.ui.components.accessibility.SpeechManager
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TracingGame(
    viewModel: TracingViewModel,
    patientId: String?,
    professionalId: String?,
    appointmentId: String?,
    onBack: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val speechManager = remember { SpeechManager() }
    val instruction = "Sigue la línea de puntos con el dedo."

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
                        text = "Lectoescritura",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "Trazos Básicos",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray
                    )
                }

                IconButton(onClick = { speechManager.speak(instruction) }) {
                    Icon(Icons.AutoMirrored.Filled.VolumeUp, contentDescription = "Repetir voz")
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Lienzo de dibujo
            Card(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(8.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    val primaryColor = MaterialTheme.colorScheme.primary
                    Canvas(
                        modifier = Modifier
                            .fillMaxSize()
                            .pointerInput(state.isCompleted) {
                                if (!state.isCompleted) {
                                    detectDragGestures(
                                        onDragStart = { offset -> viewModel.onDrawPoint(offset) },
                                        onDrag = { change, _ ->
                                            viewModel.onDrawPoint(change.position)
                                        },
                                        onDragEnd = {
                                            // Podríamos validar aquí, por ahora finalizamos si hay trazo
                                            if (state.userPoints.size > 10) {
                                                viewModel.completeTracing(patientId, professionalId, appointmentId)
                                            }
                                        }
                                    )
                                }
                            }
                    ) {
                        val canvasWidth = size.width
                        val canvasHeight = size.height
                        
                        // Dibujar el camino guía
                        val guidePath = Path()
                        when (state.pathType) {
                            TracingPathType.HORIZONTAL_LINE -> {
                                guidePath.moveTo(canvasWidth * 0.1f, canvasHeight * 0.5f)
                                guidePath.lineTo(canvasWidth * 0.9f, canvasHeight * 0.5f)
                            }
                            TracingPathType.VERTICAL_LINE -> {
                                guidePath.moveTo(canvasWidth * 0.5f, canvasHeight * 0.1f)
                                guidePath.lineTo(canvasWidth * 0.5f, canvasHeight * 0.9f)
                            }
                            TracingPathType.ZIGZAG -> {
                                guidePath.moveTo(canvasWidth * 0.1f, canvasHeight * 0.2f)
                                guidePath.lineTo(canvasWidth * 0.3f, canvasHeight * 0.8f)
                                guidePath.lineTo(canvasWidth * 0.5f, canvasHeight * 0.2f)
                                guidePath.lineTo(canvasWidth * 0.7f, canvasHeight * 0.8f)
                                guidePath.lineTo(canvasWidth * 0.9f, canvasHeight * 0.2f)
                            }
                            TracingPathType.CIRCLE -> {
                                val center = Offset(canvasWidth * 0.5f, canvasHeight * 0.5f)
                                val radius = canvasWidth.coerceAtMost(canvasHeight) * 0.35f
                                for (i in 0..360 step 5) {
                                    val angle = i * PI / 180
                                    val x = center.x + radius * cos(angle).toFloat()
                                    val y = center.y + radius * sin(angle).toFloat()
                                    if (i == 0) guidePath.moveTo(x, y) else guidePath.lineTo(x, y)
                                }
                            }
                        }
                        
                        drawPath(
                            path = guidePath,
                            color = Color.LightGray,
                            style = Stroke(
                                width = 12f,
                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(20f, 20f), 0f),
                                cap = StrokeCap.Round
                            )
                        )

                        // Dibujar el trazo del usuario
                        if (state.userPoints.size > 1) {
                            val userPath = Path()
                            userPath.moveTo(state.userPoints.first().x, state.userPoints.first().y)
                            state.userPoints.drop(1).forEach { point ->
                                userPath.lineTo(point.x, point.y)
                            }
                            drawPath(
                                path = userPath,
                                color = if (state.isCompleted) Color(0xFF4CAF50) else primaryColor,
                                style = Stroke(width = 8f, cap = StrokeCap.Round)
                            )
                        }
                    }

                    if (!state.isCompleted) {
                        IconButton(
                            onClick = { viewModel.clearDrawing() },
                            modifier = Modifier.align(Alignment.TopEnd).padding(16.dp),
                            colors = IconButtonDefaults.iconButtonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = "Borrar")
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            AnimatedVisibility(visible = state.isCompleted) {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFC8E6C9))
                ) {
                    Text(
                        text = "¡Muy bien! Buen trabajo con el trazo.",
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
                Text(if (state.isCompleted) "Siguiente Trazo" else "Reiniciar", fontSize = 18.sp)
            }
        }
    }
}
