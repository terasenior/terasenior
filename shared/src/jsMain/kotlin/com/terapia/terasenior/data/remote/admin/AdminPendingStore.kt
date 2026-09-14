package com.terapia.terasenior.data.remote.admin

internal actual object AdminPendingStore {
    actual fun read(key: String): String? = js("window.localStorage.getItem(key)") as String?
    actual fun write(key: String, value: String) { js("window.localStorage.setItem(key, value)") }
    actual fun remove(key: String) { js("window.localStorage.removeItem(key)") }
}
