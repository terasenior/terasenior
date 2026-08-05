package com.terapia.terasenior.domain.model.patient

enum class PatientStatus {
    ACTIVE, INACTIVE, DECEASED, DISCHARGED
}

data class Patient(
    val id: String,
    val entityId: String,
    val firstName: String,
    val lastName: String,
    val preferredName: String?,
    val birthDate: String?,
    val status: PatientStatus,
    val createdAt: String
) {
    val fullName: String get() = "$firstName $lastName"
    
    // Función de utilidad para mostrar el nombre más adecuado (seudonimización básica)
    val displayName: String get() = preferredName ?: firstName
}
