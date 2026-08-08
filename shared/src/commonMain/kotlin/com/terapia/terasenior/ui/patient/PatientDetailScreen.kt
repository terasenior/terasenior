package com.terapia.terasenior.ui.patient

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.terapia.terasenior.domain.model.patient.Patient
import com.terapia.terasenior.domain.model.results.ActivityResult
import com.terapia.terasenior.domain.model.therapy.PatientSessionHistory
import com.terapia.terasenior.ui.therapy.ExerciseTranslationUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PatientDetailScreen(
    viewModel: PatientDetailViewModel,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedTab by remember { mutableStateOf(0) }

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
        when (val state = uiState) {
            is PatientDetailUiState.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            is PatientDetailUiState.Error -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(state.message, color = MaterialTheme.colorScheme.error)
                }
            }
            is PatientDetailUiState.Success -> {
                Column(modifier = Modifier.fillMaxSize().padding(padding)) {
                    PatientHeader(state.patient)
                    
                    PrimaryTabRow(selectedTabIndex = selectedTab) {
                        Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }, text = { Text("Perfil") })
                        Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }, text = { Text("Evolución") })
                        Tab(selected = selectedTab == 2, onClick = { selectedTab = 2 }, text = { Text("Historial") })
                    }

                    Box(modifier = Modifier.weight(1f)) {
                        when (selectedTab) {
                            0 -> PatientInfoTab(state)
                            1 -> PatientEvolutionTab(state)
                            2 -> PatientHistoryTab(state)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PatientHeader(patient: Patient) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(24.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(64.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Text(patient.firstName.take(1), style = MaterialTheme.typography.headlineMedium)
        }
        Spacer(modifier = Modifier.width(20.dp))
        Column {
            Text(patient.fullName, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text("ID: ${patient.id.take(8)}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
        }
    }
}

@Composable
fun PatientHistoryTab(state: PatientDetailUiState.Success) {
    if (state.sessionsHistory.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No hay sesiones registradas todavía.", color = Color.Gray)
        }
    } else {
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(state.sessionsHistory) { history ->
                SessionHistoryCard(history)
            }
        }
    }
}

@Composable
fun SessionHistoryCard(history: PatientSessionHistory) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Event, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = history.session.createdAt.take(10), // Fecha YYYY-MM-DD
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.AccessTime, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color.Gray)
                        Spacer(modifier = Modifier.width(4.dp))
                        val start = history.session.startedAt?.takeLast(8)?.take(5) ?: "--:--"
                        val end = history.session.finishedAt?.takeLast(8)?.take(5) ?: "--:--"
                        Text("$start - $end", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    }
                }
                
                // Badge de Valoración Profesional
                Surface(
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Star, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color(0xFFF57C00))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(history.session.valuation.toString(), style = MaterialTheme.typography.labelSmall)
                    }
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

            // RESULTADOS AGRUPADOS POR CATEGORÍA
            history.groupedByCategory.forEach { (category, results) ->
                CategoryResultGroup(category, results)
                Spacer(modifier = Modifier.height(12.dp))
            }

            if (history.session.therapistNotes?.isNotBlank() == true) {
                Spacer(modifier = Modifier.height(8.dp))
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Observaciones del Terapeuta:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                        Text(history.session.therapistNotes, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }
}

@Composable
fun CategoryResultGroup(category: String, results: List<ActivityResult>) {
    Column {
        Text(
            text = category,
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.primary
        )
        results.forEach { result ->
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = ExerciseTranslationUtils.getDisplayName(result.activityType),
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.weight(1f)
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${result.score}%",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Black),
                        color = if(result.score > 70) Color(0xFF2E7D32) else Color.Red
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "${result.durationSeconds}s",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray
                    )
                }
            }
        }
    }
}

@Composable
fun PatientInfoTab(state: PatientDetailUiState.Success) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState())) {
        Text("Perfil Clínico", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        // ... (resto del perfil clínico existente)
    }
}

@Composable
fun PatientEvolutionTab(state: PatientDetailUiState.Success) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Tendencia de Resultados", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        // ... (gráfica de evolución existente)
    }
}
