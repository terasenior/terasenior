package com.terapia.terasenior.ui.agenda

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.terapia.terasenior.domain.model.agenda.Appointment
import com.terapia.terasenior.domain.model.agenda.AppointmentAttendee
import com.terapia.terasenior.domain.model.agenda.AppointmentStatus
import com.terapia.terasenior.domain.model.agenda.AttendanceStatus
import com.terapia.terasenior.domain.model.patient.Patient
import com.terapia.terasenior.domain.repository.agenda.AppointmentRepository
import com.terapia.terasenior.domain.repository.patient.PatientRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed interface AppointmentDetailUiState {
    data object Loading : AppointmentDetailUiState
    data class Success(
        val appointment: Appointment,
        val attendees: List<AppointmentAttendee>,
        val allPatients: List<Patient> = emptyList(),
        val allAppointments: List<Appointment> = emptyList(), // Para conflictos
        val isSaving: Boolean = false,
        val isDeleted: Boolean = false
    ) : AppointmentDetailUiState
    data class Error(val message: String) : AppointmentDetailUiState
}

class AppointmentDetailViewModel(
    private val appointmentId: String,
    private val repository: AppointmentRepository,
    private val patientRepository: PatientRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<AppointmentDetailUiState>(AppointmentDetailUiState.Loading)
    val uiState: StateFlow<AppointmentDetailUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            _uiState.value = AppointmentDetailUiState.Loading
            
            val appointmentResult = repository.getAppointmentById(appointmentId)
            val attendeesResult = repository.getAttendees(appointmentId)
            val patientsResult = patientRepository.getPatients().first()
            val allApptsResult = repository.getAppointments().first()

            if (appointmentResult.isSuccess && attendeesResult.isSuccess) {
                val appt = appointmentResult.getOrThrow()
                if (appt != null) {
                    _uiState.value = AppointmentDetailUiState.Success(
                        appointment = appt,
                        attendees = attendeesResult.getOrThrow(),
                        allPatients = patientsResult.getOrDefault(emptyList()),
                        allAppointments = allApptsResult.getOrDefault(emptyList())
                    )
                } else {
                    _uiState.value = AppointmentDetailUiState.Error("La sesión ya no existe.")
                }
            } else {
                _uiState.value = AppointmentDetailUiState.Error("Error al cargar los detalles de la sesión.")
            }
        }
    }

    fun deleteSession() {
        viewModelScope.launch {
            _uiState.value = AppointmentDetailUiState.Loading
            repository.deleteAppointment(appointmentId).onSuccess {
                _uiState.value = AppointmentDetailUiState.Success(
                    appointment = Appointment("", "", "", "", "", "", com.terapia.terasenior.domain.model.agenda.AppointmentType.INDIVIDUAL, AppointmentStatus.CANCELLED),
                    attendees = emptyList(),
                    isDeleted = true
                )
            }.onFailure { e ->
                _uiState.value = AppointmentDetailUiState.Error("No se pudo eliminar la sesión: ${e.message}")
            }
        }
    }

    fun updateFullSession(
        updatedAppointment: Appointment,
        patientIds: List<String>
    ) {
        viewModelScope.launch {
            val current = _uiState.value
            if (current is AppointmentDetailUiState.Success) {
                _uiState.value = current.copy(isSaving = true)
            }
            
            repository.updateFullAppointment(updatedAppointment, emptyList(), patientIds)
                .onSuccess { loadData() }
                .onFailure { e ->
                    val afterError = _uiState.value
                    if (afterError is AppointmentDetailUiState.Success) {
                        _uiState.value = afterError.copy(isSaving = false)
                    }
                }
        }
    }

    fun updateAttendance(attendeeId: String, status: AttendanceStatus, notes: String?) {
        viewModelScope.launch {
            repository.updateAttendeeStatus(attendeeId, status.name, notes)
                .onSuccess { loadData() }
        }
    }

    fun markAsMissed() {
        val currentState = _uiState.value
        if (currentState is AppointmentDetailUiState.Success) {
            viewModelScope.launch {
                val updated = currentState.appointment.copy(status = AppointmentStatus.MISSED)
                repository.updateAppointment(updated)
                    .onSuccess { loadData() }
            }
        }
    }

    fun completeSession() {
        val currentState = _uiState.value
        if (currentState is AppointmentDetailUiState.Success) {
            viewModelScope.launch {
                val updated = currentState.appointment.copy(status = AppointmentStatus.COMPLETED)
                repository.updateAppointment(updated)
                    .onSuccess { loadData() }
            }
        }
    }
}
