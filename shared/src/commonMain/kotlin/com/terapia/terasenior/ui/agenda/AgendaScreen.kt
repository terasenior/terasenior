package com.terapia.terasenior.ui.agenda

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.unit.dp
import com.terapia.terasenior.domain.model.agenda.Appointment
import com.terapia.terasenior.domain.model.agenda.AppointmentStatus
import com.terapia.terasenior.domain.model.agenda.AppointmentType
import kotlinx.datetime.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AgendaScreen(
    viewModel: AgendaViewModel,
    onAddAppointmentClick: () -> Unit,
    onAppointmentClick: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mi Agenda", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)) },
                actions = {
                    IconButton(onClick = { viewModel.loadAppointments() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refrescar")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddAppointmentClick,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Nueva Sesión")
            }
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            when (val state = uiState) {
                is AgendaUiState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                is AgendaUiState.Error -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(state.message, color = MaterialTheme.colorScheme.error)
                    }
                }
                is AgendaUiState.Success -> {
                    DateSelector(
                        selectedDate = state.selectedDate,
                        onDateSelected = viewModel::onDateSelected
                    )

                    if (state.filteredAppointments.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.EventBusy, contentDescription = null, modifier = Modifier.size(64.dp), tint = Color.LightGray)
                                Spacer(modifier = Modifier.height(16.dp))
                                Text("No hay sesiones para este día", color = Color.Gray)
                            }
                        }
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(state.filteredAppointments) { appointment ->
                                AppointmentCard(
                                    appointment = appointment,
                                    onClick = { onAppointmentClick(appointment.id) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(kotlin.time.ExperimentalTime::class)
@Composable
private fun DateSelector(
    selectedDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit
) {
    val today = remember {
        kotlin.time.Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
    }
    val days = remember {
        (-3..10).map { today.plus(it, DateTimeUnit.DAY) }
    }

    Column(modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))) {
        LazyRow(
            modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(days) { date ->
                val isSelected = date == selectedDate
                val isToday = date == today
                
                Card(
                    modifier = Modifier.width(60.dp).height(80.dp).clickable { onDateSelected(date) },
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                        contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                    ),
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 4.dp else 1.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        val dayLabel = when(date.dayOfWeek) {
                            DayOfWeek.MONDAY -> "Lun"
                            DayOfWeek.TUESDAY -> "Mar"
                            DayOfWeek.WEDNESDAY -> "Mié"
                            DayOfWeek.THURSDAY -> "Jue"
                            DayOfWeek.FRIDAY -> "Vie"
                            DayOfWeek.SATURDAY -> "Sáb"
                            DayOfWeek.SUNDAY -> "Dom"
                            else -> date.dayOfWeek.name.take(3)
                        }
                        Text(
                            text = dayLabel,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = if (isToday) FontWeight.ExtraBold else FontWeight.Normal
                        )
                        Text(
                            text = date.day.toString(),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        if (isToday) {
                            Box(modifier = Modifier.size(4.dp).clip(CircleShape).background(if(isSelected) Color.White else MaterialTheme.colorScheme.primary))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AppointmentCard(
    appointment: Appointment,
    onClick: () -> Unit
) {
    val statusColor = when(appointment.status) {
        AppointmentStatus.SCHEDULED -> MaterialTheme.colorScheme.primary
        AppointmentStatus.COMPLETED -> Color(0xFF2E7D32)
        AppointmentStatus.CANCELLED -> MaterialTheme.colorScheme.error
    }

    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.width(50.dp)
            ) {
                val startDateTime = kotlinx.datetime.Instant.parse(appointment.startAt).toLocalDateTime(TimeZone.currentSystemDefault())
                val endDateTime = kotlinx.datetime.Instant.parse(appointment.endAt).toLocalDateTime(TimeZone.currentSystemDefault())
                
                Text(
                    text = "${startDateTime.hour.toString().padStart(2, '0')}:${startDateTime.minute.toString().padStart(2, '0')}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${endDateTime.hour.toString().padStart(2, '0')}:${endDateTime.minute.toString().padStart(2, '0')}",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray
                )
            }

            Spacer(modifier = Modifier.width(16.dp))
            Box(modifier = Modifier.width(4.dp).height(50.dp).background(statusColor, RoundedCornerShape(2.dp)))
            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = appointment.title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    maxLines = 1
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val icon = if (appointment.type == AppointmentType.INDIVIDUAL) Icons.Default.Person else Icons.Default.Groups
                    Icon(icon, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color.Gray)
                    Spacer(modifier = Modifier.width(4.dp))
                    val typeLabel = if (appointment.type == AppointmentType.INDIVIDUAL) "Sesión Individual" else "Taller Grupal"
                    Text(
                        text = typeLabel,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
            }

            Surface(
                color = statusColor.copy(alpha = 0.1f),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = when(appointment.status) {
                        AppointmentStatus.SCHEDULED -> "Pte"
                        AppointmentStatus.COMPLETED -> "Ok"
                        AppointmentStatus.CANCELLED -> "X"
                    },
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = statusColor
                )
            }
        }
    }
}
