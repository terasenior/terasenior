package com.terapia.terasenior.ui.therapy

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.terapia.terasenior.domain.model.patient.Patient
import com.terapia.terasenior.domain.model.therapy.ExerciseConfig
import com.terapia.terasenior.domain.model.therapy.SessionMode
import com.terapia.terasenior.ui.therapy.ExerciseTranslationUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateSessionWizard(
    viewModel: CreateSessionViewModel,
    therapistId: String,
    onSessionCreated: (String) -> Unit,
    onCancel: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.createdSessionId) {
        uiState.createdSessionId?.let { onSessionCreated(it) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Nueva Sesión") },
                navigationIcon = {
                    IconButton(onClick = {
                        if (uiState.currentStep == WizardStep.MODE_SELECTION) onCancel()
                        else viewModel.goBack()
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when (uiState.currentStep) {
                WizardStep.MODE_SELECTION -> ModeSelectionStep(onModeSelected = viewModel::onModeSelected)
                WizardStep.PATIENT_SELECTION -> PatientSelectionStep(
                    patients = uiState.patients,
                    isLoading = uiState.isLoading,
                    onPatientSelected = viewModel::onPatientSelected
                )
                WizardStep.CATEGORY_SELECTION -> CategorySelectionStep(onCategorySelected = viewModel::onCategorySelected)
                WizardStep.EXERCISE_SELECTION -> ExerciseSelectionStep(
                    category = uiState.selectedCategory ?: "",
                    selectedExercises = uiState.selectedExercises,
                    onToggle = viewModel::toggleExercise,
                    onNext = viewModel::goNextFromExercises
                )
                WizardStep.LEVEL_SELECTION -> LevelSelectionStep(onLevelSelected = viewModel::onLevelSelected)
                WizardStep.QUICK_LEVEL_SELECTION -> LevelSelectionStep(onLevelSelected = viewModel::onQuickLevelSelected)
                WizardStep.SUMMARY -> SessionSummaryStep(
                    uiState = uiState,
                    onCreateSession = { viewModel.createSession(therapistId) }
                )
            }
        }
    }
}

@Composable
private fun ModeSelectionStep(onModeSelected: (SessionMode) -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Modalidad de trabajo", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        SelectionCard(
            title = "Con Paciente",
            description = "Registro clínico y seguimiento.",
            icon = Icons.Default.Person,
            onClick = { onModeSelected(SessionMode.WITH_PATIENT) }
        )
        SelectionCard(
            title = "Sin Paciente",
            description = "Uso libre o demostración.",
            icon = Icons.Default.PersonOff,
            onClick = { onModeSelected(SessionMode.WITHOUT_PATIENT) }
        )
    }
}

@Composable
private fun PatientSelectionStep(patients: List<Patient>, isLoading: Boolean, onPatientSelected: (Patient) -> Unit) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Selecciona el paciente", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(patients) { patient ->
                    Card(onClick = { onPatientSelected(patient) }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
                        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(40.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primaryContainer), contentAlignment = Alignment.Center) {
                                Text(patient.firstName.take(1))
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(patient.fullName, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CategorySelectionStep(onCategorySelected: (String) -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp, vertical = 16.dp)
    ) {
        Text("Área Cognitiva", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))

        val categories = listOf(
            Triple("Orientación", "Tiempo, espacio y persona.", Icons.Default.Event),
            Triple("Atención", "Focalización y mantenimiento visual.", Icons.Default.Visibility),
            Triple("Memoria", "Codificación y recuperación.", Icons.Default.Psychology),
            Triple("Lenguaje", "Fluidez y comprensión.", Icons.Default.RecordVoiceOver),
            Triple("Funciones Ejecutivas", "Planificación, secuencias y cálculo.", Icons.Default.Settings),
            Triple("Percepción", "Gnosias y capacidades visoespaciales.", Icons.Default.Extension),
            Triple("Lectoescritura", "Grafomotricidad y trazos.", Icons.Default.Edit)
        )

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(categories) { (title, desc, icon) ->
                SelectionCard(
                    title = title,
                    description = desc,
                    icon = icon,
                    onClick = { onCategorySelected(title) }
                )
            }
        }
    }
}

