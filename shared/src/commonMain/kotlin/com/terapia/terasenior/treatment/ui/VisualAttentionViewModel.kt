package com.terapia.terasenior.treatment.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.terapia.terasenior.domain.model.results.ActivityResult
import com.terapia.terasenior.domain.usecase.results.SaveActivityResultUseCase
import com.terapia.terasenior.treatment.repository.ExerciseContentCatalog
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.random.Random
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

data class VisualAttentionItem(
    val content: Any,
    val isFound: Boolean = false,
    val isWrong: Boolean = false,
    val isRealImage: Boolean = false // Marcador para usar Kamel
)

data class VisualAttentionUiState(
    val variation: String = "",
    val target: Any? = null,
    val isTargetRealImage: Boolean = false,
    val items: List<VisualAttentionItem> = emptyList(),
    val items2: List<VisualAttentionItem> = emptyList(),
    val gridSize: Int = 4,
    val foundCount: Int = 0,
    val totalTargets: Int = 0,
    val isCompleted: Boolean = false,
    val isSaving: Boolean = false,
    val errorsCount: Int = 0,
    val startTimeMs: Long = 0,
    val currentLevel: Int = 1,
    val feedbackMessage: String? = null,
    val isCountingPhase: Boolean = false,
    val numericOptions: List<Int> = emptyList(),
    val correctCount: Int = 0,
    val expectedNextValue: Int = 1
)

