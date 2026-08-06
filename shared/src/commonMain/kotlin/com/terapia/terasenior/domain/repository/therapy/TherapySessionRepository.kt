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
    fun getSessionsByTherapist(therapistId: String): Flow<Result<List<TherapySession>>>
    
    // Nuevos métodos Fase 2
    suspend fun logAssistance(event: AssistanceEvent): Result<Unit>
    suspend fun logIncident(incident: SessionIncident): Result<Unit>
    suspend fun getSessionDetails(sessionId: String): Result<TherapySession?>
}
