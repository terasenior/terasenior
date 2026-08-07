package com.terapia.terasenior.domain.model.therapy

import com.terapia.terasenior.domain.model.results.ActivityResult

/**
 * Representa una sesión completada con sus detalles temporales y resultados agrupados.
 */
data class PatientSessionHistory(
    val session: TherapySession,
    val results: List<ActivityResult>,
    val groupedByCategory: Map<String, List<ActivityResult>>
)
