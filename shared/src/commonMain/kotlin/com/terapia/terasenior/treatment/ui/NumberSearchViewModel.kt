package com.terapia.terasenior.treatment.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.terapia.terasenior.domain.model.results.ActivityResult
import com.terapia.terasenior.domain.usecase.results.SaveActivityResultUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.random.Random

data class Cell(
    val number: Int,
    val isFound: Boolean = false,
    val isWrong: Boolean = false
)

data class NumberSearchUiState(
    val targetNumber: Int = Random.nextInt(10),
    val grid: List<Cell> = emptyList(),
    val foundCount: Int = 0,
    val totalTargets: Int = 0,
    val isCompleted: Boolean = false,
    val isSaving: Boolean = false,
    val startTimeMs: Long = 0,
    val errorsCount: Int = 0
)

class NumberSearchViewModel(
    private val saveResultUseCase: SaveActivityResultUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(NumberSearchUiState())
    val uiState: StateFlow<NumberSearchUiState> = _uiState.asStateFlow()

    init {
        startNewGame()
    }

    @OptIn(kotlin.time.ExperimentalTime::class)
    fun startNewGame() {
        val target = Random.nextInt(10)
        val grid = generateGrid(target)
        _uiState.value = NumberSearchUiState(
            targetNumber = target,
            grid = grid,
            totalTargets = grid.count { it.number == target },
            startTimeMs = kotlin.time.Clock.System.now().toEpochMilliseconds()
        )
    }

    fun onCellClicked(index: Int, patientId: String?, professionalId: String?, appointmentId: String?) {
        val currentState = _uiState.value
        if (currentState.isCompleted || currentState.grid[index].isFound) return

        val cell = currentState.grid[index]
        if (cell.number == currentState.targetNumber) {
            val newGrid = currentState.grid.toMutableList()
            newGrid[index] = cell.copy(isFound = true)
            
            val newFoundCount = currentState.foundCount + 1
            val completed = newFoundCount >= currentState.totalTargets
            
            _uiState.update { it.copy(
                grid = newGrid, 
                foundCount = newFoundCount, 
                isCompleted = completed 
            ) }

            if (completed && patientId != null && professionalId != null) {
                saveResult(patientId, professionalId, appointmentId)
            }
        } else {
            val newGrid = currentState.grid.toMutableList()
            newGrid[index] = cell.copy(isWrong = true)
            _uiState.update { it.copy(
                grid = newGrid, 
                errorsCount = currentState.errorsCount + 1 
            ) }
        }
    }

    @OptIn(kotlin.time.ExperimentalTime::class)
    private fun saveResult(patientId: String, professionalId: String, appointmentId: String?) {
        val currentState = _uiState.value
        val endTime = kotlin.time.Clock.System.now().toEpochMilliseconds()
        val duration = ((endTime - currentState.startTimeMs) / 1000L).toInt()

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            
            val result = ActivityResult(
                id = "",
                patientId = patientId,
                professionalId = professionalId,
                appointmentId = appointmentId,
                activityType = "number_search",
                score = (100 - (currentState.errorsCount * 5)).coerceAtLeast(0),
                durationSeconds = duration,
                errorsCount = currentState.errorsCount,
                difficultyLevel = "NORMAL",
                createdAt = ""
            )

            saveResultUseCase(result)
            _uiState.update { it.copy(isSaving = false) }
        }
    }

    private fun generateGrid(target: Int): List<Cell> {
        val size = 25
        val cells = mutableListOf<Cell>()
        var targetCount = 0

        repeat(size) {
            val isTarget = Random.nextFloat() < 0.20f
            val num = if (isTarget) {
                targetCount++
                target
            } else {
                var n = Random.nextInt(10)
                while (n == target) n = Random.nextInt(10)
                n
            }
            cells.add(Cell(number = num))
        }

        while (targetCount < 3) {
            val idx = Random.nextInt(size)
            if (cells[idx].number != target) {
                cells[idx] = Cell(number = target)
                targetCount++
            }
        }
        return cells
    }
}
