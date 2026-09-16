package com.terapia.terasenior.treatment.repository

/**
 * Banco de 500 ejercicios de funciones ejecutivas.
 *
 * Las actividades se presentan sin una etiqueta GDS. Cada actividad adapta
 * automáticamente su longitud, vocabulario y número de alternativas al nivel
 * elegido para la sesión: GDS 3 supone mayor carga y GDS 5 una consigna más
 * breve con menos alternativas.
 */
object TherapeuticExerciseCatalog {
    data class GuidedExercise(
        val id: String,
        val name: String,
        val category: String,
        val description: String
    )

    data class GuidedQuestion(
        val text: String,
        val options: List<String>,
        val correctAnswer: String
    )

    private enum class Family(val title: String, val description: String) {
        COUNTDOWN("Cuenta atrás", "Cuenta hacia atrás siguiendo una pauta."),
        DECISIONS("Decisiones cotidianas", "Elige una solución sencilla para una situación diaria."),
        EMOTIONS("Adivina la emoción", "Reconoce la emoción más probable en una situación."),
        RHYTHMS("Ritmos y secuencias", "Completa una secuencia de palabras sencillas."),
        CALCULATION("Cálculo con atención", "Resuelve un cálculo breve sin perder la consigna."),
        REVERSE("Series al revés", "Recuerda una serie y reconoce el orden inverso."),
        FLEXIBILITY("Frases alternativas", "Elige una continuación útil y con sentido."),
        STORIES("Historias encadenadas", "Escoge el siguiente paso lógico de una historia."),
        LOGIC("Problemas sencillos", "Resuelve una relación o regla clara."),
        PLANNING("Planifica la tarea", "Selecciona el primer paso de una tarea cotidiana.")
    }

    private val dailyThemes = listOf(
        "la compra", "el paseo", "el desayuno", "una visita", "la colada",
        "una llamada", "el calendario", "las plantas", "una receta", "el autobús"
    )

    val items: List<GuidedExercise> = Family.entries.flatMap { family ->
        (1..50).map { number ->
            GuidedExercise(
                id = "guided_executive_${family.name.lowercase()}_$number",
                name = "${family.title} ${number.toString().padStart(2, '0')}",
                category = "Funciones Ejecutivas",
                description = family.description
            )
        }
    }

    init {
        check(items.size == 500) { "El catálogo debe contener exactamente 500 actividades." }
        check(items.map { it.id }.distinct().size == items.size) { "Cada actividad debe tener un identificador único." }
    }

    fun find(id: String): GuidedExercise? = items.firstOrNull { it.id == id }

    fun contains(id: String): Boolean = id.startsWith("guided_executive_") && find(id) != null

    fun forCategory(category: String): List<GuidedExercise> = items.filter { it.category == category }

    /**
     * [challengeLevel] va de 1 (GDS 5, menor carga) a 5 (GDS 3, mayor carga).
     */
    fun question(id: String, challengeLevel: Int): GuidedQuestion? {
        val activity = find(id) ?: return null
        val parts = activity.id.removePrefix("guided_executive_").split("_")
        val family = Family.entries.firstOrNull { it.name.lowercase() == parts.firstOrNull() } ?: return null
        val number = parts.lastOrNull()?.toIntOrNull() ?: return null
        val level = challengeLevel.coerceIn(1, 5)
        return when (family) {
            Family.COUNTDOWN -> countdown(number, level)
            Family.DECISIONS -> decision(number, level)
            Family.EMOTIONS -> emotion(number, level)
            Family.RHYTHMS -> rhythm(number, level)
            Family.CALCULATION -> calculation(number, level)
            Family.REVERSE -> reverseSeries(number, level)
            Family.FLEXIBILITY -> flexibility(number, level)
            Family.STORIES -> story(number, level)
            Family.LOGIC -> logic(number, level)
            Family.PLANNING -> planning(number, level)
        }
    }

    private fun countdown(number: Int, level: Int): GuidedQuestion {
        val step = if (level >= 4) 3 else 2
        val start = 12 + (number % 12) * step
        val first = start - step
        val correct = (first - step).toString()
        return question(
            text = "Completa la cuenta atrás: $start, $first, __.",
            correct = correct,
            wrong = listOf((first + step).toString(), (correct.toInt() + 1).toString(), (correct.toInt() - 1).toString()),
            level = level
        )
    }

