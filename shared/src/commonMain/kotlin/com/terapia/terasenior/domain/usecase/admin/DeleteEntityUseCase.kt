package com.terapia.terasenior.domain.usecase.admin

import com.terapia.terasenior.domain.repository.admin.EntityRepository

class DeleteEntityUseCase(
    private val repository: EntityRepository
) {
    suspend operator fun invoke(entityId: String): Result<Unit> {
        return repository.deleteEntity(entityId)
    }
}
