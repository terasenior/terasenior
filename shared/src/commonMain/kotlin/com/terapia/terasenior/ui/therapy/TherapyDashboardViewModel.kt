package com.terapia.terasenior.ui.therapy

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.terapia.terasenior.domain.model.therapy.TherapySession
import com.terapia.terasenior.domain.repository.therapy.TherapySessionRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class DashboardUiState(
    val summary: Map<String, Int> = emptyMap(),
    val recentSessions: List<TherapySession> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

class TherapyDashboardViewModel(
    private val repository: TherapySessionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    fun loadDashboard(therapistId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            val summaryResult = repository.getTherapistSummary(therapistId)
            
            repository.getRecentSessions(therapistId).collect { recentResult ->
                _uiState.update { state ->
                    state.copy(
                        summary = summaryResult.getOrDefault(emptyMap()),
                        recentSessions = recentResult.getOrDefault(emptyList()),
                        isLoading = false
                    )
                }
            }
        }
    }
}
