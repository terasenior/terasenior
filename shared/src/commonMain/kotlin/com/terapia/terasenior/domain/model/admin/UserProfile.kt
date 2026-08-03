package com.terapia.terasenior.domain.model.admin

import com.terapia.terasenior.models.UserRole

data class UserProfile(
    val id: String,
    val entityId: String?,
    val role: UserRole,
    val fullName: String,
    val email: String,
    val phone: String?,
    val isActive: Boolean
)
