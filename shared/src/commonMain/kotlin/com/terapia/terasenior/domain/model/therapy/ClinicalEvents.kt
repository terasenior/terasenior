package com.terapia.terasenior.domain.model.therapy

enum class AssistanceType {
    VERBAL, VISUAL, DEMONSTRATION, PHYSICAL
}

enum class IncidentType {
    FATIGUE, FRUSTRATION, TECHNICAL, MEDICAL, DISORIENTATION
}

data class AssistanceEvent(
    val id: String,
    val sessionId: String,
    val exerciseId: String?,
    val type: AssistanceType,
    val description: String?,
    val occurredAt: String
)

data class SessionIncident(
    val id: String,
    val sessionId: String,
    val exerciseId: String?,
    val type: IncidentType,
    val description: String?,
    val severity: String,
    val occurredAt: String
)
