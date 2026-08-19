package com.terapia.terasenior.ui.agenda

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.terapia.terasenior.domain.model.agenda.Appointment
import com.terapia.terasenior.domain.repository.agenda.AppointmentRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock as DateClock
import kotlinx.datetime.*

sealed interface AgendaUiState {
    data object Loading : AgendaUiState
    data class Success(
        val appointments: List<Appointment>,
        val selectedDate: LocalDate,
        val filteredAppointments: List<Pair<Appointment, List<String>>>, // Cita y nombres de pacientes
        val errorMessage: String? = null
    ) : AgendaUiState
    data class Error(val message: String) : AgendaUiState
}

@OptIn(kotlin.time.ExperimentalTime::class)
class AgendaViewModel(
    private val repository: AppointmentRepository
) : ViewModel() {

    private val _selectedDate: MutableStateFlow<LocalDate> = MutableStateFlow(
        DateClock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
    )
    private val _allAppointments = MutableStateFlow<List<Appointment>>(emptyList())
    private val _attendeesMap = MutableStateFlow<Map<String, List<String>>>(emptyMap()) // ID Cita -> Nombres
    private val _isLoading = MutableStateFlow(true)
    private val _error = MutableStateFlow<String?>(null)

    val uiState: StateFlow<AgendaUiState> = combine(
        _allAppointments, _selectedDate, _attendeesMap, _isLoading, _error
    ) { args ->
        val appointments = args[0] as List<Appointment>
        val date = args[1] as LocalDate
        val attendees = args[2] as Map<String, List<String>>
        val loading = args[3] as Boolean
        val error = args[4] as String?

        if (loading) {
            AgendaUiState.Loading
        } else if (error != null && appointments.isEmpty()) {
            AgendaUiState.Error(error)
        } else {
            val filtered = appointments.filter { 
                try {
                    val startInstant = kotlinx.datetime.Instant.parse(it.startAt)
                    val appointmentDate = startInstant.toLocalDateTime(TimeZone.currentSystemDefault()).date
                    appointmentDate == date
                } catch (e: Exception) {
                    false
                }
            }.sortedBy { it.startAt }.map { it to (attendees[it.id] ?: emptyList()) }

            AgendaUiState.Success(
                appointments = appointments,
                selectedDate = date,
                filteredAppointments = filtered,
                errorMessage = error
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AgendaUiState.Loading)

    init {
        loadAppointments()
    }

    fun loadAppointments() {
        viewModelScope.launch {
            _isLoading.value = true
            repository.getAppointments().collect { result ->
                result.onSuccess { list ->
                    _allAppointments.value = list
                    _error.value = null
                    
                    // Cargar asistentes para las citas de los próximos 30 días para tener datos listos
                    loadAttendeesForList(list)
                    
                    _isLoading.value = false
                }.onFailure { e ->
                    _error.value = e.message ?: "Error al cargar agenda"
                    _isLoading.value = false
                }
            }
        }
    }

    private suspend fun loadAttendeesForList(appointments: List<Appointment>) {
        val newMap = _attendeesMap.value.toMutableMap()
        appointments.forEach { appt ->
            if (!newMap.containsKey(appt.id)) {
                repository.getAttendees(appt.id).onSuccess { attendees ->
                    newMap[appt.id] = attendees.map { it.patientName }
                }
            }
        }
        _attendeesMap.value = newMap
    }

    fun onDateSelected(date: LocalDate) {
        _selectedDate.value = date
    }
}
