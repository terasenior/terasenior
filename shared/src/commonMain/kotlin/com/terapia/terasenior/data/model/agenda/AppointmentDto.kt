package com.terapia.terasenior.data.model.agenda

import com.terapia.terasenior.domain.model.agenda.Appointment
import com.terapia.terasenior.domain.model.agenda.AppointmentStatus
import com.terapia.terasenior.domain.model.agenda.AppointmentType
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AppointmentDto(
    @SerialName("id") val id: String? = null,
    @SerialName("entity_id") val entityId: String,
    @SerialName("title") val title: String,
    @SerialName("description") val description: String? = null,
    @SerialName("start_at") val startAt: String,
    @SerialName("end_at") val endAt: String,
    @SerialName("type") val type: String = "INDIVIDUAL",
    @SerialName("status") val status: String = "SCHEDULED"
)

fun AppointmentDto.toDomain() = Appointment(
    id = id.orEmpty(),
    entityId = entityId,
    title = title,
    description = description,
    startAt = startAt,
    endAt = endAt,
    type = try { AppointmentType.valueOf(type) } catch (e: Exception) { AppointmentType.INDIVIDUAL },
    status = try { AppointmentStatus.valueOf(status) } catch (e: Exception) { AppointmentStatus.SCHEDULED }
)

fun Appointment.toData() = AppointmentDto(
    id = id.ifEmpty { null },
    entityId = entityId,
    title = title,
    description = description,
    startAt = startAt,
    endAt = endAt,
    type = type.name,
    status = status.name
)
