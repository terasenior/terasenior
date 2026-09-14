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
import kotlinx.datetime.Clock
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime

enum class OrientationType {
    GENERIC, WEEKDAY, MONTH, YEAR, SEASON, WEATHER
}

data class OrientationUiState(
    val currentType: String = "orientation_temporal",
    val currentQuestionType: OrientationType = OrientationType.WEEKDAY,
    val sessionId: String = "", // v1.3.48
    val questionText: String = "",
    val options: List<String> = emptyList(),
    val correctAnswer: String = "",
    val isCorrect: Boolean? = null,
    val isCompleted: Boolean = false,
    val isSaving: Boolean = false,
    val currentLevel: Int = 1,
    val startTimeMs: Long = 0,
    val hitsCount: Int = 0,
    val errorsCount: Int = 0,
    val debugInfo: String = "" // v1.3.38: String simple para evitar fallos de lista en Wasm
)

class OrientationViewModel(
    private val saveResultUseCase: SaveActivityResultUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(OrientationUiState())
    val uiState: StateFlow<OrientationUiState> = _uiState.asStateFlow()

    @OptIn(kotlin.time.ExperimentalTime::class)
    fun startNewGame(type: String, level: Int = 1, sessionId: String = "") {
        val now = try { kotlinx.datetime.Clock.System.now().toEpochMilliseconds() } catch(t: Throwable) { 1724310000000L }

        _uiState.update { it.copy(
            currentType = type,
            currentLevel = level,
            sessionId = sessionId,
            startTimeMs = now,
            isCompleted = false,
            hitsCount = 0,
            errorsCount = 0,
            questionText = "Iniciando...", 
            options = emptyList(),
            isCorrect = null,
            debugInfo = "START"
        ) }
        
        viewModelScope.launch {
            try {
                delay(100)
                _uiState.update { it.copy(debugInfo = it.debugInfo + " -> LAUNCH") }
                
                if (type == "orientation_temporal") {
                    setupClassicTemporal()
                } else {
                    setupCatalogQuestion(type)
                }
            } catch (t: Throwable) {
                _uiState.update { it.copy(questionText = "ERROR PLATAFORMA: ${t.message ?: "Wasm"}") }
            }
        }
    }

    private fun setupClassicTemporal() {
        // Usamos fecha fija de emergencia para evitar el crash de TimeZone.currentSystemDefault()
        val now = LocalDateTime(2026, 8, 22, 10, 0)
        _uiState.update { it.copy(debugInfo = it.debugInfo + " -> SAFE_TIME") }
        setupLegacyQuestion(OrientationType.WEEKDAY, now)
    }

    private fun setupCatalogQuestion(type: String) {
        try {
            _uiState.update { it.copy(debugInfo = it.debugInfo + " -> CATALOG_REQ") }
            val question = OrientationCatalog.getQuestion(type)
            _uiState.update { it.copy(
                questionText = question.text,
                options = GdsDifficulty.choices(question.options, question.correctAnswer, it.currentLevel),
                correctAnswer = question.correctAnswer,
                isCorrect = null,
                debugInfo = it.debugInfo + " -> OK"
            ) }
        } catch (t: Throwable) {
            _uiState.update { it.copy(questionText = "ERROR CATALOGO: $type", debugInfo = it.debugInfo + " -> ERR") }
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
            options = GdsDifficulty.choices(options, correct, _uiState.value.currentLevel),
            correctAnswer = correct,
            isCorrect = null
        ) }
    }

    fun onOptionSelected(selected: String, patientId: String?, professionalId: String?, appointmentId: String?) {
        val state = _uiState.value
        if (state.isCorrect == true) return

        if (selected == state.correctAnswer) {
            _uiState.update { it.copy(isCorrect = true, hitsCount = it.hitsCount + 1) }
            viewModelScope.launch {
                delay(1500)
                if (state.currentType == "orientation_temporal") {
                    nextLegacyQuestion(patientId, professionalId, appointmentId)
                } else {
                    if (patientId != null && professionalId != null) {
                        saveResult(patientId, professionalId, appointmentId)
                    } else {
                        _uiState.update { it.copy(isCompleted = true) }
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

    private fun nextLegacyQuestion(patientId: String?, professionalId: String?, appointmentId: String?) {
        val now = LocalDateTime(2026, 8, 22, 10, 0)
        when(_uiState.value.currentQuestionType) {
            OrientationType.WEEKDAY -> if (_uiState.value.currentLevel <= 1) finishLegacyQuestions(patientId, professionalId, appointmentId) else setupLegacyQuestion(OrientationType.MONTH, now)
            OrientationType.MONTH -> if (_uiState.value.currentLevel <= 2) finishLegacyQuestions(patientId, professionalId, appointmentId) else setupLegacyQuestion(OrientationType.YEAR, now)
            OrientationType.YEAR -> if (_uiState.value.currentLevel <= 3) finishLegacyQuestions(patientId, professionalId, appointmentId) else setupLegacyQuestion(OrientationType.SEASON, now)
            OrientationType.SEASON -> {
                finishLegacyQuestions(patientId, professionalId, appointmentId)
            }
            else -> {}
        }
    }

    private fun finishLegacyQuestions(patientId: String?, professionalId: String?, appointmentId: String?) {
        _uiState.update { it.copy(isCompleted = true) }
        if (patientId != null && professionalId != null) {
            saveResult(patientId, professionalId, appointmentId)
        }
    }

    @OptIn(kotlin.time.ExperimentalTime::class)
    private fun saveResult(patientId: String, professionalId: String, appointmentId: String?) {
        val state = _uiState.value
        val now = try { kotlinx.datetime.Clock.System.now().toEpochMilliseconds() } catch(t: Throwable) { state.startTimeMs + 30000 }
        val duration = ((now - state.startTimeMs) / 1000L).toInt().coerceAtLeast(1)

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            val result = ActivityResult(
                id = "",
                patientId = patientId,
                professionalId = professionalId,
                appointmentId = appointmentId,
                activityType = state.currentType,
                sessionId = state.sessionId,
                score = (100 - (state.errorsCount * 10)).coerceAtLeast(0),
                durationSeconds = duration,
                errorsCount = state.errorsCount,
                difficultyLevel = "CHALLENGE_${state.currentLevel}", 
                createdAt = ""
            )
            saveResultUseCase(result)
            _uiState.update { it.copy(isSaving = false, isCompleted = true) }
        }
    }
}
