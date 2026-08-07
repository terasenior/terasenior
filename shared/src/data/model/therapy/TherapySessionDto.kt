package com.terapia.terasenior.data.model.therapy

import com.terapia.terasenior.domain.model.therapy.SessionMode
import com.terapia.terasenior.domain.model.therapy.SessionStatus
import com.terapia.terasenior.domain.model.therapy.TherapySession
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TherapySessionDto(
    @SerialName("id") val id: String? = null,
    @SerialName("title") val title: String,
    @SerialName("description") val description: String? = null,
    @SerialName("therapist_id") val therapistId: String,
    @SerialName("patient_id") val patientId: String? = null,
    @SerialName("appointment_id") val appointmentId: String? = null,
    @SerialName("mode") val mode: String,
    @SerialName("status") val status: String = "DRAFT",
    @SerialName("started_at") val startedAt: String? = null,
    @SerialName("finished_at") val finishedAt: String? = null,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("participation_level") val participationLevel: String? = null,
    @SerialName("fatigue_level") val fatigueLevel: String? = null,
    @SerialName("therapist_notes") val therapistNotes: String? = null,
    @SerialName("session_valuation") val valuation: Int = 5
)

fun TherapySessionDto.toDomain() = TherapySession(
    id = id.orEmpty(),
    title = title,
    description = description,
    therapistId = therapistId,
    patientId = patientId,
    appointmentId = appointmentId,
    mode = try { SessionMode.valueOf(mode) } catch (e: Exception) { SessionMode.WITHOUT_PATIENT },
    status = try { SessionStatus.valueOf(status) } catch (e: Exception) { SessionStatus.DRAFT },
    startedAt = startedAt,
    finishedAt = finishedAt,
    createdAt = createdAt.orEmpty(),
    participationLevel = participationLevel,
    fatigueLevel = fatigueLevel,
    therapistNotes = therapistNotes,
    valuation = valuation
)

fun TherapySession.toData() = TherapySessionDto(
    id = id.ifEmpty { null },
    title = title,
    description = description,
    therapistId = therapistId,
    patientId = patientId,
    appointmentId = appointmentId,
    mode = mode.name,
    status = status.name,
    startedAt = startedAt,
    finishedAt = finishedAt,
    participationLevel = participationLevel,
    fatigueLevel = fatigueLevel,
    therapistNotes = therapistNotes,
    valuation = valuation
)
