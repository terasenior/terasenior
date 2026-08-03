package com.terapia.terasenior.data.repository.admin

import com.terapia.terasenior.data.model.admin.EntityDto
import com.terapia.terasenior.data.model.admin.toData
import com.terapia.terasenior.data.model.admin.toDomain
import com.terapia.terasenior.domain.model.admin.Entity
import com.terapia.terasenior.domain.repository.admin.EntityRepository
import com.terapia.terasenior.supabase
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns

class SupabaseEntityRepository : EntityRepository {

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

    override suspend fun createEntity(entity: Entity): Result<Unit> = runCatching {
        supabase.postgrest["entities"].insert(entity.toData())
    }

    override suspend fun updateEntity(entity: Entity): Result<Unit> = runCatching {
        // 1. Actualizar la entidad
        supabase.postgrest["entities"].update(entity.toData()) {
            filter {
                eq("id", entity.id)
            }
        }

        // 2. Si se desactiva, desactivar usuarios en cascada
        if (entity.status == "INACTIVE") {
            deactivateUsers(entity.id)
        }
    }

    override suspend fun deleteEntity(entityId: String): Result<Unit> = runCatching {
        supabase.postgrest["entities"].delete {
            filter {
                eq("id", entityId)
            }
        }
    }

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

    override suspend fun deactivateEntityWithUsers(entityId: String): Result<Unit> = runCatching {
        // Marcar entidad como Inactiva
        supabase.postgrest["entities"].update(mapOf("status" to "INACTIVE")) {
            filter {
                eq("id", entityId)
            }
        }
        // Desactivar usuarios
        deactivateUsers(entityId)
    }

    private suspend fun deactivateUsers(entityId: String) {
        supabase.postgrest["user_profiles"].update(mapOf("is_active" to false)) {
            filter {
                eq("entity_id", entityId)
            }
        }
    }
}
