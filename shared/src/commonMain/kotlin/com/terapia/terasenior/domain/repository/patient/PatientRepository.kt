package com.terapia.terasenior.domain.repository.patient

import com.terapia.terasenior.domain.model.patient.Consent
import com.terapia.terasenior.domain.model.patient.Patient
import com.terapia.terasenior.domain.model.patient.TherapeuticProfile
import kotlinx.coroutines.flow.Flow

interface PatientRepository {
    fun getPatients(): Flow<Result<List<Patient>>>
    suspend fun getPatientById(id: String): Result<Patient?>
    suspend fun createPatient(patient: Patient): Result<Unit>
    suspend fun updatePatient(patient: Patient): Result<Unit>
    suspend fun getTherapeuticProfile(patientId: String): Result<TherapeuticProfile?>
    suspend fun updateTherapeuticProfile(profile: TherapeuticProfile): Result<Unit>
    suspend fun getConsents(patientId: String): Result<List<Consent>>
    suspend fun updateConsent(consent: Consent): Result<Unit>
    suspend fun logAccess(patientId: String, action: String): Result<Unit>
}
