package com.terapia.terasenior.ui.agenda

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.terapia.terasenior.domain.model.patient.Patient

@Composable
fun PatientPickerDialog(
    patients: List<Patient>,
    initiallySelectedIds: List<String>,
    onDismiss: () -> Unit,
    onConfirm: (List<String>) -> Unit
) {
    val selectedIds = remember { mutableStateListOf<String>().apply { addAll(initiallySelectedIds) } }
    var query by remember { mutableStateOf("") }
    
    val filteredPatients = remember(query, patients) {
        if (query.isBlank()) patients
        else patients.filter { it.fullName.contains(query, ignoreCase = true) }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Seleccionar Asistentes", style = MaterialTheme.typography.headlineSmall) },
        text = {
            Column(modifier = Modifier.fillMaxWidth().heightIn(min = 400.dp, max = 500.dp)) {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    placeholder = { Text("Buscar por nombre...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filteredPatients) { patient ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { 
                                    if (selectedIds.contains(patient.id)) selectedIds.remove(patient.id)
                                    else selectedIds.add(patient.id)
                                }
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = selectedIds.contains(patient.id),
                                onCheckedChange = { 
                                    if (it) selectedIds.add(patient.id) else selectedIds.remove(patient.id)
                                }
                            )
                            Text(patient.fullName, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                    if (filteredPatients.isEmpty()) {
                        item {
                            Text("No se encontraron resultados", modifier = Modifier.padding(16.dp), color = Color.Gray)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(selectedIds.toList()) }) {
                Text("Confirmar (${selectedIds.size})")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        },
        shape = RoundedCornerShape(24.dp)
    )
}
