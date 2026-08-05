package com.terapia.terasenior.ui.agenda

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.terapia.terasenior.domain.model.agenda.Appointment
import com.terapia.terasenior.domain.model.agenda.AppointmentStatus
import com.terapia.terasenior.domain.model.agenda.AppointmentType
import com.terapia.terasenior.domain.model.patient.Patient
import com.terapia.terasenior.domain.model.admin.UserProfile
import com.terapia.terasenior.domain.repository.agenda.AppointmentRepository
import com.terapia.terasenior.domain.repository.admin.UserProfileRepository
import com.terapia.terasenior.domain.repository.patient.PatientRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.datetime.*

sealed interface CreateAppointmentUiState {
    data object Loading : CreateAppointmentUiState
    data class Success(
        val patients: List<Patient>,
        val professionals: List<UserProfile>
    ) : CreateAppointmentUiState
    data class Error(val message: String) : CreateAppointmentUiState
    data object Created : CreateAppointmentUiState
}

class CreateAppointmentViewModel(
    private val agendaRepository: AppointmentRepository,
    private val patientRepository: PatientRepository,
    private val userRepository: UserProfileRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<CreateAppointmentUiState>(CreateAppointmentUiState.Loading)
    val uiState: StateFlow<CreateAppointmentUiState> = _uiState.asStateFlow()

    private var _patients = emptyList<Patient>()
    private var _professionals = emptyList<UserProfile>()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            _uiState.value = CreateAppointmentUiState.Loading
            
            // Cargamos pacientes y profesionales en paralelo
            val patientsResult = patientRepository.getPatients().first()
            val professionalsResult = userRepository.getUserProfiles(null)

            if (patientsResult.isSuccess && professionalsResult.isSuccess) {
                _patients = patientsResult.getOrDefault(emptyList())
                _professionals = professionalsResult.getOrDefault(emptyList())
                _uiState.value = CreateAppointmentUiState.Success(_patients, _professionals)
            } else {
                _uiState.value = CreateAppointmentUiState.Error("No se pudieron cargar los datos para la sesión.")
            }
        }
    }

    fun createAppointment(
        entityId: String,
        title: String,
        description: String?,
        startDate: LocalDate,
        startTime: LocalTime,
        endTime: LocalTime,
        type: AppointmentType,
        selectedStaffIds: List<String>,
        selectedPatientIds: List<String>
    ) {
        viewModelScope.launch {
            _uiState.value = CreateAppointmentUiState.Loading
            
            val startInstant = startDate.atTime(startTime).toInstant(TimeZone.currentSystemDefault())
            val endInstant = startDate.atTime(endTime).toInstant(TimeZone.currentSystemDefault())

            val appointment = Appointment(
                id = "",
                entityId = entityId,
                title = title,
                description = description,
                startAt = startInstant.toString(),
                endAt = endInstant.toString(),
                type = type,
                status = AppointmentStatus.SCHEDULED
            )

            agendaRepository.createFullAppointment(appointment, selectedStaffIds, selectedPatientIds)
                .onSuccess {
                    _uiState.value = CreateAppointmentUiState.Created
                }
                .onFailure { error ->
                    _uiState.value = CreateAppointmentUiState.Error(error.message ?: "Error al crear la sesión")
                }
        }
    }
}
