package com.terapia.terasenior.ui.agenda

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.terapia.terasenior.domain.model.agenda.Appointment
import com.terapia.terasenior.domain.model.agenda.AppointmentAttendee
import com.terapia.terasenior.domain.model.agenda.AppointmentType
import com.terapia.terasenior.domain.model.patient.Patient
import com.terapia.terasenior.domain.model.therapy.ExerciseConfig
import com.terapia.terasenior.util.DateUtils
import kotlinx.datetime.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditAppointmentDialog(
    appointment: Appointment,
    attendees: List<AppointmentAttendee>,
    allPatients: List<Patient>,
    onDismiss: () -> Unit,
    onConfirm: (updatedAppt: Appointment, selectedPatientIds: List<String>) -> Unit,
    isLoading: Boolean
) {
    var title by remember { mutableStateOf(appointment.title) }
    var description by remember { mutableStateOf(appointment.description ?: "") }
    
    val tz = TimeZone.currentSystemDefault()
    val startInstant = Instant.parse(appointment.startAt)
    val endInstant = Instant.parse(appointment.endAt)
    val startTime = startInstant.toLocalDateTime(tz).time
    val endTime = endInstant.toLocalDateTime(tz).time
    val date = startInstant.toLocalDateTime(tz).date

    var startHour by remember { mutableStateOf(startTime.hour.toString().padStart(2, '0')) }
    var startMin by remember { mutableStateOf(startTime.minute.toString().padStart(2, '0')) }
    var endHour by remember { mutableStateOf(endTime.hour.toString().padStart(2, '0')) }
    var endMin by remember { mutableStateOf(endTime.minute.toString().padStart(2, '0')) }
    
    val selectedPatientIds = remember { mutableStateListOf<String>().apply { addAll(attendees.map { it.patientId }) } }
    val plannedExercises = remember { mutableStateListOf<ExerciseConfig>().apply { addAll(appointment.plannedExercises) } }

    var showPatientPicker by remember { mutableStateOf(false) }
    var showExercisePicker by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Editar Sesión Programada", style = MaterialTheme.typography.headlineSmall) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Título") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Text("Horario", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(value = startHour, onValueChange = { if(it.length <= 2) startHour = it }, modifier = Modifier.weight(1f), label = { Text("Hora") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), shape = RoundedCornerShape(12.dp))
                    Text(":")
                    OutlinedTextField(value = startMin, onValueChange = { if(it.length <= 2) startMin = it }, modifier = Modifier.weight(1f), label = { Text("Min") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), shape = RoundedCornerShape(12.dp))
                    Icon(Icons.Default.AccessTime, contentDescription = null)
                    OutlinedTextField(value = endHour, onValueChange = { if(it.length <= 2) endHour = it }, modifier = Modifier.weight(1f), label = { Text("Hora") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), shape = RoundedCornerShape(12.dp))
                    Text(":")
                    OutlinedTextField(value = endMin, onValueChange = { if(it.length <= 2) endMin = it }, modifier = Modifier.weight(1f), label = { Text("Min") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), shape = RoundedCornerShape(12.dp))
                }

                // SECCIÓN DE ASISTENTES
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Asistentes (${selectedPatientIds.size})", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                        Button(
                            onClick = { showPatientPicker = true },
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                        ) {
                            Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Cambiar", fontSize = 12.sp)
                        }
                    }
                    
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            selectedPatientIds.forEach { id ->
                                val name = allPatients.find { it.id == id }?.fullName ?: "Desconocido"
                                Text("• $name", style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(start = 8.dp))
                            }
                        }
                    }
                }

                // SECCIÓN DE PLANIFICACIÓN
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Plan de Ejercicios (${plannedExercises.size})", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                        Button(
                            onClick = { showExercisePicker = true },
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Ajustar", fontSize = 12.sp)
                        }
                    }

                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            plannedExercises.forEach { config ->
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 2.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(config.name, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                                    Text("Nivel ${config.level}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { 
                    val sTime = LocalTime(startHour.toIntOrNull() ?: 10, startMin.toIntOrNull() ?: 0)
                    val eTime = LocalTime(endHour.toIntOrNull() ?: 11, endMin.toIntOrNull() ?: 0)
                    val updated = appointment.copy(
                        title = title,
                        description = description,
                        startAt = date.atTime(sTime).toInstant(tz).toString(),
                        endAt = date.atTime(eTime).toInstant(tz).toString(),
                        plannedExercises = plannedExercises.toList()
                    )
                    onConfirm(updated, selectedPatientIds.toList()) 
                },
                enabled = title.isNotBlank() && selectedPatientIds.isNotEmpty() && !isLoading,
                shape = RoundedCornerShape(12.dp)
            ) {
                if (isLoading) CircularProgressIndicator(modifier = Modifier.size(20.dp)) else Text("Guardar Cambios")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } },
        shape = RoundedCornerShape(24.dp)
    )

    if (showPatientPicker) {
        PatientPickerDialog(
            patients = allPatients,
            initiallySelectedIds = selectedPatientIds.toList(),
            onDismiss = { showPatientPicker = false },
            onConfirm = { ids ->
                selectedPatientIds.clear()
                selectedPatientIds.addAll(ids)
                showPatientPicker = false
            }
        )
    }

    if (showExercisePicker) {
        ExercisePickerDialog(
            initiallySelected = plannedExercises.toList(),
            onDismiss = { showExercisePicker = false },
            onConfirm = { list ->
                plannedExercises.clear()
                plannedExercises.addAll(list)
                showExercisePicker = false
            }
        )
    }
}
