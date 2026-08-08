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
        val statusFilter: PatientStatus? = null,
        val currentPage: Int = 1,
        val totalPages: Int = 1,
        val pageSize: Int = 6 
    ) : PatientListUiState
    data class Error(val message: String) : PatientListUiState
}

class PatientListViewModel(
    private val getPatientsUseCase: GetPatientsUseCase
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    private val _statusFilter = MutableStateFlow<PatientStatus?>(null)
    private val _currentPage = MutableStateFlow(1)
    private val _pageSize = 6
    
    private val _isLoading = MutableStateFlow(true)
    private val _allPatients = MutableStateFlow<List<Patient>>(emptyList())
    private val _error = MutableStateFlow<String?>(null)

    val uiState: StateFlow<PatientListUiState> = combine(
        _allPatients, _searchQuery, _statusFilter, _currentPage, _isLoading, _error
    ) { values ->
        val patients = values[0] as List<Patient>
        val query = values[1] as String
        val status = values[2] as PatientStatus?
        val page = values[3] as Int
        val loading = values[4] as Boolean
        val error = values[5] as String?

        if (loading) {
            PatientListUiState.Loading
        } else if (error != null) {
            PatientListUiState.Error(error)
        } else {
            val filtered = patients.filter { patient ->
                val matchesQuery = if (query.isBlank()) true else {
                    patient.fullName.contains(query, ignoreCase = true) || 
                    patient.preferredName?.contains(query, ignoreCase = true) == true ||
                    patient.nif?.contains(query, ignoreCase = true) == true ||
                    patient.externalId?.contains(query, ignoreCase = true) == true
                }
                val matchesStatus = if (status == null) true else patient.status == status
                matchesQuery && matchesStatus
            }
            
            val totalPages = kotlin.math.ceil(filtered.size.toDouble() / _pageSize).toInt().coerceAtLeast(1)
            val validatedPage = page.coerceIn(1, totalPages)
            
            val startIndex = (validatedPage - 1) * _pageSize
            val paginatedList = filtered.drop(startIndex).take(_pageSize)

            PatientListUiState.Success(
                patients = paginatedList, 
                searchQuery = query, 
                statusFilter = status,
                currentPage = validatedPage,
                totalPages = totalPages,
                pageSize = _pageSize
            )
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
        _currentPage.value = 1 
    }

    fun onStatusFilterChanged(status: PatientStatus?) {
        _statusFilter.value = status
        _currentPage.value = 1 
    }

    fun onPageChanged(page: Int) {
        _currentPage.value = page
    }
}
