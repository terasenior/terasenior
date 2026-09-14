package com.terapia.terasenior.data.remote.admin

import com.terapia.terasenior.data.model.admin.EntityDto
import com.terapia.terasenior.data.model.admin.UserProfileDto
import com.terapia.terasenior.data.model.admin.toDomain
import com.terapia.terasenior.data.model.admin.toData
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.*
import kotlin.coroutines.*
import kotlin.test.*

class AdminCompatibilityTest {
    @OptIn(ExperimentalSerializationApi::class)
    @Test fun entityAssociationIntentionSurvivesSerializerOptions() {
        for (defaults in listOf(false, true)) for (nulls in listOf(false, true)) {
            val json = Json { encodeDefaults = defaults; explicitNulls = nulls }
            val requests = listOf(
                AdminUpdateUserRequest("target"),
                AdminUpdateUserRequest("target", entityId = NullableStringUpdate.clear()),
                AdminUpdateUserRequest("target", entityId = NullableStringUpdate.set("center"))
            )
            requests.forEach { assertEquals(it, json.decodeFromString<AdminUpdateUserRequest>(json.encodeToString(it))) }
            val clear = json.encodeToJsonElement(requests[1]).jsonObject["entityId"]!!.jsonObject
            assertEquals(JsonPrimitive("CLEAR"), clear["action"])
            assertNotEquals(json.encodeToString(requests[0]), json.encodeToString(requests[1]))
        }
    }

    @Test fun ambiguousUpdatesAreRejected() {
        assertFailsWith<IllegalArgumentException> { NullableStringUpdate(NullableStringAction.SET) }
        assertFailsWith<IllegalArgumentException> { NullableStringUpdate(NullableStringAction.CLEAR, "center") }
        assertFailsWith<IllegalArgumentException> { AdminUpdateUserRequest("id", phone = "123", clearPhone = true) }
        assertFailsWith<IllegalArgumentException> { AdminUpdateEntityRequest("id", address = "street", clearAddress = true) }
        assertFailsWith<IllegalArgumentException> {
            Json.decodeFromString<NullableStringUpdate>("""{"action":"SET"}""")
        }
    }

    @Test fun directUserPayloadCannotCarryPrivilegesAndCanClearPhone() {
        val payload = UserProfileOrdinaryUpdate("Name", null).toPostgrestPayload()
        assertEquals(setOf("full_name", "phone"), payload.keys)
        assertEquals(JsonNull, payload["phone"])
    }

    @Test fun directEntityPayloadCannotCarryPrivilegesAndCanClearNullableFields() {
        val payload = EntityOrdinaryUpdate("Center", "CIF", null, null).toPostgrestPayload()
        assertEquals(setOf("name", "cif", "address", "logo_url"), payload.keys)
        assertEquals(JsonNull, payload["address"])
        assertEquals(JsonNull, payload["logo_url"])
        val contract = Json { encodeDefaults = true }.encodeToJsonElement(AdminUpdateEntityRequest("id")).jsonObject
        assertFalse("status" in contract)
        assertFalse("licenseExpiresAt" in contract)
    }

    @Test fun ordinaryEditsAreDistinguishedFromEveryEditableUserPrivilege() {
        val current = UserProfileDto("id", "center", "TERAPEUTA", "Name", "user@example.invalid")
        val requested = current.toDomain()
        assertFalse(hasPrivilegedUserChanges(current, requested.copy(fullName = "New", phone = "123")))
        for (modified in listOf(
            requested.copy(role = com.terapia.terasenior.models.UserRole.SUPER_ADMIN),
            requested.copy(entityId = null), requested.copy(isActive = false),
            requested.copy(email = "other@example.invalid"), requested.copy(centerName = "Other")
        )) assertTrue(hasPrivilegedUserChanges(current, modified))
    }

    @Test fun ordinaryEntityEditsNeverHideStatusOrLicenseChanges() {
        val current = EntityDto(id = "id", name = "Center", cif = "CIF", status = "active").toDomain()
        assertEquals("ACTIVE", current.status)
        assertEquals("ACTIVE", EntityDto(name = "Center", cif = "CIF").status)
        assertEquals("INACTIVE", current.copy(status = "inactive").toData().status)
        assertFalse(hasPrivilegedEntityChanges(current, current.copy(name = "New", logoUrl = "logo")))
        assertTrue(hasPrivilegedEntityChanges(current, current.copy(status = "INACTIVE")))
        assertTrue(hasPrivilegedEntityChanges(current, current.copy(licenseExpiresAt = "2030-01-01")))
    }

    @Test fun passwordWireContractMatchesExistingFunction() {
        val payload = Json.encodeToJsonElement(AdminChangePasswordRequest("target", "synthetic-value")).jsonObject
        assertEquals(setOf("targetUserId", "newPassword"), payload.keys)
        assertEquals(JsonPrimitive("target"), payload["targetUserId"])
    }

    @Test fun unavailableGatewayNeverReportsSuccess() = immediate {
        val api = UnavailableAdminRemoteDataSource()
        val results = listOf(
            api.createUser(AdminCreateUserRequest("u@example.invalid", "synthetic-value", "Name", "TERAPEUTA", "center", null, true, null)),
            api.updateUser(AdminUpdateUserRequest("id")), api.deleteUser(AdminDeleteUserRequest("id")),
            api.changeUserPassword(AdminChangePasswordRequest("id", "synthetic-value")),
            api.createEntity(AdminCreateEntityRequest("Name", "CIF", null, null, null)),
            api.setEntityStatus(AdminSetEntityStatusRequest("id", "INACTIVE")),
            api.setEntityLicense(AdminSetEntityLicenseRequest("id", null)),
            api.deleteEntity(AdminDeleteEntityRequest("id"))
        )
        results.forEach {
            assertTrue(it.isFailure)
            assertIs<AdminBackendUnavailableException>(it.exceptionOrNull())
            assertTrue(it.exceptionOrNull()!!.message!!.contains("backend administrativo seguro"))
        }
    }

    @Test fun unsuccessfulOrIncompleteResponsesAreNotSuccessfulWrites() {
        assertTrue(Result.success(AdminActionResponse(false)).toActionResult().isFailure)
        assertTrue(Result.success(AdminCreateUserResponse(true)).toCreatedUserResult().isFailure)
        assertTrue(Result.success(AdminCreateEntityResponse(true)).toCreatedEntityResult().isFailure)
        assertTrue(Result.success(AdminCreateUserResponse(true, AdminCreatedUser("id"))).toCreatedUserResult().isSuccess)
        assertFailsWith<IllegalArgumentException> { AdminSetEntityStatusRequest("id", "active") }
    }

    // These gateway methods complete synchronously and perform no IO. Fail if that changes.
    private fun immediate(block: suspend () -> Unit) {
        var outcome: Result<Unit>? = null
        block.startCoroutine(object : Continuation<Unit> {
            override val context = EmptyCoroutineContext
            override fun resumeWith(result: Result<Unit>) { outcome = result }
        })
        assertNotNull(outcome, "El gateway no disponible no debe suspender ni realizar IO.").getOrThrow()
    }
}
