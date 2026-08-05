package com.terapia.terasenior.data.repository.patient

import com.terapia.terasenior.data.model.patient.PatientDto
import com.terapia.terasenior.data.model.patient.toData
import com.terapia.terasenior.data.model.patient.toDomain
import com.terapia.terasenior.domain.model.patient.Patient
import com.terapia.terasenior.domain.repository.patient.PatientRepository
import com.terapia.terasenior.supabase
import io.github.jan.supabase.postgrest.postgrest
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
}
