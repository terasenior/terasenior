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

data class MemoryCard(
    val id: Int,
    val icon: ImageVector,
    val isFlipped: Boolean = false,
    val isMatched: Boolean = false
)

data class PairsUiState(
    val cards: List<MemoryCard> = emptyList(),
    val firstSelectedCardIndex: Int? = null,
    val isProcessing: Boolean = false,
    val pairsFound: Int = 0,
    val totalPairs: Int = 0,
    val isCompleted: Boolean = false,
    val isSaving: Boolean = false,
    val startTimeMs: Long = 0,
    val errorsCount: Int = 0,
    val currentLevel: Int = 3
)

class PairsViewModel(
    private val saveResultUseCase: SaveActivityResultUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(PairsUiState())
    val uiState: StateFlow<PairsUiState> = _uiState.asStateFlow()

    private val availableIcons = listOf(
        Icons.Default.MedicalServices,
        Icons.Default.Medication,
        Icons.Default.Favorite,
        Icons.Default.WatchLater,
        Icons.Default.Bed,
        Icons.Default.Chair,
        Icons.Default.Phone,
        Icons.Default.Light,
        Icons.Default.Build,
        Icons.Default.Work,
        Icons.Default.MenuBook,
        Icons.Default.PhotoCamera
    )

    @OptIn(kotlin.time.ExperimentalTime::class)
    fun startNewGame(level: Int = 3) {
        val numPairs = when(level) {
            1 -> 2
            2 -> 4
            3 -> 6
            4 -> 8
            5 -> 10
            else -> 6
        }
        
        val selectedIcons = availableIcons.shuffled().take(numPairs)
        val gameIcons = (selectedIcons + selectedIcons).shuffled()
        
        val cards = gameIcons.mapIndexed { index, icon ->
            MemoryCard(id = index, icon = icon)
        }

        _uiState.value = PairsUiState(
            cards = cards,
            totalPairs = numPairs,
            currentLevel = level,
            startTimeMs = kotlin.time.Clock.System.now().toEpochMilliseconds()
        )
    }

    fun onCardClicked(index: Int, patientId: String?, professionalId: String?, appointmentId: String?) {
        val state = _uiState.value
        if (state.isProcessing || state.cards[index].isFlipped || state.cards[index].isMatched) return

        if (state.firstSelectedCardIndex == null) {
            // Primer clic
            val newCards = state.cards.toMutableList()
            newCards[index] = newCards[index].copy(isFlipped = true)
            _uiState.update { it.copy(cards = newCards, firstSelectedCardIndex = index) }
        } else {
            // Segundo clic
            val firstIndex = state.firstSelectedCardIndex
            val newCards = state.cards.toMutableList()
            newCards[index] = newCards[index].copy(isFlipped = true)
            _uiState.update { it.copy(cards = newCards, isProcessing = true) }

            viewModelScope.launch {
                delay(1000)
                val firstCard = newCards[firstIndex]
                val secondCard = newCards[index]

                if (firstCard.icon == secondCard.icon) {
                    // ¡Pareja encontrada!
                    newCards[firstIndex] = firstCard.copy(isMatched = true)
                    newCards[index] = secondCard.copy(isMatched = true)
                    
                    val newPairsFound = state.pairsFound + 1
                    val completed = newPairsFound >= state.totalPairs
                    
                    _uiState.update { it.copy(
                        cards = newCards,
                        firstSelectedCardIndex = null,
                        isProcessing = false,
                        pairsFound = newPairsFound,
                        isCompleted = completed
                    ) }

                    if (completed && patientId != null && professionalId != null) {
                        saveResult(patientId, professionalId, appointmentId)
                    }
                } else {
                    // No coinciden
                    newCards[firstIndex] = firstCard.copy(isFlipped = false)
                    newCards[index] = secondCard.copy(isFlipped = false)
                    _uiState.update { it.copy(
                        cards = newCards,
                        firstSelectedCardIndex = null,
                        isProcessing = false,
                        errorsCount = state.errorsCount + 1
                    ) }
                }
            }
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
                activityType = "memory_pairs",
                score = (100 - (currentState.errorsCount * 5)).coerceAtLeast(0),
                durationSeconds = duration,
                errorsCount = currentState.errorsCount,
                difficultyLevel = "NIVEL_${currentState.currentLevel}",
                createdAt = ""
            )

            saveResultUseCase(result)
            _uiState.update { it.copy(isSaving = false) }
        }
    }
}
