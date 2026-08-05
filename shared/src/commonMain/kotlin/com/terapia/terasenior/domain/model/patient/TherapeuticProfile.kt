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
    val goals: String?
)
