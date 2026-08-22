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
import kotlinx.datetime.Instant

data class MemoryUiState(
    val currentType: String = "memory_cultural",
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

class MemoryViewModel(
    private val saveResultUseCase: SaveActivityResultUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(MemoryUiState())
    val uiState: StateFlow<MemoryUiState> = _uiState.asStateFlow()

    @OptIn(kotlin.time.ExperimentalTime::class)
    fun startNewGame(type: String, level: Int = 1) {
        val nowInstant = try { Clock.System.now() } catch(t: Throwable) { Instant.fromEpochMilliseconds(1724310000000L) }
        
        _uiState.update { it.copy(
            currentType = type,
            currentLevel = level,
            startTimeMs = nowInstant.toEpochMilliseconds(),
            isCompleted = false,
            errorsCount = 0,
            questionText = "Cargando v1.3.43...",
            options = emptyList(),
            isCorrect = null
        ) }
        setupCatalogQuestion(type)
    }

    private fun setupCatalogQuestion(type: String) {
        try {
            val question = MemoryCatalog.getQuestion(type)
            _uiState.update { it.copy(
                questionText = question.text,
                options = question.options,
                correctAnswer = question.correctAnswer,
                isCorrect = null
            ) }
        } catch (t: Throwable) {
            _uiState.update { it.copy(questionText = "Error al cargar ejercicio de memoria.") }
        }
    }

    fun onOptionSelected(selected: String, patientId: String?, professionalId: String?, appointmentId: String?) {
        val state = _uiState.value
        if (state.isCorrect == true || state.isCompleted) return

        if (selected == state.correctAnswer || state.correctAnswer == "Respuesta libre") {
            _uiState.update { it.copy(isCorrect = true) }
            viewModelScope.launch {
                delay(1500)
                _uiState.update { it.copy(isCompleted = true) }
                if (patientId != null && professionalId != null) {
                    saveResult(patientId, professionalId, appointmentId)
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
        val now = try { Clock.System.now() } catch(t: Throwable) { Instant.fromEpochMilliseconds(1724310000000L) }
        val endTime = now.toEpochMilliseconds()
        val diff = endTime - state.startTimeMs
        val duration = (diff / 1000L).toInt()

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
