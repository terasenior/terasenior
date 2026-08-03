package com.terapia.terasenior.domain.model.admin

data class UserProfile(
    val id: String,
    val entityId: String?,
    val role: UserRole,
    val fullName: String,
    val email: String,
    val phone: String?,
    val isActive: Boolean
)
