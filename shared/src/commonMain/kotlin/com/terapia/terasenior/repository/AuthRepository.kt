package com.terapia.terasenior.repository

import com.terapia.terasenior.models.Entity
import com.terapia.terasenior.models.Profile
import com.terapia.terasenior.models.UserRole
import com.terapia.terasenior.supabase
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.rpc
import kotlinx.datetime.*
import kotlinx.datetime.Clock as DateClock
import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import com.terapia.terasenior.data.remote.admin.*
import com.terapia.terasenior.data.repository.admin.SupabaseUserProfileRepository
import com.terapia.terasenior.domain.model.admin.UserProfile

class AuthRepository(
    private val adminRemote: AdminRemoteDataSource = EdgeAdminRemoteDataSource()
) {

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
        return adminRemote.createUser(AdminCreateUserRequest(
            email, password, fullName, role.name, entityId, phone, isActive, centerName
        )).toCreatedUserResult()
    }

    suspend fun updateUserProfile(profile: Profile): Result<Unit> =
        SupabaseUserProfileRepository(adminRemote).updateUserProfile(UserProfile(
            id = profile.id, email = profile.email, fullName = profile.fullName.orEmpty(),
            role = profile.role, entityId = profile.entityId, phone = profile.phone,
            isActive = profile.isActive, centerName = profile.centerName
        ))

    suspend fun deleteUser(userId: String): Result<Unit> =
        adminRemote.deleteUser(AdminDeleteUserRequest(userId)).toActionResult()

    suspend fun adminChangePassword(targetUserId: String, newPassword: String): Result<Unit> {
        // No fallback a auth.updateUser: modificaría la cuenta del administrador.
        return try {
            val session = supabase.auth.currentSessionOrNull()
                ?: return Result.failure(IllegalStateException("Inicia sesión de nuevo."))
            if (targetUserId.isBlank() || targetUserId == session.user?.id) {
                return Result.failure(IllegalArgumentException("Selecciona otra cuenta de usuario."))
            }
            val client = HttpClient {
                followRedirects = false
                install(HttpTimeout) { requestTimeoutMillis = 30_000 }
            }
            try {
                val response = client.post("${supabase.supabaseHttpUrl}/functions/v1/admin-change-password") {
                    bearerAuth(session.accessToken)
                    header("apikey", supabase.supabaseKey)
                    contentType(ContentType.Application.Json)
                    setBody(Json.encodeToString(AdminChangePasswordRequest(targetUserId, newPassword)))
                }
                val message = when (response.status.value) {
                    204 -> null
                    400, 422 -> "Revisa la contraseña: debe cumplir la política de seguridad del servicio."
                    401 -> "La sesión ha caducado. Inicia sesión de nuevo."
                    403 -> "No tienes permiso para cambiar la contraseña de esta cuenta."
                    404, 503 -> "El cambio administrativo de contraseña no está disponible."
                    429 -> "Demasiados intentos. Inténtalo más tarde."
                    else -> "No se pudo confirmar el cambio de contraseña."
                }
                if (message == null) Result.success(Unit)
                else Result.failure(IllegalStateException(message))
            } finally {
                client.close()
            }
        } catch (error: CancellationException) {
            throw error
        } catch (_: Exception) {
            // No propagar respuestas, cabeceras o excepciones que puedan contener credenciales.
            Result.failure(IllegalStateException("No se pudo confirmar el cambio. Comprueba la conexión antes de reintentarlo."))
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

            if (profile.role == UserRole.SUPER_ADMIN) {
                recordLogin()
                return@runCatching
            }

            val entityId = profile.entityId ?: throw Exception("Usuario sin centro asociado.")
            val entity = supabase.postgrest["entities"].select {
                filter { eq("id", entityId) }
            }.decodeSingleOrNull<Entity>() ?: throw Exception("No se encontró la información de tu centro.")

            if (entity.status != "ACTIVE") {
                val now = DateClock.System.now()
                val reason = if (entity.licenseExpiresAt != null && 
                    Instant.parse(entity.licenseExpiresAt) < now) {
                    "La licencia de tu centro expiró el ${entity.licenseExpiresAt.take(10)} y el acceso ha sido revocado automáticamente."
                } else {
                    "El acceso para tu centro está suspendido actualmente."
                }
                throw Exception(reason)
            }

            recordLogin()
        }
    }

    private suspend fun recordLogin() {
        runCatching {
            supabase.postgrest.rpc("record_user_login")
        }
    }
}
