package com.terapia.terasenior.data.model.results

import com.terapia.terasenior.domain.model.results.ActivityResult
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ActivityResultDto(
    @SerialName("id") val id: String? = null,
    @SerialName("patient_id") val patientId: String,
    @SerialName("professional_id") val professionalId: String,
    @SerialName("appointment_id") val appointmentId: String? = null,
    @SerialName("session_id") val sessionId: String? = null,
    @SerialName("activity_type") val activityType: String,
    @SerialName("score") val score: Int,
    @SerialName("duration_seconds") val durationSeconds: Int,
    @SerialName("errors_count") val errorsCount: Int,
    @SerialName("difficulty_level") val difficultyLevel: String,
    @SerialName("created_at") val createdAt: String? = null
)

fun ActivityResultDto.toDomain() = ActivityResult(
    id = id.orEmpty(),
    patientId = patientId,
    professionalId = professionalId,
    appointmentId = appointmentId,
    sessionId = sessionId,
    activityType = activityType,
    score = score,
    durationSeconds = durationSeconds,
    errorsCount = errorsCount,
    difficultyLevel = difficultyLevel,
    createdAt = createdAt.orEmpty()
)

fun ActivityResult.toData() = ActivityResultDto(
    id = id.ifEmpty { null },
    patientId = patientId,
    professionalId = professionalId,
    appointmentId = appointmentId,
    sessionId = sessionId,
    activityType = activityType,
    score = score,
    durationSeconds = durationSeconds,
    errorsCount = errorsCount,
    difficultyLevel = difficultyLevel
)
