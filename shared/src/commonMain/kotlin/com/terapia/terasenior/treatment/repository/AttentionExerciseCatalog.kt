package com.terapia.terasenior.treatment.repository

/** Banco de 750 actividades de atención para personas mayores. */
object AttentionExerciseCatalog {
    data class AttentionExercise(val id: String, val name: String, val description: String, val imageUrl: String, val category: String)
    data class AttentionQuestion(val text: String, val options: List<String>, val correctAnswer: String, val imageUrl: String)

    private data class Stimulus(val name: String, val category: String, val color: String, val image: String)
    private val stimuli = listOf(
        Stimulus("Manzana", "Fruta", "Rojo", RealisticExerciseImageCatalog.apple),
        Stimulus("Plátano", "Fruta", "Amarillo", RealisticExerciseImageCatalog.banana),
        Stimulus("Perro", "Animal", "Marrón", RealisticExerciseImageCatalog.dog),
        Stimulus("Gato", "Animal", "Gris", RealisticExerciseImageCatalog.cat),
        Stimulus("Reloj", "Objeto", "Claro", RealisticExerciseImageCatalog.clock),
        Stimulus("Taza", "Hogar", "Blanco", RealisticExerciseImageCatalog.cup),
        Stimulus("Silla", "Hogar", "Marrón", RealisticExerciseImageCatalog.chair),
        Stimulus("Teléfono", "Objeto", "Negro", RealisticExerciseImageCatalog.phone),
        Stimulus("Libro", "Objeto", "Azul", RealisticExerciseImageCatalog.book),
        Stimulus("Autobús", "Transporte", "Azul", RealisticExerciseImageCatalog.bus)
    )

    private enum class Family(val title: String, val description: String, val category: String = "Atención") {
        TARGET("Reconocer objetos", "Identifica objetos cotidianos mediante las gnosias visuales.", "Percepción"),
        SAME("Identifica iguales", "Selecciona la palabra que coincide con la imagen."),
        DIFFERENT("Encuentra el diferente", "Distingue una característica visual importante."),
        COUNT("Conteo atento", "Cuenta elementos sencillos sin perder la consigna."),
        SEQUENCE("Secuencias", "Completa una serie corta manteniendo la atención."),
        CATEGORY("Evocación categorial", "Agrupa estímulos por una categoría común.", "Funciones Ejecutivas"),
        COLOR("Color predominante", "Identifica el color predominante del estímulo.", "Percepción"),
        POSITION("Orientación espacial", "Responde a una posición sencilla.", "Percepción"),
        RULE("Cambio de regla", "Aplica una regla breve y clara."),
        DAILY("Planificación cotidiana", "Identifica la información necesaria para planificar una acción.", "Funciones Ejecutivas"),
        FOCALIZATION("Focalización", "Dirige la atención hacia un estímulo concreto."),
        MAIN("Información principal", "Selecciona el dato importante de una situación."),
        SUSTAINED("Sostenimiento atencional", "Mantiene la atención durante una tarea breve."),
        ALTERNATION("Alternancias", "Cambia el foco de atención siguiendo una pauta."),
        DIVIDED("Foco dividido", "Atiende a dos instrucciones sencillas a la vez.")
    }

    val items: List<AttentionExercise> = Family.entries.flatMap { family ->
        (1..50).map { number ->
            val stimulus = stimuli[(number - 1) % stimuli.size]
            AttentionExercise("guided_attention_${family.name.lowercase()}_$number", spokenTitle(family), family.description, stimulus.image, family.category)
        }
    }

    init {
        check(items.size == 750) { "El catálogo de atención debe contener 750 actividades." }
        check(items.map { it.id }.distinct().size == items.size) { "Cada actividad debe tener un identificador único." }
        check(items.mapNotNull { question(it.id, 3)?.text }.distinct().size == items.size) {
            "Cada actividad de atención debe tener una consigna distinta."
        }
    }

    fun contains(id: String): Boolean = id.startsWith("guided_attention_") && items.any { it.id == id }
    fun forCategory(category: String): List<AttentionExercise> = items.filter { it.category == category }

    private fun spokenTitle(family: Family): String = when (family) {
        Family.TARGET -> "Reconoce el objeto"
        Family.SAME -> "Busca la palabra igual"
        Family.DIFFERENT -> "Encuentra lo diferente"
        Family.COUNT -> "Cuenta con atención"
        Family.SEQUENCE -> "Completa la secuencia"
        Family.CATEGORY -> "Evoca la categoría"
        Family.COLOR -> "Identifica el color"
        Family.POSITION -> "Indica la posición"
        Family.RULE -> "Sigue la regla"
        Family.DAILY -> "Planifica la situación cotidiana"
        Family.FOCALIZATION -> "Busca el estímulo indicado"
        Family.MAIN -> "Encuentra la información importante"
        Family.SUSTAINED -> "Mantén la atención"
        Family.ALTERNATION -> "Cambia siguiendo la pauta"
        Family.DIVIDED -> "Atiende a dos indicaciones"
    }

