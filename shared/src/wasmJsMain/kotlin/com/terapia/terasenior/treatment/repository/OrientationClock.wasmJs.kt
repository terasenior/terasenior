@file:OptIn(kotlin.js.ExperimentalWasmJsInterop::class)

package com.terapia.terasenior.treatment.repository

actual fun currentOrientationLocalDateTimeIso(): String =
    js("new Date(Date.now() - new Date().getTimezoneOffset() * 60000).toISOString().slice(0, 19)")
