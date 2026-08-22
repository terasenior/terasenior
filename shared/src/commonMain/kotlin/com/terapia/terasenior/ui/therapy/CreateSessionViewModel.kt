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
                Triple("memory_cultural_inventor", "Inventor de bombilla", "Memoria cultural histórica."),
                Triple("memory_cultural_discovery", "Descubrimiento América", "Memoria cultural histórica."),
                Triple("memory_cultural_moon", "Llegada a la Luna", "Memoria cultural histórica."),
                Triple("memory_cultural_painting", "Pintor Las Meninas", "Memoria cultural artística."),
                Triple("memory_cultural_writer", "Autor El Quijote", "Memoria cultural literaria."),
                Triple("memory_recent_today_weather", "Tiempo de hoy", "Memoria episódica reciente."),
                Triple("memory_recent_last_meal", "Cena de anoche", "Memoria episódica reciente."),
                Triple("memory_prospective_meds", "Hora medicación", "Memoria prospectiva."),
                Triple("memory_working_numbers_rev", "Números al revés", "Memoria de trabajo."),
                Triple("memory_semantic_capital_italy", "Capital de Italia", "Memoria semántica geográfica."),
                Triple("memory_semantic_capital_germany", "Capital de Alemania", "Memoria semántica geográfica."),
                Triple("memory_semantic_capital_portugal", "Capital de Portugal", "Memoria semántica geográfica."),
                Triple("memory_semantic_ocean_depth", "Animal mayor océano", "Memoria semántica biológica."),
                Triple("memory_semantic_planet_red", "Planeta rojo", "Memoria semántica astronómica."),
                Triple("memory_semantic_island_canary", "Isla de Canarias", "Memoria semántica geográfica."),
                Triple("memory_semantic_island_balearic", "Isla de Baleares", "Memoria semántica geográfica."),
                Triple("memory_semantic_river_egypt", "Río de Egipto", "Memoria semántica geográfica."),
                Triple("memory_semantic_mountain_everest", "Montaña más alta", "Memoria semántica geográfica."),
                Triple("memory_semantic_continent_kangaroo", "Continente canguros", "Memoria semántica biológica."),
                Triple("memory_semantic_continent_pyramids", "Continente pirámides", "Memoria semántica geográfica."),
                Triple("memory_utility_hammer", "Uso del martillo", "Memoria instrumental."),
                Triple("memory_utility_scissors", "Uso de tijeras", "Memoria instrumental."),
                Triple("memory_utility_broom", "Uso de la escoba", "Memoria instrumental."),
                Triple("memory_utility_keys", "Uso de las llaves", "Memoria instrumental."),
                Triple("memory_utility_glasses", "Uso de las gafas", "Memoria instrumental."),
                Triple("memory_needs_coffee", "Necesidad para café", "Memoria de procesos."),
                Triple("memory_needs_letter", "Necesidad para carta", "Memoria de procesos."),
                Triple("memory_needs_wash_hair", "Necesidad para pelo", "Memoria de procesos."),
                Triple("memory_needs_teeth", "Necesidad para dientes", "Memoria de procesos."),
                Triple("memory_needs_rain", "Necesidad para lluvia", "Memoria de procesos."),
                Triple("memory_semantic_fruit_yellow", "Fruta amarilla", "Memoria semántica visual."),
                Triple("memory_semantic_fruit_red", "Fruta roja", "Memoria semántica visual."),
                Triple("memory_semantic_animal_bark", "Animal que ladra", "Memoria semántica onomatopéyica."),
                Triple("memory_semantic_animal_meow", "Animal que maúlla", "Memoria semántica onomatopéyica."),
                Triple("memory_semantic_animal_moo", "Animal que muge", "Memoria semántica onomatopéyica."),
                Triple("memory_semantic_color_grass", "Color de hierba", "Memoria semántica visual."),
                Triple("memory_semantic_color_sky", "Color del cielo", "Memoria semántica visual."),
                Triple("memory_semantic_color_coal", "Color del carbón", "Memoria semántica visual."),
                Triple("memory_semantic_color_milk", "Color de leche", "Memoria semántica visual."),
                Triple("memory_semantic_color_blood", "Color de sangre", "Memoria semántica visual."),
                Triple("memory_semantic_season_cold", "Estación más fría", "Memoria semántica temporal."),
                Triple("memory_semantic_season_hot", "Estación más calurosa", "Memoria semántica temporal."),
                Triple("memory_semantic_day_first", "Primer día semana", "Memoria semántica temporal."),
                Triple("memory_semantic_day_last", "Último día semana", "Memoria semántica temporal."),
                Triple("memory_semantic_month_first", "Primer mes año", "Memoria semántica temporal."),
                Triple("memory_semantic_clothing_feet", "Prenda para pies", "Memoria semántica instrumental."),
                Triple("memory_semantic_clothing_hands", "Prenda para manos", "Memoria semántica instrumental."),
                Triple("memory_semantic_clothing_head", "Prenda para cabeza", "Memoria semántica instrumental."),
                Triple("memory_semantic_tool_nails", "Herramienta clavos", "Memoria semántica instrumental."),
                Triple("memory_semantic_tool_screws", "Herramienta tornillos", "Memoria semántica instrumental."),
                Triple("memory_semantic_kitchen_fry", "Objeto para freír", "Memoria semántica cocina."),
                Triple("memory_semantic_kitchen_soup", "Objeto para sopa", "Memoria semántica cocina."),
                Triple("memory_semantic_kitchen_bake", "Lugar para asar", "Memoria semántica cocina."),
                Triple("memory_semantic_home_sleep", "Mueble para dormir", "Memoria semántica hogar."),
                Triple("memory_semantic_home_sit", "Mueble para sentarse", "Memoria semántica hogar."),
                Triple("memory_semantic_home_clothes", "Lugar para ropa", "Memoria semántica hogar."),
                Triple("memory_semantic_home_food", "Lugar para comida", "Memoria semántica hogar."),
                Triple("memory_semantic_body_see", "Parte para ver", "Memoria semántica cuerpo."),
                Triple("memory_semantic_body_hear", "Parte para oír", "Memoria semántica cuerpo."),
                Triple("memory_semantic_body_smell", "Parte para oler", "Memoria semántica cuerpo."),
                Triple("memory_semantic_body_eat", "Parte para comer", "Memoria semántica cuerpo."),
                Triple("memory_semantic_body_walk", "Parte para caminar", "Memoria semántica cuerpo."),
                Triple("memory_semantic_body_write", "Parte para escribir", "Memoria semántica cuerpo."),
                Triple("memory_semantic_family_son", "Hijo del padre", "Memoria semántica familia."),
                Triple("memory_semantic_family_father_father", "Padre del padre", "Memoria semántica familia."),
                Triple("memory_semantic_family_sister", "Hija de la madre", "Memoria semántica familia."),
                Triple("memory_semantic_family_uncle", "Hermano de madre", "Memoria semántica familia."),
                Triple("memory_semantic_family_nephew", "Hijo del hermano", "Memoria semántica familia."),
                Triple("memory_semantic_object_umbrella", "Uso paraguas", "Memoria semántica pragmática."),
                Triple("memory_semantic_object_soap", "Uso jabón", "Memoria semántica pragmática."),
                Triple("memory_semantic_object_towel", "Uso toalla", "Memoria semántica pragmática."),
                Triple("memory_semantic_object_keys", "Uso llaves", "Memoria semántica pragmática."),
                Triple("memory_semantic_object_phone", "Uso teléfono", "Memoria semántica pragmática."),
                Triple("memory_semantic_object_tv", "Uso televisor", "Memoria semántica pragmática."),
                Triple("memory_semantic_object_radio", "Uso radio", "Memoria semántica pragmática."),
                Triple("memory_semantic_object_clock", "Uso reloj", "Memoria semántica pragmática."),
                Triple("memory_semantic_object_money", "Uso dinero", "Memoria semántica pragmática."),
                Triple("memory_semantic_object_wallet", "Uso cartera", "Memoria semántica pragmática."),
                Triple("memory_semantic_animal_hen", "Origen huevos", "Memoria semántica biológica."),
                Triple("memory_semantic_animal_cow", "Origen leche", "Memoria semántica biológica."),
                Triple("memory_semantic_animal_sheep", "Origen lana", "Memoria semántica biológica."),
                Triple("memory_semantic_animal_bee", "Origen miel", "Memoria semántica biológica."),
                Triple("memory_semantic_animal_spider", "Origen telaraña", "Memoria semántica biológica."),
                Triple("memory_semantic_food_bread", "Lugar compra pan", "Memoria semántica comercio."),
                Triple("memory_semantic_food_meat", "Lugar compra carne", "Memoria semántica comercio."),
                Triple("memory_semantic_food_fish", "Lugar compra pescado", "Memoria semántica comercio."),
                Triple("memory_semantic_food_fruit", "Lugar compra fruta", "Memoria semántica comercio."),
                Triple("memory_semantic_food_medicine", "Lugar compra medicina", "Memoria semántica comercio."),
                Triple("memory_semantic_place_church", "Lugar para misa", "Memoria semántica social."),
                Triple("memory_semantic_place_hospital", "Lugar para enfermos", "Memoria semántica social."),
                Triple("memory_semantic_place_school", "Lugar para aprender", "Memoria semántica social."),
                Triple("memory_semantic_place_park", "Lugar para pasear", "Memoria semántica social."),
                Triple("memory_semantic_place_kitchen", "Lugar para cocinar", "Memoria semántica hogar."),
                Triple("memory_semantic_place_bathroom", "Lugar para ducha", "Memoria semántica hogar."),
                Triple("memory_semantic_color_sun", "Color del sol", "Memoria semántica visual."),
                Triple("memory_semantic_color_tomato", "Color del tomate", "Memoria semántica visual."),
                Triple("memory_semantic_color_lemon", "Color del limón", "Memoria semántica visual."),
                Triple("memory_semantic_color_orange", "Color de naranja", "Memoria semántica visual."),
                Triple("memory_cultural_first_king", "Primer Rey democracia", "Memoria cultural política."),
                Triple("memory_cultural_civil_war", "Siglo Guerra Civil", "Memoria cultural histórica."),
                Triple("memory_cultural_discovery_navigator", "Naveante América", "Memoria cultural histórica."),
                Triple("memory_cultural_capital_france", "Capital de Francia", "Memoria cultural geográfica."),
                Triple("memory_cultural_capital_uk", "Capital Reino Unido", "Memoria cultural geográfica."),
                Triple("memory_cultural_euro_intro", "Año entrada Euro", "Memoria cultural histórica."),
                Triple("memory_cultural_olympics_barcelona", "Año Olimpiadas BCN", "Memoria cultural histórica."),
                Triple("memory_cultural_monalisa", "Pintor La Gioconda", "Memoria cultural artística."),
                Triple("memory_cultural_beethoven", "Compositor 9ª Sinfonía", "Memoria cultural musical."),
                Triple("memory_cultural_cervantes_birth", "Ciudad Cervantes", "Memoria cultural literaria.")
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
