package com.terapia.terasenior.treatment.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.automirrored.filled.MenuBook
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

data class NamingObjectsUiState(
    val targetItem: GameItem? = null,
    val options: List<String> = emptyList(),
    val isCorrect: Boolean? = null,
    val isCompleted: Boolean = false,
    val isSaving: Boolean = false,
    val currentLevel: Int = 1,
    val errorsCount: Int = 0,
    val startTimeMs: Long = 0
)

class NamingObjectsViewModel(
    private val saveResultUseCase: SaveActivityResultUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(NamingObjectsUiState())
    val uiState: StateFlow<NamingObjectsUiState> = _uiState.asStateFlow()

    private val catalog = listOf(
        GameItem("Cama", Icons.Default.Bed, "Hogar"),
        GameItem("Silla", Icons.Default.Chair, "Hogar"),
        GameItem("Lámpara", Icons.Default.Light, "Hogar"),
        GameItem("Teléfono", Icons.Default.Phone, "Hogar"),
        GameItem("Reloj", Icons.Default.WatchLater, "Hogar"),
        GameItem("Médico", Icons.Default.MedicalServices, "Salud"),
        GameItem("Medicina", Icons.Default.Medication, "Salud"),
        GameItem("Corazón", Icons.Default.Favorite, "Salud"),
        GameItem("Caminar", Icons.AutoMirrored.Filled.DirectionsWalk, "Salud"),
        GameItem("Herramienta", Icons.Default.Build, "Trabajo"),
        GameItem("Maleta", Icons.Default.Work, "Trabajo"),
        GameItem("Libro", Icons.AutoMirrored.Filled.MenuBook, "Ocio"),
        GameItem("Cámara", Icons.Default.PhotoCamera, "Ocio"),
        GameItem("Música", Icons.Default.MusicNote, "Ocio")
    )

    @OptIn(ExperimentalTime::class)
    fun startNewGame(level: Int = 1) {
        val numOptions = when (level) {
            1 -> 2
            2 -> 3
            3 -> 4
            4 -> 6
            5 -> 8
            else -> 4
        }

        val shuffledCatalog = catalog.shuffled()
        val target = shuffledCatalog[0]
        val otherOptions = shuffledCatalog.drop(1).take(numOptions - 1).map { it.name }
        val allOptions = (otherOptions + target.name).shuffled()

        _uiState.value = NamingObjectsUiState(
            targetItem = target,
            options = allOptions,
            currentLevel = level,
            startTimeMs = Clock.System.now().toEpochMilliseconds()
        )
    }

    fun onOptionSelected(name: String, patientId: String?, professionalId: String?, appointmentId: String?) {
        val state = _uiState.value
        if (state.isCompleted || state.isCorrect == true) return

        if (name == state.targetItem?.name) {
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
                activityType = "language_naming_objects",
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
