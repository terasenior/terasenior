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
import com.terapia.terasenior.domain.model.patient.PatientStatus

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun CreatePatientDialog(
    onDismiss: () -> Unit,
    onConfirm: (
        firstName: String, 
        lastName: String, 
        preferredName: String, 
        birthDate: String, 
        externalId: String, 
        nif: String,
        admissionDate: String,
        address: String,
        phone: String,
        contactName: String,
        contactPhone: String,
        status: PatientStatus
    ) -> Unit,
    isLoading: Boolean,
    errorMessage: String? = null
) {
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var preferredName by remember { mutableStateOf("") }
    var birthDate by remember { mutableStateOf("") }
    var externalId by remember { mutableStateOf("") }
    var nif by remember { mutableStateOf("") }
    var admissionDate by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var contactName by remember { mutableStateOf("") }
    var contactPhone by remember { mutableStateOf("") }
    var status by remember { mutableStateOf(PatientStatus.ACTIVE) }

    var selectedTab by remember { mutableStateOf(0) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Alta de Nuevo Paciente", style = MaterialTheme.typography.headlineSmall) },
        text = {
            Column(modifier = Modifier.fillMaxWidth().heightIn(min = 400.dp)) {
                if (errorMessage != null) {
                    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer), modifier = Modifier.padding(bottom = 12.dp)) {
                        Text(text = errorMessage, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(12.dp))
                    }
                }

                TabRow(selectedTabIndex = selectedTab, modifier = Modifier.padding(bottom = 16.dp)) {
                    Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }, text = { Text("Admin") })
                    Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }, text = { Text("Personal") })
                    Tab(selected = selectedTab == 2, onClick = { selectedTab = 2 }, text = { Text("Contacto") })
                }

                Column(modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    when (selectedTab) {
                        0 -> {
                            OutlinedTextField(value = externalId, onValueChange = { externalId = it }, label = { Text("Nº Expediente / ID Interno") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
                            OutlinedTextField(value = nif, onValueChange = { nif = it }, label = { Text("DNI / NIF") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
                            OutlinedTextField(value = admissionDate, onValueChange = { admissionDate = it }, label = { Text("Fecha Alta (AAAA-MM-DD)") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
                            
                            Text("Estado Inicial", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                PatientStatus.entries.filter { it != PatientStatus.BAJA }.forEach { s ->
                                    val label = when(s) {
                                        PatientStatus.ACTIVE -> "Activo"
                                        PatientStatus.INACTIVE -> "Inactivo"
                                        PatientStatus.DISCHARGED -> "Alta"
                                        else -> ""
                                    }
                                    if (label.isNotEmpty()) {
                                        FilterChip(selected = status == s, onClick = { status = s }, label = { Text(label) })
                                    }
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
                            OutlinedTextField(value = address, onValueChange = { address = it }, label = { Text("Dirección Completa") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
                            OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("Teléfono Paciente") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
                            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                            Text("Contacto de Emergencia", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                            OutlinedTextField(value = contactName, onValueChange = { contactName = it }, label = { Text("Nombre Referente") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
                            OutlinedTextField(value = contactPhone, onValueChange = { contactPhone = it }, label = { Text("Teléfono Referente") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(firstName, lastName, preferredName, birthDate, externalId, nif, admissionDate, address, phone, contactName, contactPhone, status) },
                enabled = firstName.isNotBlank() && lastName.isNotBlank() && !isLoading,
                shape = RoundedCornerShape(12.dp)
            ) {
                if (isLoading) CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp) else Text("Finalizar Alta")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss, enabled = !isLoading) { Text("Cancelar") } },
        shape = RoundedCornerShape(24.dp)
    )
}
