package com.terapia.terasenior.ui.patient

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.terapia.terasenior.domain.model.patient.SupportLevel
import com.terapia.terasenior.domain.model.patient.TherapeuticProfile

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditClinicalProfileDialog(
    profile: TherapeuticProfile?,
    patientId: String,
    onDismiss: () -> Unit,
    onConfirm: (TherapeuticProfile) -> Unit
) {
    var supportLevel by remember { mutableStateOf(profile?.supportLevel ?: SupportLevel.NONE) }
    var dominance by remember { mutableStateOf(profile?.manualDominance ?: "") }
    var preserved by remember { mutableStateOf(profile?.preservedCapacities ?: "") }
    var difficulties by remember { mutableStateOf(profile?.observedDifficulties ?: "") }
    var goals by remember { mutableStateOf(profile?.goals ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Configurar Perfil Clínico", style = MaterialTheme.typography.headlineSmall) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text("Nivel de Apoyo", style = MaterialTheme.typography.labelLarge)
                
                ScrollableTabRow(selectedTabIndex = supportLevel.ordinal, edgePadding = 0.dp) {
                    SupportLevel.entries.forEach { level ->
                        val label = when(level) {
                            SupportLevel.NONE -> "Ninguno"
                            SupportLevel.PUNCTUAL -> "Puntual"
                            SupportLevel.VERBAL -> "Verbal"
                            SupportLevel.VISUAL -> "Visual"
                            SupportLevel.PARTIAL_PHYSICAL -> "Físico Parcial"
                            SupportLevel.FULL_PHYSICAL -> "Físico Total"
                        }
                        Tab(
                            selected = supportLevel == level,
                            onClick = { supportLevel = level },
                            text = { Text(label, style = MaterialTheme.typography.labelSmall) }
                        )
                    }
                }

                OutlinedTextField(
                    value = dominance,
                    onValueChange = { dominance = it },
                    label = { Text("Dominancia Manual") },
                    placeholder = { Text("Ej: Diestra, Zurda...") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = preserved,
                    onValueChange = { preserved = it },
                    label = { Text("Capacidades Preservadas") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = difficulties,
                    onValueChange = { difficulties = it },
                    label = { Text("Dificultades Observadas") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = goals,
                    onValueChange = { goals = it },
                    label = { Text("Objetivos Terapéuticos") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    shape = RoundedCornerShape(12.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { 
                    onConfirm(TherapeuticProfile(
                        patientId = patientId,
                        supportLevel = supportLevel,
                        manualDominance = dominance,
                        preservedCapacities = preserved,
                        observedDifficulties = difficulties,
                        interests = profile?.interests,
                        goals = goals
                    ))
                }
            ) {
                Text("Guardar Perfil")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}
