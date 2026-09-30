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
            "shared/src/commonMain/composeResources/drawable/therapeutic/"

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

    val objectImages = listOf(apple, dog, cat, banana, cup, clock, chair, phone, book, bus)

    /** Una imagen contextual para cada familia de las 500 actividades ejecutivas. */
    fun forExecutiveFamily(familyId: String): String = when (familyId) {
        "countdown" -> clock
        "decisions" -> phone
        "emotions" -> dog
        "rhythms" -> cup
        "calculation" -> apple
        "reverse" -> book
        "flexibility" -> chair
        "stories" -> bus
        "logic" -> banana
        "planning" -> phone
        else -> book
    }
}
