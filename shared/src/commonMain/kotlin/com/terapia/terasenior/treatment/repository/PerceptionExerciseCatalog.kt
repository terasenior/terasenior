package com.terapia.terasenior.treatment.repository

/**
 * Banco de 500 actividades de percepción para personas mayores.
 *
 * Emplea fotografías de objetos familiares, alto contraste y consignas
 * breves. El nivel escogido en la sesión regula alternativas y complejidad,
 * sin exponer etiquetas clínicas a la persona.
 */
object PerceptionExerciseCatalog {
    data class PerceptionExercise(
        val id: String,
        val name: String,
        val description: String,
        val imageUrl: String
    )

    data class PerceptionQuestion(
        val text: String,
        val options: List<String>,
        val correctAnswer: String,
        val imageUrl: String
    )

    private data class ObjectCard(val name: String, val category: String, val function: String, val color: String, val image: String)
    private val objects = listOf(
        ObjectCard("Manzana", "Fruta", "Comer", "Rojo", RealisticExerciseImageCatalog.apple),
        ObjectCard("Plátano", "Fruta", "Comer", "Amarillo", RealisticExerciseImageCatalog.banana),
        ObjectCard("Perro", "Animal", "Compañía", "Marrón", RealisticExerciseImageCatalog.dog),
        ObjectCard("Gato", "Animal", "Compañía", "Gris", RealisticExerciseImageCatalog.cat),
        ObjectCard("Reloj", "Objeto", "Saber la hora", "Claro", RealisticExerciseImageCatalog.clock),
        ObjectCard("Taza", "Hogar", "Beber", "Blanco", RealisticExerciseImageCatalog.cup),
        ObjectCard("Silla", "Hogar", "Sentarse", "Marrón", RealisticExerciseImageCatalog.chair),
        ObjectCard("Teléfono", "Objeto", "Llamar", "Negro", RealisticExerciseImageCatalog.phone),
        ObjectCard("Libro", "Objeto", "Leer", "Azul", RealisticExerciseImageCatalog.book),
        ObjectCard("Autobús", "Transporte", "Viajar", "Azul", RealisticExerciseImageCatalog.bus)
    )

    private enum class Family(val title: String, val description: String) {
        IDENTIFY("Reconocer objetos", "Identifica un objeto cotidiano en una fotografía."),
        CATEGORY("Clasificar objetos", "Reconoce a qué grupo pertenece lo que ve."),
        FUNCTION("Uso de objetos", "Relaciona un objeto con su uso cotidiano."),
        COLOR("Color predominante", "Identifica el color más visible del objeto."),
        SHAPE("Forma general", "Distingue la forma más parecida al objeto."),
        DETAIL("Detalle visual", "Observa un detalle sencillo de la fotografía."),
        POSITION("Orientación espacial", "Responde a una pregunta sencilla sobre posición."),
        BODY("Percepción corporal", "Reconoce una parte del cuerpo o una acción segura."),
        DIFFERENCE("Discriminación visual", "Distingue una característica importante."),
        CONTEXT("Contexto cotidiano", "Relaciona una imagen con una situación diaria.")
    }

    val items: List<PerceptionExercise> = Family.entries.flatMap { family ->
        (1..50).map { number ->
            val item = objects[(number - 1) % objects.size]
            PerceptionExercise(
                id = "guided_perception_${family.name.lowercase()}_$number",
                name = spokenTitle(family),
                description = family.description,
                imageUrl = if (family == Family.BODY) RealisticExerciseImageCatalog.hand else item.image
            )
        }
    }

    init {
        check(items.size == 500) { "El catálogo de percepción debe contener 500 actividades." }
        check(items.map { it.id }.distinct().size == items.size) { "Cada actividad debe tener un identificador único." }
        check(items.mapNotNull { question(it.id, 3)?.text }.distinct().size == items.size) {
            "Cada actividad de percepción debe tener una consigna distinta."
        }
    }

    fun contains(id: String): Boolean = id.startsWith("guided_perception_") && items.any { it.id == id }
    fun forCategory(category: String): List<PerceptionExercise> = if (category == "Percepción") items else emptyList()

    private fun spokenTitle(family: Family): String = when (family) {
        Family.IDENTIFY -> "Reconoce el objeto de la imagen"
        Family.CATEGORY -> "Clasifica el objeto"
        Family.FUNCTION -> "Para qué sirve el objeto"
        Family.COLOR -> "Identifica el color"
        Family.SHAPE -> "Reconoce la forma"
        Family.DETAIL -> "Observa el detalle"
        Family.POSITION -> "Indica dónde está"
        Family.BODY -> "Reconoce una parte del cuerpo"
        Family.DIFFERENCE -> "Encuentra la diferencia"
        Family.CONTEXT -> "Relaciona la imagen con su situación"
    }

