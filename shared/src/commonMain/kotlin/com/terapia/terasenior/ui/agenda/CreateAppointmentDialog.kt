package com.terapia.terasenior.ui.agenda

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.terapia.terasenior.domain.model.agenda.AppointmentType
import com.terapia.terasenior.domain.model.patient.Patient
import com.terapia.terasenior.domain.model.admin.UserProfile
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateAppointmentDialog(
    selectedDate: LocalDate,
    patients: List<Patient>,
    professionals: List<UserProfile>,
    onDismiss: () -> Unit,
    onConfirm: (title: String, desc: String?, start: LocalTime, end: LocalTime, type: AppointmentType, staff: List<String>, attendees: List<String>) -> Unit,
    isLoading: Boolean
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var startTime by remember { mutableStateOf(LocalTime(10, 0)) }
    var endTime by remember { mutableStateOf(LocalTime(11, 0)) }
    var appointmentType by remember { mutableStateOf(AppointmentType.INDIVIDUAL) }
    
    // Selección múltiple simplificada para este diálogo
    val selectedPatients = remember { mutableStateListOf<String>() }
    val selectedStaff = remember { mutableStateListOf<String>() }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Programar Nueva Sesión", style = MaterialTheme.typography.headlineSmall) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Título de la sesión") },
                    placeholder = { Text("Ej: Taller de Memoria") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = "${startTime.hour}:${startTime.minute.toString().padStart(2, '0')}",
                        onValueChange = { },
                        label = { Text("Inicio") },
                        modifier = Modifier.weight(1f),
                        readOnly = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                    OutlinedTextField(
                        value = "${endTime.hour}:${endTime.minute.toString().padStart(2, '0')}",
                        onValueChange = { },
                        label = { Text("Fin") },
                        modifier = Modifier.weight(1f),
                        readOnly = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                Text("Tipo de Sesión", style = MaterialTheme.typography.labelLarge)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = appointmentType == AppointmentType.INDIVIDUAL,
                        onClick = { appointmentType = AppointmentType.INDIVIDUAL },
                        label = { Text("Individual") }
                    )
                    FilterChip(
                        selected = appointmentType == AppointmentType.GROUP,
                        onClick = { appointmentType = AppointmentType.GROUP },
                        label = { Text("Grupal") }
                    )
                }

                // Selección de Paciente (Simplificada como lista de checkboxes para demo)
                Text("Asistentes (Pacientes)", style = MaterialTheme.typography.labelLarge)
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        patients.forEach { patient ->
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Checkbox(
                                    checked = selectedPatients.contains(patient.id),
                                    onCheckedChange = { 
                                        if (it) selectedPatients.add(patient.id) else selectedPatients.remove(patient.id)
                                    }
                                )
                                Text(patient.fullName, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { 
                    onConfirm(title, description, startTime, endTime, appointmentType, selectedStaff, selectedPatients) 
                },
                enabled = title.isNotBlank() && selectedPatients.isNotEmpty() && !isLoading
            ) {
                if (isLoading) CircularProgressIndicator(modifier = Modifier.size(20.dp)) else Text("Programar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        },
        shape = RoundedCornerShape(24.dp)
    )
}
