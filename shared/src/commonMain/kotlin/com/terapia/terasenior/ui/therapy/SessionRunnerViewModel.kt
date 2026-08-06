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
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

sealed interface SessionRunnerUiState {
    data object Loading : SessionRunnerUiState
    data class Playing(
        val session: TherapySession,
        val exercises: List<TherapySessionExercise>,
        val currentIndex: Int = 0,
        val isPaused: Boolean = false,
        val showProfessionalPanel: Boolean = false
    ) : SessionRunnerUiState
    data class Transition(val nextExerciseName: String) : SessionRunnerUiState
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
            repository.getSessionDetails(sessionId).onSuccess { session ->
                if (session != null) {
                    // En una app real, cargaríamos los ejercicios de la tabla exercises.
                    // Para esta fase, simulamos la carga de la configuración guardada.
                    _uiState.value = SessionRunnerUiState.Playing(
                        session = session,
                        exercises = emptyList() // Se llenará con la lógica de base de datos
                    )
                }
            }
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
        viewModelScope.launch {
            repository.logAssistance(
                AssistanceEvent(
                    id = "",
                    sessionId = sessionId,
                    exerciseId = null,
                    type = type,
                    description = desc,
                    occurredAt = ""
                )
            )
        }
    }

    fun logIncident(type: IncidentType, desc: String?) {
        val state = _uiState.value as? SessionRunnerUiState.Playing ?: return
        viewModelScope.launch {
            repository.logIncident(
                SessionIncident(
                    id = "",
                    sessionId = sessionId,
                    exerciseId = null,
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
            _uiState.value = SessionRunnerUiState.Finished
        }
    }
}
