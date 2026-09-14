package com.terapia.terasenior.data.repository.admin

import com.terapia.terasenior.data.model.admin.UserProfileDto
import com.terapia.terasenior.data.remote.admin.*
import com.terapia.terasenior.data.model.admin.toDomain
import com.terapia.terasenior.domain.model.admin.UserProfile
import com.terapia.terasenior.domain.repository.admin.UserProfileRepository
import com.terapia.terasenior.supabase
import io.github.jan.supabase.postgrest.postgrest

class SupabaseUserProfileRepository(
    private val adminRemote: AdminRemoteDataSource = EdgeAdminRemoteDataSource()
) : UserProfileRepository {

    override suspend fun getUserProfiles(entityId: String?): Result<List<UserProfile>> = runCatching {
        supabase.postgrest["user_profiles"]
            .select {
                filter {
                    if (entityId != null) {
                        eq("entity_id", entityId)
                    }
                }
            }
            .decodeList<UserProfileDto>()
            .map { it.toDomain() }
    }

    override suspend fun getUserProfileById(id: String): Result<UserProfile?> = runCatching {
        supabase.postgrest["user_profiles"]
            .select {
                filter {
                    eq("id", id)
                }
            }
            .decodeSingleOrNull<UserProfileDto>()
            ?.toDomain()
    }

    // This legacy API has no password; it cannot safely create an Auth account.
    override suspend fun createUserProfile(profile: UserProfile): Result<Unit> =
        Result.failure(AdminBackendUnavailableException("crear perfil de usuario; usar alta administrativa completa"))

    override suspend fun updateUserProfile(profile: UserProfile): Result<Unit> = runCatching {
        val current = supabase.postgrest["user_profiles"].select {
            filter { eq("id", profile.id) }
        }.decodeSingleOrNull<UserProfileDto>()
            ?: error("No se pudo leer el perfil para comprobar los campos editados.")

        if (hasPrivilegedUserChanges(current, profile)) {
            adminRemote.updateUser(AdminUpdateUserRequest(
                targetUserId = profile.id, email = profile.email, fullName = profile.fullName,
                phone = profile.phone, roleId = profile.role.name,
                entityId = profile.entityId?.let { NullableStringUpdate.set(it) } ?: NullableStringUpdate.clear(),
                isActive = profile.isActive, centerName = profile.centerName,
                clearPhone = profile.phone == null, clearCenterName = profile.centerName == null
            )).toActionResult().getOrThrow()
        } else {
            updateOrdinaryProfile(profile.id, UserProfileOrdinaryUpdate(profile.fullName, profile.phone)).getOrThrow()
        }
    }

    suspend fun updateOrdinaryProfile(userId: String, payload: UserProfileOrdinaryUpdate): Result<Unit> = runCatching {
        val updated = supabase.postgrest["user_profiles"].update(payload.toPostgrestPayload()) {
            filter { eq("id", userId) }
            select(io.github.jan.supabase.postgrest.query.Columns.list("id"))
        }.decodeList<kotlinx.serialization.json.JsonObject>()
        check(updated.size == 1) { "No se pudo actualizar el perfil: comprueba los permisos de edición." }
    }
}
