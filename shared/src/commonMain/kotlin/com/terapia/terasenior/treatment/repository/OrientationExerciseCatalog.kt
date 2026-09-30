package com.terapia.terasenior.treatment.repository

/**
 * Banco de 500 actividades de orientación para personas mayores.
 * Las preguntas se basan en referencias temporales, espaciales y situacionales
 * estables; el nivel regula alternativas y complejidad sin mostrar etiquetas.
 */
object OrientationExerciseCatalog {
    data class OrientationExercise(val id: String, val name: String, val description: String, val imageUrl: String)
    data class OrientationQuestion(val text: String, val options: List<String>, val correctAnswer: String, val imageUrl: String)

    private enum class Family(val title: String, val description: String, val image: String) {
        WEEK("Días de la semana", "Ordena y reconoce los días de la semana.", RealisticExerciseImageCatalog.weekCalendar),
        MONTH("Meses del año", "Reconoce el orden de los meses.", RealisticExerciseImageCatalog.dailyCalendar),
        SEASON("Estaciones", "Relaciona el tiempo con una estación.", RealisticExerciseImageCatalog.seasons),
        DAYTIME("Momento del día", "Distingue mañana, tarde y noche.", RealisticExerciseImageCatalog.dailyCalendar),
        HOME("Orientación en casa", "Identifica espacios y objetos cotidianos.", RealisticExerciseImageCatalog.homeCommunity),
        COMMUNITY("Orientación comunitaria", "Reconoce lugares y servicios del entorno.", RealisticExerciseImageCatalog.homeCommunity),
        TIME_USE("Uso del tiempo", "Relaciona objetos con información temporal.", RealisticExerciseImageCatalog.dailyCalendar),
        WEATHER("Tiempo y preparación", "Elige una acción adecuada según el tiempo.", RealisticExerciseImageCatalog.seasons),
        ROUTINE("Rutinas diarias", "Ordena acciones habituales del día.", RealisticExerciseImageCatalog.dailyCalendar),
        SITUATION("Situaciones cotidianas", "Reconoce la referencia adecuada en una situación.", RealisticExerciseImageCatalog.homeCommunity)
    }

    val items: List<OrientationExercise> = Family.entries.flatMap { family ->
        (1..50).map { number ->
            OrientationExercise(
                "guided_orientation_${family.name.lowercase()}_$number",
                titleFor(family, number),
                family.description,
                family.image
            )
        }
    }

    init {
        check(items.size == 500) { "El catálogo de orientación debe contener 500 actividades." }
        check(items.map { it.id }.distinct().size == items.size) { "Cada actividad debe tener un identificador único." }
    }

    fun contains(id: String): Boolean = id.startsWith("guided_orientation_") && items.any { it.id == id }
    fun forCategory(category: String): List<OrientationExercise> = if (category == "Orientación") items else emptyList()

    /** Título que escucha la persona antes de empezar; nunca expone el número técnico del catálogo. */
    private fun titleFor(family: Family, number: Int): String = when (family) {
        Family.WEEK -> week(number, 1).first
        Family.MONTH -> month(number, 1).first
        Family.SEASON -> season(number, 1).first
        Family.DAYTIME -> daytime(number, 1).first
        Family.HOME -> home(number, 1).first
        Family.COMMUNITY -> community(number, 1).first
        Family.TIME_USE -> timeUse(number, 1).first
        Family.WEATHER -> weather(number, 1).first
        Family.ROUTINE -> routine(number, 1).first
        Family.SITUATION -> situation(number, 1).first
    }

    fun question(id: String, challengeLevel: Int): OrientationQuestion? {
        val activity = items.firstOrNull { it.id == id } ?: return null
        val parts = id.removePrefix("guided_orientation_").split("_")
        val family = Family.entries.firstOrNull { it.name.lowercase() == parts.firstOrNull() } ?: return null
        val number = parts.lastOrNull()?.toIntOrNull() ?: return null
        val level = challengeLevel.coerceIn(1, 5)
        val triple = when (family) {
            Family.WEEK -> week(number, level)
            Family.MONTH -> month(number, level)
            Family.SEASON -> season(number, level)
            Family.DAYTIME -> daytime(number, level)
            Family.HOME -> home(number, level)
            Family.COMMUNITY -> community(number, level)
            Family.TIME_USE -> timeUse(number, level)
            Family.WEATHER -> weather(number, level)
            Family.ROUTINE -> routine(number, level)
            Family.SITUATION -> situation(number, level)
        }
        return OrientationQuestion(triple.first, triple.second, triple.third, activity.imageUrl)
    }

    private fun week(number: Int, level: Int): Triple<String, List<String>, String> {
        val days = listOf("Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado", "Domingo")
        val current = number % days.size
        val next = days[(current + 1) % days.size]
        return choice("Después de ${days[current]}, ¿qué día viene?", next, days.filter { it != next }, level)
    }

    private fun month(number: Int, level: Int): Triple<String, List<String>, String> {
        val months = listOf("Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio", "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre")
        val current = number % months.size
        val next = months[(current + 1) % months.size]
        return choice("Después de ${months[current]}, ¿qué mes viene?", next, months.filter { it != next }, level)
    }

    private fun season(number: Int, level: Int): Triple<String, List<String>, String> {
        val examples = listOf(
            Triple("Cuando hace mucho calor y los días son largos, ¿qué estación suele ser?", "Verano", "Invierno"),
            Triple("Cuando caen muchas hojas, ¿qué estación suele ser?", "Otoño", "Primavera"),
            Triple("Cuando hace frío y usamos abrigo, ¿qué estación suele ser?", "Invierno", "Verano"),
            Triple("Cuando empiezan a salir flores, ¿qué estación suele ser?", "Primavera", "Otoño"))
        val item = examples[number % examples.size]
        return choice(item.first, item.second, listOf(item.third, "Noche", "Mañana"), level)
    }

