package com.terapia.terasenior.ui.components

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Información de espacio disponible compartida por las pantallas.
 *
 * Un navegador maximizado en un portátil de 14 pulgadas suele dejar menos de
 * 760 dp de altura útil. Las pantallas usan este indicador para no recortar
 * acciones ni obligar a depender de una ventana concreta.
 */
data class TeraseniorLayout(
    val width: Dp,
    val height: Dp
) {
    val isCompactHeight: Boolean get() = height < 760.dp
    val isCompactWidth: Boolean get() = width < 900.dp
}

val LocalTeraseniorLayout = staticCompositionLocalOf {
    TeraseniorLayout(width = 1280.dp, height = 800.dp)
}

@Composable
fun TeraseniorResponsiveRoot(content: @Composable () -> Unit) {
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        CompositionLocalProvider(
            LocalTeraseniorLayout provides TeraseniorLayout(maxWidth, maxHeight),
            content = content
        )
    }
}
