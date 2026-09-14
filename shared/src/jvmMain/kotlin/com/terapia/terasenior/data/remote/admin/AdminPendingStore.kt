package com.terapia.terasenior.data.remote.admin

internal actual object AdminPendingStore {
    private val preferences = java.util.prefs.Preferences.userRoot().node("terasenior/admin-pending")
    actual fun read(key: String): String? = preferences.get(key, null)
    actual fun write(key: String, value: String) { preferences.put(key, value); preferences.flush() }
    actual fun remove(key: String) { preferences.remove(key); preferences.flush() }
}
