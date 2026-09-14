package com.terapia.terasenior.data.remote.admin

import com.terapia.terasenior.data.model.admin.UserProfileDto
import com.terapia.terasenior.domain.model.admin.UserProfile
import com.terapia.terasenior.domain.model.admin.Entity
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.jsonObject

/** Full replacement of ordinary fields only; the row ID belongs in the filter. */
@Serializable
data class UserProfileOrdinaryUpdate(
    @SerialName("full_name") val fullName: String?,
    val phone: String?
) {
    fun toPostgrestPayload(): JsonObject = Json.encodeToJsonElement(this).jsonObject
}

@Serializable
data class EntityOrdinaryUpdate(
    val name: String,
    val cif: String,
    val address: String?,
    @SerialName("logo_url") val logoUrl: String?
) {
    fun toPostgrestPayload(): JsonObject = Json.encodeToJsonElement(this).jsonObject
}

/** Compatibility guards: reject mixed edits before writing the ordinary fields.
 * These are not authorization controls: RLS/grants must enforce permissions remotely.
 */
internal fun hasPrivilegedUserChanges(current: UserProfileDto, requested: UserProfile): Boolean =
    current.roleId != requested.role.name || current.entityId != requested.entityId ||
        current.isActive != requested.isActive || current.email != requested.email ||
        current.centerName != requested.centerName

internal fun hasPrivilegedEntityChanges(current: Entity, requested: Entity): Boolean =
    current.status != requested.status.uppercase() || current.licenseExpiresAt != requested.licenseExpiresAt
