package com.terapia.terasenior.domain.model.patient

data class PatientAssessment(
    val id: String,
    val patientId: String,
    val authorId: String,
    val authorName: String,
    val authorRole: String,
    val mobility: String? = null,
    val basicActivities: String? = null,
    val instrumentalActivities: String? = null,
    val cognitiveStatus: String? = null,
    val emotionalStatus: String? = null,
    val risks: String? = null,
    val decisionCapacity: String? = null,
    val status: String = "ACTIVE",
    val createdAt: String = "",
    val updatedAt: String = "",
    val updatedByName: String? = null,
    val updatedByRole: String? = null,
    val discontinuedAt: String? = null,
    val discontinuedByName: String? = null
)
