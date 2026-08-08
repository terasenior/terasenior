package com.terapia.terasenior.ui.agenda

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.terapia.terasenior.domain.model.agenda.AppointmentType
import com.terapia.terasenior.domain.model.patient.Patient
import com.terapia.terasenior.domain.model.admin.UserProfile
import com.terapia.terasenior.domain.model.therapy.ExerciseConfig
import com.terapia.terasenior.ui.therapy.SessionPlannerComponent
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
    val selectedPatients = remember { mutableStateListOf<String>() }
    val plannedExercises = remember { mutableStateListOf<ExerciseConfig>() }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Programar Sesión Terapéutica", style = MaterialTheme.typography.headlineSmall) },
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

                Text("Asistentes", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        patients.forEach { patient ->
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().clickable { if (selectedPatients.contains(patient.id)) selectedPatients.remove(patient.id) else selectedPatients.add(patient.id) }) {
                                Checkbox(checked = selectedPatients.contains(patient.id), onCheckedChange = { if (it) selectedPatients.add(patient.id) else selectedPatients.remove(patient.id) })
                                Text(patient.fullName, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }

                // COMPONENTE DE PLANIFICACIÓN
                SessionPlannerComponent(
                    plannedExercises = plannedExercises,
                    onAddExercise = { plannedExercises.add(it) },
                    onRemoveExercise = { plannedExercises.removeAt(it) }
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { 
                    val start = LocalTime(startHour.toIntOrNull() ?: 10, startMin.toIntOrNull() ?: 0)
                    val end = LocalTime(endHour.toIntOrNull() ?: 11, endMin.toIntOrNull() ?: 0)
                    onConfirm(title, description, start, end, appointmentType, emptyList(), selectedPatients.toList(), plannedExercises.toList()) 
                },
                enabled = title.isNotBlank() && selectedPatients.isNotEmpty() && !isLoading,
                shape = RoundedCornerShape(12.dp)
            ) {
                if (isLoading) CircularProgressIndicator(modifier = Modifier.size(20.dp)) else Text("Programar")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } },
        shape = RoundedCornerShape(24.dp)
    )
}
