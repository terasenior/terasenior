package com.terapia.terasenior.treatment.repository

/**
 * Da a cada actividad del catálogo una consigna completa y distinta sin
 * introducir etiquetas técnicas ni aumentar innecesariamente la dificultad.
 */
internal fun distinctCatalogPrompt(text: String, number: Int): String {
    val introductions = listOf(
        "Mira con calma y responde:",
        "Fíjate bien antes de elegir:",
        "Escucha la pregunta y responde:",
        "Tómate un momento para pensar:",
        "Lee cada opción con atención:",
        "Busca la pista más importante:",
        "Observa despacio antes de responder:",
        "Elige la respuesta que tenga más sentido:",
        "Comprueba la información y responde:",
        "Cuando estés preparado, contesta:"
    )
    val supports = listOf(
        "Lee la pregunta con calma.",
        "Elige una sola respuesta.",
        "No hace falta responder deprisa.",
        "Vuelve a leer la pregunta si lo necesitas.",
        "Piensa en una situación cotidiana."
    )
    val index = (number - 1).coerceAtLeast(0)
    return "${introductions[index % introductions.size]} $text\n\n${supports[(index / introductions.size) % supports.size]}"
}
