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
    val useRealImages: Boolean = false
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

    private val realImageCatalog = listOf(
        "https://images.unsplash.com/photo-1560806887-1e4cd0b6cbd6?w=200", // Manzana
        "https://images.unsplash.com/photo-1543466835-00a7907e9de1?w=200", // Perro
        "https://images.unsplash.com/photo-1514888286974-6c03e2ca1dba?w=200", // Gato
        "https://images.unsplash.com/photo-1571771894821-ad9b5886419a?w=200", // Plátano
        "https://images.unsplash.com/photo-1585059895324-582b12879c73?w=200", // Taza
        "https://images.unsplash.com/photo-1523275335684-37898b6baf30?w=200", // Reloj
        "https://images.unsplash.com/photo-1551488831-00ddcb6c6bd3?w=200", // Silla
        "https://images.unsplash.com/photo-1583847268964-b28dc2f51ac9?w=200", // Mesa
        "https://images.unsplash.com/photo-1544620347-c4fd4a3d5957?w=200", // Autobús
        "https://images.unsplash.com/photo-1517841905240-472988babdf9?w=200"  // Coche
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
        
        val useReal = level >= 2 // A partir de nivel 2 usamos fotos reales
        
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
            startTimeMs = kotlin.time.Clock.System.now().toEpochMilliseconds(),
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
                    
                    _uiState.update { it.copy(cards = newCards, firstSelectedCardIndex = null, isProcessing = false, pairsFound = newPairsFound, isCompleted = completed) }

                    if (completed && patientId != null && professionalId != null) {
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
