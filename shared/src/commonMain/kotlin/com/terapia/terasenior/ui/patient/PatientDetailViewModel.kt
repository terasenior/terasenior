package com.terapia.terasenior.ui.patient

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.terapia.terasenior.domain.model.patient.Patient
import com.terapia.terasenior.domain.model.patient.TherapeuticProfile
import com.terapia.terasenior.domain.model.patient.Consent
import com.terapia.terasenior.domain.model.results.ActivityResult
import com.terapia.terasenior.domain.model.therapy.PatientSessionHistory
import com.terapia.terasenior.domain.repository.patient.PatientRepository
import com.terapia.terasenior.domain.repository.results.ResultsRepository
import com.terapia.terasenior.domain.repository.therapy.TherapySessionRepository
import com.terapia.terasenior.domain.usecase.patient.UpdatePatientUseCase
import com.terapia.terasenior.domain.usecase.patient.UpdateTherapeuticProfileUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

sealed interface PatientDetailUiState {
    data object Loading : PatientDetailUiState
    data class Success(
        val patient: Patient,
        val therapeuticProfile: TherapeuticProfile?,
        val consents: List<Consent> = emptyList(),
        val sessionsHistory: List<PatientSessionHistory> = emptyList(),
        val rawResults: List<ActivityResult> = emptyList(),
        val historyPage: Int = 1,
        val historyPageSize: Int = 10,
        val isUpdating: Boolean = false
    ) : PatientDetailUiState
    data class Error(val message: String) : PatientDetailUiState
}

class PatientDetailViewModel(
    private val patientId: String,
    private val repository: PatientRepository,
    private val resultsRepository: ResultsRepository,
    private val therapyRepository: TherapySessionRepository,
    private val updatePatientUseCase: UpdatePatientUseCase,
    private val updateTherapeuticProfileUseCase: UpdateTherapeuticProfileUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<PatientDetailUiState>(PatientDetailUiState.Loading)
    val uiState: StateFlow<PatientDetailUiState> = _uiState.asStateFlow()

    init {
        loadPatientData()
    }

    fun loadPatientData() {
        viewModelScope.launch {
            _uiState.value = PatientDetailUiState.Loading
            
            repository.logAccess(patientId, "ACCESS_DETAIL")

            val patientResult = repository.getPatientById(patientId)
            val profileResult = repository.getTherapeuticProfile(patientId)
            val consentsResult = repository.getConsents(patientId)
            val resultsResult = resultsRepository.getPatientResults(patientId).first()
            val sessionsResult = therapyRepository.getPatientSessions(patientId).first()

            patientResult.onSuccess { patient ->
                if (patient != null) {
                    val allResults = resultsResult.getOrDefault(emptyList())
                    val allSessions = sessionsResult.getOrDefault(emptyList())
                    
                    // Mapear resultados a sus sesiones
                    val history = allSessions.map { session ->
                        val sessionResults = allResults.filter { it.sessionId == session.id }
                        PatientSessionHistory(
                            session = session,
                            results = sessionResults,
                            groupedByCategory = groupResultsByCategory(sessionResults)
                        )
                    }

                    _uiState.value = PatientDetailUiState.Success(
                        patient = patient,
                        therapeuticProfile = profileResult.getOrNull(),
                        consents = consentsResult.getOrDefault(emptyList()),
                        sessionsHistory = history,
                        rawResults = allResults
                    )
                } else {
                    _uiState.value = PatientDetailUiState.Error("Paciente no encontrado")
                }
            }.onFailure { e ->
                _uiState.value = PatientDetailUiState.Error(e.message ?: "Error desconocido")
            }
        }
    }

    private fun groupResultsByCategory(results: List<ActivityResult>): Map<String, List<ActivityResult>> {
        return results.groupBy { result ->
            when {
                result.activityType.startsWith("orientation") -> "Orientación"
                result.activityType.startsWith("attention") || result.activityType == "number_search" -> "Atención"
                result.activityType.startsWith("memory") -> "Memoria"
                result.activityType.startsWith("language") -> "Lenguaje"
                result.activityType.startsWith("executive") || result.activityType.startsWith("calculation") -> "FF.EE. / Cálculo"
                result.activityType.startsWith("literacy") -> "Lectoescritura"
                else -> "Otros"
            }
        }
    }

    fun updatePatient(patient: Patient) {
        viewModelScope.launch {
            setUpdating(true)
            updatePatientUseCase(patient)
                .onSuccess { loadPatientData() }
                .onFailure { setUpdating(false) }
        }
    }

    fun updateClinicalProfile(profile: TherapeuticProfile) {
        viewModelScope.launch {
            setUpdating(true)
            updateTherapeuticProfileUseCase(profile)
                .onSuccess { loadPatientData() }
                .onFailure { setUpdating(false) }
        }
    }

    fun setHistoryPage(page: Int) {
        val current = _uiState.value
        if (current is PatientDetailUiState.Success) {
            _uiState.value = current.copy(historyPage = page)
        }
    }

    private fun setUpdating(updating: Boolean) {
        val current = _uiState.value
        if (current is PatientDetailUiState.Success) {
            _uiState.value = current.copy(isUpdating = updating)
        }
    }
}
