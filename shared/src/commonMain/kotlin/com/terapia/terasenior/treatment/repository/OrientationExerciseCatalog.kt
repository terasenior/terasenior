package com.terapia.terasenior.treatment.repository

/**
 * Banco de 500 actividades de orientación para personas mayores.
 * Las preguntas se basan en referencias temporales, espaciales y situacionales
 * estables; el nivel regula alternativas y complejidad sin mostrar etiquetas.
 */
object OrientationExerciseCatalog {
    data class OrientationExercise(val id: String, val name: String, val description: String, val imageUrl: String)
    data class OrientationQuestion(val text: String, val options: List<String>, val correctAnswer: String, val imageUrl: String?)

    private enum class Family(val title: String, val description: String, val image: String) {
        WEEK("Días de la semana", "Ordena y reconoce los días de la semana.", RealisticExerciseImageCatalog.weekCalendar),
        MONTH("Meses del año", "Reconoce el orden de los meses.", RealisticExerciseImageCatalog.yearCalendar),
        SEASON("Estaciones", "Relaciona el tiempo con una estación.", RealisticExerciseImageCatalog.seasonsLandscape),
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
        check(items.mapNotNull { question(it.id, 3)?.text }.distinct().size == items.size) {
            "Cada actividad de orientación debe tener una consigna distinta."
        }
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
        val familyKey = id.removePrefix("guided_orientation_").substringBeforeLast("_")
        val family = Family.entries.firstOrNull { it.name.lowercase() == familyKey } ?: return null
        val number = id.substringAfterLast("_").toIntOrNull() ?: return null
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
        return OrientationQuestion(distinctCatalogPrompt(triple.first, number), triple.second, triple.third, imageFor(family, number))
    }

    private fun imageFor(family: Family, number: Int): String? = when (family) {
        // Un calendario estático no puede indicar correctamente todos los días.
        Family.WEEK -> null
        Family.MONTH -> RealisticExerciseImageCatalog.yearCalendar
        Family.DAYTIME -> daytimeScenario(number).imageUrl
        Family.ROUTINE -> RealisticExerciseImageCatalog.dailyCalendar
        Family.SEASON, Family.WEATHER -> RealisticExerciseImageCatalog.seasonsLandscape
        Family.HOME, Family.COMMUNITY -> RealisticExerciseImageCatalog.homeCommunity
        Family.TIME_USE -> if (number % 5 == 0) RealisticExerciseImageCatalog.clock else RealisticExerciseImageCatalog.dailyCalendar
        Family.SITUATION -> when (number % 5) {
            2 -> RealisticExerciseImageCatalog.phone
            3 -> RealisticExerciseImageCatalog.bus
            4 -> RealisticExerciseImageCatalog.dailyCalendar
            else -> RealisticExerciseImageCatalog.homeCommunity
        }
    }

