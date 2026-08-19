package com.terapia.terasenior.ui.therapy

import androidx.compose.foundation.BorderStroke
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
import com.terapia.terasenior.ui.therapy.ExerciseTranslationUtils
import com.terapia.terasenior.domain.model.therapy.ExerciseConfig

// SessionPlannerComponent v1.3.26 - Planificador Multi-Ejercicio
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SessionPlannerComponent(
    plannedExercises: List<ExerciseConfig>,
    onAddExercise: (ExerciseConfig) -> Unit,
    onRemoveExercise: (Int) -> Unit
) {
    var selectedCategory by remember { mutableStateOf("Orientación") }
    var selectedGameType by remember { mutableStateOf("orientation_temporal_day") }
    var selectedLevel by remember { mutableStateOf(1) }

    val categories = listOf("Orientación", "Atención", "Memoria", "Lenguaje", "Funciones Ejecutivas", "Percepción", "Lectoescritura")
    
    // Juegos ordenados por nombre (v1.3.26)
    val gamesByCategory: Map<String, List<Pair<String, String>>> = remember {
        mapOf(
            "Orientación" to getOrientationPoolForPlanner(),
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
                "executive_logical_reasoning" to "Razonamiento",
                "calculation_simple" to "Cálculos Sencillos"
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

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
        ) {
            Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                
                // Selector de Categoría
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text("1. Categoría:", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        contentPadding = PaddingValues(vertical = 2.dp)
                    ) {
                        items(categories) { cat ->
                            FilterChip(
                                selected = selectedCategory == cat,
                                onClick = { 
                                    selectedCategory = cat
                                    selectedGameType = gamesByCategory[cat]?.firstOrNull()?.first ?: ""
                                },
                                label = { Text(cat, fontSize = 10.sp) },
                                shape = RoundedCornerShape(6.dp)
                            )
                        }
                    }
                }

                // Selector de Juego
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text("2. Ejercicio:", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                    var expanded by remember { mutableStateOf(false) }
                    var searchText by remember { mutableStateOf("") }
                    val filteredGames = remember(selectedCategory, searchText) {
                        val games = gamesByCategory[selectedCategory] ?: emptyList()
                        if (searchText.isBlank()) games
                        else games.filter { it.second.contains(searchText, ignoreCase = true) }
                    }

                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedButton(
                            onClick = { expanded = true },
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.outlinedButtonColors(containerColor = Color.White),
                            contentPadding = PaddingValues(horizontal = 12.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                                Text(
                                    text = gamesByCategory[selectedCategory]?.find { it.first == selectedGameType }?.second ?: "Seleccionar...",
                                    modifier = Modifier.weight(1f),
                                    style = MaterialTheme.typography.bodySmall,
                                    maxLines = 1
                                )
                                Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                            }
                        }
                        DropdownMenu(
                            expanded = expanded, 
                            onDismissRequest = { expanded = false },
                            modifier = Modifier.fillMaxWidth(0.9f).heightIn(max = 400.dp)
                        ) {
                            // Buscador dentro del dropdown (v1.3.26)
                            OutlinedTextField(
                                value = searchText,
                                onValueChange = { searchText = it },
                                placeholder = { Text("Filtrar...", fontSize = 12.sp) },
                                modifier = Modifier.fillMaxWidth().padding(8.dp).height(48.dp),
                                shape = RoundedCornerShape(8.dp),
                                singleLine = true,
                                textStyle = MaterialTheme.typography.bodySmall
                            )
                            
                            filteredGames.forEach { (type, name) ->
                                DropdownMenuItem(
                                    text = { Text(name, fontSize = 13.sp) },
                                    onClick = {
                                        selectedGameType = type
                                        expanded = false
                                        searchText = ""
                                    }
                                )
                            }
                            
                            if (filteredGames.isEmpty()) {
                                Text("No se encontraron resultados", modifier = Modifier.padding(16.dp), style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                            }
                        }
                    }
                }

                // Selector de Nivel y Botón Agregar
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("3. Nivel GDS:", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                        Row(modifier = Modifier.padding(top = 2.dp)) {
                            (1..5).forEach { level ->
                                FilterChip(
                                    selected = selectedLevel == level,
                                    onClick = { selectedLevel = level },
                                    label = { Text(ExerciseTranslationUtils.getGdsLabel(level), fontSize = 9.sp) },
                                    modifier = Modifier.padding(end = 4.dp),
                                    shape = RoundedCornerShape(6.dp)
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
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Añadir", fontSize = 11.sp)
                    }
                }
            }
        }

        // Lista de Actividades Programadas
        if (plannedExercises.isNotEmpty()) {
            Text("Plan (${plannedExercises.size}):", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
            Box(modifier = Modifier.fillMaxWidth().heightIn(max = 180.dp)) {
                val listScrollState = rememberScrollState()
                Column(
                    modifier = Modifier.fillMaxWidth().verticalScroll(listScrollState),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    plannedExercises.forEachIndexed { index, config ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                        ) {
                            Row(modifier = Modifier.padding(6.dp), verticalAlignment = Alignment.CenterVertically) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(config.name, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                                    Text("${config.category} • ${ExerciseTranslationUtils.getGdsLabel(config.level)}", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                                }
                                IconButton(onClick = { onRemoveExercise(index) }, modifier = Modifier.size(28.dp)) {
                                    Icon(Icons.Default.Delete, contentDescription = "Quitar", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun getOrientationPoolForPlanner(): List<Pair<String, String>> {
    return listOf(
        "orientation_temporal_day" to "Día del mes",
        "orientation_temporal_month" to "Mes del año",
        "orientation_temporal_year" to "Año actual",
        "orientation_temporal_season" to "Estación del año",
        "orientation_temporal_dayweek" to "Día de la semana",
        "orientation_temporal_partday" to "Parte del día",
        "orientation_temporal_hour" to "Hora aproximada",
        "orientation_temporal_century" to "Siglo actual",
        "orientation_temporal_decade" to "Década actual",
        "orientation_temporal_yesterday" to "Día ayer",
        "orientation_temporal_tomorrow" to "Día mañana",
        "orientation_spatial_city" to "Ciudad o Pueblo",
        "orientation_spatial_province" to "Provincia",
        "orientation_spatial_country" to "País",
        "orientation_spatial_continent" to "Continente",
        "orientation_spatial_planet" to "Planeta",
        "orientation_spatial_place" to "Lugar actual",
        "orientation_spatial_floor" to "Planta edificio",
        "orientation_spatial_kitchen" to "Uso de cocina",
        "orientation_spatial_bedroom" to "Uso de dormitorio",
        "orientation_spatial_library" to "Uso de biblioteca",
        "orientation_spatial_pharmacy" to "Uso de farmacia",
        "orientation_spatial_bakery" to "Uso de panadería",
        "orientation_personal_name" to "Nombre propio",
        "orientation_calc_year_days" to "Cálculo días año",
        "orientation_calc_year_months" to "Cálculo meses año",
        "orientation_calc_week_days" to "Cálculo días semana",
        "orientation_calc_day_hours" to "Cálculo horas día",
        "orientation_calc_minutes_hour" to "Cálculo minutos hora",
        "orientation_calc_seconds_minute" to "Cálculo segundos minuto",
        "orientation_calc_half_day" to "Cálculo medio día",
        "orientation_calc_feet_count" to "Cálculo pies",
        "orientation_calc_hands_count" to "Cálculo manos",
        "orientation_calc_fingers_hand" to "Cálculo dedos mano",
        "orientation_calc_fingers_total" to "Cálculo dedos total",
        "orientation_calc_dozen" to "Cálculo docena",
        "orientation_calc_half_dozen" to "Cálculo media docena",
        "orientation_calc_century_years" to "Cálculo años siglo",
        "orientation_calc_decade_years" to "Cálculo años década",
        "orientation_situational_currency" to "Moneda actual",
        "orientation_situational_language" to "Idioma hablado",
        "orientation_situational_color_sky" to "Color del cielo",
        "orientation_situational_color_grass" to "Color de hierba",
        "orientation_situational_king" to "Rey actual",
        "orientation_situational_blood" to "Color sangre",
        "orientation_situational_fire" to "Sensación fuego",
        "orientation_situational_ice" to "Sensación hielo",
        "orientation_situational_sun" to "Salida del sol",
        "orientation_situational_snow_color" to "Color nieve",
        "orientation_situational_lemon_taste" to "Sabor limón",
        "orientation_situational_sugar_taste" to "Sabor azúcar",
        "orientation_situational_sea_water" to "Sabor agua mar",
        "orientation_situational_eyes_count" to "Número de ojos",
        "orientation_situational_ears_count" to "Número de orejas",
        "orientation_situational_nose_count" to "Número de narices",
        "orientation_situational_mouth_count" to "Número de bocas",
        "orientation_situational_head_count" to "Número de cabezas",
        "orientation_situational_arms_count" to "Número de brazos",
        "orientation_situational_legs_count" to "Número de piernas",
        "orientation_situational_hair_color" to "Color de pelo",
        "orientation_situational_sun_shape" to "Forma del sol",
        "orientation_situational_ball_shape" to "Forma de balón",
        "orientation_situational_table_use" to "Uso de mesa",
        "orientation_situational_knife_use" to "Uso de cuchillo",
        "orientation_situational_spoon_use" to "Uso de cuchara",
        "orientation_situational_comb_use" to "Uso de peine",
        "orientation_situational_soap_use" to "Uso de jabón",
        "orientation_situational_towel_use" to "Uso de toalla",
        "orientation_situational_broom_use" to "Uso de escoba",
        "orientation_situational_oven_use" to "Uso de horno",
        "orientation_situational_pill_use" to "Uso de medicinas",
        "orientation_situational_phone_use" to "Uso de teléfono",
        "orientation_situational_keys_use" to "Uso de llaves",
        "orientation_situational_glasses_use" to "Uso de gafas",
        "orientation_situational_watch_use" to "Uso de reloj",
        "orientation_situational_wallet_use" to "Uso de cartera",
        "orientation_situational_calendar_use" to "Uso de calendario",
        "orientation_situational_stop_color" to "Color señal STOP",
        "orientation_situational_zebra_cross" to "Color paso cebra",
        "orientation_situational_traffic_light_go" to "Semaforo verde",
        "orientation_situational_traffic_light_stop" to "Semaforo rojo",
        "orientation_situational_dog_sound" to "Sonido perro",
        "orientation_situational_cat_sound" to "Sonido gato",
        "orientation_situational_cow_sound" to "Sonido vaca",
        "orientation_situational_sheep_sound" to "Sonido oveja",
        "orientation_situational_dentist" to "Médico de dientes",
        "orientation_situational_umbrella" to "Uso paraguas",
        "orientation_situational_shoes_wear" to "Donde van zapatos",
        "orientation_situational_hat_wear" to "Donde va sombrero",
        "orientation_situational_gloves_wear" to "Donde van guantes",
        "orientation_situational_fridge_use" to "Uso frigorífico",
        "orientation_situational_chair_use" to "Uso silla",
        "orientation_situational_bed_use" to "Uso cama"
    ).sortedBy { it.second }
}
