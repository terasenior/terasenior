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
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.terapia.terasenior.domain.model.agenda.AppointmentType
import com.terapia.terasenior.domain.model.patient.Patient
import com.terapia.terasenior.domain.model.admin.UserProfile
import com.terapia.terasenior.domain.model.therapy.ExerciseConfig
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateAppointmentDialog(
    selectedDate: LocalDate,
    patients: List<Patient>,
    professionals: List<UserProfile>,
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

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Programar Sesión Terapéutica", style = MaterialTheme.typography.headlineSmall) },
        text = {
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
                            Text("Agregar", fontSize = 12.sp)
                        }
                    }
                    
                    if (selectedPatientIds.isEmpty()) {
                        Text("Ningún paciente seleccionado.", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    } else {
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                selectedPatientIds.forEach { id ->
                                    val name = patients.find { it.id == id }?.fullName ?: "Desconocido"
                                    Text("• $name", style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(start = 8.dp))
                                }
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
                            Text("Agregar", fontSize = 12.sp)
                        }
                    }

                    if (plannedExercises.isEmpty()) {
                        Text("Sin actividades programadas.", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    } else {
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
            }
        },
        confirmButton = {
            Button(
                onClick = { 
                    val start = LocalTime(startHour.toIntOrNull() ?: 10, startMin.toIntOrNull() ?: 0)
                    val end = LocalTime(endHour.toIntOrNull() ?: 11, endMin.toIntOrNull() ?: 0)
                    onConfirm(title, description, start, end, appointmentType, emptyList(), selectedPatientIds.toList(), plannedExercises.toList()) 
                },
                enabled = title.isNotBlank() && selectedPatientIds.isNotEmpty() && !isLoading,
                shape = RoundedCornerShape(12.dp)
            ) {
                if (isLoading) CircularProgressIndicator(modifier = Modifier.size(20.dp)) else Text("Programar")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } },
        shape = RoundedCornerShape(24.dp)
    )

    if (showPatientPicker) {
        PatientPickerDialog(
            patients = patients,
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
