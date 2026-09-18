package com.terapia.terasenior.ui.agenda

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.terapia.terasenior.domain.model.agenda.Appointment
import com.terapia.terasenior.domain.repository.agenda.AppointmentRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import kotlinx.datetime.*
import com.terapia.terasenior.treatment.repository.currentOrientationLocalDateTimeIso

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
        agendaToday()
    )
    private val _allAppointments = MutableStateFlow<List<Appointment>>(emptyList())
    val appointments: StateFlow<List<Appointment>> = _allAppointments.asStateFlow()
    private val _attendeesMap = MutableStateFlow<Map<String, List<String>>>(emptyMap()) // ID Cita -> Nombres
    private val _isLoading = MutableStateFlow(true)
    private val _error = MutableStateFlow<String?>(null)
    private val _diagnosticEvents = MutableStateFlow<List<String>>(emptyList())
    val diagnosticEvents: StateFlow<List<String>> = _diagnosticEvents.asStateFlow()

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
                    // La zona horaria del sistema no está disponible de forma fiable en WebAssembly.
                    // Las citas se almacenan en UTC, por lo que usamos la misma referencia al filtrarlas.
                    val appointmentDate = startInstant.toLocalDateTime(TimeZone.UTC).date
                    appointmentDate == date
                } catch (_: Throwable) {
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
            addDiagnostic("Inicio de carga de Agenda")
            _isLoading.value = true
            _error.value = null
            try {
                addDiagnostic("Solicitando citas a Supabase")
                val result = withTimeout(15_000L) {
                    repository.getAppointments().first()
                }
                result.onSuccess { list ->
                    _allAppointments.value = list
                    addDiagnostic("Citas recibidas: ${list.size}")

                    // La agenda debe estar disponible aunque la consulta de asistentes sea lenta o falle.
                    viewModelScope.launch {
                        addDiagnostic("Carga de asistentes iniciada")
                        loadAttendeesForList(list)
                        addDiagnostic("Carga de asistentes finalizada")
                    }
                }.onFailure { e ->
                    _error.value = e.message ?: "Error al cargar agenda"
                    addDiagnostic("Error de Supabase: ${e.message ?: "sin detalle"}")
                }
            } catch (_: kotlinx.coroutines.TimeoutCancellationException) {
                _error.value = "La agenda tarda demasiado en responder. Pulsa Reintentar."
                addDiagnostic("Tiempo de espera agotado tras 15 segundos")
            } catch (e: Throwable) {
                _error.value = e.message ?: "Error al cargar agenda"
                addDiagnostic("Error inesperado: ${e.message ?: "sin detalle"}")
            } finally {
                _isLoading.value = false
                addDiagnostic("Carga de Agenda finalizada")
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

    private fun addDiagnostic(message: String) {
        val timestamp = try {
            currentOrientationLocalDateTimeIso().replace('T', ' ')
        } catch (_: Throwable) {
            "Sin hora"
        }
        _diagnosticEvents.value = (_diagnosticEvents.value + "$timestamp · $message").takeLast(20)
    }

    fun onDateSelected(date: LocalDate) {
        _selectedDate.value = date
    }
}

private fun agendaToday(): LocalDate = try {
    LocalDateTime.parse(currentOrientationLocalDateTimeIso()).date
} catch (_: Throwable) {
    LocalDate(2026, 9, 18)
}