@Composable
private fun ExerciseSelectionStep(
    category: String,
    selectedExercises: List<ExerciseConfig>,
    onToggle: (type: String, name: String, category: String, desc: String) -> Unit,
    onNext: () -> Unit
) {
    val orientationPool = remember { getOrientationPool() }
    val exercises = remember(category) {
        when (category) {
            "Orientación" -> orientationPool
            "Atención" -> listOf(
                Triple("number_search", "Busca el Número", "Entrenamiento de atención focalizada."),
                Triple("attention_different", "Rodear el diferente", "Buscar el elemento intruso."),
                Triple("attention_equals_model", "Rodear los iguales al modelo", "Búsqueda visual selectiva."),
                Triple("attention_positions", "Rodear posiciones iguales", "Orientación espacial."),
                Triple("attention_differences", "Buscar diferencias", "Atención al detalle."),
                Triple("attention_letters", "Rodear las letras iguales", "Búsqueda de grafemas."),
                Triple("attention_symbols", "Rodear símbolos iguales", "Atención visual simbólica."),
                Triple("attention_matrices", "Matrices (Animales/Símbolos)", "Atención en cuadrícula."),
                Triple("attention_row_cancel", "Tachado por filas con recuento", "Cancelación y conteo."),
                Triple("attention_consecutive", "Rodear números consecutivos", "Atención sostenida."),
                Triple("attention_yes_no", "Tachar una sí y otra no", "Alternancia atencional."),
                Triple("attention_dual_task", "Tarea Dual (Doble instrucción)", "Atención dividida."),
                Triple("attention_count", "Contar dibujos", "Conteo visual."),
                Triple("attention_longest", "Palabra/Cifra más larga", "Discriminación visual."),
                Triple("attention_missing_part", "Parte del dibujo que falta", "Integración visual."),
                Triple("attention_word_search", "Sopa de letras/números", "Búsqueda sistemática.")
            ).sortedBy { it.second }
            "Memoria" -> listOf(
                Triple("memory_pairs", "Parejas de Memoria", "Encuentra las parejas de cartas iguales."),
                Triple("memory_cultural", "Cultura General", "Preguntas sobre geografía e historia."),
                Triple("memory_utility", "Utilidad de Objetos", "Relacionar objetos con su función."),
                Triple("memory_needs", "Necesidades para Tareas", "Identificar qué se necesita para una tarea."),
                Triple("memory_recent", "Memoria Reciente", "Preguntas sobre eventos cercanos.")
            ).sortedBy { it.second }
            "Lenguaje" -> listOf(
                Triple("language_word_image", "Vocabulario: Palabra-Imagen", "Identifica la imagen que corresponde a la palabra."),
                Triple("language_denomination", "Denominación de Objetos", "Elige el nombre correcto para la imagen mostrada."),
                Triple("language_semantic_category", "Clasificación Semántica", "Agrupa los objetos según su familia o categoría.")
            ).sortedBy { it.second }
            "Funciones Ejecutivas" -> listOf(
                Triple("executive_color_shape_sequence", "Secuencias Lógicas", "Completar series de colores y formas."),
                Triple("calculation_simple", "Cálculos Sencillos", "Resuelve operaciones aritméticas básicas.")
            ).sortedBy { it.second }
            "Percepción" -> listOf(
                Triple("perception_color_identification", "Identificación de Colores", "Toca el color que se indica por nombre."),
                Triple("perception_size_ordering", "Orden de Tamaños", "Ordena los objetos de menor a mayor tamaño."),
                Triple("perception_lateral_dominance", "Dominancia Lateral (Izq/Der)", "Identificar izquierda y derecha."),
                Triple("perception_mirror", "Imagen en Espejo", "Reconocer formas y letras reflejadas."),
                Triple("perception_body_parts", "Partes del Cuerpo", "Identificar y nombrar partes del cuerpo."),
                Triple("perception_shape_fitting", "Encaje de Formas", "Arrastra cada pieza hasta su silueta correspondiente.")
            ).sortedBy { it.second }
            "Lectoescritura" -> listOf(
                Triple("literacy_tracing", "Trazos Básicos", "Sigue las líneas punteadas con precisión.")
            )
            else -> emptyList()
        }
    }

    var searchQuery by remember { mutableStateOf("") }
    val filteredExercises = remember(exercises, searchQuery) {
        if (searchQuery.isBlank()) exercises
        else exercises.filter { it.second.contains(searchQuery, ignoreCase = true) }
    }

    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp, vertical = 16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Ejercicios de $category", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
            Text("${selectedExercises.size} seleccionados", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
        }
        
        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Buscar ejercicio...", fontSize = 14.sp) },
            leadingIcon = { Icon(Icons.Default.Search, null, modifier = Modifier.size(20.dp)) },
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(12.dp),
            singleLine = true,
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) { Icon(Icons.Default.Clear, null, modifier = Modifier.size(20.dp)) }
                }
            }
        )

        Spacer(modifier = Modifier.height(12.dp))

        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
            val listState = rememberLazyListState()
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(6.dp),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                items(filteredExercises) { (type, name, desc) ->
                    ExerciseItem(
                        title = name,
                        isSelected = selectedExercises.any { it.type == type },
                        onToggle = { onToggle(type, name, category, desc) }
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = onNext, modifier = Modifier.fillMaxWidth().height(56.dp), shape = RoundedCornerShape(16.dp), enabled = selectedExercises.isNotEmpty()) {
            Text("Siguiente")
        }
    }
}

