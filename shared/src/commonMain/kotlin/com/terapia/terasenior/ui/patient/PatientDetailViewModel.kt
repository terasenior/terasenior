package com.terapia.terasenior.ui.patient

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.terapia.terasenior.domain.model.patient.Consent
import com.terapia.terasenior.domain.model.patient.Patient
import com.terapia.terasenior.domain.model.patient.TherapeuticProfile
import com.terapia.terasenior.domain.repository.patient.PatientRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface PatientDetailUiState {
    data object Loading : PatientDetailUiState
    data class Success(
        val patient: Patient,
        val therapeuticProfile: TherapeuticProfile?,
        val consents: List<Consent> = emptyList()
    ) : PatientDetailUiState
    data class Error(val message: String) : PatientDetailUiState
}

class PatientDetailViewModel(
    private val patientId: String,
    private val repository: PatientRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<PatientDetailUiState>(PatientDetailUiState.Loading)
    val uiState: StateFlow<PatientDetailUiState> = _uiState.asStateFlow()

    init {
        loadPatientData()
    }

    fun loadPatientData() {
        viewModelScope.launch {
            _uiState.value = PatientDetailUiState.Loading
            
            // 1. Registrar el acceso en auditoría antes de cargar
            repository.logAccess(patientId, "ACCESS_DETAIL")

            val patientResult = repository.getPatientById(patientId)
            val profileResult = repository.getTherapeuticProfile(patientId)
            val consentsResult = repository.getConsents(patientId)

            patientResult.onSuccess { patient ->
                if (patient != null) {
                    _uiState.value = PatientDetailUiState.Success(
                        patient = patient,
                        therapeuticProfile = profileResult.getOrNull(),
                        consents = consentsResult.getOrDefault(emptyList())
                    )
                } else {
                    _uiState.value = PatientDetailUiState.Error("Paciente no encontrado")
                }
            }.onFailure { error ->
                _uiState.value = PatientDetailUiState.Error(error.message ?: "Error desconocido")
            }
        }
    }
}
