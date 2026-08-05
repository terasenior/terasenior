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
        birthDate: String? = null
    ): Result<Unit> {
        if (firstName.isBlank() || lastName.isBlank()) {
            return Result.failure(Exception("Nombre y apellidos son obligatorios"))
        }

        val patient = Patient(
            id = "", 
            entityId = entityId,
            firstName = firstName,
            lastName = lastName,
            preferredName = preferredName,
            birthDate = birthDate,
            status = PatientStatus.ACTIVE,
            createdAt = ""
        )

        return repository.createPatient(patient)
    }
}
