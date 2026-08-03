package com.terapia.terasenior.domain.usecase.admin

import com.terapia.terasenior.domain.model.admin.Entity
import com.terapia.terasenior.domain.repository.admin.EntityRepository

class GetEntitiesUseCase(
    private val repository: EntityRepository
) {
    suspend operator fun invoke(): Result<List<Entity>> {
        return repository.getEntities()
    }
}
