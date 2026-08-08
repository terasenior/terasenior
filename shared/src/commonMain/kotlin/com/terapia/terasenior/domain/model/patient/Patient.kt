package com.terapia.terasenior.domain.model.patient

enum class PatientStatus {
    ACTIVE, INACTIVE, BAJA, DISCHARGED
}

data class Patient(
    val id: String,
    val entityId: String,
    val externalId: String? = null, // Número de Expediente / ID Interno
    val nif: String? = null,        // DNI / NIF oficial
    val firstName: String,
    val lastName: String,
    val preferredName: String?,
    val birthDate: String?,
    val admissionDate: String? = null, // Fecha de Alta
    val dischargeDate: String? = null, // Fecha de Baja
    val address: String? = null,
    val city: String? = null,          // Población
    val postalCode: String? = null,    // Código Postal
    val province: String? = null,      // Provincia
    val phone: String? = null,
    val contactName: String? = null,
    val contactPhone: String? = null,
    val notes: String? = null,        // Notas generales del terapeuta
    val status: PatientStatus,
    val createdAt: String
) {
    val fullName: String get() = "$firstName $lastName"
    
    // Función de utilidad para mostrar el nombre más adecuado (seudonimización básica)
    val displayName: String get() = preferredName ?: firstName
}
