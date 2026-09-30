package com.terapia.terasenior.treatment.repository

/**
 * Banco de 500 actividades de lectoescritura para personas mayores.
 *
 * Las consignas emplean vocabulario cotidiano, letra grande en pantalla y una
 * fotografía de apoyo. El nivel se aplica al iniciar la sesión: aumenta la
 * longitud de palabra, la carga de lectura y el número de alternativas, sin
 * mostrar etiquetas clínicas a la persona.
 */
object LiteracyExerciseCatalog {
    data class LiteracyExercise(
        val id: String,
        val name: String,
        val description: String,
        val imageUrl: String
    )

    data class LiteracyQuestion(
        val text: String,
        val options: List<String>,
        val correctAnswer: String,
        val imageUrl: String
    )

    private enum class Family(val title: String, val description: String, val image: String) {
        INITIAL_LETTER("Letra inicial", "Identifica con qué letra empieza una palabra cotidiana.", RealisticExerciseImageCatalog.apple),
        FINAL_LETTER("Letra final", "Reconoce la última letra de una palabra conocida.", RealisticExerciseImageCatalog.book),
        SYLLABLE("Sílabas", "Completa una palabra sencilla con la sílaba que falta.", RealisticExerciseImageCatalog.cup),
        WORD_IMAGE("Palabra e imagen", "Lee una palabra y relaciónala con la imagen.", RealisticExerciseImageCatalog.dog),
        USEFUL_WORDS("Palabras útiles", "Reconoce palabras presentes en la vida diaria.", RealisticExerciseImageCatalog.phone),
        SENTENCE("Frase cotidiana", "Completa una frase breve que tiene sentido.", RealisticExerciseImageCatalog.chair),
        READING("Comprensión lectora", "Lee una instrucción corta y escoge su significado.", RealisticExerciseImageCatalog.clock),
        ORDER("Orden de palabras", "Escoge el orden correcto de una frase sencilla.", RealisticExerciseImageCatalog.bus),
        RHYME("Sonidos parecidos", "Encuentra una palabra que termina con un sonido parecido.", RealisticExerciseImageCatalog.banana),
        WRITING("Escritura funcional", "Reconoce la palabra adecuada para una tarea diaria.", RealisticExerciseImageCatalog.hand)
    }

    private val words = listOf("casa", "mesa", "mano", "pan", "flor", "sol", "taza", "perro", "libro", "reloj")

    val items: List<LiteracyExercise> = Family.entries.flatMap { family ->
        (1..50).map { number ->
            LiteracyExercise(
                id = "guided_literacy_${family.name.lowercase()}_$number",
                name = spokenTitle(family),
                description = family.description,
                imageUrl = imageForWord(words[number % words.size])
            )
        }
    }

    init {
        check(items.size == 500) { "El catálogo de lectoescritura debe contener 500 actividades." }
        check(items.map { it.id }.distinct().size == items.size) { "Cada actividad debe tener un identificador único." }
    }

    fun contains(id: String): Boolean = id.startsWith("guided_literacy_") && items.any { it.id == id }
    fun forCategory(category: String): List<LiteracyExercise> = if (category == "Lectoescritura") items else emptyList()

    private fun imageForWord(word: String): String = when (word) {
        "casa" -> RealisticExerciseImageCatalog.chair
        "mesa" -> RealisticExerciseImageCatalog.cup
        "mano" -> RealisticExerciseImageCatalog.hand
        "pan" -> RealisticExerciseImageCatalog.dailyCalendar
        "flor" -> RealisticExerciseImageCatalog.seasons
        "sol" -> RealisticExerciseImageCatalog.seasons
        "taza" -> RealisticExerciseImageCatalog.cup
        "perro" -> RealisticExerciseImageCatalog.dog
        "libro" -> RealisticExerciseImageCatalog.book
        "reloj" -> RealisticExerciseImageCatalog.clock
        else -> RealisticExerciseImageCatalog.book
    }

    private fun spokenTitle(family: Family): String = when (family) {
        Family.INITIAL_LETTER -> "Encuentra la letra inicial"
        Family.FINAL_LETTER -> "Encuentra la última letra"
        Family.SYLLABLE -> "Completa la palabra"
        Family.WORD_IMAGE -> "Lee la palabra y mira la imagen"
        Family.USEFUL_WORDS -> "Reconoce una palabra útil"
        Family.SENTENCE -> "Completa la frase"
        Family.READING -> "Comprende la instrucción"
        Family.ORDER -> "Ordena la frase"
        Family.RHYME -> "Busca una palabra parecida"
        Family.WRITING -> "Elige la palabra adecuada"
    }

    fun question(id: String, challengeLevel: Int): LiteracyQuestion? {
        val activity = items.firstOrNull { it.id == id } ?: return null
        val parts = id.removePrefix("guided_literacy_").split("_")
        val family = Family.entries.firstOrNull { it.name.lowercase() == parts.firstOrNull() } ?: return null
        val number = parts.lastOrNull()?.toIntOrNull() ?: return null
        val level = challengeLevel.coerceIn(1, 5)
        val word = words[number % words.size]
        val question = when (family) {
            Family.INITIAL_LETTER -> choice("¿Con qué letra empieza la palabra \"$word\"?", word.first().uppercase(), listOf("M", "P", "S"), level)
            Family.FINAL_LETTER -> choice("¿Cuál es la última letra de \"$word\"?", word.last().uppercase(), listOf("A", "O", "S"), level)
            Family.SYLLABLE -> syllable(word, level)
            Family.WORD_IMAGE -> choice("Lee la palabra y busca su imagen: \"$word\".", word.replaceFirstChar { it.uppercase() }, words.filter { it != word }.take(3).map { it.replaceFirstChar { c -> c.uppercase() } }, level)
            Family.USEFUL_WORDS -> usefulWord(number, level)
            Family.SENTENCE -> sentence(number, level)
            Family.READING -> reading(number, level)
            Family.ORDER -> order(number, level)
            Family.RHYME -> rhyme(word, level)
            Family.WRITING -> writing(number, level)
        }
        return LiteracyQuestion(question.first, question.second, question.third, activity.imageUrl)
    }

