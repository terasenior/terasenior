package com.terapia.terasenior.data.repository.agenda

import com.terapia.terasenior.data.model.agenda.AppointmentAttendeeDto
import com.terapia.terasenior.data.model.agenda.AppointmentDto
import com.terapia.terasenior.data.model.agenda.toData
import com.terapia.terasenior.data.model.agenda.toDomain
import com.terapia.terasenior.domain.model.agenda.Appointment
import com.terapia.terasenior.domain.model.agenda.AppointmentAttendee
import com.terapia.terasenior.domain.repository.agenda.AppointmentRepository
import com.terapia.terasenior.supabase
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class SupabaseAppointmentRepository : AppointmentRepository {

    override fun getAppointments(): Flow<Result<List<Appointment>>> = flow {
        emit(runCatching {
            supabase.postgrest["appointments"]
                .select()
                .decodeList<AppointmentDto>()
                .map { it.toDomain() }
        })
    }

    override suspend fun getAppointmentById(id: String): Result<Appointment?> = runCatching {
        supabase.postgrest["appointments"]
            .select { filter { eq("id", id) } }
            .decodeSingleOrNull<AppointmentDto>()
            ?.toDomain()
    }

    override suspend fun createFullAppointment(
        appointment: Appointment,
        staffIds: List<String>,
        patientIds: List<String>
    ): Result<Unit> = runCatching {
        val inserted = supabase.postgrest["appointments"].insert(appointment.toData()) {
            select()
        }.decodeSingle<AppointmentDto>()
        
        val appointmentId = inserted.id ?: throw Exception("Error al obtener ID de la cita")

        if (staffIds.isNotEmpty()) {
            val staffData = staffIds.map { mapOf("appointment_id" to appointmentId, "professional_id" to it) }
            supabase.postgrest["appointment_staff"].insert(staffData)
        }

        if (patientIds.isNotEmpty()) {
            val attendeesData = patientIds.map { mapOf("appointment_id" to appointmentId, "patient_id" to it) }
            supabase.postgrest["appointment_attendees"].insert(attendeesData)
        }
    }

    override suspend fun updateAppointment(appointment: Appointment): Result<Unit> = runCatching {
        supabase.postgrest["appointments"].update(appointment.toData()) {
            filter { eq("id", appointment.id) }
        }
    }

    override suspend fun deleteAppointment(id: String): Result<Unit> = runCatching {
        supabase.postgrest["appointments"].delete {
            filter { eq("id", id) }
        }
    }

    override suspend fun getAttendees(appointmentId: String): Result<List<AppointmentAttendee>> = runCatching {
        supabase.postgrest["appointment_attendees"]
            .select(Columns.raw("*, patients(first_name, last_name)")) {
                filter { eq("appointment_id", appointmentId) }
            }
            .decodeList<AppointmentAttendeeDto>()
            .map { it.toDomain() }
    }

    override suspend fun updateAttendeeStatus(attendeeId: String, status: String, notes: String?): Result<Unit> = runCatching {
        val updateMap = mutableMapOf<String, Any?>("attendance_status" to status)
        if (notes != null) updateMap["notes"] = notes
        
        supabase.postgrest["appointment_attendees"].update(updateMap) {
            filter { eq("id", attendeeId) }
        }
    }
}
