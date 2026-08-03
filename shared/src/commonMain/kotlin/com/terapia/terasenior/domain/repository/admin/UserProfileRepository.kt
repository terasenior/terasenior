package com.terapia.terasenior.domain.repository.admin

import com.terapia.terasenior.domain.model.admin.UserProfile

interface UserProfileRepository {
    suspend fun getUserProfiles(entityId: String?): Result<List<UserProfile>>
    suspend fun getUserProfileById(id: String): Result<UserProfile?>
    suspend fun createUserProfile(profile: UserProfile): Result<Unit>
    suspend fun updateUserProfile(profile: UserProfile): Result<Unit>
}
