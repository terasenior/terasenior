package com.terapia.terasenior.domain.model.admin

enum class UserRole(val id: String) {
    SUPER_ADMIN("SUPER_ADMIN"),
    ADMIN_CENTRO("ADMIN_CENTRO"),
    TERAPEUTA("TERAPEUTA"),
    AUXILIAR("AUXILIAR");

    companion object {
        fun fromId(id: String): UserRole = entries.find { it.id == id } ?: AUXILIAR
    }
}
