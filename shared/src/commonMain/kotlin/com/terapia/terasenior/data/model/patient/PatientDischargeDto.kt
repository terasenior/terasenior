package com.terapia.terasenior.data.model.patient

import com.terapia.terasenior.domain.model.patient.PatientDischarge
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PatientDischargeDto(
    val id: String? = null,
    @SerialName("patient_id") val patientId: String,
    @SerialName("author_id") val authorId: String,
    @SerialName("author_name") val authorName: String,
    @SerialName("author_role") val authorRole: String,
    @SerialName("assessment_date") val assessmentDate: String? = null,
    @SerialName("service_arrival_date") val serviceArrivalDate: String? = null,
    @SerialName("intervention_start_date") val interventionStartDate: String? = null,
    @SerialName("discharge_date") val dischargeDate: String,
    @SerialName("program_duration") val programDuration: String? = null,
    @SerialName("program_follow_up") val programFollowUp: String? = null,
    @SerialName("discharge_reason") val dischargeReason: String,
    val observations: String? = null,
    @SerialName("is_voluntary") val isVoluntary: Boolean = false,
    @SerialName("voluntary_declarant_name") val voluntaryDeclarantName: String? = null,
    @SerialName("voluntary_declarant_document_id") val voluntaryDeclarantDocumentId: String? = null,
    @SerialName("voluntary_declarant_relationship") val voluntaryDeclarantRelationship: String? = null,
    @SerialName("voluntary_declaration_date") val voluntaryDeclarationDate: String? = null,
    @SerialName("created_at") val createdAt: String? = null
)

fun PatientDischargeDto.toDomain() = PatientDischarge(
    id = id.orEmpty(), patientId = patientId, authorId = authorId, authorName = authorName, authorRole = authorRole,
    assessmentDate = assessmentDate, serviceArrivalDate = serviceArrivalDate, interventionStartDate = interventionStartDate,
    dischargeDate = dischargeDate, programDuration = programDuration, programFollowUp = programFollowUp,
    dischargeReason = dischargeReason, observations = observations, isVoluntary = isVoluntary,
    voluntaryDeclarantName = voluntaryDeclarantName, voluntaryDeclarantDocumentId = voluntaryDeclarantDocumentId,
    voluntaryDeclarantRelationship = voluntaryDeclarantRelationship, voluntaryDeclarationDate = voluntaryDeclarationDate,
    createdAt = createdAt.orEmpty()
)

fun PatientDischarge.toData() = PatientDischargeDto(
    id = id.ifBlank { null }, patientId = patientId, authorId = authorId, authorName = authorName, authorRole = authorRole,
    assessmentDate = assessmentDate, serviceArrivalDate = serviceArrivalDate, interventionStartDate = interventionStartDate,
    dischargeDate = dischargeDate, programDuration = programDuration, programFollowUp = programFollowUp,
    dischargeReason = dischargeReason, observations = observations, isVoluntary = isVoluntary,
    voluntaryDeclarantName = voluntaryDeclarantName, voluntaryDeclarantDocumentId = voluntaryDeclarantDocumentId,
    voluntaryDeclarantRelationship = voluntaryDeclarantRelationship, voluntaryDeclarationDate = voluntaryDeclarationDate
)
