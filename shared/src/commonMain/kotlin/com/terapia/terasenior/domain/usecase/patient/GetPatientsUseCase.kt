package com.terapia.terasenior.domain.usecase.patient

import com.terapia.terasenior.domain.model.patient.Patient
import com.terapia.terasenior.domain.repository.patient.PatientRepository
import kotlinx.coroutines.flow.Flow

class GetPatientsUseCase(
    private val repository: PatientRepository
) {
    operator fun invoke(): Flow<Result<List<Patient>>> {
        return repository.getPatients()
    }
}
