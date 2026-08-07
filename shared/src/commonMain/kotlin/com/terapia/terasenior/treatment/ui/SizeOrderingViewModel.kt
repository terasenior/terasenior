package com.terapia.terasenior.treatment.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.terapia.terasenior.domain.model.results.ActivityResult
import com.terapia.terasenior.domain.usecase.results.SaveActivityResultUseCase
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

data class SizeItem(
    val id: Int,
    val sizeDp: Int,
    val icon: ImageVector
)

data class SizeOrderingUiState(
    val items: List<SizeItem> = emptyList(),
    val selectedIds: List<Int> = emptyList(),
    val isCorrect: Boolean? = null,
    val isCompleted: Boolean = false,
    val isSaving: Boolean = false,
    val currentLevel: Int = 1,
    val errorsCount: Int = 0,
    val startTimeMs: Long = 0
)

class SizeOrderingViewModel(
    private val saveResultUseCase: SaveActivityResultUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(SizeOrderingUiState())
    val uiState: StateFlow<SizeOrderingUiState> = _uiState.asStateFlow()

    private val availableIcons = listOf(
        Icons.Default.Circle,
        Icons.Default.Square,
        Icons.Default.Star,
        Icons.Default.Favorite
    )

    @OptIn(ExperimentalTime::class)
    fun startNewGame(level: Int = 1) {
        val numItems = when (level) {
            1 -> 3
            2 -> 3
            3 -> 4
            4 -> 4
            5 -> 5
            else -> 3
        }

        val icon = availableIcons.random()
        val baseSize = 40
        val step = 30
        
        val items = (0 until numItems).map { i ->
            SizeItem(i, baseSize + (i * step), icon)
        }.shuffled()

        _uiState.value = SizeOrderingUiState(
            items = items,
            currentLevel = level,
            startTimeMs = Clock.System.now().toEpochMilliseconds()
        )
    }

    fun onItemClicked(item: SizeItem, patientId: String?, professionalId: String?, appointmentId: String?) {
        val state = _uiState.value
        if (state.isCompleted || state.selectedIds.contains(item.id)) return

        val newSelectedIds = state.selectedIds + item.id
        
        // Comprobar si el último seleccionado es el correcto en el orden (el más pequeño de los restantes)
        val remainingItems = state.items.filter { !state.selectedIds.contains(it.id) }
        val smallestRemaining = remainingItems.minByOrNull { it.sizeDp }

        if (item.id == smallestRemaining?.id) {
            _uiState.update { it.copy(selectedIds = newSelectedIds) }
            
            if (newSelectedIds.size == state.items.size) {
                _uiState.update { it.copy(isCorrect = true, isCompleted = true) }
                if (patientId != null && professionalId != null) {
                    saveResult(patientId, professionalId, appointmentId)
                }
            }
        } else {
            _uiState.update { it.copy(isCorrect = false, errorsCount = state.errorsCount + 1) }
            viewModelScope.launch {
                delay(1000)
                _uiState.update { it.copy(isCorrect = null) }
            }
        }
    }

    @OptIn(ExperimentalTime::class)
    private fun saveResult(patientId: String, professionalId: String, appointmentId: String?) {
        val state = _uiState.value
        val endTime = Clock.System.now().toEpochMilliseconds()
        val duration = ((endTime - state.startTimeMs) / 1000L).toInt()

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            val result = ActivityResult(
                id = "",
                patientId = patientId,
                professionalId = professionalId,
                appointmentId = appointmentId,
                activityType = "perception_size_ordering",
                score = (100 - (state.errorsCount * 10)).coerceAtLeast(0),
                durationSeconds = duration,
                errorsCount = state.errorsCount,
                difficultyLevel = "NIVEL_${state.currentLevel}",
                createdAt = ""
            )
            saveResultUseCase(result)
            _uiState.update { it.copy(isSaving = false) }
        }
    }
}
