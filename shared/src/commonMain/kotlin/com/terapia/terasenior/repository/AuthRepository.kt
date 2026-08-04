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
import kotlinx.datetime.Clock

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

    suspend fun checkLicenseAndRecordLogin(profile: Profile): Result<Unit> {
        return runCatching {
            // 1. Ejecutar limpieza preventiva de licencias expiradas en el servidor
            runCatching { supabase.postgrest.rpc("check_and_deactivate_expired_licenses") }

            // 2. Si es SUPER_ADMIN, acceso perpetuo
            if (profile.role == UserRole.SUPER_ADMIN) {
                recordLogin(profile.id)
                return@runCatching
            }

            // 3. Obtener datos actualizados de la entidad (tras la limpieza)
            val entityId = profile.entityId ?: throw Exception("Usuario sin centro asociado.")
            val entity = supabase.postgrest["entities"].select {
                filter { eq("id", entityId) }
            }.decodeSingleOrNull<Entity>() ?: throw Exception("No se encontró la información de tu centro.")

            // 4. Validar Estado (ya estará INACTIVE si la limpieza detectó expiración)
            if (entity.status != "ACTIVE") {
                val reason = if (entity.licenseExpiresAt != null && 
                    Instant.parse(entity.licenseExpiresAt) < Clock.System.now()) {
                    "La licencia de tu centro expiró el ${entity.licenseExpiresAt.take(10)} y el acceso ha sido revocado automáticamente."
                } else {
                    "El acceso para tu centro está suspendido actualmente."
                }
                throw Exception(reason)
            }

            // 5. Registrar login
            recordLogin(profile.id)
        }
    }

    private suspend fun recordLogin(userId: String) {
        runCatching {
            supabase.postgrest.rpc("record_user_login", mapOf("p_user_id" to userId))
        }
    }
}