@Composable
private fun ExerciseItem(title: String, isSelected: Boolean, onToggle: () -> Unit, enabled: Boolean = true) {
    Surface(
        onClick = if(enabled) onToggle else ({}),
        shape = RoundedCornerShape(8.dp),
        color = if(isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
        border = if(!isSelected) CardDefaults.outlinedCardBorder() else null,
        enabled = enabled,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
            Checkbox(checked = isSelected, onCheckedChange = { onToggle() }, enabled = enabled)
            Spacer(modifier = Modifier.width(12.dp))
            Text(title, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium), color = if(enabled) Color.Unspecified else Color.Gray)
        }
    }
}

@Composable
private fun LevelSelectionStep(onLevelSelected: (Int) -> Unit) {
    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Nivel de Dificultad (GDS)", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        (1..5).forEach { level ->
            Card(onClick = { onLevelSelected(level) }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(ExerciseTranslationUtils.getGdsLabel(level), fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                    Icon(Icons.Default.ChevronRight, contentDescription = null)
                }
            }
        }
    }
}

@Composable
private fun SessionSummaryStep(uiState: CreateSessionUiState, onCreateSession: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("Confirmar Sesión", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))) {
            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                SummaryRow("Paciente", uiState.selectedPatient?.fullName ?: "Sesión Libre")
                SummaryRow("Ejercicios", "${uiState.selectedExercises.size} seleccionados")
            }
        }
        Spacer(modifier = Modifier.weight(1f))
        Button(onClick = onCreateSession, modifier = Modifier.fillMaxWidth().height(56.dp), shape = RoundedCornerShape(16.dp), enabled = !uiState.isLoading) {
            if (uiState.isLoading) CircularProgressIndicator(color = Color.White) else Text("Iniciar Sesión", fontSize = 18.sp)
        }
    }
}

@Composable
private fun SummaryRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
        Text(value, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
    }
}

