package com.terapia.terasenior.domain.model.results

data class ActivityResult(
    val id: String,
    val patientId: String,
    val professionalId: String,
    val appointmentId: String?,
    val sessionId: String? = null, // Vínculo con la sesión (Fase 4)
    val activityType: String,
    val score: Int,
    val durationSeconds: Int,
    val errorsCount: Int,
    val difficultyLevel: String,
    val createdAt: String
)