    private fun decision(number: Int, level: Int): GuidedQuestion {
        val cases = listOf(
            Triple("Hace frío al salir", "Ponerse un abrigo", "Salir sin abrigo"),
            Triple("No recuerda una cita", "Mirar el calendario", "Esperar sin comprobar"),
            Triple("Quiere comprar pan", "Hacer una lista corta", "Ir sin saber qué necesita"),
            Triple("Tiene una duda con el autobús", "Preguntar al conductor", "Subir sin comprobar"),
            Triple("Hay agua en el suelo", "Secarla o pedir ayuda", "Caminar deprisa encima"),
            Triple("No encuentra las llaves", "Buscar en el lugar habitual", "Salir sin cerrar"),
            Triple("Suena el teléfono", "Contestar con calma", "Tirarlo a un lado"),
            Triple("Empieza a llover", "Usar paraguas", "Seguir sin protección"),
            Triple("La cocina está encendida", "Apagarla antes de salir", "Dejarla encendida"),
            Triple("Está cansado durante el paseo", "Descansar o volver acompañado", "Seguir aunque se encuentre mal")
        )
        val item = cases[number % cases.size]
        return question("${item.first}. ¿Qué puede hacer?", item.second, listOf(item.third, "Hacer otra cosa sin relación", "No pensar en ello"), level)
    }

    private fun emotion(number: Int, level: Int): GuidedQuestion {
        val cases = listOf(
            Triple("María recibe la visita de su nieta.", "Alegría", "Enfado"),
            Triple("Luis pierde su cartera.", "Preocupación", "Diversión"),
            Triple("Carmen escucha una buena noticia.", "Alegría", "Miedo"),
            Triple("Pedro espera una llamada importante.", "Nerviosismo", "Aburrimiento"),
            Triple("Ana no encuentra su camino.", "Preocupación", "Orgullo"),
            Triple("Rosa termina una tarea que le costaba.", "Satisfacción", "Tristeza"),
            Triple("Manuel recibe un regalo sorpresa.", "Sorpresa", "Enfado"),
            Triple("Elena se despide de una amiga.", "Tristeza", "Risa"),
            Triple("Javier oye un ruido fuerte inesperado.", "Susto", "Calma"),
            Triple("Pilar ayuda a un vecino.", "Satisfacción", "Vergüenza")
        )
        val item = cases[number % cases.size]
        return question("${item.first} ¿Cómo puede sentirse?", item.second, listOf(item.third, "Hambre", "Sueño"), level)
    }

    private fun rhythm(number: Int, level: Int): GuidedQuestion {
        val words = listOf("palma", "mesa", "sol", "flor", "pan", "casa", "tren", "mar")
        val first = words[number % words.size]
        val second = words[(number + 1) % words.size]
        val third = words[(number + 2) % words.size]
        val sequence = if (level <= 2) listOf(first, second, first) else listOf(first, second, third, first)
        val correct = if (level <= 2) second else second
        return question(
            text = "Completa la serie: ${sequence.joinToString(", ")}, __.",
            correct = correct,
            wrong = words.filter { it != correct && it !in sequence }.take(3),
            level = level
        )
    }

    private fun calculation(number: Int, level: Int): GuidedQuestion {
        val first = 2 + number % 7
        val second = if (level >= 4) 3 + number % 5 else 1 + number % 4
        val correct = first + second
        return question(
            text = "Recuerda: hoy es un buen día. Ahora calcula: $first + $second = __.",
            correct = correct.toString(),
            wrong = listOf((correct - 1).toString(), (correct + 1).toString(), (correct + 2).toString()),
            level = level
        )
    }

    private fun reverseSeries(number: Int, level: Int): GuidedQuestion {
        val digits = (0 until (level + 1)).map { ((number + it * 2) % 9 + 1).toString() }
        val correct = digits.reversed().joinToString(" - ")
        return question(
            text = "Mira los números: ${digits.joinToString(" - ")}. ¿Cuál es el orden al revés?",
            correct = correct,
            wrong = listOf(
                digits.joinToString(" - "),
                digits.drop(1).plus(digits.first()).joinToString(" - "),
                digits.reversed().drop(1).plus(digits.last()).joinToString(" - ")
            ).distinct(),
            level = level
        )
    }

    private fun flexibility(number: Int, level: Int): GuidedQuestion {
        val cases = listOf(
            Triple("Hace calor, pero…", "puedo beber agua.", "me pongo un abrigo."),
            Triple("Llueve, así que…", "puedo usar paraguas.", "puedo salir sin protección."),
            Triple("Tengo hambre, entonces…", "puedo preparar algo sencillo.", "dejo la comida sin guardar."),
            Triple("No encuentro una dirección, por eso…", "puedo pedir ayuda.", "me enfado y no pregunto."),
            Triple("Estoy cansado, así que…", "puedo descansar.", "debo seguir deprisa."),
            Triple("Tengo una cita, entonces…", "puedo mirar el calendario.", "no necesito comprobar la hora."),
            Triple("Hace frío, por eso…", "puedo ponerme una chaqueta.", "puedo quitarme toda la ropa."),
            Triple("No oigo bien, entonces…", "puedo pedir que repitan.", "debo adivinar sin escuchar."),
            Triple("No recuerdo una palabra, así que…", "puedo tomarme un momento.", "debo dejar de hablar."),
            Triple("La tarea es larga, entonces…", "puedo hacerla paso a paso.", "debo hacerlo todo a la vez.")
        )
        val item = cases[number % cases.size]
        return question(item.first, item.second, listOf(item.third, "no hago nada", "cambio de tema"), level)
    }

