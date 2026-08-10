package com.terapia.terasenior.treatment.ui

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.terapia.terasenior.ui.components.accessibility.SpeechManager
import io.kamel.image.KamelImage
import io.kamel.image.asyncPainterResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LanguageGame(
    viewModel: LanguageViewModel,
    patientId: String?,
    professionalId: String?,
    appointmentId: String?,
    onBack: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val speechManager = remember { SpeechManager() }
    val isMultipleChoice = state.type == "language_denomination" || state.type == "language_semantic_naming"

    // Voz automática al cargar el ejercicio o cambiar el prompt
    LaunchedEffect(state.instruction, state.prompt) {
        if (state.instruction.isNotEmpty()) {
            speechManager.speak(state.instruction)
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
            modifier = Modifier.fillMaxSize().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Cabecera con botón de cierre uniforme
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(
                    onClick = onBack,
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.8f)
                    )
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = MaterialTheme.colorScheme.error)
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Lenguaje y Evocación",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = state.type.replace("language_", "").replace("_", " ").uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                IconButton(onClick = { speechManager.speak(state.instruction) }) {
                    Icon(Icons.AutoMirrored.Filled.VolumeUp, contentDescription = "Repetir instrucción")
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Área de Contenido Principal
            Box(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                if (isMultipleChoice) {
                    MultipleChoiceLayout(state, viewModel, patientId, professionalId, appointmentId)
                } else {
                    TextEntryLayout(state, viewModel, patientId, professionalId, appointmentId)
                }
            }

            // Feedback Visual de Resultado
            AnimatedVisibility(
                visible = state.isCorrect != null,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                val (color, message) = if (state.isCorrect == true) {
                    Color(0xFF4CAF50) to "¡Excelente!"
                } else {
                    MaterialTheme.colorScheme.error to "Inténtalo de nuevo"
                }
                
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                    colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f)),
                    border = androidx.compose.foundation.BorderStroke(2.dp, color)
                ) {
                    Text(
                        text = message,
                        modifier = Modifier.padding(16.dp).fillMaxWidth(),
                        textAlign = TextAlign.Center,
                        color = color,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                }
            }

            // Botón para siguiente o reintentar (si completado)
            if (state.isCompleted) {
                Button(
                    onClick = { viewModel.startNewGame(state.type, state.currentLevel) },
                    modifier = Modifier.fillMaxWidth().height(64.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("Siguiente Ejercicio", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun TextEntryLayout(
    state: LanguageUiState,
    viewModel: LanguageViewModel,
    patientId: String?,
    professionalId: String?,
    appointmentId: String?
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxWidth()
    ) {
        Card(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            shape = RoundedCornerShape(32.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f))
        ) {
            Column(
                modifier = Modifier.padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (state.prompt.isNotEmpty()) {
                    Text(
                        text = state.prompt,
                        style = MaterialTheme.typography.displayLarge.copy(fontWeight = FontWeight.Black),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }
                Text(
                    text = state.instruction,
                    textAlign = TextAlign.Center,
                    style = if (state.prompt.isEmpty()) MaterialTheme.typography.headlineMedium else MaterialTheme.typography.titleMedium,
                    fontWeight = if (state.prompt.isEmpty()) FontWeight.Bold else FontWeight.Normal
                )
            }
        }

        Spacer(modifier = Modifier.height(48.dp))

        OutlinedTextField(
            value = state.userInput,
            onValueChange = { viewModel.onUserInputChange(it) },
            label = { Text("Escribe aquí tu respuesta") },
            modifier = Modifier.fillMaxWidth(0.8f),
            textStyle = MaterialTheme.typography.headlineMedium.copy(textAlign = TextAlign.Center),
            singleLine = true,
            shape = RoundedCornerShape(20.dp),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { viewModel.validateInput(patientId, professionalId, appointmentId) }),
            trailingIcon = {
                if (state.userInput.isNotEmpty()) {
                    IconButton(onClick = { viewModel.validateInput(patientId, professionalId, appointmentId) }) {
                        Icon(Icons.Default.Check, contentDescription = "Validar", tint = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        )
    }
}

@Composable
private fun MultipleChoiceLayout(
    state: LanguageUiState,
    viewModel: LanguageViewModel,
    patientId: String?,
    professionalId: String?,
    appointmentId: String?
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxSize()
    ) {
        // Área de Imágenes
        Card(
            modifier = Modifier.size(280.dp),
            shape = RoundedCornerShape(40.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                if (state.type == "language_denomination") {
                    if (!state.imageUrl.isNullOrBlank()) {
                        KamelImage(
                            resource = { asyncPainterResource(state.imageUrl) },
                            contentDescription = "Objeto",
                            modifier = Modifier.fillMaxSize().padding(16.dp).clip(RoundedCornerShape(24.dp)),
                            onLoading = { CircularProgressIndicator() },
                            onFailure = { Icon(Icons.Default.Close, null, modifier = Modifier.size(120.dp), tint = Color.Gray) }
                        )
                    } else if (state.icon != null) {
                        Icon(
                            imageVector = state.icon,
                            contentDescription = null,
                            modifier = Modifier.size(160.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                } else if (state.type == "language_semantic_naming") {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        state.images.forEach { img ->
                            Icon(img, contentDescription = null, modifier = Modifier.size(64.dp), tint = Color.Gray)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(48.dp))

        // Cuadrícula de opciones (v1.3.3 - Responsive)
        Box(modifier = Modifier.fillMaxWidth().heightIn(max = 240.dp)) {
            ResponsiveGrid(items = state.options, columns = 2, spacing = 16.dp) { _, option, size ->
                val isSelected = state.isCompleted && option == state.targetValue
                val isWrong = state.isCorrect == false && option == state.userInput
                
                Button(
                    onClick = { viewModel.onOptionSelected(option, patientId, professionalId, appointmentId) },
                    modifier = Modifier.height(size),
                    shape = RoundedCornerShape(24.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = when {
                            isSelected -> Color(0xFF4CAF50)
                            isWrong -> MaterialTheme.colorScheme.error
                            else -> MaterialTheme.colorScheme.surfaceVariant
                        },
                        contentColor = if (isSelected || isWrong) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
                ) {
                    Text(option, fontSize = 18.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                }
            }
        }
    }
}
