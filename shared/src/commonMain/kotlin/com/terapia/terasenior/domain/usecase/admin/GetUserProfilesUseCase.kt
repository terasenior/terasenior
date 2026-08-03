package com.terapia.terasenior.domain.usecase.admin

import com.terapia.terasenior.domain.model.admin.UserProfile
import com.terapia.terasenior.domain.repository.admin.UserProfileRepository

class GetUserProfilesUseCase(
    private val repository: UserProfileRepository
) {
    suspend operator fun invoke(entityId: String? = null): Result<List<UserProfile>> {
        return repository.getUserProfiles(entityId)
    }
}
