package com.terapia.terasenior.treatment.repository

/** Banco de 500 actividades de memoria con apoyos cotidianos para personas mayores. */
object MemoryExerciseCatalog {
    data class MemoryExercise(val id: String, val name: String, val description: String, val imageUrl: String)
    data class MemoryQuestion(val text: String, val options: List<String>, val correctAnswer: String, val imageUrl: String)

    private data class Card(val name: String, val category: String, val place: String, val use: String, val image: String)
    private val cards = listOf(
        Card("Manzana", "Fruta", "Frutero", "Comer", RealisticExerciseImageCatalog.apple),
        Card("Plátano", "Fruta", "Frutero", "Comer", RealisticExerciseImageCatalog.banana),
        Card("Perro", "Animal", "Casa o calle", "Compañía", RealisticExerciseImageCatalog.dog),
        Card("Gato", "Animal", "Casa o calle", "Compañía", RealisticExerciseImageCatalog.cat),
        Card("Reloj", "Objeto", "Pared o mesa", "Saber la hora", RealisticExerciseImageCatalog.clock),
        Card("Taza", "Hogar", "Cocina", "Beber", RealisticExerciseImageCatalog.cup),
        Card("Silla", "Hogar", "Salón", "Sentarse", RealisticExerciseImageCatalog.chair),
        Card("Teléfono", "Objeto", "Mesa", "Llamar", RealisticExerciseImageCatalog.phone),
        Card("Libro", "Objeto", "Estantería", "Leer", RealisticExerciseImageCatalog.book),
        Card("Autobús", "Transporte", "Parada", "Viajar", RealisticExerciseImageCatalog.bus)
    )

    private enum class Family(val title: String, val description: String) {
        VISUAL("Recuerdo visual", "Recuerda un objeto familiar visto en una imagen."),
        ASSOCIATION("Asociaciones", "Relaciona un objeto con una idea cotidiana."),
        CATEGORY("Recuerdo por categoría", "Evoca el grupo al que pertenece una palabra."),
        LOCATION("Dónde estaba", "Recuerda el lugar habitual de un objeto."),
        USE("Para qué sirve", "Recuerda el uso práctico de un objeto."),
        SEQUENCE("Secuencias", "Mantiene y recupera una serie breve."),
        DAILY("Memoria cotidiana", "Recuerda una información útil del día a día."),
        PAIRS("Parejas", "Reconoce una pareja relacionada."),
        ORDER("Orden temporal", "Recuerda el orden lógico de una acción."),
        WORDS("Palabras recordadas", "Identifica la palabra que se pidió recordar.")
    }

    val items: List<MemoryExercise> = Family.entries.flatMap { family ->
        (1..50).map { number ->
            val card = cards[(number - 1) % cards.size]
            MemoryExercise("guided_memory_${family.name.lowercase()}_$number", "${family.title} ${number.toString().padStart(2, '0')}", family.description, card.image)
        }
    }

    init {
        check(items.size == 500) { "El catálogo de memoria debe contener 500 actividades." }
        check(items.map { it.id }.distinct().size == items.size) { "Cada actividad debe tener un identificador único." }
    }

    fun contains(id: String): Boolean = id.startsWith("guided_memory_") && items.any { it.id == id }
    fun forCategory(category: String): List<MemoryExercise> = if (category == "Memoria") items else emptyList()

    fun question(id: String, challengeLevel: Int): MemoryQuestion? {
        val activity = items.firstOrNull { it.id == id } ?: return null
        val parts = id.removePrefix("guided_memory_").split("_")
        val family = Family.entries.firstOrNull { it.name.lowercase() == parts.firstOrNull() } ?: return null
        val number = parts.lastOrNull()?.toIntOrNull() ?: return null
        val card = cards[(number - 1) % cards.size]
        val level = challengeLevel.coerceIn(1, 5)
        val triple = when (family) {
            Family.VISUAL -> choice("Mira y recuerda. ¿Qué objeto aparece en la imagen?", card.name, cards.filter { it != card }.map { it.name }, level)
            Family.ASSOCIATION -> choice("Recuerda la relación: ¿qué palabra va con \"${card.name}\"?", card.category, cards.filter { it.category != card.category }.map { it.category }, level)
            Family.CATEGORY -> choice("¿A qué grupo pertenece \"${card.name}\"?", card.category, listOf("Fruta", "Animal", "Hogar", "Objeto", "Transporte").filter { it != card.category }, level)
            Family.LOCATION -> choice("Recuerda dónde solemos encontrarlo. ¿Dónde está \"${card.name}\"?", card.place, cards.filter { it.place != card.place }.map { it.place }, level)
            Family.USE -> choice("¿Para qué se usa \"${card.name}\"?", card.use, cards.filter { it.use != card.use }.map { it.use }, level)
            Family.SEQUENCE -> sequence(number, level)
            Family.DAILY -> daily(number, level)
            Family.PAIRS -> pair(card, level)
            Family.ORDER -> order(number, level)
            Family.WORDS -> words(number, level)
        }
        return MemoryQuestion(triple.first, triple.second, triple.third, activity.imageUrl)
    }

