package com.terapia.terasenior.data.model.patient

import com.terapia.terasenior.domain.model.patient.Patient
import com.terapia.terasenior.domain.model.patient.PatientStatus
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PatientDto(
    @SerialName("id") val id: String? = null,
    @SerialName("entity_id") val entityId: String,
    @SerialName("external_id") val externalId: String? = null,
    @SerialName("first_name") val firstName: String,
    @SerialName("last_name") val lastName: String,
    @SerialName("preferred_name") val preferredName: String? = null,
    @SerialName("birth_date") val birthDate: String? = null,
    @SerialName("admission_date") val admissionDate: String? = null,
    @SerialName("discharge_date") val dischargeDate: String? = null,
    @SerialName("address") val address: String? = null,
    @SerialName("phone") val phone: String? = null,
    @SerialName("contact_name") val contactName: String? = null,
    @SerialName("contact_phone") val contactPhone: String? = null,
    @SerialName("status") val status: String = "ACTIVE",
    @SerialName("created_at") val createdAt: String? = null
)

fun PatientDto.toDomain() = Patient(
    id = id.orEmpty(),
    entityId = entityId,
    externalId = externalId,
    firstName = firstName,
    lastName = lastName,
    preferredName = preferredName,
    birthDate = birthDate,
    admissionDate = admissionDate,
    dischargeDate = dischargeDate,
    address = address,
    phone = phone,
    contactName = contactName,
    contactPhone = contactPhone,
    status = try { 
        if (status == "DECEASED") PatientStatus.BAJA else PatientStatus.valueOf(status) 
    } catch (e: Exception) { PatientStatus.ACTIVE },
    createdAt = createdAt.orEmpty()
)

fun Patient.toData() = PatientDto(
    id = id.ifEmpty { null },
    entityId = entityId,
    externalId = externalId,
    firstName = firstName,
    lastName = lastName,
    preferredName = preferredName,
    birthDate = birthDate,
    admissionDate = admissionDate,
    dischargeDate = dischargeDate,
    address = address,
    phone = phone,
    contactName = contactName,
    contactPhone = contactPhone,
    status = status.name
)
