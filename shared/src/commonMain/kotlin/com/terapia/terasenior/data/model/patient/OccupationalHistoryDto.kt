package com.terapia.terasenior.data.model.patient

import com.terapia.terasenior.domain.model.patient.OccupationalHistory
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class OccupationalHistoryDto(
    @SerialName("patient_id") val patientId: String,
    @SerialName("author_id") val authorId: String,
    @SerialName("author_name") val authorName: String,
    @SerialName("author_role") val authorRole: String,
    @SerialName("referral_reason") val referralReason: String? = null,
    @SerialName("medical_diagnosis") val medicalDiagnosis: String? = null,
    @SerialName("relevant_health_history") val relevantHealthHistory: String? = null,
    val medication: String? = null,
    @SerialName("functional_assessment") val functionalAssessment: String? = null,
    @SerialName("cognitive_emotional_status") val cognitiveEmotionalStatus: String? = null,
    @SerialName("sensory_communication") val sensoryCommunication: String? = null,
    @SerialName("housing_and_support") val housingAndSupport: String? = null,
    @SerialName("family_social_context") val familySocialContext: String? = null,
    @SerialName("daily_routines") val dailyRoutines: String? = null,
    @SerialName("occupational_history") val occupationalHistory: String? = null,
    @SerialName("interests_preferences") val interestsPreferences: String? = null,
    @SerialName("therapeutic_goals") val therapeuticGoals: String? = null,
    @SerialName("intervention_plan") val interventionPlan: String? = null,
    val observations: String? = null,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("updated_at") val updatedAt: String? = null
)

fun OccupationalHistoryDto.toDomain() = OccupationalHistory(
    patientId = patientId, authorId = authorId, authorName = authorName, authorRole = authorRole,
    referralReason = referralReason, medicalDiagnosis = medicalDiagnosis, relevantHealthHistory = relevantHealthHistory,
    medication = medication, functionalAssessment = functionalAssessment, cognitiveEmotionalStatus = cognitiveEmotionalStatus,
    sensoryCommunication = sensoryCommunication, housingAndSupport = housingAndSupport, familySocialContext = familySocialContext,
    dailyRoutines = dailyRoutines, occupationalHistory = occupationalHistory, interestsPreferences = interestsPreferences,
    therapeuticGoals = therapeuticGoals, interventionPlan = interventionPlan, observations = observations,
    createdAt = createdAt.orEmpty(), updatedAt = updatedAt.orEmpty()
)

fun OccupationalHistory.toData() = OccupationalHistoryDto(
    patientId = patientId, authorId = authorId, authorName = authorName, authorRole = authorRole,
    referralReason = referralReason, medicalDiagnosis = medicalDiagnosis, relevantHealthHistory = relevantHealthHistory,
    medication = medication, functionalAssessment = functionalAssessment, cognitiveEmotionalStatus = cognitiveEmotionalStatus,
    sensoryCommunication = sensoryCommunication, housingAndSupport = housingAndSupport, familySocialContext = familySocialContext,
    dailyRoutines = dailyRoutines, occupationalHistory = occupationalHistory, interestsPreferences = interestsPreferences,
    therapeuticGoals = therapeuticGoals, interventionPlan = interventionPlan, observations = observations
)
