package com.terapia.terasenior.ui.agenda

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.terapia.terasenior.domain.model.agenda.AppointmentStatus
import com.terapia.terasenior.domain.model.agenda.AppointmentAttendee
import com.terapia.terasenior.domain.model.agenda.AttendanceStatus
import com.terapia.terasenior.domain.model.agenda.Appointment
import com.terapia.terasenior.ui.therapy.ExerciseTranslationUtils
import com.terapia.terasenior.util.DateUtils
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AppointmentDetailScreen(
    viewModel: AppointmentDetailViewModel,
    onStartSession: (Appointment, com.terapia.terasenior.domain.model.patient.Patient?) -> Unit,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var showEditDialog by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    LaunchedEffect(uiState) {
        if (uiState is AppointmentDetailUiState.Success && (uiState as AppointmentDetailUiState.Success).isDeleted) {
            onBack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Detalle de la Sesión")
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "v1.3.31",
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when (val state = uiState) {
                is AppointmentDetailUiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                is AppointmentDetailUiState.Error -> {
                    Text(state.message, modifier = Modifier.align(Alignment.Center), color = MaterialTheme.colorScheme.error)
                }
                is AppointmentDetailUiState.Success -> {
                    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                        // Info de la Sesión
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f))
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(state.appointment.title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                                    if (state.appointment.status != AppointmentStatus.COMPLETED) {
                                        IconButton(onClick = { showEditDialog = true }) { Icon(Icons.Default.Edit, contentDescription = "Editar", tint = MaterialTheme.colorScheme.primary) }
                                        IconButton(onClick = { showDeleteConfirm = true }) { Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = MaterialTheme.colorScheme.error) }
                                    }
                                }
                                Text(state.appointment.description ?: "Sin descripción", style = MaterialTheme.typography.bodyMedium)
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.AccessTime, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    val startDateTime = kotlinx.datetime.Instant.parse(state.appointment.startAt).toLocalDateTime(TimeZone.currentSystemDefault())
                                    val dateStr = DateUtils.toUserFormat(startDateTime.date.toString())
                                    val statusLabel = when(state.appointment.status) {
                                        AppointmentStatus.SCHEDULED -> "Programada"
                                        AppointmentStatus.CONFIRMED -> "Confirmada"
                                        AppointmentStatus.IN_PROGRESS -> "En curso"
                                        AppointmentStatus.COMPLETED -> "Finalizada"
                                        AppointmentStatus.CANCELLED -> "Cancelada"
                                        AppointmentStatus.MISSED -> "No presentado"
                                    }
                                    Text("Fecha: $dateStr • Estado: $statusLabel", style = MaterialTheme.typography.labelMedium)
                                }

                                if (state.appointment.plannedExercises.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text("Actividades Planificadas:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                                    FlowRow(
                                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        state.appointment.plannedExercises.forEach { config ->
                                            SuggestionChip(
                                                onClick = {},
                                                label = { Text("${ExerciseTranslationUtils.getDisplayName(config.type)} (${ExerciseTranslationUtils.getGdsLabel(config.level)})", fontSize = 11.sp) }
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // ACCIONES CLÍNICAS
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            if (state.appointment.status == AppointmentStatus.SCHEDULED || state.appointment.status == AppointmentStatus.CONFIRMED) {
                                Button(
                                    onClick = { 
                                        // Intentar encontrar el primer paciente de la lista de asistentes
                                        val firstPatientId = state.attendees.firstOrNull()?.patientId
                                        val patient = state.allPatients.find { it.id == firstPatientId }
                                        onStartSession(state.appointment, patient) 
                                    },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(Icons.Default.PlayArrow, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Preparar e Iniciar")
                                }
                                
                                OutlinedButton(
                                    onClick = { viewModel.markAsMissed() },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                                ) {
                                    Icon(Icons.Default.PersonOff, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("No presentado")
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Asistencia de Pacientes", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                            if (state.appointment.status == AppointmentStatus.IN_PROGRESS) {
                                Button(onClick = { viewModel.completeSession() }) {
                                    Text("Finalizar Sesión")
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(state.attendees) { attendee ->
                                AttendeeCard(
                                    attendee = attendee,
                                    onStatusChange = { newStatus -> 
                                        viewModel.updateAttendance(attendee.id, newStatus, attendee.notes) 
                                    }
                                )
                            }
                        }
                    }

                    if (showEditDialog) {
                        EditAppointmentDialog(
                            appointment = state.appointment,
                            attendees = state.attendees,
                            allPatients = state.allPatients,
                            existingAppointments = state.allAppointments,
                            onDismiss = { showEditDialog = false },
                            onConfirm = { updated, pIds -> 
                                viewModel.updateFullSession(updated, pIds)
                                showEditDialog = false
                            },
                            isLoading = state.isSaving
                        )
                    }

                    if (showDeleteConfirm) {
                        AlertDialog(
                            onDismissRequest = { showDeleteConfirm = false },
                            title = { Text("Eliminar Sesión") },
                            text = { Text("¿Estás seguro de que deseas eliminar esta sesión programada? Esta acción no se puede deshacer.") },
                            confirmButton = {
                                Button(onClick = { viewModel.deleteSession(); showDeleteConfirm = false }, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)) {
                                    Text("Eliminar")
                                }
                            },
                            dismissButton = {
                                TextButton(onClick = { showDeleteConfirm = false }) { Text("Cancelar") }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AttendeeCard(
    attendee: AppointmentAttendee,
    onStatusChange: (AttendanceStatus) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(attendee.patientName, fontWeight = FontWeight.Bold)
                Text(
                    text = when(attendee.status) {
                        AttendanceStatus.PENDING -> "Pendiente"
                        AttendanceStatus.PRESENT -> "Presente ✅"
                        AttendanceStatus.ABSENT -> "Ausente ❌"
                    },
                    style = MaterialTheme.typography.labelSmall,
                    color = if(attendee.status == AttendanceStatus.PRESENT) Color(0xFF2E7D32) else Color.Gray
                )
            }

            // Toggles de asistencia
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                IconButton(
                    onClick = { onStatusChange(AttendanceStatus.PRESENT) },
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = if(attendee.status == AttendanceStatus.PRESENT) Color(0xFFC8E6C9) else Color.Transparent
                    )
                ) {
                    Icon(Icons.Default.Check, contentDescription = "Presente", tint = if(attendee.status == AttendanceStatus.PRESENT) Color(0xFF1B5E20) else Color.Gray)
                }
                IconButton(
                    onClick = { onStatusChange(AttendanceStatus.ABSENT) },
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = if(attendee.status == AttendanceStatus.ABSENT) Color(0xFFFFCDD2) else Color.Transparent
                    )
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Ausente", tint = if(attendee.status == AttendanceStatus.ABSENT) Color(0xFFB71C1C) else Color.Gray)
                }
            }
        }
    }
}
