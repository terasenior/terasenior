package com.terapia.terasenior.ui.therapy

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.terapia.terasenior.domain.model.therapy.ExerciseConfig

// SessionPlannerComponent v1.3.19 - Planificador con Scroll y Ordenación
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SessionPlannerComponent(
    plannedExercises: List<ExerciseConfig>,
    onAddExercise: (ExerciseConfig) -> Unit,
    onRemoveExercise: (Int) -> Unit
) {
    var selectedCategory by remember { mutableStateOf("Atención") }
    var selectedGameType by remember { mutableStateOf("number_search") }
    var selectedLevel by remember { mutableStateOf(1) }

    val categories = listOf("Orientación", "Atención", "Memoria", "Lenguaje", "Funciones Ejecutivas", "Percepción", "Lectoescritura")
    
    // Juegos ordenados por nombre (v1.3.19)
    val gamesByCategory = remember {
        mapOf(
            "Orientación" to listOf("orientation_temporal" to "Orientación Temporal"),
            "Atención" to listOf(
                "number_search" to "Busca el Número",
                "attention_different" to "El Intruso",
                "attention_equals_model" to "Buscar Iguales",
                "attention_positions" to "Orientación Espacial",
                "attention_letters" to "Letras",
                "attention_numbers" to "Números",
                "attention_symbols" to "Símbolos",
                "attention_matrices" to "Matrices",
                "attention_row_cancel" to "Tachado por Filas",
                "attention_consecutive" to "Números Consecutivos",
                "attention_yes_no" to "Tachar Sí/No",
                "attention_dual_task" to "Tarea Dual",
                "attention_count" to "Contar Dibujos",
                "attention_word_search" to "Sopa de Letras"
            ).sortedBy { it.second },
            "Memoria" to listOf(
                "memory_cultural" to "Memoria Cultural",
                "memory_utility" to "Utilidad de Objetos",
                "memory_needs" to "Necesidades de Tarea",
                "memory_recent" to "Memoria Reciente"
            ).sortedBy { it.second },
            "Lenguaje" to listOf(
                "language_word_image" to "Vocabulario: Imagen",
                "language_denomination" to "Denominación",
                "language_semantic_category" to "Categorías",
                "language_start_letter" to "Primera Letra",
                "language_start_syllable" to "Primera Sílaba"
            ).sortedBy { it.second },
            "Funciones Ejecutivas" to listOf(
                "executive_color_shape_sequence" to "Secuencias Lógicas",
                "executive_planning_steps" to "Planificación AVD",
                "executive_money_calculation" to "Dinero",
                "executive_time_logic" to "Tiempo",
                "executive_logical_reasoning" to "Razonamiento"
            ).sortedBy { it.second },
            "Percepción" to listOf(
                "perception_color_identification" to "Identificación Colores",
                "perception_size_ordering" to "Orden Tamaños",
                "perception_lateral_dominance" to "Lateralidad",
                "perception_mirror" to "Espejo",
                "perception_body_parts" to "Cuerpo Humano",
                "perception_shape_fitting" to "Encaje de Formas"
            ).sortedBy { it.second },
            "Lectoescritura" to listOf(
                "literacy_tracing_basic" to "Trazos",
                "literacy_complete_letters" to "Completar Letras",
                "literacy_copy_words" to "Copia Palabras",
                "literacy_form_shapes" to "Formas"
            ).sortedBy { it.second }
        )
    }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Planificador de Actividades", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
        ) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                
                // Selector de Categoría (v1.3.19 - LazyRow para desplazamiento fluido)
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("1. Selecciona Categoría:", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(vertical = 4.dp)
                    ) {
                        items(categories) { cat ->
                            FilterChip(
                                selected = selectedCategory == cat,
                                onClick = { 
                                    selectedCategory = cat
                                    selectedGameType = gamesByCategory[cat]?.first()?.first ?: ""
                                },
                                label = { Text(cat, fontSize = 11.sp) },
                                shape = RoundedCornerShape(8.dp)
                            )
                        }
                    }
                }

                // Selector de Juego
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("2. Elige el Ejercicio:", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                    var expanded by remember { mutableStateOf(false) }
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedButton(
                            onClick = { expanded = true },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.outlinedButtonColors(containerColor = Color.White)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                                Text(
                                    text = gamesByCategory[selectedCategory]?.find { it.first == selectedGameType }?.second ?: "Seleccionar...",
                                    modifier = Modifier.weight(1f)
                                )
                                Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                            }
                        }
                        DropdownMenu(
                            expanded = expanded, 
                            onDismissRequest = { expanded = false },
                            modifier = Modifier.fillMaxWidth(0.8f).heightIn(max = 300.dp)
                        ) {
                            gamesByCategory[selectedCategory]?.forEach { (type, name) ->
                                DropdownMenuItem(
                                    text = { Text(name, fontSize = 14.sp) },
                                    onClick = {
                                        selectedGameType = type
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                // Selector de Nivel y Botón Agregar
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("3. Nivel:", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                        Row(modifier = Modifier.padding(top = 4.dp)) {
                            (1..5).forEach { level ->
                                FilterChip(
                                    selected = selectedLevel == level,
                                    onClick = { selectedLevel = level },
                                    label = { Text(level.toString()) },
                                    modifier = Modifier.padding(end = 4.dp)
                                )
                            }
                        }
                    }

                    Button(
                        onClick = {
                            val gameName = gamesByCategory[selectedCategory]?.find { it.first == selectedGameType }?.second ?: "Juego"
                            onAddExercise(ExerciseConfig(selectedGameType, gameName, selectedCategory, selectedLevel))
                        },
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Agregar", fontSize = 12.sp)
                    }
                }
            }
        }

        // Lista de Actividades Programadas (v1.3.19 - Con Scroll si hay muchas)
        if (plannedExercises.isNotEmpty()) {
            Text("Plan de Sesión (${plannedExercises.size}):", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
            Box(modifier = Modifier.fillMaxWidth().heightIn(max = 200.dp)) {
                val listScrollState = rememberScrollState()
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(listScrollState),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    plannedExercises.forEachIndexed { index, config ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = CardDefaults.outlinedCardBorder()
                        ) {
                            Row(
                                modifier = Modifier.padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(config.name, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                                    Text("${config.category} • Nivel ${config.level}", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                                }
                                IconButton(onClick = { onRemoveExercise(index) }, modifier = Modifier.size(32.dp)) {
                                    Icon(Icons.Default.Delete, contentDescription = "Quitar", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                    }
                }
            }
        } else {
            Text("No hay actividades en el plan.", style = MaterialTheme.typography.bodySmall, color = Color.Gray, modifier = Modifier.padding(vertical = 8.dp))
        }
    }
}
