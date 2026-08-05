package com.terapia.terasenior.ui.agenda

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
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
    
    var startHour by remember { mutableStateOf("10") }
    var startMin by remember { mutableStateOf("00") }
    var endHour by remember { mutableStateOf("11") }
    var endMin by remember { mutableStateOf("00") }
    
    var appointmentType by remember { mutableStateOf(AppointmentType.INDIVIDUAL) }
    val selectedPatients = remember { mutableStateListOf<String>() }

    // Estado para la búsqueda de pacientes
    var patientQuery by remember { mutableStateOf("") }
    val filteredPatients = remember(patientQuery, patients) {
        if (patientQuery.isBlank()) patients
        else patients.filter { it.fullName.contains(patientQuery, ignoreCase = true) }
    }

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
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Text("Horario (HH:MM)", style = MaterialTheme.typography.labelLarge)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = startHour,
                        onValueChange = { if(it.length <= 2) startHour = it },
                        modifier = Modifier.weight(1f),
                        label = { Text("Hora") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = RoundedCornerShape(12.dp)
                    )
                    Text(":")
                    OutlinedTextField(
                        value = startMin,
                        onValueChange = { if(it.length <= 2) startMin = it },
                        modifier = Modifier.weight(1f),
                        label = { Text("Min") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = RoundedCornerShape(12.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(Icons.Default.AccessTime, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    OutlinedTextField(
                        value = endHour,
                        onValueChange = { if(it.length <= 2) endHour = it },
                        modifier = Modifier.weight(1f),
                        label = { Text("Hora") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = RoundedCornerShape(12.dp)
                    )
                    Text(":")
                    OutlinedTextField(
                        value = endMin,
                        onValueChange = { if(it.length <= 2) endMin = it },
                        modifier = Modifier.weight(1f),
                        label = { Text("Min") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                Text("Asistentes (Pacientes)", style = MaterialTheme.typography.labelLarge)
                
                // Buscador de pacientes
                OutlinedTextField(
                    value = patientQuery,
                    onValueChange = { patientQuery = it },
                    placeholder = { Text("Buscar paciente...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                // Lista de pacientes con altura fija y scroll
                Card(
                    modifier = Modifier.fillMaxWidth().heightIn(max = 200.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                ) {
                    // Usamos una Column simple dentro de la Card porque el diálogo ya tiene scroll vertical
                    Column(modifier = Modifier.padding(4.dp)) {
                        filteredPatients.forEach { patient ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically, 
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { 
                                        if (selectedPatients.contains(patient.id)) selectedPatients.remove(patient.id) 
                                        else selectedPatients.add(patient.id) 
                                    }
                                    .padding(vertical = 4.dp)
                            ) {
                                Checkbox(
                                    checked = selectedPatients.contains(patient.id),
                                    onCheckedChange = { 
                                        if (it) selectedPatients.add(patient.id) else selectedPatients.remove(patient.id)
                                    }
                                )
                                Text(patient.fullName, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                        if (filteredPatients.isEmpty()) {
                            Text("No se encontraron resultados", modifier = Modifier.padding(16.dp), style = MaterialTheme.typography.bodySmall, color = Color.Gray)
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
                    onConfirm(title, description, start, end, appointmentType, emptyList(), selectedPatients) 
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
