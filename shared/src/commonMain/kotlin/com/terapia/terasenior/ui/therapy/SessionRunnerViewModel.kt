package com.terapia.terasenior.ui.therapy

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.terapia.terasenior.domain.model.agenda.AppointmentStatus
import com.terapia.terasenior.domain.model.therapy.*
import com.terapia.terasenior.domain.model.results.ActivityResult
import com.terapia.terasenior.domain.repository.agenda.AppointmentRepository
import com.terapia.terasenior.domain.repository.results.ResultsRepository
import com.terapia.terasenior.domain.repository.therapy.TherapySessionRepository
import com.terapia.terasenior.treatment.repository.TherapeuticExerciseCatalog
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ExerciseOutcome(
    val activityType: String,
    val area: String,
    val hits: Int,
    val errors: Int,
    val durationSeconds: Int
)

data class CognitiveAreaSummary(
    val area: String,
    val hits: Int,
    val errors: Int,
    val durationSeconds: Int,
    val activities: Int
)

sealed interface SessionRunnerUiState {
    data object Loading : SessionRunnerUiState
    data class Playing(
        val session: TherapySession,
        val exercises: List<TherapySessionExercise>,
        val currentIndex: Int = 0,
        val isPaused: Boolean = false,
        val showProfessionalPanel: Boolean = false,
        // v1.3.52: Acumuladores locales para sesiones sin paciente o fallos de red
        val accumulatedHits: Int = 0,
        val accumulatedErrors: Int = 0,
        val accumulatedDurationSeconds: Int = 0,
        val outcomes: List<ExerciseOutcome> = emptyList()
    ) : SessionRunnerUiState
    data class Transition(
        val nextExerciseName: String,
        val nextIndex: Int,
        val isFirst: Boolean = false
    ) : SessionRunnerUiState
    data class Summary(
        val session: TherapySession,
        val hits: Int,
        val errors: Int,
        val durationSeconds: Int,
        val resultsCount: Int = 0,
        val areaSummaries: List<CognitiveAreaSummary> = emptyList()
    ) : SessionRunnerUiState
    data object Finished : SessionRunnerUiState
    data class Error(val message: String) : SessionRunnerUiState
}

/**
 * SessionRunnerViewModel (v1.0.2 - Resync Trigger).
 */
