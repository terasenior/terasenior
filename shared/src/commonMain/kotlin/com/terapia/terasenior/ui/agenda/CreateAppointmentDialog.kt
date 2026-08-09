package com.terapia.terasenior.ui.agenda

import androidx.compose.foundation.background
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
import com.terapia.terasenior.domain.model.agenda.AppointmentType
import com.terapia.terasenior.domain.model.patient.Patient
import com.terapia.terasenior.domain.model.admin.UserProfile
import com.terapia.terasenior.domain.model.therapy.ExerciseConfig
import kotlinx.datetime.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateAppointmentDialog(
    selectedDate: LocalDate,
    patients: List<Patient>,
    professionals: List<UserProfile>,
    existingAppointments: List<Appointment>, // Para detectar conflictos
    onDismiss: () -> Unit,
    onConfirm: (title: String, desc: String?, start: LocalTime, end: LocalTime, type: AppointmentType, staff: List<String>, attendees: List<String>, exercises: List<ExerciseConfig>) -> Unit,
    isLoading: Boolean
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    
    var startHour by remember { mutableStateOf("10") }
    var startMin by remember { mutableStateOf("00") }
    var endHour by remember { mutableStateOf("11") }
    var endMin by remember { mutableStateOf("00") }
    
    var appointmentType by remember { mutableStateOf(AppointmentType.INDIVIDUAL) }
    val selectedPatientIds = remember { mutableStateListOf<String>() }
    val plannedExercises = remember { mutableStateListOf<ExerciseConfig>() }

    var showPatientPicker by remember { mutableStateOf(false) }
    var showExercisePicker by remember { mutableStateOf(false) }
    
    // Estado para conflictos
    var conflictAppt by remember { mutableStateOf<Appointment?>(null) }
    var forceSave by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (conflictAppt != null) "¡Conflicto de Horario!" else "Programar Sesión Terapéutica", style = MaterialTheme.typography.headlineSmall) },
        text = {
            if (conflictAppt != null) {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Icon(Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(48.dp).align(Alignment.CenterHorizontally))
                    Text(
                        text = "El horario seleccionado coincide con otra sesión programada:",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f))) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(conflictAppt!!.title, fontWeight = FontWeight.Bold)
                            val start = Instant.parse(conflictAppt!!.startAt).toLocalDateTime(TimeZone.currentSystemDefault())
                            val end = Instant.parse(conflictAppt!!.endAt).toLocalDateTime(TimeZone.currentSystemDefault())
                            Text("Horario: ${start.hour}:${start.minute.toString().padStart(2,'0')} - ${end.hour}:${end.minute.toString().padStart(2,'0')}", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                    Text("¿Deseas corregir el horario o permitir que se solapen?", style = MaterialTheme.typography.bodySmall)
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Título de la sesión") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Text("Horario (HH:MM)", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        OutlinedTextField(value = startHour, onValueChange = { if(it.length <= 2) startHour = it }, modifier = Modifier.weight(1f), label = { Text("Hora") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), shape = RoundedCornerShape(12.dp))
                        Text(":")
                        OutlinedTextField(value = startMin, onValueChange = { if(it.length <= 2) startMin = it }, modifier = Modifier.weight(1f), label = { Text("Min") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), shape = RoundedCornerShape(12.dp))
                        Icon(Icons.Default.AccessTime, contentDescription = null)
                        OutlinedTextField(value = endHour, onValueChange = { if(it.length <= 2) endHour = it }, modifier = Modifier.weight(1f), label = { Text("Hora") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), shape = RoundedCornerShape(12.dp))
                        Text(":")
                        OutlinedTextField(value = endMin, onValueChange = { if(it.length <= 2) endMin = it }, modifier = Modifier.weight(1f), label = { Text("Min") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), shape = RoundedCornerShape(12.dp))
                    }

                    // ASISTENTES Y EJERCICIOS (Secciones resumidas)
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text("Asistentes (${selectedPatientIds.size})", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                            Button(onClick = { showPatientPicker = true }, shape = RoundedCornerShape(8.dp)) { Text("Agregar", fontSize = 12.sp) }
                        }
                        if (selectedPatientIds.isNotEmpty()) {
                            Card(modifier = Modifier.fillMaxWidth()) {
                                Column(modifier = Modifier.padding(8.dp)) {
                                    selectedPatientIds.forEach { id -> Text("• ${patients.find { it.id == id }?.fullName ?: "Desconocido"}", style = MaterialTheme.typography.bodySmall) }
                                }
                            }
                        }
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text("Plan de Ejercicios (${plannedExercises.size})", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                            Button(onClick = { showExercisePicker = true }, shape = RoundedCornerShape(8.dp)) { Text("Agregar", fontSize = 12.sp) }
                        }
                        if (plannedExercises.isNotEmpty()) {
                            Card(modifier = Modifier.fillMaxWidth()) {
                                Column(modifier = Modifier.padding(8.dp)) {
                                    plannedExercises.forEach { config -> Text("• ${config.name} (Nivel ${config.level})", style = MaterialTheme.typography.bodySmall) }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            if (conflictAppt != null) {
                Button(onClick = { 
                    val start = LocalTime(startHour.toIntOrNull() ?: 10, startMin.toIntOrNull() ?: 0)
                    val end = LocalTime(endHour.toIntOrNull() ?: 11, endMin.toIntOrNull() ?: 0)
                    onConfirm(title, description, start, end, appointmentType, emptyList(), selectedPatientIds.toList(), plannedExercises.toList()) 
                }) {
                    Text("Permitir Solape")
                }
            } else {
                Button(
                    onClick = { 
                        val sTime = LocalTime(startHour.toIntOrNull() ?: 10, startMin.toIntOrNull() ?: 0)
                        val eTime = LocalTime(endHour.toIntOrNull() ?: 11, endMin.toIntOrNull() ?: 0)
                        
                        // Lógica de detección de conflicto
                        val tz = TimeZone.currentSystemDefault()
                        val newStart = selectedDate.atTime(sTime).toInstant(tz)
                        val newEnd = selectedDate.atTime(eTime).toInstant(tz)
                        
                        val conflict = existingAppointments.find { appt ->
                            val exStart = Instant.parse(appt.startAt)
                            val exEnd = Instant.parse(appt.endAt)
                            newStart < exEnd && exStart < newEnd
                        }
                        
                        if (conflict != null && !forceSave) {
                            conflictAppt = conflict
                        } else {
                            onConfirm(title, description, sTime, eTime, appointmentType, emptyList(), selectedPatientIds.toList(), plannedExercises.toList()) 
                        }
                    },
                    enabled = title.isNotBlank() && selectedPatientIds.isNotEmpty() && !isLoading,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (isLoading) CircularProgressIndicator(modifier = Modifier.size(20.dp)) else Text("Programar")
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
        PatientPickerDialog(patients = patients, initiallySelectedIds = selectedPatientIds.toList(), onDismiss = { showPatientPicker = false }, onConfirm = { ids -> selectedPatientIds.clear(); selectedPatientIds.addAll(ids); showPatientPicker = false })
    }

    if (showExercisePicker) {
        ExercisePickerDialog(initiallySelected = plannedExercises.toList(), onDismiss = { showExercisePicker = false }, onConfirm = { list -> plannedExercises.clear(); plannedExercises.addAll(list); showExercisePicker = false })
    }
}
