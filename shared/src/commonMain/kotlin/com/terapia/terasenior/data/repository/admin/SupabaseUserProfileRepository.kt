package com.terapia.terasenior.data.repository.admin

import com.terapia.terasenior.data.model.admin.UserProfileDto
import com.terapia.terasenior.data.model.admin.toData
import com.terapia.terasenior.data.model.admin.toDomain
import com.terapia.terasenior.domain.model.admin.UserProfile
import com.terapia.terasenior.domain.repository.admin.UserProfileRepository
import com.terapia.terasenior.supabase
import io.github.jan.supabase.postgrest.postgrest

class SupabaseUserProfileRepository : UserProfileRepository {

    override suspend fun getUserProfiles(entityId: String?): Result<List<UserProfile>> = runCatching {
        supabase.postgrest["user_profiles"]
            .select {
                filter {
                    if (entityId != null) {
                        eq("entity_id", entityId)
                    }
                }
            }
            .decodeList<UserProfileDto>()
            .map { it.toDomain() }
    }

    override suspend fun getUserProfileById(id: String): Result<UserProfile?> = runCatching {
        supabase.postgrest["user_profiles"]
            .select {
                filter {
                    eq("id", id)
                }
            }
            .decodeSingleOrNull<UserProfileDto>()
            ?.toDomain()
    }

    override suspend fun createUserProfile(profile: UserProfile): Result<Unit> = runCatching {
        supabase.postgrest["user_profiles"].insert(profile.toData())
    }

    override suspend fun updateUserProfile(profile: UserProfile): Result<Unit> = runCatching {
        supabase.postgrest["user_profiles"].update(profile.toData()) {
            filter {
                eq("id", profile.id)
            }
        }
    }
}
