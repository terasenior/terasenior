package com.terapia.terasenior.domain.model.patient

enum class ConsentType {
    RESULTS, IMAGES, DATA_EXPORT, THIRD_PARTY
}

enum class ConsentStatus {
    PENDING, ACCEPTED, REJECTED, REVOKED
}

data class Consent(
    val id: String,
    val patientId: String,
    val type: ConsentType,
    val status: ConsentStatus,
    val version: String,
    val signedAt: String?,
    val observations: String?
)
