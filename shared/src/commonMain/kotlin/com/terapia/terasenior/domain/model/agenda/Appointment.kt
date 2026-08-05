package com.terapia.terasenior.domain.model.agenda

enum class AppointmentType {
    INDIVIDUAL, GROUP
}

enum class AppointmentStatus {
    SCHEDULED, COMPLETED, CANCELLED
}

enum class AttendanceStatus {
    PENDING, PRESENT, ABSENT
}

data class Appointment(
    val id: String,
    val entityId: String,
    val title: String,
    val description: String?,
    val startAt: String,
    val endAt: String,
    val type: AppointmentType,
    val status: AppointmentStatus
)
