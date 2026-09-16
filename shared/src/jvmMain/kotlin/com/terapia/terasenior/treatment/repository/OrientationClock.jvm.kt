package com.terapia.terasenior.treatment.repository

import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

actual fun currentOrientationLocalDateTimeIso(): String =
    Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).toString()
