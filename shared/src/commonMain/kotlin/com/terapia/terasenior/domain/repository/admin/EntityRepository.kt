package com.terapia.terasenior.domain.repository.admin

import com.terapia.terasenior.domain.model.admin.Entity

interface EntityRepository {
    suspend fun getEntities(): Result<List<Entity>>
    suspend fun getEntityById(id: String): Result<Entity?>
    suspend fun createEntity(entity: Entity): Result<Unit>
    suspend fun updateEntity(entity: Entity): Result<Unit>
    suspend fun deleteEntity(entityId: String): Result<Unit>
    suspend fun hasDependentData(entityId: String): Result<Boolean>
    suspend fun deactivateEntityWithUsers(entityId: String): Result<Unit>
}
