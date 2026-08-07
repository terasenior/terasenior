package com.terapia.terasenior.treatment.ui

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.terapia.terasenior.ui.components.accessibility.SpeechManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExecutiveFunctionsGame(
    viewModel: ExecutiveFunctionsViewModel,
    patientId: String?,
    professionalId: String?,
    appointmentId: String?,
    onBack: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val speechManager = remember { SpeechManager() }

    LaunchedEffect(state.mode) {
        if (state.voiceInstruction.isNotEmpty()) {
            speechManager.speak(state.voiceInstruction)
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
            // Navigation Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = MaterialTheme.colorScheme.error)
                }
                Text(
                    text = "Funciones Ejecutivas",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )
                IconButton(onClick = { speechManager.speak(state.voiceInstruction) }) {
                    Icon(Icons.AutoMirrored.Filled.VolumeUp, contentDescription = "Repetir")
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Instruction
            Text(
                text = state.instruction,
                style = MaterialTheme.typography.headlineSmall,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Game Content
            Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.TopCenter) {
                when (state.mode) {
                    "executive_planning_steps" -> PlanningStepsUI(state, viewModel)
                    "executive_shopping_list", "executive_logical_reasoning", "executive_analogies", "executive_intrusos" -> MultipleChoiceUI(state, viewModel)
                    "executive_money_calculation", "executive_time_logic", "executive_math_advanced" -> NumericInputUI(state, viewModel)
                    "executive_abstractions" -> AbstractionsUI(state, viewModel)
                }
            }

            // Success/Error Feedback
            if (state.isCorrect == false) {
                Text("Vuelve a intentarlo", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Next Button
            AnimatedVisibility(visible = state.isCompleted) {
                Button(
                    onClick = { viewModel.saveAndFinish(patientId, professionalId, appointmentId) },
                    modifier = Modifier.fillMaxWidth().height(56.dp).widthIn(max = 400.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                ) {
                    Text("Continuar", fontSize = 20.sp)
                }
            }
        }
    }
}

@Composable
private fun PlanningStepsUI(state: ExecutiveFunctionsUiState, viewModel: ExecutiveFunctionsViewModel) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        LazyColumn(
            modifier = Modifier.widthIn(max = 500.dp).weight(1f),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            itemsIndexed(state.items) { index, item ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${index + 1}.",
                            style = MaterialTheme.typography.titleLarge,
                            modifier = Modifier.width(32.dp)
                        )
                        Text(
                            text = item,
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.weight(1f)
                        )
                        
                        Column {
                            IconButton(
                                onClick = {
                                    if (index > 0) {
                                        val newList = state.items.toMutableList()
                                        val temp = newList[index]
                                        newList[index] = newList[index - 1]
                                        newList[index - 1] = temp
                                        viewModel.onReorder(newList)
                                    }
                                },
                                enabled = index > 0
                            ) {
                                Icon(Icons.Default.ArrowUpward, contentDescription = "Subir")
                            }
                            IconButton(
                                onClick = {
                                    if (index < state.items.size - 1) {
                                        val newList = state.items.toMutableList()
                                        val temp = newList[index]
                                        newList[index] = newList[index + 1]
                                        newList[index + 1] = temp
                                        viewModel.onReorder(newList)
                                    }
                                },
                                enabled = index < state.items.size - 1
                            ) {
                                Icon(Icons.Default.ArrowDownward, contentDescription = "Bajar")
                            }
                        }
                    }
                }
            }
        }
        
        Button(
            onClick = { viewModel.onCheckPlanning() },
            modifier = Modifier.padding(16.dp).height(56.dp).width(200.dp)
        ) {
            Text("Comprobar")
        }
    }
}

@Composable
private fun MultipleChoiceUI(state: ExecutiveFunctionsUiState, viewModel: ExecutiveFunctionsViewModel) {
    Column(
        modifier = Modifier.widthIn(max = 600.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (state.question.isNotEmpty()) {
            Text(
                text = state.question,
                style = MaterialTheme.typography.headlineMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        state.options.forEach { option ->
            Button(
                onClick = { viewModel.onOptionSelected(option) },
                modifier = Modifier.fillMaxWidth().height(72.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                )
            ) {
                Text(option, fontSize = 20.sp, textAlign = TextAlign.Center)
            }
        }
    }
}

@Composable
private fun NumericInputUI(state: ExecutiveFunctionsUiState, viewModel: ExecutiveFunctionsViewModel) {
    var text by remember { mutableStateOf("") }
    
    Column(
        modifier = Modifier.widthIn(max = 500.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Text(
            text = state.question,
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Bold
        )

        OutlinedTextField(
            value = text,
            onValueChange = { text = it },
            label = { Text("Tu respuesta") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            shape = RoundedCornerShape(16.dp),
            textStyle = MaterialTheme.typography.headlineMedium.copy(textAlign = TextAlign.Center)
        )

        Button(
            onClick = { viewModel.onAnswerInput(text) },
            modifier = Modifier.height(56.dp).width(200.dp)
        ) {
            Text("Comprobar")
        }
    }
}

@Composable
private fun AbstractionsUI(state: ExecutiveFunctionsUiState, viewModel: ExecutiveFunctionsViewModel) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        // Key
        Card(
            modifier = Modifier.fillMaxWidth().widthIn(max = 600.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f))
        ) {
            Row(
                modifier = Modifier.padding(16.dp).fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                state.symbolKey.forEach { (symbol, value) ->
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(symbol, fontSize = 32.sp)
                        Text(value.toString(), fontWeight = FontWeight.Bold, fontSize = 24.sp)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Sequence to translate
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            state.symbolSequence.forEachIndexed { index, symbol ->
                Column(
                    modifier = Modifier.padding(8.dp).width(80.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(symbol, fontSize = 40.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    var input by remember { mutableStateOf("") }
                    
                    OutlinedTextField(
                        value = input,
                        onValueChange = { 
                            if (it.length <= 1) {
                                input = it
                                it.toIntOrNull()?.let { valInt ->
                                    viewModel.onAbstractionInput(index, valInt)
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        textStyle = MaterialTheme.typography.bodyLarge.copy(textAlign = TextAlign.Center)
                    )
                }
            }
        }
    }
}
