package com.terapia.terasenior.ui.patient

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.terapia.terasenior.domain.usecase.patient.CreatePatientUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface CreatePatientUiState {
    data object Idle : CreatePatientUiState
    data object Loading : CreatePatientUiState
    data object Success : CreatePatientUiState
    data class Error(val message: String) : CreatePatientUiState
}

class CreatePatientViewModel(
    private val createPatientUseCase: CreatePatientUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<CreatePatientUiState>(CreatePatientUiState.Idle)
    val uiState: StateFlow<CreatePatientUiState> = _uiState.asStateFlow()

    fun createPatient(
        entityId: String,
        firstName: String,
        lastName: String,
        preferredName: String?,
        birthDate: String?,
        externalId: String?,
        admissionDate: String?,
        address: String?,
        phone: String?,
        contactName: String?,
        contactPhone: String?
    ) {
        viewModelScope.launch {
            _uiState.value = CreatePatientUiState.Loading
            createPatientUseCase(entityId, firstName, lastName, preferredName, birthDate, externalId, admissionDate, address, phone, contactName, contactPhone)
                .onSuccess {
                    _uiState.value = CreatePatientUiState.Success
                }
                .onFailure { error ->
                    _uiState.value = CreatePatientUiState.Error(error.message ?: "Error al crear paciente")
                }
        }
    }

    fun resetState() {
        _uiState.value = CreatePatientUiState.Idle
    }
}
