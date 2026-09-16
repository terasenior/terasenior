package com.terapia.terasenior.treatment.repository

import kotlinx.datetime.LocalDateTime

/** Fecha y hora local del dispositivo para las preguntas de orientación temporal. */
expect fun currentOrientationLocalDateTime(): LocalDateTime
