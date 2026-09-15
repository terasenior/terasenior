package com.terapia.terasenior.treatment.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
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
import kotlinx.datetime.Clock as DateClock

data class LanguageUiState(
    val type: String = "",
    val prompt: String = "",
    val instruction: String = "",
    val targetValue: String = "",
    val options: List<String> = emptyList(),
    val userInput: String = "",
    val isCorrect: Boolean? = null,
    val isCompleted: Boolean = false,
    val isSaving: Boolean = false,
    val errorsCount: Int = 0,
    val currentLevel: Int = 1,
    val startTimeMs: Long = 0,
    val sessionId: String = "", // v1.3.48
    val icon: ImageVector? = null,
    val imageUrl: String? = null,
    val images: List<ImageVector> = emptyList()
)

class LanguageViewModel(
    private val saveResultUseCase: SaveActivityResultUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(LanguageUiState())
    val uiState: StateFlow<LanguageUiState> = _uiState.asStateFlow()

    private val categories = mapOf(
        "Nombres de hombre" to listOf("Juan", "Pedro", "Luis", "Carlos", "Javier", "Andrés"),
        "Meses del año" to listOf("Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio"),
        "Herramientas" to listOf("Martillo", "Destornillador", "Alicates", "Sierra", "Llave"),
        "Países" to listOf("España", "Francia", "Italia", "Portugal", "Alemania", "México"),
        "Frutas" to listOf("Manzana", "Pera", "Plátano", "Uva", "Naranja", "Fresa")
    )

    data class GameItem(val name: String, val icon: ImageVector, val category: String, val imageUrl: String? = null)

    private val objects = listOf(
        GameItem("Manzana", Icons.Default.Restaurant, "Frutas", "https://images.unsplash.com/photo-1560806887-1e4cd0b6cbd6?w=400"),
        GameItem("Perro", Icons.Default.Pets, "Animales", "https://images.unsplash.com/photo-1543466835-00a7907e9de1?w=400"),
        GameItem("Reloj", Icons.Default.WatchLater, "Objetos", "https://images.unsplash.com/photo-1523275335684-37898b6baf30?w=400"),
        GameItem("Taza", Icons.Default.Coffee, "Hogar", "https://images.unsplash.com/photo-1585059895324-582b12879c73?w=400"),
        GameItem("Silla", Icons.Default.Chair, "Hogar", "https://images.unsplash.com/photo-1551488831-00ddcb6c6bd3?w=400"),
        GameItem("Mesa", Icons.Default.TableBar, "Hogar", "https://images.unsplash.com/photo-1583847268964-b28dc2f51ac9?w=400")
    )

    fun startNewGame(type: String, level: Int, sessionId: String = "", config: Map<String, String> = emptyMap()) {
        val startTime = DateClock.System.now().toEpochMilliseconds()
        
        when (type) {
            "language_start_letter" -> {
                val letter = config["value"] ?: listOf("A", "B", "C", "D", "E", "F", "G", "M", "P", "S").random()
                _uiState.value = LanguageUiState(type = type, prompt = letter, targetValue = letter, instruction = "Escribe una palabra que empiece por la letra $letter", currentLevel = level, startTimeMs = startTime, sessionId = sessionId)
            }
            "language_start_syllable" -> {
                val syllable = config["value"] ?: listOf("MA", "PA", "CA", "LA", "DE", "TE").random()
                _uiState.value = LanguageUiState(type = type, prompt = syllable, targetValue = syllable, instruction = "Escribe una palabra que empiece por la sílaba $syllable", currentLevel = level, startTimeMs = startTime, sessionId = sessionId)
            }
            "language_end_letter" -> {
                val letter = config["value"] ?: listOf("A", "O", "E", "R", "S", "N").random()
                _uiState.value = LanguageUiState(type = type, prompt = letter, targetValue = letter, instruction = "Escribe una palabra que termine en la letra $letter", currentLevel = level, startTimeMs = startTime, sessionId = sessionId)
            }
            "language_end_syllable" -> {
                val syllable = config["value"] ?: listOf("ON", "AS", "ES", "AR", "ER").random()
                _uiState.value = LanguageUiState(type = type, prompt = syllable, targetValue = syllable, instruction = "Escribe una palabra que termine en la sílaba $syllable", currentLevel = level, startTimeMs = startTime, sessionId = sessionId)
            }
            "language_complex_cluster" -> {
                val cluster = config["value"] ?: listOf("CL", "PL", "BR", "DR", "TR", "FL").random()
                _uiState.value = LanguageUiState(type = type, prompt = cluster, targetValue = cluster, instruction = "Escribe una palabra que contenga el grupo $cluster", currentLevel = level, startTimeMs = startTime, sessionId = sessionId)
            }
            "language_denomination" -> {
                val target = objects.random()
                val options = GdsDifficulty.choices(
                    objects.map { it.name },
                    target.name,
                    level
                )
                _uiState.value = LanguageUiState(type = type, prompt = target.name, icon = if (target.imageUrl == null) target.icon else null, imageUrl = target.imageUrl, options = options, targetValue = target.name, instruction = "Nombra el objeto que ves en la imagen", currentLevel = level, startTimeMs = startTime, sessionId = sessionId)
            }
            "language_semantic_completion" -> {
                val category = categories.keys.random()
                _uiState.value = LanguageUiState(type = type, prompt = category, targetValue = category, instruction = "Escribe un ejemplo de la categoría: $category", currentLevel = level, startTimeMs = startTime, sessionId = sessionId)
            }
            "language_semantic_naming" -> {
                val category = categories.keys.random()
                val itemCount = level.coerceIn(1, 5)
                val items = categories[category]!!.shuffled().take(itemCount)
                val itemIcons = items.map { Icons.AutoMirrored.Filled.Label } 
                val options = GdsDifficulty.choices(categories.keys.toList(), category, level)
                _uiState.value = LanguageUiState(type = type, prompt = category, options = options, targetValue = category, images = itemIcons, instruction = "¿A qué categoría pertenecen estos elementos?", currentLevel = level, startTimeMs = startTime, sessionId = sessionId)
            }
        }
    }

    fun onUserInputChange(text: String) {
        _uiState.update { it.copy(userInput = text, isCorrect = null) }
    }

    fun validateInput(patientId: String?, professionalId: String?, appointmentId: String?) {
        val state = _uiState.value
        val input = state.userInput.trim()
        if (input.isEmpty()) return

        val isCorrect = when (state.type) {
            "language_start_letter" -> input.startsWith(state.targetValue, ignoreCase = true)
            "language_start_syllable" -> input.startsWith(state.targetValue, ignoreCase = true)
            "language_end_letter" -> input.endsWith(state.targetValue, ignoreCase = true)
            "language_end_syllable" -> input.endsWith(state.targetValue, ignoreCase = true)
            "language_complex_cluster" -> input.contains(state.targetValue, ignoreCase = true)
            "language_semantic_completion" -> {
                categories[state.targetValue]?.any { it.equals(input, ignoreCase = true) } ?: (input.length > 2)
            }
            else -> false
        }

        if (isCorrect) {
            _uiState.update { it.copy(isCorrect = true) }
            viewModelScope.launch {
                delay(1500)
                saveResult(patientId, professionalId, appointmentId)
            }
        } else {
            _uiState.update { it.copy(isCorrect = false, errorsCount = state.errorsCount + 1) }
            viewModelScope.launch {
                delay(1500)
                _uiState.update { it.copy(isCorrect = null) }
            }
        }
    }

    fun onOptionSelected(option: String, patientId: String?, professionalId: String?, appointmentId: String?) {
        val state = _uiState.value
        if (state.isCompleted) return

        if (option == state.targetValue) {
            _uiState.update { it.copy(isCorrect = true) }
            viewModelScope.launch {
                delay(1500)
                saveResult(patientId, professionalId, appointmentId)
            }
        } else {
            _uiState.update { it.copy(isCorrect = false, errorsCount = state.errorsCount + 1) }
            viewModelScope.launch {
                delay(1500)
                _uiState.update { it.copy(isCorrect = null) }
            }
        }
    }

    private fun saveResult(patientId: String?, professionalId: String?, appointmentId: String?) {
        val state = _uiState.value
        val endTime = DateClock.System.now().toEpochMilliseconds()
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
                    activityType = state.type,
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
