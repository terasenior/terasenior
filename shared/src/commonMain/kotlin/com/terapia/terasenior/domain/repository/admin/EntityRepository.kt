package com.terapia.terasenior.domain.repository.admin

import com.terapia.terasenior.domain.model.admin.Entity

interface EntityRepository {
    suspend fun getEntities(): Result<List<Entity>>
    suspend fun getEntityById(id: String): Result<Entity?>
    suspend fun createEntity(entity: Entity): Result<Unit>
    suspend fun updateEntity(entity: Entity): Result<Unit>
}