    fun question(id: String, challengeLevel: Int): PerceptionQuestion? {
        val activity = items.firstOrNull { it.id == id } ?: return null
        val parts = id.removePrefix("guided_perception_").split("_")
        val family = Family.entries.firstOrNull { it.name.lowercase() == parts.firstOrNull() } ?: return null
        val number = parts.lastOrNull()?.toIntOrNull() ?: return null
        val item = objects[(number - 1) % objects.size]
        val level = challengeLevel.coerceIn(1, 5)
        val triple = when (family) {
            Family.IDENTIFY -> choice("¿Qué objeto aparece en la imagen?", item.name, objects.filter { it != item }.map { it.name }, level)
            Family.CATEGORY -> choice("¿A qué grupo pertenece este objeto?", item.category, listOf("Animal", "Fruta", "Hogar", "Transporte", "Objeto").filter { it != item.category }, level)
            Family.FUNCTION -> choice("¿Para qué se usa principalmente?", item.function, objects.filter { it.function != item.function }.map { it.function }, level)
            Family.COLOR -> choice("¿Qué color predomina en el objeto?", item.color, listOf("Rojo", "Amarillo", "Azul", "Marrón", "Negro", "Blanco", "Gris").filter { it != item.color }, level)
            Family.SHAPE -> shape(item, level)
            Family.DETAIL -> detail(item, level)
            Family.POSITION -> position(number, level)
            Family.BODY -> body(number, level)
            Family.DIFFERENCE -> difference(item, level)
            Family.CONTEXT -> context(item, level)
        }
        return PerceptionQuestion(distinctCatalogPrompt(triple.first, number), triple.second, triple.third, activity.imageUrl)
    }

    private fun shape(item: ObjectCard, level: Int): Triple<String, List<String>, String> {
        val shape = when (item.name) { "Manzana", "Reloj", "Taza" -> "Redonda"; "Libro", "Teléfono" -> "Rectangular"; "Plátano" -> "Curvada"; else -> "Alargada" }
        return choice("¿Qué forma se parece más a la del objeto?", shape, listOf("Triangular", "Cuadrada", "Ondulada", "Estrellada").filter { it != shape }, level)
    }

    private fun detail(item: ObjectCard, level: Int): Triple<String, List<String>, String> {
        val answer = when (item.name) { "Reloj" -> "Tiene números"; "Libro" -> "Tiene páginas"; "Teléfono" -> "Sirve para llamar"; "Taza" -> "Tiene asa"; "Autobús" -> "Tiene ruedas"; else -> "Es un objeto conocido" }
        return choice("Observa con atención. ¿Qué detalle es correcto?", answer, listOf("No se reconoce", "No tiene forma", "No se puede usar"), level)
    }

    private fun position(number: Int, level: Int): Triple<String, List<String>, String> {
        val examples = listOf(
            Triple("Si el reloj está encima de la mesa, ¿dónde está el reloj?", "Encima", "Debajo"),
            Triple("Si la taza está dentro del armario, ¿dónde está la taza?", "Dentro", "Fuera"),
            Triple("Si el autobús está delante de la parada, ¿dónde está?", "Delante", "Detrás"),
            Triple("Si el libro está a la derecha de la silla, ¿dónde está el libro?", "A la derecha", "A la izquierda"),
            Triple("Si el teléfono está junto al reloj, ¿están cerca?", "Sí", "No")
        )[number % 5]
        return choice(examples.first, examples.second, listOf(examples.third, "No se sabe", "Lejos"), level)
    }

    private fun body(number: Int, level: Int): Triple<String, List<String>, String> {
        val examples = listOf(
            Triple("Mira la mano. ¿Cuántos dedos tiene normalmente una mano?", "5", "4"),
            Triple("¿Con qué parte del cuerpo sujetamos una taza?", "Mano", "Rodilla"),
            Triple("¿Con qué parte del cuerpo vemos un reloj?", "Ojos", "Codos"),
            Triple("¿Con qué parte del cuerpo escuchamos el teléfono?", "Orejas", "Pies"),
            Triple("¿Qué parte doblamos para sentarnos?", "Rodillas", "Nariz")
        )[number % 5]
        return choice(examples.first, examples.second, listOf(examples.third, "Hombro", "Tobillo"), level)
    }

    private fun difference(item: ObjectCard, level: Int): Triple<String, List<String>, String> {
        val answer = when (item.category) { "Animal" -> "Es un ser vivo"; "Fruta" -> "Se puede comer"; "Transporte" -> "Sirve para desplazarse"; else -> "Es un objeto" }
        return choice("¿Qué característica distingue mejor a lo que ves?", answer, listOf("Es una prenda", "Es una vivienda", "Es una estación"), level)
    }

    private fun context(item: ObjectCard, level: Int): Triple<String, List<String>, String> {
        val place = when (item.name) { "Autobús" -> "En una parada"; "Taza" -> "En la cocina"; "Libro" -> "En una estantería"; "Reloj" -> "En una pared o mesa"; "Perro", "Gato" -> "En casa o en la calle"; else -> "En la vida diaria" }
        return choice("¿Dónde es habitual encontrar este objeto?", place, listOf("En el mar", "En la luna", "Dentro de una nube"), level)
    }

    private fun choice(text: String, correct: String, wrong: List<String>, level: Int): Triple<String, List<String>, String> {
        val count = when (level) { 1, 2 -> 2; 3 -> 3; else -> 4 }
        return Triple(text, (listOf(correct) + wrong.filter { it != correct }).take(count).shuffled(), correct)
    }
}
