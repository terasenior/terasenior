package com.terapia.terasenior.ui.therapy

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.terapia.terasenior.domain.model.agenda.Appointment
import com.terapia.terasenior.domain.model.therapy.SessionStatus
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

@Composable
fun TherapyDashboardScreen(
    viewModel: TherapyDashboardViewModel,
    therapistId: String,
    onNewSessionClick: () -> Unit,
    onNewPatientClick: () -> Unit,
    onGoToAgenda: () -> Unit,
    onAppointmentClick: (String) -> Unit,
    onPatientClick: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(therapistId) {
        viewModel.loadDashboard(therapistId)
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Panel de Trabajo",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "v1.3.36 • Gestión clínica diaria.",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.Gray
                        )
                    }
                    
                    Surface(
                        color = Color(0xFFE8F5E9),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CloudDone, contentDescription = null, tint = Color(0xFF2E7D32), modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Online", style = MaterialTheme.typography.labelSmall, color = Color(0xFF2E7D32))
                        }
                    }
                }
            }

            // ACCIONES RÁPIDAS
            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    QuickActionCard(title = "Nueva Sesión", icon = Icons.Default.PlayArrow, color = MaterialTheme.colorScheme.primary, modifier = Modifier.weight(1f), onClick = onNewSessionClick)
                    QuickActionCard(title = "Nuevo Paciente", icon = Icons.Default.PersonAdd, color = MaterialTheme.colorScheme.tertiary, modifier = Modifier.weight(1f), onClick = onNewPatientClick)
                    QuickActionCard(title = "Agenda", icon = Icons.Default.Event, color = MaterialTheme.colorScheme.secondary, modifier = Modifier.weight(1f), onClick = onGoToAgenda)
                }
            }

            // DISTRIBUCIÓN EN DOS COLUMNAS
            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                    // COLUMNA IZQUIERDA: SESIONES
                    Column(modifier = Modifier.weight(1.2f), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("Sesiones programadas para hoy", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        
                        if (uiState.todayAppointments.isEmpty()) {
                            Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))) {
                                Text("Sin sesiones hoy.", modifier = Modifier.padding(12.dp), color = Color.Gray, style = MaterialTheme.typography.bodySmall)
                            }
                        } else {
                            uiState.todayAppointments.forEach { pair ->
                                TodayAppointmentItem(
                                    appt = pair.first, 
                                    attendeeNames = pair.second,
                                    onClick = { onAppointmentClick(pair.first.id) }
                                )
                            }
                        }
                    }

                    // COLUMNA DERECHA: PACIENTES
                    Column(modifier = Modifier.weight(0.8f), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("Pacientes del Día", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        
                        if (uiState.todayPatients.isEmpty()) {
                            Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))) {
                                Text("Sin pacientes.", modifier = Modifier.padding(12.dp), color = Color.Gray, style = MaterialTheme.typography.bodySmall)
                            }
                        } else {
                            Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
                                Column(modifier = Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    uiState.todayPatients.forEachIndexed { index, info ->
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.fillMaxWidth().clickable { onPatientClick(info.first) }.padding(8.dp)
                                        ) {
                                            Text(text = "${index + 1}.", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.primary, modifier = Modifier.width(20.dp))
                                            Text(info.second, style = MaterialTheme.typography.bodySmall, maxLines = 1)
                                            Spacer(modifier = Modifier.weight(1f))
                                            Icon(Icons.Default.ChevronRight, null, tint = Color.LightGray, modifier = Modifier.size(14.dp))
                                        }
                                        if (index < uiState.todayPatients.size - 1) HorizontalDivider(modifier = Modifier.padding(horizontal = 8.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun QuickActionCard(title: String, icon: androidx.compose.ui.graphics.vector.ImageVector, color: Color, modifier: Modifier, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = modifier.height(75.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = color)
    ) {
        Column(modifier = Modifier.fillMaxSize().padding(8.dp), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.height(4.dp))
            Text(title, color = Color.White, style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp), fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
        }
    }
}

@Composable
private fun TodayAppointmentItem(appt: Appointment, attendeeNames: List<String>, onClick: () -> Unit) {
    val tz = TimeZone.currentSystemDefault()
    val start = kotlinx.datetime.Instant.parse(appt.startAt).toLocalDateTime(tz)
    val end = kotlinx.datetime.Instant.parse(appt.endAt).toLocalDateTime(tz)
    
    val statusLabel = if (appt.status == com.terapia.terasenior.domain.model.agenda.AppointmentStatus.COMPLETED) "FIN" else "PEND"
    val statusColor = if (appt.status == com.terapia.terasenior.domain.model.agenda.AppointmentStatus.COMPLETED) Color(0xFF2E7D32) else Color(0xFFF57C00)

    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(60.dp)) {
                Text(
                    text = "${start.hour.toString().padStart(2, '0')}:${start.minute.toString().padStart(2, '0')}",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "${end.hour.toString().padStart(2, '0')}:${end.minute.toString().padStart(2, '0')}",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray
                )
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = appt.title, 
                        style = MaterialTheme.typography.bodyMedium, 
                        fontWeight = FontWeight.Bold, 
                        modifier = Modifier.weight(1f),
                        maxLines = 1
                    )
                    
                    Surface(
                        color = statusColor.copy(alpha = 0.12f),
                        shape = RoundedCornerShape(4.dp),
                        border = BorderStroke(1.dp, statusColor.copy(alpha = 0.4f))
                    ) {
                        Text(
                            text = statusLabel,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp),
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black, fontSize = 8.sp),
                            color = statusColor
                        )
                    }
                }
                
                if (attendeeNames.isNotEmpty()) {
                    Text(
                        text = attendeeNames.joinToString(", "),
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray,
                        maxLines = 1,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.LightGray, modifier = Modifier.size(16.dp))
        }
    }
}
