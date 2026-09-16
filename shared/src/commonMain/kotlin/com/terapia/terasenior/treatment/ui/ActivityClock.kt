package com.terapia.terasenior.treatment.ui

/** Milisegundos actuales para medir actividades en todos los destinos. */
expect fun platformActivityTimeMillisText(): String

fun activityTimeMillis(): Long = platformActivityTimeMillisText().toLongOrNull() ?: 0L