class VisualAttentionViewModel(
    private val saveResultUseCase: SaveActivityResultUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(VisualAttentionUiState())
    val uiState: StateFlow<VisualAttentionUiState> = _uiState.asStateFlow()

    private val symbolCatalog = listOf(
        Icons.Default.Favorite, Icons.Default.Star, Icons.Default.ThumbUp, Icons.Default.Face,
        Icons.Default.Home, Icons.Default.ShoppingCart, Icons.Default.Lightbulb, Icons.Default.Bed,
        Icons.Default.Chair, Icons.Default.WatchLater, Icons.Default.MedicalServices, Icons.Default.Build
    )

    // Catálogo de imágenes reales para pacientes mayores (v1.3.3)
    private val realImageCatalog = listOf(
        "https://images.unsplash.com/photo-1560806887-1e4cd0b6cbd6?w=200", // Manzana
        "https://images.unsplash.com/photo-1543466835-00a7907e9de1?w=200", // Perro
        "https://images.unsplash.com/photo-1514888286974-6c03e2ca1dba?w=200", // Gato
        "https://images.unsplash.com/photo-1571771894821-ad9b5886419a?w=200", // Plátano
        "https://images.unsplash.com/photo-1585059895324-582b12879c73?w=200", // Taza
        "https://images.unsplash.com/photo-1523275335684-37898b6baf30?w=200", // Reloj
        "https://images.unsplash.com/photo-1551488831-00ddcb6c6bd3?w=200", // Silla
        "https://images.unsplash.com/photo-1583847268964-b28dc2f51ac9?w=200"  // Mesa
    )

    private val letterCatalog = ('A'..'Z').toList()
    
    // ... rest of catalogs ...

    private val wordCategories = mapOf(
        "Animales" to listOf("PERRO", "GATO", "LEON", "TIGRE", "CEBRA", "MONO", "VACA"),
        "Frutas" to listOf("MANZANA", "PERA", "PLATANO", "UVA", "FRESA", "KIWI"),
        "Colores" to listOf("ROJO", "AZUL", "VERDE", "AMARILLO", "ROSA", "NEGRO")
    )

    private val wordsForLongest = listOf("CASA", "ORDENADOR", "MESA", "ELECTRODOMESTICO", "SOL", "PANTALLA", "RELOJ", "CONSTITUCION")

    @OptIn(ExperimentalTime::class)
    fun startNewGame(variation: String, level: Int = 1) {
        val gridSize = when (level) {
            1 -> 3
            2 -> 4
            3 -> 5
            4 -> 6
            5 -> 8
            else -> 4
        }

        val totalCells = gridSize * gridSize
        var target: Any? = null
        val items = mutableListOf<VisualAttentionItem>()
        var totalTargets = 0
        var isTargetReal = false

        // Decidir si usamos imágenes reales (50% de probabilidad en niveles de símbolos)
        val useRealImages = level >= 2 && variation in listOf("attention_different", "attention_equals_model", "attention_symbols") && Random.nextFloat() > 0.5f

        when (variation) {
            "attention_different" -> {
                if (useRealImages) {
                    val shuffled = realImageCatalog.shuffled()
                    val normal = shuffled[0]
                    val odd = shuffled[1]
                    repeat(totalCells) { items.add(VisualAttentionItem(normal, isRealImage = true)) }
                    val oddIndex = Random.nextInt(totalCells)
                    items[oddIndex] = VisualAttentionItem(odd, isRealImage = true)
                    target = null
                    totalTargets = 1
                } else {
                    val shuffledSymbols = symbolCatalog.shuffled()
                    val normalSymbol = shuffledSymbols[0]
                    val oddSymbol = shuffledSymbols[1]
                    repeat(totalCells) { items.add(VisualAttentionItem(normalSymbol)) }
                    val oddIndex = Random.nextInt(totalCells)
                    items[oddIndex] = VisualAttentionItem(oddSymbol)
                    target = null
                    totalTargets = 1
                }
            }
            "attention_equals_model", "attention_symbols" -> {
                if (useRealImages) {
                    val shuffled = realImageCatalog.shuffled()
                    val t = shuffled[0]
                    target = t
                    isTargetReal = true
                    val distractors = shuffled.subList(1, shuffled.size)
                    repeat(totalCells) {
                        val isTarget = Random.nextFloat() < 0.2f || items.isEmpty()
                        if (isTarget) {
                            items.add(VisualAttentionItem(t, isRealImage = true))
                            totalTargets++
                        } else {
                            items.add(VisualAttentionItem(distractors.random(), isRealImage = true))
                        }
                    }
                } else {
                    val shuffledSymbols = symbolCatalog.shuffled()
                    val t = shuffledSymbols[0]
                    target = t
                    val distractors = shuffledSymbols.subList(1, shuffledSymbols.size)
                    repeat(totalCells) {
                        val isTarget = Random.nextFloat() < 0.2f || items.isEmpty()
                        if (isTarget) {
                            items.add(VisualAttentionItem(t))
                            totalTargets++
                        } else {
                            items.add(VisualAttentionItem(distractors.random()))
                        }
                    }
                }
            }
            "attention_letters" -> {
                val shuffledLetters = letterCatalog.shuffled()
                val t = shuffledLetters[0]
                target = t
                val distractors = shuffledLetters.subList(1, shuffledLetters.size)
                repeat(totalCells) {
                    val isTarget = Random.nextFloat() < 0.2f || items.isEmpty()
                    if (isTarget) {
                        items.add(VisualAttentionItem(t))
                        totalTargets++
                    } else {
                        items.add(VisualAttentionItem(distractors.random()))
                    }
                }
            }
            "attention_numbers" -> {
                val t = Random.nextInt(10, 99)
                target = t
                repeat(totalCells) {
                    val isTarget = Random.nextFloat() < 0.2f || items.isEmpty()
                    if (isTarget) {
                        items.add(VisualAttentionItem(t))
                        totalTargets++
                    } else {
                        var n = Random.nextInt(10, 99)
                        while (n == t) n = Random.nextInt(10, 99)
                        items.add(VisualAttentionItem(n))
                    }
                }
            }
            "attention_positions" -> {
                val positions = listOf(0f, 90f, 180f, 270f)
                val t = positions.random()
                target = t
                val icon = Icons.Default.PlayArrow
                repeat(totalCells) {
                    val isTarget = Random.nextFloat() < 0.2f || items.isEmpty()
                    if (isTarget) {
                        items.add(VisualAttentionItem(Pair(icon, t)))
                        totalTargets++
                    } else {
                        var p = positions.random()
                        while (p == t) p = positions.random()
                        items.add(VisualAttentionItem(Pair(icon, p)))
                    }
                }
            }
            "attention_matrices" -> {
                val shuffledSymbols = symbolCatalog.shuffled()
                val t = shuffledSymbols[0]
                target = t
                val distractors = shuffledSymbols.subList(1, shuffledSymbols.size)
                val densityGridSize = gridSize + 1
                val totalDensityCells = densityGridSize * densityGridSize
                repeat(totalDensityCells) {
                    val isTarget = Random.nextFloat() < 0.3f
                    if (isTarget) {
                        items.add(VisualAttentionItem(t))
                        totalTargets++
                    } else {
                        items.add(VisualAttentionItem(distractors.random()))
                    }
                }
                if (totalTargets == 0) {
                    items[Random.nextInt(totalDensityCells)] = VisualAttentionItem(t)
                    totalTargets = 1
                }
            }
            "attention_row_cancel" -> {
                val shuffledSymbols = symbolCatalog.shuffled()
                val t = shuffledSymbols[0]
                target = t
                val distractors = shuffledSymbols.subList(1, shuffledSymbols.size)
                repeat(totalCells) {
                    val isTarget = Random.nextFloat() < 0.25f || items.isEmpty()
                    if (isTarget) {
                        items.add(VisualAttentionItem(t))
                        totalTargets++
                    } else {
                        items.add(VisualAttentionItem(distractors.random()))
                    }
                }
            }
            "attention_yes_no" -> {
                val shuffledLetters = letterCatalog.shuffled()
                repeat(totalCells) { i ->
                    items.add(VisualAttentionItem(shuffledLetters.random()))
                    if (i % 2 == 0) totalTargets++
                }
            }
            "attention_count" -> {
                val pool = if (useRealImages) realImageCatalog else symbolCatalog.map { it }
                val count = when(level) {
                    1 -> Random.nextInt(3, 6)
                    2 -> Random.nextInt(5, 10)
                    else -> Random.nextInt(8, 15)
                }
                repeat(count) {
                    items.add(VisualAttentionItem(pool.random(), isRealImage = useRealImages))
                }
                totalTargets = count
            }
            "attention_differences" -> {
                val shuffledSymbols = symbolCatalog.shuffled()
                repeat(totalCells) { items.add(VisualAttentionItem(shuffledSymbols.random())) }
                val items2 = items.toMutableList()
                val diffIndex = Random.nextInt(totalCells)
                var newContent = symbolCatalog.random()
                while (newContent == items[diffIndex].content) newContent = symbolCatalog.random()
                items2[diffIndex] = VisualAttentionItem(newContent)
                // We'll use items2 in state, and items will be the reference
                totalTargets = 1
                target = diffIndex // Store index of difference
            }
            "attention_dual_task" -> {
                val shuffledSymbols = symbolCatalog.shuffled()
                val t1 = shuffledSymbols[0]
                val t2 = shuffledSymbols[1]
                target = listOf(t1, t2)
                val distractors = shuffledSymbols.subList(2, shuffledSymbols.size)
                repeat(totalCells) {
                    val r = Random.nextFloat()
                    if (r < 0.15f) {
                        items.add(VisualAttentionItem(t1))
                        totalTargets++
                    } else if (r < 0.30f) {
                        items.add(VisualAttentionItem(t2))
                        totalTargets++
                    } else {
                        items.add(VisualAttentionItem(distractors.random()))
                    }
                }
                if (totalTargets == 0) {
                    items[0] = VisualAttentionItem(t1)
                    items[1] = VisualAttentionItem(t2)
                    totalTargets = 2
                }
            }
            "attention_consecutive" -> {
                val nums = (1..totalCells).toList().shuffled()
                nums.forEach { items.add(VisualAttentionItem(it)) }
                totalTargets = totalCells
            }
            "attention_longest" -> {
                val pickWords = Random.nextBoolean()
                val candidates = if (pickWords) {
                    wordsForLongest.shuffled().take(4)
                } else {
                    List(4) { Random.nextInt(10, 1000000).toString() }
                }
                candidates.forEach { items.add(VisualAttentionItem(it)) }
                val longest = candidates.maxByOrNull { it.length }
                target = longest
                totalTargets = 1
            }
            "attention_word_search" -> {
                val category = wordCategories.keys.random()
                val word = wordCategories[category]!!.random()
                
                val row = Random.nextInt(gridSize)
                val colStart = Random.nextInt(gridSize - word.length + 1)
                
                val grid = MutableList(totalCells) { letterCatalog.random() }
                for (i in word.indices) {
                    grid[row * gridSize + colStart + i] = word[i]
                }
                
                grid.forEach { items.add(VisualAttentionItem(it)) }
                val wordIndices = (colStart until colStart + word.length).map { row * gridSize + it }
                target = Pair(category, wordIndices)
                totalTargets = word.length
            }
        }

        val items2 = if (variation == "attention_differences") {
            val shuffledSymbols = symbolCatalog.shuffled()
            val tempItems = mutableListOf<VisualAttentionItem>()
            repeat(totalCells) { tempItems.add(VisualAttentionItem(shuffledSymbols.random())) }
            val diffIndex = target as Int
            val list2 = items.toMutableList()
            var newContent = symbolCatalog.random()
            while (newContent == items[diffIndex].content) newContent = symbolCatalog.random()
            list2[diffIndex] = VisualAttentionItem(newContent)
            list2
        } else emptyList()

        val correctCount = if (variation == "attention_row_cancel" || variation == "attention_count") totalTargets else 0
        val numericOptions = if (correctCount > 0) generateOptions(correctCount) else emptyList()

        _uiState.value = VisualAttentionUiState(
            variation = variation,
            target = target,
            isTargetRealImage = isTargetReal,
            items = items,
            items2 = items2,
            gridSize = when (variation) {
                "attention_matrices" -> gridSize + 1
                "attention_count" -> 3
                "attention_yes_no" -> 4
                "attention_longest" -> 1
                else -> gridSize
            },
            totalTargets = totalTargets,
            currentLevel = level,
            startTimeMs = Clock.System.now().toEpochMilliseconds(),
            correctCount = correctCount,
            numericOptions = numericOptions,
            isCountingPhase = variation == "attention_count",
            expectedNextValue = 1
        )
    }

    private fun generateOptions(correct: Int): List<Int> {
        val options = mutableSetOf(correct)
        while (options.size < 4) {
            val offset = Random.nextInt(-3, 4)
            val opt = (correct + offset).coerceAtLeast(1)
            options.add(opt)
        }
        return options.toList().shuffled()
    }

    fun onItemClicked(index: Int, patientId: String?, professionalId: String?, appointmentId: String?, isGrid2: Boolean = false) {
        val state = _uiState.value
        if (state.isCompleted || state.isCountingPhase) return
        
        val itemsList = if (isGrid2) state.items2 else state.items
        if (index >= itemsList.size || itemsList[index].isFound) return

        val item = itemsList[index]
        val isCorrect = when (state.variation) {
            "attention_different" -> {
                val counts = state.items.groupBy { it.content }.mapValues { it.value.size }
                counts[item.content] == 1
            }
            "attention_yes_no" -> index % 2 == 0
            "attention_differences" -> isGrid2 && index == state.target
            "attention_dual_task" -> (state.target as? List<*>)?.contains(item.content) ?: false
            "attention_consecutive" -> item.content == state.expectedNextValue
            "attention_longest" -> item.content == state.target
            "attention_positions" -> (item.content as? Pair<*, *>)?.second == state.target
            "attention_word_search" -> {
                val wordIndices = (state.target as? Pair<*, *>)?.second as? List<*>
                wordIndices?.contains(index) == true
            }
            else -> item.content == state.target
        }

        if (isCorrect) {
            val newItems = state.items.toMutableList()
            val newItems2 = state.items2.toMutableList()
            
            if (isGrid2) {
                newItems2[index] = item.copy(isFound = true)
            } else {
                newItems[index] = item.copy(isFound = true)
            }
            
            val newFoundCount = state.foundCount + 1
            
            val isRowCancel = state.variation == "attention_row_cancel"
            val allTapped = newFoundCount >= state.totalTargets
            
            val completed = if (isRowCancel) false else allTapped
            val enteringCountingPhase = isRowCancel && allTapped
            val nextVal = if (state.variation == "attention_consecutive") state.expectedNextValue + 1 else state.expectedNextValue

            _uiState.update { it.copy(
                items = newItems,
                items2 = newItems2,
                foundCount = newFoundCount,
                isCompleted = completed,
                isCountingPhase = enteringCountingPhase,
                expectedNextValue = nextVal,
                feedbackMessage = when {
                    completed -> "¡Excelente trabajo!"
                    enteringCountingPhase -> "¿Cuántos has encontrado?"
                    else -> null
                }
            ) }

            if (completed && patientId != null && professionalId != null) {
                saveResult(patientId, professionalId, appointmentId)
            }
        } else {
            val newItems = state.items.toMutableList()
            val newItems2 = state.items2.toMutableList()
            
            if (isGrid2) {
                newItems2[index] = item.copy(isWrong = true)
            } else {
                newItems[index] = item.copy(isWrong = true)
            }
            
            _uiState.update { it.copy(
                items = newItems,
                items2 = newItems2,
                errorsCount = state.errorsCount + 1
            ) }
            
            viewModelScope.launch {
                delay(1000)
                _uiState.update { currentState ->
                    val resetItems = currentState.items.toMutableList()
                    val resetItems2 = currentState.items2.toMutableList()
                    if (index < (if (isGrid2) resetItems2.size else resetItems.size)) {
                        if (isGrid2) resetItems2[index] = resetItems2[index].copy(isWrong = false)
                        else resetItems[index] = resetItems[index].copy(isWrong = false)
                    }
                    currentState.copy(items = resetItems, items2 = resetItems2)
                }
            }
        }
    }

    fun onNumericOptionSelected(value: Int, patientId: String?, professionalId: String?, appointmentId: String?) {
        val state = _uiState.value
        if (state.isCompleted || !state.isCountingPhase) return

        if (value == state.correctCount) {
            _uiState.update { it.copy(
                isCompleted = true,
                feedbackMessage = "¡Excelente trabajo!"
            ) }
            if (patientId != null && professionalId != null) {
                saveResult(patientId, professionalId, appointmentId)
            }
        } else {
            _uiState.update { it.copy(
                errorsCount = state.errorsCount + 1,
                feedbackMessage = "¡No es correcto! Prueba de nuevo."
            ) }
            viewModelScope.launch {
                delay(1500)
                if (!_uiState.value.isCompleted) {
                    _uiState.update { it.copy(feedbackMessage = "¿Cuántos hay?") }
                }
            }
        }
    }

    @OptIn(ExperimentalTime::class)
    private fun saveResult(patientId: String, professionalId: String, appointmentId: String?) {
        val state = _uiState.value
        val endTime = Clock.System.now().toEpochMilliseconds()
        val diff = endTime - state.startTimeMs
        val duration = (diff / 1000L).toInt()

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            val result = ActivityResult(
                id = "",
                patientId = patientId,
                professionalId = professionalId,
                appointmentId = appointmentId,
                activityType = state.variation,
                score = (100 - (state.errorsCount * 5)).coerceAtLeast(0),
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
