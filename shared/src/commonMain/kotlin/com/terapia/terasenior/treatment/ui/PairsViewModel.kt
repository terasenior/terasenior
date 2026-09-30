package com.terapia.terasenior.treatment.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.terapia.terasenior.domain.model.results.ActivityResult
import com.terapia.terasenior.domain.usecase.results.SaveActivityResultUseCase
import com.terapia.terasenior.treatment.repository.RealisticExerciseImageCatalog
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock as DateClock
import kotlin.random.Random

data class MemoryCard(
    val id: Int,
    val icon: ImageVector? = null,
    val imageUrl: String? = null,
    val isFlipped: Boolean = false,
    val isMatched: Boolean = false,
    val isRealImage: Boolean = false
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
    val currentLevel: Int = 3,
    val useRealImages: Boolean = false,
    val sessionId: String = "" // v1.3.48
)

class PairsViewModel(
    private val saveResultUseCase: SaveActivityResultUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(PairsUiState())
    val uiState: StateFlow<PairsUiState> = _uiState.asStateFlow()

    private val availableIcons = listOf(
        Icons.Default.MedicalServices, Icons.Default.Medication, Icons.Default.Favorite,
        Icons.Default.WatchLater, Icons.Default.Bed, Icons.Default.Chair, Icons.Default.Phone,
        Icons.Default.Light, Icons.Default.Build, Icons.Default.Work, Icons.Default.MenuBook, Icons.Default.PhotoCamera
    )

    private val realImageCatalog = RealisticExerciseImageCatalog.objectImages

    fun startNewGame(level: Int = 3, sessionId: String = "") {
        val numPairs = when(level) {
            1 -> 2
            2 -> 4
            3 -> 6
            4 -> 8
            5 -> 10
            else -> 6
        }
        
        val useReal = true // Las imágenes reales se usan en todos los niveles.
        
        val cards = if (useReal) {
            val selected = realImageCatalog.shuffled().take(numPairs)
            val gameList = (selected + selected).shuffled()
            gameList.mapIndexed { index, url ->
                MemoryCard(id = index, imageUrl = url, isRealImage = true)
            }
        } else {
            val selected = availableIcons.shuffled().take(numPairs)
            val gameList = (selected + selected).shuffled()
            gameList.mapIndexed { index, icon ->
                MemoryCard(id = index, icon = icon, isRealImage = false)
            }
        }

        _uiState.value = PairsUiState(
            cards = cards,
            totalPairs = numPairs,
            currentLevel = level,
            sessionId = sessionId,
            startTimeMs = activityTimeMillis(),
            useRealImages = useReal
        )
    }

    fun onCardClicked(index: Int, patientId: String?, professionalId: String?, appointmentId: String?) {
        val state = _uiState.value
        if (state.isProcessing || state.cards[index].isFlipped || state.cards[index].isMatched) return

        if (state.firstSelectedCardIndex == null) {
            val newCards = state.cards.toMutableList()
            newCards[index] = newCards[index].copy(isFlipped = true)
            _uiState.update { it.copy(cards = newCards, firstSelectedCardIndex = index) }
        } else {
            val firstIndex = state.firstSelectedCardIndex
            val newCards = state.cards.toMutableList()
            newCards[index] = newCards[index].copy(isFlipped = true)
            _uiState.update { it.copy(cards = newCards, isProcessing = true) }

            viewModelScope.launch {
                delay(1000)
                val firstCard = newCards[firstIndex]
                val secondCard = newCards[index]

                val isMatch = if (state.useRealImages) firstCard.imageUrl == secondCard.imageUrl else firstCard.icon == secondCard.icon

                if (isMatch) {
                    newCards[firstIndex] = firstCard.copy(isMatched = true)
                    newCards[index] = secondCard.copy(isMatched = true)
                    
                    val newPairsFound = state.pairsFound + 1
                    val completed = newPairsFound >= state.totalPairs
                    
                    _uiState.update { it.copy(cards = newCards, firstSelectedCardIndex = null, isProcessing = false, pairsFound = newPairsFound) }

                    if (completed) {
                        saveResult(patientId, professionalId, appointmentId)
                    }
                } else {
                    newCards[firstIndex] = firstCard.copy(isFlipped = false)
                    newCards[index] = secondCard.copy(isFlipped = false)
                    _uiState.update { it.copy(cards = newCards, firstSelectedCardIndex = null, isProcessing = false, errorsCount = state.errorsCount + 1) }
                }
            }
        }
    }

    private fun saveResult(patientId: String?, professionalId: String?, appointmentId: String?) {
        val currentState = _uiState.value
        val endTime = activityTimeMillis()
        val duration = ((endTime - currentState.startTimeMs) / 1000L).toInt()

        viewModelScope.launch {
            if (patientId != null && professionalId != null) {
                _uiState.update { it.copy(isSaving = true) }
                val result = ActivityResult(
                    id = "",
                    patientId = patientId,
                    professionalId = professionalId,
                    appointmentId = appointmentId,
                    sessionId = currentState.sessionId, // v1.3.48
                    activityType = "memory_pairs",
                    score = (100 - (currentState.errorsCount * 5)).coerceAtLeast(0),
                    durationSeconds = duration,
                    errorsCount = currentState.errorsCount,
                    difficultyLevel = "NIVEL_${currentState.currentLevel}",
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
