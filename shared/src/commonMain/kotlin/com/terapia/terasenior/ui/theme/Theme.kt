package com.terapia.terasenior.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Paleta de colores "Terapéutica Moderna"
private val LightColors = lightColorScheme(
    primary = Color(0xFF00668B),      // Azul profundo confiable
    onPrimary = Color.White,
    primaryContainer = Color(0xFFC5E7FF),
    onPrimaryContainer = Color(0xFF001E2D),
    
    secondary = Color(0xFF4D616C),    // Gris azulado calmado
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFD0E6F2),
    onSecondaryContainer = Color(0xFF081E27),
    
    tertiary = Color(0xFF2E7D32),     // Verde salud
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFC8E6C9),
    onTertiaryContainer = Color(0xFF002105),

    error = Color(0xFFBA1A1A),
    background = Color(0xFFFBFCFF),   // Blanco hueso muy suave
    surface = Color(0xFFFBFCFF),
    onSurface = Color(0xFF191C1E),
    onSurfaceVariant = Color(0xFF41484D)
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF7CD0FF),
    onPrimary = Color(0xFF00344A),
    primaryContainer = Color(0xFF004C6A),
    onPrimaryContainer = Color(0xFFC5E7FF),
    
    secondary = Color(0xFFB5CAD6),
    onSecondary = Color(0xFF1F333D),
    secondaryContainer = Color(0xFF354A54),
    onSecondaryContainer = Color(0xFFD0E6F2),
    
    tertiary = Color(0xFF81C784),
    onTertiary = Color(0xFF00390B),
    tertiaryContainer = Color(0xFF005315),
    onTertiaryContainer = Color(0xFFC8E6C9)
)

@Composable
fun TeraseniorTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColors else LightColors

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography(), // Usaremos la por defecto de M3 que es muy legible
        content = content
    )
}
