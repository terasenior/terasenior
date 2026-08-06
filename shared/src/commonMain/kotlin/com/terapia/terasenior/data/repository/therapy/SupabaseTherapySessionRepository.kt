package com.terapia.terasenior.data.repository.therapy

import com.terapia.terasenior.data.model.therapy.TherapySessionDto
import com.terapia.terasenior.data.model.therapy.TherapySessionExerciseDto
import com.terapia.terasenior.data.model.therapy.toData
import com.terapia.terasenior.data.model.therapy.toDomain
import com.terapia.terasenior.domain.model.therapy.AssistanceEvent
import com.terapia.terasenior.domain.model.therapy.SessionIncident
import com.terapia.terasenior.domain.model.therapy.TherapySession
import com.terapia.terasenior.domain.model.therapy.TherapySessionExercise
import com.terapia.terasenior.domain.repository.therapy.TherapySessionRepository
import com.terapia.terasenior.supabase
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class SupabaseTherapySessionRepository : TherapySessionRepository {

    override suspend fun createSession(session: TherapySession): Result<String> = runCatching {
        val inserted = supabase.postgrest["therapy_sessions"].insert(session.toData()) {
            select()
        }.decodeSingle<TherapySessionDto>()
        inserted.id ?: throw Exception("Error al obtener ID de la sesión")
    }

    override suspend fun addExerciseToSession(exercise: TherapySessionExercise): Result<Unit> = runCatching {
        supabase.postgrest["therapy_session_exercises"].insert(exercise.toData())
    }

    override suspend fun updateSessionStatus(sessionId: String, status: String): Result<Unit> = runCatching {
        supabase.postgrest["therapy_sessions"].update(mapOf("status" to status)) {
            filter { eq("id", sessionId) }
        }
    }

    override fun getSessionsByTherapist(therapistId: String): Flow<Result<List<TherapySession>>> = flow {
        emit(runCatching {
            supabase.postgrest["therapy_sessions"]
                .select {
                    filter { eq("therapist_id", therapistId) }
                }
                .decodeList<TherapySessionDto>()
                .map { it.toDomain() }
        })
    }

    override suspend fun logAssistance(event: AssistanceEvent): Result<Unit> = runCatching {
        supabase.postgrest["session_assistance_events"].insert(mapOf(
            "session_id" to event.sessionId,
            "exercise_id" to event.exerciseId,
            "assistance_type" to event.type.name,
            "description" to event.description
        ))
    }

    override suspend fun logIncident(incident: SessionIncident): Result<Unit> = runCatching {
        supabase.postgrest["session_incidents"].insert(mapOf(
            "session_id" to incident.sessionId,
            "exercise_id" to incident.exerciseId,
            "incident_type" to incident.type.name,
            "description" to incident.description,
            "severity" to incident.severity
        ))
    }

    override suspend fun getSessionDetails(sessionId: String): Result<TherapySession?> = runCatching {
        supabase.postgrest["therapy_sessions"]
            .select { filter { eq("id", sessionId) } }
            .decodeSingleOrNull<TherapySessionDto>()
            ?.toDomain()
    }

    override suspend fun getExercisesForSession(sessionId: String): Result<List<TherapySessionExercise>> = runCatching {
        supabase.postgrest["therapy_session_exercises"]
            .select {
                filter { eq("session_id", sessionId) }
                order("position", io.github.jan.supabase.postgrest.query.Order.ASCENDING)
            }
            .decodeList<TherapySessionExerciseDto>()
            .map { it.toDomain() }
    }

    override suspend fun getTherapistSummary(therapistId: String): Result<Map<String, Int>> = runCatching {
        val sessions = supabase.postgrest["therapy_sessions"]
            .select { filter { eq("therapist_id", therapistId) } }
            .decodeList<TherapySessionDto>()
        
        mapOf(
            "total_sessions" to sessions.size,
            "completed_sessions" to sessions.count { it.status == "COMPLETED" },
            "pending_sessions" to sessions.count { it.status == "DRAFT" || it.status == "READY" }
        )
    }

    override fun getRecentSessions(therapistId: String, limit: Int): Flow<Result<List<TherapySession>>> = flow {
        emit(runCatching {
            supabase.postgrest["therapy_sessions"]
                .select {
                    filter { eq("therapist_id", therapistId) }
                    order("created_at", io.github.jan.supabase.postgrest.query.Order.DESCENDING)
                    limit(limit.toLong())
                }
                .decodeList<TherapySessionDto>()
                .map { it.toDomain() }
        })
    }
}
