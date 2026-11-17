package com.terapia.terasenior.domain.model.patient

data class ManualActivityLog(
    val id: String = "",
    val patientId: String,
    val authorId: String = "",
    val authorName: String = "",
    val authorRole: String = "",
    val activityDate: String,
    val activityName: String,
    val therapeuticArea: String? = null,
    val complexity: String = "MEDIA",
    val performanceScore: Int? = null,
    val durationMinutes: Int? = null,
    val resultNotes: String? = null,
    val behavioralObservations: String? = null,
    val createdAt: String = ""
)
