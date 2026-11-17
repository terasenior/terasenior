package com.terapia.terasenior.data.model.patient

import com.terapia.terasenior.domain.model.patient.ManualActivityLog
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ManualActivityLogDto(
    val id: String? = null,
    @SerialName("patient_id") val patientId: String,
    @SerialName("author_id") val authorId: String,
    @SerialName("author_name") val authorName: String,
    @SerialName("author_role") val authorRole: String,
    @SerialName("activity_date") val activityDate: String,
    @SerialName("activity_name") val activityName: String,
    @SerialName("therapeutic_area") val therapeuticArea: String? = null,
    val complexity: String = "MEDIA",
    @SerialName("performance_score") val performanceScore: Int? = null,
    @SerialName("duration_minutes") val durationMinutes: Int? = null,
    @SerialName("result_notes") val resultNotes: String? = null,
    @SerialName("behavioral_observations") val behavioralObservations: String? = null,
    @SerialName("created_at") val createdAt: String? = null
)

fun ManualActivityLogDto.toDomain() = ManualActivityLog(
    id = id.orEmpty(), patientId = patientId, authorId = authorId, authorName = authorName, authorRole = authorRole,
    activityDate = activityDate, activityName = activityName, therapeuticArea = therapeuticArea, complexity = complexity,
    performanceScore = performanceScore, durationMinutes = durationMinutes, resultNotes = resultNotes,
    behavioralObservations = behavioralObservations, createdAt = createdAt.orEmpty()
)

fun ManualActivityLog.toData() = ManualActivityLogDto(
    id = id.ifBlank { null }, patientId = patientId, authorId = authorId, authorName = authorName, authorRole = authorRole,
    activityDate = activityDate, activityName = activityName, therapeuticArea = therapeuticArea, complexity = complexity,
    performanceScore = performanceScore, durationMinutes = durationMinutes, resultNotes = resultNotes,
    behavioralObservations = behavioralObservations
)
