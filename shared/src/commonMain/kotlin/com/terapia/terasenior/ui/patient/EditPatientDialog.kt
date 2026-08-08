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
    var nif by remember { mutableStateOf(patient.nif ?: "") }
    var firstName by remember { mutableStateOf(patient.firstName) }
    var lastName by remember { mutableStateOf(patient.lastName) }
    var preferredName by remember { mutableStateOf(patient.preferredName ?: "") }
    var birthDate by remember { mutableStateOf(patient.birthDate ?: "") }
    var admissionDate by remember { mutableStateOf(patient.admissionDate ?: "") }
    var dischargeDate by remember { mutableStateOf(patient.dischargeDate ?: "") }
    var address by remember { mutableStateOf(patient.address ?: "") }
    var city by remember { mutableStateOf(patient.city ?: "") }
    var postalCode by remember { mutableStateOf(patient.postalCode ?: "") }
    var province by remember { mutableStateOf(patient.province ?: "") }
    var phone by remember { mutableStateOf(patient.phone ?: "") }
    var contactName by remember { mutableStateOf(patient.contactName ?: "") }
    var contactPhone by remember { mutableStateOf(patient.contactPhone ?: "") }
    var status by remember { mutableStateOf(patient.status) }

    var selectedTab by remember { mutableStateOf(0) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edición de Ficha Clínica", style = MaterialTheme.typography.headlineSmall) },
        text = {
            Column(modifier = Modifier.fillMaxWidth().heightIn(min = 450.dp)) {
                
                TabRow(selectedTabIndex = selectedTab, modifier = Modifier.padding(bottom = 16.dp)) {
                    Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }, text = { Text("Gestión") })
                    Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }, text = { Text("Ficha") })
                    Tab(selected = selectedTab == 2, onClick = { selectedTab = 2 }, text = { Text("Contacto") })
                }

                Column(modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    when (selectedTab) {
                        0 -> {
                            OutlinedTextField(value = externalId, onValueChange = { externalId = it }, label = { Text("Nº Expediente / ID Interno") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
                            OutlinedTextField(value = nif, onValueChange = { nif = it }, label = { Text("DNI / NIF Oficial") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
                            OutlinedTextField(value = admissionDate, onValueChange = { admissionDate = it }, label = { Text("Fecha Alta (AAAA-MM-DD)") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
                            OutlinedTextField(value = dischargeDate, onValueChange = { dischargeDate = it }, label = { Text("Fecha Baja (Si aplica)") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
                            
                            Text("Estado Actual", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
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
                        1 -> {
                            OutlinedTextField(value = firstName, onValueChange = { firstName = it }, label = { Text("Nombre") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
                            OutlinedTextField(value = lastName, onValueChange = { lastName = it }, label = { Text("Apellidos") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
                            OutlinedTextField(value = preferredName, onValueChange = { preferredName = it }, label = { Text("Nombre Preferido") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
                            OutlinedTextField(value = birthDate, onValueChange = { birthDate = it }, label = { Text("Fecha Nacimiento (AAAA-MM-DD)") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
                        }
                        2 -> {
                            OutlinedTextField(value = address, onValueChange = { address = it }, label = { Text("Dirección") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(value = city, onValueChange = { city = it }, label = { Text("Población") }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp))
                                OutlinedTextField(value = postalCode, onValueChange = { postalCode = it }, label = { Text("CP") }, modifier = Modifier.width(100.dp), shape = RoundedCornerShape(12.dp))
                            }
                            OutlinedTextField(value = province, onValueChange = { province = it }, label = { Text("Provincia") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
                            OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("Teléfono Principal") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
                            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                            Text("Referente de Emergencia", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                            OutlinedTextField(value = contactName, onValueChange = { contactName = it }, label = { Text("Nombre Familiar") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
                            OutlinedTextField(value = contactPhone, onValueChange = { contactPhone = it }, label = { Text("Teléfono Familiar") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { 
                    onConfirm(patient.copy(
                        externalId = externalId, nif = nif, firstName = firstName, lastName = lastName, 
                        preferredName = preferredName, birthDate = birthDate, admissionDate = admissionDate,
                        dischargeDate = dischargeDate, address = address, city = city, postalCode = postalCode,
                        province = province, phone = phone, contactName = contactName, 
                        contactPhone = contactPhone, status = status
                    ))
                },
                enabled = firstName.isNotBlank() && lastName.isNotBlank() && !isLoading,
                shape = RoundedCornerShape(12.dp)
            ) {
                if (isLoading) CircularProgressIndicator(modifier = Modifier.size(20.dp)) else Text("Guardar Cambios")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } },
        shape = RoundedCornerShape(24.dp)
    )
}
