package com.terapia.terasenior.treatment.ui

import androidx.compose.ui.geometry.Offset
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
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

enum class TracingPathType {
    HORIZONTAL_LINE, VERTICAL_LINE, CIRCLE, ZIGZAG
}

data class TracingUiState(
    val pathType: TracingPathType = TracingPathType.HORIZONTAL_LINE,
    val userPoints: List<Offset> = emptyList(),
    val isCompleted: Boolean = false,
    val isSaving: Boolean = false,
    val currentLevel: Int = 1,
    val errorsCount: Int = 0,
    val startTimeMs: Long = 0
)

class TracingViewModel(
    private val saveResultUseCase: SaveActivityResultUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(TracingUiState())
    val uiState: StateFlow<TracingUiState> = _uiState.asStateFlow()

    @OptIn(ExperimentalTime::class)
    fun startNewGame(level: Int = 1) {
        val type = when (level) {
            1 -> TracingPathType.HORIZONTAL_LINE
            2 -> TracingPathType.VERTICAL_LINE
            3 -> TracingPathType.ZIGZAG
            4 -> TracingPathType.CIRCLE
            5 -> TracingPathType.ZIGZAG // Más complejo?
            else -> TracingPathType.HORIZONTAL_LINE
        }

        _uiState.value = TracingUiState(
            pathType = type,
            currentLevel = level,
            startTimeMs = Clock.System.now().toEpochMilliseconds()
        )
    }

    fun onDrawPoint(offset: Offset) {
        if (_uiState.value.isCompleted) return
        _uiState.update { it.copy(userPoints = it.userPoints + offset) }
    }

    fun completeTracing(patientId: String?, professionalId: String?, appointmentId: String?) {
        if (_uiState.value.isCompleted) return
        _uiState.update { it.copy(isCompleted = true) }
        
        if (patientId != null && professionalId != null) {
            saveResult(patientId, professionalId, appointmentId)
        }
    }

    fun clearDrawing() {
        _uiState.update { it.copy(userPoints = emptyList(), isCompleted = false) }
    }

    @OptIn(ExperimentalTime::class)
    private fun saveResult(patientId: String, professionalId: String, appointmentId: String?) {
        val state = _uiState.value
        val endTime = Clock.System.now().toEpochMilliseconds()
        val duration = ((endTime - state.startTimeMs) / 1000L).toInt()

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            val result = ActivityResult(
                id = "",
                patientId = patientId,
                professionalId = professionalId,
                appointmentId = appointmentId,
                activityType = "literacy_tracing",
                score = 100, // Basado en precisión? Por ahora simplificado.
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
