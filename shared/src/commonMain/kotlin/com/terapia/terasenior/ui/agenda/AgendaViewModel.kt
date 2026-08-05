package com.terapia.terasenior.ui.agenda

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.terapia.terasenior.domain.model.agenda.Appointment
import com.terapia.terasenior.domain.repository.agenda.AppointmentRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

sealed interface AgendaUiState {
    data object Loading : AgendaUiState
    data class Success(
        val appointments: List<Appointment>,
        val filterDate: String? = null
    ) : AgendaUiState
    data class Error(val message: String) : AgendaUiState
}

class AgendaViewModel(
    private val repository: AppointmentRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<AgendaUiState>(AgendaUiState.Loading)
    val uiState: StateFlow<AgendaUiState> = _uiState.asStateFlow()

    init {
        loadAppointments()
    }

    fun loadAppointments() {
        viewModelScope.launch {
            _uiState.value = AgendaUiState.Loading
            repository.getAppointments().collect { result ->
                result.onSuccess { list ->
                    _uiState.value = AgendaUiState.Success(list)
                }.onFailure { e ->
                    _uiState.value = AgendaUiState.Error(e.message ?: "Error al cargar agenda")
                }
            }
        }
    }
}
