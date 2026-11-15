package com.terapia.terasenior.treatment.repository

/** Banco de 650 actividades de memoria con apoyos cotidianos para personas mayores. */
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
        WORDS("Palabras o imágenes recordadas", "Recuerda y reconoce palabras o imágenes mostradas."),
        CULTURAL("Conocimiento cultural", "Evoca conocimientos culturales, escolares y semánticos."),
        DECLARATIVE("Memoria declarativa y evocación", "Recuerda hechos, relatos, nombres y experiencias recientes."),
        PROCEDURAL("Memoria procedimental o implícita", "Reconoce cómo se utilizan objetos y acciones habituales.")
    }

    val items: List<MemoryExercise> = Family.entries.flatMap { family ->
        (1..50).map { number ->
            val card = cards[(number - 1) % cards.size]
            MemoryExercise(
                "guided_memory_${family.name.lowercase()}_$number",
                spokenTitle(family),
                family.description,
                imageFor(family, number, card)
            )
        }
    }

    init {
        check(items.size == 650) { "El catálogo de memoria debe contener 650 actividades." }
        check(items.map { it.id }.distinct().size == items.size) { "Cada actividad debe tener un identificador único." }
        check(items.mapNotNull { question(it.id, 3)?.text }.distinct().size == items.size) {
            "Cada actividad de memoria debe tener una consigna distinta."
        }
    }

    fun contains(id: String): Boolean = id.startsWith("guided_memory_") && items.any { it.id == id }
    fun forCategory(category: String): List<MemoryExercise> = if (category == "Memoria") items else emptyList()

    private fun spokenTitle(family: Family): String = when (family) {
        Family.VISUAL -> "Recuerda el objeto de la imagen"
        Family.ASSOCIATION -> "Recuerda la palabra relacionada"
        Family.CATEGORY -> "Recuerda el grupo correcto"
        Family.LOCATION -> "Recuerda dónde se encuentra"
        Family.USE -> "Recuerda para qué sirve"
        Family.SEQUENCE -> "Recuerda la secuencia"
        Family.DAILY -> "Recuerda la rutina diaria"
        Family.PAIRS -> "Encuentra la pareja relacionada"
        Family.ORDER -> "Recuerda el orden"
        Family.WORDS -> "Recuerda la palabra o la imagen"
        Family.CULTURAL -> "Recuerda el conocimiento cultural"
        Family.DECLARATIVE -> "Recuerda la información presentada"
        Family.PROCEDURAL -> "Recuerda cómo se hace"
    }

    /** Selecciona una imagen que apoye la consigna y nunca un objeto ajeno a ella. */
    private fun imageFor(family: Family, number: Int, card: Card): String = when (family) {
        Family.SEQUENCE, Family.WORDS, Family.PROCEDURAL -> cards[number % cards.size].image
        Family.DAILY -> when (number % 5) {
            2 -> RealisticExerciseImageCatalog.phone
            else -> RealisticExerciseImageCatalog.dailyCalendar
        }
        Family.ORDER -> when (number % 5) {
            0 -> RealisticExerciseImageCatalog.cup
            1 -> RealisticExerciseImageCatalog.phone
            2 -> RealisticExerciseImageCatalog.book
            3 -> RealisticExerciseImageCatalog.bus
            else -> RealisticExerciseImageCatalog.chair
        }
        else -> card.image
    }

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
            Family.CULTURAL -> cultural(number, level)
            Family.DECLARATIVE -> declarative(number, level)
            Family.PROCEDURAL -> procedural(number, level)
        }
        return MemoryQuestion(distinctCatalogPrompt(triple.first, number), triple.second, triple.third, activity.imageUrl)
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

    private fun cultural(number: Int, level: Int): Triple<String, List<String>, String> {
        val examples = listOf(
            Triple("¿Qué grupo forman enero, febrero y marzo?", "Meses del año", "Días de la semana"),
            Triple("¿Qué monumento es famoso en Granada?", "La Alhambra", "La Torre Eiffel"),
            Triple("¿Qué planeta vivimos?", "La Tierra", "La Luna"),
            Triple("¿Qué autor escribió Don Quijote?", "Miguel de Cervantes", "Pablo Picasso"),
            Triple("¿Qué grupo forman manzana, pera y plátano?", "Frutas", "Herramientas"),
            Triple("¿Qué mar baña parte de la costa española?", "El Mediterráneo", "El mar Caspio"),
            Triple("¿Qué instrumento se usa para medir la temperatura?", "Termómetro", "Calendario"),
            Triple("¿Qué grupo forman martillo, destornillador y alicate?", "Herramientas", "Animales"),
            Triple("¿Qué estación llega después del verano?", "Otoño", "Primavera"),
            Triple("¿Qué refrán habla de la paciencia?", "Poco a poco se llega lejos", "Nunca hay tiempo")
        )
        val item = examples[number % examples.size]
        return choice(item.first, item.second, listOf(item.third, "No pertenecen al mismo grupo"), level)
    }

    private fun declarative(number: Int, level: Int): Triple<String, List<String>, String> {
        val examples = listOf(
            Triple("Recuerda esta lista: pan, leche y fruta. ¿Qué había que comprar?", "Leche", "Un paraguas"),
            Triple("Recuerda: Ana tiene un perro y Luis tiene un gato. ¿Quién tiene el gato?", "Luis", "Ana"),
            Triple("Recuerda esta frase: el autobús sale a las diez. ¿A qué hora sale?", "A las diez", "A las doce"),
            Triple("Recuerda: primero se lavan las manos y después se come. ¿Qué se hace primero?", "Lavarse las manos", "Comer"),
            Triple("Recuerda estos nombres: Carmen, Pedro y Julia. ¿Cuál era un nombre de mujer?", "Carmen", "Autobús"),
            Triple("Recuerda: ayer hubo paseo por el parque. ¿Dónde fue el paseo?", "En el parque", "En la farmacia"),
            Triple("Recuerda: la cita es el martes por la mañana. ¿Qué día es la cita?", "Martes", "Viernes"),
            Triple("Recuerda: en la receta hacen falta huevos y harina. ¿Qué ingrediente se nombró?", "Harina", "Una silla"),
            Triple("Recuerda: el cumpleaños de Marta es en junio. ¿En qué mes es?", "Junio", "Enero"),
            Triple("Recuerda: el cuento terminaba con una fiesta. ¿Cómo terminaba?", "Con una fiesta", "Con una tormenta")
        )
        val item = examples[number % examples.size]
        return choice(item.first, item.second, listOf(item.third, "No se mencionó"), level)
    }

    private fun procedural(number: Int, level: Int): Triple<String, List<String>, String> {
        val examples = listOf(
            Triple("¿Para qué se usa un peine?", "Para peinarse", "Para cortar papel"),
            Triple("¿Cómo usamos unas gafas?", "Las ponemos delante de los ojos", "Las ponemos en los pies"),
            Triple("¿Para qué sirve un paraguas?", "Para protegerse de la lluvia", "Para cocinar"),
            Triple("¿Qué hacemos con un tenedor al comer?", "Pinchar o coger alimentos", "Escribir una carta"),
            Triple("¿Para qué se usa una tijera?", "Para cortar", "Para medir la hora"),
            Triple("¿Qué se hace con un cepillo de dientes?", "Cepillarse los dientes", "Peinarse"),
            Triple("¿Para qué sirve un martillo?", "Para golpear clavos", "Para beber agua"),
            Triple("¿Cómo se usa una bicicleta?", "Pedaleando", "Con una cuchara"),
            Triple("¿Para qué sirve una llave?", "Para abrir una cerradura", "Para leer"),
            Triple("¿Qué hacemos con una cuchara?", "Tomar alimentos", "Coser ropa")
        )
        val item = examples[number % examples.size]
        return choice(item.first, item.second, listOf(item.third, "No se utiliza"), level)
    }

    private fun choice(text: String, correct: String, wrong: List<String>, level: Int): Triple<String, List<String>, String> {
        val count = when (level) { 1, 2 -> 2; 3 -> 3; else -> 4 }
        return Triple(text, (listOf(correct) + wrong.filter { it != correct }).take(count).shuffled(), correct)
    }
}
