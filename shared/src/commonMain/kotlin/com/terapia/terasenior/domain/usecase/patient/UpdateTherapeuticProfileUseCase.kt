package com.terapia.terasenior.domain.usecase.patient

import com.terapia.terasenior.domain.model.patient.TherapeuticProfile
import com.terapia.terasenior.domain.repository.patient.PatientRepository

class UpdateTherapeuticProfileUseCase(
    private val repository: PatientRepository
) {
    suspend operator fun invoke(profile: TherapeuticProfile): Result<Unit> {
        return repository.updateTherapeuticProfile(profile)
    }
}
