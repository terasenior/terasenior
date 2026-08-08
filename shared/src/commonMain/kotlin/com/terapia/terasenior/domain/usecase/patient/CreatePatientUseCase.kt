package com.terapia.terasenior.domain.usecase.patient

import com.terapia.terasenior.domain.model.patient.Patient
import com.terapia.terasenior.domain.model.patient.PatientStatus
import com.terapia.terasenior.domain.repository.patient.PatientRepository

class CreatePatientUseCase(
    private val repository: PatientRepository
) {
    suspend operator fun invoke(
        entityId: String,
        firstName: String,
        lastName: String,
        preferredName: String? = null,
        birthDate: String? = null,
        externalId: String? = null,
        nif: String? = null,
        admissionDate: String? = null,
        address: String? = null,
        city: String? = null,
        postalCode: String? = null,
        province: String? = null,
        phone: String? = null,
        contactName: String? = null,
        contactPhone: String? = null,
        notes: String? = null,
        status: PatientStatus = PatientStatus.ACTIVE
    ): Result<Unit> {
        if (firstName.isBlank() || lastName.isBlank()) {
            return Result.failure(Exception("Nombre y apellidos son obligatorios"))
        }

        val patient = Patient(
            id = "", 
            entityId = entityId,
            externalId = externalId,
            nif = nif,
            firstName = firstName,
            lastName = lastName,
            preferredName = preferredName,
            birthDate = birthDate,
            admissionDate = admissionDate,
            address = address,
            city = city,
            postalCode = postalCode,
            province = province,
            phone = phone,
            contactName = contactName,
            contactPhone = contactPhone,
            notes = notes,
            status = status,
            createdAt = ""
        )

        return repository.createPatient(patient)
    }
}
