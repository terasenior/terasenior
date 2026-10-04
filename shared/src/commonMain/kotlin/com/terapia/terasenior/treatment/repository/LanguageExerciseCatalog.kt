package com.terapia.terasenior.treatment.repository

/**
 * Banco de 500 actividades de lenguaje para personas mayores.
 * Las tareas se basan en vocabulario cotidiano y apoyos fotográficos claros.
 * La sesión adapta alternativas y carga verbal al nivel seleccionado.
 */
object LanguageExerciseCatalog {
    data class LanguageExercise(val id: String, val name: String, val description: String, val imageUrl: String)
    data class LanguageQuestion(val text: String, val options: List<String>, val correctAnswer: String, val imageUrl: String)

    private data class WordCard(val name: String, val category: String, val use: String, val image: String)
    private val words = listOf(
        WordCard("Manzana", "Frutas", "Comer", RealisticExerciseImageCatalog.apple),
        WordCard("Plátano", "Frutas", "Comer", RealisticExerciseImageCatalog.banana),
        WordCard("Perro", "Animales", "Compañía", RealisticExerciseImageCatalog.dog),
        WordCard("Gato", "Animales", "Compañía", RealisticExerciseImageCatalog.cat),
        WordCard("Reloj", "Objetos", "Saber la hora", RealisticExerciseImageCatalog.clock),
        WordCard("Taza", "Hogar", "Beber", RealisticExerciseImageCatalog.cup),
        WordCard("Silla", "Hogar", "Sentarse", RealisticExerciseImageCatalog.chair),
        WordCard("Teléfono", "Objetos", "Llamar", RealisticExerciseImageCatalog.phone),
        WordCard("Libro", "Objetos", "Leer", RealisticExerciseImageCatalog.book),
        WordCard("Autobús", "Transporte", "Viajar", RealisticExerciseImageCatalog.bus)
    )

    private enum class Family(val title: String, val description: String) {
        NAMING("Denominación", "Nombra un objeto cotidiano a partir de una imagen."),
        CATEGORY("Categorías", "Reconoce la familia semántica de una palabra."),
        FUNCTION("Palabras y uso", "Relaciona una palabra con su uso habitual."),
        INITIAL("Letra inicial", "Identifica la letra con la que empieza una palabra."),
        SYLLABLE("Completar sílabas", "Completa una palabra sencilla con la sílaba adecuada."),
        OPPOSITES("Palabras opuestas", "Escoge el significado contrario más sencillo."),
        SENTENCES("Completar frases", "Completa una frase cotidiana con sentido."),
        COMPREHENSION("Comprensión oral", "Comprende una instrucción breve y clara."),
        EXPRESSIONS("Expresiones cotidianas", "Completa expresiones conocidas de la vida diaria."),
        COMMUNICATION("Comunicación práctica", "Elige una respuesta adecuada en una situación común.")
    }

    val items: List<LanguageExercise> = Family.entries.flatMap { family ->
        (1..50).map { number ->
            val word = words[(number - 1) % words.size]
            LanguageExercise(
                id = "guided_language_${family.name.lowercase()}_$number",
                name = spokenTitle(family),
                description = family.description,
                imageUrl = word.image
            )
        }
    }

    init {
        check(items.size == 500) { "El catálogo de lenguaje debe contener 500 actividades." }
        check(items.map { it.id }.distinct().size == items.size) { "Cada actividad debe tener un identificador único." }
        check(items.mapNotNull { question(it.id, 3)?.text }.distinct().size == items.size) {
            "Cada actividad de lenguaje debe tener una consigna distinta."
        }
    }

    fun contains(id: String): Boolean = id.startsWith("guided_language_") && items.any { it.id == id }
    fun forCategory(category: String): List<LanguageExercise> = if (category == "Lenguaje") items else emptyList()

    private fun spokenTitle(family: Family): String = when (family) {
        Family.NAMING -> "Nombra el objeto de la imagen"
        Family.CATEGORY -> "Reconoce la categoría de la palabra"
        Family.FUNCTION -> "Indica para qué sirve"
        Family.INITIAL -> "Encuentra la letra inicial"
        Family.SYLLABLE -> "Completa la palabra"
        Family.OPPOSITES -> "Busca la palabra contraria"
        Family.SENTENCES -> "Completa la frase"
        Family.COMPREHENSION -> "Comprende la instrucción"
        Family.EXPRESSIONS -> "Completa la expresión"
        Family.COMMUNICATION -> "Elige una respuesta adecuada"
    }

    fun question(id: String, challengeLevel: Int): LanguageQuestion? {
        val activity = items.firstOrNull { it.id == id } ?: return null
        val parts = id.removePrefix("guided_language_").split("_")
        val family = Family.entries.firstOrNull { it.name.lowercase() == parts.firstOrNull() } ?: return null
        val number = parts.lastOrNull()?.toIntOrNull() ?: return null
        val word = words[(number - 1) % words.size]
        val level = challengeLevel.coerceIn(1, 5)
        val triple = when (family) {
            Family.NAMING -> choice("¿Qué objeto aparece en la imagen?", word.name, words.filter { it != word }.map { it.name }, level)
            Family.CATEGORY -> choice("¿A qué categoría pertenece \"${word.name}\"?", word.category, listOf("Frutas", "Animales", "Hogar", "Objetos", "Transporte").filter { it != word.category }, level)
            Family.FUNCTION -> choice("¿Para qué usamos principalmente \"${word.name}\"?", word.use, words.filter { it.use != word.use }.map { it.use }, level)
            Family.INITIAL -> choice("¿Con qué letra empieza \"${word.name}\"?", word.name.first().uppercase(), listOf("A", "B", "C", "M", "P", "S", "T").filter { it != word.name.first().uppercase() }, level)
            Family.SYLLABLE -> syllable(word.name, level)
            Family.OPPOSITES -> opposite(number, level)
            Family.SENTENCES -> sentence(number, level)
            Family.COMPREHENSION -> comprehension(number, level)
            Family.EXPRESSIONS -> expression(number, level)
            Family.COMMUNICATION -> communication(number, level)
        }
        return LanguageQuestion(distinctCatalogPrompt(triple.first, number), triple.second, triple.third, activity.imageUrl)
    }