class SessionRunnerViewModel(
    private val sessionId: String,
    private val repository: TherapySessionRepository,
    private val agendaRepository: AppointmentRepository,
    private val resultsRepository: ResultsRepository
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

    private var pendingHits = 0
    private var pendingErrors = 0
    private var pendingDuration = 0
    private var pendingOutcomes: List<ExerciseOutcome> = emptyList()

    fun startExercise(index: Int) {
        viewModelScope.launch {
            val session = repository.getSessionDetails(sessionId).getOrNull() ?: return@launch
            val exercises = repository.getExercisesForSession(sessionId).getOrDefault(emptyList())
            
            _uiState.value = SessionRunnerUiState.Playing(
                session = session,
                exercises = exercises,
                currentIndex = index,
                accumulatedHits = pendingHits,
                accumulatedErrors = pendingErrors,
                accumulatedDurationSeconds = pendingDuration,
                outcomes = pendingOutcomes
            )
            repository.updateSessionStatus(sessionId, SessionStatus.IN_PROGRESS.name)
        }
    }

    fun nextExercise(hits: Int = 0, errors: Int = 0, duration: Int = 0): Boolean {
        val state = _uiState.value as? SessionRunnerUiState.Playing ?: return false
        val currentExercise = state.exercises.getOrNull(state.currentIndex) ?: run {
            _uiState.value = SessionRunnerUiState.Error("No se pudo identificar la actividad terminada.")
            return false
        }
        
        val newAccumulatedHits = state.accumulatedHits + hits
        val newAccumulatedErrors = state.accumulatedErrors + errors
        val newAccumulatedDuration = state.accumulatedDurationSeconds + duration
        val newOutcomes = state.outcomes + ExerciseOutcome(
            activityType = currentExercise.exerciseType,
            area = cognitiveArea(currentExercise.exerciseType),
            hits = hits.coerceAtLeast(0),
            errors = errors.coerceAtLeast(0),
            durationSeconds = duration.coerceAtLeast(1)
        )
        
        val nextIndex = state.currentIndex + 1

        if (nextIndex < state.exercises.size) {
            _uiState.value = SessionRunnerUiState.Transition(
                nextExerciseName = getExerciseDisplayName(state.exercises[nextIndex].exerciseType),
                nextIndex = nextIndex
            )
            
            pendingHits = newAccumulatedHits
            pendingErrors = newAccumulatedErrors
            pendingDuration = newAccumulatedDuration
            pendingOutcomes = newOutcomes
        } else {
            val areaSummaries = try {
                newOutcomes.groupBy { it.area }.map { (area, results) ->
                    CognitiveAreaSummary(
                        area = area,
                        hits = results.sumOf { it.hits },
                        errors = results.sumOf { it.errors },
                        durationSeconds = results.sumOf { it.durationSeconds },
                        activities = results.size
                    )
                }.sortedBy { it.area }
            } catch (_: Throwable) {
                listOf(CognitiveAreaSummary(
                    area = "Resultados de la sesión",
                    hits = newAccumulatedHits,
                    errors = newAccumulatedErrors,
                    durationSeconds = newAccumulatedDuration,
                    activities = newOutcomes.size
                ))
            }

            _uiState.value = SessionRunnerUiState.Summary(
                session = state.session,
                hits = newAccumulatedHits,
                errors = newAccumulatedErrors,
                durationSeconds = newAccumulatedDuration,
                resultsCount = newOutcomes.size,
                areaSummaries = areaSummaries
            )

            viewModelScope.launch {
                delay(2000)
                val resultsResult = resultsRepository.getSessionResults(sessionId)
                val dbResults = resultsResult.getOrDefault(emptyList())
                saveMissingPatientResults(state.session, newOutcomes, dbResults.map { it.activityType }.toSet())
            }
        }
        return true
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
        notes: String,
        hits: Int,
        errors: Int,
        duration: Int
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
                therapistNotes = notes,
                totalHits = hits,
                totalErrors = errors,
                totalDurationSeconds = duration
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

    private fun getExerciseDisplayName(type: String): String =
        TherapeuticExerciseCatalog.find(type)?.name ?: ExerciseTranslationUtils.getDisplayName(type)

    private suspend fun saveMissingPatientResults(
        session: TherapySession,
        outcomes: List<ExerciseOutcome>,
        existingActivityTypes: Set<String>
    ) {
        val patientId = session.patientId ?: return
        outcomes.filter { it.activityType !in existingActivityTypes }.forEach { outcome ->
            val attempts = outcome.hits + outcome.errors
            resultsRepository.saveResult(ActivityResult(
                id = "", patientId = patientId, professionalId = session.therapistId,
                appointmentId = session.appointmentId, sessionId = session.id,
                activityType = outcome.activityType,
                score = if (attempts == 0) 0 else (outcome.hits * 100 / attempts).coerceIn(0, 100),
                durationSeconds = outcome.durationSeconds, errorsCount = outcome.errors,
                difficultyLevel = "SESSION", createdAt = ""
            ))
        }
    }

    private fun cognitiveArea(activityType: String): String = when {
        activityType.startsWith("orientation") -> "Orientación"
        activityType.startsWith("attention") || activityType == "number_search" -> "Atención"
        activityType.startsWith("memory") -> "Memoria"
        activityType.startsWith("language") -> "Lenguaje"
        activityType.startsWith("executive") || activityType.startsWith("calculation") -> "Funciones ejecutivas"
        activityType.startsWith("guided_executive_") -> "Funciones ejecutivas"
        activityType.startsWith("perception") -> "Percepción"
        activityType.startsWith("literacy") -> "Lectoescritura"
        else -> "Otros"
    }
}
