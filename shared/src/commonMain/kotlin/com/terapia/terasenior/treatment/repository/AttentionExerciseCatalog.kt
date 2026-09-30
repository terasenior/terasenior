package com.terapia.terasenior.treatment.repository

/** Banco de 500 actividades de atención para personas mayores. */
object AttentionExerciseCatalog {
    data class AttentionExercise(val id: String, val name: String, val description: String, val imageUrl: String)
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

    private enum class Family(val title: String, val description: String) {
        TARGET("Busca el objetivo", "Mantén la atención en un objeto conocido."),
        SAME("Identifica iguales", "Selecciona la palabra que coincide con la imagen."),
        DIFFERENT("Encuentra el diferente", "Distingue una característica visual importante."),
        COUNT("Conteo atento", "Cuenta elementos sencillos sin perder la consigna."),
        SEQUENCE("Secuencias", "Completa una serie corta manteniendo la atención."),
        CATEGORY("Atención a categorías", "Selecciona el grupo correcto sin distraerte."),
        COLOR("Atención al color", "Identifica el color predominante del estímulo."),
        POSITION("Atención espacial", "Responde a una posición sencilla."),
        RULE("Cambio de regla", "Aplica una regla breve y clara."),
        DAILY("Atención cotidiana", "Detecta el dato importante de una situación diaria.")
    }

    val items: List<AttentionExercise> = Family.entries.flatMap { family ->
        (1..50).map { number ->
            val stimulus = stimuli[(number - 1) % stimuli.size]
            AttentionExercise("guided_attention_${family.name.lowercase()}_$number", "${family.title} ${number.toString().padStart(2, '0')}", family.description, stimulus.image)
        }
    }

    init {
        check(items.size == 500) { "El catálogo de atención debe contener 500 actividades." }
        check(items.map { it.id }.distinct().size == items.size) { "Cada actividad debe tener un identificador único." }
    }

    fun contains(id: String): Boolean = id.startsWith("guided_attention_") && items.any { it.id == id }
    fun forCategory(category: String): List<AttentionExercise> = if (category == "Atención") items else emptyList()

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
        }
        return AttentionQuestion(triple.first, triple.second, triple.third, activity.imageUrl)
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

    private fun choice(text: String, correct: String, wrong: List<String>, level: Int): Triple<String, List<String>, String> {
        val count = when (level) { 1, 2 -> 2; 3 -> 3; else -> 4 }
        return Triple(text, (listOf(correct) + wrong.filter { it != correct }).take(count).shuffled(), correct)
    }
}
