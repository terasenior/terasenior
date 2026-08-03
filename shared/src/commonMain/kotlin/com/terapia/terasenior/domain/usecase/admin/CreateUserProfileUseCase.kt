package com.terapia.terasenior.domain.usecase.admin

import com.terapia.terasenior.domain.model.admin.UserProfile
import com.terapia.terasenior.domain.repository.admin.UserProfileRepository

class CreateUserProfileUseCase(
    private val repository: UserProfileRepository
) {
    private val emailRegex = "^[A-Za-z0-9+_.-]+@(.+)$".toRegex()

    suspend operator fun invoke(profile: UserProfile): Result<Unit> {
        if (!profile.email.matches(emailRegex)) {
            return Result.failure(Exception("El formato del correo electrónico no es válido"))
        }
        
        if (profile.fullName.isBlank()) {
            return Result.failure(Exception("El nombre completo es obligatorio"))
        }

        return repository.createUserProfile(profile)
    }
}
