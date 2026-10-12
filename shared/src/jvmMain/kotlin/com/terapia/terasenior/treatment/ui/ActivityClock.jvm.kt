package com.terapia.terasenior.treatment.ui

import kotlinx.datetime.Clock

actual fun platformActivityTimeMillisText(): String = Clock.System.now().toEpochMilliseconds().toString()

actual fun platformTodayIsoDate(): String = java.time.LocalDate.now().toString()
