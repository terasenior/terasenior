package com.terapia.terasenior.util

/**
 * Utilidad para el manejo y conversión de fechas en el proyecto Terasenior.
 * Soporta conversión entre formato de usuario (DD-MM-AAAA) y formato de DB (YYYY-MM-DD).
 */
object DateUtils {

    /**
     * Convierte una fecha de formato YYYY-MM-DD (DB) a DD-MM-AAAA (Usuario).
     */
    fun toUserFormat(dbDate: String?): String {
        if (dbDate.isNullOrBlank()) return ""
        val parts = dbDate.split("-")
        if (parts.size != 3) return dbDate
        return "${parts[2]}-${parts[1]}-${parts[0]}"
    }

    /**
     * Convierte una fecha de formato DD-MM-AAAA (Usuario) a YYYY-MM-DD (DB).
     */
    fun toDbFormat(userDate: String?): String {
        if (userDate.isNullOrBlank()) return ""
        val parts = userDate.split("-")
        if (parts.size != 3) return userDate
        // Si el primer segmento tiene 4 dígitos, ya está en formato DB
        if (parts[0].length == 4) return userDate
        return "${parts[2]}-${parts[1]}-${parts[0]}"
    }
}
