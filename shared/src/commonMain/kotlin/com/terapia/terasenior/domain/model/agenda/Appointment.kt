package com.terapia.terasenior.domain.model.agenda

enum class AppointmentType {
    INDIVIDUAL, GROUP
}

enum class AttendanceStatus {
    PENDING, PRESENT, ABSENT
}

enum class AppointmentStatus {
    SCHEDULED, CONFIRMED, IN_PROGRESS, COMPLETED, CANCELLED, MISSED
}

data class Appointment(
    val id: String,
    val entityId: String,
    val title: String,
    val description: String?,
    val startAt: String,
    val endAt: String,
    val type: AppointmentType,
    val status: AppointmentStatus,
    val sessionId: String? = null,
    val interventionType: String? = "ESTIMULACION",
    val notes: String? = null,
    val plannedExercises: List<com.terapia.terasenior.domain.model.therapy.ExerciseConfig> = emptyList() 
)
