package com.terapia.terasenior.ui.patient

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.terapia.terasenior.domain.model.patient.Patient
import com.terapia.terasenior.domain.model.patient.PatientStatus

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun EditPatientDialog(
    patient: Patient,
    onDismiss: () -> Unit,
    onConfirm: (Patient) -> Unit,
    isLoading: Boolean
) {
    var externalId by remember { mutableStateOf(patient.externalId ?: "") }
    var firstName by remember { mutableStateOf(patient.firstName) }
    var lastName by remember { mutableStateOf(patient.lastName) }
    var preferredName by remember { mutableStateOf(patient.preferredName ?: "") }
    var birthDate by remember { mutableStateOf(patient.birthDate ?: "") }
    var admissionDate by remember { mutableStateOf(patient.admissionDate ?: "") }
    var dischargeDate by remember { mutableStateOf(patient.dischargeDate ?: "") }
    var address by remember { mutableStateOf(patient.address ?: "") }
    var phone by remember { mutableStateOf(patient.phone ?: "") }
    var contactName by remember { mutableStateOf(patient.contactName ?: "") }
    var contactPhone by remember { mutableStateOf(patient.contactPhone ?: "") }
    var status by remember { mutableStateOf(patient.status) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Editar Ficha Paciente", style = MaterialTheme.typography.headlineSmall) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                SectionTitle("Identificación")
                OutlinedTextField(value = externalId, onValueChange = { externalId = it }, label = { Text("ID Externo / NIF") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
                OutlinedTextField(value = firstName, onValueChange = { firstName = it }, label = { Text("Nombre") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
                OutlinedTextField(value = lastName, onValueChange = { lastName = it }, label = { Text("Apellidos") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))

                SectionTitle("Contacto y Localización")
                OutlinedTextField(value = address, onValueChange = { address = it }, label = { Text("Dirección") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
                OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("Teléfono Principal") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
                
                SectionTitle("Persona de Referencia")
                OutlinedTextField(value = contactName, onValueChange = { contactName = it }, label = { Text("Nombre Familiar/Contacto") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
                OutlinedTextField(value = contactPhone, onValueChange = { contactPhone = it }, label = { Text("Teléfono Familiar/Contacto") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))

                SectionTitle("Fechas Administrativas")
                OutlinedTextField(value = admissionDate, onValueChange = { admissionDate = it }, label = { Text("Fecha Alta") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
                OutlinedTextField(value = dischargeDate, onValueChange = { dischargeDate = it }, label = { Text("Fecha Baja") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))

                SectionTitle("Estado")
                FlowRow(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    PatientStatus.entries.forEach { s ->
                        val label = when(s) {
                            PatientStatus.ACTIVE -> "Activo"
                            PatientStatus.INACTIVE -> "Inactivo"
                            PatientStatus.BAJA -> "Baja"
                            PatientStatus.DISCHARGED -> "Alta"
                        }
                        FilterChip(selected = status == s, onClick = { status = s }, label = { Text(label) })
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { 
                    onConfirm(patient.copy(
                        externalId = externalId,
                        firstName = firstName, 
                        lastName = lastName, 
                        preferredName = preferredName, 
                        birthDate = birthDate,
                        admissionDate = admissionDate,
                        dischargeDate = dischargeDate,
                        address = address,
                        phone = phone,
                        contactName = contactName,
                        contactPhone = contactPhone,
                        status = status
                    ))
                },
                enabled = firstName.isNotBlank() && lastName.isNotBlank() && !isLoading
            ) {
                if (isLoading) CircularProgressIndicator(modifier = Modifier.size(20.dp)) else Text("Guardar Cambios")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}

@Composable
private fun SectionTitle(title: String) {
    Text(text = title, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 8.dp))
}
