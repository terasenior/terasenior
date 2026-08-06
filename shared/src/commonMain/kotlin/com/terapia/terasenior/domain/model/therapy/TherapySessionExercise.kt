package com.terapia.terasenior.domain.model.therapy

data class ExerciseConfig(
    val type: String,
    val name: String,
    val category: String,
    val level: Int = 1,
    val description: String = ""
)

data class TherapySessionExercise(
    val id: String,
    val sessionId: String,
    val exerciseType: String,
    val level: Int,
    val position: Int,
    val configuration: Map<String, String> = emptyMap()
)
