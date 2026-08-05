package com.terapia.terasenior.data.model.agenda

import com.terapia.terasenior.domain.model.agenda.AppointmentAttendee
import com.terapia.terasenior.domain.model.agenda.AttendanceStatus
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AppointmentAttendeeDto(
    @SerialName("id") val id: String? = null,
    @SerialName("appointment_id") val appointmentId: String,
    @SerialName("patient_id") val patientId: String,
    @SerialName("attendance_status") val status: String = "PENDING",
    @SerialName("notes") val notes: String? = null,
    // Relación con pacientes
    @SerialName("patients") val patient: PatientNameDto? = null
)

@Serializable
data class PatientNameDto(
    @SerialName("first_name") val firstName: String,
    @SerialName("last_name") val lastName: String
)

fun AppointmentAttendeeDto.toDomain() = AppointmentAttendee(
    id = id.orEmpty(),
    appointmentId = appointmentId,
    patientId = patientId,
    patientName = patient?.let { "${it.firstName} ${it.lastName}" } ?: "Paciente desconocido",
    status = try { AttendanceStatus.valueOf(status) } catch (e: Exception) { AttendanceStatus.PENDING },
    notes = notes
)

fun AppointmentAttendee.toData() = AppointmentAttendeeDto(
    id = id.ifEmpty { null },
    appointmentId = appointmentId,
    patientId = patientId,
    status = status.name,
    notes = notes
)
