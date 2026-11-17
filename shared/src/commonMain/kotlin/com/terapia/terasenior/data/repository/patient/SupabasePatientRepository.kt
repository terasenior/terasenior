package com.terapia.terasenior.data.repository.patient

import com.terapia.terasenior.data.model.patient.ConsentDto
import com.terapia.terasenior.data.model.patient.PatientDto
import com.terapia.terasenior.data.model.patient.PatientAssessmentDto
import com.terapia.terasenior.data.model.patient.PatientDischargeDto
import com.terapia.terasenior.data.model.patient.OccupationalHistoryDto
import com.terapia.terasenior.data.model.patient.OccupationalFollowUpDto
import com.terapia.terasenior.data.model.patient.ManualActivityLogDto
import com.terapia.terasenior.data.model.patient.TherapeuticProfileDto
import com.terapia.terasenior.data.model.patient.toData
import com.terapia.terasenior.data.model.patient.toDomain
import com.terapia.terasenior.domain.model.patient.Consent
import com.terapia.terasenior.domain.model.patient.Patient
import com.terapia.terasenior.domain.model.patient.PatientAssessment
import com.terapia.terasenior.domain.model.patient.PatientDischarge
import com.terapia.terasenior.domain.model.patient.OccupationalHistory
import com.terapia.terasenior.domain.model.patient.OccupationalFollowUp
import com.terapia.terasenior.domain.model.patient.ManualActivityLog
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

    override suspend fun getPatientAssessments(patientId: String): Result<List<PatientAssessment>> = runCatching {
        supabase.postgrest["patient_assessments"].select {
            filter { eq("patient_id", patientId) }
        }.decodeList<PatientAssessmentDto>().map { it.toDomain() }
    }

    override suspend fun createPatientAssessment(assessment: PatientAssessment): Result<Unit> = runCatching {
        supabase.postgrest["patient_assessments"].insert(assessment.toData())
    }

    override suspend fun updatePatientAssessment(assessment: PatientAssessment): Result<Unit> = runCatching {
        supabase.postgrest["patient_assessments"].update(assessment.toData()) {
            filter { eq("id", assessment.id) }
        }
    }

    override suspend fun getPatientDischarges(patientId: String): Result<List<PatientDischarge>> = runCatching {
        supabase.postgrest["patient_discharges"].select {
            filter { eq("patient_id", patientId) }
        }.decodeList<PatientDischargeDto>().map { it.toDomain() }
    }

    override suspend fun createPatientDischarge(discharge: PatientDischarge): Result<Unit> = runCatching {
        supabase.postgrest["patient_discharges"].insert(discharge.toData())
    }

    override suspend fun getOccupationalHistory(patientId: String): Result<OccupationalHistory?> = runCatching {
        supabase.postgrest["patient_occupational_histories"].select {
            filter { eq("patient_id", patientId) }
        }.decodeSingleOrNull<OccupationalHistoryDto>()?.toDomain()
    }

    override suspend fun saveOccupationalHistory(history: OccupationalHistory): Result<Unit> = runCatching {
        supabase.postgrest["patient_occupational_histories"].upsert(history.toData())
    }

    override suspend fun getOccupationalFollowUps(patientId: String): Result<List<OccupationalFollowUp>> = runCatching {
        supabase.postgrest["patient_occupational_followups"].select {
            filter { eq("patient_id", patientId) }
        }.decodeList<OccupationalFollowUpDto>().map { it.toDomain() }
    }

    override suspend fun createOccupationalFollowUp(followUp: OccupationalFollowUp): Result<Unit> = runCatching {
        supabase.postgrest["patient_occupational_followups"].insert(followUp.toData())
    }

    override suspend fun getManualActivityLogs(patientId: String): Result<List<ManualActivityLog>> = runCatching {
        supabase.postgrest["patient_manual_activity_logs"].select {
            filter { eq("patient_id", patientId) }
        }.decodeList<ManualActivityLogDto>().map { it.toDomain() }
    }

    override suspend fun createManualActivityLog(log: ManualActivityLog): Result<Unit> = runCatching {
        supabase.postgrest["patient_manual_activity_logs"].insert(log.toData())
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
