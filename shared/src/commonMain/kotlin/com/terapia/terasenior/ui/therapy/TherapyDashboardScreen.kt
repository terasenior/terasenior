package com.terapia.terasenior.ui.therapy

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
            contentPadding = PaddingValues(24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Panel de Trabajo",
                            style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "v1.2.3 • Gestión clínica diaria.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                    }
                    
                    Surface(
                        color = Color(0xFFE8F5E9),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CloudDone, contentDescription = null, tint = Color(0xFF2E7D32), modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Online", style = MaterialTheme.typography.labelSmall, color = Color(0xFF2E7D32))
                        }
                    }
                }
            }

            // SECCIÓN INFORMES (NUEVO)
            item {
                Text("Informes del terapeuta", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(12.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    ReportActionCard(
                        title = "Informes de Sesión",
                        icon = Icons.Default.Description,
                        modifier = Modifier.weight(1f),
                        onClick = { /* Navegar a informes */ }
                    )
                    ReportActionCard(
                        title = "Evolución Grupal",
                        icon = Icons.Default.Insights,
                        modifier = Modifier.weight(1f),
                        onClick = { /* Navegar a evolución grupal */ }
                    )
                }
            }

            // ACCIONES RÁPIDAS
            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Card(
                        onClick = onNewSessionClick,
                        modifier = Modifier.weight(1f).height(100.dp),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Column(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.Center) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.White)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Nueva Sesión", color = Color.White, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                        }
                    }

                    Card(
                        onClick = onGoToAgenda,
                        modifier = Modifier.weight(1f).height(100.dp),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondary)
                    ) {
                        Column(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.Center) {
                            Icon(Icons.Default.Event, contentDescription = null, tint = Color.White)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Agenda", color = Color.White, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // SESIONES DE HOY
            item {
                Text("Sesiones para Hoy", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            }

            if (uiState.todayAppointments.isEmpty()) {
                item {
                    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))) {
                        Text("No hay sesiones programadas para hoy.", modifier = Modifier.padding(16.dp), color = Color.Gray, style = MaterialTheme.typography.bodySmall)
                    }
                }
            } else {
                items(uiState.todayAppointments) { appt ->
                    TodayAppointmentItem(appt, onClick = { onAppointmentClick(appt.id) })
                }
            }

            // PACIENTES DE HOY (Numerados y Accesibles)
            if (uiState.todayPatients.isNotEmpty()) {
                item {
                    Text("Pacientes del Día", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                }
                item {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            uiState.todayPatients.forEachIndexed { index, info ->
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth().clickable { onPatientClick(info.first) }.padding(vertical = 4.dp)
                                ) {
                                    Text(
                                        text = "${index + 1}.", 
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.width(24.dp)
                                    )
                                    Text(info.second, style = MaterialTheme.typography.bodyMedium)
                                    Spacer(modifier = Modifier.weight(1f))
                                    Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.LightGray, modifier = Modifier.size(16.dp))
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
private fun ReportActionCard(title: String, icon: androidx.compose.ui.graphics.vector.ImageVector, modifier: Modifier, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text(title, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
        }
    }
}

@Composable
private fun TodayAppointmentItem(appt: Appointment, onClick: () -> Unit) {
    val start = kotlinx.datetime.Instant.parse(appt.startAt).toLocalDateTime(TimeZone.currentSystemDefault())
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "${start.hour.toString().padStart(2, '0')}:${start.minute.toString().padStart(2, '0')}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(appt.title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                Text(appt.interventionType ?: "Estimulación", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            }
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.LightGray)
        }
    }
}
