package com.terapia.terasenior.treatment.ui

/**
 * Convierte el estadio GDS clínico en carga cognitiva para los juegos.
 * GDS 1 implica menor deterioro y, por tanto, la carga más alta; GDS 5 la más baja.
 */
object GdsDifficulty {
    fun challengeLevel(gdsLevel: Int): Int = 6 - gdsLevel.coerceIn(1, 5)

    fun choices(options: List<String>, correctAnswer: String, challengeLevel: Int): List<String> {
        val wanted = when (challengeLevel.coerceIn(1, 5)) {
            1 -> 2
            2 -> 2
            3 -> 3
            else -> 4
        }
        val distinct = options.distinct()
        if (distinct.size <= wanted || correctAnswer !in distinct) return distinct.shuffled()
        return (distinct.filter { it != correctAnswer }.shuffled().take(wanted - 1) + correctAnswer).shuffled()
    }
}
