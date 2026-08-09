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
import androidx.compose.material.icons.filled.Warning
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
    existingAppointments: List<Appointment>, // Para detectar conflictos
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
    
    var conflictAppt by remember { mutableStateOf<Appointment?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (conflictAppt != null) "¡Conflicto de Horario!" else "Editar Sesión Programada", style = MaterialTheme.typography.headlineSmall) },
        text = {
            if (conflictAppt != null) {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Icon(Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(48.dp).align(Alignment.CenterHorizontally))
                    Text(text = "El nuevo horario coincide con otra sesión:", style = MaterialTheme.typography.bodyMedium)
                    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f))) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(conflictAppt!!.title, fontWeight = FontWeight.Bold)
                            val s = Instant.parse(conflictAppt!!.startAt).toLocalDateTime(tz)
                            val e = Instant.parse(conflictAppt!!.endAt).toLocalDateTime(tz)
                            Text("Horario: ${s.hour}:${s.minute.toString().padStart(2,'0')} - ${e.hour}:${e.minute.toString().padStart(2,'0')}", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            } else {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp).verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Título") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))

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

                    // Secciones resumidas de Asistentes y Ejercicios
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text("Asistentes (${selectedPatientIds.size})", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                            Button(onClick = { showPatientPicker = true }, shape = RoundedCornerShape(8.dp)) { Text("Cambiar", fontSize = 12.sp) }
                        }
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                selectedPatientIds.forEach { id -> Text("• ${allPatients.find { it.id == id }?.fullName ?: "Desconocido"}", style = MaterialTheme.typography.bodySmall) }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            if (conflictAppt != null) {
                Button(onClick = { 
                    val sTime = LocalTime(startHour.toIntOrNull() ?: 10, startMin.toIntOrNull() ?: 0)
                    val eTime = LocalTime(endHour.toIntOrNull() ?: 11, endMin.toIntOrNull() ?: 0)
                    val updated = appointment.copy(
                        title = title,
                        startAt = date.atTime(sTime).toInstant(tz).toString(),
                        endAt = date.atTime(eTime).toInstant(tz).toString(),
                        plannedExercises = plannedExercises.toList()
                    )
                    onConfirm(updated, selectedPatientIds.toList()) 
                }) {
                    Text("Permitir Solape")
                }
            } else {
                Button(
                    onClick = { 
                        val sTime = LocalTime(startHour.toIntOrNull() ?: 10, startMin.toIntOrNull() ?: 0)
                        val eTime = LocalTime(endHour.toIntOrNull() ?: 11, endMin.toIntOrNull() ?: 0)
                        
                        val newStart = date.atTime(sTime).toInstant(tz)
                        val newEnd = date.atTime(eTime).toInstant(tz)
                        
                        val conflict = existingAppointments.find { appt ->
                            if (appt.id == appointment.id) return@find false // No chocar con sí misma
                            val exStart = Instant.parse(appt.startAt)
                            val exEnd = Instant.parse(appt.endAt)
                            newStart < exEnd && exStart < newEnd
                        }
                        
                        if (conflict != null) {
                            conflictAppt = conflict
                        } else {
                            val updated = appointment.copy(
                                title = title,
                                description = description,
                                startAt = newStart.toString(),
                                endAt = newEnd.toString(),
                                plannedExercises = plannedExercises.toList()
                            )
                            onConfirm(updated, selectedPatientIds.toList()) 
                        }
                    },
                    enabled = title.isNotBlank() && selectedPatientIds.isNotEmpty() && !isLoading,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (isLoading) CircularProgressIndicator(modifier = Modifier.size(20.dp)) else Text("Guardar Cambios")
                }
            }
        },
        dismissButton = { 
            TextButton(onClick = { if (conflictAppt != null) conflictAppt = null else onDismiss() }) { 
                Text(if (conflictAppt != null) "Corregir Horario" else "Cancelar") 
            } 
        },
        shape = RoundedCornerShape(24.dp)
    )

    if (showPatientPicker) {
        PatientPickerDialog(patients = allPatients, initiallySelectedIds = selectedPatientIds.toList(), onDismiss = { showPatientPicker = false }, onConfirm = { ids -> selectedPatientIds.clear(); selectedPatientIds.addAll(ids); showPatientPicker = false })
    }

    if (showExercisePicker) {
        ExercisePickerDialog(initiallySelected = plannedExercises.toList(), onDismiss = { showExercisePicker = false }, onConfirm = { list -> plannedExercises.clear(); plannedExercises.addAll(list); showExercisePicker = false })
    }
}