    private fun story(number: Int, level: Int): GuidedQuestion {
        val cases = listOf(
            Triple("Marta va a la panadería. Primero entra en la tienda.", "Pide el pan", "Se pone a dormir"),
            Triple("Carlos tiene una cita a las diez. Mira el reloj.", "Se prepara para salir", "Apaga el reloj y espera"),
            Triple("Rosa quiere regar una planta. Coge una regadera.", "Echa agua con cuidado", "Guarda la planta en el armario"),
            Triple("Paco recibe una carta. Ve su nombre escrito.", "Abre la carta con cuidado", "La tira sin mirar"),
            Triple("Elena llega a la parada. Ve venir el autobús.", "Comprueba si es su línea", "Cruza la calle sin mirar"),
            Triple("Luis prepara café. Tiene una taza limpia.", "Sirve el café en la taza", "Guarda la taza bajo la cama"),
            Triple("Ana está en el mercado. Lleva una lista.", "Busca el primer producto", "Compra objetos al azar"),
            Triple("Pedro termina de cocinar. La cocina sigue encendida.", "Apaga la cocina", "Sale sin revisar"),
            Triple("Carmen llama a su amiga. Su amiga no responde.", "Deja un mensaje claro", "Grita al teléfono"),
            Triple("Manuel se prepara para pasear. Mira por la ventana.", "Elige ropa adecuada", "Se pone ropa sin mirar el tiempo")
        )
        val item = cases[number % cases.size]
        return question("${item.first} ¿Qué puede pasar después?", item.second, listOf(item.third, "No hace falta decidir", "Empieza una tarea distinta"), level)
    }

    private fun logic(number: Int, level: Int): GuidedQuestion {
        val cases = listOf(
            Triple("Si todos los lunes hay taller y hoy es lunes, ¿hay taller?", "Sí", "No"),
            Triple("Si una tienda abre a las nueve y son las ocho, ¿está abierta?", "No", "Sí"),
            Triple("Si el paraguas sirve para la lluvia, ¿lo usamos cuando llueve?", "Sí", "No"),
            Triple("Si una semana tiene siete días, ¿dos semanas tienen catorce días?", "Sí", "No"),
            Triple("Si un autobús llega después de las diez y son las nueve, ¿ya ha llegado?", "No", "Sí"),
            Triple("Si una receta dice 'primero lavar', ¿lavamos antes de cocinar?", "Sí", "No"),
            Triple("Si una puerta está cerrada, ¿podemos entrar sin abrirla?", "No", "Sí"),
            Triple("Si una lista tiene tres productos y ya compramos dos, ¿falta uno?", "Sí", "No"),
            Triple("Si es de noche, ¿solemos encender una luz para ver mejor?", "Sí", "No"),
            Triple("Si una taza está vacía, ¿tiene café dentro?", "No", "Sí")
        )
        val item = cases[number % cases.size]
        return question(item.first, item.second, listOf(item.third, "Depende del color", "No lo sé"), level)
    }

    private fun planning(number: Int, level: Int): GuidedQuestion {
        val cases = listOf(
            Triple("Para hacer una lista de la compra", "Pensar qué falta en casa", "Ir a la tienda sin decidir"),
            Triple("Para preparar una tostada", "Reunir pan y plato", "Guardar el plato antes de usarlo"),
            Triple("Para llamar a una persona", "Buscar su número", "Hablar sin marcar"),
            Triple("Para ir a una cita", "Mirar la fecha y la hora", "Salir sin saber dónde ir"),
            Triple("Para regar una planta", "Preparar agua", "Dejar agua en el suelo"),
            Triple("Para lavar ropa", "Separar las prendas", "Mezclar todo sin mirar"),
            Triple("Para salir a pasear", "Ponerse calzado cómodo", "Salir sin llaves"),
            Triple("Para preparar una visita", "Confirmar la hora", "Esperar sin organizar nada"),
            Triple("Para guardar un documento", "Usar una carpeta", "Dejarlo en cualquier sitio"),
            Triple("Para ordenar el día", "Anotar las tareas", "Empezar muchas tareas a la vez")
        )
        val item = cases[number % cases.size]
        return question("${item.first}, ¿qué va primero?", item.second, listOf(item.third, "Hacer algo sin relación", "No comprobar nada"), level)
    }

    private fun question(text: String, correct: String, wrong: List<String>, level: Int): GuidedQuestion {
        val wanted = when (level) {
            1, 2 -> 2
            3 -> 3
            else -> 4
        }
        val options = (listOf(correct) + wrong.filter { it != correct }).take(wanted).shuffled()
        return GuidedQuestion(text = text, options = options, correctAnswer = correct)
    }
}
