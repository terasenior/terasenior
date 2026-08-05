package com.terapia.terasenior.data.repository.results

import com.terapia.terasenior.data.model.results.ActivityResultDto
import com.terapia.terasenior.data.model.results.toData
import com.terapia.terasenior.data.model.results.toDomain
import com.terapia.terasenior.domain.model.results.ActivityResult
import com.terapia.terasenior.domain.repository.results.ResultsRepository
import com.terapia.terasenior.supabase
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class SupabaseResultsRepository : ResultsRepository {

    override suspend fun saveResult(result: ActivityResult): Result<Unit> = runCatching {
        supabase.postgrest["activity_results"].insert(result.toData())
    }

    override fun getPatientResults(patientId: String): Flow<Result<List<ActivityResult>>> = flow {
        emit(runCatching {
            supabase.postgrest["activity_results"]
                .select() {
                    filter { eq("patient_id", patientId) }
                }
                .decodeList<ActivityResultDto>()
                .map { it.toDomain() }
                .sortedByDescending { it.createdAt }
        })
    }

    override suspend fun getGlobalStats(entityId: String): Result<Map<String, Any>> {
        // Implementación futura para gráficas agregadas
        return Result.success(emptyMap())
    }
}
