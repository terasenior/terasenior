package com.terapia.terasenior.domain.model.patient

data class OccupationalHistory(
    val patientId: String,
    val authorId: String = "",
    val authorName: String = "",
    val authorRole: String = "",
    val referralReason: String? = null,
    val medicalDiagnosis: String? = null,
    val relevantHealthHistory: String? = null,
    val medication: String? = null,
    val functionalAssessment: String? = null,
    val cognitiveEmotionalStatus: String? = null,
    val sensoryCommunication: String? = null,
    val housingAndSupport: String? = null,
    val familySocialContext: String? = null,
    val dailyRoutines: String? = null,
    val occupationalHistory: String? = null,
    val interestsPreferences: String? = null,
    val therapeuticGoals: String? = null,
    val interventionPlan: String? = null,
    val observations: String? = null,
    val createdAt: String = "",
    val updatedAt: String = ""
)
