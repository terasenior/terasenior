package com.terapia.terasenior.domain.repository.results

import com.terapia.terasenior.domain.model.results.ActivityResult
import kotlinx.coroutines.flow.Flow

interface ResultsRepository {
    suspend fun saveResult(result: ActivityResult): Result<Unit>
    fun getPatientResults(patientId: String): Flow<Result<List<ActivityResult>>>
    suspend fun getGlobalStats(entityId: String): Result<Map<String, Any>>
}
