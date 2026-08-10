package com.terapia.terasenior.treatment.ui

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.terapia.terasenior.ui.components.accessibility.SpeechManager
import io.kamel.image.KamelImage
import io.kamel.image.asyncPainterResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VisualAttentionGame(
    viewModel: VisualAttentionViewModel,
    patientId: String?,
    professionalId: String?,
    appointmentId: String?,
    onBack: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val speechManager = remember { SpeechManager() }
    
    val instruction = when (state.variation) {
        "attention_different" -> "Busca el elemento que es diferente a los demás."
        "attention_equals_model" -> "Busca todos los elementos iguales al modelo de arriba."
        "attention_positions" -> "Busca los que están en la misma posición que la flecha de arriba."
        "attention_letters" -> "Busca todas las letras iguales al modelo."
        "attention_numbers" -> "Busca todos los números iguales al modelo."
        "attention_symbols" -> "Busca todos los símbolos iguales al modelo."
        "attention_matrices" -> "Encuentra todos los símbolos iguales al modelo en la matriz."
        "attention_row_cancel" -> "Tacha todos los elementos iguales al modelo y luego cuéntalos."
        "attention_yes_no" -> "Tacha una letra sí y otra no, siguiendo el orden."
        "attention_count" -> "Cuenta cuántos elementos hay en total en el recuadro."
        "attention_differences" -> "Encuentra el elemento que ha cambiado en el segundo recuadro."
        "attention_dual_task" -> "Busca todos los elementos que sean iguales a cualquiera de los dos modelos."
        "attention_consecutive" -> "Pulsa los números en orden consecutivo, empezando por el 1."
        "attention_longest" -> "Pulsa sobre la palabra o cifra que sea más larga."
        "attention_word_search" -> "Busca la palabra escondida en la sopa de letras."
        else -> "Presta atención y busca los elementos indicados."
    }

    LaunchedEffect(state.variation) {
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
            // Header
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
                        text = "Atención Visual",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = when(state.variation) {
                            "attention_different" -> "El intruso"
                            "attention_equals_model" -> "Búsqueda visual"
                            "attention_positions" -> "Orientación"
                            "attention_letters" -> "Letras"
                            "attention_numbers" -> "Números"
                            "attention_symbols" -> "Símbolos"
                            "attention_matrices" -> "Matriz de atención"
                            "attention_row_cancel" -> "Cancelación y recuento"
                            "attention_yes_no" -> "Alternancia"
                            "attention_count" -> "Recuento"
                            "attention_differences" -> "Buscar diferencias"
                            "attention_dual_task" -> "Tarea dual"
                            "attention_consecutive" -> "Números consecutivos"
                            "attention_longest" -> "Elemento más largo"
                            "attention_word_search" -> "Sopa de letras"
                            else -> "Variación"
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray
                    )
                }

                IconButton(onClick = { speechManager.speak(instruction) }) {
                    Icon(Icons.AutoMirrored.Filled.VolumeUp, contentDescription = "Repetir voz")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Model / Target Display
            if (state.target != null && state.variation != "attention_differences" &&
                state.variation != "attention_consecutive" && state.variation != "attention_longest") {
                
                val targets = if (state.variation == "attention_dual_task") {
                    state.target as? List<*> ?: emptyList<Any>()
                } else if (state.variation == "attention_word_search") {
                    listOf((state.target as? Pair<*, *>)?.first ?: "")
                } else {
                    listOf(state.target!!)
                }

                Text(
                    text = if (state.variation == "attention_word_search") "Categoría:" else "Modelo a buscar:",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    targets.forEach { t ->
                        if (t != null) TargetDisplayCard(content = t, isRealImage = state.isTargetRealImage)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Game Grid (v1.3.3 - Responsive Layout)
            Box(modifier = Modifier.weight(if (state.isCountingPhase) 0.7f else 1f).fillMaxWidth()) {
                if (state.variation == "attention_differences") {
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        horizontalArrangement = Arrangement.spacedBy(24.dp)
                    ) {
                        Box(modifier = Modifier.weight(1f)) {
                            ResponsiveGrid(items = state.items, columns = state.gridSize) { _, item, size ->
                                VisualAttentionItemCard(item = item, size = size, onClick = { })
                            }
                        }
                        Box(modifier = Modifier.weight(1f)) {
                            ResponsiveGrid(items = state.items2, columns = state.gridSize) { index, item, size ->
                                VisualAttentionItemCard(
                                    item = item,
                                    size = size,
                                    onClick = { viewModel.onItemClicked(index, patientId, professionalId, appointmentId, isGrid2 = true) }
                                )
                            }
                        }
                    }
                } else if (state.variation == "attention_longest") {
                    Column(
                        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        state.items.forEachIndexed { index, item ->
                            Button(
                                onClick = { viewModel.onItemClicked(index, patientId, professionalId, appointmentId) },
                                modifier = Modifier.fillMaxWidth(0.8f).height(72.dp).padding(vertical = 4.dp),
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = when {
                                        item.isFound -> Color(0xFFC8E6C9)
                                        item.isWrong -> Color(0xFFFFCDD2)
                                        else -> MaterialTheme.colorScheme.primaryContainer
                                    },
                                    contentColor = if (item.isFound || item.isWrong) Color.Black else MaterialTheme.colorScheme.onPrimaryContainer
                                ),
                                border = if (item.isFound) BorderStroke(3.dp, Color(0xFF4CAF50)) else if (item.isWrong) BorderStroke(3.dp, Color.Red) else null
                            ) {
                                Text(item.content.toString(), fontSize = 24.sp, fontWeight = FontWeight.ExtraBold)
                            }
                        }
                    }
                } else {
                    ResponsiveGrid(items = state.items, columns = state.gridSize) { index, item, size ->
                        VisualAttentionItemCard(
                            item = item,
                            size = size,
                            onClick = { viewModel.onItemClicked(index, patientId, professionalId, appointmentId) }
                        )
                    }
                }
            }

            if (state.isCountingPhase) {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "¿Cuántos hay en total?",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            state.numericOptions.forEach { option ->
                                FilledTonalButton(
                                    onClick = { viewModel.onNumericOptionSelected(option, patientId, professionalId, appointmentId) },
                                    modifier = Modifier.size(width = 80.dp, height = 60.dp),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text(option.toString(), fontSize = 22.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }

            // Feedback and Next Button
            AnimatedVisibility(visible = state.isCompleted) {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFC8E6C9))
                ) {
                    Text(
                        text = state.feedbackMessage ?: "¡Muy bien hecho!",
                        modifier = Modifier.padding(16.dp).fillMaxWidth(),
                        textAlign = TextAlign.Center,
                        color = Color(0xFF1B5E20),
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Button(
                onClick = { viewModel.startNewGame(state.variation, state.currentLevel) },
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
fun TargetDisplayCard(content: Any, isRealImage: Boolean = false) {
    Surface(
        modifier = Modifier.size(80.dp),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.primaryContainer,
        tonalElevation = 4.dp
    ) {
        Box(contentAlignment = Alignment.Center) {
            ItemContent(content = content, size = 48.dp, color = MaterialTheme.colorScheme.onPrimaryContainer, isRealImage = isRealImage)
        }
    }
}

@Composable
fun VisualAttentionItemCard(
    item: VisualAttentionItem,
    size: androidx.compose.ui.unit.Dp,
    onClick: () -> Unit
) {
    val borderColor = when {
        item.isFound -> Color(0xFF4CAF50)
        item.isWrong -> Color(0xFFF44336)
        else -> Color.Transparent
    }

    val containerColor = when {
        item.isFound -> Color(0xFFE8F5E9)
        item.isWrong -> Color(0xFFFFEBEE)
        else -> MaterialTheme.colorScheme.surface
    }

    Surface(
        onClick = onClick,
        modifier = Modifier.size(size),
        shape = RoundedCornerShape(16.dp),
        color = containerColor,
        tonalElevation = 2.dp,
        border = if (borderColor != Color.Transparent) BorderStroke(4.dp, borderColor) else null
    ) {
        Box(contentAlignment = Alignment.Center) {
            ItemContent(
                content = item.content,
                size = size * 0.7f,
                color = if (item.isFound) Color(0xFF2E7D32) else MaterialTheme.colorScheme.onSurface,
                isRealImage = item.isRealImage
            )
        }
    }
}

@Composable
fun ItemContent(content: Any, size: androidx.compose.ui.unit.Dp, color: Color, isRealImage: Boolean = false) {
    if (isRealImage && content is String) {
        KamelImage(
            resource = { asyncPainterResource(content) },
            contentDescription = null,
            modifier = Modifier.size(size).clip(RoundedCornerShape(8.dp)),
            onLoading = { CircularProgressIndicator(modifier = Modifier.size(size * 0.5f)) },
            onFailure = { Icon(Icons.Default.BrokenImage, null, tint = color) }
        )
    } else {
        when (content) {
            is ImageVector -> {
                Icon(imageVector = content, contentDescription = null, modifier = Modifier.size(size), tint = color)
            }
            is Char -> {
                Text(text = content.toString(), fontSize = (size.value * 0.8f).sp, fontWeight = FontWeight.Black, color = color)
            }
            is String -> {
                Text(text = content, fontSize = (size.value * 0.6f).sp, fontWeight = FontWeight.Black, color = color, textAlign = TextAlign.Center)
            }
            is Int -> {
                Text(text = content.toString(), fontSize = (size.value * 0.7f).sp, fontWeight = FontWeight.Black, color = color)
            }
            is Pair<*, *> -> {
                val icon = content.first as ImageVector
                val rotation = content.second as Float
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(size).rotate(rotation),
                    tint = color
                )
            }
            is Float -> {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = null,
                    modifier = Modifier.size(size).rotate(content),
                    tint = color
                )
            }
        }
    }
}
