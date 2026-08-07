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
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

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
    val icon: ImageVector? = null,
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

    private val objects = listOf(
        GameItem("Hospital", Icons.Default.LocalHospital, "Lugares"),
        GameItem("Escuela", Icons.Default.School, "Lugares"),
        GameItem("Herramientas", Icons.Default.Build, "Objetos"),
        GameItem("Reloj", Icons.Default.WatchLater, "Objetos"),
        GameItem("Cama", Icons.Default.Bed, "Hogar"),
        GameItem("Silla", Icons.Default.Chair, "Hogar")
    )

    @OptIn(ExperimentalTime::class)
    fun startNewGame(type: String, level: Int, config: Map<String, String> = emptyMap()) {
        val startTime = Clock.System.now().toEpochMilliseconds()
        
        when (type) {
            "language_start_letter" -> {
                val letter = config["value"] ?: listOf("A", "B", "C", "D", "E", "F", "G", "M", "P", "S").random()
                _uiState.value = LanguageUiState(
                    type = type,
                    prompt = letter,
                    targetValue = letter,
                    instruction = "Escribe una palabra que empiece por la letra $letter",
                    currentLevel = level,
                    startTimeMs = startTime
                )
            }
            "language_start_syllable" -> {
                val syllable = config["value"] ?: listOf("MA", "PA", "CA", "LA", "DE", "TE").random()
                _uiState.value = LanguageUiState(
                    type = type,
                    prompt = syllable,
                    targetValue = syllable,
                    instruction = "Escribe una palabra que empiece por la sílaba $syllable",
                    currentLevel = level,
                    startTimeMs = startTime
                )
            }
            "language_end_letter" -> {
                val letter = config["value"] ?: listOf("A", "O", "E", "R", "S", "N").random()
                _uiState.value = LanguageUiState(
                    type = type,
                    prompt = letter,
                    targetValue = letter,
                    instruction = "Escribe una palabra que termine en la letra $letter",
                    currentLevel = level,
                    startTimeMs = startTime
                )
            }
            "language_end_syllable" -> {
                val syllable = config["value"] ?: listOf("ON", "AS", "ES", "AR", "ER").random()
                _uiState.value = LanguageUiState(
                    type = type,
                    prompt = syllable,
                    targetValue = syllable,
                    instruction = "Escribe una palabra que termine en la sílaba $syllable",
                    currentLevel = level,
                    startTimeMs = startTime
                )
            }
            "language_complex_cluster" -> {
                val cluster = config["value"] ?: listOf("CL", "PL", "BR", "DR", "TR", "FL").random()
                _uiState.value = LanguageUiState(
                    type = type,
                    prompt = cluster,
                    targetValue = cluster,
                    instruction = "Escribe una palabra que contenga el grupo $cluster",
                    currentLevel = level,
                    startTimeMs = startTime
                )
            }
            "language_denomination" -> {
                val target = objects.random()
                val otherOptions = objects.filter { it.name != target.name }.shuffled().take(3).map { it.name }
                val options = (otherOptions + target.name).shuffled()
                _uiState.value = LanguageUiState(
                    type = type,
                    prompt = target.name,
                    icon = target.icon,
                    options = options,
                    targetValue = target.name,
                    instruction = "Nombra el objeto que ves en la imagen",
                    currentLevel = level,
                    startTimeMs = startTime
                )
            }
            "language_semantic_completion" -> {
                val category = categories.keys.random()
                _uiState.value = LanguageUiState(
                    type = type,
                    prompt = category,
                    targetValue = category,
                    instruction = "Escribe un ejemplo de la categoría: $category",
                    currentLevel = level,
                    startTimeMs = startTime
                )
            }
            "language_semantic_naming" -> {
                val category = categories.keys.random()
                val items = categories[category]!!.shuffled().take(3)
                // Usamos iconos genéricos para los items por ahora o placeholders
                val itemIcons = items.map { Icons.AutoMirrored.Filled.Label } 
                val otherCategories = categories.keys.filter { it != category }.shuffled().take(3)
                val options = (otherCategories + category).shuffled()
                _uiState.value = LanguageUiState(
                    type = type,
                    prompt = category,
                    options = options,
                    targetValue = category,
                    images = itemIcons,
                    instruction = "¿A qué categoría pertenecen estos elementos?",
                    currentLevel = level,
                    startTimeMs = startTime
                )
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
                // Validación básica: que esté en la lista o simplemente que no esté vacío (más flexible para el profesional)
                categories[state.targetValue]?.any { it.equals(input, ignoreCase = true) } ?: (input.length > 2)
            }
            else -> false
        }

        if (isCorrect) {
            _uiState.update { it.copy(isCorrect = true, isCompleted = true) }
            if (patientId != null && professionalId != null) {
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
            _uiState.update { it.copy(isCorrect = true, isCompleted = true) }
            if (patientId != null && professionalId != null) {
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
                activityType = state.type,
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
