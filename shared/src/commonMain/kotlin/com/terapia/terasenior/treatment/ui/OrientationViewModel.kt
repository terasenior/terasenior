package com.terapia.terasenior.treatment.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.terapia.terasenior.domain.model.results.ActivityResult
import com.terapia.terasenior.domain.usecase.results.SaveActivityResultUseCase
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.*

enum class OrientationType {
    WEEKDAY, MONTH, YEAR, SEASON, WEATHER
}

data class OrientationUiState(
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

    private val days = listOf("Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado", "Domingo")
    private val months = listOf("Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio", "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre")
    private val seasons = listOf("Primavera", "Verano", "Otoño", "Invierno")
    private val weatherOptions = listOf("Frío", "Calor", "Templado", "Mucho Calor")

    fun startNewGame(level: Int = 1) {
        val nowInstant = kotlinx.datetime.Clock.System.now()
        val now = nowInstant.toLocalDateTime(kotlinx.datetime.TimeZone.currentSystemDefault())
        setupQuestion(OrientationType.WEEKDAY, now)
        _uiState.update { it.copy(
            currentLevel = level,
            startTimeMs = nowInstant.toEpochMilliseconds(),
            isCompleted = false,
            errorsCount = 0
        ) }
    }

    private fun setupQuestion(type: OrientationType, now: LocalDateTime) {
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
                    else -> ""
                }
                Triple("¿Qué día de la semana es hoy?", days.shuffled(), correctDay)
            }
            OrientationType.MONTH -> {
                val mOrdinal = now.month.ordinal
                Triple("¿En qué mes estamos?", months.shuffled().take(4).let { if(it.contains(months[mOrdinal])) it else it.take(3) + months[mOrdinal] }.shuffled(), months[mOrdinal])
            }
            OrientationType.YEAR -> {
                val year = now.year.toString()
                Triple("¿En qué año estamos?", listOf(year, (now.year-1).toString(), (now.year+1).toString(), "2020").shuffled(), year)
            }
            OrientationType.SEASON -> {
                val mOrdinal = now.month.ordinal + 1
                val correctSeason = when(mOrdinal) {
                    in 3..5 -> "Primavera"
                    in 6..8 -> "Verano"
                    in 9..11 -> "Otoño"
                    else -> "Invierno"
                }
                Triple("¿En qué estación del año estamos?", seasons, correctSeason)
            }
            OrientationType.WEATHER -> Triple("¿Qué tiempo hace hoy?", weatherOptions, "Templado")
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
                nextQuestion(patientId, professionalId, appointmentId)
            }
        } else {
            _uiState.update { it.copy(isCorrect = false, errorsCount = state.errorsCount + 1) }
            viewModelScope.launch {
                delay(1000)
                _uiState.update { it.copy(isCorrect = null) }
            }
        }
    }

    private fun nextQuestion(patientId: String?, professionalId: String?, appointmentId: String?) {
        val now = kotlinx.datetime.Clock.System.now().toLocalDateTime(kotlinx.datetime.TimeZone.currentSystemDefault())
        when(_uiState.value.currentQuestionType) {
            OrientationType.WEEKDAY -> setupQuestion(OrientationType.MONTH, now)
            OrientationType.MONTH -> setupQuestion(OrientationType.YEAR, now)
            OrientationType.YEAR -> setupQuestion(OrientationType.SEASON, now)
            OrientationType.SEASON -> {
                _uiState.update { it.copy(isCompleted = true) }
                if (patientId != null && professionalId != null) {
                    saveResult(patientId, professionalId, appointmentId)
                }
            }
            else -> {}
        }
    }

    private fun saveResult(patientId: String, professionalId: String, appointmentId: String?) {
        val state = _uiState.value
        val endTime = kotlinx.datetime.Clock.System.now().toEpochMilliseconds()
        val diff = endTime - state.startTimeMs
        val duration = (diff / 1000).toInt()

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            val result = ActivityResult(
                id = "",
                patientId = patientId,
                professionalId = professionalId,
                appointmentId = appointmentId,
                activityType = "orientation_temporal",
                score = (100 - (state.errorsCount * 10)).coerceAtLeast(0),
                durationSeconds = duration,
                errorsCount = state.errorsCount,
                difficultyLevel = "NIVEL_${state.currentLevel}",
                createdAt = ""
            )
            saveResultUseCase(result)
            _uiState.update { it.copy(isSaving = false) }
        }
    }
}