    fun question(id: String, challengeLevel: Int): AttentionQuestion? {
        val activity = items.firstOrNull { it.id == id } ?: return null
        val parts = id.removePrefix("guided_attention_").split("_")
        val family = Family.entries.firstOrNull { it.name.lowercase() == parts.firstOrNull() } ?: return null
        val number = parts.lastOrNull()?.toIntOrNull() ?: return null
        val stimulus = stimuli[(number - 1) % stimuli.size]
        val level = challengeLevel.coerceIn(1, 5)
        val triple = when (family) {
            Family.TARGET -> choice("Mira con atención. ¿Qué objeto ves?", stimulus.name, stimuli.filter { it != stimulus }.map { it.name }, level)
            Family.SAME -> choice("Elige la palabra que es igual al objeto de la imagen.", stimulus.name, stimuli.filter { it != stimulus }.map { it.name }, level)
            Family.DIFFERENT -> different(stimulus, level)
            Family.COUNT -> count(number, level)
            Family.SEQUENCE -> sequence(number, level)
            Family.CATEGORY -> choice("Sin distraerte, ¿a qué grupo pertenece lo que ves?", stimulus.category, listOf("Fruta", "Animal", "Hogar", "Objeto", "Transporte").filter { it != stimulus.category }, level)
            Family.COLOR -> choice("Fíjate bien. ¿Qué color predomina?", stimulus.color, listOf("Rojo", "Amarillo", "Azul", "Marrón", "Negro", "Blanco", "Gris").filter { it != stimulus.color }, level)
            Family.POSITION -> position(number, level)
            Family.RULE -> rule(number, level)
            Family.DAILY -> daily(number, level)
            Family.FOCALIZATION -> focalization(stimulus, level)
            Family.MAIN -> mainInfo(number, level)
            Family.SUSTAINED -> sustained(number, level)
            Family.ALTERNATION -> alternation(number, level)
            Family.DIVIDED -> divided(number, level)
        }
        return AttentionQuestion(distinctCatalogPrompt(triple.first, number), triple.second, triple.third, activity.imageUrl)
    }

    private fun different(stimulus: Stimulus, level: Int): Triple<String, List<String>, String> {
        val correct = when (stimulus.category) { "Fruta" -> "Se puede comer"; "Animal" -> "Es un ser vivo"; "Transporte" -> "Sirve para viajar"; else -> "Es un objeto de uso diario" }
        return choice("¿Qué dato diferencia mejor lo que ves?", correct, listOf("Es una estación", "Es una prenda", "Es una vivienda"), level)
    }

    private fun count(number: Int, level: Int): Triple<String, List<String>, String> {
        val amount = 2 + (number % (if (level >= 4) 6 else 4))
        return choice("Cuenta con calma: si ves $amount ${if (amount == 1) "objeto" else "objetos"}, ¿cuántos hay?", amount.toString(), listOf((amount - 1).toString(), (amount + 1).toString(), (amount + 2).toString()), level)
    }

    private fun sequence(number: Int, level: Int): Triple<String, List<String>, String> {
        val first = (number % 5) + 1
        val second = first + 1
        val correct = (second + 1).toString()
        return choice("Mantén la atención y completa: $first, $second, __.", correct, listOf(first.toString(), (second + 2).toString(), (first + 4).toString()), level)
    }

    private fun position(number: Int, level: Int): Triple<String, List<String>, String> {
        val examples = listOf(
            Triple("Si la taza está encima de la mesa, ¿dónde está?", "Encima", "Debajo"),
            Triple("Si el libro está dentro del cajón, ¿dónde está?", "Dentro", "Fuera"),
            Triple("Si el autobús está delante de la parada, ¿dónde está?", "Delante", "Detrás"),
            Triple("Si el teléfono está a la izquierda del reloj, ¿dónde está?", "A la izquierda", "A la derecha"),
            Triple("Si la silla está junto a la mesa, ¿están cerca?", "Sí", "No")
        )[number % 5]
        return choice(examples.first, examples.second, listOf(examples.third, "No se sabe", "Lejos"), level)
    }

    private fun rule(number: Int, level: Int): Triple<String, List<String>, String> {
        val examples = listOf(
            Triple("Regla: toca solo las frutas. ¿Cuál eliges?", "Manzana", "Reloj"),
            Triple("Regla: toca solo animales. ¿Cuál eliges?", "Perro", "Libro"),
            Triple("Regla: toca solo objetos para llamar. ¿Cuál eliges?", "Teléfono", "Taza"),
            Triple("Regla: toca solo medios de transporte. ¿Cuál eliges?", "Autobús", "Silla"),
            Triple("Regla: toca solo objetos para beber. ¿Cuál eliges?", "Taza", "Gato")
        )[number % 5]
        return choice(examples.first, examples.second, listOf(examples.third, "Ninguno", "Todos"), level)
    }

