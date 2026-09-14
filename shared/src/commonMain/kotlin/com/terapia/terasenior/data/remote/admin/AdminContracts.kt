package com.terapia.terasenior.data.remote.admin

import kotlinx.serialization.Serializable

/** Wire names are camelCase, independently of the PostgREST column names. */
@Serializable
data class AdminCreateUserRequest(
    val email: String,
    val password: String,
    val fullName: String,
    val roleId: String,
    val entityId: String?,
    val phone: String?,
    val isActive: Boolean,
    val centerName: String?
)

@Serializable
enum class NullableStringAction { SET, CLEAR }

/** null/omitted wrapper = keep; CLEAR = SQL null; SET = the supplied value.
 * CLEAR remains explicit even with explicitNulls=false or encodeDefaults=false.
 */
@Serializable
data class NullableStringUpdate(val action: NullableStringAction, val value: String? = null) {
    init {
        require((action == NullableStringAction.SET) == (value != null)) {
            "SET requiere un valor y CLEAR no admite valor."
        }
    }

    companion object {
        fun set(value: String) = NullableStringUpdate(NullableStringAction.SET, value)
        fun clear() = NullableStringUpdate(NullableStringAction.CLEAR)
    }
}

@Serializable
data class AdminUpdateUserRequest(
    val targetUserId: String,
    val email: String? = null,
    val fullName: String? = null,
    val phone: String? = null,
    val roleId: String? = null,
    val entityId: NullableStringUpdate? = null,
    val isActive: Boolean? = null,
    val centerName: String? = null,
    // Nullable strings otherwise mean keep. Clearing is explicit, also for these fields.
    val clearPhone: Boolean = false,
    val clearCenterName: Boolean = false
) {
    init {
        require(!clearPhone || phone == null) { "No se puede asignar y borrar phone a la vez." }
        require(!clearCenterName || centerName == null) { "No se puede asignar y borrar centerName a la vez." }
    }
}

@Serializable data class AdminDeleteUserRequest(val targetUserId: String)
@Serializable data class AdminChangePasswordRequest(val targetUserId: String, val newPassword: String)

@Serializable
data class AdminCreateEntityRequest(
    val name: String,
    val cif: String,
    val address: String?,
    val licenseExpiresAt: String?,
    val logoUrl: String?
)

/** Ordinary partial edit: never contains status or licenseExpiresAt. */
@Serializable
data class AdminUpdateEntityRequest(
    val entityId: String,
    val name: String? = null,
    val cif: String? = null,
    val address: String? = null,
    val logoUrl: String? = null,
    val clearAddress: Boolean = false,
    val clearLogoUrl: Boolean = false
) {
    init {
        require(!clearAddress || address == null) { "No se puede asignar y borrar address a la vez." }
        require(!clearLogoUrl || logoUrl == null) { "No se puede asignar y borrar logoUrl a la vez." }
    }
}

@Serializable
data class AdminSetEntityStatusRequest(val entityId: String, val status: String) {
    init { require(status == "ACTIVE" || status == "INACTIVE") { "Estado de centro no válido." } }
}

/** Dedicated operation: null explicitly removes expiry; it never means keep. */
@Serializable data class AdminSetEntityLicenseRequest(val entityId: String, val licenseExpiresAt: String?)
@Serializable data class AdminDeleteEntityRequest(val entityId: String)

@Serializable
data class AdminActionResponse(val success: Boolean, val message: String? = null, val code: String? = null)

@Serializable data class AdminCreatedUser(val userId: String)
@Serializable data class AdminCreatedEntity(val entityId: String)

@Serializable
data class AdminCreateUserResponse(
    val success: Boolean, val data: AdminCreatedUser? = null,
    val message: String? = null, val code: String? = null
)

@Serializable
data class AdminCreateEntityResponse(
    val success: Boolean, val data: AdminCreatedEntity? = null,
    val message: String? = null, val code: String? = null
)
