package com.terapia.terasenior.data.model.patient

import com.terapia.terasenior.domain.model.patient.OccupationalFollowUp
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class OccupationalFollowUpDto(
    val id: String? = null,
    @SerialName("patient_id") val patientId: String,
    @SerialName("author_id") val authorId: String,
    @SerialName("author_name") val authorName: String,
    @SerialName("author_role") val authorRole: String,
    @SerialName("followup_date") val followUpDate: String,
    @SerialName("neuropsychological_indications") val neuropsychologicalIndications: String? = null,
    @SerialName("treatment_objectives") val treatmentObjectives: String? = null,
    val intervention: String? = null,
    val observations: String? = null,
    @SerialName("signature_name") val signatureName: String? = null,
    @SerialName("created_at") val createdAt: String? = null
)

fun OccupationalFollowUpDto.toDomain() = OccupationalFollowUp(
    id = id.orEmpty(), patientId = patientId, authorId = authorId, authorName = authorName, authorRole = authorRole,
    followUpDate = followUpDate, neuropsychologicalIndications = neuropsychologicalIndications,
    treatmentObjectives = treatmentObjectives, intervention = intervention, observations = observations,
    signatureName = signatureName, createdAt = createdAt.orEmpty()
)

fun OccupationalFollowUp.toData() = OccupationalFollowUpDto(
    id = id.ifBlank { null }, patientId = patientId, authorId = authorId, authorName = authorName, authorRole = authorRole,
    followUpDate = followUpDate, neuropsychologicalIndications = neuropsychologicalIndications,
    treatmentObjectives = treatmentObjectives, intervention = intervention, observations = observations, signatureName = signatureName
)
