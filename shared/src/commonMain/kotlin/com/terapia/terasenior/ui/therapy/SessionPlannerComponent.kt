package com.terapia.terasenior.ui.therapy

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
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
    
    val gamesByCategory = mapOf(
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
        ),
        "Memoria" to listOf(
            "memory_cultural" to "Memoria Cultural",
            "memory_utility" to "Utilidad de Objetos",
            "memory_needs" to "Necesidades de Tarea",
            "memory_recent" to "Memoria Reciente"
        ),
        "Lenguaje" to listOf(
            "language_word_image" to "Vocabulario: Imagen",
            "language_denomination" to "Denominación",
            "language_semantic_category" to "Categorías",
            "language_start_letter" to "Primera Letra",
            "language_start_syllable" to "Primera Sílaba"
        ),
        "Funciones Ejecutivas" to listOf(
            "executive_color_shape_sequence" to "Secuencias Lógicas",
            "executive_planning_steps" to "Planificación AVD",
            "executive_money_calculation" to "Dinero",
            "executive_time_logic" to "Tiempo",
            "executive_logical_reasoning" to "Razonamiento"
        ),
        "Percepción" to listOf(
            "perception_color_identification" to "Identificación Colores",
            "perception_size_ordering" to "Orden Tamaños",
            "perception_lateral_dominance" to "Lateralidad",
            "perception_mirror" to "Espejo",
            "perception_body_parts" to "Cuerpo Humano"
        ),
        "Lectoescritura" to listOf(
            "literacy_tracing_basic" to "Trazos",
            "literacy_complete_letters" to "Completar Letras",
            "literacy_copy_words" to "Copia Palabras",
            "literacy_form_shapes" to "Formas"
        )
    )

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Planificador de Actividades", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
        ) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                // Selector de Categoría
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Categoría:", style = MaterialTheme.typography.labelMedium, modifier = Modifier.width(80.dp))
                    ScrollableTabRow(
                        selectedTabIndex = categories.indexOf(selectedCategory).coerceAtLeast(0),
                        edgePadding = 0.dp,
                        containerColor = Color.Transparent,
                        divider = {}
                    ) {
                        categories.forEach { cat ->
                            Tab(
                                selected = selectedCategory == cat,
                                onClick = { 
                                    selectedCategory = cat
                                    selectedGameType = gamesByCategory[cat]?.first()?.first ?: ""
                                },
                                text = { Text(cat, fontSize = 11.sp) }
                            )
                        }
                    }
                }

                // Selector de Juego
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Ejercicio:", style = MaterialTheme.typography.labelMedium, modifier = Modifier.width(80.dp))
                    var expanded by remember { mutableStateOf(false) }
                    Box(modifier = Modifier.weight(1f)) {
                        OutlinedButton(
                            onClick = { expanded = true },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(gamesByCategory[selectedCategory]?.find { it.first == selectedGameType }?.second ?: "Seleccionar...")
                        }
                        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                            gamesByCategory[selectedCategory]?.forEach { (type, name) ->
                                DropdownMenuItem(
                                    text = { Text(name) },
                                    onClick = {
                                        selectedGameType = type
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                // Selector de Nivel
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Nivel:", style = MaterialTheme.typography.labelMedium, modifier = Modifier.width(80.dp))
                    (1..5).forEach { level ->
                        FilterChip(
                            selected = selectedLevel == level,
                            onClick = { selectedLevel = level },
                            label = { Text(level.toString()) },
                            modifier = Modifier.padding(end = 4.dp)
                        )
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    Button(
                        onClick = {
                            val gameName = gamesByCategory[selectedCategory]?.find { it.first == selectedGameType }?.second ?: "Juego"
                            onAddExercise(ExerciseConfig(selectedGameType, gameName, selectedCategory, selectedLevel))
                        },
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                        Text("Agregar", fontSize = 12.sp)
                    }
                }
            }
        }

        // Lista de Actividades Programadas
        if (plannedExercises.isNotEmpty()) {
            Text("Sesión Programada:", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    plannedExercises.forEachIndexed { index, config ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(8.dp))
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(config.name, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                                Text("${config.category} • Nivel ${config.level}", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                            }
                            IconButton(onClick = { onRemoveExercise(index) }, modifier = Modifier.size(24.dp)) {
                                Icon(Icons.Default.Delete, contentDescription = "Quitar", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
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
