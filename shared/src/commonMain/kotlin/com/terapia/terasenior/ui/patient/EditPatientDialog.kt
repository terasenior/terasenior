package com.terapia.terasenior.ui.patient

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.terapia.terasenior.domain.model.patient.Patient
import com.terapia.terasenior.domain.model.patient.PatientStatus

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditPatientDialog(
    patient: Patient,
    onDismiss: () -> Unit,
    onConfirm: (Patient) -> Unit,
    isLoading: Boolean
) {
    var firstName by remember { mutableStateOf(patient.firstName) }
    var lastName by remember { mutableStateOf(patient.lastName) }
    var preferredName by remember { mutableStateOf(patient.preferredName ?: "") }
    var status by remember { mutableStateOf(patient.status) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Editar Paciente", style = MaterialTheme.typography.headlineSmall) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = firstName,
                    onValueChange = { firstName = it },
                    label = { Text("Nombre") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = lastName,
                    onValueChange = { lastName = it },
                    label = { Text("Apellidos") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = preferredName,
                    onValueChange = { preferredName = it },
                    label = { Text("Nombre Preferido") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Text("Estado del Paciente", style = MaterialTheme.typography.labelLarge)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    PatientStatus.entries.forEach { s ->
                        FilterChip(
                            selected = status == s,
                            onClick = { status = s },
                            label = { Text(s.name) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { 
                    onConfirm(patient.copy(firstName = firstName, lastName = lastName, preferredName = preferredName, status = status))
                },
                enabled = firstName.isNotBlank() && lastName.isNotBlank() && !isLoading
            ) {
                Text("Guardar Cambios")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}
