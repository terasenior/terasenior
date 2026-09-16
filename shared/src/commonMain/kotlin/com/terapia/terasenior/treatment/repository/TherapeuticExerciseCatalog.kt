package com.terapia.terasenior.treatment.repository

/**
 * 500 ejercicios de funciones ejecutivas para personas mayores.
 *
 * Combina 20 situaciones cotidianas, cinco procesos ejecutivos y cinco
 * adaptaciones GDS. El terapeuta mantiene el control de la selección.
 */
object TherapeuticExerciseCatalog {
    data class GuidedExercise(
        val id: String,
        val name: String,
        val category: String,
        val description: String,
        val gdsLevel: Int,
        val question: String,
        val correctAnswer: String,
        val options: List<String>
    )

    private data class Situation(
        val id: String,
        val name: String,
        val context: String,
        val plan: String,
        val firstStep: String,
        val safeChoice: String,
        val solution: String,
        val check: String
    )

    private val situations = listOf(
        Situation("shopping", "Compra semanal", "preparar una compra sencilla", "Hacer una lista de lo necesario", "Revisar qué falta en casa", "Llevar una lista y el dinero preparado", "Consultar la lista y comprar solo lo necesario", "Comprobar que lleva todos los productos"),
        Situation("appointment", "Cita médica", "acudir a una cita programada", "Anotar la fecha y la hora", "Mirar la cita en el calendario", "Salir con tiempo suficiente", "Consultar el centro si tiene una duda", "Comprobar la hora y la documentación"),
        Situation("medication", "Rutina de medicación", "organizar una toma pautada", "Seguir el pastillero o la pauta indicada", "Leer la pauta preparada", "Consultar a un profesional ante cualquier duda", "Pedir aclaración al profesional sanitario", "Comprobar el día y la toma antes de usarla"),
        Situation("meal", "Preparar una comida", "preparar una comida sencilla", "Reunir los ingredientes y utensilios", "Leer o recordar los pasos", "Mantener la cocina ordenada y vigilada", "Detenerse y pedir ayuda si surge un riesgo", "Comprobar que la cocina queda apagada"),
        Situation("laundry", "Colada", "organizar una colada", "Separar la ropa por tipo o color", "Revisar las prendas", "Seguir las indicaciones de lavado", "Consultar una etiqueta si no la entiende", "Comprobar que no queda ropa dentro"),
        Situation("walk", "Paseo seguro", "preparar un paseo", "Elegir una ruta y un horario adecuados", "Comprobar el tiempo exterior", "Llevar calzado estable y teléfono", "Acortar el paseo si no se encuentra bien", "Comprobar que lleva las llaves"),
        Situation("phone", "Llamada importante", "realizar una llamada", "Preparar el número y el motivo", "Buscar el número correcto", "Sentarse en un lugar tranquilo", "Dejar un mensaje claro si no responden", "Comprobar que ha llamado al número correcto"),
        Situation("home_safety", "Seguridad en casa", "salir de casa de forma segura", "Seguir una rutina de salida", "Revisar puertas y electrodomésticos", "Cerrar la puerta y guardar las llaves", "Volver a revisar si tiene una duda", "Comprobar que la cocina está apagada"),
        Situation("bus", "Uso del autobús", "hacer un desplazamiento en autobús", "Preparar ruta, parada y horario", "Consultar la parada correcta", "Esperar en una zona segura", "Preguntar al conductor si no está seguro", "Comprobar que lleva el billete o tarjeta"),
        Situation("bill", "Revisar un recibo", "revisar un recibo sencillo", "Leer el importe y la fecha", "Localizar el importe", "Guardar el recibo en un lugar fijo", "Pedir ayuda si observa un dato inesperado", "Comprobar la fecha de vencimiento"),
        Situation("visitors", "Preparar una visita", "organizar la llegada de una visita", "Decidir lo necesario con antelación", "Confirmar la hora de llegada", "Preparar un espacio cómodo", "Avisar si necesita cambiar el plan", "Comprobar que tiene lo necesario preparado"),
        Situation("garden", "Cuidado de plantas", "regar una planta", "Preparar agua y lugar de trabajo", "Comprobar si la tierra está seca", "Evitar dejar agua en el suelo", "Pedir ayuda con macetas pesadas", "Comprobar que no queda agua derramada"),
        Situation("documents", "Documentación", "guardar documentos importantes", "Usar una carpeta identificada", "Reunir los documentos", "Guardar las copias en un lugar conocido", "Pedir apoyo si falta un documento", "Comprobar que la carpeta queda cerrada"),
        Situation("birthday", "Recordar un cumpleaños", "preparar un cumpleaños", "Anotar la fecha y un pequeño detalle", "Mirar el calendario", "Preparar el detalle con antelación", "Llamar si no puede acudir", "Comprobar la fecha antes de felicitar"),
        Situation("weather", "Cambio de tiempo", "adaptar una salida al tiempo", "Revisar la previsión antes de salir", "Mirar el tiempo exterior", "Llevar la prenda adecuada", "Posponer la salida si hay riesgo", "Comprobar que lleva protección adecuada"),
        Situation("market", "Compra en mercado", "comprar alimentos frescos", "Decidir qué necesita y cuánto", "Revisar la lista", "Conservar el dinero y la cartera de forma segura", "Preguntar el precio antes de decidir", "Comprobar que la cartera sigue guardada"),
        Situation("library", "Préstamo de libro", "pedir prestado un libro", "Elegir el libro y anotar la devolución", "Buscar el título", "Guardar el resguardo", "Pedir ayuda al personal para localizarlo", "Comprobar la fecha de devolución"),
        Situation("exercise", "Rutina de ejercicio", "preparar ejercicio suave pautado", "Elegir el momento y el material indicado", "Comprobar que el espacio está libre", "Usar calzado y apoyo adecuados", "Detenerse si nota malestar y pedir ayuda", "Comprobar que el espacio queda despejado"),
        Situation("emergency", "Situación imprevista", "actuar ante una situación preocupante", "Mantener la calma y valorar el entorno", "Alejarse de la situación de riesgo", "Pedir ayuda mediante el 112 si hay emergencia", "Contactar con ayuda adecuada", "Comprobar que está en un lugar seguro"),
        Situation("schedule", "Organizar el día", "planificar tareas del día", "Ordenar las tareas por prioridad", "Anotar las tareas pendientes", "Dejar tiempo para descanso y desplazamientos", "Cambiar el orden si surge algo importante", "Comprobar qué tarea queda pendiente")
    )