private fun getOrientationPool(): List<Triple<String, String, String>> {
    return listOf(
        Triple("orientation_temporal_day", "Día del mes", "Identificar el número del día actual."),
        Triple("orientation_temporal_month", "Mes del año", "Identificar en qué mes estamos."),
        Triple("orientation_temporal_year", "Año actual", "Reconocer el año en curso."),
        Triple("orientation_temporal_season", "Estación del año", "¿Es primavera, verano, otoño o invierno?"),
        Triple("orientation_temporal_dayweek", "Día de la semana", "¿Qué día de la semana es hoy?"),
        Triple("orientation_temporal_partday", "Parte del día", "Mañana, tarde o noche."),
        Triple("orientation_temporal_hour", "Hora aproximada", "Estimación del tiempo actual."),
        Triple("orientation_temporal_century", "Siglo actual", "Reconocimiento de la era actual."),
        Triple("orientation_temporal_decade", "Década actual", "Ubicación en la década."),
        Triple("orientation_temporal_yesterday", "Ayer qué día fue", "Orientación temporal retrospectiva."),
        Triple("orientation_temporal_tomorrow", "Mañana qué día será", "Orientación temporal prospectiva."),
        Triple("orientation_temporal_week_next", "Día próxima semana", "Cálculo de fechas futuras."),
        Triple("orientation_temporal_christmas", "Mes de Navidad", "Reconocimiento de festividades."),
        Triple("orientation_temporal_newyear", "Día Año Nuevo", "Inicio del ciclo anual."),
        Triple("orientation_temporal_night_light", "Luz nocturna", "Astronomía básica."),
        Triple("orientation_temporal_day_light", "Luz diurna", "Astronomía básica."),
        Triple("orientation_temporal_spring_month", "Mes de Primavera", "Ciclos estacionales."),
        Triple("orientation_temporal_summer_month", "Mes de Verano", "Ciclos estacionales."),
        Triple("orientation_temporal_autumn_month", "Mes de Otoño", "Ciclos estacionales."),
        Triple("orientation_temporal_winter_month", "Mes de Invierno", "Ciclos estacionales."),
        
        Triple("orientation_spatial_city", "Ciudad o Pueblo", "¿En qué localidad se encuentra?"),
        Triple("orientation_spatial_province", "Provincia", "¿En qué provincia estamos?"),
        Triple("orientation_spatial_country", "País", "Reconocimiento nacional."),
        Triple("orientation_spatial_continent", "Continente", "Ubicación geográfica global."),
        Triple("orientation_spatial_planet", "Planeta", "Ubicación espacial cósmica."),
        Triple("orientation_spatial_place", "Lugar actual", "Reconocimiento del entorno inmediato."),
        Triple("orientation_spatial_floor", "Planta edificio", "Orientación en altura."),
        Triple("orientation_spatial_kitchen", "Uso de cocina", "Funcionalidad de estancias."),
        Triple("orientation_spatial_bedroom", "Uso de dormitorio", "Funcionalidad de estancias."),
        Triple("orientation_spatial_library", "Uso de biblioteca", "Funcionalidad de estancias."),
        Triple("orientation_spatial_pharmacy", "Uso de farmacia", "Servicios de salud."),
        Triple("orientation_spatial_bakery", "Uso de panadería", "Servicios básicos."),
        Triple("orientation_spatial_ceiling", "Objeto en techo", "Percepción espacial."),
        Triple("orientation_spatial_ocean", "Océano mayor", "Geografía global."),
        Triple("orientation_spatial_moon_orbit", "Orbita lunar", "Conocimiento cósmico."),
        Triple("orientation_spatial_capital_spain", "Capital de España", "Geografía política."),
        
        Triple("orientation_personal_name", "Nombre propio", "Identidad personal básica."),
        Triple("orientation_personal_surname", "Primer apellido", "Identidad personal básica."),
        
        Triple("orientation_calc_year_days", "Días del año", "Conocimiento matemático temporal."),
        Triple("orientation_calc_year_months", "Meses del año", "Estructura del calendario."),
        Triple("orientation_calc_week_days", "Días de la semana", "Estructura semanal."),
        Triple("orientation_calc_day_hours", "Horas del día", "Estructura horaria."),
        Triple("orientation_calc_minutes_hour", "Minutos en una hora", "División del tiempo."),
        Triple("orientation_calc_seconds_minute", "Segundos en un minuto", "División del tiempo."),
        Triple("orientation_calc_half_day", "Horas medio día", "Cálculo fraccional."),
        Triple("orientation_calc_feet_count", "Número de pies", "Esquema corporal."),
        Triple("orientation_calc_hands_count", "Número de manos", "Esquema corporal."),
        Triple("orientation_calc_fingers_hand", "Dedos en una mano", "Conteo básico."),
        Triple("orientation_calc_fingers_total", "Dedos totales", "Suma básica."),
        Triple("orientation_calc_century_years", "Años en un siglo", "Escala histórica."),
        Triple("orientation_calc_decade_years", "Años en una década", "Escala histórica."),
        Triple("orientation_calc_dozen", "Unidades docena", "Unidades de medida."),
        Triple("orientation_calc_half_dozen", "Unidades media docena", "Unidades de medida."),
        Triple("orientation_calc_wheels_car", "Ruedas de coche", "Observación del entorno."),
        Triple("orientation_calc_wheels_bike", "Ruedas de bicicleta", "Observación del entorno."),
        Triple("orientation_calc_wheels_tricycle", "Ruedas de triciclo", "Observación del entorno."),

        Triple("orientation_situational_currency", "Moneda actual", "Contexto económico."),
        Triple("orientation_situational_language", "Idioma hablado", "Contexto cultural."),
        Triple("orientation_situational_color_sky", "Color del cielo", "Percepción ambiental."),
        Triple("orientation_situational_color_grass", "Color de hierba", "Percepción ambiental."),
        Triple("orientation_situational_king", "Rey actual", "Orientación sociopolítica."),
        Triple("orientation_situational_blood", "Color sangre", "Conocimiento biológico."),
        Triple("orientation_situational_fire", "Sensación fuego", "Termoalgesia cognitiva."),
        Triple("orientation_situational_ice", "Sensación hielo", "Termoalgesia cognitiva."),
        Triple("orientation_situational_sun", "Salida del sol", "Orientación natural."),
        Triple("orientation_situational_lemon_taste", "Sabor limón", "Gusto y memoria."),
        Triple("orientation_situational_sugar_taste", "Sabor azúcar", "Gusto y memoria."),
        Triple("orientation_situational_sea_water", "Sabor agua mar", "Conocimiento geográfico."),
        Triple("orientation_situational_stop_color", "Color señal STOP", "Seguridad vial."),
        Triple("orientation_situational_zebra_cross", "Color paso cebra", "Seguridad vial."),
        Triple("orientation_situational_traffic_light_go", "Semaforo verde", "Seguridad vial."),
        Triple("orientation_situational_traffic_light_stop", "Semaforo rojo", "Seguridad vial."),
        Triple("orientation_situational_dog_sound", "Sonido perro", "Reconocimiento onomatopéyico."),
        Triple("orientation_situational_cat_sound", "Sonido gato", "Reconocimiento onomatopéyico."),
        Triple("orientation_situational_cow_sound", "Sonido vaca", "Reconocimiento onomatopéyico."),
        Triple("orientation_situational_sheep_sound", "Sonido oveja", "Reconocimiento onomatopéyico."),
        Triple("orientation_situational_milk_color", "Color de leche", "Propiedades de objetos."),
        Triple("orientation_situational_coal_color", "Color de carbón", "Propiedades de objetos."),
        Triple("orientation_situational_tomato_color", "Color de tomate", "Propiedades de objetos."),
        Triple("orientation_situational_banana_color", "Color de plátano", "Propiedades de objetos."),
        Triple("orientation_situational_dentist", "Médico de dientes", "Red social de apoyo."),
        Triple("orientation_situational_umbrella", "Uso paraguas", "Pragmática de objetos."),
        Triple("orientation_situational_glasses", "Uso gafas", "Pragmática de objetos."),
        Triple("orientation_situational_shoes_wear", "Uso zapatos", "Pragmática de objetos."),
        Triple("orientation_situational_hat_wear", "Uso sombrero", "Pragmática de objetos."),
        Triple("orientation_situational_gloves_wear", "Uso guantes", "Pragmática de objetos."),
        Triple("orientation_situational_fridge_use", "Uso frigorífico", "Pragmática de objetos."),
        Triple("orientation_situational_chair_use", "Uso silla", "Pragmática de objetos."),
        Triple("orientation_situational_bed_use", "Uso cama", "Pragmática de objetos."),
        Triple("orientation_situational_eyes_count", "Número de ojos", "Esquema corporal."),
        Triple("orientation_situational_ears_count", "Número de orejas", "Esquema corporal."),
        Triple("orientation_situational_nose_count", "Número de narices", "Esquema corporal."),
        Triple("orientation_situational_mouth_count", "Número de bocas", "Esquema corporal."),
        Triple("orientation_situational_head_count", "Número de cabezas", "Esquema corporal."),
        Triple("orientation_situational_arms_count", "Número de brazos", "Esquema corporal."),
        Triple("orientation_situational_legs_count", "Número de piernas", "Esquema corporal."),
        Triple("orientation_situational_hair_color", "Color pelo anciano", "Identidad física."),
        Triple("orientation_situational_sun_shape", "Forma del sol", "Geometría natural."),
        Triple("orientation_situational_ball_shape", "Forma de balón", "Geometría de objetos."),
        Triple("orientation_situational_table_use", "Uso de mesa", "Pragmática funcional."),
        Triple("orientation_situational_knife_use", "Uso de cuchillo", "Pragmática funcional."),
        Triple("orientation_situational_spoon_use", "Uso de cuchara", "Pragmática funcional."),
        Triple("orientation_situational_comb_use", "Uso de peine", "Pragmática funcional."),
        Triple("orientation_situational_soap_use", "Uso de jabón", "Pragmática funcional."),
        Triple("orientation_situational_towel_use", "Uso de toalla", "Pragmática funcional."),
        Triple("orientation_situational_broom_use", "Uso de escoba", "Pragmática funcional."),
        Triple("orientation_situational_oven_use" , "Uso de horno", "Pragmática funcional."),
        Triple("orientation_situational_pill_use", "Uso de medicinas", "Conciencia de salud."),
        Triple("orientation_situational_phone_use", "Uso de teléfono", "Tecnología básica."),
        Triple("orientation_situational_keys_use", "Uso de llaves", "Seguridad del hogar."),
        Triple("orientation_situational_glasses_use", "Uso de gafas", "Compensación sensorial."),
        Triple("orientation_situational_watch_use", "Uso de reloj", "Orientación temporal externa."),
        Triple("orientation_situational_wallet_use", "Uso de cartera", "Gestión instrumental."),
        Triple("orientation_situational_calendar_use", "Uso de calendario", "Orientación temporal externa."),
        Triple("orientation_situational_doctor_tool", "Herramienta médico", "Entorno sanitario."),
        Triple("orientation_situational_firemen", "Función bomberos", "Seguridad ciudadana."),
        Triple("orientation_situational_stewardess", "Lugar azafata", "Transporte público."),
        Triple("orientation_situational_pilot", "Función piloto", "Transporte público."),
        Triple("orientation_situational_ship_captain", "Función capitán", "Transporte marítimo."),
        Triple("orientation_situational_cow_milk", "Origen leche", "Conocimiento animal."),
        Triple("orientation_situational_hen_eggs", "Origen huevos", "Conocimiento animal."),
        Triple("orientation_situational_bee_honey", "Origen miel", "Conocimiento animal."),
        Triple("orientation_situational_spider_web", "Origen tela araña", "Conocimiento animal.")
    ).sortedBy { it.second }
}

@Composable
private fun SelectionCard(title: String, description: String, icon: ImageVector, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(modifier = Modifier.size(40.dp), shape = CircleShape, color = MaterialTheme.colorScheme.primaryContainer) {
                Box(contentAlignment = Alignment.Center) { Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp)) }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                Text(description, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            }
        }
    }
}
