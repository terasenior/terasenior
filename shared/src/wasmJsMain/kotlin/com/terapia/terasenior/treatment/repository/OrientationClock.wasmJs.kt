@file:OptIn(kotlin.js.ExperimentalWasmJsInterop::class)

package com.terapia.terasenior.treatment.repository

import kotlinx.datetime.LocalDateTime

actual fun currentOrientationLocalDateTime(): LocalDateTime =
    LocalDateTime.parse(js("new Date(Date.now() - new Date().getTimezoneOffset() * 60000).toISOString().slice(0, 19)"))
