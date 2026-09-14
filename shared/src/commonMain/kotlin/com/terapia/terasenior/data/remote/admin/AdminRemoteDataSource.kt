package com.terapia.terasenior.data.remote.admin

/** Future privileged endpoints. Transport/authorization must live on the server.
 * Implementations must treat success=false as failure, not as a successful write.
 */
interface AdminRemoteDataSource {
    suspend fun createUser(request: AdminCreateUserRequest): Result<AdminCreateUserResponse>
    suspend fun updateUser(request: AdminUpdateUserRequest): Result<AdminActionResponse>
    suspend fun deleteUser(request: AdminDeleteUserRequest): Result<AdminActionResponse>
    suspend fun changeUserPassword(request: AdminChangePasswordRequest): Result<AdminActionResponse>
    suspend fun createEntity(request: AdminCreateEntityRequest): Result<AdminCreateEntityResponse>
    suspend fun setEntityStatus(request: AdminSetEntityStatusRequest): Result<AdminActionResponse>
    suspend fun setEntityLicense(request: AdminSetEntityLicenseRequest): Result<AdminActionResponse>
    suspend fun deleteEntity(request: AdminDeleteEntityRequest): Result<AdminActionResponse>
}

class AdminBackendUnavailableException(operation: String) : IllegalStateException(
    "La operación '$operation' necesita el backend administrativo seguro (Edge Function pendiente). No se ha guardado ningún cambio."
)

/** No network and no simulated successes. Existing admin-change-password keeps its own transport. */
class UnavailableAdminRemoteDataSource : AdminRemoteDataSource {
    private fun <T> unavailable(operation: String): Result<T> = Result.failure(AdminBackendUnavailableException(operation))
    override suspend fun createUser(request: AdminCreateUserRequest): Result<AdminCreateUserResponse> = unavailable("crear usuario")
    override suspend fun updateUser(request: AdminUpdateUserRequest): Result<AdminActionResponse> = unavailable("editar campos administrativos del usuario")
    override suspend fun deleteUser(request: AdminDeleteUserRequest): Result<AdminActionResponse> = unavailable("eliminar usuario")
    override suspend fun changeUserPassword(request: AdminChangePasswordRequest): Result<AdminActionResponse> = unavailable("cambiar contraseña")
    override suspend fun createEntity(request: AdminCreateEntityRequest): Result<AdminCreateEntityResponse> = unavailable("crear centro")
    override suspend fun setEntityStatus(request: AdminSetEntityStatusRequest): Result<AdminActionResponse> = unavailable("cambiar estado del centro")
    override suspend fun setEntityLicense(request: AdminSetEntityLicenseRequest): Result<AdminActionResponse> = unavailable("cambiar licencia del centro")
    override suspend fun deleteEntity(request: AdminDeleteEntityRequest): Result<AdminActionResponse> = unavailable("eliminar centro")
}

internal fun Result<AdminActionResponse>.toActionResult(): Result<Unit> = mapCatching {
    check(it.success) { it.message ?: "El backend administrativo rechazó la operación." }
}

internal fun Result<AdminCreateUserResponse>.toCreatedUserResult(): Result<Unit> = mapCatching {
    check(it.success && !it.data?.userId.isNullOrBlank()) { it.message ?: "El backend no confirmó la creación del usuario." }
}

internal fun Result<AdminCreateEntityResponse>.toCreatedEntityResult(): Result<Unit> = mapCatching {
    check(it.success && !it.data?.entityId.isNullOrBlank()) { it.message ?: "El backend no confirmó la creación del centro." }
}