    private fun sequence(number: Int, level: Int): Triple<String, List<String>, String> {
        val first = cards[number % cards.size].name
        val second = cards[(number + 1) % cards.size].name
        val third = cards[(number + 2) % cards.size].name
        val correct = if (level <= 2) second else third
        val text = if (level <= 2) "Recuerda la serie: $first, $second. ¿Cuál fue la segunda palabra?" else "Recuerda la serie: $first, $second, $third. ¿Cuál fue la última palabra?"
        return choice(text, correct, cards.map { it.name }.filter { it != correct }, level)
    }

    private fun daily(number: Int, level: Int): Triple<String, List<String>, String> {
        val examples = listOf(
            Triple("Para recordar una cita, ¿dónde es útil anotarla?", "En el calendario", "En la nevera sin nota"),
            Triple("Para recordar una medicina, ¿qué ayuda?", "Una pauta escrita", "Dejarla escondida"),
            Triple("Para recordar una llamada, ¿qué conviene hacer?", "Anotarla", "No decir nada"),
            Triple("Para recordar una compra, ¿qué ayuda?", "Hacer una lista", "Ir sin pensar"),
            Triple("Para recordar una llave, ¿qué conviene hacer?", "Dejarla en su lugar habitual", "Cambiarla de sitio cada día"))
        val item = examples[number % examples.size]
        return choice(item.first, item.second, listOf(item.third, "No importa", "Olvidarlo"), level)
    }

    private fun pair(card: Card, level: Int): Triple<String, List<String>, String> {
        val match = when (card.name) { "Taza" -> "Café"; "Teléfono" -> "Llamada"; "Libro" -> "Lectura"; "Autobús" -> "Parada"; "Reloj" -> "Hora"; else -> card.category }
        return choice("Recuerda la pareja más relacionada con \"${card.name}\".", match, listOf("Techo", "Zapato", "Nube"), level)
    }

    private fun order(number: Int, level: Int): Triple<String, List<String>, String> {
        val examples = listOf(
            Triple("Para beber café, ¿qué hacemos después de preparar una taza?", "Servir la bebida", "Guardar la taza"),
            Triple("Para llamar por teléfono, ¿qué hacemos primero?", "Buscar el número", "Colgar antes de llamar"),
            Triple("Para leer un libro, ¿qué hacemos primero?", "Abrir el libro", "Cerrar los ojos"),
            Triple("Para coger un autobús, ¿qué hacemos antes?", "Ir a la parada", "Bajarse sin subir"),
            Triple("Para sentarse, ¿qué hacemos antes?", "Acercarse a la silla", "Guardar la silla"))
        val item = examples[number % examples.size]
        return choice(item.first, item.second, listOf(item.third, "Hacer otra cosa", "No hacer nada"), level)
    }

    private fun words(number: Int, level: Int): Triple<String, List<String>, String> {
        val target = cards[number % cards.size].name
        return choice("Recuerda esta palabra: \"$target\". ¿Cuál era?", target, cards.filter { it.name != target }.map { it.name }, level)
    }

    private fun choice(text: String, correct: String, wrong: List<String>, level: Int): Triple<String, List<String>, String> {
        val count = when (level) { 1, 2 -> 2; 3 -> 3; else -> 4 }
        return Triple(text, (listOf(correct) + wrong.filter { it != correct }).take(count).shuffled(), correct)
    }
}
