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

enum class MemoryType {
    CULTURAL, UTILITY, NEEDS, RECENT
}

data class MemoryUiState(
    val currentType: MemoryType = MemoryType.CULTURAL,
    val questionText: String = "",
    val options: List<String> = emptyList(),
    val correctAnswer: String = "",
    val isCorrect: Boolean? = null,
    val isCompleted: Boolean = false,
    val isSaving: Boolean = false,
    val currentLevel: Int = 1,
    val startTimeMs: Long = 0,
    val errorsCount: Int = 0,
    val currentStep: Int = 0,
    val totalSteps: Int = 4
)

@OptIn(kotlin.time.ExperimentalTime::class)
class MemoryViewModel(
    private val saveResultUseCase: SaveActivityResultUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(MemoryUiState())
    val uiState: StateFlow<MemoryUiState> = _uiState.asStateFlow()

    private val culturalQuestions = listOf(
        Triple("¿Cuál es la capital de España?", listOf("Madrid", "Barcelona", "Sevilla", "Valencia"), "Madrid"),
        Triple("¿Qué río pasa por Madrid?", listOf("Manzanares", "Tajo", "Ebro", "Duero"), "Manzanares"),
        Triple("¿En qué provincia está la Alhambra?", listOf("Granada", "Málaga", "Córdoba", "Sevilla"), "Granada"),
        Triple("¿Cuál es la capital de Francia?", listOf("París", "Lyon", "Marsella", "Niza"), "París")
    )

    private val utilityQuestions = listOf(
        Triple("¿Para qué se usa un peine?", listOf("Peinar el pelo", "Cortar papel", "Comer sopa", "Escribir"), "Peinar el pelo"),
        Triple("¿Para qué sirven las gafas?", listOf("Ver mejor", "Oír mejor", "Andar", "Oler"), "Ver mejor"),
        Triple("¿Para qué se usa una cuchara?", listOf("Comer sopa", "Peinarse", "Abrir puertas", "Coser"), "Comer sopa")
    )

    private val needsQuestions = listOf(
        Triple("¿Qué necesitas para cocinar una sopa?", listOf("Una olla", "Un martillo", "Un peine", "Unas llaves"), "Una olla"),
        Triple("¿Qué necesitas para escribir una carta?", listOf("Un bolígrafo", "Una sartén", "Un cepillo", "Una radio"), "Un bolígrafo"),
        Triple("¿Qué necesitas para regar las plantas?", listOf("Una regadera", "Un libro", "Un reloj", "Un espejo"), "Una regadera")
    )

    private val recentQuestions = listOf(
        Triple("¿Qué has desayunado hoy?", listOf("Leche con galletas", "Tostadas", "Fruta", "No recuerdo"), "Leche con galletas"), // Simplified for mock
        Triple("¿Qué tiempo hacía ayer?", listOf("Soleado", "Lluvioso", "Nublado", "Mucho viento"), "Soleado"),
        Triple("¿Has recibido alguna visita hoy?", listOf("Sí", "No", "No estoy seguro", "Mañana"), "Sí")
    )

    fun startNewGame(type: MemoryType, level: Int = 1) {
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
            MemoryType.CULTURAL -> culturalQuestions
            MemoryType.UTILITY -> utilityQuestions
            MemoryType.NEEDS -> needsQuestions
            MemoryType.RECENT -> recentQuestions
        }

        if (state.currentStep >= questions.size) {
            _uiState.update { it.copy(isCompleted = true) }
            return
        }

        val question = questions[state.currentStep]
        _uiState.update { it.copy(
            questionText = question.first,
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

    private fun totalQuestionsForType(type: MemoryType): Int = when(type) {
        MemoryType.CULTURAL -> culturalQuestions.size
        MemoryType.UTILITY -> utilityQuestions.size
        MemoryType.NEEDS -> needsQuestions.size
        MemoryType.RECENT -> recentQuestions.size
    }

    private fun saveResult(patientId: String, professionalId: String, appointmentId: String?) {
        val state = _uiState.value
        val endTime = kotlin.time.Clock.System.now().toEpochMilliseconds()
        val duration = ((endTime - state.startTimeMs) / 1000L).toInt()

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            val activityType = when(state.currentType) {
                MemoryType.CULTURAL -> "memory_cultural"
                MemoryType.UTILITY -> "memory_utility"
                MemoryType.NEEDS -> "memory_needs"
                MemoryType.RECENT -> "memory_recent"
            }
            val result = ActivityResult(
                id = "",
                patientId = patientId,
                professionalId = professionalId,
                appointmentId = appointmentId,
                activityType = activityType,
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