    private fun syllable(word: String, level: Int): Triple<String, List<String>, String> {
        val cut = (word.length / 2).coerceAtLeast(1)
        return choice("Completa la palabra: ${word.take(cut).uppercase()}__", word.drop(cut).uppercase(), listOf("MA", "TA", "SO", "NA"), level)
    }

    private fun opposite(number: Int, level: Int): Triple<String, List<String>, String> {
        val examples = listOf(
            Triple("¿Cuál es la palabra contraria de \"grande\"?", "Pequeño", "Alto"),
            Triple("¿Cuál es la palabra contraria de \"frío\"?", "Caliente", "Mojado"),
            Triple("¿Cuál es la palabra contraria de \"cerca\"?", "Lejos", "Dentro"),
            Triple("¿Cuál es la palabra contraria de \"abrir\"?", "Cerrar", "Mirar"),
            Triple("¿Cuál es la palabra contraria de \"día\"?", "Noche", "Mañana")
        )[number % 5]
        return choice(examples.first, examples.second, listOf(examples.third, "Igual", "No sé"), level)
    }

    private fun sentence(number: Int, level: Int): Triple<String, List<String>, String> {
        val examples = listOf(
            Triple("Completa: Para saber la hora miro el…", "RELOJ", "ZAPATO"),
            Triple("Completa: Para llamar a un familiar uso el…", "TELÉFONO", "TENEDOR"),
            Triple("Completa: Para leer una historia abro un…", "LIBRO", "PLATO"),
            Triple("Completa: Para sentarme uso una…", "SILLA", "NUBE"),
            Triple("Completa: Para beber café uso una…", "TAZA", "LLAVE")
        )[number % 5]
        return choice(examples.first, examples.second, listOf(examples.third, "VENTANA", "JARDÍN"), level)
    }

    private fun comprehension(number: Int, level: Int): Triple<String, List<String>, String> {
        val examples = listOf(
            Triple("Escucha: \"Mira el calendario\". ¿Qué debes hacer?", "Consultar la fecha", "Cerrar los ojos"),
            Triple("Escucha: \"Llama a María\". ¿Qué debes hacer?", "Usar el teléfono", "Ir a dormir"),
            Triple("Escucha: \"Bebe despacio\". ¿Qué debes hacer?", "Beber con calma", "Correr deprisa"),
            Triple("Escucha: \"Ponte el abrigo\". ¿Qué debes hacer?", "Ponerse una prenda", "Guardar los zapatos"),
            Triple("Escucha: \"Cierra la puerta\". ¿Qué debes hacer?", "Cerrar la puerta", "Abrir la puerta")
        )[number % 5]
        return choice(examples.first, examples.second, listOf(examples.third, "No hacer nada", "Cambiar de tema"), level)
    }

    private fun expression(number: Int, level: Int): Triple<String, List<String>, String> {
        val examples = listOf(
            Triple("Completa: \"Buenos días, ¿cómo…?\"", "estás", "comes"),
            Triple("Completa: \"Por favor, ¿puede ayudarme…?\"", "un momento", "el techo"),
            Triple("Completa: \"Muchas…\"", "gracias", "sillas"),
            Triple("Completa: \"Hasta…\"", "mañana", "reloj"),
            Triple("Completa: \"Que tenga buen…\"", "día", "autobús")
        )[number % 5]
        return choice(examples.first, examples.second, listOf(examples.third, "zapato", "ventana"), level)
    }

    private fun communication(number: Int, level: Int): Triple<String, List<String>, String> {
        val examples = listOf(
            Triple("No ha entendido una explicación. ¿Qué puede decir?", "¿Puede repetirlo, por favor?", "No voy a escuchar"),
            Triple("Quiere saber la hora de una cita. ¿Qué puede preguntar?", "¿A qué hora es la cita?", "¿Dónde está el sol?"),
            Triple("Necesita ayuda para encontrar una sala. ¿Qué puede decir?", "¿Puede indicarme dónde está?", "No quiero hablar"),
            Triple("Le ofrecen un café y no quiere. ¿Qué puede responder?", "No, gracias", "Dame dos"),
            Triple("Quiere llamar a un familiar. ¿Qué puede pedir?", "¿Me deja usar el teléfono?", "Apague la luz")
        )[number % 5]
        return choice(examples.first, examples.second, listOf(examples.third, "No sé", "Me voy"), level)
    }

    private fun choice(text: String, correct: String, wrong: List<String>, level: Int): Triple<String, List<String>, String> {
        val count = when (level) { 1, 2 -> 2; 3 -> 3; else -> 4 }
        return Triple(text, (listOf(correct) + wrong.filter { it != correct }).take(count).shuffled(), correct)
    }
}
