package com.terapia.terasenior.data.model.admin

import com.terapia.terasenior.domain.model.admin.Entity
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class EntityDto(
    @SerialName("id") val id: String? = null,
    @SerialName("name") val name: String,
    @SerialName("cif") val cif: String,
    @SerialName("address") val address: String? = null,
    @SerialName("status") val status: String = "active",
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("license_expires_at") val licenseExpiresAt: String? = null,
    @SerialName("logo_url") val logoUrl: String? = null
)

fun EntityDto.toDomain() = Entity(
    id = id.orEmpty(),
    name = name,
    cif = cif,
    address = address,
    status = status,
    createdAt = createdAt.orEmpty(),
    licenseExpiresAt = licenseExpiresAt,
    logoUrl = logoUrl
)

fun Entity.toData() = EntityDto(
    id = id.ifEmpty { null },
    name = name,
    cif = cif,
    address = address,
    status = status,
    licenseExpiresAt = licenseExpiresAt,
    logoUrl = logoUrl
)
