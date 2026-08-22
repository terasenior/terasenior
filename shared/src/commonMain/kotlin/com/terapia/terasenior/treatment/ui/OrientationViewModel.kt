package com.terapia.terasenior.treatment.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.terapia.terasenior.domain.model.results.ActivityResult
import com.terapia.terasenior.domain.usecase.results.SaveActivityResultUseCase
import com.terapia.terasenior.treatment.repository.OrientationCatalog
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.*

enum class OrientationType {
    GENERIC, WEEKDAY, MONTH, YEAR, SEASON, WEATHER
}

data class OrientationUiState(
    val currentType: String = "orientation_temporal",
    val currentQuestionType: OrientationType = OrientationType.WEEKDAY,
    val questionText: String = "",
    val options: List<String> = emptyList(),
    val correctAnswer: String = "",
    val isCorrect: Boolean? = null,
    val isCompleted: Boolean = false,
    val isSaving: Boolean = false,
    val currentLevel: Int = 1,
    val startTimeMs: Long = 0,
    val errorsCount: Int = 0
)

class OrientationViewModel(
    private val saveResultUseCase: SaveActivityResultUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(OrientationUiState())
    val uiState: StateFlow<OrientationUiState> = _uiState.asStateFlow()

    @OptIn(kotlin.time.ExperimentalTime::class)
    fun startNewGame(type: String, level: Int = 1) {
        val nowInstant = try { 
            Clock.System.now() 
        } catch(t: Throwable) { 
            Instant.fromEpochMilliseconds(1724140000000L) 
        }

        _uiState.update { it.copy(
            currentType = type,
            currentLevel = level,
            startTimeMs = nowInstant.toEpochMilliseconds(),
            isCompleted = false,
            errorsCount = 0,
            questionText = "Cargando v1.3.36...", 
            options = emptyList(),
            isCorrect = null
        ) }
        
        viewModelScope.launch {
            try {
                // Mayor retardo y limpieza explícita (v1.3.36)
                delay(300)
                if (type == "orientation_temporal") {
                    setupClassicTemporal()
                } else {
                    setupCatalogQuestion(type)
                }
            } catch (t: Throwable) {
                _uiState.update { it.copy(questionText = "Error de plataforma. Use F5.") }
            }
        }
    }

    private fun setupClassicTemporal() {
        val now = try { 
            kotlinx.datetime.Clock.System.now().toLocalDateTime(TimeZone.UTC) 
        } catch(t: Throwable) { 
            LocalDateTime(2026, 8, 20, 12, 0) 
        }
        setupLegacyQuestion(OrientationType.WEEKDAY, now)
    }

    private fun setupCatalogQuestion(type: String) {
        try {
            val question = OrientationCatalog.getQuestion(type)
            _uiState.update { it.copy(
                questionText = question.text,
                options = question.options.shuffled(),
                correctAnswer = question.correctAnswer,
                isCorrect = null
            ) }
        } catch (t: Throwable) {
            _uiState.update { it.copy(questionText = "Error en catálogo: $type") }
        }
    }

    private fun setupLegacyQuestion(type: OrientationType, now: LocalDateTime) {
        val days = listOf("Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado", "Domingo")
        val months = listOf("Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio", "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre")
        val seasons = listOf("Primavera", "Verano", "Otoño", "Invierno")

        val (question, options, correct) = when(type) {
            OrientationType.WEEKDAY -> {
                val correctDay = when(now.dayOfWeek) {
                    DayOfWeek.MONDAY -> "Lunes"
                    DayOfWeek.TUESDAY -> "Martes"
                    DayOfWeek.WEDNESDAY -> "Miércoles"
                    DayOfWeek.THURSDAY -> "Jueves"
                    DayOfWeek.FRIDAY -> "Viernes"
                    DayOfWeek.SATURDAY -> "Sábado"
                    DayOfWeek.SUNDAY -> "Domingo"
                }
                Triple("¿Qué día de la semana es hoy?", days.shuffled(), correctDay)
            }
            OrientationType.MONTH -> {
                val mOrdinal = (now.monthNumber - 1).coerceIn(0, 11)
                Triple("¿En qué mes estamos?", months.shuffled().take(4).let { if(it.contains(months[mOrdinal])) it else it.take(3) + months[mOrdinal] }.shuffled(), months[mOrdinal])
            }
            OrientationType.YEAR -> {
                val year = now.year.toString()
                Triple("¿En qué año estamos?", listOf(year, (now.year-1).toString(), (now.year+1).toString(), "2020").shuffled(), year)
            }
            OrientationType.SEASON -> {
                val mNum = now.monthNumber
                val correctSeason = when(mNum) {
                    in 3..5 -> "Primavera"
                    in 6..8 -> "Verano"
                    in 9..11 -> "Otoño"
                    else -> "Invierno"
                }
                Triple("¿En qué estación del año estamos?", seasons, correctSeason)
            }
            else -> Triple("¿Qué tiempo hace hoy?", listOf("Sol", "Nubes", "Lluvia"), "Sol")
        }

        _uiState.update { it.copy(
            currentQuestionType = type,
            questionText = question,
            options = options,
            correctAnswer = correct,
            isCorrect = null
        ) }
    }

    fun onOptionSelected(selected: String, patientId: String?, professionalId: String?, appointmentId: String?) {
        val state = _uiState.value
        if (state.isCorrect == true) return

        if (selected == state.correctAnswer) {
            _uiState.update { it.copy(isCorrect = true) }
            viewModelScope.launch {
                delay(1500)
                if (state.currentType == "orientation_temporal") {
                    nextLegacyQuestion(patientId, professionalId, appointmentId)
                } else {
                    _uiState.update { it.copy(isCompleted = true) }
                    if (patientId != null && professionalId != null) {
                        saveResult(patientId, professionalId, appointmentId)
                    }
                }
            }
        } else {
            _uiState.update { it.copy(isCorrect = false, errorsCount = state.errorsCount + 1) }
            viewModelScope.launch {
                delay(1000)
                _uiState.update { it.copy(isCorrect = null) }
            }
        }
    }

    @OptIn(kotlin.time.ExperimentalTime::class)
    private fun nextLegacyQuestion(patientId: String?, professionalId: String?, appointmentId: String?) {
        val now = try { 
            Clock.System.now().toLocalDateTime(TimeZone.UTC)
        } catch(t: Throwable) {
            LocalDateTime(2026, 8, 20, 12, 0)
        }
        when(_uiState.value.currentQuestionType) {
            OrientationType.WEEKDAY -> setupLegacyQuestion(OrientationType.MONTH, now)
            OrientationType.MONTH -> setupLegacyQuestion(OrientationType.YEAR, now)
            OrientationType.YEAR -> setupLegacyQuestion(OrientationType.SEASON, now)
            OrientationType.SEASON -> {
                _uiState.update { it.copy(isCompleted = true) }
                if (patientId != null && professionalId != null) {
                    saveResult(patientId, professionalId, appointmentId)
                }
            }
            else -> {}
        }
    }

    @OptIn(kotlin.time.ExperimentalTime::class)
    private fun saveResult(patientId: String, professionalId: String, appointmentId: String?) {
        val state = _uiState.value
        val now = try { Clock.System.now() } catch(t: Throwable) { Instant.fromEpochMilliseconds(1724140000000L) }
        val endTime = now.toEpochMilliseconds()
        val duration = ((endTime - state.startTimeMs) / 1000L).toInt()

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            val result = ActivityResult(
                id = "",
                patientId = patientId,
                professionalId = professionalId,
                appointmentId = appointmentId,
                activityType = state.currentType,
                score = (100 - (state.errorsCount * 10)).coerceAtLeast(0),
                durationSeconds = duration,
                errorsCount = state.errorsCount,
                difficultyLevel = "GDS_${state.currentLevel + 2}", 
                createdAt = ""
            )
            saveResultUseCase(result)
            _uiState.update { it.copy(isSaving = false) }
        }
    }
}
