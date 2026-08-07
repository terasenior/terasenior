package com.terapia.terasenior.treatment.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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

data class SpotOddOneOutUiState(
    val items: List<GameItem> = emptyList(),
    val oddItemIndex: Int = -1,
    val isCorrect: Boolean? = null,
    val isCompleted: Boolean = false,
    val isSaving: Boolean = false,
    val currentLevel: Int = 1,
    val errorsCount: Int = 0,
    val startTimeMs: Long = 0
)

class SpotOddOneOutViewModel(
    private val saveResultUseCase: SaveActivityResultUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(SpotOddOneOutUiState())
    val uiState: StateFlow<SpotOddOneOutUiState> = _uiState.asStateFlow()

    private val catalog = listOf(
        GameItem("Cama", Icons.Default.Bed, "Hogar"),
        GameItem("Silla", Icons.Default.Chair, "Hogar"),
        GameItem("Lámpara", Icons.Default.Light, "Hogar"),
        GameItem("Teléfono", Icons.Default.Phone, "Hogar"),
        GameItem("Reloj", Icons.Default.WatchLater, "Hogar"),
        GameItem("Médico", Icons.Default.MedicalServices, "Salud"),
        GameItem("Medicina", Icons.Default.Medication, "Salud"),
        GameItem("Corazón", Icons.Default.Favorite, "Salud"),
        GameItem("Caminar", Icons.Default.DirectionsWalk, "Salud"),
        GameItem("Herramienta", Icons.Default.Build, "Trabajo"),
        GameItem("Maleta", Icons.Default.Work, "Trabajo"),
        GameItem("Libro", Icons.Default.MenuBook, "Ocio"),
        GameItem("Cámara", Icons.Default.PhotoCamera, "Ocio"),
        GameItem("Música", Icons.Default.MusicNote, "Ocio")
    )

    @OptIn(ExperimentalTime::class)
    fun startNewGame(level: Int = 1) {
        val totalItems = when (level) {
            1 -> 3
            2 -> 4
            3 -> 6
            4 -> 9
            5 -> 12
            else -> 4
        }

        val shuffledCatalog = catalog.shuffled()
        val normalItem = shuffledCatalog[0]
        val oddItem = shuffledCatalog[1]

        val items = MutableList(totalItems) { normalItem }
        val oddIndex = (0 until totalItems).random()
        items[oddIndex] = oddItem

        _uiState.value = SpotOddOneOutUiState(
            items = items,
            oddItemIndex = oddIndex,
            currentLevel = level,
            startTimeMs = Clock.System.now().toEpochMilliseconds()
        )
    }

    fun onItemClicked(index: Int, patientId: String?, professionalId: String?, appointmentId: String?) {
        val state = _uiState.value
        if (state.isCompleted || state.isCorrect == true) return

        if (index == state.oddItemIndex) {
            _uiState.update { it.copy(isCorrect = true, isCompleted = true) }
            if (patientId != null && professionalId != null) {
                saveResult(patientId, professionalId, appointmentId)
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
                activityType = "attention_spot_odd_one_out",
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
