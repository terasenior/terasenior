package com.terapia.terasenior.data.remote.admin

import com.terapia.terasenior.supabase
import io.github.jan.supabase.auth.auth
import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.request.*
import io.ktor.client.statement.bodyAsText
import io.ktor.http.*
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.*
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/** The remaining administrative operations continue to fail explicitly. */
class EdgeAdminRemoteDataSource : AdminRemoteDataSource by UnavailableAdminRemoteDataSource() {
    override suspend fun createUser(request: AdminCreateUserRequest): Result<AdminCreateUserResponse> = runSafely {
        val wire = json.encodeToJsonElement(request).jsonObject
        json.decodeFromJsonElement<AdminCreateUserResponse>(call("admin-create-user",
            JsonObject(wire.filterKeys { it != "password" }), request.password))
    }

    override suspend fun createEntity(request: AdminCreateEntityRequest): Result<AdminCreateEntityResponse> = runSafely {
        val expiry = request.licenseExpiresAt?.trim()?.takeIf { it.isNotEmpty() }?.let {
            try {
                if (it.length == 10) LocalDate.parse(it).toString() + "T23:59:59Z" else it
            } catch (_: IllegalArgumentException) {
                throw AdminClientException("La fecha de licencia debe ser válida y tener formato AAAA-MM-DD.")
            }
        }
        json.decodeFromJsonElement<AdminCreateEntityResponse>(call("admin-create-entity",
            json.encodeToJsonElement(request.copy(licenseExpiresAt = expiry)).jsonObject))
    }

    override suspend fun updateUser(request: AdminUpdateUserRequest): Result<AdminActionResponse> = runSafely {
        json.decodeFromJsonElement<AdminActionResponse>(call("admin-update-user", json.encodeToJsonElement(request).jsonObject))
    }

    private suspend fun <T> runSafely(action: suspend () -> T): Result<T> = try {
        Result.success(action())
    } catch (cancelled: CancellationException) { throw cancelled }
    catch (error: AdminClientException) { Result.failure(error) }
    catch (_: Exception) { Result.failure(AdminClientException("No se pudo confirmar la operación. Repite la misma solicitud para consultar su resultado.")) }

    @OptIn(ExperimentalUuidApi::class)
    private suspend fun call(operation: String, body: JsonObject, password: String? = null): JsonObject = mutex.withLock {
        val session = supabase.auth.currentSessionOrNull() ?: throw AdminClientException("Inicia sesión de nuevo.")
        val actor = session.user?.id ?: throw AdminClientException("Inicia sesión de nuevo.")
        val slot = "admin-pending-$actor-$operation"
        val saved = try {
            AdminPendingStore.read(slot)?.let { json.decodeFromString<PendingAdminRequest>(it) }
        } catch (_: Exception) {
            throw AdminClientException("El navegador no permite recuperar la solicitud. Habilita el almacenamiento del sitio.")
        }
        val pending = saved ?: PendingAdminRequest(Uuid.random().toString(), body)
        if (saved == null) try {
            AdminPendingStore.write(slot, json.encodeToString(pending))
        } catch (_: Exception) {
            throw AdminClientException("No se envió la solicitud: habilita el almacenamiento del sitio en el navegador.")
        }
        val mode = if (saved == null) "execute" else "result"
        val timeoutMillis = if (operation == "admin-create-user") 45_000L else 20_000L
        val client = HttpClient { followRedirects = false; install(HttpTimeout) { requestTimeoutMillis = timeoutMillis } }
        try {
            suspend fun send(requestMode: String): Pair<Int, JsonObject> {
                val response = client.post("${supabase.supabaseHttpUrl}/functions/v1/$operation") {
                    bearerAuth(session.accessToken)
                    header("apikey", supabase.supabaseKey)
                    header("Idempotency-Key", pending.id)
                    header("X-Admin-Mode", requestMode)
                    contentType(ContentType.Application.Json)
                    val wire = if (requestMode == "execute" && password != null)
                        JsonObject(pending.body + ("password" to JsonPrimitive(password))) else pending.body
                    setBody(json.encodeToString(wire))
                }
                return response.status.value to json.parseToJsonElement(response.bodyAsText()).jsonObject
            }
            var (status, result) = send(mode)
            var code = result["code"]?.jsonPrimitive?.contentOrNull
            if (mode == "result" && code == "NOT_OBSERVED") {
                val closed = send("close")
                status = closed.first; result = closed.second
                code = result["code"]?.jsonPrimitive?.contentOrNull
            }
            val success = status == 200 && code == "COMPLETED" && result["success"]?.jsonPrimitive?.booleanOrNull == true
            if (success) {
                if (operation == "admin-create-entity" && result["data"]?.jsonObject?.get("entityId")?.jsonPrimitive?.contentOrNull.isNullOrBlank()) {
                    throw AdminClientException("Resultado pendiente de confirmación. Repite la misma solicitud.")
                }
                AdminPendingStore.remove(slot)
                if (saved != null && saved.body != body) throw AdminClientException("La operación anterior quedó confirmada. Revisa la lista y vuelve a guardar este cambio si procede.")
                return@withLock result
            }
            val terminal = code in setOf("CANCELLED", "CREATION_REJECTED", "EMAIL_CHANGE_UNAVAILABLE", "PROTECTED_ROLE", "INVALID_ASSOCIATION",
                "ENTITY_TRANSFER_UNAVAILABLE", "DATABASE_FAILURE", "CONSTRAINT_REJECTED") ||
                (mode == "execute" && status in setOf(400, 403, 409) && code in setOf("FORBIDDEN", "INVALID_INPUT", "IDEMPOTENCY_CONFLICT"))
            if (terminal) AdminPendingStore.remove(slot)
            throw AdminClientException(when {
                status == 401 -> "La sesión ha caducado. Inicia sesión de nuevo y repite la solicitud."
                code == "FORBIDDEN" -> "No tienes permiso para modificar esta cuenta o centro."
                code == "EMAIL_CHANGE_UNAVAILABLE" -> "El cambio de correo no está disponible. No se guardó el cambio."
                code == "PROTECTED_ROLE" -> "Las cuentas SUPER_ADMIN están protegidas."
                code == "CREATION_REJECTED" -> "No se creó la cuenta. Comprueba que el correo no esté registrado y que la contraseña cumpla los requisitos."
                code == "ENTITY_TRANSFER_UNAVAILABLE" || code == "INVALID_ASSOCIATION" -> "No se permite trasladar esta cuenta a otro centro."
                code == "CANCELLED" -> "La solicitud anterior quedó cancelada sin aplicarse. Puedes volver a guardar."
                terminal -> "No se guardó el cambio. Revisa los campos y los permisos."
                else -> "Resultado pendiente de confirmación. Repite la misma solicitud para consultarlo sin duplicarla."
            })
        } finally { client.close() }
    }

    companion object {
        private val mutex = Mutex()
        private val json = Json { ignoreUnknownKeys = true }
    }
}

@Serializable
private data class PendingAdminRequest(val id: String, val body: JsonObject)
private class AdminClientException(message: String) : IllegalStateException(message)
