package com.terapia.terasenior.domain.usecase.patient

import com.terapia.terasenior.domain.model.patient.TherapeuticProfile
import com.terapia.terasenior.domain.repository.patient.PatientRepository

class GetTherapeuticProfileUseCase(
    private val repository: PatientRepository
) {
    suspend operator fun invoke(patientId: String): Result<TherapeuticProfile?> {
        return repository.getTherapeuticProfile(patientId)
    }
}
