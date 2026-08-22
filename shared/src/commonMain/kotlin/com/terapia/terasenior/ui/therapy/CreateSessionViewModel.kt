package com.terapia.terasenior.ui.therapy

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.terapia.terasenior.domain.model.agenda.Appointment
import com.terapia.terasenior.domain.model.patient.Patient
import com.terapia.terasenior.domain.model.therapy.ExerciseConfig
import com.terapia.terasenior.domain.model.therapy.SessionMode
import com.terapia.terasenior.domain.model.therapy.SessionStatus
import com.terapia.terasenior.domain.model.therapy.TherapySession
import com.terapia.terasenior.domain.model.therapy.TherapySessionExercise
import com.terapia.terasenior.domain.repository.patient.PatientRepository
import com.terapia.terasenior.domain.repository.therapy.TherapySessionRepository
import com.terapia.terasenior.ui.therapy.ExerciseTranslationUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class WizardStep {
    MODE_SELECTION, PATIENT_SELECTION, CATEGORY_SELECTION, EXERCISE_SELECTION, LEVEL_SELECTION, SUMMARY, QUICK_LEVEL_SELECTION
}

data class CreateSessionUiState(
    val currentStep: WizardStep = WizardStep.MODE_SELECTION,
    val mode: SessionMode? = null,
    val isStandardized: Boolean = false, // v1.3.40
    val selectedPatient: Patient? = null,
    val selectedAppointmentId: String? = null,
    val selectedCategory: String? = null,
    val selectedExercises: List<ExerciseConfig> = emptyList(),
    val patients: List<Patient> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val createdSessionId: String? = null
)

