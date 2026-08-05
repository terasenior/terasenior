package com.terapia.terasenior.ui.patient

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.terapia.terasenior.domain.model.patient.Patient
import com.terapia.terasenior.domain.usecase.patient.GetPatientsUseCase
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

sealed interface PatientListUiState {
    data object Loading : PatientListUiState
    data class Success(
        val patients: List<Patient>,
        val searchQuery: String = ""
    ) : PatientListUiState
    data class Error(val message: String) : PatientListUiState
}

class PatientListViewModel(
    private val getPatientsUseCase: GetPatientsUseCase
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    private val _isLoading = MutableStateFlow(true)
    private val _allPatients = MutableStateFlow<List<Patient>>(emptyList())
    private val _error = MutableStateFlow<String?>(null)

    val uiState: StateFlow<PatientListUiState> = combine(
        _allPatients, _searchQuery, _isLoading, _error
    ) { patients, query, loading, error ->
        if (loading) {
            PatientListUiState.Loading
        } else if (error != null) {
            PatientListUiState.Error(error)
        } else {
            val filtered = if (query.isBlank()) {
                patients
            } else {
                patients.filter { 
                    it.fullName.contains(query, ignoreCase = true) || 
                    it.preferredName?.contains(query, ignoreCase = true) == true
                }
            }
            PatientListUiState.Success(patients = filtered, searchQuery = query)
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
}
