package com.terapia.terasenior.data.model.patient

import com.terapia.terasenior.domain.model.patient.Consent
import com.terapia.terasenior.domain.model.patient.ConsentStatus
import com.terapia.terasenior.domain.model.patient.ConsentType
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ConsentDto(
    @SerialName("id") val id: String? = null,
    @SerialName("patient_id") val patientId: String,
    @SerialName("consent_type") val type: String,
    @SerialName("status") val status: String,
    @SerialName("version") val version: String,
    @SerialName("signed_at") val signedAt: String? = null,
    @SerialName("observations") val observations: String? = null
)

fun ConsentDto.toDomain() = Consent(
    id = id.orEmpty(),
    patientId = patientId,
    type = try { ConsentType.valueOf(type) } catch (e: Exception) { ConsentType.RESULTS },
    status = try { ConsentStatus.valueOf(status) } catch (e: Exception) { ConsentStatus.PENDING },
    version = version,
    signedAt = signedAt,
    observations = observations
)

fun Consent.toData() = ConsentDto(
    id = id.ifEmpty { null },
    patientId = patientId,
    type = type.name,
    status = status.name,
    version = version,
    signedAt = signedAt,
    observations = observations
)
