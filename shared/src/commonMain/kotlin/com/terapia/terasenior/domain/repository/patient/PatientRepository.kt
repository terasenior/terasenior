package com.terapia.terasenior.domain.repository.patient

import com.terapia.terasenior.domain.model.patient.Patient
import kotlinx.coroutines.flow.Flow

interface PatientRepository {
    fun getPatients(): Flow<Result<List<Patient>>>
    suspend fun getPatientById(id: String): Result<Patient?>
    suspend fun createPatient(patient: Patient): Result<Unit>
    suspend fun updatePatient(patient: Patient): Result<Unit>
}
