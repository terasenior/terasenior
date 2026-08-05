package com.terapia.terasenior.data.repository.agenda

import com.terapia.terasenior.data.model.agenda.AppointmentDto
import com.terapia.terasenior.data.model.agenda.toData
import com.terapia.terasenior.data.model.agenda.toDomain
import com.terapia.terasenior.domain.model.agenda.Appointment
import com.terapia.terasenior.domain.repository.agenda.AppointmentRepository
import com.terapia.terasenior.supabase
import io.github.jan.supabase.postgrest.postgrest
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

    override suspend fun createAppointment(appointment: Appointment): Result<Unit> = runCatching {
        supabase.postgrest["appointments"].insert(appointment.toData())
    }

    override suspend fun updateAppointment(appointment: Appointment): Result<Unit> = runCatching {
        supabase.postgrest["appointments"].update(appointment.toData()) {
            filter { eq("id", appointment.id) }
        }
    }

    override suspend fun getAppointmentById(id: String): Result<Appointment?> = runCatching {
        supabase.postgrest["appointments"]
            .select { filter { eq("id", id) } }
            .decodeSingleOrNull<AppointmentDto>()
            ?.toDomain()
    }
}