    private data class Task(
        val id: String,
        val name: String,
        val question: (Situation) -> String,
        val answer: (Situation) -> String
    )

    private val tasks = listOf(
        Task("plan", "Planificación", { "Para ${it.context}, ¿qué estrategia ayuda a organizarse?" }, { it.plan }),
        Task("first", "Primer paso", { "Para ${it.context}, ¿qué conviene hacer primero?" }, { it.firstStep }),
        Task("safe", "Decisión segura", { "Al ${it.context}, ¿cuál es la opción más segura?" }, { it.safeChoice }),
        Task("solve", "Resolver un imprevisto", { "Si surge una dificultad al ${it.context}, ¿qué respuesta es adecuada?" }, { it.solution }),
        Task("check", "Revisión final", { "Después de ${it.context}, ¿qué conviene comprobar?" }, { it.check })
    )

    val items: List<GuidedExercise> = situations.flatMap { situation ->
        tasks.flatMap { task -> (1..5).map { level -> buildExercise(situation, task, level) } }
    }

    init {
        check(situations.size == 20) { "El catálogo requiere 20 situaciones funcionales." }
        check(tasks.size == 5) { "El catálogo requiere cinco procesos ejecutivos." }
        check(items.size == 500) { "El catálogo debe contener exactamente 500 ejercicios." }
        check(items.map { it.id }.distinct().size == items.size) { "Los ejercicios deben tener identificadores únicos." }
    }

    fun find(id: String): GuidedExercise? = items.firstOrNull { it.id == id }

    fun contains(id: String): Boolean = id.startsWith("guided_executive_") && find(id) != null

    fun forCategory(category: String): List<GuidedExercise> = items.filter { it.category == category }

    private fun buildExercise(situation: Situation, task: Task, level: Int): GuidedExercise {
        val correct = task.answer(situation)
        return GuidedExercise(
            id = "guided_executive_${situation.id}_${task.id}_$level",
            name = "${situation.name}: ${task.name} · ${gdsLabel(level)}",
            category = "Funciones Ejecutivas",
            description = "${task.name} aplicada a una actividad cotidiana; adaptación ${gdsLabel(level)}.",
            gdsLevel = level,
            question = questionForLevel(task.question(situation), level),
            correctAnswer = correct,
            options = optionsFor(correct, task.id)
        )
    }

    private fun optionsFor(correct: String, taskId: String): List<String> {
        val distractors = when (taskId) {
            "plan" -> listOf("Hacerlo sin pensarlo", "Dejarlo para otro día sin decidir", "Cambiar de tarea continuamente")
            "first" -> listOf("Empezar por el último paso", "Hacer varias cosas a la vez", "No revisar la situación")
            "safe" -> listOf("Actuar deprisa sin revisar", "Ignorar una duda", "Continuar aunque exista riesgo")
            "solve" -> listOf("Insistir sin pedir apoyo", "Tomar una decisión impulsiva", "Abandonar sin avisar")
            else -> listOf("Dar la tarea por terminada sin mirar", "Dejarlo para más tarde", "Empezar otra tarea sin cerrar esta")
        }
        return listOf(correct) + distractors
    }

    private fun questionForLevel(question: String, level: Int): String = when (level) {
        1 -> "Piensa en la situación y selecciona la estrategia más adecuada. $question"
        2 -> "Lee la situación y elige la mejor respuesta. $question"
        3 -> question
        4 -> "Elige la respuesta correcta. $question"
        else -> question
    }

    private fun gdsLabel(level: Int): String = when (level) {
        1 -> "GDS 3"
        2 -> "GDS 3-4"
        3 -> "GDS 4"
        4 -> "GDS 4-5"
        else -> "GDS 5"
    }
}
