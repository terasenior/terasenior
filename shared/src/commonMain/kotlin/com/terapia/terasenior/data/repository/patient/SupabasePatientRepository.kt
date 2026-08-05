package com.terapia.terasenior.data.repository.patient

import com.terapia.terasenior.data.model.patient.ConsentDto
import com.terapia.terasenior.data.model.patient.PatientDto
import com.terapia.terasenior.data.model.patient.TherapeuticProfileDto
import com.terapia.terasenior.data.model.patient.toData
import com.terapia.terasenior.data.model.patient.toDomain
import com.terapia.terasenior.domain.model.patient.Consent
import com.terapia.terasenior.domain.model.patient.Patient
import com.terapia.terasenior.domain.model.patient.TherapeuticProfile
import com.terapia.terasenior.domain.repository.patient.PatientRepository
import com.terapia.terasenior.supabase
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.rpc
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class SupabasePatientRepository : PatientRepository {

    override fun getPatients(): Flow<Result<List<Patient>>> = flow {
        emit(runCatching {
            supabase.postgrest["patients"]
                .select()
                .decodeList<PatientDto>()
                .map { it.toDomain() }
        })
    }

    override suspend fun getPatientById(id: String): Result<Patient?> = runCatching {
        supabase.postgrest["patients"]
            .select { filter { eq("id", id) } }
            .decodeSingleOrNull<PatientDto>()
            ?.toDomain()
    }

    override suspend fun createPatient(patient: Patient): Result<Unit> = runCatching {
        supabase.postgrest["patients"].insert(patient.toData())
    }

    override suspend fun updatePatient(patient: Patient): Result<Unit> = runCatching {
        supabase.postgrest["patients"].update(patient.toData()) {
            filter { eq("id", patient.id) }
        }
    }

    override suspend fun getTherapeuticProfile(patientId: String): Result<TherapeuticProfile?> = runCatching {
        supabase.postgrest["patient_therapeutic_profiles"]
            .select { filter { eq("patient_id", patientId) } }
            .decodeSingleOrNull<TherapeuticProfileDto>()
            ?.toDomain()
    }

    override suspend fun updateTherapeuticProfile(profile: TherapeuticProfile): Result<Unit> = runCatching {
        supabase.postgrest["patient_therapeutic_profiles"].upsert(profile.toData())
    }

    override suspend fun getConsents(patientId: String): Result<List<Consent>> = runCatching {
        supabase.postgrest["patient_consents"]
            .select { filter { eq("patient_id", patientId) } }
            .decodeList<ConsentDto>()
            .map { it.toDomain() }
    }

    override suspend fun updateConsent(consent: Consent): Result<Unit> = runCatching {
        supabase.postgrest["patient_consents"].upsert(consent.toData())
    }

    override suspend fun logAccess(patientId: String, action: String): Result<Unit> = runCatching {
        supabase.postgrest.rpc("log_patient_access", mapOf(
            "p_patient_id" to patientId,
            "p_action" to action
        ))
    }
}
