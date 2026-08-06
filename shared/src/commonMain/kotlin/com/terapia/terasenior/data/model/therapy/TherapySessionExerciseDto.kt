package com.terapia.terasenior.data.model.therapy

import com.terapia.terasenior.domain.model.therapy.TherapySessionExercise
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TherapySessionExerciseDto(
    @SerialName("id") val id: String? = null,
    @SerialName("session_id") val sessionId: String,
    @SerialName("exercise_type") val exerciseType: String,
    @SerialName("level") val level: Int,
    @SerialName("position") val position: Int,
    @SerialName("configuration") val configuration: Map<String, String> = emptyMap()
)

fun TherapySessionExerciseDto.toDomain() = TherapySessionExercise(
    id = id.orEmpty(),
    sessionId = sessionId,
    exerciseType = exerciseType,
    level = level,
    position = position,
    configuration = configuration
)

fun TherapySessionExercise.toData() = TherapySessionExerciseDto(
    id = id.ifEmpty { null },
    sessionId = sessionId,
    exerciseType = exerciseType,
    level = level,
    position = position,
    configuration = configuration
)
