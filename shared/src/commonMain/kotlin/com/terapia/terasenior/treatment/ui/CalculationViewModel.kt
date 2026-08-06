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
import kotlin.random.Random

data class CalculationUiState(
    val operationText: String = "",
    val correctAnswer: Int = 0,
    val options: List<Int> = emptyList(),
    val isCorrect: Boolean? = null,
    val isCompleted: Boolean = false,
    val isSaving: Boolean = false,
    val currentLevel: Int = 3,
    val errorsCount: Int = 0,
    val startTimeMs: Long = 0
)

class CalculationViewModel(
    private val saveResultUseCase: SaveActivityResultUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(CalculationUiState())
    val uiState: StateFlow<CalculationUiState> = _uiState.asStateFlow()

    @OptIn(kotlin.time.ExperimentalTime::class)
    fun startNewGame(level: Int = 3) {
        val (opText, answer) = generateOperation(level)
        
        // Generar distractores cercanos a la respuesta
        val options = mutableSetOf(answer)
        while (options.size < 4) {
            val offset = Random.nextInt(-3, 4)
            val distractor = (answer + offset).coerceAtLeast(0)
            options.add(distractor)
        }

        _uiState.value = CalculationUiState(
            operationText = opText,
            correctAnswer = answer,
            options = options.toList().shuffled(),
            currentLevel = level,
            startTimeMs = kotlin.time.Clock.System.now().toEpochMilliseconds()
        )
    }

    private fun generateOperation(level: Int): Pair<String, Int> {
        val a: Int
        val b: Int
        return when(level) {
            1 -> { // Sumas < 10
                a = Random.nextInt(1, 6)
                b = Random.nextInt(1, 5)
                "$a + $b" to (a + b)
            }
            2 -> { // Restas < 10
                a = Random.nextInt(5, 10)
                b = Random.nextInt(1, a)
                "$a - $b" to (a - b)
            }
            3 -> { // Sumas/Restas < 20
                if (Random.nextBoolean()) {
                    a = Random.nextInt(5, 15)
                    b = Random.nextInt(1, 6)
                    "$a + $b" to (a + b)
                } else {
                    a = Random.nextInt(10, 20)
                    b = Random.nextInt(1, 10)
                    "$a - $b" to (a - b)
                }
            }
            else -> {
                a = Random.nextInt(10, 30)
                b = Random.nextInt(5, 20)
                "$a + $b" to (a + b)
            }
        }
    }

    fun onAnswerSelected(selected: Int, patientId: String?, professionalId: String?, appointmentId: String?) {
        val state = _uiState.value
        if (state.isCompleted || state.isCorrect == true) return

        if (selected == state.correctAnswer) {
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

    @OptIn(kotlin.time.ExperimentalTime::class)
    private fun saveResult(patientId: String, professionalId: String, appointmentId: String?) {
        val state = _uiState.value
        val endTime = kotlin.time.Clock.System.now().toEpochMilliseconds()
        val duration = ((endTime - state.startTimeMs) / 1000L).toInt()

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            val result = ActivityResult(
                id = "",
                patientId = patientId,
                professionalId = professionalId,
                appointmentId = appointmentId,
                activityType = "calculation_simple",
                score = (100 - (state.errorsCount * 15)).coerceAtLeast(0),
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
