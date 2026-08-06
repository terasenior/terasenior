package com.terapia.terasenior.domain.model.therapy

data class TherapySessionExercise(
    val id: String,
    val sessionId: String,
    val exerciseType: String,
    val level: Int,
    val position: Int,
    val configuration: Map<String, String> = emptyMap()
)
