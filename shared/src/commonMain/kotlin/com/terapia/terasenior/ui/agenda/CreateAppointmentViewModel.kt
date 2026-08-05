package com.terapia.terasenior.ui.agenda

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.terapia.terasenior.domain.model.agenda.Appointment
import com.terapia.terasenior.domain.model.agenda.AppointmentStatus
import com.terapia.terasenior.domain.model.agenda.AppointmentType
import com.terapia.terasenior.domain.model.patient.Patient
import com.terapia.terasenior.domain.model.admin.Entity
import com.terapia.terasenior.domain.model.admin.UserProfile
import com.terapia.terasenior.domain.repository.agenda.AppointmentRepository
import com.terapia.terasenior.domain.repository.admin.EntityRepository
import com.terapia.terasenior.domain.repository.admin.UserProfileRepository
import com.terapia.terasenior.domain.repository.patient.PatientRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.datetime.*

sealed interface CreateAppointmentUiState {
    data object Idle : CreateAppointmentUiState
    data object Loading : CreateAppointmentUiState
    data class Success(
        val patients: List<Patient>,
        val professionals: List<UserProfile>,
        val entities: List<Entity> = emptyList()
    ) : CreateAppointmentUiState
    data class Error(val message: String) : CreateAppointmentUiState
    data object Created : CreateAppointmentUiState
}

class CreateAppointmentViewModel(
    private val agendaRepository: AppointmentRepository,
    private val patientRepository: PatientRepository,
    private val userRepository: UserProfileRepository,
    private val entityRepository: EntityRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<CreateAppointmentUiState>(CreateAppointmentUiState.Idle)
    val uiState: StateFlow<CreateAppointmentUiState> = _uiState.asStateFlow()

    fun loadInitialData() {
        viewModelScope.launch {
            _uiState.value = CreateAppointmentUiState.Loading
            
            val patientsResult = patientRepository.getPatients().first()
            val professionalsResult = userRepository.getUserProfiles(null)
            val entitiesResult = entityRepository.getEntities()

            if (patientsResult.isSuccess && professionalsResult.isSuccess && entitiesResult.isSuccess) {
                _uiState.value = CreateAppointmentUiState.Success(
                    patients = patientsResult.getOrDefault(emptyList()),
                    professionals = professionalsResult.getOrDefault(emptyList()),
                    entities = entitiesResult.getOrDefault(emptyList())
                )
            } else {
                _uiState.value = CreateAppointmentUiState.Error("No se pudieron cargar los datos necesarios.")
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
        if (entityId.isBlank()) {
            _uiState.value = CreateAppointmentUiState.Error("Error: El ID del centro no puede estar vacío.")
            return
        }

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
                    _uiState.value = CreateAppointmentUiState.Error(error.message ?: "Error al programar la sesión")
                }
        }
    }

    fun resetState() {
        _uiState.value = CreateAppointmentUiState.Idle
    }
}
