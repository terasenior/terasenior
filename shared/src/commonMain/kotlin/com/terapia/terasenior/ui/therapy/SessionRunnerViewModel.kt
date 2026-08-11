package com.terapia.terasenior.ui.therapy

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.terapia.terasenior.domain.model.agenda.AppointmentStatus
import com.terapia.terasenior.domain.model.therapy.*
import com.terapia.terasenior.domain.repository.agenda.AppointmentRepository
import com.terapia.terasenior.domain.repository.therapy.TherapySessionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
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

/**
 * SessionRunnerViewModel (v1.0.2 - Resync Trigger).
 */
class SessionRunnerViewModel(
    private val sessionId: String,
    private val repository: TherapySessionRepository,
    private val agendaRepository: AppointmentRepository
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
            // En lugar de cerrar directo, vamos al Summary
            _uiState.value = SessionRunnerUiState.Summary(state.session)
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

    fun finishSession(
        participation: String,
        fatigue: String,
        notes: String
    ) {
        viewModelScope.launch {
            _uiState.value = SessionRunnerUiState.Loading
            
            // 1. Recuperar sesión fresca
            val currentSession = repository.getSessionDetails(sessionId).getOrNull() 
                ?: return@launch _uiState.update { SessionRunnerUiState.Error("No se pudo recuperar la sesión para cerrar.") }
            
            val updatedSession = currentSession.copy(
                status = SessionStatus.COMPLETED,
                participationLevel = participation,
                fatigueLevel = fatigue,
                therapistNotes = notes
            )
            
            // 2. Guardar cierre de sesión
            repository.saveSessionClosing(updatedSession).onSuccess { 
                
                // 3. Si viene de una cita, actualizar estado de la cita
                currentSession.appointmentId?.let { apptId ->
                    agendaRepository.getAppointmentById(apptId).onSuccess { appt ->
                        appt?.let {
                            agendaRepository.updateAppointment(it.copy(status = AppointmentStatus.COMPLETED))
                            
                            // 4. Marcar asistencia por defecto como PRESENT para los que no estén ABSENT
                            agendaRepository.getAttendees(apptId).onSuccess { attendees ->
                                attendees.forEach { attendee ->
                                    if (attendee.status == com.terapia.terasenior.domain.model.agenda.AttendanceStatus.PENDING) {
                                        agendaRepository.updateAttendeeStatus(attendee.id, "PRESENT", null)
                                    }
                                }
                            }
                        }
                    }
                }
                
                // 5. Finalizar flujo
                _uiState.value = SessionRunnerUiState.Finished 
                
            }.onFailure { e ->
                _uiState.value = SessionRunnerUiState.Error("Error al guardar la sesión clínica: ${e.message}. Asegúrate de tener las columnas participation_level, fatigue_level y therapist_notes en Supabase.")
            }
        }
    }

    fun abortSession() {
        viewModelScope.launch {
            repository.updateSessionStatus(sessionId, SessionStatus.CANCELLED.name)
            _uiState.value = SessionRunnerUiState.Finished
        }
    }

    private fun getExerciseDisplayName(type: String): String = ExerciseTranslationUtils.getDisplayName(type)
}
