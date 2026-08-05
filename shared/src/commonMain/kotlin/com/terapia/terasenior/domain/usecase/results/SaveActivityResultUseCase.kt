package com.terapia.terasenior.domain.usecase.results

import com.terapia.terasenior.domain.model.results.ActivityResult
import com.terapia.terasenior.domain.repository.results.ResultsRepository

class SaveActivityResultUseCase(
    private val repository: ResultsRepository
) {
    suspend operator fun invoke(result: ActivityResult): Result<Unit> {
        return repository.saveResult(result)
    }
}