class CreateSessionViewModel(
    private val therapyRepository: TherapySessionRepository,
    private val patientRepository: PatientRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CreateSessionUiState())
    val uiState: StateFlow<CreateSessionUiState> = _uiState.asStateFlow()

    fun onModeSelected(mode: SessionMode) {
        val isQuick = _uiState.value.isStandardized
        _uiState.update { it.copy(
            mode = mode,
            currentStep = if (isQuick) {
                if (mode == SessionMode.WITHOUT_PATIENT) WizardStep.QUICK_LEVEL_SELECTION else WizardStep.PATIENT_SELECTION
            } else {
                if (mode == SessionMode.WITHOUT_PATIENT) WizardStep.CATEGORY_SELECTION else WizardStep.PATIENT_SELECTION
            }
        ) }
        
        if (mode == SessionMode.WITH_PATIENT) {
            fetchPatients()
        }
    }

    private fun fetchPatients() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            patientRepository.getPatients().collect { result ->
                result.onSuccess { list ->
                    _uiState.update { it.copy(patients = list, isLoading = false) }
                }.onFailure { e ->
                    _uiState.update { it.copy(error = e.message, isLoading = false) }
                }
            }
        }
    }

    fun startQuickEvaluationFlow() {
        _uiState.update { it.copy(
            isStandardized = true,
            currentStep = WizardStep.MODE_SELECTION
        ) }
    }

    fun onPatientSelected(patient: Patient) {
        val isQuick = _uiState.value.isStandardized
        _uiState.update { it.copy(
            selectedPatient = patient, 
            currentStep = if (isQuick) WizardStep.QUICK_LEVEL_SELECTION else WizardStep.CATEGORY_SELECTION
        ) }
    }

    fun startFromAppointment(appointment: Appointment, patient: Patient?) {
        val planned = appointment.plannedExercises
        
        _uiState.update { it.copy(
            mode = SessionMode.FROM_APPOINTMENT,
            selectedAppointmentId = appointment.id,
            selectedPatient = patient,
            selectedExercises = planned,
            currentStep = if (patient != null) {
                if (planned.isNotEmpty()) WizardStep.SUMMARY else WizardStep.CATEGORY_SELECTION
            } else {
                WizardStep.PATIENT_SELECTION
            }
        ) }
        if (patient == null) fetchPatients()
    }

    fun onCategorySelected(category: String) {
        _uiState.update { it.copy(selectedCategory = category, currentStep = WizardStep.EXERCISE_SELECTION) }
    }

    fun toggleExercise(type: String, name: String, category: String, description: String) {
        _uiState.update { state ->
            val current = state.selectedExercises
            val updated = if (current.any { it.type == type }) {
                current.filter { it.type != type }
            } else {
                current + ExerciseConfig(type, name, category, description = description)
            }
            state.copy(selectedExercises = updated)
        }
    }

    fun onLevelSelected(level: Int) {
        _uiState.update { state ->
            val updated = state.selectedExercises.map { it.copy(level = level) }
            state.copy(selectedExercises = updated, currentStep = WizardStep.SUMMARY)
        }
    }

    fun onQuickLevelSelected(level: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val exercises = generateStandardizedExercises(level)
            _uiState.update { it.copy(
                selectedExercises = exercises,
                currentStep = WizardStep.SUMMARY,
                isLoading = false
            ) }
        }
    }

    private fun generateStandardizedExercises(level: Int): List<ExerciseConfig> {
        val result = mutableListOf<ExerciseConfig>()
        val categories = listOf("Orientación", "Atención", "Memoria", "Lenguaje", "Funciones Ejecutivas", "Percepción", "Lectoescritura")
        
        categories.forEach { cat ->
            val pool = getExercisesForCategory(cat)
            val selected = pool.shuffled().take(10)
            selected.forEach { (type, name, desc) ->
                result.add(ExerciseConfig(type, name, cat, level, desc))
            }
        }
        return result
    }

    private fun getExercisesForCategory(category: String): List<Triple<String, String, String>> {
        return when (category) {
            "Orientación" -> listOf(
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
            )
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
            )
            "Memoria" -> listOf(
                Triple("memory_pairs", "Parejas de Memoria", "Encuentra las parejas de cartas iguales."),
                Triple("memory_cultural", "Cultura General", "Preguntas sobre geografía e historia."),
                Triple("memory_utility", "Utilidad de Objetos", "Relacionar objetos con su función."),
                Triple("memory_needs", "Necesidades para Tareas", "Identificar qué se necesita para una tarea."),
                Triple("memory_recent", "Memoria Reciente", "Preguntas sobre eventos cercanos.")
            )
            "Lenguaje" -> listOf(
                Triple("language_word_image", "Vocabulario: Palabra-Imagen", "Identifica la imagen que corresponde a la palabra."),
                Triple("language_denomination", "Denominación de Objetos", "Elige el nombre correcto para la imagen mostrada."),
                Triple("language_semantic_category", "Clasificación Semántica", "Agrupa los objetos según su familia o categoría.")
            )
            "Funciones Ejecutivas" -> listOf(
                Triple("executive_color_shape_sequence", "Secuencias Lógicas", "Completar series de colores y formas."),
                Triple("calculation_simple", "Cálculos Sencillos", "Resuelve operaciones aritméticas básicas.")
            )
            "Percepción" -> listOf(
                Triple("perception_color_identification", "Identificación de Colores", "Toca el color que se indica por nombre."),
                Triple("perception_size_ordering", "Orden de Tamaños", "Ordena los objetos de menor a mayor tamaño."),
                Triple("perception_lateral_dominance", "Dominancia Lateral (Izq/Der)", "Identificar izquierda y derecha."),
                Triple("perception_mirror", "Imagen en Espejo", "Reconocer formas y letras reflejadas."),
                Triple("perception_body_parts", "Partes del Cuerpo", "Identificar y nombrar partes del cuerpo."),
                Triple("perception_shape_fitting", "Encaje de Formas", "Arrastra cada pieza hasta su silueta correspondiente.")
            )
            "Lectoescritura" -> listOf(
                Triple("literacy_tracing", "Trazos Básicos", "Sigue las líneas punteadas con precisión.")
            )
            else -> emptyList()
        }
    }

    fun goNextFromExercises() {
        if (_uiState.value.selectedExercises.isNotEmpty()) {
            _uiState.update { it.copy(currentStep = WizardStep.LEVEL_SELECTION) }
        }
    }

    fun goBack() {
        val currentState = _uiState.value
        val previousStep = when (currentState.currentStep) {
            WizardStep.MODE_SELECTION -> WizardStep.MODE_SELECTION
            WizardStep.PATIENT_SELECTION -> WizardStep.MODE_SELECTION
            WizardStep.CATEGORY_SELECTION -> if (currentState.mode == SessionMode.WITHOUT_PATIENT || currentState.mode == SessionMode.FROM_APPOINTMENT) WizardStep.MODE_SELECTION else WizardStep.PATIENT_SELECTION
            WizardStep.EXERCISE_SELECTION -> WizardStep.CATEGORY_SELECTION
            WizardStep.LEVEL_SELECTION -> WizardStep.EXERCISE_SELECTION
            WizardStep.SUMMARY -> if (currentState.isStandardized) WizardStep.QUICK_LEVEL_SELECTION else WizardStep.LEVEL_SELECTION
            WizardStep.QUICK_LEVEL_SELECTION -> if (currentState.mode == SessionMode.WITHOUT_PATIENT) WizardStep.MODE_SELECTION else WizardStep.PATIENT_SELECTION
        }
        _uiState.update { it.copy(currentStep = previousStep) }
    }

    fun resetWizard() {
        _uiState.value = CreateSessionUiState()
    }

    fun createSession(therapistId: String) {
        val state = _uiState.value
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            val session = TherapySession(
                id = "",
                title = "Sesión Combinada",
                description = "Intervención de estimulación cognitiva",
                therapistId = therapistId,
                patientId = state.selectedPatient?.id,
                appointmentId = state.selectedAppointmentId,
                mode = state.mode ?: SessionMode.WITHOUT_PATIENT,
                status = SessionStatus.READY,
                startedAt = null,
                finishedAt = null,
                createdAt = ""
            )

            therapyRepository.createSession(session).onSuccess { sessionId ->
                state.selectedExercises.forEachIndexed { index, config ->
                    val exercise = TherapySessionExercise(
                        id = "",
                        sessionId = sessionId,
                        exerciseType = config.type,
                        level = config.level,
                        position = index
                    )
                    therapyRepository.addExerciseToSession(exercise)
                }
                _uiState.update { it.copy(createdSessionId = sessionId, isLoading = false) }
            }.onFailure { e ->
                _uiState.update { it.copy(error = e.message, isLoading = false) }
            }
        }
    }
}