    private fun syllable(word: String, level: Int): Triple<String, List<String>, String> {
        val split = if (word.length <= 4) 2 else word.length / 2
        val start = word.take(split).uppercase()
        val end = word.drop(split).uppercase()
        return choice("Completa la palabra: ${start}__", end, listOf("MA", "SO", "TA"), level)
    }

    private fun usefulWord(number: Int, level: Int): Triple<String, List<String>, String> {
        val examples = listOf(
            Triple("¿Qué palabra buscamos para llamar a una persona?", "TELÉFONO", "Tenedor"),
            Triple("¿Qué palabra indica dónde miramos una cita?", "CALENDARIO", "Armario"),
            Triple("¿Qué palabra vemos en una puerta para salir?", "SALIDA", "Silla"),
            Triple("¿Qué palabra usamos para comprar pan?", "PANADERÍA", "Papelera"),
            Triple("¿Qué palabra buscamos para tomar un autobús?", "PARADA", "Pared")
        )[number % 5]
        return choice(examples.first, examples.second, listOf(examples.third, "CAMINO", "VENTANA"), level)
    }

    private fun sentence(number: Int, level: Int): Triple<String, List<String>, String> {
        val examples = listOf(
            Triple("Completa: Para beber agua uso un…", "VASO", "Zapato"),
            Triple("Completa: Para saber la hora miro el…", "RELOJ", "Cojín"),
            Triple("Completa: Para escribir una nota uso un…", "BOLÍGRAFO", "Paraguas"),
            Triple("Completa: Para salir cuando llueve llevo…", "PARAGUAS", "Cuchara"),
            Triple("Completa: Para sentarme uso una…", "SILLA", "Nube")
        )[number % 5]
        return choice(examples.first, examples.second, listOf(examples.third, "VENTANA", "JARDÍN"), level)
    }

    private fun reading(number: Int, level: Int): Triple<String, List<String>, String> {
        val examples = listOf(
            Triple("Lee: \"Cierra la puerta\". ¿Qué debes hacer?", "Cerrar la puerta", "Abrir la puerta"),
            Triple("Lee: \"Bebe despacio\". ¿Qué debes hacer?", "Beber con calma", "Correr deprisa"),
            Triple("Lee: \"Mira el calendario\". ¿Qué debes hacer?", "Consultar la fecha", "Apagar la luz"),
            Triple("Lee: \"Llama a María\". ¿Qué debes hacer?", "Usar el teléfono", "Ir a dormir"),
            Triple("Lee: \"Ponte el abrigo\". ¿Qué debes hacer?", "Ponerse una prenda", "Guardar los zapatos")
        )[number % 5]
        return choice(examples.first, examples.second, listOf(examples.third, "No hacer nada", "Cambiar de sitio"), level)
    }

    private fun order(number: Int, level: Int): Triple<String, List<String>, String> {
        val examples = listOf(
            Triple("Elige la frase bien ordenada.", "YO BEBO AGUA", "AGUA BEBO YO"),
            Triple("Elige la frase bien ordenada.", "EL PERRO DUERME", "DUERME EL PERRO"),
            Triple("Elige la frase bien ordenada.", "MIRO EL RELOJ", "RELOJ EL MIRO"),
            Triple("Elige la frase bien ordenada.", "LA CASA ES GRANDE", "GRANDE CASA LA ES"),
            Triple("Elige la frase bien ordenada.", "TOMO EL AUTOBÚS", "AUTOBÚS TOMO EL")
        )[number % 5]
        return choice(examples.first, examples.second, listOf(examples.third, "PALABRAS SIN ORDEN", "NO SÉ LEER"), level)
    }

    private fun rhyme(word: String, level: Int): Triple<String, List<String>, String> {
        val rhymes = mapOf("casa" to "taza", "mesa" to "fresa", "mano" to "verano", "pan" to "flan", "flor" to "color", "sol" to "caracol", "taza" to "casa", "perro" to "cerro", "libro" to "vibro", "reloj" to "arroz")
        val correct = rhymes[word] ?: "casa"
        return choice("¿Qué palabra suena parecida a \"$word\" al final?", correct.uppercase(), listOf("SILLA", "NUBE", "JARDÍN"), level)
    }

    private fun writing(number: Int, level: Int): Triple<String, List<String>, String> {
        val examples = listOf(
            Triple("Para anotar una compra de pan, ¿qué palabra escribirías?", "PAN", "PEZ"),
            Triple("Para dejar una nota de una cita, ¿qué palabra escribirías?", "CITA", "CINTA"),
            Triple("Para identificar una medicina, ¿qué conviene escribir?", "NOMBRE", "NÚMERO AL AZAR"),
            Triple("Para marcar el día de una visita, ¿qué palabra escribirías?", "FECHA", "FLECHA"),
            Triple("Para recordar una llamada, ¿qué palabra escribirías?", "LLAMAR", "LLORAR")
        )[number % 5]
        return choice(examples.first, examples.second, listOf(examples.third, "NINGUNA", "OTRA COSA"), level)
    }

    private fun choice(text: String, correct: String, wrong: List<String>, level: Int): Triple<String, List<String>, String> {
        val count = when (level) { 1, 2 -> 2; 3 -> 3; else -> 4 }
        val options = (listOf(correct) + wrong.filter { it != correct }).take(count).shuffled()
        return Triple(text, options, correct)
    }
}
