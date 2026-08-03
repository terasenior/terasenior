package com.terapia.terasenior.repository

import com.terapia.terasenior.models.Entity
import com.terapia.terasenior.models.Profile
import com.terapia.terasenior.models.UserRole
import com.terapia.terasenior.supabase
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.rpc
import kotlinx.datetime.Instant

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

    @OptIn(kotlin.time.ExperimentalTime::class)
    suspend fun checkLicenseAndRecordLogin(profile: Profile): Result<Unit> {
        return runCatching {
            if (profile.role == UserRole.SUPER_ADMIN) {
                recordLogin(profile.id)
                return@runCatching
            }

            val entityId = profile.entityId ?: throw Exception("Usuario sin centro asociado. Contacte con soporte.")

            val entity = supabase.postgrest["entities"].select {
                filter { eq("id", entityId) }
            }.decodeSingleOrNull<Entity>() ?: throw Exception("No se encontró la información de tu centro.")

            if (entity.status != "ACTIVE") {
                throw Exception("El acceso para tu centro está suspendido actualmente.")
            }

            entity.licenseExpiresAt?.let { expiresAtStr ->
                val expirationDate = Instant.parse(expiresAtStr)
                val now = kotlin.time.Clock.System.now()
                
                if (now > expirationDate) {
                    val formattedDate = expiresAtStr.take(10)
                    throw Exception("La licencia de tu centro expiró el $formattedDate. Ponte en contacto con el administrador para renovarla.")
                }
            } ?: throw Exception("Tu centro no tiene una licencia configurada. Contacte con soporte.")

            recordLogin(profile.id)
        }
    }

    private suspend fun recordLogin(userId: String) {
        runCatching {
            supabase.postgrest.rpc("record_user_login", mapOf("p_user_id" to userId))
        }
    }
}