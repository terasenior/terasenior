package com.terapia.terasenior.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class UserRole {
    @SerialName("SUPER_ADMIN") SUPER_ADMIN,
    @SerialName("ADMIN_CENTRO") ADMIN_CENTRO,
    @SerialName("TERAPEUTA") TERAPEUTA,
    @SerialName("AUXILIAR") AUXILIAR;

    companion object {
        fun fromId(id: String): UserRole = entries.find { it.name == id } ?: AUXILIAR
    }
}

@Serializable
data class Profile(
    val id: String,
    val email: String,
    @SerialName("role_id")
    val roleId: String, // Usamos String para ser más robustos (v1.3.22)
    @SerialName("entity_id")
    val entityId: String? = null,
    @SerialName("full_name")
    val fullName: String? = null,
    @SerialName("is_active")
    val isActive: Boolean = true,
    @SerialName("last_login_at")
    val lastLoginAt: String? = null,
    @SerialName("center_name")
    val centerName: String? = null,
    val phone: String? = null
) {
    val role: UserRole get() = UserRole.fromId(roleId)
}

@Serializable
data class Entity(
    val id: String,
    val name: String,
    @SerialName("status")
    val status: String = "ACTIVE",
    @SerialName("license_expires_at")
    val licenseExpiresAt: String? = null,
    @SerialName("logo_url")
    val logoUrl: String? = null,
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