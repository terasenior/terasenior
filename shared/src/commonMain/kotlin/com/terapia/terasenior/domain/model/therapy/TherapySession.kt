package com.terapia.terasenior.domain.model.therapy

enum class SessionStatus {
    DRAFT, READY, IN_PROGRESS, PAUSED, COMPLETED, CANCELLED
}

enum class SessionMode {
    WITH_PATIENT, WITHOUT_PATIENT, FROM_APPOINTMENT
}

data class TherapySession(
    val id: String,
    val title: String,
    val description: String?,
    val therapistId: String,
    val patientId: String?,
    val appointmentId: String?,
    val mode: SessionMode,
    val status: SessionStatus,
    val startedAt: String?,
    val finishedAt: String?,
    val createdAt: String,
    // Valoración Clínica (Fase 4)
    val participationLevel: String? = null,
    val fatigueLevel: String? = null,
    val therapistNotes: String? = null,
    val valuation: Int = 5
)
