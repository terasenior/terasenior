package com.terapia.terasenior.data.repository.admin

import com.terapia.terasenior.data.model.admin.EntityDto
import com.terapia.terasenior.data.remote.admin.*
import com.terapia.terasenior.data.model.admin.toDomain
import com.terapia.terasenior.domain.model.admin.Entity
import com.terapia.terasenior.domain.repository.admin.EntityRepository
import com.terapia.terasenior.supabase
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns

class SupabaseEntityRepository(
    private val adminRemote: AdminRemoteDataSource = EdgeAdminRemoteDataSource()
) : EntityRepository {

    override suspend fun getEntities(): Result<List<Entity>> = runCatching {
        supabase.postgrest["entities"]
            .select()
            .decodeList<EntityDto>()
            .map { it.toDomain() }
    }

    override suspend fun getEntityById(id: String): Result<Entity?> = runCatching {
        supabase.postgrest["entities"]
            .select {
                filter {
                    eq("id", id)
                }
            }
            .decodeSingleOrNull<EntityDto>()
            ?.toDomain()
    }

    override suspend fun createEntity(entity: Entity): Result<Unit> {
        if (entity.status.uppercase() != "ACTIVE") {
            return Result.failure(AdminBackendUnavailableException("crear centro con estado personalizado"))
        }
        return adminRemote.createEntity(AdminCreateEntityRequest(
            entity.name, entity.cif, entity.address, entity.licenseExpiresAt, entity.logoUrl
        )).toCreatedEntityResult()
    }

    override suspend fun updateEntity(entity: Entity): Result<Unit> = runCatching {
        val current = getEntityById(entity.id).getOrThrow()
            ?: error("No se pudo leer el centro para comprobar los campos editados.")
        if (hasPrivilegedEntityChanges(current, entity)) {
            // Do not partially save a mixed form, or split it into non-atomic writes.
            throw AdminBackendUnavailableException("editar estado o licencia del centro")
        }
        updateOrdinaryEntity(entity.id, EntityOrdinaryUpdate(
            entity.name, entity.cif, entity.address, entity.logoUrl
        )).getOrThrow()
    }

    suspend fun updateOrdinaryEntity(entityId: String, payload: EntityOrdinaryUpdate): Result<Unit> = runCatching {
        val updated = supabase.postgrest["entities"].update(payload.toPostgrestPayload()) {
            filter { eq("id", entityId) }
            select(Columns.list("id"))
        }.decodeList<kotlinx.serialization.json.JsonObject>()
        check(updated.size == 1) { "No se pudo actualizar el centro: comprueba los permisos de edición." }
    }

    override suspend fun deleteEntity(entityId: String): Result<Unit> =
        adminRemote.deleteEntity(AdminDeleteEntityRequest(entityId)).toActionResult()

    override suspend fun hasDependentData(entityId: String): Result<Boolean> = runCatching {
        // Comprobar usuarios
        val usersResponse = supabase.postgrest["user_profiles"].select(Columns.list("id")) {
            filter {
                eq("entity_id", entityId)
            }
            limit(1)
        }.decodeList<Map<String, String>>()
        
        if (usersResponse.isNotEmpty()) return@runCatching true

        // Comprobar pacientes (asumiendo que existe la tabla)
        // val patientsResponse = supabase.postgrest["patients"].select(Columns.list("id")) { ... }
        
        false
    }

    // The server must own the state transition and any associated user deactivation.
    override suspend fun deactivateEntityWithUsers(entityId: String): Result<Unit> =
        adminRemote.setEntityStatus(AdminSetEntityStatusRequest(entityId, "INACTIVE")).toActionResult()
}
