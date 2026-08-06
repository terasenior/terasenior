package com.terapia.terasenior.domain.model.agenda

import com.terapia.terasenior.domain.model.agenda.AttendanceStatus

data class AppointmentAttendee(
    val id: String,
    val appointmentId: String,
    val patientId: String,
    val patientName: String, // Para mostrar en la lista sin JOIN extra costoso en cliente
    val status: AttendanceStatus,
    val notes: String?
)
