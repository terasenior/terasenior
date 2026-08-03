package com.terapia.terasenior.domain.usecase.admin

import com.terapia.terasenior.domain.model.admin.Entity
import com.terapia.terasenior.domain.repository.admin.EntityRepository

class UpdateEntityUseCase(
    private val repository: EntityRepository
) {
    suspend operator fun invoke(entity: Entity): Result<Unit> {
        if (entity.name.isBlank()) {
            return Result.failure(Exception("El nombre del centro no puede estar vacío"))
        }
        return repository.updateEntity(entity)
    }
}
