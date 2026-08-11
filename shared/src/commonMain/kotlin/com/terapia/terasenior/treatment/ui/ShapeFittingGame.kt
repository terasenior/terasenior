package com.terapia.terasenior.treatment.ui

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.terapia.terasenior.ui.components.accessibility.SpeechManager
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShapeFittingGame(
    viewModel: ShapeFittingViewModel,
    patientId: String?,
    professionalId: String?,
    appointmentId: String?,
    onBack: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val speechManager = remember { SpeechManager() }
    val density = LocalDensity.current
    val instruction = "Arrastra la pieza hasta su silueta."

    LaunchedEffect(Unit) { 
        speechManager.speak(instruction)
        viewModel.startNewGame(state.currentLevel)
    }
    
    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(modifier = Modifier.fillMaxSize().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            // Cabecera
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                IconButton(onClick = onBack, colors = IconButtonDefaults.iconButtonColors(containerColor = MaterialTheme.colorScheme.errorContainer)) {
                    Icon(Icons.Default.Close, null, tint = MaterialTheme.colorScheme.error)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Percepción / Praxias", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
                    Text("Encaje de Formas", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                }
                IconButton(onClick = { speechManager.speak(instruction) }) { Icon(Icons.AutoMirrored.Filled.VolumeUp, null) }
            }

            Spacer(modifier = Modifier.height(24.dp))

            var containerOffset by remember { mutableStateOf(Offset.Zero) }

            // ÁREA DE TRABAJO (v1.3.15 - Drag Fix)
            BoxWithConstraints(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .onGloballyPositioned { containerOffset = it.positionInWindow() }
            ) {
                val areaWidth = constraints.maxWidth.toFloat()
                val areaHeight = constraints.maxHeight.toFloat()

                // 1. DIBUJAR SILUETAS (DESTINOS)
                Row(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .fillMaxWidth()
                        .padding(top = 40.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    state.pieces.forEach { piece ->
                        Box(
                            modifier = Modifier
                                .size(120.dp)
                                .clip(RoundedCornerShape(20.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                                .onGloballyPositioned { layoutCoordinates ->
                                    // Calculamos la posición relativa al contenedor principal
                                    val windowPos = layoutCoordinates.positionInWindow()
                                    val targetPos = windowPos - containerOffset
                                    viewModel.updatePieceTarget(piece.id, targetPos)
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(piece.icon, null, modifier = Modifier.size(80.dp).alpha(0.2f), tint = Color.Black)
                        }
                    }
                }

                // 2. DIBUJAR PIEZAS (MÓVILES)
                state.pieces.forEach { piece ->
                    // Calculamos posición inicial abajo si no se ha movido
                    val initialOffset = remember(areaWidth, areaHeight, state.pieces.size) {
                        val index = state.pieces.indexOf(piece)
                        val x = (areaWidth / (state.pieces.size + 1)) * (index + 1) - with(density) { 60.dp.toPx() }
                        val y = areaHeight - with(density) { 180.dp.toPx() }
                        Offset(x, y)
                    }

                    val currentPos = if (piece.currentOffset == Offset.Zero) initialOffset else piece.currentOffset
                    val currentPosState = rememberUpdatedState(currentPos)

                    Box(
                        modifier = Modifier
                            .offset { IntOffset(currentPos.x.roundToInt(), currentPos.y.roundToInt()) }
                            .size(120.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(if (piece.isSnapped) Color(0xFFC8E6C9) else MaterialTheme.colorScheme.primaryContainer)
                            .pointerInput(piece.id, piece.isSnapped) {
                                if (!piece.isSnapped) {
                                    detectDragGestures(
                                        onDrag = { change, dragAmount ->
                                            change.consume()
                                            viewModel.updatePiecePosition(piece.id, currentPosState.value + dragAmount)
                                        },
                                        onDragEnd = {
                                            viewModel.trySnapPiece(piece.id, patientId, professionalId, appointmentId)
                                        }
                                    )
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = piece.icon,
                            contentDescription = null,
                            modifier = Modifier.size(80.dp),
                            tint = if (piece.isSnapped) Color(0xFF1B5E20) else MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            // Feedback de Éxito
            AnimatedVisibility(visible = state.isCompleted) {
                Card(modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFFC8E6C9))) {
                    Text("¡Excelente trabajo! Has completado el encaje.", modifier = Modifier.padding(16.dp).fillMaxWidth(), textAlign = TextAlign.Center, color = Color(0xFF1B5E20), fontWeight = FontWeight.Bold)
                }
            }

            if (state.isCompleted) {
                Button(onClick = { viewModel.startNewGame(state.currentLevel) }, modifier = Modifier.fillMaxWidth().height(64.dp), shape = RoundedCornerShape(20.dp)) {
                    Text("Siguiente Ejercicio", fontSize = 20.sp)
                }
            }
        }
    }
}