package com.terapia.terasenior.treatment.ui

import androidx.compose.ui.geometry.Offset
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.terapia.terasenior.domain.model.results.ActivityResult
import com.terapia.terasenior.domain.usecase.results.SaveActivityResultUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.math.sqrt

enum class LiteracyVariation {
    TRACING_BASIC,
    COMPLETE_LETTERS,
    COPY_WORDS,
    FORM_SHAPES
}

data class LiteracyUiState(
    val variation: LiteracyVariation = LiteracyVariation.TRACING_BASIC,
    val instruction: String = "",
    val prompt: String = "",
    val targetValue: String = "",
    val options: List<String> = emptyList(),
    val userInput: String = "",
    val userPoints: List<Offset> = emptyList(),
    val targetPoints: List<Offset> = emptyList(), // For shapes/tracing guide
    val isCorrect: Boolean? = null,
    val isCompleted: Boolean = false,
    val isSaving: Boolean = false,
    val currentLevel: Int = 1,
    val errorsCount: Int = 0,
    val startTimeMs: Long = 0,
    val tracingAccuracy: Float = 0f
)

class LiteracyViewModel(
    private val saveResultUseCase: SaveActivityResultUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(LiteracyUiState())
    val uiState: StateFlow<LiteracyUiState> = _uiState.asStateFlow()

    fun startNewGame(variation: LiteracyVariation, level: Int = 1) {
        val (instruction, prompt, target, options) = when (variation) {
            LiteracyVariation.TRACING_BASIC -> Quadruple(
                "Sigue la línea de puntos con el dedo.",
                "",
                "",
                emptyList()
            )
            LiteracyVariation.COMPLETE_LETTERS -> {
                val data = getCompleteLettersData(level)
                Quadruple("Completa la palabra con la letra que falta.", data.first, data.second, data.third)
            }
            LiteracyVariation.COPY_WORDS -> {
                val word = getCopyWordsData(level)
                Quadruple("Copia la palabra en el recuadro.", word, word, emptyList())
            }
            LiteracyVariation.FORM_SHAPES -> Quadruple(
                "Une los puntos para formar la figura.",
                "",
                "",
                emptyList()
            )
        }

        _uiState.value = LiteracyUiState(
            variation = variation,
            instruction = instruction,
            prompt = prompt,
            targetValue = target,
            options = options,
            currentLevel = level,
            startTimeMs = kotlinx.datetime.Clock.System.now().toEpochMilliseconds()
        )
    }

    fun onUserInputChange(input: String) {
        if (_uiState.value.isCompleted) return
        _uiState.update { it.copy(userInput = input) }
        
        if (_uiState.value.variation == LiteracyVariation.COPY_WORDS && input.equals(_uiState.value.targetValue, ignoreCase = true)) {
            _uiState.update { it.copy(isCorrect = true, isCompleted = true) }
        }
    }

    fun onOptionSelected(option: String, patientId: String?, professionalId: String?, appointmentId: String?) {
        if (_uiState.value.isCompleted) return
        
        val isCorrect = option == _uiState.value.targetValue
        _uiState.update { 
            it.copy(
                userInput = option,
                isCorrect = isCorrect,
                isCompleted = isCorrect,
                errorsCount = if (isCorrect) it.errorsCount else it.errorsCount + 1
            ) 
        }
        
        if (isCorrect && patientId != null && professionalId != null) {
            saveResult(patientId, professionalId, appointmentId)
        }
    }

    fun onDrawPoint(offset: Offset) {
        if (_uiState.value.isCompleted) return
        _uiState.update { it.copy(userPoints = it.userPoints + offset) }
    }

    fun setTargetPoints(points: List<Offset>) {
        _uiState.update { it.copy(targetPoints = points) }
    }

    fun completeCanvasTask(patientId: String?, professionalId: String?, appointmentId: String?) {
        if (_uiState.value.isCompleted) return
        
        val accuracy = calculateTracingAccuracy()
        val isSuccess = accuracy > 0.7f // 70% accuracy threshold
        
        _uiState.update { 
            it.copy(
                isCompleted = true,
                isCorrect = isSuccess,
                tracingAccuracy = accuracy,
                errorsCount = if (isSuccess) it.errorsCount else it.errorsCount + 1
            ) 
        }
        
        if (patientId != null && professionalId != null) {
            saveResult(patientId, professionalId, appointmentId)
        }
    }

    fun clearDrawing() {
        _uiState.update { it.copy(userPoints = emptyList(), isCorrect = null, isCompleted = false) }
    }

    private fun calculateTracingAccuracy(): Float {
        val userPoints = _uiState.value.userPoints
        val targetPoints = _uiState.value.targetPoints
        
        if (userPoints.isEmpty() || targetPoints.isEmpty()) return 0f
        
        var pointsNearTarget = 0
        val threshold = 50f // Distance in pixels
        
        // Basic implementation: what percentage of user points are close to the guide?
        userPoints.forEach { up ->
            val isNear = targetPoints.any { tp ->
                val dist = sqrt((up.x - tp.x) * (up.x - tp.x) + (up.y - tp.y) * (up.y - tp.y))
                dist < threshold
            }
            if (isNear) pointsNearTarget++
        }
        
        // Also check coverage: what percentage of target points were visited?
        var targetCoverage = 0
        targetPoints.forEach { tp ->
            val isVisited = userPoints.any { up ->
                val dist = sqrt((up.x - tp.x) * (up.x - tp.x) + (up.y - tp.y) * (up.y - tp.y))
                dist < threshold
            }
            if (isVisited) targetCoverage++
        }
        
        val precision = pointsNearTarget.toFloat() / userPoints.size
        val coverage = targetCoverage.toFloat() / targetPoints.size
        
        return (precision + coverage) / 2f
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
                activityType = "literacy_${state.variation.name.lowercase()}",
                score = (state.tracingAccuracy * 100).toInt().coerceIn(0, 100).let { if (it == 0 && state.isCorrect == true) 100 else it },
                durationSeconds = duration,
                errorsCount = state.errorsCount,
                difficultyLevel = "NIVEL_${state.currentLevel}",
                createdAt = ""
            )
            saveResultUseCase(result)
            _uiState.update { it.copy(isSaving = false) }
        }
    }

    private fun getCompleteLettersData(level: Int): Triple<String, String, List<String>> {
        return when (level) {
            1 -> Triple("C_SA", "A", listOf("A", "E", "O", "U"))
            2 -> Triple("P_LO", "E", listOf("A", "E", "I", "U"))
            3 -> Triple("LI_RO", "B", listOf("P", "B", "D", "T"))
            else -> Triple("M_NO", "A", listOf("A", "E", "I", "O"))
        }
    }

    private fun getCopyWordsData(level: Int): String {
        return when (level) {
            1 -> "CASA"
            2 -> "PERRO"
            3 -> "MAÑANA"
            else -> "HOLA"
        }
    }

    data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
}
