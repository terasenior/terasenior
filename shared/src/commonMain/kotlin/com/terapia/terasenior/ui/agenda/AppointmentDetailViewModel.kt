package com.terapia.terasenior.ui.agenda

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.terapia.terasenior.domain.model.agenda.Appointment
import com.terapia.terasenior.domain.model.agenda.AppointmentAttendee
import com.terapia.terasenior.domain.model.agenda.AppointmentStatus
import com.terapia.terasenior.domain.model.agenda.AttendanceStatus
import com.terapia.terasenior.domain.repository.agenda.AppointmentRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface AppointmentDetailUiState {
    data object Loading : AppointmentDetailUiState
    data class Success(
        val appointment: Appointment,
        val attendees: List<AppointmentAttendee>,
        val isSaving: Boolean = false
    ) : AppointmentDetailUiState
    data class Error(val message: String) : AppointmentDetailUiState
}

class AppointmentDetailViewModel(
    private val appointmentId: String,
    private val repository: AppointmentRepository
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

            if (appointmentResult.isSuccess && attendeesResult.isSuccess) {
                _uiState.value = AppointmentDetailUiState.Success(
                    appointment = appointmentResult.getOrThrow()!!,
                    attendees = attendeesResult.getOrThrow()
                )
            } else {
                _uiState.value = AppointmentDetailUiState.Error("Error al cargar los detalles de la sesión.")
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
