package com.terapia.terasenior.ui.patient

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AssignmentTurnedIn
import androidx.compose.material.icons.filled.Edit
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
import com.terapia.terasenior.domain.model.patient.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PatientDetailScreen(
    viewModel: PatientDetailViewModel,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Resumen", "Clínico", "Consentimientos")
    
    var showEditPatient by remember { mutableStateOf(false) }
    var showEditClinical by remember { mutableStateOf(false) }

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
                        PatientHeader(state.patient)

                        TabRow(selectedTabIndex = selectedTab) {
                            tabs.forEachIndexed { index, title ->
                                Tab(
                                    selected = selectedTab == index,
                                    onClick = { selectedTab = index },
                                    text = { Text(title) }
                                )
                            }
                        }

                        Box(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
                            when (selectedTab) {
                                0 -> SummaryTab(state.patient, onEdit = { showEditPatient = true })
                                1 -> ClinicalTab(state.therapeuticProfile, onEdit = { showEditClinical = true })
                                2 -> ConsentsTab(state.consents)
                            }
                        }
                    }

                    if (showEditPatient) {
                        EditPatientDialog(
                            patient = state.patient,
                            onDismiss = { showEditPatient = false },
                            onConfirm = { updated ->
                                viewModel.updatePatient(updated)
                                showEditPatient = false
                            },
                            isLoading = state.isUpdating
                        )
                    }

                    if (showEditClinical) {
                        EditClinicalProfileDialog(
                            profile = state.therapeuticProfile,
                            patientId = state.patient.id,
                            onDismiss = { showEditClinical = false },
                            onConfirm = { updated ->
                                viewModel.updateClinicalProfile(updated)
                                showEditClinical = false
                            }
                        )
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
                val statusLabel = when(patient.status) {
                    PatientStatus.ACTIVE -> "Activo"
                    PatientStatus.INACTIVE -> "Inactivo"
                    PatientStatus.DECEASED -> "Fallecido"
                    PatientStatus.DISCHARGED -> "Alta Terapéutica"
                }
                Text("Estado: $statusLabel", style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

@Composable
private fun SummaryTab(patient: Patient, onEdit: () -> Unit) {
    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        InfoCard(title = "Datos Identificativos", icon = Icons.Default.Info, onEdit = onEdit) {
            InfoRow("Nombre completo", patient.fullName)
            InfoRow("Nombre preferido", patient.preferredName ?: "No indicado")
            InfoRow("Fecha de nacimiento", patient.birthDate ?: "No indicada")
            InfoRow("ID Interno", patient.id.take(8).uppercase())
        }
    }
}

@Composable
private fun ClinicalTab(profile: TherapeuticProfile?, onEdit: () -> Unit) {
    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        if (profile == null) {
            Text("Perfil terapéutico no configurado.", style = MaterialTheme.typography.bodyLarge)
            Button(onClick = onEdit) { Text("Configurar Perfil") }
        } else {
            val supportLabel = when(profile.supportLevel) {
                SupportLevel.NONE -> "Sin apoyo"
                SupportLevel.PUNCTUAL -> "Apoyo puntual"
                SupportLevel.VERBAL -> "Apoyo verbal"
                SupportLevel.VISUAL -> "Apoyo visual"
                SupportLevel.PARTIAL_PHYSICAL -> "Apoyo físico parcial"
                SupportLevel.FULL_PHYSICAL -> "Apoyo físico completo"
            }
            InfoCard(title = "Evaluación Terapéutica", icon = Icons.Default.Psychology, onEdit = onEdit) {
                InfoRow("Nivel de apoyo", supportLabel)
                InfoRow("Dominancia manual", profile.manualDominance ?: "No determinada")
                
                Spacer(modifier = Modifier.height(12.dp))
                Text("Objetivos Terapéuticos:", fontWeight = FontWeight.Bold)
                Text(profile.goals ?: "Sin datos", style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

@Composable
private fun ConsentsTab(consents: List<Consent>) {
    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        InfoCard(title = "Gestión de Privacidad", icon = Icons.Default.AssignmentTurnedIn) {
            if (consents.isEmpty()) {
                Text("No hay registros de consentimiento.", style = MaterialTheme.typography.bodyMedium)
            } else {
                consents.forEach { consent ->
                    ConsentRow(consent)
                    if (consent != consents.last()) HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Button(onClick = { }, modifier = Modifier.fillMaxWidth()) {
                Text("Registrar Nuevo Consentimiento")
            }
        }
        
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.3f))
        ) {
            Text(
                text = "🛡️ Esta información es sensible. Cada acceso a esta ficha está siendo registrado en la auditoría de seguridad.",
                modifier = Modifier.padding(16.dp),
                style = MaterialTheme.typography.labelSmall
            )
        }
    }
}

@Composable
private fun ConsentRow(consent: Consent) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Column(modifier = Modifier.weight(1f)) {
            val typeLabel = when(consent.type) {
                ConsentType.RESULTS -> "Resultados de actividades"
                ConsentType.IMAGES -> "Uso de imágenes"
                ConsentType.DATA_EXPORT -> "Exportación de datos"
                ConsentType.THIRD_PARTY -> "Compartir con terceros"
            }
            Text(typeLabel, fontWeight = FontWeight.SemiBold)
            Text("Versión: ${consent.version}", style = MaterialTheme.typography.labelSmall)
        }
        Surface(
            color = when(consent.status) {
                ConsentStatus.ACCEPTED -> Color(0xFFC8E6C9)
                ConsentStatus.REVOKED -> Color(0xFFFFCDD2)
                else -> Color(0xFFF5F5F5)
            },
            shape = RoundedCornerShape(8.dp)
        ) {
            val statusLabel = when(consent.status) {
                ConsentStatus.PENDING -> "Pendiente"
                ConsentStatus.ACCEPTED -> "Aceptado"
                ConsentStatus.REJECTED -> "Rechazado"
                ConsentStatus.REVOKED -> "Revocado"
            }
            Text(
                text = statusLabel,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = if (consent.status == ConsentStatus.ACCEPTED) Color(0xFF1B5E20) else Color.DarkGray
                )
            )
        }
    }
}

@Composable
private fun InfoCard(
    title: String, 
    icon: ImageVector, 
    onEdit: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(title, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                }
                if (onEdit != null) {
                    IconButton(onClick = onEdit) {
                        Icon(Icons.Default.Edit, contentDescription = "Editar", modifier = Modifier.size(20.dp))
                    }
                }
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
