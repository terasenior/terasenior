package com.terapia.terasenior.domain.model.patient

data class OccupationalFollowUp(
    val id: String = "",
    val patientId: String,
    val authorId: String = "",
    val authorName: String = "",
    val authorRole: String = "",
    val followUpDate: String,
    val neuropsychologicalIndications: String? = null,
    val treatmentObjectives: String? = null,
    val intervention: String? = null,
    val observations: String? = null,
    val signatureName: String? = null,
    val createdAt: String = ""
)
