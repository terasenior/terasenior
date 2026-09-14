@file:OptIn(kotlin.js.ExperimentalWasmJsInterop::class)
package com.terapia.terasenior.data.remote.admin

internal actual object AdminPendingStore {
    actual fun read(key: String): String? = readStorage(key)
    actual fun write(key: String, value: String) = writeStorage(key, value)
    actual fun remove(key: String) = removeStorage(key)
}
private fun readStorage(key: String): String? = js("window.localStorage.getItem(key)")
private fun writeStorage(key: String, value: String): Unit = js("window.localStorage.setItem(key, value)")
private fun removeStorage(key: String): Unit = js("window.localStorage.removeItem(key)")
