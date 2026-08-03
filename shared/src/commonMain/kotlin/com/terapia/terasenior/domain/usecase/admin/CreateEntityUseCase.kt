package com.terapia.terasenior.domain.usecase.admin

import com.terapia.terasenior.domain.model.admin.Entity
import com.terapia.terasenior.domain.repository.admin.EntityRepository

class CreateEntityUseCase(
    private val repository: EntityRepository
) {
    suspend operator fun invoke(entity: Entity): Result<Unit> {
        if (entity.name.isBlank()) {
            return Result.failure(Exception("El nombre del centro no puede estar vacío"))
        }
        if (entity.cif.length < 5) {
            return Result.failure(Exception("El CIF proporcionado no es válido"))
        }
        
        return repository.createEntity(entity)
    }
}
