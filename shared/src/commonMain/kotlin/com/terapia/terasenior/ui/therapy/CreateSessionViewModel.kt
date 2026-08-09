package com.terapia.terasenior.ui.therapy

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.terapia.terasenior.domain.model.agenda.Appointment
import com.terapia.terasenior.domain.model.patient.Patient
import com.terapia.terasenior.domain.model.therapy.ExerciseConfig
import com.terapia.terasenior.domain.model.therapy.SessionMode
import com.terapia.terasenior.domain.model.therapy.SessionStatus
import com.terapia.terasenior.domain.model.therapy.TherapySession
import com.terapia.terasenior.domain.model.therapy.TherapySessionExercise
import com.terapia.terasenior.domain.repository.patient.PatientRepository
import com.terapia.terasenior.domain.repository.therapy.TherapySessionRepository
import com.terapia.terasenior.ui.therapy.ExerciseTranslationUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class WizardStep {
    MODE_SELECTION, PATIENT_SELECTION, CATEGORY_SELECTION, EXERCISE_SELECTION, LEVEL_SELECTION, SUMMARY
}

data class CreateSessionUiState(
    val currentStep: WizardStep = WizardStep.MODE_SELECTION,
    val mode: SessionMode? = null,
    val selectedPatient: Patient? = null,
    val selectedAppointmentId: String? = null,
    val selectedCategory: String? = null,
    val selectedExercises: List<ExerciseConfig> = emptyList(),
    val patients: List<Patient> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val createdSessionId: String? = null
)

class CreateSessionViewModel(
    private val therapyRepository: TherapySessionRepository,
    private val patientRepository: PatientRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CreateSessionUiState())
    val uiState: StateFlow<CreateSessionUiState> = _uiState.asStateFlow()

    fun onModeSelected(mode: SessionMode) {
        _uiState.update { it.copy(
            mode = mode,
            currentStep = if (mode == SessionMode.WITHOUT_PATIENT) WizardStep.CATEGORY_SELECTION else WizardStep.PATIENT_SELECTION
        ) }
        
        if (mode == SessionMode.WITH_PATIENT) {
            loadPatients()
        }
    }

    private fun loadPatients() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            patientRepository.getPatients().collect { result ->
                result.onSuccess { list ->
                    _uiState.update { it.copy(patients = list, isLoading = false) }
                }.onFailure { e ->
                    _uiState.update { it.copy(error = e.message, isLoading = false) }
                }
            }
        }
    }

    fun onPatientSelected(patient: Patient) {
        _uiState.update { it.copy(selectedPatient = patient, currentStep = WizardStep.CATEGORY_SELECTION) }
    }

    fun startFromAppointment(appointment: Appointment, patient: Patient?) {
        val planned = appointment.plannedExercises
        
        _uiState.update { it.copy(
            mode = SessionMode.FROM_APPOINTMENT,
            selectedAppointmentId = appointment.id,
            selectedPatient = patient,
            selectedExercises = planned,
            currentStep = if (patient != null) {
                if (planned.isNotEmpty()) WizardStep.SUMMARY else WizardStep.CATEGORY_SELECTION
            } else {
                WizardStep.PATIENT_SELECTION
            }
        ) }
        if (patient == null) loadPatients()
    }

    fun onCategorySelected(category: String) {
        _uiState.update { it.copy(selectedCategory = category, currentStep = WizardStep.EXERCISE_SELECTION) }
    }

    fun toggleExercise(type: String, name: String, category: String, description: String) {
        _uiState.update { state ->
            val current = state.selectedExercises
            val updated = if (current.any { it.type == type }) {
                current.filter { it.type != type }
            } else {
                current + ExerciseConfig(type, name, category, description = description)
            }
            state.copy(selectedExercises = updated)
        }
    }

    fun onLevelSelected(level: Int) {
        _uiState.update { state ->
            val updated = state.selectedExercises.map { it.copy(level = level) }
            state.copy(selectedExercises = updated, currentStep = WizardStep.SUMMARY)
        }
    }

    fun goNextFromExercises() {
        if (_uiState.value.selectedExercises.isNotEmpty()) {
            _uiState.update { it.copy(currentStep = WizardStep.LEVEL_SELECTION) }
        }
    }

    fun goBack() {
        val currentState = _uiState.value
        val previousStep = when (currentState.currentStep) {
            WizardStep.MODE_SELECTION -> WizardStep.MODE_SELECTION
            WizardStep.PATIENT_SELECTION -> WizardStep.MODE_SELECTION
            WizardStep.CATEGORY_SELECTION -> if (currentState.mode == SessionMode.WITHOUT_PATIENT || currentState.mode == SessionMode.FROM_APPOINTMENT) WizardStep.MODE_SELECTION else WizardStep.PATIENT_SELECTION
            WizardStep.EXERCISE_SELECTION -> WizardStep.CATEGORY_SELECTION
            WizardStep.LEVEL_SELECTION -> WizardStep.EXERCISE_SELECTION
            WizardStep.SUMMARY -> WizardStep.LEVEL_SELECTION
        }
        _uiState.update { it.copy(currentStep = previousStep) }
    }

    fun resetWizard() {
        _uiState.value = CreateSessionUiState()
    }

    fun createSession(therapistId: String) {
        val state = _uiState.value
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            val session = TherapySession(
                id = "",
                title = "Sesión Combinada",
                description = "Intervención de estimulación cognitiva",
                therapistId = therapistId,
                patientId = state.selectedPatient?.id,
                appointmentId = state.selectedAppointmentId,
                mode = state.mode ?: SessionMode.WITHOUT_PATIENT,
                status = SessionStatus.READY,
                startedAt = null,
                finishedAt = null,
                createdAt = ""
            )

            therapyRepository.createSession(session).onSuccess { sessionId ->
                state.selectedExercises.forEachIndexed { index, config ->
                    val exercise = TherapySessionExercise(
                        id = "",
                        sessionId = sessionId,
                        exerciseType = config.type,
                        level = config.level,
                        position = index
                    )
                    therapyRepository.addExerciseToSession(exercise)
                }
                _uiState.update { it.copy(createdSessionId = sessionId, isLoading = false) }
            }.onFailure { e ->
                _uiState.update { it.copy(error = e.message, isLoading = false) }
            }
        }
    }
}