    private fun daytime(number: Int, level: Int): Triple<String, List<String>, String> {
        val examples = listOf(
            Triple("Solemos desayunar por la…", "Mañana", "Noche"),
            Triple("Solemos cenar por la…", "Noche", "Mañana"),
            Triple("Después de comer suele ser la…", "Tarde", "Madrugada"),
            Triple("Cuando sale el sol comienza la…", "Mañana", "Noche"))
        val item = examples[number % examples.size]
        return choice(item.first, item.second, listOf(item.third, "Invierno", "Domingo"), level)
    }

    private fun home(number: Int, level: Int): Triple<String, List<String>, String> {
        val examples = listOf(
            Triple("¿En qué habitación solemos encontrar una cama?", "Dormitorio", "Cocina"),
            Triple("¿En qué habitación solemos cocinar?", "Cocina", "Baño"),
            Triple("¿En qué habitación solemos ducharnos?", "Baño", "Salón"),
            Triple("¿Dónde solemos sentarnos a descansar?", "Salón", "Garaje"),
            Triple("¿Dónde guardamos alimentos fríos?", "Nevera", "Armario de ropa"))
        val item = examples[number % examples.size]
        return choice(item.first, item.second, listOf(item.third, "Tejado", "Escalera"), level)
    }

    private fun community(number: Int, level: Int): Triple<String, List<String>, String> {
        val examples = listOf(
            Triple("¿Dónde esperamos normalmente el autobús?", "En la parada", "En el tejado"),
            Triple("¿Dónde compramos pan?", "En la panadería", "En la farmacia"),
            Triple("¿Dónde vamos si necesitamos atención médica?", "Al centro de salud", "Al cine"),
            Triple("¿Dónde se prestan libros?", "En la biblioteca", "En el mercado"),
            Triple("¿Dónde compramos fruta?", "En la frutería", "En el taller"))
        val item = examples[number % examples.size]
        return choice(item.first, item.second, listOf(item.third, "En la luna", "En el mar"), level)
    }

    private fun timeUse(number: Int, level: Int): Triple<String, List<String>, String> {
        val examples = listOf(
            Triple("¿Qué objeto usamos para saber la hora?", "Reloj", "Taza"),
            Triple("¿Qué objeto usamos para saber una fecha?", "Calendario", "Almohada"),
            Triple("¿Qué miramos antes de una cita?", "La hora", "El techo"),
            Triple("¿Qué anotamos para no olvidar una visita?", "El día y la hora", "El color de la pared"),
            Triple("¿Qué indica un reloj?", "La hora", "La estación"))
        val item = examples[number % examples.size]
        return choice(item.first, item.second, listOf(item.third, "El peso", "El sabor"), level)
    }

    private fun weather(number: Int, level: Int): Triple<String, List<String>, String> {
        val examples = listOf(
            Triple("Si llueve, ¿qué es útil llevar?", "Paraguas", "Gafas de sol"),
            Triple("Si hace frío, ¿qué es útil ponerse?", "Abrigo", "Bañador"),
            Triple("Si hay mucho sol, ¿qué conviene hacer?", "Protegerse", "No beber agua"),
            Triple("Si hay viento fuerte, ¿qué conviene comprobar?", "La ropa adecuada", "El color del sofá"),
            Triple("Si hace calor, ¿qué conviene beber?", "Agua", "Nada"))
        val item = examples[number % examples.size]
        return choice(item.first, item.second, listOf(item.third, "Salir sin mirar", "Dormir de pie"), level)
    }

    private fun routine(number: Int, level: Int): Triple<String, List<String>, String> {
        val examples = listOf(
            Triple("Después de levantarnos, ¿qué hacemos normalmente antes de salir?", "Asearnos", "Acostarnos"),
            Triple("Antes de dormir, ¿qué hacemos normalmente?", "Prepararnos para descansar", "Desayunar"),
            Triple("Antes de comer, ¿qué es habitual hacer?", "Lavarse las manos", "Ponerse el abrigo"),
            Triple("Después de usar una taza, ¿qué conviene hacer?", "Limpiarla", "Esconderla"),
            Triple("Antes de salir de casa, ¿qué conviene comprobar?", "Las llaves", "El color del techo"))
        val item = examples[number % examples.size]
        return choice(item.first, item.second, listOf(item.third, "No hacer nada", "Cambiar de día"), level)
    }

    private fun situation(number: Int, level: Int): Triple<String, List<String>, String> {
        val examples = listOf(
            Triple("Si tiene una cita mañana, ¿cuándo debe prepararse?", "Antes de la cita", "Después de que pase"),
            Triple("Si no sabe dónde está, ¿qué puede hacer?", "Preguntar dónde se encuentra", "Caminar sin rumbo"),
            Triple("Si necesita llamar a alguien, ¿qué objeto busca?", "Teléfono", "Reloj"),
            Triple("Si quiere ir en autobús, ¿qué debe comprobar?", "La línea", "La estación del año"),
            Triple("Si recibe una carta con una fecha, ¿qué debe mirar?", "El día indicado", "El color del sobre"))
        val item = examples[number % examples.size]
        return choice(item.first, item.second, listOf(item.third, "No importa", "Nada relacionado"), level)
    }

    private fun choice(text: String, correct: String, wrong: List<String>, level: Int): Triple<String, List<String>, String> {
        val count = when (level) { 1, 2 -> 2; 3 -> 3; else -> 4 }
        return Triple(text, (listOf(correct) + wrong.filter { it != correct }).take(count).shuffled(), correct)
    }
}
