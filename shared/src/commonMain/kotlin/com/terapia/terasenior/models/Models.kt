package com.terapia.terasenior.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

enum class UserRole {
    SUPER_ADMIN,
    ENTITY_ADMIN,
    THERAPIST
}

@Serializable
data class Profile(
    val id: String,
    val email: String,
    val role: UserRole,
    @SerialName("entity_id")
    val entityId: String? = null,
    @SerialName("full_name")
    val fullName: String? = null
)

@Serializable
data class Entity(
    val id: String,
    val name: String,
    @SerialName("created_at")
    val createdAt: String? = null
)

@Serializable
data class Patient(
    val id: String,
    @SerialName("entity_id")
    val entityId: String,
    @SerialName("full_name")
    val fullName: String,
    @SerialName("birth_date")
    val birthDate: String? = null
)