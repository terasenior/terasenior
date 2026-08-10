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

data class SemanticItem(
    val name: String,
    val icon: ImageVector,
    val category: String,
    val isFound: Boolean = false
)

data class SemanticCategoryUiState(
    val items: List<SemanticItem> = emptyList(),
    val targetCategory: String = "",
    val categories: List<String> = emptyList(),
    val isCompleted: Boolean = false,
    val isSaving: Boolean = false,
    val currentLevel: Int = 3,
    val score: Int = 0,
    val totalItemsToFind: Int = 0,
    val foundCount: Int = 0,
    val errorIndex: Int? = null,
    val errorsCount: Int = 0,
    val startTimeMs: Long = 0
)

class SemanticCategoryViewModel(
    private val saveResultUseCase: SaveActivityResultUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(SemanticCategoryUiState())
    val uiState: StateFlow<SemanticCategoryUiState> = _uiState.asStateFlow()

    private val allItems = listOf(
        SemanticItem("Manzana", Icons.Default.Restaurant, "Frutas"),
        SemanticItem("Plátano", Icons.Default.Icecream, "Frutas"), // Placeholder icon
        SemanticItem("Uva", Icons.Default.BakeryDining, "Frutas"), // Placeholder icon
        SemanticItem("Silla", Icons.Default.Chair, "Muebles"),
        SemanticItem("Cama", Icons.Default.Bed, "Muebles"),
        SemanticItem("Mesa", Icons.Default.TableRestaurant, "Muebles"),
        SemanticItem("Martillo", Icons.Default.Build, "Herramientas"),
        SemanticItem("Llave", Icons.Default.Key, "Herramientas"),
        SemanticItem("Serrucho", Icons.Default.Handyman, "Herramientas"),
        SemanticItem("Coche", Icons.Default.DirectionsCar, "Vehículos"),
        SemanticItem("Bici", Icons.Default.PedalBike, "Vehículos"),
        SemanticItem("Avión", Icons.Default.Flight, "Vehículos")
    )

    @OptIn(kotlin.time.ExperimentalTime::class)
    fun startNewGame(level: Int = 3) {
        val shuffled = allItems.shuffled()
        val numCategories = when(level) {
            1 -> 2
            else -> 3
        }
        val categories = shuffled.map { it.category }.distinct().take(numCategories)
        val targetCategory = categories.random()
        
        val gameItems = shuffled.filter { it.category in categories }.take(level * 3)
        val totalToFind = gameItems.count { it.category == targetCategory }

        _uiState.value = SemanticCategoryUiState(
            items = gameItems,
            targetCategory = targetCategory,
            categories = categories,
            totalItemsToFind = totalToFind,
            currentLevel = level,
            startTimeMs = kotlin.time.Clock.System.now().toEpochMilliseconds()
        )
    }

    fun onItemClicked(index: Int, patientId: String?, professionalId: String?, appointmentId: String?) {
        val state = _uiState.value
        if (state.isCompleted || state.errorIndex != null || state.items[index].isFound) return

        val item = state.items[index]
        if (item.category == state.targetCategory) {
            val newItems = state.items.toMutableList()
            newItems[index] = item.copy(isFound = true)
            
            val newFoundCount = state.foundCount + 1
            val completed = newFoundCount >= state.totalItemsToFind
            
            _uiState.update { it.copy(
                items = newItems,
                foundCount = newFoundCount,
                isCompleted = completed
            ) }

            if (completed && patientId != null && professionalId != null) {
                saveResult(patientId, professionalId, appointmentId)
            }
        } else {
            _uiState.update { it.copy(errorIndex = index, errorsCount = state.errorsCount + 1) }
            viewModelScope.launch {
                delay(800)
                _uiState.update { it.copy(errorIndex = null) }
            }
        }
    }

    @OptIn(kotlin.time.ExperimentalTime::class)
    private fun saveResult(patientId: String, professionalId: String, appointmentId: String?) {
        val state = _uiState.value
        val endTime = kotlin.time.Clock.System.now().toEpochMilliseconds()
        val duration = ((endTime - state.startTimeMs) / 1000L).toInt()

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            val result = ActivityResult(
                id = "",
                patientId = patientId,
                professionalId = professionalId,
                appointmentId = appointmentId,
                activityType = "language_semantic_category",
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
