package com.terapia.terasenior.domain.usecase.patient

import com.terapia.terasenior.domain.model.patient.Patient
import com.terapia.terasenior.domain.repository.patient.PatientRepository

class UpdatePatientUseCase(
    private val repository: PatientRepository
) {
    suspend operator fun invoke(patient: Patient): Result<Unit> {
        if (patient.firstName.isBlank() || patient.lastName.isBlank()) {
            return Result.failure(Exception("Nombre y apellidos son obligatorios"))
        }
        return repository.updatePatient(patient)
    }
}
