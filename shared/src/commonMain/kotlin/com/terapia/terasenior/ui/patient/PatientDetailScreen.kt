package com.terapia.terasenior.ui.patient

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.terapia.terasenior.domain.model.patient.Patient
import com.terapia.terasenior.domain.model.patient.TherapeuticProfile

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PatientDetailScreen(
    viewModel: PatientDetailViewModel,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Resumen", "Perfil Clínico")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Ficha del Paciente") },
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
                is PatientDetailUiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                is PatientDetailUiState.Error -> {
                    Text(state.message, modifier = Modifier.align(Alignment.Center), color = MaterialTheme.colorScheme.error)
                }
                is PatientDetailUiState.Success -> {
                    Column(modifier = Modifier.fillMaxSize()) {
                        // Cabecera con Info Básica
                        PatientHeader(state.patient)

                        // Pestañas
                        TabRow(selectedTabIndex = selectedTab) {
                            tabs.forEachIndexed { index, title ->
                                Tab(
                                    selected = selectedTab == index,
                                    onClick = { selectedTab = index },
                                    text = { Text(title) }
                                )
                            }
                        }

                        // Contenido según pestaña
                        Box(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
                            when (selectedTab) {
                                0 -> SummaryTab(state.patient)
                                1 -> ClinicalTab(state.therapeuticProfile)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PatientHeader(patient: Patient) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(70.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    patient.firstName.take(1).uppercase(),
                    style = MaterialTheme.typography.headlineLarge,
                    color = Color.White
                )
            }
            Spacer(modifier = Modifier.width(20.dp))
            Column {
                Text(patient.fullName, style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold))
                Text("Estado: ${patient.status.name}", style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

@Composable
private fun SummaryTab(patient: Patient) {
    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        InfoCard(title = "Datos Identificativos", icon = Icons.Default.Info) {
            InfoRow("Nombre completo", patient.fullName)
            InfoRow("Nombre preferido", patient.preferredName ?: "No indicado")
            InfoRow("Fecha de nacimiento", patient.birthDate ?: "No indicada")
            InfoRow("Fecha de alta", patient.createdAt.take(10))
        }
    }
}

@Composable
private fun ClinicalTab(profile: TherapeuticProfile?) {
    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        if (profile == null) {
            Text("No se ha configurado el perfil terapéutico todavía.", style = MaterialTheme.typography.bodyLarge)
            Button(onClick = { /* Próximamente Editar */ }) {
                Text("Configurar Perfil")
            }
        } else {
            InfoCard(title = "Evaluación Terapéutica", icon = Icons.Default.Psychology) {
                InfoRow("Nivel de apoyo", profile.supportLevel.name)
                InfoRow("Dominancia manual", profile.manualDominance ?: "No determinada")
                
                Spacer(modifier = Modifier.height(8.dp))
                Text("Capacidades Preservadas:", fontWeight = FontWeight.Bold)
                Text(profile.preservedCapacities ?: "Sin datos", style = MaterialTheme.typography.bodyMedium)
                
                Spacer(modifier = Modifier.height(8.dp))
                Text("Dificultades Observadas:", fontWeight = FontWeight.Bold)
                Text(profile.observedDifficulties ?: "Sin datos", style = MaterialTheme.typography.bodyMedium)

                Spacer(modifier = Modifier.height(8.dp))
                Text("Objetivos Terapéuticos:", fontWeight = FontWeight.Bold)
                Text(profile.goals ?: "Sin datos", style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

@Composable
private fun InfoCard(title: String, icon: ImageVector, content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(8.dp))
                Text(title, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
            }
            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
            content()
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold))
    }
}
