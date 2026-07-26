package com.terapia.terasenior.treatment.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun TreatmentMenuScreen(
    onNumberSearchClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .statusBarsPadding()
        ) {
            // Cabecera de Bienvenida
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Hola de nuevo,",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Panel de Terapia",
                        style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                
                // Indicador de estado/usuario
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Text("👨‍⚕️", fontSize = 24.sp)
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "Ejercicios Disponibles",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Grid de Actividades
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                ActivityCard(
                    title = "Busca el Número",
                    description = "Entrena tu atención visual encontrando los números indicados.",
                    icon = "🔍",
                    gradient = Brush.linearGradient(listOf(Color(0xFFE3F2FD), Color(0xFFBBDEFB))),
                    onClick = onNumberSearchClick
                )

                ActivityCard(
                    title = "Busca la Letra",
                    description = "Próximamente: Mejora tu reconocimiento léxico.",
                    icon = "🔤",
                    gradient = Brush.linearGradient(listOf(Color(0xFFF1F8E9), Color(0xFFC5E1A5))),
                    onClick = { /* Próximamente */ }
                )
            }
        }
    }
}

@Composable
private fun ActivityCard(
    title: String,
    description: String,
    icon: String,
    gradient: Brush,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .background(gradient)
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icono destacado
            Surface(
                modifier = Modifier.size(64.dp),
                shape = RoundedCornerShape(16.dp),
                color = Color.White.copy(alpha = 0.6f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(icon, fontSize = 32.sp)
                }
            }

            Spacer(modifier = Modifier.width(20.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = Color(0xFF191C1E)
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF41484D),
                    maxLines = 2
                )
            }
            
            Text("→", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
        }
    }
}
