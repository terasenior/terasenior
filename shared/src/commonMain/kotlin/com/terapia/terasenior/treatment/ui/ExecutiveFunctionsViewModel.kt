package com.terapia.terasenior.treatment.ui

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

data class ExecutiveFunctionsUiState(
    val mode: String = "",
    val instruction: String = "",
    val voiceInstruction: String = "",
    // Planning / Reordering
    val items: List<String> = emptyList(),
    val targetOrder: List<String> = emptyList(),
    // Multiple Choice (Reasoning, Analogies, Intrusos, Shopping)
    val question: String = "",
    val options: List<String> = emptyList(),
    val correctAnswer: String = "",
    // Abstractions
    val symbolKey: Map<String, Int> = emptyMap(),
    val symbolSequence: List<String> = emptyList(),
    val userTranslation: List<Int?> = emptyList(),
    // General
    val isCorrect: Boolean? = null,
    val isCompleted: Boolean = false,
    val isSaving: Boolean = false,
    val currentLevel: Int = 3,
    val errorsCount: Int = 0,
    val startTimeMs: Long = 0
)

class ExecutiveFunctionsViewModel(
    private val saveResultUseCase: SaveActivityResultUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ExecutiveFunctionsUiState())
    val uiState: StateFlow<ExecutiveFunctionsUiState> = _uiState.asStateFlow()

    fun startNewGame(mode: String, level: Int = 3) {
        val newState = when (mode) {
            "executive_planning_steps" -> generatePlanningSteps(level)
            "executive_shopping_list" -> generateShoppingList(level)
            "executive_money_calculation" -> generateMoneyCalculation(level)
            "executive_time_logic" -> generateTimeLogic(level)
            "executive_logical_reasoning" -> generateLogicalReasoning(level)
            "executive_analogies" -> generateAnalogies(level)
            "executive_abstractions" -> generateAbstractions(level)
            "executive_intrusos" -> generateIntrusos(level)
            "executive_math_advanced" -> generateMathAdvanced(level)
            else -> ExecutiveFunctionsUiState(mode = mode)
        }

        _uiState.value = newState.copy(
            mode = mode,
            currentLevel = level,
            startTimeMs = kotlin.time.Clock.System.now().toEpochMilliseconds()
        )
    }

    private fun generatePlanningSteps(level: Int): ExecutiveFunctionsUiState {
        val tasks = listOf(
            Triple("Ducharse", listOf("Quitarse la ropa", "Enjabonarse", "Aclararse", "Secarse", "Vestirse"), "Gemma, ordena los pasos para ducharte"),
            Triple("Desayunar", listOf("Preparar café", "Tostar pan", "Untar mermelada", "Comer", "Recoger la mesa"), "Gemma, ordena los pasos para desayunar"),
            Triple("Hacer la cama", listOf("Quitar las sábanas sucias", "Poner la sábana bajera", "Poner la sábana encimera", "Estirar la colcha", "Poner la almohada"), "Gemma, ordena los pasos para hacer la cama")
        )
        val selected = tasks.random()
        val originalOrder = selected.second
        val shuffled = originalOrder.shuffled()
        
        return ExecutiveFunctionsUiState(
            instruction = "Ordena los pasos para: ${selected.first}",
            voiceInstruction = selected.third,
            items = shuffled,
            targetOrder = originalOrder
        )
    }

    private fun generateShoppingList(level: Int): ExecutiveFunctionsUiState {
        val items = mapOf(
            "Leche" to "Cocina",
            "Jabón" to "Baño",
            "Sartén" to "Cocina",
            "Champú" to "Baño",
            "Toalla" to "Baño"
        )
        val item = items.keys.random()
        val correct = items[item]!!
        val options = listOf("Cocina", "Baño", "Salón", "Dormitorio")

        return ExecutiveFunctionsUiState(
            instruction = "¿Dónde guardarías este objeto?",
            question = item,
            options = options,
            correctAnswer = correct,
            voiceInstruction = "Gemma, ¿dónde guardarías la $item?"
        )
    }

    private fun generateMoneyCalculation(level: Int): ExecutiveFunctionsUiState {
        val problems = listOf(
            Triple("Si tienes 10€ y compras pan por 1.20€, ¿cuánto te queda?", "8.80", "Si tienes 10 euros y compras pan por 1 euro con 20, ¿cuánto te queda?"),
            Triple("Tienes 50€. Compras fruta por 15€ y carne por 12€. ¿Cuánto te queda?", "23", "Tienes 50 euros. Compras fruta por 15 y carne por 12. ¿Cuánto te queda?"),
            Triple("Una entrada de cine cuesta 8.50€. ¿Cuánto cuestan 2 entradas?", "17", "¿Cuánto cuestan 2 entradas de cine de 8 euros con 50?")
        )
        val selected = problems.random()
        return ExecutiveFunctionsUiState(
            instruction = "Cálculo con dinero",
            question = selected.first,
            correctAnswer = selected.second,
            voiceInstruction = selected.third
        )
    }

    private fun generateTimeLogic(level: Int): ExecutiveFunctionsUiState {
        val problems = listOf(
            Triple("Si son las 15:00 y estuviste en el banco hace 1 hora, ¿qué hora era?", "14:00", "Si son las 15:00 y estuviste en el banco hace 1 hora, ¿qué hora era?"),
            Triple("Tu cita es a las 10:30 y tardas 45 minutos en llegar. ¿A qué hora debes salir?", "09:45", "Tu cita es a las 10:30 y tardas 45 minutos en llegar. ¿A qué hora debes salir?"),
            Triple("Son las 18:15. ¿Qué hora será dentro de 2 horas y 15 minutos?", "20:30", "Son las 18:15. ¿Qué hora será dentro de 2 horas y 15 minutos?")
        )
        val selected = problems.random()
        return ExecutiveFunctionsUiState(
            instruction = "Lógica temporal",
            question = selected.first,
            correctAnswer = selected.second,
            voiceInstruction = selected.third
        )
    }

    private fun generateLogicalReasoning(level: Int): ExecutiveFunctionsUiState {
        val problems = listOf(
            Triple("¿Qué harías si pierdes las llaves de casa?", listOf("Llamar a un cerrajero", "Romper la ventana", "Quedarme a vivir en el rellano"), "Llamar a un cerrajero"),
            Triple("Vas por la calle y encuentras una cartera con dinero y DNI. ¿Qué haces?", listOf("Llevarla a la policía", "Quedarme el dinero", "Dejarla donde está"), "Llevarla a la policía"),
            Triple("Hueles a quemado en la cocina. ¿Qué es lo primero que haces?", listOf("Revisar los fogones", "Abrir las ventanas", "Llamar a los bomberos"), "Revisar los fogones")
        )
        val selected = problems.random()
        return ExecutiveFunctionsUiState(
            instruction = "Razonamiento Lógico",
            question = selected.first,
            options = selected.second,
            correctAnswer = selected.third,
            voiceInstruction = "Gemma, ${selected.first}"
        )
    }

    private fun generateAnalogies(level: Int): ExecutiveFunctionsUiState {
        val analogies = listOf(
            Triple("Liebre es a campo como pájaro es a...", listOf("Cielo", "Mar", "Tierra"), "Cielo"),
            Triple("Caliente es a frío como luz es a...", listOf("Oscuridad", "Brillo", "Lámpara"), "Oscuridad"),
            Triple("Dedo es a mano como pétalo es a...", listOf("Flor", "Árbol", "Raíz"), "Flor")
        )
        val selected = analogies.random()
        return ExecutiveFunctionsUiState(
            instruction = "Completa la analogía",
            question = selected.first,
            options = selected.second,
            correctAnswer = selected.third,
            voiceInstruction = "Gemma, completa la analogía: ${selected.first}"
        )
    }

    private fun generateAbstractions(level: Int): ExecutiveFunctionsUiState {
        val keys = mapOf("☀" to 1, "☁" to 2, "★" to 3, "❤" to 4)
        val sequence = keys.keys.shuffled()
        return ExecutiveFunctionsUiState(
            instruction = "Sustitución de Símbolos",
            voiceInstruction = "Gemma, sustituye cada símbolo por su número correspondiente",
            symbolKey = keys,
            symbolSequence = sequence,
            userTranslation = List(sequence.size) { null }
        )
    }

    private fun generateIntrusos(level: Int): ExecutiveFunctionsUiState {
        val groups = listOf(
            Triple("¿Cuál NO es un animal?", listOf("Perro", "Gato", "Mesa", "León"), "Mesa"),
            Triple("¿Cuál NO está hecho de madera?", listOf("Silla", "Puerta", "Vaso de cristal", "Lápiz"), "Vaso de cristal"),
            Triple("¿Cuál NO es un color?", listOf("Rojo", "Coche", "Verde", "Azul"), "Coche")
        )
        val selected = groups.random()
        return ExecutiveFunctionsUiState(
            instruction = "Encuentra al intruso",
            question = selected.first,
            options = selected.second.shuffled(),
            correctAnswer = selected.third,
            voiceInstruction = "Gemma, ${selected.first}"
        )
    }

    private fun generateMathAdvanced(level: Int): ExecutiveFunctionsUiState {
        val a = (5..12).random()
        val b = (2..9).random()
        val question = "$a x $b"
        val answer = (a * b).toString()
        
        return ExecutiveFunctionsUiState(
            instruction = "Cálculo avanzado",
            question = question,
            correctAnswer = answer,
            voiceInstruction = "Gemma, ¿cuánto es $a por $b?"
        )
    }

    fun onReorder(newItems: List<String>) {
        _uiState.update { it.copy(items = newItems) }
    }

    fun onCheckPlanning() {
        val state = _uiState.value
        if (state.items == state.targetOrder) {
            handleCorrect()
        } else {
            handleError()
        }
    }

    fun onOptionSelected(selected: String) {
        val state = _uiState.value
        if (selected == state.correctAnswer) {
            handleCorrect()
        } else {
            handleError()
        }
    }

    fun onAnswerInput(answer: String) {
        val state = _uiState.value
        if (answer.trim().lowercase() == state.correctAnswer.lowercase()) {
            handleCorrect()
        } else {
            handleError()
        }
    }
    
    fun onAbstractionInput(index: Int, value: Int) {
        val currentTranslation = _uiState.value.userTranslation.toMutableList()
        currentTranslation[index] = value
        _uiState.update { it.copy(userTranslation = currentTranslation) }
        
        // Comprobar si ha terminado
        if (currentTranslation.all { it != null }) {
            val isAllCorrect = _uiState.value.symbolSequence.indices.all { i ->
                val symbol = _uiState.value.symbolSequence[i]
                currentTranslation[i] == _uiState.value.symbolKey[symbol]
            }
            if (isAllCorrect) handleCorrect() else handleError()
        }
    }

    private fun handleCorrect() {
        _uiState.update { it.copy(isCorrect = true, isCompleted = true) }
    }

    private fun handleError() {
        _uiState.update { it.copy(isCorrect = false, errorsCount = it.errorsCount + 1) }
        viewModelScope.launch {
            delay(1000)
            _uiState.update { it.copy(isCorrect = null) }
        }
    }

    fun saveAndFinish(patientId: String?, professionalId: String?, appointmentId: String?) {
        val state = _uiState.value
        val endTime = kotlin.time.Clock.System.now().toEpochMilliseconds()
        val duration = ((endTime - state.startTimeMs) / 1000L).toInt()

        if (patientId != null && professionalId != null) {
            viewModelScope.launch {
                _uiState.update { it.copy(isSaving = true) }
                val result = ActivityResult(
                    id = "",
                    patientId = patientId,
                    professionalId = professionalId,
                    appointmentId = appointmentId,
                    activityType = state.mode,
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
}