    private fun daily(number: Int, level: Int): Triple<String, List<String>, String> {
        val examples = listOf(
            Triple("Para llegar a una cita a tiempo, ¿en qué dato debes fijarte?", "La hora", "El color de la pared"),
            Triple("Al tomar una medicina, ¿qué dato debes comprobar?", "El nombre", "El tiempo que hace"),
            Triple("Al cruzar una calle, ¿en qué debes fijarte?", "En los vehículos", "En las nubes"),
            Triple("Al subir a un autobús, ¿qué debes comprobar?", "La línea", "El tamaño del asiento"),
            Triple("Al hacer una lista de compra, ¿en qué debes fijarte?", "En lo que falta", "En el ruido"))
        val item = examples[number % examples.size]
        return choice(item.first, item.second, listOf(item.third, "En nada", "En otra cosa"), level)
    }

    private fun focalization(stimulus: Stimulus, level: Int): Triple<String, List<String>, String> =
        choice("Busca solo el estímulo indicado. ¿Cuál es \"${stimulus.name}\"?", stimulus.name, stimuli.filter { it != stimulus }.map { it.name }, level)

    private fun mainInfo(number: Int, level: Int): Triple<String, List<String>, String> {
        val examples = listOf(
            Triple("Para no perder un autobús, ¿qué información es la más importante?", "La hora de salida", "El color del asiento"),
            Triple("En una receta, ¿qué dato necesitamos para preparar la comida?", "Los ingredientes", "El color del papel"),
            Triple("En una cita médica, ¿qué conviene recordar?", "El día y la hora", "El tamaño de la sala"),
            Triple("En una lista de compra, ¿qué buscamos primero?", "Lo que falta en casa", "El ruido de la tienda"),
            Triple("En una señal de calle, ¿qué dato nos orienta?", "El nombre de la calle", "El color del cielo")
        )[number % 5]
        return choice(examples.first, examples.second, listOf(examples.third, "Un dato sin importancia"), level)
    }

    private fun sustained(number: Int, level: Int): Triple<String, List<String>, String> {
        val target = (number % 4) + 2
        return choice("Mantén la atención: cuenta desde 1 hasta $target. ¿Cuál es el último número?", target.toString(), listOf((target - 1).toString(), (target + 1).toString(), "0"), level)
    }

    private fun alternation(number: Int, level: Int): Triple<String, List<String>, String> {
        val examples = listOf(
            Triple("Sigue la pauta número-letra: 1, A, 2, B, 3, __. ¿Qué sigue?", "C", "4"),
            Triple("Sigue la pauta fruta-animal: manzana, perro, plátano, gato, __. ¿Qué sigue?", "Fruta", "Animal"),
            Triple("Sigue la pauta día-número: lunes, 1, martes, 2, __. ¿Qué sigue?", "Miércoles", "3"),
            Triple("Sigue la pauta arriba-abajo: arriba, abajo, arriba, __. ¿Qué sigue?", "Abajo", "Arriba"),
            Triple("Sigue la pauta grande-pequeño: grande, pequeño, grande, __. ¿Qué sigue?", "Pequeño", "Grande")
        )[number % 5]
        return choice(examples.first, examples.second, listOf(examples.third, "No hay pauta"), level)
    }

    private fun divided(number: Int, level: Int): Triple<String, List<String>, String> {
        val examples = listOf(
            Triple("Haz dos cosas: busca una fruta y di su color. ¿Cuál es una respuesta correcta?", "Manzana roja", "Reloj amarillo"),
            Triple("Haz dos cosas: mira el reloj y di para qué sirve. ¿Cuál es correcta?", "Sirve para saber la hora", "Sirve para comer"),
            Triple("Haz dos cosas: busca el animal y di dónde puede vivir. ¿Cuál es correcta?", "Perro en casa", "Libro en un nido"),
            Triple("Haz dos cosas: busca el transporte y di para qué sirve. ¿Cuál es correcta?", "Autobús para viajar", "Taza para viajar"),
            Triple("Haz dos cosas: busca un objeto y di su uso. ¿Cuál es correcta?", "Teléfono para llamar", "Silla para nadar")
        )[number % 5]
        return choice(examples.first, examples.second, listOf(examples.third, "No se puede responder"), level)
    }

    private fun choice(text: String, correct: String, wrong: List<String>, level: Int): Triple<String, List<String>, String> {
        val count = when (level) { 1, 2 -> 2; 3 -> 3; else -> 4 }
        return Triple(text, (listOf(correct) + wrong.filter { it != correct }).take(count).shuffled(), correct)
    }
}
