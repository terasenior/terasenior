package com.terapia.terasenior.treatment.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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

enum class PerceptionType {
    LATERAL_DOMINANCE, MIRROR, BODY_PARTS
}

data class PerceptionUiState(
    val currentType: PerceptionType = PerceptionType.LATERAL_DOMINANCE,
    val questionText: String = "",
    val stimulus: PerceptionStimulus? = null,
    val options: List<String> = emptyList(),
    val correctAnswer: String = "",
    val isCorrect: Boolean? = null,
    val isCompleted: Boolean = false,
    val isSaving: Boolean = false,
    val currentLevel: Int = 1,
    val startTimeMs: Long = 0,
    val errorsCount: Int = 0,
    val currentStep: Int = 0,
    val totalSteps: Int = 3
)

sealed interface PerceptionStimulus {
    data class ColorCircle(val color: androidx.compose.ui.graphics.Color, val side: String) : PerceptionStimulus
    data class Shape(val icon: androidx.compose.ui.graphics.vector.ImageVector, val color: androidx.compose.ui.graphics.Color, val side: String) : PerceptionStimulus
    data class Text(val text: String, val isMirror: Boolean = false) : PerceptionStimulus
    data class Image(val imageUrl: String) : PerceptionStimulus
}


@OptIn(kotlin.time.ExperimentalTime::class)
class PerceptionViewModel(
    private val saveResultUseCase: SaveActivityResultUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(PerceptionUiState())
    val uiState: StateFlow<PerceptionUiState> = _uiState.asStateFlow()

    private val lateralQuestions = listOf(
        Triple("¿De qué lado está el círculo rojo?", listOf("Izquierda", "Derecha"), "Derecha"),
        Triple("¿De qué lado está el cuadrado azul?", listOf("Izquierda", "Derecha"), "Izquierda"),
        Triple("¿De qué lado está la estrella amarilla?", listOf("Izquierda", "Derecha"), "Derecha")
    )

    private val lateralStimuli = listOf(
        PerceptionStimulus.ColorCircle(androidx.compose.ui.graphics.Color.Red, "Derecha"),
        PerceptionStimulus.Shape(Icons.Default.AccountBox, androidx.compose.ui.graphics.Color.Blue, "Izquierda"),
        PerceptionStimulus.Shape(Icons.Default.Favorite, androidx.compose.ui.graphics.Color(0xFFFFC107), "Derecha")
    )

    private val mirrorQuestions = listOf(
        Triple("Si ves una 'b' en el espejo, ¿qué letra parece?", listOf("d", "p", "q", "b"), "d"),
        Triple("Si levantas la mano derecha frente al espejo, ¿qué mano levanta tu reflejo?", listOf("Izquierda", "Derecha"), "Izquierda"),
        Triple("¿Cuál es el reflejo de la palabra 'AMA'?", listOf("AMA", "OMA", "AMI", "EMA"), "AMA")
    )

    private val mirrorStimuli = listOf(
        PerceptionStimulus.Text("b", isMirror = true),
        null,
        PerceptionStimulus.Text("AMA", isMirror = true)
    )

    private val bodyPartsQuestions = listOf(
        Triple("¿Con qué parte del cuerpo hueles las flores?", listOf("Nariz", "Boca", "Ojos", "Orejas"), "Nariz"),
        Triple("¿Cómo se llama la parte del brazo que se dobla?", listOf("Codo", "Rodilla", "Muñeca", "Hombro"), "Codo"),
        Triple("¿Cuántos dedos tenemos en una mano normal?", listOf("5", "4", "6", "10"), "5")
    )

    private val bodyPartsStimuli = listOf(
        PerceptionStimulus.Image("https://images.unsplash.com/photo-1544367567-0f2fcb009e0b?w=400"), // Yoga/Body
        null,
        null
    )

    fun startNewGame(type: PerceptionType, level: Int = 1) {
        _uiState.update { it.copy(
            currentType = type,
            currentLevel = level,
            startTimeMs = kotlin.time.Clock.System.now().toEpochMilliseconds(),
            isCompleted = false,
            errorsCount = 0,
            currentStep = 0
        ) }
        setupQuestion()
    }

    private fun setupQuestion() {
        val state = _uiState.value
        val questions = when(state.currentType) {
            PerceptionType.LATERAL_DOMINANCE -> lateralQuestions
            PerceptionType.MIRROR -> mirrorQuestions
            PerceptionType.BODY_PARTS -> bodyPartsQuestions
        }
        val stimuli = when(state.currentType) {
            PerceptionType.LATERAL_DOMINANCE -> lateralStimuli
            PerceptionType.MIRROR -> mirrorStimuli
            PerceptionType.BODY_PARTS -> bodyPartsStimuli
        }

        if (state.currentStep >= questions.size) {
            _uiState.update { it.copy(isCompleted = true) }
            return
        }

        val question = questions[state.currentStep]
        val stimulus = stimuli.getOrNull(state.currentStep)
        
        _uiState.update { it.copy(
            questionText = question.first,
            stimulus = stimulus,
            options = question.second.shuffled(),
            correctAnswer = question.third,
            isCorrect = null,
            totalSteps = questions.size
        ) }
    }

    fun onOptionSelected(selected: String, patientId: String?, professionalId: String?, appointmentId: String?) {
        val state = _uiState.value
        if (state.isCorrect == true || state.isCompleted) return

        if (selected == state.correctAnswer) {
            _uiState.update { it.copy(isCorrect = true) }
            viewModelScope.launch {
                delay(1500)
                val nextStep = state.currentStep + 1
                _uiState.update { it.copy(currentStep = nextStep) }
                if (nextStep >= totalQuestionsForType(state.currentType)) {
                    _uiState.update { it.copy(isCompleted = true) }
                    if (patientId != null && professionalId != null) {
                        saveResult(patientId, professionalId, appointmentId)
                    }
                } else {
                    setupQuestion()
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

    private fun totalQuestionsForType(type: PerceptionType): Int = when(type) {
        PerceptionType.LATERAL_DOMINANCE -> lateralQuestions.size
        PerceptionType.MIRROR -> mirrorQuestions.size
        PerceptionType.BODY_PARTS -> bodyPartsQuestions.size
    }

    private fun saveResult(patientId: String, professionalId: String, appointmentId: String?) {
        val state = _uiState.value
        val endTime = kotlin.time.Clock.System.now().toEpochMilliseconds()
        val duration = ((endTime - state.startTimeMs) / 1000L).toInt()

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            val activityType = when(state.currentType) {
                PerceptionType.LATERAL_DOMINANCE -> "perception_lateral_dominance"
                PerceptionType.MIRROR -> "perception_mirror"
                PerceptionType.BODY_PARTS -> "perception_body_parts"
            }
            val result = ActivityResult(
                id = "",
                patientId = patientId,
                professionalId = professionalId,
                appointmentId = appointmentId,
                activityType = activityType,
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
