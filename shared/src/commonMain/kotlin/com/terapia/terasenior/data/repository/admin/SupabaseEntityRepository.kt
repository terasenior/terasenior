package com.terapia.terasenior.data.repository.admin

import com.terapia.terasenior.data.model.admin.EntityDto
import com.terapia.terasenior.data.model.admin.toData
import com.terapia.terasenior.data.model.admin.toDomain
import com.terapia.terasenior.domain.model.admin.Entity
import com.terapia.terasenior.domain.repository.admin.EntityRepository
import com.terapia.terasenior.supabase
import io.github.jan.supabase.postgrest.postgrest

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
        supabase.postgrest["entities"].update(entity.toData()) {
            filter {
                eq("id", entity.id)
            }
        }
    }
}
