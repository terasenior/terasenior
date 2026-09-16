package com.terapia.terasenior.treatment.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.terapia.terasenior.domain.model.results.ActivityResult
import com.terapia.terasenior.domain.usecase.results.SaveActivityResultUseCase
import com.terapia.terasenior.treatment.repository.ExerciseContentCatalog
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock as DateClock

data class NamingGameItem(val name: String, val icon: ImageVector, val imageUrl: String? = null)

data class NamingObjectsUiState(
    val targetItem: NamingGameItem? = null,
    val options: List<String> = emptyList(),
    val isCorrect: Boolean? = null,
    val isCompleted: Boolean = false,
    val isSaving: Boolean = false,
    val currentLevel: Int = 1,
    val errorsCount: Int = 0,
    val startTimeMs: Long = 0,
    val sessionId: String = "" // v1.3.48
)

class NamingObjectsViewModel(
    private val saveResultUseCase: SaveActivityResultUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(NamingObjectsUiState())
    val uiState: StateFlow<NamingObjectsUiState> = _uiState.asStateFlow()

    fun startNewGame(level: Int = 1, sessionId: String = "") {
        val numOptions = when (level) {
            1 -> 2
            2 -> 3
            3 -> 4
            4 -> 6
            5 -> 8
            else -> 4
        }

        val catalog = ExerciseContentCatalog.items.map { NamingGameItem(it.name, it.icon, it.imageUrl) }
        val shuffledCatalog = catalog.shuffled()
        val target = shuffledCatalog[0]
        val otherOptions = shuffledCatalog.drop(1).take(numOptions - 1).map { it.name }
        val allOptions = (otherOptions + target.name).shuffled()

        _uiState.value = NamingObjectsUiState(
            targetItem = target,
            options = allOptions,
            currentLevel = level,
            sessionId = sessionId,
            startTimeMs = activityTimeMillis()
        )
    }

    fun onOptionSelected(name: String, patientId: String?, professionalId: String?, appointmentId: String?) {
        val state = _uiState.value
        if (state.isCompleted || state.isCorrect == true) return

        if (name == state.targetItem?.name) {
            _uiState.update { it.copy(isCorrect = true) }
            saveResult(patientId, professionalId, appointmentId)
        } else {
            _uiState.update { it.copy(isCorrect = false, errorsCount = state.errorsCount + 1) }
            viewModelScope.launch {
                delay(1000)
                _uiState.update { it.copy(isCorrect = null) }
            }
        }
    }

    private fun saveResult(patientId: String?, professionalId: String?, appointmentId: String?) {
        val state = _uiState.value
        val endTime = activityTimeMillis()
        val duration = ((endTime - state.startTimeMs) / 1000L).toInt()

        viewModelScope.launch {
            if (patientId != null && professionalId != null) {
                _uiState.update { it.copy(isSaving = true) }
                val result = ActivityResult(
                    id = "",
                    patientId = patientId,
                    professionalId = professionalId,
                    appointmentId = appointmentId,
                    sessionId = state.sessionId, // v1.3.48
                    activityType = "language_naming_objects",
                    score = (100 - (state.errorsCount * 10)).coerceAtLeast(0),
                    durationSeconds = duration,
                    errorsCount = state.errorsCount,
                    difficultyLevel = "NIVEL_${state.currentLevel}",
                    createdAt = ""
                )
                saveResultUseCase(result)
                _uiState.update { it.copy(isSaving = false, isCompleted = true) }
            } else {
                _uiState.update { it.copy(isCompleted = true) }
            }
        }
    }
}
