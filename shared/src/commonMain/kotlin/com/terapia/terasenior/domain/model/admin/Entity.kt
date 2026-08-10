package com.terapia.terasenior.domain.model.admin

data class Entity(
    val id: String,
    val name: String,
    val cif: String,
    val address: String?,
    val status: String,
    val createdAt: String,
    val licenseExpiresAt: String? = null,
    val logoUrl: String? = null
)
