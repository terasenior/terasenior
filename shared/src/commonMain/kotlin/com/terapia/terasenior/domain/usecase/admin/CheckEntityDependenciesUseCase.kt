package com.terapia.terasenior.domain.usecase.admin

import com.terapia.terasenior.domain.repository.admin.EntityRepository

class CheckEntityDependenciesUseCase(
    private val repository: EntityRepository
) {
    suspend operator fun invoke(entityId: String): Result<Boolean> {
        return repository.hasDependentData(entityId)
    }
}
