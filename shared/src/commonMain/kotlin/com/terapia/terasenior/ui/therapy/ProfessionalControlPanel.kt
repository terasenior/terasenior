package com.terapia.terasenior.ui.therapy

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.terapia.terasenior.domain.model.therapy.AssistanceType
import com.terapia.terasenior.domain.model.therapy.IncidentType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfessionalControlPanel(
    onLogAssistance: (AssistanceType) -> Unit,
    onLogIncident: (IncidentType) -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Panel de Control del Terapeuta", style = MaterialTheme.typography.titleLarge)
            
            Text("Registrar Ayuda Proporcionada", style = MaterialTheme.typography.labelLarge)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AssistanceButton("Verbal", { onLogAssistance(AssistanceType.VERBAL) })
                AssistanceButton("Visual", { onLogAssistance(AssistanceType.VISUAL) })
                AssistanceButton("Física", { onLogAssistance(AssistanceType.PHYSICAL) })
            }

            Divider()

            Text("Registrar Incidencia", style = MaterialTheme.typography.labelLarge)
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IncidentChip("Fatiga", { onLogIncident(IncidentType.FATIGUE) })
                IncidentChip("Frustración", { onLogIncident(IncidentType.FRUSTRATION) })
                IncidentChip("Desorientación", { onLogIncident(IncidentType.DISORIENTATION) })
            }
            
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun AssistanceButton(label: String, onClick: () -> Unit) {
    Button(
        onClick = { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer, contentColor = MaterialTheme.colorScheme.onSecondaryContainer)
    ) {
        Text(label)
    }
}

@Composable
private fun IncidentChip(label: String, onClick: () -> Unit) {
    FilterChip(
        selected = false,
        onClick = onClick,
        label = { Text(label) },
        leadingIcon = { Icon(Icons.Default.Warning, contentDescription = null, modifier = Modifier.size(16.dp)) }
    )
}
