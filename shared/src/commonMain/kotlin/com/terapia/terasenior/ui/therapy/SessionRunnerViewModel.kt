package com.terapia.terasenior.ui.therapy

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.terapia.terasenior.domain.model.therapy.*
import com.terapia.terasenior.domain.repository.therapy.TherapySessionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed interface SessionRunnerUiState {
    data object Loading : SessionRunnerUiState
    data class Playing(
        val session: TherapySession,
        val exercises: List<TherapySessionExercise>,
        val currentIndex: Int = 0,
        val isPaused: Boolean = false,
        val showProfessionalPanel: Boolean = false
    ) : SessionRunnerUiState
    data class Transition(
        val nextExerciseName: String,
        val nextIndex: Int,
        val isFirst: Boolean = false
    ) : SessionRunnerUiState
    data class Summary(val session: TherapySession) : SessionRunnerUiState
    data object Finished : SessionRunnerUiState
    data class Error(val message: String) : SessionRunnerUiState
}

class SessionRunnerViewModel(
    private val sessionId: String,
    private val repository: TherapySessionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<SessionRunnerUiState>(SessionRunnerUiState.Loading)
    val uiState: StateFlow<SessionRunnerUiState> = _uiState.asStateFlow()

    init {
        loadSession()
    }

    private fun loadSession() {
        viewModelScope.launch {
            _uiState.value = SessionRunnerUiState.Loading
            
            val sessionResult = repository.getSessionDetails(sessionId)
            val exercisesResult = repository.getExercisesForSession(sessionId)

            if (sessionResult.isSuccess && exercisesResult.isSuccess) {
                val session = sessionResult.getOrThrow()
                val exercises = exercisesResult.getOrThrow()

                if (session != null && exercises.isNotEmpty()) {
                    _uiState.value = SessionRunnerUiState.Transition(
                        nextExerciseName = getExerciseDisplayName(exercises.first().exerciseType),
                        nextIndex = 0,
                        isFirst = true
                    )
                } else {
                    _uiState.value = SessionRunnerUiState.Error("No se encontraron ejercicios configurados.")
                }
            } else {
                _uiState.value = SessionRunnerUiState.Error("Error al cargar la sesión.")
            }
        }
    }

    fun startExercise(index: Int) {
        viewModelScope.launch {
            val session = repository.getSessionDetails(sessionId).getOrNull() ?: return@launch
            val exercises = repository.getExercisesForSession(sessionId).getOrDefault(emptyList())
            
            _uiState.value = SessionRunnerUiState.Playing(
                session = session,
                exercises = exercises,
                currentIndex = index
            )
            repository.updateSessionStatus(sessionId, SessionStatus.IN_PROGRESS.name)
        }
    }

    fun nextExercise() {
        val state = _uiState.value as? SessionRunnerUiState.Playing ?: return
        val nextIndex = state.currentIndex + 1

        if (nextIndex < state.exercises.size) {
            _uiState.value = SessionRunnerUiState.Transition(
                nextExerciseName = getExerciseDisplayName(state.exercises[nextIndex].exerciseType),
                nextIndex = nextIndex
            )
        } else {
            finishSession()
        }
    }

    fun toggleProfessionalPanel() {
        _uiState.update { state ->
            if (state is SessionRunnerUiState.Playing) {
                state.copy(showProfessionalPanel = !state.showProfessionalPanel)
            } else state
        }
    }

    fun logAssistance(type: AssistanceType, desc: String?) {
        val state = _uiState.value as? SessionRunnerUiState.Playing ?: return
        val exerciseId = state.exercises.getOrNull(state.currentIndex)?.id
        
        viewModelScope.launch {
            repository.logAssistance(
                AssistanceEvent(
                    id = "",
                    sessionId = sessionId,
                    exerciseId = exerciseId,
                    type = type,
                    description = desc,
                    occurredAt = ""
                )
            )
        }
    }

    fun logIncident(type: IncidentType, desc: String?) {
        val state = _uiState.value as? SessionRunnerUiState.Playing ?: return
        val exerciseId = state.exercises.getOrNull(state.currentIndex)?.id

        viewModelScope.launch {
            repository.logIncident(
                SessionIncident(
                    id = "",
                    sessionId = sessionId,
                    exerciseId = exerciseId,
                    type = type,
                    description = desc,
                    severity = "MEDIUM",
                    occurredAt = ""
                )
            )
        }
    }

    fun finishSession() {
        viewModelScope.launch {
            repository.updateSessionStatus(sessionId, SessionStatus.COMPLETED.name)
            val session = repository.getSessionDetails(sessionId).getOrNull()
            if (session != null) {
                _uiState.value = SessionRunnerUiState.Summary(session)
            } else {
                _uiState.value = SessionRunnerUiState.Finished
            }
        }
    }

    private fun getExerciseDisplayName(type: String): String = when(type) {
        "number_search" -> "Busca el Número"
        "memory_pairs" -> "Parejas de Figuras"
        "language_word_image" -> "Vocabulario"
        else -> type
    }
}
