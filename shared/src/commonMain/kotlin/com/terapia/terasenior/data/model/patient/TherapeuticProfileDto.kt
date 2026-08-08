package com.terapia.terasenior.data.model.patient

import com.terapia.terasenior.domain.model.patient.SupportLevel
import com.terapia.terasenior.domain.model.patient.TherapeuticProfile
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TherapeuticProfileDto(
    @SerialName("patient_id") val patientId: String,
    @SerialName("support_level") val supportLevel: String,
    @SerialName("manual_dominance") val manualDominance: String? = null,
    @SerialName("preserved_capacities") val preservedCapacities: String? = null,
    @SerialName("observed_difficulties") val observedDifficulties: String? = null,
    @SerialName("interests_preferences") val interests: String? = null,
    @SerialName("therapeutic_goals") val goals: String? = null,
    @SerialName("mobility") val mobility: String? = null,
    @SerialName("basic_activities") val basicActivities: String? = null,
    @SerialName("instrumental_activities") val instrumentalActivities: String? = null,
    @SerialName("cognitive_status") val cognitiveStatus: String? = null,
    @SerialName("emotional_status") val emotionalStatus: String? = null,
    @SerialName("risks") val risks: String? = null,
    @SerialName("decision_capacity") val decisionCapacity: String? = null
)

fun TherapeuticProfileDto.toDomain() = TherapeuticProfile(
    patientId = patientId,
    supportLevel = try { SupportLevel.valueOf(supportLevel) } catch (e: Exception) { SupportLevel.NONE },
    manualDominance = manualDominance,
    preservedCapacities = preservedCapacities,
    observedDifficulties = observedDifficulties,
    interests = interests,
    goals = goals,
    mobility = mobility,
    basicActivities = basicActivities,
    instrumentalActivities = instrumentalActivities,
    cognitiveStatus = cognitiveStatus,
    emotionalStatus = emotionalStatus,
    risks = risks,
    decisionCapacity = decisionCapacity
)

fun TherapeuticProfile.toData() = TherapeuticProfileDto(
    patientId = patientId,
    supportLevel = supportLevel.name,
    manualDominance = manualDominance,
    preservedCapacities = preservedCapacities,
    observedDifficulties = observedDifficulties,
    interests = interests,
    goals = goals,
    mobility = mobility,
    basicActivities = basicActivities,
    instrumentalActivities = instrumentalActivities,
    cognitiveStatus = cognitiveStatus,
    emotionalStatus = emotionalStatus,
    risks = risks,
    decisionCapacity = decisionCapacity
)
