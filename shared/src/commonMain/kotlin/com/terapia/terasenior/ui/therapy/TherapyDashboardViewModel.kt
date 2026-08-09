package com.terapia.terasenior.ui.therapy

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.terapia.terasenior.domain.model.agenda.Appointment
import com.terapia.terasenior.domain.repository.agenda.AppointmentRepository
import com.terapia.terasenior.domain.repository.therapy.TherapySessionRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.datetime.*

data class DashboardUiState(
    val summary: Map<String, Int> = emptyMap(),
    val todayAppointments: List<Appointment> = emptyList(),
    val todayPatients: List<Pair<String, String>> = emptyList(), // ID y Nombre
    val isLoading: Boolean = false,
    val error: String? = null
)

class TherapyDashboardViewModel(
    private val repository: TherapySessionRepository,
    private val agendaRepository: AppointmentRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    @OptIn(kotlin.time.ExperimentalTime::class)
    fun loadDashboard(therapistId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            val summaryResult = repository.getTherapistSummary(therapistId)
            
            // 1. Cargar Citas de Hoy
            val now = kotlin.time.Clock.System.now()
            val today = now.toLocalDateTime(TimeZone.currentSystemDefault()).date
            val appointmentsResult = agendaRepository.getAppointments().first()
            
            val todayAppts = appointmentsResult.getOrDefault(emptyList()).filter { 
                try {
                    val start = Instant.parse(it.startAt).toLocalDateTime(TimeZone.currentSystemDefault()).date
                    start == today
                } catch(e: Exception) { false }
            }

            // 2. Cargar pacientes de hoy con sus IDs para navegación
            val patientsInfo = mutableListOf<Pair<String, String>>()
            todayAppts.forEach { appt ->
                agendaRepository.getAttendees(appt.id).onSuccess { attendees ->
                    attendees.forEach { patientsInfo.add(it.patientId to it.patientName) }
                }
            }

            // Actualizamos el estado con los datos de hoy
            _uiState.update { state ->
                state.copy(
                    summary = summaryResult.getOrDefault(emptyMap()),
                    todayAppointments = todayAppts,
                    todayPatients = patientsInfo.distinctBy { it.first },
                    isLoading = false
                )
            }
        }
    }
}
