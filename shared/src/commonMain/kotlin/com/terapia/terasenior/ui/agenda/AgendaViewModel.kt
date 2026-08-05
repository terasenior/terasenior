package com.terapia.terasenior.ui.agenda

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.terapia.terasenior.domain.model.agenda.Appointment
import com.terapia.terasenior.domain.repository.agenda.AppointmentRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

sealed interface AgendaUiState {
    data object Loading : AgendaUiState
    data class Success(
        val appointments: List<Appointment>,
        val selectedDate: LocalDate,
        val filteredAppointments: List<Appointment>,
        val errorMessage: String? = null
    ) : AgendaUiState
    data class Error(val message: String) : AgendaUiState
}

@OptIn(kotlin.time.ExperimentalTime::class)
class AgendaViewModel(
    private val repository: AppointmentRepository
) : ViewModel() {

    private val _selectedDate = MutableStateFlow(
        kotlin.time.Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
    )
    private val _allAppointments = MutableStateFlow<List<Appointment>>(emptyList())
    private val _isLoading = MutableStateFlow(true)
    private val _error = MutableStateFlow<String?>(null)

    val uiState: StateFlow<AgendaUiState> = combine(
        _allAppointments, _selectedDate, _isLoading, _error
    ) { args ->
        val appointments = args[0] as List<Appointment>
        val date = args[1] as LocalDate
        val loading = args[2] as Boolean
        val error = args[3] as String?

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
            }.sortedBy { it.startAt }

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
                    _isLoading.value = false
                }.onFailure { e ->
                    _error.value = e.message ?: "Error al cargar agenda"
                    _isLoading.value = false
                }
            }
        }
    }

    fun onDateSelected(date: LocalDate) {
        _selectedDate.value = date
    }
}
