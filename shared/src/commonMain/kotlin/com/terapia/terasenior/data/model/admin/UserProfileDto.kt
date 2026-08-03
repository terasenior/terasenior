package com.terapia.terasenior.data.model.admin

import com.terapia.terasenior.domain.model.admin.UserProfile
import com.terapia.terasenior.domain.model.admin.UserRole
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UserProfileDto(
    @SerialName("id") val id: String,
    @SerialName("entity_id") val entityId: String? = null,
    @SerialName("role_id") val roleId: String,
    @SerialName("full_name") val fullName: String,
    @SerialName("email") val email: String,
    @SerialName("phone") val phone: String? = null,
    @SerialName("is_active") val isActive: Boolean = true
)

fun UserProfileDto.toDomain() = UserProfile(
    id = id,
    entityId = entityId,
    role = UserRole.fromId(roleId),
    fullName = fullName,
    email = email,
    phone = phone,
    isActive = isActive
)

fun UserProfile.toData() = UserProfileDto(
    id = id,
    entityId = entityId,
    roleId = role.id,
    fullName = fullName,
    email = email,
    phone = phone,
    isActive = isActive
)
