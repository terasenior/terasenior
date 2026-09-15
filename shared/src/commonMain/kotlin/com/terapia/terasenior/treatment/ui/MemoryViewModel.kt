package com.terapia.terasenior.treatment.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.terapia.terasenior.domain.model.results.ActivityResult
import com.terapia.terasenior.domain.usecase.results.SaveActivityResultUseCase
import com.terapia.terasenior.treatment.repository.MemoryCatalog
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone

data class MemoryUiState(
    val currentType: String = "memory_cultural",
    val sessionId: String = "", // v1.3.48
    val questionText: String = "",
    val options: List<String> = emptyList(),
    val correctAnswer: String = "",
    val isCorrect: Boolean? = null,
    val isCompleted: Boolean = false,
    val isSaving: Boolean = false,
    val currentLevel: Int = 1,
    val startTimeMs: Long = 0,
    val errorsCount: Int = 0,
    val debugInfo: String = "" // v1.3.44
)

class MemoryViewModel(
    private val saveResultUseCase: SaveActivityResultUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(MemoryUiState())
    val uiState: StateFlow<MemoryUiState> = _uiState.asStateFlow()

    @OptIn(kotlin.time.ExperimentalTime::class)
    fun startNewGame(type: String, level: Int = 1, sessionId: String = "") {
        val now = try { kotlinx.datetime.Clock.System.now().toEpochMilliseconds() } catch(t: Throwable) { 1724310000000L }
        
        _uiState.update { it.copy(
            currentType = type,
            currentLevel = level,
            sessionId = sessionId,
            startTimeMs = now,
            isCompleted = false,
            errorsCount = 0,
            questionText = "Iniciando...",
            options = emptyList(),
            isCorrect = null,
            debugInfo = "START"
        ) }
        
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(debugInfo = it.debugInfo + " -> LAUNCH") }
                val question = MemoryCatalog.getQuestion(type)
                _uiState.update { it.copy(
                    questionText = question.text,
                    options = GdsDifficulty.choices(question.options, question.correctAnswer, level),
                    correctAnswer = question.correctAnswer,
                    isCorrect = null,
                    debugInfo = it.debugInfo + " -> OK"
                ) }
            } catch (t: Throwable) {
                _uiState.update { it.copy(
                    questionText = "ERROR: ${t.message}",
                    debugInfo = it.debugInfo + " -> FAIL"
                ) }
            }
        }
    }

    private fun setupCatalogQuestion(type: String) {
        try {
            _uiState.update { it.copy(debugInfo = it.debugInfo + " -> CATALOG_REQ") }
            val question = MemoryCatalog.getQuestion(type)
            _uiState.update { it.copy(
                questionText = question.text,
                options = question.options,
                correctAnswer = question.correctAnswer,
                isCorrect = null,
                debugInfo = it.debugInfo + " -> OK"
            ) }
        } catch (t: Throwable) {
            _uiState.update { it.copy(questionText = "ERROR CATALOGO: $type", debugInfo = it.debugInfo + " -> ERR") }
        }
    }

    fun onOptionSelected(selected: String, patientId: String?, professionalId: String?, appointmentId: String?) {
        val state = _uiState.value
        if (state.isCorrect == true || state.isCompleted) return

        if (selected == state.correctAnswer || state.correctAnswer == "Respuesta libre") {
            _uiState.update { it.copy(isCorrect = true) }
            viewModelScope.launch {
                delay(1500)
                if (patientId != null && professionalId != null) {
                    saveResult(patientId, professionalId, appointmentId)
                } else {
                    _uiState.update { it.copy(isCompleted = true) }
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
                sessionId = state.sessionId,
                activityType = state.currentType,
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
