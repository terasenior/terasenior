package com.terapia.terasenior.data.remote.admin

// Only bounded requests, never passwords or session tokens. Removed on completion.
internal expect object AdminPendingStore {
    fun read(key: String): String?
    fun write(key: String, value: String)
    fun remove(key: String)
}
