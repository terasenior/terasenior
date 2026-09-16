package com.terapia.terasenior.treatment.ui

import androidx.compose.ui.graphics.Color
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
import kotlinx.datetime.Clock as DateClock

data class ColorGameItem(
    val color: Color,
    val name: String
)

data class ColorIdentificationUiState(
    val targetColor: ColorGameItem? = null,
    val options: List<ColorGameItem> = emptyList(),
    val isCorrect: Boolean? = null,
    val isCompleted: Boolean = false,
    val isSaving: Boolean = false,
    val currentLevel: Int = 1,
    val errorsCount: Int = 0,
    val startTimeMs: Long = 0,
    val sessionId: String = "" // v1.3.48
)

class ColorIdentificationViewModel(
    private val saveResultUseCase: SaveActivityResultUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ColorIdentificationUiState())
    val uiState: StateFlow<ColorIdentificationUiState> = _uiState.asStateFlow()

    private val colorCatalog = listOf(
        ColorGameItem(Color(0xFFF44336), "Rojo"),
        ColorGameItem(Color(0xFF2196F3), "Azul"),
        ColorGameItem(Color(0xFF4CAF50), "Verde"),
        ColorGameItem(Color(0xFFFFEB3B), "Amarillo"),
        ColorGameItem(Color(0xFFFF9800), "Naranja"),
        ColorGameItem(Color(0xFF9C27B0), "Violeta"),
        ColorGameItem(Color(0xFF795548), "Marrón"),
        ColorGameItem(Color(0xFF000000), "Negro"),
        ColorGameItem(Color(0xFFFFFFFF), "Blanco"),
        ColorGameItem(Color(0xFF9E9E9E), "Gris"),
        ColorGameItem(Color(0xFFFFC107), "Ámbar"),
        ColorGameItem(Color(0xFF00BCD4), "Cian")
    )

    fun startNewGame(level: Int = 1, sessionId: String = "") {
        val numOptions = when (level) {
            1 -> 2
            2 -> 3
            3 -> 4
            4 -> 6
            5 -> 9
            else -> 4
        }

        val shuffled = colorCatalog.shuffled()
        val options = shuffled.take(numOptions)
        val target = options.random()

        _uiState.value = ColorIdentificationUiState(
            targetColor = target,
            options = options,
            currentLevel = level,
            sessionId = sessionId,
            startTimeMs = activityTimeMillis()
        )
    }

    fun onColorSelected(item: ColorGameItem, patientId: String?, professionalId: String?, appointmentId: String?) {
        val state = _uiState.value
        if (state.isCompleted || state.isCorrect == true) return

        if (item == state.targetColor) {
            _uiState.update { it.copy(isCorrect = true) }
            saveResult(patientId, professionalId, appointmentId)
        } else {
            _uiState.update { it.copy(isCorrect = false, errorsCount = state.errorsCount + 1) }
            viewModelScope.launch {
                delay(1000)
                _uiState.update { it.copy(isCorrect = null) }
            }
        }
    }

    private fun saveResult(patientId: String?, professionalId: String?, appointmentId: String?) {
        val state = _uiState.value
        val endTime = activityTimeMillis()
        val duration = ((endTime - state.startTimeMs) / 1000L).toInt()

        viewModelScope.launch {
            if (patientId != null && professionalId != null) {
                _uiState.update { it.copy(isSaving = true) }
                val result = ActivityResult(
                    id = "",
                    patientId = patientId,
                    professionalId = professionalId,
                    appointmentId = appointmentId,
                    sessionId = state.sessionId, // v1.3.48
                    activityType = "perception_color_identification",
                    score = (100 - (state.errorsCount * 10)).coerceAtLeast(0),
                    durationSeconds = duration,
                    errorsCount = state.errorsCount,
                    difficultyLevel = "NIVEL_${state.currentLevel}",
                    createdAt = ""
                )
                saveResultUseCase(result)
                _uiState.update { it.copy(isSaving = false, isCompleted = true) }
            } else {
                _uiState.update { it.copy(isCompleted = true) }
            }
        }
    }
}
