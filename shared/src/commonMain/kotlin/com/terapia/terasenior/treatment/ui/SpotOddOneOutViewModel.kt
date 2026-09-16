package com.terapia.terasenior.treatment.ui

import androidx.compose.material.icons.Icons
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
import kotlin.random.Random

data class OddOneOutItem(val name: String, val icon: ImageVector)

data class SpotOddOneOutUiState(
    val items: List<OddOneOutItem> = emptyList(),
    val oddItemIndex: Int = -1,
    val isCorrect: Boolean? = null,
    val isCompleted: Boolean = false,
    val isSaving: Boolean = false,
    val currentLevel: Int = 1,
    val errorsCount: Int = 0,
    val startTimeMs: Long = 0,
    val sessionId: String = "" // v1.3.48
)

class SpotOddOneOutViewModel(
    private val saveResultUseCase: SaveActivityResultUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(SpotOddOneOutUiState())
    val uiState: StateFlow<SpotOddOneOutUiState> = _uiState.asStateFlow()

    fun startNewGame(level: Int = 1, sessionId: String = "") {
        val numItems = when (level) {
            1 -> 4
            2 -> 4
            3 -> 9
            4 -> 9
            else -> 16
        }

        val catalog = ExerciseContentCatalog.items.map { OddOneOutItem(it.name, it.icon) }
        val shuffled = catalog.shuffled()
        val normalItem = shuffled[0]
        val oddItem = shuffled[1]
        
        val itemsList = MutableList(numItems) { normalItem }
        val oddIndex = Random.nextInt(numItems)
        itemsList[oddIndex] = oddItem

        _uiState.value = SpotOddOneOutUiState(
            items = itemsList,
            oddItemIndex = oddIndex,
            currentLevel = level,
            sessionId = sessionId,
            startTimeMs = activityTimeMillis()
        )
    }

    fun onItemClicked(index: Int, patientId: String?, professionalId: String?, appointmentId: String?) {
        val state = _uiState.value
        if (state.isCompleted || state.isCorrect == true) return

        if (index == state.oddItemIndex) {
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
                    activityType = "attention_spot_odd_one_out",
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
