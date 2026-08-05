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
import com.terapia.terasenior.domain.model.agenda.AppointmentStatus
import com.terapia.terasenior.domain.model.agenda.AppointmentAttendee
import com.terapia.terasenior.domain.model.agenda.AttendanceStatus

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppointmentDetailScreen(
    viewModel: AppointmentDetailViewModel,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detalle de la Sesión") },
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
                                Text(state.appointment.title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                                Text(state.appointment.description ?: "Sin descripción", style = MaterialTheme.typography.bodyMedium)
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.AccessTime, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Estado: ${state.appointment.status.name}", style = MaterialTheme.typography.labelMedium)
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
                            if (state.appointment.status == AppointmentStatus.SCHEDULED) {
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
