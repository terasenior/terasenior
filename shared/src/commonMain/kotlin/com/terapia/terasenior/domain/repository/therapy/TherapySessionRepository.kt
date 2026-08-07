package com.terapia.terasenior.domain.repository.therapy

import com.terapia.terasenior.domain.model.therapy.AssistanceEvent
import com.terapia.terasenior.domain.model.therapy.SessionIncident
import com.terapia.terasenior.domain.model.therapy.TherapySession
import com.terapia.terasenior.domain.model.therapy.TherapySessionExercise
import kotlinx.coroutines.flow.Flow

interface TherapySessionRepository {
    suspend fun createSession(session: TherapySession): Result<String>
    suspend fun addExerciseToSession(exercise: TherapySessionExercise): Result<Unit>
    suspend fun updateSessionStatus(sessionId: String, status: String): Result<Unit>
    suspend fun saveSessionClosing(session: TherapySession): Result<Unit>
    fun getSessionsByTherapist(therapistId: String): Flow<Result<List<TherapySession>>>
    
    // Nuevos métodos Fase 2
    suspend fun logAssistance(event: AssistanceEvent): Result<Unit>
    suspend fun logIncident(incident: SessionIncident): Result<Unit>
    suspend fun getSessionDetails(sessionId: String): Result<TherapySession?>
    suspend fun getExercisesForSession(sessionId: String): Result<List<TherapySessionExercise>>
    
    // Métodos de estadísticas y resumen
    suspend fun getTherapistSummary(therapistId: String): Result<Map<String, Int>>
    fun getRecentSessions(therapistId: String, limit: Int = 5): Flow<Result<List<TherapySession>>>
}
