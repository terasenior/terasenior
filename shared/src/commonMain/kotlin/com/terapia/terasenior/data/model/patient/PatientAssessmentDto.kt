package com.terapia.terasenior.data.model.patient

import com.terapia.terasenior.domain.model.patient.PatientAssessment
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PatientAssessmentDto(
    val id: String? = null,
    @SerialName("patient_id") val patientId: String,
    @SerialName("author_id") val authorId: String,
    @SerialName("author_name") val authorName: String,
    @SerialName("author_role") val authorRole: String,
    val mobility: String? = null,
    @SerialName("basic_activities") val basicActivities: String? = null,
    @SerialName("instrumental_activities") val instrumentalActivities: String? = null,
    @SerialName("cognitive_status") val cognitiveStatus: String? = null,
    @SerialName("emotional_status") val emotionalStatus: String? = null,
    val risks: String? = null,
    @SerialName("decision_capacity") val decisionCapacity: String? = null,
    val status: String = "ACTIVE",
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("updated_at") val updatedAt: String? = null,
    @SerialName("updated_by_name") val updatedByName: String? = null,
    @SerialName("updated_by_role") val updatedByRole: String? = null,
    @SerialName("discontinued_at") val discontinuedAt: String? = null,
    @SerialName("discontinued_by_name") val discontinuedByName: String? = null
)

fun PatientAssessmentDto.toDomain() = PatientAssessment(
    id = id.orEmpty(), patientId = patientId, authorId = authorId, authorName = authorName, authorRole = authorRole,
    mobility = mobility, basicActivities = basicActivities, instrumentalActivities = instrumentalActivities,
    cognitiveStatus = cognitiveStatus, emotionalStatus = emotionalStatus, risks = risks, decisionCapacity = decisionCapacity,
    status = status, createdAt = createdAt.orEmpty(), updatedAt = updatedAt.orEmpty(),
    updatedByName = updatedByName, updatedByRole = updatedByRole, discontinuedAt = discontinuedAt, discontinuedByName = discontinuedByName
)

fun PatientAssessment.toData() = PatientAssessmentDto(
    id = id.ifBlank { null }, patientId = patientId, authorId = authorId, authorName = authorName, authorRole = authorRole,
    mobility = mobility, basicActivities = basicActivities, instrumentalActivities = instrumentalActivities,
    cognitiveStatus = cognitiveStatus, emotionalStatus = emotionalStatus, risks = risks, decisionCapacity = decisionCapacity,
    status = status, updatedByName = updatedByName, updatedByRole = updatedByRole,
    discontinuedAt = discontinuedAt, discontinuedByName = discontinuedByName
)
