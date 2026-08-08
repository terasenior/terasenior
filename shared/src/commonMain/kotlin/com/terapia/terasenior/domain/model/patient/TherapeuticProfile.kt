package com.terapia.terasenior.domain.model.patient

enum class SupportLevel {
    NONE, PUNCTUAL, VERBAL, VISUAL, PARTIAL_PHYSICAL, FULL_PHYSICAL
}

data class TherapeuticProfile(
    val patientId: String,
    val supportLevel: SupportLevel,
    val manualDominance: String?,
    val preservedCapacities: String?,
    val observedDifficulties: String?,
    val interests: String?,
    val goals: String?,
    // Valoración geriátrica y funcional (v1.1.1)
    val mobility: String? = null,
    val basicActivities: String? = null,
    val instrumentalActivities: String? = null,
    val cognitiveStatus: String? = null,
    val emotionalStatus: String? = null,
    val risks: String? = null,
    val decisionCapacity: String? = null
)
