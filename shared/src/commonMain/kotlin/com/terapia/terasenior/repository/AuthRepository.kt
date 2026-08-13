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

    suspend fun adminCreateUser(
        email: String,
        password: String,
        fullName: String,
        role: UserRole,
        entityId: String?,
        phone: String?,
        isActive: Boolean,
        centerName: String? = null
    ): Result<Unit> {
        return runCatching {
            // 1. Crear el usuario en Supabase Auth
            // Usamos signUpWith. Nota: Si la confirmación de email está activa, el usuario no aparecerá como "Confirmado"
            val authResponse = supabase.auth.signUpWith(Email) {
                this.email = email
                this.password = password
            }

            val newUserId = authResponse?.id ?: throw Exception("Supabase Auth no devolvió un ID de usuario. Verifica si el email ya existe o si el registro está deshabilitado.")

            // 2. Crear el objeto Profile
            val newProfile = Profile(
                id = newUserId,
                email = email,
                roleId = role.name,
                fullName = fullName,
                entityId = entityId,
                isActive = isActive,
                phone = phone,
                centerName = centerName
            )

            // 3. Insertar el objeto Profile en la tabla pública
            try {
                supabase.postgrest["user_profiles"].insert(newProfile)
            } catch (e: Exception) {
                throw Exception("Usuario autenticado correctamente (ID: $newUserId) pero falló la creación de su perfil clínico: ${e.message}")
            }
        }
    }

    suspend fun updateUserProfile(profile: Profile): Result<Unit> {
        return runCatching {
            supabase.postgrest["user_profiles"].update(profile) {
                filter { eq("id", profile.id) }
            }
        }
    }

    suspend fun deleteUser(userId: String): Result<Unit> {
        return runCatching {
            supabase.postgrest["user_profiles"].delete {
                filter { eq("id", userId) }
            }
        }
    }

    suspend fun changePassword(newPassword: String): Result<Unit> {
        return runCatching {
            // Nota: Supabase permite que el usuario logueado cambie su propia contraseña
            // Para cambiar la de OTROS, se requiere el Admin SDK (Service Role)
            supabase.auth.updateUser {
                password = newPassword
            }
        }
    }

    suspend fun logout(): Result<Unit> {
        return runCatching {
            supabase.auth.signOut()
        }
    }

    suspend fun resetPassword(email: String): Result<Unit> {
        return runCatching {
            supabase.auth.resetPasswordForEmail(email)
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
            // 1. Verificar si el usuario está activo
            if (!profile.isActive) {
                throw Exception("Tu cuenta de usuario está desactivada. Contacta con tu administrador.")
            }

            runCatching { supabase.postgrest.rpc("check_and_deactivate_expired_licenses") }

            if (profile.role == UserRole.SUPER_ADMIN) {
                recordLogin(profile.id)
                return@runCatching
            }

            val entityId = profile.entityId ?: throw Exception("Usuario sin centro asociado.")
            val entity = supabase.postgrest["entities"].select {
                filter { eq("id", entityId) }
            }.decodeSingleOrNull<Entity>() ?: throw Exception("No se encontró la información de tu centro.")

            if (entity.status != "ACTIVE") {
                val reason = if (entity.licenseExpiresAt != null && 
                    Instant.parse(entity.licenseExpiresAt) < kotlin.time.Clock.System.now()) {
                    "La licencia de tu centro expiró el ${entity.licenseExpiresAt.take(10)} y el acceso ha sido revocado automáticamente."
                } else {
                    "El acceso para tu centro está suspendido actualmente."
                }
                throw Exception(reason)
            }

            recordLogin(profile.id)
        }
    }

    private suspend fun recordLogin(userId: String) {
        runCatching {
            supabase.postgrest.rpc("record_user_login", mapOf("p_user_id" to userId))
        }
    }
}