package com.terapia.terasenior.ui.reports

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ReportsScreen() {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
            Text(
                text = "Informes del Terapeuta",
                style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "v1.2.5 • Análisis y reportes clínicos.",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 250.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                item {
                    ReportActionCard(
                        title = "Informes de Sesión",
                        description = "Resumen detallado de las actividades realizadas por sesión.",
                        icon = Icons.Default.Description,
                        onClick = { /* Navegar a informes de sesión */ }
                    )
                }
                item {
                    ReportActionCard(
                        title = "Evolución Grupal",
                        description = "Análisis comparativo del rendimiento del grupo de pacientes.",
                        icon = Icons.Default.Insights,
                        onClick = { /* Navegar a evolución grupal */ }
                    )
                }
                item {
                    ReportActionCard(
                        title = "Asistencia Mensual",
                        description = "Control de presencia y ausencias del último mes.",
                        icon = Icons.Default.EventAvailable,
                        onClick = { /* Navegar a informes de asistencia */ }
                    )
                }
                item {
                    ReportActionCard(
                        title = "Resumen de Valoraciones",
                        description = "Estado actual de las valoraciones geriátricas de todos los pacientes.",
                        icon = Icons.Default.AssignmentInd,
                        onClick = { /* Navegar a resumen de valoraciones */ }
                    )
                }
            }
        }
    }
}

@Composable
private fun ReportActionCard(
    title: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().height(160.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        )
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(20.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray,
                textAlign = TextAlign.Center,
                lineHeight = 16.sp
            )
        }
    }
}
