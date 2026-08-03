package com.terapia.terasenior.repository

import com.terapia.terasenior.models.Profile
import com.terapia.terasenior.supabase
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.postgrest.postgrest

class AuthRepository {

    suspend fun login(userEmail: String, userPassword: String): Result<Unit> {
        return runCatching {
            supabase.auth.signInWith(Email) {
                email = userEmail
                password = userPassword
            }
        }
    }

    suspend fun register(userEmail: String, userPassword: String): Result<Unit> {
        return runCatching {
            supabase.auth.signUpWith(Email) {
                email = userEmail
                password = userPassword
            }
        }
    }

    suspend fun getCurrentProfile(): Result<Profile?> {
        return runCatching {
            val user = supabase.auth.currentUserOrNull()
            if (user != null) {
                supabase.postgrest["user_profiles"].select {
                    filter {
                        eq("id", user.id)
                    }
                }.decodeSingleOrNull<Profile>()
            } else {
                null
            }
        }
    }
}