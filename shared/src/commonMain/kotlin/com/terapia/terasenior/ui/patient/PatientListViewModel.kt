package com.terapia.terasenior.ui.patient

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.terapia.terasenior.domain.model.patient.Patient
import com.terapia.terasenior.domain.model.patient.PatientStatus
import com.terapia.terasenior.domain.usecase.patient.GetPatientsUseCase
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

sealed interface PatientListUiState {
    data object Loading : PatientListUiState
    data class Success(
        val patients: List<Patient>,
        val searchQuery: String = "",
        val statusFilter: PatientStatus? = null
    ) : PatientListUiState
    data class Error(val message: String) : PatientListUiState
}

class PatientListViewModel(
    private val getPatientsUseCase: GetPatientsUseCase
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    private val _statusFilter = MutableStateFlow<PatientStatus?>(null)
    private val _isLoading = MutableStateFlow(true)
    private val _allPatients = MutableStateFlow<List<Patient>>(emptyList())
    private val _error = MutableStateFlow<String?>(null)

    val uiState: StateFlow<PatientListUiState> = combine(
        _allPatients, _searchQuery, _statusFilter, _isLoading, _error
    ) { patients, query, status, loading, error ->
        if (loading) {
            PatientListUiState.Loading
        } else if (error != null) {
            PatientListUiState.Error(error)
        } else {
            val filtered = patients.filter { patient ->
                val matchesQuery = if (query.isBlank()) true else {
                    patient.fullName.contains(query, ignoreCase = true) || 
                    patient.preferredName?.contains(query, ignoreCase = true) == true
                }
                val matchesStatus = if (status == null) true else patient.status == status
                matchesQuery && matchesStatus
            }
            PatientListUiState.Success(patients = filtered, searchQuery = query, statusFilter = status)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), PatientListUiState.Loading)

    init {
        loadPatients()
    }

    fun loadPatients() {
        viewModelScope.launch {
            _isLoading.value = true
            getPatientsUseCase().collect { result ->
                result.onSuccess { list ->
                    _allPatients.value = list
                    _error.value = null
                    _isLoading.value = false
                }.onFailure { e ->
                    _error.value = e.message ?: "Error al cargar pacientes"
                    _isLoading.value = false
                }
            }
        }
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun onStatusFilterChanged(status: PatientStatus?) {
        _statusFilter.value = status
    }
}
