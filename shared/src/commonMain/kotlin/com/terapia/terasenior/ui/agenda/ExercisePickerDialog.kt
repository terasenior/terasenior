package com.terapia.terasenior.ui.agenda

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.terapia.terasenior.domain.model.therapy.ExerciseConfig
import com.terapia.terasenior.ui.therapy.SessionPlannerComponent

@Composable
fun ExercisePickerDialog(
    initiallySelected: List<ExerciseConfig>,
    onDismiss: () -> Unit,
    onConfirm: (List<ExerciseConfig>) -> Unit
) {
    val selectedExercises = remember { mutableStateListOf<ExerciseConfig>().apply { addAll(initiallySelected) } }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Planificador de Ejercicios", style = MaterialTheme.typography.headlineSmall) },
        text = {
            Box(modifier = Modifier.fillMaxWidth().heightIn(min = 400.dp, max = 500.dp)) {
                SessionPlannerComponent(
                    plannedExercises = selectedExercises,
                    onAddExercise = { selectedExercises.add(it) },
                    onRemoveExercise = { selectedExercises.removeAt(it) }
                )
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(selectedExercises.toList()) }) {
                Text("Confirmar (${selectedExercises.size})")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        },
        shape = RoundedCornerShape(24.dp)
    )
}