    private fun week(number: Int, level: Int): Triple<String, List<String>, String> {
        val days = listOf("Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado", "Domingo")
        val current = number % days.size
        val previous = days[(current + days.size - 1) % days.size]
        val next = days[(current + 1) % days.size]
        val twoBefore = days[(current + days.size - 2) % days.size]
        val twoAfter = days[(current + 2) % days.size]
        val variant = (number - 1) / days.size
        val prompt = when (variant) {
            0 -> Triple("Después de ${days[current]}, ¿qué día viene?", next, days.filter { it != next })
            1 -> Triple("Antes de ${days[current]}, ¿qué día fue?", previous, days.filter { it != previous })
            2 -> Triple("Dos días después de ${days[current]}, ¿qué día será?", twoAfter, days.filter { it != twoAfter })
            3 -> Triple("Dos días antes de ${days[current]}, ¿qué día fue?", twoBefore, days.filter { it != twoBefore })
            4 -> Triple("¿Qué día está entre $previous y $next?", days[current], days.filter { it != days[current] })
            5 -> Triple("Si ayer fue $previous, ¿qué día es hoy?", days[current], days.filter { it != days[current] })
            6 -> Triple("Si mañana es $next, ¿qué día es hoy?", days[current], days.filter { it != days[current] })
            else -> Triple("¿Qué día va justo antes de $next?", days[current], days.filter { it != days[current] })
        }
        return choice(prompt.first, prompt.second, prompt.third, level)
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

    private data class DaytimeScenario(val text: String, val answer: String, val imageUrl: String)

    private val daytimeScenarios = listOf(
        DaytimeScenario("Solemos desayunar por la…", "Mañana", RealisticExerciseImageCatalog.dailyCalendar),
        DaytimeScenario("Cuando abrimos las persianas y entra luz, suele ser la…", "Mañana", RealisticExerciseImageCatalog.sunrise),
        DaytimeScenario("Si vemos salir el sol, comienza la…", "Mañana", RealisticExerciseImageCatalog.sunrise),
        DaytimeScenario("Antes de ir a una cita temprano, es la…", "Mañana", RealisticExerciseImageCatalog.dailyCalendar),
        DaytimeScenario("Al levantarnos de la cama normalmente es la…", "Mañana", RealisticExerciseImageCatalog.sunrise),
        DaytimeScenario("Tomar café y tostada suele hacerse por la…", "Mañana", RealisticExerciseImageCatalog.dailyCalendar),
        DaytimeScenario("Cuando el reloj marca las ocho y hay luz, es la…", "Mañana", RealisticExerciseImageCatalog.sunrise),
        DaytimeScenario("Lavarse y vestirse para empezar el día se hace por la…", "Mañana", RealisticExerciseImageCatalog.dailyCalendar),
        DaytimeScenario("Un paseo temprano, antes de comer, suele ser por la…", "Mañana", RealisticExerciseImageCatalog.sunrise),
        DaytimeScenario("La primera comida del día se toma por la…", "Mañana", RealisticExerciseImageCatalog.dailyCalendar),
        DaytimeScenario("Cuando la calle empieza a tener movimiento, suele ser la…", "Mañana", RealisticExerciseImageCatalog.sunrise),
        DaytimeScenario("Preparar el desayuno corresponde a la…", "Mañana", RealisticExerciseImageCatalog.dailyCalendar),
        DaytimeScenario("Si la luz del sol entra poco a poco por la ventana, es la…", "Mañana", RealisticExerciseImageCatalog.sunrise),
        DaytimeScenario("Cuando empieza a clarear justo al despertar, corresponde a la…", "Mañana", RealisticExerciseImageCatalog.sunrise),
        DaytimeScenario("Después de comer suele ser la…", "Tarde", RealisticExerciseImageCatalog.afterLunch),
        DaytimeScenario("La merienda se toma habitualmente por la…", "Tarde", RealisticExerciseImageCatalog.afterLunch),
        DaytimeScenario("Cuando terminamos la comida del mediodía, empieza la…", "Tarde", RealisticExerciseImageCatalog.afterLunch),
        DaytimeScenario("Una siesta corta se hace normalmente por la…", "Tarde", RealisticExerciseImageCatalog.afterLunch),
        DaytimeScenario("Ir a hacer una compra después de comer suele ser por la…", "Tarde", RealisticExerciseImageCatalog.afterLunch),
        DaytimeScenario("Si el reloj marca las cinco, es la…", "Tarde", RealisticExerciseImageCatalog.afterLunch),
        DaytimeScenario("Visitar a un amigo después de la comida suele ser por la…", "Tarde", RealisticExerciseImageCatalog.afterLunch),
        DaytimeScenario("Regar las plantas cuando el sol aún está alto suele ser por la…", "Tarde", RealisticExerciseImageCatalog.afterLunch),
        DaytimeScenario("¿Cuándo solemos tomar una merienda de fruta o una infusión?", "Tarde", RealisticExerciseImageCatalog.afterLunch),
        DaytimeScenario("Cuando recogemos la mesa de la comida, estamos en la…", "Tarde", RealisticExerciseImageCatalog.afterLunch),
        DaytimeScenario("Si todavía hay mucha luz pero ya hemos comido, es la…", "Tarde", RealisticExerciseImageCatalog.afterLunch),
        DaytimeScenario("Un paseo tranquilo antes de cenar suele ser por la…", "Tarde", RealisticExerciseImageCatalog.afterLunch),
        DaytimeScenario("Las actividades del centro después de comer son por la…", "Tarde", RealisticExerciseImageCatalog.afterLunch),
        DaytimeScenario("Solemos cenar por la…", "Noche", RealisticExerciseImageCatalog.dinnerNight),
        DaytimeScenario("Si fuera está oscuro y encendemos la lámpara, es de…", "Noche", RealisticExerciseImageCatalog.dinnerNight),
        DaytimeScenario("Ponerse el pijama suele hacerse por la…", "Noche", RealisticExerciseImageCatalog.dinnerNight),
        DaytimeScenario("Cepillarse los dientes antes de dormir se hace por la…", "Noche", RealisticExerciseImageCatalog.dinnerNight),
        DaytimeScenario("Ver la luna por la ventana indica que es de…", "Noche", RealisticExerciseImageCatalog.dinnerNight),
        DaytimeScenario("Cerrar las persianas antes de acostarse corresponde a la…", "Noche", RealisticExerciseImageCatalog.dinnerNight),
        DaytimeScenario("Ver un programa después de cenar suele ser por la…", "Noche", RealisticExerciseImageCatalog.dinnerNight),
        DaytimeScenario("Cuando preparamos la cama para dormir, es de…", "Noche", RealisticExerciseImageCatalog.dinnerNight),
        DaytimeScenario("Si el reloj marca las diez y el cielo está oscuro, es la…", "Noche", RealisticExerciseImageCatalog.dinnerNight),
        DaytimeScenario("La cena en casa se toma normalmente por la…", "Noche", RealisticExerciseImageCatalog.dinnerNight),
        DaytimeScenario("Encender una luz porque ya no hay sol es propio de la…", "Noche", RealisticExerciseImageCatalog.dinnerNight),
        DaytimeScenario("Ir a dormir suele hacerse durante la…", "Noche", RealisticExerciseImageCatalog.dinnerNight),
        DaytimeScenario("Si son las dos de la mañana, es la…", "Madrugada", RealisticExerciseImageCatalog.earlyMorning),
        DaytimeScenario("Cuando casi todos duermen y aún está oscuro, es la…", "Madrugada", RealisticExerciseImageCatalog.earlyMorning),
        DaytimeScenario("Si el reloj marca las cuatro y no ha salido el sol, es la…", "Madrugada", RealisticExerciseImageCatalog.earlyMorning),
        DaytimeScenario("La parte de la noche justo antes del amanecer es la…", "Madrugada", RealisticExerciseImageCatalog.earlyMorning),
        DaytimeScenario("Mirar la luna a las tres de la mañana corresponde a la…", "Madrugada", RealisticExerciseImageCatalog.earlyMorning),
        DaytimeScenario("Si la calle está vacía y es muy temprano, puede ser la…", "Madrugada", RealisticExerciseImageCatalog.earlyMorning),
        DaytimeScenario("Cuando suena una alarma a las cinco y todavía está oscuro, es la…", "Madrugada", RealisticExerciseImageCatalog.earlyMorning),
        DaytimeScenario("Antes de que salga el sol, durante la noche, es la…", "Madrugada", RealisticExerciseImageCatalog.earlyMorning),
        DaytimeScenario("Si nos despertamos de noche y el reloj marca las tres, es la…", "Madrugada", RealisticExerciseImageCatalog.earlyMorning),
        DaytimeScenario("La hora silenciosa entre la noche y la mañana se llama…", "Madrugada", RealisticExerciseImageCatalog.earlyMorning),
        DaytimeScenario("Cuando aún no hay luz y falta poco para amanecer, es la…", "Madrugada", RealisticExerciseImageCatalog.earlyMorning)
    )

    private fun daytimeScenario(number: Int): DaytimeScenario =
        daytimeScenarios[(number - 1).coerceAtLeast(0) % daytimeScenarios.size]

    private fun daytime(number: Int, level: Int): Triple<String, List<String>, String> {
        val scenario = daytimeScenario(number)
        return choice(
            scenario.text,
            scenario.answer,
            listOf("Mañana", "Tarde", "Noche", "Madrugada").filter { it != scenario.answer },
            level
        )
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
