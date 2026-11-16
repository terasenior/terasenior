package com.terapia.terasenior.domain.model.patient

data class PatientDischarge(
    val id: String = "",
    val patientId: String,
    val authorId: String = "",
    val authorName: String = "",
    val authorRole: String = "",
    val assessmentDate: String? = null,
    val serviceArrivalDate: String? = null,
    val interventionStartDate: String? = null,
    val dischargeDate: String,
    val programDuration: String? = null,
    val programFollowUp: String? = null,
    val dischargeReason: String,
    val observations: String? = null,
    val isVoluntary: Boolean = false,
    val voluntaryDeclarantName: String? = null,
    val voluntaryDeclarantDocumentId: String? = null,
    val voluntaryDeclarantRelationship: String? = null,
    val voluntaryDeclarationDate: String? = null,
    val createdAt: String = ""
)
