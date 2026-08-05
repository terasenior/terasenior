package com.terapia.terasenior.domain.repository.agenda

import com.terapia.terasenior.domain.model.agenda.Appointment
import com.terapia.terasenior.domain.model.agenda.AppointmentAttendee
import kotlinx.coroutines.flow.Flow

interface AppointmentRepository {
    fun getAppointments(): Flow<Result<List<Appointment>>>
    suspend fun getAppointmentById(id: String): Result<Appointment?>
    
    // Creación completa con participantes
    suspend fun createFullAppointment(
        appointment: Appointment,
        staffIds: List<String>,
        patientIds: List<String>
    ): Result<Unit>
    
    suspend fun updateAppointment(appointment: Appointment): Result<Unit>
    suspend fun deleteAppointment(id: String): Result<Unit>

    // Gestión de asistentes
    suspend fun getAttendees(appointmentId: String): Result<List<AppointmentAttendee>>
    suspend fun updateAttendeeStatus(attendeeId: String, status: String, notes: String?): Result<Unit>
}
