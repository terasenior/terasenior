package com.terapia.terasenior.treatment.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
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

data class SequenceItem(
    val shape: ImageVector,
    val color: Color,
    val name: String
)

data class ColorShapeSequenceUiState(
    val sequence: List<SequenceItem> = emptyList(),
    val targetItem: SequenceItem? = null,
    val options: List<SequenceItem> = emptyList(),
    val isCorrect: Boolean? = null,
    val isCompleted: Boolean = false,
    val isSaving: Boolean = false,
    val currentLevel: Int = 1,
    val errorsCount: Int = 0,
    val startTimeMs: Long = 0
)

class ColorShapeSequenceViewModel(
    private val saveResultUseCase: SaveActivityResultUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ColorShapeSequenceUiState())
    val uiState: StateFlow<ColorShapeSequenceUiState> = _uiState.asStateFlow()

    private val shapes = listOf(
        Icons.Default.Circle to "Círculo",
        Icons.Default.Square to "Cuadrado",
        Icons.Default.ChangeHistory to "Triángulo",
        Icons.Default.Star to "Estrella"
    )

    private val colors = listOf(
        Color(0xFFF44336) to "Rojo",
        Color(0xFF2196F3) to "Azul",
        Color(0xFF4CAF50) to "Verde",
        Color(0xFFFFEB3B) to "Amarillo"
    )

    @OptIn(ExperimentalTime::class)
    fun startNewGame(level: Int = 1) {
        val patternSize = when (level) {
            1 -> 2 // ABAB
            2 -> 2 // ABAB but longer sequence
            3 -> 3 // ABCABC
            4 -> 3 // AABAAB
            5 -> 4 // ABCDABCD
            else -> 2
        }

        val sequenceLength = 6
        val availableShapes = shapes.shuffled()
        val availableColors = colors.shuffled()
        
        val pattern = mutableListOf<SequenceItem>()
        for (i in 0 until patternSize) {
            val shapeInfo = availableShapes[i % availableShapes.size]
            val colorInfo = availableColors[i % availableColors.size]
            pattern.add(SequenceItem(shapeInfo.first, colorInfo.first, "${colorInfo.second} ${shapeInfo.second}"))
        }

        val sequence = mutableListOf<SequenceItem>()
        for (i in 0 until sequenceLength - 1) {
            sequence.add(pattern[i % patternSize])
        }

        val target = pattern[(sequenceLength - 1) % patternSize]
        
        val options = mutableListOf(target)
        while (options.size < 4) {
            val randomShape = shapes.random()
            val randomColor = colors.random()
            val newItem = SequenceItem(randomShape.first, randomColor.first, "${randomColor.second} ${randomShape.second}")
            if (options.none { it.shape == newItem.shape && it.color == newItem.color }) {
                options.add(newItem)
            }
        }

        _uiState.value = ColorShapeSequenceUiState(
            sequence = sequence,
            targetItem = target,
            options = options.shuffled(),
            currentLevel = level,
            startTimeMs = Clock.System.now().toEpochMilliseconds()
        )
    }

    fun onOptionSelected(item: SequenceItem, patientId: String?, professionalId: String?, appointmentId: String?) {
        val state = _uiState.value
        if (state.isCompleted || state.isCorrect == true) return

        if (item == state.targetItem) {
            _uiState.update { it.copy(isCorrect = true, isCompleted = true) }
            if (patientId != null && professionalId != null) {
                saveResult(patientId, professionalId, appointmentId)
            }
        } else {
            _uiState.update { it.copy(isCorrect = false, errorsCount = state.errorsCount + 1) }
            viewModelScope.launch {
                delay(1000)
                _uiState.update { it.copy(isCorrect = null) }
            }
        }
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
                activityType = "executive_color_shape_sequence",
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
