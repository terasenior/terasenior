package com.terapia.terasenior.treatment.repository

/**
 * Imágenes fotográficas de alto contraste para estimulación cognitiva.
 *
 * Los recursos son propios del proyecto y se muestran sin texto ni marcas,
 * para que resulten claros a personas mayores. Todo ejercicio nuevo que
 * requiera una imagen debe obtenerla desde este catálogo, no desde iconos ni
 * desde proveedores externos cambiantes.
 */
object RealisticExerciseImageCatalog {
    private const val baseUrl =
        "https://raw.githubusercontent.com/terasenior/terasenior/main/" +
            "shared/src/commonMain/composeResources/drawable/"

    val apple = baseUrl + "realistic_apple.png"
    val banana = baseUrl + "realistic_banana.png"
    val dog = baseUrl + "realistic_dog.png"
    val cat = baseUrl + "realistic_cat.png"
    val clock = baseUrl + "realistic_clock.png"
    val cup = baseUrl + "realistic_cup.png"
    val chair = baseUrl + "realistic_chair.png"
    val phone = baseUrl + "realistic_phone.png"
    val book = baseUrl + "realistic_book.png"
    val bus = baseUrl + "realistic_bus.png"
    val hand = baseUrl + "realistic_hand.png"
    val weekCalendar = baseUrl + "realistic_week_calendar.png"
    val seasons = baseUrl + "realistic_seasons.png"
    val dailyCalendar = baseUrl + "realistic_daily_calendar.png"
    val appointmentCalendar = baseUrl + "realistic_appointment_calendar.png"
    val emotions = baseUrl + "realistic_emotions.png"
    val homeCommunity = baseUrl + "realistic_home_community.png"

    val objectImages = listOf(apple, dog, cat, banana, cup, clock, chair, phone, book, bus)

    /** Texto breve para que la fotografía sea una ayuda terapéutica explícita. */
    fun supportText(imageUrl: String): String = when (imageUrl) {
        apple -> "Observa la manzana de la imagen. Úsala como ayuda antes de responder."
        banana -> "Observa el plátano de la imagen. Úsalo como ayuda antes de responder."
        dog -> "Observa el perro de la imagen. Úsalo como ayuda antes de responder."
        cat -> "Observa el gato de la imagen. Úsalo como ayuda antes de responder."
        clock -> "Observa el reloj de la imagen. Úsalo como ayuda antes de responder."
        cup -> "Observa la taza de la imagen. Úsala como ayuda antes de responder."
        chair -> "Observa la silla de la imagen. Úsala como ayuda antes de responder."
        phone -> "Observa el teléfono de la imagen. Úsalo como ayuda antes de responder."
        book -> "Observa el libro de la imagen. Úsalo como ayuda antes de responder."
        bus -> "Observa el autobús de la imagen. Úsalo como ayuda antes de responder."
        hand -> "Observa la mano de la imagen. Úsala como ayuda antes de responder."
        weekCalendar -> "Observa el calendario semanal. Sigue el orden de los días antes de responder."
        seasons -> "Observa las cuatro estaciones. Fíjate en el tiempo y en los cambios del paisaje."
        dailyCalendar -> "Observa el calendario y los objetos de la mañana. Úsalos como ayuda antes de responder."
        appointmentCalendar -> "Observa el calendario: hay una fecha marcada para recordar la cita."
        emotions -> "Observa las expresiones de las personas. Fíjate en cómo se sienten antes de responder."
        homeCommunity -> "Observa las habitaciones y los lugares del barrio. Úsalos como ayuda antes de responder."
        else -> "Observa la imagen con calma. Úsala como ayuda antes de responder."
    }

    /** Una imagen contextual para cada familia de las 500 actividades ejecutivas. */
    fun forExecutiveFamily(familyId: String): String = when (familyId) {
        "countdown" -> clock
        "decisions" -> homeCommunity
        "emotions" -> emotions
        "rhythms" -> hand
        "calculation" -> apple
        "reverse" -> book
        "flexibility" -> book
        "stories" -> homeCommunity
        "logic" -> dailyCalendar
        "planning" -> dailyCalendar
        else -> book
    }
}

