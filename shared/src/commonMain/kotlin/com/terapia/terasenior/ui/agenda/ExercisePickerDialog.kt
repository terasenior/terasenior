package com.terapia.terasenior.ui.agenda

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import com.terapia.terasenior.domain.model.therapy.ExerciseConfig
import com.terapia.terasenior.ui.therapy.SessionPlannerComponent

@Composable
fun ExercisePickerDialog(
    initiallySelected: List<ExerciseConfig>,
    onDismiss: () -> Unit,
    onConfirm: (List<ExerciseConfig>) -> Unit
) {
    val layout = com.terapia.terasenior.ui.components.LocalTeraseniorLayout.current
    val selectedExercises = remember { mutableStateListOf<ExerciseConfig>().apply { addAll(initiallySelected) } }

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier
            .fillMaxWidth(if (layout.isCompactWidth) 0.96f else 0.9f)
            .widthIn(max = 760.dp)
            .heightIn(max = if (layout.isCompactHeight) 620.dp else 720.dp),
        properties = DialogProperties(usePlatformDefaultWidth = false),
        title = { Text("Planificador de Ejercicios", style = MaterialTheme.typography.headlineSmall) },
        text = {
            Box(modifier = Modifier.fillMaxWidth().heightIn(max = if (layout.isCompactHeight) 420.dp else 500.dp)) {
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
