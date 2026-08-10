package com.terapia.terasenior.treatment.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.vector.ImageVector
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
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

data class PuzzlePiece(
    val id: Int,
    val icon: ImageVector,
    val name: String,
    val initialOffset: Offset = Offset.Zero,
    val currentOffset: Offset = Offset.Zero,
    val targetOffset: Offset = Offset.Zero,
    val isSnapped: Boolean = false
)

data class ShapeFittingUiState(
    val pieces: List<PuzzlePiece> = emptyList(),
    val isCompleted: Boolean = false,
    val isSaving: Boolean = false,
    val currentLevel: Int = 1,
    val errorsCount: Int = 0,
    val startTimeMs: Long = 0
)

class ShapeFittingViewModel(
    private val saveResultUseCase: SaveActivityResultUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ShapeFittingUiState())
    val uiState: StateFlow<ShapeFittingUiState> = _uiState.asStateFlow()

    private val shapes = listOf(
        Icons.Default.Square to "Cuadrado",
        Icons.Default.ChangeHistory to "Triángulo",
        Icons.Default.Circle to "Círculo",
        Icons.Default.Star to "Estrella",
        Icons.Default.Pentagon to "Pentágono",
        Icons.Default.Favorite to "Corazón"
    )

    @OptIn(ExperimentalTime::class)
    fun startNewGame(level: Int = 1) {
        val numPieces = when (level) {
            1 -> 1
            2 -> 2
            3 -> 3
            4 -> 4
            else -> 6
        }

        val selectedShapes = shapes.shuffled().take(numPieces)
        val pieces = selectedShapes.mapIndexed { index, shape ->
            PuzzlePiece(
                id = index,
                icon = shape.first,
                name = shape.second
            )
        }

        _uiState.value = ShapeFittingUiState(
            pieces = pieces,
            currentLevel = level,
            startTimeMs = Clock.System.now().toEpochMilliseconds()
        )
    }

    fun updatePiecePosition(id: Int, newOffset: Offset) {
        _uiState.update { state ->
            val newPieces = state.pieces.map { 
                if (it.id == id && !it.isSnapped) it.copy(currentOffset = newOffset) else it 
            }
            state.copy(pieces = newPieces)
        }
    }

    fun updatePieceTarget(id: Int, target: Offset) {
        _uiState.update { state ->
            val newPieces = state.pieces.map { 
                if (it.id == id) it.copy(targetOffset = target) else it 
            }
            state.copy(pieces = newPieces)
        }
    }

    fun trySnapPiece(id: Int, patientId: String?, professionalId: String?, appointmentId: String?) {
        _uiState.update { state ->
            val piece = state.pieces.find { it.id == id } ?: return@update state
            
            // Calculamos la distancia al objetivo (targetOffset se establece en la UI)
            val distance = (piece.currentOffset - piece.targetOffset).getDistance()
            
            val isSnap = distance < 60f // Umbral de imantación
            
            val newPieces = state.pieces.map { 
                if (it.id == id) it.copy(isSnapped = isSnap, currentOffset = if (isSnap) it.targetOffset else it.initialOffset) 
                else it 
            }
            
            val completed = newPieces.all { it.isSnapped }
            val newState = state.copy(
                pieces = newPieces,
                isCompleted = completed,
                errorsCount = if (!isSnap) state.errorsCount + 1 else state.errorsCount
            )
            
            if (completed && patientId != null && professionalId != null) {
                saveResult(patientId, professionalId, appointmentId, newState)
            }
            
            newState
        }
    }

    @OptIn(ExperimentalTime::class)
    private fun saveResult(patientId: String, professionalId: String, appointmentId: String?, finalState: ShapeFittingUiState) {
        val endTime = Clock.System.now().toEpochMilliseconds()
        val duration = ((endTime - finalState.startTimeMs) / 1000L).toInt()

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            val result = ActivityResult(
                id = "",
                patientId = patientId,
                professionalId = professionalId,
                appointmentId = appointmentId,
                activityType = "perception_shape_fitting",
                score = (100 - (finalState.errorsCount * 10)).coerceAtLeast(0),
                durationSeconds = duration,
                errorsCount = finalState.errorsCount,
                difficultyLevel = "NIVEL_${finalState.currentLevel}",
                createdAt = ""
            )
            saveResultUseCase(result)
            _uiState.update { it.copy(isSaving = false) }
        }
    }
}
